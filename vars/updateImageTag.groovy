def call(Map config = [:]) {
    def imageTag = config.imageTag ?: "${env.COMMIT_SHA}-${env.BUILD_NUMBER}"
    def branch = 'main'
    def secretName = config.secretName ?: error("Missing 'secretName'")
    def repoUrl = "https://github.com/stackcouture/Java-WebAPP-CD"
    def repoDir = "Java-WebAPP-CD"

    // Verify the image is signed before proceeding
    def isSigned = cosignVerifyECR(
        imageTag: imageTag,
        ecrRepoName: params.ECR_REPO_NAME,
        region: params.REGION,
        awsAccountId: params.AWS_ACCOUNT_ID
    )

    if (!isSigned) {
        error "Image ${imageTag} is not signed. Skipping deployment."
    }

    // Fetch the image digest from ECR if the image is signed
    def imageDigest = sh(script: """
        aws ecr describe-images --repository-name ${params.ECR_REPO_NAME} --image-ids imageTag=${imageTag} --region ${params.REGION} --query 'imageDetails[0].imageDigest' --output text
    """, returnStdout: true).trim()

    echo "Image digest for ${imageTag} is: ${imageDigest}"

    def secrets = getAwsSecret(secretName, 'ap-south-1')
    def gitToken = secrets.github_pat

    sh "rm -rf ${repoDir}"

    withEnv(["GIT_PAT=${gitToken}"]) {
        retry(2) {
            sh "git clone https://\$GIT_PAT@${repoUrl.replace('https://', '')} ${repoDir}"
        }
    }

    dir(repoDir) {
        sh "git checkout ${branch}"

        if (!fileExists("deploy/dev-values.yaml")) {
            error("File deploy/dev-values.yaml not found in branch ${branch}")
        }

        def currentTag = sh(
            script: "grep '^ *tag:' deploy/dev-values.yaml | awk '{print \$2}'",
            returnStdout: true
        ).trim()

        echo "Current tag in values.yaml: ${currentTag}"

        if (currentTag == imageDigest) {
            echo "Image tag is already up to date — skipping update and commit."
            return
        }

        echo "Updating image tag from ${currentTag} to ${imageDigest}"
        sh "sed -i 's/^ *tag:.*/tag: ${imageDigest}/' deploy/dev-values.yaml"

        sh 'git config user.email "stackcouture@gmail.com"'
        sh 'git config user.name "Naveen"'
        sh "git add deploy/dev-values.yaml"
        sh "git commit -m \"chore: update image tag to ${imageDigest}\""

        try {
            sh "git push origin ${branch}"
            echo "Tag update pushed with Image Tag: ${imageDigest}"
        } catch (e) {
            error("Failed to push tag update. Reason:\n${e.message}")
        }
    }
}

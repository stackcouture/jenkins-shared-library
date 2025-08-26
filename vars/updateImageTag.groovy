def call(Map config = [:]) {
    def imageTag = config.imageTag ?: "${env.COMMIT_SHA}"
    def branch = 'main'
    def secretName = config.secretName ?: error("Missing 'secretName'")
    def repoUrl = "https://github.com/stackcouture/Java-WebAPP-CD"
    def repoDir = "Java-WebAPP-CD"
    def ecrRepoName = config.ecrRepoName
    def region = config.region
    def cosignPassword = config.cosignPassword
    def awsAccountId = config.awsAccountId

    // Ensure imageTag is not null or empty
    if (!imageTag) {
        error "Missing 'imageTag'. Please provide a valid imageTag."
    }

    // // Verify the image is signed before proceeding
    // def isSigned = cosignVerifyECR(
    //     imageTag: imageTag,
    //     ecrRepoName: ecrRepoName,
    //     region: region,
    //     awsAccountId: awsAccountId
    // )

    // if (!isSigned) {
    //     error "Image ${imageTag} is not signed. Skipping deployment."
    // }

    // Fetch the image digest from ECR if the image is signed
    def imageDigest = sh(script: """
        aws ecr describe-images --repository-name ${ecrRepoName} --image-ids imageTag=${imageTag} --region ${region} --query 'imageDetails[0].imageDigest' --output text
    """, returnStdout: true).trim()

    if (!imageDigest) {
        error "Could not fetch image digest for tag ${imageTag} in repository ${ecrRepoName}."
    }

    echo "Image digest for ${imageTag} is: ${imageDigest}"

    def secrets = getAwsSecret(secretName, 'ap-south-1')
    def gitToken = secrets.github_pat

    sh "rm -rf ${repoDir}"

    withEnv(["GIT_PAT=${gitToken}"]) {
        retry(3) {
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
        
         // Fix indentation while replacing the 'tag'
        sh """
          sed -i '/^ *tag:/s/^ *tag:.*/  tag: ${imageDigest}/' deploy/dev-values.yaml
        """

        // Commit the changes to Git
        sh 'git config user.email "stackcouture@gmail.com"'
        sh 'git config user.name "Naveen"'
        sh "git add deploy/dev-values.yaml"
        sh "git commit -m \"chore: update image tag to ${imageTag}\""

        try {
            // Push the changes back to the repository
            sh "git push origin ${branch}"
            echo "Tag update pushed with Image Tag: ${imageTag}"
        } catch (Exception e) {
            error("Failed to push tag update. Reason:\n${e.message}")
        }
    }
}

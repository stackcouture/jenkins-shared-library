def call(Map config = [:]) {
    def imageTag    = config.imageTag    ?: error("Missing 'imageTag'")
    def ecrRepoName = config.ecrRepoName ?: error("Missing 'ecrRepoName'")
    def awsAccountId= config.awsAccountId ?: error("Missing 'awsAccountId'")
    def region      = config.region      ?: error("Missing 'region'")

    def fullTag = "${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${ecrRepoName}:${imageTag}"

    echo "Tagging image ${ecrRepoName}:${imageTag} → ${fullTag}"
    sh "docker tag ${ecrRepoName}:${imageTag} ${fullTag}"

    echo "Pushing Docker image to ECR..."
    sh "docker push ${fullTag}"

    echo "Retrieving image digest for digest-based reference..."
    def digest = sh(
        script: """
            aws ecr describe-images \
                --repository-name ${ecrRepoName} \
                --image-ids imageTag=${imageTag} \
                --region ${region} \
                --query 'imageDetails[0].imageDigest' \
                --output text
        """,
        returnStdout: true
    ).trim()

    def fullDigestTag = "${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${ecrRepoName}@${digest}"
    echo "Digest-based tag: ${fullDigestTag}"

    return fullDigestTag
}

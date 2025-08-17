def call(Map config = [:]) {

    def imageTag = config.imageTag ?: error("Missing 'imageTag'")
    def ecrRepoName = config.ecrRepoName ?: error("Missing 'ecrRepoName'")
    def awsAccountId = config.awsAccountId ?: error("Missing 'awsAccountId'")
    def region = config.region ?: error("Missing 'region'")

    def fullTag = "${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${ecrRepoName}:${imageTag}"

    sh """  
        docker tag ${ecrRepoName}:${imageTag} ${fullTag}
        docker push ${fullTag}
    """

    // Fetch digest for digest-based signing
    def digest = sh(script: """
        aws ecr describe-images --repository-name ${ecrRepoName} --image-ids imageTag=${imageTag} --query 'imageDetails[0].imageDigest' --output text
    """, returnStdout: true).trim()

    return "${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${ecrRepoName}@${digest}"
}

def call(Map config = [:]) {

    def imageTag = config.imageTag ?: error("Missing 'imageTag'")
    def ecrRepoName = config.ecrRepoName ?: error("Missing 'ecrRepoName'")
    def awsAccountId = config.awsAccountId ?: error("Missing 'awsAccountId'")
    def region = config.region ?: error("Missing 'region'")

    def fullTag = "${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${imageTag}"

    sh """  
        docker tag ${imageTag} ${fullTag}
        docker push ${fullTag}
    """
}
def call(Map config = [:]) {
    def ecrRepoName = config.ecrRepoName
    def imageTag = config.imageTag
    def region = config.region
    
    def command = """
        aws ecr batch-get-image \
            --repository-name ${ecrRepoName} \
            --image-ids imageTag=${imageTag} \
            --region ${region} \
            --query 'images[0].imageId.imageDigest' \
            --output text
    """
    def digest = sh(script: command, returnStdout: true).trim()
    
    if (digest == '') {
        error "Failed to get the image digest for image: ${imageTag} in repo: ${ecrRepoName}"
    }

    return digest
}

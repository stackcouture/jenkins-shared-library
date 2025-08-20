def getImageDigest(Map params) {
    def ecrRepoName = params.ecrRepoName
    def imageTag = params.imageTag
    def region = params.region

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

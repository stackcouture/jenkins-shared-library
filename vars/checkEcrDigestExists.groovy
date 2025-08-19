def call(String repositoryName, String imageTag, String awsAccountId, String region) {
    def fullImageName = "${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${repositoryName}"
    echo "Checking if ECR image exists: ${fullImageName}:${imageTag}"

    try {
        def output = sh(
            script: """
                aws ecr describe-images \
                    --repository-name ${repositoryName} \
                    --image-ids imageTag=${imageTag} \
                    --region ${region} \
                    --output json
            """,
            returnStdout: true
        ).trim()

        def json = readJSON text: output

        def imageDigest = json?.imageDetails?.getAt(0)?.imageDigest

        if (imageDigest) {
            echo "Image exists. Digest: ${imageDigest}"
            return imageDigest
        } else {
            echo "Image tag found but no digest returned."
            return null
        }
    } catch (Exception e) {
        echo "Image not found or error occurred: ${e.getMessage()}"
        return null
    }
}

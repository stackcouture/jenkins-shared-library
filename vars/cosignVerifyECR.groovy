def cosignVerifyECR(Map config = [:]) {
    def imageTag = config.imageTag
    def ecrRepoName = config.ecrRepoName
    def region = config.region
    def awsAccountId = config.awsAccountId

    // Ensure COSIGN credentials are available
    withCredentials([file(credentialsId: 'cosign-public-key', variable: 'COSIGN_PUBLIC_KEY')]) {
        script {
            // Get the image digest from ECR
            def imageDigest = sh(script: """
                aws ecr describe-images --repository-name ${ecrRepoName} --image-ids imageTag=${imageTag} --region ${region} --query 'imageDetails[0].imageDigest' --output text
            """, returnStdout: true).trim()

            // Construct the image reference
            def imageRef = "${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${ecrRepoName}@${imageDigest}"

            // Verify the image using Cosign
            def isSigned = sh(script: """
                COSIGN_EXPERIMENTAL=1 cosign verify --key $COSIGN_PUBLIC_KEY ${imageRef} > /dev/null 2>&1
            """, returnStatus: true) == 0

            // Return whether the image is signed or not
            return isSigned
        }
    }
}

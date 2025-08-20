def call(Map config = [:]) {
    def imageTag = config.imageTag
    def ecrRepoName = config.ecrRepoName
    def region = config.region
    def cosignPassword = config.cosignPassword
    def awsAccountId = config.awsAccountId

    withCredentials([file(credentialsId: 'cosign-private-key', variable: 'COSIGN_KEY'),
                     file(credentialsId: 'cosign-public-key', variable: 'COSIGN_PUBLIC_KEY')]) {
        script {
            
            def isSigned = cosignVerifyECR(
                imageTag: imageTag,
                ecrRepoName: ecrRepoName,
                region: region,
                awsAccountId: awsAccountId
            )

            if (!isSigned) {
                echo "Image is not signed. Proceeding with signing..."

                def imageDigest = sh(script: """
                    aws ecr describe-images --repository-name ${ecrRepoName} --image-ids imageTag=${imageTag} --region ${region} --query 'imageDetails[0].imageDigest' --output text
                """, returnStdout: true).trim()

                if (imageDigest == "") {
                    error("Failed to retrieve image digest from ECR for imageTag: ${imageTag}")
                }

                def imageRef = "${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${ecrRepoName}@${imageDigest}"

                try {
                    echo "Signing image ${imageRef}..."

                    retry(3) {
                        sh """
                            export COSIGN_PASSWORD=${cosignPassword}
                            cosign sign --key $COSIGN_KEY --upload --yes ${imageRef}
                        """
                    }

                    echo "Image signed successfully."
                } catch (Exception e) {
                    error("Cosign signing failed after 3 attempts: ${e.message}")
                }
            } else {
                echo "Image is already signed. Skipping signing process."
            }
        }
    }
}

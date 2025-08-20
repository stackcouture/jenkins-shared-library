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

            // If the image is not signed, sign it
            if (!isSigned) {
                echo "Image is not signed. Proceeding with signing..."
                def imageDigest = sh(script: """
                    aws ecr describe-images --repository-name ${ecrRepoName} --image-ids imageTag=${imageTag} --region ${region} --query 'imageDetails[0].imageDigest' --output text
                """, returnStdout: true).trim()

                def imageRef = "${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${ecrRepoName}@${imageDigest}"

                // Sign the image using Cosign
                sh """
                    export COSIGN_PASSWORD=${cosignPassword}
                    cosign sign --key $COSIGN_KEY --upload --yes ${imageRef}
                """
                echo "Image signed successfully."
            } else {
                echo "Image is already signed. Skipping signing process."
            }
        }
    }
}

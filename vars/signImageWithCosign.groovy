def call(Map config = [:]) {
    def imageTag = config.imageTag    
    def ecrRepoName = config.ecrRepoName 
    def region = config.region 
    def cosignPassword = config.cosignPassword 
    def awsAccountId = config.awsAccountId 

    withCredentials([file(credentialsId: 'cosign-private-key', variable: 'COSIGN_KEY')]) {
        script {
            def imageDigest = sh(script: """
                aws ecr describe-images --repository-name ${ecrRepoName} --image-ids imageTag=${imageTag} --region ${region} --query 'imageDetails[0].imageDigest' --output text
            """, returnStdout: true).trim()

            def imageRef = "${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${ecrRepoName}@${imageDigest}"

            echo "Signing Docker image with Cosign: ${imageRef}"

            // Sign the image with Cosign using the private key stored in Jenkins credentials
            sh """
                export COSIGN_PASSWORD=${cosignPassword}
                cosign sign --key $COSIGN_KEY --upload --yes ${imageRef}
            """
            // Store the image digest for further use
            env.ECR_IMAGE_DIGEST = imageDigest
        }
    }
}

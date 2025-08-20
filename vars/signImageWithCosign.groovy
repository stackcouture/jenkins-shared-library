def call(Map config = [:]) {
    def imageTag = config.imageTag    
    def ecrRepoName = config.ecrRepoName 
    def region = config.region 
    def cosignPassword = config.cosignPassword 
    def awsAccountId = config.awsAccountId 

    withCredentials([file(credentialsId: 'cosign-private-key', variable: 'COSIGN_KEY'),
                    file(credentialsId: 'cosign-public-key', variable: 'COSIGN_PUBLIC_KEY')]) {
        script {
            def imageDigest = sh(script: """
                aws ecr describe-images --repository-name ${ecrRepoName} --image-ids imageTag=${imageTag} --region ${region} --query 'imageDetails[0].imageDigest' --output text
            """, returnStdout: true).trim()

            def imageRef = "${awsAccountId}.dkr.ecr.${region}.amazonaws.com/${ecrRepoName}@${imageDigest}"
            // Check if image is already signed using the public key
            def isSigned = sh(script: """
                COSIGN_EXPERIMENTAL=1 cosign verify --key $COSIGN_PUBLIC_KEY ${imageRef} > /dev/null 2>&1
            """, returnStatus: true) == 0

            if (isSigned) {
                echo "Image ${imageRef} is already signed. Skipping signing."
            } else {
                echo "Image ${imageRef} is not signed yet. Signing now..."
                sh """
                    export COSIGN_PASSWORD=${COSIGN_PASSWORD}
                    cosign sign --key $COSIGN_KEY --upload --yes ${imageRef}
                """
            }
            env.ECR_IMAGE_DIGEST = imageDigest
        }
    }
}

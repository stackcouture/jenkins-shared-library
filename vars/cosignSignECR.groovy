def cosignSignECR(Map config = [:]) {
    def fullDigestTag = config.fullDigestTag ?: error("Missing 'fullDigestTag'")
    def awsAccountId = config.awsAccountId ?: error("Missing 'awsAccountId'")
    def region = config.region ?: error("Missing 'region'")

    echo "Signing image with Cosign: ${fullDigestTag}"

    sh """
        export COSIGN_EXPERIMENTAL=1
        export COSIGN_PASSWORD=\$COSIGN_PASSWORD

        # Authenticate to ECR
        aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${awsAccountId}.dkr.ecr.${region}.amazonaws.com

        # Sign the image digest
        cosign sign --key \$COSIGN_KEY ${fullDigestTag}
    """
}
def call(Map config = [:]) {
    def fullDigestTag = config.fullDigestTag ?: error("Missing 'fullDigestTag'")
    
    echo "Signing image with Cosign: ${fullDigestTag}"

    // Use Jenkins credentials for private key + password
    withEnv(["COSIGN_PASSWORD=${env.COSIGN_PASSWORD}"]) {
        sh """
            cosign sign \
                --key ${env.COSIGN_KEY} \
                ${fullDigestTag}
        """
    }

    echo "Cosign signing completed for ${fullDigestTag}"
}

def call(Map config = [:]) {
    def fullDigestTag = config.fullDigestTag ?: error("Missing 'fullDigestTag'")

    echo "Verifying Cosign signature for image: ${fullDigestTag}"

    // Use Jenkins credentials for public key
    withEnv([]) {
        sh """
            cosign verify \
                --key ${env.COSIGN_PUB} \
                ${fullDigestTag}
        """
    }

    echo "Cosign verification completed for ${fullDigestTag}"
}

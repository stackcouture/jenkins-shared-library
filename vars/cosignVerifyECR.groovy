def cosignVerifyECR(Map config = [:]) {
    def fullDigestTag = config.fullDigestTag ?: error("Missing 'fullDigestTag'")

    echo "Verifying Cosign signature for image: ${fullDigestTag}"

    sh """
        export COSIGN_EXPERIMENTAL=1
        cosign verify --key \$COSIGN_PUB ${fullDigestTag}
    """
}
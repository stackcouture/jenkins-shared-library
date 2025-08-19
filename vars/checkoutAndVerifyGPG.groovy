def call(Map config = [:]) {
    def branch = config.branch ?: 'main'
    def gitUrl = config.gitUrl ?: error("Missing gitUrl")
    def commitSha = config.commitSha
    def gpgCredentialsId = config.gpgCredentialsId ?: error("Missing gpgCredentialsId")
    def secretPath = config.secretPath // you might want to use it in checkoutGit, assuming it is used there

    withCredentials([file(credentialsId: gpgCredentialsId, variable: 'GPG_KEY_FILE')]) {
        // Setup isolated GPG home directory to avoid contaminating user's keyring
        def gpgHome = "${pwd()}/.gnupg"
        sh """
            mkdir -p ${gpgHome}
            chmod 700 ${gpgHome}
        """

        try {
            echo "Importing GPG key to isolated keyring..."
            sh """
                export GNUPGHOME=${gpgHome}
                gpg --batch --import "$GPG_KEY_FILE"
            """

            echo "Starting Git checkout..."
            // Assuming checkoutGit supports branch, gitUrl and secretPath
            checkoutGit(branch, gitUrl, secretPath)

            if (commitSha) {
                echo "Checking out specific commit ${commitSha}..."
                sh "git checkout ${commitSha}"
            }

            // Use full commit SHA, don't truncate
            def resolvedCommit = commitSha ?: sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
            env.COMMIT_SHA = resolvedCommit

            echo "Checked out commit: ${resolvedCommit}"

            // Verify commit signature with isolated GPG home
            def verifyStatus = sh(
                script: """export GNUPGHOME=${gpgHome}
                           git verify-commit ${resolvedCommit}""",
                returnStatus: true
            )

            if (verifyStatus != 0) {
                error "GPG signature verification failed for commit ${resolvedCommit}!"
            } else {
                echo "GPG signature verification passed for commit ${resolvedCommit}."
            }

            // Get signature key info (using full SHA)
            def commitKey = sh(
                script: """git log -1 --format="%Gg" ${resolvedCommit}""",
                returnStdout: true
            ).trim()

            echo "Commit signed by GPG key: ${commitKey}"

        } catch (Exception e) {
            currentBuild.result = 'FAILURE'
            echo "Git checkout or verification failed: ${e.message}"
            error("Stopping pipeline due to checkout failure")
        } finally {
            echo "Cleaning up isolated GPG home..."
            sh "rm -rf ${gpgHome}"
        }
    }
}

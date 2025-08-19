def call(Map config = [:]) {
    def branch = config.branch
    def gitUrl = config.gitUrl
    def commitSha = config.commitSha
    def gpgCredentialsId = config.gpgCredentialsId
    def secretPath = config.secretPath

    withCredentials([file(credentialsId: gpgCredentialsId, variable: 'GPG_KEY1')]) {

        echo "Importing GPG key..."
        // Fix quoting so shell expands the variable properly
        sh "gpg --batch --import $GPG_KEY1"

        try {
            echo "Starting Git checkout..."
            // Assuming checkoutGit handles git clone/fetch + checkout branch + credentials
            checkoutGit(branch, gitUrl, secretPath)

            // If commitSha provided, checkout that commit explicitly
            if (commitSha) {
                echo "Checking out specific commit ${commitSha}"
                sh "git checkout ${commitSha}"
            }

            def resolvedCommit = commitSha ?: sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
            env.COMMIT_SHA = resolvedCommit

            echo "Checked out commit: ${env.COMMIT_SHA}"

            def verifyStatus = sh(
                script: "git verify-commit ${env.COMMIT_SHA}",
                returnStatus: true
            )

            if (verifyStatus != 0) {
                error "GPG signature verification failed for commit ${env.COMMIT_SHA}!"
            } else {
                echo "GPG signature verification passed."
            }

            def commitKey = sh(
                script: "git log -1 --format='%Gg' ${env.COMMIT_SHA}",
                returnStdout: true
            ).trim()

            echo "Commit signed by GPG key: ${commitKey}"

        } catch (Exception e) {
            currentBuild.result = 'FAILURE'
            echo "Git checkout or verification failed: ${e.message}"
            error("Stopping pipeline due to checkout failure")
        } finally {
            echo "Cleaning up imported GPG key..."
            // Fix quoting here as well
            sh "rm -f $GPG_KEY1"
        }
    }
}

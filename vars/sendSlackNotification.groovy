def call(Map config = [:]) {
    def color = config.color ?: '#36a64f'
    def status = (config.status ?: error("Missing 'status'")).toUpperCase()
    def secretName = config.secretName ?: error("Missing 'secretName'")
    def slackChannel = config.channel ?: "#app-demo" // You can override this via config

    // Additional optional info for Gitleaks
    def leakCount = config.leakCount ?: 0
    def reportUrl = config.reportUrl ?: ""

    // Get secrets from AWS
    def secrets
    try {
        secrets = getAwsSecret(secretName, 'ap-south-1')
    } catch (e) {
        error("Failed to retrieve AWS secret '${secretName}': ${e.message}")
    }

    def slackToken = secrets.slack_bot_token ?: error("Missing 'slack_bot_token' in secrets '${secretName}'")

    def emojiMap = [
        SUCCESS : "✅ Deployment Successful!",
        FAILURE : "❌ Deployment Failed!",
        UNSTABLE: "⚠️ Unstable Deployment!",
        ABORTED : "🛑 Deployment Aborted!"
    ]

    // Custom message for Gitleaks scan
    def customMessage = ""
    if (config.isGitleaksNotification == true) {
        if (leakCount == 0) {
            customMessage = "✅ *Gitleaks Scan Result:* No secrets found in the scanned commit."
        } else {
            customMessage = "⚠️ *Gitleaks Scan Result:* Found *${leakCount}* potential secret(s)!\n" +
                            "*Report:* <${reportUrl}|Click here to view>"
            // Override status and color for Gitleaks
            status = leakCount > 0 ? 'FAILURE' : 'SUCCESS'
            color = leakCount > 0 ? 'danger' : 'good'
        }
    }

    // Wrap to access BUILD_USER info
    wrap([$class: 'BuildUser']) {
        def triggeredBy = env.BUILD_USER_ID ?: "Automated"
        def commitSha = env.COMMIT_SHA ?: "N/A"
        def buildUrl = env.BUILD_URL ?: "#"
        def jobName = env.JOB_NAME ?: "N/A"
        def buildNumber = env.BUILD_NUMBER ?: "N/A"
        def branch = params.BRANCH ?: env.GIT_BRANCH ?: "N/A"

        // Build message explicitly to ensure customMessage is appended properly
        def slackMessage = """\
            *${emojiMap[status] ?: status}*
            *Project:* `${jobName}`
            *Commit:* `${commitSha}`
            *Build Number:* #${buildNumber}
            *Branch:* `${branch}`
            *Triggered By:* ${triggeredBy} 👤
            *Build Link:* <${buildUrl}|Click to view in Jenkins>
        """.stripIndent()

        if (customMessage) {
            slackMessage += "\n${customMessage}"
        }

        slackMessage += "\n_This is an automated notification from Jenkins 🤖_"

        slackSend(
            channel: slackChannel,
            token: slackToken,
            color: color,
            message: slackMessage
        )
    }
}

def call(Map config = [:]) {
    def baseColor = config.color ?: '#36a64f'
    def baseStatus = (config.status ?: error("Missing 'status'")).toUpperCase()
    def secretName = config.secretName ?: error("Missing 'secretName'")
    def slackChannel = config.channel ?: "#app-demo"

    def leakCount = config.leakCount ?: 0
    def reportUrl = config.reportUrl ?: ""
    def isGitleaks = config.isGitleaksNotification?.toString()?.toLowerCase() == 'true'

    def effectiveStatus = baseStatus
    def effectiveColor = baseColor
    def customMessage = ""
    def isGitleaksOnly = false

    echo "[DEBUG] isGitleaksNotification (raw): ${config.isGitleaksNotification}"
    echo "[DEBUG] isGitleaks (parsed): ${isGitleaks}"
    echo "[DEBUG] leakCount: ${leakCount}"
    echo "[DEBUG] reportUrl: ${reportUrl}"

    // Retrieve Slack token from AWS Secrets Manager
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

    // Gitleaks-specific handling
    if (isGitleaks) {
        isGitleaksOnly = true
        if (leakCount == 0) {
            customMessage = "✅ *Gitleaks Scan Result:* No secrets found in the scanned commit."
            effectiveStatus = 'SUCCESS'
            effectiveColor = 'good'
        } else {
            customMessage = "⚠️ *Gitleaks Scan Result:* Found *${leakCount}* potential secret(s)!\n" +
                            "*Report:* <${reportUrl}|Click here to view>"
            effectiveStatus = 'FAILURE'
            effectiveColor = 'danger'
        }
    }

    wrap([$class: 'BuildUser']) {
        def triggeredBy = env.BUILD_USER_ID ?: "Automated"
        def commitSha = env.COMMIT_SHA ?: "N/A"
        def buildUrl = env.BUILD_URL ?: "#"
        def jobName = env.JOB_NAME ?: "N/A"
        def buildNumber = env.BUILD_NUMBER ?: "N/A"
        def branch = params.BRANCH ?: env.GIT_BRANCH ?: "N/A"

        def slackMessage = ""

        if (isGitleaksOnly) {
            slackMessage = customMessage + "\n"
        } else {
            slackMessage = """\
*${emojiMap[effectiveStatus] ?: effectiveStatus}*
*Project:* `${jobName}`
*Commit:* `${commitSha}`
*Build Number:* #${buildNumber}
*Branch:* `${branch}`
*Triggered By:* ${triggeredBy} 👤
*Build Link:* <${buildUrl}|Click to view in Jenkins>
""".stripIndent()

            if (customMessage?.trim()) {
                slackMessage += "\n${customMessage}"
            }
        }

        slackMessage += "\n_This is an automated notification from Jenkins 🤖_"

        slackSend(
            channel: slackChannel,
            token: slackToken,
            color: effectiveColor,
            message: slackMessage
        )
    }
}

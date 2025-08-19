// vars/checkoutGit.groovy
def call(String gitBranch, String gitUrl, String secretName) {
    echo "Fetching AWS secrets..."
    def secrets = getAwsSecret(secretName, 'ap-south-1')

    if (secrets == null) {
        error("Failed to retrieve secrets from AWS for path: ${secretName}")
    }
    if (!secrets.github_pat) {
        error("GitHub PAT not found in AWS secrets at path: ${secretName}")
    }

    def credentialsId = secrets.github_pat

    echo "Checking out branch '${gitBranch}' from '${gitUrl}' using provided credentials."

    checkout([
        $class: 'GitSCM',
        branches: [[name: "*/${gitBranch}"]],
        userRemoteConfigs: [[url: gitUrl, credentialsId: credentialsId]]
    ])
}

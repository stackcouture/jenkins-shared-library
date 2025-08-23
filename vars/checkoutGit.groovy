def call(String gitBranch, String gitUrl, String secretName) {
    
    withCredentials([usernamePassword(credentialsId: 'github-credentials-id', usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_PAT')]) {
        echo "Using GitHub PAT for git operations"
        sh 'rm -rf source'
        dir('source') {
            checkout([
                $class: 'GitSCM',
                branches: [[name: "*/${gitBranch}"]],
                userRemoteConfigs: [[
                    url: gitUrl,
                    credentialsId: 'github-credentials-id'
                ]]
            ])
        }
    }
}

def call(String secretName, String region = 'ap-south-1') {
    try {
        // Fetch secret from AWS Secrets Manager
        def secretJson = sh(
            script: """#!/bin/bash
                aws secretsmanager get-secret-value \
                    --secret-id '${secretName}' \
                    --region '${region}' \
                    --query SecretString \
                    --output text
            """,
            returnStdout: true,
            label: 'Fetching secret'
        ).trim()

        // Check if the secret was fetched successfully
        if (!secretJson) {
            error("Failed to fetch secret: ${secretName} from AWS Secrets Manager.")
        }

        // Parse the JSON response
        def secret = readJSON(text: secretJson)
        return secret

    } catch (Exception e) {
        // Catch any exceptions during secret retrieval or JSON parsing
        error("Error fetching or parsing secret '${secretName}': ${e.message}")
    }
}

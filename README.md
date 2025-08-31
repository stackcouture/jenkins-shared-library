## Jenkins Shared Library - `vars` Directory

![View Only](https://img.shields.io/badge/license-view--only-red)
![Status](https://img.shields.io/badge/usage-portfolio%20only-blue)

> ⚠️ **Notice:** This repository is made publicly available for **portfolio evaluation purposes only**.  
> **You are not permitted to copy, reuse, distribute, or modify this code** in any form.  
> All rights are reserved by the author. Please contact [Naveen R](naveenramlu28@gmail.com) for permission.

---

This directory contains reusable Groovy scripts (global variables) for Jenkins pipelines. Each script provides a callable function that can be used directly in your `Jenkinsfile`. These scripts cover **CI/CD tasks**, including Docker image management, Git operations, security scanning, deployment, and notifications.

---

### Directory Overview

Scripts are grouped by functionality for easier navigation.

<details>
<summary> Docker & Image Management</summary>

- `buildDockerImage.groovy` – Builds Docker images for your application.  
- `cleanupDockerImages.groovy` – Cleans unused Docker images from the Jenkins agent.  
- `dockerPush.groovy` – Pushes Docker images to the registry (ECR/DockerHub).  
- `signImageWithCosign.groovy` – Signs Docker images using Cosign.  
- `getImageDigest.groovy` – Retrieves the digest of a Docker image from a registry.  
- `checkEcrDigestExists.groovy` – Checks if a specific image digest already exists in ECR.  

</details>

<details>
<summary> Git & Source Control</summary>

- `checkoutGit.groovy` – Performs a Git checkout for a repository.  
- `checkoutAndVerifyGPG.groovy` – Checks out a Git repo and verifies commits with GPG.  
- `confirmYamlUpdate.groovy` – Confirms YAML updates before applying changes.  

</details>

<details>
<summary> Security & Compliance</summary>

- `runTrivyScanUnified.groovy` – Performs Trivy container and file-system scans.  
- `runSnykScan.groovy` – Runs Snyk security vulnerability scans.  
- `runGptSecuritySummary.groovy` – Generates AI-powered HTML security reports summarizing scan 
                                    results.  
- `cosignVerifyECR.groovy` – Verifies Docker image signatures in ECR.  
- `uploadSbomToDependencyTrack.groovy` – Uploads SBOM (CycloneDX) to Dependency-Track.  

</details>

<details>
<summary>Deployment & CI/CD</summary>

- `deployApp.groovy` – Deploys applications to target environments.  
- `updateImageTag.groovy` – Updates image tags in GitOps YAML manifests.  
- `postBuildTestArtifacts.groovy` – Handles post-build artifact management and testing.  

</details>

<details>
<summary>Notifications</summary>

- `sendSlackNotification.groovy` – Sends notifications to Slack channels.  
- `sendAiReportEmail.groovy` – Sends AI-generated security reports via email.  

</details>

<details>
<summary>Utilities / Misc</summary>

- `cleanWorkspace.groovy` – Cleans the Jenkins workspace before a build.  
- `getAwsSecret.groovy` – Retrieves secrets from AWS Secrets Manager.  
- `sonarScan.groovy` – Runs a SonarQube scan on the codebase.  
- `sonarQualityGateCheck.groovy` – Validates SonarQube quality gate status.  

</details>

---

### How to Use

1. Include the shared library in your `Jenkinsfile`:

```groovy
@Library('my-shared-library') _

pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                buildDockerImage('my-app', 'latest')
                cleanupDockerImages()
            }
        }
        stage('Security Scan') {
            steps {
                runTrivyScanUnified()
                runSnykScan()
                runGptSecuritySummary()
            }
        }
        stage('Deploy') {
            steps {
                deployApp('dev')
                updateImageTag('my-app', 'latest')
            }
        }
        stage('Notify') {
            steps {
                sendSlackNotification('Deployment complete!')
                sendAiReportEmail('Security report')
            }
        }
    }
}
```

---

### Mapping of Jenkinsfile Stages to Shared Library Functions

| Jenkinsfile Stage               | Shared Library Function(s) |
|--------------------------------|---------------------------|
| Init & Checkout                 | `cleanWorkspace.groovy` → `cleanWs()`<br>`checkoutGit.groovy` → `checkoutGit(branch, url, secretName)` |
| SBOM + FS Scan                  | `uploadSbomToDependencyTrack.groovy` → `uploadSbomToDependencyTrack(...)`<br>`runTrivyScanUnified.groovy` → `runTrivyScanUnified(...)` |
| SonarQube Analysis & Gate       | `sonarScan.groovy` → `sonarScan(...)`<br>`sonarQualityGateCheck.groovy` → `sonarQualityGateCheck(...)` |
| Build Docker Image              | `buildDockerImage.groovy` → `buildDockerImage(...)` |
| Security Scans Before Push      | `runTrivyScanUnified.groovy` → `runTrivyScanUnified(...)`<br>`runSnykScan.groovy` → `runSnykScan(...)` |
| ECR Push                        | `dockerPush.groovy` → `dockerPush(...)` |
| Sign Image with Cosign           | `signImageWithCosign.groovy` → `signImageWithCosign(...)`<br>`getImageDigest.groovy` → `getImageDigest(...)` |
| Security Scans After Push       | `runTrivyScanUnified.groovy` → `runTrivyScanUnified(...)`<br>`runSnykScan.groovy` → `runSnykScan(...)` |
| Confirm YAML Update             | `confirmYamlUpdate.groovy` → `confirmYamlUpdate()` |
| Update Deployment Files         | `updateImageTag.groovy` → `updateImageTag(...)` |
| Deploy App                      | `deployApp.groovy` → `deployApp()` |
| Cleanup                         | `cleanupDockerImages.groovy` → `cleanupDockerImages(...)` |
| Post / Notifications            | `sendSlackNotification.groovy` → `sendSlackNotification(...)`<br>`sendAiReportEmail.groovy` → `sendAiReportEmail(...)`<br>`postBuildTestArtifacts.groovy` → `postBuildTestArtifacts(...)` |

---

### License

This project is provided under a **view-only** license for review and portfolio purposes.  
See the [LICENSE](./LICENSE) file for details.

---

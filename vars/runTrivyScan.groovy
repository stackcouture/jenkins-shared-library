def runTrivyScan(Map config = [:]) {
    def stageName = config.stageName ?: error("Missing 'stageName'")
    def scanTarget = config.scanTarget ?: error("Missing 'scanTarget'")
    def scanType = config.scanType ?: error("Missing 'scanType'")
    def fileName = config.fileName

    if (!['image', 'fs'].contains(scanType)) {
        error("Invalid scanType: ${scanType}. Allowed values are 'image' or 'fs'.")
    }

    def reportDir = "reports/trivy/${env.BUILD_NUMBER}/${stageName}"

    def htmlReport = scanType == 'fs' 
        ? "${reportDir}/trivy-fs-scan-${fileName}.html"
        : "${reportDir}/trivy-image-scan-${fileName}.html"

    def jsonReport = scanType == 'fs' 
        ? "${reportDir}/trivy-fs-scan-${fileName}.json"
        : "${reportDir}/trivy-image-scan-${fileName}.json"

    sh """
        set -e
        mkdir -p ${reportDir}
        trivy ${scanType} --format template --template "@contrib/html.tpl" -o ${htmlReport} ${scanTarget}
        trivy ${scanType} --format json -o ${jsonReport} ${scanTarget}
    """

    publishHTML(target: [
        allowMissing: true,
        alwaysLinkToLastBuild: true,
        keepAll: true,
        reportDir: reportDir,
        reportFiles: "*.html",
        reportName: "Trivy ${scanType == 'fs' ? 'File System' : 'Image'} Scan (${stageName}) - Build ${env.BUILD_NUMBER}"
    ])

    archiveArtifacts artifacts: "${reportDir}/*.html,${reportDir}/*.json", allowEmptyArchive: true
}
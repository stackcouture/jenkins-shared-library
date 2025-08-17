def call(String reportName = 'Test Report', String reportFilePattern = 'surefire-report.html') {
    script {
        archiveArtifacts artifacts: "target/surefire-reports/*.xml", allowEmptyArchive: true

        if (fileExists('target/surefire-reports')) {
            junit 'target/surefire-reports/*.xml'
        } else {
            echo "No test results found."
        }

        // Collect available reports
        def cards = []

        // JUnit HTML (Surefire)
        def reportDir = 'target/site'
        def resolved = findFiles(glob: "${reportDir}/**/${reportFilePattern}")
        if (resolved.length > 0) {
            def actualFile = resolved[0]
            def reportDirPath = new File(actualFile.path).getParent()
            def reportFileName = new File(actualFile.path).getName()
            def junitRelPath = "${new File(reportDirPath).name}/${reportFileName}"
            cards << """
              <div class="card junit">
                <h2>JUnit Test Report</h2>
                <p><a href="${junitRelPath}">View Test Report</a></p>
              </div>
            """
        } else {
            echo "No HTML test report found matching: ${reportDir}/**/${reportFilePattern}"
        }

        // JaCoCo
        def jacocoHtmlDir = 'target/site/jacoco'
        if (fileExists("${jacocoHtmlDir}/index.html")) {
            cards << """
              <div class="card jacoco">
                <h2>JaCoCo Coverage</h2>
                <p><a href="jacoco/index.html">View Coverage Report</a></p>
              </div>
            """
        } else {
            echo "No JaCoCo HTML report found at: ${jacocoHtmlDir}/index.html"
        }

        // Javadoc
        def javadocDir = 'target/site/apidocs'
        if (fileExists("${javadocDir}/index.html")) {
            cards << """
              <div class="card javadoc">
                <h2>API Documentation (Javadoc)</h2>
                <p><a href="apidocs/index.html">View API Docs</a></p>
              </div>
            """
        } else {
            echo "No Javadoc HTML report found at: ${javadocDir}/index.html"
        }

        // Build dashboard HTML
        def htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8">
          <title>Project Reports</title>
          <style>
            body { font-family: Arial, sans-serif; background: #f8f9fa; padding: 20px; }
            h1 { text-align: center; }
            .card-container { display: flex; gap: 20px; justify-content: center; flex-wrap: wrap; }
            .card {
              background: #fff; border-radius: 10px; box-shadow: 0 2px 6px rgba(0,0,0,0.1);
              width: 250px; padding: 20px; text-align: center; transition: transform 0.2s;
            }
            .card:hover { transform: translateY(-5px); }
            .card h2 { font-size: 18px; margin-bottom: 10px; }
            .jacoco { border-top: 5px solid #28a745; }
            .javadoc { border-top: 5px solid #007bff; }
            .junit { border-top: 5px solid #6f42c1; }
            a { text-decoration: none; font-weight: bold; color: #333; }
          </style>
        </head>
        <body>
          <h1>📊 Project Reports Dashboard</h1>
          <div class="card-container">
            ${cards.join("\n")}
          </div>
        </body>
        </html>
        """

        // Write and publish the dashboard
        writeFile file: 'target/site/reports-dashboard.html', text: htmlContent

        publishHTML([
            reportName: 'Reports Dashboard',
            reportDir: 'target/site',
            reportFiles: 'reports-dashboard.html',
            keepAll: true,
            alwaysLinkToLastBuild: true,
            allowMissing: false
        ])
    }
}

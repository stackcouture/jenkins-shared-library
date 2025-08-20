def call() {
    script {
        def cards = []
        def reportDir = 'target/site'

        // JUnit
        def resolved = findFiles(glob: "${reportDir}/**/${reportFilePattern}")
        if (resolved.length > 0) {
            def junitRelPath = resolved[0].path.replaceFirst("${reportDir}/", "")
            cards << """
              <div class="card junit">
                <h2>JUnit Test Report</h2>
                <p><a href="./${junitRelPath}">View Test Report</a></p>
              </div>
            """
        }

        // JaCoCo
        def jacocoPath = "${reportDir}/jacoco/index.html"
        if (fileExists(jacocoPath)) {
            cards << """
              <div class="card jacoco">
                <h2>JaCoCo Coverage</h2>
                <p><a href="./jacoco/index.html">View Coverage Report</a></p>
              </div>
            """
        }

        // Javadoc
        def javadocPath = "${reportDir}/apidocs/index.html"
        if (fileExists(javadocPath)) {
            cards << """
              <div class="card javadoc">
                <h2>API Documentation (Javadoc)</h2>
                <p><a href="./apidocs/index.html">View API Docs</a></p>
              </div>
            """
        }

        // Write CSS
        def cssContent = """
        body {
          font-family: Arial, sans-serif;
          background: #111827;
          color: #f5f5f5;
          margin: 0;
          padding: 20px;
        }
        h1 {
          text-align: center;
          font-size: 2rem;
          margin-bottom: 20px;
        }
        .card-container {
          display: flex;
          gap: 20px;
          justify-content: center;
          flex-wrap: wrap;
        }
        .card {
          background: #1f2937;
          border-radius: 12px;
          box-shadow: 0 2px 6px rgba(0,0,0,0.3);
          width: 250px;
          padding: 20px;
          text-align: center;
          transition: transform 0.2s;
        }
        .card:hover { transform: translateY(-5px); }
        .card h2 { font-size: 18px; margin-bottom: 10px; }
        .jacoco { border-top: 4px solid #28a745; }
        .javadoc { border-top: 4px solid #007bff; }
        .junit { border-top: 4px solid #a855f7; }
        a {
          text-decoration: none;
          font-weight: bold;
          color: #3b82f6;
        }
        a:hover { text-decoration: underline; }
        """
        writeFile file: "${reportDir}/styles.css", text: cssContent

        // Dashboard HTML
        def htmlContent = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>📊 Project Reports Dashboard</title>
          <link rel="stylesheet" href="./styles.css">
        </head>
        <body>
          <h1>📊 Project Reports Dashboard</h1>
          <div class="card-container">
            ${cards.join("\n")}
          </div>
        </body>
        </html>
        """
        writeFile file: "${reportDir}/scan-reports-dashboard.html", text: htmlContent

        publishHTML([
            reportName: 'Scan Reports Dashboard',
            reportDir: reportDir,
            reportFiles: 'scan-reports-dashboard.html',
            keepAll: true,
            alwaysLinkToLastBuild: true,
            allowMissing: false
        ])
    }
}

def call(String reportName = 'Test Report', String reportFilePattern = 'surefire-report.html') {
    script {
        archiveArtifacts artifacts: "target/surefire-reports/*.xml", allowEmptyArchive: true
        if (fileExists('target/surefire-reports')) {
            junit 'target/surefire-reports/*.xml'
        }

        def cards = []
        def reportDir = 'target/site'

        // ✅ JUnit HTML (Surefire)
        def resolved = findFiles(glob: "${reportDir}/**/${reportFilePattern}")
        if (resolved.length > 0) {
            def junitRelPath = resolved[0].path.replaceFirst("${reportDir}/", "")
            cards << """
              <div class="card junit">
                <h2>JUnit Test Report</h2>
                <p><a href="${junitRelPath}" target="_blank">View Test Report</a></p>
              </div>
            """
        }

        // ✅ JaCoCo Coverage
        if (fileExists("${reportDir}/jacoco/index.html")) {
            cards << """
              <div class="card jacoco">
                <h2>JaCoCo Coverage</h2>
                <p><a href="jacoco/index.html" target="_blank">View Coverage Report</a></p>
              </div>
            """
        }

        // ✅ Javadoc
        if (fileExists("${reportDir}/apidocs/index.html")) {
            cards << """
              <div class="card javadoc">
                <h2>API Documentation (Javadoc)</h2>
                <p><a href="apidocs/index.html" target="_blank">View API Docs</a></p>
              </div>
            """
        }

        // ✅ Build Dashboard HTML (pure CSS toggle)
        def htmlContent = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>Project Reports Dashboard</title>
          <style>
            /* Default (Light) Theme */
            .dashboard {
              --bg: #f8f9fa;
              --card: #ffffff;
              --text: #333333;
              --shadow: 0 2px 6px rgba(0,0,0,0.15);
            }
            /* Dark Theme overrides when toggle checked */
            #darkToggle:checked ~ .dashboard {
              --bg: #1e1e2f;
              --card: #2b2b3c;
              --text: #f5f5f5;
            }

            body {
              font-family: Arial, sans-serif;
              margin: 0;
              padding: 20px;
              background: var(--bg);
              color: var(--text);
              transition: background 0.3s, color 0.3s;
            }
            h1 {
              text-align: center;
              margin-bottom: 20px;
              font-size: 2rem;
            }
            .toggle-container {
              display: flex;
              justify-content: flex-end;
              margin-bottom: 20px;
            }
            .toggle-label {
              cursor: pointer;
              background: #007bff;
              color: #fff;
              padding: 8px 16px;
              border-radius: 6px;
              font-size: 14px;
              user-select: none;
              transition: background 0.3s;
            }
            .toggle-label:hover {
              background: #0056b3;
            }
            .card-container {
              display: flex;
              gap: 20px;
              justify-content: center;
              flex-wrap: wrap;
            }
            .card {
              background: var(--card);
              border-radius: 12px;
              box-shadow: var(--shadow);
              width: 250px;
              padding: 20px;
              text-align: center;
              transition: transform 0.2s, background 0.3s, color 0.3s;
            }
            .card:hover { transform: translateY(-5px); }
            .card h2 { font-size: 18px; margin-bottom: 10px; }
            .jacoco { border-top: 5px solid #28a745; }
            .javadoc { border-top: 5px solid #007bff; }
            .junit { border-top: 5px solid #6f42c1; }
            a { text-decoration: none; font-weight: bold; color: #1e90ff; }
            a:hover { text-decoration: underline; }
          </style>
        </head>
        <body>
          <!-- Dark Mode Toggle -->
          <input type="checkbox" id="darkToggle" hidden>
          <div class="toggle-container">
            <label for="darkToggle" class="toggle-label">🌙 Toggle Dark Mode</label>
          </div>

          <!-- Dashboard -->
          <div class="dashboard">
            <h1>📊 Project Reports Dashboard</h1>
            <div class="card-container">
              ${cards.join("\n")}
            </div>
          </div>
        </body>
        </html>
        """

        writeFile file: "${reportDir}/reports-dashboard.html", text: htmlContent
        publishHTML([
            reportName: 'Reports Dashboard',
            reportDir: reportDir,
            reportFiles: 'reports-dashboard.html',
            keepAll: true,
            alwaysLinkToLastBuild: true,
            allowMissing: false
        ])
    }
}

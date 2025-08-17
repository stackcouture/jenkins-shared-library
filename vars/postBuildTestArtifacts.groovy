def call(String reportName = 'Test Report', String reportFilePattern = 'surefire-report.html') {
    script {
        // Archive raw test result XMLs
        archiveArtifacts artifacts: "target/surefire-reports/*.xml", allowEmptyArchive: true

        // Publish JUnit results (if available)
        if (fileExists('target/surefire-reports')) {
            junit 'target/surefire-reports/*.xml'
        } else {
            echo "No test results found."
        }

        // Collect available cards
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
        } else {
            echo "No HTML test report found matching: ${reportDir}/**/${reportFilePattern}"
        }

        // ✅ JaCoCo Coverage
        def jacocoHtmlDir = "${reportDir}/jacoco"
        if (fileExists("${jacocoHtmlDir}/index.html")) {
            cards << """
              <div class="card jacoco">
                <h2>JaCoCo Coverage</h2>
                <p><a href="jacoco/index.html" target="_blank">View Coverage Report</a></p>
              </div>
            """
        } else {
            echo "No JaCoCo HTML report found at: ${jacocoHtmlDir}/index.html"
        }

        // ✅ Javadoc
        def javadocDir = "${reportDir}/apidocs"
        if (fileExists("${javadocDir}/index.html")) {
            cards << """
              <div class="card javadoc">
                <h2>API Documentation (Javadoc)</h2>
                <p><a href="apidocs/index.html" target="_blank">View API Docs</a></p>
              </div>
            """
        } else {
            echo "No Javadoc HTML report found at: ${javadocDir}/index.html"
        }

        // ✅ Build Dashboard HTML
        def htmlContent = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>Project Reports Dashboard</title>
          <style>
            :root {
              --bg-light: #f8f9fa;
              --bg-dark: #1e1e2f;
              --card-light: #ffffff;
              --card-dark: #2b2b3c;
              --text-light: #333333;
              --text-dark: #f5f5f5;
              --shadow: 0 2px 6px rgba(0,0,0,0.15);
            }
            body {
              font-family: Arial, sans-serif;
              margin: 0;
              padding: 20px;
              background: var(--bg-light);
              color: var(--text-light);
              transition: background 0.3s, color 0.3s;
            }
            body.dark {
              background: var(--bg-dark);
              color: var(--text-dark);
            }
            h1 {
              text-align: center;
              margin-bottom: 20px;
              font-size: 2rem;
            }
            .theme-toggle {
              display: flex;
              justify-content: flex-end;
              margin-bottom: 20px;
            }
            .toggle-btn {
              cursor: pointer;
              background: #007bff;
              color: #fff;
              border: none;
              padding: 8px 16px;
              border-radius: 6px;
              font-size: 14px;
              transition: background 0.3s;
            }
            .toggle-btn:hover {
              background: #0056b3;
            }
            .card-container {
              display: flex;
              gap: 20px;
              justify-content: center;
              flex-wrap: wrap;
            }
            .card {
              background: var(--card-light);
              border-radius: 12px;
              box-shadow: var(--shadow);
              width: 250px;
              padding: 20px;
              text-align: center;
              transition: transform 0.2s, background 0.3s, color 0.3s;
            }
            body.dark .card {
              background: var(--card-dark);
            }
            .card:hover {
              transform: translateY(-5px);
            }
            .card h2 {
              font-size: 18px;
              margin-bottom: 10px;
            }
            .jacoco { border-top: 5px solid #28a745; }
            .javadoc { border-top: 5px solid #007bff; }
            .junit { border-top: 5px solid #6f42c1; }
            a {
              text-decoration: none;
              font-weight: bold;
              color: #1e90ff;
            }
            a:hover {
              text-decoration: underline;
            }
          </style>
        </head>
        <body>
          <div class="theme-toggle">
            <button class="toggle-btn" onclick="toggleTheme()">🌙 Dark Mode</button>
          </div>
          <h1>📊 Project Reports Dashboard</h1>
          <div class="card-container">
            ${cards.join("\n")}
          </div>
          <script>
            function toggleTheme() {
              document.body.classList.toggle("dark");
              const btn = document.querySelector(".toggle-btn");
              if (document.body.classList.contains("dark")) {
                btn.textContent = "☀️ Light Mode";
              } else {
                btn.textContent = "🌙 Dark Mode";
              }
            }
          </script>
        </body>
        </html>
        """

        // Write and publish dashboard
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

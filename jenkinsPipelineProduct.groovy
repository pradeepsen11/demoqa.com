import groovy.json.JsonOutput

/** Reusable Windows Jenkins pipeline. The caller checks out the repository and passes configuration. */
def runPipeline(Map config) {
    long startedAt = System.currentTimeMillis()
    env.TEST_SUMMARY = 'Tests run: unknown, Failures: unknown, Errors: unknown, Skipped: unknown'
    env.FAILED_TESTS = ''
    env.REPORT_UPLOAD_STATUS = 'NOT_ATTEMPTED'
    env.GOOGLE_DRIVE_REPORT_URL = ''
    env.EXTENT_REPORT_RELATIVE_PATH = ''
    env.MAVEN_EXIT_CODE = 'not-started'
    env.TEST_EXECUTION_SECONDS = '0'

    String client = (config.client ?: 'DemoQA').toString()
    String environment = (config.environment ?: 'PREPROD').toString()
    String suiteXml = (config.suiteXml ?: 'TestStudentRegistration.xml').toString()
    String suiteName = (config.suiteName ?: 'DemoQA_StudentRegistration').toString()
    boolean uploadToDrive = config.uploadToGoogleDrive != false
    boolean sendEmail = config.sendEmail != false
    boolean sendGoogleChat = config.sendGoogleChat == true
    String emailTo = (config.emailTo ?: '').toString()
    String emailFrom = (config.emailFrom ?: '').toString()
    String driveCredentialId = (config.googleDriveCredentialId ?: 'google-drive-service-account-json').toString()
    String driveFolderIdCredentialId = (config.googleDriveFolderIdCredentialId ?: 'google-drive-report-folder-id').toString()

    if (!(suiteXml ==~ /[A-Za-z0-9_.-]+\.xml/) || !fileExists(suiteXml)) {
        error("TestNG suite XML is missing or invalid: ${suiteXml}")
    }

    try {
        stage('Build and Test') {
            long testStartedAt = System.currentTimeMillis()
            int mavenStatus = bat(returnStatus: true, script: """@echo off
mvn -B -ntp clean install "-DsuiteXmlFile=${suiteXml}" > maven-output.log 2>&1
set "MAVEN_EXIT=%ERRORLEVEL%"
type maven-output.log
exit /b %MAVEN_EXIT%
""")
            env.MAVEN_EXIT_CODE = "${mavenStatus}"
            env.TEST_EXECUTION_SECONDS = "${((System.currentTimeMillis() - testStartedAt) / 1000L).longValue()}"

            String output = fileExists('maven-output.log') ? readFile('maven-output.log') : ''
            def summaries = output.readLines().findAll { it =~ /Tests run:\s*\d+,\s*Failures:\s*\d+,\s*Errors:\s*\d+,\s*Skipped:\s*\d+/ }
            if (summaries) {
                env.TEST_SUMMARY = summaries[-1].trim().replaceFirst(/^\[[A-Z]+\]\s*/, '')
            } else {
                env.TEST_SUMMARY = 'Test summary unavailable (no Maven Surefire summary found)'
                currentBuild.result = 'FAILURE'
                echo 'No Maven Surefire test summary was found; refusing to report this build as a test success.'
            }

            // Surefire failure lines are more reliable than looking only for stack-trace markers.
            def failures = output.readLines().findAll { it ==~ /^\[ERROR\]\s+[^ ]+\.[^:]+:.*/ }
            env.FAILED_TESTS = failures ? failures.unique().take(20).join('\n') : ''

            if (mavenStatus != 0) {
                currentBuild.result = 'FAILURE'
                echo "Maven/TestNG returned ${mavenStatus}; continuing to report and email stages."
            }
        }
    } catch (err) {
        currentBuild.result = 'FAILURE'
        echo "Build/test stage infrastructure error: ${err}"
    } finally {
        stage('Discover and Archive Extent Report') {
            try {
                String expectedName = (env.MAVEN_EXIT_CODE == '0' && currentBuild.currentResult != 'FAILURE')
                    ? 'ExtentReport_TestStudentRegistration_PASS.html'
                    : 'ExtentReport_TestStudentRegistration.html'
                String expectedPath = "ExtentReports/${expectedName}"
                if (!fileExists(expectedPath)) {
                    String alternateName = expectedName == 'ExtentReport_TestStudentRegistration_PASS.html'
                        ? 'ExtentReport_TestStudentRegistration.html'
                        : 'ExtentReport_TestStudentRegistration_PASS.html'
                    String alternatePath = "ExtentReports/${alternateName}"
                    if (fileExists(alternatePath)) {
                        echo "WARNING: Expected ${expectedPath} missing; using ${alternatePath}. Verify report naming in the test framework."
                        expectedPath = alternatePath
                    }
                }
                String reportPath = fileExists(expectedPath) ? expectedPath : '' 
                if (reportPath) {
                    env.EXTENT_REPORT_RELATIVE_PATH = reportPath.replace('\\', '/')
                    env.EXTENT_REPORT_PATH = "${env.WORKSPACE}\\${reportPath.replace('/', '\\')}"
                    archiveArtifacts artifacts: "${env.EXTENT_REPORT_RELATIVE_PATH},maven-output.log", allowEmptyArchive: false, fingerprint: true
                    archiveArtifacts artifacts: 'target/surefire-reports/**', allowEmptyArchive: true, fingerprint: false
                    echo "Discovered and archived report: ${env.EXTENT_REPORT_RELATIVE_PATH}"
                } else {
                    echo 'No non-empty HTML report found under ExtentReports.'
                }
            } catch (reportError) {
                echo "Report discovery/archive failed: ${reportError}"
                if (!currentBuild.result || currentBuild.result == 'SUCCESS') currentBuild.result = 'UNSTABLE'
            }
        }

        stage('Update Google Drive Report') {
            if (!uploadToDrive) {
                env.REPORT_UPLOAD_STATUS = 'DISABLED_BY_CONFIGURATION'
            } else if (!env.EXTENT_REPORT_PATH) {
                env.REPORT_UPLOAD_STATUS = 'NO_REPORT_FOUND'
                if (!currentBuild.result || currentBuild.result == 'SUCCESS') currentBuild.result = 'UNSTABLE'
            } else {
                try {
                    withCredentials([
                        file(credentialsId: driveCredentialId, variable: 'GOOGLE_DRIVE_CREDENTIALS'),
                        string(credentialsId: driveFolderIdCredentialId, variable: 'GOOGLE_DRIVE_FOLDER_ID')
                    ]) {
                        String uploadOutput = bat(returnStdout: true, script: '''@echo off
setlocal
set "PYTHON_EXE=C:\Users\CSA\AppData\Local\Programs\Python\Python315\python.exe"
set "PYTHON_LIBS=%WORKSPACE%\jenkins-python-libs"
if not exist "%PYTHON_EXE%" (echo ERROR: Python not found at %PYTHON_EXE% & exit /b 1)
if not exist "%PYTHON_LIBS%" mkdir "%PYTHON_LIBS%"
"%PYTHON_EXE%" -m pip install --disable-pip-version-check --target "%PYTHON_LIBS%" google-api-python-client google-auth
if errorlevel 1 exit /b 1
"%PYTHON_EXE%" -c "import runpy,sys; sys.path.insert(0, r'%PYTHON_LIBS%'); sys.argv=[r'upload_extent_report.py', r'%EXTENT_REPORT_PATH%', r'%GOOGLE_DRIVE_FOLDER_ID%']; runpy.run_path('upload_extent_report.py', run_name='__main__')"
if errorlevel 1 exit /b 1
endlocal
''').trim()
                        echo uploadOutput
                        def urlMatcher = (uploadOutput =~ /Google Drive report URL:\s*(https:\/\/\S+)/)
                        if (!urlMatcher.find()) error('Uploader exited successfully but did not return a Google Drive report URL.')
                        env.GOOGLE_DRIVE_REPORT_URL = urlMatcher.group(1)
                        env.REPORT_UPLOAD_STATUS = 'SUCCESS'
                    }
                } catch (uploadError) {
                    env.REPORT_UPLOAD_STATUS = 'FAILED'
                    echo "Google Drive update failed: ${uploadError}"
                    if (!currentBuild.result || currentBuild.result == 'SUCCESS') currentBuild.result = 'UNSTABLE'
                }
            }
        }

        stage('Send Email Alert') {
            if (!sendEmail) {
                echo 'Email notification disabled by configuration.'
            } else if (!emailTo || !emailFrom) {
                echo 'Email skipped: configure non-empty emailTo and emailFrom values.'
                if (!currentBuild.result || currentBuild.result == 'SUCCESS') currentBuild.result = 'UNSTABLE'
            } else {
                try {
                    String result = currentBuild.currentResult ?: 'FAILURE'
                    String duration = formatDuration(((System.currentTimeMillis() - startedAt) / 1000L).longValue())
                    def counts = parseCounts(env.TEST_SUMMARY)
                    String subjectStatus = (result == 'SUCCESS' && env.REPORT_UPLOAD_STATUS == 'SUCCESS') ? 'SUCCESS' : 'FAILURE'
                    String icon = subjectStatus == 'SUCCESS' ? '[SUCCESS]' : '[FAILURE]'
                    String reportBlock = env.GOOGLE_DRIVE_REPORT_URL
                        ? "<p>Link: <b>Extent Report:</b><br><a href=\"${env.GOOGLE_DRIVE_REPORT_URL}\">${env.GOOGLE_DRIVE_REPORT_URL}</a></p>"
                        : "<p>Link: <b>Google Drive report:</b> unavailable (${env.REPORT_UPLOAD_STATUS}). See the Jenkins build for the archived report, if generated.</p>"
                    String failedBlock = env.FAILED_TESTS
                        ? "<h3>Failed test cases</h3><pre>${escapeHtml(env.FAILED_TESTS)}</pre>"
                        : (counts.failures > 0 || counts.errors > 0 ? '<p>Failure details were not extracted from Maven output; see the Jenkins build log and Surefire reports.</p>' : '')
                    String subject = "${icon} ${client} Selenium Automation | ${subjectStatus} | #${env.BUILD_NUMBER} | ${environment}"
                    String body = """<p>Hi,</p>
<p>Please find test execution summary below -</p>
<p>- Execution Time: ${duration}<br>
Tests run: ${counts.total}, Failures: ${counts.failures}, Skipped: ${counts.skipped}<br>
Errors: ${counts.errors}<br>
- Build Result: ${result}<br>
- Report upload: ${env.REPORT_UPLOAD_STATUS}</p>
${failedBlock}
${reportBlock}
<p>Attached: the selected Extent HTML report and Maven execution log only.</p>
<p>Regards<br>Pradeep</p>"""
                    String attachmentPatterns = ([env.EXTENT_REPORT_RELATIVE_PATH, 'maven-output.log'].findAll { it }.join(','))
                    emailext(to: emailTo, from: emailFrom, subject: subject, body: body,
                        mimeType: 'text/html', attachLog: false,
                        attachmentsPattern: attachmentPatterns)
                    echo "Email notification requested: ${subject}"
                } catch (emailError) {
                    echo "Email notification failed: ${emailError}"
                    if (!currentBuild.result || currentBuild.result == 'SUCCESS') currentBuild.result = 'UNSTABLE'
                }
            }
        }

        if (sendGoogleChat) {
            echo 'Google Chat is enabled in config, but this DemoQA email workflow intentionally does not send Google Chat messages.'
        }
    }
}

private Map parseCounts(String summary) {
    def matcher = (summary ?: '') =~ /Tests run:\s*(\d+),\s*Failures:\s*(\d+),\s*Errors:\s*(\d+),\s*Skipped:\s*(\d+)/
    if (matcher.find()) return [total: matcher.group(1) as int, failures: matcher.group(2) as int, errors: matcher.group(3) as int, skipped: matcher.group(4) as int]
    return [total: 0, failures: 0, errors: 0, skipped: 0]
}

private String formatDuration(long seconds) {
    return "${(long)(seconds / 60)} minutes ${(long)(seconds % 60)} seconds"
}

private String escapeHtml(String value) {
    return (value ?: '').replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
        .replace('"', '&quot;').replace("'", '&#39;')
}

return this

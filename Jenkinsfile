import groovy.json.JsonOutput

pipeline {
    agent any

    triggers {
        // Daily at 10:00 PM India Standard Time.
        cron('TZ=Asia/Kolkata\n0 22 * * *')
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        string(name: 'CLIENT', defaultValue: 'DemoQA',
            description: 'Project/client label shown in notifications.')
        choice(name: 'ENVIRONMENT', choices: ['PUBLIC', 'QA', 'STAGING'],
            description: 'Notification label only; it does not change the application URL.')
        string(name: 'SUITE_XML', defaultValue: 'TestStudentRegistration.xml',
            description: 'TestNG suite XML filename relative to the repository root.')
        string(name: 'SUITE_NAME', defaultValue: 'DemoQA_StudentRegistration',
            description: 'Readable suite name used in notifications.')
        booleanParam(name: 'UPLOAD_TO_GOOGLE_DRIVE', defaultValue: true,
            description: 'Update the existing Google Drive HTML report file.')
        booleanParam(name: 'SEND_EMAIL_NOTIFICATION', defaultValue: true,
            description: 'Send the build summary and report link by email.')
        booleanParam(name: 'SEND_GOOGLE_CHAT_NOTIFICATION', defaultValue: false,
            description: 'Send a summary to Google Chat. Requires credential demoqa-google-chat-webhook.')
    }

    environment {
        GOOGLE_DRIVE_REPORT_URL = ''
        EXTENT_REPORT_PATH = ''
        EXTENT_REPORT_RELATIVE_PATH = ''
        TEST_SUMMARY = 'Test summary unavailable'
        FAILED_TESTS = ''
        MAVEN_EXIT_CODE = 'not-started'
        REPORT_UPLOAD_STATUS = 'NOT_ATTEMPTED'
        TEST_EXECUTION_SECONDS = '0'
    }

    stages {
        stage('Checkout') {
            steps {
                // A fresh workspace prevents uploading a report from a previous build.
                deleteDir()
                git url: 'https://github.com/pradeepsen11/demoqa.com.git',
                    branch: 'main'
            }
        }

        stage('Validate Parameters') {
            steps {
                script {
                    if (!(params.SUITE_XML ==~ /^[A-Za-z0-9_.-]+\.xml$/)) {
                        error("Invalid SUITE_XML '${params.SUITE_XML}'. Use an XML filename only.")
                    }
                    if (!fileExists(params.SUITE_XML)) {
                        error("TestNG suite XML not found at repository root: ${params.SUITE_XML}")
                    }

                    echo "Client      : ${params.CLIENT}"
                    echo "Environment : ${params.ENVIRONMENT}"
                    echo "Suite name  : ${params.SUITE_NAME}"
                    echo "Suite XML   : ${params.SUITE_XML}"
                }
            }
        }

        stage('Build and Test') {
            steps {
                script {
                    long testStart = System.currentTimeMillis()

                    int mavenStatus = bat(
                        returnStatus: true,
                        script: """@echo off
mvn clean install "-DsuiteXmlFile=${params.SUITE_XML}" > maven-output.log 2>&1
set "MAVEN_EXIT=%ERRORLEVEL%"
type maven-output.log
exit /b %MAVEN_EXIT%
"""
                    )

                    env.MAVEN_EXIT_CODE = "${mavenStatus}"
                    env.TEST_EXECUTION_SECONDS =
                        "${((System.currentTimeMillis() - testStart) / 1000L).longValue()}"

                    String output = fileExists('maven-output.log') ? readFile('maven-output.log') : ''
                    def summaries = output.readLines().findAll {
                        it =~ /Tests run:\s*\d+,\s*Failures:\s*\d+,\s*Errors:\s*\d+,\s*Skipped:\s*\d+/
                    }
                    if (summaries) {
                        env.TEST_SUMMARY = summaries[-1].trim()
                    }

                    def failures = output.readLines().findAll {
                        (it.contains('<<< FAILURE!') || it.contains('<<< ERROR!')) &&
                        it.contains('Time elapsed:')
                    }.take(10)
                    env.FAILED_TESTS = failures ? failures.join('\n') : ''

                    if (mavenStatus != 0) {
                        error("Maven/TestNG failed with exit code ${mavenStatus}. ${env.TEST_SUMMARY}")
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                // Find the newest report even when Maven/TestNG fails.
                try {
                    def reportPath = powershell(
                        returnStdout: true,
                        script: """
                            \$root = Join-Path \$env:WORKSPACE 'ExtentReports'
                            \$report = Get-ChildItem -Path \$root -Filter '*.html' -File -Recurse -ErrorAction SilentlyContinue |
                                Sort-Object LastWriteTime -Descending |
                                Select-Object -First 1
                            if (\$report) {
                                Write-Output \$report.FullName
                            }
                        """
                    ).trim()

                    if (reportPath) {
                        env.EXTENT_REPORT_PATH = reportPath
                        env.EXTENT_REPORT_RELATIVE_PATH =
                            reportPath.substring(env.WORKSPACE.length)
                                .replace('\\', '/')
                                .replaceFirst('^/', '')

                        echo "Extent report: ${env.EXTENT_REPORT_RELATIVE_PATH}"
                        archiveArtifacts(
                            artifacts: env.EXTENT_REPORT_RELATIVE_PATH,
                            allowEmptyArchive: true,
                            fingerprint: true
                        )
                    } else {
                        echo 'No HTML report was generated under ExtentReports.'
                    }
                } catch (reportError) {
                    echo "Report discovery/archive failed: ${reportError}"
                }

                // Update the same Google Drive file ID on each execution.
                if (params.UPLOAD_TO_GOOGLE_DRIVE && env.EXTENT_REPORT_PATH) {
                    try {
                        withCredentials([
                            file(credentialsId: 'google-drive-service-account-json',
                                variable: 'GOOGLE_DRIVE_CREDENTIALS'),
                            string(credentialsId: 'google-drive-report-file-id',
                                variable: 'GOOGLE_DRIVE_FILE_ID')
                        ]) {
                            bat """
@echo off
py -3 -m pip install --user --disable-pip-version-check --quiet google-api-python-client google-auth
if errorlevel 1 exit /b %ERRORLEVEL%
py -3 upload_extent_report.py "%EXTENT_REPORT_PATH%" "%GOOGLE_DRIVE_FILE_ID%"
"""

                            env.GOOGLE_DRIVE_REPORT_URL =
                                "https://drive.google.com/file/d/${env.GOOGLE_DRIVE_FILE_ID}/view"
                            env.REPORT_UPLOAD_STATUS = 'SUCCESS'
                            echo "Google Drive report URL: ${env.GOOGLE_DRIVE_REPORT_URL}"
                        }
                    } catch (uploadError) {
                        env.REPORT_UPLOAD_STATUS = 'FAILED'
                        echo "Google Drive upload failed: ${uploadError}"
                        if (currentBuild.currentResult == 'SUCCESS') {
                            currentBuild.result = 'UNSTABLE'
                        }
                    }
                } else if (!params.UPLOAD_TO_GOOGLE_DRIVE) {
                    env.REPORT_UPLOAD_STATUS = 'DISABLED_BY_PARAMETER'
                } else {
                    env.REPORT_UPLOAD_STATUS = 'NO_REPORT_FOUND'
                }

                long elapsedSeconds =
                    ((System.currentTimeMillis() - currentBuild.startTimeInMillis) / 1000L).longValue()
                String elapsedText = "${(long)(elapsedSeconds / 60)} min ${(long)(elapsedSeconds % 60)} sec"
                String result = currentBuild.currentResult ?: 'UNKNOWN'
                String driveUrl = env.GOOGLE_DRIVE_REPORT_URL ?: 'Google Drive report URL unavailable'
                String jenkinsUrl = env.BUILD_URL ?: ''
                String failedTestsText = env.FAILED_TESTS
                    ? "\nFailed test details:\n${env.FAILED_TESTS}"
                    : ''

                String message = """${result == 'SUCCESS' ? '✅' : '❌'} DemoQA Selenium Automation | ${result} | #${env.BUILD_NUMBER}

🔹 Client: ${params.CLIENT}
🔹 Environment: ${params.ENVIRONMENT}
🔹 Suite: ${params.SUITE_NAME}
🔹 TestNG XML: ${params.SUITE_XML}
🔹 Execution time: ${elapsedText}
🔹 Test summary: ${env.TEST_SUMMARY}
🔹 Google Drive upload: ${env.REPORT_UPLOAD_STATUS}
🔹 Report URL: ${driveUrl}
🔹 Jenkins build: ${jenkinsUrl}${failedTestsText}"""

                if (params.SEND_GOOGLE_CHAT_NOTIFICATION) {
                    try {
                        withCredentials([
                            string(credentialsId: 'demoqa-google-chat-webhook',
                                variable: 'GOOGLE_CHAT_WEBHOOK')
                        ]) {
                            writeFile(
                                file: 'google-chat-payload.json',
                                text: JsonOutput.toJson([text: message])
                            )
                            powershell(
                                script: """
                                    \$ErrorActionPreference = 'Stop'
                                    Invoke-RestMethod -Uri \$env:GOOGLE_CHAT_WEBHOOK -Method Post -ContentType 'application/json; charset=utf-8' -InFile 'google-chat-payload.json' | Out-Null
                                """
                            )
                            echo 'Google Chat notification sent.'
                        }
                    } catch (chatError) {
                        echo "Google Chat notification failed: ${chatError}"
                    }
                }

                if (params.SEND_EMAIL_NOTIFICATION) {
                    try {
                        String reportLinkHtml = env.GOOGLE_DRIVE_REPORT_URL
                            ? """<p><b>Latest Google Drive report:</b>
                                 <a href="${env.GOOGLE_DRIVE_REPORT_URL}">Open Extent Report</a></p>"""
                            : '<p><b>Google Drive report:</b> Upload unavailable; see Jenkins console.</p>'

                        String attachment = env.EXTENT_REPORT_RELATIVE_PATH ?: ''

                        emailext(
                            to: 'pksen7117@gmail.com',
                            from: 'automationbypradeep@gmail.com',
                            subject: "Extent Report | ${params.CLIENT} | ${params.ENVIRONMENT} | #${env.BUILD_NUMBER} | ${result}",
                            body: """
                                <h2>DemoQA Selenium Automation - ${result}</h2>
                                <p><b>Client:</b> ${params.CLIENT}</p>
                                <p><b>Environment:</b> ${params.ENVIRONMENT}</p>
                                <p><b>Suite:</b> ${params.SUITE_NAME}</p>
                                <p><b>TestNG XML:</b> ${params.SUITE_XML}</p>
                                <p><b>Execution time:</b> ${elapsedText}</p>
                                <p><b>Test summary:</b> ${env.TEST_SUMMARY}</p>
                                <p><b>Google Drive upload:</b> ${env.REPORT_UPLOAD_STATUS}</p>
                                ${reportLinkHtml}
                                <p><b>Jenkins build:</b>
                                   <a href="${jenkinsUrl}">${jenkinsUrl}</a></p>
                                ${env.FAILED_TESTS ? "<h3>Failed test details</h3><pre>${env.FAILED_TESTS}</pre>" : ""}
                            """,
                            attachLog: true,
                            attachmentsPattern: attachment,
                            mimeType: 'text/html'
                        )
                    } catch (emailError) {
                        echo "Email notification failed: ${emailError}"
                    }
                }
            }
        }
    }
}

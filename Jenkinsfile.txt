pipeline {
    agent any

    triggers {
        cron('00 21 * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout the code from the Git repository, main branch
                git url: 'https://github.com/pradeepsen11/flipkartHome-selenium-automation.git', branch: 'flipkart'
            }
        }
        
        stage('Build and Test') {
            steps {
                // Run Maven build and tests
                bat 'mvn clean install'
            }
        }
    }

    post {
        always {
            // Publish Extent Report
            script {
                emailext (
                    to: 'pksen7117@gmail.com',
                    subject: "Extent Report | ${env.JOB_NAME} | ${env.BUILD_NUMBER} | ${currentBuild.currentResult}",
                    body: """<p>Build status: ${currentBuild.currentResult}</p>
                             <p>Job: ${env.JOB_NAME}</p>
                             <p>Build number: ${env.BUILD_NUMBER}</p>
                             <p>View build: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>""",
                    from: 'automationbypradeep@gmail.com',
                    attachLog: true,
                    attachmentsPattern: 'ExtentReports/ExtentReportResult.html',
                    mimeType: 'text/html'
                )
            }
        }
    }
}

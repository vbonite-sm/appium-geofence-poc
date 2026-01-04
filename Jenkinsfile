pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven3'
        allure 'Allure'
    }

    environment {
        BROWSERSTACK_USERNAME = credentials('browserstack-username')
        BROWSERSTACK_ACCESSKEY = credentials('browserstack-accesskey')
        // Jira Integration Credentials
        JIRA_BASE_URL = credentials('jira-base-url')
        JIRA_EMAIL = credentials('jira-email')
        JIRA_API_TOKEN = credentials('jira-api-token')
        JIRA_PROJECT_KEY = 'GEO'
        JIRA_ENABLED = 'true'
        // Confluence Integration Credentials
        CONFLUENCE_BASE_URL = credentials('confluence-base-url')
        CONFLUENCE_SPACE_KEY = 'GEO'
    }

    parameters {
        choice(
            name: 'TEST_TYPE',
            choices: ['all', 'android-local', 'android-browserstack', 'ios-browserstack', 'api', 'atlassian-api'],
            description: 'Select which tests to run'
        )
        booleanParam(
            name: 'CREATE_JIRA_DEFECTS',
            defaultValue: true,
            description: 'Automatically create Jira defects for failed tests'
        )
        booleanParam(
            name: 'PUBLISH_TO_CONFLUENCE',
            defaultValue: true,
            description: 'Publish test report to Confluence'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out code...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Building project...'
                bat 'mvn clean compile -DskipTests'
            }
        }

        stage('Run API Tests') {
            when {
                expression { params.TEST_TYPE == 'all' || params.TEST_TYPE == 'api' }
            }
            steps {
                echo '🔌 Running API Tests...'
                bat 'mvn test -Dtest=GeofenceApiTest'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Run Android Tests - Local') {
            when {
                expression { params.TEST_TYPE == 'android-local' }
            }
            steps {
                echo '📱 Running Android Tests on Local Emulator...'
                bat 'mvn test -Dtest=GeofenceTest -DexecutionMode=local'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Run Android Tests - BrowserStack') {
            when {
                expression { params.TEST_TYPE == 'all' || params.TEST_TYPE == 'android-browserstack' }
            }
            steps {
                echo '☁️ Running Android Tests on BrowserStack...'
                bat "mvn test -Dtest=GeofenceTest -DexecutionMode=browserstack -Dbrowserstack.username=%BROWSERSTACK_USERNAME% -Dbrowserstack.accesskey=%BROWSERSTACK_ACCESSKEY%"
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Run iOS Tests - BrowserStack') {
            when {
                expression { params.TEST_TYPE == 'all' || params.TEST_TYPE == 'ios-browserstack' }
            }
            steps {
                echo '🍎 Running iOS Tests on BrowserStack...'
                bat "mvn test -Dtest=GeofenceTestiOS -Dbrowserstack.username=%BROWSERSTACK_USERNAME% -Dbrowserstack.accesskey=%BROWSERSTACK_ACCESSKEY%"
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Run Atlassian API Tests') {
            when {
                expression { params.TEST_TYPE == 'atlassian-api' }
            }
            steps {
                echo '🔌 Running Jira/Confluence API Tests...'
                bat 'mvn test -DsuiteXmlFile=src/test/resources/testng-atlassian.xml'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Generate Allure Report') {
            steps {
                echo '📊 Generating Allure Report...'
                allure([
                    includeProperties: false,
                    jdk: '',
                    results: [[path: 'target/allure-results']]
                ])
            }
        }

        stage('Create Jira Defects') {
            when {
                expression { params.CREATE_JIRA_DEFECTS && currentBuild.result != 'SUCCESS' }
            }
            steps {
                echo '🐛 Creating Jira defects for failed tests...'
                script {
                    // Parse test results and create Jira issues for failures
                    def testResults = junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                    if (testResults.failCount > 0) {
                        bat """
                            mvn exec:java -Dexec.mainClass="com.geofence.integrations.jira.JiraDefectCreator" ^
                            -Dexec.args="--build-number=${BUILD_NUMBER} --build-url=${BUILD_URL} --results-dir=target/surefire-reports"
                        """
                    }
                }
            }
        }

        stage('Publish to Confluence') {
            when {
                expression { params.PUBLISH_TO_CONFLUENCE }
            }
            steps {
                echo '📝 Publishing test report to Confluence...'
                script {
                    bat """
                        mvn exec:java -Dexec.mainClass="com.geofence.integrations.confluence.ConfluenceReportPublisher" ^
                        -Dexec.args="--build-number=${BUILD_NUMBER} --build-url=${BUILD_URL} --allure-url=${BUILD_URL}allure"
                    """
                }
            }
        }
    }

    post {
        always {
            echo '🧹 Cleaning up workspace...'
            cleanWs()
        }
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}
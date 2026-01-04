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
    }

    parameters {
        choice(
            name: 'TEST_TYPE',
            choices: ['all', 'android-local', 'android-browserstack', 'ios-browserstack', 'api'],
            description: 'Select which tests to run'
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
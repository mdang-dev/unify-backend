pipeline {
    agent any
    tools {
        jdk 'jdk21'
        maven 'maven-3.9.6'
    }
    environment {
        SCANNER_HOME = tool 'sonar-scanner'
        APP_NAME = "unify-backend"
        RELEASE = "1.0.0"
        DOCKER_USER = "minhdangdev"
        DOCKER_PASS = "dockerhub"
        IMAGE_NAME = "${DOCKER_USER}" + "/" + "${APP_NAME}"
        IMAGE_TAG = "${RELEASE}-${BUILD_NUMBER}"
    }
    stages {
        stage('clean workspace') {
            steps {
                cleanWs()
            }
        }
        stage('Checkout from Git') {
            steps {
                git branch: 'main', url: 'https://github.com/mdang-dev/unify-backend.git'
            }
        }
        stage('Sonarqube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube-server') {
                    sh '''
                        $SCANNER_HOME/bin/sonar-scanner -Dsonar.projectName=unify-backend \
                        -Dsonar.projectKey=unify-backend    
                    '''
                }
            }
        }
        stage('Quality Gate') {
            steps {
                script {
                    waitForQualityGate abortPipeline: false, credentialsId: 'sonarqube-token'
                }
            }
        }
        stage('Build') { 
            steps {
                sh 'mvn -ntp verify' 
            }
        }
        stage('TRIVY FS SCAN') {
            steps {
                sh "trivy fs . > trivyfs.txt"
            }
        }
    }
}
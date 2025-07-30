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
        stage('Build') { 
            steps {
                sh 'mvn -ntp clean package -DskipTests' 
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
        stage('TRIVY FS SCAN') {
            steps {
                sh "trivy fs . > trivyfs.txt"
            }
        }
        stage('Build & Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('', DOCKER_PASS) {
                        docker_image = docker.build "${IMAGE_NAME}"
                    }
                    docker.withRegistry('', DOCKER_PASS) {
                        docker_image.push("${IMAGE_TAG}")
                        docker_image.push('latest')
                    }
                }
            }   
        }
    }
}

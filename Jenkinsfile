pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'docker.io' // Docker Hub
        DOCKER_NAMESPACE = 'test' // Your Docker Hub username stored in Jenkins credentials
        APP_NAME = 'spring-petclinic'
        BUILD_NUMBER = "${env.BUILD_ID}"
    }
    
    stages {
        stage('Checkstyle') {
            steps {
                script {
                    echo 'Running Checkstyle validation'
                    sh './mvnw checkstyle:check'
                }
            }
        }
        
        stage('Code Coverage') {
            steps {
                script {
                    echo 'Running code coverage analysis'
                    sh './mvnw test jacoco:report'
                    junit '**/target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java'
                    )
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                script {
                    echo 'Running unit tests'
                    sh './mvnw test'
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    echo 'Building the application'
                    sh './mvnw clean package -DskipTests'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    echo 'Building Docker images'
                    sh 'docker-compose build'
                }
            }
        }
        
        stage('List Containers/Images') {
            steps {
                script {
                    echo 'Listing existing containers'
                    sh 'docker ps -a'
                    echo 'Listing existing images'
                    sh 'docker images'
                }
            }
        }
        
        stage('Docker Login') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub',
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    script {
                        echo 'Logging into Docker Hub'
                        sh "echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin ${DOCKER_REGISTRY}"
                    }
                }
            }
        }
        
        stage('Tag Images') {
            steps {
                script {
                    echo 'Tagging images with build number and latest'
                    def services = ['admin-server', 'api-gateway', 'config-server', 'customers-service', 'discovery-server', 'vets-service', 'visits-service']
                    services.each { service ->
                        sh """
                            docker tag ${APP_NAME}-${service}:latest ${DOCKER_NAMESPACE}/${APP_NAME}-${service}:${BUILD_NUMBER}
                            docker tag ${APP_NAME}-${service}:latest ${DOCKER_NAMESPACE}/${APP_NAME}-${service}:latest
                        """
                    }
                }
            }
        }
        
        stage('Push Images') {
            steps {
                script {
                    echo 'Pushing images to Docker Hub'
                    def services = ['admin-server', 'api-gateway', 'config-server', 'customers-service', 'discovery-server', 'vets-service', 'visits-service']
                    services.each { service ->
                        sh """
                            docker push ${DOCKER_NAMESPACE}/${APP_NAME}-${service}:${BUILD_NUMBER}
                            docker push ${DOCKER_NAMESPACE}/${APP_NAME}-${service}:latest
                        """
                    }
                }
            }
        }
        
        stage('Clean') {
            steps {
                script {
                    echo 'Cleaning up'
                    sh 'docker system prune -f'
                    sh './mvnw clean'
                    // Logout from Docker
                    sh 'docker logout'
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed - cleaning up workspace'
            cleanWs()
        }
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
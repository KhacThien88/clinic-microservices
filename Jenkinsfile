pipeline {
    agent any

    environment {
        projectName = 'lab01hcmus'
        DOCKERHUB_CREDENTIALS = credentials('dockerhub')
        DOCKERHUB_REPO = 'ktei8htop15122004/clinic-microservices'
    }

    stages {
        stage('Initialize') {
            steps {
                sh '''
                    java -version
                    mvn -version
                    docker -v
                '''
                sh '''
                    echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin || { echo "Docker Hub login failed. Check 'dockerhub' credentials in Jenkins."; exit 1; }
                '''
            }
        }

        stage('Test') {
            when {
                anyOf {
                    changeset "spring-petclinic-admin-server/**"
                }
            }
            parallel {
                stage('Checkstyle') {
                    steps {
                        timeout(time: 2, unit: 'MINUTES') {
                            script {
                                def changedModule = sh(script: "git diff --name-only HEAD^ HEAD | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
                                if (changedModule) {
                                    sh """
                                        cd ${changedModule}
                                        mvn validate
                                    """
                                } else {
                                    echo "No service module changed for Checkstyle."
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Build') {
            when {
                anyOf {
                    changeset "spring-petclinic-admin-server/**"
                }
            }
            steps {
                script {
                    def changedModule = sh(script: "git diff --name-only HEAD^ HEAD | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
                    if (changedModule) {
                        sh """
                            cd ${changedModule}
                            mvn clean install -DskipTests
                            echo "Artifact built: ${changedModule}/target/${changedModule}-3.4.1.jar"
                        """
                        archiveArtifacts artifacts: "${changedModule}/target/*.jar", allowEmptyArchive: true
                    } else {
                        echo "No service module changed for Build."
                    }
                }
            }
        }
        stage('Docker Build and Push') {
            when {
                anyOf {
                    changeset "spring-petclinic-admin-server/**"
                }
            }
            steps {
                script {
                    def changedModule = sh(script: "git diff --name-only HEAD^ HEAD | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
                    if (changedModule) {
                        def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                        def serviceName = changedModule.replace('spring-petclinic-', '')
                        def portMap = [
                            'admin-server': '9090'
                        ]
                        def exposedPort = portMap[serviceName] ?: '9090'
                        sh """
                            # Create a temporary build context
                            mkdir -p docker/build
                            cp ${changedModule}/target/${changedModule}-3.4.1.jar docker/build/
                            cd docker/build
                            docker build -f ../Dockerfile -t ${DOCKERHUB_REPO}:${serviceName}-${commitId} \
                                --build-arg ARTIFACT_NAME=${changedModule}-3.4.1 \
                                --build-arg EXPOSED_PORT=${exposedPort} .
                            echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin
                            docker push ${DOCKERHUB_REPO}:${serviceName}-${commitId} || { echo "Failed to push ${DOCKERHUB_REPO}:${serviceName}-${commitId}. Check Docker Hub credentials and repository access."; exit 1; }
                            # Clean up temporary build context
                            rm -rf docker/build
                        """
                    } else {
                        echo "No service module changed for Docker Build."
                    }
                }
            }
        }
    }
}
pipeline {
    agent any

    environment {
        projectName = 'lab01hcmus'
    }

    stages {
        stage('Initialize') {
            steps {
                sh '''
                    java -version
                    mvn -version
                    docker -v
                '''
            }
        }

        stage('Code') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Tests') {
            parallel {
                stage('Unit Test') {
                    steps {
                        timeout(time: 10, unit: 'MINUTES') {
                            sh '''
                                mvn test surefire-report:report
                                echo "surefire report generated in http://localhost:8080/job/$projectName/$BUILD_ID/execution/node/3/ws/target/site/surefire-report.html"
                            '''
                        }
                    }
                }

                stage('Checkstyle') {
                    steps {
                        timeout(time: 2, unit: 'MINUTES') {
                            sh 'mvn validate'
                        }
                    }
                }

                stage('Run Tests & Coverage') {
                    steps {
                        timeout(time: 10, unit: 'MINUTES') {
                            sh '''
                                mvn verify -Pcoverage
                                echo "Surefire report: http://localhost:8080/job/$projectName/$BUILD_ID/execution/node/3/ws/target/site/surefire-report.html"
                                echo "JaCoCo report:   http://localhost:8080/job/$projectName/$BUILD_ID/execution/node/3/ws/target/site/jacoco/index.html"
                            '''
                        }
                    }
                }
            }
        }

        stage('Docker - Build') {
            steps {
                sh 'docker image build -f dockerfile -t $projectName:$BUILD_ID .'
            }
        }

        stage('Docker - Tag') {
            steps {
                script {
                    parallel(
                        listContainer: {
                            sh 'docker container ls -a'
                        },
                        listImages: {
                            sh 'docker image ls -a'
                        },
                        toglatest: {
                            sh "docker tag $projectName:$BUILD_ID ktei8htop15122004/$projectName:$BUILD_ID"
                        },
                        togltest: {
                            sh "docker tag $projectName:$BUILD_ID ktei8htop15122004/$projectName:latest"
                        }
                    )
                }
            }
        }

        stage('Docker - Publish') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKER_REGISTRY_PWD', usernameVariable: 'DOCKER_REGISTRY_USER')]) {
                    sh '''
                        docker login -u $DOCKER_REGISTRY_USER -p $DOCKER_REGISTRY_PWD
                        echo 'login success...'
                        docker push ktei8htop15122004/$projectName:$BUILD_ID
                        docker push ktei8htop15122004/$projectName:latest
                        docker logout
                        echo 'logout...'
                    '''
                }
            }
        }

        stage('Docker - Clean') {
            steps {
                sh '''
                    echo 'Detecting local images...'
                    docker image ls
                    docker rmi -f $(docker images -aq)
                    docker image ls
                '''
            }
        }
    }
}

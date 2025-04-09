pipeline {
    agent any
    environment {
        projectName = "Lab01-HCMUS"
    }
    
    stages {
        stage('Initialize') {
            steps {
                script {
                    // Moved softwareVersion inside script block
                    sh '''#!/bin/bash
                    java -version
                    mvn -version
                    docker -v
                    echo ''
                    '''
                }
            }
        }
        
        stage('Code') {
            steps {
                sh '''#!/bin/bash
                mvn clean install -DskipTests
                '''
            }
        }
        
        stage('Tests') {
            parallel {
                stage('Unit Test') {
                    steps {
                        timeout(time: 10, unit: 'MINUTES') {
                            sh '''#!/bin/bash
                            mvn test surefire-report:report
                            echo "surefire report generated in http://localhost:8080/job/$projectName/$BUILD_ID/execution/node/3/ws/target/site/surefire-report.html"
                            '''
                        }
                    }
                }
                stage('Checkstyle') {
                    steps {
                        timeout(time: 2, unit: 'MINUTES') {
                            sh '''#!/bin/bash
                            mvn validate
                            '''
                        }
                    }
                }
                stage('Code Coverage') {
                    steps {
                        timeout(time: 1, unit: 'MINUTES') {
                            sh '''#!/bin/bash
                            mvn jacoco:report
                            echo "Jacoco report generated in http://localhost:8080/job/$projectName/$BUILD_ID/execution/node/3/ws/target/site/jacoco/index.html"
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Docker') {
            stages {
                stage('Build') {
                    steps {
                        sh '''#!/bin/bash
                        docker image build -f dockerfile -t $projectName:$BUILD_ID .
                        '''
                    }
                }
                stage('Tag') {
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
                stage('Publish') {
                    steps {
                        withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKER_REGISTRY_PWD', usernameVariable: 'DOCKER_REGISTRY_USER')]) {
                            sh '''#!/bin/bash
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
                stage('Clean') {
                    steps {
                        sh '''#!/bin/bash
                        docker image ls
                        echo 'Detecting local images...'
                        docker rmi -f $(docker images -aq)
                        docker image ls
                        '''
                    }
                }
            }
        }
    }
}
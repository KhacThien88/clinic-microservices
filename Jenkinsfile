node {
    projectName="Lab01-HCMUS";
    stage('code') {
        stage('coggle') {
            sh ''' #!/bin/bash
            mvn clean install -DskipTests
            '''
        }
    }
    
    stage('Tests') {
        parallel unitTest: {
            stage ('unitTest') {
                timeout(time: 10, unit: 'MINUTES') {
                    sh ''' #!/bin/bash
                    mvn test surefire-report:report
                    echo 'surefire report generated in http://localhost:8080/job/${projectName}/${env.BUILD_ID}/execution/node/3/ws/target/site/surefire-report.html'
                    '''
                } // timeout
            } // stage: unittest
        }, checkstyle: {
            stage ('checkstyle') {
                timeout(time: 2, unit: 'MINUTES') {
                    sh ''' #!/bin/bash
                    mvn validate
                    '''
                } // timeout
            } // stage: validate
        }, codecoverage: {
            stage ("codecoverage") {
                timeout(time: 1, unit: "MINUTES") {
                    sh ''' #!/bin/bash
                    mvn jacoco:report
                    echo 'Jacoco report generated in http://localhost:8080/job/${projectName}/${env.BUILD_ID}/execution/node/3/ws/target/site/jacoco/index.html'
                    '''
                } // timeout
            } // stage: Jacoco
        } // parallel
    } // stage: tests
    
    stage ("docker") {
        stage('build') {
            sh ''' #!/bin/bash
            docker image build -f dockerfile -t ${projectName}:${env.BUILD_ID} .
            '''
        } // stage: build
        stage('tag') {
            parallel listContainer: { 
                sh ''' #!/bin/bash
                docker container ls -a
                '''
            }, listImages: {
                sh ''' #!/bin/bash
                docker image ls -a
                '''
            }, toglatest: {
                sh ''' #!/bin/bash
                docker tag ${projectName}:${env.BUILD_ID} krishnamanchikalapudi/${projectName}:${env.BUILD_ID}
                '''
            }, togltest: {
                sh ''' #!/bin/bash
                docker tag ${projectName}:${env.BUILD_ID} krishnamanchikalapudi/${projectName}:latest
                '''
            }
        } // stage: tag
        stage('publish') {
            withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKER_REGISTRY_PWD', usernameVariable: 'DOCKER_REGISTRY_USER')]) {
                sh ''' #!/bin/bash
                docker login -u $DOCKER_REGISTRY_USER -p $DOCKER_REGISTRY_PWD
                echo 'login success...'
                docker push krishnamanchikalapudi/${projectName}:${env.BUILD_ID}
                docker push krishnamanchikalapudi/${projectName}:latest
                docker logout
                echo 'logout...'
                '''
            } // withCredentials: dockerhub
        } // stage: push
        stage('clean') {
            sh ''' #!/bin/bash
            docker image ls
            echo 'Detecting local images...'
            docker rmi -f $(docker images -aq)
            docker image ls
            '''
        } // stage: clean
    } // stage: docker
}

def softwareVersion() {
    sh ''' #!/bin/bash
    java -version
    mvn -version
    docker -v
    echo ''
    '''
}
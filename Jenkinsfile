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
                    changeset "spring-petclinic-discovery-server/**"
                }
            }
            parallel {
                stage('Unit Test') {
                    steps {
                        timeout(time: 10, unit: 'MINUTES') {
                            script {
                                def changedModule = sh(script: "git diff --name-only HEAD^ HEAD | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
                                if (changedModule) {
                                    sh """
                                        cd ${changedModule}
                                        mvn test
                                        mvn test surefire-report:report
                                        echo "Surefire report generated in http://localhost:8080/job/$projectName/$BUILD_ID/execution/node/3/ws/${changedModule}/target/site/surefire-report.html"
                                    """
                                    junit "${changedModule}/target/surefire-reports/*.xml"
                                } else {
                                    echo "No service module changed for Unit Test."
                                }
                            }
                        }
                    }
                }

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
                    changeset "spring-petclinic-discovery-server/**"
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
        stage('Coverage') {
                    steps {
                        timeout(time: 10, unit: 'MINUTES') {
                            script {
                                def changedModule = sh(script: "git diff --name-only HEAD^ HEAD | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
                                if (changedModule) {
                                    sh """
                                        cd ${changedModule}
                                        mvn verify -PbuildJacoco
                                        mkdir -p target/site/jacoco
                                        ls -l target
                                        find target -name "jacoco.xml"
                                        pwd
                                        echo "Surefire report: http://localhost:8080/job/$projectName/$BUILD_ID/execution/node/3/ws/${changedModule}/target/site/surefire-report.html"
                                        echo "JaCoCo report:   http://localhost:8080/job/$projectName/$BUILD_ID/execution/node/3/ws/${changedModule}/target/site/jacoco/index.html"
                                        if [ -f target/site/jacoco/jacoco.xml ]; then
                                            covered_line=\$(grep '<counter type="LINE"' target/site/jacoco/jacoco.xml | grep -oP 'covered="\\K[0-9]+' | paste -sd+ - | bc)
                                            missed_line=\$(grep '<counter type="LINE"' target/site/jacoco/jacoco.xml | grep -oP 'missed="\\K[0-9]+' | paste -sd+ - | bc)
                                            covered=\${covered_line:-0}
                                            missed=\${missed_line:-0}
                                            total=\$((covered + missed))
                                            if [ "\$total" -gt 0 ]; then
                                                percent=\$((100 * covered / total))
                                                echo "Line Coverage: \$percent% (\$covered / \$total)"
                                            else
                                                echo "No coverage data found."
                                            fi
                                        else
                                            echo "Jacoco report not found."
                                        fi
                                    """
                                    archiveArtifacts artifacts: "${changedModule}/target/site/jacoco/**", allowEmptyArchive: true
                                } else {
                                    echo "No service module changed for Coverage."
                                }
                            }
                        }
                    }
                }
        stage('Docker Build and Push') {
            when {
                anyOf {
                    changeset "spring-petclinic-discovery-server/**"
                }
            }
            steps {
                script {
                    def changedModule = sh(script: "git diff --name-only HEAD^ HEAD | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
                    if (changedModule) {
                        def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                        def serviceName = changedModule.replace('spring-petclinic-', '')
                        def portMap = [
                            'discovery-server': '8761'
                        ]
                        def exposedPort = portMap[serviceName] ?: '8761'
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
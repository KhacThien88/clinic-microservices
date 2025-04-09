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
        stage('Test') {
            when {
                anyOf {
                    changeset "spring-petclinic-admin-server/**"
                    changeset "spring-petclinic-customers-service/**"
                    changeset "spring-petclinic-vets-service/**"
                    changeset "spring-petclinic-visits-service/**"
                    changeset "spring-petclinic-genai-service/**"
                    changeset "spring-petclinic-config-server/**"
                    changeset "spring-petclinic-discovery-server/**"
                    changeset "spring-petclinic-api-gateway/**"
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

                stage('Coverage') {
                    steps {
                        timeout(time: 10, unit: 'MINUTES') {
                            script {
                                def changedModule = sh(script: "git diff --name-only HEAD^ HEAD | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
                                if (changedModule) {
                                    sh """
                                        cd ${changedModule}
                                        mvn test
                                        mkdir -p target/site/jacoco
                                        find . -name "jacoco.xml" -exec cp {} target/site/jacoco/ \\;
                                        echo "Surefire report: http://localhost:8080/job/$projectName/$BUILD_ID/execution/node/3/ws/${changedModule}/target/site/surefire-report.html"
                                        echo "JaCoCo report:   http://localhost:8080/job/$projectName/$BUILD_ID/execution/node/3/ws/${changedModule}/target/site/jacoco/index.html"
                                        if [ -f target/site/jacoco/jacoco.xml ]; then
                                            covered=\$(grep -oP 'covered="\\\\K[0-9]+' target/site/jacoco/jacoco.xml | paste -sd+ - | bc)
                                            missed=\$(grep -oP 'missed="\\\\K[0-9]+' target/site/jacoco/jacoco.xml | paste -sd+ - | bc)
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
            }
        }

        stage('Build') {
            when {
                anyOf {
                    changeset "spring-petclinic-admin-server/**"
                    changeset "spring-petclinic-customers-service/**"
                    changeset "spring-petclinic-vets-service/**"
                    changeset "spring-petclinic-visits-service/**"
                    changeset "spring-petclinic-genai-service/**"
                    changeset "spring-petclinic-config-server/**"
                    changeset "spring-petclinic-discovery-server/**"
                    changeset "spring-petclinic-api-gateway/**"
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
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/site/jacoco/**', allowEmptyArchive: true
        }
    }
}
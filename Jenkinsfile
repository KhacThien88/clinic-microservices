pipeline {
    agent any

    environment {
        projectName = 'lab01hcmus'
        GITHUB_CREDENTIALS = credentials('github-account')
    }
    stages {
        stage('Initialize') {
            steps {
                sh '''
                    java -version
                    mvn -version
                    docker -v
                '''
            
                step([
                    $class: 'GitHubCommitStatusSetter',
                    reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/KhacThien88/clinic-microservices"],
                    contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build"],
                    statusResultSource: [$class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", state: "PENDING", message: "Build started"]]]
                ])
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
        success {
            step([
                $class: 'GitHubCommitStatusSetter',
                reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/KhacThien88/clinic-microservices"],
                contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build"],
                statusResultSource: [$class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", state: "SUCCESS", message: "Build passed"]]]
            ])
        }
        failure {
            step([
                $class: 'GitHubCommitStatusSetter',
                reposSource: [$class: "ManuallyEnteredRepositorySource", url: "hhttps://github.com/KhacThien88/clinic-microservices"],
                contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build"],
                statusResultSource: [$class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", state: "FAILURE", message: "Build failed"]]]
            ])
        }
        always {
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/site/jacoco/**', allowEmptyArchive: true
        }
    }
}
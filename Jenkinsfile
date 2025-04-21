pipeline {
    agent any
    parameters {
        string(name: 'DEPLOY_BRANCH', defaultValue: '', description: 'Branch to deploy for the changed service (e.g., dev_vets_service)')
    }
    environment {
        projectName = 'lab01hcmus'
        GITHUB_TOKEN = credentials('token-github')
        DOCKERHUB_CREDENTIALS = credentials('dockerhub')
        DOCKERHUB_REPO = 'ktei8htop15122004/clinic-microservices'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                // Fetch all branches to ensure DEPLOY_BRANCH is available
                sh 'git fetch origin'
            }
        }
        stage('Initialize') {
            steps {
                sh '''
                    java -version
                    mvn -version
                    docker -v
                '''
                // Verify Docker Hub login early to catch authentication issues
                sh '''
                    echo \$DOCKERHUB_CREDENTIALS_PSW | docker login -u \$DOCKERHUB_CREDENTIALS_USR --password-stdin || { echo "Docker Hub login failed. Check 'dockerhub' credentials in Jenkins."; exit 1; }
                '''
                step([
                    $class: "GitHubCommitStatusSetter",
                    reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/KhacThien88/clinic-microservices"],
                    contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build-status"],
                    errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
                    statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: "START BUILD", state: "PENDING"]] ]
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
                expression { env.DEPLOY_BRANCH == '' }
            }
            parallel {
                stage('Unit Test') {
                    steps {
                        timeout(time: 10, unit: 'MINUTES') {
                            script {
                                def changedModule = sh(script: "git diff --name-only origin/main...HEAD | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
                                if (changedModule) {
                                    sh """
                                        cd ${changedModule}
                                        mvn test surefire-report:report
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
                                def changedModule = sh(script: "git diff --name-only origin/main...HEAD | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
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
                                def changedModule = sh(script: "git diff --name-only origin/main...HEAD | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
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
                expression { env.DEPLOY_BRANCH == '' }
            }
            steps {
                script {
                    def changedModule = sh(script: "git diff --name-only origin/main...HEAD | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
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
                    changeset "spring-petclinic-customers-service/**"
                    changeset "spring-petclinic-vets-service/**"
                    changeset "spring-petclinic-visits-service/**"
                    changeset "spring-petclinic-genai-service/**"
                    changeset "spring-petclinic-config-server/**"
                    changeset "spring-petclinic-discovery-server/**"
                    changeset "spring-petclinic-api-gateway/**"
                }
                expression { env.DEPLOY_BRANCH == '' }
            }
            steps {
                script {
                    def changedModule = sh(script: "git diff --name-only origin/main...HEAD | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
                    if (changedModule) {
                        def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                        def serviceName = changedModule.replace('spring-petclinic-', '')
                        // Define service-specific ports (verify these match server.port in each service's application.yml)
                        def portMap = [
                            'admin-server': '9090',
                            'customers-service': '8081',
                            'vets-service': '8083',
                            'visits-service': '8082',
                            'genai-service': '9966',
                            'config-server': '8888',
                            'discovery-server': '8761',
                            'api-gateway': '8080'
                        ]
                        def exposedPort = portMap[serviceName] ?: '9966'
                        sh """
                            # Create a temporary build context
                            mkdir -p docker/build
                            cp ${changedModule}/target/${changedModule}-3.4.1.jar docker/build/
                            cd docker/build
                            docker build -f ../Dockerfile -t ${DOCKERHUB_REPO}:${serviceName}-${commitId} \
                                --build-arg ARTIFACT_NAME=${changedModule}-3.4.1 \
                                --build-arg EXPOSED_PORT=${exposedPort} .
                            echo \$DOCKERHUB_CREDENTIALS_PSW | docker login -u \$DOCKERHUB_CREDENTIALS_USR --password-stdin
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
        stage('Developer Build (CD)') {
            when {
                expression { env.DEPLOY_BRANCH != '' }
            }
            steps {
                script {
                    def services = [
                        'admin-server',
                        'customers-service',
                        'vets-service',
                        'visits-service',
                        'genai-service',
                        'config-server',
                        'discovery-server',
                        'api-gateway'
                    ]

                    // Validate DEPLOY_BRANCH
                    def branchExists = sh(script: "git ls-remote --heads origin ${env.DEPLOY_BRANCH} | wc -l", returnStdout: true).trim() == '1'
                    if (!branchExists) {
                        error "Branch ${env.DEPLOY_BRANCH} does not exist in the repository."
                    }

                    // Checkout the DEPLOY_BRANCH
                    sh "git checkout ${env.DEPLOY_BRANCH}"

                    def changedModule = sh(script: "git diff --name-only origin/main...${env.DEPLOY_BRANCH} | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
                    def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    def serviceName = changedModule ? changedModule.replace('spring-petclinic-', '') : ''

                    // Build and push the image for the specified branch if a module is changed
                    if (changedModule && env.DEPLOY_BRANCH) {
                        // Build the service's JAR if not already built
                        sh """
                            cd ${changedModule}
                            mvn clean install -DskipTests
                        """
                        // Define service-specific ports (verify these match server.port in each service's application.yml)
                        def portMap = [
                            'admin-server': '9090',
                            'customers-service': '8081',
                            'vets-service': '8083',
                            'visits-service': '8082',
                            'genai-service': '8084',
                            'config-server': '8888',
                            'discovery-server': '8761',
                            'api-gateway': '8080'
                        ]
                        def exposedPort = portMap[serviceName] ?: '9966'
                        sh """
                            mkdir -p docker/build
                            cp ${changedModule}/target/${changedModule}-3.4.1.jar docker/build/
                            cd docker/build
                            docker build -f ../Dockerfile -t ${DOCKERHUB_REPO}:${serviceName}-${commitId} \
                                --build-arg ARTIFACT_NAME=${changedModule}-3.4.1 \
                                --build-arg EXPOSED_PORT=${exposedPort} .
                            echo \$DOCKERHUB_CREDENTIALS_PSW | docker login -u \$DOCKERHUB_CREDENTIALS_USR --password-stdin
                            docker push ${DOCKERHUB_REPO}:${serviceName}-${commitId} || { echo "Failed to push ${DOCKERHUB_REPO}:${serviceName}-${commitId}. Check Docker Hub credentials and repository access."; exit 1; }
                            # Clean up temporary build context
                            rm -rf docker/build
                        """
                    }

                    // Deploy services with port mappings
                    echo "Deploying services..."
                    def portMap = [
                        'admin-server': '9090',
                        'customers-service': '8081',
                        'vets-service': '8083',
                        'visits-service': '8082',
                        'genai-service': '8084',
                        'config-server': '8888',
                        'discovery-server': '8761',
                        'api-gateway': '8080'
                    ]
                    services.each { svc ->
                        def imageTag = (svc == serviceName && env.DEPLOY_BRANCH && changedModule) ? "${svc}-${commitId}" : "${svc}-latest"
                        def exposedPort = portMap[svc] ?: '9966'
                        echo "Deploying ${DOCKERHUB_REPO}:${imageTag} for service ${svc} on port ${exposedPort}"
                        sh "docker run -d --name ${svc}-${imageTag} -p ${exposedPort}:${exposedPort} ${DOCKERHUB_REPO}:${imageTag}"
                    }
                }
            }
        }
        stage('Cleanup') {
            when {
                expression { env.DEPLOY_BRANCH != '' }
            }
            steps {
                script {
                    def services = [
                        'admin-server',
                        'customers-service',
                        'vets-service',
                        'visits-service',
                        'genai-service',
                        'config-server',
                        'discovery-server',
                        'api-gateway'
                    ]
                    def changedModule = sh(script: "git diff --name-only origin/main...${env.DEPLOY_BRANCH} | grep -o 'spring-petclinic-[a-z-]*' | head -1", returnStdout: true).trim()
                    def commitId = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    def serviceName = changedModule ? changedModule.replace('spring-petclinic-', '') : ''

                    echo "Cleaning up deployed containers..."
                    services.each { svc ->
                        def imageTag = (svc == serviceName && env.DEPLOY_BRANCH && changedModule) ? "${svc}-${commitId}" : "${svc}-latest"
                        sh """
                            docker rm -f ${svc}-${imageTag} || true
                            echo "Cleaned up container for ${DOCKERHUB_REPO}:${imageTag}"
                        """
                    }
                }
            }
        }
    }
    post {
        success {
            step([
                $class: "GitHubCommitStatusSetter",
                reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/KhacThien88/clinic-microservices"],
                contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build-status"],
                errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
                statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: "Build success", state: "SUCCESS"]] ]
            ])
            echo "Build and cleanup completed successfully. Log: ${env.BUILD_URL}"
        }
        failure {
            step([
                $class: "GitHubCommitStatusSetter",
                reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/KhacThien88/clinic-microservices"],
                contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build-status"],
                errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
                statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: "Build failed", state: "FAILURE"]] ]
            ])
            echo "Build or cleanup failed. Check log: ${env.BUILD_URL}"
        }
        always {
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/site/jacoco/**', allowEmptyArchive: true
        }
    }
}
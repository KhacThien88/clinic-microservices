pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('Checkout & Detect Changes') {
            steps {
                checkout scm
                script {
                    def changes = getGitChanges()
                    env.CHANGED_SERVICES = changes.join(',')
                    echo "Modified services: ${env.CHANGED_SERVICES}"
                }
                // Notify GitHub about build start
                script {
                    publishChecks name: 'Jenkins CI', 
                               status: 'IN_PROGRESS', 
                               summary: 'Build started'
                }
            }
        }

        stage('Test') {
            when {
                anyOf {
                    expression { env.CHANGED_SERVICES }
                    expression { currentBuild.previousBuild == null }
                }
            }
            steps {
                script {
                    def services = env.CHANGED_SERVICES ? env.CHANGED_SERVICES.split(',') : getAllServices()
                    
                    services.each { service ->
                        dir(service) {
                            try {
                                // Notify GitHub about test start
                                publishChecks name: "Jenkins CI - ${service}", 
                                           status: 'IN_PROGRESS', 
                                           summary: 'Tests running'
                                
                                // Run tests with JaCoCo coverage
                                sh """
                                    ../mvnw clean test \\
                                        -Dmaven.test.failure.ignore=true \\
                                        org.jacoco:jacoco-maven-plugin:prepare-agent
                                """
                                
                                // Publish test results
                                junit "**/target/surefire-reports/*.xml"
                                
                                // Check coverage (fail if <70%)
                                def coverage = getTestCoverage("${service}/target/site/jacoco/jacoco.xml")
                                echo "${service} test coverage: ${coverage}%"
                                
                                if (coverage < 70.0) {
                                    publishChecks name: "Jenkins CI - ${service}", 
                                               status: 'FAILURE', 
                                               summary: "Coverage ${coverage}% < 70%"
                                    error("${service} coverage ${coverage}% < 70% - Pipeline failed!")
                                } else {
                                    publishChecks name: "Jenkins CI - ${service}", 
                                               status: 'SUCCESS', 
                                               summary: "Tests passed (${coverage}% coverage)"
                                }
                                
                            } catch (err) {
                                publishChecks name: "Jenkins CI - ${service}", 
                                           status: 'FAILURE', 
                                           summary: 'Tests failed'
                                error("${service} tests failed!")
                            }
                        }
                    }
                }
            }
        }

        stage('Build') {
            when {
                anyOf {
                    expression { env.CHANGED_SERVICES }
                    expression { currentBuild.previousBuild == null }
                }
            }
            steps {
                script {
                    def services = env.CHANGED_SERVICES ? env.CHANGED_SERVICES.split(',') : getAllServices()
                    
                    services.each { service ->
                        dir(service) {
                            sh "../mvnw clean package -DskipTests"
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                def status = currentBuild.result == 'SUCCESS' ? 'SUCCESS' : 'FAILURE'
                publishChecks name: 'Jenkins CI', 
                           status: status, 
                           summary: "Build ${currentBuild.result}"
            }
            cleanWs()
        }
    }
}

def getGitChanges() {
    def changes = []
    def gitDiff = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
    
    if (gitDiff) {
        // Map changed files to services
        gitDiff.split('\n').each { file ->
            if (file.startsWith('spring-petclinic-')) {
                def service = file.split('/')[0]
                if (!changes.contains(service)) {
                    changes.add(service)
                }
            }
        }
    }
    return changes
}

def getAllServices() {
    return sh(script: """
        ls -d spring-petclinic-*/ | sed 's/\\///'
    """, returnStdout: true).trim().split('\n')
}

def getTestCoverage(reportPath) {
    def coverage = sh(script: """
        python3 -c "\\
            import xml.etree.ElementTree as ET; \\
            tree = ET.parse('${reportPath}'); \\
            root = tree.getroot(); \\
            print(root.find('.//counter[@type=\"LINE\"]').get('covered'));"
    """, returnStdout: true).trim()
    
    return coverage.toFloat()
}
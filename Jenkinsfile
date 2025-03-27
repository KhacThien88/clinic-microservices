pipeline {
    agent any
    
    options {
        skipDefaultCheckout false
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    
    environment {
        SERVICES = 'vets-service,visits-service,customers-service,api-gateway'
    }
    
    stages {
        stage('Initialize') {
            steps {
                script {
                    // Clean workspace before checkout
                    cleanWs()
                    
                    // Checkout source code
                    checkout scm
                    
                    sh 'chmod +x mvnw'
                    // Verify git is available
                    sh 'git --version'
                }
            }
        }
        
        stage('Detect Changes') {
            steps {
                script {
                    // Get changed files with improved detection
                    def changedFiles = getChangedFiles()
                    
                    // Determine which services were modified
                    def changedServices = getChangedServices(changedFiles)
                    
                    // If manual trigger, build all services
                    if (currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause')) {
                        echo "Manual trigger detected - building all services"
                        env.CHANGED_SERVICES = env.SERVICES
                    } else {
                        // Automatic trigger - only build changed services
                        env.CHANGED_SERVICES = changedServices.join(',')
                    }
                    
                    echo "Changed services: ${env.CHANGED_SERVICES}"
                    echo "All services: ${env.SERVICES}"
                }
            }
        }
        
        stage('Build and Test') {
            when {
                expression { return env.CHANGED_SERVICES }
            }
            
            parallel {
                stage('Build and Test vets-service') {
                    when {
                        expression { return env.CHANGED_SERVICES.contains('vets-service') }
                    }
                    
                    steps {
                        buildAndTestService('vets-service')
                    }
                }
                
                stage('Build and Test visits-service') {
                    when {
                        expression { return env.CHANGED_SERVICES.contains('visits-service') }
                    }
                    
                    steps {
                        buildAndTestService('visits-service')
                    }
                }
                
                stage('Build and Test customers-service') {
                    when {
                        expression { return env.CHANGED_SERVICES.contains('customers-service') }
                    }
                    
                    steps {
                        buildAndTestService('customers-service')
                    }
                }
                
                stage('Build and Test api-gateway') {
                    when {
                        expression { return env.CHANGED_SERVICES.contains('api-gateway') }
                    }
                    
                    steps {
                        buildAndTestService('api-gateway')
                    }
                }
            }
        }
    }
    
    post {
        always {
            script {
                // Archive important artifacts
                archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                sh 'rm -rf /var/lib/jenkins/jobs/*/builds/*'
                // Clean up workspace
                cleanWs()
            }
        }
        
        // success {
        //     echo 'Pipeline completed successfully'
        // }
        
        // failure {
        //     echo 'Pipeline failed'
        //     emailext body: '${DEFAULT_CONTENT}', 
        //             subject: 'Pipeline Failed: ${JOB_NAME} - Build #${BUILD_NUMBER}', 
        //             to: 'KTeightop1512@gmail.com'
        // }
    }
}

// Helper functions
def getChangedFiles() {
    def changedFiles = []
    
    try {
        // Method 1: Check SCM changes (works for automatic triggers)
        if (currentBuild.changeSets) {
            currentBuild.changeSets.each { changeSet ->
                changeSet.items.each { item ->
                    item.affectedFiles.each { file ->
                        changedFiles << file.path
                    }
                }
            }
            echo "Detected changes via SCM: ${changedFiles}"
        }
        
        // Method 2: Git diff (works for manual triggers and fallback)
        if (changedFiles.isEmpty()) {
            def gitDiff = sh(script: "git diff --name-only HEAD~1", returnStdout: true).trim()
            if (gitDiff) {
                changedFiles = gitDiff.split('\n').toList()
                echo "Detected changes via git diff: ${changedFiles}"
            }
        }
        
        // Method 3: Last commit (final fallback)
        if (changedFiles.isEmpty()) {
            def lastCommit = sh(script: "git show --name-only --pretty=format:''", returnStdout: true).trim()
            if (lastCommit) {
                changedFiles = lastCommit.split('\n').toList()
                echo "Detected changes via last commit: ${changedFiles}"
            }
        }
    } catch (Exception e) {
        echo "Error detecting changed files: ${e.message}"
        // If we can't determine changes, build all services
        changedFiles = ['ALL'] // Special value that will trigger all services
    }
    
    return changedFiles.unique()
}

def getChangedServices(changedFiles) {
    def services = []
    
    // If we couldn't determine changes, build all services
    if (changedFiles == ['ALL']) {
        return env.SERVICES.tokenize(',')
    }
    
    changedFiles.each { file ->
        def normalizedFile = file.toLowerCase()
        
        if (normalizedFile.contains('vets-service')) {
            services << 'vets-service'
        }
        else if (normalizedFile.contains('visits-service')) {
            services << 'visits-service'
        }
        else if (normalizedFile.contains('customers-service')) {
            services << 'customers-service'
        }
        else if (normalizedFile.contains('api-gateway')) {
            services << 'api-gateway'
        }
        else {
            echo "File not mapped to any service: ${file}"
        }
    }
    
    return services.unique()
}

def buildAndTestService(serviceName) {
    dir("spring-petclinic-${serviceName}") {
        // Stage 1: Build
        stage("Build ${serviceName}") {
            sh "../mvnw clean install -DskipTests"
        }
        
        // Stage 2: Test with coverage
        stage("Test ${serviceName}") {
            try {
                // Run tests with JaCoCo coverage
                sh "../mvn org.jacoco:jacoco-maven-plugin:0.8.10:prepare-agent"
                sh "../mvn test"
                sh "../mvn org.jacoco:jacoco-maven-plugin:0.8.10:report"
                sh "../mvnw test jacoco:report"
                
                // Publish test results
                junit "**/target/surefire-reports/*.xml"
                
                // Publish coverage report
                publishHTML(target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: false,
                    keepAll: true,
                    reportDir: "target/site/jacoco",
                    reportFiles: "index.html",
                    reportName: "${serviceName} Coverage Report"
                ])
                
                // Verify coverage meets threshold
                def coverage = getCoveragePercentage("target/site/jacoco/jacoco.xml")
                echo "${serviceName} test coverage: ${coverage}%"
                
                if (coverage < 70) {
                    error("${serviceName} test coverage ${coverage}% is below required 70% threshold")
                }
            } catch (Exception e) {
                currentBuild.result = 'FAILURE'
                error("Failed testing ${serviceName}: ${e.message}")
            }
        }
    }
}

def getCoveragePercentage(coverageFile) {
    try {
        def parser = new XmlParser().parse(coverageFile)
        def counter = parser.counter.find { it.@type == 'INSTRUCTION' }
        def covered = counter.@covered.toDouble()
        def missed = counter.@missed.toDouble()
        def percentage = (covered / (covered + missed)) * 100
        return Math.round(percentage * 100) / 100
    } catch (Exception e) {
        echo "Error parsing coverage file: ${e.message}"
        return 0
    }
}
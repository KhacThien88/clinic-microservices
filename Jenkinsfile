pipeline {
    agent any
    
    options {
        skipDefaultCheckout true
    }
    
    stages {
        stage('Check Changed Services') {
            steps {
                script {
                    // Get changed files
                    def changedFiles = getChangedFiles()
                    
                    // Determine which services were modified
                    def changedServices = getChangedServices(changedFiles)
                    
                    // Set environment variable for downstream stages
                    env.CHANGED_SERVICES = changedServices.join(',')
                }
                
                echo "Changed services: ${env.CHANGED_SERVICES}"
            }
        }
        
        stage('Parallel Build and Test') {
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
            }
        }
    }
    
    post {
        always {
            // Clean up workspace
            cleanWs()
        }
    }
}

// Helper functions
def getChangedFiles() {
    // For multibranch pipeline, get changes from SCM
    def changeLogSets = currentBuild.changeSets
    def changedFiles = []
    
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            def files = new ArrayList(entry.affectedFiles)
            for (int k = 0; k < files.size(); k++) {
                changedFiles.add(files[k].path)
            }
        }
    }
    
    return changedFiles.unique()
}

def getChangedServices(changedFiles) {
    def services = []
    
    changedFiles.each { file ->
        if (file.startsWith('spring-petclinic-vets-service')) {
            services << 'vets-service'
        }
        else if (file.startsWith('spring-petclinic-visits-service')) {
            services << 'visits-service'
        }
        else if (file.startsWith('spring-petclinic-customers-service')) {
            services << 'customers-service'
        }
    }
    
    return services.unique()
}

def buildAndTestService(serviceName) {
    dir("spring-petclinic-${serviceName}") {
        // Stage 1: Build
        stage("Build ${serviceName}") {
            sh "./mvnw clean package -DskipTests"
        }
        
        // Stage 2: Test with coverage
        stage("Test ${serviceName}") {
            try {
                // Run tests with JaCoCo coverage
                sh "./mvnw test jacoco:report"
                
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
                throw e
            }
        }
    }
}

def getCoveragePercentage(coverageFile) {
    def parser = new XmlParser().parse(coverageFile)
    def counter = parser.counter.find { it.@type == 'INSTRUCTION' }
    def covered = counter.@covered.toDouble()
    def missed = counter.@missed.toDouble()
    def percentage = (covered / (covered + missed)) * 100
    return Math.round(percentage * 100) / 100
}
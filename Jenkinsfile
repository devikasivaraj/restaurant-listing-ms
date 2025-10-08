pipeline {
  // FIX 1: Reverting to 'agent any' to bypass the "Invalid agent type docker" error.
  // The Docker agent usage must now be implemented within a 'script' block for the build stages.
  agent any

  environment {
    // Variable to hold Docker Hub credentials retrieved from Jenkins Credentials Manager
    DOCKERHUB_CREDENTIALS = credentials('DOCKER_HUB_CREDENTIAL')
    
    // Set the image tag to the current Jenkins build number (e.g., "1", "2", "3")
    VERSION = "${env.BUILD_ID}"
  }
  
  stages {
    
    stage('Initialization Check') {
      steps {
        sh 'echo "Jenkins host agent initialization successful. Starting CI/CD pipeline."'
      }
    }

    // FIX 2: Grouping Maven stages to run inside the Java 22 Docker container
    stage('Code Build & Analysis (Java 22)') {
      steps {
        script {
          // Use a Docker block inside the script to run all subsequent steps in the correct Java 22 environment.
          // This isolates the build environment and resolves the previous Java version error.
          docker.image('maven:3.9-eclipse-temurin-22').inside('-v /var/run/docker.sock:/var/run/docker.run') {
            
            // --- Maven Build ---
            stage('Maven Build') {
              sh 'echo "--- Running Maven Build inside Java 22 Docker container ---"'
              // Compile and package the application, skipping tests initially
              sh 'mvn clean package -DskipTests'
            }

            // --- Run Tests ---
            stage('Run Tests') {
              sh 'echo "--- Running Tests inside Java 22 Docker container ---"'
              // Run unit tests and generate JaCoCo coverage reports
              sh 'mvn test'
            }

            // --- SonarQube Analysis ---
            stage('SonarQube Analysis') {
              sh 'echo "--- Running SonarQube Analysis inside Java 22 Docker container ---"'
              // FIX 3: Updated SonarQube IP and ensured correct URL format
              sh 'mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install sonar:sonar -Dsonar.host.url=http://13.37.220.128:9000 -Dsonar.login=squ_a4cd4d12609e34e03e5099a3f92e05e0562a4e36'
            }
          }
        }
      }
    }

    // This stage remains outside the Docker container, executing on 'agent any'.
    stage('Check code coverage') {
      steps {
        script {
          def token = "squ_a4cd4d12609e34e03e5099a3f92e05e0562a4e36"
          def sonarQubeBaseUrl = "http://13.37.220.128:9000"
          def componentKey = "com.devika:restaurantlisting"
          def coverageThreshold = 80.0

          // Fetch coverage measure from SonarQube API
          def response = sh (
              script: "curl -s -H 'Authorization: Bearer ${token}' '${sonarQubeBaseUrl}/api/measures/component?component=${componentKey}&metricKeys=coverage'",
              returnStdout: true
          ).trim()

          // Use 'jq' to extract the coverage value
          def coverageString = sh (
              script: "echo '${response}' | jq -r '.component.measures[] | select(.metric == \"coverage\").value'",
              returnStdout: true
          ).trim()
          
          def coverage = 0.0
          
          if (!coverageString.isEmpty()) {
              coverage = coverageString.toDouble()
          }

          echo "Code Coverage for component ${componentKey}: ${coverage}%"

          if (coverage < coverageThreshold) {
              error "Coverage (${coverage}%) is below the threshold of ${coverageThreshold}%. Aborting the pipeline."
          } else {
              echo "Coverage gate passed. Coverage is ${coverage}%."
          }
        }
      }
    } 

    // This stage runs on 'agent any' to access the host's Docker daemon.
    stage('Docker Build and Push') {
      agent any 
      steps {
        sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
        sh "docker build -t devika044/restaurant-listing-service:${VERSION} ."
        sh "docker push devika044/restaurant-listing-service:${VERSION}"
      }
    } 

    // This stage runs on 'agent any' to perform Git operations.
    stage('Update Image Tag in GitOps') {
      agent any // Continue on master/host node for Git operations
      steps {
        // Checkout the GitOps repository containing Kubernetes manifests
        checkout scmGit(
          branches: [[name: '*/master']], 
          extensions: [], 
          userRemoteConfigs: [
            [ 
              credentialsId: 'git-ssh', // Credential ID for the private key
              url: 'git@github.com:devikasivaraj/deployment-folder-master.git'
            ]
          ]
        )
        
        script {
          // Use 'sed' to replace the image tag in your deployment manifest
          sh '''
            sed -i "s|image:.*restaurant-listing-service:.*|image: devika044/restaurant-listing-service:${VERSION}|" aws/restaurant-manifest.yml
          '''
          
          // Set Git user details before committing
          sh 'git config user.email "jenkins@pipeline.com"'
          sh 'git config user.name "Jenkins Pipeline Bot"'
          sh 'git add .'
          sh "git commit -m 'CI: Update restaurant-listing-service image tag to ${VERSION}'"
          
          // Use sshagent block to load the private key for authentication before pushing
          sshagent(['git-ssh']) {
            sh('git push origin master') 
          }
        }
      }
    }
    
    stage('Cleanup Workspace') {
      steps {
        // Clean up the workspace to save disk space
        deleteDir()
      }
    }
  }
}
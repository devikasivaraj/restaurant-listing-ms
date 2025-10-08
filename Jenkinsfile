pipeline {
  // CRITICAL FIX: Switching from 'agent any' to a Docker agent.
  // This bypasses the corrupted shell execution environment on the Jenkins master node
  // by running the entire pipeline inside a clean, pre-configured Maven/Java container.
  agent {
    docker {
      // Use a Maven image with Java 21 to match the Dockerfile and modern standards
      image 'maven:3.9-eclipse-temurin-21' 
      // Mount the Docker socket so the agent container can access the host's Docker daemon
      args '-v /var/run/docker.sock:/var/run/docker.sock' 
    }
  }

  environment {
    // Variable to hold Docker Hub credentials retrieved from Jenkins Credentials Manager
    DOCKERHUB_CREDENTIALS = credentials('DOCKER_HUB_CREDENTIAL')
    
    // Set the image tag to the current Jenkins build number (e.g., "1", "2", "3")
    VERSION = "${env.BUILD_ID}"
  }

  // The 'tools' block is unnecessary when using a Maven Docker image.
  /* tools {
    maven "Maven"
  } */

  stages {
    
    stage('Initialization Check') {
      steps {
        // This stage now serves its original purpose as a checkpoint.
        sh 'echo "Jenkins agent initialization successful. Starting CI/CD pipeline."'
      }
    }

    stage('Maven Build') {
      steps {
        // Compile and package the application, skipping tests initially
        sh 'mvn clean package -DskipTests'
      }
    }

    stage('Run Tests') {
      steps {
        // Run unit tests and generate JaCoCo coverage reports
        sh 'mvn test'
      }
    }

    stage('SonarQube Analysis') {
      steps {
        // Run analysis, ensuring the sonar.login token is masked or fetched from credentials if possible
        sh 'mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install sonar:sonar -Dsonar.host.url=http://35.181.51.183:9000/ -Dsonar.login=squ_a4cd4d12609e34e03e5099a3f92e05e0562a4e36'
      }
    }

    stage('Check Code Coverage Gate') {
      steps {
        script {
          // NOTE ON SECURITY: Storing the Sonar token directly in the script is risky.
          // In a real environment, fetch this from Jenkins Credentials Manager.
          def token = "squ_a4cd4d12609e34e03e5099a3f92e05e0562a4e36"
          // Corrected the SonarQube URL to remove the extra colon and the /api suffix, 
          // as the API path should be added in the curl request.
          def sonarQubeBaseUrl = "http://35.181.51.183:9000"
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

    // NOTE: Because Docker commands must be run outside the Maven container, we must use a second,
    // dedicated Docker agent for the Docker stages. This agent will run on the master node 
    // where Docker is installed.
    stage('Docker Build and Push') {
      agent any // Revert to master/host node for Docker operations
      steps {
        // Use environment variables for secure login
        sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
        
        // Build the image using the current build ID as the tag
        sh "docker build -t devika044/restaurant-listing-service:${VERSION} ."
        
        // Push the new image tag
        sh "docker push devika044/restaurant-listing-service:${VERSION}"
      }
    } 

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
          
          // Git operations
          sh 'git config user.email "jenkins@pipeline.com"'
          sh 'git config user.name "Jenkins Pipeline Bot"'
          sh 'git add .'
          sh "git commit -m 'CI: Update restaurant-listing-service image tag to ${VERSION}'"
          
          // Use sshagent block to load the private key for authentication
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
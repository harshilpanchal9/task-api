pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "harshilpanchal09/task-api"
        SNYK_TOKEN   = credentials('snyk-token')
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/harshilpanchal9/task-api.git'
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build'
            }
        }

        stage('Snyk SAST + SCA') {
            steps {
                sh 'npm install -g snyk'
                sh 'snyk auth $SNYK_TOKEN'
                sh 'snyk test --all-sub-projects || true'
                sh 'snyk code test || true'
            }
        }

        stage('Docker Build & Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        docker build -t $DOCKER_IMAGE:$BUILD_NUMBER .
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push $DOCKER_IMAGE:$BUILD_NUMBER
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    kubectl apply -f k8s/configmap.yaml
                    kubectl apply -f k8s/deployment.yaml
                    kubectl apply -f k8s/service.yaml
                    kubectl set image deployment/task-api \
                        task-api=$DOCKER_IMAGE:$BUILD_NUMBER
                    kubectl rollout status deployment/task-api
                '''
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                    sleep 10
                    curl -f http://192.168.49.2:30080/health
                '''
            }
        }
    }

    post {
        always {
            echo "Pipeline complete — build #${BUILD_NUMBER}"
        }
        failure {
            echo "Build failed — check Snyk reports"
        }
    }
}
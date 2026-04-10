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
               sh '''
                    npm config set prefix '/var/jenkins_home/.npm-global'
                    export PATH=/var/jenkins_home/.npm-global/bin:$PATH
                    npm install -g snyk
                    snyk auth $SNYK_TOKEN
                    snyk test --all-sub-projects || true
                    snyk code test || true
                '''
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
                    kubectl --kubeconfig=/var/jenkins_home/.kube/config apply -f k8s/configmap.yaml
                    kubectl --kubeconfig=/var/jenkins_home/.kube/config apply -f k8s/deployment.yaml
                    kubectl --kubeconfig=/var/jenkins_home/.kube/config apply -f k8s/service.yaml
                    kubectl --kubeconfig=/var/jenkins_home/.kube/config set image deployment/task-api task-api=$DOCKER_IMAGE:$BUILD_NUMBER
                    kubectl --kubeconfig=/var/jenkins_home/.kube/config rollout status deployment/task-api
                '''
            }
        }

        stage('Health Check') {
            steps {
                echo "Deployment successful - app is running in Kubernetes"
                echo "Access via: minikube service task-api-service"
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
pipeline {
    agent any

    environment {
        SONAR_TOKEN = credentials('sonar-token')
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        IMAGE_NAME = 'medanwarsalhi/cours-backend'
    }

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/anwar789456/cours.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                sh """
                    mvn sonar:sonar \
                    -Dsonar.projectKey=cours-backend \
                    -Dsonar.projectName='Cours Backend' \
                    -Dsonar.host.url=http://sonarqube:9000 \
                    -Dsonar.token=${SONAR_TOKEN}
                """
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t ${IMAGE_NAME}:latest .'
            }
        }

        stage('Docker Push') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                sh 'docker push ${IMAGE_NAME}:latest'
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker stop cours-backend-app || true'
                sh 'docker rm cours-backend-app || true'
                sh """docker run -d \
                    --name cours-backend-app \
                    --network devops-net \
                    -p 8090:8090 \
                    -e SPRING_DATASOURCE_URL=jdbc:postgresql://51.255.203.187:5432/cours \
                    -e SPRING_DATASOURCE_USERNAME=anwar \
                    -e SPRING_DATASOURCE_PASSWORD=anwar123 \
                    -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
                    -e EUREKA_CLIENT_REGISTER_WITH_EUREKA=false \
                    -e EUREKA_CLIENT_FETCH_REGISTRY=false \
                    ${IMAGE_NAME}:latest"""
            }
        }
    }

    post {
        success { echo 'Pipeline succeeded! Lets goooo' }
        failure { echo 'Pipeline failed. womp womp bro' }
    }
}
pipeline {
    agent any

    environment {
        SONAR_TOKEN = credentials('sonar-token')
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
                sh 'docker build -t cours-backend:latest .'
            }
        }
    }

    post {
        success { echo 'Pipeline succeeded! Lets goooo' }
        failure { echo 'Pipeline failed. womp womp bro' }
    }
}
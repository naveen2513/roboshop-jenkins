def call() {
    pipeline {

        agent {
            node {
                label 'workstation'
            }
        }

        options {
            ansiColor('xterm')
        }


        stages {

            stage('Code Quality') {
                steps {
                    //sh 'sonar-scanner -Dsonar.projectKey=${component} -Dsonar.host.url=http://172.31.8.27:9000 -Dsonar.login=admin -Dsonar.password=admin123 -Dsonar.qualitygate.wait=true'
                    sh 'echo Code Quality'
                }
            }

            stage('Unit Test Cases') {
                steps {
                    sh 'echo Unit tests'
                    //sh 'npm test'
                }
            }

            stage('CheckMarx SAST Scan') {
                steps {
                    sh 'echo Checkmarx Scan'
                }
            }

            stage('CheckMarx SCA Scan') {
                steps {
                    sh 'echo Checkmarx SCA Scan'
                }
            }

            stage('Release Application') {
                when {
                    expression {
                        env.TAG_NAME ==~ ".*"
                    }
                }
                steps {
                    sh 'echo $TAG_NAME >VERSION'
                    sh 'zip -r ${component}-${TAG_NAME}.zip *'
                    // Deleting this file as it is not needed.
                    //sh 'zip -d ${component}-${TAG_NAME}.zip Jenkinsfile'
                    //sh 'curl -f -v -u admin:admin123 --upload-file ${component}-${TAG_NAME}.zip http://172.31.82.149:8081/repository/${component}/${component}-${TAG_NAME}.zip'
                    sh 'aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 633788536644.dkr.ecr.us-east-1.amazonaws.com'
                    sh 'docker build -t 633788536644.dkr.ecr.us-east-1.amazonaws.com/${component}:${TAG_NAME} .'
                    sh 'docker push 633788536644.dkr.ecr.us-east-1.amazonaws.com/${component}:${TAG_NAME}'
                }
            }


        }

        post {
            always {
                cleanWs()
            }
        }

    }


}
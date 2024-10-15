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

        environment {
            NEXUS = credentials('NEXUS')
        }


        stages {

            stage('Code Compile') {
                steps {
                    sh 'mvn compile'
                }
            }

            stage('Code Quality') {
                steps {
//          sh 'ls -l'
//          sh 'sonar-scanner -Dsonar.projectKey=${component} -Dsonar.host.url=http://172.31.8.27:9000 -Dsonar.login=admin -Dsonar.password=admin123 -Dsonar.qualitygate.wait=true -Dsonar.java.binaries=./target'
                    sh 'echo COde Quality'
                }
            }

            stage('Unit Test Cases') {
                steps {
                    sh 'echo Unit tests'
                    //sh 'mvn test'
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
                    sh 'mvn package ; cp target/${component}-1.0.jar ${component}.jar'
                    sh 'echo $TAG_NAME >VERSION'
                    sh 'if [ -n "${schema_dir}" ]; then  aws ssm put-parameter --name "${component}.schema.checksum" --type "String" --value "$(md5sum schema/*.sql | awk "{print \\$1}")" --overwrite; fi '
                    //sh 'zip -r ${component}-${TAG_NAME}.zip ${component}.jar VERSION ${schema_dir}'
                    //sh 'curl -f -v -u ${NEXUS_USR}:${NEXUS_PSW} --upload-file ${component}-${TAG_NAME}.zip http://172.31.82.149:8081/repository/${component}/${component}-${TAG_NAME}.zip'
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
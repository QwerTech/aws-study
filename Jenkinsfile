#!/usr/bin/env groovy

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timeout(time: 1, unit: 'HOURS')
    }

    tools {
        jdk 'openjdk-11'
        maven 'default'
        dockerTool 'docker-latest'
    }

    environment {
        POM_VERSION = getVersion()
        AWS_ECR_REGION = 'eu-central-1'
        AWS_ECS_SERVICE = 'aws-study-service'
        AWS_ECS_TASK_DEFINITION = 'aws-study-taskdefinition'
        AWS_ECS_COMPATIBILITY = 'EC2'
        AWS_ECS_NETWORK_MODE = 'awsvpc'
        AWS_ECS_CPU = '256'
        AWS_ECS_MEMORY = '512'
        AWS_ECS_CLUSTER = 'main-cluster'
        AWS_ECS_TASK_DEFINITION_PATH = './ecs/container-definition-update-image.json'
        AWS_ECR_URL = credentials('AWS_REPOSITORY_URL_SECRET')
    }

    stages {
        stage('Build & Test') {
            steps {
                withMaven(options: [artifactsPublisher(), mavenLinkerPublisher(), dependenciesFingerprintPublisher(disabled: true), jacocoPublisher(disabled: true), junitPublisher(disabled: true)]) {
                    sh "mvn -B -U clean package"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                    script {
                        docker.build("${AWS_ECR_URL}:${POM_VERSION}", ".")
                    }
            }
        }

        stage('Push image to ECR') {
            steps {
                withAWS(region: "${AWS_ECR_REGION}", credentials: 'personal-aws-ecr') {
                    script {
                        def login = ecrLogin()
//                        sh('#!/bin/sh -e\n' + "${login}") // hide logging
                        sh("${login}")
                        docker.image("${AWS_ECR_URL}:${POM_VERSION}").push()
                    }
                }
            }
        }

        stage('Deploy in ECS') {
            steps {
                withCredentials([string(credentialsId: 'AWS_EXECUTION_ROL_SECRET', variable: 'AWS_ECS_EXECUTION_ROL')]) {
                    script {
                        updateContainerDefinitionJsonWithImageVersion()
                        sh("/usr/local/bin/aws ecs register-task-definition --region ${AWS_ECR_REGION} --family ${AWS_ECS_TASK_DEFINITION}  --requires-compatibilities ${AWS_ECS_COMPATIBILITY} --network-mode ${AWS_ECS_NETWORK_MODE} --cpu ${AWS_ECS_CPU} --memory ${AWS_ECS_MEMORY} --container-definitions file://${AWS_ECS_TASK_DEFINITION_PATH}")
                        def taskRevision = sh(script: "/usr/local/bin/aws ecs describe-task-definition --task-definition ${AWS_ECS_TASK_DEFINITION} | egrep \"revision\" | tr \"/\" \" \" | awk '{print \$2}' | sed 's/\"\$//'", returnStdout: true)
                        sh("/usr/local/bin/aws ecs update-service --cluster ${AWS_ECS_CLUSTER} --service ${AWS_ECS_SERVICE} --task-definition ${AWS_ECS_TASK_DEFINITION}:${taskRevision}")
                    }
                }
            }
        }
    }

//    post {
//        always {
//            withCredentials([string(credentialsId: 'AWS_REPOSITORY_URL_SECRET', variable: 'AWS_ECR_URL')]) {
//                junit allowEmptyResults: true, testResults: 'target/surfire-reports/*.xml'
//                publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'target/site/jacoco-ut/', reportFiles: 'index.html', reportName: 'Unit Testing Coverage', reportTitles: 'Unit Testing Coverage'])
//                jacoco(execPattern: 'target/jacoco-ut.exec')
////                deleteDir()
//                sh 'docker rmi ${AWS_ECR_URL}:${POM_VERSION}'
//            }
//        }
//    }
}


def getVersion() {
    def pom = readMavenPom file: './pom.xml'
    return pom.version
}

def updateContainerDefinitionJsonWithImageVersion() {
    def containerDefinitionJson = readJSON file: AWS_ECS_TASK_DEFINITION_PATH, returnPojo: true
    containerDefinitionJson[0 as String]['image'] = "${AWS_ECR_URL}:${POM_VERSION}".inspect()
    echo "task definiton json: ${containerDefinitionJson}"
    writeJSON file: AWS_ECS_TASK_DEFINITION_PATH, json: containerDefinitionJson
}

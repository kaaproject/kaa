env.GITHUB_HTTP_URL = 'https://github.com/jbt-iot/kaa'
env.GITHUB_GIT_URL = 'git@github.com:jbt-iot/kaa.git'

// noinspection GroovyAssignabilityCheck
properties([
        disableConcurrentBuilds(),

//        pipelineTriggers([
//                issueCommentTrigger('.*test.*')
//        ]),

        [
                $class         : 'BuildBlockerProperty',
                blockingJobs   : '.*jbt-.*',
                useBuildBlocker: true
        ],

        [
                $class       : 'GithubProjectProperty',
                displayName  : '',
                projectUrlStr: env.GITHUB_HTTP_URL
        ],

        [
                $class  : 'BuildDiscarderProperty',
                strategy: [
                        $class               : 'LogRotator',
                        artifactDaysToKeepStr: '7',
                        artifactNumToKeepStr : '7',
                        daysToKeepStr        : '7',
                        numToKeepStr         : '7'
                ]
        ],

])

def gitCredentialsId = '1e989ebf-11b4-458c-8ef7-90256dd62c87'

def jbtInfrastructureBranch = 'master'

def isPR() {
    return env.BRANCH_NAME ==~ /PR-\d+/
}

def kaaTag="untagged"

node('master') {

    stage('init') {
        step([$class: 'WsCleanup'])

        env.PATH = "${env.HOME}/.local/bin:${env.PATH}"
        sh "which aws"

        sh "which mvn"
    }

    stage('git') {
        sshagent(credentials: ["${gitCredentialsId}"]) {

            dir('kaa') {
                if (isPR()) {
                    echo "Checkout PR: ${env.BRANCH_NAME}"
                    git(
                            branch: "${env.CHANGE_TARGET}",
                            credentialsId: "${gitCredentialsId}",
                            url: 'git@github.com:jbt-iot/kaa.git'
                    )

                    sh "git fetch origin pull/${CHANGE_ID}/merge:${env.BRANCH_NAME}"
                    sh "git checkout ${env.BRANCH_NAME}"

                } else {
                    echo "Checkout branch: ${env.BRANCH_NAME}"
                    git(
                            branch: "${env.BRANCH_NAME}",
                            credentialsId: "${gitCredentialsId}",
                            url: 'git@github.com:jbt-iot/kaa.git'
                    )
                }

                def kaaCommit = sh(
                        script: "git rev-parse ${env.BRANCH_NAME}",
                        returnStdout: true
                ).trim().take(8)

                currentBuild.description = "kaa: ${env.BRANCH_NAME}-${kaaCommit}"
                kaaTag = "${env.BRANCH_NAME}-${kaaCommit}"
            }

            dir('jbt-infrastructure') {
                git(
                        branch: "${jbtInfrastructureBranch}",
                        credentialsId: "${gitCredentialsId}",
                        url: 'git@github.com:jbt-iot/jbt-infrastructure.git'
                )
                def bldCommit = sh(
                        script: "git rev-parse ${jbtInfrastructureBranch}",
                        returnStdout: true
                ).trim().take(8)

                currentBuild.description += " inf: ${jbtInfrastructureBranch}-${bldCommit}"
            }
        }
    }


    stage('build kaa deb') {
        dir('kaa') {
            sh "mvn clean package"
        }
    }

    stage('build kaa docker') {
        if (isPR()) {
            echo "skip docker build for PR- pseudobranch"
        } else {
            dir('jbt-infrastructure') {
                sh """#!/bin/bash
                set -e
                set -x
                cd nix

                `aws ecr get-login --region us-east-1 --no-include-email`
                
                cp ../../kaa/server/node/target/kaa-node.deb .
                cp ../../kaa/server/node/target/sdk/cpp/kaa-cpp-ep-sdk-0.9.0.tar.gz .
                
                docker build -t 138150065595.dkr.ecr.us-east-1.amazonaws.com/kaa:${kaaTag} .
                docker push 138150065595.dkr.ecr.us-east-1.amazonaws.com/kaa:${kaaTag}

                docker build -t 138150065595.dkr.ecr.us-east-1.amazonaws.com/kaa:${env.BRANCH_NAME} .
                docker push 138150065595.dkr.ecr.us-east-1.amazonaws.com/kaa:${env.BRANCH_NAME}
            """
            }
        }
    }
}//node


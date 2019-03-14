env.GITHUB_HTTP_URL = 'https://github.com/jbt-iot/kaa'
env.GITHUB_GIT_URL = 'git@github.com:jbt-iot/kaa.git'

// noinspection GroovyAssignabilityCheck
properties([
        disableConcurrentBuilds(),

//        pipelineTriggers([
//                issueCommentTrigger('.*test.*')
//        ]),
        parameters([
                string(
                        defaultValue: '0.9.1-SNAPSHOT',
                        description: 'Kaa version',
                        name: 'VERSION'
                ),
                string(
                        defaultValue: 'http://10.0.1.5:5000',
                        description: 'Aptly URL',
                        name: 'APTLY_URL'
                )

        ]),

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
def jbtKaaAgentBuilderBranch = 'master'
def jbtBackendBranch = 'master'
def jbtIotBranch='master'

def isPR() {
    return env.BRANCH_NAME ==~ /PR-\d+/
}

def isMaster() {
    return env.BRANCH_NAME == '0.9.0-patched'
}

def kaaBranch="none"
def kaaCommit="00000000"
def kaaTag="untagged"

node(isPR()?'slave-02':'master') {

    stage('init') {
        step([$class: 'WsCleanup'])

        sh "echo 'dce9897a5359f29284224295c0d179e1 ./docker-compose' > ./docker-compose.md5"
        sh "md5sum -c ./docker-compose.md5 || curl -L https://github.com/docker/compose/releases/download/1.21.0/docker-compose-`uname -s`-`uname -m` -o ./docker-compose"
        sh "chmod +x ./docker-compose"
        env.PATH = "${env.WORKSPACE}:${env.PWD}:${env.PATH}"
        sh "which docker-compose"

        env.PATH = "${env.HOME}/.local/bin:${env.PATH}"
        sh "which aws"

        env.M2_HOME = "${tool name: 'mvn360', type: 'maven'}"
        env.PATH = "${env.M2_HOME}/bin:${env.PATH}"
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

                    kaaBranch = "${env.CHANGE_TARGET}"

                } else {
                    echo "Checkout branch: ${env.BRANCH_NAME}"
                    git(
                            branch: "${env.BRANCH_NAME}",
                            credentialsId: "${gitCredentialsId}",
                            url: 'git@github.com:jbt-iot/kaa.git'
                    )
                    kaaBranch = "${env.BRANCH_NAME}"
                }

                kaaCommit = sh(
                        script: "git rev-parse ${env.BRANCH_NAME}",
                        returnStdout: true
                ).trim().take(8)

                kaaTag = "${kaaBranch}-${kaaCommit}"

                currentBuild.description = "kaa: ${env.BRANCH_NAME}-${kaaCommit}"
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


            dir('jbt-kaa-agent-builder') {
                git(
                        branch: "${jbtKaaAgentBuilderBranch}",
                        credentialsId: "${gitCredentialsId}",
                        url: 'git@github.com:jbt-iot/jbt-kaa-agent-builder.git'
                )
            }

            dir('jbt-backend') {
                git(
                        branch: "${jbtBackendBranch}",
                        credentialsId: "${gitCredentialsId}",
                        url: 'git@github.com:jbt-iot/jbt-backend.git'
                )
            }

            dir('JBT-IoT') {
                git(
                        branch: "${jbtIotBranch}",
                        credentialsId: "${gitCredentialsId}",
                        url: 'git@github.com:jbt-iot/JBT-IoT.git'
                )
            }

        }
    }


    stage('build kaa deb') {
        dir('kaa') {
            sh "KAA_VERSION=${env.VERSION} envsubst < ./server/node/src/deb/control/control.template > ./server/node/src/deb/control/control"
            if (isPR()) {
                sh "mvn -P compile-gwt,cassandra-dao,postgresql-dao,kafka clean package verify"
            } else {
                sh "mvn -DskipTests -DskipITs -P compile-gwt,cassandra-dao,postgresql-dao,kafka clean package"
            }
        }
    }

    stage('build kaa docker') {
        if (isPR()) {
            echo "skip build kaa docker for PR builds"
            return
        }

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

            docker build -t 138150065595.dkr.ecr.us-east-1.amazonaws.com/kaa:${kaaBranch} .
            docker push 138150065595.dkr.ecr.us-east-1.amazonaws.com/kaa:${kaaBranch}
        """
        }

    }

    stage('run local env'){
        if (isPR()) {
            echo "skip run local env for PR builds"
            return
        }

        dir('jbt-backend') {
            sh "./gradlew clean build -x test"
        }

        dir('jbt-kaa-agent-builder') {
            withCredentials([usernamePassword(credentialsId: 'JBT_QA_E2E_CREDENTIALS', usernameVariable: 'JBT_QA_E2E_USER', passwordVariable: 'JBT_QA_E2E_PASS'),
                             usernamePassword(credentialsId: 'KAA_CREDS_STAGE', usernameVariable: 'JBT_QA_E2E_KAA_USERNAME', passwordVariable: 'JBT_QA_E2E_KAA_PASSWORD'),
                             usernamePassword(credentialsId: 'KAA_CREDS_STAGE', usernameVariable: 'KAA_USERNAME', passwordVariable: 'KAA_PASSWORD'),
                             string(credentialsId: '5b51337c-78c3-4677-9153-f9eca88ee8bc', variable: 'AWS_ACCESS_KEY_ID'),
                             string(credentialsId: 'd27d9f8f-018d-4ed0-ac7b-749e21721e64', variable: 'AWS_SECRET_ACCESS_KEY'),
                             string(credentialsId: '5a2efc62-9fbc-4096-9bd0-719d30cd7f2b', variable: 'AWS_DEFAULT_REGION'),
                             string(credentialsId: '5a2efc62-9fbc-4096-9bd0-719d30cd7f2b', variable: 'AWS_REGION'),
                             string(credentialsId: 'ARTIFACTORY_PASS', variable: 'ARTIFACTORY_PASS'),
                             string(credentialsId: 'JBT_QA_E2E_KAA_PASSWORD', variable: 'KAA_PASSWORD'),

            ]) {
                sh "export KAA_TAG=${kaaTag}; export COMPOSE_PROJECT=${kaaCommit}; ./run_local.sh"
            }

        }

    }

    stage('e2e vs local env'){
        if (isPR()) {
            echo "skip e2e check for PR builds"
            return
        }
        try{
            def kaaAgentTag = parseKaaAgentTag()
            build(
                    job: 'jbt-iot/jbt-qa-e2e/master',
                    parameters: [
                            string(name: 'JBT_QA_E2E_APPLICATION_URL', value: 'http://localhost:8084'),
                            string(name: 'JBT_QA_E2E_KAA_HOST', value: 'localhost'),
                            string(name: 'JBT_QA_E2E_CASSANDRA_HOST', value: 'localhost'),
                            string(name: 'JBT_QA_E2E_BOOTSTRAP_SERVERS', value: 'localhost:9092'),
                            string(name: 'JBT_QA_E2E_AGENT_IMAGE_TAG', value: kaaAgentTag)
                    ]
            )
        }catch (e) {
            echo "FAILED: $e"
            throw e
        } finally {
            dir('jbt-kaa-agent-builder') {
                withCredentials([usernamePassword(credentialsId: 'JBT_QA_E2E_CREDENTIALS', usernameVariable: 'JBT_QA_E2E_USER', passwordVariable: 'JBT_QA_E2E_PASS'),
                                 usernamePassword(credentialsId: 'KAA_CREDS_STAGE', usernameVariable: 'JBT_QA_E2E_KAA_USERNAME', passwordVariable: 'JBT_QA_E2E_KAA_PASSWORD'),
                                 usernamePassword(credentialsId: 'KAA_CREDS_STAGE', usernameVariable: 'KAA_USERNAME', passwordVariable: 'KAA_PASSWORD'),

                ]) {
                    saveLogs("${kaaCommit}")
                    sh "export JBT_BACKEND_DIR=`cd ../jbt-backend;pwd`; docker-compose --project-name ${kaaCommit} down -t 1 || true"
                }
            }
        }
    }

    stage('aptly') {
        if (!isPR()) {
            dir('kaa') {
                sh "curl -F 'file=@./server/node/target/kaa-node.deb;filename=kaa-node_${env.VERSION}_amd64.deb' ${env.APTLY_URL}/api/files/jbt"
                sh "curl -X POST ${env.APTLY_URL}/api/repos/jbt/file/jbt?forceReplace=1"
                sh "curl -X PUT -H 'Content-Type: application/json' --data '{\"Signing\": {\"GpgKey\": \"Nborisenko <nborisenko@kaaiot.io>\"}}' ${env.APTLY_URL}/api/publish/:./xenial"
            }
        }
    }

    stage('deploy-on-stage') {
        if (isMaster()) {
            build(
                    job: 'deploy_kaa_stage',
                    parameters: [
                            string(name: 'VERSION', value: "${env.VERSION}")
                    ]
            )
        }
    }

}//node

def parseKaaAgentTag() {
    return sh(
            script: "cat jbt-kaa-agent-builder/kaa-agent.tag | awk -F= '{print \$2}'",
            returnStdout: true
    ).trim()
}

def saveLogs(String project) {

    fetchDockerLog("${project}_jbt-kaa-appender-cfg_1")
    fetchDockerLog("${project}_ui_1")
    fetchDockerLog("${project}_kaa_1")
    fetchDockerLog("${project}_web-app_1")
    fetchDockerLog("${project}_code-regeneration-service_1")
    fetchDockerLog("${project}_kafka_1")
    fetchDockerLog("${project}_spark-worker_1")
    fetchDockerLog("${project}_cassandra-kaa_1")
    fetchDockerLog("${project}_zoo_1")
    fetchDockerLog("${project}_postgres_1")
    fetchDockerLog("${project}_spark-master_1")
    fetchDockerLog("${project}_redis_1")
    fetchDockerLog("${project}_cassandra_1")
    fetchSparkLogs(project)
    archiveArtifacts allowEmptyArchive: true, artifacts: '**/*.log.gz'
}

def fetchDockerLog(String container) {
    sh "docker logs '${container}' 2>&1 | gzip -vc > '${container}.log.gz'"
}

def fetchSparkLogs(String project, String filter = " ") {
    sh """docker exec ${project}_spark-worker_1 bash -c 'find /spark/work -name stderr | grep driver | xargs grep -e "$filter"' | gzip -vc > ${project}_spark_worker.driver.log.gz"""
    sh """docker exec ${project}_spark-worker_1 bash -c 'find /spark/work -name stderr | grep app    | xargs grep -e "$filter"' | gzip -vc > ${project}_spark_worker.app.log.gz"""
}

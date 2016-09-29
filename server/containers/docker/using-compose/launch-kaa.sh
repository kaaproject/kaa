#!/bin/bash
#
# Copyright 2014-2016 CyberVision, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
DATABASE_SQL_NOSQL=$1
KAA_NODE_SCALE=$2
INTERNAL_LOCALHOST=$3

VARIATIONS_OF_DATABASES=("mariadb-mongodb" "mariadb-cassandra" "postgresql-mongodb" "postgresql-cassandra")

isValid=false
DEFAULT_KAA_SERVICE_NAME=kaa_

DEFAULT_INTERNAL_ADMIN_PORT=8080
DEFAULT_ADMIN_PORT=10080
DEFAULT_BOOTSTRAP_TCP=10088
DEFAULT_BOOTSTRAP_HTTP=10089
DEFAULT_OPERATIONS_TCP=10097
DEFAULT_OPERATIONS_HTTP=10099

IMAGE_VERSION_MARIADB="5.5"
IMAGE_VERSION_POSTGRESQL="9.4"
IMAGE_VERSION_MONGODB="3.2"
IMAGE_VERSION_CASSANDRA="3.7"

KAA_ADMIN_UI_PORTS=()
STRING_NAMES_NODES_KAA=()

isValidDatabases() {
if [ -z "$DATABASE_SQL_NOSQL" ]; then
    echo "Please choose correct variation of SQL and NoSQL databases"
    for i in "${VARIATIONS_OF_DATABASES[@]}"
    do
        echo $i
    done
    exit 0;
fi
    return 0;
}

configureAndStartThirdPartyComponents() {
    for i in "${VARIATIONS_OF_DATABASES[@]}"
    do
        if [ "$i" = "$DATABASE_SQL_NOSQL" ]; then
            isValid=true
            IFS="-" read -r -a array <<< $i
            SQL_PROVIDER_NAME=${array[0]}
            NOSQL_PROVIDER_NAME=${array[1]}
            sed -i "s/\(env_file *: *\).*/\1${SQL_PROVIDER_NAME}-sql-example.env/" third-party-docker-compose.yml
            sed -i "s/\(SQL_PROVIDER_NAME *= *\).*/\1${SQL_PROVIDER_NAME}/" kaa-docker-compose.yml.template
            sed -i "s/\(NOSQL_PROVIDER_NAME *= *\).*/\1${NOSQL_PROVIDER_NAME}/" kaa-docker-compose.yml.template
            case $SQL_PROVIDER_NAME in
                mariadb)
                    SQL_PROVIDER_NAME="mariadb:"$IMAGE_VERSION_MARIADB
                    ;;
                postgresql)
                    SQL_PROVIDER_NAME="postgres:"$IMAGE_VERSION_POSTGRESQL
                    ;;
                *)
                    exit 1
                    ;;
            esac
            case $NOSQL_PROVIDER_NAME in
                mongodb)
                    NOSQL_PROVIDER_NAME="mongo:"$IMAGE_VERSION_MONGODB
                    ;;
                cassandra)
                    NOSQL_PROVIDER_NAME="cassandra:"$IMAGE_VERSION_CASSANDRA
                    ;;
                *)
                    exit 1
                    ;;
            esac
            sed -i -e "21s@.*@    image: ${SQL_PROVIDER_NAME}@" third-party-docker-compose.yml
            sed -i -e "34s@.*@    image: ${NOSQL_PROVIDER_NAME}@" third-party-docker-compose.yml

            docker-compose -f third-party-docker-compose.yml up -d
        fi
    done

    if [ "$isValid" = "false" ]; then
        echo "No such variation of databases.
Please choose correct variation of SQL and NoSQL databases:"
        for i in "${VARIATIONS_OF_DATABASES[@]}"
        do
            echo $i
        done
        exit 1
    fi
}

stopRunningContainers() {
    RUNNING_CONTAINERS=`docker ps -q`
    if ! [ -z "$RUNNING_CONTAINERS" ]; then
        docker stop $(docker ps -q)
    fi
    return 0;
}

removeAvailableContainers() {
    AVAILABLE_CONTAINERS=`docker ps -a -q`
    if ! [ -z "$AVAILABLE_CONTAINERS" ]; then
        docker rm $(docker ps -a -q)
    fi
    return 0;
}

configureOneNodeKaa() {
    KAA_ADMIN_UI_PORTS+=(${DEFAULT_ADMIN_PORT})
    STRING_NAMES_NODES_KAA+=(${DEFAULT_KAA_SERVICE_NAME}0)
    cat kaa-docker-compose.yml.template | sed \
    -e "s|{{KAA_SERVICE_NAME}}|${DEFAULT_KAA_SERVICE_NAME}0|g" \
    -e "s|{{ADMIN_PORT}}|${DEFAULT_ADMIN_PORT}|g" \
    -e "s|{{BOOTSTRAP_TCP}}|${DEFAULT_BOOTSTRAP_TCP}|g" \
    -e "s|{{BOOTSTRAP_HTTP}}|${DEFAULT_BOOTSTRAP_HTTP}|g" \
    -e "s|{{OPERATIONS_TCP}}|${DEFAULT_OPERATIONS_TCP}|g" \
    -e "s|{{OPERATIONS_HTTP}}|${DEFAULT_OPERATIONS_HTTP}|g" \
    > kaa-docker-compose.yml
    return 0;
}

configureClusterNodesKaa() {
    configureOneNodeKaa
    KAA_NODE_SCALE=$(( KAA_NODE_SCALE - 1 ))
    for i in `seq 1 $KAA_NODE_SCALE`; do
        KAA_ADMIN_UI_PORT+=( $(( DEFAULT_ADMIN_PORT + $i * 100 )) )
        STRING_NAMES_NODES_KAA+=(${DEFAULT_KAA_SERVICE_NAME}${i})
        KAA_ADMIN_UI_PORTS+=(${KAA_ADMIN_UI_PORT})
        ex -sc "41i|`cat kaa-docker-compose.yml.template | tail -37 | head -22 | sed \
        -e "s|{{KAA_SERVICE_NAME}}|${DEFAULT_KAA_SERVICE_NAME}${i} |g" \
        -e "s|{{ADMIN_PORT}}|${KAA_ADMIN_UI_PORT}|g" \
        -e "s|{{BOOTSTRAP_TCP}}|$(( DEFAULT_BOOTSTRAP_TCP + $i * 100 ))|g" \
        -e "s|{{BOOTSTRAP_HTTP}}|$(( DEFAULT_BOOTSTRAP_HTTP + $i * 100 ))|g" \
        -e "s|{{OPERATIONS_TCP}}|$(( DEFAULT_OPERATIONS_TCP + $i * 100 ))|g" \
        -e "s|{{OPERATIONS_HTTP}}|$(( DEFAULT_OPERATIONS_HTTP + $i * 100 ))|g" \
        `" -cx kaa-docker-compose.yml
    done
    return 0;
}

createNginxConfFiles() {
    INTERNAL_HOST_KAA=`docker inspect --format='{{(index (index .NetworkSettings.Networks ) "usingcompose_front-tier").IPAddress}}' usingcompose_${DEFAULT_KAA_SERVICE_NAME}0_1`
    cat kaa-nginx-config/nginx.conf.template | sed \
        -e "s|{{PROXY_HOST_KAA}}|${INTERNAL_HOST_KAA}|g" \
        -e "s|{{PROXY_PORT}}|${DEFAULT_ADMIN_PORT}|g" \
    > kaa-nginx-config/kaa-nginx.conf
    cat kaa-nginx-config/default.conf.template | sed \
        -e "s|{{NGINX_PORT}}|${DEFAULT_INTERNAL_ADMIN_PORT}|g" \
        -e "s|{{NGINX_HOST}}|${INTERNAL_LOCALHOST}|g" \
    > kaa-nginx-config/kaa-default.conf
}

stopRunningContainers

isValidDatabases
configureAndStartThirdPartyComponents

[ -n "$INTERNAL_LOCALHOST" ] || INTERNAL_LOCALHOST=`ip route get 8.8.8.8 | awk '{print $NF; exit}'`
echo "TRANSPORT_PUBLIC_INTERFACE =" $INTERNAL_LOCALHOST
sed -i "s/\(TRANSPORT_PUBLIC_INTERFACE *= *\).*/\1${INTERNAL_LOCALHOST}/" kaa-example.env

if [ -z "$KAA_NODE_SCALE" ]; then
    configureOneNodeKaa
    docker-compose -f kaa-docker-compose.yml up -d "${DEFAULT_KAA_SERVICE_NAME}0"
    createNginxConfFiles
    docker-compose -f kaa-docker-compose.yml up -d kaa_lb
else
    configureClusterNodesKaa
    docker-compose -f kaa-docker-compose.yml up -d ${STRING_NAMES_NODES_KAA[@]}
    createNginxConfFiles
    j=1
    for i in "${KAA_ADMIN_UI_PORTS[@]}"
    do
        if [ "$i" =  "$DEFAULT_ADMIN_PORT" ]; then
            createNginxConfFiles
        else
            INTERNAL_HOST_KAA=`docker inspect --format='{{(index (index .NetworkSettings.Networks ) "usingcompose_front-tier").IPAddress}}' usingcompose_${DEFAULT_KAA_SERVICE_NAME}${j}_1`
            (( j++ ))
            ex -sc "47i|`cat kaa-nginx-config/nginx.conf.template | tail -5 | head -1 | sed \
            -e "s|{{PROXY_HOST_KAA}}|${INTERNAL_HOST_KAA}|g" \
            -e "s|{{PROXY_PORT}}|${i}|g" \
            `" -cx kaa-nginx-config/kaa-nginx.conf
        fi
    done
    docker-compose -f kaa-docker-compose.yml up -d kaa_lb
fi

docker-compose -f kaa-docker-compose.yml ps
docker-compose -f third-party-docker-compose.yml ps

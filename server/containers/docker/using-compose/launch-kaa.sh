#!/bin/bash

DATABASE_SQL_NOSQL=$1
KAA_NODE_SCALE=$2

VARIATIONS_OF_DATABASES=("mariadb-mongodb" "mariadb-cassandra" "postgresql-mongodb" "postgresql-cassandra")

isValid=false
DEFAULT_KAA_SERVICE_NAME=kaa_

DEFAULT_ADMIN_PORT=10080
DEFAULT_BOOTSTRAP_TCP=10088
DEFAULT_BOOTSTRAP_HTTP=10089
DEFAULT_OPERATIONS_TCP=10097
DEFAULT_OPERATIONS_HTTP=10099

IMAGE_VERSION_MARIADB="5.5"
IMAGE_VERSION_POSTGRESQL="9.4"
IMAGE_VERSION_MONGODB="3.2"
IMAGE_VERSION_CASSANDRA="3.7"

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
            sed -i -e "6s@.*@    image: ${SQL_PROVIDER_NAME}@" third-party-docker-compose.yml
            sed -i -e "19s@.*@    image: ${NOSQL_PROVIDER_NAME}@" third-party-docker-compose.yml

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

configureAndStartKaa() {
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

configureAndStartClusterKaa() {
    configureAndStartKaa
    KAA_NODE_SCALE=$(( KAA_NODE_SCALE - 1 ))
    for i in `seq 1 $KAA_NODE_SCALE`; do
        ex -sc "24i|`cat kaa-docker-compose.yml.template | tail -26 | head -20 | sed \
        -e "s|{{KAA_SERVICE_NAME}}|${DEFAULT_KAA_SERVICE_NAME}${i} |g" \
        -e "s|{{ADMIN_PORT}}|$(( DEFAULT_ADMIN_PORT + $i * 100 ))|g" \
        -e "s|{{BOOTSTRAP_TCP}}|$(( DEFAULT_BOOTSTRAP_TCP + $i * 100 ))|g" \
        -e "s|{{BOOTSTRAP_HTTP}}|$(( DEFAULT_BOOTSTRAP_HTTP + $i * 100 ))|g" \
        -e "s|{{OPERATIONS_TCP}}|$(( DEFAULT_OPERATIONS_TCP + $i * 100 ))|g" \
        -e "s|{{OPERATIONS_HTTP}}|$(( DEFAULT_OPERATIONS_HTTP + $i * 100 ))|g" \
        `" -cx kaa-docker-compose.yml
    done
    return 0;
}

stopRunningContainers
removeAvailableContainers

isValidDatabases
configureAndStartThirdPartyComponents

configureAndStartKaa
if ! [ -z "$KAA_NODE_SCALE" ]; then
    configureAndStartClusterKaa
    docker-compose -f kaa-docker-compose.yml up -d
else
    configureAndStartKaa
    docker-compose -f kaa-docker-compose.yml up -d
fi

docker ps

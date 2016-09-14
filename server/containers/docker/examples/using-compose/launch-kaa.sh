#!/bin/sh

DATABASE_SQL_NOSQL=$1
KAA_NODE_SCALE=$2

VARIATIONS_OF_DATABASES="mariadb-mongodb mariadb-cassandra postgresql-mongodb postgresql-cassandra"

isValid=false

if [ -z "$DATABASE_SQL_NOSQL" ]; then
    echo "Please choose correct variation of SQL and NoSQL databases \n
    mariadb-mongodb \n
    mariadb-cassandra \n
    postgresql-mongodb \n
    postgresql-cassandra"
    return 1

else ! [ -z "$DATABASE_SQL_NOSQL" ];

    for i in "${VARIATIONS_OF_DATABASES}"
    do
        if [ "$i"=="$DATABASE_SQL_NOSQL" ]; then
            isValid=true
        fi
    done

    if [ ! $isValid ]; then
        echo 'No such variation of databases'
        return 1
    fi

    cp -- "$DATABASE_SQL_NOSQL/docker-compose.yml" "docker-compose.yml"
    cp -- "$DATABASE_SQL_NOSQL/sql-example.env" "sql-example.env"

fi

RUNNING_CONTAINERS=`docker ps -q`
if ! [ -z "$RUNNING_CONTAINERS" ]; then
    docker stop $(docker ps -q)
fi

AVAILABLE_CONTAINERS=`docker ps -a -q`
if ! [ -z "$AVAILABLE_CONTAINERS" ]; then
    docker rm $(docker ps -a -q)
fi

docker-compose up -d

if ! [ -z $KAA_NODE_SCALE ]; then
    docker-compose -f docker-compose.yml scale kaa=$KAA_NODE_SCALE
fi

docker-compose ps
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
touch /var/log/kaa/kaa-node.log;

. /kaa/configure-kaa.sh || exit 1;
echo "Kaa configured!"

# Determine wait time:
# > 0 seconds
# = 0 don't wait
# < 0 wait forever
[[ $SERVICES_WAIT_TIMEOUT == ?(-)+([0-9]) ]] || SERVICES_WAIT_TIMEOUT=-1;

# Loop through all ZK nodes
# Passes if one node is reachable
isZKReachable() {
    OIFS="$IFS"
    IFS="," read -r -a ZNODE <<< $ZOOKEEPER_NODE_LIST
    for i in "${!ZNODE[@]}"
    do
        # echo "Reaching node #$i: ${ZNODE[i]}"
        HOST=$(echo ${ZNODE[i]} | cut -f1 -d:)
        PORT=$(echo ${ZNODE[i]} | cut -f2 -d:)
        bash -c "until [[ $(echo ruok | nc -q 2 $HOST $PORT) = imok ]]; do sleep 0.1; done;" >/dev/null 2>/dev/null && return 0;
    done
    IFS=OIFS;

    # No ZK nodes were reachable
    return 1;
}

# Exit if ZK not reachable after $SERVICE_WAIT_TIMEOUT
waitForZK() {
    echo "Waiting for Zookeeper nodes: $ZOOKEEPER_NODE_LIST"

    local I=0
    until [ ! $SERVICES_WAIT_TIMEOUT -lt 0 ] && [ $I -gt $SERVICES_WAIT_TIMEOUT ]; do
        isZKReachable && echo "Zookeeper is reachable, proceeding..." && return 0;

        sleep 1;
        let I=I+1;
    done;

    echo "Zookeeper is unreachable, aborting!";
    exit 1;
}

isSQLReachable() {
    bash -c "cat < /dev/null > /dev/tcp/${JDBC_HOST}/${JDBC_PORT}" >/dev/null 2>/dev/null \
        && return 0 \
        || return 1;
}

# Exit if ZK not reachable after $SERVICE_WAIT_TIMEOUT
waitForSQL() {
    echo "Waiting for SQL ($JDBC_HOST:$JDBC_PORT)"

    local I=0
    until [ ! $SERVICES_WAIT_TIMEOUT -lt 0 ] && [ $I -gt $SERVICES_WAIT_TIMEOUT ]; do
        isSQLReachable && echo "SQL is reachable, proceeding..." && return 0;

        sleep 1;
        let I=I+1;
    done;

    echo "SQL is unreachable, aborting!";
    exit 1;
}

# Loop through all Mongo nodes
# Passes if one node is reachable
isMongoReachable() {
    OIFS="$IFS"
    IFS="," read -r -a MDB_NODES <<< $MONGODB_NODE_LIST
    for i in "${!MDB_NODES[@]}"
    do
        # echo "Reaching node #$i: ${MDB_NODES[i]}"
        HOST=$(echo ${MDB_NODES[i]} | cut -f1 -d:)
        PORT=$(echo ${MDB_NODES[i]} | cut -f2 -d:)
        bash -c "cat < /dev/null > /dev/tcp/$HOST/$PORT" >/dev/null 2>/dev/null && return 0
    done
    IFS=OIFS;

    # No MongoDB nodes were reachable
    return 1;
}

# Exit if Mongo not reachable after $SERVICE_WAIT_TIMEOUT
waitForMongo() {
    echo "Waiting for MongoDB nodes: $MONGODB_NODE_LIST"

    local I=0
    until [ ! $SERVICES_WAIT_TIMEOUT -lt 0 ] && [ $I -gt $SERVICES_WAIT_TIMEOUT ]; do
        isMongoReachable && echo "MongoDB is reachable, proceeding..." && return 0;

        sleep 1;
        let I=I+1;
    done;

    echo "MongoDB is unreachable, aborting!";
    exit 1;
}

# Loop through all Cassandra nodes
# Passes if one node is reachable
isCassandraReachable() {
    OIFS="$IFS"
    IFS="," read -r -a CASSANDRA_NODES <<< $CASSANDRA_NODE_LIST
    for i in "${!CASSANDRA_NODES[@]}"
    do
        # echo "Reaching node #$i: ${CASSANDRA_NODES[i]}"
        HOST=$(echo ${CASSANDRA_NODES[i]} | cut -f1 -d:)
        PORT=$(echo ${CASSANDRA_NODES[i]} | cut -f2 -d:)
        bash -c "cat < /dev/null > /dev/tcp/$HOST/$PORT" >/dev/null 2>/dev/null && return 0
    done
    IFS=OIFS;

    # No Cassandra nodes were reachable
    return 1;
}

# Exit if Cassandra not reachable after $SERVICE_WAIT_TIMEOUT
waitForCassandra() {
    echo "Waiting for Cassandra nodes: $CASSANDRA_NODE_LIST"

    local I=0
    until [ ! $SERVICES_WAIT_TIMEOUT -lt 0 ] && [ $I -gt $SERVICES_WAIT_TIMEOUT ]; do
        isCassandraReachable && echo "Cassandra is reachable, proceeding..." && return 0;

        sleep 1;
        let I=I+1;
    done;

    echo "Cassandra is unreachable, aborting!";
    exit 1;
}

if [ $SERVICES_WAIT_TIMEOUT -ne 0 ]
then
    # Wait for ZK
    waitForZK
    # Wait for MariaDB | PostgreSQL
    waitForSQL
    # Wait for MongoDB | Cassandra
    if [[ "$NOSQL_PROVIDER_NAME" = "mongodb" ]]; then waitForMongo;
    else [[ "$NOSQL_PROVIDER_NAME" = "cassandra" ]]; waitForCassandra; fi;
else
    echo "Not waiting for dependent services and immediately starting kaa-node."
fi

service kaa-node start &
. /kaa/tail-node.sh

/bin/bash
exit 0;

#!/bin/bash

isCassandraReachable() {
    bash -c "cat < /dev/null > /dev/tcp/127.0.0.1/9042" >/dev/null 2>/dev/null && return 0
    return 1;
}

# Exit if Cassandra not reachable after $SERVICE_WAIT_TIMEOUT
waitForCassandra() {
    echo "Waiting for Cassandra 127.0.0.1:9042"
    SERVICES_WAIT_TIMEOUT=-1
    local I=0
    until [ ! $SERVICES_WAIT_TIMEOUT -lt 0 ] && [ $I -gt $SERVICES_WAIT_TIMEOUT ]; do
        echo "Waiting for Cassandra 127.0.0.1:9042"
        isCassandraReachable && cqlsh -f cassandra.cql && return 0;
        sleep 1;
        let I=I+1;
    done;

    echo "Cassandra is unreachable, aborting!";
    exit 1;
}

. /docker-entrypoint.sh cassandra -f & waitForCassandra

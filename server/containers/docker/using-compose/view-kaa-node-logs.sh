#!/bin/sh

KAA_SERVICE_NAME=$1

if [ -z "$KAA_SERVICE_NAME" ]; then
    docker-compose -p usingcompose exec $KAA_SERVICE_NAME sh /kaa/tail-node.sh
else
    echo "Please specify KAA_SERVICE_NAME which is specified in the 'kaa-docker-compose.yml' file."
fi

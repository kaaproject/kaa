#!/bin/sh

KAA_NODE_SCALE=$1

RUNNING_CONTAINERS=`docker ps -q`
if ! [ -z "$RUNNING_CONTAINERS" ]; then
   docker kill $(docker ps -q)
fi

AVAILABLE_CONTAINERS=`docker ps -a -q`
if ! [ -z "$AVAILABLE_CONTAINERS" ]; then
   docker-compose rm -f
   docker rm $(docker ps -a -q)
fi

# Run all containers except cms-backend.
docker-compose up -d

if ! [ -z $KAA_NODE_SCALE ]; then
   docker-compose -f docker-compose.yml scale kaa=$KAA_NODE_SCALE
fi

docker-compose ps

#cd ../..

#[ -d sdk ] && rm -rf sdk
#mkdir sdk
#cd sdk
#chmod +x ../scripts/get_KAA_SDK.sh
#../scripts/get_KAA_SDK.sh

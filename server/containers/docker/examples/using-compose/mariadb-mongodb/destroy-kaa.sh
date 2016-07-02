#!/bin/sh

docker-compose -p kaaiot down && \
docker volume rm kaaiot_sql-data kaaiot_nosql-data kaaiot_zookeeper-data

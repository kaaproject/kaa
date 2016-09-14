#!/bin/sh

docker-compose -p usingcompose down && \
docker volume rm usingcompose_sql-data usingcompose_nosql-data usingcompose_zookeeper-data

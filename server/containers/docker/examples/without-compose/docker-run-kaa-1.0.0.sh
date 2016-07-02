#!/bin/sh

# ! -> A newer, better example using docker-compose can be found under examples/using-compose/
# ! -> You must link containers yourself for this example!

# Suggestion to run:
# - Link containers using Docker network (recommended)
# - Link containers using --link (deprecated)

# Dependencies:
# - Zookeeper 3.4.8
# - MongoDB 3.2.6 OR Cassandra 2.2.5
# - MariaDB 5.5 OR PostgreSQL 9.4

docker run \
-d \
--name kaa \
--net=kaa \
--env-file example-env.dockerenv \
-p 8080:8080 \
-p 9888:9888 \
-p 9889:9889 \
-p 9997:9997 \
-p 9997:9997 \
cburr25/kaa:1.0.0

# To view logs:
# $ docker logs kaa

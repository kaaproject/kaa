#!/bin/bash
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
kaaproject/kaa:0.9.0

# To view logs:
# $ docker logs kaa

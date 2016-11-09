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
        isCassandraReachable && cqlsh -f cassandra.cql && echo "Cassandra is reachable" > initCassandra.log && return 0;
        sleep 1;
        let I=I+1;
    done;

    echo "Cassandra is unreachable, aborting!";
    exit 1;
}

if [ ! -f initCassandra.log ]; then
    touch initCassandra.log
fi
if [ ! -s initCassandra.log ]; then
    waitForCassandra
fi
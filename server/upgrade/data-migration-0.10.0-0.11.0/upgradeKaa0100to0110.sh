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

SQL_PROVIDER_NAME=$1

if [ -z "${SQL_PROVIDER_NAME}" ]; then
    echo "Please specify sql provider name.
mariadb or postgresql"
else
    case $SQL_PROVIDER_NAME in
        mariadb)
            echo "Upgrade mariadb 0.10.0 to 0.11.0 Kaa version"
            mysql -usqladmin -padmin kaa < upgradeSql0100to0110kaaVersionMariadb.sql
            ;;
        postgresql)
            echo "Upgrade postgresql 0.10.0 to 0.11.0 Kaa version"
            sudo -u postgres psql -d kaa -a -f upgradeSql0100to0110kaaVersionPostgreSQL.sql
            ;;
        *)
        exit 1
        ;;
    esac

    echo "Upgrade finished!"
fi





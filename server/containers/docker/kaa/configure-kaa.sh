#!/bin/bash -x
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
## Kaa Docker configurator

touch /usr/lib/kaa-node/conf/tmp.properties.template

# Check if JDBC host:port + DB name are provided, use defaults otherwise
[ -n "$JDBC_DB_NAME" ] || JDBC_DB_NAME="kaa"

# Determine JDBC url and driver
# SQL_PROVIDER_NAME is a mandatory environment variable
[ -n "$SQL_PROVIDER_NAME" ] || SQL_PROVIDER_NAME="mariadb"
if [ $SQL_PROVIDER_NAME = "mariadb" ]
then

  echo -e "Using MariaDB as SQL provider."
  SQL_PROVIDER_NAME="mysql:failover"
  HIBERNATE_DIALECT="org.hibernate.dialect.MySQL5InnoDBDialect"
  JDBC_DRIVER_CLASSNAME="org.mariadb.jdbc.Driver"
  JDBC_USERNAME="sqladmin"
  JDBC_PASSWORD="admin"
  [ -n "$JDBC_PORT" ] || JDBC_PORT="3306"
  JDBC_URL="jdbc:mysql:failover:\\/\\/${JDBC_HOST}:${JDBC_PORT}\\/${JDBC_DB_NAME}"

elif [ $SQL_PROVIDER_NAME = "postgresql" ]
then

  echo -e "Using PostgreSQL as SQL provider."
  SQL_PROVIDER_NAME="postgresql"
  HIBERNATE_DIALECT="org.hibernate.dialect.PostgreSQL82Dialect"
  JDBC_DRIVER_CLASSNAME="org.postgresql.Driver"
  JDBC_USERNAME="postgres"
  JDBC_PASSWORD="admin"
  [ -n "$JDBC_PORT" ] || JDBC_PORT="5432"
  JDBC_URL="jdbc:postgresql:\\/\\/${JDBC_HOST}:${JDBC_PORT}\\/${JDBC_DB_NAME}"

else
  echo -e "\nIncorrect SQL provider name: '${SQL_PROVIDER_NAME}'\nValid options: 'mariadb' , 'postgresql'\nConfiguration exiting now..."
  exit 1
fi

# > admin-dao.properties
sed \
  -e "s/\(hibernate_dialect *= *\).*/\1${HIBERNATE_DIALECT}/" \
  -e "s/\(jdbc_driver_className *= *\).*/\1${JDBC_DRIVER_CLASSNAME}/" \
  -e "s/\(jdbc_username *= *\).*/\1${JDBC_USERNAME}/" \
  -e "s/\(jdbc_password *= *\).*/\1${JDBC_PASSWORD}/" \
  -e "s/\(jdbc_url *= *\).*/\1${JDBC_URL}/" \
   /usr/lib/kaa-node/conf/admin-dao.properties > /usr/lib/kaa-node/conf/tmp.properties.template

cat /usr/lib/kaa-node/conf/tmp.properties.template > /usr/lib/kaa-node/conf/admin-dao.properties

# > sql-dao.properties
sed \
  -e "s/\(db_name *= *\).*/\1${JDBC_DB_NAME}/" \
  -e "s/\(hibernate_dialect *= *\).*/\1${HIBERNATE_DIALECT}/" \
  -e "s/\(jdbc_driver_className *= *\).*/\1${JDBC_DRIVER_CLASSNAME}/" \
  -e "s/\(jdbc_username *= *\).*/\1${JDBC_USERNAME}/" \
  -e "s/\(jdbc_password *= *\).*/\1${JDBC_PASSWORD}/" \
  -e "s/\(jdbc_host_port *= *\).*/\1${JDBC_HOST}:${JDBC_PORT}/" \
  -e "s/\(sql_provider_name *= *\).*/\1${SQL_PROVIDER_NAME}/" \
   /usr/lib/kaa-node/conf/sql-dao.properties > /usr/lib/kaa-node/conf/tmp.properties.template

cat /usr/lib/kaa-node/conf/tmp.properties.template > /usr/lib/kaa-node/conf/sql-dao.properties

# > common-dao-cassandra.properties
sed -i "s/\(node_list *= *\).*/\1${CASSANDRA_NODE_LIST}/" /usr/lib/kaa-node/conf/common-dao-cassandra.properties

# > common-dao-mongodb.properties
sed -i "s/\(servers *= *\).*/\1${MONGODB_NODE_LIST}/" /usr/lib/kaa-node/conf/common-dao-mongodb.properties

# > nosql-dao.properties
# Fail early if invalid provider name
if ! [[ "$NOSQL_PROVIDER_NAME" =~ ^(mongodb|cassandra)$ ]];
then
  echo -e "\nIncorrect NoSQL provider name: '${NOSQL_PROVIDER_NAME}'\nValid options: 'mongodb' , 'cassandra'\nConfiguration exiting now...";
  exit 1
fi
sed -i "s/\(nosql_db_provider_name *= *\).*/\1${NOSQL_PROVIDER_NAME}/" /usr/lib/kaa-node/conf/nosql-dao.properties

# > kaa-node.properties
[ -n "$TRANSPORT_PUBLIC_INTERFACE" ]  || TRANSPORT_PUBLIC_INTERFACE=`/sbin/ifconfig eth1 | grep "inet addr" | awk -F: '{print $2}' | awk '{print $1}'`
[ -n "$THRIFT_HOST" ] || THRIFT_HOST=`/sbin/ifconfig eth1 | grep "inet addr" | awk -F: '{print $2}' | awk '{print $1}'`
sed \
  -e "s/\(zk_host_port_list *= *\).*/\1${ZOOKEEPER_NODE_LIST}/" \
  -e "s/\(admin_port *= *\).*/\1${ADMIN_PORT}/" \
  -e "s/\(transport_public_interface *= *\).*/\1${TRANSPORT_PUBLIC_INTERFACE}/" \
  -e "s/\(thrift_host *= *\).*/\1${THRIFT_HOST}/" \
   /usr/lib/kaa-node/conf/kaa-node.properties > /usr/lib/kaa-node/conf/tmp.properties.template

cat /usr/lib/kaa-node/conf/tmp.properties.template > /usr/lib/kaa-node/conf/kaa-node.properties

# > kaa-bootstrap-http-transport.config.properties
sed -i "s/\(\"bindPort\" *: *\).*/\1${BOOTSTRAP_HTTP},/" /usr/lib/kaa-node/conf/bootstrap-http-transport.config
sed -i "s/\(\"publicPorts\" *: *\).*/\1\"${BOOTSTRAP_HTTP}\",/" /usr/lib/kaa-node/conf/bootstrap-http-transport.config

# > bootstrap-tcp-transport.config
sed -i "s/\(\"bindPort\" *: *\).*/\1${BOOTSTRAP_TCP},/" /usr/lib/kaa-node/conf/bootstrap-tcp-transport.config
sed -i "s/\(\"publicPorts\" *: *\).*/\1\"${BOOTSTRAP_TCP}\"/" /usr/lib/kaa-node/conf/bootstrap-tcp-transport.config

# > operations-http-transport.config
sed -i "s/\(\"bindPort\" *: *\).*/\1${OPERATIONS_HTTP},/" /usr/lib/kaa-node/conf/operations-http-transport.config
sed -i "s/\(\"publicPorts\" *: *\).*/\1\"${OPERATIONS_HTTP}\",/" /usr/lib/kaa-node/conf/operations-http-transport.config

# > operations-tcp-transport.config
sed -i "s/\(\"bindPort\" *: *\).*/\1${OPERATIONS_TCP},/" /usr/lib/kaa-node/conf/operations-tcp-transport.config
sed -i "s/\(\"publicPorts\" *: *\).*/\1\"${OPERATIONS_TCP}\"/" /usr/lib/kaa-node/conf/operations-tcp-transport.config

rm /usr/lib/kaa-node/conf/tmp.properties.template
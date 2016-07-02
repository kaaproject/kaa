#!/bin/bash -x

## Kaa Docker configurator
## - Christopher Burroughs @ xMight Inc. <chris@xmight.com>

# Check if JDBC host:port + DB name are provided, use defaults otherwise
[ -n "$JDBC_HOST" ] || JDBC_HOST="localhost"
[ -n "$JDBC_DB_NAME" ] || JDBC_DB_NAME="kaa"

# Determine JDBC url and driver
# SQL_PROVIDER_NAME is a mandatory environment variable
[ -n "$SQL_PROVIDER_NAME" ] || SQL_PROVIDER_NAME="NULL"
if [ $SQL_PROVIDER_NAME = "mariadb" ]
then

  echo -e "Using MariaDB as SQL provider."
  SQL_PROVIDER_NAME="mysql:failover"
  HIBERNATE_DIALECT="org.hibernate.dialect.MySQL5InnoDBDialect"
  JDBC_DRIVER_CLASSNAME="org.mariadb.jdbc.Driver"
  [ -n "$JDBC_PORT" ] || JDBC_PORT="3306"
  JDBC_URL="jdbc:mysql:failover://${JDBC_HOST}:${JDBC_PORT}/${JDBC_DB_NAME}"

elif [ $SQL_PROVIDER_NAME = "postgresql" ]
then

  echo -e "Using PostgreSQL as SQL provider."
  SQL_PROVIDER_NAME="postgresql"
  HIBERNATE_DIALECT="org.hibernate.dialect.PostgreSQL82Dialect"
  JDBC_DRIVER_CLASSNAME="org.postgresql.Driver"
  [ -n "$JDBC_PORT" ] || JDBC_PORT="5432"
  JDBC_URL="jdbc:postgresql://${JDBC_HOST}:${JDBC_PORT}/${JDBC_DB_NAME}"

else
  echo -e "\nIncorrect SQL provider name: '${SQL_PROVIDER_NAME}'\nValid options: 'mariadb' , 'postgresql'\nConfiguration exiting now..."
  exit 1
fi

## Process configuration templates ##

# > admin-dao.properties
cat /usr/lib/kaa-node/conf/admin-dao.properties.template | sed \
  -e "s|{{HIBERNATE_DIALECT}}|${HIBERNATE_DIALECT}|g" \
  -e "s|{{JDBC_DRIVER_CLASSNAME}}|${JDBC_DRIVER_CLASSNAME}|g" \
  -e "s|{{JDBC_USERNAME}}|${JDBC_USERNAME:-sqladmin}|g" \
  -e "s|{{JDBC_PASSWORD}}|${JDBC_PASSWORD:-admin}|g" \
  -e "s|{{JDBC_URL}}|${JDBC_URL}|g" \
   > /usr/lib/kaa-node/conf/admin-dao.properties

# > sql-dao.properties
cat /usr/lib/kaa-node/conf/sql-dao.properties.template | sed \
  -e "s|{{JDBC_DB_NAME}}|${JDBC_DB_NAME}|g" \
  -e "s|{{HIBERNATE_DIALECT}}|${HIBERNATE_DIALECT}|g" \
  -e "s|{{JDBC_DRIVER_CLASSNAME}}|${JDBC_DRIVER_CLASSNAME}|g" \
  -e "s|{{JDBC_USERNAME}}|${JDBC_USERNAME:-sqladmin}|g" \
  -e "s|{{JDBC_PASSWORD}}|${JDBC_PASSWORD:-admin}|g" \
  -e "s|{{JDBC_HOST}}|${JDBC_HOST}|g" \
  -e "s|{{JDBC_PORT}}|${JDBC_PORT}|g" \
  -e "s|{{SQL_PROVIDER_NAME}}|${SQL_PROVIDER_NAME}|g" \
   > /usr/lib/kaa-node/conf/sql-dao.properties

# > mariadb-dao.properties
cat /usr/lib/kaa-node/conf/mariadb-dao.properties.template | sed \
  -e "s|{{JDBC_USERNAME}}|${JDBC_USERNAME:-sqladmin}|g" \
  -e "s|{{JDBC_PASSWORD}}|${JDBC_PASSWORD:-admin}|g" \
  -e "s|{{JDBC_URL}}|${JDBC_URL}|g" \
   > /usr/lib/kaa-node/conf/mariadb-dao.properties

# > postgresql-dao.properties
cat /usr/lib/kaa-node/conf/postgresql-dao.properties.template | sed \
  -e "s|{{JDBC_USERNAME}}|${JDBC_USERNAME:-postgres}|g" \
  -e "s|{{JDBC_PASSWORD}}|${JDBC_PASSWORD:-admin}|g" \
  -e "s|{{JDBC_URL}}|${JDBC_URL}|g" \
   > /usr/lib/kaa-node/conf/postgresql-dao.properties

# > common-dao-cassandra.properties
[ -n "$CASSANDRA_NODE_LIST" ] || CASSANDRA_NODE_LIST="localhost:9042"
cat /usr/lib/kaa-node/conf/common-dao-cassandra.properties.template | sed \
  -e "s|{{CASSANDRA_CLUSTER_NAME}}|${CASSANDRA_CLUSTER_NAME:-Kaa Cluster}|g" \
  -e "s|{{CASSANDRA_KEYSPACE_NAME}}|${CASSANDRA_KEYSPACE_NAME:-kaa}|g" \
  -e "s|{{CASSANDRA_NODE_LIST}}|${CASSANDRA_NODE_LIST}|g" \
  -e "s|{{CASSANDRA_USE_SSL}}|${CASSANDRA_USE_SSL:-false}|g" \
  -e "s|{{CASSANDRA_USE_JMX}}|${CASSANDRA_USE_JMX:-true}|g" \
  -e "s|{{CASSANDRA_USE_CREDENTIALS}}|${CASSANDRA_USE_CREDENTIALS:-false}|g" \
  -e "s|{{CASSANDRA_USERNAME}}|${CASSANDRA_USERNAME:-}|g" \
  -e "s|{{CASSANDRA_PASSWORD}}|${CASSANDRA_PASSWORD:-}|g" \
   > /usr/lib/kaa-node/conf/common-dao-cassandra.properties

# > common-dao-mongodb.properties
[ -n "$MONGODB_NODE_LIST" ] || MONGODB_NODE_LIST="localhost:27017"
cat /usr/lib/kaa-node/conf/common-dao-mongodb.properties.template | sed \
  -e "s|{{MONGODB_NODE_LIST}}|${MONGODB_NODE_LIST}|g" \
  -e "s|{{MONGODB_DB_NAME}}|${MONGODB_DB_NAME:-kaa}|g" \
  -e "s|{{MONGODB_WRITE_CONCERN}}|${MONGODB_WRITE_CONCERN:-acknowledged}|g" \
   > /usr/lib/kaa-node/conf/common-dao-mongodb.properties

# > nosql-dao.properties
[ -n "$NOSQL_PROVIDER_NAME" ] || NOSQL_PROVIDER_NAME="mongodb"
# Fail early if invalid provider name
if ! [[ "$NOSQL_PROVIDER_NAME" =~ ^(mongodb|cassandra)$ ]];
then
  echo -e "\nIncorrect NoSQL provider name: '${NOSQL_PROVIDER_NAME}'\nValid options: 'mongodb' , 'cassandra'\nConfiguration exiting now...";
  exit 1
fi
cat /usr/lib/kaa-node/conf/nosql-dao.properties.template | sed \
  -e "s|{{NOSQL_PROVIDER_NAME}}|${NOSQL_PROVIDER_NAME}|g" \
   > /usr/lib/kaa-node/conf/nosql-dao.properties

# > kaa-node.properties
[ -n "$ZOOKEEPER_NODE_LIST" ] || ZOOKEEPER_NODE_LIST="localhost:2181"
cat /usr/lib/kaa-node/conf/kaa-node.properties.template | sed \
  -e "s|{{CONTROL_SERVER_ENABLED}}|${CONTROL_SERVER_ENABLED:-true}|g" \
  -e "s|{{BOOTSTRAP_SERVER_ENABLED}}|${BOOTSTRAP_SERVER_ENABLED:-true}|g" \
  -e "s|{{OPERATIONS_SERVER_ENABLED}}|${OPERATIONS_SERVER_ENABLED:-true}|g" \
  -e "s|{{THRIFT_HOST}}|${THRIFT_HOST:-localhost}|g" \
  -e "s|{{THRIFT_PORT}}|${THRIFT_PORT:-9090}|g" \
  -e "s|{{ADMIN_PORT}}|${ADMIN_PORT:-8080}|g" \
  -e "s|{{ZOOKEEPER_NODE_LIST}}|${ZOOKEEPER_NODE_LIST}|g" \
  -e "s|{{SUPPORT_UNENCRYPTED_CONNECTION}}|${SUPPORT_UNENCRYPTED_CONNECTION:-true}|g" \
  -e "s|{{TRANSPORT_BIND_INTERFACE}}|${TRANSPORT_BIND_INTERFACE:-0.0.0.0}|g" \
  -e "s|{{TRANSPORT_PUBLIC_INTERFACE}}|${TRANSPORT_PUBLIC_INTERFACE:-localhost}|g" \
  -e "s|{{METRICS_ENABLED}}|${METRICS_ENABLED:-true}|g" \
   > /usr/lib/kaa-node/conf/kaa-node.properties

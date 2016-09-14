#!/bin/bash -x

## Kaa Docker configurator

# Check if JDBC host:port + DB name are provided, use defaults otherwise
[ -n "$JDBC_HOST" ] || JDBC_HOST="localhost"
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
  JDBC_URL="jdbc:mysql:failover://${JDBC_HOST}:${JDBC_PORT}/${JDBC_DB_NAME}"

elif [ $SQL_PROVIDER_NAME = "postgresql" ]
then

  echo -e "Using PostgreSQL as SQL provider."
  SQL_PROVIDER_NAME="postgresql"
  HIBERNATE_DIALECT="org.hibernate.dialect.PostgreSQL82Dialect"
  JDBC_DRIVER_CLASSNAME="org.postgresql.Driver"
  JDBC_USERNAME="postgres"
  JDBC_PASSWORD="admin"
  [ -n "$JDBC_PORT" ] || JDBC_PORT="5432"
  JDBC_URL="jdbc:postgresql://${JDBC_HOST}:${JDBC_PORT}/${JDBC_DB_NAME}"

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
   /usr/lib/kaa-node/conf/admin-dao.properties

# > sql-dao.properties
sed \
  -e "s/\(db_name *= *\).*/\1${JDBC_DB_NAME}/" \
  -e "s/\(hibernate_dialect *= *\).*/\1${HIBERNATE_DIALECT}/" \
  -e "s/\(jdbc_driver_className *= *\).*/\1${JDBC_DRIVER_CLASSNAME}/" \
  -e "s/\(jdbc_username *= *\).*/\1${JDBC_USERNAME}/" \
  -e "s/\(jdbc_password *= *\).*/\1${JDBC_PASSWORD}/" \
  -e "s/\(jdbc_host_port *= *\).*/\1${JDBC_HOST}:${JDBC_PORT}/" \
  -e "s/\(sql_provider_name *= *\).*/\1${SQL_PROVIDER_NAME}/" \
   /usr/lib/kaa-node/conf/sql-dao.properties

# > common-dao-cassandra.properties
[ -n "$CASSANDRA_NODE_LIST" ] || CASSANDRA_NODE_LIST="localhost:9042"
sed -i "s/\(node_list *= *\).*/\1${CASSANDRA_NODE_LIST}/" /usr/lib/kaa-node/conf/common-dao-cassandra.properties

# > common-dao-mongodb.properties
[ -n "$MONGODB_NODE_LIST" ] || MONGODB_NODE_LIST="localhost:27017"
sed -i "s/\(servers *= *\).*/\1${MONGODB_NODE_LIST}/" /usr/lib/kaa-node/conf/common-dao-mongodb.properties

# > nosql-dao.properties
[ -n "$NOSQL_PROVIDER_NAME" ] || NOSQL_PROVIDER_NAME="mongodb"
# Fail early if invalid provider name
if ! [[ "$NOSQL_PROVIDER_NAME" =~ ^(mongodb|cassandra)$ ]];
then
  echo -e "\nIncorrect NoSQL provider name: '${NOSQL_PROVIDER_NAME}'\nValid options: 'mongodb' , 'cassandra'\nConfiguration exiting now...";
  exit 1
fi
sed -i "s/\(nosql_db_provider_name *= *\).*/\1${NOSQL_PROVIDER_NAME}/" /usr/lib/kaa-node/conf/nosql-dao.properties
It is recommended to first checkout Kaa's official installation guide before using this image:
-> http://docs.kaaproject.org/display/KAA/Installation+guide

Kaa IoT requires the following dependencies to run:

- Zookeeper 3.4.5
- MariaDB 5.5 <b><i>or</i></b> PostgreSQL 9.4
- MongoDB 3.2.6 <b><i>or</i></b> Cassandra 2.2.5

<hr />

## Build

<b>Docker build</b>

1. Download Kaa's debian package at: http://www.kaaproject.org/download-kaa/ and place it inside 'install/'

2. Build, specifying the debian package location: 
$ docker build --build-arg setupfile=&lt;KAA_DEB_PACKAGE&gt; -t cburr25/kaa:0.9.0 .

## Quick run

Two example runs using docker-compose are provided. Simply run <b>launch-kaa.sh</b> in either:

<i>Using MariaDB + MongoDB:</i>
- examples/using-compose/<b>mariadb-mongodb</b>/

<i>Using PostgreSQL + MongoDB:</i>
- examples/using-compose/<b>postgresql-mongodb</b>/

## Without compose

<b>(1)</b> Run Zookeeper (3.4.8), MariaDB (5.5)/PostgreSQL (9.4) and MongoDB (3.2.6)/Cassandra (2.2.5)

<b>(2)</b> Write up a Docker environment file to configure your server, see <i>examples/using-compose/kaa-example.env</i>. You <u>must</u> specify SQL_PROVIDER_NAME.

<u>List of available environment variables:</u>

| VARIABLE         		       	|   DEFAULT					| NOTE / POSSIBLE VALUES
| -----------------------------	|--------------------------	| ----------------------------
| SERVICES_WAIT_TIMEOUT			| -1 (forever)				| Seconds (integer) before timeout while waiting for ZK/SQL/NoSQL to be ready, otherwise abort.<br>10: wait 10 seconds.<br>0: don't wait<br>-1: wait forever.
|								|							|
| ZOOKEEPER_NODE_LIST			| localhost:2181			| <i>comma separated list</i>
| 								| 							|
| SQL_PROVIDER_NAME				| mariadb 					| mariadb , postgresql
| JDBC_HOST						| localhost					|
| JDBC_PORT						| if mariadb: 3306<br>if postgresql: 5432 | 
| JDBC_USERNAME					| sqladmin					| 
| JDBC_PASSWORD					| admin						|
| JDBC_DB_NAME					| kaa 						| 
								| 							| 
| CASSANDRA_CLUSTER_NAME		| Kaa Cluster 				| 
| CASSANDRA_KEYSPACE_NAME		| kaa 						| 
| CASSANDRA_NODE_LIST			| localhost:9042 			| <i>comma separated list</i>
| CASSANDRA_USE_SSL				| false 					| 
| CASSANDRA_USE_JMX				| true 						| 
| CASSANDRA_USE_CREDENTIALS		| false 					| 
| CASSANDRA_USERNAME 			| (empty) 					| 
| CASSANDRA_PASSWORD 			| (empty) 					| 
| 								| 							| 
| MONGODB_NODE_LIST 			| localhost:27017 			| 
| MONGODB_DB_NAME				| kaa 						| 
| MONGODB_WRITE_CONCERN 		| acknowledged 				| 
| 								| 							| 
| NOSQL_PROVIDER_NAME			| mongodb 					| mongodb , cassandra
|								|							|
| CONTROL_SERVER_ENABLED		| true						| true/false
| BOOTSTRAP_SERVER_ENABLED		| true						| true/false
| OPERATIONS_SERVER_ENABLED		| true						| true/false
| THRIFT_HOST					| localhost					| 
| THRIFT_PORT					| 9090						| 
| ADMIN_PORT					| 8080						| 
| SUPPORT_UNENCRYPTED_CONNECTION | true						| true/false
| TRANSPORT_BIND_INTERFACE		| 0.0.0.0					| 
| TRANSPORT_PUBLIC_INTERFACE	| localhost					| 
| METRICS_ENABLED				| true 						| true/false


<b>(3)</b> Run this image, link the containers however you want. <i>See 'docker-run-kaa-1.0.0.sh' for an example.</i>

## Logs

If you run your Docker container as a daemon, you won't see its output. That's okay, just run:

$ docker exec &lt;container-name&gt; tail -f /var/log/kaa/kaa-node.log

Or simply run the shortcut script 'view-kaa-node-logs.sh' in the examples !

<hr />
## Maintainers:
- Christopher Burroughs (xMight Corp.)

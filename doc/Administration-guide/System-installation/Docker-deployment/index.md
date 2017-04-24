---
layout: page
title: Docker deployment
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to deploy a [Kaa cluster]({{root_url}}Glossary/#kaa-cluster) using the [Docker](https://www.docker.com/) containerization platform.

The deployed cluster will include:
	
- Kaa nodes
- One Zookeeper node
- One SQL Database node -- MariaDB/PostgreSQL
- One NoSQL Database node -- MongoDB/Cassandra

## Deployment process

To deploy a Kaa cluster on your machine:

1. Install [Docker Engine](https://docs.docker.com/engine/installation/).

	If you use Linux, you can install Docker Engine by running the [installation script](https://get.docker.com/).

		wget -qO- https://get.docker.com/ | sh

	>**IMPORTANT:** If you use Windows, you must also have [Python](https://www.python.org/downloads/) installed.
	{:.important}

	To verify your Docker installation, run:
	
		docker --version
	
2. Install [Docker Сompose](https://docs.docker.com/compose/install/).

	To verify your Docker Compose installation, run:
	
		docker-compose --version

3. Download Kaa debian package from the [official site](http://www.kaaproject.org/download-kaa/) or build your Kaa project locally.

	To build locally, clone Kaa repository
	
		$ git clone https://github.com/kaaproject/kaa.git
	
	and run the following command from the `kaa` directory.
	
		$ mvn -P compile-gwt,mongo-dao,mariadb-dao clean install verify
	
	This will generate a `kaa-node.deb` file in the `server/node/target/` directory.
	
	>**NOTE:** For more information, see [how to build your Kaa project locally]({{root_url}}Administration-guide/System-installation/Building-Kaa-server-from-source-code/).
	{:.note}

4. Put the `kaa-node.deb` file in the `server/containers/docker` directory of your cloned Kaa repository.

5. Run the following command from the `server/containers/docker` directory.

		docker build --build-arg setupfile=kaa-node.deb -t kaa-node:0.10.0 .

	Alternatively, you can run the following command from the same directory.

		build.sh


### Single node installation

To install a single node:

1. Get your public host by specifying the `TRANSPORT_PUBLIC_INTERFACE` parameter in the `server/containers/docker/docker-compose-1-node/kaa-example.env` file.

	* Linux / macOS

			ip route get 8.8.8.8 | awk '{print $NF; exit}'

	* Windows

			netsh interface ip show address "Ethernet" | findstr "IP Address"

2. Open any directory in the `docker-compose-1-node` directory.

		cd docker-compose-1-node/$SQL-NoSQL/

	The following SQL-NoSQL databases are available:
	
	 * mariadb-mongodb
	 * mariadb-cassandra
	 * postgresql-mongodb
	 * postgresql-cassandra

3. Run the following command.

		docker-compose up

	If you want to run Docker container as a daemon, run

		docker-compose up -d


### Cluster node installation

To install a cluster node:

1. Specify SQL-NoSQL databases which you want to use.
To do this, run the following command from the `server/containers/docker/using-compose` directory.
		
		python launch-kaa.py SQL-NoSQL
	
	See example below.
		
		python launch-kaa.py mariadb-mongodb

	The following SQL-NoSQL databases are available:

	 * mariadb-mongodb
	 * mariadb-cassandra
	 * postgresql-mongodb
	 * postgresql-cassandra

	If you want deploy Kaa in cluster mode, run the previous command with an additional option.

		python launch-kaa.py SQL-NoSQL NODE_COUNT

    See example below.
			
		python launch-kaa.py mariadb-mongodb 3
		

2. Execute the following command to access the running container.

		docker exec -it usingcompose_KAA_SERVICE_NAME_1 /bin/bash -c "export TERM=xterm; exec bash"
		
	`KAA_SERVICE_NAME` is the Kaa service name specified in the `kaa-docker-compose.yml` file.
            
    See example below.

		docker exec -it usingcompose_kaa_0_1 /bin/bash -c "export TERM=xterm; exec bash"

	`usingcompose` is the name of the directory containing the `kaa-docker-compose.yml` and `third-party-docker-compose.yml` files.

3. Wait a few seconds until the `kaa-node` service starts and open your browser at `localhost:8080`.
See the `kaa node` container log to check if the service has started.

## Logs

When you run your Docker container as a daemon, its output is not displayed.
To access it, run
    
	docker-compose -f kaa-docker-compose.yml -p usingcompose exec KAA_SERVICE_NAME sh /kaa/tail-node.sh

`KAA_SERVICE_NAME` is the Kaa service name specified in the `kaa-docker-compose.yml` file.

Alternatively, you can run the `view-kaa-node-logs.sh` to obtain the same result.
    
You can also use the command below.
    
	docker logs DOCKER_SERVICE_NAME
    
To get the names of all Docker containers, run
    
	docker ps -a

To get the names of all Docker containers that are running, execute the following command.
    
	docker ps
	
## Base image configuration

 Base image configuration is done using the following environment variables.
 
 | Variable         		       	|   Default					| Note / Possible values
 |-----------------------------|--------------------------|----------------------------
 | `SERVICES_WAIT_TIMEOUT`			| -1 (forever)				| Seconds (integer) before timeout while waiting for ZK/SQL/NoSQL to be ready, otherwise abort.<br>10: wait 10 seconds.<br>0: don't wait<br>-1: wait forever.
 |								|							|
 | `ZOOKEEPER_NODE_LIST`			| localhost:2181			| <i>comma separated list</i>
 | 								| 							|
 | `SQL_PROVIDER_NAME`				| mariadb 					| mariadb , postgresql
 | `JDBC_HOST`						| localhost					|
 | `JDBC_PORT`						| if mariadb: 3306<br>if postgresql: 5432|
 | `JDBC_USERNAME`					| sqladmin					|
 | `JDBC_PASSWORD`					| admin						|
 | `JDBC_DB_NAME`					| kaa 						|
  								| 							|
 | `CASSANDRA_CLUSTER_NAME`		| Kaa Cluster 				|
 | `CASSANDRA_KEYSPACE_NAME`		| kaa 						|
 | `CASSANDRA_NODE_LIST`			| localhost:9042 			| <i>comma separated list</i>
 | `CASSANDRA_USE_SSL`				| false 					|
 | `CASSANDRA_USE_JMX`				| true 						|
 | `CASSANDRA_USE_CREDENTIALS`		| false 					|
 | `CASSANDRA_USERNAME` 			| (empty) 					|
 | `CASSANDRA_PASSWORD` 			| (empty) 					| 
 | 								| 							| 
 | `MONGODB_NODE_LIST` 			| localhost:27017 			| 
 | `MONGODB_DB_NAME`				| kaa 						| 
 | `MONGODB_WRITE_CONCERN` 		| acknowledged 				| 
 | 								| 							| 
 | `NOSQL_PROVIDER_NAME`			| mongodb 					| mongodb , cassandra
 |								|							|
 | `CONTROL_SERVER_ENABLED`		| true						| true/false
 | `BOOTSTRAP_SERVER_ENABLED`		| true						| true/false
 | `OPERATIONS_SERVER_ENABLED`		| true						| true/false
 | `THRIFT_HOST`					| localhost					| 
 | `THRIFT_PORT`					| 9090						| 
 | `ADMIN_PORT`					| 8080						| 
 | `SUPPORT_UNENCRYPTED_CONNECTION`| true						| true/false
 | `TRANSPORT_BIND_INTERFACE`		| 0.0.0.0					| 
 | `TRANSPORT_PUBLIC_INTERFACE`	| current public host					|
 | `METRICS_ENABLED`				| true 						| true/false
  

# Kaa Docker image

This readme contains information about Kaa Docker image deployment.

Default environments that comes in this package is:
  - Fully functional Kaa cluster (Nx Kaa node, 1x Zookeeper node, 1x Database SQL node, 1x Database NoSQL node)
    - Kaa node
    - Zookeeper node
    - MariaDb/PostgreSQL
    - MongoDB/Cassandra

Base image configuration is done using the following environment variables:

| VARIABLE         		       	|   DEFAULT					| NOTE / POSSIBLE VALUES
|-----------------------------|--------------------------|----------------------------
| SERVICES_WAIT_TIMEOUT			| -1 (forever)				| Seconds (integer) before timeout while waiting for ZK/SQL/NoSQL to be ready, otherwise abort.<br>10: wait 10 seconds.<br>0: don't wait<br>-1: wait forever.
|								|							|
| ZOOKEEPER_NODE_LIST			| localhost:2181			| <i>comma separated list</i>
| 								| 							|
| SQL_PROVIDER_NAME				| mariadb 					| mariadb , postgresql
| JDBC_HOST						| localhost					|
| JDBC_PORT						| if mariadb: 3306<br>if postgresql: 5432|
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
| SUPPORT_UNENCRYPTED_CONNECTION| true						| true/false
| TRANSPORT_BIND_INTERFACE		| 0.0.0.0					| 
| TRANSPORT_PUBLIC_INTERFACE	| current public host					|
| METRICS_ENABLED				| true 						| true/false
 
# Steps to deploy

1. Download debian package from [official site](http://www.kaaproject.org/download-kaa/)
 or build your Kaa project locally (kaa-node.deb located in **server/node/target/**)
 Put kaa-node.deb into **server/containers/docker/** folder.
 
2. From server/containers/docker folder execute:

     ```
     docker build --build-arg setupfile=kaa-node.deb -t kaa-node:0.10.0 .
     ```
        
    or
        
     ```
     sh build.sh
     ```

## Single node installation

You can use some prepared files in **server/containers/docker/docker-compose-1-node** folder. 

First of all in **server/containers/docker/docker-compose-1-node/kaa-example.env** please specify the 
**TRANSPORT_PUBLIC_INTERFACE**

For getting your public host just run

for Linux or Mac OS:

 ```
  ip route get 8.8.8.8 | awk '{print $NF; exit}'
 ```
 
for Windows:

 ```
  netsh interface ip show address "Ethernet" | findstr "IP Address"
 ```

Navigate into any one of the possible folders in **docker-compose-1-node** folder 

 ```
 cd docker-compose-1-node/$SQL-NoSQL/ 
 ```
 where all available options of SQL-NoSQL databases:
 
 * mariadb-mongodb
 * mariadb-cassandra
 * postgresql-mongodb
 * postgresql-cassandra 
 
 and execute

 ```
 docker-compose up 
 ```
    
or if you want run Docker container as a daemon, run

 ```
 docker-compose up -d
 ```

## Cluster node installation

1. From server/containers/docker/using-compose folder run command:
    
     ```
     python launch-kaa.py SQL-NoSQL
     ```
            
    And specify SQL-NoSQL databases which you wan to use
            
    for example: 
            
     ```
     python launch-kaa.py mariadb-mongodb
     ```
            
    All available options of SQL-NoSQL databases:
            
     * mariadb-mongodb
     * mariadb-cassandra
     * postgresql-mongodb
     * postgresql-cassandra 
                
    If you want deploy Kaa in cluster mode, run previous command with additional optional
            
     ```
     python launch-kaa.py SQL-NoSQL NODE_COUNT
     ```
            
    or example: 
            
     ```
     python launch-kaa.py mariadb-mongodb 3
     ```

2. Execute following command to get into running container:

     ```
     docker exec -it usingcompose_KAA_SERVICE_NAME_1 /bin/bash -c "export TERM=xterm; exec bash"
     ```
            
    KAA_SERVICE_NAME it's Kaa service name in kaa-docker-compose.yml file.
            
    for example: 
            
     ```
     docker exec -it usingcompose_kaa_0_1 /bin/bash -c "export TERM=xterm; exec bash"
     ```  
             
    Wait a few seconds until the service kaa-node starts.
            
    Open your browser on localhost:8080 (You can see there some error, just refresh page. It's mean that's service kaa-node does not start yet. 
    You can verify this by looking logs of kaa-node containers.)
            
    In this example usingcompose - it's name of folder where located kaa-docker-compose.yml and third-party-docker-compose.yml files.


# Logs

When you run your Docker container as a daemon, you won't see its output. So you can use:
    
 ```
 docker-compose -f kaa-docker-compose.yml -p usingcompose exec KAA_SERVICE_NAME sh /kaa/tail-node.sh
 ```
    
KAA_SERVICE_NAME it's Kaa service name in kaa-docker-compose.yml file.
        
Or simply run the shortcut script 'view-kaa-node-logs.sh' in the examples.
    
Also you can use
    
 ```
 docker logs DOCKER_SERVICE_NAME
 ```
    
For getting names of all Docker running containers, just run:
    
 ```
 docker ps
 ```
    
For getting names of all Docker containers, run:
    
 ```
 docker ps -a
 ```

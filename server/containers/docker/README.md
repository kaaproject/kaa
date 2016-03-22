# Kaa Docker image

This readme contain information about Kaa Docker image deployment.

Default environments that comes in this package is:
  - Fully functional Kaa cluster (1x Kaa node, 1x Zookeeper node, 1x Z
    - Kaa node
    - Zookeeper node
    - PostgreSQL
    - Cassandra
    - MongoDB
  - Single Kaa node, that can be deployed to an existing cluster

Base image configuration done with next environment variables:
| Variable name      | Sample values                    | Description                                                      |
|--------------------|----------------------------------|------------------------------------------------------------------|
| CONTROL_ENABLED    | true/false                       | Determines whether control server enabled on this node or not    |
| BOOTSTRAP_ENABLED  | true/false                       | Determines whether bootstrap server enabled on this node or not  |
| OPERATIONS_ENABLED | true/false                       | Determines whether operations server enabled on this node or not |
| DATABASE           | cassandra/mongodb                | Determines whether Cassandra or MongoDB provider will be used    |
| ZK_HOSTS           | localhost:2181, anotherhost:2181 | Comma-separated list of Zookeeper nodes hostname:port            |
| CASSANDRA_HOSTS    | localhost:9042, ...              | Comma-separated list of Cassandra nodes hostname:port            |
| MONGODB_HOSTS      | localhost:27017, ...             | Comma-separated list of MongoDB nodes hostname:port              |
| JDBC_HOST          | localhost                        | PostgreSQL database hostname                                     |
| JDBC_PORT          | 5432                             | PostgreSQL database port                                         |

## Steps to deploy

1. From node/ folder execute:
```sh
$ docker build -t kaaproject/node:0.9.0 .
```
2. Return to docker/ folder and run:
```sh
$ docker-compose -f docker-compose-cluster-1-node.yaml up -d
```
If you want deploy single node to external cluster, edit docker-compose-1-node.yaml file (set correct service hostnames) and run
```sh
$ docker-compose -f docker-compose-1-node.yaml up -d
```
3. Execute following command to get into running container:
```sh
$ docker exec -i -t docker_kaa_node_1 bash
```


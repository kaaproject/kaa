---
layout: page
title: Cluster setup guide
permalink: /:path/
sort_idx: 30
---

* [Introduction](#introduction)
* [Cluster configuration](#cluster-configuration)
  * [List of properties](#list-of-properties)
  * [Kaa-node configuration](#kaa-node-configuration)
  * [Zookeeper configuration](#zookeeper-configuration)
  * [SQL database configuration](#sql-database-configuration)
  * [NoSQL database configuration](#nosql-database-configuration)
  * [Firewall rules configuration](#firewall-rules-configuration)

## Introduction

This page describes how to setup and configure kaa-node cluster.

In general cluster setup is similar to [single node setup](../Single-node-installation), except few details. We need at least 3 nodes to create a reliable cluster. For simplicity in this tutorial we will be using single instance of SQL and NoSQL databases, so this tutorial doesn't cover such themes like setting up Cassandra, MongoDB or PostgreSQL cluster setup. Let us assume that we have 3 nodes with Ubuntu 14.04 installed on each of them.

```bash
node1 172.1.1.1
node2 172.2.2.2 
node3 172.3.3.3
```

On node1 we will setup our SQL (PostgreSql) and NoSQl (MongoDB) databases and run Zookeeper.

We had successfully installed Kaa on every node and everything that left is to configure kaa-node services.

## Cluster configuration

### List of properties

It is necessary to edit next properties to set up cluster:
 
 | Property name             | Example values                   | Description                                                      | File location                                                              | 
 |-------------------------- |--------------------------------- | ---------------------------------------------------------------- | -------------------------------------------------------------------------- |
 | control_server_enabled    | true/false                       | Determines whether control server enabled on this node or not    | /etc/kaa-node/conf/kaa-node.properties                                     |
 | bootstrap_server_enabled  | true/false                       | Determines whether bootstrap server enabled on this node or not  | /etc/kaa-node/conf/kaa-node.properties                                     |
 | operations_server_enabled | true/false                       | Determines whether operations server enabled on this node or not | /etc/kaa-node/conf/kaa-node.properties                                     |
 | zk_host_port_list         | localhost:2181, anotherhost:2181 | Comma-separated list of Zookeeper nodes hostname:port            | /etc/kaa-node/conf/kaa-node.properties                                     |
 | node id                   | 1-255                            | single line of text that represents node id                      | /etc/zookeeper/myid                                                        |
 | jdbc_host                 | localhost                        | PostgreSQL database hostname                                     | /etc/kaa-node/conf/dao.properties, /etc/kaa-node/conf/admin-dao.properties |
 | jdbc_port                 | 5432                             | PostgreSQL database port                                         | /etc/kaa-node/conf/dao.properties, /etc/kaa-node/conf/admin-dao.properties |
 | nosql_db_provider_name    | cassandra/mongodb                | Determines whether Cassandra or MongoDB provider will be used    | /etc/kaa-node/conf/dao.properties                                          |
 | node_list                 | localhost:9042, ...              | Comma-separated list of Cassandra nodes hostname:port            | /etc/kaa-node/conf/common-dao-cassandra.properties                         |
 | servers                   | localhost:27017, ...             | Comma-separated list of MongoDB nodes hostname:port              | /etc/kaa-node/conf/common-dao-mongodb.properties                           |

<br/>
After Kaa installation on Ubuntu/Debian OS (deb packages), configuration files for each Kaa component will be extracted into the ```/etc/kaa-{component-name}/conf``` directories. We are interested in 3 configuration files.

### Kaa-node configuration

Stop kaa-node service before starting configuration by executing next command:

```bash
 $ sudo service kaa-node stop
```

Kaa services (bootstrap, control or operations) can be enabled or disabled on node by editing corresponding properties in ```/etc/kaa-node/conf/kaa-node.properties``` file.

```bash
# Specifies if Control Server is enabled.
control_server_enabled=true

# Specifies if Bootstrap Server is enabled.
bootstrap_server_enabled=true

# Specifies if Operations Server is enabled.
operations_server_enabled=true
```

Kaa-node transport properties, in this properties we need to specify IP address of current node.

```bash
 # The Control Server notifies every Operations/Bootstrap Server on most data updates via a Thrift-based protocol.

 # Thrift server host
 thrift_host=<ip_of_current_machine>

 # Interface that will be reported by all transports
 transport_public_interface=<ip_of_current_machine>
```

So for ```node1``` this properties will look like this

```bash
 thrift_host=172.1.1.1
 transport_public_interface=172.1.1.1
```

For ```node2```

```bash
 thrift_host=172.2.2.2
 transport_public_interface=172.2.2.2
```

And for ```node3```

```bash
 thrift_host=172.3.3.3
 transport_public_interface=172.3.3.3
```

### Zookeeper configuration

Kaa zookeeper hosts - list of all zookeeper services on nodes.

```bash
 # Zookeeper service url list.
 zk_host_port_list=<zookeeper_ip>:<zookeeper_port>
```

We assume that zookeeper with default port (2181) is running on every node. Configuration would look as follow:

```bash
 # node1
 zk_host_port_list=172.1.1.1:2181,172.2.2.2:2181,172.3.3.3:2181

 # node2
 zk_host_port_list=172.1.1.1:2181,172.2.2.2:2181,172.3.3.3:2181

 # node3
 zk_host_port_list=172.1.1.1:2181,172.2.2.2:2181,172.3.3.3:2181
```

For every node insert node id in file ```/etc/zookeeper/myid```. The myid file consists of a single line containing only the text of that machine's id. So myid of server 1 would contain the text "1" and nothing else. The id must be unique within the ensemble and should have a value between 1 and 255. For example we have 3 nodes so we will have next values 1, 2, 3. For more details visit this [documentation page](https://zookeeper.apache.org/doc/r3.3.2/zookeeperAdmin.html#sc_zkMulitServerSetup).

So for ```node1``` myid would look like this

```bash
 1
```

For ```node2```

```bash
 2
```

And for ```node3```

```bash
 3
```

### SQL database configuration

Configure SQL database host and port ```/etc/kaa-node/conf/dao.properties```

```bash
 # specify jdbc database host
 jdbc_host=<postgresql_ip>
 
 # specify jdbc database post
 jdbc_port=<postgresql_port>
```

Configurations for all three nodes would look like this

```bash
 jdbc_host=172.1.1.1
 jdbc_port=5432
```

and ```/etc/kaa-node/conf/admin-dao.properties```

```bash
 # specify jdbc database url
 jdbc_url=jdbc:postgresql://<postgresql_ip>:<postgresql_port>/kaa
```

For all three nodes it would be like this

```bash
 jdbc_url=jdbc:postgresql://172.1.1.1:5432/kaa
```

And also configure [username and password](../Single-node-installation#sql-database-configuration)

### NoSQL database configuration

Select NoSQL database ```mongo``` or ```cassandra```in ```/etc/kaa-node/conf/dao.properties``` file.

```bash
 # NoSQL database provider name, autogenerated when mongo-dao or cassandra-dao profile is activated
 # Possible options: mongodb, cassandra
 nosql_db_provider_name=<no_sql_database_name>
```

For all three nodes it would be like this

```bash
 nosql_db_provider_name=mongodb
```

Setup MongoDB host IP ```/etc/kaa-node/conf/common-dao-mongodb.properties```

```bash
 # list of mongodb nodes, possible to use multiply servers
 servers=<mongo_database_ip>:<mongo_database_port>
```

Assuming that we peek standard MongoDB port, for all three nodes property would look like this

```bash
 servers=172.1.1.1:27017
```

Setup Cassandra host ip ```/etc/kaa-node/conf/common-dao-cassandra.properties```

```bash
 # Specify node list
node_list=<cassandra_database_ip>:<cassandra_database_port>
```

Assuming that we peek standard Cassandra port, for all three nodes property would look like this

```bash
 servers=172.1.1.1:9042
```

### Firewall rules configuration

On every endpoin Kaa Administrator needs to configure [firewall rules](../Single-node-installation#firewall-rules-configuration).

After all configuration properties set up

```bash
 $ sudo service kaa-node start
```

If everithing is configured properly Kaa Administrator will see next output on command:

```bash
 $ sudo service kaa-node status 
  * Kaa Node daemon is running
```

---

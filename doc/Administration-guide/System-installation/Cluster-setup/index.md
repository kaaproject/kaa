---
layout: page
title: Cluster setup guide
permalink: /:path/
sort_idx: 30
---

{% include variables.md %}

* TOC
{:toc}

> **Verified against host OS:**
> 
> * Ubuntu 14.04 LTS Desktop 64 bit

This guide describes configuration of Kaa cluster on Linux nodes.

The guide contains instructions on how to configure kaa-node service and required third party components like [Apache Zookeeper](https://zookeeper.apache.org/) service, SQL and NoSQL databases.

## Requirements

In order to set up Kaa cluster you need to have at least 3 Linux nodes with ```kaa-node``` service installed on each of them to create a reliable cluster, refer to [Single node installation]({{root_url}}Administration-guide/System-installation/Single-node-installation/) guide for installation details. 

> Note: Each Kaa server should have different RSA security key-pairs.
> 
> Every ```kaa-node``` service generates two sets of public.key and private.key file pairs for [Bootstrap]({{root_url}}Architecture-overview/#bootstrap-service) and [Operations]({{root_url}}Architecture-overview/#operations-service) services. 
> To ensure that every ```kaa-node``` service use different security key-pairs you can compare checksums of key files on different servers. 
> 
> ```bash
> $ sudo md5sum /usr/lib/kaa-node/keys/bootstrap/public.key
>  2df11d4006dbe69c4d208d24f52ea2eb /usr/lib/kaa-node/keys/bootstrap/public.key
> 
> $ sudo md5sum /usr/lib/kaa-node/keys/bootstrap/private.key
>  cc6f4c598c1ac2cd0c414b54dad953db /usr/lib/kaa-node/keys/bootstrap/private.key
> 
> $ sudo md5sum /usr/lib/kaa-node/keys/operations/public.key 
>  8d9b8d4838ff03ddac52dec97797546c /usr/lib/kaa-node/keys/operations/public.key
> 
> $ sudo md5sum /usr/lib/kaa-node/keys/operations/private.key 
>  f9aab71ef879916d24dfd5200ad94ccd /usr/lib/kaa-node/keys/operations/private.key 
> ```
> In order to refresh key files stop ```kaa-node``` service, remove key files and start the service again.

Also ```kaa-node``` service requires some third party dependencies like SQL and NoSQL databases and Apache Zookeeper service, more details you can find in [Architecture overview]({{root_url}}Architecture-overview/). 
You can find detailed instructions on how to install and configure Zookeeper service and one of supported SQL and NoSQL databases in [Single node installation]({{root_url}}Administration-guide/System-installation/Single-node-installation/#installation-steps) guide. 
A set of databases (for example MongoDB + PostgreSQL) depends on your particular use case.

In this guide we assume that you had already set up your SQL and NoSQL database clusters, so this tutorial doesn't cover such themes like setting up ones for Cassandra, MongoDB or PostgreSQL. 
Refer to official [Cassandra](http://docs.datastax.com/en/landing_page/doc/landing_page/current.html), [MongoDB](https://docs.mongodb.com/manual/) and [PostgreSQL](https://www.postgresql.org/docs/) documentation in order to setup corresponding database cluster.

MariaDB come with out-of-the-box master-master replication support thus we recommend to use this database in your Kaa cluster. 
You can find detailed instructions in [MariaDB cluster setup guide]({{root_url}}Administration-guide/System-installation/Cluster-setup/MariaDB-cluster-setup-guide/).

> Note: In a cluster you need to connect to databases from external hosts so you need to allow such external connections in corresponding database security configurations and configure firewall rules for database host machine. 
> Refer to official documentation for corresponding database for security configuration details.

In addition to added on ```kaa-node``` service installation step firewall rules on every endpoint Kaa Administrator need to add some more rules for databases (depending on a set databases in the cluster) and Zookeeper ports.

> Note: This configuration will not affect AWS deployment or any other cloud provider and must be applied only if you setting up cluster on VMs or separate Linux machines. 
> Next instructions depend on specific cloud provider. 

```bash
# MongoDB port
$ sudo iptables -I INPUT -p tcp -m tcp --dport 27017 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 27017 -j ACCEPT

# Cassandra port
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9042 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 9042 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 7000 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 7000 -j ACCEPT
# 7001 if SSL is enabled
$ sudo iptables -I INPUT -p tcp -m tcp --dport 7001 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 7001 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 7199 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 7199 -j ACCEPT
# for the Cassandra Thrift client
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9160 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 9160 -j ACCEPT

# MariaDB port 
$ sudo iptables -I INPUT -p tcp -m tcp --dport 3306 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 3306 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 4444 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 4444 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 4567 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 4567 -j ACCEPT

# PostgreSQL port
$ sudo iptables -I INPUT -p tcp -m tcp --dport 5432 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 5432 -j ACCEPT

# Zookeeper port
$ sudo iptables -I INPUT -p tcp -m tcp --dport 2181 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 2181 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 2888 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 2888 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 3888 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 3888 -j ACCEPT

$ sudo apt-get install iptables-persistent
$ sudo service iptables-persistent start
$ sudo service iptables-persistent save
```

## Introduction

The following is the hosts list that we had setup for this guide.

```bash
node1 172.1.1.1
node2 172.2.2.2
node3 172.3.3.3
```

On each of nodes were installed NoSQL database (MongoDB or Cassandra), Zookeeper service and Kaa node service and everything that left it is to configure ```kaa-node``` services. 
In this guide we chose default ports for databases and Zookeeper service: 

| Service    | Port  |
| ---------- | ----- |
| MongoDB    | 27017 |
| Cassandra  | 9042  |
| MariaDB    | 3306  |
| PostgreSQL | 5432  |
| Zookeeper  | 2181  |

## Cluster configuration steps

### Stop Kaa node service

Ensure the existing ```kaa-node``` service is stopped before starting configuration by executing next command:

```bash
 $ sudo service kaa-node stop
```

### Kaa node configuration

Kaa services (Bootstrap, Control or Operations) can be enabled or disabled on Kaa node by editing corresponding properties in ```/etc/kaa-node/conf/kaa-node.properties``` file.

```bash
# Specifies if Control Service is enabled.
control_service_enabled=true

# Specifies if Bootstrap Service is enabled.
bootstrap_service_enabled=true

# Specifies if Operations Service is enabled.
operations_service_enabled=true
```

In kaa-node transport properties specify IP address (or host name) of current node.

```bash
 # The Control Service notifies every Operations/Bootstrap Service on most data updates via a Thrift-based protocol.

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

Specify list of all zookeeper services hosts in the cluster.

```bash
 # Zookeeper service url list.
 zk_host_port_list=<zookeeper_ip>:<zookeeper_port>
```

Zookeeper services are running on every node with default ports (2181). For every node configuration would look as follow:

```bash
 zk_host_port_list=172.1.1.1:2181,172.2.2.2:2181,172.3.3.3:2181
```

For every node insert node id in file ```/etc/zookeeper/myid```. The myid file consists of a single line containing only the text of that machine's id. So myid of server 1 would contain the text "1" and nothing else. The id must be unique within the ensemble and should have a value between 1 and 255. For example we have 3 nodes so we will have next values 1, 2, 3. For more details visit this [documentation page](https://zookeeper.apache.org/doc/r3.3.2/zookeeperAdmin.html#sc_zkMulitServerSetup).

Paste next command in command line of each node:

```bash
 sudo su -c 'echo $N > /etc/zookeeper/myid'
```

where **`$N`** is proper node ID from range(1-255).

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

SQL database configuration will be almost the same as in single node setup except for database host, refer to [Single node installation guide - SQL database configuration]({{root_url}}Administration-guide/System-installation/Single-node-installation/#sql-database-configuration) section for more details.

In order to configure SQL database in a cluster follow next steps:

1. Set SQL database host and port properties in ```/etc/kaa-node/conf/sql-dao.properties``` configuration file

   <ul class="nav nav-tabs">
     <li class="active"><a data-toggle="tab" href="#MariaDB">MariaDB</a></li>
     <li><a data-toggle="tab" href="#PostgreSQL">PostgreSQL</a></li>
   </ul>

   <div class="tab-content">
   
   <div id="MariaDB" class="tab-pane fade in active" markdown="1">

   Configurations for all three nodes would look like this
   
   ```bash
   # specify jdbc database hosts and ports
   jdbc_host_port=172.1.1.1:3306,172.2.2.2:3306,172.3.3.3:3306
   
   # specify jdbc database provider name
   sql_provider_name=mysql:failover
   ```
   
   </div><div id="PostgreSQL" class="tab-pane fade" markdown="1">

   Configurations for all three nodes would look like this

   ```bash
    # specify jdbc database hosts and ports
    jdbc_host_port=172.1.1.1:5432,172.2.2.2:5432,172.3.3.3:5432
    
    # specify jdbc database provider name
    sql_provider_name=postgresql
   ```
   
   </div></div>

2. Set SQL database URL property in ```/etc/kaa-node/conf/admin-dao.properties```

   <ul class="nav nav-tabs">
     <li class="active"><a data-toggle="tab" href="#MariaDB1">MariaDB</a></li>
     <li><a data-toggle="tab" href="#PostgreSQL1">PostgreSQL</a></li>
   </ul>
   
   <div class="tab-content">
   
   <div id="MariaDB1" class="tab-pane fade in active" markdown="1">
   
   <br>
   
   For all three nodes it would be like this
   
   ```bash
   # specify jdbc database url
   jdbc_url=jdbc:mysql:failover://172.1.1.1:3306,172.2.2.2:3306,172.3.3.3:3306/kaa
   ```
   
   <br>
   
   </div><div id="PostgreSQL1" class="tab-pane fade" markdown="1">
   
   <br>
   
   For all three nodes it would be like this
   
   ```bash
    # specify jdbc database url
    jdbc_url=jdbc:postgresql://172.1.1.1:5432,172.2.2.2:5432,172.3.3.3:5432/kaa
   ```
   
   <br>
   
   </div></div>

And also configure [username and password]({{root_url}}Administration-guide/System-installation/Single-node-installation#sql-database-configuration).

### NoSQL database configuration

NoSQL database configuration will be almost the same as in single node setup too except for database node list, refer to [Single node installation guide - NoSQL database configuration]({{root_url}}Administration-guide/System-installation/Single-node-installation/#nosql-database-configuration) section for more details. 
Select NoSQL database ```mongo``` or ```cassandra``` in ```/etc/kaa-node/conf/nosql-dao.properties``` file.

```bash
 # NoSQL database provider name, autogenerated when mongo-dao or cassandra-dao profile is activated
 # Possible options: mongodb, cassandra
 nosql_db_provider_name=<no_sql_database_name>
```

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#MongoDB">MongoDB</a></li>
  <li><a data-toggle="tab" href="#Cassandra">Cassandra</a></li>
</ul>

<div class="tab-content">

<div id="MongoDB" class="tab-pane fade in active" markdown="1">

<br>

For all three nodes it would be like this

```bash
 nosql_db_provider_name=mongodb
```

Setup MongoDB host IP in ```/etc/kaa-node/conf/common-dao-mongodb.properties``` file. 
Assuming that we peek standard MongoDB port, for all three nodes property would look like this

```bash
 # list of mongodb nodes, possible to use multiply servers
 servers=172.1.1.1:27017,172.2.2.2:27017,172.3.3.3:27017
```

<br>

</div><div id="Cassandra" class="tab-pane fade" markdown="1">

<br>

For all three nodes it would be like this

```bash
 nosql_db_provider_name=cassandra
```

Setup Cassandra host IP in ```/etc/kaa-node/conf/common-dao-cassandra.properties``` file. 
Assuming that we peek standard Cassandra port, for all three nodes property would look like this

```bash
 # Specify node list
 node_list=172.1.1.1:9042,172.2.2.2:9042,172.3.3.3:9042
```

<br>

</div></div>

### Start up Kaa node service

After all configuration properties set up

```bash
 $ sudo service kaa-node start
```

If everything is configured properly Kaa Administrator will see next output on command:

```bash
 $ sudo service kaa-node status
  * Kaa Node daemon is running
```

Check logs for exceptions after the startup.

```bash
 $ grep ERROR /var/log/kaa/*
```

If configuration was successful you wouldn't see any errors in logs.

---

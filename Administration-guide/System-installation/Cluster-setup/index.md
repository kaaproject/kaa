---
layout: page
title: Cluster setup guide
permalink: /:path/
sort_idx: 30
---

* TOC
{:toc}

This page describes how to setup and configure Kaa cluster.

In general cluster setup is similar to [Single node installation]({{root_url}}Administration-guide/System-installation/Single-node-installation/), except few details. 
We need at least 3 nodes to create a reliable cluster. 

The following is the hosts list that we had setup for this guide.

```bash
node1 172.1.1.1
node2 172.2.2.2
node3 172.3.3.3
```

On each of nodes were installed NoSQl database (MongoDB or Cassandra), Zookeeper service and Kaa node service and everything that left it is to configure kaa-node services. 
In this guide we chose default ports for databases and Zookeeper service: for MongoDB it is 27017, Cassandra - 9042, MariaDB - 3306, PostgreSQL - 5432 and Zookeeper - 2181.

In this guide we assume that you had already set up your SQL and NoSQL database's clusters, so this tutorial doesn't cover such themes like setting up ones for Cassandra, MongoDB or PostgreSQL. 
Refer to official [Cassandra](http://docs.datastax.com/en/landing_page/doc/landing_page/current.html), [MongoDB](https://docs.mongodb.com/manual/) and [PostgreSQL](https://www.postgresql.org/docs/) documentation in order to setup corresponding database cluster.

MariaDB come with out of the box master-master replication support thus we recommend to use this database in your Kaa cluster setup. 
You can find detailed instructions in [MariaDB cluster setup guide]({{root_url}}/Administration-guide/System-installation/Cluster-setup/MariaDB-cluster-setup-guide/).

## List of configuration properties

You can find configuration files for Kaa node service in the ```/etc/kaa-node/conf``` directory.
List of properties that you need to edit in order to set up cluster:

 | Property name             | Example values                                                                 | Description                                                      | File location                                                              |
 |-------------------------- |------------------------------------------------------------------------------- | ---------------------------------------------------------------- | -------------------------------------------------------------------------- |
 | control_server_enabled    | true/false                                                                     | Determines whether control server enabled on this node or not    | /etc/kaa-node/conf/kaa-node.properties                                     |
 | bootstrap_server_enabled  | true/false                                                                     | Determines whether bootstrap server enabled on this node or not  | /etc/kaa-node/conf/kaa-node.properties                                     |
 | operations_server_enabled | true/false                                                                     | Determines whether operations server enabled on this node or not | /etc/kaa-node/conf/kaa-node.properties                                     |
 | zk_host_port_list         | localhost:2181, 172.1.1.1:2181                                                 | Comma-separated list of Zookeeper nodes hostname:port            | /etc/kaa-node/conf/kaa-node.properties                                     |
 | node id                   | 1-255                                                                          | Single line of text that represents node id                      | /etc/zookeeper/myid                                                        |
 | sql_host_port             | 172.1.1.1:3306                                                                 | SQL database (MariaDB or PostgreSQL) host/ip_address and port    | /etc/kaa-node/conf/sql-dao.properties                                      |
 | sql_provider_name         | postgresql, mysql:failover                                                     | JDBC database provider name                                      | /etc/kaa-node/conf/sql-dao.properties                                      |
 | jdbc_url                  | jdbc:mysql:failover://172.1.1.1:3306/kaa, jdbc:postgresql://172.1.1.1:5432/kaa | SQL database host                                                | /etc/kaa-node/conf/admin-dao.properties                                    |
 | nosql_db_provider_name    | cassandra/mongodb                                                              | Determines whether Cassandra or MongoDB provider will be used    | /etc/kaa-node/conf/nosql-dao.properties                                    |
 | node_list                 | 172.1.1.1:9042, 172.2.2.2:9042, ...                                            | Comma-separated list of Cassandra nodes hostname:port            | /etc/kaa-node/conf/common-dao-cassandra.properties                         |
 | servers                   | 172.1.1.1:27017, 172.2.2.2:27017, ...                                          | Comma-separated list of MongoDB nodes hostname:port              | /etc/kaa-node/conf/common-dao-mongodb.properties                           |

<br/>

## Cluster configuration steps

### Stop Kaa node service

Stop kaa-node service before starting configuration by executing next command:

```bash
 $ sudo service kaa-node stop
```

### Kaa node configuration

Kaa services (bootstrap, control or operations) can be enabled or disabled on Kaa node by editing corresponding properties in ```/etc/kaa-node/conf/kaa-node.properties``` file.

```bash
# Specifies if Control Server is enabled.
control_server_enabled=true

# Specifies if Bootstrap Server is enabled.
bootstrap_server_enabled=true

# Specifies if Operations Server is enabled.
operations_server_enabled=true
```

In kaa-node transport properties specify IP address (or host name) of current node.

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
   jdbc_host_port=127.1.1.1:3306,127.2.2.2:3306,127.3.3.3:3306
   
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

2. and ```/etc/kaa-node/conf/admin-dao.properties```

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
   jdbc_url=jdbc:mysql:failover://172.1.1.1:5432,172.2.2.2:5432,172.3.3.3:5432/kaa
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

And also configure [username and password]({{root_url}}Administration-guide/System-installation/Single-node-installation#sql-database-configuration)

### NoSQL database configuration

NoSQL database configuration will be almost the same as in single node setup too except for database node list, refer to [Single node installation guide - NoSQL database configuration]({{root_url}}Administration-guide/System-installation/Single-node-installation/#nosql-database-configuration) section for more details. 
Select NoSQL database ```mongo``` or ```cassandra``` in ```/etc/kaa-node/conf/dao.properties``` file.

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

Setup Cassandra host ip in ```/etc/kaa-node/conf/common-dao-cassandra.properties``` file. 
Assuming that we peek standard Cassandra port, for all three nodes property would look like this

```bash
 # Specify node list
 node_list=127.1.1.1:9042,127.2.2.2:9042,127.3.3.3:9042
```

<br>

</div></div>

### Firewall rules configuration

On every endpoint Kaa Administrator need to configure [firewall rules]({{root_url}}Administration-guide/System-installation/Single-node-installation#firewall-rules-configuration) same as in Single node installation. Additionally Kaa administrator need to configure firewall rules for databases (depending on databases setup) and Zookeeper ports.

```bash
# MongoDB port
$ sudo iptables -I INPUT -p tcp -m tcp --dport 27017 -j ACCEPT

# Cassandra port
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9042 -j ACCEPT

# MariaDB port 
$ sudo iptables -I INPUT -p tcp -m tcp --dport 3306 -j ACCEPT

# PostgreSQL port
$ sudo iptables -I INPUT -p tcp -m tcp --dport 5432 -j ACCEPT

# Zookeeper port
$ sudo iptables -I INPUT -p tcp -m tcp --dport 2181 -j ACCEPT

$ sudo apt-get install iptables-persistent
$ sudo service netfilter-persistent start
$ sudo netfilter-persistent save
```

### Start up Kaa node service

After all configuration properties set up

```bash
 $ sudo service kaa-node start
```

If everithing is configured properly Kaa Administrator will see next output on command:

```bash
 $ sudo service kaa-node status
  * Kaa Node daemon is running
```

Check logs for exceptions after the startup.

```bash
 $ cd /var/log/kaa
 $ cat * | grep ERROR
```

If configuration was sucessfull you wouldn't see any errors in logs.

---

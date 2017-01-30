---
layout: page
title: Cluster setup
permalink: /:path/
sort_idx: 30
---

{% include variables.md %}

* TOC
{:toc}

This guide describes how to configure a [Kaa cluster]({{root_url}}Glossary/#kaa-cluster) on Linux nodes.

It includes instructions on how to configure the **Kaa node** service and the required third party components such as [Apache Zookeeper](https://zookeeper.apache.org/) service, SQL and NoSQL databases.

>**NOTE:** This guide is verified against Ubuntu 14.04 LTS Desktop 64-bit.
{:.note}

## Prerequisites

To set up a Kaa cluster, you need to have at least 3 Linux nodes with the Kaa node service installed on each of them.
See [Single node installation]({{root_url}}Administration-guide/System-installation/Single-node-installation/).

Every Kaa node service generates two sets of `public.key` and `private.key` file pairs for [Bootstrap]({{root_url}}Architecture-overview/#bootstrap-service) and [Operations]({{root_url}}Architecture-overview/#operations-service) services.
Each Kaa server must have a unique RSA security key pair.
To ensure that every Kaa node service uses a unique security key pair, you can compare checksums of the key files on different servers.

```bash
$ sudo md5sum /usr/lib/kaa-node/keys/bootstrap/public.key
2df11d4006dbe69c4d208d24f52ea2eb /usr/lib/kaa-node/keys/bootstrap/public.key

$ sudo md5sum /usr/lib/kaa-node/keys/bootstrap/private.key
cc6f4c598c1ac2cd0c414b54dad953db /usr/lib/kaa-node/keys/bootstrap/private.key

$ sudo md5sum /usr/lib/kaa-node/keys/operations/public.key
8d9b8d4838ff03ddac52dec97797546c /usr/lib/kaa-node/keys/operations/public.key

$ sudo md5sum /usr/lib/kaa-node/keys/operations/private.key
f9aab71ef879916d24dfd5200ad94ccd /usr/lib/kaa-node/keys/operations/private.key
```

To refresh the key files, stop the `kaa-node` service, replace the key files, and start the service again.

The Kaa node service requires some third party dependencies, such as SQL and NoSQL databases, and Apache Zookeeper service.
For more information, see [Architecture overview]({{root_url}}Architecture-overview/).
A choice of databases (for example, MongoDB + PostgreSQL) depends on your particular use case.
For database cluster installation, refer to the official [Cassandra](http://cassandra.apache.org/), [MongoDB](https://docs.mongodb.com/manual/) and [PostgreSQL](https://www.postgresql.org/docs/) documentation.

See also [MariaDB cluster setup guide]({{root_url}}Administration-guide/System-installation/Cluster-setup/MariaDB-cluster-setup-guide/).

To allow connections to the databases from external hosts, configure the corresponding database security settings and set up the firewall rules for the database host machine.
For security configuration settings, refer to the database official documentation.

In addition to the firewall rules set up during the Kaa node service installation process, you need to add some rules for the databases (depending on the choice of databases in the cluster), Kaa node thrift and Zookeeper ports.

>**NOTE:** This configuration will not affect AWS deployment or any other cloud provider and must be applied only if you are setting up a Kaa cluster on VMs or separate Linux machines.
>The following instructions can vary depending on a chosen cloud provider.

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

# Kaa node thrift
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9090 -j ACCEPT
$ sudo iptables -I OUTPUT -p tcp -m tcp --dport 9090 -j ACCEPT

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

## General settings

As an example, let's assume that the following hosts are set up.

```bash
node1 172.1.1.1
node2 172.2.2.2
node3 172.3.3.3
```

A NoSQL database (MongoDB or Cassandra), a Zookeeper service, and the Kaa node service is installed on each node.

In this guide, the following default ports are used for databases and the Zookeeper service.

| Service    | Port  |
| ---------- | ----- |
| MongoDB    | 27017 |
| Cassandra  | 9042  |
| MariaDB    | 3306  |
| PostgreSQL | 5432  |
| Zookeeper  | 2181  |

As the next step, you need to configure your Kaa cluster.

## Configure Kaa cluster

### Stop Kaa node service

Before making changes to the configuration, make sure that the current Kaa node service is stopped.

```bash
$ sudo service kaa-node stop
```

### Configure Kaa node

To enable or disable the Kaa services (Bootstrap, Control or Operations), edit the corresponding properties in the `/etc/kaa-node/conf/kaa-node.properties` file.

```bash
# Specifies if Control Service is enabled.
control_service_enabled=true

# Specifies if Bootstrap Service is enabled.
bootstrap_service_enabled=true

# Specifies if Operations Service is enabled.
operations_service_enabled=true
```

In the Kaa node transport properties, specify the IP address (or the host name) of the current node.

```bash
# The Control Service notifies every Operations/Bootstrap Service on most data updates via a Thrift-based protocol.

# Thrift server host
thrift_host=<ip_of_current_machine>

# Interface that will be reported by all transports
transport_public_interface=<ip_of_current_machine>
```

So these properties will look as follows for `node1`.

```bash
thrift_host=172.1.1.1
transport_public_interface=172.1.1.1
```

Properties for `node2`.

```bash
thrift_host=172.2.2.2
transport_public_interface=172.2.2.2
```

Properties for `node3`.

```bash
thrift_host=172.3.3.3
transport_public_interface=172.3.3.3
```

### Configure Zookeeper

Specify all Zookeeper service hosts in the cluster.

```bash
# Zookeeper service url list.
zk_host_port_list=<zookeeper_ip>:<zookeeper_port>
```

Zookeeper services run on every node using the default port (2181).
Therefore, the node configuration will look as follows.

```bash
zk_host_port_list=172.1.1.1:2181,172.2.2.2:2181,172.3.3.3:2181
```

For each node, specify the node ID in the `/etc/zookeeper/myid` file.
This file consists a single string of text representing the ID of the machine.
The ID must be unique within the cluster and must be a value between 1 and 255.
For this example, let's have **1**, **2**, and **3** as the IDs of the node 1, node 2, and node 3 accordingly.
See also  [Zookeper Clustered Setup](https://zookeeper.apache.org/doc/r3.3.2/zookeeperAdmin.html#sc_zkMulitServerSetup).

Run the following command in the command line of each node.

```bash
sudo su -c 'echo $N > /etc/zookeeper/myid'
```

In this command, `$N` is the node ID value in the 1-255 range.

So `$N` is **1** for `node1`, **2** for `node2`, and **3** for `node3`.


### Configure SQL database

The SQL database configuration process is similar to the single node setup except for the database host settings.
See [SQL database configuration]({{root_url}}Administration-guide/System-installation/Single-node-installation/#sql-database-configuration).

To configure an SQL database in the cluster:

1. Set the SQL database host and port properties in the `/etc/kaa-node/conf/sql-dao.properties` configuration file.

   <ul class="nav nav-tabs">
     <li class="active"><a data-toggle="tab" href="#MariaDB">MariaDB</a></li>
     <li><a data-toggle="tab" href="#PostgreSQL">PostgreSQL</a></li>
   </ul>

   <div class="tab-content">
   
   <div id="MariaDB" class="tab-pane fade in active" markdown="1">

   ```bash
   # specify jdbc database hosts and ports
   jdbc_host_port=172.1.1.1:3306,172.2.2.2:3306,172.3.3.3:3306
   
   # specify jdbc database provider name
   sql_provider_name=mysql:failover
   ```
   
   </div><div id="PostgreSQL" class="tab-pane fade" markdown="1">

   ```bash
    # specify jdbc database hosts and ports
    jdbc_host_port=172.1.1.1:5432,172.2.2.2:5432,172.3.3.3:5432
    
    # specify jdbc database provider name
    sql_provider_name=postgresql
   ```
   
   </div></div>

2. Set the SQL database URL property in the `/etc/kaa-node/conf/admin-dao.properties` file.

   <ul class="nav nav-tabs">
     <li class="active"><a data-toggle="tab" href="#MariaDB1">MariaDB</a></li>
     <li><a data-toggle="tab" href="#PostgreSQL1">PostgreSQL</a></li>
   </ul>
   
   <div class="tab-content">
   
   <div id="MariaDB1" class="tab-pane fade in active" markdown="1">
   
   ```bash
   # specify jdbc database url
   jdbc_url=jdbc:mysql:failover://172.1.1.1:3306,172.2.2.2:3306,172.3.3.3:3306/kaa
   ```
   
   </div><div id="PostgreSQL1" class="tab-pane fade" markdown="1">
   
   ```bash
   # specify jdbc database url
   jdbc_url=jdbc:postgresql://172.1.1.1:5432,172.2.2.2:5432,172.3.3.3:5432/kaa
   ```

   </div></div>

3. Configure the [username and password]({{root_url}}Administration-guide/System-installation/Single-node-installation#sql-database-configuration).

### Configure NoSQL database

The NoSQL database configuration process is similar to the single node setup except for the database node list.
See [NoSQL database configuration]({{root_url}}Administration-guide/System-installation/Single-node-installation/#nosql-database-configuration).

Select **mongo** or **cassandra** NoSQL database in the `/etc/kaa-node/conf/nosql-dao.properties` file.

```bash
# The NoSQL database provider name, auto-generated when the mongo-dao or cassandra-dao profile is activated
# Possible options: mongodb, cassandra
nosql_db_provider_name=<no_sql_database_name>
```

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#MongoDB">MongoDB</a></li>
  <li><a data-toggle="tab" href="#Cassandra">Cassandra</a></li>
</ul>

<div class="tab-content">

<div id="MongoDB" class="tab-pane fade in active" markdown="1">

Specify database provider.

```bash
nosql_db_provider_name=mongodb
```

Set up the MongoDB host IP in the `/etc/kaa-node/conf/common-dao-mongodb.properties` file.
Assuming that you selected the standard MongoDB port, the property will be as follows for all the three nodes.

```bash
# a list of mongodb nodes, possible to use multiple servers
servers=172.1.1.1:27017,172.2.2.2:27017,172.3.3.3:27017
```

</div><div id="Cassandra" class="tab-pane fade" markdown="1">

Specify database provider.

```bash
nosql_db_provider_name=cassandra
```

Set up the Cassandra host IP in the `/etc/kaa-node/conf/common-dao-cassandra.properties` file.
Assuming that you selected the standard Cassandra port, the property will be as follows for all the three nodes.

```bash
# Specify the node list
node_list=172.1.1.1:9042,172.2.2.2:9042,172.3.3.3:9042
```

</div></div>

### Start up Kaa node service

After you set up all configuration properties, start up the Kaa node service.

```bash
$ sudo service kaa-node start
```

If you your configuration is valid, the following message will be displayed in the console.

```bash
$ sudo service kaa-node status
* Kaa Node daemon is running
```

Check logs for exceptions after the startup.

```bash
$ grep ERROR /var/log/kaa/*
```

---

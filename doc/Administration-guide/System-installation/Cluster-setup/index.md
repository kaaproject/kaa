---
layout: page
title: Cluster setup guide
permalink: /:path/
sort_idx: 30
---

* TOC
{:toc}

This page describes how to setup and configure Kaa cluster.

In general cluster setup is similar to [single node setup]({{root_url}}Administration-guide/System-installation/Single-node-installation/), except few details. 
We need at least 3 nodes to create a reliable cluster. 
For simplicity in this tutorial we assume that you had already set up your SQL and NoSQL databases clusters, so this tutorial doesn't cover such themes like setting up Cassandra, MongoDB or PostgreSQL cluster setup. 
Let's assume that we have 3 nodes with Ubuntu 14.04 installed on each of them.

```bash
node1 172.1.1.1
node2 172.2.2.2
node3 172.3.3.3
```
MariaDB come with out of the box master-master replication support thus we recommend to use this database in your Kaa cluster. 
PostgreSQL requires third party components to be installed in order to set up a cluster, so in scope of this guide we assume that you have only one instance of PostgreSQL database. 
So let's consider that if we decided to use MariaDB - we have three instances installed on each of nodes (172.1.1.1, 172.2.2.2, 172.3.3.3) and if PostgreSQL - we have only one instance that running on 172.1.1.1 node. 

On each of nodes we have installed NoSQl databases (MongoDB and Cassandra), Zookeeper service and Kaa node service and everything that left is to configure kaa-node services.

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
After Kaa installation on Ubuntu/Debian OS (deb packages), configuration files for Kaa node service will be extracted into the ```/etc/kaa-node/conf``` directory.
We are interested in 3 configuration files.

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

We assume that zookeeper with default port (2181) is running on every node. For every node configuration would look as follow:

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

Refer to [Single node installation guide - SQL database configuration]({{root_url}}Administration-guide/System-installation/Single-node-installation/#sql-database-configuration) section for more details.

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
    # specify jdbc database url
    jdbc_url=jdbc:postgresql://172.1.1.1:5432/kaa
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
    jdbc_url=jdbc:postgresql://172.1.1.1:5432/kaa
   ```
   
   <br>
   
   </div></div>

And also configure [username and password]({{root_url}}Administration-guide/System-installation/Single-node-installation#sql-database-configuration)

### NoSQL database configuration

Refer to [Single node installation guide - NoSQL database configuration]({{root_url}}Administration-guide/System-installation/Single-node-installation/#nosql-database-configuration) section for more details. 
Select NoSQL database ```mongo``` or ```cassandra```in ```/etc/kaa-node/conf/dao.properties``` file.

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

On every endpoin Kaa Administrator needs to configure [firewall rules]({{root_url}}Administration-guide/System-installation/Single-node-installation#firewall-rules-configuration).

After all configuration properties set up

```bash
 $ sudo service kaa-node start
```

If everithing is configured properly Kaa Administrator will see next output on command:

```bash
 $ sudo service kaa-node status
  * Kaa Node daemon is running
```

<!-- TODO: add verification step (or some cluster steps), maybe just grep for exceptions -->

## MariaDB cluster setup

We need at least 3 hosts running together with Ubuntu 14.04 Operating system to form a reliable cluster. 
The following is the hosts list that we had setup for this article, where we will deploy the MariaDB Galera cluster:

```bash
  ubuntu-node1 172.1.1.1 
  ubuntu-node2 172.2.2.2 
  ubuntu-node3 172.3.3.3
```

Now we will install the its required packages `rsync`, `galera` and `mariadb-galera-server` that need to be installed on all the three nodes.

### Add the MariaDB Repositories

The MariaDB and Galera packages are not available in the default Ubuntu repositories. 
However, the MariaDB project maintains its own repositories for Ubuntu, which contain all the packages that we need.

On each of the three servers that will be configured for this cluster, do the following:

1. Install the `python-software-properties` package.

   ```bash
     sudo apt-get update
     sudo apt-get install python-software-properties
   ```

2. Add the key files for the MariaDB repository.

   ```bash
     sudo apt-key adv --recv-keys --keyserver hkp://keyserver.ubuntu.com:80 0xcbcb082a1bb943db
   ```

3. Add the repository.

   ```bash
     sudo add-apt-repository 'deb http://mirror.jmu.edu/pub/mariadb/repo/5.5/ubuntu precise main'
   ```

### Install MariaDB with Galera Patches

You can now install the Galera patches through the apt interface.

```bash
  sudo apt-get update
  sudo apt-get install mariadb-galera-server galera
```

During the installation process you will be asked to configure the root password for the MariaDB, so make sure that **you configured the same root password on all the three nodes**.

![alt MariaDB root user password](attach/mariadb_galera_password.png)

Once the installations of these packages are done, you will get a MariaDB server on each one of your three nodes but they aren't yet configured.

If, for some reason, you do not already have `rsync` installed on your machines, you should install it now by typing:

```bash
  sudo apt-get install rsync
```

### Configuring MariaDB Cluster

#### MySQL Settings

First of all, open the `"/etc/mysql/my.cnf"` file and comment the following lines **on all the three nodes**:

```bash
  root@ubuntu-nodeX:~# nano /etc/mysql/my.cnf
  #bind-address           = 127.0.0.1
  #default_storage_engine = InnoDB
  #query_cache_limit              = 128K
  #query_cache_size               = 64M
```

Then, change the `max_allowed_packet` variable.

```bash
max_allowed_packet=20M
```

Add the following lines under `[mysqld]`.

```bash
  [mysqld]
  binlog_format=ROW
  default_storage_engine=innodb
  innodb_autoinc_lock_mode=2
  innodb_locks_unsafe_for_binlog=1
  innodb_doublewrite=1
  lower_case_table_names=1
```

#### VSRep Providers Configurations

Proceed to set the wsrep configurations on each node under the `[mysqld]`, using the specific hostname, root password and IP address of each node.

##### Configurations for `ubuntu-node1`

```bash
  [mysqld]
  wsrep_provider=/usr/lib/galera/libgalera_smm.so
  wsrep_provider_options="gcache.size=256M; gcache.page_size=128M"
  wsrep_cluster_address=gcomm://ubuntu-node1
  wsrep_cluster_name="MariaDB_Cluster"
  wsrep_node_address="ubuntu-node1"
  wsrep_node_name="ubuntu-node1"
  wsrep_sst_auth="root:'password for the MariaDB'"
  wsrep_node_incoming_address=172.1.1.1
  wsrep_sst_receive_address=172.1.1.1
  wsrep_slave_threads=16
```

##### Configurations For `ubuntu-node2`

```bash
  [mysqld]
  wsrep_provider=/usr/lib/galera/libgalera_smm.so
  wsrep_provider_options="gcache.size=256M; gcache.page_size=128M"
  wsrep_cluster_address=gcomm://ubuntu-node1
  wsrep_cluster_name="MariaDB_Cluster"
  wsrep_node_address="ubuntu-node2"
  wsrep_node_name="ubuntu-node2"
  wsrep_sst_auth="root:'password for the MariaDB'"
  wsrep_node_incoming_address=172.2.2.2
  wsrep_sst_receive_address=172.2.2.2
  wsrep_slave_threads=16
```

##### Configurations For `ubuntu-node3`

```bash
  [mysqld]
  wsrep_provider=/usr/lib/galera/libgalera_smm.so
  wsrep_provider_options="gcache.size=256M; gcache.page_size=128M"
  wsrep_cluster_address=gcomm://ubuntu-node1
  wsrep_cluster_name="MariaDB_Cluster"
  wsrep_node_address="ubuntu-node3"
  wsrep_node_name="ubuntu-node3"
  wsrep_sst_auth="root:'password for the MariaDB'"
  wsrep_node_incoming_address=172.3.3.3
  wsrep_sst_receive_address=172.3.3.3
  wsrep_slave_threads=16
```

To finish, save and close the file `"/etc/mysql/my.cnf"` on all the three nodes.

### Copying Debian Maintenance Configuration

Currently, Ubuntu and Debian's MariaDB servers use a special maintenance user to do routine maintenance. 
Some tasks that fall outside of the maintenance category are also executed by this user, including some important functions such as stopping MySQL.

In the case of a cluster environment shared between individual nodes, the maintenance user, which randomly generates login credentials on each node, is unable to execute its commands correctly. 
In such a case only the initial server will have the correct maintenance credentials, and the others will attempt to use their local settings to access the shared cluster environment.

To fix this, copy the contents of the maintenance file to each individual node as follows:

1. On one of your servers, open the Debian maintenance configuration file:

   ```bash
     sudo nano /etc/mysql/debian.cnf
   ```

   You will see a file that looks like this:

   ```bash
     [client]
     host     = localhost
     user     = debian-sys-maint
     password = 03P8rdlknkXr1upf
     socket   = /var/run/mysqld/mysqld.sock
     [mysql_upgrade]
     host     = localhost
     user     = debian-sys-maint
     password = 03P8rdlknkXr1upf
     socket   = /var/run/mysqld/mysqld.sock
     basedir  = /usr
   ```

   You will simply need to copy this information and paste it into the same file on each node.

2. On your second and third nodes, open the same file:

   ```bash
     sudo nano /etc/mysql/debian.cnf
   ```

3. Delete the current information and paste the parameters from the first node's configuration file into these other servers' files:

   ```bash
     [client]
     host     = localhost
     user     = debian-sys-maint
     password = 03P8rdlknkXr1upf
     socket   = /var/run/mysqld/mysqld.sock
     [mysql_upgrade]
     host     = localhost
     user     = debian-sys-maint
     password = 03P8rdlknkXr1upf
     socket   = /var/run/mysqld/mysqld.sock
     basedir  = /usr
   ```

**They should be exactly the same now.** Save and close the files.

### Start MariaDB cluster

To start the MariaDB cluster, do the following:

1. Stop the running MariaDB service by typing the following line on each of the nodes.

   ```bash
     sudo service mysql stop
   ```

2. Start up your first node with a special parameter.

   ```bash
     sudo service mysql start --wsrep-new-cluster
   ```

   In the cluster configuration, each node that comes online tries to connect to at least one other node specified in its configuration file to get its initial state. 
   Without the `--wsrep-new-cluster` parameter, this command would fail because the first node is unable to connect to any other nodes.

3. On each of the other nodes, start MariaDB as you normally would.

   ```bash
     sudo service mysql start
   ```

Your cluster should now be online and communicating.

You can also confirm the status of your running cluster and its replication by running the following command on each of your node. 
The cluster size will be also displayed in the output of this command.

```bash
  root@ubuntu-node1:~# mysql -u root -pmariadb_admin_password -e
  'SELECT VARIABLE_VALUE as "cluster size" FROM INFORMATION_SCHEMA.GLOBAL_STATUS WHERE VARIABLE_NAME="wsrep_cluster_size"'
  root@ubuntu-node2:~# mysql -u root -pmariadb_admin_password -e
  'SELECT VARIABLE_VALUE as "cluster size" FROM INFORMATION_SCHEMA.GLOBAL_STATUS WHERE VARIABLE_NAME="wsrep_cluster_size"'
  root@ubuntu-node3:~# mysql -u root -pmariadb_admin_password -e
  'SELECT VARIABLE_VALUE as "cluster size" FROM INFORMATION_SCHEMA.GLOBAL_STATUS WHERE VARIABLE_NAME="wsrep_cluster_size"'
```

### Create new user

If you want to create a new user, do as follows:

> Don't use "ALL PRIVILEGES" when you create a user for your database.
>
> For more information about "privileges" provided by MySQL refer to http://dev.mysql.com/doc/refman/5.7/en/privileges-provided.html

```bash
  root@ubuntu-any_node:~# mysql -u root -p
  Enter password:
 
  MariaDB>CREATE USER 'user_name'@'(host or '%')' IDENTIFIED BY 'some_password';
  MariaDB>GRANT ALL PRIVILEGES ON *.* TO 'user_name'@'(host or '%')' WITH GRANT OPTION;
  MariaDB>FLUSH PRIVILEGES;
```
---

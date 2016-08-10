---
layout: page
title: MariaDB cluster setup guide
permalink: /:path/
sort_idx: 30
---

{% include variables.md %}

* TOC
{:toc}

> **Verified against host OS:**
> 
> * Ubuntu 14.04 LTS Desktop 64 bit

## MariaDB cluster setup

We need at least 3 hosts running together with Ubuntu 14.04 Operating system to form a reliable cluster. 
The following is the hosts list that we had setup for this article, where we will deploy the MariaDB Galera cluster:

```bash
  ubuntu-node1 172.1.1.1 
  ubuntu-node2 172.2.2.2 
  ubuntu-node3 172.3.3.3
```

Now we will install the required packages `rsync`, `galera` and `mariadb-galera-server` that need to be installed on all the three nodes.

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

First of all, open the `"/etc/mysql/my.cnf"` file and **comment** the following lines **on all the three nodes**:

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

#### WSRep providers configurations

Proceed to set the `wsrep` configurations on each node under the `[mysqld]`, using the specific hostname, root password and IP address of each node.

##### Configurations for `ubuntu-node1`

```bash
  [mysqld]
  wsrep_provider=/usr/lib/galera/libgalera_smm.so
  wsrep_provider_options="gcache.size=256M; gcache.page_size=128M"
  wsrep_cluster_address=gcomm://172.1.1.1,172.2.2.2,172.3.3.3
  wsrep_cluster_name="MariaDB_Cluster"
  wsrep_node_address="ubuntu-node1"
  wsrep_node_name="ubuntu-node1"
  wsrep_sst_auth="root:'your password'"
  wsrep_node_incoming_address=172.1.1.1
  wsrep_sst_receive_address=172.1.1.1
  wsrep_slave_threads=16
```

##### Configurations for `ubuntu-node2`

```bash
  [mysqld]
  wsrep_provider=/usr/lib/galera/libgalera_smm.so
  wsrep_provider_options="gcache.size=256M; gcache.page_size=128M"
  wsrep_cluster_address=gcomm://172.1.1.1,172.2.2.2,172.3.3.3
  wsrep_cluster_name="MariaDB_Cluster"
  wsrep_node_address="ubuntu-node2"
  wsrep_node_name="ubuntu-node2"
  wsrep_sst_auth="root:'your password'"
  wsrep_node_incoming_address=172.2.2.2
  wsrep_sst_receive_address=172.2.2.2
  wsrep_slave_threads=16
```

##### Configurations for `ubuntu-node3`

```bash
  [mysqld]
  wsrep_provider=/usr/lib/galera/libgalera_smm.so
  wsrep_provider_options="gcache.size=256M; gcache.page_size=128M"
  wsrep_cluster_address=gcomm://172.1.1.1,172.2.2.2,172.3.3.3
  wsrep_cluster_name="MariaDB_Cluster"
  wsrep_node_address="ubuntu-node3"
  wsrep_node_name="ubuntu-node3"
  wsrep_sst_auth="root:'your password'"
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

> Please check ports 4444 and 4567. This ports must be free and open for connections from hosts with other nodes - this is important for ```wsrep``` communication. 

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
  root@ubuntu-node1:~# mysql -u root -pmariadb_admin_password -e 'SHOW STATUS LIKE "wsrep_cluster_size"'
  root@ubuntu-node2:~# mysql -u root -pmariadb_admin_password -e 'SHOW STATUS LIKE "wsrep_cluster_size"'
  root@ubuntu-node3:~# mysql -u root -pmariadb_admin_password -e 'SHOW STATUS LIKE "wsrep_cluster_size"'
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

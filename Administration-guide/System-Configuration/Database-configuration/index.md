---
layout: page
title: Database configuration
permalink: /:path/
sort_idx: 220
---

{% include variables.md %}

* TOC
{:toc}

After [Kaa installation]({{root_url}}Administration-guide/System-installation/), configuration files for each Kaa 
component will be extracted into the
/etc/kaa-node/conf directory.

admin-dao.properties, sql-dao.properties and nosql-dao.properties files are responsible for database configuration.

>**NOTE:**
> After changing properties in any file from this folder, you must restart the node for changes to take effect, 
by executing following command:
>
```bash
$ sudo service kaa-node restart
```

# SQL database configuration

> **Note:** Kaa requires one of possible options:
> * [MariaDB 5.5](https://mariadb.com/)
> * [PostgreSQL 9.4](http://www.postgresql.org/download/)
> MariaDB is the default choice.

For more information about installing MariaDB and PostgreSQL databases use
[Third party components installation]({{root_url}}Administration-guide/System-installation/Single-node-installation/#third-party-components-installation)

You can find SQL database configuration property file templates in /etc/kaa-node/conf/ folder: maria-dao.properties.template, 
mariadb-dao.properties.template files for MariaDB database and postgre-dao.properties.template, 
postgresql-dao.properties.template files for PostgreSQL.

admin-dao.properties and sql-dao.properties consist of the following parameters:

**Database name**
db_name=kaa

**Specific configurations for DAO layer
Max wait time in seconds for history dao class. Custom property for Kaa History Service.**
dao_max_wait_tim
Default: _5_

**Specify hibernate sql dialect**
hibernate_dialect

**Specify if hibernate will format sql request**
hibernate_format_sql
Default: _false_

**Specify if show hibernate sql request**
hibernate_show_sql
Default: _false_

**Specify hibernate hbm2ddl strategy**
hibernate_hbm2ddl_auto
Default: _update_

**Specify jdbc driver class**
jdbc_driver_className

**Specify jdbc database user name**
jdbc_username

**Specify jdbc mariaDB database password root**
jdbc_password

**Specify jdbc database hosts and ports**
jdbc_host_port

**Specify jdbc database provider name**
sql_provider_name

## MariaDB configuration

Check that the MariaDB login and password is up to date in the server configuration files.

```bash
$ cat /etc/kaa-node/conf/admin-dao.properties | grep jdbc_username
jdbc_username=sqladmin

$ cat /etc/kaa-node/conf/admin-dao.properties | grep jdbc_password
jdbc_password=admin

$ cat /etc/kaa-node/conf/sql-dao.properties | grep jdbc_username
jdbc_username=sqladmin

$ cat /etc/kaa-node/conf/sql-dao.properties | grep jdbc_password
jdbc_password=admin
```
In case of the password or username mismatch, edit the configuration file to set a new password.

```bash
$ sudo nano /etc/kaa-node/conf/admin-dao.properties

$ sudo nano /etc/kaa-node/conf/sql-dao.properties
```

If you wish to switch from PostgreSQL to MariaDB you should copy content 
of MariaDB config files to Kaa DB config files:

```bash
$ sudo bash -c "cat /etc/kaa-node/conf/maria-dao.properties.template > /etc/kaa-node/conf/sql-dao.properties"
$ sudo bash -c "cat /etc/kaa-node/conf/mariadb-dao.properties.template > /etc/kaa-node/conf/admin-dao.properties"
```

## PostgreSQL configuration

Check that the PostgreSQL password is up to date in the server configuration files.

```bash
$ cat /etc/kaa-node/conf/admin-dao.properties | grep jdbc_password
jdbc_password=admin

$ cat /etc/kaa-node/conf/sql-dao.properties | grep jdbc_password
jdbc_password=admin
```
In case of the password or username mismatch, edit the configuration file to set a new password.

```bash
$ sudo nano /etc/kaa-node/conf/admin-dao.properties

$ sudo nano /etc/kaa-node/conf/sql-dao.properties
```

If you wish to switch from MariaDB to PostgreSQL you should copy content 
of PostgreSQL config files to Kaa DB config files:

```bash
$ sudo bash -c "cat /etc/kaa-node/conf/postgre-dao.properties.template > /etc/kaa-node/conf/sql-dao.properties"
$ sudo bash -c "cat /etc/kaa-node/conf/postgresql-dao.properties.template > /etc/kaa-node/conf/admin-dao.properties"
```

# NoSQL database configuration

> **Note:** Kaa requires one of possible options: 
> * [MongoDB 2.6.9](http://www.mongodb.org/downloads)
> * [Cassandra 3.5](http://cassandra.apache.org/download/)
> MongoDB is the default choice.

For more information about installing MongoDB and Cassandra databases use
[Third party components installation]({{root_url}}Administration-guide/System-installation/Single-node-installation/#third-party-components-installation)

You can find NoSQL database configuration property file nosql-dao.properties in /etc/kaa-node/conf/.

The nosql-dao.properties consist of the following parameters:

**NoSQL database provider name.**
**Possible options: mongodb, cassandra**
nosql_db_provider_name
Default: _mongodb_

In case you are going to use Cassandra, execute the following commands.

```bash
$ sudo cqlsh -f /etc/kaa-node/conf/cassandra.cql
$ sudo nano /etc/kaa-node/conf/nosql-dao.properties
nosql_db_provider_name=cassandra
```

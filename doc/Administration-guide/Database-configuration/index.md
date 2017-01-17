---
layout: page
title: Database configuration
permalink: /:path/
sort_idx: 15
---

{% include variables.md %}

* TOC
{:toc}

When you complete [Kaa installation]({{root_url}}Administration-guide/System-installation/), the configuration files for each Kaa component will be extracted into the `/etc/kaa-node/conf/` directory.

Use the following files to configure your database:

* `admin-dao.properties`
* `sql-dao.properties`
* `nosql-dao.properties`
	
After you changed the properties in the configuration files, restart the node for the changes to take effect.

```bash
$ sudo service kaa-node restart
```

## SQL database configuration

You can use one of the following databases:

* [MariaDB 5.5](https://mariadb.com/)
* [PostgreSQL 9.4](http://www.postgresql.org/download/)

By default, MariaDB database is used.

For database installation instructions, see [Third party components]({{root_url}}Administration-guide/System-installation/Single-node-installation/#third-party-components).

The following templates of the configuration property file for SQL database are located in the `/etc/kaa-node/conf/` directory:

* `maria-dao.properties.template`, `mariadb-dao.properties.template` -- for MariaDB.
* `postgre-dao.properties.template`, `postgresql-dao.properties.template` -- for PostgreSQL.

See the table below for configuration parameters contained in the `admin-dao.properties` and `sql-dao.properties` files.

|Parameter name|Default value|Description|
|--------------|-------------|-----------|
|`db_name=kaa`||Database name.|
|`dao_max_wait_time`|5|Maximum wait time in seconds for DAO history class. Custom property for Kaa **History service**.|
|`hibernate_dialect`||Specifies hibernate sql dialect.|
|`hibernate_format_sql`|false|Specifies if hibernate will format sql request.|
|`hibernate_show_sql`|false|Shows hibernate sql request.|
|`hibernate_hbm2ddl_auto`|update|Specifies hibernate `hbm2ddl` strategy.|
|`jdbc_driver_className`||Specifies the `jdbc` driver class.|
|`jdbc_username`||Specifies `jdbc` database user name.|
|`jdbc_password`||Specifies `jdbc` mariaDB database password root.|
|`jdbc_host_port`||Specifies `jdbc` database hosts and ports.|
|`sql_provider_name`||Specifies `jdbc` database provider name.|


<ul class="nav nav-tabs">
     <li class="active"><a data-toggle="tab" href="#MariaDB">MariaDB</a></li>
     <li><a data-toggle="tab" href="#PostgreSQL">PostgreSQL</a></li>
</ul>

<div class="tab-content"><div id="MariaDB" class="tab-pane fade in active" markdown="1">

Check that the MariaDB username and password is up to date in the server configuration files.

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

In case of the password or username mismatch, edit the configuration file.

```bash
$ sudo nano /etc/kaa-node/conf/admin-dao.properties

$ sudo nano /etc/kaa-node/conf/sql-dao.properties
```

To switch from PostgreSQL to MariaDB, copy the contents of MariaDB configuration files to the Kaa database configuration files.

```bash
$ sudo bash -c "cat /etc/kaa-node/conf/maria-dao.properties.template > /etc/kaa-node/conf/sql-dao.properties"
$ sudo bash -c "cat /etc/kaa-node/conf/mariadb-dao.properties.template > /etc/kaa-node/conf/admin-dao.properties"
```

</div><div id="PostgreSQL" class="tab-pane fade" markdown="1">

Check that the PostgreSQL password is up to date in the server configuration files.

```bash
$ cat /etc/kaa-node/conf/admin-dao.properties | grep jdbc_password
jdbc_password=admin

$ cat /etc/kaa-node/conf/sql-dao.properties | grep jdbc_password
jdbc_password=admin
```

In case of the password or username mismatch, edit the configuration file.

```bash
$ sudo nano /etc/kaa-node/conf/admin-dao.properties

$ sudo nano /etc/kaa-node/conf/sql-dao.properties
```

To switch from MariaDB to PostgreSQL, copy the contents of PostgreSQL configuration files to the Kaa database configuration files.

```bash
$ sudo bash -c "cat /etc/kaa-node/conf/postgre-dao.properties.template > /etc/kaa-node/conf/sql-dao.properties"
$ sudo bash -c "cat /etc/kaa-node/conf/postgresql-dao.properties.template > /etc/kaa-node/conf/admin-dao.properties"
```

</div></div>

## NoSQL database configuration

You can use one of the following databases:

* [MongoDB 2.6.9](http://www.mongodb.org/downloads)
* [Cassandra 3.5](http://cassandra.apache.org/download/)

By default, MongoDB database is used.

For database installation instructions, see [Third party components]({{root_url}}Administration-guide/System-installation/Single-node-installation/#third-party-components).

The `nosql-dao.properties` template of the configuration property file for SQL database is located in the `/etc/kaa-node/conf/` directory.

The `nosql-dao.properties` file contains the `nosql_db_provider_name` parameter that can be set to **mongodb** (default) or **cassandra**.

If you use Cassandra, run the following commands.

```bash
$ sudo cqlsh -f /etc/kaa-node/conf/cassandra.cql
$ sudo nano /etc/kaa-node/conf/nosql-dao.properties
nosql_db_provider_name=cassandra
```

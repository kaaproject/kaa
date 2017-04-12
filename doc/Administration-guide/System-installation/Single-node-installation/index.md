---
layout: page
title: Single node installation
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

* TOC:
{:toc}

This guide describes how to install and configure your Kaa components on a single Linux node.

If this is the first time you use Kaa or you are not sure if you can manually install the node, use [Kaa Sandbox]({{root_url}}Getting-started/#kaa-sandbox).
Kaa Sandbox emulates a single-node Kaa installation which comes already pre-configured so you can start developing your applications right away.


## Prerequisites

Kaa supports the following operating system families and provides installation packages for each of them:

* Ubuntu and Debian systems
* Red Hat/CentOS/Oracle systems

>**NOTE:** This guide is verified against:
>
> * Ubuntu 14.04 LTS Desktop 64-bit
> * Ubuntu 16.04 LTS Desktop 64-bit
> * CentOS 6.7 64-bit
> * CentOS 7.2 64-bit
{:.note}

To use Kaa, your system must meet the following minimum requirements:

* 64-bit OS
* 4 Gb RAM

Kaa requires the following third-party components to be installed and configured:

* [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [PostgreSQL 9.4](http://www.postgresql.org/download/)
* [MariaDB 5.5](https://mariadb.com/)
* [Zookeeper 3.4.5 or later](http://zookeeper.apache.org/doc/r3.4.5/)

Kaa has been tested on the latest production release of MariaDB and PostgreSQL.

Kaa also requires [MongoDB 2.6.9](http://www.mongodb.org/downloads) or [Cassandra 3.5](http://cassandra.apache.org/download/) as a NoSQL database.

Installation of the third-party components is described in the next section.

## Installation

### Third-party components

Follow the instructions below to install the required third-party components.

<ul class="nav nav-tabs">
	<li class="active"><a data-toggle="tab" href="#Ubuntu">Ubuntu 14.04/16.04</a></li>
	<li><a data-toggle="tab" href="#CentOS">CentOS 6.7/7.2</a></li>
</ul>

<div class="tab-content"><!---T---><div id="Ubuntu" class="tab-pane fade in active" markdown="1" ><!---U--->

1. Download and install common utilities: wget, ca-certificates, curl.

   ```bash
   $ sudo apt-get install wget ca-certificates curl
   ```

2. Install [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
This java repository is not official.
For alternative Oracle JDK installation process, see the [official page](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

   ```bash
   $ sudo add-apt-repository ppa:webupd8team/java
   $ sudo apt-get update
   $ sudo apt-get install oracle-java8-installer
   ```

   To check if you have Oracle JDK installed, run the following command.

   ```bash
   $ javac -version
   ```

   JDK version will be displayed upon successful installation.

   ```bash
   javac 1.8.0_91
   ```

3. Install SQL DB.
Kaa requires MariaDB (used by default) or PostgreSQL.

<ul>
	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#maria">MariaDB</a></li>
		<li><a data-toggle="tab" href="#postgre">PostgreSQL</a></li>
	</ul>
</ul>

<ul>
	<div class="tab-content"><!---tMarPos---><div id="maria" class="tab-pane fade in active" markdown="1" ><!---Mar--->

Install the python-software-properties package.

```bash
$ sudo apt-get install python-software-properties
```

Add key files for MariaDB repository.

```bash
$ sudo apt-key adv --recv-keys --keyserver hkp://keyserver.ubuntu.com:80 0xcbcb082a1bb943db
```

Add repository.

```bash
$ sudo add-apt-repository 'deb http://mirror.jmu.edu/pub/mariadb/repo/5.5/ubuntu trusty main'
```

Install [MariaDB 5.5](https://mariadb.com/) with Galera Patches.

```bash
$ sudo apt-get update
$ sudo apt-get install mariadb-galera-server-5.5 mariadb-client-5.5
```

If you don't have rsync installed on your machines, run the following command to install it.

```bash
$ sudo apt-get install rsync
```

Check if the MariaDB service is running.

```bash
$ sudo netstat -ntlp | grep 3306
```

Connect to the mysql-server.

```bash
$ mysql -u root -p
```

Specify username and password.
By default, **sqladmin** username and **admin** password are used.

```sql
CREATE USER 'sqladmin'@'localhost' IDENTIFIED BY 'admin'; GRANT ALL PRIVILEGES ON *.* TO 'sqladmin'@'localhost' WITH GRANT OPTION; FLUSH PRIVILEGES;
```

Create Kaa database.

```sql
CREATE DATABASE kaa
   CHARACTER SET utf8
   COLLATE utf8_general_ci;
```

</div><!---Mar---><div id="postgre" class="tab-pane fade" markdown="1" ><!---Pos--->

Add an official PostgreSQL repository.

```bash
$ sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt/ $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
$ wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
```
Install [PostgreSQL 9.4](http://www.postgresql.org/download/).

```bash
$ sudo apt-get update
$ sudo apt-get install postgresql-9.4
```
Check if the Postgresql service is running.

```bash
$ sudo netstat -ntlp | grep 5432
```
For more information, see the [official page](https://wiki.postgresql.org/wiki/Apt).

Connect to the postgresql-server using the the psql utility.

```bash
$ sudo -u postgres psql
```

Specify the password for Postgres user.
Default password is **admin**.

```bash
postgres=# \password Enter new password: admin Enter it again: admin
```

Create Kaa database.

```sql
CREATE DATABASE "kaa"
   WITH OWNER "postgres"
   ENCODING 'UTF8'
   LC_COLLATE = 'en_US.UTF-8'
   LC_CTYPE = 'en_US.UTF-8'
   TEMPLATE template0;
```

</div><!---Pos--->
</div><!---tMarPos--->
</ul>

<ol>
<li value="4" markdown="1">
Install [Zookeeper 3.4.8](http://zookeeper.apache.org/doc/r3.4.8/).

```bash
$ sudo apt-get install zookeeperd
```

Start Zookeeper.

```bash
$ sudo /usr/share/zookeeper/bin/zkServer.sh start
```

Check if Zookeeper service is running.

```bash
$ netstat -ntlp | grep 2181
```
</li>
</ol>

<ol>
<li value="5" markdown="1">
Install a NoSQL DB.
Kaa requires MongoDB (used by default) or Cassandra.
</li>
</ol>

<ul>
	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#mongo">MongoDB</a></li>
		<li><a data-toggle="tab" href="#cassandra">Cassandra</a></li>
	</ul>
</ul>

<ul>
	<div class="tab-content"><!---tMonCas---><div id="mongo" class="tab-pane fade in active" markdown="1" ><!---Mon--->

If you have an older version installed, uninstall it.

```bash
$ sudo dpkg -l | grep mongo
$ sudo apt-get remove mongodb* --purge
```

Follow the instructions for your OS.

<ul>
	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#mongo_ubuntu14">Ubuntu 14.04</a></li>
		<li><a data-toggle="tab" href="#mongo_ubuntu16">Ubuntu 16.04</a></li>
	</ul>
</ul>

<ul>
	<div class="tab-content"><!---tMon46---><div id="mongo_ubuntu14" class="tab-pane fade in active" markdown="1" ><!---Mon4--->

Add a MongoDB repository to the `/etc/apt/sources.list.d/mongodb.list` file.

```bash
$ sudo apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10
$ echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | sudo tee /etc/apt/sources.list.d/mongodb.list
```

Install [MongoDB 2.6](http://www.mongodb.org/downloads).

```bash
$ sudo apt-get update
$ sudo apt-get install -y mongodb-org=2.6.9 mongodb-org-server=2.6.9 mongodb-org-shell=2.6.9 mongodb-org-mongos=2.6.9 mongodb-org-tools=2.6.9
```

Start MongoDB.

```bash
$ sudo service mongod start
```

Verify that MongoDB started successfully.

```bash
$ sudo service mongod status
$ cat /var/log/mongodb/mongod.log | grep "waiting for connections on port"
2015-09-23T16:39:35.455+0300 [initandlisten] waiting for connections on port 27017
```

</div><!---Mon4---><div id="mongo_ubuntu16" class="tab-pane fade" markdown="1" ><!---Mon6--->

Add a MongoDB repository to the `/etc/apt/sources.list.d/mongodb.list` file.

```bash
$ sudo apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10
$ echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | sudo tee /etc/apt/sources.list.d/mongodb.list
```

Install [MongoDB 2.6](http://www.mongodb.org/downloads).

```bash
$ sudo apt-get update
$ sudo apt-get install -y mongodb-org=2.6.9 mongodb-org-server=2.6.9 mongodb-org-shell=2.6.9 mongodb-org-mongos=2.6.9 mongodb-org-tools=2.6.9
```

Before starting MongoDB, edit the following file.

```bash
$ sudo nano /etc/systemd/system/mongodb.service


[Unit]
Description=High-performance, schema-free document-oriented database
After=network.target

[Service]
User=mongodb
ExecStart=/usr/bin/mongod --quiet --config /etc/mongod.conf

[Install]
WantedBy=multi-user.target
```

Start MongoDB.

```bash
$ sudo systemctl start mongodb
```

Verify that MongoDB started successfully.

```bash
$ sudo systemctl status mongodb
$ cat /var/log/mongodb/mongod.log | grep "waiting for connections on port"
2015-09-23T16:39:35.455+0300 [initandlisten] waiting for connections on port 27017
```

</div><!---Mon6---></div><!---tMon46--->
</ul>

</div><!---Mon---><div id="cassandra" class="tab-pane fade" markdown="1" ><!---Cas--->

Follow the instructions for your OS.

<ul>
	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#cassandra_ubuntu14">Ubuntu 14.04</a></li>
		<li><a data-toggle="tab" href="#cassandra_ubuntu16">Ubuntu 16.04</a></li>
	</ul>
</ul>

<ul>
	<div class="tab-content"><!---tCas46---><div id="cassandra_ubuntu14" class="tab-pane fade in active" markdown="1" ><!---Cas4--->

Add the DataStax Community repository to the `/etc/apt/sources.list.d/cassandra.sources.list` file.

```bash
$ echo "deb http://www.apache.org/dist/cassandra/debian 35x main" | sudo tee -a /etc/apt/sources.list.d/cassandra.sources.list
$ curl -L http://debian.datastax.com/debian/repo_key | sudo apt-key add -
```

Install [Cassandra 3.5](http://cassandra.apache.org/download/).

```bash
$ sudo apt-get update
$ sudo apt-get install cassandra=3.5
```

</div><!---Cas4---><div id="cassandra_ubuntu16" class="tab-pane fade" markdown="1" ><!---Cas6--->

Cassandra requires `python-support`.
Since this package was removed in Ubuntu 16.04, you need to install it manually.
Download the `deb` package and unpack it.

```bash
$ sudo wget "http://launchpadlibrarian.net/109052632/python-support_1.0.15_all.deb"
$ sudo dpkg --install python-support_1.0.15_all.deb
```

Set up Apache repository.
Change the 35x to match the latest version.
For example, use 36x if Cassandra 3.6 is the latest version.

```bash
$ echo "deb http://www.apache.org/dist/cassandra/debian 35x main" | sudo tee -a /etc/apt/sources.list.d/cassandra.sources.list
$ curl -L http://debian.datastax.com/debian/repo_key | apt-key add -
```

Install [Cassandra 3.5](http://cassandra.apache.org/download/).

```bash
$ sudo apt-get update
$ sudo apt-get install -y --allow-unauthenticated cassandra=3.5
```

Check if Cassandra service is running.

```bash
$ sudo netstat -ntlp | grep 9042
```

Install Java Native Access (JNA).

```bash
$ sudo apt-get install libjna-java
```

Check Cassandra cql shell.

```bash
$ cqlsh
Connected to Test Cluster at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 3.5 | CQL spec 3.4.0 | Native protocol v4]
Use HELP for help.
cqlsh>
```
</div><!---Cas6---></div><!---tCas46--->
</ul>
</div><!---Cas---></div><!---tMonCas--->
</ul>
</div><!---U---><div id="CentOS" class="tab-pane fade" markdown="1" ><!---C--->

1. Download and install common utilities: wget, nc, gzip.

   ```bash
   $ sudo yum install wget nc gzip
   ```

2. Install [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
Download and install JDK rpm.

   ```bash
   $ cd ~
   $ wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u60-b27/jdk-8u60-linux-x64.rpm"
   $ sudo yum localinstall jdk-8u60-linux-x64.rpm
   ```

   Update Java alternatives for the new JDK.

   ```bash
   $ cd /usr/java/jdk1.8.0_60/
   $ sudo alternatives --install /usr/bin/java java /usr/java/jdk1.8.0_60/bin/java 2
   $ sudo alternatives --config java
   There are 2 programs which provide 'java'.

     Selection    Command
   -----------------------------------------------
   *  1           /usr/java/jdk1.8.0_60/jre/bin/java
    + 2           /usr/java/jdk1.8.0_60/bin/java
   ```

   Check Java version.

   ```bash
   $ java -version

   java version "1.8.0_60"
   Java(TM) SE Runtime Environment (build 1.8.0_60-b27)
   Java HotSpot(TM) 64-Bit Server VM (build 25.60-b23, mixed mode)
   ```

3. Install SQL DB.
Kaa requires MariaDB (used by default) or PostgreSQL.

<ul>
	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#maria_centos">MariaDB</a></li>
		<li><a data-toggle="tab" href="#postgre_centos">PostgreSQL</a></li>
	</ul>
</ul>

<ul>
	<div class="tab-content"><!---tMarPos---><div id="maria_centos" class="tab-pane fade in active" markdown="1" ><!---Mar--->

Add MariaDB YUM repository entry for CentOS.

```bash

sudo nano /etc/yum.repos.d/MariaDB.repo
```

Copy the contents below to the `/etc/yum.repos.d/MariaDB.repo` file.

<ul>
	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#maria_centos67">CentOS 6.7</a></li>
		<li><a data-toggle="tab" href="#maria_centos72">CentOS 7.2</a></li>
	</ul>
</ul>

<ul>
	<div class="tab-content"><!---tCent67---><div id="maria_centos67" class="tab-pane fade in active" markdown="1" ><!---Cent6--->

```bash

# MariaDB 5.5 CentOS repository list
# http://mariadb.org/mariadb/repositories/
[mariadb]
name = MariaDB
baseurl = http://yum.mariadb.org/5.5/centos6-amd64
gpgkey=https://yum.mariadb.org/RPM-GPG-KEY-MariaDB
gpgcheck=1
```

</div><!---Cent6---><div id="maria_centos72" class="tab-pane fade" markdown="1" ><!---Cent7--->

```bash

# MariaDB 5.5 CentOS repository list
# http://mariadb.org/mariadb/repositories/
[mariadb]
name = MariaDB
baseurl = https://downloads.mariadb.com/MariaDB/mariadb-5.5.53/yum/centos7-amd64/
gpgkey=https://yum.mariadb.org/RPM-GPG-KEY-MariaDB
gpgcheck=1
```

</div><!---Cent7---></div><!---tCent67--->
</ul>

Then, run the following commands.

```bash

$ sudo yum -y update
$ sudo service mysqld stop
$ sudo yum -y remove mysql-server mysql

```

Install [MariaDB 5.5](https://mariadb.com/).

```bash
$ sudo yum install MariaDB-server MariaDB-client
$ sudo service mysql start
```

Check if MariaDB service is running.

```bash
$ sudo netstat -ntlp | grep 3306

tcp        0      0 0.0.0.0:3306            0.0.0.0:*               LISTEN      5476/mysqld
```

For more information, see the [official page](https://mariadb.org/).

Connect to the mariadb-server.

```bash
$ mysql -u root -p
```

Create a new user for the database.

```bash
CREATE USER 'sqladmin'@'localhost' IDENTIFIED BY 'admin'; GRANT ALL PRIVILEGES ON *.* TO 'sqladmin'@'localhost' WITH GRANT OPTION; FLUSH PRIVILEGES;
```

Create a Kaa database.

```bash
CREATE DATABASE kaa
    CHARACTER SET utf8
    COLLATE utf8_general_ci;
```

</div><!---Mar---><div id="postgre_centos" class="tab-pane fade" markdown="1" ><!---Pos--->

Follow the instructions for your OS.

<ul>
	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#postgre_centos67">CentOS 6.7</a></li>
		<li><a data-toggle="tab" href="#postgre_centos72">CentOS 7.2</a></li>
	</ul>
</ul>

<ul>
	<div class="tab-content"><!---tCent67---><div id="postgre_centos67" class="tab-pane fade in active" markdown="1" ><!---Cent6--->

Exclude old PostgreSQL from the default repository, append a line `exclude=postgresql*` to the sections **[base]** and **[updates]**.

```bash
$ sudo nano /etc/yum.repos.d/CentOS-Base.repo

...

[base]
name=CentOS-$releasever - Base
mirrorlist=http://mirrorlist.centos.org/?release=$releasever&arch=$basearch&repo=os
#baseurl=http://mirror.centos.org/centos/$releasever/os/$basearch/
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-6
exclude=postgresql*

...

[updates]
name=CentOS-$releasever - Updates
mirrorlist=http://mirrorlist.centos.org/?release=$releasever&arch=$basearch&repo=updates
#baseurl=http://mirror.centos.org/centos/$releasever/updates/$basearch/
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-6
exclude=postgresql*

...
```

Install [PostgreSQL 9.4](http://www.postgresql.org/download/) PGDG file.

```bash
$ sudo yum localinstall https://yum.postgresql.org/9.4/redhat/rhel-6-x86_64/pgdg-centos94-9.4-3.noarch.rpm
```

List available PostgreSQL installations and install the PostgreSQL server.

```bash
$ sudo yum list postgres*
$ sudo yum install postgresql94-server
```

Initialize the PostgreSQL database.

```bash
$ sudo service postgresql-9.4 initdb
Initializing database:                [  OK  ]
```

Configure the database to start automatically when OS starts.

```bash
$ sudo chkconfig postgresql-9.4 on
```

Start the database.

```bash
$ sudo service postgresql-9.4 start
Starting postgresql-9.4 service:            [  OK  ]
```

Connect to the postgresql-server using the psql utility.

```bash
$ sudo -u postgres psql
```

Specify the password for the Postgres user (the default password in the Kaa configuration files is **admin**).

```bash
postgres=# \password
Enter new password: admin
Enter it again: admin
```

Create a Kaa database.

```bash
CREATE DATABASE "kaa"
	  WITH OWNER "postgres"
	  ENCODING 'UTF8'
	  LC_COLLATE = 'en_US.UTF-8'
	  LC_CTYPE = 'en_US.UTF-8'
	  TEMPLATE template0;
```

Update the `pg\_hba.conf` file to allow local connections.

```bash
$ sudo nano /var/lib/pgsql/9.4/data/pg_hba.conf

remove lines:
local   all             all                                     peer
host    all             all             127.0.0.1/32            ident

add lines:
local   all             all                                     trust
host    all             all             127.0.0.1/32            trust
```

Restart the database.

```bash
$ sudo service postgresql-9.4 restart

Stopping postgresql-9.4 service:            [  OK  ]
Starting postgresql-9.4 service:            [  OK  ]
```

</div><!---Cent6---><div id="postgre_centos72" class="tab-pane fade" markdown="1" ><!---Cent7--->

Exclude old PostgreSQL from the default repository, append a line `exclude=postgresql*` to the sections **[base]** and **[updates]**.

```bash
$ sudo nano /etc/yum.repos.d/CentOS-Base.repo

...

[base]
name=CentOS-$releasever - Base
mirrorlist=http://mirrorlist.centos.org/?release=$releasever&arch=$basearch&repo=os&infra=$infra
#baseurl=http://mirror.centos.org/centos/$releasever/os/$basearch/
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-7
exclude=postgresql*

#released updates
[updates]
name=CentOS-$releasever - Updates
mirrorlist=http://mirrorlist.centos.org/?release=$releasever&arch=$basearch&repo=updates&infra=$infra
#baseurl=http://mirror.centos.org/centos/$releasever/updates/$basearch/
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-CentOS-7
exclude=postgresql*

...
```

Install [PostgreSQL 9.4](http://www.postgresql.org/download/) PGDG file.

```bash
$ sudo yum localinstall https://yum.postgresql.org/9.4/redhat/rhel-7.2-x86_64/pgdg-centos94-9.4-3.noarch.rpm
```

List available PostgreSQL installations and install the PostgreSQL server.

```bash
$ sudo yum list postgres*
$ sudo yum install postgresql94-server
```

Initialize the PostgreSQL database.

```bash
$ sudo /usr/pgsql-9.4/bin/postgresql94-setup initdb
Initializing database ... OK
```

Configure the database to start automatically when OS starts.

```bash
$ systemctl enable postgresql-9.4
```

Start the database.

```bash
$ systemctl start postgresql-9.4
```

Connect to the postgresql-server using the psql utility.

```bash
$ sudo -u postgres psql
```

Specify the password for the Postgres user (the default password in the Kaa configuration files is **admin**).

```bash
postgres=# \password
Enter new password: admin
Enter it again: admin
```

Create a Kaa database.

```bash
CREATE DATABASE "kaa"
	  WITH OWNER "postgres"
	  ENCODING 'UTF8'
	  LC_COLLATE = 'en_US.UTF-8'
	  LC_CTYPE = 'en_US.UTF-8'
	  TEMPLATE template0;
```

Update the `pg\_hba.conf` file to allow local connections.

```bash
$ sudo nano /var/lib/pgsql/9.4/data/pg_hba.conf

remove lines:
local   all             all                                     peer
host    all             all             127.0.0.1/32            ident

add lines:
local   all             all                                     trust
host    all             all             127.0.0.1/32            trust
```

Restart the database.

```bash
$ systemctl restart postgresql-9.4

```


</div><!---Cent7---></div><!---tCent67--->
</ul>

</div><!---Pos---></div><!---tMarPos--->
</ul>

<ol>
<li value="4" markdown="1">
Install [Zookeeper 3.4.9](http://zookeeper.apache.org/doc/r3.4.9/).

Download and extract Zookeeper packages.

```bash
$ cd /opt
$ sudo wget http://www.eu.apache.org/dist/zookeeper/zookeeper-3.4.9/zookeeper-3.4.9.tar.gz
$ sudo tar zxvf zookeeper-3.4.9.tar.gz
$ sudo cp zookeeper-3.4.9/conf/zoo_sample.cfg zookeeper-3.4.9/conf/zoo.cfg
```

Create a data directory.

```bash
$ sudo mkdir /var/zookeeper
```

Edit the `dataDir` property in the zookeeper configuration file.

```bash
$ sudo nano /opt/zookeeper-3.4.9/conf/zoo.cfg
```

```bash
...
dataDir=/var/zookeeper
...
```

<ul>
	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#supervisor_centos67">CentOS 6.7</a></li>
		<li><a data-toggle="tab" href="#supervisor_centos72">CentOS 7.2</a></li>
	</ul>
</ul>

<ul>
	<div class="tab-content"><!---tCent67---><div id="supervisor_centos67" class="tab-pane fade in active" markdown="1" ><!---Cent6--->

Install the supervisor utility.

```bash
$ sudo rpm -Uvh http://download.fedoraproject.org/pub/epel/6/i386/epel-release-6-8.noarch.rpm
$ sudo yum install supervisor
```

Edit the `/etc/supervisord.conf` file and add a section about ZooKeeper to it.

```bash
$ sudo nano /etc/supervisord.conf
[program:zookeeper]
command=/opt/zookeeper-3.4.9/bin/zkServer.sh start-foreground
autostart=true
autorestart=true
startsecs=1
startretries=999
redirect_stderr=false
stdout_logfile=/var/log/zookeeper-out
stdout_logfile_maxbytes=10MB
stdout_logfile_backups=10
stdout_events_enabled=true
stderr_logfile=/var/log/zookeeper-err
stderr_logfile_maxbytes=100MB
stderr_logfile_backups=10
stderr_events_enabled=true
```

Configure the supervisor to start automatically when OS starts.

```bash
$ sudo chkconfig supervisord on
```

Start Zookeeper.

```bash
$ sudo service supervisord start
Starting supervisord:                 [  OK  ]
```

Check Zookeeper status.

```bash
$ sudo supervisorctl
zookeeper RUNNING pid 24765, uptime 0:00:06
```

Check if Zookeeper service is running.

```bash
$ netstat -ntlp | grep 2181
tcp    0   0 :::2181         :::*           LISTEN   2132/java
```

</div><!---Cent6---><div id="supervisor_centos72" class="tab-pane fade" markdown="1" ><!---Cent7--->

Install the supervisor utility.

```bash
$ sudo rpm -Uvh http://dl.fedoraproject.org/pub/epel/7/x86_64/e/epel-release-7-8.noarch.rpm
$ sudo yum install supervisor
```

Edit the `/etc/supervisord.conf` file and add a section about ZooKeeper to it.

```bash
$ sudo nano /etc/supervisord.conf
[program:zookeeper]
command=/opt/zookeeper-3.4.9/bin/zkServer.sh start-foreground
autostart=true
autorestart=true
startsecs=1
startretries=999
redirect_stderr=false
stdout_logfile=/var/log/zookeeper-out
stdout_logfile_maxbytes=10MB
stdout_logfile_backups=10
stdout_events_enabled=true
stderr_logfile=/var/log/zookeeper-err
stderr_logfile_maxbytes=100MB
stderr_logfile_backups=10
stderr_events_enabled=true
```

Configure the supervisor to start automatically when OS starts.

```bash
$ systemctl enable supervisord
```

Start Zookeeper.

```bash
$ systemctl start supervisord
```

Check Zookeeper status.

```bash
$ sudo supervisorctl
zookeeper                        RUNNING   pid 15546, uptime 0:00:41
```

Check if Zookeeper service is running.

```bash
$ sudo netstat -ntlp | grep 2181
  tcp6       0      0 :::2181                 :::*                    LISTEN      15546/java
```

</div><!---Cent7---></div><!---tCent67--->
</ul>

</li>
</ol>

<ol>
<li value="5" markdown="1">
Install a NoSQL DB.
Kaa requires MongoDB (used by default) or Cassandra.
</li>
</ol>

<ul>
	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#mongo_centos">MongoDB</a></li>
		<li><a data-toggle="tab" href="#cassandra_centos">Cassandra</a></li>
	</ul>
</ul>

<ul>
	<div class="tab-content"><!---tMonCas---><div id="mongo_centos" class="tab-pane fade in active" markdown="1" ><!---Mon--->

Add the MongoDB yum repository.

```bash
$ sudo nano /etc/yum.repos.d/mongodb.repo
[mongodb]
name=MongoDB Repository
baseurl=http://downloads-distro.mongodb.org/repo/redhat/os/x86_64/
gpgcheck=0
enabled=1
```

Install [MongoDB 2.6](http://www.mongodb.org/downloads).

```bash
$ sudo yum install -y mongodb-org
```

Start MongoDB.

```
$ sudo service mongod start
Starting mongod: [ OK ]
```

Verify that MongoDB started successfully.

```bash
$ sudo cat /var/log/mongodb/mongod.log | grep "waiting for connections on port"
2015-09-23T16:39:35.455+0300 [initandlisten] waiting for connections on port 27017
```

Configure the database to start automatically when OS starts.

```
$ sudo chkconfig mongod on
```

</div><!---Mon---><div id="cassandra_centos" class="tab-pane fade" markdown="1" ><!---Cas--->

Add the DataStax Community yum repository.

```bash
$ sudo nano /etc/yum.repos.d/datastax.repo
[datastax-ddc]
name = DataStax Repo for Apache Cassandra
baseurl = http://rpm.datastax.com/datastax-ddc/3.6
enabled = 1
gpgcheck = 0
```

Install Java Native Access (JNA).

```bash
$ sudo yum install jna
```

Install [Cassandra 3.6](http://cassandra.apache.org/download/).

```bash
$ sudo yum install datastax-ddc
```

Export java variables.

```bash
$ export JAVA_HOME=/usr/java/jdk1.8.0_60/
$ export PATH=$PATH:/usr/java/jdk1.8.0_60/bin/
```

Start Cassandra.

```bash
$ service cassandra start
```

Configure the database to start automatically when OS starts.

```bash
$ sudo chkconfig cassandra on
```

Check Cassandra cql shell.

```bash
$ cqlsh
Connected to Test Cluster at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 3.6.0 | CQL spec 3.4.2 | Native protocol v4]
Use HELP for help.
cqlsh>
```

</div><!---Cas---></div><!---tMonCas--->
</ul>
</div><!---C--->

</div><!---T--->


### Kaa server components

To install Kaa, you need to [download](http://www.kaaproject.org/download-kaa/) pre-built packages or build them from the [source code](https://github.com/kaaproject/kaa).
In this guide, the pre-built packages are used.

<ul class="nav nav-tabs">
	<li class="active"><a data-toggle="tab" href="#Ubuntu_">Ubuntu 14.04/16.04</a></li>
	<li><a data-toggle="tab" href="#CentOS_">CentOS 6.7/7.2</a></li>
</ul>

<div class="tab-content"><div id="Ubuntu_" class="tab-pane fade in active" markdown="1" >

1. Download the latest Debian package from the [Kaa download page](http://www.kaaproject.org/download-kaa/).

2. Unpack the downloaded tarball.

   ```bash
   $ tar -xvf kaa-deb-*.tar.gz
   ```

3. Install the Node service.

   ```bash
   $ sudo dpkg -i kaa-node-*.deb
   ```

</div><div id="CentOS_" class="tab-pane fade" markdown="1" >

1. Download the latest RPM package from the [Kaa download page](http://www.kaaproject.org/download-kaa/).

2. Unpack the downloaded tarball.

   ```bash
   $ tar -xvf kaa-rpm-*.tar.gz
   ```

3. Install the Node service.

   ```bash
   $ sudo rpm -i kaa-node.rpm
   ```

</div>
</div>

## Configuration

### SQL database

You can use MariaDB (used by default) or PostgreSQL.
Templates for the configuration property file is locatied in the `/etc/kaa-node/conf/` directory: `maria-dao.properties.template`, `mariadb-dao.properties.template` files for MariaDB database and `postgre-dao.properties.template`, `postgresql-dao.properties.template` files for PostgreSQL.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#maria_conf">MariaDB</a></li>
  <li><a data-toggle="tab" href="#postgre_conf">PostgreSQL</a></li>
</ul>

<div class="tab-content"><div id="maria_conf" class="tab-pane fade in active" markdown="1" >

Check that the MariaDB username and password are valid in the server configuration files.

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
In case of password or username mismatch, edit the configuration file.

```bash
$ sudo nano /etc/kaa-node/conf/admin-dao.properties

$ sudo nano /etc/kaa-node/conf/sql-dao.properties
```

To switch from PostgreSQL to MariaDB, copy the contents of the MariaDB config files to the Kaa database configuration files.

```bash
$ sudo bash -c "cat /etc/kaa-node/conf/maria-dao.properties.template > /etc/kaa-node/conf/sql-dao.properties"
$ sudo bash -c "cat /etc/kaa-node/conf/mariadb-dao.properties.template > /etc/kaa-node/conf/admin-dao.properties"
```

</div><div id="postgre_conf" class="tab-pane fade" markdown="1" >

Check that PostgreSQL username and password are valid in the server configuration files.

```bash
$ cat /etc/kaa-node/conf/admin-dao.properties | grep jdbc_password
jdbc_password=admin

$ cat /etc/kaa-node/conf/sql-dao.properties | grep jdbc_password
jdbc_password=admin
```
In case of password or username mismatch, edit the configuration file.

```bash
$ sudo nano /etc/kaa-node/conf/admin-dao.properties

$ sudo nano /etc/kaa-node/conf/sql-dao.properties
```

To switch from MariaDB to PostgreSQL, copy the contents of the PostgreSQL config files to the Kaa database configuration files.

```bash
$ sudo bash -c "cat /etc/kaa-node/conf/postgre-dao.properties.template > /etc/kaa-node/conf/sql-dao.properties"
$ sudo bash -c "cat /etc/kaa-node/conf/postgresql-dao.properties.template > /etc/kaa-node/conf/admin-dao.properties"
```

</div>
</div>

### NoSQL database

Check that the NoSQL database name is correct.

```
$ cat /etc/kaa-node/conf/nosql-dao.properties | grep nosql_db_provider_name
nosql_db_provider_name=mongodb
```

If you use Cassandra, run the following commands.

```bash
$ sudo cqlsh -f /etc/kaa-node/conf/cassandra.cql
$ sudo nano /etc/kaa-node/conf/nosql-dao.properties
nosql_db_provider_name=cassandra
```

### Network interface

To configure your interface for the [Operations]({{root_url}}Glossary/#operations-service) and [Bootstrap]({{root_url}}Glossary/#bootstrap-service) services, specify a hostname or an IP address that will be visible for devices in your network.
This will allow various devices to communicate with the server components.

```bash
$ sudo nano /etc/kaa-node/conf/kaa-node.properties
transport_public_interface=localhost=YOUR_PUBLIC_INTERFACE
```

### Firewall

<ul class="nav nav-tabs">
	<li class="active"><a data-toggle="tab" href="#Ubuntu14__">Ubuntu 14.04</a></li>
	<li><a data-toggle="tab" href="#Ubuntu16__">Ubuntu 16.04</a></li>
	<li><a data-toggle="tab" href="#CentOS6__">CentOS 6.7</a></li>
	<li><a data-toggle="tab" href="#CentOS7__">CentOS 7.2</a></li>
</ul>

<div class="tab-content"><div id="Ubuntu14__" class="tab-pane fade in active" markdown="1" >

Open TCP ports to be used by [Administration UI]({{root_url}}Glossary/#administration-ui) (8080), Bootstrap service (9888, 9889), and Operations service (9997, 9999).

```bash
$ sudo iptables -I INPUT -p tcp -m tcp --dport 22 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 8080 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9888 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9889 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9997 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9999 -j ACCEPT
$ sudo apt-get install iptables-persistent
$ sudo service iptables-persistent start
$ sudo service iptables-persistent save
```

</div><div id="Ubuntu16__" class="tab-pane fade" markdown="1" >

Open TCP ports to be used by Administration UI (8080), Bootstrap service (9888, 9889), and Operations service (9997, 9999).

```bash
$ sudo iptables -I INPUT -p tcp -m tcp --dport 22 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 8080 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9888 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9889 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9997 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9999 -j ACCEPT
$ sudo apt-get install iptables-persistent
$ sudo service netfilter-persistent start
$ sudo netfilter-persistent save
```

</div><div id="CentOS6__" class="tab-pane fade" markdown="1" >

Open TCP ports to be used by Administration UI (8080), Bootstrap service (9888, 9889), and Operations service (9997, 9999).

```bash
$ sudo iptables -I INPUT -p tcp -m tcp --dport 22 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 8080 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9888 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9889 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9997 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9999 -j ACCEPT
$ sudo service iptables save
```

</div><div id="CentOS7__" class="tab-pane fade" markdown="1" >

Open TCP ports to be used by Administration UI (8080), Bootstrap service (9888, 9889), and Operations service (9997, 9999).

```bash
$ systemctl stop firewalld
$ systemctl mask firewalld
$ yum install iptables-services
$ systemctl enable iptables
$ systemctl start iptables
$ sudo iptables -I INPUT -p tcp -m tcp --dport 22 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 8080 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9888 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9889 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9997 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9999 -j ACCEPT
$ sudo service iptables save
```

</div>
</div>

## Startup

Start the Kaa service.

```bash
$ sudo service kaa-node start
```

Check the logs after startup.

```bash
$ cat /var/log/kaa/* | grep ERROR
```

Open Administration UI in a web browser: [http://YOUR\_SERVER\_HOST:8080/kaaAdmin].
A page will open where you can log in as Kaa administrator.

---

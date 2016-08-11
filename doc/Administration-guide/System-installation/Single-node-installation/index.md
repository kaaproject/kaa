---
layout: page
title: Single node installation guide
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

* TOC:
{:toc}


> **Verified against host OS:** 
>
> * Ubuntu 14.04 LTS Desktop 64-bit
> * Ubuntu 16.04 LTS Desktop 64-bit
> * CentOS 6.7 64-bit


This guide describes installation and configuration of Kaa components on a single Linux node. If this is the first time you use Kaa, we recommend that you start the evaluation using the [Kaa Sandbox]({{root_url}}Getting-started/#kaa-sandbox) instead of attempting manual installation described in this guide. The Kaa Sandbox emulates a single-node Kaa installation which comes already pre-configured so that you could instantly start developing applications. Additionally, the Kaa Sandbox provides a number of demo applications for you to play with and learn by example. 

## Kaa requirements and supported versions

### Supported OS
Kaa supports the following operating system families and provides installation packages for each of them:

* Ubuntu and Debian systems
* Red Hat/CentOS/Oracle 5 or Red Hat 6 systems

### System requirements
To use Kaa, your system must meet the following minimum system requirements:

* 64-bit OS
* 4 Gb RAM

### Third party components
Kaa requires the following third party components to be installed and configured:

* [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Kaa has been tested on JDK 8.
* [PostgreSQL 9.4](http://www.postgresql.org/download/)
* [MariaDB 5.5](https://mariadb.com/)
* [Zookeeper 3.4.5 or later](http://zookeeper.apache.org/doc/r3.4.5/). Kaa requires ZooKeeper for coordination of server components.

Kaa has been tested on the latest production release of MariaDB and PostgreSQL.

Kaa also requires [MongoDB 2.6.9](http://www.mongodb.org/downloads) or [Cassandra 3.5](http://cassandra.apache.org/download/) as a NoSQL database. The installation steps for third-party components are provided in the following section.


## Installation steps

### Third party components installation

Refer to the following guides for more details.

Kaa requires the following third party components to be installed and configured. For more information refer to [required third party](../#third-party-components) list.

<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Ubuntu">Ubuntu 14.04/16.04</a></li>
  <li><a data-toggle="tab" href="#CentOS">CentOS 6.7</a></li>
</ul>

<div class="tab-content">
<div id="Ubuntu" class="tab-pane fade in active" markdown="1" >

1. Install common utils.

   Download and install wget, ca-certificates, curl.

   ```bash
   $ sudo apt-get install wget ca-certificates curl
   ```



2. Install [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html).  
   Add java apt repository. This repository is not official, for a different way to install Oracle JDK, see the [official page](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

   ```bash
   $ sudo add-apt-repository ppa:webupd8team/java
   $ sudo apt-get update
   $ sudo apt-get install oracle-java8-installer
   ```

   You can check if Oracle JDK is installed by executing the following command.

   ```bash
   $ javac -version
   ```

   In case of successful installation, you will receive the JDK version.

   ```bash
   javac 1.8.0_91
   ```

3. Install SQL DB:

   > **Note:** Kaa requires one of possible options: MariaDB or PostgresSQL. MariaDB is the default choice.

   <ul>
   <li style="list-style-type: none;">
   <ul class="nav nav-tabs">
   <li class="active"><a data-toggle="tab" href="#maria">MariaDB</a></li>
   <li><a data-toggle="tab" href="#postgre">PostgreSQL</a></li>
   </ul>

   <div class="tab-content">
   <div id="maria" class="tab-pane fade in active" markdown="1" >

   Install [MariaDB 5.5](https://mariadb.com/).

   Install the python-software-properties package.

   ```bash
   $ sudo apt-get install python-software-properties
   ```

   Now, we can add the key files for the MariaDB repository. 

   ```bash
   $ sudo apt-key adv --recv-keys --keyserver hkp://keyserver.ubuntu.com:80 0xcbcb082a1bb943db
   ```

   After that we add repository:

   ```bash
   $ sudo add-apt-repository 'deb http://mirror.jmu.edu/pub/mariadb/repo/5.5/ubuntu trusty main'
   ```

   Install MariaDB with Galera Patches:

   ```bash
   $ sudo apt-get update
   $ sudo apt-get install mariadb-galera-server-5.5 mariadb-client-5.5
   ```

   If, for some reason, you do not already have rsync installed on your machines, you should install it now by typing:

   ```bash   
   $ sudo apt-get install rsync
   ```

   You can check if the MariaDB service is running by executing the following command:

   ```bash   
   $ sudo netstat -ntlp | grep 3306
   ```

   Connect to the mysql-server by executing the following command:

   ```bash   
   $ mysql -u root -p
   ```

   Specify the login and password for user (the default login and password in Kaa configuration files is "sqladmin" and "admin"):


   ```sql
   CREATE USER 'sqladmin'@'localhost' IDENTIFIED BY 'admin'; GRANT ALL PRIVILEGES ON *.* TO 'sqladmin'@'localhost' WITH GRANT OPTION; FLUSH PRIVILEGES;
   ```

   Create the Kaa database by executing the following command:

   ```sql   
   CREATE DATABASE kaa;
   ```

   </div><div id="postgre" class="tab-pane fade" markdown="1" >

   Install [PostgreSQL 9.4](http://www.postgresql.org/download/).

   Add official PostgreSQL repository.

   ```bash
   $ sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt/ $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
   $ wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
   ```

   Install PostgreSQL 9.4.

   ```bash
   $ sudo apt-get update
   $ sudo apt-get install postgresql-9.4
   ```

   You can check if the Postgresql service is running by executing the following command.

   ```bash
   $ sudo netstat -ntlp | grep 5432
   ```

   For more details, please refer to the [official page](https://wiki.postgresql.org/wiki/Apt).

   Connect to the postgresql-server via the psql utility by executing the following command.

   ```bash
   $ sudo -u postgres psql
   ```

   Specify the password for the postgres user (the default password in Kaa configuration files is "admin").

   ```bash
   postgres=# \password Enter new password: admin Enter it again: admin
   ```

   Create the Kaa database by executing the following command.

   ```sql
   CREATE DATABASE kaa;
   ```

   </div>
   </div>
   </li>
   </ul>

4. Install [Zookeeper 3.4.8](http://zookeeper.apache.org/doc/r3.4.8/).

   ```bash
   $ sudo apt-get install zookeeperd
   ```

   You can check if the Zookeeper service is running by executing the following command.

   ```bash
   $ netstat -ntlp | grep 2181
   ```

5. Install NoSQL DB:

> **Note:** Kaa requires one of possible options: MongoDB or Cassandra. MongoDB is the default choice.

<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#mongo">MongoDB</a></li>
  <li><a data-toggle="tab" href="#cassandra">Cassandra</a></li>
</ul>

<div class="tab-content">
<div id="mongo" class="tab-pane fade in active" markdown="1" >

Install [MongoDB](http://www.mongodb.org/downloads) (Optional, you may install [Cassandra 3.5](http://cassandra.apache.org/download/) instead) ([source](http://docs.mongodb.org/v2.6/tutorial/install-mongodb-on-red-hat/)).

If you have installed previous version, this is how you can completely uninstall it:

```bash
$ sudo dpkg -l | grep mongo
$ sudo apt-get remove mongodb* --purge
```

<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#mongo_ubuntu14">Ubuntu 14.04</a></li>
  <li><a data-toggle="tab" href="#mongo_ubuntu16">Ubuntu 16.04</a></li>
</ul>

<div class="tab-content">
<div id="mongo_ubuntu14" class="tab-pane fade in active" markdown="1" >

Add the MongoDB repository to the /etc/apt/sources.list.d/mongodb.list.

```bash
$ sudo apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10
$ echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | sudo tee /etc/apt/sources.list.d/mongodb.list
```

Install MongoDB.

```bash  
$ sudo apt-get update
$ sudo apt-get install -y mongodb-org=2.6.9 mongodb-org-server=2.6.9 mongodb-org-shell=2.6.9 mongodb-org-mongos=2.6.9 mongodb-org-tools=2.6.9
```

Start the MongoDB.

```bash
$ sudo service mongod start
```

</div><div id="mongo_ubuntu16" class="tab-pane fade" markdown="1" >

Add the MongoDB repository to the /etc/apt/sources.list.d/mongodb.list.

```bash
$ sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv EA312927
$ echo "deb http://repo.mongodb.org/apt/ubuntu trusty/mongodb-org/3.2 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.2.list
```

Install MongoDB.

```bash
$ sudo apt-get update
$ sudo apt-get install -y --allow-unauthenticated mongodb-org
```

Before start edit file:

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

Start the MongoDB.

```bash
$ sudo systemctl start mongodb
```

</div>
</div>
</li>
</ul>

Verify that MongoDB has started successfully.

```bash
$ sudo systemctl status mongodb
$ cat /var/log/mongodb/mongod.log | grep "waiting for connections on port"
2015-09-23T16:39:35.455+0300 [initandlisten] waiting for connections on port 27017
```

</div><div id="cassandra" class="tab-pane fade" markdown="1" >

Install [Cassandra 3.5](http://cassandra.apache.org/download/) (Optional, you may install [MongoDB](http://www.mongodb.org/downloads) instead) ([source](http://www.liquidweb.com/kb/how-to-install-cassandra-on-centos-6/)).

<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#cassandra_ubuntu14">Ubuntu 14.04</a></li>
  <li><a data-toggle="tab" href="#cassandra_ubuntu16">Ubuntu 16.04</a></li>
</ul>

<div class="tab-content">
<div id="cassandra_ubuntu14" class="tab-pane fade in active" markdown="1" >

Add the DataStax Community repository to the /etc/apt/sources.list.d/cassandra.sources.list.

```bash
$ echo "deb http://www.apache.org/dist/cassandra/debian 35x main" | sudo tee -a /etc/apt/sources.list.d/cassandra.sources.list
$ curl -L http://debian.datastax.com/debian/repo_key | sudo apt-key add -
```

Install Cassandra 3.5.

```bash 
$ sudo apt-get update
$ sudo apt-get install cassandra=3.5
```

</div><div id="cassandra_ubuntu16" class="tab-pane fade" markdown="1" >

Since Cassandra requires python-support and this package was removed in Ubuntu 16.04 , manually install python-support.
Download deb package and unpack it:

```bash
$ sudo wget "http://launchpadlibrarian.net/109052632/python-support_1.0.15_all.deb"
$ sudo dpkg --install python-support_1.0.15_all.deb
```

Set Apache repo. Change the 35x to match the latest version. For example, use 36x if Cassandra 3.6 is the latest version:

```bash
$ echo "deb http://www.apache.org/dist/cassandra/debian 35x main" | sudo tee -a /etc/apt/sources.list.d/cassandra.sources.list
$ curl -L http://debian.datastax.com/debian/repo_key | apt-key add -
```

Install Cassandra 3.5.

```bash
$ sudo apt-get update
$ sudo apt-get install -y --allow-unauthenticated cassandra=3.5
```
</div>
</div>
</li>
</ul>

You can check if the Cassandra service is running by executing the following command.

```bash
$ sudo netstat -ntlp | grep 9042
```

Install Java Native Access (JNA).

```bash
$ sudo apt-get install libjna-java
```

Check cassandra cql shell.

```bash
$ cqlsh
Connected to Test Cluster at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 3.5 | CQL spec 3.4.0 | Native protocol v4]
Use HELP for help.
cqlsh>
```

</div>
</div>
</li>
</ul>

</div><div id="CentOS" class="tab-pane fade" markdown="1" >

1. Install common utils.

   Download and install wget, nc, gzip.

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
   $ alternatives --install /usr/bin/java java /usr/java/jdk1.8.0_60/bin/java 2
   $ alternatives --config java
   There are 3 programs which provide 'java'.
   Selection   Command
   -----------------------------------------------
    1      /usr/lib/jvm/jre-1.7.0-openjdk.x86_64/bin/java
    2      /usr/lib/jvm/jre-1.6.0-openjdk.x86_64/bin/java
    3      /usr/java/jdk1.8.0_60/bin/java
   ```

   Check Java version.

   ```bash
   $ java -version

   java version "1.8.0_60"
   Java(TM) SE Runtime Environment (build 1.8.0_60-b27)
   Java HotSpot(TM) 64-Bit Server VM (build 25.60-b23, mixed mode)
   ```

3. Install SQL DB:

   > **Note:** Kaa requires one of possible options: MariaDB or PostgresSQL. MariaDB is the default choice.

   <ul>
   <li style="list-style-type: none;">
   <ul class="nav nav-tabs">
     <li class="active"><a data-toggle="tab" href="#maria_centos">MariaDB</a></li>
     <li><a data-toggle="tab" href="#postgre_centos">PostgreSQL</a></li>
   </ul>

   <div class="tab-content">
   <div id="maria_centos" class="tab-pane fade in active" markdown="1" >

   Install [MariaDB 5.5](https://mariadb.org/download/) (Optional, you may install [PostgreSQL 9.4](http://www.postgresql.org/download/) instead).

   Install MariaDB 5.5 for CentOS 6.7 64-bit.
   Add MariaDB YUM repository entry for CentOS. Copy and paste it into a file under /etc/yum.repos.d/ (name it MariaDB.repo or something similar).


   ```bash

   # MariaDB 5.5 CentOS repository list - created 2016-04-01 10:22 UTC
   # http://mariadb.org/mariadb/repositories/
   [mariadb]
   name = MariaDB
   baseurl = http://yum.mariadb.org/5.5/centos6-amd64
   gpgkey=https://yum.mariadb.org/RPM-GPG-KEY-MariaDB
   gpgcheck=1
   ```

   Then execute folowing commands

   ```bash

   $ sudo yum -y update
   $ sudo service mysqld stop
   $ sudo yum -y remove mysql-server mysql

   ```

   With the repo file in place you can now install MariaDB like so:

   ```bash
   $ sudo yum install MariaDB-server MariaDB-client
   $ sudo service mysql start
   ```

   You can check if the MariaDB server is running by executing the following command.

   ```bash
   $ sudo netstat -ntlp | grep 3306

   tcp    0   0 127.0.0.1:3306     0.0.0.0:*       LISTEN   7386/mysqld
   ```

   For more details, please refer to the [official page](https://mariadb.org/).

   Connect to the mariadb-server by executing the following command.

   ```bash
   $ mysql
   ```

   Create new user for database by executing the following command.

   ```bash
   MariaDB [(none)]> CREATE USER sqladmin@localhost IDENTIFIED BY 'admin';
   ```

   Create the Kaa database by executing the following command.

   ```bash
   MariaDB [(none)]> CREATE DATABASE kaa;
   ```

   </div><div id="postgre_centos" class="tab-pane fade" markdown="1" >

   Install [PostgreSQL 9.4](http://www.postgresql.org/download/) ( [source](https://wiki.postgresql.org/wiki/YUM_Installation) ) (Optional, you may install [MariaDB 5.5](https://mariadb.org/download/) instead).

   Exclude old PostgreSQL from the default repository, append a line ```exclude=postgresql*``` to the sections **[base]** and **[updates]**.

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

   Install PostgreSQL 9.4 PGDG file for CentOS 6 64-bit.

   ```bash
   $ sudo yum localinstall http://yum.postgresql.org/9.4/redhat/rhel-6-x86_64/pgdg-centos94-9.4-1.noarch.rpm
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

   Connect to the postgresql-server via the psql utility by executing the following command.

   ```bash
   $ sudo -u postgres psql
   ```

   Specify the password for the postgres user (default password in kaa configuration files is "admin").

   ```bash
   postgres=# \password
   Enter new password: admin
   Enter it again: admin
   ```

   Create the Kaa database by executing the following command.

   ```bash
   CREATE DATABASE kaa;
   ```

   Update pg\_hba.conf file to allow local connections.

   ```bash
   $ sudo nano /var/lib/pgsql/9.4/data/pg_hba.conf

   remove lines:
   local   all      all                ident
   host   all      all      127.0.0.1/32      ident

   add lines:
   local   all      all                trust
   host   all      all      127.0.0.1/32      trust
   ```

   Restart the database.

   ```bash
   $ sudo service postgresql-9.4 restart
   
   Stopping postgresql-9.4 service:            [  OK  ]
   Starting postgresql-9.4 service:            [  OK  ]
   ```

   </div>
   </div>
   </li>
   </ul>

4. Install [Zookeeper 3.4.7](http://zookeeper.apache.org/doc/r3.4.7/).

   Download and extract Zookeeper packages.

   ```bash
   $ cd /opt
   $ sudo wget http://www.eu.apache.org/dist/zookeeper/zookeeper-3.4.7/zookeeper-3.4.7.tar.gz
   $ sudo tar zxvf zookeeper-3.4.7.tar.gz
   $ sudo cp zookeeper-3.4.7/conf/zoo_sample.cfg zookeeper-3.4.7/conf/zoo.cfg
   ```

   Create a data directory.

   ```bash
   $ sudo mkdir /var/zookeeper
   ```

   Edit dataDir property in zookeeper configuration file.

   ```bash
   $ sudo nano /opt/zookeeper-3.4.7/conf/zoo.cfg
   ```

   Install the supervisor utility.

   ```bash
   $ sudo rpm -Uvh http://download.fedoraproject.org/pub/epel/6/i386/epel-release-6-8.noarch.rpm
   $ yum install supervisor
   ```

   Edit the _/etc/supervisord.conf_ file and add a section about ZooKeeper to it.

   ```bash
   $ sudo nano /etc/supervisord.conf
   [program:zookeeper]
   command=/opt/zookeeper-3.4.7/bin/zkServer.sh start-foreground
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

   Configure the database to start automatically when OS starts.

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

   You can check if the Zookeeper service is running by executing the following command.

   ```bash
   $ netstat -ntlp | grep 2181
   tcp    0   0 :::2181         :::*           LISTEN   2132/java
   ```


5. Install NoSQL DB:
 
> **Note:** Kaa requires one of possible options: MongoDB or Cassandra. MongoDB is the default choice.

<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#mongo_centos">MongoDB</a></li>
  <li><a data-toggle="tab" href="#cassandra_centos">Cassandra</a></li>
</ul>

<div class="tab-content">
<div id="mongo_centos" class="tab-pane fade in active" markdown="1" >

Install [MongoDB 2.6](http://www.mongodb.org/downloads) (Optional, you may install [Cassandra 3.5](http://cassandra.apache.org/download/) instead) ([source](http://docs.mongodb.org/v2.6/tutorial/install-mongodb-on-red-hat/)).

Add the MongoDB yum repository.

```bash
$ sudo nano /etc/yum.repos.d/mongodb.repo
[mongodb]
name=MongoDB Repository
baseurl=http://downloads-distro.mongodb.org/repo/redhat/os/x86_64/
gpgcheck=0
enabled=1
```

Install MongoDB.

```bash
$ sudo yum install -y mongodb-org
```

Start MongoDB.

```   
$ sudo service mongod startStarting mongod: [ OK ]
```

Verify that MongoDB has started successfully.

```bash
$ cat /var/log/mongodb/mongod.log | grep "waiting for connections on port"
2015-09-23T16:39:35.455+0300 [initandlisten] waiting for connections on port 27017
```

Configure database to start automatically when OS starts.

```
$ sudo chkconfig mongod on
```

</div><div id="cassandra_centos" class="tab-pane fade" markdown="1" >

Install [Cassandra 3.5](http://cassandra.apache.org/download/) (Optional, you may install [MongoDB 2.6](http://www.mongodb.org/downloads) instead) ([source](http://www.liquidweb.com/kb/how-to-install-cassandra-on-centos-6/)).

Add the DataStax Community yum repository.

```bash
$ sudo nano /etc/yum.repos.d/datastax.repo
[datastax-ddc]
name = DataStax Repo for Apache Cassandra
baseurl = http://rpm.datastax.com/datastax-ddc/3.5
enabled = 1
gpgcheck = 0
```

Install Java Native Access (JNA).   

```bash
$ sudo yum install jna
```

Install Cassandra.

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

Check cassandra cql shell.

```bash
$ cqlsh
Connected to Test Cluster at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 3.5 | CQL spec 3.4.0 | Native protocol v4]
Use HELP for help.
cqlsh>
```

</div>
</div>
</li>
</ul>


</div>
</div>
</li>
</ul>


### Kaa server components

To install Kaa you will need to [download](http://www.kaaproject.org/download-kaa/) pre-built packages or [build](../Planning-your-deployment#building-kaa-from-source) them from the [source code](https://github.com/kaaproject/kaa). We will use pre-built packages in this guide.


<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Ubuntu_">Ubuntu 14.04/16.04</a></li>
  <li><a data-toggle="tab" href="#CentOS_">CentOS 6.7</a></li>
</ul>

<div class="tab-content"><div id="Ubuntu_" class="tab-pane fade in active" markdown="1" >

1. Download the latest Debian package from the [Kaa download page](http://www.kaaproject.org/download-kaa/).
2. Unpack the downloaded tarball by executing the following command.

   ```bash
   $ tar -xvf kaa-deb-*.tar.gz
   ```


3. Install the Node service by executing the following command.

   ```bash
   $ sudo dpkg -i kaa-node.deb
   ```

</div><div id="CentOS_" class="tab-pane fade" markdown="1" >

1. Download the latest RPM package from the [Kaa download page](http://www.kaaproject.org/download-kaa/).
2. Unpack the downloaded tarball by executing the following command.

   ```bash
   $ tar -xvf kaa-rpm-*.tar.gz
   ```

3. Install the Node service by executing the following command.

   ```bash
   $ sudo rpm -i kaa-node.rpm
   ```

</div>
</div>
</li>
</ul>

## Configuration steps

### SQL database configuration

You can choose which SQL database to use: MariaDB (used by default) or PostgreSQL.
You can find SQL database configuration property file templates in /etc/kaa-node/conf/ folder: maria-dao.properties.template, mariadb-dao.properties.template files for MariaDB database and postgre-dao.properties.template, postgresql-dao.properties.template files for PostgreSQL.

<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#maria_conf">MariaDB</a></li>
  <li><a data-toggle="tab" href="#postgre_conf">PostgreSQL</a></li>
</ul>

<div class="tab-content">
<div id="maria_conf" class="tab-pane fade in active" markdown="1" >

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

If you wish to switch from PostgreSQL to MariaDB you should copy content of MariaDB config files to Kaa DB config files:

```bash
$ sudo bash -c "cat /etc/kaa-node/conf/maria-dao.properties.template > /etc/kaa-node/conf/sql-dao.properties"
$ sudo bash -c "cat /etc/kaa-node/conf/mariadb-dao.properties.template > /etc/kaa-node/conf/admin-dao.properties"
```

</div><div id="postgre_conf" class="tab-pane fade" markdown="1" >

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

If you wish to switch from MariaDB to PostgreSQL you should copy content of PostgreSQL config files to Kaa DB config files:

```bash
$ sudo bash -c "cat /etc/kaa-node/conf/postgre-dao.properties.template > /etc/kaa-node/conf/sql-dao.properties"
$ sudo bash -c "cat /etc/kaa-node/conf/postgresql-dao.properties.template > /etc/kaa-node/conf/admin-dao.properties"
```

</div>
</div>
</li>
</ul>

### NoSQL database configuration

Check that a NoSQL database name matches your choice.

```
$ cat /etc/kaa-node/conf/nosql-dao.properties | grep nosql_db_provider_name
nosql_db_provider_name=mongodb
```

In case you are going to use Cassandra, execute the following commands.

```bash
$ sudo cqlsh -f /etc/kaa-node/conf/cassandra.cql
$ sudo nano /etc/kaa-node/conf/nosql-dao.properties
nosql_db_provider_name=cassandra
```

### Network interface configuration

This step will configure a public interface for Operations and Bootstrap servers. It is important to specify the hostname or an IP address that is visible for devices in your network. This will allow various devices to communicate with the server components.

```bash
$ sudo nano /etc/kaa-node/conf/kaa-node.properties
transport_public_interface=localhost=YOUR_PUBLIC_INTERFACE
```

### Firewall rules configuration

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Ubuntu14__">Ubuntu 14.04</a></li>
  <li><a data-toggle="tab" href="#Ubuntu16__">Ubuntu 16.04</a></li>
  <li><a data-toggle="tab" href="#CentOS__">CentOS 6.7</a></li>
</ul>

<div class="tab-content">
<div id="Ubuntu14__" class="tab-pane fade in active" markdown="1" >

Open TCP ports for Admin UI (8080), Bootstrap server (9888, 9889), Operations server (9997, 9999).

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

Open TCP ports for Admin UI (8080), Bootstrap server (9888, 9889), Operations server (9997, 9999).

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

</div><div id="CentOS__" class="tab-pane fade" markdown="1" >

Open TCP ports for Admin UI (8080), Bootstrap server (9888, 9889), Operations server (9997, 9999).

```bash
$ sudo iptables -I INPUT -p tcp -m tcp --dport 22 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 8080 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9888 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9889 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9997 -j ACCEPT
$ sudo iptables -I INPUT -p tcp -m tcp --dport 9999 -j ACCEPT
$ sudo service iptables save
```

</div></div>

## Startup steps

Start Kaa service.

```bash
$ sudo service kaa-node start
```

Check logs after the startup.

```bash
$ cat /var/log/kaa/* | grep ERROR
```

Open Admin UI in a web browser: [http://YOUR\_SERVER\_HOST:8080/kaaAdmin]. This will open a web page that will request to enter the Kaa administrator login and password information. This is one time operation.

---

---
layout: page
title: Single node installation guide
permalink: /:path/
nav: /:path/Administration-guide/System-installation/Single-node-installation
sort_idx: 20
---

# Single node installation guide


* [Introduction](#introduction)
* [Kaa requirements and supported versions](#kaa-requirements-and-supported-versions)
  * [Supported OS](#supported-os)
  * [System requirements](#system-requirements)
  * [Third party components](#third-party-components)
* [Installation steps](#installation-steps)
  * [Third party components instalation](#third-party-components-instalation)
  * [Kaa server components](#kaa-server-components)
* [Configuration steps](#configuration-steps)
  * [SQL database configuration](#sql-database-configuration)
  * [NoSQL database configuration](#nosql-database-configuration)
  * [Network interface configuration](#network-interface-configuration)
  * [Firewall rules configuration](#firewall-rules-configuration)
* [Startup steps](#startup-steps)
* [Further reading](#further-reading)

## Introduction

This guide describes installation and configuration of Kaa components on a single Linux node. If this is the first time you use Kaa, we recommend that you [Getting-started] the evaluation using the [Kaa-Sandbox] instead of attempting manual installation described in this guide. The Kaa Sandbox emulates a single-node Kaa installation which comes already pre-configured so that you could instantly start developing applications. Additionally, the Kaa Sandbox provides a number of demo applications for you to play with and learn by example.

## Kaa requirements and supported versions

### Supported OS

Kaa supports the following operating system families and provides installation packages for each of them.

* Ubuntu and Debian systems
* Red Hat/CentOS/Oracle 5 or Red Hat 6 systems

Please note that the instructions from this guide were tested on Ubuntu 14.04 and Centos 6.7\. Instructions for other OS may have minor differences.

### System requirements

To use Kaa, your system must meet the following minimum system requirements.

* 64-bit OS
* 4 Gb RAM

### Third party components

Kaa requires the following third party components to be installed and configured.

* [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Kaa has been tested on JDK 8\.
* [PostgreSQL 9.4](http://www.postgresql.org/download/). Kaa has been tested on the latest production release of PostgreSQL.
* [MariaDB 5.5](https://mariadb.org/download/). Kaa has been tested on the latest production release of MariaDB.
* [Zookeeper 3.4.5](http://zookeeper.apache.org/doc/r3.4.5/). Kaa requires ZooKeeper for coordination of server components.

Kaa also requires [MongoDB 2.6.9](http://www.mongodb.org/downloads) or [Cassandra 2.2.5](http://cassandra.apache.org/download/) as a NoSQL database. The installation steps for third-party components are provided in the following section.

## Installation steps

### Third party components instalation

Refer to the folowing guides for more details

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Ubuntu">Ubuntu 14.04</a></li>
  <li><a data-toggle="tab" href="#CentOS">CentOS 6.7</a></li>
</ul>

<div class="tab-content">
<div id="Ubuntu" class="tab-pane fade in active" markdown="1" >

### Ubuntu 14.04

1. Install common utils.

   Download and install wget, ca-certificates, curl.

   ```bash
   $ sudo apt-get install wget ca-certificates curl
   ```

2. Install [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html).  
   Add java apt repository. This repository is not official; for a different way to install Oracle JDK, see the [official page](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

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
   javac 1.8.0_66
   ```

3. Install [PostgreSQL 9.4](http://www.postgresql.org/download/).  
Add official PostgreSQL repository.

   ```bash
   $ sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt/ $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
   $ wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
   ```

   Install PostgreSQL 9.4 for Ubuntu 14.04 64-bit.

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
   postgres=# \password
   Enter new password: admin
   Enter it again: admin
   ```

   Create the Kaa database by executing the following command.

   ```bash
   CREATE DATABASE kaa;
   ```

4. Install [MariaDB 5.5](https://mariadb.org/download/)

   Install MariaDB 5.5 for Ubuntu 14.04 64-bit.

   ```bash
    $ sudo apt-get install software-properties-common
    $ sudo apt-key adv --recv-keys --keyserver hkp://keyserver.ubuntu.com:80 0xcbcb082a1bb943db
    $ sudo add-apt-repository 'deb [arch=amd64,i386] http://ftp.hosteurope.de/mirror/mariadb.org/repo/10.1/ubuntu trusty main'
   ```

   Once the key is imported and the repository added you can install MariaDB with:

   ```bash
    $ sudo apt-get update
    $ sudo apt-get install mariadb-server
   ```

   During the installation process you will be asked to configure the root password for the MariaDB, enter "admin".

   Connect to the mariadb-server by executing the following command.

   ```bash
    $ mysql -u root -padmin
   ```

   Create the Kaa database by executing the following command.

   ```bash
   MariaDB [(none)]> create database kaa;
   ```

5. Install [Zookeeper 3.4.5](http://zookeeper.apache.org/doc/r3.4.6/).  
Install Zookeeper 3.4.5 for Ubuntu 14.04 64-bit.

   ```bash
   $ sudo apt-get install zookeeperd
   ```

   You can check if the Zookeeper service is running by executing the following command.

   ```bash
   $ netstat -ntlp | grep 2181
   ```

6. Install [MongoDB 2.6](http://www.mongodb.org/downloads) (Optional, you may install [Cassandra 2.2.5](http://cassandra.apache.org/download/) instead) ([source](http://docs.mongodb.org/v2.6/tutorial/install-mongodb-on-red-hat/)).  
Add the MongoDB repository to the /etc/apt/sources.list.d/mongodb.list.

   ```bash
   $ sudo apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10
   $ echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | sudo tee /etc/apt/sources.list.d/mongodb.list
   ```

   Install MongoDB 2.6.9 for Ubuntu 14.04 64-bit.

   ```bash
   $ sudo apt-get update
   $ sudo apt-get install -y mongodb-org=2.6.9 mongodb-org-server=2.6.9 mongodb-org-shell=2.6.9 mongodb-org-mongos=2.6.9 mongodb-org-tools=2.6.9
   ```

   Start the MongoDB.

   ```bash
   $ sudo service mongod start
   ```

   Verify that MongoDB has started successfully.

   ```bash
   $ cat /var/log/mongodb/mongod.log | grep "waiting for connections on port"
    2015-09-23T16:39:35.455+0300 [initandlisten] waiting for connections on port 27017
   ```

7. Install [Cassandra 2.2.5](http://cassandra.apache.org/download/) (Optional, you may install [MongoDB 2.6](http://www.mongodb.org/downloads) instead) ([source](http://www.liquidweb.com/kb/how-to-install-cassandra-on-centos-6/)).  
   Add the DataStax Community repository to the /etc/apt/sources.list.d/cassandra.sources.list.

   ```bash
   $ echo "deb http://debian.datastax.com/community stable main" | sudo tee -a /etc/apt/sources.list.d/cassandra.sources.list
   $ curl -L http://debian.datastax.com/debian/repo_key | sudo apt-key add -
   ```

   Install Cassandra 2.2.5 for Ubuntu 14.04 64-bit.

   ```bash
   $ sudo apt-get update
   $ sudo apt-get install cassandra=2.2.5
   ```

   You can check if the Cassandra service is running by executing the following command.

   ```bash
   $ netstat -ntlp | grep 9042
   ```

   Install Java Native Access (JNA).

   ```bash
   $ sudo apt-get install libjna-java
   ```

   Check cassandra cql shell.

   ```bash
   $ cqlsh
   Connected to Test Cluster at 127.0.0.1:9042.
   [cqlsh 5.0.1 | Cassandra 2.2.5 | CQL spec 3.2.0 | Native protocol v3]
   Use HELP for help.
   cqlsh>
   ```

</div><div id="CentOS" class="tab-pane fade" markdown="1" >


### CentOS 6.7

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
      Selection    Command
    -----------------------------------------------
       1           /usr/lib/jvm/jre-1.7.0-openjdk.x86_64/bin/java
       2           /usr/lib/jvm/jre-1.6.0-openjdk.x86_64/bin/java
     + 3           /usr/java/jdk1.8.0_60/bin/java
   ```

   Check Java version.

   ```bash
    $ java -version

    java version "1.8.0_60"
    Java(TM) SE Runtime Environment (build 1.8.0_60-b27)
    Java HotSpot(TM) 64-Bit Server VM (build 25.60-b23, mixed mode)
   ```

3. Install [PostgreSQL 9.4](http://www.postgresql.org/download/) ( [source](https://wiki.postgresql.org/wiki/YUM_Installation) ).

   Exclude old PostgreSQL from the default repository.

   ```bash
    $ sudo nano /etc/yum.repos.d/CentOS-Base.repo
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
    Initializing database:                                     [  OK  ]
   ```

   Configure the database to start automatically when OS starts.

   ```bash
    $ sudo chkconfig postgresql-9.4 on
   ```

   Start the database.

   ```bash
    $ sudo service postgresql-9.4 start
    Starting postgresql-9.4 service:                           [  OK  ]
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
    sudo nano /var/lib/pgsql/9.4/data/pg_hba.conf
    remove lines:
    local   all             all                                     ident
    host    all             all             127.0.0.1/32            ident
    add lines:
    local   all             all                                     trust
    host    all             all             127.0.0.1/32            trust
   ```

   Restart the database.

   ```bash
    sudo service postgresql-9.4 restart
    Stopping postgresql-9.4 service:                           [  OK  ]
    Starting postgresql-9.4 service:                           [  OK  ]
   ```

4. Install [MariaDB 5.5](https://mariadb.org/download/)

   Install MariaDB 5.5 for CentOS 6.7 64-bit.
   Add MariaDB YUM repository entry for CentOS. Copy and paste it into a file under /etc/yum.repos.d/ (name it MariaDB.repo or something similar).

   ```bash
    # MariaDB 10.1 CentOS repository list - created 2016-03-31 15:28 UTC
    # http://mariadb.org/mariadb/repositories/
    [mariadb]
    name = MariaDB
    baseurl = http://yum.mariadb.org/10.1/centos6-amd64
    gpgkey=https://yum.mariadb.org/RPM-GPG-KEY-MariaDB
    gpgcheck=1
   ```

   With the repo file in place you can now install MariaDB like so:

   ```bash
    $ sudo yum install MariaDB-server MariaDB-client
   ```

   During the installation process you will be asked to configure the root password for the MariaDB, enter "admin".

   Connect to the mariadb-server by executing the following command.

   ```bash
    $ mysql -u root -padmin
   ```

   Create the Kaa database by executing the following command.

   ```bash
   MariaDB [(none)]> create database kaa;
   ```

5. Install [Zookeeper 3.4.7](http://zookeeper.apache.org/doc/r3.4.5/).

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
    Starting supervisord:                                      [  OK  ]
   ```

   Check Zookeeper status.

   ```bash
    $ sudo supervisorctl
    zookeeper RUNNING pid 24765, uptime 0:00:06
   ```

   You can check if the Zookeeper service is running by executing the following command.

   ```bash
    $ netstat -ntlp | grep 2181
    tcp        0      0 :::2181                     :::*                        LISTEN      2132/java
   ```

6. Install [MongoDB 2.6](http://www.mongodb.org/downloads) (Optional, you may install [Cassandra 2.2.5](http://cassandra.apache.org/download/) instead) ([source](http://docs.mongodb.org/v2.6/tutorial/install-mongodb-on-red-hat/)).

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

7. Install [Cassandra 2.2.5](http://cassandra.apache.org/download/) (Optional, you may install [MongoDB 2.6](http://www.mongodb.org/downloads) instead) ([source](http://www.liquidweb.com/kb/how-to-install-cassandra-on-centos-6/)).

   Add the DataStax Community yum repository.

   ```bash
    $ sudo nano /etc/yum.repos.d/datastax.repo
    [datastax]
    name = DataStax Repo for Apache Cassandra
    baseurl = http://rpm.datastax.com/community
    enabled = 1
    gpgcheck = 0
   ```

   Install Java Native Access (JNA).   

   ```bash
    $ sudo yum install jna
   ```

   Install Cassandra.

   ```bash
    $ sudo yum install dsc22
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
    [cqlsh 5.0.1 | Cassandra 2.2.5 | CQL spec 3.2.0 | Native protocol v3]
    Use HELP for help.
    cqlsh>
   ```

</div></div>


### Kaa server components

To install Kaa you will need to [download](http://www.kaaproject.org/download-kaa/) pre-built packages or [build](Building-Kaa-server-from-source) them from the [source code](https://github.com/kaaproject/kaa). We will use pre-built packages in this guide.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Ubuntu1">Ubuntu 14.04</a></li>
  <li><a data-toggle="tab" href="#CentOS1">CentOS 6.7</a></li>
</ul>

<div class="tab-content">
<div id="Ubuntu1" class="tab-pane fade in active" markdown="1" >

1. Download the latest Debian package from the [Kaa download page](http://www.kaaproject.org/download-kaa/).
2. Unpack the downloaded tarball by executing the following command.

   ```bash
    $ tar -xvf kaa-deb-*.tar.gz
   ```


3. Install the Node service by executing the following command.

   ```bash
    $ sudo dpkg -i kaa-node.deb
   ```

</div><div id="CentOS1" class="tab-pane fade" markdown="1" >

1. Download the latest RPM package from the [Kaa download page](http://www.kaaproject.org/download-kaa/).
2. Unpack the downloaded tarball by executing the following command.

   ```bash
    $ tar -xvf kaa-rpm-*.tar.gz
   ```

3. Install the Node service by executing the following command.

   ```bash
    $ sudo rpm -i kaa-node.rpm
   ```

</div></div>

## Configuration steps

### SQL database configuration

Check that the PostgreSQL password is up to date in the server configuration files.

```bash
$ cat /etc/kaa-node/conf/admin-dao.properties | grep jdbc_password
jdbc_password=admin
$ cat /etc/kaa-node/conf/dao.properties | grep jdbc_password
jdbc_password=admin
```

In case of the password mismatch, edit the configuration file to set a new password.

```bash
$ sudo nano /etc/kaa-node/conf/admin-dao.properties
$ sudo nano /etc/kaa-node/conf/dao.properties
```

### NoSQL database configuration

Check that a NoSQL database name matches your choice.

```
$ cat /etc/kaa-node/conf/dao.properties | grep nosql_db_provider_name
nosql_db_provider_name=mongodb
```

In case you are going to use Cassandra, execute the following commands.

```bash
$ sudo cqlsh -f /etc/kaa-node/conf/cassandra.cql
$ sudo nano /etc/kaa-node/conf/dao.properties
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
  <li class="active"><a data-toggle="tab" href="#Ubuntu2">Ubuntu</a></li>
  <li><a data-toggle="tab" href="#CentOS2">CentOS</a></li>
</ul>

<div class="tab-content">
<div id="Ubuntu2" class="tab-pane fade in active" markdown="1" >

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

</div><div id="CentOS2" class="tab-pane fade" markdown="1" >

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
$ cd /var/log/kaa
$ cat * | grep ERROR
```

Open Admin UI in a web browser: [http://YOUR\_SERVER\_HOST:8080/kaaAdmin]. This will open a web page that will request to enter the Kaa administrator login and password information. This is one time operation.

## Further reading

Use the following guides and references to make the most of Kaa.

[Administration UI guide](#) Use this guide to start working with the Kaa web UI.

[Programming guide](#) Use this guide to create your own Kaa applications.

---

Copyright (c) 2014-2016, [CyberVision, Inc.](http://www.cybervisiontech.com/)

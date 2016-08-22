---
layout: page
title: Building server from source
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

* TOC
{:toc}


This page describes how to build the Kaa server from the source code available on [GitHub](https://github.com/kaaproject/kaa).
Before building the Kaa server from source, ensure that [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Apache Maven](https://maven.apache.org/) are installed on your machine.

### Fetching source code

It is allowed to use any Git client to fetch the Kaa source code from the repository.

[Set up your Git configuration](https://git-scm.com/book/tr/v2/Customizing-Git-Git-Configuration) (at least the username and email) and download Kaa repository as follows:

```bash
 $ git clone https://github.com/kaaproject/kaa.git
```

To build Kaa node Debian/RPM packages, execute the following command.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Debian">Ubuntu</a></li>
  <li><a data-toggle="tab" href="#RPM">CentOS</a></li>
</ul>
<div class="tab-content">
<div id="Debian" class="tab-pane fade in active" markdown="1" ><br/>  
          
```bash
 $ mvn clean install -P jenkins,compile-gwt,mariadb-dao,mongo-dao -Dappenders -Dverifiers -DskipTests
```

</div><div id="RPM" class="tab-pane fade" markdown="1" ><br/>
      
```bash
 $ mvn clean install -P jenkins,compile-gwt,mariadb-dao,mongo-dao,build-rpm -Dappenders -Dverifiers -DskipTests
```

</div></div><br/>


> **NOTE:**  The Debian build will work correctly on both Linux and Windows operation systems, while the RPM build will work only on Linux operated machines with the RPM tool installed.

> **NOTE:**  If you want to enable execution of tests than remove "-DskipTests" suffix in mvn command.


## Available maven profiles

| Maven profile         | Description                                                                                                                                                                                   | 
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|build-rpm              |	As implied in the profile name, it will force to generate .rpm packages. This is useful if you are going to install Kaa on .rpm based Linux distribution (Red Hat Linux, Oracle Linux, etc.)|											
|compile-gwt            |	Compiles administration user interface. Can be skipped during regular builds.											                                                                    |
|compile-client-c       |	Compiles [C endpoint SDK](http://docs.kaaproject.org/display/KAA/Linux#Linux-CendpointSDK) and executes tests.											                                    |
|compile-client-cpp     |   Compiles [C++ endpoint SDK](http://docs.kaaproject.org/display/KAA/Linux#Linux-C++endpointSDK) and executes tests.											                                |
|cassandra	            |   Compiles [Cassandra](http://cassandra.apache.org/) log appender.											                                                                                |
|couchbase              |	Compiles [Couchbase](http://www.couchbase.com/) log appender.											                                                                                    |
|kafka                  |	Compiles [Kafka](http://kafka.apache.org/) log appender.											                                                                                        |
|compile-client-objc	|   Compiles [Objective-C endpoint SDK](http://docs.kaaproject.org/display/KAA/iOS) and executes tests. Compiling only possible on OS X and CentOS operation systems.							|
|oracle-nosql	        |   Compiles [Oracle NoSQL](http://www.oracle.com/us/products/database/nosql/overview/index.html) log appender.											                                        |
|jenkins	            |   Forces Kaa to execute integration tests.											                                                                                                        |
|cassandra-dao          |   Forces Kaa to use [Cassandra](http://cassandra.apache.org/) NoSQL storage. If none is set MongoDB used by default.											                                |
|mariadb-dao	        |   Forces Kaa to use [MariaDB](https://mariadb.org/) SQL storage. Enabled by default.											                                                                |
|license	            |   Forces Kaa to use [Maven License](http://code.mycila.com/license-maven-plugin/) plugin.											                                                            |
|postgresql-dao	        |   Forces Kaa to use [PostgreSQL](http://www.postgresql.org/) SQL storage. If none is set MariaDB used by default.											                                    |
|mongo-dao	            |   Forces Kaa to use [MongoDB](https://www.mongodb.org/) NoSQL storage. Enabled by default.											                                                        |
|compile-thrift	        |   Forces [Thrift](https://thrift.apache.org/) compiler to generate code from thrift files.											                                                        |



## Build artifacts

The following command can be used to browse the Kaa node build artifacts in case of successful build.

    $ ls server/node/target/kaa-node.deb

    $ ls server/node/target/rpm/kaa-node/RPMS/noarch/kaa-node*.rpm

## Installing Kaa

You can deploy Kaa server as a single node or as multi node cluster, for more details refer to corresponding pages:

* [Single node installation]({{root_url}}Administration-guide/System-installation/Single-node-installation/)
* [Node cluster setup]({{root_url}}Administration-guide/System-installation/Cluster-setup/)
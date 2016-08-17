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
 $ mvn clean install -P jenkins,compile-gwt,mariadb-dao,mongo-dao -Dappenders -Dverifiers
```

</div><div id="RPM" class="tab-pane fade" markdown="1" ><br/>
      
```bash
 $ mvn clean install -P jenkins,compile-gwt,mariadb-dao,mongo-dao,build-rpm -Dappenders -Dverifiers
```

</div></div><br/>



> **NOTE:**  The Debian build will work correctly on both Linux and Windows operation systems, while the RPM build will work only on Linux operated machines with the RPM tool installed.

> **NOTE:**  For the mvn command, the build number and git commit variables are set to emulate Jenkins build variables that are substituted automatically on the build machine.

> **NOTE:**  Please add "-DskipTests" suffix to the mvn command in order to skip execution of tests and speed up build process.

## Available maven profiles

| Maven profile         | Description                                                                                                                                                                                   | 
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|build-rpm              |	As implied in the profile name, it will force to generate .rpm packages. This is useful if you are going to install Kaa on .rpm based Linux distribution (Red Hat Linux, Oracle Linux, etc.)|											
|compile-gwt            |	Compiles administration user interface. Can be skipped during regular builds.											                                                                    |
|compile-client-c       |	Compiles C endpoint SDK and executes tests.											                                                                                                        |
|compile-client-cpp     |   Compiles C++ endpoint SDK and executes tests.											                                                                                                    |
|cassandra	            |   Compiles Cassandra log appender.											                                                                                                                |
|cdap	                |   Compiles CDAP log appender.											                                                                                                                        |
|couchbase              |	Compiles Couchbase log appender.											                                                                                                                |
|kafka                  |	Compiles Kafka log appender.											                                                                                                                    |
|compile-client-objc	|   Compiles Objective-C endpoint SDK and executes tests. Compiling only possible on OS X and CentOS operation systems.											                                |
|oracle-nosql	        |   Compiles Oracle NoSQL log appender.											                                                                                                                |
|jenkins	            |   Forces Kaa to execute integration tests.											                                                                                                        |
|cassandra-dao          |   Forces Kaa to use Cassandra NoSQL storage. If none is set MongoDB used by default.											                                                                |
|mariadb-dao	        |   Forces Kaa to use MariaDB SQL storage. Enabled by default.											                                                                                        |
|license	            |   Forces Kaa to use Maven License plugin.											                                                                                                            |
|postgresql-dao	        |   Forces Kaa to use PostgreSQL SQL storage. If none is set MariaDB used by default.											                                                                |
|mongo-dao	            |   Forces Kaa to use MongoDB NoSQL storage. Enabled by default.											                                                                                    |
|compile-thrift	        |   Forces Thrift compiler to generate code from thrift files.											                                                                                        |



## Build artifacts

The following command can be used to browse the Kaa node build artifacts in case of successful build.

    $ ls server/node/target/kaa-node.deb

    $ ls server/node/target/rpm/kaa-node/RPMS/noarch/kaa-node*.rpm

## Installing Kaa

You can deploy Kaa server as a single node or as multi node cluster, for more details refer to corresponding pages:

* [Single node installation](../Single-node-installation)
* [Node cluster setup](../Cluster-setup)
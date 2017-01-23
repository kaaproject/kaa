---
layout: page
title: Building Kaa server from source code
permalink: /:path/
sort_idx: 10
---

* TOC
{:toc}

{% include variables.md %}

This section describes how to build the [Kaa server]({{root_url}}Glossary/#) from the source code [available on GitHub]({{github_url}}).

Before building the Kaa server from source, make sure that [Oracle JDK 8](http://www.oracle.com/) and [Apache Maven](https://maven.apache.org/) are installed on your machine.

## Fetching source code

You can use any Git client to fetch Kaa source code from the repository.

To download Kaa repository, run the command below.

```
$ git clone https://github.com/kaaproject/kaa.git
```

To build Kaa node Debian/RPM packages, change the current directory after cloning.

```
cd kaa
```
And run the following command.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#debian1">Debian</a></li>
  <li><a data-toggle="tab" href="#rpm1">RPM</a></li>
</ul>

<div class="tab-content"><div id="debian1" class="tab-pane fade in active" markdown="1" >

```
$ mvn -P compile-gwt,mongo-dao,mariadb-dao clean install verify
```

</div><div id="rpm1" class="tab-pane fade" markdown="1" >

```
$ mvn -P compile-gwt,mongo-dao,mariadb-dao,build-rpm clean install verify
```

</div>
</div>

The Debian build will work correctly on both Linux and Windows operation systems, while the RPM build will only work on Linux with the RPM package manager installed.

For the `mvn` command, the build number and git commit variables are set to emulate [Jenkins](https://jenkins.io/) build variables that are substituted automatically on the build machine.

Add the `-DskipTests` suffix to the `mvn` command to skip execution of tests and speed up the build process.

## Available Maven profiles

| Maven profile | Description |
|--------------|-----------|
| `build-rpm` |As implied in the profile name, it will force to generate `.rpm` packages. This is useful if you are going to install Kaa on RPM-based Linux distribution (Red Hat Linux, Oracle Linux, etc.). |
| `cassandra` |Compiles [Cassandra log appender]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Cassandra-log-appender/). |
| `cassandra-dao` |Forces Kaa to use Cassandra NoSQL storage. If none is set, [MongoDB](https://www.mongodb.com/) used by default. |
| `compile-client-c` |Compiles endpoint [C SDK]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/) and runs tests. |
| `compile-client-cpp` |Compiles endpoint [C++ SDK]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/) and runs tests. |
| `compile-client-objc` |Compiles endpoint [Objective-C]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/Objective-C/)  SDK and executes tests. Compiling only possible on OS X and CentOS operation systems. |
| `compile-gwt` |Compiles [Administration UI]({{root_url}}Glossary/#administration-ui). Can be skipped during regular builds. |
| `compile-thrift` |Forces Thrift compiler to generate code from Thrift files. |
| `couchbase` |Compiles [Couchbase log appender]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Couchbase-log-appender/). |
| `jenkins` |Forces Kaa to run integration tests. |
| `kafka` |Compiles [Kafka log appender]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Kafka-log-appender/). |
| `license` |Forces Kaa to use Maven License plugin. |
| `mariadb-dao` |Forces Kaa to use [MariaDB SQL](https://mariadb.org/) storage. Enabled by default. |
| `mongo-dao` |Forces Kaa to use MongoDB NoSQL storage. Enabled by default. |
| `oracle-nosql` |Compiles  [Oracle NoSQL]({{root_url}}Programming-guide/Key-platform-features/Data-collection/Oracle-NoSQL-log-appender/) log appender. |
| `postgresql-dao` |Forces Kaa to use [PostgreSQL SQL](https://www.postgresql.org/) storage. If none is set, MariaDB used by default. |

>**NOTE:** `compile-client-*` profiles are optional.
>All endpoint SDKs will be available in Kaa node by default.
>This profiles are used only for verification of endpoint SDK build during development.
{:.note}

## Build artifacts

You can use the following command to browse the Kaa node build artifacts in case of successful build.

<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#debian2">Debian</a></li>
  <li><a data-toggle="tab" href="#rpm2">RPM</a></li>
</ul>

<div class="tab-content"><div id="debian2" class="tab-pane fade in active" markdown="1" >

```
$ ls server/node/target/kaa-node.deb
```

</div><div id="rpm2" class="tab-pane fade" markdown="1" >

```
$ ls server/node/target/rpm/kaa-node/RPMS/noarch/kaa-node*.rpm
```

</div>
</div>

## Installing Kaa

After you successfully built the Kaa server from sources, install a Kaa single node as described in [Single node installation]({{root_url}}Administration-guide/System-installation/Single-node-installation/).
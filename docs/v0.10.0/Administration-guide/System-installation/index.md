---
layout: page
title: System installation
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

* TOC
{:toc}

This section provides an overview of the [Kaa platform]({{root_url}}Glossary/#kaa-platform) installation process.

## Prerequisites

Below are the minimum system requirements for a [Kaa deployment]({{root_url}}Glossary/#kaa-instance-kaa-deployment):

   * 64-bit OS
   * 256 Mb RAM if third-party components (SQL, NoSQL, Zookeeper, etc.) are deployed remotely.
   * 4 Gb RAM if third-party components are deployed on the same node.

Kaa supports the following operating system families and provides installation packages for each of them.

   * CentOS 6
   * Red Hat Enterprise Linux (RHEL) 6
   * Oracle Linux 5
   * Oracle Linux 6
   * Ubuntu 14.04
   * Ubuntu 16.04

>**NOTE:** This guide is verified against:
>
> * Ubuntu 14.04 LTS Desktop 64-bit
> * Ubuntu 16.04 LTS Desktop 64-bit
> * CentOS 6.7 64-bit
{:.note}

The following software components are required:

   * yum (for RHEL or CentOS)
   * rpm (for RHEL, CentOS, or SLES)
   * scp
   * curl
   * wget
   * unzip
   * tar

## Third-party components

Kaa requires the following third-party components to be installed and configured.

* [Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
* [PostgreSQL 9.4](http://www.postgresql.org/download/) or [MariaDB 5.5](https://mariadb.org/download/).
* [Zookeeper 3.4.5](http://zookeeper.apache.org/doc/r3.4.5/).

Kaa has been tested on the latest production release of MariaDB and PostgreSQL.

Kaa also requires [MongoDB 2.6.9](http://www.mongodb.org/downloads) or [Cassandra 3.5](http://cassandra.apache.org/download/) as a NoSQL database.

### Zookeeper

Apache ZooKeeper enables highly reliable coordination of distributed nodes in a [Kaa cluster]({{root_url}}Glossary/#kaa-cluster).
A Kaa node continuously pushes information about its connection parameters, enabled services, and their load.
Other Kaa nodes use this information to obtain a list of their siblings and communicate with them.
The [Control service]({{root_url}}Glossary/#control-service) uses information about available [Bootstrap services]({{root_url}}Glossary/#bootstrap-service) and their connection parameters during the [SDK]({{root_url}}Glossary/#endpoint-sdk) generation.

### SQL database

An SQL database instance is used to store metadata about [tenants]({{root_url}}Glossary/#tenant), [applications]({{root_url}}Glossary/#kaa-application), [endpoint groups]({{root_url}}Glossary/#endpoint-group), etc.
This information is shared between [endpoints]({{root_url}}Glossary/#endpoint-ep), therefore its volume does not scale up and can be efficiently stored in modern SQL databases.
To support high availability of the Kaa cluster, an SQL database should be also deployed in the cluster mode.

Kaa supports two SQL databases at the moment: PostgresSQL and MariaDB.
If you plan to use Kaa in as a [single node]({{root_url}}Administration-guide/System-installation/Single-node-installation/) instance, PostgreSQL is recommended.
For a multi-node cluster, it is recommended that you use MariaDB because it provides better clusterization capabilities.

### NoSQL database

A NoSQL database instance is used to store information about [endpoint profiles]({{root_url}}Glossary/#endpoint-profile-client-side-server-side), [notifications]({{root_url}}Programming-guide/Key-platform-features/Notifications/), [configurations]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/), etc.
The volume of this information scales linearly with the number of endpoints managed by a particular Kaa cluster instance.
NoSQL database nodes can be co-located with Kaa nodes on the same physical or virtual machines.

Kaa supports Apache Cassandra and MongoDB as a NoSQL database at the moment.
The choice between MongoDB and Apache Cassandra depends solely on your specific data analysis needs.

## Installing Kaa

To install and configure Kaa components on a single Linux node, follow the instructions in [Single node installation]({{root_url}}Administration-guide/System-installation/Single-node-installation/).

To learn how to create a Kaa node cluster, see [Cluster setup guide]({{root_url}}Administration-guide/System-installation/Cluster-setup/).

---
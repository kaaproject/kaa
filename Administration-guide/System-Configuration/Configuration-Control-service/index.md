---
layout: page
title: Control service
permalink: /:path/
nav: /:path/Administration-guide/System-Configuration/Configuration-Control-service
sort_idx: 10
---

* [Introduction](#introduction)
* [Configurations](#configurations)

## Introduction

A Kaa Control service is responsible for managing overall system data, processing API calls from the web UI and external integrated systems, and delivering notifications to Operations servers. A Control service manages data stored in a database (independently for each tenant) and notifies every Operations server on most data updates via a Thrift-based protocol.

A Control server maintains an up-to-date list of available Operations servers by continuously obtaining this information from ZooKeeper.

In addition, Control service provides web UI, which is a standalone component that integrates with the Control server and allows users to create applications, register and configure endpoints, create endpoint groups, etc.

To support high availability, a Kaa cluster must include at least two nodes with control service enabled, with one of them being active and the other(s) being in a standby mode. In case of the active Control service failure, ZooKeeper notifies one of the standby Control service and promotes it to the active Control service.

## Configurations

Kaa administrator can specify thrift hos by editing  ```thrift_host``` property in ```/usr/lib/kaa-node/conf/kaa-node.properties``` file. 

```bash
thrift_host=<ip_of_current_machine>
```


Property ```transport_public_interface``` is used as Bootstrap server host and also this host will be built in generated SDKs. 

```bash
transport_public_interface=<ip_of_current_machine>
```

For the list of available Zookeeper servers responsible ```zk_host_port_list``` property.

```bash
zk_host_port_list=<zookeeper_ip>:<zookeeper_port>
```

For connection to SQL database responsible next properties:

SQL database host and port ```/usr/lib/kaa-node/conf/dao.properties```

```bash
# specify jdbc database host
jdbc_host=<postgresql_ip>
# specify jdbc database post
jdbc_port=<postgresql_port>
```

and in ```/usr/lib/kaa-node/conf/admin-dao.properties``` file.
    
```bash
# specify jdbc database url
jdbc_url=jdbc:postgresql://<postgresql_ip>:<postgresql_port>/kaa
```

Kaa administrator can select NoSQL database between ```mongo``` or ```cassandra``` in ```usr/lib/kaa-node/conf/dao.properties``` file.

```bash
nosql_db_provider_name=<no_sql_database_name>
```

If administrator select MongoDB then he must to populate property ```servers``` in ```/usr/lib/kaa-node/conf/common-dao-mongodb.properties``` file

```bash
servers=<mongo_database_ip>:<mongo_database_port>
```

If he decide to use Cassandra as NoSQL database then he mast to populate property ```node_list``` in ```/usr/lib/kaa-node/conf/common-dao-cassandra.properties``` file

```bash
node_list=<cassandra_database_ip>:<cassandra_database_port>
```



---

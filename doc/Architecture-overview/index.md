---
layout: page
title: Architecture overview
permalink: /:path/
nav: /:path/Customization-guide/Architecture-overview
sort_idx: 15
---

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [High-level architecture](#high-level-architecture)
- [Further reading](#further-reading)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

The Kaa IoT platform consists of the Kaa server and endpoint SDKs. The Kaa cluster implements the back-end part of the platform, exposes integration interfaces, and offers administrative capabilities. 
An endpoint SDK is a library which provides communication, data marshalling, persistence, and other functions available in Kaa for specific type of an endpoint (e.g. Java-based, C++-based, C-based, Objective-C-based). 
This SDK can be used to create Kaa clients, which are any pieces of software that utilize Kaa functionality and are installed on some connected devices. 
It is the responsibility of the Kaa client to process structured data provided by the Kaa server (configuration, notifications, etc.) and to supply data to the return path interfaces (profiles, logs, etc.).

# Architecture in brief

Kaa cluster consists of Kaa nodes that use [Apache ZooKeeper](https://zookeeper.apache.org/) for services coordination. 
Kaa cluster also requires NoSQL and SQL database instances to store endpoint data and metadata accordingly. 
Various client applications communicate with Kaa cluster through different network protocols using Kaa SDK. 

<img src="attach/high-level-architecture.png" width="921" height="1002" style="display: block;margin-left: auto;margin-right: auto;">

## Kaa cluster

The Kaa cluster node is comprised of the Control, Operations, and Bootstrap services. Kaa administrator can enable or disable each of this services individually, see
[general configuration]({{root_url}}/Administration-guide/System-Configuration/General-configuration/).

### Kaa node

#### Control service

A Kaa Control service is responsible for managing overall system data, processing API calls from the web UI and external integrated systems, and delivering notifications to Operations servers. 
A Control service manages data stored in a database (independently for each tenant) and notifies every Operations server on most data updates via a Thrift-based protocol. 
A Control server maintains an up-to-date list of available Operations servers by continuously obtaining this information from ZooKeeper. 
In addition, Control service provides web UI, which is a standalone component that integrates with the Control server and allows users to create applications, register and configure endpoints, create endpoint groups, etc.
To support high availability, a Kaa cluster must include at least two nodes with control service enabled, with one of them being active and the other(s) being in a standby mode. 
In case of the active Control service failure, ZooKeeper notifies one of the standby Control service and promotes it to the active Control service.

#### Operations service

A Kaa Operations service is a “worker” service that is responsible for concurrently handling multiple requests from multiple clients. 
Most common Operations service tasks include endpoint registration, processing endpoint profile updates, configuration updates distribution, and notifications delivery.

Multiple nodes with Operations service enabled may be set up in a Kaa cluster for the purpose of horizontal scaling. 
In this case, all the Operations service will function concurrently. 
In case an Operations service outage happens, the corresponding endpoints switch to the other available Operations services automatically.
A Kaa cluster provides instruments for the workload re-balancing at run time, thus effectively routing endpoints to the less loaded nodes in the cluster.

#### Bootstrap service

A Kaa Bootstrap service is responsible for directing endpoints to Operations services. 
On their part, Kaa endpoints have a built-in list of Bootstrap services set up in the given Kaa deployment. 
The endpoints use this list to query the Bootstrap services and retrieve a list of currently available Operations services from them, as well as security credentials. 
Bootstrap services maintain their lists of available Operations service nodes by coordinating with the ZooKeeper servers.

### Third-party components

#### Zookeeper

[Apache ZooKeeper](https://zookeeper.apache.org/) enables highly reliable distributed coordination of Kaa cluster nodes. 
Each Kaa node push information about connection parameters, enabled services and corresponding services load.
Other Kaa nodes use this information in order to get list of their neighbors and communicate with them. 
Control service uses information about existing Bootstrap services and their connection parameters during SDK generation.

#### SQL database

SQL database instance is used to store metadata about tenants, applications, endpoint groups, etc. 
This information is shared between endpoints, thus it's volume does not scale and it can be efficiently stored in modern SQL databases. 
To support high availability of Kaa cluster, SQL database should be also deployed in cluster mode.
Kaa support [MariaDB](https://mariadb.org/) and [PostgreSQL](https://www.postgresql.org/) as a SQL database at the moment.

#### NoSQL database

NoSQL database instance is used to store information about endpoint profiles, notifications, configurations, etc. 
The volume of this information scales linearly with amount of endpoints that are managed using particular Kaa cluster instance. 
NoSQL database nodes can be co-located with Kaa nodes on the same physical or virtual machines. 
Kaa support [Apache Cassandra](http://cassandra.apache.org/) and [MongoDB](https://www.mongodb.com/) as a NoSQL database at the moment.

### Internode communications

Kaa cluster uses Apache Thirft 
Each node obtains metadata about it's siblings using Apache Zookeeper. This metadata contain information about Thr

### High availability of Kaa node components

There are no single points of failure in Kaa cluster architecture. Kaa Operations and Bootstrap services are identical. 
Single Kaa Control server acts as a leader, however, any Kaa Control server may take leadership in case of leader node failure.
High availability(HA) of Kaa Cluster also depends on HA of SQL and NoSQL databases.

### Scalability

Since Kaa "worker" nodes are identical, Kaa cluster can scale horizontally with linear performance. 
Kaa uses consistent hashing algorithm to address requests to particular business entity that is shared between endpoints.

### Built-in Load Balancing

Kaa SDK choose target worker node randomly during session initialization. 
However, it is possible that nodes in the cluster have different hardware configuration, or new node is added to the cluster during peak load time.
In order to handle such cases, each Kaa node publish information about corresponding services load: endpoint count, load average, etc.
Kaa Control node that acts as a master uses this information to periodically recalculate weights of each node. 
In case of node overload, requests to that node are forwarded to different servers.

## Kaa SDK

A Kaa endpoint is a particular application which uses the Kaa client SDK and resides on a particular connected device. 
The Kaa endpoint SDK provides functionality for communicating with the Kaa server, managing data locally in the client application, as well as provides integration APIs. 
The client SDK abstracts the communication protocol, data persistence, and other implementation details that may be specific for any concrete solution based on Kaa.

# Key Logical Concepts

## Kaa Endpoint (EP)

A Kaa endpoint is a particular application which uses the Kaa client SDK and resides on a particular connected device. 
The Kaa endpoint SDK provides functionality for communicating with the Kaa server, managing data locally in the client application, as well as provides integration APIs. 
The client SDK abstracts the communication protocol, data persistence, and other implementation details that may be specific for any concrete solution based on Kaa.

### EP Profile

The endpoint profile is a customizable structured data set that describes specific characteristics of the endpoint. 
Endpoint profiles are used to classify endpoints into endpoint groups and are comprised of the client-side, server-side and system part. 
The structure of both client-side and server-side of endpoint profile is defined by application developer using the Apache Avro schema format. 
Application developer may reuse and share certain data structures using CTL.

### EP Owner

To exchange events between several endpoints, it is required that those endpoints were attached to the same owner (in other words, registered with the same user). 
Kaa provides necessary APIs to attach/detach endpoints to/from owners through one of the following two flows:

- Owner access token flow
- Endpoint access token flow

### EP Group

Kaa allows for aggregating endpoints related to the same application into endpoint groups. 
The endpoint group represents an independent managed entity which is defined by the profile filters assigned to the group.
Those endpoints whose profiles match the profile filters of the specific endpoint group become automatically registered as members of this group. 
There is no restriction for endpoints on having membership in a number of groups at a time.

## Kaa Instance

A Kaa instance (interchangeable with Kaa deployment) is a particular implementation of the Kaa platform and it consists of a Kaa cluster and endpoints. 
A Kaa cluster represents a number of interconnected Kaa server nodes. An endpoint is an abstraction which represents a separate managed entity within a Kaa deployment. 
Practically speaking, an endpoint is a specific Kaa client registered (or waiting to be registered) within a Kaa deployment. 
For example, a news application installed on your mobile phone, the same news application installed on your tablet, and the same news application on your WiFi-enabled fridge would be considered three different endpoints in Kaa.

## Kaa Application

An application in Kaa represents a family of available implementations of a specific software application used by endpoints. For example, two versions of a sound frequency measuring application which differ by their implementation for, respectively, Arduino and STM32 platforms would be considered the same application in Kaa.

## Multitenancy

Tenant in Kaa is a separate business entity which contains its own endpoints, applications and users.
A single Kaa deployment is able to support multiple tenants, with multiple applications per each tenant. As illustrated in the following diagram, applications belong to tenants, while endpoints register within applications. In addition to tenants, applications and endpoints, there are also users and endpoint groups, which will be described in detail further in this reference.

<img src="attach/logical-concepts.png" width="1382" height="703" style="display: block;margin-left: auto;margin-right: auto;">

# Further reading

Use the following guides and references to make the most of Kaa.

| Guide                                                          | What it is for                                                                                                                                                                                           |
|----------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Key system features]({{root_url}}Programming-guide/Key-system-features/)                           | Use this reference to learn about features and capabilities of Kaa \([Endpoint profiling]({{root_url}}Programming-guide/Key-system-features/Endpoint-profiling/), [Events\*\*\*](#), [Notifications\*\*\*](#), [Logging\*\*\*](#), and other features\). |
| [Installation guide]({{root_url}}Administration-guide/System-installation)                       | Use this guide to install and configure Kaa either on a single Linux node or in a cluster environment.                                                                                                    |
| [Contribute To Kaa]({{root_url}}Customization-guide/How-to-contribute/)                       | Use this guide to learn how to contribute to Kaa project and which code/documentation style conventions we adhere to.                                                                                                   |

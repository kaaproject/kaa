---
layout: page
title: System components overview
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}

* TOC
{:toc}

The Kaa IoT platform consists of the Kaa server, Kaa extensions, and endpoint SDKs (see [Architecture overview]({{root_url}}Architecture-overview/) for more details). 
This guide will provide the overview of key system components from the administration point of view.

## Kaa node

Each Kaa node in cluster runs combination of Control, Operations, and Bootstrap services. 
A system administrator can enable or disable any of the services on a particular node, thus flexibly configuring the cluster deployment (see [general configuration]({{root_url}}Administration-guide/System-Configuration/General-configuration/) for more details).

Kaa Control service is responsible for managing overall system data, processing API calls from the Administration UI and external integrated systems, and delivering corresponding notifications to Operations services. 
Kaa Control service also embeds Administration UI web server.

Kaa Operations service is a “worker” service, the primary role of which is concurrent communication with multiple endpoints. Operations services process endpoint requests and serve them with data.
Kaa Bootstrap service is responsible for distributing Operations services connection parameters to endpoints. Depending on the configured protocol stack, connection parameters may include IP address, TCP port, security credentials, etc. 

Kaa node is distributed as *debian* or *rpm* packages that may be installed on various supported operating systems (see [system installation]({{root_url}}Administration-guide/System-installation/) for more details).


### Third-party components

#### Zookeeper

[Apache ZooKeeper](https://zookeeper.apache.org/) enables highly reliable distributed coordination of Kaa cluster nodes.
Each Kaa node continuously pushes information on connection parameters, enabled services and the corresponding services load.
Other Kaa nodes use this information in order to get list of their siblings and communicate with them.
Active Control service uses information about available Bootstrap services and their connection parameters during the SDK generation.

#### SQL database

SQL database instance is used to store tenants, applications, endpoint groups and other metadata that does not grow with the increase in the amount of endpoints.
Kaa officially supports [MariaDB](https://mariadb.org/) and [PostgreSQL](https://www.postgresql.org/) as embedded SQL databases at the moment.

#### NoSQL database

NoSQL database instance is used to store endpoint-related data that grows linearly with the amount of managed endpoints.
Kaa officially supports [Apache Cassandra](http://cassandra.apache.org/) and [MongoDB](https://www.mongodb.com/) as embedded NoSQL database at the moment.

## High availability (HA)

In order to provide HA of Kaa services, one should deploy topology with at least two Kaa nodes. Each node should act as Control, Operations and Boostrap service simultaniously. 
Apache Zookeeper should be deployed in cluster mode, since it is used for Kaa node coordination.

>**NOTE:**
> Two Kaa nodes is the minimum HA configuration. At least three geo-distributed Kaa nodes is recommended for production usage.

High availability of a Kaa cluster also depends on deploying both SQL and NoSQL databases in HA mode. 
Database nodes can be co-located with Kaa nodes on the same physical or virtual machines.

## Scalability

Multiple nodes with Operations service enabled may be set up in a Kaa cluster for the purpose of horizontal scaling of endpoints requests processing. 
Multiple nodes with Control service enabled provide horizontal scalability of administration REST API calls processing.

## Load Balancing (LB)

LB task can be decoupled into two subtasks based on originator of requests to Kaa cluster: Kaa Endpoint SDK and REST API requests.

#### Kaa Endpoint SDK requests

Kaa SDK choose Bootstrap and Operations service instances pseudo-randomly during session initiation.
However, under the conditions when the cluster is heavily loaded, random endpoints distribution may not be good enough.
Further, as a node joins the cluster, there is a need to re-balance the load across the new cluster set up.
Kaa server uses the active LB approach to instruct some of the endpoints to reconnect to a different Operations service, thus equalizing the load across the nodes.
The algorithm takes servers load information (connected endpoints count, load average, etc.) published by Kaa nodes as an input, and periodically recalculates weights of each node.
Further, the overloaded nodes are instructed to redirect some of the connecting endpoints to a different node.

A similar approach can be used for offloading all of the load from a node subject to a scheduled service, or to gradually migrate the cluster across the physical or virtual machines.
Doing so requires setting of a custom LB strategy by implementing [Rebalancer](https://github.com/kaaproject/kaa/blob/master/server/node/src/main/java/org/kaaproject/kaa/server/control/service/loadmgmt/dynamicmgmt/Rebalancer.java) interface. 
See default [implementation](https://github.com/kaaproject/kaa/blob/master/server/node/src/main/java/org/kaaproject/kaa/server/control/service/loadmgmt/dynamicmgmt/EndpointCountRebalancer.java) for more details.

#### Kaa REST API requests

Existing HTTP(s) LB solutions ([Nginx](https://www.nginx.com/), [AWS Elastic Load balancing](https://aws.amazon.com/elasticloadbalancing/), [Google Cloud LB](https://cloud.google.com/compute/docs/load-balancing-and-autoscaling)),  with sticky session support may be used to provide LB of Administration REST API. 


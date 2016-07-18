---
layout: page
title: Architecture overview
permalink: /:path/
sort_idx: 15
---

{% include variables.md %}

* TOC
{:toc}

First things first, let us take a look at some fundamental concepts necessary for understanding the Kaa architecture and the logical design.

The Kaa IoT platform consists of the *Kaa server*, *Kaa extensions*, and *endpoint SDKs*.

* The Kaa server implements the back-end part of the platform, performs tenants, applications, users, and devices management, exposes integration interfaces, and offers administrative capabilities.
* Kaa extensions are independent software modules that improve the platform functionality.
(In this version of the documentation you will notice some extensions actually managed within the core platform code base. Those are planned to be fully decoupled in future Kaa releases.)
* Endpoint SDK is a library which provides client-side APIs for the various [Kaa platform features]({{root_url}}Programming-guide/Key-platform-features/) and handles communication, data marshalling, persistence, etc.
Kaa SDKs are designed to facilitate the creation of client applications to be run on various connected devices - however, client applications that do not use Kaa endpoint SDK are also possible.
Several implementations of the [Endpoint SDK]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/) are available in different programming languages.

*Kaa cluster* consists of Kaa server nodes that use [Apache ZooKeeper](https://zookeeper.apache.org/) for services coordination.
Kaa cluster also requires NoSQL and SQL database instances to store endpoint data and metadata, accordingly.

<img src="attach/high-level-architecture.png" width="921" height="1002" style="display: block;margin-left: auto;margin-right: auto;">

# Kaa cluster

Kaa nodes in a cluster run a combination of Control, Operations, and Bootstrap services.

## Control service

Kaa Control service is responsible for managing overall system data, processing [API calls]({{root_url}}Programming-guide/Server-REST-APIs/) from the web UI and external integrated systems, and delivering corresponding notifications to Operations services.
Control service maintains an up-to-date list of available Operations services by continuously receiving this information from ZooKeeper.
Additionally, Control service runs embedded Administrative web UI component, which uses Control service APIs to provide platform users with a convenient web-based interface for managing tenants, user accounts, applications, application data, etc.

To support high availability (HA), a Kaa cluster must include at least two nodes with Control service enabled.
In HA mode one of the Control services acts as active and other(s) function in standby mode.
In case of the active Control service failure, ZooKeeper notifies one of the standby Control service and promotes it to the active Control service.

## Operations service

Kaa Operations service is a “worker” service, the primary role of which is concurrent communication with multiple endpoints.
Operations services process endpoint requests and serve them with data.

Multiple nodes with Operations service enabled may be set up in a Kaa cluster for the purpose of horizontal scaling.
In this case, all instances of Operations service will function concurrently.
In case of an Operations service outage, previously connected endpoints switch to other available Operations services automatically.
Kaa server provides instruments for the load re-balancing at run time, thus effectively routing endpoints to the less loaded nodes in the cluster.

## Bootstrap service

Kaa Bootstrap service is responsible for distributing Operations services connection parameters to endpoints.
Depending on the configured protocol stack, connection parameters may include IP address, TCP port, security credentials, etc.
Kaa SDKs contain a pre-generated list of Bootstrap services available in the Kaa cluster that was used to generate the SDK library.
Endpoints query Bootstrap services from this list to retrieve connection parameters for the currently available Operations services.
Bootstrap services maintain their lists of available Operations services by coordinating with ZooKeeper.

## Third-party components

### Zookeeper

[Apache ZooKeeper](https://zookeeper.apache.org/) enables highly reliable distributed coordination of Kaa cluster nodes.
Each Kaa node continuously pushes information on connection parameters, enabled services and the corresponding services load.
Other Kaa nodes use this information in order to get list of their siblings and communicate with them.
Active Control service uses information about available Bootstrap services and their connection parameters during the SDK generation.

### SQL database

SQL database instance is used to store tenants, applications, endpoint groups and other metadata that does not grow with the increase in the amount of endpoints.

High availability of a Kaa cluster is achieved by deploying the SQL database in HA mode.
Kaa officially supports [MariaDB](https://mariadb.org/) and [PostgreSQL](https://www.postgresql.org/) as embedded SQL databases at the moment.

### NoSQL database

NoSQL database instance is used to store endpoint-related data that grows linearly with the amount of managed endpoints.

NoSQL database nodes can be co-located with Kaa nodes on the same physical or virtual machines, and should be deployed in HA mode for the overall high availability of the system.
Kaa officially supports [Apache Cassandra](http://cassandra.apache.org/) and [MongoDB](https://www.mongodb.com/) as embedded NoSQL database at the moment.

## Internode communications

Kaa services use [Apache Thirft](https://thrift.apache.org/) to communicate across processes and nodes.
Each service obtains metadata about its siblings using [Apache ZooKeeper](https://zookeeper.apache.org/).
This metadata contain information about Thrift host and port.

## High availability and scalability

Kaa cluster scales horizontally and linearly; there is no single point of failure in Kaa cluster architecture.
Kaa Operations and Bootstrap services are identical and function in active-active HA mode.
One of the Kaa Control service instances acts as a leader, however, any of the passive ones may be promoted in case of the leader node failure.
High availability of Kaa Cluster also depends on HA of SQL and NoSQL databases.

## Active load balancing

Kaa SDK choose Bootstrap and Operations service instances pseudo-randomly during session initiation (see [Administration guide]({{root_url}}Administration-guide/System-components-overview/) for more details). 

# Endpoint SDK

The Kaa endpoint SDK is a library which provides communication, data marshalling, persistence, and other functions available in Kaa for specific type of an endpoint (e.g. [C-based](Using-Kaa-C-endpoint-SDK), [C++-based](Using-Kaa-Cpp-endpoint-SDK), [Java-based](Using-Kaa-Java-endpoint-SDK), [Android-based](), [Objective-C-based](Using-Kaa-Objective-C-endpoint-SDK)).
The client SDK abstracts the communication protocol, data persistence, and other implementation details that may be specific for any concrete solution based on Kaa.

Endpoint SDK helps to save time on development routine and allows to concentrate on your business logic.


# Kaa instance

<img src="attach/logical-concepts.png" width="1382" height="703" style="display: block;margin-left: auto;margin-right: auto;">

*Kaa instance* (interchangeable with *Kaa deployment*) is a particular installation of the Kaa platform, either as a [single node]({{root_url}}Administration-guide/System-installation/Single-node-installation/), or a [clustered deployment]({{root_url}}Administration-guide/System-installation/Cluster-setup/).

An *application* in Kaa defines the set of data models, the corresponding flows among the endpoints and Kaa server, and processing rules.
Kaa applications are agnostic to the specific target platform, operating system, or the client software implementation.
For example, two firmware implementations for a pressure sensor will differ between Arduino and STM32 platforms, yet will be considered the same application in Kaa, as long as they report identically structured telemetry data.

The Kaa platform is *multi-tenant*, and may support multiple independent business entities out of a single instance.
As illustrated in the preceding diagram, applications belong to tenants, while endpoints register within applications.

An *endpoint* (also commonly shortcut as *EP*), on its part, is an abstraction that represents a separate managed entity within a Kaa deployment.
Practically speaking, an endpoint is a specific Kaa client registered (or waiting to be registered) within a Kaa instance.
Depending on the use case, different level physical entities may be considered as endpoints.
In the industrial setting, a single air quality sensor may represent an individual endpoint, while in a fleet tracking application, a truck (despite carrying on board multiple sensors reporting data) may be a more appropriate entity to be declared as an endpoint.

In order to be able to distinguish endpoints not only by their *IDs*, but also associated properties, Kaa introduces a concept of [*endpoint profiles*]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles/).
Endpoint profile is a custom structured data set that contains characteristics of a specific endpoint within an application.
Profiles are comprised of the *client-side*, *server-side*, and *system* part.
Client-side profile part contents is provided by the endpoint; server-side and system parts are managed by the server.
Structure of client-side or server-side endpoint profile is defined by application developer by defining a corresponding (versioned) *data schema*.
Data schemas play an important role in virtually every aspect of Kaa functionality.

Profile data is used to attribute endpoints to [*endpoint groups*]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups/): independent managed entities defined via *profile filters*.
Those endpoints whose profiles match the profile filters of a specific endpoint group automatically become members of this group. 
There is no restriction on the amount of groups an endpoint may be a member of at the same time.

Endpoints may also be associated with [*owners*]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership/).
Depending on the application, owners may be persons, groups of people, or organizations.

# Further reading

Use the following guides and references to make the most of Kaa.

| Guide                                                          | What it is for                                                                                                                                                                                           |
|----------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Key system features]({{root_url}}Programming-guide/Key-system-features/)                           | Use this reference to learn about features and capabilities of Kaa \([Endpoint profiling]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles/), [Data collection]({{root_url}}Programming-guide/Key-platform-features/Data-collection/), [Configuration management]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/), [Events]({{root_url}}Programming-guide/Key-platform-features/Events/), [Notifications]({{root_url}}Programming-guide/Key-platform-features/Notifications/), and other features\). |
| [Installation guide]({{root_url}}Administration-guide/System-installation)                       | Use this guide to install and configure Kaa either on a single Linux node or in a cluster environment.                                                                                                    |
| [Contribute To Kaa]({{root_url}}Customization-guide/How-to-contribute/)                       | Use this guide to learn how to contribute to Kaa project and which code/documentation style conventions we adhere to.                                                                                                   |
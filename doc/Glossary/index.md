---
layout: page
title: Glossary
permalink: /:path/
sort_idx: 50
---


## A

### Administration UI (admin UI)  
>An online tool for managing Kaa users, applications, schemas, etc.  
>For more information, refer to [Administration guide](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Administration-guide).  
>See also [Web UI](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#web-ui).

### Application  
>An application in Kaa represents a family of available implementations of a specific software application used by [endpoints](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-ep).
>For example, you have two versions of a sound frequency measuring application: one for Arduino platform and one for STM32 platform.
>Despite the difference in their implementation specifics, they are considered the same application in Kaa.  
>For more information, see [Design reference](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    

### Avro UI form  
>A component of the graphical user interface in the [Admin UI](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#administration-ui-admin-ui).
>Use this form to create Kaa schemas and record the data without using the Avro/JSON syntax.  
>For more information, see [Avro UI forms](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/).

### Application token  
>A unique identifier for auto-generated application.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      

## B

### Base schema  
>A derivative schema obtained from the [configuration schema](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#configuration-schema).
>Base schema is used within an application to update the configuration of all [endpoints](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-ep) belonging to the configuration schema.  
>For more information, see [Configuration management](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Key-platform-features/Configuration-management).

### Bootstrap server  
>One of the three server types used in Kaa.
>The other two server types are [Control server](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#control-server) and [Operations server](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#operations-server).
>A Bootstrap server is responsible for directing [endpoints](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-ep) to Operations servers.
>Kaa endpoints have an embedded list of Bootstrap servers set up for certain Kaa deployment.
>The endpoints use this list to query the Bootstrap servers to obtain security credentials and retrieve the list of currently available Operations servers.
>Bootstrap servers maintain their lists of available Operations servers by coordinating with the [ZooKeeper service](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#zookeeper-service) service.  
>For more information, see [Design reference](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/).                                                                                                                                                                                                                                                                                                                               

## C

### Client (Kaa client)  
>A particular [application](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#application) or piece of software embedded into a device.
>Kaa client uses Kaa [endpoint SDK](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-sdk-kaa-sdk) to process structured data provided by the [Kaa server](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#kaa-server) (configuration, notifications, etc.) and to supply data to the return path interfaces (profiles, logs, etc.).  
>For more information, see [Design reference](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 

### Configuration data (configuration)  
>A set of configuration values specified by the user based on the corresponding [configuration schema](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#configuration-schema).
>Once specified on the server and distributed to the [endpoints](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-ep) belonging to a certain [application](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#application), these values support the corresponding configuration schema.  
>For more information, see [Configuration management](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Key-platform-features/Configuration-management).

### Configuration extension  
>Bla...

### Configuration schema  
>A user-defined specification of the [application](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#application) data model that Kaa [Configuration extension](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#configuration-extension) uses to configure [endpoints](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-ep) registered under the application.
>Configuration schema defines the format of the actual [configuration data](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#configuration-data-configuration) to be entered by the user/developer and then transferred to the endpoints.
>There can be multiple schemas created for a single application.
>The version of an endpoint schema is selected during the SDK generation.  
>For more information, see [Configuration management](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Key-platform-features/Configuration-management) and [Administration guide](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Administration-guide).

### Control server  
>One of the three server types in Kaa.
>The other two server types are [Bootstrap server](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#bootstrap-server) and [Operations server](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#operations-server).
>Control servers manage the system data, process API calls from the [web UI](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#web-ui) and external integrated systems, deliver notifications to Operations servers.
>Control servers manage the database independently for each tenant and notify every Operations server on most data updates using a Thrift-based protocol.
>Control servers maintain an up-to-date list of available Operations servers by continuously obtaining this information from the [ZooKeeper service](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#zookeeper-service).
>To support high availability, a [Kaa cluster](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#kaa-cluster) must include at least two Control servers, with one of them being active and the other(s) being in standby mode.
>In case the active Control server fails, ZooKeeper notifies one of the standby Control servers about that and makes the notified server an active one.  
>For more information, see [Design reference](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/).

## D

## E

### Endpoint (EP)  
>An abstract notion used to define a separately managed entity within a [Kaa deployment](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#kaa-instance-kaa-deployment).
>Physically, an endpoint is a programmed device registered or waiting to be registered within a Kaa deployment.
>See also [Kaa client](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#client-kaa-client).  
>For example, a news application installed on your smartphone, the same news application installed on your tablet and on your Wi-Fi-enabled fridge are considered three different endpoints in Kaa.  
>For more information, see [Design reference](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/).

### Endpoint group  
>An individually managed entity defined by the [profile filters](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-group-profile-filter-profile-filter) applied to it.
>All the [endpoints](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-ep) of an endpoint group are automatically registered as members of this group.
>Any endpoint can be a member of multiple groups at the same time.  
>For more information, see [Endpoint groups](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Key-platform-features/Endpoint-groups).

### Endpoint group profile filter (profile filter)  
>A predicate expression based on the Spring Expression Language.
>A profile filter defines the characteristics of the members ([endpoints](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-ep)) in a group.
>To find out whether an [endpoint](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-ep) belongs to a certain group or not, the profile filter checks the [profile](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-profile) of that endpoint.  
>For more information, see [Endpoint profiles](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Key-platform-features/Endpoint-profiles).

### Endpoint profile  
>A structured set of data that describes specific characteristics of an [endpoint](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-ep).
>Endpoint profiles are used to classify endpoints into [endpoint groups](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-group).  
>For more information, see [Endpoint profiles](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Key-platform-features/Endpoint-profiles).

### Endpoint profile schema (profile schema)  
>Profile schema defines the structure of an [endpoint profile](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-profile).
>One profile schema can be used for multiple [endpoints](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-ep).
>Profile schemas use the Apache Avro schema format and support all of the Avro features, such as primitive types, complex types, arrays, maps, etc.  
>For more information, see [Endpoint profiles](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Key-platform-features/Endpoint-profiles).

### Endpoint SDK (Kaa SDK)  
>A service development kit that represents a library used for communication, data marshaling, persistence, and other functions available in Kaa for a specific [endpoint type](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-type) (e.g. Java endpoint SDK, C++ endpoint SDK, C endpoint SDK).
>An SDK is used to create [Kaa clients](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#client-kaa-client) on the connected endpoints of the same type within a [Kaa cluster](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#kaa-cluster).  
>For more information, see [Using Kaa endpoint SDKs](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Using-Kaa-endpoint-SDKs).

### Endpoint type  
>Bla...

### Event  
>A message generated by an [endpoint](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#endpoint-ep).
>Every event is based on a particular [event class](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#event-class-ec) and is generated in compliance with the corresponding [event class schema](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#event-class-schema).  
>For more information, see [Events](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Key-platform-features/Events).

### Event class (EC)  
>A class of schema describing the structure of an [event](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#event).
>An EC is uniquely identified by a fully qualified name (FQN) and a [tenant](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#tenant).
>No tenant can have two ECs with the same FQN.
>For more information, see [Events](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Key-platform-features/Events).

### Event class schema  
>An Avro-based schema that specifies an [event](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#event) structure.
>An event class schema contains the `classType` attribute that supports two values: `event` and `object`.
>Kaa uses the `classType` attribute to distinguish actual events from objects which are reusable parts of events.
>This helps avoid redundant methods in the SDK API.  
>For more information, see [Events](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Key-platform-features/Events).

### Event class family (ECF)  
>A group of [events](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#event) with a similar usage specifics called [event subject](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#event-subject).
>For example, you have a `temperatureCheck` [event class](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#event-class-ec) that returns endpoint temperature and time, and a `statusCheck` event class that returns endpoint temperature and credentials validity.
>In this case, both these event classes share a similar usage specifics which is temperature value.
>Therefore, both these event classes can be grouped under the same ECF.
>ECFs and their corresponding schemas are registered within a Kaa [tenant](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#tenant).
>An ECF is uniquely identified by its name and/or class name, and tenant.
>No tenant can have two ECFs with the same name or class name.  
>For more information, see [Events](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Key-platform-features/Events).

### Event subject
>Describes the usage specifics of an [event](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#event).
>This parameter is used to group [event classes](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#event-class-ec) under the same [event class family](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Glossary/#event-class-family-ecf).

## F

## G

### Group all  
>A default, non-editable group created for each Kaa application.
>The profile filter of this group is automatically set to "true" for every profile schema version in the system.
>The group _all_ includes every endpoint registered in the application.
>This group is used to define the default configuration, default access list for notification topics, and for some other special functions.
>For more information, see [Endpoint groups](http://kaaproject.github.io/kaa/kaa-docs/v0.10.0/Programming-guide/Key-platform-features/Endpoint-groups).

## H

## I

## J

## K

### Kaa admin  
>The highest-level administrator of Kaa.
>He is able to create, edit, and delete tenant admins.
>See also tenant admin and tenant developer.
>Refer to Administration UI guidefor more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      

### Kaa platform    
>The Kaa platform consists of the Kaa server and endpoint SDKs.
>The Kaa server implements the back-end part of the platform, exposes integration interfaces, and offers administrative capabilities.
>An endpoint SDK is a library which provides communication, data marshaling, persistence, and other functions available in Kaa for specific type of an endpoint (e.g. Java-based, C++-based, C-based).
>This SDK can be used to create Kaa clients, which are any pieces of software that utilize Kaa functionality and are installed on some connected devices.
>It is the responsibility of the Kaa client to process structured data provided by the Kaa server (configuration, notifications, etc.) and to supply data to the return path interfaces (profiles, logs, etc.).
>Refer to Design reference main page for more information.

### Kaa instance (Kaa deployment)  
>A particular implementation of the Kaa platform and it consists of a Kaa cluster and endpoints.
>A Kaa cluster represents a number of interconnected Kaa servers.
>An endpoint is an abstraction which represents a separate managed entity within a Kaa deployment.
>Practically speaking, an endpoint is a specific Kaa client registered (or waiting to be registered) within a Kaa deployment.
>For example, a news application installed on your mobile phone, the same news application installed on your tablet, and the same news application on your WiFi-enabled fridge would be considered three different endpoints in Kaa.
>Refer to Design reference main page for more information.                                                                                                                                                                                                                                                                                       

### Kaa cluster
>Bla...

### Kaa server
>Bla...

## L

### Load balancing strategy  
>A particular way of re-balancing workload between the Operations servers within a Kaa cluster to achieve more or less equal load for each server.
>Kaa implements a number of load balancing strategies and automatically uses them at run time.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      

### Log appender  
>A service utility which resides on the Operations server.
>This utility is responsible for writing logs (received by the Operations server from endpoints) to a single specific storage, as defined by the log appender's type.
>Each Kaa application may use only one log appender at a time.
>A Kaa developer is able to add, update and delete log appenders using Admin UI or REST API.
>Kaa provides several default implementations of log appenders.
>It is also possible to create custom log appenders.
>Refer to Log appenders for more information.                                                                                                                                                                                                                                                                                                                                                                                                                              

### Log schema  
>Defines the structure of logs collected by endpoints and subsequently transferred to the server.
>A log schema is fully compatible with the Apache Avro schema.
>There is one log schema defined by default for each Kaa application.
>This schema supports versioning, therefore, whenever a new log schema is configured on the Kaa server for the application, this new schema gets a new sequence version assigned.
>The Kaa server maintains compatibility with the older versions of the log schema to ensure proper functioning of the clients that for some reason are not yet upgraded to the latest schema version.
>Refer to Logging for more information.                                                                                                                                                                                                                                                                                                                     

## M

### Multitenancy  
>A capability of Kaa to serve multiple user groups within a single Kaa instance by providing separate functionality to each of those groups.
>Refer to Design reference main page for more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                

## N

### Notification pipeline  
>Manages individual notifications within a notification topic.
>A notification remains queued in the pipeline until its time-to-live (TTL) expires, after which the notification is dropped.
>The notification pipeline type specifies the scope of notifications delivery.
>It can be either multicast (targeted to an unbounded number of endpoints) or unicast (targeted to a single specific endpoint).
>Refer to Notifications for more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 

### Notification schema  
>Defines the structure of data transferred by corresponding notifications from the server to endpoints.
>Refer to Notifications for more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  

### Notification topic  
>Allows grouping related notifications within the application.
>It is required that every notification in the system be associated with some topic.
>Thus, to receive notifications, every endpoint needs to be subscribed to corresponding topics.
>Refer to Notifications for more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        

## O

### Operations server  
>One of the three server types in Kaa, with the other two being the Bootstrap server and Control server.
>An Operations server is a “worker” server that is responsible for concurrently handling multiple requests from multiple clients.
>Most common Operations server tasks include endpoint registration, endpoint profile updating, configuration updates distribution, and notifications delivery.
>Multiple Operations servers may be set up in a Kaa cluster for the purpose of horizontal scaling.
>In this case, all the Operations servers will function concurrently.
>In case an Operations server outage happens, the corresponding endpoints switch to the other available Operations server automatically.
>A Kaa cluster provides instruments for the workload re-balancing at run time, thus effectively routing endpoints to the less loaded Operations servers in the cluster.
>Refer to Design reference main page for more information.

### Override schema  
>A derivative schema obtained from the configuration schema and used within the given application for updating configuration of a specific endpoint group belonging to that configuration schema.
>Refer to Configuration and its Group-specific configuration management section for more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                

## P

### Profile filter  
>See endpoint group profile filter.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   

### Profile schema  
>See endpoint profile schema.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         

### Protocol schema  
>Determines the structure of the data updates that Kaa server sends to the endpoints and possible update actions (change, add, reset, and leave unchanged).
>A protocol schema is automatically generated by the Control server for each configuration schema (when the configuration schema is loaded by the user).
>Refer to Configuration for more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      

## Q

## R

## S

## T

### Tenant  
>Bla...

### Tenant admin  
>A Kaa user who is responsible for managing applications, users and event class families.
>See also Kaa admin and tenant developer.
>Refer to Administration UI guide for more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             

### Tenant developer  
>A user that creates SDKs based on customer requirements.
>Tenant developers set schemas, group endpoints, and control notification processes.
>See also Kaa admin and tenant admin.
>Refer to Administration UI guide for more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              

### Transport  
>A Kaa server component responsible for communication with a Kaa client.
>Refer to Transports for more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     

### Transport channel  
>A Kaa client component responsible for communication with a Kaa server.
>Refer to Transports for more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     

## U

### User verifier  
>A server component that handles the user verification.
>Refer to User verifiers for more information.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 

## V

## W

### Web UI   
>An online tool for managing Kaa users, applications, schemas, etc.
>Refer to Administration UI guide for more information.
>See also Administration UI.

## X

## Y

## Z                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                

### ZooKeeper service
>Bla...
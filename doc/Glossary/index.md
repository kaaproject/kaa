---
layout: page
title: Glossary
permalink: /:path/
sort_idx: 50
---

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

| [A](#a) | [B](#b) | [C](#c) | [D](#d) | [E](#e) | [F](#f) | [G](#g) | [H](#h) | [I](#i) | [J](#j) | [K](#k) | [L](#l) | [M](#m) | [N](#n) | [O](#o) | [P](#p) | [Q](#q) | [R](#r) | [S](#s) | [T](#t) | [U](#u) | [V](#v) | [W](#w) | [X](#x) | [Y](#y) | [Z](#z) |


<div id="a"/>

## Administration UI (also, Admin UI and web UI)             

An online tool for managing Kaa users, applications, schemas, etc. Refer to [Applications management guide]({{root_url}}Administration-guide/Tenants-and-applications-management/) for more information.

## Application  

The application in Kaa represents a family of available implementations of a specific software application used by endpoints. For example, two versions of a sound frequency measuring application which differ by their implementation for, respectively, Arduino and STM32 platforms would be considered the same application in Kaa. Refer to [System components overview]({{root_url}}Administration-guide/System-components-overview/) for more information.

## Avro UI form                                            

A GUI component in Admin UI that allows the user to either create Kaa schemas or enter corresponding data records, in both cases without using the [Avro/JSON](http://avro.apache.org/docs/current/spec.html) syntax. **TODO:** Refer to somewhere

## Application Token

A unique auto-generated application identifier.

<div id="b"/>

## Base schema                                             

A derivative schema obtained from the configuration schema and used within the given application for updating configuration of all endpoints belonging to that configuration schema. Refer to [Configuration guide]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/) and its [Schema-specific configuration management section]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/#addressing) for more information.

## Bootstrap service                                        

Kaa Bootstrap service is responsible for distributing Operations services connection parameters to endpoints. Depending on the configured protocol stack, connection parameters may include IP address, TCP port, security credentials, etc. Kaa SDKs contain a pre-generated list of Bootstrap services available in the Kaa cluster that was used to generate the SDK library. Endpoints query Bootstrap services from this list to retrieve connection parameters for the currently available Operations services. Bootstrap services maintain their lists of available Operations services by coordinating with ZooKeeper. Refer to [Architecture overview]({{root_url}}Architecture-overview/) for more information.

<div id="c"/>

## Client (also, Kaa client)                                

A particular application or piece of software which uses the Kaa endpoint SDK and resides on a particular connected device. It is the responsibility of the Kaa client to process structured data provided by the Kaa server (configuration, notifications, etc.) and to supply data to the return path interfaces (profiles, logs, etc.). Refer to [System components overview]({{root_url}}Administration-guide/System-components-overview/) for more information.

## Client-side endpoint profile

A user-defined specification of the application data model that Kaa Profiling subsystem uses to allows endpoints change your group dynamic under the application. Client-side endpoint profile can changed in client application. Refer to [Endpoint profiling guide]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles) for more information.

## Configuration schema                                    

A user-defined specification of the application data model that Kaa Configuration subsystem uses to configure endpoints registered under the application. In other words, the configuration schema defines in which format the actual configuration data should be entered by the user/developer and then transferred to the endpoints. Note that there can be several schemas created for a single application. The version of the schema for the endpoint is selected during [SDK generation]({{root_url}}Administration-guide/Tenants-and-applications-management/#generating-endpoint-sdk). Refer to [Configuration guide]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/) for more information.

## Configuration data (in short, configuration)

A set of configuration values specified by the user based on the corresponding configuration schema. Having been specified on the server, these values are then distributed to the endpoints that belong to the corresponding application and support the corresponding configuration schema. Refer to [Configuration guide]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/) for more information.

## Control service                                          

Kaa Control service is responsible for managing overall system data, processing API calls from the web UI and external integrated systems, and delivering corresponding notifications to Operations services. Control service maintains an up-to-date list of available Operations services by continuously receiving this information from ZooKeeper. Additionally, Control service runs embedded Administrative web UI component, which uses Control service APIs to provide platform users with a convenient web-based interface for managing tenants, user accounts, applications, application data, etc. Refer to [Architecture overview]({{root_url}}Architecture-overview/) for more information.

<div id="d"/>

## Delta (also, delta update)

The difference between the new configuration created on the server (and due to be applied to the endpoint) and the current configuration used by the endpoint. The delta is sent to the endpoint and then merged with the endpoint current configuration to achieve the required up-to-date configuration. Using deltas instead of full configuration resets is an effective way to reduce load on available data channels. Refer to [Configuration guide]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/) and its [Endpoint data synchronization section]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/index.md#endpoint-data-synchronization) for more information.
<div id="e"/>

## Endpoint (EP)

An abstraction which represents a separate managed entity within a Kaa deployment. Practically speaking, an endpoint is a specific Kaa client (see [Client](#client-also-kaa-client) registered (or waiting to be registered) within a Kaa deployment. For example, a news application installed on your mobile phone, the same news application installed on your tablet, and the same news application on your WiFi-enabled fridge would be considered three different endpoints in Kaa. Refer to [System components overview]({{root_url}}Administration-guide/System-components-overview/) for more information.

## Endpoint Access Token

Security token that is used during endpoint attachment procedure using endpoint access token flow. Refer to [User verifiers page]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership/) for more information.

## Endpoint group

An independent managed entity which is defined by the profile filters assigned to it. Those endpoints whose profiles match the profile filters of the specific endpoint group become automatically registered as members of this group. There is no restriction for endpoints on having membership in more than one group at a time. Refer to [Endpoint grouping guide]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups) for more information.

## Endpoint group profile filter (in short, profile filter)

A predicate expression based on the [Spring Expression Language](http://docs.spring.io/spring/docs/3.0.x/reference/expressions.html) which defines characteristics of the corresponding group members (endpoints). These filters are executed against the endpoint profile to figure out whether or not the endpoint belongs to the group. Refer to Endpoint profiling for more information. Refer to [Endpoint grouping guide]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups) for more information.

## Endpoint profile schema (in short, profile schema)

Defines an endpoint profile structure. A profile schema can be used across multiple endpoints. It is defined by the Apache Avro schema format and supports all of Avro features: primitive types, complex types, arrays, maps, etc. Refer to [Endpoint profiling guide]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles) for more information.

## Endpoint SDK (also, Kaa SDK)

The Kaa endpoint SDK is a library which provides communication, data marshalling, persistence, and other functions available in Kaa for specific type of an endpoint (e.g. C-based, C++-based, Java-based, Android-based, Objective-C-based). The client SDK abstracts the communication protocol, data persistence, and other implementation details that may be specific for any concrete solution based on Kaa.
Refer to [Endpoint profiling guide]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles) for more information.

## External user id

Identifier by which user can be identified in the external authentication system. Refer to [User verifiers page]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership/) for more information.

## Event

A message generated by the endpoint based on a particular event class and in accordance with the corresponding event class schema. After being generated, an event is sent to the Kaa server for further processing. Refer to [Events guide]({{root_url}}Programming-guide/Key-platform-features/Events) for more information.

## Event class (EC)

A description of an event structure in the form of the event class schema. An EC is uniquely identified by a fully qualified name (FQN) and a tenant. In other words, there can be no two ECs with the same FQN within a single tenant. Refer to [Events guide]({{root_url}}Programming-guide/Key-platform-features/Events) for more information.

## Event class schema

An Avro-based schema used for specifying an event structure. An event class schema contains the **classType** attribute that supports two values: event and object. Kaa uses the **classType** attribute to distinguish actual events from objects, which are reusable parts of events. This is useful for avoiding redundant methods in SDK API. Refer to [Events guide]({{root_url}}Programming-guide/Key-platform-features/Events) for more information.

## Event class family (ECF)

A group of events related together by the event subject. ECFs are registered within the Kaa tenant together with the corresponding event class family schemas. An ECF is uniquely identified by its name and/or class name and tenant. In other words, there can be no two ECFs with the same name or same class name within a single tenant. Refer to [Events guide]({{root_url}}Programming-guide/Key-platform-features/Events) for more information.

<div id="f"/><div id="g"/>

## Group "all"

A default, non-editable group created for each Kaa application. The profile filter of this group is automatically set to "true" for every profile schema version in the system. As a result, the "all" group contains every endpoint registered in the application. The "all" group is used to define the default configuration, default notification topics access list, and for some other special functions. Refer to [Endpoint grouping guide]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups) for more information.

<div id="h"/><div id="i"/><div id="j"/><div id="k"/>

## Kaa admin

The highest-level administrator of Kaa. He is able to create, edit, and delete tenant admins. See also tenant admin and tenant developer. Refer to [Tenants and applications management guide]({{root_url}}Administration-guide/Tenants-and-applications-management/) for more information.

## Kaa framework

The Kaa framework consists of the Kaa server and endpoint SDKs. The Kaa server implements the back-end part of the framework, exposes integration interfaces, and offers administrative capabilities. An endpoint SDK is a library which provides communication, data marshaling, persistence, and other functions available in Kaa for specific type of an endpoint (e.g. Java-based, C++-based, C-based). This SDK can be used to create *Kaa clients*, which are any pieces of software that utilize Kaa functionality and are installed on some connected devices. It is the responsibility of the Kaa client to process structured data provided by the Kaa server (configuration, notifications, etc.) and to supply data to the return path interfaces (profiles, logs, etc.). Refer to [Architecture overview]({{root_url}}Architecture-overview/) for more information.

## Kaa instance (also, Kaa deployment)

A particular implementation of the Kaa framework and it consists of a Kaa cluster and endpoints. A Kaa cluster represents a number of interconnected Kaa servers. An endpoint is an abstraction which represents a separate managed entity within a Kaa deployment. Practically speaking, an endpoint is a specific Kaa client registered (or waiting to be registered) within a Kaa deployment. For example, a news application installed on your mobile phone, the same news application installed on your tablet, and the same news application on your WiFi-enabled fridge would be considered three different endpoints in Kaa. Refer to [Architecture overview]({{root_url}}Architecture-overview/) for more information.

## Kaa Node

A server application part of Kaa Cluster that contains Control, Operations, and Bootstrap services. Every service in Kaa Node can be enabled or disabled.

<div id="l"/>

## Load balancing strategy

A particular way of re-balancing workload between nodes with enabled Operations service within a Kaa cluster to achieve more or less equal load for each node. Kaa implements a number of load balancing strategies and automatically uses them at run time. Refer to [System components overview]({{root_url}}Administration-guide/System-components-overview/) for more information.

## Log appender

A service utility which resides in the Operations service. This utility is responsible for writing logs (received by the Operations server from endpoints) to a single specific storage, as defined by the log appender's type. Each Kaa application may use multiple log appenders at a time. A Kaa developer is able to add, update and delete log appenders using [Admin UI]({{root_url}}Administration-guide/Tenants-and-applications-management/) or [REST API]({{root_url}}Programming-guide/Server-REST-APIs/). Kaa provides several default implementations of log appenders. It is also possible to create custom log appenders. Refer to [Data collection]({{root_url}}Programming-guide/Key-platform-features/Data-collection/) for more information.

## Log schema

Defines the structure of logs collected by endpoints and subsequently transferred to the server. A log schema is fully compatible with the [Apache Avro schema](http://avro.apache.org/docs/current/spec.html#schemas). There is one log schema defined by default for each Kaa application. This schema supports versioning, therefore, whenever a new log schema is configured on the Kaa server for the application, this new schema gets a new sequence version assigned. The Kaa server maintains compatibility with the older versions of the log schema to ensure proper functioning of the clients that for some reason are not yet upgraded to the latest schema version. Refer to [Data collection]({{root_url}}Programming-guide/Key-platform-features/Data-collection/) for more information.

<div id="m"/>

## Multitenancy

A capability of Kaa to serve multiple user groups within a single Kaa instance by providing separate functionality to each of those groups. **TODO:** Refer to somewhere

<div id="n"/>

## Notification pipeline

Manages individual notifications within a notification topic. A notification remains queued in the pipeline until its time-to-live (TTL) expires, after which the notification is dropped. The notification pipeline type specifies the scope of notifications delivery. It can be either multicast (targeted to an unbounded number of endpoints) or unicast (targeted to a single specific endpoint). Refer to Notifications for more information.

## Notification schema

Defines the structure of data transferred by corresponding notifications from the server to endpoints. Refer to [Data collection]({{root_url}}Programming-guide/Key-platform-features/Notifications/) for more information.

## Notification topic

Allows grouping related notifications within the application. It is required that every notification in the system be associated with some topic. Thus, to receive notifications, every endpoint needs to be subscribed to corresponding topics. Refer to [Data collection]({{root_url}}Programming-guide/Key-platform-features/Notifications/) for more information.

<div id="o"/>

## Operations service

Kaa Operations service is a “worker” service, the primary role of which is concurrent communication with multiple endpoints. Operations services process endpoint requests and serve them with data. Multiple nodes with Operations service enabled may be set up in a Kaa cluster for the purpose of horizontal scaling. In this case, all instances of Operations service will function concurrently. In case of an Operations service outage, previously connected endpoints switch to other available Operations services automatically. Kaa server provides instruments for the load re-balancing at run time, thus effectively routing endpoints to the less loaded nodes in the cluster. Refer to [Architecture overview]({{root_url}}Architecture-overview/) for more information.

## Override schema

A derivative schema obtained from the configuration schema and used within the given application for updating configuration of a specific endpoint group belonging to that configuration schema. Refer to [Configuration guide]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/) and its [Group-specific configuration management section]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/#group-specific-configuration-management) for more information.

<div id="p"/>

## Profile filter

See [endpoint group profile filter](#endpoint-group-profile-filter-in-short-profile-filter).

## Profile schema

See [endpoint profile schema](#endpoint-profile-schema-in-short-profile-schema).

## Protocol schema

Determines the structure of the data updates that Kaa server sends to the endpoints and possible update actions (change, add, reset, and leave unchanged). A protocol schema is automatically generated by the Control service for each configuration schema (when the configuration schema is loaded by the user). Refer to [Configuration guide]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/) for more information.

<div id="q"/><div id="r"/><div id="s"/>

## Server-side endpoint profile

A user-defined specification of the application data model that Kaa Profiling subsystem uses to allows endpoints change your group dynamic under the application. Server-side endpoint profile is designed to be accessed and modified by various server-side applications through Kaa REST API integration layer. Server-side profile is not accessible from endpoint SDK or other client application logic. Refer to [Endpoint profiling guide]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles) for more information.

<div id="t"/>

## Tenant

A separate business entity which contains its own endpoints, applications and users.

## Tenant admin

A Kaa user who is responsible for managing applications, users and event class families. See also *Kaa admin* and *tenant developer*. Refer to [Tenants and applications management guide]({{root_url}}Administration-guide/Tenants-and-applications-management/) for more information.

## Tenant developer

A user that creates SDKs based on customer requirements. Tenant developers set schemas, group endpoints, and control notification processes. See also Kaa admin and tenant admin. Refer to [Tenants and applications management guide]({{root_url}}Administration-guide/Tenants-and-applications-management/) for more information.

## Transport

A Kaa server component responsible for communication with a Kaa client. **TODO:** Refer to somewhere

## Transport channel

A Kaa client component responsible for communication with a Kaa server. **TODO:** Refer to somewhere

<div id="u"/>

## User Access Token

Security token that is used during endpoint attachment procedure using user access token flow. Refer to [User verifiers page]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership/) for more information.

## User verifier

A server component that handles the user verification. Refer to [User verifiers page]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership/) for more information.

<div id="v"/><div id="w"/><div id="x"/><div id="y"/><div id="z"/>

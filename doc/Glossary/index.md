---
layout: page
title: Glossary
permalink: /:path/
sort_idx: 50
---


## A

### Actor
>An SDK installed in the [actor gateway]({{root_url}}Glossary/#actor-gateway) as opposed to the [endpoint SDK]({{root_url}}Glossary/#endpoint-sdk).
>The actor communicates with the [Kaa server]({{root_url}}Glossary/#kaa-server) that recognizes it as an endpoint SDK.  
>For more information, see [Using Kaa endpoint SDKs]({{root_url}}Programming-guide/Using-kaa-endpoint-sdks).

### Actor gateway
>A specific type of [Kaa deployment]({{root_url}}Glossary/#kaa-instance-kaa-deployment) used when there is no possibility to install [SDKs in the endpoints]({{root_url}}Glossary/#endpoint-sdk).
>Instead, all SDKs are installed on one machine that represents the actor gateway.
>Each SDK in the gateway communicates with the corresponding endpoint over an arbitrary protocol.
>The SDKs installed on the gateway are called actors.
>The actors communicate with the [Kaa server]({{root_url}}Glossary/#kaa-server) that recognizes them as the endpoint SDKs.

### Administration UI  
>An online tool for managing Kaa users, applications, schemas, etc.
>For more information, see [Administration guide]({{root_url}}Administration-guide).

### Application profile
>A set of structured data describing the [endpoint]({{root_url}}Glossary/#endpoint-ep) devices, installed SDKs, configurations and parameters of a [Kaa application]({{root_url}}Glossary/#kaa-instance-kaa-deployment).
>Application profile is stored and managed on the [Kaa server]({{root_url}}Glossary/#kaa-server).

### Application token  
>An auto-generated unique identifier for a [Kaa application]({{root_url}}Glossary/#kaa-application).
>You can copy from the main window of the [Administration UI]({{root_url}}Glossary/#administration-ui).
>The Administration UI makes a REST call that returns the application token.  
>An application token looks like this: 59116238625740005324

### Avro UI form  
>A component of the graphical user interface in the [Administration UI]({{root_url}}Glossary/#administration-ui).
>Use this form to create Kaa schemas and record the data without using the Avro/JSON syntax.  
>For more information, see [Avro UI forms]({{root_url}}Administration-guide/Tenants-and-applications-management/#avro-ui-forms).

## B

### Bootstrap service  
>One of the three main service types used in Kaa.
>The other two are [Control service]({{root_url}}Glossary/#control-service) and [Operations service]({{root_url}}Glossary/#operations-service).
>A Bootstrap service is responsible for directing [endpoints]({{root_url}}Glossary/#endpoint-ep) to Operations services.
>Kaa endpoints have an embedded list of Bootstrap services set up for certain [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment).
>The endpoints use this list to query the Bootstrap services to obtain security credentials and retrieve the list of currently available Operations services.
>Bootstrap services maintain their lists of available Operations services by coordinating with the ZooKeeper.  
>For more information, see [System configuration]({{root_url}}Administration-guide/System-configuration/).

## C

### Common type (CT)  
>A [Common type library]({{root_url}}Glossary/#Common-type-library-ctl) unit representing a set of data type schema versions.
>One CT can be used for multiple Kaa applications.  
>For more information, see [Common type library]({{root_url}}Programming-guide/Key-platform-features/Common-type-library).

### Common type library (CTL)  
>A repository of data type schemas used for [endpoint profiling]({{root_url}}Glossary/#endpoint-profile).
>As more schema types and versions are created, they are recorded in the CTL for future use.  
>For more information, see [Common type]({{root_url}}Glossary/#common-type-ct).

### Control service  
>One of the three main service types in Kaa.
>The other two are [Bootstrap service]({{root_url}}Glossary/#bootstrap-service) and [Operations service]({{root_url}}Glossary/#operations-service).
>Control services manage the system data, process API calls from the [web UI]({{root_url}}Glossary/#web-ui) and external integrated systems, deliver notifications to Operations services.
>Control services manage the database independently for each tenant and notify every Operations service on most data updates using a Thrift-based protocol.
>Control services maintain an up-to-date list of available Operations services by continuously obtaining this information from the ZooKeeper.
>To support high availability, a [Kaa cluster]({{root_url}}Glossary/#kaa-cluster) must include at least two Control services, with one of them being active and the other(s) being in standby mode.
>In case the active Control service fails, ZooKeeper notifies one of the standby Control services about that and makes the notified service an active one.  
>For more information, see [System configuration]({{root_url}}Administration-guide/System-configuration/).

## D

## E

### Endpoint (EP)  
>A separately managed client-side entity within a [Kaa deployment]({{root_url}}Glossary/#kaa-instance-kaa-deployment).
>Physically, an endpoint is a programmed device registered or waiting to be registered within a Kaa deployment.
>For example, a news application installed on your smartphone, the same news application installed on your tablet and on your Wi-Fi-enabled fridge are considered three different endpoints in Kaa.  
>See also [Kaa client]({{root_url}}Glossary/#kaa-client).  

### Endpoint group  
>An individually managed entity defined by the [profile filters]({{root_url}}Glossary/#endpoint-group-profile-filter-profile-filter) applied to it.
>All the [endpoints]({{root_url}}Glossary/#endpoint-ep) of an endpoint group are automatically registered as members of this group.
>Any endpoint can be a member of multiple groups at the same time.  
>For more information, see [Endpoint groups]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups).

### Endpoint profile (client-side, server-side)  
>A structured set of data that describes specific characteristics of an [endpoint]({{root_url}}Glossary/#endpoint-ep).
>Endpoint profiles are used to classify endpoints into [endpoint groups]({{root_url}}Glossary/#endpoint-group).  
>The values for the client-side endpoint profiles are specified by the the client developer using the Kaa [endpoint SDK]({{root_url}}Glossary/#endpoint-sdk).
>Then, the client-side endpoint profile is generated during registration of a new endpoint.  
>The server-side profile contains the data about endpoint properties that are controlled by your server-side applications.
>For example, client subscription plan, device activation flag, etc.  
>For more information, see [Endpoint profiles]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles).

### Endpoint SDK  
>A software development kit that is a library used for communication, data marshaling, persistence, and other functions performed between an endpoint and [Kaa server]({{root_url}}Glossary/#kaa-server).
>An SDK is used to create [Kaa clients]({{root_url}}Glossary/#kaa-client) on the connected endpoints of the same type within a [Kaa cluster]({{root_url}}Glossary/#kaa-cluster).  
>See also [SDK type]({{root_url}}Glossary/#sdk-type)
>For more information, see [Using Kaa endpoint SDKs]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs).

## F

## G

### Group _all_  
>A default, non-editable group created for each Kaa [application]({{root_url}}Glossary/#kaa-application).
>The _weight_ value of this group equals to 0.
>The [profile filter]({{root_url}}Glossary/#endpoint-group-profile-filter-profile-filter) of this group is automatically set to "true" for every [profile schema]({{root_url}}Glossary/#endpoint-profile-schema-profile-schema) version in the system.
>The group _all_ includes every [endpoint]({{root_url}}Glossary/#endpoint-ep) registered in the application.
>This group is used to define the default [configuration]({{root_url}}Glossary/#configuration-data-configuration), default access list for [notification topics]({{root_url}}Glossary/#notification-topic), and for some other special functions.
>For more information, see [Using endpoint groups]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups/#using-endpoint-groups).

## H

## I

## J

## K

### Kaa administrator  
>The highest-level administrator of Kaa.
>Kaa administrator has the rights to create, edit and delete [tenant admins]({{root_url}}Glossary/#tenant-administrator).  
>See also [tenant administrator]({{root_url}}Glossary/#tenant-administrator) and [tenant developer]({{root_url}}Glossary/#tenant-developer).  
>For more information, see [Administration guide]({{root_url}}Administration-guide).

### Kaa application  
>A [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment) with certain SDKs, extensions, parameters, configurations, etc. on both the [client]({{root_url}}Glossary/#kaa-client) and [server]({{root_url}}Glossary/#kaa-server) sides, deployed for a specific use case.  
>For more information, see [Your first Kaa application]({{root_url}}Programming-guide/Your-first-kaa-application/).

### Kaa client  
>A particular SDK embedded into an [endpoint]({{root_url}}Glossary/#endpoint-ep).
>Kaa client uses Kaa [endpoint SDK]({{root_url}}Glossary/#endpoint-sdk) to process structured data provided by the [Kaa server]({{root_url}}Glossary/#kaa-server) (configuration, notifications, etc.) and to supply data to the return path interfaces (profiles, logs, etc.).  
>For more information, see [Design reference]({{root_url}}).

### Kaa cluster
>A Kaa cluster represents a number of interconnected [Kaa server]({{root_url}}Glossary/#kaa-server) nodes.
>Every Kaa cluster is associated with a particular [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment).

### Kaa instance (Kaa deployment)  
>A particular implementation of the [Kaa platform]({{root_url}}Glossary/#kaa-platform) including [Kaa clusters]({{root_url}}Glossary/#kaa-cluster) and the [endpoints]({{root_url}}Glossary/#endpoint-ep).

### Kaa platform  
>A multi-purpose middleware platform for building complete end-to-end IoT solutions, connected applications, and smart products.
>The Kaa platform comprises [Kaa server]({{root_url}}Glossary/#kaa-server), [endpoint SDKs]({{root_url}}Glossary/#endpoint-sdk), and [Kaa applications]({{root_url}}Glossary/#kaa-application).  
>For more information, see [Key platform features]({{root_url}}Programming-guide/Key-platform-features).

## Kaa Sandbox  
>A preconfigured virtual environment designed for the users who want to use their private [instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment) of [Kaa platform]({{root_url}}Glossary/#kaa-instance-kaa-deployment) for educational, development, and proof-of-concept purposes.
>The Sandbox also includes a selection of demo applications that illustrate various aspects of the platform functionality.  
>For more information, see [Getting started]({{root_url}}Getting-started).

### Kaa server
>Kaa server functions as the back-end part of the [platform]({{root_url}}Glossary/#kaa-platform).
>The server exposes integration interfaces and allows to perform administrative tasks.

## L

### Load balancing strategy  
>A particular way of rebalancing the workload between the [Operations services]({{root_url}}Glossary/#operations-service) within a [Kaa cluster]({{root_url}}Glossary/#kaa-cluster) to achieve more or less equal load for each service.
>Kaa implements a number of load balancing strategies and automatically uses them at run time.

### Log appender  
>A service utility within the [Operations service]({{root_url}}Glossary/#operations-service).
>Operations service receives logs from the [endpoints]({{root_url}}Glossary/#endpoint-ep) and sends them to the log appender.
>Log appender writes the logs to a specific single storage as defined by the log appender type.
>Each [Kaa application]({{root_url}}Glossary/#kaa-application) can use only one log appender at a time.
>A Kaa developer can add, update, and delete log appenders using [Administration UI]({{root_url}}Glossary/#administration-ui) or REST API.
>Kaa provides several default implementations of log appenders.
>You can create custom log appenders.  
>For more information, see [Log appenders]({{root_url}}Customization-guide/Customizable-system-components/Log-appenders).

## M

## N

## O

### Operations service  
>One of the three main service types in Kaa.
>The other two are the [Bootstrap service]({{root_url}}Glossary/#bootstrap-service) and [Control service]({{root_url}}Glossary/#control-service).
>An Operations service concurrently handles multiple [client]({{root_url}}Glossary/#kaa-client) requests.
>Most common Operations service tasks include [endpoint]({{root_url}}Glossary/#endpoint-ep) registration, [endpoint profile]({{root_url}}Glossary/#endpoint-profile-client-side-server-side) update, configuration updates distribution, and notifications delivery.
>To make all the Operation services run concurrently, you can set them in a [Kaa cluster]({{root_url}}Glossary/#kaa-cluster) for horizontal scaling.
>In case of an Operations service outage, the corresponding endpoints automatically switch to the other Operations service available.
>A Kaa cluster is used to [rebalance the workload]({{root_url}}Glossary/#Load-balancing-strategy) at run time, thus effectively routing the endpoints to the less loaded Operations services in the cluster.  
>For more information, see [System configuration]({{root_url}}Administration-guide/System-configuration).

### Owner verifier (user verifier)  
>A server component that handles the user verification.  
>For more information, see [Owner verifiers](Customization-guide/Customizable-system-components/Owner-verifiers).

## P

### Profile filter  
>A predicate expression based on the Spring Expression Language.
>A profile filter defines the characteristics of the member [endpoints]({{root_url}}Glossary/#endpoint-ep) in a group.
>To find out whether an endpoint belongs to a certain group or not, the profile filter checks the [profile]({{root_url}}Glossary/#endpoint-profile) of that endpoint.  
>For more information, see [Endpoint profiles]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles).

## Q

## R

## S

### SDK type  
>SDK type in Kaa is determined by the programming language used to build an SDK.
>Currently, there are four SDK types used in Kaa: Java, C, C++, Objective-C.

## T

### Tenant  
>Owner of a particular [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment).

### Tenant administrator  
>A Kaa user who has the rights to manage [applications]({{root_url}}Glossary/#kaa-applications), users and event class families.  
>See also [Kaa administrator]({{root_url}}Glossary/#kaa-administrator) and [tenant developer]({{root_url}}Glossary/#tenant-developer).  
>For more information, see [Administration guide]({{root_url}}Administration-guide).

### Tenant developer  
>A user that creates SDKs based on the customer requirements.
>Tenant developers set up schemas, [group endpoints]({{root_url}}Glossary/#endpoint-group), and control notification processes.  
>See also [Kaa administrator]({{root_url}}Glossary/#kaa-administrator) and [tenant administrator]({{root_url}}Glossary/#tenant-administrator).  
>For more information, see [Administration guide]({{root_url}}Administration-guide).

### Transport  
>A Kaa server component that communicates with [Kaa clients]({{root_url}}Glossary/#kaa-client).  
>For more information, see [Transport configuration] ({{root_url}}Administration-guide/System-Configuration/Transport-configuration).

## U

## V

## W

## X

## Y

## Z
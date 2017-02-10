---
layout: page
title: Glossary
permalink: /:path/
sort_idx: 50
---

{% include variables.md %}

## A

### Actor
>A virtual entity that operates within the [actor gateway]({{root_url}}Glossary/#actor-gateway) to impersonate a physical device, virtual object, library, etc. as a Kaa [endpoint]({{root_url}}Glossary/#endpoint-ep).

### Actor gateway
>A Kaa-based component that initializes, manages, and uses [actors]({{root_url}}Glossary/#actor) to represent [endpoints]({{root_url}}Glossary/#endpoint-ep) connected to the gateway.
>Deployments with an actor gateway are intended for IoT environments that use proximity or non-Internet communication protocols, and thus have to be bridged to communicate to the [Kaa server]({{root_url}}Glossary/#kaa-server).

### Administration UI
>A web service that implements a user interface for managing Kaa users, [applications]({{root_url}}Glossary/#kaa-application), schemas, etc.

### Application profile
>A certain version of a [Kaa application]({{root_url}}Glossary/#kaa-application) whose behavior is defined by a specific set of [extensions]({{root_url}}Glossary/#kaa-extension) and their configurations.
>One Kaa application can have multiple profiles that are stored as different versions of that application.
>Application profile is stored and managed on the [Kaa server]({{root_url}}Glossary/#kaa-server).

### Application token
>An auto-generated unique identifier for a [Kaa application]({{root_url}}Glossary/#kaa-application).
>You can copy Application token from the main window of the [Administration UI]({{root_url}}Glossary/#administration-ui).  
>See [Your first Kaa application]({{root_url}}Programming-guide/Your-first-Kaa-application/) documentation section.

### Avro UI form
>A component of the graphical user interface in the [Administration UI]({{root_url}}Glossary/#administration-ui).
>Avro UI forms are used to create Kaa data schemas and data records in the visual environment as opposed to JSON syntax.

## B

### Bootstrap service
>One of the three main service types used in Kaa.
>The other two are [Control service]({{root_url}}Glossary/#control-service) and [Operations service]({{root_url}}Glossary/#operations-service).
>A Bootstrap service is responsible for directing [endpoints]({{root_url}}Glossary/#endpoint-ep) to Operations services.
>Kaa endpoints have an embedded list of Bootstrap services set up for certain [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment).
>[Kaa clients]({{root_url}}Glossary/#kaa-client) use a Bootstrap services list for a certain [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment) embedded in the SDK.
>Bootstrap services maintain their lists of available Operations services by coordinating with the ZooKeeper.  
>See [Architecture overview]({{root_url}}Architecture-overview/) documentation section.

## C

### Common type (CT)
>A versioned definition of data schema structure, managed within the [Common type library]({{root_url}}Glossary/#common-type-library-ctl).
>Any CT can be reused in multiple places of [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment).  
>See [Common type library]({{root_url}}Programming-guide/Key-platform-features/Common-Type-Library) documentation section.

### Common type library (CTL)
>A repository of [common types]({{root_url}}Glossary/#common-type-ct) that can be reused in Kaa modules.  
>See [Common type library]({{root_url}}Programming-guide/Key-platform-features/Common-Type-Library) documentation section.

### Control service
>One of the three main service types in Kaa.
>The other two are [Bootstrap service]({{root_url}}Glossary/#bootstrap-service) and [Operations service]({{root_url}}Glossary/#operations-service).
>Control service manages the system data, processes API calls from the [Administration UI]({{root_url}}Glossary/#administration-ui) and external integrated systems, delivers notifications to Operations services.  
>See [Architecture overview]({{root_url}}Architecture-overview/) documentation section.

## E

### Endpoint (EP)
>An independently managed client-side entity within a [Kaa deployment]({{root_url}}Glossary/#kaa-instance-kaa-deployment).
>Kaa represents every managed entity -- device, sensor, mobile phone, etc. -- as an endpoint.  
>See [Architecture overview]({{root_url}}Architecture-overview/) documentation section.

### Endpoint group
>An independently managed group of [endpoints]({{root_url}}Glossary/#endpoint-ep) within a [Kaa application]({{root_url}}Glossary/#kaa-application).
>An endpoint group is defined by a set of [filters]({{root_url}}Glossary/#profile-filter) against the endpoint profile.
>Those endpoints, whose profiles match the filter, are automatically assigned to a group.
>Any endpoint can be a member of multiple groups at the same time.  
>See [Endpoint groups]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups) documentation section.

### Endpoint profile (client-side, server-side)
>A structured set of data that describes specific characteristics of an [endpoint]({{root_url}}Glossary/#endpoint-ep).
>Endpoint profiles are used to classify endpoints into [endpoint groups]({{root_url}}Glossary/#endpoint-group).
>The values for the client-side endpoint profiles are specified by [Kaa client]({{root_url}}Glossary/#kaa-client).  
>The server-side profile contains the data about endpoint properties that are controlled by your server-side applications.
>For example, client subscription plan, device activation flag, etc.  
>See [Endpoint profiles]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles) documentation section.

### Endpoint SDK
>A library used for communication, data marshaling, persistence, and other functions performed between an endpoint and [Kaa server]({{root_url}}Glossary/#kaa-server).
>An SDK is used to create [Kaa clients]({{root_url}}Glossary/#kaa-client) on the connected endpoints of the same [Kaa application]({{root_url}}Glossary/#kaa-application) within a [Kaa cluster]({{root_url}}Glossary/#kaa-cluster).  
>See [Kaa SDK type]({{root_url}}Glossary/#kaa-sdk-type) and [Using Kaa endpoint SDKs]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs) documentation sections.

## G

### Group _all_
>A default, non-editable group created for each Kaa [application]({{root_url}}Glossary/#kaa-application).
>The _weight_ value of this group equals to 0.
>The [profile filter]({{root_url}}Glossary/#profile-filter) of this group is automatically set to **true** for every [profile schema]({{root_url}}Glossary/#endpoint-profile-client-side-server-side) version in the system.
>The group **all** includes every [endpoint]({{root_url}}Glossary/#endpoint-ep) registered in the application.
>See [Using endpoint groups]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups/#using-endpoint-groups) documentation section.

## K

### Kaa administrator
>The highest-level administrator of Kaa instance.
>Kaa administrator has the rights to create, edit and delete [tenant administrators]({{root_url}}Glossary/#tenant-administrator).  
>See also [tenant developer]({{root_url}}Glossary/#tenant-developer).

### Kaa application
>An integrity of configurations and code that utilizes [Kaa platform]({{root_url}}Glossary/#kaa-platform) functionality for a specific use case.  
>See [Your first Kaa application]({{root_url}}Programming-guide/Your-first-Kaa-application/) documentation section.

### Kaa client
>A client-side entity that implements the [endpoint]({{root_url}}Glossary/#endpoint-ep) functionality.
>Kaa client typically uses Kaa [endpoint SDK]({{root_url}}Glossary/#endpoint-sdk) to communicate to [Kaa server]({{root_url}}Glossary/#kaa-server).

### Kaa cluster
>A Kaa cluster represents a number of interconnected [Kaa server]({{root_url}}Glossary/#kaa-server) nodes.
>Every Kaa cluster runs a particular [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment).

### Kaa extension
>An independent software module that implements an isolated set of functions aimed at extending the communication capabilities between [Kaa server]({{root_url}}Glossary/#kaa-server) and [Kaa clients]({{root_url}}Glossary/#kaa-client).

### Kaa instance (Kaa deployment)
>A set of specific [Kaa platform]({{root_url}}Glossary/#kaa-platform) functionality that is logically integrated and managed as a single system.

### Kaa platform
>A multi-purpose middleware platform for building complete end-to-end IoT solutions, connected applications, and smart products.
>The Kaa platform comprises [Kaa server]({{root_url}}Glossary/#kaa-server) and [endpoint SDKs]({{root_url}}Glossary/#endpoint-sdk).  
>See [Key platform features]({{root_url}}Programming-guide/Key-platform-features) documentation section.

### Kaa Sandbox
>A preconfigured virtual environment designed for the users who want to use their private [instance of Kaa platform]({{root_url}}Glossary/#kaa-instance-kaa-deployment) for educational, development, and proof-of-concept purposes.
>The Sandbox also includes a selection of demo applications that illustrate various aspects of the platform functionality.  
>See [Getting started]({{root_url}}Getting-started) documentation section.

### Kaa SDK type
>SDK type in Kaa is determined by the programming language used to build an SDK.
>Currently, there are four SDK types supported in Kaa: [C]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/), [C++]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/), [Objective-C]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/Objective-C/), [Java]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/Java/).  
>See [Using Kaa endpoint SDKs]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/) documentation section.

### Kaa server
>A [Kaa platform]({{root_url}}Glossary/#kaa-platform) component that functions as its back-end part.
>The server exposes integration interfaces and allows performing administrative tasks.  
>See [Architecture overview]({{root_url}}Architecture-overview/) documentation section.

## L

### Load balancing strategy
>A particular way of rebalancing the workload between the [Operations services]({{root_url}}Glossary/#operations-service) within a [Kaa cluster]({{root_url}}Glossary/#kaa-cluster) to achieve the desired load for each service.
>See [Active load balancing]({{root_url}}Architecture-overview/#active-load-balancing) documentation section.

### Log appender
>A service utility within the [Operations service]({{root_url}}Glossary/#operations-service).
>Operations service receives logs from the [endpoints]({{root_url}}Glossary/#endpoint-ep) and sends them to the log appender.
>Log appender writes the logs to a specific single storage as defined by the log appender type and configuration.
>Kaa provides several default implementations of log appenders.
>You can [create custom log appenders]({{root_url}}Customization-guide/Log-appenders).

## O

### Operations service
>One of the three main service types in Kaa.
>The other two are the [Bootstrap service]({{root_url}}Glossary/#bootstrap-service) and [Control service]({{root_url}}Glossary/#control-service).
>The operation service is mainly intended for data exchange and synchronization with [endpoints]({{root_url}}Glossary/#endpoint-ep).  
>See [Architecture overview]({{root_url}}Architecture-overview/) documentation section.

### Owner verifier
>A server component that handles verification of [endpoint owners]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership/).  
>See [Owner verifiers]({{root_url}}Customization-guide/Owner-verifiers) documentation section.

## P

### Profile filter
>A predicate expression based on the Spring Expression Language and designed to filter [endpoint profiles]({{root_url}}Glossary/#endpoint-profile-client-side-server-side).
>By applying profile filters, [Kaa platform]({{root_url}}Glossary/#kaa-platform) identifies which [endpoints]({{root_url}}Glossary/#endpoint-ep) should be attributed to a specific endpoint group.  
>See [profile filters]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups/#profile-filters) documentation section.

## T

### Tenant
>A business entity that uses a part of or a whole [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment) to independently run [Kaa applications]({{root_url}}Glossary/#kaa-application).  
>See [Tenants and applications management]({{root_url}}Administration-guide/Tenants-and-applications-management/) documentation section.

### Tenant administrator
>A Kaa user who has the rights to manage [applications]({{root_url}}Glossary/#kaa-application), users and event class families within a [tenant]({{root_url}}Glossary/#tenant).  
>See also [Kaa administrator]({{root_url}}Glossary/#kaa-administrator) and [tenant developer]({{root_url}}Glossary/#tenant-developer).

### Tenant developer
>A system user that creates and manages [application profiles]({{root_url}}Glossary/#application-profile) based on the customer requirements.
>Tenant developers set up schemas, create [endpoint groups]({{root_url}}Glossary/#endpoint-group), and control [notification]({{root_url}}Programming-guide/Key-platform-features/Notifications/) processes.  
>See also [Kaa administrator]({{root_url}}Glossary/#kaa-administrator) and [tenant administrator]({{root_url}}Glossary/#tenant-administrator).

### Transport
>A component that implements communication between [Kaa clients]({{root_url}}Glossary/#kaa-client) and [Kaa server]({{root_url}}Glossary/#kaa-server).  

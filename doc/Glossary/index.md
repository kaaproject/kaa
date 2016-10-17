---
layout: page
title: Glossary
permalink: /:path/
sort_idx: 50
---


## A

### Actor
>A Kaa entity that can simulate any physical device, virtual object, library, etc. as a terminal endpoint operating within the [actor gateway]({{root_url}}Glossary/#actor-gateway).

### Actor gateway
>A specific type of [Kaa deployment]({{root_url}}Glossary/#kaa-instance-kaa-deployment) that initializes, manages, and uses [actors]({{root_url}}Glossary/#actor) to communicate with the [Kaa server]({{root_url}}Glossary/#kaa-server).
>This type of deployment is intended for IoT environments that use multiple communication protocols.

### Administration UI
>A web service that implements a user interface for managing Kaa users, applications, schemas, etc.
>You can access Administration UI from the [Kaa Sandbox]({{root_url}}Glossary/#kaa-sandbox).

### Application profile
>A certain version of a [Kaa application]({{root_url}}Glossary/#kaa-application) whose behavior is defined by a unique set of the integral parts and their parameters.
>One Kaa application can have multiple profiles that are stored as different versions of that application.
>Application profile is stored and managed on the [Kaa server]({{root_url}}Glossary/#kaa-server).

### Application token
>An auto-generated unique identifier for a [Kaa application]({{root_url}}Glossary/#kaa-application).
>You can copy from the main window of the [Administration UI]({{root_url}}Glossary/#administration-ui).  
>See [Your first Kaa application]({{root_url}}Programming-guide/Your-first-Kaa-application/).

### Avro UI form
>A component of the graphical user interface in the [Administration UI]({{root_url}}Glossary/#administration-ui).
>Use this form to create Kaa schemas and record the data without using the Avro/JSON syntax.  
>See [Avro UI forms]({{root_url}}Administration-guide/Tenants-and-applications-management/#avro-ui-forms).

## B

### Bootstrap service
>One of the three main service types used in Kaa.
>The other two are [Control service]({{root_url}}Glossary/#control-service) and [Operations service]({{root_url}}Glossary/#operations-service).
>A Bootstrap service is responsible for directing [endpoints]({{root_url}}Glossary/#endpoint-ep) to Operations services.
>Kaa endpoints have an embedded list of Bootstrap services set up for certain [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment).
>The endpoints use this list to query the Bootstrap services to obtain security credentials and retrieve the list of currently available Operations services.
>Bootstrap services maintain their lists of available Operations services by coordinating with the ZooKeeper.  
>See [Architecture overview]({{root_url}}Architecture-overview/).

## C

### Common type (CT)
>A schema structure definition stored in multiple versions and managed within the [Common type library]({{root_url}}Glossary/#common-type-library-ctl).
>One CT can be used to create schemas for multiple [versions]({{root_url}}Glossary/#application-profile) and [instances]({{root_url}}Glossary/#kaa-instance-kaa-deployment) of a [Kaa application]({{root_url}}Glossary/#kaa-application).  
>See [Common type library]({{root_url}}Programming-guide/Key-platform-features/Common-type-library).

### Common type library (CTL)
>A repository of [common type]({{root_url}}Glossary/#common-type-ct) versions used for creating schemas.
>As more CTs and CT versions are created, they are recorded in the CTL for future use.  
>See [Common type library]({{root_url}}Programming-guide/Key-platform-features/Common-type-library).

### Control service
>One of the three main service types in Kaa.
>The other two are [Bootstrap service]({{root_url}}Glossary/#bootstrap-service) and [Operations service]({{root_url}}Glossary/#operations-service).
>Control service manages the system data, processes API calls from the [web UI]({{root_url}}Glossary/#web-ui) and external integrated systems, delivers notifications to Operations services.
>This service manages the database independently for each tenant and uses a Thrift-based protocol to notify every Operations service on most data updates.  
>See [Architecture overview]({{root_url}}Architecture-overview/).

## E

### Endpoint (EP)
>An independently managed client-side entity within a [Kaa deployment]({{root_url}}Glossary/#kaa-instance-kaa-deployment).
>This entity is used by [Kaa client]({{root_url}}Glossary/#kaa-client) to communicate with [Kaa server]({{root_url}}Glossary/#kaa-server).
>For example, a news application installed on your smartphone, the same news application installed on your tablet and on your Wi-Fi-enabled fridge are considered three different endpoints in Kaa.  
>See [Architecture overview]({{root_url}}Architecture-overview/).

### Endpoint group
>An independently managed group of [endpoints]({{root_url}}Glossary/#endpoint-ep) within a [Kaa application]({{root_url}}Glossary/#kaa-application).
>Apply [profile filters]({{root_url}}Glossary/#profile-filter) to a group to define the conditions based on which certain endpoints will be classified as the members of this group.
>Any endpoint can be a member of multiple groups at the same time.  
>See [Endpoint groups]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups).

### Endpoint profile (client-side, server-side)  
>A structured set of data that describes specific characteristics of an [endpoint]({{root_url}}Glossary/#endpoint-ep).
>Endpoint profiles are used to classify endpoints into [endpoint groups]({{root_url}}Glossary/#endpoint-group).
>The values for the client-side endpoint profiles are specified by [Kaa client]({{root_url}}Glossary/#kaa-client).  
>The server-side profile contains the data about endpoint properties that are controlled by your server-side applications.
>For example, client subscription plan, device activation flag, etc.  
>See [Endpoint profiles]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles).

### Endpoint SDK
>A library used for communication, data marshaling, persistence, and other functions performed between an endpoint and [Kaa server]({{root_url}}Glossary/#kaa-server).
>An SDK is used to create [Kaa clients]({{root_url}}Glossary/#kaa-client) on the connected endpoints of the same [Kaa application]({{root_url}}Glossary/#kaa-application) within a [Kaa cluster]({{root_url}}Glossary/#kaa-cluster).  
>See [SDK type]({{root_url}}Glossary/#sdk-type) and [Using Kaa endpoint SDKs]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs).

## G

### Group _all_
>A default, non-editable group created for each Kaa [application]({{root_url}}Glossary/#kaa-application).
>The _weight_ value of this group equals to 0.
>The [profile filter]({{root_url}}Glossary/#profile-filter) of this group is automatically set to "true" for every [profile schema]({{root_url}}Glossary/#endpoint-profile-client-side-server-side) version in the system.
>The group _all_ includes every [endpoint]({{root_url}}Glossary/#endpoint-ep) registered in the application.
>See [Using endpoint groups]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups/#using-endpoint-groups).

## K

### Kaa administrator
>The highest-level administrator of Kaa instance.
>Kaa administrator has the rights to create, edit and delete [tenant administrators]({{root_url}}Glossary/#tenant-administrator).  
>See also [tenant developer]({{root_url}}Glossary/#tenant-developer).

### Kaa application
>An independent set of entities and features that utilize [Kaa platform]({{root_url}}Glossary/#kaa-platform) functionality for a specific use case.
>See [Your first Kaa application]({{root_url}}Programming-guide/Your-first-kaa-application/).

### Kaa client
>A client-side entity that implements the [endpoint]({{root_url}}Glossary/#endpoint-ep) functionality.
>Kaa client uses Kaa [endpoint SDK]({{root_url}}Glossary/#endpoint-sdk) to process structured data provided by the [Kaa server]({{root_url}}Glossary/#kaa-server) ([configuration]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/), [notifications]({{root_url}}Programming-guide/Key-platform-features/Notifications/), etc.) and to supply data to the return path interfaces (profiles, logs, etc.).  

### Kaa cluster
>A Kaa cluster represents a number of interconnected [Kaa server]({{root_url}}Glossary/#kaa-server) nodes.
>Every Kaa cluster runs a particular [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment).

### Kaa instance (Kaa deployment)
>A set of specific [Kaa platform]({{root_url}}Glossary/#kaa-platform) functionality running on a [Kaa cluster]({{root_url}}Glossary/#kaa-cluster) and preserving its functional integrity when moved to a different cluster.

### Kaa platform
>A multi-purpose middleware platform for building complete end-to-end IoT solutions, connected applications, and smart products.
>The Kaa platform comprises [Kaa server]({{root_url}}Glossary/#kaa-server), [endpoint SDKs]({{root_url}}Glossary/#endpoint-sdk), and [Kaa applications]({{root_url}}Glossary/#kaa-application).  
>See [Key platform features]({{root_url}}Programming-guide/Key-platform-features).

### Kaa Sandbox
>A preconfigured virtual environment designed for the users who want to use their private [instance of Kaa platform]({{root_url}}Glossary/#kaa-instance-kaa-deployment) for educational, development, and proof-of-concept purposes.
>The Sandbox also includes a selection of demo applications that illustrate various aspects of the platform functionality.  
>See [Getting started]({{root_url}}Getting-started).

### Kaa SDK type
>SDK type in Kaa is determined by the programming language used to build an SDK.
>Currently, there are four SDK types used in Kaa: Java, C, C++, Objective-C.  
>See [Using Kaa endpoint SDKs]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/).

### Kaa server
>A [Kaa platform]({{root_url}}Glossary/#kaa-platform) component that functions as its back-end part.
>The server exposes integration interfaces and allows performing administrative tasks.  
>See [Architecture overview]({{root_url}}Architecture-overview/).

## L

### Load balancing strategy
>A particular way of rebalancing the workload between the [Operations services]({{root_url}}Glossary/#operations-service) within a [Kaa cluster]({{root_url}}Glossary/#kaa-cluster) to achieve more or less equal load for each service.
>See [Active load balancing](Architecture-overview/#active-load-balancing).

### Log appender
>A service utility within the [Operations service]({{root_url}}Glossary/#operations-service).
>Operations service receives logs from the [endpoints]({{root_url}}Glossary/#endpoint-ep) and sends them to the log appender.
>Log appender writes the logs to a specific single storage as defined by the log appender type and configuration.
>Each [Kaa application]({{root_url}}Glossary/#kaa-application) can use only one log appender at a time.
>Kaa provides several default implementations of log appenders.
>You can create custom log appenders.  
>See [Log appenders]({{root_url}}Customization-guide/Customizable-system-components/Log-appenders).

## O

### Operations service
>One of the three main service types in Kaa.
>The other two are the [Bootstrap service]({{root_url}}Glossary/#bootstrap-service) and [Control service]({{root_url}}Glossary/#control-service).
>The operation service is mainly intended for data exchange and synchronization between [endpoints]({{root_url}}Glossary/#endpoint-ep).
>This service is also used to register [endpoints]({{root_url}}Glossary/#endpoint-ep), update [endpoint profiles]({{root_url}}Glossary/#endpoint-profile-client-side-server-side), distribute [configuration]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/) updates, and deliver [notifications]({{root_url}}Programming-guide/Key-platform-features/Notifications/).  
>See [Architecture overview]({{root_url}}Architecture-overview/).

### Owner verifier
>A server component that handles verification of [endpoint owners]({{root_url}}Programming-guide/Key-platform-features/Endpoint-ownership/).  
>See [Owner verifiers]({{root_url}}Customization-guide/Customizable-system-components/Owner-verifiers).

## P

### Profile filter
>A predicate expression based on the Spring Expression Language.
>A profile filter is applied to a group and contains [endpoint profile]({{root_url}}Glossary/#endpoint-profile-client-side-server-side) parameters.
>If there are [endpoints]({{root_url}}Glossary/#endpoint-ep) whose profiles match the parameters contained in the profile filter of a group, those endpoints will become members of that group.  
>See [profile filters]({{root_url}}Programming-guide/Key-platform-features/Endpoint-groups/#profile-filters).

## T

### Tenant
>A business entity that uses a part or a whole [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment) to run multiple [Kaa applications]({{root_url}}Glossary/#kaa-application) independently.  
>See [Tenants and applications management]({{root_url}}Administration-guide/Tenants-and-applications-management/).

### Tenant administrator
>A Kaa user who has the rights to manage [applications]({{root_url}}Glossary/#kaa-applications), users and event class families within a [tenant]({{root_url}}Glossary/#tenant).  
>See also [Kaa administrator]({{root_url}}Glossary/#kaa-administrator) and [tenant developer]({{root_url}}Glossary/#tenant-developer).

### Tenant developer
>A system user that creates and manages [application profiles]({{root_url}}Glossary/#application-profile) based on the customer requirements.
>Tenant developers set up schemas, create [endpoint groups]({{root_url}}Glossary/#endpoint-group), and control [notification]({{root_url}}Programming-guide/Key-platform-features/Notifications/) processes.  
>See also [Kaa administrator]({{root_url}}Glossary/#kaa-administrator) and [tenant administrator]({{root_url}}Glossary/#tenant-administrator).

### Transport
>A [Kaa client]({{root_url}}Glossary/#kaa-client) component that implements communication between [endpoints]({{root_url}}Glossary/#endpoint-ep) and [Kaa server]({{root_url}}Glossary/#kaa-server).  
>See [Transport configuration]({{root_url}}Administration-guide/System-Configuration/Transport-configuration).
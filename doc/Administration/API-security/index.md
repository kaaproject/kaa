---
layout: page
title: API security
permalink: /:path/
sort_idx: 2
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}


## Prerequisites

To get the most out of the below documentation, we recommend that you:

* understand the Kaa platform [microservice-based architecture][architecture overview]
* familiarize yourself with the [OAuth 2.0 Authorization Framework][oauth2] (also, great read [here](https://www.oauth.com/#in-page)).



## Introduction

The Kaa platform is based on multiple microservices that implement various aspects of the overall platform functionality.
Many of these microservices manage resources that require access authentication and authorization.
Kaa API security is implemented based on OAuth 2.0 and the [User-Managed Access (UMA)][uma].

Kaa microservices that require access authorization act as [resource servers][resource server].
The user authentication, access authorization, and resource management is implemented in a separate, OAuth 2.0- and UMA-compliant, auth server.
Kaa uses [KeyCloak][keycloak] as the default auth server implementation.

To perform an operation on a resource, the requesting user must be authenticated with the auth server, and granted the necessary scopes.
All Kaa services support bearer token HTTP authentication scheme on exposed REST APIs.



## Conventions



### Resource naming convention

Protected resources have two unique identifiers in auth server: ID and name.
By convention, Kaa services do not set resource IDs, allowing the auth server to generate IDs as it sees fit.

To ensure the uniqueness of resource names, the following pattern is applied:

```plain
<type>-<handle>
```

where:

* **type** identifies the type of the protected resource (e.g. `application`, `endpoint`, `dashboard`, etc.);
* **handle** is a unique handle of the protected resource, chosen at the discretion of the resource server (e.g. application name, endpoint ID, dashboard UUID, etc.).

Examples of the resource names that follow the above pattern: `application-building`, `endpoint-0aaf85d7-da91-4b46-b6da-dd763ee49c4d`, etc.


### Scope naming convention

[OAuth 2.0 scopes][oauth scope] defined in the Kaa IoT platform consist of at least two tokens separated by colon(s) (`:`).
By convention, the first token designates the type of the protected resource, and the last token identifies the operation type (usually `create`, `read`, `update`, `delete`).
Optional intermediary tokens may be used to further clarify the purpose of the scope in question.
For example:

* `endpoint:read` restricts read access to individual endpoint resources.
* `application:endpoint:create` grants the right to create endpoints within a given application.



## OAuth 2.0 resource types and scopes

Kaa services use several resource types to manage access authorization to corresponding resources.
Below is a summary of existing resource types and their associated [OAuth 2.0 scopes][oauth scope].



### Endpoint resource type

`endpoint` resource type is used for restricting access to Kaa [endpoints][endpoint].
These resources are managed by the [Endpoint Register service (EPR)][EPR].
Scopes are enforced by many of the Kaa platform services, including [EPR][EPR], [EPTS][EPTS], [ECR][ECR], and others.

[Endpoint ID][endpoint-id] is used as a resource handle to construct resource names according to the [resource naming convention](#resource-naming-convention).

| **Scope**         | **Description**                                                                                                |
| ----------------- | -------------------------------------------------------------------------------------------------------------- |
| `endpoint:read`   | Read access to endpoint and its associated data: tokens, metadata attributes, configuration, time-series, etc. |
| `endpoint:update` | Write access to endpoint and its associated data.                                                              |
| `endpoint:delete` | Endpoint delete operation.                                                                                     |



### Application resource type

`application` resource type is used for restricting access to Kaa [applications][application].
These resources are managed by the [Kaa Tekton service][TEKTON].

Application name is used as a resource handle to construct resource names according to the [resource naming convention](#resource-naming-convention).


| **Scope**            | **Description**                                                                            |
| -------------------- | ------------------------------------------------------------------------------------------ |
| `application:read`   | Read access to application, its associated metadata, and service instance configurations.  |
| `application:update` | Write access to application, its associated metadata, and service instance configurations. |
| `application:delete` | Application delete operation.                                                              |


#### Endpoint creation scope

Enforced by the [EPR][EPR].

| **Scope**                     | **Description**                                        |
| ----------------------------- | ------------------------------------------------------ |
| `application:endpoint:create` | Creation of a new Kaa endpoint in a given application. |


#### Endpoint filter management scopes

Enforced by the [EPR][EPR].

| **Scope**                            | **Description**                                                 |
| ------------------------------------ | --------------------------------------------------------------- |
| `application:endpoint-filter:create` | Creation of endpoint filters in a given application.            |
| `application:endpoint-filter:read`   | Read access to endpoint filters created in a given application. |
| `application:endpoint-filter:update` | Update of endpoint filters in a given application.              |
| `application:endpoint-filter:delete` | Delete operation on endpoint filters in a given application.    |


#### Endpoint metadata keys access scope

Enforced by the [EPR][EPR].

| **Scope**                                  | **Description**                                                                      |
| ------------------------------------------ | ------------------------------------------------------------------------------------ |
| `application:endpoints-metadata-keys:read` | Read access to all existing endpoint metadata attribute keys in a given application. |


#### Endpoint commands access scope

Enforced by the [CEX][CEX].

| **Scope**                           | **Description**                                                       |
| ----------------------------------- | --------------------------------------------------------------------- |
| `application:endpoint-command:read` | Read access to all existing endpoint commands in a given application. |


#### Timeseries configuration access scope

Enforced by the [EPTS][EPTS].

| **Scope**                            | **Description**                                                                         |
| ------------------------------------ | --------------------------------------------------------------------------------------- |
| `application:timeseries-config:read` | Read access to all existing endpoint time-series configurations in a given application. |


#### Default endpoint configuration management scopes

Enforced by the [ECR][ECR].

| **Scope**                            | **Description**                                                            |
| ------------------------------------ | -------------------------------------------------------------------------- |
| `application:endpoint-config:read`   | Read access to default endpoint configuration in a given application.      |
| `application:endpoint-config:update` | Write access to default endpoint configuration in a given application.     |
| `application:endpoint-config:delete` | Delete operation on default endpoint configuration in a given application. |


#### Software OTA management scopes

Enforced by the [OTAO][OTAO].

| **Scope**                     | **Description**                                                               |
| ----------------------------- | ----------------------------------------------------------------------------- |
| `application:software:read`   | Read access to over-the-air software definitions in a given application.      |
| `application:software:update` | Write access to over-the-air software definitions in a given application.     |
| `application:software:delete` | Delete operation on over-the-air software definitions in a given application. |



### Dashboard resource type

`dashboard` resource type is used for restricting access to web dashboards.
These resources are managed by the [Kaa Web Dashboard service (WD)][WD].

Dashboard UUID is used as a resource handle to construct resource names according to the [resource naming convention](#resource-naming-convention).

| **Scope**        | **Description**                       |
| ---------------- | ------------------------------------- |
| `dashboard:read` | View access to a given web dashboard. |



### Tenant resource type

`tenant` resource type is used for restricting tenant-wide operations.
One resource of this type (with the name `tenant-system`) is created per KeyCloak authentication realm (created for a Kaa tenant) by the [tenant-manager][TM].

| **Scope**                   | **Description**                    |
| --------------------------- | ---------------------------------- |
| `tenant:application:create` | Creation of a new Kaa application. |


#### Basic client credentials management scopes

The following scopes restrict access to basic (username / password) credentials, enforced by the [Client Credentials Management service][CCM].

| **Scope**                         | **Description**                           |
| --------------------------------- | ----------------------------------------- |
| `tenant:basic-credentials:create` | Creation of new basic client credentials. |
| `tenant:basic-credentials:read`   | Read access to basic client credentials.  |
| `tenant:basic-credentials:update` | Basic client credentials management.      |


#### X.509 client credentials management scopes

The following scopes restrict access to X.509 (TLS certificate) credentials, enforced by the [Client Credentials Management service][CCM].

| **Scope**                        | **Description**                           |
| -------------------------------- | ----------------------------------------- |
| `tenant:x509-credentials:create` | Creation of new X.509 client credentials. |
| `tenant:x509-credentials:read`   | Read access to X.509 client credentials.  |
| `tenant:x509-credentials:update` | X.509 client credentials management.      |



### Kaa resource type

`kaa` resource type is used for restricting platform-wide operations.
Only one resource of this type (with the name `kaa-system`) is created per Kaa platform instance by the Kaa installer.


#### Application management scopes

The following scopes restrict application management access, enforced by the [Kaa Tekton][TEKTON].

| **Scope**                | **Description**                                |
| ------------------------ | ---------------------------------------------- |
| `kaa:application:create` | Creation of a new Kaa application.             |
| `kaa:application:read`   | Bulk read of Kaa application configurations.   |
| `kaa:application:update` | Bulk update of Kaa application configurations. |


#### Tenant configuration access scope

The following scope restricts tenant-specific configuration access, enforced by the [Kaa Tekton][TEKTON].

| **Scope**                       | **Description**                                                           |
| ------------------------------- | ------------------------------------------------------------------------- |
| `kaa:tenant:configuration:read` | Read access to tenant-specific configuration of any tenant in the system. |


#### Tenant management scopes

The following scopes restrict [tenant][TM tenant] operations access, enforced by the [Kaa Tenant Manager][TM].

| **Scope**           | **Description**              |
| ------------------- | ---------------------------- |
| `kaa:tenant:create` | Creation of a new tenant.    |
| `kaa:tenant:read`   | Read access to all tenants.  |
| `kaa:tenant:update` | Write access to all tenants. |
| `kaa:tenant:delete` | Tenant delete operation.     |


#### Tenant user management scopes

The following scopes restrict [tenant user][TM tenant user] management access, enforced by the [Kaa Tenant Manager][TM].

| **Scope**                       | **Description**                |
| ------------------------------- | ------------------------------ |
| `kaa:tenant:tenant-user:create` | Creation of a new tenant user. |
| `kaa:tenant:tenant-user:read`   | Read access to tenant users.   |
| `kaa:tenant:tenant-user:update` | Write access to tenant users.  |
| `kaa:tenant:tenant-user:delete` | Tenant user delete operation.  |


#### Tenant backend client template management scopes

The following scopes restrict management access for [backend client templates][TM backend client template] for tenant realm, enforced by the [Kaa Tenant Manager][TM].

| **Scope**                                         | **Description**                                    |
| ------------------------------------------------- | -------------------------------------------------- |
| `kaa:tenant:realm-backend-client-template:create` | Creation of a new backend client template version. |
| `kaa:tenant:realm-backend-client-template:read`   | Read access to backend client template version.    |
| `kaa:tenant:realm-backend-client-template:update` | Write access to backend client template version.   |
| `kaa:tenant:realm-backend-client-template:delete` | Backend client template version delete operation.  |


#### Tenant frontend client template management scopes

The following scopes restrict management access for [frontend client templates][TM frontend client template] for tenant realm, enforced by the [Kaa Tenant Manager][TM].

| **Scope**                                          | **Description**                                             |
| -------------------------------------------------- | ----------------------------------------------------------- |
| `kaa:tenant:realm-frontend-client-template:create` | Creation of a new frontend client template version.         |
| `kaa:tenant:realm-frontend-client-template:read`   | Read access to frontend client template version.            |
| `kaa:tenant:realm-frontend-client-template:update` | Write access to frontend client template version.           |
| `kaa:tenant:realm-frontend-client-template:delete` | Frontend client template template version delete operation. |


#### Tenant default resource management scopes

The following scopes restrict management access for tenant [default resources][TM default resource], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                            | **Description**                            |
| ------------------------------------ | ------------------------------------------ |
| `kaa:tenant:default-resource:create` | Creation of a new tenant default resource. |
| `kaa:tenant:default-resource:read`   | Read access to tenant default resource.    |
| `kaa:tenant:default-resource:update` | Write access to tenant default resource.   |
| `kaa:tenant:default-resource:delete` | Tenant default resource delete operation.  |


#### Tenant default resource scope mapping management scopes

The following scopes restrict management access for tenant [default resource scope mappings][TM default resource scope mapping], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                                          | **Description**                                          |
| -------------------------------------------------- | -------------------------------------------------------- |
| `kaa:tenant:default-resource-scope-mapping:create` | Creation of a new tenant default resource scope mapping. |
| `kaa:tenant:default-resource-scope-mapping:read`   | Read access to tenant default resource scope mapping.    |
| `kaa:tenant:default-resource-scope-mapping:update` | Write access to tenant default resource scope mapping.   |
| `kaa:tenant:default-resource-scope-mapping:delete` | Tenant default resource scope mapping delete operation.  |


#### Tenant default resource version management scopes

The following scopes restrict management access for tenant [default resource versions][TM default resource version], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                                    | **Description**                                    |
| -------------------------------------------- | -------------------------------------------------- |
| `kaa:tenant:default-resource-version:create` | Creation of a new tenant default resource version. |
| `kaa:tenant:default-resource-version:read`   | Read access to tenant default resource version.    |
| `kaa:tenant:default-resource-version:update` | Write access to tenant default resource version.   |
| `kaa:tenant:default-resource-version:delete` | Tenant default resource version delete operation.  |


#### Tenant default resource version mapping management scopes

The following scopes restrict management access for tenant [default resource version mappings][TM default resource version mapping], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                                            | **Description**                                            |
| ---------------------------------------------------- | ---------------------------------------------------------- |
| `kaa:tenant:default-resource-version-mapping:create` | Creation of a new tenant default resource version mapping. |
| `kaa:tenant:default-resource-version-mapping:read`   | Read access to tenant default resource version mapping.    |
| `kaa:tenant:default-resource-version-mapping:update` | Write access to tenant default resource version mapping.   |
| `kaa:tenant:default-resource-version-mapping:delete` | Tenant default resource version mapping delete operation.  |


#### Tenant identity provider management scopes

The following scopes restrict management access for tenant [identity providers (IDPs)][TM idp], enforced by the [Kaa Tenant Manager][TM].

| **Scope**               | **Description**                             |
| ----------------------- | ------------------------------------------- |
| `kaa:tenant:idp:create` | Creation of a new tenant identity provider. |
| `kaa:tenant:idp:read`   | Read access to tenant identity provider.    |
| `kaa:tenant:idp:update` | Write access to tenant identity provider.   |
| `kaa:tenant:idp:delete` | Tenant identity provider delete operation.  |


#### Tenant realm template management scopes

The following scopes restrict management access for tenant [realm templates][TM realm template], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                          | **Description**                                  |
| ---------------------------------- | ------------------------------------------------ |
| `kaa:tenant:realm-template:create` | Creation of a new tenant realm template version. |
| `kaa:tenant:realm-template:read`   | Read access to tenant realm template version.    |
| `kaa:tenant:realm-template:update` | Write access to tenant realm template version.   |
| `kaa:tenant:realm-template:delete` | Tenant realm template version delete operation.  |


#### Tenant role management scopes

The following scopes restrict management access for tenant [roles][TM role], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                | **Description**                |
| ------------------------ | ------------------------------ |
| `kaa:tenant:role:create` | Creation of a new tenant role. |
| `kaa:tenant:role:read`   | Read access to tenant role.    |
| `kaa:tenant:role:update` | Write access to tenant role.   |
| `kaa:tenant:role:delete` | Tenant role delete operation.  |


#### Tenant role to scope mapping management scopes

The following scopes restrict management access for tenant [role to scope mappings][TM role scope mapping], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                              | **Description**                                 |
| -------------------------------------- | ----------------------------------------------- |
| `kaa:tenant:role-scope-mapping:create` | Creation of a new tenant role to scope mapping. |
| `kaa:tenant:role-scope-mapping:read`   | Read access to tenant role to scope mapping.    |
| `kaa:tenant:role-scope-mapping:update` | Write access to tenant role to scope mapping.   |
| `kaa:tenant:role-scope-mapping:delete` | Tenant role to scope mapping delete operation.  |


#### Tenant role version management scopes

The following scopes restrict management access for tenant [role versions][TM role version], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                        | **Description**                         |
| -------------------------------- | --------------------------------------- |
| `kaa:tenant:role-version:create` | Creation of a new tenant roles version. |
| `kaa:tenant:role-version:read`   | Read access to tenant roles version.    |
| `kaa:tenant:role-version:update` | Write access to tenant roles version.   |
| `kaa:tenant:role-version:delete` | Tenant roles version delete operation.  |


#### Tenant role version mapping management scopes

The following scopes restrict management access for tenant [role version mappings][TM role version mapping], which used for tenant creation, enforced by the [Kaa Tenant Manager][TM].

| **Scope**                                | **Description**                                |
| ---------------------------------------- | ---------------------------------------------- |
| `kaa:tenant:role-version-mapping:create` | Creation of a new tenant role version mapping. |
| `kaa:tenant:role-version-mapping:read`   | Read access to tenant role version mapping.    |
| `kaa:tenant:role-version-mapping:update` | Write access to tenant role version mapping.   |
| `kaa:tenant:role-version-mapping:delete` | Tenant role version mapping delete operation.  |


#### Tenant scope management scopes

The following scopes restrict management access for tenant [scopes][TM scope], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                 | **Description**                 |
| ------------------------- | ------------------------------- |
| `kaa:tenant:scope:create` | Creation of a new tenant scope. |
| `kaa:tenant:scope:read`   | Read access to tenant scope.    |
| `kaa:tenant:scope:update` | Write access to tenant scope.   |
| `kaa:tenant:scope:delete` | Tenant scope delete operation.  |


#### Tenant scope version management scopes

The following scopes restrict management access for tenant [scope versions][TM scope version], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                         | **Description**                         |
| --------------------------------- | --------------------------------------- |
| `kaa:tenant:scope-version:create` | Creation of a new tenant scope version. |
| `kaa:tenant:scope-version:read`   | Read access to tenant scope version.    |
| `kaa:tenant:scope-version:update` | Write access to tenant scope version.   |
| `kaa:tenant:scope-version:delete` | Tenant scope version delete operation.  |


#### Tenant scope version mapping management scopes

The following scopes restrict management access for tenant [scope version mappings][TM scope version mapping], which used for tenant creation, enforced by the [Kaa Tenant Manager][TM].

| **Scope**                                 | **Description**                                 |
| ----------------------------------------- | ----------------------------------------------- |
| `kaa:tenant:scope-version-mapping:create` | Creation of a new tenant scope version mapping. |
| `kaa:tenant:scope-version-mapping:read`   | Read access to tenant scope version mapping.    |
| `kaa:tenant:scope-version-mapping:update` | Write access to tenant scope version mapping.   |
| `kaa:tenant:scope-version-mapping:delete` | Tenant scope version mapping delete operation.  |


#### KeyCloak server management scopes

The following scopes restrict management access for [Keycloak servers][TM keycloak server], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                           | **Description**                    |
| ----------------------------------- | ---------------------------------- |
| `kaa:tenant:keycloak-server:create` | Creation of a new KeyCloak server. |
| `kaa:tenant:keycloak-server:read`   | Read access to KeyCloak server.    |
| `kaa:tenant:keycloak-server:update` | Write access to KeyCloak server.   |
| `kaa:tenant:keycloak-server:delete` | KeyCloak server delete operation.  |


#### Tenant subscription management scopes

The following scopes restrict management access for tenant [subscriptions][TM tenant subscription], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                               | **Description**                        |
| --------------------------------------- | -------------------------------------- |
| `kaa:tenant:tenant-subscription:create` | Creation of a new tenant subscription. |
| `kaa:tenant:tenant-subscription:read`   | Read access to tenant subscription.    |
| `kaa:tenant:tenant-subscription:update` | Write access to tenant subscription.   |
| `kaa:tenant:tenant-subscription:delete` | Tenant subscription delete operation.  |


#### Package type management scopes

The following scopes restrict management access for [packages types][TM package type], enforced by the [Kaa Tenant Manager][TM].

| **Scope**                        | **Description**                 |
| -------------------------------- | ------------------------------- |
| `kaa:tenant:package-type:create` | Creation of a new package type. |
| `kaa:tenant:package-type:read`   | Read access to package type.    |
| `kaa:tenant:package-type:update` | Write access to package type.   |
| `kaa:tenant:package-type:delete` | Package type delete operation.  |


#### UI settings management scope

The UI settings management scope is enforced by Kaa [Web Dashboard][WD].

| **Scope**       | **Description**                                                 |
| --------------- | --------------------------------------------------------------- |
| `kaa:ui:update` | Management of UI settings for all tenants (banner, logos, etc). |


#### API usage statistics scope

Enforced by the [Traffic Statistics Appender][TSA].

| **Scope**                | **Description**                                                    |
| ------------------------ | ------------------------------------------------------------------ |
| `traffic-statistic:read` | Reports on the amount of sent and received data to/from endpoints. |


#### The Things Network integration management scopes

Enforced by the [The Things Network Connector][TTNC].

| **Scope**                                           | **Description**                                                     |
| --------------------------------------------------- | ------------------------------------------------------------------- |
| `tenant:ttn-app-integration:create`                 | Display TTN integration creation button on Kaa UI.                  |
| `tenant:ttn-app-integration:read`                   | Display TTN integration dashboard on Kaa UI.                        |
| `application:ttn-app-integration:create`            | Creation of a new integration between TTN and Kaa applications.     |
| `ttn-app-integration:read`                          | Read access to TTN application integration.                         |
| `ttn-app-integration:update`                        | Write access to TTN application integration.                        |
| `ttn-app-integration:delete`                        | Delete access to TTN application integration.                       |
| `ttn-app-integration:ttn-device-integration:create` | Creation of a new integration between TTN device and Kaa endpoint.  |
| `ttn-device-integration:read`                       | Read access to TTN device integration.                              |
| `ttn-device-integration:update`                     | Write access to TTN device integration.                             |
| `ttn-device-integration:delete`                     | Delete access to TTN device integration.                            |

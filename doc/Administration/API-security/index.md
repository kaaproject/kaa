---
layout: page
title: API security
permalink: /:path/
sort_idx: 10
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


| **Scope**                                  | **Description**                                                                            |
| ------------------------------------------ | ------------------------------------------------------------------------------------------ |
| `application:read`                         | Read access to application, its associated metadata, and service instance configurations.  |
| `application:update`                       | Write access to application, its associated metadata, and service instance configurations. |
| `application:delete`                       | Application delete operation.                                                              |
| `application:endpoint:create`              | Creation of a new Kaa endpoint in a given application.                                     |
| `application:endpoint-filter:create`       | Creation of endpoint filters in a given application.                                       |
| `application:endpoint-filter:read`         | Read access to endpoint filters created in a given application.                            |
| `application:endpoint-filter:update`       | Update of endpoint filters in a given application.                                         |
| `application:endpoint-filter:delete`       | Delete operation on endpoint filters in a given application.                               |
| `application:endpoints-metadata-keys:read` | Read access to all existing endpoint metadata attribute keys in a given application.       |
| `application:endpoint-command:read`        | Read access to all existing endpoint commands in a given application.                      |
| `application:timeseries-config:read`       | Read access to all existing endpoint time-series configurations in a given application.    |
| `application:endpoint-config:read`         | Read access to default endpoint configuration in a given application.                      |
| `application:endpoint-config:update`       | Write access to default endpoint configuration in a given application.                     |
| `application:endpoint-config:delete`       | Delete operation on default endpoint configuration in a given application.                 |
| `application:software:read`                | Read access to over-the-air software definitions in a given application.                   |
| `application:software:update`              | Write access to over-the-air software definitions in a given application.                  |


### Dashboard resource type

`dashboard` resource type is used for restricting access to web dashboards.
These resources are managed by the [Kaa Web Dashboard service (WD)][WD].

Dashboard UUID is used as a resource handle to construct resource names according to the [resource naming convention](#resource-naming-convention).

| **Scope**        | **Description**                       |
| ---------------- | ------------------------------------- |
| `dashboard:read` | View access to a given web dashboard. |


### Kaa resource type

`kaa` resource type is used for restricting platform-wide operations.
Only one resource of this type (with the name `kaa-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                        | **Description**                                         |
| -------------------------------- | ------------------------------------------------------- |
| `kaa:application:create`         | Creation of a new Kaa application.                      |
| `kaa:application:read`           | Bulk read of Kaa application configurations.            |
| `kaa:application:update`         | Bulk update of Kaa application configurations.          |
| `kaa:client-credentials:create`  | Creation of new client username/password credentials.   |
| `kaa:client-credentials:read`    | Read access to client username/password credentials.    |
| `kaa:client-credentials:update`  | Client username/password credentials status management. |
| `kaa:client-certificates:create` | Creation of new client certificates.                    |
| `kaa:client-certificates:read`   | Read access to client certificates.                     |
| `kaa:client-certificates:update` | Client certificates status management.                  |


### Tenant resource type

`tenant` resource type is used for restricting access to Kaa [tenants][tenant] and tenant-wide operations.
Only one resource of this type (with the name `tenant-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                   | **Description**                    |
| --------------------------- | ---------------------------------- |
| `tenant:create`             | Creation of a new tenant.          |
| `tenant:read`               | Read access to tenant.             |
| `tenant:update`             | Write access to tenant.            |
| `tenant:delete`             | Tenant delete operation.           |
| `tenant:application:create` | Creation of a new Kaa application. |


### Tenant user resource type

`tenants-users` resource type is used for restricting access to Kaa tenant users.
Only one resource of this type (with the name `tenants-users-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                     | **Description**                |
| ----------------------------- | ------------------------------ |
| `tenant:tenants-users:create` | Creation of a new tenant user. |
| `tenant:tenants-users:read`   | Read access to tenant user.    |
| `tenant:tenants-users:update` | Write access to tenant user.   |
| `tenant:tenants-users:delete` | Tenant user delete operation.  |


### Keycloak client backend template version resource type

`templates-realm-backend-clients` resource type is used for restricting access to Kaa keycloak client backend template versions.
Only one resource of this type (with the name `templates-realm-backend-clients-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                                       | **Description**                                             |
| ----------------------------------------------- | ----------------------------------------------------------- |
| `tenant:templates-realm-backend-clients:create` | Creation of a new keycloak client backend template version. |
| `tenant:templates-realm-backend-clients:read`   | Read access to keycloak client backend template version.    |
| `tenant:templates-realm-backend-clients:update` | Write access to keycloak client backend template version.   |
| `tenant:templates-realm-backend-clients:delete` | Keycloak client backend template version delete operation.  |


### Keycloak client frontend template version resource type

`templates-realm-frontend-clients` resource type is used for restricting access to Kaa keycloak client frontend template versions.
Only one resource of this type (with the name `templates-realm-frontend-clients-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                                        | **Description**                                              |
| ------------------------------------------------ | ------------------------------------------------------------ |
| `tenant:templates-realm-frontend-clients:create` | Creation of a new keycloak client frontend template version. |
| `tenant:templates-realm-frontend-clients:read`   | Read access to keycloak client frontend template version.    |
| `tenant:templates-realm-frontend-clients:update` | Write access to keycloak client frontend template version.   |
| `tenant:templates-realm-frontend-clients:delete` | Keycloak client frontend template version delete operation.  |


### Keycloak default resource resource type

`default-resources` resource type is used for restricting access to Kaa keycloak default resources.
Only one resource of this type (with the name `default-resources-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                         | **Description**                              |
| --------------------------------- | -------------------------------------------- |
| `tenant:default-resources:create` | Creation of a new keycloak default resource. |
| `tenant:default-resources:read`   | Read access to keycloak default resource.    |
| `tenant:default-resources:update` | Write access to keycloak default resource.   |
| `tenant:default-resources:delete` | Keycloak default resource delete operation.  |


### Keycloak default resource mapping resource type

`default-resources-mappings` resource type is used for restricting access to Kaa keycloak default resource mappings.
Only one resource of this type (with the name `default-resources-mappings-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                                  | **Description**                                      |
| ------------------------------------------ | ---------------------------------------------------- |
| `tenant:default-resources-mappings:create` | Creation of a new keycloak default resource mapping. |
| `tenant:default-resources-mappings:read`   | Read access to keycloak default resource mapping.    |
| `tenant:default-resources-mappings:update` | Write access to keycloak default resource mapping.   |
| `tenant:default-resources-mappings:delete` | Keycloak default resource mapping delete operation.  |


### Keycloak default resource version resource type

`default-resources-versions` resource type is used for restricting access to Kaa keycloak default resource versions.
Only one resource of this type (with the name `default-resources-versions-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                                  | **Description**                                      |
| ------------------------------------------ | ---------------------------------------------------- |
| `tenant:default-resources-versions:create` | Creation of a new keycloak default resource version. |
| `tenant:default-resources-versions:read`   | Read access to keycloak default resource version.    |
| `tenant:default-resources-versions:update` | Write access to keycloak default resource version.   |
| `tenant:default-resources-versions:delete` | Keycloak default resource version delete operation.  |


### Keycloak default resource version mapping resource type

`default-resources-version-mappings` resource type is used for restricting access to Kaa keycloak default resource version mappings.
Only one resource of this type (with the name `default-resources-version-mappings-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                                          | **Description**                                              |
| -------------------------------------------------- | ------------------------------------------------------------ |
| `tenant:default-resources-version-mappings:create` | Creation of a new keycloak default resource version mapping. |
| `tenant:default-resources-version-mappings:read`   | Read access to keycloak default resource version mapping.    |
| `tenant:default-resources-version-mappings:update` | Write access to keycloak default resource version mapping.   |
| `tenant:default-resources-version-mappings:delete` | Keycloak default resource version mapping delete operation.  |


### Keycloak identity provider resource type

`idps` resource type is used for restricting access to Kaa keycloak identity providers.
Only one resource of this type (with the name `idps-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**            | **Description**                               |
| -------------------- | --------------------------------------------- |
| `tenant:idps:create` | Creation of a new keycloak identity provider. |
| `tenant:idps:read`   | Read access to keycloak identity provider.    |
| `tenant:idps:update` | Write access to keycloak identity provider.   |
| `tenant:idps:delete` | Keycloak identity provider delete operation.  |


### Keycloak realm template version resource type

`templates-realms` resource type is used for restricting access to Kaa keycloak realm template versions.
Only one resource of this type (with the name `templates-realms-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                        | **Description**                                    |
| -------------------------------- | -------------------------------------------------- |
| `tenant:templates-realms:create` | Creation of a new keycloak realm template version. |
| `tenant:templates-realms:read`   | Read access to keycloak realm template version.    |
| `tenant:templates-realms:update` | Write access to keycloak realm template version.   |
| `tenant:templates-realms:delete` | Keycloak realm template version delete operation.  |


### Keycloak role resource type

`roles` resource type is used for restricting access to Kaa keycloak roles.
Only one resource of this type (with the name `roles-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**             | **Description**                  |
| --------------------- | -------------------------------- |
| `tenant:roles:create` | Creation of a new keycloak role. |
| `tenant:roles:read`   | Read access to keycloak role.    |
| `tenant:roles:update` | Write access to keycloak role.   |
| `tenant:roles:delete` | Keycloak role delete operation.  |


### Keycloak role scope mapping resource type

`roles-scope-mappings` resource type is used for restricting access to Kaa keycloak role scope mappings.
Only one resource of this type (with the name `roles-scope-mappings-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                            | **Description**                                |
| ------------------------------------ | ---------------------------------------------- |
| `tenant:roles-scope-mappings:create` | Creation of a new keycloak role scope mapping. |
| `tenant:roles-scope-mappings:read`   | Read access to keycloak role scope mapping.    |
| `tenant:roles-scope-mappings:update` | Write access to keycloak role scope mapping.   |
| `tenant:roles-scope-mappings:delete` | Keycloak role scope mapping delete operation.  |


### Keycloak roles version resource type

`roles-versions` resource type is used for restricting access to Kaa keycloak roles versions.
Only one resource of this type (with the name `roles-versions-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                      | **Description**                           |
| ------------------------------ | ----------------------------------------- |
| `tenant:roles-versions:create` | Creation of a new keycloak roles version. |
| `tenant:roles-versions:read`   | Read access to keycloak roles version.    |
| `tenant:roles-versions:update` | Write access to keycloak roles version.   |
| `tenant:roles-versions:delete` | Keycloak roles version delete operation.  |


### Keycloak role version mapping resource type

`roles-versions-mappings` resource type is used for restricting access to Kaa Keycloak role version mappings.
Only one resource of this type (with the name `roles-versions-mappings-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                               | **Description**                                  |
| --------------------------------------- | ------------------------------------------------ |
| `tenant:roles-versions-mappings:create` | Creation of a new keycloak role version mapping. |
| `tenant:roles-versions-mappings:read`   | Read access to keycloak role version mapping.    |
| `tenant:roles-versions-mappings:update` | Write access to keycloak role version mapping.   |
| `tenant:roles-versions-mappings:delete` | Keycloak role version mapping delete operation.  |


### Keycloak scope resource type

`scopes` resource type is used for restricting access to Kaa keycloak scopes.
Only one resource of this type (with the name `scopes-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**              | **Description**                   |
| ---------------------- | --------------------------------- |
| `tenant:scopes:create` | Creation of a new keycloak scope. |
| `tenant:scopes:read`   | Read access to keycloak scope.    |
| `tenant:scopes:update` | Write access to keycloak scope.   |
| `tenant:scopes:delete` | Keycloak scope delete operation.  |


### Keycloak scopes version resource type

`scopes-versions` resource type is used for restricting access to Kaa keycloak scopes versions.
Only one resource of this type (with the name `scopes-versions-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                       | **Description**                            |
| ------------------------------- | ------------------------------------------ |
| `tenant:scopes-versions:create` | Creation of a new keycloak scopes version. |
| `tenant:scopes-versions:read`   | Read access to keycloak scopes version.    |
| `tenant:scopes-versions:update` | Write access to keycloak scopes version.   |
| `tenant:scopes-versions:delete` | Keycloak scopes version delete operation.  |


### Keycloak scope version mapping resource type

`scopes-versions-mappings` resource type is used for restricting access to Kaa keycloak scope version mappings.
Only one resource of this type (with the name `scopes-versions-mappings-system`) is created per Kaa platform instance by the Kaa installer.

`scopes-versions-mappings`

| **Scope**                                | **Description**                                   |
| ---------------------------------------- | ------------------------------------------------- |
| `tenant:scopes-versions-mappings:create` | Creation of a new keycloak scope version mapping. |
| `tenant:scopes-versions-mappings:read`   | Read access to keycloak scope version mapping.    |
| `tenant:scopes-versions-mappings:update` | Write access to keycloak scope version mapping.   |
| `tenant:scopes-versions-mappings:delete` | Keycloak scope version mapping delete operation.  |


### Keycloak server resource type

`keycloak-servers` resource type is used for restricting access to Kaa keycloak servers.
Only one resource of this type (with the name `keycloak-servers-system`) is created per Kaa platform instance by the Kaa installer.

`keycloak-servers`

| **Scope**                        | **Description**                    |
| -------------------------------- | ---------------------------------- |
| `tenant:keycloak-servers:create` | Creation of a new keycloak server. |
| `tenant:keycloak-servers:read`   | Read access to keycloak server.    |
| `tenant:keycloak-servers:update` | Write access to keycloak server.   |
| `tenant:keycloak-servers:delete` | Keycloak server delete operation.  |


### Package type resource type

`package-types` resource type is used for restricting access to Kaa package types.
Only one resource of this type (with the name `package-types-system`) is created per Kaa platform instance by the Kaa installer.

`package-types`

| **Scope**                     | **Description**                 |
| ----------------------------- | ------------------------------- |
| `tenant:package-types:create` | Creation of a new package type. |
| `tenant:package-types:read`   | Read access to package type.    |
| `tenant:package-types:update` | Write access to package type.   |
| `tenant:package-types:delete` | Package type delete operation.  |

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
- understand the Kaa platform [microservice-based architecture][architecture overview]
- familiarize yourself with the [OAuth 2.0 Authorization Framework][oauth2] (also, great read [here](https://www.oauth.com/#in-page)).


## Introduction

The Kaa platform is based on multiple microservices that implement various aspects of the overall platform functionality.
Many of these microservices manage resources that require access authentication and authorization.
Kaa API security is implemented based on OAuth 2.0 and the [User-Managed Access (UMA)][uma].

Kaa microservices that require access authorization act as [resource servers][resource server].
The user authentication, access authorization, and resource management is implemented in a separate, OAuth 2.0- and UMA-compliant, auth server.
Kaa uses [KeyCloak][keycloak] as the default auth server implementation.

To perform an operation on a resource, the requesting user must be authenticated with the auth server, and granted the necessary scopes.
All Kaa services support bearer token HTTP authentication scheme on exposed REST APIs.


## Resource naming convention

Protected resources have two unique identifiers in auth server: ID and name.
By convention, Kaa services do not set resource IDs, allowing the auth server to generate IDs as it sees fit.

To ensure the uniqueness of resource names, the following pattern is applied:
```
<type>-<handle>
```
where:
- **type** identifies the type of the protected resource (e.g. `application`, `endpoint`, `dashboard`, etc.);
- **handle** is a unique handle of the protected resource, chosen at the discretion of the resource server (e.g. application name, endpoint ID, dashboard UUID, etc.).

Examples of the resource names that follow the above pattern: `application-building`, `endpoint-0aaf85d7-da91-4b46-b6da-dd763ee49c4d`, etc.


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
| `application:endpoint-filter:read`         | Read access to endpoint filters created in a given application.                            |
| `application:endpoint-filter:create`       | Creation of endpoint filters in a given application.                                       |
| `application:endpoint-filter:update`       | Update of endpoint filters in a given application.                                         |
| `application:endpoint-filter:delete`       | Delete operation on endpoint filters in a given application.                               |
| `application:endpoints-metadata-keys:read` | Read access to all existing endpoint metadata attribute keys in a given application.       |
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
Only one resource of this type (with name `kaa-system`) is created per Kaa platform instance by the Kaa installer.

| **Scope**                        | **Description**                                         |
| -------------------------------- | ------------------------------------------------------- |
| `kaa:application:create`         | Creation of a new Kaa application.                      |
| `kaa:application:update`         | Bulk update of Kaa application configurations.          |
| `kaa:client-credentials:read`    | Read access to client username/password credentials.    |
| `kaa:client-credentials:create`  | Creation of new client username/password credentials.   |
| `kaa:client-credentials:update`  | Client username/password credentials status management. |
| `kaa:client-certificates:read`   | Read access to client certificates.                     |
| `kaa:client-certificates:create` | Creation of new client certificates.                    |
| `kaa:client-certificates:update` | Client certificates status management.                  |

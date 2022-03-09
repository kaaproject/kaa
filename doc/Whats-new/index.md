---
layout: page
title: What's new
permalink: /:path/
sort_idx: 7
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}


## Kaa 1.4


### Identity and Access Management (IAM)

Identity and Access Management (IAM) feature brings major changes in how user, group, and permission management operate in Kaa.
Below is the short description of changes that the Kaa IAM introduces in Kaa 1.4.


#### User management

With IAM user management you can:

* create new users
* edit created users' details
* activate / deactivate already created users
* grant / revoke user permissions using [policies][policy]
* add / remove users to / from groups

![User management](attach/v1.4/iam-user-management.png)


#### User groups

With IAM groups feature you can:

* create new groups and subgroups with unlimited nesting
* edit group details
* assign / unassign users to / from groups
* grant / revoke group permissions using [policies][policy]

![Group management](attach/v1.4/iam-group-management.png)


#### IAM policies

With IAM policies you can granularly control user / group permissions on the following resources types:

* user
* policy
* group

> The list of resource types that policies may apply to will be expanded in subsequent Kaa releases.
{:.note}

![Policy management](attach/v1.4/iam-policy-management.png)

You can find more information on how the Kaa IAM works [here][IAM].


### Custom widgets

Kaa 1.4 introduces the functionality to create your own widgets using JavaScript.
By default, custom widgets are registered to a specific tenant but can be enabled for all tenants.
Widget developers are not limited to specific frontend technologies or project bundlers.
We provide typings for our REST API clients as well as widget SDK, which are [hosted on npm](https://www.npmjs.com/package/@kaaiot/services) along with open-source examples to showing how to develop custom widgets.

You can get started by following [this tutorial][custom widget tutorial].

![Custom widget](attach/v1.4/registry.svg)

![Custom widgets configuration](attach/v1.4/custom-widgets-configuration.png)


### Public dashboards

Kaa 1.4 allows dashboards to be publicly available so that users can access them without authentication or authorization.
Users can select applications and endpoints to be public and assign additional roles and permissions to a public user.
You can even build in public Kaa dashboads on your own website to showcase the work you have done using Kaa.

![Public dashboard sharing](attach/v1.4/dashboard-sharing.png)

Check out our [public dashboard tutorial][public dashboard tutorial] for more information.


### API usage statistics

Kaa 1.4 introduces reports on the amount of sent and received data to/from [endpoints][endpoint].
Reports can be viewed on the tenant, application, and endpoint levels for the total, 24-hour, and 7-days periods.

You can find more information on this feature in the [Traffic Statistics Appender documentation][TSA], which listens to traffic events and sends them to [OpenDistro][open distro].

![Device traffic stats](attach/v1.4/api-usage-device.png)

![Application traffic stats](attach/v1.4/api-usage-application.png)


### Data types mapping and transformation pipelines

[Starting from 1.2 version](#data-analytics) Kaa is pre-integrated with [Open Distro][open distro] built on top of [Elasticsearch][elasticsearch].
Until now, Elasticsearch document data types were defined dynamically from the incoming telemetry data.
Starting from Kaa 1.4 you can define your own [mappings][elastic mapping], having greater control over how data is indexed and stored in Elasticsearch.

In addition to mappings, there is another large feature built on top of Elasticsearch: ingest pipelines.
Pipelines allow you to perform transformations on your data before indexing.
For example, you can remove fields based on custom conditions, compute some formulas, enrich data with additional values, etc.

The full list of pipeline processors is [here][elastic pipeline processors].

Below is a sample pipeline that processes plaintext data sample (e.g., `{"readings":"t=24 h=41"}`) into JSON and converts Fahrenheit to Celsius using the [script processor][elastic script processor].

![Elastic pipeline example 1](attach/v1.4/elastic-pipeline.png)

Also, as you can see from the above screenshot, you can test your pipeline on sample data in the [application][application] index by choosing **Add a test document from index** checkbox.

Here is another example of conditional transformation of telemetry data.
Based on custom condition it adds a new field to the document.

![Elastic pipeline example 2](attach/v1.4/elastic-pipeline-2.png)


### Analytics level security

The [data types mapping and transformation pipelines](#data-types-mapping-and-transformation-pipelines) feature wouldn't be possible without a new Kaa component called Analytics Security Facade (ASF).
The ASF proxies API requests to [Open Distro][open distro] adding authentication / authorization layer by resolving caller permissions on [Keycloak][keycloak].

Read more about the ASF service [here][ASF].


### Built in analytics widgets

Kaa 1.4 introduces time series chart, polar chart, and gauge widgets integrated with the analytics engine.
New widgets support aggregations, math operations, and rich customization thanks to the [ECharts][echarts] library.

![Analytics widgets](attach/v1.4/analytics-widgets.png)

![Analytics chart](attach/v1.4/analytics-chart.png)

![Analytics gauge and polar widgets](attach/v1.4/analytics-gauge-and-polar.png)


### Batch command execution based on endpoint filters

Kaa 1.4 allows you to execute a single command on a set of endpoints that match a specific [endpoint filter][endpoint-filter].
For example, you can define an endpoint filter by geolocation and send a command to these endpoints in one [REST API call][CEX REST API POST batch command], specifying the filter ID and the command payload.
Also, you can use new widgets on [Kaa UI][WD] to run and list previously executed batch commands.

![Batch command execution](attach/v1.4/batch-command-execution.png)

![Batch commands history](attach/v1.4/batch-commands-history.png)

Adding batch command execution widgets to dashboards.

![Batch commands history widget adding](attach/v1.4/batch-commands-history-widget-adding.png)

![Batch command widget setting](attach/v1.4/batch-command-widget-setting.png)

See the [Command Execution service documentation][CEX batch command overview] to find out how batch execution works.


### Attribute Dictionary Extension (ADX)

New [Attribute Dictionary Extension (ADX)][ADX] service functions similarly to other Kaa platform [extension services][extension], such as [DCX][DCX], [EPMX][EPMX] [CMX][CMX], etc.
Connected clients are able to query application-specific attributes using a request-response protocol based on existing transport protocols supported by the Kaa platform: 1/KP, MQTT, HTTP, and their flavors.
ADX sources application-specific key-value attributes to serve from its own configuration, which could be loaded from a config file or from Kaa [Tekton service][TEKTON].
The extension accepts client requests with attribute paths that support the [GJSON][gjson] syntax.

Read more about the ADX service [here][ADX].


### Other highlights of Kaa 1.4

* [**[WD]**][WD] Added Air quality monitoring solution template
* [**[WD]**][WD] Date range control redesigned
* [**[WD]**][WD] Support for actions, multi navigation and time series data in the Endpoint List widget
* [**[WD]**][WD] Support for passing endpoint ID from dashboard variables
* [**[WD]**][WD] Map widget supports default zoom customization
* [**[WD]**][WD] Fixed permission detection for configuration widgets and endpoint token status widget
* [**[WD]**][WD] Added possibility to customize solution logo displayed in public dashboard
* [**[WD]**][WD] Option to switch between gauge types in widget configuration
* [**[WD]**][WD] Option to hide widgets based on endpoint metadata or time series value
* [**[WD]**][WD] Option to use dashboard variables and theme in Raw HTML widget
* [**[WD]**][WD] Raw HTML widget supports advanced templating such as loops, conditions, and helpers
* [**[WD]**][WD] Raw HTML widget supports internal links navigation without a page refresh
* [**[CEX]**][CEX] "Expired" command status added.
  Commands expire when failed to complete execution within the `commandTtl` period.
* [**[CEX]**][CEX] `createdAt` and `updatedAt` command related fields now has ISO 8601 format in UTC timezone in [REST API][CEX REST API] responses.
  <!-- TODO: Time format in `CommandDescriptorDto` was changed to universal format for EPR-CEX API - Timestamp in ISO 8601 format (UTC timezone). -->
* [**[CM]**][CM] [**[CMX]**][CMX] [**[EPMX]**][EPMX] [**[EPR]**][EPR] [**[EPL]**][EPL] [**[KDCA]**][KDCA] [**[ECR]**][ECR] [**[CEX]**][CEX] [**[OTAO]**][OTAO] Added PAT token cache to speed up inter-service communication.
* [**[CM]**][CM] Added Grafana dashboard with HTTP server error responses, REST API request processing time, application-specific configuration update time, and JVM threads state monitoring.
* [**[CM]**][CM] [**[CEX]**][CEX] [**[EPR]**][EPR] [**[ECR]**][ECR] Added ability to granularly enable authentication without authorization.
* [**[DCX]**][DCX] is now able to handle plain text data like `21`, `55%`, `2061m`, etc. on `/plain/${metric-name}` resource path.
  Devices are no longer required to send telemetry in JSON format.
  More info [here]({{dcx_url}}#/Plain-data-handling).
* [**[DCX]**][DCX] now reports message processing time to Prometheus excluding the message queuing wait time, which makes it easier to track the two metrics in isolation.
* [**[CM]**][CM] [**[CMX]**][CMX] [**[EPMX]**][EPMX] [**[EPR]**][EPR] [**[EPL]**][EPL] [**[KDCA]**][KDCA] [**[ECR]**][ECR] [**[CEX]**][CEX] [**[OTAO]**][OTAO] [**[CCM]**][CCM] [**[KPC]**][KPC] [**[DXC]**][DCX] [**[EPTS]**][EPTS] [**[BCX]**][BCX] [**[Tekton]**][Tekton] services now use cluster internal Keycloak DNS name to issue PAT tokens.
  This allows platform services to start without depending on KeyCloak's public DNS.
* [**[EPR]**][EPR] Fixed `Location` header with duplicated endpoint resource location link.
* [**[EPR]**][EPR] now supports batch endpoint creation.
  You can create up to 1000 endpoints with a single REST API request.
* [**[EPR]**][EPR] Added Grafana dashboard with HTTP server error responses, REST API request processing time, application-specific configuration update time, JVM threads state monitoring, and current active requests.
* [**[CM]**][CM] [**[CMX]**][CMX] [**[EPMX]**][EPMX] [**[EPR]**][EPR] [**[EPL]**][EPL] [**[KDCA]**][KDCA] [**[ECR]**][ECR] [**[CEX]**][CEX] [**[OTAO]**][OTAO] Fix services start when specifying an external config file.
* [**[CM]**][CM] [**[CMX]**][CMX] [**[EPMX]**][EPMX] [**[EPR]**][EPR] [**[EPL]**][EPL] [**[KDCA]**][KDCA] [**[ECR]**][ECR] [**[CEX]**][CEX] [**[OTAO]**][OTAO] Java upgraded to version 11.
* [**[CM]**][CM] [**[EPR]**][EPR] [**[OTAO]**][OTAO] [**[CEX]**][CEX] Configured monitoring dashboards in Grafana.


## Kaa 1.3 (February 17-th, 2021)

Find below high-level descriptions of some of the major release highlights.


### Branding

Kaa 1.3 supports [branding customization][WD Branding] for the platform and per tenant.
![branding overview](attach/v1.3/branding-overview.jpg)
It is possible to set up a company's unique color scheme, add logo and icons, and change styling to match the branded palette.
The following customization options are now available:

* Company name
* Logo
  ![logo and company name configuration](attach/v1.3/branding-logo-configuration.jpg)
* Favicon
  ![favicon configuration](attach/v1.3/branding-favicon-configuration.jpg)
* Login page styling as well as theme colors used within the application
  ![login page configuration](attach/v1.3/branding-login-configuration.jpg)
* Color schema
  ![login page configuration](attach/v1.3/branding-color-schema.jpg)


### File management

Kaa 1.3 supports the sharing of uploaded or downloaded files with other users and devices.
These [file management][WD File management] capabilities are enabled by the open-source storage called [MinIO][minio].
All binary and file blobs used in WD configuration, such as branding, dashboards, and widgets, are stored in a bucket named by the tenant id.
Each tenant has two buckets, one of which is configured to allow public read access.
Minio UI and its file management functions can be accessed from the WD admin sidebar, which features a direct link.

![Minio UI](attach/v1.3/minio-overview.jpg)


### Disaster recovery plan

Kaa 1.3 introduces a Disaster Recovery Plan (DRP) by implementing backup and restore procedures.
By default, Kaa automatically backs up itself on a daily basis and uploads snapshots to an AWS S3 bucket related to the particular deployment.
Using the snapshots it is possible to restore the platform to a specific state in time.

Find more info on how Disaster Recovery Plan works [here][platform backup].


### Migration to Helm 3

In the Kaa 1.3 release, all Kaa services were migrated to [Helm 3][helm3].
You can use the improvements and features of this version, such as support of library charts, the improved Upgrade strategy, validation of chart values with JSONSchema and others.
More information about Helm 3 features and changes you can find at [changes since Helm 2][helm3-changes].


### Solution templates

Kaa 1.3 provides 3 templates with preconfigured services, configurations and dashboards to provision a fully featured, end-to-end solution.
Each template provides a simulator example that describes main protocols to communicate with the platform features, such as data collection, metadata management, command execution, and device configuration.

![Run solution](attach/v1.3/run-solution.gif)


### Request Status Extension

Kaa 1.3 has a new service called Request Status Extension that allows connected clients to retrieve the status of [1/KP-based][1/KP] message by its [Request ID][1/KP request ID].
Suppose the client published telemetry data with the request ID `67` and immediately restarted, thus having no time to handle the platform's response for the message with that request ID.
After the restart, the client should somehow know whether it should republish the telemetry data or not.
It can be done by requesting the last known status code and reason phrase for the given request ID `67` using the extension protocol implemented by [RSX][RSX].

Find more info on how to use [RSX here][RSX].


### Other highlights of Kaa 1.3

* [**[CMX]**][CMX] In previous Kaa versions, CMX would send configuration applied messages to the configuration repository, e.g., [ECR][ECR], regardless of whether the config was applied by an endpoint or not.
Starting with Kaa 1.3, CMX no longer automatically sends configuration applied messages.
Instead, CMX sends only those apply messages that were explicitly initiated by an endpoint.
* [**[WD]**][WD] added form control to upload files.
* [**[WD]**][WD] added unit converting fields for Label, Single Number widgets.
* [**[WD]**][WD] responsive layout for mobile and tablet devices.
* [**[WD]**][WD] added Single number digital view.
* [**[WD]**][WD] fixed security vulnerabilities in Raw HTML widget, Dashboard titles and description fields.
* [**[WD]**][WD] In Kaa 1.3, platform administrators can configure announcement and maintenance banners directly from UI.
A maintenance banner can be used to notify other users about planned maintenance.
It cannot be closed by the user, so everyone will know about the upcoming downtime in advance.
A marketing banner can be used for commercial announcements, discounts, or any other marketing campaigns.
[KaaIoT Cloud](https://cloud.kaaiot.com) users can [enable advanced security](https://cloud.kaaiot.com/settings/security) configuration that allows to change all tenant specific properties.
* [**[Data analytics]**][data analytics] In [**[Open Distro for Elasticsearch]**] added automatic index pattern creation for [Kibana][[open distro kibana].
* [**[EPTS]**][EPTS] Deprecated REST API for [updating endpoint time series data under an application][EPTS time series PUT REST API] is dropped.
  Using the [application version-specific API][EPTS time series PUT via app version REST API] instead is recommended going forward.
* [**[EPTS]**][EPTS] now permits time series names to contain upper-case Latin letters, making the effectively allowed charset as follows: Latin letters (`a-z`, `A-Z`), digits (`0-9`), dashes (`-`) and underscores (`_`).
  Time series value names must not include any of: backslash (`\`), circumflex accent (`^`), dollar sign (`$`), single quotation mark (`'`), double quotation mark (`"`), equal sign (`=`), or comma (`,`).
  Also, names `time`, `kaaEndpointID`, `kaaEmpty`, `_field`, and `_measurement` are reserved and cannot be used.
* [**[EPTS]**][EPTS] Data sample keys eligible for auto-extraction are now limited to Latin letters (`a-z`, `A-Z`), digits (`0-9`), underscores (`_`), and dashes (`-`) to conform to the existing restrictions on EPTS time series names.
* [**[DCX]**][DCX] Support processing non-batched, individual data samples in addition to batches defined in [2/DCP][2/DCP].
  Now an endpoint can upload a single, non-batched data sample as a JSON record.
  This is useful for devices that do not support data batching and can only send single data sample JSON record per data collection message to the platform.
* [**[DCX]**][DCX] Processes messages with an extension-specific resource path that starts with `/json` without requiring the full path match as defined in the [2/DCP][2/DCP].
  As a result, the devices that automatically append random values to the MQTT topic can communicate with DCX without restrictions.
  E.g. the resource path `/json/random_string` is handled as the `/json`.
  See the [application configuration]({{dcx_url}}Configuration/#resource-path-relaxed-validation) for details.
* [**KPC**][KPC] now supports MQTT over TLS 1.3.
* [**BCX**][BCX] supports retrieving the most recent binary data blob payload using the [REST API][BCX data blob last REST API].
* [**BCX**][BCX] supports deleting binary data blob by its ID using the [REST API][BCX data blob delete REST API].
* [**BCX**][BCX] data persistence interface configuration parameter `kaa.postgresql.url` is no longer supported.
  See [data persistence interface configuration]({{bcx_url}}Configuration/#data-persistence-interface) and [Environment variables]({{bcx_url}}Deployment/#environment-variables) as well for the new parameters.
* [**CCM**][CCM] data persistence interface configuration parameter `kaa.postgresql.url` is no longer supported.
  See [data persistence interface configuration]({{ccm_url}}Configuration/#data-persistence-interface) and [Environment variables]({{ccm_url}}Deployment/#environment-variables) as well for the new parameters.
* [**[CEX]**][CEX] supports deleting the command using the [REST API][CEX REST API DELETE command].
* [**[OTAO]**][OTAO] Now endpoint can get software specification on software creation or update without making explicit requests.
* [**[Helmfile]**][Helmfile] Starting with Kaa 1.3, the platform is deployed by Helmfile.
  With Helmfile each Kaa component has its own Helm release lifecycle.
  So if you want to upgrade or downgrade a specific Kaa component, you don't need to upgrade or downgrade the whole platform but instead only this specific component.
  Note, that the platform does not have a Kaa Helm meta-chart anymore.


## Kaa 1.2-mr1 (October 2-nd, 2020)
<!-- 1.2.1417 -->

Kaa 1.2-mr1 is a maintenance release for Kaa 1.2 with the following changes:

* **[Bug fix]** Incomplete TLS handshake prevents [KPC][KPC] from accepting other MQTT/TLS connections.
The condition occurs when an outstanding client TLS handshake hangs prior to completion, and clears when the handshake times out.


## Kaa 1.2 (July 6-th, 2020)

Find below high-level descriptions of some of the major release highlights.


### Multi-tenancy

Kaa 1.2 implements advanced multi-tenancy so that every platform tenant gets a dedicated authentication and authorization KeyCloak realm.
As a result, each tenant has a fully isolated space and can manage their own:

* users
* permissions
* applications and their versions
* endpoints and their related data
* client credentials
* solutions
* dashboards, etc.

Tenants are also able to configure their own external Identity Providers (IDPs: e.g. corporate LDAP, Active Directory, various OAuth or SAML providers).

To learn more about multi-tenancy, see [the corresponding documentation][multi-tenancy].


### Client credentials management

[**Client Credentials Management service (CCM)**][CCM] is a new Kaa service for the [client][client] authentication, which takes over these responsibilities from the [**Credentials Management service (CM)**][CM].
CCM supports authentication using basic credentials, such as username/password, and SSL/TLS certificates, based on X.509 technology.

Basic authentication is currently supported by all MQTT-based transports available in the [Kaa Protocol Communication service (KPC)][KPC]: plain MQTT, MQTT/TLS, MQTT/WebSocket.
X.509 authentication is supported by the MQTT/TLS KPC transport.

Both basic and X.509 authentication are now enforcable on a per-tenant basic, separately for each compatible transport.
You can toggle between them in your tenant right from the [Kaa Web Dashboard][WD] interface.

![Basic auth toggle on plain MQTT transport](attach/v1.2/mqtt-transport-turn-on.png)

Kaa WD now also provides interface for managing basic and X.509 credentials.
Credentials of both types are a propery of a given tenant and can be used to connect clients to exchange data on behalf of any endpoints that belong to applications of that tenant.

Basic credentials management.

![create basic credentials](attach/v1.2/create-basic-credentials.jpg)

X.509 credentials management.

![create x509 credentials](attach/v1.2/create-x509-credentials.jpg)


### HTTP transport support

[**Kaa Protocol Communication service (KPC)**][KPC] now supports [HTTP transport][KPC HTTP] that implements [1/KP protocol][1/KP] over plain HTTP.
Unlike 1/KP over MQTT, HTTP binding is synchronous: it follows the request-response communication pattern and does not support server message push.

Learn more about integrating clients using the HTTP transport [here][1/KP over HTTP].


### Binary data collection

Kaa 1.2 supports collection of binary data blobs (still images, video segments, audio recordings, etc.) from connected devices.
This is enabled by a new microservice: [**Binary data Collection Extension (BCX)**][BCX].
The supported data storage backend is AWS S3.

To upload a binary data blob, client must first [retrieve a temporary authorization token][BCX token exchange extension interface] on behalf of an endpoint from the BCX service using an existing communication channel (MQTT- or HTTP-based).
Once in possession of a temporary token, client may upload binary data blobs related to that endpoint using the [RESTful data upload API][BCX binary data blob upload REST API].
BCX also provides [REST API for managing and accessing already uploaded binary blobs][BCX data blob management REST API].

The submitted binary data blobs can be viewed and downloaded using the corresponding Kaa Web Dashboard (WD) widgets:

![Binary data blobs in Kaa WD](attach/v1.2/bcx.jpg)


### Endpoint configuration schema management

In Kaa 1.0 and 1.1, endpoint configuration was a free-form JSON document.
With Kaa 1.2 we introduce an ability to configure endpoint configuration schema in the [Endpoint Configuration Registry service (ECR)][ECR].
The endpoint configuration schemas are associated with Kaa applications and appversions.
The appversion-specific schema takes precedence over the corresponding application-specific schema.

When schema validation is enabled in ECR, it rejects provisioning endpoint configs that do not match the expected schema.

Endpoint configuration schemas can be configured for ECR in the Kaa Web Dashboard, either in the schema view:

![Endpoint configuration schema management schema view](attach/v1.2/ep-config-schema-schema.png)

or in the JSON view:

![Endpoint configuration schema management JSON view](attach/v1.2/ep-config-schema-json.png)


### Data samples enrichment with endpoint metadata

Kaa [Data Collection Extension service (DCX)][DCX] now allows enriching data samples received from connected endpoints with their metadata attributes.
When this feature is [enabled in the DCX configuration][DCX metadata enrichment config], it appends endpoint metadata key-value pairs to each data sample events using the specified path (`~ep-metadata` by default).
Doing so makes it possible to feed downstream data processing services, such as [EPTS][EPTS] or [KDCA][KDCA] with additional endpoint-related state information.
Note that only data samples that are JSON objects can be enriched with endpoint metadata.
The data samples enrichment is disabled by default for backward compatibility.

See the [DCX documentation][DCX metadata enrichment] for more details.


### Data analytics

Kaa 1.2 is now pre-integrated out of the box with the [Open Distro for Elasticsearch][open distro].
Each tenant's data is isolated in Elasticsearch and Kibana, and the security access policies are seamlessly integrated with Kaa.
This integration enables various IoT data analytics functionality, including collection, analysis, querying and visualizing device data.

![Data analytics](attach/v1.2/analytics.png)

Flexible triggers and alerts can be configured to send notifications to preferred destinations.

![Trigger and alerts](attach/v1.2/analytics-alerts.png)

Find out more about the data analytics in Kaa [here][data analytics].


### Other highlights of Kaa 1.2

* [**[TEKTON]**][TEKTON] Tekton now restricts the application version name suffix to match `^[a-z0-9]+$` regex pattern when you create a new application version using the [REST API][TEKTON app version create REST API].
  In addition, application names will be automatically generated for [newly created applications][TEKTON application create REST API] when `kaa.tekton.app-names.auto-generation.enabled` configuration variable is set to `true`.
  It is recommended that you enable the auto-generation to prevent the possible [application name conflicts](#application-and-application-version-names-conflict-in-java-based-services).
* [**[TEKTON]**][TEKTON] Tekton now supports bulk REST API for [Bulk operations][TEKTON bulk REST API] on tenants and their applications.
* [**[CEX]**][CEX] `commandRetentionTtl` time unit changed in [REST API][CEX REST API POST command] from hours to milliseconds.
* [**[CEX]**][CEX] `commandRetentionTtl` renamed in [REST API][CEX REST API POST command] to `commandTtl`.
* [**[EPTS]**][EPTS] EPTS now supports updating time series data for specified endpoints under application version in its [REST API][EPTS time series PUT via app version REST API].
  Just like with DSTP and TSTP interfaces, the data points published to this API yield time series events on the TSTP interface.
* [**[EPTS]**][EPTS] REST API for [updating endpoint time series data under an application][EPTS time series PUT REST API] is deprecated and will be dropped in the next release.
  Using the [application version-specific API][EPTS time series PUT via app version REST API] instead is recommended going forward.
* [**[EPTS]**][EPTS] EPTS now supports defining which of the `fromDate` and `toDate` are inclusive when [retrieving historical time series data][EPTS time series data REST API].
* [**[EPTS]**][EPTS] EPTS now supports data points filtering using the `beforeDate` query parameter in its [REST API][EPTS time series last REST API].
* [**[EPR]**][EPR] In previous Kaa versions, EPR provided endpoint metadata and endpoint filter management only via its [REST API][EPR REST API].
  Now, in addition to REST API it is possible to manage endpoint [metadata][endpoint-metadata] and [endpoint filters][endpoint-filter] via [NATS][nats] using the [19/EPMMP] and [20/EFMP] protocols.
  These interfaces improve overall performance and give more flexibility for platform expansion and customization.
* [**[CEX]**][CEX] now supports getting the list of existing command resources per endpoint or application name via the [REST API][CEX REST API].
* [**[CEX]**][CEX] now uses PostgresSQL database instead of Redis.
* [**[ECR]**][ECR] configuration response format changed in [default config API][ECR REST API GET default config] and [per-endpoint config API][ECR REST API GET per-endpoint config].
* [**[ECR]**][ECR] `/endpoints/{endpointId}/app-versions/{appVersionName}/current` REST endpoint removed.
  Use [per-endpoint config API][CEX REST API GET per-endpoint config] instead.
* **[RCI]** service is now fully deprecated and removed.
  [CEX][CEX] service provides a super-set of the original RCI functionality.
* [**[KDCA]**][KDCA] now adds tenant ID and the endpoint application version fields to Kafka events.
* [**[CMX]**][CMX] now supports configuration applied messages from endpoints per [7/CMP][7/CMP].
* [**[WD]**][WD] JSON schema editor integrated with Configuration form, Endpoint list, Software OTA related widgets.
* [**[WD]**][WD] New widgets added: "Device orientation" and "Luminance".
* [**[WD]**][WD] Added dynamic variables for "Gauge" widgets.
  It is possible to populate values from endpoint or application version configuration to scale, max, min and threshold fields.
* [**[WD]**][WD] "Multi series chart" now can be configured to show mixed line styles like bar, step and line.
  It is now possible to show data from multiple endpoints.
* [**[WD]**][WD] now has a pre-configured device management page for manage endpoint metadata, commands, data blobs, etc.
* [**[WD]**][WD] now has a "User management" link that redirects to the KeyCloak user administration page.
* [**[WD]**][WD] now has a "Help" page.
* [**[WD]**][WD] various UX improvements, performance optimizations, and bugfixes.
* **[Bug fix]** [KPC][KPC] sets SubAck MQTT packet QoS.
* **[Bug fix]** [KDCA][KDCA] does not recover after losing a Kafka connection.
* **[Bug fix]** Java services don't fetch Tekton configs at boot time.


## Kaa 1.1-mr3 (November 17-th, 2020)

Kaa 1.1-mr3 is a maintenance release for Kaa 1.1 with the following changes:

* **[Bug fix]** `commandRetentionTtl` zero value in [REST API](https://docs.kaaiot.io/KAA/docs/v1.1.0/Features/Command-invocation/CEX/REST-API/#endpoints__endpointid__commands__commandtype__post) was not supported.
Now commands with zero `commandRetentionTtl` are pushed to the device only once.
Also, `commandRetentionTtl` can be configured with `kaa.cex.commands.command-retention-ttl-hours` service configuration property.


## Kaa 1.1-mr2 (October 2-nd, 2020)
<!-- 1.1.119 -->

Kaa 1.1-mr2 is a maintenance release for Kaa 1.1 with the following changes:

* **[Bug fix]** Incomplete TLS handshake prevents [KPC][KPC] from accepting other MQTT/TLS connections.
The condition occurs when an outstanding client TLS handshake hangs prior to completion, and clears when the handshake times out.


## Kaa 1.1-mr1 (March 25-th, 2020)
<!-- 1.1.110 -->

Kaa 1.1-mr1 is a maintenance release for Kaa 1.1.
In scope of this release the following changes were made:

* **[Bug fix]** Creating a new application or application version overwrites configuration of an existing one.
Reproducible with Java-based services when the new name is a sub-string of an existing one.
* **[Bug fix]** The [WD][WD] permits creating endpoints with an empty token.
* **[Bug fix]** The [WD][WD] does not render variables in dashboard titles.
* **[Bug fix]** Wrong [WD][WD] documentation link anchors.


### Known issues in Kaa 1.1-mr1

#### Application and application version names conflict in Java-based services

**Affected Kaa versions**: [1.1-mr1](#kaa-11-mr1-march-25-th-2020), [1.1](#kaa-11-november-8-th-2019), and [1.0](#kaa-10-june-10-th-2019).

**Issue summary**: Application or application version names that differ only by dashes (`-`) or underscores (`_`) cause naming conflict in Java-based services.

Consider an example of a Kaa application with name `smart_kettle`.
Creating applications with names like `smart-kettle`, `smartkettle`, or similar, will cause a conflict when loading such configuration in Java-based services.

To prevent this issue, starting from [Kaa 1.2](#kaa-12-future-release) we introduced the option of application name auto-generation on [creation via the Tekton REST API][TEKTON application create REST API].
Additionally, the application version name suffix is restricted to lowercase Latin letters (`a-z`) and digits (`0-9`) when you [create new application versions][TEKTON app version create REST API].

**Known workaround**: To prevent the issue from happening in affected Kaa versions, refrain from creating applications or application versions with names that only differ by dashes (`-`) or underscores (`_`).
Starting with [Kaa 1.2](#kaa-12-future-release) it is recommended to enable the application name auto-generation feature.


## Kaa 1.1 (November 8-th, 2019)
<!-- 1.1.77 -->

Find below high-level descriptions of some of the major release highlights.


### Kaa Tekton

[Kaa Tekton][TEKTON] is a new Kaa infrastructure component that takes ownership of the Kaa [applications, application versions][application], and the application-specific configurations for [Kaa service instances][scalability].
In previous Kaa versions applications and their configurations were defined in the configuration files of each Kaa microservice.
Such configuration is still supported for backward compatibility and simple deployments.
However, Tekton now offers a more convenient management mechanism.

Tekton exposes [REST API][TEKTON REST API] for managing applications, versions, and the associated configurations.
The [Web Dashboard (WD)][WD] leverages this API and provides a convenient management UI.
Applications are represented as [protected resources][application resource type] in the auth server, so you can configure users' level of access by granting corresponding [OAuth 2.0 scopes][oauth scope].

![Application configuration](attach/v1.1/application-config.png)

Whenever the list of applications, versions, or service configs changes, Tekton generates an appropriate [17/SCMP][17/SCMP] notification message over NATS that gets delivered to all affected service replicas.
In turn, they reload updated configurations from Tekton and apply changes immediately without a restart.
It is no longer necessary to update all service instance configuration files or reboot service replicas.

You can use [this script](https://github.com/kaaproject/kaa/blob/master/doc/Whats-new/attach/v1.1/generate_tekton_config.py) to convert your Kaa 1.0 blueprint configuration files into a JSON suitable for [Tekton bulk configuration load REST API][TEKTON bulk config load REST API].
Note that due to the various compatibility reasons the application and application version names must be limited to lowercase latin letters (`a-z`), digits (`0-9`), dashes (`-`) and underscores (`_`).


### Unified resource naming convention

The Kaa platform uses [OAuth 2.0][oauth2] and [User-Managed Access (UMA)][uma] for API calls authentication and authorization.
Starting with Kaa 1.1 all Kaa components follow a [unified resource naming convention][api security] that is designed to prevent naming conflicts.
Users upgrading from Kaa 1.0 must rename resources in their auth servers according to the new convention.


### Web Dashboard theme customization

Previously, the Web Dashboard theme customization was possible via loading custom CSS.
In Kaa 1.1 the primary UI colors were revised and consolidated, and the theme customization is now possible right from the Administration page.

![Theme customization](attach/v1.1/theme.png)


### Other highlights of Kaa 1.1

* [**[CEX]**][CEX] CEX now supports synchronous and asynchronous [REST API][CEX REST API] calls for command invocation.
  Due to this, the [RCI][RCI] microservice is now deprecated and will be retired in a future Kaa version.
* [**[CEX]**][CEX] In previous Kaa versions CEX would send new commands asynchronously to any connected endpoint.
  Starting with Kaa 1.1, according to [11/CEP][11/CEP] protocol, CEX no longer would send new commands asynchronously after the endpoint sends a command request message with `observe` flag explicitly set to `false`.
* [**[CMX]**][CMX] In previous Kaa versions CMX would send new configurations asynchronously to any connected endpoint.
  Starting with Kaa 1.1, according to [7/CMP][7/CMP] protocol, CMX no longer would send new configurations asynchronously after the endpoint sends a configuration request message with `observe` flag explicitly set to `false`.
* [**[EPTS]**][EPTS] [Auto-extracted time series][EPTS time series auto extraction] now have `auto~` prefix to prevent name conflicts with user configured time series.
* [**[EPTS]**][EPTS] Support for saving time series data via the [EPTS REST API][EPTS time series PUT REST API].
* [**[EPTS]**][EPTS] Support for storing and retrieving [time series data with explicit `null` values][EPTS time series extraction].
* [**[EPTS]**][EPTS] User configured time series names are now limited to a combination of lowercase latin letters (`a-z`), digits (`0-9`), dashes (`-`) and underscores (`_`).
* [**[CM]**][CM] REST API [OAuth 2.0 scopes][oauth scope] for [client credential and certificate management][CM clients REST API] changed to `kaa:client-credentials:read`, `kaa:client-credentials:create`, `kaa:client-credentials:update`, `kaa:client-certificates:read`, `kaa:client-certificates:create`, and `kaa:client-certificates:update`.
* [**[CM]**][CM] CM now only accepts x.509 cert serial number in base 10 encoding on provisioning and validation.
  Also, a specific list of x.509 cert issuer fields processed by CM is restricted and documented [here][CM certificate POST REST API].
  Active x.509 client certificates must be re-provisioned into CM.
* [**[EPR]**][EPR] EPR no longer supports `GET /applications` and `GET /applications/{applicationName}` REST API calls.
  The API for retreving application information is now [available from Tekton][TEKTON applications REST API].
* [**[EPR]**][EPR] EPR now supports creating endpoints with [user-defined endpoint IDs][EPR endpoint POST REST API].
  Such IDs must match `^[a-zA-Z0-9._~-]+$` regex pattern.
  This is useful for matching entities between Kaa and external systems.
* [**[KPC]**][KPC] KPC now always observes the MQTT keepalive interval set by the client in the CONNECT packet.
* [**[KPC]**][KPC] Support for [endpoint][endpoint] roaming across [client][client] MQTT connections to different KPC replicas.
  When multiple gateways (Kaa clients) represent a single endpoint, they may communicate on behalf of such endpoint in turns.
  Whenever such communication switch (roaming) occurs, even across KPC replicas, they are now able to identify that and correctly remove endpoint from the routing table of the previously used KPC replica.
* [**[WD]**][WD] Default "Device management" and "Software management" pages are now available in WD to administrators with `application:update` OAuth 2.0 scope granted for a given Kaa application.
* [**[WD]**][WD] Added the ability to specify the default sort key and direction in endpoint list and time series table widgets.
* Documentation restructuring: previously Kaa components documentation was hosted separately from the general documentation.
  For the readers' convenience, all component docs are now relocated to the ["Features and components" section][kaa features].
  This documentation now captures the exact state of all platform components at the moment of the general platform release.
  You can refer to the "Components" subsection of each feature page to find out the specific service versions included in a given release.
  For example, see the ["Device management" feature page](../Features/Device-management/#components).
* Multiple performance optimizations, usability and visual improvements, and bug fixes.


## Kaa 1.0 (June 10-th, 2019)

Kaa 1.0 is the initial general release of the Kaa Enterprise IoT platform.

Prior to the 1.0 version, every Kaa component was versioned independently.
Such independent versioning still exists for each of the Kaa microservices, while the Kaa 1.0 release is a "meta-package" that includes a set of component versions.
All of the microservices in Kaa 1.0 have been tested for interoperation and can be installed in one shot to a Kubernetes cluster of your choice with a new Kaa installer.

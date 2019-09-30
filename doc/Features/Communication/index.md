---
layout: page
title: Communication
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}


## Prerequisites

- You understand the Kaa platform [microservice-based architecture]({{root_url}}Architecture-overview/).


## Basic concept

Device communication is an important aspect of an IoT platform.
The main objectives of this Kaa function are:
- Handling the data exchange among connected devices and the Kaa platform components.
- Authenticating connecting devices (both [clients and endpoints]({{root_url}}Architecture-overview/#client-endpoint)).
- Identification of the device capabilities (see [application and application version][application]).
- Securing the device communication with encryption and tamper protection.
- Handling device connectivity state (connected / disconnected).

The device communication in Kaa is enabled by a combination of microservices: Kaa Protocol Communication (KPC) and Endpoint Lifecycle (EPL) services being the most important ones.

![Communication services](communication.png)


[**Kaa Protocol Communication service (KPC)**][KPC] implements Kaa Protocol-based communication with clients and endpoints the client represents.
KPC currently supports MQTT, MQTT/TLS, and MQTT/Websocket transport implementations.
Read more about the Kaa Protocol (1/KP) [below][1/KP over MQTT topic structure].

KPC acts as an MQTT-compliant server for clients to connect and exchange endpoint data with the Kaa platform.
From the client standpoint, talking to KPC is no different from talking to any other MQTT broker.
All you need in a client is an MQTT library.

You should not expect MQTT broker behavior from KPC, though.
Its primary purpose is to enable device interoperation with the various Kaa platform features: [identity management][identity], [data collection][data collection], [configuration management][configuration] and others.
This is achieved by relaying client originated messages to [extension services][extension] and vice versa.
KPC is unaware of the specifics of the extension protocols that are multiplexed on top of 1/KP ([2/DCP][2/DCP], [7/CMP][7/CMP], [10/EPMP][10/EPMP], etc.).
Rather, it uses information available in 1/KP to (de-)multiplex extension protocols and route messages from clients to appropriate extension service instances and vice versa.

> This separation of concerns between the way in which devices are connected and the platform intrinsic features makes Kaa highly adaptable and extensible.
When you need a new IoT feature, you can build a new extension service for that without affecting the communication layer.
When you want to connect devices that support a new, previously unsupported protocol, you can do that by replacing KPC with your own implementation, without affecting anything else.
{:.tip}

KPC performs client authentication and endpoint identification via the [Credential Management service][CM].
For clients, you can configure authentication using MQTT username/password combination or client SSL certificate.
Endpoints are identified using endpoint tokens.

When endpoints connect or disconnect from KPC, it broadcasts [9/ELCE][9/ELCE] connectivity events to NATS.
Interested services may subscribe to such events to perform certain actions.
Notable examples of such services are [CMX][CMX] and EPL.

[**Endpoint Lifecycle service (EPL)**][EPL] monitors endpoint connectivity status events broadcasted by KPC.
You can configure EPL to update endpoint metadata attributes in [EPR][EPR] with the current connectivity status and the last transition timestamp.

EPL can also convert connectivity events into [14/TSTP][14/TSTP] time series data points and transmit them over NATS.
[EPTS][EPTS] can be configured to receive such data points and store them.
This is useful if you want to track the endpoint connectivity history---not just the current state.


## MQTT topic structure for the Kaa Protocol v1

The Kaa Protocol (1/KP) is a set of data exchange conventions designed to meet various real-life requirements, such as:
- extensibility
- asynchronous communication
- multiplexing
- gateway compatibility
- multi-level auth
- operation status reporting
- version control
- capability discovery
- and others.

1/KP binds to various transport protocol stacks, including MQTT, CoAP, HTTP, and others.

You can learn about the Kaa Protocol design in details from the [1/KP RFC][1/KP], however, that may be a dull reading (we warned you :).
In this section we explain the basic structure of 1/KP-based communication.
Understanding it is essential for integrating your devices and/or gateways with Kaa.

According to the 1/KP, the general MQTT topic structure for endpoint-originated requests is next:

`kp1/{application_version}/{extension_instance_name}/{endpoint_token}/{resource_path}[/{request_id}]`

where:

- `kp1` is the reserved prefix for the Kaa Protocol version 1. Future versions of Kaa Protocol will have prefixes such as `kp2`, `kp3`, and so on.
- `{application_version}` is a unique name that identifies [application version][application] that the request originating endpoint operates in, e.g. `demo_application_v1`, `smart_kettle_v1`, etc.
- `{extension_instance_name}` is a name that uniquely identifies an [extension service][extension] instance the message is destined to. It can be `dcx` that stands for [Data collection extension][DCX], `epmx` that stands for [Endpoint metadata extension][EPMX], etc.
- `{endpoint_token}` is an [endpoint token][endpoint-token] that uniquely identifies the endpoint, e.g. `JTjdbENzHh`.
- `{resource_path}` is an extension-specific resource path that exposes some IoT functionality to the endpoints. For example, [Data collection extension][DCX] has `/json` resource path that allows endpoints to push telemetry data into the platform; [Endpoint metadata extension][EPMX] has `/update/keys` for updating endpoint metadata attributes, etc.
- `{request_id}` is an optional, positive integer identifier of a request that is used for matching extension responses to endpoint requests. You must set a request ID in the request whenever you want to receive an operation confirmation from the extension service. Naturally, request IDs must be unique for outstanding requests between any given endpoint and any extension service at any moment in time.

Let's take an example.
Imagine that you have an endpoint with the `JTjdbENzHh` token that works in the `demo_application_v1` application version of the `demo_application` application.
When you want to push telemetry data into the Kaa platform on behalf of this endpoint, you should use the following MQTT topic: `kp1/demo_application_v1/dcx/JTjdbENzHh/json`.
Here `/json` is the DCX extension resource.

Note that there is no request ID specified in the MQTT topic above.
Such request will yield no response from DCX, even after the endpoint telemetry data is successfully stored by Kaa.
(There may still be an error response in case of message processing issues.)
If you want to receive a confirmation on a successful message processing, you should include the request ID in the request topic.
Read on to learn more about this.


### Request/response pattern

Many IoT functions (implemented by [extension services][extension] in the Kaa platform) require request/response style communication, which is not natively supported by MQTT.
For that reason 1/KP introduces the request ID in the request MQTT topic.
Whenever endpoint wants to use request/response pattern, it must append request ID to the end of the MQTT topic.

Successful responses to requests with the request ID arrive back to your client on the request topic with the `/status` suffix in the end.
Error responses arrive on the request topic with the `/error` suffix.

Imagine that in the previous scenario you don't just want to fire-and-forget telemetry data to the Kaa platform, but also receive confirmation messages when the submitted data is successfully processed.
Then the MQTT topic you should use to publish telemetry data must be next: `kp1/demo_application_v1/dcx/JTjdbENzHh/json/42`, where `42` is the request ID.
The request IDs for outstanding requests from that endpoint to `dcx` service instance must be unique at any point in time so that you will be able to properly match received responses.

The response on **successful** data processing will arrive to the `kp1/demo_application_v1/dcx/myToken/json/42/status` MQTT topic.
The response on **unsuccessful** data processing will arrive to the `kp1/demo_application_v1/dcx/myToken/json/42/error` MQTT topic.


# Next steps

- [How to connect a device tutorial][how to connect device] - find out how to connect your device to the Kaa platform.

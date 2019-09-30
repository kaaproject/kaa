---
layout: page
title: Command invocation
permalink: /:path/
sort_idx: 50
---

{% include variables.md %}
{% include_relative links.md %}


## Prerequisites

- You understand the Kaa platform [microservice-based architecture]({{root_url}}Architecture-overview/).


## Basic concept

Kaa command invocation feature helps controling devices remotely.
Using it, you can send short lived messages to connected devices (commands), make them react as necessary, and receive responses.
For example, you can toggle light, pop up car trunk, or request an immediate device status report.
<!-- - Remotely invoke execution of commands, synchronously or asynchronously. -->
<!-- - Scheduling commands delivery (e.g., for offline devices). -->

The command invocation feature is implemented in Kaa primarily with Command Execution Extension service (CEX) and REST Command Invocation service (RCI).

<!-- TODO: redraw -->
![Command invocation services](command-invocation.png)

[**Command Execution Extension service (CEX)**][CEX] extends the communication capability of Kaa Protocol ([1/KP][1/KP]) by implementing Command Execution Protocol ([11/CEP][11/CEP]).
CEX supports this extension protocol to deliver commands to endpoints and consume endpoint command execution result.
As with other Kaa extension services, CEX uses Extension Service Protocol ([4/ESP][4/ESP]) for integration with a communication service.

CEX implements a proactive command push---commands are sent to an endpoint as soon as possible, and an explicit endpoint subscription is not required.

CEX communicates with RCI via the Command Invocation Protocol ([12/CIP][12/CIP]).
This protocol is used to accept command requests and return results back to the caller.

[**REST Command Invocation service (RCI)**][RCI] exposes [REST API][RCI REST API] for invoking commands against endpoints.

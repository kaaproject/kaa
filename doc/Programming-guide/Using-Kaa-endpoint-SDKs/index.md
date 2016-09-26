---
layout: page
title: Using Kaa endpoint SDKs
permalink: /:path/
sort_idx: 40
---


{% include variables.md %}

* TOC
{:toc}

Developing a [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment) requires following the same routine: creating network communication stack, log delivery functionality, event exchange between endpoints, etc.
To minimize the deployment time for users, [Kaa platform]({{root_url}}) comes with all the basic functionality already implemented in the endpoint SDKs so you can use it right out of the box.

An endpoint SDK is a library that provides communication, data marshaling, persistence, and other functions available in Kaa for a specific [SDK type]({{root_url}}Glossary/#sdk-type).
Each SDK type is designed to be embedded into your endpoint and works in conjunction with [Kaa cluster]({{root_url}}Glossary/#kaa-cluster) that serves as a cloud-based middleware for a particular IoT solution.

The [Kaa client]({{root_url}}Glossary/#kaa-client) processes the structured data received from [Kaa server]({{root_url}}Glossary/#kaa-server) (e.g. [notifications]({{root_url}}Programming-guide/Key-platform-features/Notifications/), [configuration]({{root_url}}Programming-guide/Key-platform-features/Configuration-management/)) and sends it to the return path interfaces (e.g. [profiles]({{root_url}}Programming-guide/Key-platform-features/Endpoint-profiles/), [logs]({{root_url}}Programming-guide/Key-platform-features/Data-collection/)).

Endpoint SDKs help save time on development routine and allow users to concentrate on the application business logic.

See the [list of supported platforms]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/Supported-platforms/) to get an SDK that fits your IoT solution environment.

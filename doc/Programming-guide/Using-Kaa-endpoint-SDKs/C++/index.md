---
layout: page
title: C++
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

* TOC
{:toc}

The [Kaa C++ SDK]({{root_url}}Glossary/#endpoint-sdk) allows easily implementing Kaa [clients]({{root_url}}Glossary/#kaa-client) on platforms that provide the C++ runtime.

## Field of application

The Kaa C++ SDK is a good choice for major operating systems and high-end embedded platforms.
It is very effective for building high-performance gateways and endpoints of your IoT infrastructure.

In case you need to run Kaa endpoints on bare metal hardware, consider using [the C SDK]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C).

## Environment setup

To build a C++ SDK, you need to first install the following third-party dependencies:

* Avro C++
* Boost
* Botan
* SQLite3 (optional)

The installation process may vary depending on the target platform.
For detailed platform-specific instructions, refer to [Supported platforms]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/Supported-platforms/).

## Build configuration

The Kaa C++ SDK uses the CMake build system.
You can use CMake to tweak some of your SDK build parameters.
The complete summary of these parameters is contained in the root CMakeLists.txt file of the C++ SDK.

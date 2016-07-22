---
layout: page
title: C++
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

Kaa C++ SDK is a cross-platform implementation of Kaa Endpoint.
It enables IoT functionality on any platform which provides C++ runtime.

## Field of application

Kaa C++ SDK is a good choice for major operating systems and high-end embedded platforms.
It is good at building high-performance gateways and endpoints for you IoT infrastructure.

In case you need to run Kaa endpoints on bare metal hardware, consider using [the C SDK]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C).

For in-depth architecture overview, refer to [C++ SDK architecture overview page]({{root_url}}/Customization-guide/Endpoint-SDKs/C++-SDK/Architecture-overview/).

## Environment setup

To build C++ SDK, you should install dependencies first:

* Avro C++
* Boost
* Botan
* SQLite3 (optional)

Installation may differ depending on the target platform.
For detailed platform-specific instructions, check the corresponding subpages.

Another way to setup required dependencies is by using [Nix package mananger]({{root_url}}/Customization-guide/Endpoint-SDKs/C-SDK/Environment-setup/Nix-guide).
However, this is more suitable for SDK development rather than development of applications based on SDK.

## Build configuration

The Kaa C++ SDK uses CMake build system. There are some configuration parameters which can be passed to CMake to tweak SDK build.
The complete summary of these parameters can be found in the root `CMakeLists.txt` file of C++ SDK.

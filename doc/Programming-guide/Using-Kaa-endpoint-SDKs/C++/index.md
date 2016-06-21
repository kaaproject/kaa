---
layout: page
title: C++
permalink: /:path/
nav: /:path/Programming-guide/Using-Kaa-endpoint-SDKs/Using-Kaa-Cpp-endpoint-SDK
sort_idx: 20
---

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

Kaa C++ SDK is a cross-platform implementation of Kaa Endpoint.
It enables IoT functionality on any platform which provides C++ runtime.

## Field of application

Kaa C++ SDK is a good choice for major operating systems and high-end embedded platforms.
It is good at building high-performance gateways and endpoints for you IoT infrastructure.

In case you need to run Kaa endpoints on bare metal hardware, consider using [the C SDK]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C).

For in-depth architecture overview, refer to [C++ SDK architecture overview page]({{root_url}}/Customization-guide/Endpoint-SDKs/C++-SDK/Architecture-overview/) page.

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

This section covers build configuration for Kaa C++ SDK.
The Kaa C++ SDK uses CMake build system. There are some configuration parameters which can be passed to CMake to tweak SDK build.
The complete summary of these parameters follows.

- `KAA_MAX_LOG_LEVEL` - maximum log level the SDK should produce.

    Values:

    - `0` - `NONE`
    - `1` - `FATAL`
    - `2` - `ERROR`
    - `3` - `WARN`
    - `4` - `INFO`
    - `5` - `DEBUG`
    - `6` - `TRACE`

    Default: `4`.

- `KAA_WITHOUT_EVENTS` - disable Event feature.

    Values:

    - `1` - Event feature is disabled
    - `0` - Event feature is enabled

    Default: `0`.

- `KAA_WITHOUT_NOTIFICATION` - disable Notification feature.

    Values:

    - `1` - Notification feature is disabled
    - `0` - Notification feature is enabled

    Default: `0`.

- `KAA_WITHOUT_CONFIGURATION` - disable Configuration feature.

    Values:

    - `1` - Configuration feature is disabled
    - `0` - Configuration feature is enabled

    Default: `0`.

- `KAA_WITHOUT_LOGGING` - disable Logging feature.

    Values:

    - `1` - Logging feature is disabled
    - `0` - Logging feature is enabled

    Default: `0`.

- `KAA_WITH_SQLITE_STORAGE` - enables SQLite storage for Logging feature.
That requires SQLite3 headers present on the system.

    Values:

    - `0` - SQLite storage is disabled
    - `1` - SQLite storage is enabled

    Default: `0`.

- `KAA_WITHOUT_THREADSAFE` - disable thread safe mode. Otherwise, Kaa SDK will maintain a thread pool.

    Values:

    - `0` - Threadsafe mode is enabled
    - `1` - Threadsafe mode is disabled

    Default: `0`.

- `KAA_WITHOUT_CONNECTIVITY_CHECKER` - disable connectivity checking. Should be used if you implement custom connectivity checker.

    Values:

    - `0` - Default connectivity checking is enabled
    - `1` - Default connectivity checking is disabled

    Default: `0`.

- `KAA_WITHOUT_OPERATION_HTTP_CHANNEL` - disable default Operation HTTP channel. Should be used if you implement custom Operation channel.

    Values:

    - `0` - Default Operation HTTP channel is enabled
    - `1` - Default Operation HTTP channel is disabled

    Default: `0`.

- `KAA_WITHOUT_OPERATION_TCP_CHANNEL` - disable default Operation TCP channel. Should be used if you implement custom Operation channel.

    Values:

    - `0` - Default Operation TCP channel is enabled
    - `1` - Default Operation TCP channel is disabled

    Default: `0`.

- `KAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL` - disable default Operation long poll channel. Should be used if you implement custom Operation channel.

    Values:

    - `0` - Default Operation long poll channel is enabled
    - `1` - Default Operation long poll channel is disabled

    Default: `0`.

- `KAA_WITHOUT_BOOTSTRAP_HTTP_CHANNEL` - disable default Bootstrap HTTP channel. Should be used if you implement custom Bootstrap channel.

    Values:

    - `0` - Default Bootstrap HTTP channel is enabled
    - `1` - Default Bootstrap HTTP channel is disabled

    Default: `0`.

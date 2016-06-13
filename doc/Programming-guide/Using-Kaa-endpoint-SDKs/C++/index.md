---
layout: page
title: C++
permalink: /:path/
nav: /:path/Programming-guide/Using-Kaa-endpoint-SDKs/Using-Kaa-Cpp-endpoint-SDK
sort_idx: 20
---

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

## Introduction

Kaa C++ SDK is a cross-platform implementation of Kaa endpoints.
It enables high-performance IoT functionality on any platform which provides C++ runtime.
This means that you can deploy C++ clients on any hardware platform supported by Linux.

In case you need to run Kaa endpoints on more low-end hardware, consider using [Kaa C SDK]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C).

This page describes build configuration for Kaa C++ SDK.
For detailed installation instructions for different platforms, check the corresponding subpages.

## Build configuration

The Kaa C++ SDK makes use of CMake build system. There is a number of configuration parameters which can be passed to CMake in order to tweak SDK build.
The complete summary of these parameters follows.


* `KAA_MAX_LOG_LEVEL` - specifies maximum log level the SDK should produce.

    Values:

    * `0` - `NONE`
    * `1` - `FATAL`
    * `2` - `ERROR`
    * `3` - `WARN`
    * `4` - `INFO`
    * `5` - `DEBUG`
    * `6` - `TRACE`

    Default: `4`.

* `KAA_WITHOUT_EVENTS` - disable Event feature.

    Values:

    * `1` - Event feature disabled
    * `0` - Event feature enabled

    Default: `0`.

* `KAA_WITHOUT_NOTIFICATION` - disable Notification feature.

    Values:

    * `1` - Notification feature disabled
    * `0` - Notification feature enabled

    Default: `0`.

* `KAA_WITHOUT_CONFIGURATION` - disable Configuration feature.

    Values:

    * `1` - Configuration feature disabled
    * `0` - Configuration feature enabled

    Default: `0`.

* `KAA_WITHOUT_LOGGING` - disable Logging feature.

    Values:

    * `1` - Logging feature disabled
    * `0` - Logging feature enabled

    Default: `0`.

* `KAA_WITH_SQLITE_STORAGE` - enables SQLite storage for Logging feature.
This requires SQLite3 headers present on system.

    Values:

    * `0` - SQLite storage disabled
    * `1` - SQLite storage enabled

    Default: `0`.

* `KAA_WITHOUT_THREADSAFE` - disable thread safe mode. Otherwise, Kaa SDK will maintain a thread pool.

    Values:

    * `0` - Threadsafe mode enabled
    * `1` - Threadsafe mode disabled
    
    Default: `0`.

* `KAA_WITHOUT_CONNECTIVITY_CHECKER` - disable connectivity checking. Should be used if you implement custom connectivity checker.
    
    Values:

    * `0` - Default connectivity checking enabled
    * `1` - Default connectivity checking disabled
    
    Default: `0`.

* `KAA_WITHOUT_OPERATION_HTTP_CHANNEL` - disable default Operation HTTP channel. Should be used if you implement custom Opearation channel.
    
    Values:

    * `0` - Default Operation HTTP channel enabled
    * `1` - Default Operation HTTP channel disabled
    
    Default: `0`.

* `KAA_WITHOUT_OPERATION_TCP_CHANNEL` - disable default Operation TCP channel. Should be used if you implement custom Opearation channel.
    
    Values:

    * `0` - Default Operation TCP channel enabled
    * `1` - Default Operation TCP channel disabled
    
    Default: `0`.

* `KAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL` - disable default Operation long poll channel. Should be used if you implement custom Opearation channel.
    
    Values:

    * `0` - Default Operation long poll channel enabled
    * `1` - Default Operation long poll channel disabled
    
    Default: `0`.

* `KAA_WITHOUT_BOOTSTRAP_HTTP_CHANNEL` - disable default Bootstrap HTTP channel. Should be used if you implement custom Bootstrap channel.
    
    Values:

    * `0` - Default Bootstrap HTTP channel enabled
    * `1` - Default Bootstrap HTTP channel disabled
    
    Default: `0`.


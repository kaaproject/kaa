---
layout: page
title: Using Kaa endpoint SDKs
permalink: /:path/
sort_idx: 40
---


{% include variables.md %}

* TOC
{:toc}

Developing an IoT solution we all the time facing the same routine: creating network communication stack, log delivery functionality, event exchange between endpoints, etc.
The Kaa platform already provides all of this functionality, and you can get it out of the box using Kaa endpoint SDK.

An endpoint SDK is a library which provides communication, data marshalling, persistence, and other functions available in Kaa for specific type of an endpoint (e.g. [Java-based](Java), [C++-based](C++), [C-based](C), [Objective-C-based](Objective-C).
The SDK is designed to be embedded into your devices and managed applications, while Kaa cluster constitutes the middleware "cloud" basis for a particular solution.
The SDK works in conjunction with the cluster.
It is the responsibility of the Kaa client to process structured data provided by the Kaa server (configuration, notifications, etc.) and to supply data to the return path interfaces (profiles, logs, etc.).

Endpoint SDK helps to save time on development routine and allows to concentrate on your business logic.

Get SDK that perfectly fits your environment in [the Supported platforms page](Supported-platforms).

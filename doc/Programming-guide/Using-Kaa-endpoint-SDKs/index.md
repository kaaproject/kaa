    ---
layout: page
title: Using Kaa endpoint SDKs
permalink: /:path/
nav: /:path/Programming-guide/Using-Kaa-endpoint-SDKs
sort_idx: 40
---

* [Introduction](#introduction)

## Introduction

Developing some IoT solution we all the time facing the same routine - creating network communication stack, log delivery functionality, event exchange between endpoints and etcetera. All of this functionality already provides Kaa platform and you can get it out of the box using Kaa endpoint SDK.

An endpoint SDK is a library which provides communication, data marshalling, persistence, and other functions available in Kaa for specific type of an endpoint (e.g. [Java-based](Using-Kaa-Java-endpoint-SDK), [Android-based](), [C++-based](Using-Kaa-Cpp-endpoint-SDK), [C-based](Using-Kaa-C-endpoint-SDK), [Objective-C-based](Using-Kaa-Objective-C-endpoint-SDK)). The SDK is designed to be embedded into your devices and managed applications, while Kaa cluster constitutes the middleware "cloud" basis for a specific solution. The SDK works in conjunction with the cluster. It is the responsibility of the Kaa client to process structured data provided by the Kaa server (configuration, notifications, etc.) and to supply data to the return path interfaces (profiles, logs, etc.).

Endpoint SDK helps to save time on development routine and allows to concentrate on your business logic.

For more detailed overview refer to [Design reference]().


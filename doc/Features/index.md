---
layout: page
title: Features
permalink: /:path/
sort_idx: 3
---

{% include variables.md %}
{% include_relative links.md %}

Kaa is a **modular IoT platform** that leverages the **microservice architecture** for clear separation of concerns, scalability, and extensibility.
This documentation section discusses the most important **core features of the Kaa platform, their architecture, and how you can use them.**

| **Feature**                                   | **Description**                                                                                                              |
| --------------------------------------------- | -----------------------------------------------------------------------------------------------------------------------------|
| [**Device management**][identity]             | Record of **digital twins** of physical devices. **Device access credentials, metadata attributes, filtering and grouping.** |
| [**Communication**][communication]            | **Devices and gateways communication** enablement. **Authentication, access authorization, data exchange and multiplexing.** |
| [**Data collection**][data collection]        | Device **telemetry data collection** and storage. **Time series data, device logs, alerts. Connecting external systems.**    |
| [**Configuration management**][configuration] | Management and **distribution of the device configuration data**: individually or in bulk.                                   |
| [**Command invocation**][commands]            | **Remote control** of connected devices. Immediate and delayed **command invocation**.                                       |
| [**Software updates**][ota]                   | **Software version management** and upgrade orchestration. Targeted, gradual, and scheduled software roll-out.               |

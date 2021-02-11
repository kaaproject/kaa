---
layout: page
title: Getting started
permalink: /:path/
sort_idx: 1
---

{% include variables.md %}
{% include_relative links.md %}


Getting started tutorials are a great place to begin exploring the capabilities of the Kaa IoT platform.
A series of quick to complete guides will walk you through **the main Kaa features** and how to start using them.
We suggest that you follow getting started tutorials in sequence to get the most out of each.


| # | **Tutorial**                                                                                         | **Description**                                                                                                                                                                                                                  |
| - | ---------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1 | [**Connecting your first device**][connecting your first device]                                     | Connect a simulated device to the Kaa platform, submit some metadata attributes, and view them in the Kaa web interface.                                                                                                         |
| 2 | [**Collecting data from a device**][collecting data from a device]                                   | Collect telemetry data from a simulated device to the Kaa platform. You will learn how to transform collected data into well-structured time series using the auto-extraction feature and visualize it on the Kaa web interface. |
| 3 | [**Sending commands to device**][sending commands to device]                                         | Send commands to device, execute it and view the command execution result on Kaa UI.                                                                                                                                             |
| 4 | [**Open Distro alerting**][open distro alerting tutorial]                                            | Get notified via Slack when certain telemetry value from the endpoint exceeds defined threshold.                                                                                                                                 |
| 5 | [**Email alerting**][email alerting tutorial]                                                        | Get notified via email when certain telemetry value from the endpoint exceeds defined threshold.                                                                                                                                 |
| 6 | [**Authenticating client with SSL/TLS certificate**][authenticating client with tls certificate]     | Connect a device over one-way and two-way (mutual) MQTT over SSL/TLS. Authenticate device with X.509 certificate.                                                                                                                |
| 7 | [**User management**][user management]                                                               | Find out how to add new user, manage its permissions and restrict user access to applications, endpoints, dashboards.                                                                                                                |
| 8 | [**Custom web dashboard**][custom web dashboard]                                                               | Find out how to implement a web dashboard (interface) in vanilla JavaScript that integrates with the Kaa IoT platform.                                                                                                             |

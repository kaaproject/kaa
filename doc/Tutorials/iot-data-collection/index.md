---
layout: page
title: Data collection
permalink: /:path/
sort_idx: 2
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}

In this tutorial, we will look at how to collect telemetry data from a simulated device to the Kaa platform. We will learn how to transform
collected data into well-structured [time series](https://en.wikipedia.org/wiki/Time_series) and visualize it on the Kaa web interface.


## Overview

We will simulate the fleet management scenario. Our [endpoint][endpoint] will act as a device, which is integrated into a vehicle, and reports its
location (latitude and longitude), engine temperature and fuel level to the Kaa platform.

Also, we will configure [Web Dashboard][WD] with the [Endpoint Location][ep-location-widget] widget for historical and realtime vehicle location,
[Gauge][gauge-widget] widget for the current engine temperature and [Multi Series Chart][multi-series-chart-widget] widget for the fuel level.


![Solution blueprint](attach/img/architecture-overview.svg)


## Prerequisites

1. You know the Kaa platform [data collection basics][data collection].
2. You know [how to connect a device][how to connect device] to the Kaa platform.
3. The Kaa platform is up and running with the following microservices included:
   * [Kaa Protocol Communication (KPC)][KPC];
   * [Credential Management (CM)][CM];
   * [Data Collection Extension (DCX)][DCX];
   * [Endpoint Time Series (EPTS)][EPTS];
   * [Endpoint Register (EPR)][EPR];
   * [Web Dashboard (WD)][WD].
4. [Python 2.7](https://www.python.org/download/releases/2.7/) is installed on your machine.


## Playbook

**1**. Open Kaa Cluster [blueprint][blueprint] and configure [EPTS][epts ts definition] service with the `temperature`, `fuel_level` and `location` time series:

```yaml
kaa:
  applications:
    demo_application:
      versions:
        demo_application_v1:
      time-series:
        auto-extract: true # Extract all properties from data sample
        names:
          temperature:
            values:
              - path: "temperature" # Path to this field in incoming data sample. Required.
                name: "temperature" # Name of time series field that will be used in EPTS response body. If this configuration absent, EPTS will use "value" by default. Note: that one time series must not have several fields with name - "value".
                type: "number"      # Type of extracted field. Could be one of "String" or "Number".  The "Number" represents JSON number data type.
          fuel_level:
            values:
              - path: "fuelLevel"
                type: "number"
          location:
            values:
              - path: "latitude"
                name: "latitude"
                type: "number"
              - path: "longitude"
                name: "longitude"
                type: "number"
        timestamp:
          path: "timestamp"                      # JSON Path of the timestamp field in data sample
          format: "millis-unix-epoch"            # Timestamp format
          fallback-strategy: "server-timestamp"  # Take server timestamp if it is not present in data sample
```

Here `demo_application` [application][application] has three time series:
- `temperature` with `temperature` value;
- `fuel_level` with `fuelLevel` value;
- `location` with `latitude`, `longitude` values.

Also, configuration has data sample structure description for the devices in `demo_application_v1` [application version][application].

The timestamp is accessible by `$.timestamp` JSON path. It is in UNIX format in milliseconds.
When it is not present, EPTS will fall back to the timestamp of when data sample was received by the platform.

Read more about EPTS time series configuration [here][epts ts definition].

**2**. Run [Python simulator `client.py`][client_py] that simulates a vehicle.

```
python -t myToken -h {host} -p {port} client.py

INFO: Using endpoint token myToken, server at {host}:{port}
DEBUG: Composed data collection topic: kp1/demo_application_v1/dcx/myToken/json/23
INFO: Connecting to KPC instance at {host}:{port}...
INFO: Successfully connected
DEBUG: myToken: Sent next data: [{"latitude": 37.35119, "timestamp": 1555230312868, "temperature": 98, "longitude": -122.03248, "fuelLevel": 100}]
INFO: Message received: topic [kp1/demo_application_v1/dcx/myToken/json/23/status]
body []
...
```

**3**. Go to the Web Dashboard and configure an [Endpoint Location widget][ep-location-widget] to display current vehicle location.

<div align="center">
  <iframe width="640" height="385" src="https://www.youtube.com/embed/6T2F6gGupWg?rel=0" frameborder="0"
    allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
</div>

**4**. Configure a [Gauge widget][gauge-widget] to display current vehicle engine temperature.

<div align="center">
  <iframe width="640" height="385" src="https://www.youtube.com/embed/jbVPhQtKRLY?rel=0" frameborder="0"
    allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
</div>

**5**. Configure a [Multi Series Chart widget][multi-series-chart-widget] to display current vehicle fuel level.

<div align="center">
  <iframe width="640" height="385" src="https://www.youtube.com/embed/Tz6ZbTAs8XQ?rel=0" frameborder="0"
    allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
</div>

Now you can monitor vehicle location, fuel level and engine temperature in real time.

![vehicle-dashboard](attach/img/vehicle-dashboard.png)


## Resources

All tutorial resources are located on [GitHub][code_url].


[ep-location-widget]: {{wd_url}}Widgets/#ep-location
[gauge-widget]: {{wd_url}}Widgets/#gauge
[multi-series-chart-widget]: {{wd_url}}Widgets/#multi-series-chart

[epts ts definition]: {{epts_url}}Configuration/#time-series-definition

[code_url]:  https://github.com/kaaproject/kaa/tree/master/doc/Tutorials/iot-data-collection/attach/code
[client_py]: https://github.com/kaaproject/kaa/tree/master/doc/Tutorials/iot-data-collection/attach/code/client.py

---
layout: page
title: Kaa Cloud Getting Started
permalink: /:path/
sort_idx: 5
---

{% include variables.md %}
{% include_relative links.md %}
{% assign kaa_cloud_url = "https://cloud.kaaiot.com" %}
{% assign code_url = "https://github.com/kaaproject/kaa/tree/master/doc/Tutorials/getting-started-kaa-cloud/attach/code" %}

* TOC
{:toc}


## Overview

In this tutorial, we will look at how to get started with the [Kaa Cloud]({{kaa_cloud_url}}) in two simple steps:
1. Connect a [device simulator]({{code_url}}/client.py) to the Kaa Cloud server.
2. Send and visualize the device simulated data on [Web Dashboard][WD].


## Prerequisites

1. You have an account on the [Kaa Cloud]({{kaa_cloud_url}}).
2. [Python 2.7](https://www.python.org/download/releases/2.7/) is installed on your machine.


## Playbook

To help you get started with the Kaa Cloud real quick, we've created a [Python simulator]({{code_url}}/client.py) that connects to the platform and simulates a real device behavior, as follows: 

**1**. The simulator sends the following metadata to the platform on its start.

```json
{
    "serial": "00-14-22-01-23-45",
    "mac": "50:8c:b1:77:e8:e6"
}
```

**2**. The simulator sends telemetry data of the following format every 10 seconds.

```json
{
    "timestamp": 1569836796860,
    "temperature": 23,
    "log": "Randomly generated string: YjZgn3IbM6",
    "latitude": 37.34929,
    "longitude": -122.03247,
    "battery_level": 78.7
}
```

where: 
- `timestamp` - the current timestamp;
- `temperature` - a random number in the range of 20-25;
- `log` - a string with a randomly generated last part;
- `latitude` - latitude taken from the [file]({{code_url}}/location.json);
- `longitude` - longitude taken from the [file]({{code_url}}/location.json);
- `battery_level` - a number in the range of 0-100.

**3**. The simulator listens to the `HEALTH_CHECK` [command][commands] and responds with the following telemetry data upon receiving it.

```json
{
    "log": "Endpoint health status: OK"
}
```

<br/>

By default, a Kaa Cloud account is shipped with the preconfigured dashboards and widgets that visualize the above simulator data.
Also, the account includes pre-provisioned [endpoint filters]({{docs_url}}EPR/docs/current/Key-service-features/Ep-filters/) and [software (firmware) definitions][ota].

<br/>

Now let's proceed with the instructions to get you going.

**1**. Go to the "Devices" dashboard in your [Kaa Cloud account]({{kaa_cloud_url}}).

![Devices dashboard](attach/img/devices-dashboard.png)

As you can see from the empty [Endpoint List widget]({{docs_url}}WD/docs/current/Widgets/Ep-list/), there are no connected devices yet. 
Let's connect one.

<br/>

**2**. Clone the device simulator from [here]({{code_url}}).

The simulator consists of two files: 
- [client.py]({{code_url}}/client.py) - the device simulator code written in Python 2.7.
- [location.json]({{code_url}}/location.json) - geographical location data for the device live location simulation.

**Both files must be placed together in one directory.**

<br/>

**3**. Register the device digital twin. It's called "[endpoint][endpoint]" in Kaa.

![Device creation. The first step](attach/img/device-creation-1.png)

<br/>

**4**. Enter the desired [endpoint token][endpoint-token], device name, and description. 

![Device creation. Second step](attach/img/device-creation-2.png)

<br/>

**5**. Remember or save the endpoint token in some file because **you won't be able to see it again in the future**.
We will use the token in a bit to connect the simulator.

![Device creation. The third step](attach/img/device-creation-3.png)

<br/>

**6**. Go to the dashboard of the recently created endpoint and copy its [application version][application].

![Copy device application version](attach/img/device-application-version.png)

<br/>

**7**. Run the cloned simulator from a terminal.

Enter the directory with the cloned simulator (where `client.py` and `location.json` are located). 
Enter the following command replacing `{mySecretToken}` and `{myAppVersionName}` with your endpoint token and application version name, respectively.

```bash
$ python --token {mySecretToken} --appVersionName {myAppVersionName} client.py
```

After launching the above command, you should see the simulator logs.

```text
INFO: Using endpoint token myToken, server at cloud.kaaiot.com:30900
DEBUG: Composed data collection topic: kp1/6bf4ad4c-e3aa-4641-afc8-5c0f6f1f7ed16-v1/dcx/myToken/json/47
INFO: Connecting to KPC instance at cloud.kaaiot.com:30900...
INFO: Successfully connected
INFO: Sent metadata: {"serial": "00-14-22-01-23-45", "mac": "50:8c:b1:77:e:e6"}

DEBUG: myToken: Sent next data sample: [{"battery_level": 100.0, "temperature": 20, "timestamp": 1568807610661, "longitude": -122.03248, "latitude": 37.35119, "log": "Randomly generated string: UOL759IEKH"}]
INFO: Message received: topic [kp1/6bf4ad4c-e3aa-4641-afc8-5c0f6f1f7ed16-v1/dcx/myToken/json/47/status]
body []
DEBUG: myToken: Sent next data sample: [{"battery_level": 100.0, "temperature": 23, "timestamp": 1568807613666, "longitude": -122.03247, "latitude": 37.35077, "log": "Randomly generated string: QP3F8T7QJS"}]
INFO: Message received: topic [kp1/6bf4ad4c-e3aa-4641-afc8-5c0f6f1f7ed16-v1/dcx/myToken/json/47/status]
body []
```

<br/>

**8**. Go back to the dashboard of the recently connected endpoint and view the simulated data.

![Device dashboard of connected simulator](attach/img/visualized-device-data.png)

<br/>

Congratulations, you have connected your first device simulator to the Kaa Cloud! We encourage you to experiment with the simulator source code, modify the dashboards, and get back to us with the feedback!

<br/>

Explore the "Software management" dashboard and get acquainted with its capabilities. 

## Next steps

- [Communication][communication] - find out what is under the hood of the Kaa platform communication layer.
- [Device management][identity] - learn how the Kaa platform device management feature works.
- [Data collection][data collection] - learn how the Kaa platform data collection feature works.

---
layout: page
title: Connecting the BlitzWolf® BW-SHP6 WIFI smart socket
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}
{% include_relative links.md %}

* TOC
{:toc}

 
## Overview

In this tutorial, we will look at how to connect the [BlitzWolf® BW-SHP6 2300W WIFI Smart Socket][blitzwolf-smart-socket] to the Kaa platform using the default MQTT-based protocol.
You will learn how to connect the real device used for [Data Analytics and Notifications webinar][webinar Data Analytics and Notifications], publish the telemetry data and handle commands from the Kaa platform.


## Prerequisites

1. You know [how to connect a device][connecting your first device].
2. You have installed the [Arduino IDE][arduino-ide].


## Playbook

We will provide the custom software for the smart socket.
To do this repeat the steps from 1 to 7 from the [serial flash instructions guide](https://tasmota.github.io/docs/devices/BlitzWolf-SHP6/).

After that go through the below steps.

1. Before opening the Arduino IDE download and place the customized for the Blitzwolf BL0937 smart socket version of [HLW8012 library][HLW8012-library-url] to the `Arduino/libraries` folder. 
**Do not install the HLW8012 library from Arduino library manager.**
Other libraries may be installed from Arduino library manager. 

2. Open the [bw-shp6_kaa_example][code-url] project with an Arduino IDE and define bellow connection parameters in the `bw-shp6_kaa_example.ino` file.

```
#define WIFI_SSID "**********"          // your WiFi network SSID
#define WIFI_PASS "**********"          // your WiFi network password

#define KAA_HOST "**********"           // the KAA host address
#define KAA_PORT 1883
#define KAA_TOKEN "**********"          // your device token
#define KAA_APP_VERSION "**********"    // the application version you are working with
```

3. In the Arduino IDE select:

Tools->Board: Generic ESP8266 Module for BW-SHP6 10A.

Tools->Flash Size: 1M (no SPIFFS).  

Tools->Port: [your usb-serial adapter COM port].  

4. Upload the `bw-shp6_kaa_example` project to the ESP.

5. All programming part is done. Please finish all hardware preparation and make the smart socket able to connect to the power.   

Now our smart socket can send the telemetry data with the voltage, current, and power values.

Also, the device can receive commands from the Kaa platform to switch `On/Off` its state.


## Resources

* All tutorial resources are located on [GitHub][code-url].
* Customized for the Blitzwolf BL0937 smart socket version of the HLW8012 library [GitHub][HLW8012-library-url].

## Next steps

- Complete the [**Getting Started tutorials cycle**][getting started tutorials] with short tutorials about the main Kaa features.
- [Data Analytics and Notifications webinar][webinar Data Analytics and Notifications] - figure out how this smart socket used with Kaa platform for data analytics and notifications.
- [Device management][identity] - find out more about device management feature.


[HLW8012-library-url]:      https://github.com/kaaproject/hlw8012
[code-url]:                 https://github.com/kaaproject/kaa/tree/master/doc/Tutorials/device-integration/hardware-guides/connect-blitzwolf-smart-socket/attach/code
[arduino-ide]:              https://www.arduino.cc/en/Main/Software
[blitzwolf-smart-socket]:   https://www.blitzwolf.com/BlitzWolf-BW-SHP6-2300W-WIFI-Smart-Socket-EU-Plug-Works-with-Alexa-Remote-Control-Time-Switch-Electricity-Monitoring-p-300.html

---
layout: page
title: Using Kaa endpoint SDKs
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

In order to save developers' time, allow users concentrate on important business logic, and thus minimize the time to production, [Kaa platform]({{root_url}}) offers endpoint SDKs.
An endpoint SDK is a library that provides communication, data marshaling, persistence, and other functions available in Kaa for a specific [SDK type]({{root_url}}Glossary/#sdk-type).
Each SDK type is designed to be embedded into your [client application]({{root_url}}Glossary/#kaa-client) and works in conjunction with [Kaa cluster]({{root_url}}Glossary/#kaa-cluster) that serves as a cloud-based middleware for a particular IoT solution.

The following table provides platforms that were verified to support at least one type of the Kaa SDK (C, C++, Java, or Objective C). Click in an appropriate cell for the detailed instructions for your target platform and programming language.

{% capture sdk_c %}{{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C{% endcapture %}
{% capture LC %}{{sdk_c}}/SDK-Linux/{% endcapture %}
{% capture EC %}{{sdk_c}}/SDK-ESP8266/{% endcapture %}
{% capture TC %}{{sdk_c}}/SDK-TI-CC3200/{% endcapture %}
{% capture UC %}{{sdk_c}}/SDK-UDOO/{% endcapture %}
{% capture RC %}{{sdk_c}}/SDK-RPi/{% endcapture %}
{% capture BC %}{{sdk_c}}/SDK-Beaglebone/{% endcapture %}
{% capture QC %}{{sdk_c}}/SDK-QNX-Neutrino/{% endcapture %}

{% capture sdk_cpp %}{{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++{% endcapture %}
{% capture LCPP %}{{sdk_cpp}}/SDK-Linux/{% endcapture %}
{% capture RCPP %}{{sdk_cpp}}/SDK-RPi/{% endcapture %}
{% capture BCPP %}{{sdk_cpp}}/SDK-Beaglebone/{% endcapture %}
{% capture SCPP %}{{sdk_cpp}}/SDK-Samsung-Artik-5/{% endcapture %}
{% capture ICPP %}{{sdk_cpp}}/SDK-Edison/{% endcapture %}
{% capture WCPP %}{{sdk_cpp}}/SDK-Windows/{% endcapture %}

{% capture obj %}Objective-C{% endcapture %}
{% capture sdk_obj %}{{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/Objective-C{% endcapture %}
{% capture IO %}{{sdk_obj}}{% endcapture %}

{% capture sdk_java %}{{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/Java/{% endcapture %}
{% capture JJ %}{{sdk_java}}{% endcapture %}
{% capture X %}Supported{% endcapture %}

{% capture TICC3200 %}Texas Instruments CC3200{% endcapture %}

{: id="supportedPlatformsTable"}
| Platform           | [C]({{sdk_c}})  | [C++]({{sdk_cpp}}) | [{{obj}}]({{sdk_obj}}) | [Java]({{sdk_java}}) |
| -------            | ---             | ---                | ---                    | ---                  |
| Linux              | [{{X}}]({{LC}}) | [{{X}}]({{LCPP}})  |                        | [{{X}}]({{JJ}})      |
| Windows            |                 | [{{X}}]({{WCPP}})  |                        | [{{X}}]({{JJ}})      |
| QNX Neutrino RTOS  | [{{X}}]({{QC}}) |                    |                        |                      |
| Generic Desktop    |                 |                    |                        | [{{X}}]({{JJ}})      |
| Android            |                 |                    |                        | [{{X}}]({{JJ}})      |
| iOS                |                 |                    | [{{X}}]({{IO}})        |                      |
| Raspberry Pi       | [{{X}}]({{RC}}) | [{{X}}]({{RCPP}})  |                        |                      |
| Intel Edison       |                 | [{{X}}]({{ICPP}})  |                        |                      |
| Beaglebone         | [{{X}}]({{BC}}) | [{{X}}]({{BCPP}})  |                        |                      |
| Samsung Artik 5    |                 | [{{X}}]({{SCPP}})  |                        |                      |
| UDOO               | [{{X}}]({{UC}}) |                    |                        |                      |
| {{TICC3200}}       | [{{X}}]({{TC}}) |                    |                        |                      |
| ESP8266            | [{{X}}]({{EC}}) |                    |                        |                      |

---
layout: page
title: Supported platforms
permalink: /:path/
sort_idx: 60
---
{% include variables.md%}

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
{% capture IO %}{{sdk_obj}}/SDK-iOS/{% endcapture %}

{% capture sdk_java %}{{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/Java/{% endcapture %}
{% capture AJ %}{{sdk_java}}{% endcapture %}
{% capture DJ %}{{sdk_java}}{% endcapture %}
{% capture LJ %}{{sdk_java}}{% endcapture %}
{% capture X %}Supported{% endcapture %}

{% capture TICC3200 %}Texas Instruments CC3200{% endcapture %}

{: id="supportedPlatformsTable"}
| Platform           | [C]({{sdk_c}})  | [C++]({{sdk_cpp}}) | [{{obj}}]({{sdk_obj}}) | [Java]({{sdk_java}}) |
| -------            | ---             | ---                | ---                    | ---                  |
| Linux              | [{{X}}]({{LC}}) | [{{X}}]({{LCPP}})  |                        | [{{X}}]({{LJ}})      |
| ESP8266            | [{{X}}]({{EC}}) |                    |                        |                      |
| {{TICC3200}}       | [{{X}}]({{TC}}) |                    |                        |                      |
| UDOO               | [{{X}}]({{UC}}) |                    |                        |                      |
| Windows            |                 | [{{X}}]({{WCPP}})  |                        |                      |
| Raspberry Pi       | [{{X}}]({{RC}}) | [{{X}}]({{RCPP}})  |                        |                      |
| Beaglebone         | [{{X}}]({{BC}}) | [{{X}}]({{BCPP}})  |                        |                      |
| QNX Neutrino RTOS  | [{{X}}]({{QC}}) |                    |                        |                      |
| Samsung Artik 5    |                 | [{{X}}]({{SCPP}})  |                        |                      |
| Intel Edison       |                 | [{{X}}]({{ICPP}})  |                        |                      |
| iOS                |                 |                    | [{{X}}]({{IO}})        |                      |
| Android            |                 |                    |                        | [{{X}}]({{AJ}})      |
| Desktop            |                 |                    |                        | [{{X}}]({{DJ}})      |

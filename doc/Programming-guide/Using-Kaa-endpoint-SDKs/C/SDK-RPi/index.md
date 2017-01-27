---
layout: page
title: Raspberry Pi
permalink: /:path/
sort_idx: 60
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to cross-compile [Kaa C SDK]({{root_url}}Glossary/#kaa-sdk-type) for [Raspberry Pi](https://www.raspberrypi.org/).
Alternatively, you can build the Kaa C SDK directly on the Raspberry Pi board.
For more information, see [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/) for Kaa C SDK.

>**NOTE:** This guide is verified against:
>
> * **Host OS:** Ubuntu 14.04 LTS Desktop 64-bit
> * **Device:** Raspberry Pi 3
> * **Target OS:** [Raspbian Jessie](https://www.raspberrypi.org/downloads/)
{:.note}

## Prerequisites

Perform the following instructions on the host machine:

1. Install build prerequisites.

   ```bash
   sudo apt-get install cmake build-essential
   ```

2. Install toolchain.

   ```bash
   mkdir rpi_root && cd rpi_root
   git clone https://github.com/raspberrypi/tools.git
   export ARMLINUX_GCC=$(pwd)/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian-x64/bin/arm-linux-gnueabihf-gcc
   ```

## Build Kaa application

After you installed the required dependencies and built the C SDK, you can build and run your [Kaa application]({{root_url}}Glossary/#kaa-application).

Since Raspberry runs on Linux, you can use the [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/#c-sdk-build) to build and run your application.

>**NOTE:** Make sure to specify correct compiler name when compiling your Kaa application for Raspberry Pi:
>
>```bash
>cmake -DKAA_MAX_LOG_LEVEL=3 -DCMAKE_C_COMPILER=$ARMLINUX_GCC -DBUILD_TESTING=OFF ..
>make
>```
{:.note}
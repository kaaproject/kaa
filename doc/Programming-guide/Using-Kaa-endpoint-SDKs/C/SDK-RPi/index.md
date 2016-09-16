---
layout: page
title: Raspberry Pi
permalink: /:path/
sort_idx: 60
---

{% include variables.md %}

* TOC
{:toc}

The guide provides information on how to **cross-compile** C SDK for Raspberry PI.
Alternatively, you can build the Kaa C endpoint SDK right on the Raspberry Pi board.
Refer to [the Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/) for further details.

## Install dependencies

**All steps described here were tested on:**

 - **Host OS:** Ubuntu 14.04 LTS Desktop 64-bit
 - **Device:** Raspberry Pi 3
 - **Target OS:** [Raspbian Jessie](https://www.raspberrypi.org/downloads/)

**The further instructions must be executed on the host machine.**

1. Install build prerequisites.

        sudo apt-get install cmake build-essential

1. Install toolchain.

        mkdir rpi_root && cd rpi_root
        git clone https://github.com/raspberrypi/tools.git
        export ARMLINUX_GCC=$(pwd)/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian-x64/bin/arm-linux-gnueabihf-gcc

## Create application

Now, dependencies are installed and it is time to create Kaa application.
Since Raspberry is running Linux, you can refer to [the Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/#c-sdk-build) for detailed process of application creation.
But remember, you must specify correct compiler name when compiling your Kaa application for Raspberry Pi:

        cmake -DKAA_MAX_LOG_LEVEL=3 -DCMAKE_C_COMPILER=$ARMLINUX_GCC ..
        make

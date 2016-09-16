---
layout: page
title: Beaglebone
permalink: /:path/
sort_idx: 70
---

{% include variables.md %}

* TOC
{:toc}

The guide provides information on how to **cross-compile** C SDK for BeagleBone or BeagleBone Black.
Alternatively, you can build the Kaa C endpoint SDK right on the BeagleBone board.
Refer to [the Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/) for further details.

## Install dependencies

**All steps described here were tested on:**

 - **Host OS:** Ubuntu 14.04 LTS Desktop 64-bit
 - **Target OS:** [Debian 8.5](http://beagleboard.org/latest-images)
 - **Device:** [BeagleBone](http://beagleboard.org/bone-original)

**The further instructions must be executed on the host machine.**

### Download and install toolchain

        mkdir bb_root && cd bb_root
        wget -c https://releases.linaro.org/components/toolchain/binaries/5.3-2016.02/arm-linux-gnueabihf/gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf.tar.xz
        tar xf gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf.tar.xz
        export TOOLCHAIN_PATH=$(pwd)/gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf/bin

## Create application
Now, the toolchain is installed, and it is time to create Kaa application.
Since BeagleBone (BeagleBone Black) is running Linux, you can refer to [the Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/#c-sdk-build) for the detailed process of application creation.
However, remember, you must specify [the CMake C Compiler](http://www.vtk.org/Wiki/CMake_Cross_Compiling#Setting_up_the_system_and_toolchain) when compiling your Kaa application for BeagleBone:

        cmake -DKAA_MAX_LOG_LEVEL=3 -DCMAKE_C_COMPILER="$TOOLCHAIN_PATH/arm-linux-gnueabihf-gcc" ..
        make

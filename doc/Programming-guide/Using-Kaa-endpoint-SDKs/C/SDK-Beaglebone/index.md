---
layout: page
title: BeagleBone
permalink: /:path/
sort_idx: 70
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to cross-compile [Kaa C SDK]({{root_url}}Glossary/#kaa-sdk-type) for [BeagleBone](https://beagleboard.org/bone) or [BeagleBone Black](https://beagleboard.org/black).
Alternatively, you can build the Kaa C SDK directly on the BeagleBone board.
For more information, see [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/) for Kaa C SDK.

>**NOTE:** This guide is verified against:
>
> * **Host OS:** Ubuntu 14.04 LTS Desktop 64-bit
> * **Target OS:** [Debian 8.5](http://beagleboard.org/latest-images)
> * **Device:** [BeagleBone](http://beagleboard.org/bone-original)
{:.note}

Install the toolchain by running the below commands on the host machine.

```bash
mkdir bb_root && cd bb_root
wget -c https://releases.linaro.org/components/toolchain/binaries/5.3-2016.02/arm-linux-gnueabihf/gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf.tar.xz
tar xf gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf.tar.xz
export TOOLCHAIN_PATH=$(pwd)/gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf/bin
```

After you installed the required dependencies and built the C SDK, you can build and run your [Kaa application]({{root_url}}Glossary/#kaa-application).

Since BeagleBone (BeagleBone Black) runs on Linux, you can use the [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/#build-c-sdk) to build and run your application.

>**NOTE:** Make sure to specify a [CMake C Compiler](http://www.vtk.org/Wiki/CMake_Cross_Compiling#Setting_up_the_system_and_toolchain) when compiling your Kaa application for BeagleBone.
>
>```bash
>cmake -DKAA_MAX_LOG_LEVEL=3 -DCMAKE_C_COMPILER="$TOOLCHAIN_PATH/arm-linux-gnueabihf-gcc" -DBUILD_TESTING=OFF ..
>make
>```
{:.note}
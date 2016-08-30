---
layout: page
title: QNX Neutrino RTOS
permalink: /:path/
sort_idx: 90
---

* TOC
{:toc}

{% include variables.md %}

The guide explains how to cross-compile applications for [QNX Neutrino RTOS 6.6](http://www.qnx.com/products/neutrino-rtos/neutrino-rtos.html) based on the Kaa C endpoint SDK.

**NOTE:** The further instructions are expected to be executed on the host machine.

Verified against:

- **Host OS:** **TODO:** [KAA-1241](http://jira.kaaproject.org/browse/KAA-1241) retest this guide against Ubuntu 14.04 LTS 64-bit and Kaa C SDK v.0.10.0.
- **Target OS:** QNX Neutrino RTOS 6.6.

# Installing QNX Software Development Platform

To install QNX *Software Development Platform (SDP)*, proceed as follows:

1. [Register](https://www.qnx.com/account/login.html) as a developer and download the following components:
    - [QNX Software Development Platform 6.6](http://www.qnx.com/download/feature.html?programid=26114)
    - [QNX Software Development Platform 6.6 Applypatch Patch [Patch ID 4024]](http://www.qnx.com/download/feature.html?programid=26817)
    - [QNX Software Development Platform 6.6 Header Files Patch [Patch ID 3851]](http://www.qnx.com/download/feature.html?programid=26447)

1. Install [SDP](http://www.qnx.com/developers/articles/inst_5847_9.html). Note: It is recommended to install SDP in the default directory (for Linux platforms, it is `/opt/qnx660`).
1. Install [Applypatch](http://www.qnx.com/developers/articles/inst_6085_3.html) and [Header Files Patch](http://www.qnx.com/developers/articles/inst_5946_5.html).

# Configuring the environment

1. Install build dependencies.

        sudo apt-get install cmake build-essential

1. Set the path to the root directory of SDP.

        export QNX_SDK_HOME="<path_to_qnx_sdk_home>"

    **Default:** `/opt/qnx660`

1. Set the target architecture.

        export QNX_TARGET_ARCH=<architecture>

    **Supported architectures:**

    - `gcc_ntoarmv7le_cpp-ne`
    - `gcc_ntox86_cpp-ne`
    - `gcc_ntox86_gpp`
    - `gcc_ntoarmv7le`
    - `gcc_ntox86`
    - `gcc_ntoarmv7le_cpp`
    - `gcc_ntoarmv7le_gpp`
    - `gcc_ntox86_cpp`

    **Default:** `gcc_ntox86`

1. Set paths to host and target SDP files.

        export QNX_HOST="$QNX_SDK_HOME/host/linux/x86"
        export QNX_TARGET="$QNX_SDK_HOME/target/qnx6"
        export PATH="$QNX_HOST/usr/bin:$PATH"

# Creating applications based on C SDK

Since QNX is a POSIX-compliant system, [the Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/#c-sdk-build) can be followed to create applications based on C SDK.
During CMake configuration and building step, make sure [proper CMake toolchain is used](https://cmake.org/cmake/help/v3.0/manual/cmake-toolchains.7.html).
Kaa C SDK already provides the toolchain file for QNX as shown below.

```bash
mkdir -p build
cd build
cmake -DCMAKE_TOOLCHAIN_FILE=../toolchains/qnx.cmake ..
make
````

# Exporting application (if SSH is enabled)

If present, you can use SSH on a QNX-running device to transfer the application.

```bash
scp <app_name> <user>@<ip_of_target_machine>:<app_name>
```

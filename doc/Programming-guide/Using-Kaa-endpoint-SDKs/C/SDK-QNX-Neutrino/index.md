---
layout: page
title: QNX Neutrino RTOS
permalink: /:path/
sort_idx: 90
---

* TOC
{:toc}

{% include variables.md %}

The guide explains how to cross-compile [Kaa C SDK]({{root_url}}Glossary/#kaa-sdk-type) for [QNX Neutrino RTOS 6.6](http://www.qnx.com/products/neutrino-rtos/neutrino-rtos.html) and create [Kaa applications]({{root_url}}Glossary/#kaa-application).


>**NOTE:** This guide is verified against:
>
> * **Target OS:** QNX Neutrino RTOS 6.6.
{:.note}

## Prerequisites

Prior to building Kaa C SDK, install [QNX Software Development Platform (SDP)](http://www.qnx.com/download/group.html?programid=26071).
To do this:

1. [Register](https://www.qnx.com/account/login.html) as a developer and download the following components:
  * [QNX Software Development Platform 6.6](http://www.qnx.com/download/feature.html?programid=26114)
  * [QNX Software Development Platform 6.6 Applypatch Patch [Patch ID 4024]](http://www.qnx.com/download/feature.html?programid=26817)
  * [QNX Software Development Platform 6.6 Header Files Patch [Patch ID 3851]](http://www.qnx.com/download/feature.html?programid=26447)

2. Install [SDP](http://www.qnx.com/developers/articles/inst_5847_9.html).

   >**NOTE:** It is recommended that you install SDP in the default directory (`/opt/qnx660` for Linux platforms).
   {:.note}

3. Install [Applypatch](http://www.qnx.com/developers/articles/inst_6085_3.html) and [Header Files Patch](http://www.qnx.com/developers/articles/inst_5946_5.html).

## Configure build environment

To configure your build environment:

1. Install build dependencies.

   ```bash
   sudo apt-get install cmake build-essential
   ```

2. Set the path to the root directory of SDP.

   ```bash
   export QNX_SDK_HOME="<path_to_qnx_sdk_home>"
   ```
    Default value: `/opt/qnx660`

3. Set the target architecture.

   ```bash
   export QNX_TARGET_ARCH=<architecture>
   ```

    Supported architectures:

    - `gcc_ntoarmv7le_cpp-ne`
    - `gcc_ntox86_cpp-ne`
    - `gcc_ntox86_gpp`
    - `gcc_ntoarmv7le`
    - `gcc_ntox86`
    - `gcc_ntoarmv7le_cpp`
    - `gcc_ntoarmv7le_gpp`
    - `gcc_ntox86_cpp`

    Default value: `gcc_ntox86`

4. Set paths to host and target SDP files.

   ```bash
   export QNX_HOST="$QNX_SDK_HOME/host/linux/x86"
   export QNX_TARGET="$QNX_SDK_HOME/target/qnx6"
   export PATH="$QNX_HOST/usr/bin:$PATH"
   ```

## Build Kaa application

Since QNX is a POSIX-compliant system, you can use the [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/#build-c-sdk) to build and run your application.

>**NOTE:** During CMake configuration and building step, make sure to use [proper CMake toolchain](https://cmake.org/cmake/help/v3.0/manual/cmake-toolchains.7.html).
{:.note}

Kaa C SDK provides the toolchain file for QNX as shown below.

```bash
mkdir -p build
cd build
cmake -DCMAKE_TOOLCHAIN_FILE=../toolchains/qnx.cmake -DBUILD_TESTING=OFF ..
make
````

### Exporting application

If you have SSH enabled, you can use it on a QNX-running device to transfer your application.

```bash
scp <app_name> <user>@<ip_of_target_machine>:<app_name>
```
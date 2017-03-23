---
layout: page
title: Intel Edison
permalink: /:path/
sort_idx: 60
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to cross-compile [Kaa C SDK]({{root_url}}Glossary/#kaa-sdk-type) for [Intel Edison](https://software.intel.com/en-us/iot/hardware/edison).
Alternatively, you can build the Kaa C SDK directly on the Intel Edison board.
For more information, see [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/) for Kaa C SDK.

>**NOTE:** This guide is verified against:
>
> * **Host OS:** Ubuntu 16.04 LTS Desktop 64-bit
> * **Target OS:** Poky (Yocto Project Reference Distro) 1.7.3, kernel version 3.10.17-poky-edison+
{:.note}

## Prerequisites

Perform the following instructions on the host machine:

1. Download the [cross compile tools](https://downloadcenter.intel.com/download/24472/Cross-Compiler-Toolchain-for-Intel-Edison-Maker-Board) for your platform, 32-bit or 64-bit version.
Unpack the downloaded archive. <!--(don't forget to change the file name to proper one)-->

   ```
   tar -xvf edison-toolchain-20150120-linux64.tar.bz2
   ```
   
2. Install the toolchain.

   ```bash
   cd i686
   ./install_script.sh
   ```
   
    While running the installation script, you may get an error message: `find: invalid mode '+111'`.
    Fix it by running the command below.

   ```bash
   sed -i 's:+111:/111:' install_script.sh
   ```
   
    The cross-compilation toolchain is installed to the current directory by default.
    On some configurations, the script installs the toolchain to `/opt/poky-edison/1.6.1/`.

3. Install [Cmake](https://cmake.org/):

   ```bash
   sudo apt-get install cmake
   ```
   
## Build Kaa application

After you installed the required dependencies and built the C SDK, you can build and run your [Kaa application]({{root_url}}Glossary/#kaa-application).

Since Edison runs on Linux, you can use the [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/#build-c-sdk) to build and run your application.

>**NOTE:** Make sure to specify correct compiler name when compiling your Kaa application for Edison:
>
>```bash
>cmake -DKAA_MAX_LOG_LEVEL=3 -DCMAKE_TOOLCHAIN_FILE=PATH_TO_KAA_SDK/toolchains/edison.cmake -DEDISON_SDK_ROOT=PATH_TO_EDISON_SDK -DBUILD_TESTING=OFF ..
>make
>```
>Replace `PATH_TO_KAA_SDK` with the path to Kaa C SDK relative to the `build` directory, and `PATH_TO_EDISON_SDK` with the absolute path to the Edison SDK installation directory
{:.note}

For more information on how to build, upload and run your application on Edison board, see [official user guide](https://software.intel.com/en-us/intel-edison-board-user-guide).
---
layout: page
title: BeagleBone
permalink: /:path/
sort_idx: 30
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to cross-compile [Kaa C++ SDK]({{root_url}}Glossary/#kaa-sdk-type) for [BeagleBone](https://beagleboard.org/bone) or [BeagleBone Black](https://beagleboard.org/black).
Alternatively, you can build the Kaa C++ SDK directly on the BeagleBone board.
For more information, see [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/SDK-Linux/) for Kaa C++ SDK.

## Prerequisites

>**NOTE:** This guide is verified against:
>
> * **Host OS:** Ubuntu 14.04 LTS Desktop 64-bit
> * **Target OS:** [Debian 8.5](http://beagleboard.org/latest-images)
> * **Device:** [BeagleBone](http://beagleboard.org/bone-original)
{:.note}

Perform the following instructions on the host machine:

1. Install [CMake](https://cmake.org/).

   ```bash
   sudo apt-get install cmake build-essential
   ```

2. Download toolchain.

   ```bash
   mkdir bb_root && cd bb_root
   export BB_ROOT
   BB_ROOT="$(pwd)"
   wget -c https://releases.linaro.org/components/toolchain/binaries/5.3-2016.02/arm-linux-gnueabihf/gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf.tar.xz
   tar xf gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf.tar.xz
   export CROSS="${BB_ROOT}"/gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf/bin/arm-linux-gnueabihf-
   export SYSROOT="${BB_ROOT}"/gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf/libc/usr
   ```

3. Download and install [zlib](http://www.zlib.net/).

   ```bash
   cd "${BB_ROOT}"
   wget http://zlib.net/zlib-1.2.11.tar.gz
   tar -xvzf zlib-1.2.11.tar.gz
   cd zlib-1.2.8
   CROSS_PREFIX="${CROSS}" ./configure --prefix="${SYSROOT}"
   make && make install
   ```

4. Download and install [bzip2](http://www.bzip.org/).

   ```bash
   cd "${BB_ROOT}"
   wget http://www.bzip.org/1.0.6/bzip2-1.0.6.tar.gz
   tar -xvzf bzip2-1.0.6.tar.gz
   cd bzip2-1.0.6
   make CC="${CROSS}"gcc AR="${CROSS}"ar LD="${CROSS}"ld AS="${CROSS}"as PREFIX="${SYSROOT}" install
   ```

5. Download and install [Boost](http://www.boost.org/users/download/).

   ```bash
   cd "${BB_ROOT}"
   wget -O boost_1_59_0.tar.bz2 http://sourceforge.net/projects/boost/files/boost/1.59.0/boost_1_59_0.tar.bz2/download
   tar -xvf boost_1_59_0.tar.bz2
   cd boost_1_59_0
   ./bootstrap.sh
   sed -r 's#(using\s+gcc)(\s+;)#\1 : arm : '${CROSS}c++'\2#g' -i  project-config.jam
   ./b2 install --prefix="${SYSROOT}"
   ```

6. Install [Apache Avro](https://avro.apache.org/) for the host and target machines.
Avro depends on some Boost components, therefore it is critical that you install them as described in the previous step.

    Install Avro for host machine.

   ```bash
   cd "${BB_ROOT}"
   sudo apt-get install libboost-dev libboost-filesystem-dev libboost-iostreams-dev libboost-program-options-dev libboost-system-dev
   wget https://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
   mkdir -p avro/host
   tar -xvzf avro-cpp-1.7.5.tar.gz -C ./avro/host
   cd ./avro/host/avro-cpp-1.7.5
   mkdir build && cd build
   
   cmake ..
   make
   sudo make install
   ```
   
    Patch and install Avro for target machine.

   ```bash
   cd "${BB_ROOT}"
   mkdir -p avro/target
   tar -xvzf avro-cpp-1.7.5.tar.gz -C ./avro/target
   cd ./avro/target/avro-cpp-1.7.5
   
   wget {{github_url_raw}}client/client-multi/client-cpp/tools/avro-cpp-disable-tests.patch
   patch < avro-cpp-disable-tests.patch
   mkdir build && cd build
   cmake -DCMAKE_INSTALL_PREFIX:PATH="${SYSROOT}" -DCMAKE_CXX_COMPILER="${CROSS}"g++ -DCMAKE_FIND_ROOT_PATH="${SYSROOT}" ..
   make && make install
   ```

7. Build and install [Botan](https://botan.randombit.net/).

   ```bash
   cd "${BB_ROOT}"
   wget http://botan.randombit.net/releases/Botan-1.11.27.tgz
   tar -xvzf Botan-1.11.27.tgz
   cd Botan-1.11.27
   ./configure.py --cpu=arm --cc-bin="${CROSS}"g++ --prefix="${SYSROOT}"
   make && make install
   ```

## Create application

After you installed the required dependencies and built the C++ SDK, you can build and run your [Kaa application]({{root_url}}Glossary/#kaa-application).

Since BeagleBone (BeagleBone Black) runs on Linux, you can use the [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/SDK-Linux/#build-kaa-application) to build and run your application.

>**NOTE**: The [CMAKE_CXX_COMPILER](https://cmake.org/cmake/help/v3.0/variable/CMAKE_LANG_COMPILER.html) specifies C++ compiler to use and [CMAKE_FIND_ROOT_PATH](https://cmake.org/cmake/help/v3.0/variable/CMAKE_FIND_ROOT_PATH.html) tells CMake where to look for required libraries installed in the previous step.
>
>```bash
>cmake -DKAA_MAX_LOG_LEVEL=3 -DCMAKE_CXX_COMPILER="${CROSS}"g++ -DCMAKE_FIND_ROOT_PATH="${SYSROOT}" ..
>make
>```
{:.note}
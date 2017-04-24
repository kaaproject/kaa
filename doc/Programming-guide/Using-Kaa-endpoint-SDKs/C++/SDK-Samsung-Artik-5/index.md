---
layout: page
title: Samsung Artik 5
permalink: /:path/
sort_idx: 50
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to cross-compile [Kaa C++ SDK]({{root_url}}Glossary/#kaa-sdk-type) for [Samsung Artik 5](https://www.artik.io/modules/artik-520/).

If this is the first time you use the Artik board, see [official Artik getting started guide](https://developer.artik.io/documentation/getting-started-beta/) on how to configure your board and make it accessible through both serial terminal and the ssh client.

>**NOTE:** This guide is verified against:
>
> * **Host OS:** Ubuntu 14.04 LTS Desktop 64-bit
> * **Device:** Samsung Artik 5
{:.note}

## Prerequisites

Perform the following instructions on the host machine:

1. Install [CMake](https://cmake.org/).

   ```bash
   sudo apt-get install cmake
   ```

2. If your host system has 64-bit architecture, you must also install 32-bit version of some packages to be able to run 32-bit toolchain.

   ```bash
   sudo dpkg --add-architecture i386
   sudo apt-get update
   sudo apt-get install lib32z1 lib32ncurses5 lib32bz2-1.0 libstdc++6:i386
   ```

Install the required third-party components:

1. Install the toolchain.

   ```bash
   mkdir artik_root && cd artik_root
   export ARTIK_ROOT="$(pwd)"
   wget https://launchpad.net/linaro-toolchain-binaries/trunk/2013.10/+download/gcc-linaro-arm-linux-gnueabihf-4.8-2013.10_linux.tar.bz2
   tar xvjf gcc-linaro-arm-linux-gnueabihf-4.8-2013.10_linux.tar.bz2
   mv gcc-linaro-arm-linux-gnueabihf-4.8-2013.10_linux "${ARTIK_ROOT}"/gcc-linaro-arm-linux-gnueabihf
   ```

2. Install [zlib](http://www.zlib.net/) (for Avro version newer than 1.7.5).

   ```bash
   cd "${ARTIK_ROOT}"
   wget http://zlib.net/zlib-1.2.11.tar.gz
   tar -xvzf zlib-1.2.11.tar.gz
   export INSTALLDIR="${ARTIK_ROOT}"/gcc-linaro-arm-linux-gnueabihf/libc/usr
   export PATH="${ARTIK_ROOT}"/gcc-linaro-arm-linux-gnueabihf/bin:"${PATH}"
   export TARGETMACH=arm-linux-gnueabi
   export BUILDMACH=i686-pc-linux-gnu
   export CROSS=arm-linux-gnueabihf-
   cd zlib-1.2.8
   CROSS_PREFIX="${CROSS}" ./configure --prefix="${INSTALLDIR}"
   make install
   ```

3. Install [bzip2](http://www.bzip.org/) (for Avro version newer than 1.7.5).

   ```bash
   cd "${ARTIK_ROOT}"
   wget http://www.bzip.org/1.0.6/bzip2-1.0.6.tar.gz
   tar -xvzf bzip2-1.0.6.tar.gz
   cd bzip2-1.0.6
   sed -e "/^all:/s/ test//" Makefile > Makefile-libbz2_so
   make -f Makefile-libbz2_so CC="${CROSS}"gcc AR="${CROSS}"ar
   make PREFIX="${INSTALLDIR}" install
   ```

4. Install [Boost](http://www.boost.org/users/download/) (1.54 or higher).

   ```bash
   cd "${ARTIK_ROOT}"
   wget -O boost_1_59_0.tar.bz2 http://sourceforge.net/projects/boost/files/boost/1.59.0/boost_1_59_0.tar.bz2/download
   tar xvjf boost_1_59_0.tar.bz2
   cd boost_1_59_0
   ./bootstrap.sh
   sed -r 's/(using\s+gcc)(\s+;)/\1 : arm : arm-linux-gnueabihf-c++\2/g' -i project-config.jam
   ./b2 install toolset=gcc-arm --prefix="${INSTALLDIR}"
   ```

5. Install [Apache Avro](https://avro.apache.org/).

   ```bash
   cd "${ARTIK_ROOT}"
   wget https://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
   tar zxf avro-cpp-1.7.5.tar.gz
   cd avro-cpp-1.7.5
   wget {{github_url_raw}}client/client-multi/client-cpp/tools/avro-cpp-disable-tests.patch
   patch < avro-cpp-disable-tests.patch
   mkdir build && cd build
   cmake -DCMAKE_INSTALL_PREFIX:PATH="${INSTALLDIR}" -DCMAKE_CXX_COMPILER="${CROSS}"g++ -DCMAKE_FIND_ROOT_PATH="${INSTALLDIR}" ..
   make && make install
   ```

6. Install [Botan](https://botan.randombit.net/).

   ```bash
   cd "${ARTIK_ROOT}"
   wget http://botan.randombit.net/releases/Botan-1.11.28.tgz
   tar -xvzf Botan-1.11.28.tgz
   cd Botan-1.11.28
   python configure.py --cpu=arm --cc-bin="${CROSS}"g++ --prefix="${INSTALLDIR}"
   make install
   ```

7. Copy libraries to the Artik device.

   ```bash
   scp "${INSTALLDIR}"/lib/libboost_*.so* root@<put Artik ip address here>:/usr/lib
   scp "${INSTALLDIR}"/lib/libbotan*.so* root@<put Artik ip address here>:/usr/lib
   scp "${INSTALLDIR}"/lib/libavrocpp*.so* root@<put Artik ip address here>:/usr/lib
   ```

## Create application

After you installed the required dependencies and built the C++ SDK, you can build and run your [Kaa application]({{root_url}}Glossary/#kaa-application).

Since Artik runs on Linux, you can use the [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/SDK-Linux/#build-kaa-application) to build and run your application.

>**NOTE**: The [CMAKE_CXX_COMPILER](https://cmake.org/cmake/help/v3.0/variable/CMAKE_LANG_COMPILER.html) specifies C++ compiler to use and [CMAKE_FIND_ROOT_PATH](https://cmake.org/cmake/help/v3.0/variable/CMAKE_FIND_ROOT_PATH.html) tells CMake where to look for required libraries installed in previous step.
>
>```bash
>cmake -DKAA_MAX_LOG_LEVEL=3 -DCMAKE_CXX_COMPILER="${CROSS}"g++ -DCMAKE_FIND_ROOT_PATH="${INSTALLDIR}" ..
>make
>```
{:.note}
---
layout: page
title: Raspberry Pi
permalink: /:path/
sort_idx: 20
---

{% include variables.md %}

* TOC
{:toc}

This guide explains how to cross-compile [Kaa C++ SDK]({{root_url}}Glossary/#kaa-sdk-type) for [Raspberry Pi](https://www.raspberrypi.org/).
Alternatively, you can build the Kaa C++ SDK directly on the Raspberry Pi board.
For more information, see [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/SDK-Linux/) for Kaa C++ SDK.

>**NOTE:** This guide is verified against:
>
> * **Host OS:** Ubuntu 14.04 LTS Desktop 64-bit
> * **Device:** Raspberry Pi 3
> * **Target OS:** [Raspbian Jessie](https://www.raspberrypi.org/downloads/)
{:.note}

Perform the following instructions on the host machine:

1. Download and install toolchain.

   ```bash
   mkdir rpi_root && cd rpi_root
   export RPI_ROOT=$(pwd)
   git clone --depth 1 https://github.com/raspberrypi/tools.git
   export CROSS=arm-linux-gnuebihf
   ```

   <ul class="nav nav-tabs">
   <li class="active"><a data-toggle="tab" href="#32">x86-32 OS</a></li>
   <li><a data-toggle="tab" href="#64">x86-64 OS</a></li>
   </ul>

   <div class="tab-content"><div id="32" class="tab-pane fade in active" markdown="1" >

   ```bash
   export INSTALLDIR=$RPI_ROOT/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian/arm-linux-gnueabihf/libc/usr
   export PATH=$RPI_ROOT/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian/bin:$PATH
   ```

   </div><div id="64" class="tab-pane fade" markdown="1" >

   ```bash
   export INSTALLDIR=$RPI_ROOT/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian-x64/arm-linux-gnueabihf/libc/usr
   export PATH=$RPI_ROOT/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian-x64/bin:$PATH
   ```

   </div>
   </div>

2. Download and install [zlib](http://www.zlib.net/).

   ```bash
   cd $RPI_ROOT
   wget http://zlib.net/zlib-1.2.11.tar.gz
   tar -xvzf zlib-1.2.11.tar.gz
   cd zlib-1.2.8
   CROSS_PREFIX="${CROSS}-" ./configure --prefix=${INSTALLDIR}
   make && make install
   ```

3. Download and install [bzip2](http://www.bzip.org/).

   ```bash
   cd $RPI_ROOT
   wget http://www.bzip.org/1.0.6/bzip2-1.0.6.tar.gz
   tar -xvzf bzip2-1.0.6.tar.gz
   cd bzip2-1.0.6
   sed -e "/^all:/s/ test//" -i Makefile
   make CC="${CROSS}-gcc" AR="${CROSS}-ar" LD="${CROSS}-ld" AS="${CROSS}-as"
   make PREFIX=${INSTALLDIR} install
   ```

4. Download and install [Boost](http://www.boost.org/users/download/).

   ```bash
   cd $RPI_ROOT
   wget -O boost_1_59_0.tar.bz2 http://sourceforge.net/projects/boost/files/boost/1.59.0/boost_1_59_0.tar.bz2/download
   tar -xvf boost_1_59_0.tar.bz2
   cd boost_1_59_0
   ./bootstrap.sh
   sed -r 's/(using\s+gcc)(\s+;)/\1 : arm : arm-linux-gnueabihf-c++\2/g' -i project-config.jam
   ./bjam install toolset=gcc-arm --prefix=${INSTALLDIR}
   ```

5. Install [Apache Avro](https://avro.apache.org/) for the host and target machines.
Avro depends on some Boost components, therefore it is critical that you install them as described in the previous step.

    Install Avro for host machine.
    
   ```bash
   cd $RPI_ROOT
   sudo apt-get install libboost-dev libboost-filesystem-dev libboost-iostreams-dev libboost-program-options-dev libboost-system-dev
   wget https://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
   mkdir -p avro/host
   tar -xvzf avro-cpp-1.7.5.tar.gz -C ./avro/host
   cd ./avro/host/avro-cpp-1.7.5
   mkdir build && cd build

   cmake ..
   sudo make install
   ```

    Patch and install Avro for target machine.

   ```bash
   cd $RPI_ROOT
   mkdir -p avro/target
   tar -xvzf avro-cpp-1.7.5.tar.gz -C ./avro/target
   cd ./avro/target/avro-cpp-1.7.5

   wget {{github_url_raw}}client/client-multi/client-c/toolchains/rpi.cmake
   wget {{github_url_raw}}client/client-multi/client-cpp/tools/avro-cpp-disable-tests.patch
   patch < avro-cpp-disable-tests.patch
   mkdir build && cd build
   cmake -DCMAKE_INSTALL_PREFIX:PATH=${INSTALLDIR} -DCMAKE_TOOLCHAIN_FILE=../rpi.cmake ..
   make && make install
   ```
   
6. Build [Botan](https://botan.randombit.net/).

   ```bash
   cd $RPI_ROOT
   wget http://botan.randombit.net/releases/Botan-1.11.27.tgz
   tar -xvzf Botan-1.11.27.tgz
   cd Botan-1.11.27
   python configure.py --cpu=arm --cc-bin=${CROSS}-g++ --prefix=${INSTALLDIR}
   make && make install
   cp -r ${INSTALLDIR}/include/botan-1.11/botan/ ${INSTALLDIR}/include
   rm -r ${INSTALLDIR}/include/botan-1.11
   ```

7. Download Kaa C++ SDK from your [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment) and compile it.
See [Generate SDK]({{root_url}}Programming-guide/Your-first-Kaa-application/#generate-sdk).

   ```bash
   mkdir kaa-cpp
   tar -zxf kaa-cpp-ep-sdk-*.tar.gz -C kaa-cpp
   cd kaa-cpp
   mkdir build
   cd build
   cmake -DCMAKE_TOOLCHAIN_FILE=../toolchains/rpi.cmake ..
   make
   ```
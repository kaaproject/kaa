---
layout: page
title: Intel Edison
permalink: /:path/
sort_idx: 60
---
{% include variables.md %}

* TOC
{:toc}

The guide explains how to cross-compile [Kaa C++ SDK]({{root_url}}Glossary/#kaa-sdk-type) for [Intel Edison](https://software.intel.com/en-us/iot/hardware/edison).

If this is the first time you use Intel Edison technology, see official instructions on how to configure your board and make it accessible through both serial terminal and the ssh client.

>**NOTE:** This guide is verified against:
>
> * **Host OS:** Ubuntu 14.04 LTS Desktop 64-bit
> * **Device:** Intel Edison Module & Intel Edison with Kit for Arduino
> * **Target OS:** Poky (Yocto Project Reference Distro) 1.7.3, kernel version 3.10.17-poky-edison+
{:.note}

## Prerequisites

To install all required third-party components, perform the instructions below directly on the Edison board.
Some steps require root permissions.

1. Connect to the board either though serial terminal or ssh client.
See [official Edison getting started guide](https://software.intel.com/en-us/iot/library/edison-getting-started).

2. Install dependencies.

   ```bash
   opkg update
   opkg install coreutils libssp-staticdev libssp-dev
   ```

3. Install [Boost](http://www.boost.org/) (version 1.54 and newer).

   ```bash
   wget http://sourceforge.net/projects/boost/files/boost/1.58.0/boost_1_58_0.tar.gz
   tar -zxf boost_1_58_0.tar.gz
   cd boost_1_58_0/
   ./bootstrap.sh
   ./b2 install
   ```

4. Install [Apache Avro](https://avro.apache.org/) (version 1.7.5 to 1.7.7).

   ```bash
   wget http://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
   tar -zxf avro-cpp-1.7.5.tar.gz
   cd avro-cpp-1.7.5/
   cmake .
   make install
   ```

5. Install [Botan](https://botan.randombit.net/) (version 1.11).

   ```bash
   wget https://github.com/randombit/botan/archive/1.11.28.tar.gz
   tar -zxf 1.11.28.tar.gz
   cd botan-1.11.28/
   ./configure.py
   make install
   ```

6. Install [SQLite](https://www.sqlite.org/).
This step is optional and is only required if you want to use a persistent log storage for the Kaa [data collection]({{root_url}}Programming-guide/Key-platform-features/Data-collection) feature.

   ```bash
   opkg install sqlite3
   ```

   >**NOTE:** On some Edison boards, the build may crash due to the lack of system resources (out of memory).
   >If you experience this issue, reduce the number of workers used by the `make` command.
   >For example: replace `make -j4` with `make`.
   {:.note}

## Application cross-compilation

The third-party components listed above must be cross-compiled before building your Kaa application.
This way you can obtain the object files `.o` and shared object files `.so` required to build and run the application.

Perform the following instructions on the host machine:

1. Install the cross-compilation environment.

   ```bash
   mkdir edison_root && cd edison_root
   export EDISON_ROOT=$(pwd)
   export HOST_PYTHON=$(which python)
   ```

2. Download [32-bit](https://downloadmirror.intel.com/24472/eng/toolchain-20140724-linux32.sh) or [64-bit](https://downloadmirror.intel.com/24472/eng/toolchain-20140724-linux64.sh) Cross Compiler Toolchain for Intel.

3. Change the access permissions and run the `toolchain-20140724-linux*.sh` script.

   ```bash
   chmod +x toolchain-20140724-linux*.sh
   ./toolchain-20140724-linux*.sh
   ```

   ```bash
   sudo chown -R <user_name>:<user_group> /opt
   ```

   >**NOTE:** Edison toolchain default directory is `/opt/poky-edison/1.6`.
   >If you change this directory, change all dependencies in the steps below accordingly.
   {:.note}
    
3. Install g++, [CMake](https://cmake.org/download/) and (optionally) [SQLite3](https://sqlite.org/download.html).

   ```bash
   sudo apt-get install g++ cmake libboost1.55-all-dev libsqlite3-0 libsqlite3-dev
   ```
   
4. Install the [Avro C++](http://avro.apache.org/docs/1.7.6/api/cpp/html/index.html) library manually.

   ```
   cd $EDISON_ROOT
   wget http://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
   tar -zxf avro-cpp-1.7.5.tar.gz
   cd avro-cpp-1.7.5/
   cmake -G "Unix Makefiles"
   sudo make install
   ```

5. Compile [Boost](http://www.boost.org/users/download/).
Boost uses its own build system, therefore it has to be compiled for host machine first.

   ```bash
   cd $EDISON_ROOT
   wget http://sourceforge.net/projects/boost/files/boost/1.58.0/boost_1_58_0.tar.gz
   tar -zxf boost_1_58_0.tar.gz
   cd boost_1_58_0/
   ./bootstrap.sh
   ```

    Now you can build Boost libraries for Edison target.

   ```bash
   source /opt/poky-edison/1.6/environment-setup-core2-32-poky-linux
   sed -r 's#(using\s+gcc)(\s+;)#\1 :  : i586-poky-linux-g++ : <compileflags>-m32 -march=core2 -mtune=core2 -msse3 -mfpmath=sse -mstackrealign -fno-omit-frame-pointer --sysroot='${SDKTARGETSYSROOT}'\2#g' -i project-config.jam
   ./b2 install --prefix=$SDKTARGETSYSROOT/usr/local
   ```

    Copy `${SDKTARGETSYSROOT}/usr/local/lib/libboost_*` object files to Edison's `/usr/local/lib` directory (e.g. using SSH, SCP, etc.).

6. Compile [Apache Avro](https://avro.apache.org/).
Avro depends on some Boost components, therefore it is critical that you install them as described in the previous step.

   ```bash
   cd $EDISON_ROOT
   mkdir -p avro/target
   tar -xvzf avro-cpp-1.7.5.tar.gz -C ./avro/target
   cd ./avro/target/avro-cpp-1.7.5
   wget {{github_url_raw}}client/client-multi/client-cpp/toolchains/edison.cmake
   wget {{github_url_raw}}client/client-multi/client-cpp/tools/avro-cpp-disable-tests.patch
   patch < avro-cpp-disable-tests.patch
   mkdir build && cd build
   source /opt/poky-edison/1.6/environment-setup-core2-32-poky-linux
   cmake -DCMAKE_INSTALL_PREFIX=$SDKTARGETSYSROOT -DCMAKE_TOOLCHAIN_FILE=../edison.cmake -DEDISON_SDK_ROOT=/opt/poky-edison/1.6 ..
   make && make install
   ```
    Copy Avro objects files `${SDKTARGETSYSROOT}/usr/lib/libavrocpp*` to `/usr/lib` directory of Edison board.
        
7. Build [Botan](https://botan.randombit.net/).

   ```bash
   cd $EDISON_ROOT
   source /opt/poky-edison/1.6/environment-setup-core2-32-poky-linux
   wget http://botan.randombit.net/releases/Botan-1.11.27.tgz
   tar -xvzf Botan-1.11.27.tgz
   cd Botan-1.11.27
   ${HOST_PYTHON} -E configure.py --cpu=x86_32 --cc-bin=${CROSS_COMPILE}g++ --prefix=${SDKTARGETSYSROOT}/usr
   make && make install
   ```

    Copy Botan object files `${SDKTARGETSYSROOT}/usr/lib/libbotan*` to `/usr/lib` directory of Edison board.

8. Download Kaa C++ SDK from your [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment) and compile it.
See [Generate SDK]({{root_url}}Programming-guide/Your-first-Kaa-application/#generate-sdk).

   ```bash
   mkdir kaa-cpp
   tar -zxf kaa-cpp-ep-sdk-*.tar.gz -C kaa-cpp
   cd kaa-cpp
   mkdir build
   cd build
   cmake -DCMAKE_TOOLCHAIN_FILE=../toolchains/edison.cmake ..
   make
   ```

## Build Kaa application

After you installed the required dependencies and built the C++ SDK, you can build and run your [Kaa application]({{root_url}}Glossary/#kaa-application).

Since Edison runs on Linux, you can use the [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/SDK-Linux/#build-kaa-application) to build and run your application.

>**NOTE:** Make sure to specify correct compiler name when compiling your Kaa application for Edison.
{:.note}

For more information on how to build, upload and run your application on Edison board, see [official user guide](https://software.intel.com/en-us/intel-edison-board-user-guide).
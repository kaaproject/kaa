---
layout: page
title: Intel Edison
permalink: /:path/
sort_idx: 60
---
{% include variables.md %}

* TOC
{:toc}

The guide explains how to build applications for Intel Edison based on the Kaa C++ endpoint SDK.

# Configuring Intel Edison board

If this is the first time you use the Intel Edison technology, you have to start with configuring your board.
For this purpose, refer to the official Edison getting started guide.
After finishing with the guide, you will be able to access your board through both a serial terminal and the ssh client.

>**Note:** The instructions below are for the official Intel Edison firmware.

## Installing third-party components for C++ SDK

The following third-party components must be installed on the Intel Edison board before building the C++ SDK.

**Mandatory:**

- [Boost](http://www.boost.org/) (minimum 1.54)
- [Avro](http://avro.apache.org/) (1.7.5 - 1.7.7)
- [Botan](https://botan.randombit.net/) (only 1.11) libraries.

**Optional:**

- [SQLite](https://www.sqlite.org/) library. It should be installed only if you are going to use a persistent log storage for [the Kaa data collection feature]({{root_url}}Programming-guide/Key-platform-features/Data-collection).

>**NOTE:** The further instructions are expected to be executed **in the order given**, directly **on the Edison board**, and some instructions **with the root permissions**.

To install these libraries, proceed as follows:

1. Connect to the board either though the serial terminal or the ssh client.
Refer to [the official Edison getting started guide](https://software.intel.com/en-us/iot/library/edison-getting-started) for any help required.

2. Install dependencies.

   ```
   opkg update
   opkg install coreutils libssp-staticdev libssp-dev
   ```

3. Install Boost.

   ```
   wget http://sourceforge.net/projects/boost/files/boost/1.58.0/boost_1_58_0.tar.gz
   tar -zxf boost_1_58_0.tar.gz
   cd boost_1_58_0/
   ./bootstrap.sh
   ./b2 install
   ```

4. Install Avro (1.7.5--1.7.7).

   ```
   wget http://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
   tar -zxf avro-cpp-1.7.5.tar.gz
   cd avro-cpp-1.7.5/
   cmake .
   make install
   ```

5. Install Botan (1.11).

   ```
   wget https://github.com/randombit/botan/archive/1.11.28.tar.gz
   tar -zxf 1.11.28.tar.gz
   cd botan-1.11.28/
   ./configure.py
   make install
   ln -s /usr/local/include/botan-1.11/botan /usr/local/include/botan
   ```

6. Install SQLite (optionally).

   ```
   opkg install sqlite3
   ```

## Known Issues

On some Edison boards, the build crashes due to the lack of system resources (out of memory).
If you experience this issue, reduce the number of workers used by the `make`.  
As example: replace the `make -j4` for `make`.


# Cross compiling applications based on C++ SDK


## Installing dependencies

**All steps described in this guide were tested on:**

 - **Host OS:** Ubuntu 14.04 LTS Desktop 64-bit
 - **Device:** Intel Edison Module & Intel Edison with Kit for Arduino
 - **Target OS:** Yocto Linux

 **The further instructions must be executed on the host machine.**

The 3rd party components listed above have to be cross-compiled before building your Kaa application. 
This way we get object files `.o` and shared object files `.so` that are required for building and running the application. 

1. Install the cross compilation environment: 

        mkdir edison_root && cd edison_root
        export EDISON_ROOT=$(pwd)
        export HOST_PYTHON=$(which python)

   Download Cross Compiler Toolchain for Intel [32-bit](https://downloadmirror.intel.com/24472/eng/toolchain-20140724-linux32.sh) or [64-bit](https://downloadmirror.intel.com/24472/eng/toolchain-20140724-linux64.sh) depending on your system architecture.
   Change the access permissions and run `toolchain-20140724-linux*.sh` script:

        chmod +x toolchain-20140724-linux*.sh
        ./toolchain-20140724-linux*.sh

   Please fill in `< >`  user name and user group
   
        sudo chown -R <fill_user_name>:<fill_user_group> /opt


   >NOTE: Directory for Edison toolchain by default is: `/opt/poky-edison/1.6`. If you will change this directory, you should change all dependencies in further steps.
    
1. Install g++, [CMake](https://cmake.org/download/), [Boost](http://www.boost.org/users/download/) and (optionally) [SQLite3](https://sqlite.org/download.html):

        sudo apt-get install g++ cmake libboost1.55-all-dev libsqlite3-0 libsqlite3-dev

1. Install the [AvroC++](http://avro.apache.org/docs/1.7.6/api/cpp/html/index.html) library manually:

        cd $EDISON_ROOT
        wget http://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
        tar -zxf avro-cpp-1.7.5.tar.gz
        cd avro-cpp-1.7.5/
        cmake -G "Unix Makefiles"
        sudo make install
   
1. Compile Boost.
    Boost uses its own build system so it has to be compiled for host machine first.

        cd $EDISON_ROOT
        wget http://sourceforge.net/projects/boost/files/boost/1.58.0/boost_1_58_0.tar.gz
        tar -zxf boost_1_58_0.tar.gz
        cd boost_1_58_0/
        ./bootstrap.sh
    
    Now we can build Boost libraries for Edison target.
    
        source /opt/poky-edison/1.6/environment-setup-core2-32-poky-linux
        sed -r 's#(using\s+gcc)(\s+;)#\1 :  : i586-poky-linux-g++ : <compileflags>-m32 -march=core2 -mtune=core2 -msse3 -mfpmath=sse -mstackrealign -fno-omit-frame-pointer --sysroot='${SDKTARGETSYSROOT}'\2#g' -i project-config.jam
        ./b2 install --prefix=$SDKTARGETSYSROOT/usr/local

    Copy `${SDKTARGETSYSROOT}/usr/local/lib/libboost_*` object files to Edison's `/usr/local/lib` directory (e.g. using SSH, SCP, etc.).

1. Compile Avro. Also, Avro depends on some Boost components. So they need to be installed too.

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
        
    Copy Avro objects files `${SDKTARGETSYSROOT}/usr/lib/libavrocpp*` to `/usr/lib` directory of Edison board.
        
1. Build Botan.

        cd $EDISON_ROOT
        source /opt/poky-edison/1.6/environment-setup-core2-32-poky-linux
        wget http://botan.randombit.net/releases/Botan-1.11.27.tgz
        tar -xvzf Botan-1.11.27.tgz
        cd Botan-1.11.27
        ${HOST_PYTHON} -E configure.py --cpu=x86_32 --cc-bin=${CROSS_COMPILE}g++ --prefix=${SDKTARGETSYSROOT}/usr
        make && make install
        ln -rs ${SDKTARGETSYSROOT}/usr/include/botan-1.11/botan ${SDKTARGETSYSROOT}/usr/include/botan
        
    Copy Botan object files `${SDKTARGETSYSROOT}/usr/lib/libbotan*` to `/usr/lib` directory of Edison board.

1. Download C++ SDK from your [Kaa instance]({{root_url}}Glossary/#kaa-instance-kaa-deployment).
1. Compile C++ SDK.

        mkdir kaa-cpp
        tar -zxf kaa-cpp-ep-sdk-*.tar.gz -C kaa-cpp
        cd kaa-cpp
        mkdir build
        cd build
        cmake -DCMAKE_TOOLCHAIN_FILE=../toolchains/edison.cmake ..
        make
        
Now you are ready to cross compile your own Kaa application but don't forget to use proper toolchain.

# Creating applications based on C++ SDK

Creating application based on C++ SDK for Linux platform is identical for Intel Edison platform.
You can follow [the Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/SDK-Linux#quick-way-to-build-sdk).

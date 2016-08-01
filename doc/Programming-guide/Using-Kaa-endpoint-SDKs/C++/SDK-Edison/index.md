---
layout: page
title: Intel Edison
permalink: /:path/
sort_idx: 60
---
{% include variables.md %}

The guide explains how to build applications for Intel Edison based on the Kaa C++ endpoint SDK.

**Note:** The instructions below are for the official Intel Edison firmware.

## Configuring Intel Edison board

If this is the first time you use the Intel Edison technology, you have to start with configuring your board.
For this purpose, refer to the official Edison getting started guide.
After finishing with the guide, you will be able to access your board through both a serial terminal and the ssh client.

## Installing third-party components for C++ SDK

The following third-party components must be installed on the Intel Edison board before building the C++ SDK.

**Mandatory:**

- [Boost](http://www.boost.org/) (minimum 1.54)
- [Avro](http://avro.apache.org/) (1.7.5 - 1.7.7)
- [Botan](https://botan.randombit.net/) (only 1.11) libraries.

**Optional:**

- [SQLite](https://www.sqlite.org/) library. It should be installed only if you are going to use a persistent log storage for [the Kaa data collection feature]({{root_url}}Programming-guide/Key-platform-features/Data-collection).

**NOTE:** The further instructions are expected to be executed **in the order given**, directly **on the Edison board**, and some instructions **with the root permissions**.

To install these libraries, proceed as follows:

1. Connect to the board either though the serial terminal or the ssh client.
Refer to [the official Edison getting started guide](https://software.intel.com/en-us/intel-edison-board-getting-started-guide) for any help required.

2. Install coreutils.

   ```
   opkg install http://repo.opkg.net/edison/repo/core2-32/coreutils_8.22-r0_core2-32.ipk
   ```

3. Install Boost (1.54 or higher).

   ```
   wget http://sourceforge.net/projects/boost/files/boost/1.58.0/boost_1_58_0.tar.gz
   tar -zxf boost_1_58_0.tar.gz
   cd boost_1_58_0/
   ./bootstrap.sh
   ./b2 install
   ```

4. Install Avro (1.7.5--1.7.7).

   ```
   wget http://apache.ip-connect.vn.ua/avro/avro-1.7.7/cpp/avro-cpp-1.7.7.tar.gz
   tar -zxf avro-cpp-1.7.7.tar.gz
   cd avro-cpp-1.7.7/
   cmake -G "Unix Makefiles"
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

6. Install SQLite.

   ```
   wget https://www.sqlite.org/2015/sqlite-autoconf-3081002.tar.gz
   tar -zxf sqlite-autoconf-3081002.tar.gz
   cd sqlite-autoconf-3081002/
   ./configure
   make install
   ```

## Creating applications based on C++ SDK

Creating application based on C++ SDK for Linux platform is identical for Intel Edison platform.
You can follow [the Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/SDK-Linux#quick-way-to-build-sdk).

## Known Issues

On some Edison boards, the build crashes due to the lack of system resources (out of memory).
If you experience this issue, reduce the number of workers used by the `make`.  
As example: replace the `make -j4` for `make`.

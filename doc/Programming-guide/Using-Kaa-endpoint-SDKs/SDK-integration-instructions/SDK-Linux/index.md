---
layout: page
title: Linux
permalink: /:path/
nav: /:path/Programming-guide/Using-Kaa-endpoint-SDKs/SDK-integration-instructions/SDK-Linux
sort_idx: 10
---

Use the following instructions to build endpoint SDKs in Java, C, and C++ for Linux.

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Java endpoint SDK](#java-endpoint-sdk)
- [C endpoint SDK](#c-endpoint-sdk)
    - [Install build utils and dependencies](#install-build-utils-and-dependencies)
    - [Compile C endpoint SDK](#compile-c-endpoint-sdk)
    - [Configure C endpoint SDK](#configure-c-endpoint-sdk)
- [C++ endpoint SDK](#c-endpoint-sdk-1)
    - [Install build utils and dependencies](#install-build-utils-and-dependencies)
    - [Compile C++ endpoint SDK](#compile-c-endpoint-sdk-1)
    - [Configure C++ endpoint SDK](#configure-c-endpoint-sdk-1)
- [Quick way to build C/C++ endpoint SDK](#quick-way-to-build-c-c-endpoint-sdk)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

**Verified against:**

**Host OS:** Ubuntu 14.04 LTS Desktop 64-bit.

# Java endpoint SDK

<!-- TODO: Fix this link when Administration-UI-guide#AdministrationUIguide-GeneratingSDK are updated -->
To build the Java endpoint SDK, [generate](Administration-UI-guide#AdministrationUIguide-GeneratingSDK) the Java endpoint SDK in Admin UI and download the generated .jar file.

# C endpoint SDK

## Install build utils and dependencies

Before building the C endpoint SDK, install the following components on your machine:

1. Install compilers:
    1. For automatic installation, execute the following commands:

       ~~~ shel
       $ sudo apt-get install gcc
       ~~~

    2. For manual installation of version 4.8, refer to the following example:

       ~~~ shell
       $ sudo apt-get install python-software-properties
       $ sudo add-apt-repository ppa:ubuntu-toolchain-r/test
       $ sudo apt-get update
       $ sudo apt-get install gcc-4.8
       $ sudo update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-4.8 50
       ~~~

2. Install CMake utility:
    1. For automatic installation, execute the following commands (tested on Ubuntu 14.04):

       ~~~ shell
       $ sudo apt-get install cmake
       ~~~

    2. For manual installation, refer to the following example:

       ~~~ shell
       $ wget http://www.cmake.org/files/v3.3/cmake-3.3.0-rc2.tar.gz
       $ tar -zxf cmake-3.3.0-rc2.tar.gz
       $ cd cmake-3.3.0-rc2/
       $ ./configure
       $ sudo make install
       ~~~

3. Install OpenSSL:

   ~~~
   $ sudo apt-get install libssl-dev
   ~~~

4. Install CUnit:

   ~~~
   $ sudo apt-get install libcunit1-dev
   ~~~

## Compile C endpoint SDK

To build the C endpoint SDK, do the following:

<!-- TODO: Fix this link when Administration-UI-guide#AdministrationUIguide-GeneratingSDK are updated -->
1. [Generate ](Administration-UI-guide#AdministrationUIguide-GeneratingSDK)the C endpoint SDK in Admin UI.
2. Download and untar the Kaa C SDK archive.
3. Run the following commands.

   ~~~ bash
   $ mkdir build
   $ cd build
   $ cmake ..
   $ make
   $ make install
   ~~~

## Configure C endpoint SDK

To configure the C endpoint SDK build, you can optionally specify the following parameters for the **cmake** command.


{% include csv-table.html csv_path='cmake-c-options.csv' table_id='table-c-cmake-options' width='100%' %}


**\* NOTE:**

Before running the **cmake** command with the KAA\_PLATFORM parameter for a platform other than supported, do the following:

1. Create a folder in $KAA\_HOME/listfiles/platform/ and name it using \[a-zA-z\_-\] symbols. You will be able to use the folder name as a value for the KAA\_PLATFORM parameter.
2. Put the CMakeLists.txt file into the created folder. This file may contain specific compilation/linking flags, platform-dependent source files, third-party library dependencies, that is all information necessary for building the C endpoint SDK for this platform.
3. Optionally, specify the following parameters in the CMakeLists.txt file.
  * KAA\_INCLUDE\_PATHS - full path(s) to folder(s) containing additional header files
  * KAA\_SOURCE\_FILES - full path(s) to additional source files
  * KAA\_THIRDPARTY\_LIBRARIES - third-party libraries (the name of the library, for example, ssl, crypto)

The following example illustrates the build procedure for the debug build with the INFO log level and disabled EVENTS feature.

~~~
mkdir build
cd build
cmake -DKAA_DEBUG_ENABLED=1 -DKAA_MAX_LOG_LEVEL=4 -DKAA_WITHOUT_EVENTS=1 ..
make
make install
~~~

# C++ endpoint SDK

## Install build utils and dependencies

Before building the C++ endpoint SDK, install the following components on your machine:

1. Install compilers:
    1. For automatic installation, execute the following commands:

       ~~~ shell
       $ sudo apt-get install g++
       ~~~

    2. For manual installation of version 4.8, refer to the following example:

       ~~~ shell
       $ sudo apt-get install python-software-properties
       $ sudo add-apt-repository ppa:ubuntu-toolchain-r/test
       $ sudo apt-get update
       $ sudo apt-get install g++-4.8
       $ sudo update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-4.8 50
       ~~~

2. Install CMake utility:
    1. For automatic installation, execute the following commands (tested on Ubuntu 14.04):

       ~~~ shell
       $ sudo apt-get install cmake
       ~~~

    2. For manual installation, refer to the following example:

       ~~~ shell
       $ wget http://www.cmake.org/files/v3.3/cmake-3.3.0-rc2.tar.gz
       $ tar -zxf cmake-3.3.0-rc2.tar.gz
       $ cd cmake-3.3.0-rc2/
       $ ./configure
       $ sudo make install
       ~~~

3. Install the [**Boost** ](http://www.boost.org/users/download/)libraries:
    1. For automatic installation, execute the following commands:

       ~~~ shell
       $ sudo apt-get install libboost1.55-all-dev
       ~~~

    2. For manual installation, refer to the following example:

       ~~~
       $ sudo apt-get install libbz2-dev libbz2-1.0 zlib1g zlib1g-dev
       $ wget http://sourceforge.net/projects/boost/files/boost/1.58.0/boost_1_58_0.tar.gz
       $ tar -zxf boost_1_58_0.tar.gz
       $ cd boost_1_58_0/
       $ ./bootstrap.sh
       $ sudo ./b2 install
       ~~~

4. Install the [**AvroC++**](http://avro.apache.org/docs/1.7.6/api/cpp/html/index.html) library manually:

   ~~~
   $ wget http://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
   $ tar -zxf avro-cpp-1.7.7.tar.gz
   $ cd avro-cpp-1.7.5/
   $ cmake -G "Unix Makefiles"
   $ sudo make install
   ~~~

5. Install the [**Botan**](http://botan.randombit.net/) library by executing the following command:

   ~~~
   $ wget https://github.com/randombit/botan/archive/1.11.28.tar.gz
   $ tar -zxf 1.11.28.tar.gz
   $ cd botan-1.11.28/
   $ ./configure.py
   $ sudo make install
   $ sudo ln -s /usr/local/include/botan-1.11/botan /usr/local/include/botan
   ~~~

6. Install the **[SQLite](https://www.sqlite.org/index.html)** library by executing the following command:
    1. For automatic installation, execute the following commands (tested on Ubuntu 14.04):

       ~~~
       $ sudo apt-get install libsqlite3-0 libsqlite3-dev
       ~~~

    2. For manual installation, refer to the following example:

       ~~~
       $ wget https://www.sqlite.org/2015/sqlite-autoconf-3081002.tar.gz
       $ tar -zxf sqlite-autoconf-3081002.tar.gz
       $ cd sqlite-autoconf-3081002/
       $ ./configure
       $ sudo make install
       ~~~

**NOTE:** Instead of manually installing all required components and libraries, you can follow [the quick way to build C/C++ endpoint SDK](#Linux-Quickway).  (only applicable for x86\_64 platform build)

## Compile C++ endpoint SDK

To build the C++ endpoint SDK, do the following:

<!-- TODO: Fix this link when Administration-UI-guide#AdministrationUIguide-GeneratingSDK are updated -->
1. [Generate](Administration-UI-guide#AdministrationUIguide-GeneratingSDK) the C++ endpoint SDK in Admin UI.
2. Download and untar the Kaa C++ SDK archive.
3. Run the following commands.

   ~~~
   mkdir build
   cd build
   cmake ..
   make
   make install
   ~~~

## Configure C++ endpoint SDK

To configure the C++ endpoint SDK build, you can optionally specify the following parameters for the **cmake** command.

{% include csv-table.html csv_path='cmake-cpp-options.csv' table_id='table-cpp-cmake-options' width='100%' %}

The following example illustrates the build procedure for the debug build, with the INFO log level and disabled EVENTS feature and specified path to the folder Kaa will be installed in:

~~~
mkdir build
cd build
cmake -DCMAKE_INSTALL_PREFIX='/home/username/kaa' -DKAA_DEBUG_ENABLED=1 -DKAA_MAX_LOG_LEVEL=4 -DKAA_WITHOUT_EVENTS=1 ..
make
make install
~~~

# Quick way to build C/C++ endpoint SDK

If you want to quickly build the endpoint SDK or build and run Kaa C/C++ demo applications, you can use a [docker](https://www.docker.com/) container with all necessary environment preinstalled.
**NOTE:** docker natively supports only amd64 architecture.

1. Follow [docker installation guide](http://docs.docker.com/index.html) depends on your OS.
2. Download the docker container.

   ~~~
   docker pull kaaproject/demo_c_cpp_environment
   ~~~

3. Get inside container and compile what you need: SDK, demo applications, etc.

   ~~~
   docker run -it kaaproject/demo_c_cpp_environment bash
   ~~~

    **NOTE:**
    To mount a host directory to the container's filesystem, add the following flag to the previous command: -v FOLDER\_WITH\_DEMO:FOLDER\_INSIDE\_CONTAINER
    For example, the following command will build a demo project and direct you to the container's shell, where you can test immediately:

   ~~~
   docker run -v FOLDER_WITH_DEMO:/opt/demo
     -it kaaproject/demo_c_cpp_environment bash -c 'cd /opt/demo/ &&
     chmod +x build.sh && ./build.sh clean build && bash'
   ~~~

4. After the compilation, launch the demo binary located at /opt/demo/build/ in the container's filesystem.
**NOTE:**
If you would like to run a compiled binary on some other host, you should have all third-party libraries like boost, etc. preinstalled.

---
layout: page
title: Samsung Artik 5
permalink: /:path/
sort_idx: 50
---

{% include variables.md %}

* TOC
{:toc}

The guide explains how to cross-compile Kaa applications for Samsung Artik 5 based on the Kaa C++ endpoint SDK.

## Configuring Artik 5 board

If this is the first time you use the Artik board, you have to start with configuring your board.
For this purpose, refer to [the official Artik getting started guide](https://developer.artik.io/documentation/getting-started-beta/).
After finishing with the guide, you will be able to access your board through both a serial terminal and an ssh client.

## Installing prerequisites

**All steps described in this guide were tested on:**

 - **Host OS:** Ubuntu 14.04 LTS Desktop 64-bit.
 - **Device:** [Samsung Artik 5](https://www.artik.io/modules/overview/artik-5/)

 **The further instructions must be executed on the host machine.**

1. Install [CMake](https://cmake.org/):

        sudo apt-get install cmake

1. If your host system has 64-bit architecture, you must also install 32-bit version of some packages to be able to run 32-bit toolchain (see below):

        sudo dpkg --add-architecture i386
        sudo apt-get update
        sudo apt-get install lib32z1 lib32ncurses5 lib32bz2-1.0 libstdc++6:i386

## Installing third-party components

The following third-party components must be installed before building the C++ SDK.

- Boost (1.54 or above).
- Avro (1.7.5).
- Botan (1.11).

To build Avro version higher than 1.7.5, you will need Boost with the iostream library.
To build Boost with iostreams, you will need to build the zlib and the libbz2 libraries as described below.

Install the third-party components as follows.

1. Install the toolchain.

        mkdir artik_root && cd artik_root
        export ARTIK_ROOT="$(pwd)"
        wget https://launchpad.net/linaro-toolchain-binaries/trunk/2013.10/+download/gcc-linaro-arm-linux-gnueabihf-4.8-2013.10_linux.tar.bz2
        tar xvjf gcc-linaro-arm-linux-gnueabihf-4.8-2013.10_linux.tar.bz2
        mv gcc-linaro-arm-linux-gnueabihf-4.8-2013.10_linux "${ARTIK_ROOT}"/gcc-linaro-arm-linux-gnueabihf

1. Install zlib (for Avro version higher than 1.7.5).

        cd "${ARTIK_ROOT}"
        wget http://zlib.net/zlib-1.2.8.tar.gz
        tar -xvzf zlib-1.2.8.tar.gz
        export INSTALLDIR="${ARTIK_ROOT}"/gcc-linaro-arm-linux-gnueabihf/libc/usr
        export PATH="${ARTIK_ROOT}"/gcc-linaro-arm-linux-gnueabihf/bin:"${PATH}"
        export TARGETMACH=arm-linux-gnueabi
        export BUILDMACH=i686-pc-linux-gnu
        export CROSS=arm-linux-gnueabihf-
        cd zlib-1.2.8
        CROSS_PREFIX="${CROSS}" ./configure --prefix="${INSTALLDIR}"
        make install

1. Install bzip2 (for Avro version higher than 1.7.5).

        cd "${ARTIK_ROOT}"
        wget http://www.bzip.org/1.0.6/bzip2-1.0.6.tar.gz
        tar -xvzf bzip2-1.0.6.tar.gz
        cd bzip2-1.0.6
        sed -e "/^all:/s/ test//" Makefile > Makefile-libbz2_so
        make -f Makefile-libbz2_so CC="${CROSS}"gcc AR="${CROSS}"ar
        make PREFIX="${INSTALLDIR}" install


1. Install Boost (1.54 or higher).

        cd "${ARTIK_ROOT}"
        wget -O boost_1_59_0.tar.bz2 http://sourceforge.net/projects/boost/files/boost/1.59.0/boost_1_59_0.tar.bz2/download
        tar xvjf boost_1_59_0.tar.bz2
        cd boost_1_59_0
        ./bootstrap.sh
         sed -r 's/(using\s+gcc)(\s+;)/\1 : arm : arm-linux-gnueabihf-c++\2/g' -i project-config.jam
        ./b2 install toolset=gcc-arm --prefix="${INSTALLDIR}"

1. Install Avro 1.7.5.

        cd "${ARTIK_ROOT}"
        wget https://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
        tar zxf avro-cpp-1.7.5.tar.gz
        cd avro-cpp-1.7.5
        wget https://raw.githubusercontent.com/kaaproject/kaa/develop/client/client-multi/client-cpp/tools/avro-cpp-disable-tests.patch
        patch < avro-cpp-disable-tests.patch
        mkdir build && cd build
        cmake -DCMAKE_INSTALL_PREFIX:PATH="${INSTALLDIR}" -DCMAKE_CXX_COMPILER="${CROSS}"g++ -DCMAKE_FIND_ROOT_PATH="${INSTALLDIR}" ..
        make && make install

1. Install Botan.

        cd "${ARTIK_ROOT}"
        wget http://botan.randombit.net/releases/Botan-1.11.28.tgz
        tar -xvzf Botan-1.11.28.tgz
        cd Botan-1.11.28
        python configure.py --cpu=arm --cc-bin="${CROSS}"g++ --prefix="${INSTALLDIR}"
        make install
        ln -s "${INSTALLDIR}"/include/botan-1.11/botan/ "${INSTALLDIR}"/include/botan

1. Copy libraries to the Artik device.

        scp "${INSTALLDIR}"/lib/libboost_*.so* root@<put Artik ip address here>:/usr/lib
        scp "${INSTALLDIR}"/lib/libbotan*.so* root@<put Artik ip address here>:/usr/lib
        scp "${INSTALLDIR}"/lib/libavrocpp*.so* root@<put Artik ip address here>:/usr/lib

## Compiling applications

Now, dependencies are built and it is time to create Kaa application.
Artik board is running regular Linux, so you can refer to [the Linux guide]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C++/SDK-Linux/#code) for detailed process of application creation.
The [`CMAKE_CXX_COMPILER`](https://cmake.org/cmake/help/v3.0/variable/CMAKE_LANG_COMPILER.html) specifies C++ compiler to use and [`CMAKE_FIND_ROOT_PATH`](https://cmake.org/cmake/help/v3.0/variable/CMAKE_FIND_ROOT_PATH.html) tells CMake where to look for required libraries which have been installed in previous step.

        cmake -DKAA_MAX_LOG_LEVEL=3 -DCMAKE_CXX_COMPILER="${CROSS}"g++ -DCMAKE_FIND_ROOT_PATH="${INSTALLDIR}" ..
        make

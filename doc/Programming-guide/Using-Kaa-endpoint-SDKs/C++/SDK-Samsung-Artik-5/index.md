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

# Configuring Artik 5 board

If this is the first time you use the Artik board, you have to start with configuring your board.
For this purpose, refer to [the official Artik getting started guide](https://developer.artik.io/documentation/getting-started-beta/).
After finishing with the guide, you will be able to access your board through both a serial terminal and an ssh client.

# Installing third-party components for C++ SDK

The following third-party components must be installed before building the C++ SDK.

- Boost (1.54 or above).
- Avro (1.7.5).
- Botan (1.11).

To build Avro version higher than 1.7.5, you will need Boost with the iostream library.
To build Boost with iostreams, you will need to build the zlib and the libbz2 libraries as described below.

Install the third-party components as follows.

1. Install the toolchain.

        wget https://launchpad.net/linaro-toolchain-binaries/trunk/2013.10/+download/gcc-linaro-arm-linux-gnueabihf-4.8-2013.10_linux.tar.bz2
        tar xvjf gcc-linaro-arm-linux-gnueabihf-4.8-2013.10_linux.tar.bz2
        sudo mv gcc-linaro-arm-linux-gnueabihf-4.8-2013.10_linux /opt/gcc-linaro-arm-linux-gnueabihf

1. Install zlib (for Avro version higher than 1.7.5).

        wget http://zlib.net/zlib-1.2.8.tar.gz
        tar -xvzf zlib-1.2.8.tar.gz
        export INSTALLDIR=/opt/gcc-linaro-arm-linux-gnueabihf
        export PATH=$INSTALLDIR/bin:$PATH
        export TARGETMACH=arm-linux-gnueabi
        export BUILDMACH=i686-pc-linux-gnu
        export CROSS=arm-linux-gnueabihf
        export CC=${CROSS}-gcc
        export LD=${CROSS}-ld
        export AS=${CROSS}-as
        cd zlib-1.2.8
        ./configure --prefix=/opt/gcc-linaro-arm-linux-gnueabihf/arm-linux-gnueabihf/libc/usr
        make install

1. Install bzip2 (for Avro version higher than 1.7.5).

        wget http://www.bzip.org/1.0.6/bzip2-1.0.6.tar.gz
        tar -xvzf bzip2-1.0.6.tar.gz
        export INSTALLDIR=/opt/gcc-linaro-arm-linux-gnueabihf
        export PATH=$INSTALLDIR/bin:$PATH
        export TARGETMACH=arm-linux-gnueabi
        export BUILDMACH=i686-pc-linux-gnu
        export CROSS=arm-linux-gnueabihf
        export CC=${CROSS}-gcc
        export LD=${CROSS}-ld
        export AS=${CROSS}-as
        export AR=${CROSS}-ar
        cd bzip2-1.0.6
        sed -e "/^all:/s/ test//" Makefile > Makefile-libbz2_so
        make -f Makefile-libbz2_so CC="${CC}" AR="${AR}"
        make PREFIX=${INSTALLDIR}/arm-linux-gnueabihf/libc/usr install


1. Install Boost (1.54 or higher).

        wget -O boost_1_59_0.tar.bz2 http://sourceforge.net/projects/boost/files/boost/1.59.0/boost_1_59_0.tar.bz2/download
        tar xvjf boost_1_59_0.tar.bz2
        cd boost_1_59_0
        export PATH=$PATH:/opt/gcc-linaro-arm-linux-gnueabihf/bin
        ./bootstrap.sh
         sed -r 's/(using\s+gcc)(\s+;)/\1 : arm : arm-linux-gnueabihf-c++\2/g' -i project-config.jam
         ./bjam install toolset=gcc-arm --prefix=${INSTALLDIR}

1. Install Avro 1.7.5.

        wget https://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
        cd avro-cpp-1.7.5
        wget https://raw.githubusercontent.com/kaaproject/kaa/8e65212dfb855363e1a8977d4053041c80d785c7/client/client-multi/client-c/toolchains/rpi.cmake
        wget https://raw.githubusercontent.com/kaaproject/kaa/967970ec57fb0a62c23ffe573385bf0d0299d977/client/client-multi/client-cpp/tools/avro-cpp-disable-tests.patch
        patch < avro-cpp-disable-tests.patch
        mkdir build && cd build
        cmake -DCMAKE_INSTALL_PREFIX:PATH=/opt/gcc-linaro-arm-linux-gnueabihf/arm-linux-gnueabihf/libc/usr -DCMAKE_TOOLCHAIN_FILE=../artik.cmake ..
        make && make install

1. Install Botan.

        wget http://botan.randombit.net/releases/Botan-1.11.28.tgz
        tar -xvzf Botan-1.11.28.tgz
        cd Botan-1.11.28
        export PATH=$PATH:/opt/gcc-linaro-arm-linux-gnueabihf/bin
        python configure.py --cpu=arm --cc-bin=arm-linux-gnueabihf-g++ --prefix=/opt/gcc-linaro-arm-linux-gnueabihf/arm-linux-gnueabihf/libc/usr
        make install
        mv /opt/gcc-linaro-arm-linux-gnueabihf/arm-linux-gnueabihf/libc/usr/include/botan-1.11/botan/ /opt/gcc-linaro-arm-linux-gnueabihf/arm-linux-gnueabihf/libc/usr/include
        rm -r /opt/gcc-linaro-arm-linux-gnueabihf/arm-linux-gnueabihf/libc/usr/include/botan-1.11

1. Copy libraries to the Artik device.

        scp /opt/gcc-linaro-arm-linux-gnueabihf/arm-linux-gnueabihf/libc/usr/lib/libboost_*.so* root@<put Artik ip address here>:/usr/lib
        scp /opt/gcc-linaro-arm-linux-gnueabihf/arm-linux-gnueabihf/libc/usr/lib/libbotan*.so* root@<put Artik ip address here>:/usr/lib
        scp /opt/gcc-linaro-arm-linux-gnueabihf/arm-linux-gnueabihf/libc/usr/lib/libavrocpp*.so* root@<put Artik ip address here>:/usr/lib

# Create application

Now, dependencies are built and it is time to create Kaa application.
Artik board is running regular Linux, so you can refer to [the Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/#c-sdk-build) for detailed process of application creation.
But remember, you must specify [the CMake toolchain file](http://www.vtk.org/Wiki/CMake_Cross_Compiling) when compiling your Kaa application for Artik:

        cmake -DKAA_MAX_LOG_LEVEL=3 -DCMAKE_TOOLCHAIN_FILE=../artik.cmake ..
        make

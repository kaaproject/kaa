---
layout: page
title: Raspberry Pi
permalink: /:path/
sort_idx: 20
---

# The guide provides information on how to cross-compile C++ SDK for Raspberry PI

**Verified against:**

**Host OS:** Ubuntu 14.04 LTS Desktop 64-bit.

**The further instructions must be executed on the host machine**

1. Download and install toolchain.

        mkdir rpi_root && cd rpi_root
        export RPI_ROOT=$(pwd)
        git clone https://github.com/raspberrypi/tools.git
        export INSTALLDIR=$RPI_ROOT/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian
        export PATH=$PATH:$INSTALLDIR/bin
        export CROSS=arm-linux-gnueabihf
        export CC=${CROSS}-gcc
        export LD=${CROSS}-ld
        export AS=${CROSS}-as
        export AR=${CROSS}-ar

2. Download and install zlib.

        wget http://zlib.net/zlib-1.2.8.tar.gz
        tar -xvzf zlib-1.2.8.tar.gz
        cd zlib-1.2.8
        ./configure --prefix=${INSTALLDIR}/arm-linux-gnueabihf/libc/usr
        make && make install

        cd $RPI_ROOT

3. Download and install bzib2.

        wget http://www.bzip.org/1.0.6/bzip2-1.0.6.tar.gz
        tar -xvzf bzip2-1.0.6.tar.gz
        cd bzip2-1.0.6
        sed -e "/^all:/s/ test//" Makefile > Makefile-libbz2_so
        make -f Makefile-libbz2_so CC="${CC}" AR="${AR}"
        make PREFIX=${INSTALLDIR}/arm-linux-gnueabihf/libc/usr install

        cd $RPI_ROOT

4. Download and install boost.

        wget -O boost_1_59_0.tar.bz2 http://sourceforge.net/projects/boost/files/boost/1.59.0/boost_1_59_0.tar.bz2/download
        tar -xvjf boost_1_59_0.tar.bz2 && cd boost_1_59_0 && ./bootstrap.sh

    Edit the project-config.jam file. Add 'using gcc : arm : arm-linux-gnueabihf-c++ ;' instead of  'using gcc ;':

        ./bjam install toolset=gcc-arm --prefix=${INSTALLDIR}/arm-linux-gnueabihf/libc/usr
        cd $RPI_ROOT

5. Install Avro for target and host. Also Avro depends on some Boost components. So they need to be installed too.

    Install Avro for host:

        sudo apt-get install libboost-dev libboost-filesystem-dev libboost-iostreams-dev libboost-program-options-dev libboost-system-dev
        cd $RPI_ROOT
        wget https://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
        mkdir -p avro/host
        tar -xvzf avro-cpp-1.7.5.tar.gz -C ./avro/host
        unset CC
        cd ./avro/host/avro-cpp-1.7.5
        mkdir build && cd build

        cmake -G "Unix Makefiles" ..
        sudo make install

    Build Avro for target (Avro should be patched to be built):

        cd $RPI_ROOT
        mkdir -p avro/target
        tar -xvzf avro-cpp-1.7.5.tar.gz -C ./avro/target
        cd ./avro/target/avro-cpp-1.7.5

        wget https://raw.githubusercontent.com/kaaproject/kaa/8e65212dfb855363e1a8977d4053041c80d785c7/client/client-multi/client-c/toolchains/rpi.cmake
        wget https://raw.githubusercontent.com/kaaproject/kaa/967970ec57fb0a62c23ffe573385bf0d0299d977/client/client-multi/client-cpp/tools/avro-cpp-disable-tests.patch
        patch < avro-cpp-disable-tests.patch
        mkdir build && cd build
        export CC=${CROSS}-gcc
        cmake -DCMAKE_INSTALL_PREFIX:PATH=${INSTALLDIR}/arm-linux-gnueabihf/libc/usr -DCMAKE_TOOLCHAIN_FILE=../rpi.cmake ..
        make && make install

        cd $RPI_ROOT

6. Install Botan.

        wget http://botan.randombit.net/releases/Botan-1.11.27.tgz
        tar -xvzf Botan-1.11.27.tgz
        cd Botan-1.11.27
        python configure.py --cpu=arm --cc-bin=${CROSS}-g++ --prefix=${INSTALLDIR}/arm-linux-gnueabihf/libc/usr
        make && make install
        cp -r ${INSTALLDIR}/arm-linux-gnueabihf/libc/usr/include/botan-1.11/botan/ ${INSTALLDIR}/arm-linux-gnueabihf/libc/usr/include
        rm -r ${INSTALLDIR}/arm-linux-gnueabihf/libc/usr/include/botan-1.11

7. [Generate C++ SDK](#TODO).
8. Compile C++ SDK.

        mkdir kaa-cpp && tar -zxf kaa-cpp-ep-sdk-*.tar.gz -C kaa-cpp
        cd kaa-cpp
        mkdir build && cd build && cmake -DCMAKE_TOOLCHAIN_FILE=../toolchains/rpi.cmake .. && make

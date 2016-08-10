---
layout: page
title: Beaglebone
permalink: /:path/
sort_idx: 30
---

{% include variables.md %}

* TOC
{:toc}

The guide provides information on how to **cross-compile** C++ SDK for Beaglebone or Beaglebone Black.
Alternatively, you can build the Kaa C++ endpoint SDK right on the Beaglebone board.
Refer to [the Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/SDK-Linux/) for further details.

## Install dependencies

**The further instructions must be executed on the host machine.**

**Verified against:**

 - **Host OS:** Ubuntu 14.04 64-bit LTS
 - **Target OS:** Ubuntu 16.04 32-bit LTS

1. Install build prerequisites.

        sudo apt-get install cmake build-essential

1. Download toolchain.

        mkdir bb_root && cd bb_root
        export BB_ROOT
        BB_ROOT="$(pwd)"
        wget -c https://releases.linaro.org/components/toolchain/binaries/5.3-2016.02/arm-linux-gnueabihf/gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf.tar.xz
        tar xf gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf.tar.xz
        export CROSS="${BB_ROOT}"/gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf/bin/arm-linux-gnueabihf-
        export SYSROOT="${BB_ROOT}"/gcc-linaro-5.3-2016.02-x86_64_arm-linux-gnueabihf/libc/usr

1. Download and install zlib.

        cd "${BB_ROOT}"
        wget http://zlib.net/zlib-1.2.8.tar.gz
        tar -xvzf zlib-1.2.8.tar.gz
        cd zlib-1.2.8
        CROSS_PREFIX="${CROSS}" ./configure --prefix="${SYSROOT}"
        make && make install

1. Download and install bzip2.

        cd "${BB_ROOT}"
        wget http://www.bzip.org/1.0.6/bzip2-1.0.6.tar.gz
        tar -xvzf bzip2-1.0.6.tar.gz
        cd bzip2-1.0.6
        make CC="${CROSS}"gcc AR="${CROSS}"ar LD="${CROSS}"ld AS="${CROSS}"as PREFIX="${SYSROOT}" install

1. Download and install boost.

        cd "${BB_ROOT}"
        wget -O boost_1_59_0.tar.bz2 http://sourceforge.net/projects/boost/files/boost/1.59.0/boost_1_59_0.tar.bz2/download
        tar -xvf boost_1_59_0.tar.bz2
        cd boost_1_59_0
        ./bootstrap.sh
        sed -r 's#(using\s+gcc)(\s+;)#\1 : arm : '${CROSS}c++'\2#g' -i  project-config.jam
        ./b2 install --prefix="${SYSROOT}"

1. Install Avro for target and host. Also Avro depends on some Boost components. So they need to be installed too.

    Install Avro for host:

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

    Build Avro for target (Avro should be patched to be built):

        cd "${BB_ROOT}"
        mkdir -p avro/target
        tar -xvzf avro-cpp-1.7.5.tar.gz -C ./avro/target
        cd ./avro/target/avro-cpp-1.7.5

        wget https://raw.githubusercontent.com/kaaproject/kaa/967970ec57fb0a62c23ffe573385bf0d0299d977/client/client-multi/client-cpp/tools/avro-cpp-disable-tests.patch
        patch < avro-cpp-disable-tests.patch
        mkdir build && cd build
        cmake -DCMAKE_INSTALL_PREFIX:PATH="${SYSROOT}" -DCMAKE_CXX_COMPILER="${CROSS}"g++ -DCMAKE_FIND_ROOT_PATH="${SYSROOT}" ..
        make && make install

1. Build and install Botan.

        cd "${BB_ROOT}"
        wget http://botan.randombit.net/releases/Botan-1.11.27.tgz
        tar -xvzf Botan-1.11.27.tgz
        cd Botan-1.11.27
        ./configure.py --cpu=arm --cc-bin="${CROSS}"g++ --prefix="${SYSROOT}"
        make && make install
        ln -s botan-1.11/botan/ "${SYSROOT}"/include/botan

## Create application
Now, the toolchain and all dependencies are installed, and it is time to create Kaa application.
Since BeagleBone (BeagleBone Black) is running Linux, you can refer to [the Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/SDK-Linux/#cpp-sdk-build) for the detailed process of application creation.
The [`CMAKE_CXX_COMPILER`](https://cmake.org/cmake/help/v3.0/variable/CMAKE_LANG_COMPILER.html) specifies C++ compiler to use and [`CMAKE_FIND_ROOT_PATH`](https://cmake.org/cmake/help/v3.0/variable/CMAKE_FIND_ROOT_PATH.html) tells CMake where to look for required libraries which have been installed in previous step.

        cmake -DKAA_MAX_LOG_LEVEL=3 -DCMAKE_CXX_COMPILER="${CROSS}"g++ -DCMAKE_FIND_ROOT_PATH="${SYSROOT}" ..
        make

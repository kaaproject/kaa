---
layout: page
title: Linux
permalink: /:path/
sort_idx: 10
---
{% include variables.md %}

* TOC
{:toc}

This page describes Kaa C++ SDK build process on Linux system.

**Verified against:**

**Host OS:** Ubuntu 14.04 LTS Desktop 64-bit.

## Installing prerequisites

**NOTE:** Instead of manually installing all required components and libraries, you can follow [the quick way to build C/C++ endpoint SDK](#quick-way-to-build-sdk).
(Only applicable for x86\_64 platform.)

Before building the C++ endpoint SDK, install the following components on your machine:

1. Install g++, [CMake](https://cmake.org/download/), [Boost](http://www.boost.org/users/download/) and (optionally) [SQLite3](https://sqlite.org/download.html):

   ```
   sudo apt-get install g++ cmake libboost1.55-all-dev libsqlite3-0 libsqlite3-dev
   ```

4. Install the [AvroC++](http://avro.apache.org/docs/1.7.6/api/cpp/html/index.html) library manually:

   ```
   wget http://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
   tar -zxf avro-cpp-1.7.5.tar.gz
   cd avro-cpp-1.7.5/
   cmake -G "Unix Makefiles"
   sudo make install
   ```

5. Install the [Botan](http://botan.randombit.net/) 1.11 library. The stock 1.10 version is not recommended for C++11 projects,
so the newer 1.11 version is used in Kaa C++ SDK.
To install, proceed as follows:

   ```
   wget https://github.com/randombit/botan/archive/1.11.28.tar.gz
   tar -zxf 1.11.28.tar.gz
   cd botan-1.11.28/
   ./configure.py
   sudo make install
   sudo ln -s /usr/local/include/botan-1.11/botan /usr/local/include/botan
   ```

6. Install the [SQLite](https://www.sqlite.org/index.html) library by executing the following command:

    ```
    sudo apt-get install libsqlite3-0 libsqlite3-dev
    ```
## Compiling SDK

To build the C++ endpoint SDK, do the following:

<!-- TODO: KAA-700 -->
1. [Generate]({{root_url}}Administration-UI-guide#AdministrationUIguide-GeneratingSDK) the C++ endpoint SDK in Admin UI.
2. Download and untar the Kaa C++ SDK archive.

   ``` bash
   $ tar xfv kaa-cpp-ep-sdk.tar.gz
   ```
**Note: the archive name may be different in your case**

3. Run the following commands.

   ```
   mkdir build
   cd build
   cmake ..
   make
   ```

There are a lot of parameters which can be passed to cmake to customize build. [C++ SDK page]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/) gives a detailed description.

## Quick way to build SDK

### Build in Docker container
If you want to build the endpoint SDK quickly or build and run Kaa C/C++ demo applications, you can use a [docker](https://www.docker.com/) container with all necessary environment preinstalled.
**NOTE:** Docker natively supports amd64 architecture only.

1. Follow [docker installation guide](http://docs.docker.com/index.html) depends on your OS.
2. Download the docker container.

   ```
   docker pull kaaproject/demo_c_cpp_environment
   ```

3. Get inside container and compile what you need: SDK, demo applications, etc.

   ```
   docker run -it kaaproject/demo_c_cpp_environment bash
   ```

    **NOTE:**
    To mount a host directory to the container's filesystem, add the following flag to the previous command: `-v FOLDER_WITH_DEMO:FOLDER_INSIDE_CONTAINER`.
    For example, the following command will build a demo project and direct you to the container's shell, where you can test immediately:

   ```
   docker run -v FOLDER_WITH_DEMO:/opt/demo
     -it kaaproject/demo_c_cpp_environment bash -c 'cd /opt/demo/ &&
     chmod +x build.sh && ./build.sh clean build && bash'
   ```

4. After the compilation, launch the demo binary located at `/opt/demo/build/` in the container's filesystem.
**NOTE:**
If you would like to run a compiled binary on some other host, you should have all third-party libraries like boost, etc. preinstalled.

### Build in nix shell
[Nix](https://nixos.org/nix) is a package manager which is used to manage Kaa C and C++ SDKs build environment for CI purposes. You can use it to build Kaa C++ SDK quickly.
Just install Nix on your system and execute the following command from the [root directory](https://github.com/kaaproject/kaa/tree/master/client/client-multi/client-cpp) of Kaa C++ SDK:

```
nix-shell --pure --run true
```

Nix will download and compile SDK dependencies and eventually build the SDK itself.
For more details on using Nix in C and C++ SDKs refer to [Nix guide]({{root_url}}Customization-guide/Endpoint-SDKs/C-SDK/Environment-setup/Nix-guide).

## Minimal example
This section describes application development with Kaa C++ SDK.

### Directory structure

The recommended directory structure for applications using Kaa C++ SDK is as follows:

```
CMakeLists.txt
src/
    KaaDemo.cpp
kaa/
```

* `CMakeLists.txt` describes your application to CMake build system (see below).
* `src/KaaDemo.cpp` is the actual application source code.
* `kaa/` is a directory where the Kaa SDK should be placed.
<!-- TODO: KAA-700 -->
Download generated SDK archive from [AdministrationUI](TODO) and unpack it to `kaa/` directory.


### Build system overview
Kaa C++ SDK uses CMake as a build system. Although you are not limited to any particular build system,
it is recommended to use CMake, which allows you to integrate your applications tightly with Kaa C++ SDK.

The step-by-step explanation of CMakeLists.txt follows.

Firstly, the minimum required CMake  version is specified:

```CMake
cmake_minimum_required(VERSION 3.0.2)
```

Next, we tell CMake about our project and enable C++11 standard:

```CMake
project(kaa-demo CXX)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
```

Here we add kaa-demo executable and specify the source file, `src/KaaDemo.cpp`:

```CMake
add_executable(kaa-demo src/KaaDemo.cpp)
```

We also add Kaa SDK subdirectory and specify Kaa SDK as a dependency for our application.
As a result, the SDK will be built before building the application.

```CMake
add_subdirectory(kaa)

target_link_libraries(kaa-demo kaacpp)
```

### Code

The code for this demo is simple and straightforward. It just initializes and starts Kaa client,
which involves connecting to Kaa server. After the `kaaClient->start();` line, Kaa client is up and running in a dedicated thread.
Finally, the Endpoint access token is printed to `stdout` and Kaa client stops its execution.

```c++
#include <iostream>

#include <kaa/Kaa.hpp>
#include <kaa/IKaaClient.hpp>

using namespace kaa;

int main()
{
    /*
     * Initialize the Kaa endpoint.
     */
    auto kaaClient = Kaa::newClient();

    /*
     * Run the Kaa endpoint.
     */
    kaaClient->start();

    /*
     * Print access token
     */
    std::cout << "Endpoint access token: " << kaaClient->getEndpointAccessToken() << std::endl;

    /*
     * Stop the Kaa endpoint.
     */
    kaaClient->stop();

    return 0;
}
```

### Building

To build the example, proceed as follows:

```
mkdir build
cd build
cmake -DKAA_MAX_LOG_LEVEL=3 ..
make
```

First, we generate `Makefile` in `build/` directory with CMake, then we invoke `make` to build the application.
We pass `KAA_MAX_LOG_LEVEL=3` option to avoid messing up application output with Kaa SDK info level messages.
The build can be configured in a numerous ways, see [this]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/) page for detailed description.

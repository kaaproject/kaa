---
layout: page
title: Linux
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}

* TOC
{:toc}

This section describes how to build [Kaa C++ SDK]({{root_url}}Glossary/#kaa-sdk-type) on a Linux-based machine and install a [Kaa application]({{root_url}}Glossary/#kaa-application).

>**NOTE:** This guide is verified against:
>
> * Ubuntu 14.04 LTS Desktop 64-bit
> * Ubuntu 16.04 LTS Desktop 64-bit
{:.note}

## Prerequisites

To build Kaa C++ SDK, make sure to install the following components:

1. Install g++, [CMake](https://cmake.org/download/), and [SQLite3](https://sqlite.org/download.html).

   ```bash
   sudo apt-get install g++ cmake python libsqlite3-0 libsqlite3-dev
   ```

2. Install [Boost](http://www.boost.org/users/download/).

<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs">
    <li class="active"><a data-toggle="tab" href="#Platform1">Ubuntu 14.04</a></li>
    <li><a data-toggle="tab" href="#Platform2">Ubuntu 16.04</a></li>
</ul>

<div class="tab-content">
<div id="Platform1" class="tab-pane fade in active" markdown="1" >

```bash
sudo apt-get install libboost1.55-all-dev
```

</div><div id="Platform2" class="tab-pane fade" markdown="1" >

```bash
wget https://sourceforge.net/projects/boost/files/boost/1.55.0/boost_1_55_0.tar.gz
tar -xf boost_1_55_0.tar.gz
cd boost_1_55_0
./bootstrap.sh
sudo ./bjam cxxflags=-std=c++11 install
```

</div></div>
</li>
</ul>

{: start="3"}
3. Install the [Avro C++](http://avro.apache.org/docs/1.7.5/api/cpp/html/index.html) library manually.

   ```bash
   wget http://archive.apache.org/dist/avro/avro-1.7.5/cpp/avro-cpp-1.7.5.tar.gz
   tar -zxf avro-cpp-1.7.5.tar.gz
   cd avro-cpp-1.7.5/
   cmake .
   sudo make install
   ```

4. Install the [Botan](http://botan.randombit.net/) 1.11 library.
The stock 1.10 version is not recommended for C++11 projects,
so the newer 1.11 version is used in Kaa C++ SDK.

   ```bash
   wget https://github.com/randombit/botan/archive/1.11.28.tar.gz
   tar -zxf 1.11.28.tar.gz
   cd botan-1.11.28/
   ./configure.py
   sudo make install
   ```

5. After dependencies are installed, dynamic loader's links (and, optionally, its cache) will be updated.
This is required so that loader knows where the libraries are located.
Run the command below to update the links.

   ```bash
   sudo ldconfig
   ```

## Build C++ SDK

To build the C++ endpoint SDK, do the following:

1. [Generate your C++ SDK]({{root_url}}Programming-guide/Your-first-Kaa-application/#generate-sdk).

2. Unpack the C++ SDK archive.

   ```bash
   tar xfv cpp-sdk-archive-name.tar.gz
   ```

3. Create a directory where the SDK will be built.

   ```bash
   mkdir build
   cd build
   ```

4. Configure the build via CMake.

   ```bash
   cmake ..
   ```

5. Perform build.

   ```bash
   make
   ```
   
## Other ways to build C++ SDK

There are alternative ways to build Kaa C++ SDK, such as using [Docker](https://www.docker.com/) container and [Nix](http://nixos.org/nix/) shell.

### Docker container

You can use Docker containers to build your C++ SDK and run Kaa C/C++ demo applications with all necessary environment pre-installed.

>**NOTE:** Docker natively supports x86-64 architecture only.
{:.note}

To build Kaa C++ SDK using Docker:

1. Follow the [official docker installation guide](http://docs.docker.com/index.html) for your operating system.

2. Download the Docker container.

   ```bash
   docker pull kaaproject/demo_c_cpp_environment
   ```

3. Compile SDK, demo applications, etc. for your container.

   ```bash
   docker run -it kaaproject/demo_c_cpp_environment bash
   ```

    To mount a host directory to the container file system, add this flag to the previous command: `-v FOLDER_WITH_DEMO:FOLDER_INSIDE_CONTAINER`.
    
    For example, the following command will build a demo project and direct you to the container shell where you can run tests immediately.

   ```bash
   docker run -v FOLDER_WITH_DEMO:/opt/demo
     -it kaaproject/demo_c_cpp_environment bash -c 'cd /opt/demo/ &&
     chmod +x build.sh && ./build.sh clean build && bash'
   ```

4. After the compilation, launch the demo binary located in the `/opt/demo/build/` directory of the container file system.

	>**NOTE:** To run a compiled binary on some other host, you need to have all third-party libraries like `boost`, etc. pre-installed.
	{:.note}

	See also [Docker deployment]({{root_url}}Administration-guide/System-installation/Docker-deployment/).
	
### Nix shell

Kaa C and C++ SDK build environments use the [Nix](https://nixos.org/nix) package manager for CI purposes.

To build Kaa C++ SDK, install Nix as described in the [Nix guide]({{root_url}}Customization-guide/Nix-guide/).

After you installed Nix on your system, run the following command from the [root directory]({{github_url}}client/client-multi/client-cpp) of Kaa C++ SDK.

```bash
nix-shell
```

Nix will download and compile all SDK dependencies and prepare your environment for development.

## Creating Kaa applications

After you installed the required dependencies and built the C++ SDK, you can build and run your Kaa application.

### Directory structure

The recommended directory structure for applications using Kaa C++ SDK is as follows.

```
CMakeLists.txt
src/
    KaaDemo.cpp
kaa/
```

* `CMakeLists.txt` -- file describing your application for the CMake [build system](#build-system).
* `src/KaaDemo.cpp` -- file containing the application source code.
* `kaa/` -- directory where you unpack the Kaa SDK archive.

### Build system

Although you can use other build systems, it is recommended that you use CMake to tie application code together with the C++ SDK.

Below is an example how to configure the `CMakeLists.txt` file for the application.

1. Specify minimum CMake version required.

   ```bash
   cmake_minimum_required(VERSION 2.8.12)
   ```

2. Specify the project name and enable C++11 standard.

   ```bash
   project(kaa-demo CXX)

   set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
   ```

3. Add `kaa-demo` executable file and specify the source file `src/KaaDemo.cpp`.

   ```bash
   add_executable(kaa-demo src/KaaDemo.cpp)
   ```

4. Add Kaa SDK subdirectory and specify Kaa SDK as a dependency for your application.
As a result, the SDK will be built before building the application.

   ```bash
   add_subdirectory(kaa)

   target_link_libraries(kaa-demo kaacpp)
   ```

### Code

Below is a simple and straightforward example of the application code.

The application will initialize and start [Kaa client]({{root_url}}Glossary/#kaa-client), which involves connecting to [Kaa server]({{root_url}}Glossary/#kaa-server).
After the `kaaClient->start();` line, Kaa client is up and running in a dedicated thread.
Finally, the endpoint access token is printed to `stdout` and stop running.

```c++
#include <iostream>
#include <kaa/Kaa.hpp>
#include <kaa/IKaaClient.hpp>

int main()
{
    //Initialize the Kaa endpoint.
    auto kaaClient = kaa::Kaa::newClient();
    // Run the Kaa endpoint.
    kaaClient->start();
    //Print access token
    std::cout << "Endpoint access token: " << kaaClient->getEndpointAccessToken() << std::endl;
    // Stop the Kaa endpoint.
    kaaClient->stop();
    return 0;
}
```

### Build application

To build the example application above, run these commands.

```bash
mkdir build
cd build
cmake -DKAA_MAX_LOG_LEVEL=3 ..
make
```

As a result, CMake will generate `Makefile` in the `build/` directory.
Using the `KAA_MAX_LOG_LEVEL=3` will prevent mixing up the application output with Kaa SDK info level messages.
The `make` command builds the application.
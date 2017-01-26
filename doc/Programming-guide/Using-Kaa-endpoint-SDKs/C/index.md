---
layout: page
title: C
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}

* TOC
{:toc}

The [Kaa C SDK]({{root_url}}Glossary/#endpoint-sdk) is a portable, lightweight and fast library that provides APIs for [Kaa clients]({{root_url}}Glossary/#kaa-client) to utilize various platform features.
This SDK is designed to facilitate the end-user development process efforts and decrease the time-to-market for your IoT embedded solutions.

Kaa C SDK offers a number of advantages for your embedded solutions:

- Small footprint
- Modularity
- Low memory consumption
- Portability
- Performance

This makes Kaa C SDK a perfect choice even for use with low-power and low-cost microcontrollers.

You don't need any operating system to use C SDK, so you can use it in bare metal systems.

You can find auto-generated docs for Kaa C SDK [here]({{site.baseurl}}/autogen-docs/client-c/{{version}}/).

## Main components

Kaa C SDK comprises the following functional parts:

- **SDK core** starts the [application]({{root_url}}Glossary/#kaa-application) main loop and controls the SDK execution processes.
The corresponding APIs are located in the `kaa/platform/kaa_client.h` file.

- **Build system** allows generating and customizing the project files without having to create a build infrastructure for every compiler or IDE used.
The build system is written using [CMake](https://cmake.org/).

- **Extensions** are the application-level modules within SDK that provide implementations of [Kaa platform key features]({{root_url}}Programming-guide/Key-platform-features/).
Extensions are exposed to the user in a form of headers that are located in the `src/extensions` directory.

## Target support and portability

C SDK contains a platform abstraction layer that hides differences across platforms, such as memory allocation procedures or time routines.
Its interface is located in the `kaa/platform/` directory.

Below is the list of the target platforms for which Kaa C SDK already has an implementation of the platform layer:

- POSIX
    - [Linux]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/)
    - [UDOO]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-UDOO/)
    - [Raspberry Pi]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-RPi/)
    - [Beaglebone]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Beaglebone/)
    - [QNX Neutrino]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-QNX-Neutrino/)
    - Windows (cygwin)
- [ESP8266]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-ESP8266/)
- [CC3200]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-TI-CC3200/)

## Environment setup

During compilation, Kaa C SDK and the derived applications might require:

 - A compiler for the chosen taget platform, such as `arm-none-eabi` for bare metal ARM targets.
 - Vendor SDK, e.g. TI SDK for the CC3200 processor.

### Build in Nix shell
[Nix](https://nixos.org/nix) is a package manager which is used to manage Kaa C and C++ SDKs build environment for CI purposes.
You can use it to build Kaa C SDK quickly.

For more details on using Nix in C and C++ SDKs refer to [Nix guide]({{root_url}}Customization-guide/Nix-guide/).

### Build configuration

Build configuration is performed on the Makefile file generation stage.
If you want to pass a configuration parameter, prefixed it with `-D`.

    cmake -DCMAKE_INSTALL_PREFIX=/home/username/installdir

This way you can customize your build for Kaa C SDK.
For example, you can disable debug logging to decrease memory footprint.

The list of available configuration parameters is located in the `CMakeLists.txt` file.

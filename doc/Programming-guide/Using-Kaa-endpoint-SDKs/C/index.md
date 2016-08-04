---
layout: page
title: C
permalink: /:path/
sort_idx: 10
---

* TOC
{:toc}

{% include variables.md %}

*C SDK* is a portable, lightweight and fast library that provides APIs for Kaa clients to conveniently use various platform features.
It specially designed to reduce development efforts and decrease time-to-market when developing your IoT embedded solutions.

C SDK is the best fit for the embedded devices.
Small footprint, modularity, low memory consumption, portability with the C's execution speed make C SDK the best choice for using it even with low-power and low-cost microcontrollers.

Using C SDK requires **no operating system**, so you can use it on bare metal systems.

# Major components

From a usage point of view, C SDK consists of following parts.

- **SDK core** -- part responsible for starting an application main loop and execution control of the SDK itself.
You may check the corresponding API in the `kaa/platform/kaa_client.h` directory.

- **Build system** -- written using CMake, it allows to generate and customize project files without the necessity of creating build infrastructure for every compiler or IDE used.
To get more familiar with CMake, refer to [the official documentation](https://cmake.org/).

- **Extensions** -- application-level modules shipped within SDK; implement [the set of Kaa features]({{root_url}}/Programming-guide/Key-platform-features/).
Extensions are exposed to the user in a form of headers that one can find in `src/extensions` directory.

# Target support and portability

C SDK contains platform abstraction layer, which hides differences across platforms, such as memory allocation procedures or time routines.
Its interface is placed under `kaa/platform/` directory.

Below is the list of the targets for which C SDK already has an implementation of the platform layer.

- POSIX
    - [Linux]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/)
    - [UDOO]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-UDOO/)
    - [Windows (cygwin)]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Windows)
    - [Raspberry PI]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-RPi/)
    - [Beaglebone]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Beaglebone/)
    - [QNX Neutrino]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-QNX-Neutrino/)
- [ESP8266]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/)
- [CC3200]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux/)

If you have not found interested target, or you want to know more about working with platform layer, refer to [the porting guide for C SDK]({{root_url}}/Customization-guide/Endpoint-SDKs/C-SDK/Porting-guide/)

# Build environment

During compilation, C SDK and derived applications requires:

 - Compiler for given target, such as `arm-none-eabi` for bare metal ARM targets.
 - Vendor SDK, e.g. TI SDK for the CC3200 processor.
 - java, to generate keypairs during build procedure.

[Nix is the prefferable way]({{root_url}}/Customization-guide/Endpoint-SDKs/C-SDK/Environment-setup/) to deploy build environment.

Manual installation of the required packages varies between targets.
[Use links from the previous section](#target-support-and-portability) to find how to build and use SDK for the desired platform.

## Build configuration

Build configuration is performed on the Makefile generation stage.
To pass any configuration parameter, it should be prefixed with `-D`, e.g.:

    cmake -DCMAKE_INSTALL_PREFIX=/home/username/installdir

In such way, you can customize build for Kaa C SDK. For example, you can disable debug logging thus, decrease memory footprint.

The available configuration parameters list can be found in the root `CMakeLists.txt` file.

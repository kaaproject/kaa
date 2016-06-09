---
layout: page
title: C
permalink: /:path/
sort_idx: 10
---

- [Intruduction](#introduction)
- [Build configuration](#build-configuration)

# Introduction

*C SDK* was designed to be the best fit for the embedded devices. Small footprint, modularity, low memory consumption, portability with the C's execution speed make C SDK the best choice for using it even with low-power microcontrollers. Using C SDK requires no operating system, so you can use it on bare metal systems.

# Build configuration

C SDK uses CMake as a build system. So, build configuration is performed on the make file generation stage. To pass any configuration parameter, it should be prefixed with `-D`, e.g.:

    cmake -DCMAKE_INSTALL_PREFIX=/home/username/installdir

The available configuration parameters list:

-  `KAA_MAX_LOG_LEVEL` - Maximum log level used by C SDK. The higher value the more detailed logs.

    Values:

    - `0` - NONE (no logs)
    - `1` - FATAL
    - `2` - ERROR
    - `3` - WARN
    - `4` - INFO
    - `5` - DEBUG
    - `6` - TRACE

    Default: If build type is `Release`, `KAA_MAX_LOG_LEVEL=4`. If `Debug`, `KAA_MAX_LOG_LEVEL=6`.

- `WITH_EXTENSION_[EXTENSION_NAME]` - specifies which extensions should be included to build.

    Extensions' names:

    - PROFILE
    - CONFIGURATION
    - EVENT
    - LOGGING
    - NOTIFICATION
    - USER

    Values:

    - `ON` - enable extension
    - `OFF` - disable extension

    Default: `ON` - all extensions are included to the build.

- `KAA_PLATFORM` - build sdk for a specific target.

    Values:

    - `cc32xx`
    - `esp8266`
    - `x86-64`

    Default: `x86-64`

- `KAA_UNITETESTS_COMPILE` - compile unit tests.

    Values:

    - `ON`
    - `OFF`

    Default: `OFF`

    Note: requires [cmocka](https://cmocka.org/) test framework to be installed.

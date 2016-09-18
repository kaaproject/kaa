---
layout: page
title: Porting guide
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

The guide describes platform-level dependencies of the C SDK.
It is extremely superficial but should give you the starting point and a basic idea of the porting process.

# Dependencies

The SDK uses a platform-level layer that depends on the environment where the SDK will run.
If you want to port it, you must implement all mandatory platform-level functions.

All the functions can be divided into the next categories:

## Mandatory

> **NOTE:** All links are given to the POSIX port.

- [Dynamic memory allocation routines]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/posix/platform/mem.h).

- [Networking stack]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/posix/platform/sock.h).

- [Time utilities]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/posix/platform/time.h).

- [Output stream]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/posix/platform/stdio.h).

- [Default settings]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/posix/platform/defaults.h).

- [SHA-1 calculation]({{github_url}}client/client-multi/client-c/src/kaa/platform/ext_sha.h) (you may use a default implementation).


## Optional

- [Encryption]({{github_url}}client/client-multi/client-c/src/kaa/platform/ext_key_utils.h).

- [System logger]({{github_url}}client/client-multi/client-c/src/kaa/platform/ext_system_logger.h).

# CMake files

There are two CMake files you are interested in:

- [Platform listfile]({{github_url}}client/client-multi/client-c/listfiles/platform). It is responsible for compiling the platform layer. You must implement one.

- [CMake toolchain file]({{github_url}}client/client-multi/client-c/toolchains). You might need a CMake toolchain file if you want to cross-compile the SDK. For more info, see [CMake Cross-Compiling documentation](http://www.vtk.org/Wiki/CMake_Cross_Compiling) and [example toolchain files]({{github_url}}client/client-multi/client-c/toolchains).

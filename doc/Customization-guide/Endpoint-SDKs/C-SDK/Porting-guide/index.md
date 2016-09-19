---
layout: page
title: Porting guide
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

* TOC
{:toc}

The guide explains how to port C SDK to a platform and describe some dependencies of the platform.
It is incomplete and brief. It can give some difficulties between the process.

# Dependencies

Kaa C SDK can be ported to most platforms with OS, without any OS and is "endian safe". 

> **NOTE:** It supports only 32-bits and 64-bits platforms. A device needs to have a network controller with TCP/IP stack.

The SDK uses a platform-level layer that depends on the environment where the SDK should run. 
If you want to port it, you need to create a few files of the SDK and implement platform-dependent functions. These files can be divided into the next categories:

## Mandatory             
    
 - Dynamic memory allocation, [interface]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/mem.h). 

 - Networking stack, [interface]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/sock.h).

 - Time utilities, [interface]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/time.h).
 
 - Output stream, [interface]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/stdio.h).
 
 - Default settings, [interface]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/defaults.h).
 
 - SHA-1 calculation (you may use a default implementation), [interface]({{github_url}}client/client-multi/client-c/src/kaa/platform/ext_sha.h).  
  
> **NOTE:** All references are files which are used by esp8266. You need to use the same but with own implementations and put ones to a folder which needs to create in the [platform-impl]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/). The new folder needs to have the same folder tree as esp8266 folder. 
 
## Optional         

 - Encryption. Compile-time generated and run-time generated encryption keys. For example, ESP8266 demos use only the compile-time generated key.
 
 - Debugging log.  
 
> **NOTE:** Consider to use the compile-time generated key in case of constrained, bare-metal platforms to save MCU resources and  absence of entropy, and generating keys on a device is a potential vulnerability.
  
Use [a file dependencies]({{github_url}}client/client-multi/client-c/CMakeLists.txt) (Search keywords "List is available in configuration parameters") which shows more information.
   
# CMake files

There are two platform dependencies files:

 - A CMakeLists.txt file. There are references to the source files of the platform, include directories. Implement own file and put to a new platform folder (it needs to create, see example esp8266 folder) in a [platform dependence directory]({{github_url}}client/client-multi/client-c/listfiles/platform).
 
 > **NOTE:** Consider to provide a mechanism to set a platform name to a variable "KAA_PLATFORM". See a [CMake file]({{github_url}}client/client-multi/client-c/CMakeLists.txt), by default it uses "posix".
 
- A CMake toolchain file. The file sets a proper compiler, a linker, some default flags and a search path. You might need a CMake toolchain file if you want to cross-compile the SDK. For more info, see [CMake Cross-Compiling documentation](http://www.vtk.org/Wiki/CMake_Cross_Compiling) and [example toolchain files]({{github_url}}client/client-multi/client-c/toolchains).

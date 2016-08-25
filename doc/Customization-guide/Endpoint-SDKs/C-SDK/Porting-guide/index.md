---
layout: page
title: Porting guide
permalink: /:path/
sort_idx: 40
---

{% include variables.md %}

* TOC
{:toc}

 
# Prerequisites
The guide is incomplete and brief. It can give some difficulties between the process.

# Dependencies

Kaa C SDK can be ported to most platforms with OS, without any OS and it's "endian safe". 

> **NOTE:** It supports only 32-bits and 64-bits platforms. A device needs to have a network controller with TCP-IP stack.

The SDK uses a platform-level layer that depends on the environment where the SDK should run. 
If you want to port it, you need to create a few files of the SDK and implement platform-dependent functions. These files can be divided into the next categories:

## Mandatory             
    
 - Dynamic memory allocation, [interface]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/mem.h). 

 - Networking stack, [interface]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/sock.h).

 - Time utilities, [interface]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/time.h).
 
 - Output stream, [interface]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/stdio.h).
 
 - Default settings, [interface]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/defaults.h).
  
> **NOTE:** All references are files which are used by esp8266. You need to use the same but with own implementations and put ones to a folder which needs to create in the [platform-impl]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/). The new folder needs to have the same folder tree how esp8266 folder. 
 
## Optional         

 - Encryption (supports to disable or enable). Compile time generated and run time generated keys of the encryption. For example, the ESP8266 demo uses only the compile time generated key.

 - SHA-1 calculation (you may use a default implementation).
 
 - Log and debug.  
 
> **NOTE:** Consider to use the compile time generated key in case of constrained, bare-metal platforms to save MCU resources and own implementation of the encryption for devices which have a small size of the data (~32 kB) and code memory (~128 kB).
  
More information about these dependencies in a [file's]({{github_url}}client/client-multi/client-c/CMakeLists.txt) line 17 "List of avaliable configuration parameters"
  
  
# Examples
   
[The code]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266) which was implemented for esp8266.                      
[The custom app]({{root_url}}Programming-guide/Your-first-Kaa-application/) can help to see how platform-dependent code is working and then explore existing [implementation]({{github_url}}client/client-multi/client-c ) for our C SDK.

# CMake files

There are two platform dependencies files:

 - A CMakeLists.txt file. There are references to the source files of the platform, include directories. Implement own file, create a new platform's folder (see esp8266 folder) in a [directory]({{github_url}}client/client-multi/client-c/listfiles/platform) and put the file to the new folder.
 
 > **NOTE:** Consider to provide a mechanism to set the platform to a variable KAA PLATFORM. See a [file's]({{github_url}}client/client-multi/client-c/CMakeLists.txt) line 161. By default it uses "posix", see line 121.
 
 - A CMake toolchain file. The file sets a proper compiler, a linker, some default flags and a search path. Need to create own file, may add it to a [directory]({{github_url}}client/client-multi/client-c/toolchains) and set the patch to a variable CMAKE_TOOLCHAIN_FILE. More info about implementing toolchains is in the [documentation](http://www.vtk.org/Wiki/CMake_Cross_Compiling).

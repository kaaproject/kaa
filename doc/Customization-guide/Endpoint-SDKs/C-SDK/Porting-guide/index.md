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

Kaa C SDK can be ported to all devices  with or without any operations sytem. For example it can be ported to: Linux, Windows, Android, IOS, FireFox, Solaris, microcontrollers with or without any Rtos, etc.
Kaa supports Big and Little Endian platforms automaticaly, user doesn't need to care about it.
Note! A platform must have enough memory for the SDK and a network controller with TCP-IP stack.

# Dependencies

The SDK uses a platform-level layer that depends on the environment where the SDK should run. 
If you want to port it, you must modified a few files of the SDC and implement own functios. These files provide:

## Mandatory		 	
	
 - Dynamic allocation routines of the memory, [interface and implementation]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/mem.h). 

 - Network communication, [interface and implementation]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/sock.h).

 - Time routine, [interface and implementation]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/time.h).
 
 - Output stream, [interface and implementation]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/stdio.h).
 
 - Default settings, [interface and implementation]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl/esp8266/platform/defaults.h).
  
> **NOTE:** All references are files which are used by esp8266. You must create the same but with own implementations.
 
## Optional		 

 - Public key generation (it may be some hard-coded key, see the ESP8266 demo).

 - SHA-1 calculation (you may use a default implementation)
 
 > **NOTE:** If you want to save memory and speed of the device, better to use own implementation of this code. For example it will good for devices which have small size of the data(32 kB) and code memory(128 kB).
  
## Additional

 - Log and debug. It supports different levels of the information, each one takes different resource of the processor.
 
 - Encription. It supports to disable or enable the encription with public(static), private(dynamic) keys.
    
> **NOTE:** For small platforms(microcontrollers) we recommend to use the encryption with public key, because the private key doesn't give a big dispersion and it will save the resource of the procesor.
More information about these dependencies you can [find here]({{github_url}}client/client-multi/client-c/CMakeLists.txt).
  
  
# Implemented
   
The code for a few platform were implemented [here]({{github_url}}client/client-multi/client-c/src/kaa/platform-impl). It helps to start to work. If you have a platfor from these or implement own platform using examples.                        
[The custom app]({{root_url}}Programming-guide/Your-first-Kaa-application/) can help to see how platform-dependent code is working and then explore existing [implementation]({{github_url}}client/client-multi/client-c ) for our C SDK.




# Cmake files

There are two files: CMAKE_TOOLCHAIN_FILE and KAA_PLATFORM:

 - CMAKE_TOOLCHAIN_FILE - CMake uses a toolchain of utilities to compile, link libraries and create archives, and other tasks to drive the build.. Now Kaa C SDK supports a few hardware platforms. You can find CMake toolchain files for these platforms [here]({{github_url}}client/client-multi/client-c/toolchains). More info about implementing toolchains is [here](http://www.vtk.org/Wiki/CMake_Cross_Compiling).

 - KAA_PLATFORM - the file contains platform-dependent source [files]({{github_url}}client/client-multi/client-c/listfiles/platform), third-party library dependencies and specific compilation/linking flags.  


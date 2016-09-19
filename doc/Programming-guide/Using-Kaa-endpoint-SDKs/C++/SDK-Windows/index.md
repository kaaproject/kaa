---
layout: page
title: Windows
permalink: /:path/
sort_idx: 50
---

{% include variables.md %}

* TOC
{:toc}

This page describes C++ SDK build process on Windows.

## Installing prerequisites

**All steps described here were tested on:**:

 - **Host OS**: Windows 7, 8, 10

 - **IDE**: MS Visual Studio 2013, 2015

### Dependencies

Before building the C++ endpoint SDK, install the following components on your machine:

1. [wget](http://downloads.sourceforge.net/gnuwin32/wget-1.11.4-1-setup.exe) and [libarchive](http://downloads.sourceforge.net/gnuwin32/libarchive-2.4.12-1-setup.exe).
1. [cmake 3.5.1](https://cmake.org/files/v3.5/cmake-3.5.1-win32-x86.msi).

    >**NOTE:** Make sure that you chose one  "Add CMake to system path..." for all users or for current user on the "Install options" step.

1. Python 2.7 [32-bit](https://www.python.org/ftp/python/2.7.9/python-2.7.9.msi) depending on your system architecture or [64-bit](https://www.python.org/ftp/python/2.7.9/python-2.7.9.amd64.msi).
1. Boost 1.60 [32-bit](https://sourceforge.net/projects/boost/files/boost-binaries/1.60.0/boost_1_60_0-msvc-14.0-32.exe/download)
    or [64-bit](https://sourceforge.net/projects/boost/files/boost-binaries/1.60.0/boost_1_60_0-msvc-14.0-64.exe/download).
1. 7-Zip from http://www.7-zip.org/ . Add the directory you installed 7-Zip into to your `PATH`.
1. `libbz2.dll` [32-bit](https://github.com/philr/bzip2-windows/releases/download/v1.0.6/bzip2-dll-1.0.6-win-x86.zip) or [64-bit](https://github.com/philr/bzip2-windows/releases/download/v1.0.6/bzip2-dll-1.0.6-win-x64.zip).
    Unzip and add the directory whre the unzipped `libbz2.dll` is stored to your `PATH`.
    
### SDK Prerequisites

1. Download and untar an appropriate C++ SDK tar.gz archive into some directory (further in text `KAA_BUILD_DIR`).
1. Configure installation by editing the `KAA_BUILD_DIR\tools\env.bat` file:

    * `BUILD_PLATFORM`

        Description: Target architecture.

        Values: `x86`, `x64`.

        Default: `x64`.

    * `MSVC_VERSION`

        Descrpition: MSVC++ Version.

        Values:

         - `14` Visual Studio 2015
         - `12` Visual Studio 2013
         - `11` Visual Studio 2012
         - `10` Visual Studio 2010
         - `9`  Visual Studio 2008
         - `8`  Visual Studio 2005

        Default: `14`.

    * `BOOST_ROOT`

        Description: Boost libraries installation path.

        Default: `C:\local\boost_1_60_0\`

    * `ROOT_PATH`

        Description: Path where all packages will be installed.

        Default: `C:\local\`

1. Open the command line terminal [Developer Command Prompt](https://msdn.microsoft.com/en-us/en-en/library/ms229859(v=vs.110).aspx)
and proceed as follows:
    1. Go to `KAA_BUILD_DIR`:

       ```
       cd KAA_BUILD_DIR
       ```

    1. Build thirparty components. Execute the `build_sdk_thirdparty.bat` script.

       ```
       cd tools
       build_sdk_thirdparty.bat
       ```

       >**NOTE:** By default, the debug configuration is used. To build release versions, use the `release` argument:

       ```
       build_sdk_thirdparty.bat release
       ```

## Building SDK

To build the Kaa C++ SDK, proceed as follows:

1. Download and untar an appropriate C++ SDK tar.gz archive from Admin UI.
1. Open the command line terminal [Developer Command Prompt](https://msdn.microsoft.com/en-us/en-en/library/ms229859(v=vs.110).aspx)
and run the following commands:

   ```
   cd KAA_BUILD_DIR
   tools\env.bat
   avrogen.bat
   md build && cd build
   ```

1. To build Kaa C++ SDK with `nmake` run:

   ```
   cmake -G "NMake Makefiles" -DCMAKE_FIND_ROOT_PATH=%ROOT_PATH% -DCMAKE_BUILD_TYPE=Debug -DKAA_MAX_LOG_LEVEL=3 ..
   nmake
   ```

1. To generate Visual Studio Project run and build it from command line:
    * 32-bit:

   ```
   cmake -G "Visual Studio 14" -DCMAKE_FIND_ROOT_PATH=%ROOT_PATH% -DCMAKE_BUILD_TYPE=Debug -DKAA_MAX_LOG_LEVEL=3 ..
   msbuild kaacpp.vcxproj /property:Platform=%BUILD_PLATFORM%
   ```

    * 64-bit:

   ```
   cmake -G "Visual Studio 14 Win64" -DCMAKE_FIND_ROOT_PATH=%ROOT_PATH% -DCMAKE_BUILD_TYPE=Debug -DKAA_MAX_LOG_LEVEL=3 ..
   msbuild kaacpp.vcxproj /property:Platform=%BUILD_PLATFORM%
   ```


For additional CMake options, see [CMakeLists.txt](https://github.com/kaaproject/kaa/blob/master/client/client-multi/client-cpp/CMakeLists.txt) file located in the C++ SDK root.

## Demo applications

For minimal example application, refer to the [C++ SDK Linux guide]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C++/SDK-Linux/#minimal-example).

To build and run a Kaa C++ demo, proceed as follows:

1. Download and untar appropriate C++ demo sources from Kaa Sandbox
1. Open the command line terminal [Developer Command Prompt](https://msdn.microsoft.com/en-us/en-en/library/ms229859(v=vs.110).aspx)
and run the following commands:

   ```
   KAA_BUILD_DIR\env.bat
   build.bat deploy
   ```

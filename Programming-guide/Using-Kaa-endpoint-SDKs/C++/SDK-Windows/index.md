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

**Verified against**:

OS: Windows XP SP2, Windows Vista, Windows 7, Windows 8, Windows 10

IDE: MS Visual Studio 2013,MS Visual Studio 2015

## Installing prerequisites

1. Download and install folowing dependencies:
    1. [wget](http://downloads.sourceforge.net/gnuwin32/wget-1.11.4-1-setup.exe) and [libarchive](http://downloads.sourceforge.net/gnuwin32/libarchive-2.4.12-1-setup.exe).
    1. [cmake 3.5.1](https://cmake.org/files/v3.5/cmake-3.5.1-win32-x86.msi).

        **NOTE:** Make sure that you chose one  "Add CMake to system path..." for all users or for current user on the "Install options" step.

    1. [Python 2.7(amd64)](https://www.python.org/ftp/python/2.7.9/python-2.7.9.amd64.msi) or [Python 2.7(i386)](https://www.python.org/ftp/python/2.7.9/python-2.7.9.msi) depending on your system architecture.
    1. [Boost 1.60](https://sourceforge.net/projects/boost/files/boost-binaries/1.60.0/boost_1_60_0-msvc-14.0-32.exe/download).
1. Create a separate directory (for example, `C:\build_kaa`), further in text `KAA_BUILD_DIR`.
1. Place [env.bat](https://raw.githubusercontent.com/kaaproject/kaa/master/client/client-multi/client-cpp/tools/env.bat)
in `KAA_BUILD_DIR`.
1. Place [build_sdk_thirdparty.bat](https://raw.githubusercontent.com/kaaproject/kaa/master/client/client-multi/client-cpp/tools/build_sdk_thirdparty.bat)
in `KAA_BUILD_DIR`.
1. Configure installation by editing `env.bat` file:

    * `BUILD_PLATFORM`

        Description: Target architecture.

        Values: `x86`, `x64`.

        Default: `x64`.

    * `MSVC_VERSION`

        Descrpition: MSVC++ Version.

        Values:

        | Value | Meaning            |
        |-------|--------------------|
        | `14`  | Visual Studio 2015 |
        | `12`  | Visual Studio 2013 |
        | `11`  | Visual Studio 2012 |
        | `10`  | Visual Studio 2010 |
        | `9`   | Visual Studio 2008 |
        | `8`   | Visual Studio 2005 |

        Default: `14`.

    * `MSVS_ROOT`

        Descrpition: It must point to MS Visual Studio installation directory.

        Default: `C:\Program Files (x86)\Microsoft Visual Studio %MSVC_VERSION%.0`

    * `BOOST_ROOT`

        Descrpition:It must point to the Boost installation directory.

        Default: `C:\local\boost_1_60_0\`

    * `GNUWIN32_ROOT`

        Descrpition: It must point to the wget and libarchive installation directory.

        Default: `C:\Program Files (x86)\GnuWin32`

    * `ZLIB_ROOT`

        Descrpition: It must point to the directory where Avro binaries and includes will be installed.

        Default: `C:\local\%ZLIB_SRC%\%BUILD_PLATFORM%`.

    * `AVRO_ROOT`

        Description: It must point to the directory where Avro binaries and includes will be installed.

        Default: `C:\local\%AVRO_SRC%\%BUILD_PLATFORM%`

    * `BOTAN_ROOT`

        Descrpiption: It must point to the directory where Botan binaries and includes will be installed.

        Default: `C:\local\%BOTAN_SRC%\%BUILD_PLATFORM%`

    * `SQLITE_ROOT`

        Description: It must point to the directory where SQlite3 binaries and includes will be installed.

        Default: `C:\local\sqlite-autoconf\`

1. Open the command line terminal [Developer Command Prompt](https://msdn.microsoft.com/en-us/en-en/library/ms229859(v=vs.110).aspx)
and proceed as follows:
    1. Go to `KAA_BUILD_DIR`:

       ```
       cd KAA_BUILD_DIR
       ```

    1. Build thirparty components. Execute the build_sdk_thirdparty.bat script.

       ```
       build_sdk_thirdparty.bat
       ```

       **NOTE:** By default, the debug configuration is used. To build release versions, use the 'release' argument:

       ```
       build_sdk_thirdparty.bat release
       ```

## Building SDK

To build the Kaa C++ SDK, proceed as follows:

1. Download and untar an appropriate C++ SDK tar.gz archive from Admin UI.
1. Open the command line terminal [Developer Command Prompt](https://msdn.microsoft.com/en-us/en-en/library/ms229859(v=vs.110).aspx)
and run the following commands:

   ```
   KAA_BUILD_DIR\env.bat
   avrogen.bat
   md build
   cd build
   ```

1. To build Kaa C++ SDk with nmake run:

   ```
   cmake -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=Debug -DKAA_MAX_LOG_LEVEL=3 ..
   nmake
   ```

1. To generate Visual Studio Project run and build it from command line:
    * 32-bit:

   ```
   cmake -G "Visual Studio 14" -DCMAKE_BUILD_TYPE=Debug -DKAA_MAX_LOG_LEVEL=3 ..
   msbuild INSTALL.vcxproj /property:Configuration=%BUILD_TYPE% /property:Platform=x32
   ```

    * 64-bit:

   ```
   cmake -G "Visual Studio 14 Win64" -DCMAKE_BUILD_TYPE=Debug -DKAA_MAX_LOG_LEVEL=3 ..
   msbuild INSTALL.vcxproj /property:Configuration=%BUILD_TYPE% /property:Platform=x64
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

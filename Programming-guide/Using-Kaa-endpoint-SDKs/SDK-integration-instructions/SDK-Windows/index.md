---
layout: page
title: Windows
permalink: /:path/
sort_idx: 10
---
<!-- Start: Cross links helper -->
<!-- This header should be placed in all files that h
{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}
<!-- End:   Cross links helper -->

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Prerequisites](#prerequisites)
- [Instructions](#instructions)
- [Demo applications](#demo-applications)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


## Prerequisites

OS: Windows XP SP2, Windows Vista, Windows 7, Windows 8

IDE: MS Visual Studio 2013

## Instructions

1. Download and install folowing dependencies:
    1. [wget](http://downloads.sourceforge.net/gnuwin32/wget-1.11.4-1-setup.exe) and [libarchive](http://downloads.sourceforge.net/gnuwin32/libarchive-2.4.12-1-setup.exe)
    2. [cmake 3.5.1](https://cmake.org/files/v3.5/cmake-3.5.1-win32-x86.msi)

        **NOTE:** Make sure that you chose one  "Add CMake to system path..." for all users or for current user on the "Install options" step

    3. [Python 2.7(amd64)](https://www.python.org/ftp/python/2.7.9/python-2.7.9.amd64.msi) or [Python 2.7(i386)](https://www.python.org/ftp/python/2.7.9/python-2.7.9.msi) depended on your system architecture
    4. [Boost 1.60](https://sourceforge.net/projects/boost/files/boost-binaries/1.60.0/boost_1_60_0-msvc-14.0-64.exe/download)
2. Create a separate directory (for example, `C:\build_kaa`), further in text `KAA_BUILD_DIR`.
<!--     1. Download [Avro 1.8.0](http://archive.apache.org/dist/avro/avro-1.8.0/avro-src-1.8.0.tar.gz) and [Botan 1.11.28](https://github.com/randombit/botan/archive/1.11.28.tar.gz) -->
<!--     1. Place archives avro-src-1.7.5.tar.gz and Botan-1.11.28.tgz in KAA_BUILD_DIR. -->
    1. Place [env.bat](env.bat) in `KAA_BUILD_DIR`. <!--(File env.bat located in the 'tools' folder in C++ SDK)-->
    2. Place [build_sdk_thirdparty.bat](build_sdk_thirdparty.bat) in `KAA_BUILD_DIR`. <!--(File build_sdk_thirdparty.bat located in the 'tools' folder in C++ SDK)-->
3. Edit env.bat:
    1. Update the `MSVS_HOME` variable. It must point to MS Visual Studio installation directory.
    2. Update the `BOOST_ROOT` variable. It must point to the Boost installation directory (see the Download and install Boost step).
    3. Update the `GNUWIN32_HOME` variable. It must point to the wgen and libarchive installation directory (see the Download and install wget and libarchive step).
    3. Update the `AVRO_ROOT_DIR` variable. It must point to the directory where Avro binaries and includes will be installed.
    4. Update the `BOTAN_HOME variable`. It must point to the directory where Botan binaries and includes will be installed.
    5. Update the `ZLIB_HOME variable`. It must point to the directory where zlib binaries and includes will be installed.
4. Open the command line terminal [Developer Command Prompt](https://msdn.microsoft.com/en-us/en-en/library/ms229859(v=vs.110).aspx)
and proceed as follows:
    1. Go to `KAA_BUILD_DIR`:

       ~~~
       $ cd KAA_BUILD_DIR
       ~~~

    2. Build thirparty components. Execute the build_sdk_thirdparty.bat script.

       ~~~
       $ build_sdk_thirdparty.bat
       ~~~

       NOTE: By default, the debug configuration is used. To build release versions, use the 'release' argument:

       ~~~
       $ build_sdk_thirdparty.bat release
       ~~~

    3. To build the Kaa C++ SDK, proceed as follows:
        1. Download and untar an appropriate C++ SDK tar.gz archive from Admin UI.
        2. Open the command line terminal [Developer Command Prompt](https://msdn.microsoft.com/en-us/en-en/library/ms229859(v=vs.110).aspx)
        and run the following commands:

           ~~~
           $ KAA_BUILD_DIR\env.bat
           $ avrogen.bat
           $ md build
           $ cd build
           $ cmake -G "NMake Makefiles" -DKAA_INSTALL_PATH="C:\KaaSdk" -DKAA_DEBUG_ENABLED=1 ..
           $ nmake
           $ nmake install
           ~~~

        For additional cmake options, see the build configuration page for
        [C++ endpoint SDK]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/SDK-integration-instructions/SDK-Linux/#configure-c-endpoint-sdk-1).

## Demo applications

To build and run a Kaa C++ demo, proceed as follows:

1. Download and untar appropriate C++ demo sources from Kaa Sandbox
2. Open the command line terminal [Developer Command Prompt](https://msdn.microsoft.com/en-us/en-en/library/ms229859(v=vs.110).aspx)
and run the following commands:

        $ KAA_BUILD_DIR\env.bat
        $ build.bat deploy

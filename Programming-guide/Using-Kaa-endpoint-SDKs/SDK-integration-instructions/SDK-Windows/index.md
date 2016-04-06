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
    1. [7-Zip](http://www.7-zip.org/)
        1. Install [7-Zip](http://www.7-zip.org/) from [http://www.7-zip.org/](http://www.7-zip.org/)
        2. Add the directory of the installed 7-Zip into your `PATH`
        (Start -> Control Panel -> System -> Advanced -> Environment Variables).
    2. [cmake 3.5.1](https://cmake.org/files/v3.5/cmake-3.5.1-win32-x86.msi)
    3. Python 2.7
        1. Install [Python 2.7(amd64)](https://www.python.org/ftp/python/2.7.9/python-2.7.9.amd64.msi) or [Python 2.7(i386)](https://www.python.org/ftp/python/2.7.9/python-2.7.9.msi) depended on your system architecture
        2. Add the directory where you've installed Python into to your PATH (Start -> Control Panel -> System -> Advanced -> Environment Variables).
    3. [Boost 1.55](http://sourceforge.net/projects/boost/files/boost-binaries/1.55.0-build2/boost_1_55_0-msvc-12.0-32.exe/download)
2. Create a separate directory (for example, `C:\build_kaa`), further in text `KAA_BUILD_DIR`.
    1. Download [Avro 1.7.5](http://archive.apache.org/dist/avro/avro-1.7.5/avro-src-1.7.5.tar.gz) and [Botan 1.11.28](https://github.com/randombit/botan/archive/1.11.28.tar.gz)
    1. Place archives avro-src-1.7.5.tar.gz and Botan-1.11.28.tgz in KAA_BUILD_DIR.
    2. Place env.bat in `KAA_BUILD_DIR`. (File env.bat located in the 'tools' folder in C++ SDK)
    3. Place build_sdk_thirdparty.bat in `KAA_BUILD_DIR`. (File build_sdk_thirdparty.bat located in the 'tools' folder in C++ SDK)
3. Edit env.bat:
    1. Update the `MSVS_HOME` variable. It must point to MS Visual Studio installation directory.
    2. Update the `BOOST_ROOT` variable. It must point to the Boost installation directory (see the Download and install Boost step).
    3. Update the `AVRO_ROOT_DIR` variable. It must point to the directory where Avro binaries and includes will be installed.
    4. Update the `BOTAN_HOME variable`. It must point to the directory where Botan binaries and includes will be installed.
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
           $ cmake -G "NMake Makefiles" -DKAA_INSTALL_PATH="C:\KaaSdk" -DKAA_DEBUG_ENABLED=1 ..`
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

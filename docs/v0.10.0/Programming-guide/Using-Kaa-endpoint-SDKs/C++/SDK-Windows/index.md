---
layout: page
title: Windows
permalink: /:path/
sort_idx: 50
---

{% include variables.md %}

* TOC
{:toc}

This section describes how to build [Kaa C++ SDK]({{root_url}}Glossary/#kaa-sdk-type) for [Microsoft Windows](https://www.microsoft.com/en-us/windows/).

>**NOTE:** This guide is verified against:
>
> * **Host OS:** Windows 7, 8, 10
> * **IDE:** MS Visual Studio 2013, 2015
{:.note}

## Prerequisites

Prior to building Kaa C++ SDK, install the following components on your machine:

1. [Wget](http://downloads.sourceforge.net/gnuwin32/wget-1.11.4-1-setup.exe) and [libarchive](http://downloads.sourceforge.net/gnuwin32/libarchive-2.4.12-1-setup.exe).

2. [Cmake 3.5.1](https://cmake.org/files/v3.5/cmake-3.5.1-win32-x86.msi).

    >**NOTE:** Make sure that you choose one  **Add CMake to system path...** for all users or for current user on the **Install options** step.
    {:.note}
3. Python 2.7 [32-bit](https://www.python.org/ftp/python/2.7.9/python-2.7.9.msi) or [64-bit](https://www.python.org/ftp/python/2.7.9/python-2.7.9.amd64.msi) depending on your system architecture.

4. Boost 1.60 [32-bit](https://sourceforge.net/projects/boost/files/boost-binaries/1.60.0/boost_1_60_0-msvc-14.0-32.exe/download)
    or [64-bit](https://sourceforge.net/projects/boost/files/boost-binaries/1.60.0/boost_1_60_0-msvc-14.0-64.exe/download).

5. 7-Zip [32-bit](http://www.7-zip.org/a/7z1602.exe) or [64-bit](http://www.7-zip.org/a/7z1602-x64.exe). Add the directory where the 7-Zip is stored to your `PATH`.

6. Bzip2 1.0.6 [32-bit](https://github.com/philr/bzip2-windows/releases/download/v1.0.6/bzip2-dll-1.0.6-win-x86.zip) or [64-bit](https://github.com/philr/bzip2-windows/releases/download/v1.0.6/bzip2-dll-1.0.6-win-x64.zip).
    Unzip downloaded archive, add the directory where the unzipped `libbz2.dll` is stored to your `PATH`.

### SDK Prerequisites

Download Kaa C++ SDK `tar.gz` archive from the [Administration UI]({{root_url}}Glossary/#administration-ui) and unpack it.
In the example below, it is unpacked to the `KAA_BUILD_DIR` directory.

You can configure installation by editing the `KAA_BUILD_DIR\tools\env.bat` file.
See table below.

| Parameter | Description | Example values | Default value |
|-----------|-------------|----------------|---------------|
| `BUILD_PLATFORM` | Target architecture. | `x86`, `x64` | `x64` |
| `MSVC_VERSION` | MSVC++ Version. | `14` Visual Studio 2015<br />`12` Visual Studio 2013<br />`11` Visual Studio 2012<br />`10` Visual Studio 2010<br />`9` Visual Studio 2008<br />`8` Visual Studio 2005 | `14` |
| `BOOST_ROOT` | Installation path for Boost libraries. |  | `C:\local\boost_1_60_0\` |
| `ROOT_PATH` | Path where all packages will be installed. |  | `C:\local\` |

Open the [Developer Command Prompt](https://msdn.microsoft.com/en-us/en-en/library/ms229859(v=vs.110).aspx)
and proceed as follows:

1. Switch to `KAA_BUILD_DIR`.

   ```bash
   cd KAA_BUILD_DIR
   ```

2. Build third-party components.

   ```bash
   cd tools
   build_sdk_thirdparty.bat
   ```

    >**NOTE:** By default, the debug configuration is used.
    >To build release versions, use the `release` argument:
    >
    >```bash
    >build_sdk_thirdparty.bat release
    >```
    {:.note}

## Build C++ SDK

To build the Kaa C++ SDK:

1. Open the Developer Command Prompt and run the following commands.

   ```bash
   cd KAA_BUILD_DIR
   tools\env.bat
   avrogen.bat
   mkdir build && cd build
   ```

2. Build Kaa C++ SDK with `nmake`.

   ```bash
   cmake -G "NMake Makefiles" -DCMAKE_FIND_ROOT_PATH=%ROOT_PATH% -DCMAKE_BUILD_TYPE=Debug -DKAA_MAX_LOG_LEVEL=3 ..
   nmake
   ```

3. Generate Visual Studio Project and build it from command line:

<ul>
<ul class="nav nav-tabs">
   <li class="active"><a data-toggle="tab" href="#32">32-bit</a></li>
   <li><a data-toggle="tab" href="#64">64-bit</a></li>
</ul>

<div class="tab-content"><div id="32" class="tab-pane fade in active" markdown="1" >

```bash
cmake -G "Visual Studio 14" -DCMAKE_FIND_ROOT_PATH=%ROOT_PATH% -DCMAKE_BUILD_TYPE=Debug -DKAA_MAX_LOG_LEVEL=3 ..
msbuild kaacpp.vcxproj /property:Platform=%BUILD_PLATFORM%
```

</div><div id="64" class="tab-pane fade" markdown="1" >

```bash
cmake -G "Visual Studio 14 Win64" -DCMAKE_FIND_ROOT_PATH=%ROOT_PATH% -DCMAKE_BUILD_TYPE=Debug -DKAA_MAX_LOG_LEVEL=3 ..
msbuild kaacpp.vcxproj /property:Platform=%BUILD_PLATFORM%
```

</div>
</div>
</ul>

For additional CMake options, see [CMakeLists.txt]({{github_url}}client/client-multi/client-cpp/CMakeLists.txt) file located in the C++ SDK root.

## Build Kaa application

After you installed the required dependencies and built the C++ SDK, you can build and run your [Kaa application]({{root_url}}Glossary/#kaa-application).

Use the [Linux guide]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/C++/SDK-Linux/#build-kaa-application) to build and run your application.
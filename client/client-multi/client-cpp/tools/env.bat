@REM
@REM Copyright 2014-2016 CyberVision, Inc.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off

@REM Set  target Platform(x86|x64)
set BUILD_PLATFORM=x64

@REM Set MSVC++ version from following table
@REM VS version         - MSVC++ version
@REM Visual Studio 2015 - 14
@REM Visual Studio 2013 - 12
@REM Visual Studio 2012 - 11
@REM Visual Studio 2010 - 10
@REM Visual Studio 2008 - 9
@REM Visual Studio 2005 - 8

set MSVC_VERSION=14

@REM Set path for thirparty tools
set MSVS_ROOT=C:\Program Files (x86)\Microsoft Visual Studio %MSVC_VERSION%.0
set BOOST_ROOT=C:\local\boost_1_60_0\
set PYTHON_ROOT=C:\Python27
set GNUWIN32_ROOT=C:\Program Files (x86)\GnuWin32

if not %BUILD_PLATFORM% == x86 (
  set BUILD_PLATFORM=x64
)

@REM Root path which will be passed to CMake during SDK compilation
set ROOT_PATH=C:\local\

@REM zlib configuration
set ZLIB_SRC=zlib-1.2.8
set ZLIB_URL=http://zlib.net/zlib-1.2.8.tar.gz

@REM Avro configuration
set AVRO_SRC=avro-src-1.8.0
set AVRO_URL=http://archive.apache.org/dist/avro/avro-1.8.0/avro-src-1.8.0.tar.gz

@REM Botan configuration
set BOTAN_SRC=Botan-1.11.28
set BOTAN_URL=https://github.com/randombit/botan/archive/1.11.28.tar.gz

@REM Sqlite configuration
set SQLITE_SRC=sqlite-autoconf-3120200
set SQLITE_URL=https://www.sqlite.org/2016/sqlite-autoconf-3120200.tar.gz

@REM PATH configuration
if %BUILD_PLATFORM% == x86 (
 set BOOST_LIBRARYDIR=%BOOST_ROOT%\lib32-msvc-%MSVC_VERSION%.0
) else (
 set BOOST_LIBRARYDIR=%BOOST_ROOT%\lib64-msvc-%MSVC_VERSION%.0
)
set BOTAN_INCLUDE_DIR=%BOTAN_ROOT%\include

set PATH=%ROOT_PATH%\bin;%ROOT_PATH%\lib;%PATH%
set PATH=%MSVS_ROOT%\VC;%BOOST_LIBRARYDIR%;%PYTHON_ROOT%;%GNUWIN32_ROOT%\bin;%SQLITE_ROOT%\lib;%PATH%

call vcvarsall.bat %BUILD_PLATFORM%

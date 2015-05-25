::
:: Copyright 2014-2015 CyberVision, Inc.
::
:: Licensed under the Apache License, Version 2.0 (the "License");
:: you may not use this file except in compliance with the License.
:: You may obtain a copy of the License at
::
::      http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.
::

@echo off

set MSVS_HOME=C:\Program Files (x86)\Microsoft Visual Studio 12.0
set BOOST_ROOT=C:\boost
set AVRO_ROOT_DIR=C:\Avro-cpp
set BOTAN_HOME=C:\botan

set BOOST_LIBRARYDIR=%BOOST_ROOT%\lib32-msvc-12.0
set AVRO_LIBRARYDIR=%AVRO_ROOT_DIR%\lib

set PATH=%MSVS_HOME%\VC;%BOTAN_HOME%;%AVRO_ROOT_DIR%\bin;%AVRO_LIBRARYDIR%;%BOOST_LIBRARYDIR%;%PATH%

call vcvarsall.bat
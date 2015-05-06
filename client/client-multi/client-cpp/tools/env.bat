@echo off

set MSVS_HOME=C:\Program Files (x86)\Microsoft Visual Studio 12.0
set BOOST_ROOT=C:\boost
set AVRO_ROOT_DIR=C:\Avro-cpp
set BOTAN_HOME=C:\botan

set BOOST_LIBRARYDIR=%BOOST_ROOT%\lib32-msvc-12.0
set AVRO_LIBRARYDIR=%AVRO_ROOT_DIR%\lib

set PATH=%MSVS_HOME%\VC;%BOTAN_HOME%;%AVRO_ROOT_DIR%\bin;%AVRO_LIBRARYDIR%;%BOOST_LIBRARYDIR%;%PATH%

call vcvarsall.bat
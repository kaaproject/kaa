@REM
@REM  Copyright 2014-2016 CyberVision, Inc.
@REM
@REM  Licensed under the Apache License, Version 2.0 (the "License");
@REM  you may not use this file except in compliance with the License.
@REM  You may obtain a copy of the License at
@REM
@REM       http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM  Unless required by applicable law or agreed to in writing, software
@REM  distributed under the License is distributed on an "AS IS" BASIS,
@REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM  See the License for the specific language governing permissions and
@REM  limitations under the License.
@REM

@echo off

setlocal

call env.bat
SET BUILD_HOME=%CD%
SET BUILD_TYPE=debug

if "%1" == "" goto startBuild
if /i %1 == release call :setRelease

:startBuild

call :buildKaaThirdparty
goto :eof

:setRelease
  SET BUILD_TYPE=release
goto :eof

:buildKaaThirdparty
   echo Building Kaa thirdparty components...
   call :buildAvro
   call :buildBotan
goto :eof

:buildAvro
 echo Building Avro...
  
 SET AVRO_SRC=avro-src-1.7.5

 IF EXIST %AVRO_SRC%\NUL (
   call :deleteDir %AVRO_SRC%
 )
   
 7z x -y %AVRO_SRC%.tar.gz
 7z x -y %AVRO_SRC%.tar

 md %AVRO_SRC%\lang\c++\build.win
 cd %AVRO_SRC%\lang\c++\build.win

 if %BUILD_TYPE%==debug (
    cmake .. -DCMAKE_INSTALL_PREFIX:PATH=%AVRO_ROOT_DIR% -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=Debug
 ) else (
    cmake .. -DCMAKE_INSTALL_PREFIX:PATH=%AVRO_ROOT_DIR% -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=Release
 )
 nmake install

 cd %BUILD_HOME%

goto :eof

:buildBotan

 echo Building Botan...

 SET BOTAN_SRC=botan-1.11.28

 IF EXIST %BOTAN_SRC%\NUL (
   call :deleteDir %BOTAN_SRC%
 )

 7z x -y -obotan_archive botan-1.11.28.tar.gz
 7z x -y -o. botan_archive\botan-1.11.28.tar.gz

 cd %BOTAN_SRC%

 if %BUILD_TYPE%==debug (
   python configure.py --cc=msvc --cpu=i386 --prefix=%BOTAN_ROOT% --with-debug-info --no-optimizations
 ) else (
   python configure.py --cc=msvc --cpu=i386 --prefix=%BOTAN_ROOT%
 ) 

 nmake install

 move %BOTAN_ROOT%/include/botan-1.11/botan %BOTAN_ROOT%/include

 cd %BUILD_HOME%

goto :eof

:deleteDir
 del /s /f /q %1\*.*
 for /f %%f in ('dir /ad /b %1\') do rd /s /q %1\%%f
 rd /s /q %1
goto :eof

endlocal


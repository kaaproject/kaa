@REM
@REM Copyright 2014-2015 CyberVision, Inc.
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

setlocal EnableDelayedExpansion

if "%1" == "" goto help
if not "%2" == "" goto help

set RUN_DIR=%CD%

set APP_NAME="demo_client"
set PROJECT_HOME=%CD%
set BUILD_DIR=build
set LIBS_PATH=libs
set KAA_LIB_PATH=%LIBS_PATH%\kaa
set KAA_C_LIB_HEADER_PATH=%KAA_LIB_PATH%\src
set KAA_CPP_LIB_HEADER_PATH=%KAA_LIB_PATH%\kaa
set KAA_SDK_TAR="kaa-client*.tar.gz"

set KAA_SDK_TAR_NAME=

call :checkEnv

if /i %1 == build (
   call :build_thirdparty
   call :build_app
   goto :eof
)

if /i %1 == run goto run

if /i %1 == deploy (
   call :clean
   call :build_thirdparty
   call :build_app
   call :run
   goto :eof
)

if /i %1 == clean goto clean

goto help


:build_thirdparty
 IF NOT EXIST %KAA_C_LIB_HEADER_PATH%\NUL (
	IF NOT EXIST %KAA_CPP_LIB_HEADER_PATH%\NUL (
		for /R %PROJECT_HOME% %%f in (kaa-client*.tar.gz) do (
                        set val=%%f
                        set KAA_SDK_TAR_NAME=!val:\=/!
		)
        	md %KAA_LIB_PATH%
        	7z x -y !KAA_SDK_TAR_NAME! -o%KAA_LIB_PATH%
		7z x -y !KAA_SDK_TAR_NAME:~0,-3! -o%KAA_LIB_PATH%
	) 	
 )

 IF NOT EXIST %KAA_LIB_PATH%\%BUILD_DIR%\NUL (
        cd %KAA_LIB_PATH%
        call avrogen.bat 
        md %BUILD_DIR%
        cd %BUILD_DIR%
        cmake -G "NMake Makefiles" ^
	      -DKAA_DEBUG_ENABLED=1 ^
              -DKAA_WITHOUT_EVENTS=1 ^
              -DKAA_WITHOUT_CONFIGURATION=1 ^
              -DKAA_WITHOUT_NOTIFICATIONS=1 ^
              -DKAA_WITHOUT_OPERATION_LONG_POLL_CHANNEL=1 ^
              -DKAA_WITHOUT_OPERATION_HTTP_CHANNEL=1 ^
              -DKAA_MAX_LOG_LEVEL=3 ^
              ..
 )

 cd %PROJECT_HOME%\%KAA_LIB_PATH%\%BUILD_DIR%
 nmake
 cd %PROJECT_HOME%
goto :eof

:build_app
 cd %PROJECT_HOME%
 md %PROJECT_HOME%\%BUILD_DIR%
 cd %BUILD_DIR%
 cmake -G "NMake Makefiles" -DAPP_NAME=%APP_NAME% ..
 nmake
goto :eof

:clean
 call :deleteDir "%KAA_LIB_PATH%\%BUILD_DIR%"
 call :deleteDir "%PROJECT_HOME%\%BUILD_DIR%"
goto :eof

:run
 cd %PROJECT_HOME%\%BUILD_DIR%
 call %APP_NAME%.exe
goto :eof

:deleteDir
 del /s /f /q %1\*.*
 for /f %%f in ('dir /ad /b %1\') do rd /s /q %1\%%f
 rd /s /q %1
goto :eof

:checkEnv

IF EXIST %PROJECT_HOME%\env.bat (
    call %PROJECT_HOME%\env.bat
)

goto :eof

:help
echo "Choose one of the following: {build|run|deploy|clean}"
goto :eof

endlocal
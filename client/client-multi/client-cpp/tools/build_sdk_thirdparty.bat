@echo off            

setlocal

call env.bat
SET BUILD_HOME=%CD%
SET BUILD_TYPE=debug     

if /i %1 == release (
  SET BUILD_TYPE=release
)

call :buildKaaThirdparty
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

 SET BOTAN_SRC=Botan-1.10.9

 IF EXIST %BOTAN_SRC%\NUL (
   call :deleteDir %BOTAN_SRC%
 )

 7z x -y -obotan_archive Botan-1.10.9.tgz 
 7z x -y -o. botan_archive\Botan-1.10.9.tgz 

 cd %BOTAN_SRC%

 if %BUILD_TYPE%==debug (
   python configure.py --cc=msvc --cpu=i386 --prefix=%BOTAN_ROOT% --enable-debug
 ) else (
   python configure.py --cc=msvc --cpu=i386 --prefix=%BOTAN_ROOT%
 ) 

 nmake install

 cd %BUILD_HOME%

goto :eof

:deleteDir
 del /s /f /q %1\*.*
 for /f %%f in ('dir /ad /b %1\') do rd /s /q %1\%%f
 rd /s /q %1
goto :eof

endlocal



if(NOT DEFINED SDK_32bits)
	set(SDK_32bits FALSE)
endif()

#Set the target parameters
set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_VERSION 3.10.17-poky-edison+)
set(CMAKE_SYSTEM_PROCESSOR i686)

if(NOT DEFINED edison_sdk_root AND WIN32)
	if(${SDK_32bits})
		set(edison_sdk_root C:/Edison/poky-edison-eglibc-i686-edison-image-core2-32-toolchain-1.7.2)
	else()
		set(edison_sdk_root C:/Edison/poky-edison-eglibc-x86_64-edison-image-core2-32-toolchain-1.7.2)
	endif()
else(NOT DEFINED edison_sdk_root)
	set(edison_sdk_root /opt/poky-edison/1.7.2)
endif()

#Set the host parameters
if(WIN32)
    	#Windows host
	if(${SDK_32bits})
		set(cross_compiler_sysroot ${edison_sdk_root}/sysroots/i686-pokysdk-mingw32)
		#MinGW make
		set(CMAKE_MAKE_PROGRAM "C:/Program Files/mingw/i686-4.9.2-posix-dwarf-rt_v3-rev1/mingw32/bin/mingw32-make.exe")
	else()
		set(cross_compiler_sysroot ${edison_sdk_root}/sysroots/x86_64-pokysdk-mingw32)
		#MinGW make
		set(CMAKE_MAKE_PROGRAM "C:/Program Files (x86)/mingw/i686-4.9.2-posix-dwarf-rt_v3-rev1/mingw32/bin/mingw32-make.exe")
	endif()
	set(CMAKE_C_COMPILER ${cross_compiler_sysroot}/usr/bin/i586-poky-linux/i586-poky-linux-gcc.exe)
	set(CMAKE_CXX_COMPILER ${cross_compiler_sysroot}/usr/bin/i586-poky-linux/i586-poky-linux-g++.exe)	
else()
	#Linux host
	if(${SDK_32bits})
		set(cross_compiler_sysroot ${edison_sdk_root}/sysroots/i686-pokysdk-linux)
	else()
		set(cross_compiler_sysroot ${edison_sdk_root}/sysroots/x86_64-pokysdk-linux)
	endif()
	set(CMAKE_C_COMPILER ${cross_compiler_sysroot}/usr/bin/i586-poky-linux/i586-poky-linux-gcc)
	set(CMAKE_CXX_COMPILER ${cross_compiler_sysroot}/usr/bin/i586-poky-linux/i586-poky-linux-g++)	
endif()


set(CMAKE_SYSROOT ${edison_sdk_root}/sysroots/core2-32-poky-linux)
set(CMAKE_FIND_ROOT_PATH ${CMAKE_SYSROOT})
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE ONLY)

SET(CMAKE_C_FLAGS "-Os -g3 -Wall --sysroot=${CMAKE_SYSROOT} -m32 -march=i586 -ffunction-sections -fdata-sections"  CACHE STRING "" FORCE)
SET(CMAKE_CXX_FLAGS "-Os -g3 -Wall --sysroot=${CMAKE_SYSROOT} -m32 -march=i586 -ffunction-sections -fdata-sections"  CACHE STRING "" FORCE)

INCLUDE_DIRECTORIES(${CMAKE_SYSROOT}/usr/include)
INCLUDE_DIRECTORIES(${CMAKE_SYSROOT}/usr/include/c++)
INCLUDE_DIRECTORIES(${CMAKE_SYSROOT}/usr/include/c++/i586-poky-linux)

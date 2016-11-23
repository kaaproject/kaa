#
# Copyright 2014-2016 CyberVision, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#
# The following configuration variables are accepted:
#
# * EDISON_SDK_ROOT
#   Absolute path to the Intel Edison SDK root directory.
#   Default value:
#       * Windows:
#           * 32-bit: C:/Edison/poky-edison-eglibc-i686-edison-image-core2-32-toolchain-1.7.2
#           * 64-bit: C:/Edison/poky-edison-eglibc-x86_64-edison-image-core2-32-toolchain-1.7.2
#       * UNIX: /opt/poky-edison/1.6
# * EDISON_MAKE_PROGRAM
# Used on Windows platform to supply absolute path to make utility.
#   Default value:
#       * 32-bit: C:/Program Files/mingw/i686-4.9.2-posix-dwarf-rt_v3-rev1/mingw32/bin/mingw32-make.exe
#       * 64-bit: C:/Program Files (x86)/mingw/i686-4.9.2-posix-dwarf-rt_v3-rev1/mingw32/bin/mingw32-make.exe
#

if(NOT DEFINED SDK_32bits)
    set(SDK_32bits FALSE)
endif()

#Set the target parameters
set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_VERSION 3.10.17-poky-edison+)
set(CMAKE_SYSTEM_PROCESSOR i686)

if(NOT DEFINED EDISON_SDK_ROOT)
    if(WIN32)
        if(SDK_32bits)
            set(EDISON_SDK_ROOT "C:/Edison/poky-edison-eglibc-i686-edison-image-core2-32-toolchain-1.7.2")
        else()
            set(EDISON_SDK_ROOT "C:/Edison/poky-edison-eglibc-x86_64-edison-image-core2-32-toolchain-1.7.2")
        endif()
    else()
        set(EDISON_SDK_ROOT /opt/poky-edison/1.6)
    endif()
endif()

message(STATUS "EDISON_SDK_ROOT: ${EDISON_SDK_ROOT}")

#Set the host parameters
if(WIN32)
    if(NOT DEFINED EDISON_MAKE_PROGRAM)
        if(SDK_32bits)
            set(CMAKE_MAKE_PROGRAM "C:/Program Files/mingw/i686-4.9.2-posix-dwarf-rt_v3-rev1/mingw32/bin/mingw32-make.exe")
        else(SDK_32bits)
            set(CMAKE_MAKE_PROGRAM "C:/Program Files (x86)/mingw/i686-4.9.2-posix-dwarf-rt_v3-rev1/mingw32/bin/mingw32-make.exe")
        endif(SDK_32bits)
    else()
        set(CMAKE_MAKE_PROGRAM "${EDISON_MAKE_PROGRAM}")
    endif()
    #Windows host
    if(SDK_32bits)
		set(cross_compiler_sysroot ${EDISON_SDK_ROOT}/sysroots/i686-pokysdk-mingw32)
	else()
		set(cross_compiler_sysroot ${EDISON_SDK_ROOT}/sysroots/x86_64-pokysdk-mingw32)
	endif()
	set(CMAKE_C_COMPILER ${cross_compiler_sysroot}/usr/bin/i586-poky-linux/i586-poky-linux-gcc.exe)
	set(CMAKE_CXX_COMPILER ${cross_compiler_sysroot}/usr/bin/i586-poky-linux/i586-poky-linux-g++.exe)
else()
	#Linux host
	if(SDK_32bits)
		set(cross_compiler_sysroot ${EDISON_SDK_ROOT}/sysroots/i686-pokysdk-linux)
	else()
		set(cross_compiler_sysroot ${EDISON_SDK_ROOT}/sysroots/x86_64-pokysdk-linux)
	endif()
	set(CMAKE_C_COMPILER ${cross_compiler_sysroot}/usr/bin/i586-poky-linux/i586-poky-linux-gcc)
	set(CMAKE_CXX_COMPILER ${cross_compiler_sysroot}/usr/bin/i586-poky-linux/i586-poky-linux-g++)
endif()


set(CMAKE_SYSROOT ${EDISON_SDK_ROOT}/sysroots/core2-32-poky-linux)
set(CMAKE_FIND_ROOT_PATH ${CMAKE_SYSROOT})
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE ONLY)

SET(CMAKE_C_FLAGS "--sysroot=${CMAKE_SYSROOT} -m32 -march=i586 -ffunction-sections -fdata-sections"  CACHE STRING "" FORCE)
SET(CMAKE_CXX_FLAGS "--sysroot=${CMAKE_SYSROOT} -m32 -march=i586 -ffunction-sections -fdata-sections"  CACHE STRING "" FORCE)

include_directories(${CMAKE_SYSROOT}/usr/include)
include_directories(${CMAKE_SYSROOT}/usr/include/c++)
include_directories(${CMAKE_SYSROOT}/usr/include/c++/i586-poky-linux)

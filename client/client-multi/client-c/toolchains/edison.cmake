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
#   Default value: /opt/poky-edison/1.6
#


if(NOT DEFINED SDK_32bits)
    set(SDK_32bits FALSE)
endif()

#Set the target parameters
set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_VERSION 3.10.17-poky-edison+)
set(CMAKE_SYSTEM_PROCESSOR i686)

if(NOT DEFINED EDISON_SDK_ROOT)
    set(EDISON_SDK_ROOT /opt/poky-edison/1.6)
endif()

message(STATUS "EDISON_SDK_ROOT: ${EDISON_SDK_ROOT}")

if(SDK_32bits)
    set(cross_compiler_sysroot ${EDISON_SDK_ROOT}/sysroots/i686-pokysdk-linux)
else()
    set(cross_compiler_sysroot ${EDISON_SDK_ROOT}/sysroots/x86_64-pokysdk-linux)
endif()
set(CMAKE_C_COMPILER ${cross_compiler_sysroot}/usr/bin/i586-poky-linux/i586-poky-linux-gcc)
set(CMAKE_CXX_COMPILER ${cross_compiler_sysroot}/usr/bin/i586-poky-linux/i586-poky-linux-g++)


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

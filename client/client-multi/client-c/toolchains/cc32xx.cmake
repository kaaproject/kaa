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

include(CMakeForceCompiler)

set(CMAKE_SYSTEM_NAME Generic)

set(ARM_GCC_COMPILER "arm-none-eabi-gcc${CMAKE_EXECUTABLE_SUFFIX}")

# Find toolchain path
if (NOT DEFINED CC32XX_TOOLCHAIN_PATH)
  # Check if GCC is reachable.
  find_path(CC32XX_TOOLCHAIN_PATH bin/${ARM_GCC_COMPILER})

  if (NOT CC32XX_TOOLCHAIN_PATH)
    # Set default path.
    set(CC32XX_TOOLCHAIN_PATH "/opt/kaa/gcc-arm-none-eabi")
    message(STATUS "GCC not found, default path will be used")
  endif ()
endif ()

# Find CC32XX SDK
if (NOT DEFINED CC32XX_SDK)
  set(CC32XX_SDK "/opt/kaa/cc3200-sdk")
  message(STATUS "Default SDK location will be used")
endif ()

message(STATUS "CC32XX SDK path: ${CC32XX_SDK}")
message(STATUS "Toolchain path: ${CC32XX_TOOLCHAIN_PATH}")

# Specify target's environment
set(CMAKE_FIND_ROOT_PATH "${CC32XX_TOOLCHAIN_PATH}/arm-none-eabi/")

set(CMAKE_C_COMPILER   "${CC32XX_TOOLCHAIN_PATH}/bin/arm-none-eabi-gcc${CMAKE_EXECUTABLE_SUFFIX}")
set(CMAKE_CXX_COMPILER "${CC32XX_TOOLCHAIN_PATH}/bin/arm-none-eabi-g++${CMAKE_EXECUTABLE_SUFFIX}")
set(CMAKE_CXX_LINKER   "${CC32XX_TOOLCHAIN_PATH}/bin/arm-none-eabi-ld${CMAKE_EXECUTABLE_SUFFIX}")
set(CMAKE_OBJCOPY
        "${CC32XX_TOOLCHAIN_PATH}/bin/arm-none-eabi-objcopy${CMAKE_EXECUTABLE_SUFFIX}"
        CACHE STRING "Objcopy" FORCE)

CMAKE_FORCE_C_COMPILER(${CMAKE_C_COMPILER} GNU)
CMAKE_FORCE_CXX_COMPILER(${CMAKE_CXX_COMPILER} GNU)

set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE ONLY)

set(CMAKE_C_FLAGS
        "${CMAKE_C_FLAGS} -mthumb -mcpu=cortex-m4 -ffunction-sections -fdata-sections -MD -O0 "
        CACHE STRING "C flags" FORCE)

set(CC32XX_SDK "${CC32XX_SDK}" CACHE STRING "CC32XX SDK location" FORCE)

set(CMAKE_SHARED_LIBRARY_LINK_C_FLAGS)    # remove -rdynamic
set(CMAKE_EXE_LINK_DYNAMIC_C_FLAGS)       # remove -Wl,-Bdynamic

set(CMAKE_C_LINK_EXECUTABLE
    "<CMAKE_C_COMPILER> -Wl,--entry=ResetISR,--gc-sections -o <TARGET> <OBJECTS> <LINK_LIBRARIES>")

# These includes placed here intentionally. Include dirs must be moved to
# the target-level support modules when such will be ready.
# Until then all project will be exposed with target-level code.
include_directories(
        ${CC32XX_SDK}/inc
        ${CC32XX_SDK}/driverlib
        ${CC32XX_SDK}/simplelink
        ${CC32XX_SDK}/simplelink/include
        ${CC32XX_SDK}/simplelink/source
        ${CC32XX_SDK}/simplelink_extlib/provisioninglib
        ${CC32XX_SDK}/example/common
        )

add_definitions(-Dgcc -DCC32XX -DUSER_INPUT_ENABLE)

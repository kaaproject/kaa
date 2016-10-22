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

#######################
### Toolchain setup ###
#######################

set(CMAKE_SYSTEM_NAME "Generic")

include(CMakeForceCompiler)

set(XTENSA_GCC_COMPILER "xtensa-lx106-elf-gcc${CMAKE_EXECUTABLE_SUFFIX}")

if(NOT DEFINED ESP8266_TOOLCHAIN_PATH)
    # Check if GCC is reachable.
    find_path(ESP8266_TOOLCHAIN_PATH bin/${XTENSA_GCC_COMPILER})

    if(NOT ESP8266_TOOLCHAIN_PATH)
        # Set default path.
        set(ESP8266_TOOLCHAIN_PATH /opt/Espressif/crosstool-NG/builds/xtensa-lx106-elf)
        message(STATUS "GCC not found, default path will be used: ${ESP8266_TOOLCHAIN_PATH}")
    endif()
else()
    message(STATUS "Toolchain path: ${ESP8266_TOOLCHAIN_PATH}")
endif()

cmake_force_c_compiler(${ESP8266_TOOLCHAIN_PATH}/bin/${XTENSA_GCC_COMPILER} GNU)

set(CMAKE_OBJCOPY ${ESP8266_TOOLCHAIN_PATH}/bin/xtensa-lx106-elf-objcopy CACHE PATH "")

#########################
### ESP8266 SDK setup ###
#########################

if(NOT DEFINED ESP8266_SDK_PATH)
    set(ESP8266_SDK_PATH /opt/Espressif/esp-rtos-sdk)
    message(STATUS "Default SDK location will be used: ${ESP8266_SDK_PATH}")
else()
    message(STATUS "ESP8266 SDK path: ${ESP8266_SDK_PATH}")
endif()

set(CMAKE_LIBRARY_PATH ${ESP8266_SDK_PATH}/lib/)

set(ESP8266_INCDIRS
        ${ESP8266_SDK_PATH}/extra_include
        ${ESP8266_SDK_PATH}/include
        ${ESP8266_SDK_PATH}/include/lwip
        ${ESP8266_SDK_PATH}/include/lwip/ipv4
        ${ESP8266_SDK_PATH}/include/lwip/ipv6
        ${ESP8266_SDK_PATH}/include/espressif/
        ${ESP8266_TOOLCHAIN_PATH}/lib/gcc/xtensa-lx106-elf/4.8.2/include/
        )

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-comment -fno-builtin -Wno-implicit-function-declaration -Os -Wpointer-arith -Wl,-EL,--gc-sections -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals -ffunction-sections" CACHE FORCE "")
set(CMAKE_C_LINK_EXECUTABLE "<CMAKE_C_COMPILER> <FLAGS> <CMAKE_C_LINK_FLAGS> <LINK_FLAGS> -o <TARGET> -Wl,--start-group <OBJECTS> <LINK_LIBRARIES> -Wl,--end-group" CACHE FORCE "")

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

include(CMakeForceCompiler)

set(ARM_GCC_COMPILER "xtensa-lx106-elf-gcc${CMAKE_EXECUTABLE_SUFFIX}")

if(DEFINED ENV{ESP8266_TOOLCHAIN_PATH})
  set(TOOLCHAIN_PATH "$ENV{ESP8266_TOOLCHAIN_PATH}")
  message(STATUS "Toolchain path is provided: ${TOOLCHAIN_PATH}")
else ()
  # Check if GCC is reachable.
  find_path(TOOLCHAIN_PATH bin/${ARM_GCC_COMPILER})

  if (NOT TOOLCHAIN_PATH)
    # Set default path.
    set(TOOLCHAIN_PATH /opt/Espressif/crosstool-NG/builds/xtensa-lx106-elf)
    message(STATUS "GCC not found, default path will be used")
  endif ()
endif ()

CMAKE_FORCE_C_COMPILER(${TOOLCHAIN_PATH}/bin/xtensa-lx106-elf-gcc GNU)


#########################
### ESP8266 SDK setup ###
#########################

if(DEFINED ENV{ESP8266_SDK_BASE})
    set(ESP8266_SDK_BASE $ENV{ESP8266_SDK_BASE})
else()
    set(ESP8266_SDK_BASE /opt/Espressif/esp-rtos-sdk)
endif()

set(CMAKE_LIBRARY_PATH ${ESP8266_SDK_BASE}/lib/)

set(ESP8266_INCDIRS
    ${ESP8266_SDK_BASE}/extra_include
    ${ESP8266_SDK_BASE}/include
    ${ESP8266_SDK_BASE}/include/lwip
    ${ESP8266_SDK_BASE}/include/lwip/ipv4
    ${ESP8266_SDK_BASE}/include/lwip/ipv6
    ${ESP8266_SDK_BASE}/include/espressif/
    ${TOOLCHAIN_PATH}/lib/gcc/xtensa-lx106-elf/4.8.2/include/
    )

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-comment -fno-builtin -Wno-implicit-function-declaration -Os -Wpointer-arith  -Wl,-EL -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals -ffunction-sections")


#
#  Copyright 2014-2016 CyberVision, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

include(CMakeForceCompiler)

if(NOT DEFINED ENV{ESPRESSIF_HOME})
    set(ESPRESSIF_HOME /opt/Espressif)
else()
    set(ESPRESSIF_HOME $ENV{ESPRESSIF_HOME})
endif()

set(ESP_TOOLCHAIN_BASE ${ESPRESSIF_HOME}/crosstool-NG/builds/xtensa-lx106-elf/bin)

set(ESP_SDK_BASE ${ESPRESSIF_HOME}/esp-rtos-sdk)

CMAKE_FORCE_C_COMPILER(${ESP_TOOLCHAIN_BASE}/xtensa-lx106-elf-gcc GNU)

set(CMAKE_LIBRARY_PATH ${ESP_SDK_BASE}/lib/)

set(ESP8266_INCDIRS 
    ${ESP_SDK_BASE}/extra_include ${ESP_SDK_BASE}/include
    ${ESP_SDK_BASE}/include/lwip ${ESP_SDK_BASE}/include/lwip/ipv4
    ${ESP_SDK_BASE}/include/lwip/ipv6 ${ESP_SDK_BASE}/include/espressif/
    ${ESP_TOOLCHAIN_BASE}/../lib/gcc/xtensa-lx106-elf/4.8.2/include/
    )

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-comment -fno-builtin -Wno-implicit-function-declaration -Os -Wpointer-arith  -Wl,-EL -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals -ffunction-sections")


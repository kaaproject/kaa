#
# Copyright 2014 CyberVision, Inc.
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

cmake_minimum_required(VERSION 2.8.8)

project (C-SDK-sample)
enable_language(C)

find_package (OpenSSL REQUIRED)

set(KAA_SDK_ROOT /usr/local/kaa-esp8266)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=gnu99 -Os -Wall -Wextra")

set(ESP_TOOLCHAIN_DIR /opt/Espressif/crosstool-NG/builds/xtensa-lx106-elf/bin)

set(ESP_SDK_BASE /opt/Espressif/esp-rtos-sdk)

set(ESP8266_SRC user/user_main.c driver/uart.c)

set(ESP8266_LIBS "-lfreertos -lhal -lgcc -lpp -lphy -lnet80211 -lwpa -llwip -lmain -lssl")

set(ESP8266_INCDIRS ${ESP_SDK_BASE}/extra_include ${ESP_SDK_BASE}/include ${ESP_SDK_BASE}/include/lwip ${ESP_SDK_BASE}/include/lwip/ipv4 ${ESP_SDK_BASE}/include/lwip/ipv6 ${ESP_SDK_BASE}/include/espressif/ )

set(ESP8266_LINKSCRIPT ${CMAKE_CURRENT_SOURCE_DIR}/ld/eagle.app.v6.ld)

set(ESP8266_LIBDIR "-L${CMAKE_CURRENT_SOURCE_DIR}/build -L${ESP_TOOLCHAIN_DIR}/../lib/gcc/xtensa-lx106-elf/4.8.2/ ")
                    
set(ESP8266_LIBDIR "${ESP8266_LIBDIR} -L${ESP_SDK_BASE}/lib -L${CMAKE-CMAKE_CURRENT_SOURCE_DIR} -L${ESP_TOOLCHAIN_DIR}/ld")

set(ESP8266_LDFLAGS "${ESP8266_LIBDIR} -T${ESP8266_LINKSCRIPT} -nostdlib -u call_user_start -static --start-group ${ESP8266_LIBS} -lesp-platform -lkaac_s -l${APP_NAME}_s --end-group --gc-sections -Map map")

set(CMAKE_AR ${ESP_TOOLCHAIN_DIR}/xtensa-lx106-elf-ar)

set(CMAKE_RANLIB ${ESP_TOOLCHAIN_DIR}/xtensa-lx106-elf-ranlib)

set(CMAKE_C_LINK_EXECUTABLE "${ESP_TOOLCHAIN_DIR}/xtensa-lx106-elf-ld <LINK_FLAGS> <OBJECTS>")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DESP8266_PLATFORM -Os -Wpointer-arith -Wl,-EL -fno-inline-functions -nostdlib -mlongcalls -mtext-section-literals -D__ets__ -DICACHE_FLASH")

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src
                    ${CMAKE_CURRENT_SOURCE_DIR}/include
                    ${KAA_SDK_ROOT}/include
                    ${ESP8266_INCDIRS}
                   )

link_directories(${KAA_SDK_ROOT}/lib
                 ${ESP_SDK_BASE}/lib
                 ${CMAKE_CURRENT_SOURCE_DIR}/ld
                
                )

set (SRC
    src/kaa_demo.c
    )

add_library(esp-platform STATIC ${ESP8266_SRC})
add_executable(${APP_NAME} blank.c)
add_dependencies(${APP_NAME} esp-platform ${APP_NAME}_s kaac_s)
add_library(${APP_NAME}_s STATIC ${SRC})

add_custom_command(TARGET ${APP_NAME}
                   POST_BUILD
                   COMMAND esptool.py elf2image -o ./ ${APP_NAME}
                   & echo "Making firmware image"
                  )
set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} ${ESP8266_LDFLAGS} -o ${APP_NAME}")


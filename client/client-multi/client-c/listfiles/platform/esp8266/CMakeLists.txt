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

# esp8266-rtos-sdk has some warnings, so we put -Wno-comment and -Wno-pedantic to suppress them
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DESP8266_PLATFORM -Wno-comment -Wno-pedantic")

set(ESP8266_SRC_FOLDER ${KAA_SRC_FOLDER}/platform-impl/esp8266)

set(ESP8266_NAME "esp8266")

set(ESP8266_SRC
    ${ESP8266_SRC_FOLDER}/esp8266_logger.c
    ${ESP8266_SRC_FOLDER}/esp8266_kaa_client.c
    ${ESP8266_SRC_FOLDER}/esp8266_configuration_persistence.c
    ${ESP8266_SRC_FOLDER}/esp8266_status.c
    ${ESP8266_SRC_FOLDER}/esp8266_key_utils.c
    ${ESP8266_SRC_FOLDER}/esp8266_tcp_utils.c
    ${ESP8266_SRC_FOLDER}/esp8266_time.c
    ${ESP8266_SRC_FOLDER}/exit.c
    ${ESP8266_SRC_FOLDER}/snprintf.c
    ${KAA_SRC_FOLDER}/platform-impl/common/sha.c
    ${KAA_SRC_FOLDER}/platform-impl/common/sha1.c
    ${KAA_SRC_FOLDER}/platform-impl/common/kaa_failover_strategy.c
    ${KAA_SRC_FOLDER}/platform-impl/common/ext_log_storage_memory.c
    ${KAA_SRC_FOLDER}/platform-impl/common/ext_log_upload_strategies.c
    ${KAA_SRC_FOLDER}/platform-impl/common/kaa_tcp_channel.c
    ${KAA_SRC_FOLDER}/kaa_protocols/kaa_tcp/kaatcp_parser.c
    ${KAA_SRC_FOLDER}/kaa_protocols/kaa_tcp/kaatcp_request.c
    )

set(KAA_SOURCE_FILES ${KAA_SOURCE_FILES} ${ESP8266_SRC})

set(KAA_INCLUDE_PATHS ${KAA_INCLUDE_PATHS} ${ESP8266_INCDIRS})

set(KAA_BUILD_STATIC_ONLY 1)

execute_process(COMMAND java -jar ${CMAKE_CURRENT_SOURCE_DIR}/tools/pub_key_generator.jar
                ${CMAKE_CURRENT_BINARY_DIR}/rsa.h)
include_directories(${CMAKE_CURRENT_BINARY_DIR})

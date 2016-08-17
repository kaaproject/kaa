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

set(EXTENSION_LOGGING_SOURCE_FILES
    ${CMAKE_CURRENT_LIST_DIR}/kaa_logging.c)

set(EXTENSION_LOGGING_HEADER_FILES
    ${CMAKE_CURRENT_LIST_DIR}/kaa_logging.h)

add_library(extension_logging ${EXTENSION_LOGGING_SOURCE_FILES})
target_include_directories(extension_logging PUBLIC ${CMAKE_CURRENT_LIST_DIR})
target_link_libraries(extension_logging PUBLIC kaac)

kaa_add_unit_test(NAME test_log
    SOURCES
    ${CMAKE_CURRENT_LIST_DIR}/test/test_kaa_log.c
    test/kaa_test_external.c
    ${KAA_SRC_FOLDER}/gen/kaa_logging_gen.c
    ${KAA_SRC_FOLDER}/avro_src/io.c
    ${KAA_SRC_FOLDER}/avro_src/encoding_binary.c
    ${KAA_SRC_FOLDER}/collections/kaa_list.c
    ${KAA_SRC_FOLDER}/utilities/kaa_log.c
    ${KAA_SRC_FOLDER}/platform-impl/posix/logger.c
    ${KAA_SRC_FOLDER}/kaa_platform_utils.c
    ${KAA_SRC_FOLDER}/kaa_channel_manager.c
    ${KAA_SRC_FOLDER}/kaa_common_schema.c
    ${KAA_SRC_FOLDER}/kaa_status.c
    ${KAA_SRC_FOLDER}/platform-impl/common/kaa_failover_strategy.c
    DEPENDS
    kaac extension_logging)

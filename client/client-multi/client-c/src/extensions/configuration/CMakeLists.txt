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

set(EXTENSION_CONFIGURATION_SOURCE_FILES
    ${CMAKE_CURRENT_LIST_DIR}/kaa_configuration_manager.c)

set(EXTENSION_CONFIGURATION_HEADER_FILES
    ${CMAKE_CURRENT_LIST_DIR}/kaa_configuration_manager.h)

add_library(extension_configuration ${EXTENSION_CONFIGURATION_SOURCE_FILES})
target_include_directories(extension_configuration PUBLIC ${CMAKE_CURRENT_LIST_DIR})
target_link_libraries(extension_configuration PUBLIC kaac)

kaa_add_unit_test(NAME test_kaa_configuration_manager
    SOURCES
    ${CMAKE_CURRENT_LIST_DIR}/test/test_kaa_configuration.c
    ${KAA_SRC_FOLDER}/platform-impl/common/kaa_failover_strategy.c
    test/kaa_test_external.c
    DEPENDS
    kaac extension_configuration)

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

if(KAA_UNITTESTS_COMPILE)
    find_package(cmocka REQUIRED)
    find_program(MEMORYCHECK_COMMAND valgrind)
    set(MEMORYCHECK_COMMAND_OPTIONS "--leak-check=full --show-reachable=yes --trace-children=yes -v")

    include(CTest)
    include(CMakeParseArguments)
endif()

################################################################################
# Creates an unit test with given name and dependencies.
#
# TODO: move this function to the separate module, after new design will be implemented.
#       When new SDK build system will be ready, tests will be encapsulated
#       within each production module. Test system helpers, like this function
#       will be moved to separate cmake build modules or scripts.
#
# Syntax:
# kaa_add_unit_test(NAME test_name
#                   SOURCES test_sources_files...
#                   [DEPENDS list_of_dependencies...]
#                   [INC_DIRS list_of_include_directories...])
function(kaa_add_unit_test)
    if(KAA_UNITTESTS_COMPILE)
        cmake_parse_arguments(
            UNIT_TEST
            ""
            "NAME"
            "SOURCES;DEPENDS;INC_DIRS"
            ${ARGN})

        if (NOT DEFINED UNIT_TEST_NAME AND DEFINED UNIT_TEST_SOURCES)
            message(FATAL_ERROR "Test sources and name must be defined!")
        endif()

        add_executable(${UNIT_TEST_NAME} ${UNIT_TEST_SOURCES})
        add_test(NAME ${UNIT_TEST_NAME} COMMAND ${UNIT_TEST_NAME})
        target_link_libraries(${UNIT_TEST_NAME} ${CMOCKA_LIBRARIES})

        if(UNIT_TEST_DEPENDS)
            target_link_libraries(${UNIT_TEST_NAME} ${UNIT_TEST_DEPENDS})
        endif()

        target_include_directories(
            ${UNIT_TEST_NAME}
            PRIVATE
            test)
        if(UNIT_TEST_INC_DIRS)
            target_include_directories(
                ${UNIT_TEST_NAME}
                PRIVATE
                ${UNIT_TEST_INC_DIRS})
        endif()

        message("-----------------------------------------------")
        message("    Test added: ${UNIT_TEST_NAME}")
        message("    Test sources: ${UNIT_TEST_SOURCES}")
        message("    Test dependencies: ${UNIT_TEST_DEPENDS}")
        message("    Test includes: ${UNIT_TEST_INC_DIRS}")
        message("-----------------------------------------------")
    endif()
endfunction()

################################################################################

configure_file("${CMAKE_CURRENT_SOURCE_DIR}/sonar-project.properties.in"
        "${CMAKE_CURRENT_SOURCE_DIR}/sonar-project.properties")


if(WITH_EXTENSION_LOGGING)
    kaa_add_unit_test(NAME test_ext_log_storage_memory
        SOURCES
        test/platform-impl/test_ext_log_storage_memory.c
        test/kaa_test_external.c
        DEPENDS
        kaac
        INC_DIRS
        test)

    kaa_add_unit_test(NAME test_ext_log_upload_strategy_by_volume
        SOURCES
        test/platform-impl/test_ext_log_upload_strategies.c
        test/kaa_test_external.c
        DEPENDS
        kaac
        INC_DIRS
        test)

    kaa_add_unit_test(NAME test_platform_protocol
        SOURCES
        test/test_platform_protocol.c
        DEPENDS
        kaac)
endif()

kaa_add_unit_test(NAME test_meta_extension
        SOURCES
        test/test_meta_extension.c
        test/kaa_test_external.c
        DEPENDS
        kaac)

kaa_add_unit_test(NAME test_platform_utils
        SOURCES
        test/test_platform_utils.c
        test/kaa_test_external.c
        DEPENDS
        kaac)

kaa_add_unit_test(NAME test_context
        SOURCES
        test/test_kaa_context.c
        test/kaa_test_external.c
        DEPENDS
        kaac)

kaa_add_unit_test(NAME test_status
        SOURCES
        test/test_kaa_status.c
        DEPENDS
        kaac)

kaa_add_unit_test(NAME test_common
        SOURCES
        test/test_kaa_common.c
        test/kaa_test_external.c
        DEPENDS
        kaac)

kaa_add_unit_test(NAME test_list
        SOURCES
        test/collections/test_kaa_list.c
        src/kaa/collections/kaa_list.c
        INC_DIRS
        src/kaa ${KAA_INCLUDE_PATHS} test)

kaa_add_unit_test(NAME test_kaatcp_parser
        SOURCES
        test/kaatcp/kaatcp_parser_test.c
        test/kaa_test_external.c
        DEPENDS
        kaac
        INC_DIRS
        test)

kaa_add_unit_test(NAME test_kaatcp_request
        SOURCES
        test/kaatcp/kaatcp_request_test.c
        test/kaa_test_external.c
        DEPENDS
        kaac
        INC_DIRS
        test)

kaa_add_unit_test(NAME test_kaa_tcp_channel_bootstrap
        SOURCES
        test/kaa_tcp_channel/test_kaa_tcp_channel_bootstrap.c
        test/kaa_test_external.c
        DEPENDS
        kaac
        INC_DIRS
        test)

kaa_add_unit_test(NAME test_kaa_common_schema
        SOURCES
        test/test_kaa_common_schema.c
        test/kaa_test_external.c
        DEPENDS
        kaac)

kaa_add_unit_test(NAME test_kaa_reallocation
        SOURCES
        test/utilities/test_kaa_reallocation.c
        DEPENDS
        kaac
        INC_DIRS
        test)

kaa_add_unit_test(NAME test_kaa_extension
        SOURCES
        test/test_kaa_extension.c src/kaa/kaa_extension.c
        INC_DIRS
        test/kaa_extension src/kaa)

kaa_add_unit_test(NAME test_kaa_extension_private
        SOURCES
        test/test_kaa_extension_private.c src/kaa/kaa_extension.c
        INC_DIRS
        src/kaa src/extensions/bootstrap ${KAA_INCLUDE_PATHS})

# KAA-985
#kaa_add_unit_test(NAME test_kaa_tcp_channel_operation
#        SOURCES
#        test/kaa_tcp_channel/test_kaa_tcp_channel_operation.c
#        test/kaa_test_external.c
#        DEPENDS
#        kaac
#        INC_DIRS
#        test)

kaa_add_unit_test(NAME test_kaa_channel_manager
        SOURCES
        test/test_kaa_channel_manager.c
        DEPENDS
        kaac)

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

project (Kaa-c)
enable_language(C)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_CURRENT_SOURCE_DIR}/Modules)

SET(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC -std=c1x -Wall")
#SET(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DKAA_TRACE_MEMORY_ALLOCATIONS")

if("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
    SET(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -undefined dynamic_lookup")
endif()

if (CMAKE_COMPILER_IS_GNUCC)
# TODO: add switch for Debug/Release versions
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -g")
if (KAA_COLLECT_COVERAGE)
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} --coverage")
endif ()

#    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os")
endif ()

if (KAA_WITHOUT_EVENTS)
    set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DKAA_DISABLE_FEATURE_EVENTS")
endif()

if (KAA_WITHOUT_LOGGING)
    set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DKAA_DISABLE_FEATURE_LOGGING")
endif()

# set max log level
# TODO: add switch for Debug/Release versions
SET(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DKAA_MAX_LOG_LEVEL=6")

find_package (OpenSSL REQUIRED)

include_directories (${CMAKE_CURRENT_SOURCE_DIR}/src
)

set (KAA_SOURCE_FILES
                       src/avro_src/io.c
                       src/avro_src/encoding_binary.c
                       src/collections/kaa_deque.c
                       src/collections/kaa_list.c
                       src/gen/kaa_endpoint_gen.c
                       src/kaa_bootstrap.c
                       src/kaa_channel_manager.c
                       src/kaa_common_schema.c
                       src/kaa_common.c
                       src/kaa_event.c
                       src/kaa_logging.c
                       src/kaa_platform_protocol.c
                       src/kaa_profile.c
                       src/kaa_status.c
                       src/kaa_user.c
                       src/kaa.c
                       src/log/kaa_memory_log_storage.c
                       src/utilities/kaa_log.c
                       src/utilities/kaa_mem.c
                       src/utilities/kaa_uuid_int.c

)

include(${CMAKE_CURRENT_SOURCE_DIR}/listfiles/CMakeGen.cmake)

add_library(kaac_o OBJECT ${KAA_SOURCE_FILES})

add_library (kaac SHARED ./src $<TARGET_OBJECTS:kaac_o>)

target_link_libraries(kaac ssl crypto)

add_library (kaac_s STATIC ./src $<TARGET_OBJECTS:kaac_o>)

if (KAA_UNITTESTS_COMPILE)

set (CUNIT_LIB_NAME "")
find_package (CUnit)

if (CUNIT_FOUND)
    set (CUNIT_LIB_NAME ${CUNIT_LIBRARIES})
    set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DKAA_TEST_CUNIT_ENABLED")
endif()

configure_file (
                    "${CMAKE_CURRENT_SOURCE_DIR}/sonar-project.properties.in"
                    "${CMAKE_CURRENT_SOURCE_DIR}/sonar-project.properties"
)

add_executable  (test_context
                    test/test_kaa_context.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_context kaac crypto ${CUNIT_LIB_NAME})

add_executable  (test_event
                    test/test_kaa_event.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_event kaac crypto ${CUNIT_LIB_NAME})

add_executable  (test_status
                    test/test_kaa_status.c
                )
target_link_libraries(test_status kaac crypto ${CUNIT_LIB_NAME})

add_executable  (test_bootstrap
                    test/test_kaa_bootstrap.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_bootstrap kaac crypto ${CUNIT_LIB_NAME})

add_executable  (test_profile
                    src/gen/kaa_profile_gen.c
                    test/test_kaa_profile.c
                   test/kaa_test_external.c
                )
target_link_libraries(test_profile kaac crypto ${CUNIT_LIB_NAME})

add_executable  (test_common
                    test/test_kaa_common.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_common kaac crypto ${CUNIT_LIB_NAME})

add_executable  (test_log
                    src/gen/kaa_logging_gen.c
                    test/test_kaa_log.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_log kaac crypto ${CUNIT_LIB_NAME})

add_executable  (test_deque
                    test/test_kaa_deque.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_deque kaac crypto ${CUNIT_LIB_NAME})

endif ()

install (TARGETS kaac kaac_s
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
    RUNTIME DESTINATION lib)

install (DIRECTORY src/ DESTINATION include/kaac
    FILES_MATCHING PATTERN *.h)

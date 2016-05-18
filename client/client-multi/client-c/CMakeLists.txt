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

cmake_minimum_required(VERSION 2.8.8)

project(Kaa-c)
enable_language(C)
enable_testing()

option(WITH_EXTENSION_CONFIGURATION "Enable configuration extension" ON)
option(WITH_EXTENSION_EVENT "Enable event extension" ON)
option(WITH_EXTENSION_LOGGING "Enable logging extension" ON)

# Expose Kaa SDK directory to all modules
set(KAA_SDK_DIR ${CMAKE_CURRENT_LIST_DIR})

# Path to CMake's scripts.
set(CMAKE_MODULE_PATH
        ${CMAKE_MODULE_PATH}
        ${CMAKE_CURRENT_SOURCE_DIR}/Modules
        )

#
# Sets common compilation flags.
#
# NOTE: Platform specific compilation flags should be set
# in the corresponding platform CMake script(s).
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99 -Wall -Wextra -pedantic")

set(CMAKE_C_FLAGS_MINSIZEREL "${CMAKE_C_FLAGS_MINSIZEREL} -Os")

# Compiler specific flags.
if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang" AND APPLE)
    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -undefined dynamic_lookup")
endif ()

if (CMAKE_COMPILER_IS_GNUCC)
    set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -O0 -ggdb")

    if (KAA_COLLECT_COVERAGE)
        set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} --coverage")
    endif ()
endif ()

# Creates variable to specify thirdparty libraries.
set(KAA_THIRDPARTY_LIBRARIES "")

# Sets the default build platform to POSIX.
if (NOT DEFINED KAA_PLATFORM)
    set(KAA_PLATFORM "x86-64")
endif ()

#
# Sets maximum Kaa SDK log level.
#
# Logging constants:
# NONE  - 0
# FATAL - 1
# ERROR - 2
# WARN  - 3
# INFO  - 4
# DEBUG - 5
# TRACE - 6
if (NOT DEFINED KAA_MAX_LOG_LEVEL)
    if ("${CMAKE_BUILD_TYPE}" STREQUAL "Debug")
        set(KAA_MAX_LOG_LEVEL 6)
    else ()
        set(KAA_MAX_LOG_LEVEL 4)
    endif ()
endif ()

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DKAA_MAX_LOG_LEVEL=${KAA_MAX_LOG_LEVEL}")

message("==================================")
# Prints build parameters.
if("${CMAKE_BUILD_TYPE}" STREQUAL "")
    message("BUILD_TYPE = (Default)")
else()
    message("BUILD_TYPE = ${CMAKE_BUILD_TYPE}")
endif()
message("KAA_PLATFORM = ${KAA_PLATFORM}")
message("KAA_MAX_LOG_LEVEL = ${KAA_MAX_LOG_LEVEL}")
message("==================================")

# Sets path(s) to header files.
set(KAA_SRC_FOLDER "${CMAKE_CURRENT_LIST_DIR}/src/kaa")

#include platform-depended CMakeLists.txt
include(${CMAKE_CURRENT_LIST_DIR}/listfiles/platform/${KAA_PLATFORM}/CMakeLists.txt)

# Sets path(s) to source files.
set(KAA_SOURCE_FILES
        ${KAA_SOURCE_FILES}
        ${KAA_SRC_FOLDER}/avro_src/io.c
        ${KAA_SRC_FOLDER}/avro_src/encoding_binary.c
        ${KAA_SRC_FOLDER}/collections/kaa_list.c
        ${KAA_SRC_FOLDER}/utilities/kaa_log.c
        ${KAA_SRC_FOLDER}/utilities/kaa_mem.c
        ${KAA_SRC_FOLDER}/utilities/kaa_buffer.c
        ${KAA_SRC_FOLDER}/kaa_platform_utils.c
        ${KAA_SRC_FOLDER}/kaa_platform_protocol.c
        ${KAA_SRC_FOLDER}/kaa_bootstrap_manager.c
        ${KAA_SRC_FOLDER}/kaa_channel_manager.c
        ${KAA_SRC_FOLDER}/kaa_common_schema.c
        ${KAA_SRC_FOLDER}/kaa_profile.c
        ${KAA_SRC_FOLDER}/kaa_status.c
        ${KAA_SRC_FOLDER}/kaa_user.c
        ${KAA_SRC_FOLDER}/kaa.c
        ${KAA_SRC_FOLDER}/kaa_notification_manager.c
        ${KAA_SRC_FOLDER}/kaa_extension.c
        ${KAA_SRC_FOLDER}/platform-impl/common/kaa_htonll.c
        )

# Includes auto-generated Cmake's scripts.
include(${CMAKE_CURRENT_LIST_DIR}/listfiles/CMakeGen.cmake)

# Kaa include directories
set(KAA_INCLUDE_DIRS
        ${KAA_INCLUDE_PATHS}         # Provided by platform cmake
        ${CMAKE_CURRENT_SOURCE_DIR}/src
        ${KAA_SRC_FOLDER}
        ${KAA_THIRDPARTY_INCLUDE_DIR})

# Build unit tests.
include(${CMAKE_CURRENT_LIST_DIR}/listfiles/UnitTest.cmake)

# Build Kaa libraries.
add_library(kaac ${KAA_SOURCE_FILES})
target_include_directories(kaac PUBLIC ${KAA_INCLUDE_DIRS})
target_link_libraries(kaac PRIVATE ${KAA_THIRDPARTY_LIBRARIES})

# Disables Kaa library modules.
if(WITH_EXTENSION_CONFIGURATION)
    message("CONFIGURATION ENABLED")

    include(${CMAKE_CURRENT_LIST_DIR}/src/extensions/configuration/CMakeLists.txt)
    target_link_libraries(kaac PRIVATE extension_configuration)
else()
    add_definitions(-DKAA_DISABLE_FEATURE_CONFIGURATION)
endif()

if (WITH_EXTENSION_EVENT)
    message("EVENTS ENABLED")

    include(${CMAKE_CURRENT_LIST_DIR}/src/extensions/event/CMakeLists.txt)
    target_link_libraries(kaac PRIVATE extension_event)
else ()
    add_definitions(-DKAA_DISABLE_FEATURE_EVENTS)
endif ()

if (WITH_EXTENSION_LOGGING)
    message("LOGGING ENABLED")

    include(${CMAKE_CURRENT_LIST_DIR}/src/extensions/logging/CMakeLists.txt)
    target_link_libraries(kaac PRIVATE extension_logging)
else ()
    add_definitions(-DKAA_DISABLE_FEATURE_LOGGING)
endif ()

if (KAA_WITHOUT_NOTIFICATION)
    add_definitions(-DKAA_DISABLE_FEATURE_NOTIFICATION)
else ()
    message("NOTIFICATION ENABLED")
endif ()

message("KAA WILL BE INSTALLED TO  ${CMAKE_INSTALL_PREFIX} ")
install(DIRECTORY ${KAA_SRC_FOLDER}/ DESTINATION include/kaa
        FILES_MATCHING PATTERN *.h)

install(TARGETS kaac DESTINATION lib)

add_subdirectory(Modules/doxygen)

add_subdirectory(Modules/cppcheck)

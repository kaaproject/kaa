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

# List of avaliable configuration parameters:
#
#	-  `KAA_MAX_LOG_LEVEL` - Maximum log level used by C SDK. The higher value, the more detailed logs.
#
#	Values:
#
#	- `0` - NONE (no logs)
#	- `1` - FATAL
#	- `2` - ERROR
#	- `3` - WARN
#	- `4` - INFO
#	- `5` - DEBUG
#	- `6` - TRACE
#
#	Default: If build type is `Release`, `KAA_MAX_LOG_LEVEL=4`. If `Debug`, `KAA_MAX_LOG_LEVEL=6`.
#
#	- `WITH_EXTENSION_[EXTENSION_NAME]` - specifies which extensions should be included to build.
#
#	Extensions' names:
#
#	- PROFILE
#	- CONFIGURATION
#	- EVENT
#	- LOGGING
#	- NOTIFICATION
#	- USER
#
#	Values:
#
#	- `ON` - enable extension
#	- `OFF` - disable extension
#
#	Default: `ON` - all extensions are included in the build.
#
#	- `KAA_PLATFORM` - build SDK for a particular target.
#
#	Values:
#
#	- `cc32xx`
#	- `esp8266`
#	- `posix`
#
#	Default: `posix`
#
#	- `KAA_UNITETESTS_COMPILE` - compile unit tests.
#
#	Values:
#
#	- `ON`
#	- `OFF`
#
#	Default: `OFF`
#
#	Note: requires [cmocka](https://cmocka.org/) test framework to be installed.

cmake_minimum_required(VERSION 2.8.12)

project(Kaa-c C)

include(ExternalProject)

option(WITH_EXTENSION_PROFILE "Enable profile extension" ON)
option(WITH_EXTENSION_CONFIGURATION "Enable configuration extension" ON)
option(WITH_EXTENSION_EVENT "Enable event extension" ON)
option(WITH_EXTENSION_LOGGING "Enable logging extension" ON)
option(WITH_EXTENSION_NOTIFICATION "Enable notification extension" ON)
option(WITH_EXTENSION_USER "Enable user extension" ON)
option(WITH_ENCRYPTION "Enable encryption" ON)
option(KAA_RUNTIME_KEY_GENERATION "Enable RSA key generation at runtime" OFF)

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
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99 -Wall -Wextra -pedantic -D_GNU_SOURCE")

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
set(KAA_THIRDPARTY_LIBRARIES mbedtls)

# Sets the default build platform to POSIX.
if (NOT DEFINED KAA_PLATFORM)
    set(KAA_PLATFORM "posix")
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
        ${KAA_SRC_FOLDER}/utilities/kaa_aes_rsa.c
        ${KAA_SRC_FOLDER}/utilities/kaa_log.c
        ${KAA_SRC_FOLDER}/utilities/kaa_mem.c
        ${KAA_SRC_FOLDER}/utilities/kaa_buffer.c
        ${KAA_SRC_FOLDER}/kaa_platform_utils.c
        ${KAA_SRC_FOLDER}/kaa_platform_protocol.c
        ${KAA_SRC_FOLDER}/kaa_channel_manager.c
        ${KAA_SRC_FOLDER}/kaa_common_schema.c
        ${KAA_SRC_FOLDER}/kaa_status.c
        ${KAA_SRC_FOLDER}/kaa.c
        ${KAA_SRC_FOLDER}/kaa_extension.c
        ${KAA_SRC_FOLDER}/platform-impl/common/kaa_htonll.c
)

if(WITH_ENCRYPTION)
    set(KAA_SOURCE_FILES
        ${KAA_SOURCE_FILES}
        ${KAA_SRC_FOLDER}/utilities/kaa_aes_rsa.c
        ${KAA_SRC_FOLDER}/platform-impl/common/encryption_utils.c)
endif(WITH_ENCRYPTION)


# Includes auto-generated Cmake's scripts.
include(${CMAKE_CURRENT_LIST_DIR}/listfiles/CMakeGen.cmake)

# Kaa include directories
set(KAA_INCLUDE_DIRS
        ${KAA_INCLUDE_PATHS}
        ${CMAKE_CURRENT_LIST_DIR}/src
        ${CMAKE_CURRENT_LIST_DIR}/thirdparty/mbedtls
        ${CMAKE_CURRENT_LIST_DIR}/tools/kaa_encryption/rsa_key_gen/include
        ${KAA_SRC_FOLDER}
        ${KAA_THIRDPARTY_INCLUDE_DIR})

# Build unit tests.
include(${CMAKE_CURRENT_LIST_DIR}/listfiles/UnitTest.cmake)

include_directories(SYSTEM ${KAA_INCLUDE_PATHS})

# This already includes mbedtls
add_subdirectory(tools/kaa_encryption/rsa_key_gen)

# Build Kaa libraries.
add_library(kaac ${KAA_SOURCE_FILES})
target_include_directories(kaac PUBLIC ${KAA_INCLUDE_DIRS})
target_link_libraries(kaac PRIVATE ${KAA_THIRDPARTY_LIBRARIES})

message("BOOTSTRAP ENABLED")
include(${CMAKE_CURRENT_LIST_DIR}/src/extensions/bootstrap/CMakeLists.txt)
target_link_libraries(kaac PUBLIC extension_bootstrap)

if(WITH_EXTENSION_PROFILE)
    message("PROFILE ENABLED")

    include(${CMAKE_CURRENT_LIST_DIR}/src/extensions/profile/CMakeLists.txt)
    target_link_libraries(kaac PUBLIC extension_profile)
else()
    add_definitions(-DKAA_DISABLE_FEATURE_PROFILE)
endif()

if(WITH_EXTENSION_USER)
    message("USER EXTENSION ENABLED")

    include(${CMAKE_CURRENT_LIST_DIR}/src/extensions/user/CMakeLists.txt)
    target_link_libraries(kaac PUBLIC extension_user)
else()
    add_definitions(-DKAA_DISABLE_FEATURE_USER)
endif()

if(WITH_EXTENSION_CONFIGURATION)
    message("CONFIGURATION ENABLED")

    include(${CMAKE_CURRENT_LIST_DIR}/src/extensions/configuration/CMakeLists.txt)
    target_link_libraries(kaac PUBLIC extension_configuration)
else()
    add_definitions(-DKAA_DISABLE_FEATURE_CONFIGURATION)
endif()

if(WITH_EXTENSION_EVENT)
    message("EVENTS ENABLED")

    include(${CMAKE_CURRENT_LIST_DIR}/src/extensions/event/CMakeLists.txt)
    target_link_libraries(kaac PUBLIC extension_event)
else()
    add_definitions(-DKAA_DISABLE_FEATURE_EVENTS)
endif()

if(WITH_EXTENSION_LOGGING)
    message("LOGGING ENABLED")

    include(${CMAKE_CURRENT_LIST_DIR}/src/extensions/logging/CMakeLists.txt)
    target_link_libraries(kaac PUBLIC extension_logging)
else()
    add_definitions(-DKAA_DISABLE_FEATURE_LOGGING)
endif()

if(WITH_EXTENSION_NOTIFICATION)
    message("NOTIFICATION ENABLED")

    include(${CMAKE_CURRENT_LIST_DIR}/src/extensions/notification/CMakeLists.txt)
    target_link_libraries(kaac PUBLIC extension_notification)
else()
    add_definitions(-DKAA_DISABLE_FEATURE_NOTIFICATION)
endif()

if(WITH_ENCRYPTION)
    message("ENCRYPTION ENABLED")
    target_link_libraries(kaac PUBLIC rsa_keygen)
    add_definitions(-DKAA_ENCRYPTION)
endif()

message("KAA WILL BE INSTALLED TO  ${CMAKE_INSTALL_PREFIX}")
install(DIRECTORY ${KAA_SRC_FOLDER}/ DESTINATION include/kaa
        FILES_MATCHING PATTERN *.h)

install(TARGETS kaac DESTINATION lib)

add_subdirectory(Modules/doxygen)

add_subdirectory(Modules/cppcheck)

#Set key pair generation parameter for posix platform
#Use application for another platform
if(KAA_RUNTIME_KEY_GENERATION)
    if(KAA_PLATFORM STREQUAL "posix")
        add_definitions(-DKAA_RUNTIME_KEY_GENERATION)
        target_link_libraries(kaac PUBLIC rsa_keygen)
    else()
        message(FATAL_ERROR "The runtime key generation option may be enabled only for POSIX platform")
    endif()
    target_link_libraries(kaac PUBLIC rsa_keygen)
else()
    add_dependencies(kaac kaa_rsa_keygen)
    externalproject_add(kaa_rsa_keygen SOURCE_DIR "${CMAKE_CURRENT_LIST_DIR}/tools/kaa_encryption"
                        INSTALL_COMMAND  "./generate_rsa_keys" COMMENT "Generating RSA headers"
                        CMAKE_ARGS -DKAA_SDK_DIR=${KAA_SDK_DIR}
                        BINARY_DIR ${CMAKE_CURRENT_BINARY_DIR}/kaa_keys)

    target_include_directories(kaac PUBLIC ${CMAKE_CURRENT_BINARY_DIR}/kaa_keys)
endif()

#
# Copyright 2014-2015 CyberVision, Inc.
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

project (Cpp-SDK-notification-sample)
enable_language(CXX)

# Path to CMake's scripts.
set(CMAKE_MODULE_PATH 
	 ${CMAKE_MODULE_PATH}
	 ${CMAKE_CURRENT_SOURCE_DIR}/libs/kaa/Modules
    )

if(MSVC)   
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS}")
else()
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -g -Wall -Wextra -I/usr/local/include")
endif()

if (CMAKE_COMPILER_IS_GNUCXX)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L/usr/lib/x86_64-linux-gnu -std=c++0x")
endif()

if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L/usr/local/lib -std=gnu++11 -stdlib=libc++")
endif()

find_package (Boost 1.55 REQUIRED COMPONENTS log system)
find_package (Avro REQUIRED)
find_package (Botan REQUIRED)

if (WIN32 AND NOT CYGWIN AND NOT MSYS)
add_definitions (/EHs)
add_definitions (
    -DBOOST_REGEX_DYN_LINK
    -DBOOST_FILESYSTEM_DYN_LINK
    -DBOOST_SYSTEM_DYN_LINK
    -DBOOST_PROGRAM_OPTIONS_DYN_LINK
    -DBOOST_LOG_DYN_LINK
    -DBOOST_ALL_NO_LIB)
endif()

include_directories(${Boost_INCLUDE_DIRS}
                    ${AVRO_INCLUDE_DIRS}
                    ${BOTAN_INCLUDE_DIR}
                    ${CMAKE_CURRENT_SOURCE_DIR}/src
                    ${CMAKE_CURRENT_SOURCE_DIR}/libs/kaa)

link_directories(${CMAKE_CURRENT_SOURCE_DIR}/build)

set (SAMPLE_SOURCE_FILES
            src/KaaDemo.cpp
)

add_executable(${APP_NAME} ${SAMPLE_SOURCE_FILES})

if (CMAKE_COMPILER_IS_GNUCXX)
    target_link_libraries(${APP_NAME} kaacpp_s ${BOTAN_LIBRARY} ${AVRO_LIBRARIES} ${Boost_LIBRARIES} pthread)
endif()

if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
    target_link_libraries(${APP_NAME} kaacpp_s ${BOTAN_LIBRARY} ${AVRO_LIBRARIES} ${Boost_LIBRARIES})
endif()

if (MSVC)
    target_link_libraries(${APP_NAME} kaacpp_s ${BOTAN_LIBRARY} ${AVRO_LIBRARIES} ${Boost_LIBRARIES} dbghelp)
endif()

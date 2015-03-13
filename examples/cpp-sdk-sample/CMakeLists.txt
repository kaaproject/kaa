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

project (Cpp-SDK-sample)
enable_language(CXX)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -g -Wall -Wextra -I/usr/local/include")

if (CMAKE_COMPILER_IS_GNUCXX)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L/usr/lib/x86_64-linux-gnu -std=c++0x")
endif()

if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L/usr/local/lib -std=gnu++11 -stdlib=libc++")
endif()

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src
                    ${CMAKE_CURRENT_SOURCE_DIR}/libs/kaa
                   )

link_directories(${CMAKE_CURRENT_SOURCE_DIR}/build)

set (SAMPLE_SOURCE_FILES
            src/KaaDemo.cpp
)

add_executable(${APP_NAME} ${SAMPLE_SOURCE_FILES})

if (CMAKE_COMPILER_IS_GNUCXX)
    target_link_libraries(${APP_NAME} kaacpp_s botan-1.10 boost_log boost_system avrocpp pthread)
endif()

if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
    target_link_libraries(${APP_NAME} kaacpp_s botan-1.10 boost_log-mt boost_system-mt avrocpp)
endif()

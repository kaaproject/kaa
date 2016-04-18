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

project (kaatcp-c-test)
enable_language(C)

if (CMAKE_COMPILER_IS_GNUCC)
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -g -Wall -std=c99")
endif ()

include_directories (${CMAKE_CURRENT_SOURCE_DIR}/../src
                     ${CMAKE_CURRENT_SOURCE_DIR}/src)

SET(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC")

set (KAA_TEST_SOURCES
                       ../src/kaatcp_request.c
                       ../src/kaatcp_parser.c
                       src/kaatcp_test.c
                       src/kaatcp_request_test.c
                       src/kaatcp_parser_test.c
)

add_executable ( kaatcp-test  ${KAA_TEST_SOURCES})

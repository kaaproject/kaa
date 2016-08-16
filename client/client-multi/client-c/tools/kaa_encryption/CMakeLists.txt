#
#  Copyright 2014-2016 CyberVision, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

cmake_minimum_required(VERSION 2.8.12)
project(kaa_rsa_keygen_app C)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC -std=c99 -Wall -Wextra -pedantic -D_GNU_SOURCE")

include_directories(rsa_key_gen)

add_subdirectory(${CMAKE_CURRENT_LIST_DIR}/rsa_key_gen)

add_executable(generate_rsa_keys kaa_rsa_key_gen_app.c)

target_link_libraries(generate_rsa_keys rsa_keygen)

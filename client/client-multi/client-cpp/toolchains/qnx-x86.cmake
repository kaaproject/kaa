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

set(CMAKE_SYSTEM_NAME QNX)
set(CMAKE_SYSTEM_VERSION 6.5.0)

set(CMAKE_C_COMPILER ${QNX_HOST}/usr/bin/i486-pc-nto-qnx${CMAKE_SYSTEM_VERSION}-gcc)
set(CMAKE_CXX_COMPILER ${QNX_HOST}/usr/bin/i486-pc-nto-qnx${CMAKE_SYSTEM_VERSION}-g++)

set(CMAKE_LIBRARY_ARCHITECTURE x86)

set(CMAKE_FIND_ROOT_PATH
        ${CMAKE_FIND_ROOT_PATH}
        ${QNX_TARGET}
        ${QNX_TARGET}/${CMAKE_LIBRARY_ARCHITECTURE})

set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE ONLY)

add_definitions(-DQNX_650_CPP11_TO_STRING_PATCH)

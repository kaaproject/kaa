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

find_package(OpenSSL REQUIRED)

if(CMAKE_COMPILER_IS_GNUCC)
    if(KAA_DEBUG_ENABLED)
        set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -O0 -ggdb")
    else()
        set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -Os -s")
    endif()

    if(KAA_COLLECT_COVERAGE AND KAA_DEBUG_ENABLED)
        set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} --coverage")
    endif()
endif()

set(KAA_SOURCE_FILES 
        ${KAA_SOURCE_FILES}
        src/platform-impl/posix/logger.c
        src/platform-impl/posix/sha.c
        src/platform-impl/ext_log_storage_memory.c
        src/platform-impl/ext_log_upload_strategy_by_volume.c
    )

set(KAA_THIRDPARTY_LIBRARIES
        ${KAA_THIRDPARTY_LIBRARIES} 
        ${OPENSSL_LIBRARIES}
    )
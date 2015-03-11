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

set(KAA_SOURCE_FILES 
        ${KAA_SOURCE_FILES}
        src/kaa/platform-impl/posix/sha.c
        src/kaa/platform-impl/posix/logger.c
        src/kaa/platform-impl/posix/posix_file_utils.c
        src/kaa/platform-impl/posix/posix_key_utils.c
        src/kaa/platform-impl/posix/posix_status.c
        src/kaa/platform-impl/posix/posix_configuration_persistence.c
        src/kaa/platform-impl/ext_log_storage_memory.c
        src/kaa/platform-impl/ext_log_upload_strategy_by_volume.c
    )

if(NOT KAA_WITHOUT_TCP_CHANNEL)
    set(KAA_SOURCE_FILES 
            ${KAA_SOURCE_FILES}
            src/kaa/kaa_protocols/kaa_tcp/kaatcp_parser.c
            src/kaa/kaa_protocols/kaa_tcp/kaatcp_request.c
            src/kaa/platform-impl/posix/posix_tcp_utils.c
            src/kaa/platform-impl/kaa_tcp_channel.c
        )
endif()

set(KAA_THIRDPARTY_LIBRARIES
        ${KAA_THIRDPARTY_LIBRARIES} 
        ${OPENSSL_LIBRARIES}
    )

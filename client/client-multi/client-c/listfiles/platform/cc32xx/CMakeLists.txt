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

set(KAA_SOURCE_FILES
        ${KAA_SOURCE_FILES}
        ${KAA_SRC_FOLDER}/platform-impl/posix/kaa_client.c
        ${KAA_SRC_FOLDER}/platform-impl/common/kaa_failover_strategy.c
        ${KAA_SRC_FOLDER}/platform-impl/common/sha.c
        ${KAA_SRC_FOLDER}/platform-impl/cc32xx/logger.c
        ${KAA_SRC_FOLDER}/platform-impl/cc32xx/file_utils.c
        ${KAA_SRC_FOLDER}/platform-impl/cc32xx/status.c
        ${KAA_SRC_FOLDER}/platform-impl/cc32xx/configuration_persistence.c
        ${KAA_SRC_FOLDER}/platform-impl/cc32xx/time.c
        ${KAA_SRC_FOLDER}/platform-impl/cc32xx/reboot.c
        ${KAA_SRC_FOLDER}/platform-impl/common/key_utils.c
        ${KAA_SRC_FOLDER}/platform-impl/common/ext_log_storage_memory.c
        ${KAA_SRC_FOLDER}/platform-impl/common/ext_log_upload_strategies.c
        )

if(NOT KAA_WITHOUT_TCP_CHANNEL)
    set(KAA_SOURCE_FILES
            ${KAA_SOURCE_FILES}
            ${KAA_SRC_FOLDER}/kaa_protocols/kaa_tcp/kaatcp_parser.c
            ${KAA_SRC_FOLDER}/kaa_protocols/kaa_tcp/kaatcp_request.c
            ${KAA_SRC_FOLDER}/platform-impl/cc32xx/tcp_utils.c
            ${KAA_SRC_FOLDER}/platform-impl/common/kaa_tcp_channel.c
            )
endif()

set(KAA_THIRDPARTY_LIBRARIES
        ${KAA_THIRDPARTY_LIBRARIES}
        )


set(CC32XX_INCDIRS ${CC32XX_SDK}/inc
        ${CC32XX_SDK}/driverlib
        ${CC32XX_SDK}/oslib
        ${CC32XX_SDK}/simplelink
        ${CC32XX_SDK}/simplelink/include
        ${CC32XX_SDK}/simplelink/source
        ${CC32XX_SDK}/example/common
        )

    set(KAA_INCLUDE_PATHS ${KAA_INCLUDE_PATHS} ${CC32XX_INCDIRS} ${KAA_SRC_FOLDER}/platform-impl/cc32xx)


add_library(driver STATIC IMPORTED)
set_property(TARGET driver PROPERTY IMPORTED_LOCATION ${CC32XX_SDK}/driverlib/gcc/exe/libdriver.a)

add_library(simplelink_nonos STATIC IMPORTED)
set_property(TARGET simplelink_nonos PROPERTY IMPORTED_LOCATION ${CC32XX_SDK}/simplelink/gcc/exe/libsimplelink_nonos.a)

add_definitions(-DCC32XX_PLATFORM)

set(KAA_THIRDPARTY_LIBRARIES ${KAA_THIRDPARTY_LIBRARIES} driver simplelink_nonos)

set(KAA_BUILD_STATIC_ONLY 1)

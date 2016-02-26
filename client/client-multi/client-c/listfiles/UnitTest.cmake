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

enable_testing()

find_package(CUnit)
find_package(OpenSSL REQUIRED)

set(CUNIT_LIB_NAME "")

if(CUNIT_FOUND)
    set(CUNIT_LIB_NAME ${CUNIT_LIBRARIES})
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DKAA_TEST_CUNIT_ENABLED")
endif()

configure_file("${CMAKE_CURRENT_SOURCE_DIR}/sonar-project.properties.in"
               "${CMAKE_CURRENT_SOURCE_DIR}/sonar-project.properties"
              )

add_executable  (test_ext_log_storage_memory
                    test/platform-impl/test_ext_log_storage_memory.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_ext_log_storage_memory kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_ext_log_upload_strategy_by_volume
                    test/platform-impl/test_ext_log_upload_strategies.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_ext_log_upload_strategy_by_volume kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_user_extension
                    test/test_kaa_user.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_user_extension kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_meta_extension
                    test/test_meta_extension.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_meta_extension kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_platform_utils
                    test/test_platform_utils.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_platform_utils kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_context
                    test/test_kaa_context.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_context kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_event
                    test/test_kaa_event.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_event kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_status
                    test/test_kaa_status.c
                )
target_link_libraries(test_status kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

#add_executable  (test_bootstrap_manager
#                    test/test_kaa_bootstrap_manager.c
#                    test/kaa_test_external.c
#                )
#target_link_libraries(test_bootstrap_manager kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_profile
                    ${KAA_SRC_FOLDER}/gen/kaa_profile_gen.c
                    test/test_kaa_profile.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_profile kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_common
                    test/test_kaa_common.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_common kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_log
                    test/test_kaa_log.c
                    test/kaa_test_external.c
                    ${KAA_SRC_FOLDER}/gen/kaa_logging_gen.c
                    ${KAA_SRC_FOLDER}/avro_src/io.c
                    ${KAA_SRC_FOLDER}/avro_src/encoding_binary.c
                    ${KAA_SRC_FOLDER}/collections/kaa_list.c
                    ${KAA_SRC_FOLDER}/utilities/kaa_log.c
                    ${KAA_SRC_FOLDER}/platform-impl/posix/logger.c
                    ${KAA_SRC_FOLDER}/kaa_platform_utils.c
                    ${KAA_SRC_FOLDER}/kaa_bootstrap_manager.c
                    ${KAA_SRC_FOLDER}/kaa_channel_manager.c
                    ${KAA_SRC_FOLDER}/kaa_common_schema.c
                    ${KAA_SRC_FOLDER}/kaa_logging.c
                    ${KAA_SRC_FOLDER}/kaa_status.c
                    ${KAA_SRC_FOLDER}/platform-impl/common/kaa_failover_strategy.c
                )
target_link_libraries(test_log ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_platform_protocol
                    test/test_platform_protocol.c
                )
target_link_libraries(test_platform_protocol kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_list
                    test/collections/test_kaa_list.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_list kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

#add_executable  (test_channel_manager
#                    test/test_kaa_channel_manager.c
#                    test/kaa_test_external.c
#                )
#target_link_libraries(test_channel_manager kaac ${OPENSSL_LIBRARIES} ${CUNIT_LIB_NAME})

add_executable  (test_kaatcp_parser
                    test/kaatcp/kaatcp_parser_test.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_kaatcp_parser kaac ${CUNIT_LIB_NAME})

add_executable  (test_kaatcp_request
                    test/kaatcp/kaatcp_request_test.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_kaatcp_request kaac ${CUNIT_LIB_NAME})

add_executable  (test_kaa_tcp_channel_bootstrap
                    test/kaa_tcp_channel/test_kaa_tcp_channel_bootstrap.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_kaa_tcp_channel_bootstrap kaac ${CUNIT_LIB_NAME})

#add_executable  (test_kaa_tcp_channel_operation
#                    test/kaa_tcp_channel/test_kaa_tcp_channel_operation.c
#                    test/kaa_test_external.c
#                )
#target_link_libraries(test_kaa_tcp_channel_operation kaac ${CUNIT_LIB_NAME})

add_executable  (test_kaa_configuration_manager
                    test/test_kaa_configuration.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_kaa_configuration_manager kaac ${CUNIT_LIB_NAME})

add_executable  (test_kaa_common_schema
                    test/test_kaa_common_schema.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_kaa_common_schema kaac ${CUNIT_LIB_NAME})

add_executable  (test_kaa_notification_manager
                    test/test_kaa_notification.c
                )
target_link_libraries(test_kaa_notification_manager kaac ${CUNIT_LIB_NAME})

add_executable  (test_kaa_reallocation
                    test/utilities/test_kaa_reallocation.c
                )
target_link_libraries(test_kaa_reallocation kaac ${CUNIT_LIB_NAME})

/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <string.h>
#include <stddef.h>
#include <stdlib.h>
#include <time.h>

#include "kaa_test.h"

#include "kaa_bootstrap_manager.h"
#include "kaa_channel_manager.h"
#include "utilities/kaa_log.h"
#include "utilities/kaa_mem.h"
#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"



extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p
                                            , kaa_logger_t *logger);

extern void kaa_channel_manager_destroy(kaa_channel_manager_t *self);

extern kaa_error_t kaa_bootstrap_manager_create(kaa_bootstrap_manager_t **bootstrap_manager_p
                                              , kaa_channel_manager_t *channel_manager
                                              , kaa_logger_t *logger);

extern void kaa_bootstrap_manager_destroy(kaa_bootstrap_manager_t *self);

extern kaa_error_t kaa_bootstrap_manager_handle_server_sync(kaa_bootstrap_manager_t *self
                                                          , kaa_platform_message_reader_t *reader
                                                          , uint32_t extension_options
                                                          , size_t extension_length);



static kaa_logger_t *logger = NULL;
static kaa_channel_manager_t *channel_manager = NULL;



void test_create_bootstrap_manager()
{
    KAA_TRACE_IN(logger);

    kaa_error_t error_code;
    kaa_bootstrap_manager_t* manager = NULL;

    error_code = kaa_bootstrap_manager_create(NULL, channel_manager, logger);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_bootstrap_manager_create(&manager, NULL, logger);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_bootstrap_manager_create(&manager, channel_manager, NULL);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_bootstrap_manager_create(&manager, channel_manager, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);


    kaa_bootstrap_manager_destroy(manager);
}

static kaa_access_point_t *create_access_point()
{
    const uint16_t MAX_CONNECTION_DATA_SIZE = 16;

    kaa_access_point_t *access_point = (kaa_access_point_t *)KAA_MALLOC(sizeof(kaa_access_point_t));
    ASSERT_NOT_NULL(access_point);

    access_point->id = rand();
    access_point->connection_data_len = 1 + rand() % MAX_CONNECTION_DATA_SIZE;
    access_point->connection_data = (char *)KAA_MALLOC(access_point->connection_data_len);

    ASSERT_NOT_NULL(access_point->connection_data);

    return access_point;
}

void test_handle_server_sync()
{
    KAA_TRACE_IN(logger);

    kaa_error_t error_code;
    kaa_bootstrap_manager_t *manager = NULL;

    error_code = kaa_bootstrap_manager_create(&manager, channel_manager, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_transport_protocol_id_t protocol1 = { 1, 1 };
    kaa_transport_protocol_id_t protocol2 = { 2, 1 };

    uint16_t server_sync_payload_size = 0;

    size_t access_point_count = 0;
    kaa_access_point_t *access_point1_protocol1 = create_access_point();
    ASSERT_NOT_NULL(access_point1_protocol1);
    ++access_point_count;
    server_sync_payload_size += kaa_aligned_size_get(access_point1_protocol1->connection_data_len);

    kaa_access_point_t *access_point2_protocol1 = create_access_point();
    ASSERT_NOT_NULL(access_point2_protocol1);
    ++access_point_count;
    server_sync_payload_size += kaa_aligned_size_get(access_point2_protocol1->connection_data_len);

    kaa_access_point_t *access_point1_protocol2 = create_access_point();
    ASSERT_NOT_NULL(access_point1_protocol2);
    ++access_point_count;
    server_sync_payload_size += kaa_aligned_size_get(access_point1_protocol2->connection_data_len);

    server_sync_payload_size += sizeof(uint16_t)
                              + sizeof(uint16_t)
                              + access_point_count * (sizeof(uint32_t)
                                                    + sizeof(uint32_t)
                                                    + sizeof(uint16_t)
                                                    + sizeof(uint16_t));

    char server_sync_buffer[server_sync_payload_size];
    kaa_platform_message_writer_t *writer;
    error_code = kaa_platform_message_writer_create(&writer, server_sync_buffer, server_sync_payload_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint16_t network_order_16;
    uint32_t network_order_32;

    // request id
    error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    network_order_16 = KAA_HTONS(access_point_count);
    error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_access_point_t *access_points[] = { access_point1_protocol1
                                          , access_point2_protocol1
                                          , access_point1_protocol2 };

    kaa_transport_protocol_id_t *protoco_infos[] = { &protocol1
                                                     , &protocol1
                                                     , &protocol2 };

    for (uint16_t i = 0; i < access_point_count; ++i) {
        network_order_32 = KAA_HTONL(access_points[i]->id);
        error_code = kaa_platform_message_write(writer, &network_order_32, sizeof(uint32_t));
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);

        network_order_32 = KAA_HTONL(protoco_infos[i]->id);
        error_code = kaa_platform_message_write(writer, &network_order_32, sizeof(uint32_t));
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);

        network_order_16 = KAA_HTONS(protoco_infos[i]->version);
        error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);

        network_order_16 = KAA_HTONS(access_points[i]->connection_data_len);
        error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);

        error_code = kaa_platform_message_write_aligned(writer
                                                      , access_points[i]->connection_data
                                                      , access_points[i]->connection_data_len);
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    }

    kaa_platform_message_reader_t *reader;
    error_code = kaa_platform_message_reader_create(&reader, server_sync_buffer, server_sync_payload_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_bootstrap_manager_handle_server_sync(manager, reader, 0, server_sync_payload_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    /**
     * TEST
     */
    kaa_access_point_t *test_access_point;
    kaa_transport_protocol_id_t unknown_protocol = { 456, 456 };
    test_access_point = kaa_bootstrap_manager_get_current_access_point(manager, &unknown_protocol);
    ASSERT_NULL(test_access_point);

    test_access_point = kaa_bootstrap_manager_get_current_access_point(manager, &protocol2);
    ASSERT_NOT_NULL(test_access_point);
    ASSERT_EQUAL(test_access_point->id, access_point1_protocol2->id);

    test_access_point = kaa_bootstrap_manager_get_current_access_point(manager, &protocol1);
    ASSERT_NOT_NULL(test_access_point);
    ASSERT_EQUAL(test_access_point->id, access_point2_protocol1->id);

    test_access_point = kaa_bootstrap_manager_get_next_access_point(manager, &protocol1);
    ASSERT_NOT_NULL(test_access_point);
    ASSERT_EQUAL(test_access_point->id, access_point1_protocol1->id);

    test_access_point = kaa_bootstrap_manager_get_next_access_point(manager, &protocol1);
    ASSERT_NULL(test_access_point);

    kaa_platform_message_reader_destroy(reader);
    kaa_platform_message_writer_destroy(writer);
    kaa_bootstrap_manager_destroy(manager);
}

int test_init(void)
{
    srand(time(NULL));

    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger)
        return error;

    error = kaa_channel_manager_create(&channel_manager, logger);
    if (error || !logger)
        return error;

    return 0;
}

int test_deinit(void)
{
    kaa_channel_manager_destroy(channel_manager);
    kaa_log_destroy(logger);

    return 0;
}



KAA_SUITE_MAIN(Bootstrap, test_init, test_deinit,
        KAA_TEST_CASE(create_bootstrap_manager, test_create_bootstrap_manager)
        KAA_TEST_CASE(handle_server_sync, test_handle_server_sync)
)


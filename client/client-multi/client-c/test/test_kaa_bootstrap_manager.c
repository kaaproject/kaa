/*
 * Copyright 2014-2016 CyberVision, Inc.
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
#include <stdbool.h>
#include <stdio.h>
#include <stdint.h>

#include "kaa_test.h"

#include "kaa_context.h"
#include "kaa_defaults.h"
#include "kaa_bootstrap_manager.h"
#include "kaa_channel_manager.h"
#include "utilities/kaa_log.h"
#include "utilities/kaa_mem.h"
#include "kaa_platform_common.h"
#include "kaa_platform_protocol.h"
#include "kaa_platform_utils.h"
#include "platform/ext_transport_channel.h"
#include "platform/sock.h"

#include "kaa_private.h"

typedef struct {
    kaa_transport_protocol_id_t protocol_info;
    kaa_access_point_t *access_point;
    kaa_extension_id* services;
    size_t         services_count;
    kaa_transport_context_t transport_context;
} test_channel_context_t;



static kaa_context_t kaa_context;
static kaa_logger_t *logger = NULL;
static kaa_channel_manager_t *channel_manager = NULL;

static kaa_extension_id SUPPORTED_SERVICES[] = { KAA_EXTENSION_PROFILE
                                            , KAA_EXTENSION_USER
                                            , KAA_EXTENSION_EVENT
                                            , KAA_EXTENSION_LOGGING };

static size_t SUPPORTED_SERVICES_COUNT = sizeof(SUPPORTED_SERVICES) / sizeof(kaa_extension_id);



static kaa_error_t test_init_channel(void *channel_context
                                   , kaa_transport_context_t *transport_context)
{
    KAA_RETURN_IF_NIL2(channel_context, transport_context, KAA_ERR_BADPARAM);

    ((test_channel_context_t *)channel_context)->transport_context = *transport_context;
    return KAA_ERR_NONE;
}

static kaa_error_t test_set_access_point(void *context
                                       , kaa_access_point_t *access_point)
{
    KAA_RETURN_IF_NIL2(context, access_point, KAA_ERR_BADPARAM);
    test_channel_context_t *channel_context = (test_channel_context_t *)context;
    channel_context->access_point = access_point;

    return KAA_ERR_NONE;
}

static kaa_error_t test_get_protocol_info(void *context, kaa_transport_protocol_id_t *protocol_info)
{
    KAA_RETURN_IF_NIL2(context, protocol_info, KAA_ERR_BADPARAM);

    test_channel_context_t *channel_context = (test_channel_context_t *)context;
    *protocol_info = channel_context->protocol_info;

    return KAA_ERR_NONE;
}

static kaa_error_t test_get_supported_services(void *context
                                             , kaa_extension_id **supported_services
                                             , size_t *service_count)
{
    KAA_RETURN_IF_NIL3(context, supported_services, service_count, KAA_ERR_BADPARAM);

    test_channel_context_t *channel_context = (test_channel_context_t *)context;
    *supported_services = channel_context->services;
    *service_count = channel_context->services_count;

    return KAA_ERR_NONE;
}

static kaa_error_t test_sync_handler(void *context
                                   , const kaa_extension_id services[]
                                   , size_t service_count)
{
    KAA_RETURN_IF_NIL3(context, services, service_count, KAA_ERR_BADPARAM);
    return KAA_ERR_NONE;
}

static void test_create_channel_interface(kaa_transport_channel_interface_t *channel
                                        , test_channel_context_t *context)
{
    channel->context = context;
    channel->get_protocol_id = &test_get_protocol_info;
    channel->get_supported_services = &test_get_supported_services;
    channel->sync_handler = &test_sync_handler;
    channel->destroy = NULL;
    channel->init = &test_init_channel;
    channel->set_access_point = &test_set_access_point;
}



void test_create_bootstrap_manager(void **state)
{
    (void)state;

    kaa_error_t error_code;
    kaa_bootstrap_manager_t* manager = NULL;

    error_code = kaa_bootstrap_manager_create(NULL, &kaa_context);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_bootstrap_manager_create(&manager, NULL);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_bootstrap_manager_create(&manager, &kaa_context);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_bootstrap_manager_destroy(manager);
}

static kaa_access_point_t *create_access_point(void)
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

static void destroy_access_point(kaa_access_point_t * access_point)
{
    if (access_point) {
        if (access_point->connection_data) {
            KAA_FREE(access_point->connection_data);
        }
        KAA_FREE(access_point);
    }
}

void test_handle_server_sync(void **state)
{
    (void)state;

    kaa_error_t error_code;
    kaa_bootstrap_manager_t *bootstrap_manager = NULL;


    error_code = kaa_bootstrap_manager_create(&bootstrap_manager, &kaa_context);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_context.bootstrap_manager = bootstrap_manager;
    /**
     * TEST DATA
     */
    kaa_transport_protocol_id_t protocol1 = { 1, 1 };
    kaa_transport_protocol_id_t protocol2 = { 2, 1 };
    kaa_transport_protocol_id_t protocol3 = { 465, 564 };

    test_channel_context_t protocol1_channel_context = { protocol1, NULL, SUPPORTED_SERVICES,
                                                         SUPPORTED_SERVICES_COUNT, {NULL} };
    kaa_transport_channel_interface_t protocol1_channel;
    test_create_channel_interface(&protocol1_channel, &protocol1_channel_context);

    test_channel_context_t protocol2_channel_context = { protocol2, NULL, SUPPORTED_SERVICES,
                                                         SUPPORTED_SERVICES_COUNT, {NULL} };
    kaa_transport_channel_interface_t protocol2_channel;
    test_create_channel_interface(&protocol2_channel, &protocol2_channel_context);

    test_channel_context_t protocol3_channel_context = { protocol3, NULL, SUPPORTED_SERVICES,
                                                         SUPPORTED_SERVICES_COUNT, {NULL} };
    kaa_transport_channel_interface_t protocol3_channel;
    test_create_channel_interface(&protocol3_channel, &protocol3_channel_context);

    uint32_t protocol1_channel_id;
    error_code = kaa_channel_manager_add_transport_channel(channel_manager
                                                         , &protocol1_channel
                                                         , &protocol1_channel_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint32_t protocol3_channel_id;
    error_code = kaa_channel_manager_add_transport_channel(channel_manager
                                                         , &protocol3_channel
                                                         , &protocol3_channel_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

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

    /**
     * SERIALIZE TEST DATA
     */
    char server_sync_buffer[server_sync_payload_size];
    kaa_platform_message_writer_t *writer;
    error_code = kaa_platform_message_writer_create(&writer, server_sync_buffer, server_sync_payload_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint16_t network_order_16 = 0xAEF5;
    uint32_t network_order_32 = 0xEAFC5370;

    // request id
    error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    network_order_16 = KAA_HTONS(access_point_count);
    error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    /**
     * DO NOT CHANGE ELEMENT ORDER IN NEXT TWO ARRAYS
     */
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

    kaa_platform_message_reader_t *reader = NULL;
    error_code = kaa_platform_message_reader_create(&reader, server_sync_buffer, server_sync_payload_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    /**
     * TEST
     */
    ASSERT_NULL(protocol1_channel_context.access_point);
    ASSERT_NULL(protocol2_channel_context.access_point);
    ASSERT_NULL(protocol3_channel_context.access_point);

    error_code = kaa_bootstrap_manager_handle_server_sync(bootstrap_manager, reader, 0, server_sync_payload_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_NOT_NULL(protocol1_channel_context.access_point);
    ASSERT_EQUAL(protocol1_channel_context.access_point->id, access_point2_protocol1->id);

    ASSERT_NULL(protocol3_channel_context.access_point);

    uint32_t protocol2_channel_id;
    error_code = kaa_channel_manager_add_transport_channel(channel_manager
                                                         , &protocol2_channel
                                                         , &protocol2_channel_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(protocol2_channel_context.access_point);
    ASSERT_EQUAL(protocol2_channel_context.access_point->id, access_point1_protocol2->id);

    ASSERT_NOT_NULL(protocol1_channel_context.transport_context.kaa_context);
    ASSERT_NOT_NULL(protocol1_channel_context.transport_context.kaa_context->bootstrap_manager);
    error_code = kaa_bootstrap_manager_on_access_point_failed(protocol1_channel_context.transport_context.kaa_context->bootstrap_manager
                                                            , &protocol1_channel_context.protocol_info
                                                            , KAA_SERVER_OPERATIONS);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(protocol1_channel_context.access_point);
    ASSERT_EQUAL(protocol1_channel_context.access_point->id, access_point1_protocol1->id);

    error_code = kaa_bootstrap_manager_on_access_point_failed(protocol1_channel_context.transport_context.kaa_context->bootstrap_manager
                                                            , &protocol1_channel_context.protocol_info
                                                            , KAA_SERVER_OPERATIONS);
    ASSERT_EQUAL(error_code, KAA_ERR_NOT_FOUND);

    /**
     * CLEAN UP
     */
    destroy_access_point(access_point1_protocol1);
    destroy_access_point(access_point1_protocol2);
    destroy_access_point(access_point2_protocol1);
    kaa_channel_manager_remove_transport_channel(channel_manager, protocol1_channel_id);
    kaa_channel_manager_remove_transport_channel(channel_manager, protocol2_channel_id);
    kaa_channel_manager_remove_transport_channel(channel_manager, protocol3_channel_id);
    kaa_platform_message_reader_destroy(reader);
    kaa_platform_message_writer_destroy(writer);
    kaa_bootstrap_manager_destroy(bootstrap_manager);
    kaa_context.bootstrap_manager = NULL;
}



#if KAA_BOOTSTRAP_ACCESS_POINT_COUNT > 0

static kaa_error_t find_bootstrap_access_point_index(kaa_transport_protocol_id_t *id
                                                   , size_t index_from
                                                   , size_t *access_point_index)
{
    KAA_RETURN_IF_NIL2(id, access_point_index, KAA_ERR_BADDATA);

    if (index_from < KAA_BOOTSTRAP_ACCESS_POINT_COUNT) {
        for (size_t i = index_from; i < KAA_BOOTSTRAP_ACCESS_POINT_COUNT; ++i) {
            if (0 == memcmp(id, &(KAA_BOOTSTRAP_ACCESS_POINTS[i].protocol_id), sizeof(kaa_transport_protocol_id_t))) {
                *access_point_index = i;
                return KAA_ERR_NONE;
            }
        }
    }

    return KAA_ERR_NOT_FOUND;
}

void test_bootstrap_channel(void **state)
{
    (void)state;

    kaa_error_t error_code;
    kaa_bootstrap_manager_t *bootstrap_manager = NULL;


    error_code = kaa_bootstrap_manager_create(&bootstrap_manager, &kaa_context);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_context.bootstrap_manager = bootstrap_manager;

     /**
      * TEST DATA
      */
    size_t index = rand() % KAA_BOOTSTRAP_ACCESS_POINT_COUNT;
    kaa_extension_id bootstrap_service[] = { KAA_EXTENSION_BOOTSTRAP };

    test_channel_context_t bootstrap_channel_context = { KAA_BOOTSTRAP_ACCESS_POINTS[index].protocol_id
                                                       , NULL
                                                       , bootstrap_service
                                                       , 1
                                                       , {NULL} };
    kaa_transport_channel_interface_t bootstrap_channel;
    test_create_channel_interface(&bootstrap_channel, &bootstrap_channel_context);

    uint32_t bootstrap_channel_id;
    error_code = kaa_channel_manager_add_transport_channel(channel_manager
                                                         , &bootstrap_channel
                                                         , &bootstrap_channel_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t expected_index;
    error_code = find_bootstrap_access_point_index((kaa_transport_protocol_id_t *)&(KAA_BOOTSTRAP_ACCESS_POINTS[index].protocol_id)
                                                 , 0
                                                 , &expected_index);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_NOT_NULL(bootstrap_channel_context.access_point);
    ASSERT_EQUAL(bootstrap_channel_context.access_point->connection_data_len
               , KAA_BOOTSTRAP_ACCESS_POINTS[expected_index].access_point.connection_data_len);
    ASSERT_EQUAL(0, memcmp(bootstrap_channel_context.access_point->connection_data
                         , KAA_BOOTSTRAP_ACCESS_POINTS[expected_index].access_point.connection_data
                         , bootstrap_channel_context.access_point->connection_data_len));


#if KAA_BOOTSTRAP_ACCESS_POINT_COUNT > 1
    error_code = find_bootstrap_access_point_index((kaa_transport_protocol_id_t *)&(KAA_BOOTSTRAP_ACCESS_POINTS[index].protocol_id)
                                                 , expected_index + 1
                                                 , &expected_index);

    if (!error_code) {
        error_code = kaa_bootstrap_manager_on_access_point_failed(bootstrap_manager
                                                                , (kaa_transport_protocol_id_t *)&(KAA_BOOTSTRAP_ACCESS_POINTS[index].protocol_id)
                                                                , KAA_SERVER_BOOTSTRAP);
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);

        ASSERT_NOT_NULL(bootstrap_channel_context.access_point);
        ASSERT_EQUAL(bootstrap_channel_context.access_point->connection_data_len
                   , KAA_BOOTSTRAP_ACCESS_POINTS[expected_index].access_point.connection_data_len);
        ASSERT_EQUAL(0, memcmp(bootstrap_channel_context.access_point->connection_data
                             , KAA_BOOTSTRAP_ACCESS_POINTS[expected_index].access_point.connection_data
                             , bootstrap_channel_context.access_point->connection_data_len));
    }
#endif

    error_code = kaa_bootstrap_manager_on_access_point_failed(bootstrap_manager
                                                            , (kaa_transport_protocol_id_t *)&(KAA_BOOTSTRAP_ACCESS_POINTS[index].protocol_id)
                                                            , KAA_SERVER_BOOTSTRAP);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_bootstrap_manager_destroy(bootstrap_manager);
    kaa_context.bootstrap_manager = NULL;
}
#endif

int test_init(void)
{
    srand(time(NULL));

    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger)
        return error;

    kaa_context.logger = logger;
    kaa_context.platform_protocol = NULL;
    kaa_context.bootstrap_manager = NULL;

    error = kaa_channel_manager_create(&channel_manager, &kaa_context);
    if (error || !channel_manager)
        return error;

    return 0;
}

int test_deinit(void)
{
    kaa_channel_manager_destroy(channel_manager);
    kaa_log_destroy(logger);

    return 0;
}


#if KAA_BOOTSTRAP_ACCESS_POINT > 0
KAA_SUITE_MAIN(Bootstrap, test_init, test_deinit,
        KAA_TEST_CASE(create_bootstrap_manager, test_create_bootstrap_manager)
        KAA_TEST_CASE(handle_server_sync, test_handle_server_sync)
        KAA_TEST_CASE(test_bootstrap_channel, test_bootstrap_channel)
)
#else
KAA_SUITE_MAIN(Bootstrap, test_init, test_deinit,
        KAA_TEST_CASE(create_bootstrap_manager, test_create_bootstrap_manager)
        KAA_TEST_CASE(handle_server_sync, test_handle_server_sync)
)
#endif


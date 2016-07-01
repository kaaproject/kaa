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


#include <stdint.h>
#include <stddef.h>
#include <stdlib.h>
#include <time.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>

#include "kaa_test.h"

#include "kaa_common.h"
#include "kaa_bootstrap_manager.h"
#include "kaa_channel_manager.h"
#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"
#include "utilities/kaa_log.h"
#include "utilities/kaa_mem.h"
#include "platform/ext_transport_channel.h"
#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"
#include "platform/sock.h"

#include "kaa_private.h"

typedef struct {
    kaa_transport_protocol_id_t protocol_info;
    kaa_access_point_t *access_point;
    kaa_extension_id* services;
    size_t         services_count;
} test_channel_context_t;



static kaa_context_t kaa_context;
static kaa_logger_t *logger = NULL;

static kaa_extension_id SUPPORTED_SERVICES[] = { KAA_EXTENSION_BOOTSTRAP
                                            , KAA_EXTENSION_PROFILE
                                            , KAA_EXTENSION_USER
                                            , KAA_EXTENSION_EVENT
                                            , KAA_EXTENSION_LOGGING };
static const size_t supported_services_count = sizeof(SUPPORTED_SERVICES) / sizeof(kaa_extension_id);



static kaa_error_t test_init_channel(void *channel_context
                             , kaa_transport_context_t *transport_context)
{
    (void)channel_context;
    (void)transport_context;

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
                                             , const kaa_extension_id **supported_services
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
    channel->set_access_point =&test_set_access_point;
}

static void compare_channels(kaa_transport_channel_interface_t *actual_channel
                           , kaa_transport_channel_interface_t *expected_channel)
{
    KAA_RETURN_IF_NIL2(actual_channel, expected_channel,);

    kaa_error_t error_code;
    kaa_transport_protocol_id_t expected_info = { 0, 0 };
    kaa_transport_protocol_id_t actual_info = { 1, 1 };

    error_code = expected_channel->get_protocol_id(expected_channel->context, &expected_info);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = actual_channel->get_protocol_id(actual_channel->context, &actual_info);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_NOT_EQUAL(0, kaa_transport_protocol_id_equals(&actual_info, &expected_info));
}

static void test_auth_failure_handler(kaa_auth_failure_reason reason, void *context)
{
    assert_ptr_not_equal(context, NULL);

    *(kaa_auth_failure_reason *)context = reason;
}


/**
 * UNIT TESTS
 */
void test_create_channel_manager(void **state)
{
    (void)state;

    kaa_error_t error_code;
    kaa_channel_manager_t *channel_manager = NULL;

    error_code = kaa_channel_manager_create(NULL, &kaa_context);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_channel_manager_create(&channel_manager, NULL);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_channel_manager_create(&channel_manager, &kaa_context);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_EQUAL(channel_manager, NULL);

    kaa_channel_manager_destroy(channel_manager);
}

void test_add_channel(void **state)
{
    (void)state;

    kaa_error_t error_code;
    kaa_channel_manager_t *channel_manager = NULL;

    error_code = kaa_channel_manager_create(&channel_manager, &kaa_context);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint32_t protocol_id = 0xAABBCCDD;
    uint16_t protocol_version = 2;
    test_channel_context_t channel_context = { { protocol_id, protocol_version }
                                              , NULL
                                              , SUPPORTED_SERVICES
                                              , supported_services_count };

    kaa_transport_channel_interface_t expected_channel;
    test_create_channel_interface(&expected_channel, &channel_context);

    uint32_t channel_id;
    error_code = kaa_channel_manager_add_transport_channel(channel_manager, &expected_channel, &channel_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_channel_manager_add_transport_channel(channel_manager, &expected_channel, &channel_id);
    ASSERT_EQUAL(error_code, KAA_ERR_ALREADY_EXISTS);

    kaa_transport_channel_interface_t *actual_channel =
                        kaa_channel_manager_get_transport_channel(
                                channel_manager, rand() % supported_services_count);
    ASSERT_NOT_EQUAL(actual_channel, NULL);

    compare_channels(actual_channel, &expected_channel);

    error_code = kaa_channel_manager_remove_transport_channel(channel_manager, channel_id);
    assert_int_equal(KAA_ERR_NONE, error_code);

    actual_channel = kaa_channel_manager_get_transport_channel(channel_manager
                                                             , rand() % supported_services_count);
    ASSERT_EQUAL(actual_channel, NULL);

    kaa_channel_manager_destroy(channel_manager);
}

void test_get_service_specific_channel(void **state)
{
    (void)state;

    kaa_error_t error_code;
    kaa_channel_manager_t *channel_manager = NULL;

    error_code = kaa_channel_manager_create(&channel_manager, &kaa_context);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint32_t global_channel_protocol_id = 0xAABBCCAA;
    uint16_t protocol_version = 2;
    test_channel_context_t global_channel_context = { { global_channel_protocol_id, protocol_version }
                                                      , NULL
                                                      , SUPPORTED_SERVICES
                                                      , supported_services_count };

    kaa_transport_channel_interface_t global_channel;
    test_create_channel_interface(&global_channel, &global_channel_context);

    uint32_t global_channel_id;
    error_code = kaa_channel_manager_add_transport_channel(channel_manager, &global_channel, &global_channel_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint32_t logging_channel_protocol_id = 0xAABBCCBB;
    kaa_extension_id logging_channel_service[] = { KAA_EXTENSION_LOGGING };
    test_channel_context_t logging_channel_context = { { logging_channel_protocol_id, protocol_version }
                                                      , NULL
                                                      , logging_channel_service
                                                      , 1 };

    uint32_t logging_channel_id;
    kaa_transport_channel_interface_t logging_channel;
    test_create_channel_interface(&logging_channel, &logging_channel_context);

    error_code = kaa_channel_manager_add_transport_channel(channel_manager, &logging_channel, &logging_channel_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_transport_channel_interface_t *actual_channel =
            kaa_channel_manager_get_transport_channel(channel_manager, KAA_EXTENSION_LOGGING);
    ASSERT_NOT_EQUAL(actual_channel, NULL);

    compare_channels(actual_channel, &logging_channel);

    actual_channel = kaa_channel_manager_get_transport_channel(channel_manager, KAA_EXTENSION_BOOTSTRAP);
    ASSERT_NOT_EQUAL(actual_channel, NULL);

    compare_channels(actual_channel, &global_channel);

    error_code = kaa_channel_manager_remove_transport_channel(channel_manager, logging_channel_id);
    assert_int_equal(KAA_ERR_NONE, error_code);

    actual_channel = kaa_channel_manager_get_transport_channel(channel_manager, KAA_EXTENSION_LOGGING);
    ASSERT_NOT_EQUAL(actual_channel, NULL);

    compare_channels(actual_channel, &global_channel);

    error_code = kaa_channel_manager_remove_transport_channel(channel_manager, global_channel_id);
    assert_int_equal(KAA_ERR_NONE, error_code);

    actual_channel = kaa_channel_manager_get_transport_channel(channel_manager, KAA_EXTENSION_LOGGING);
    ASSERT_EQUAL(actual_channel, NULL);

    kaa_channel_manager_destroy(channel_manager);
}

void test_get_bootstrap_client_sync_size(void **state)
{
    (void)state;

    kaa_error_t error_code;
    size_t expected_size = 0, actual_size = 0;
    kaa_channel_manager_t *channel_manager = NULL;

    error_code = kaa_channel_manager_create(&channel_manager, &kaa_context);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_channel_manager_bootstrap_request_get_size(channel_manager, &actual_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(actual_size, 0);

    uint16_t channel_count = 0;

    uint32_t channel1_protocol_id = 0xAABBCCAA;
    uint16_t protocol_version = 2;
    test_channel_context_t channel_context1 = { { channel1_protocol_id, protocol_version }
                                                , NULL
                                                , SUPPORTED_SERVICES
                                                , supported_services_count };

    kaa_transport_channel_interface_t channel1;
    test_create_channel_interface(&channel1, &channel_context1);
    ++channel_count;

    uint32_t channel2_protocol_id = 0xAABBCCBB;
    test_channel_context_t channel_context2 = { { channel2_protocol_id, protocol_version }
                                                , NULL
                                                , SUPPORTED_SERVICES
                                                , supported_services_count };

    kaa_transport_channel_interface_t channel2;
    test_create_channel_interface(&channel2, &channel_context2);
    ++channel_count;

    uint32_t channel1_id;
    error_code = kaa_channel_manager_add_transport_channel(channel_manager, &channel1, &channel1_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint32_t channel2_id;
    error_code = kaa_channel_manager_add_transport_channel(channel_manager, &channel2, &channel2_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_channel_manager_bootstrap_request_get_size(channel_manager, &actual_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    expected_size += KAA_EXTENSION_HEADER_SIZE
                   + sizeof(uint16_t)
                   + sizeof(uint16_t)
                   + channel_count * (sizeof(uint32_t)
                                    + sizeof(uint16_t)
                                    + sizeof(uint16_t));
    ASSERT_EQUAL(actual_size, expected_size);

    error_code = kaa_channel_manager_remove_transport_channel(channel_manager, channel1_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    --channel_count;

    error_code = kaa_channel_manager_bootstrap_request_get_size(channel_manager, &actual_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    expected_size -= sizeof(uint32_t)
                   + sizeof(uint16_t)
                   + sizeof(uint16_t);
    ASSERT_EQUAL(actual_size, expected_size);

    error_code = kaa_channel_manager_remove_transport_channel(channel_manager, channel2_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    --channel_count;

    error_code = kaa_channel_manager_bootstrap_request_get_size(channel_manager, &actual_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(actual_size, 0);

    kaa_channel_manager_destroy(channel_manager);
}

void test_get_bootstrap_client_sync_serialize(void **state)
{
    (void)state;

    uint16_t channel_count = 0;
    kaa_error_t error_code;
    size_t sync_size = 0;
    kaa_channel_manager_t *channel_manager = NULL;

    error_code = kaa_channel_manager_create(&channel_manager, &kaa_context);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint32_t channel1_protocol_id = 0xAABBCCAA;
    uint16_t protocol_version = 2;
    test_channel_context_t channel_context1 = { { channel1_protocol_id, protocol_version }
                                                , NULL
                                                , SUPPORTED_SERVICES
                                                , supported_services_count };
    kaa_transport_channel_interface_t channel1;
    test_create_channel_interface(&channel1, &channel_context1);
    ++channel_count;

    uint32_t channel2_protocol_id = 0xAABBCCBB;
    test_channel_context_t channel_context2 = { { channel2_protocol_id, protocol_version }
                                                , NULL
                                                , SUPPORTED_SERVICES
                                                , supported_services_count };
    kaa_transport_channel_interface_t channel2;
    test_create_channel_interface(&channel2, &channel_context2);
    ++channel_count;

    error_code = kaa_channel_manager_add_transport_channel(channel_manager, &channel1, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_channel_manager_add_transport_channel(channel_manager, &channel2, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_channel_manager_bootstrap_request_get_size(channel_manager, &sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint8_t manual_buffer[sync_size];
    uint8_t auto_buffer[sync_size];

    kaa_platform_message_writer_t *manual_writer;
    kaa_platform_message_writer_t *auto_writer;

    error_code = kaa_platform_message_writer_create(&manual_writer, manual_buffer, sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_message_writer_create(&auto_writer, auto_buffer, sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_message_write_extension_header(manual_writer
                                                           , KAA_EXTENSION_BOOTSTRAP
                                                           , 0
                                                           , (sync_size - KAA_EXTENSION_HEADER_SIZE));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint16_t request_id = 1;
    uint16_t network_bytes_16;
    uint32_t network_bytes_32;

    network_bytes_16 = KAA_HTONS(request_id);
    error_code = kaa_platform_message_write(manual_writer, &network_bytes_16, sizeof(network_bytes_16));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    network_bytes_16 = KAA_HTONS(channel_count);
    error_code = kaa_platform_message_write(manual_writer, &network_bytes_16, sizeof(network_bytes_16));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);


    kaa_transport_protocol_id_t protocol_info;
    kaa_transport_channel_interface_t *channels[] = { &channel1
                                                    , &channel2 };
    ASSERT_EQUAL(channel_count, sizeof(channels) / sizeof(kaa_transport_channel_interface_t *));

    while (channel_count--) {
        error_code = channels[channel_count]->get_protocol_id(channels[channel_count]->context, &protocol_info);
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);

        network_bytes_32 = KAA_HTONL(protocol_info.id);
        error_code = kaa_platform_message_write(manual_writer, &network_bytes_32, sizeof(network_bytes_32));
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);

        network_bytes_16 = KAA_HTONS(protocol_info.version);
        error_code = kaa_platform_message_write(manual_writer, &network_bytes_16, sizeof(network_bytes_16));
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);

        network_bytes_16 = 0;
        error_code = kaa_platform_message_write(manual_writer, &network_bytes_16, sizeof(network_bytes_16));
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    }

    error_code = kaa_channel_manager_bootstrap_request_serialize(channel_manager, auto_writer);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_EQUAL(0, memcmp(auto_buffer, manual_buffer, sync_size));

    kaa_platform_message_writer_destroy(manual_writer);
    kaa_platform_message_writer_destroy(auto_writer);

    kaa_channel_manager_destroy(channel_manager);
}

void test_process_auth_failure(void **state)
{
    (void)state;

    kaa_error_t error_code;
    kaa_channel_manager_t *channel_manager = NULL;
    kaa_auth_failure_reason reason;

    error_code = kaa_channel_manager_create(&channel_manager, &kaa_context);
    assert_int_equal(error_code, KAA_ERR_NONE);
    kaa_channel_manager_set_auth_failure_handler(channel_manager, test_auth_failure_handler, &reason);

    kaa_channel_manager_process_auth_failure(channel_manager, KAA_AUTH_STATUS_BAD_CREDENTIALS);
    assert_int_equal(reason, KAA_AUTH_STATUS_BAD_CREDENTIALS);

    kaa_channel_manager_process_auth_failure(channel_manager, KAA_AUTH_STATUS_VERIFICATION_FAILED);
    assert_int_equal(reason, KAA_AUTH_STATUS_VERIFICATION_FAILED);
}

int test_init(void)
{
    srand(time(NULL));

    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger)
        return error;

    kaa_context.logger = logger;
    kaa_context.bootstrap_manager = NULL;
    kaa_context.platform_protocol = NULL;

    return 0;
}

int test_deinit(void)
{
    kaa_log_destroy(logger);
    return 0;
}



KAA_SUITE_MAIN(Log, test_init, test_deinit
       ,
       KAA_TEST_CASE(create_channel_manager, test_create_channel_manager)
       /* KAA-988 */
       /*
       KAA_TEST_CASE(add_channel, test_add_channel)
       KAA_TEST_CASE(get_service_specific_channel, test_get_service_specific_channel)
       KAA_TEST_CASE(get_bootstrap_client_sync_size, test_get_bootstrap_client_sync_size)
       KAA_TEST_CASE(get_bootstrap_client_sync_serialize, test_get_bootstrap_client_sync_serialize)
       */
       KAA_TEST_CASE(process_auth_faliure, test_process_auth_failure)
       )

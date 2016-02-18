/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <stdint.h>
#include <time.h>
#include <unistd.h>
#include "platform/ext_sha.h"

#include "kaa_logging.h"

#ifndef KAA_DISABLE_FEATURE_LOGGING

#include "kaa_test.h"

#include "kaa_context.h"
#include "kaa_platform_protocol.h"
#include "kaa_channel_manager.h"
#include "kaa_platform_utils.h"
#include "kaa_status.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "platform/sock.h"
#include "platform/ext_log_storage.h"
#include "platform/ext_log_upload_strategy.h"



extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void        kaa_status_destroy(kaa_status_t *self);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_context_t *context);
extern void        kaa_channel_manager_destroy(kaa_channel_manager_t *self);

extern kaa_error_t kaa_log_collector_create(kaa_log_collector_t ** log_collector_p
                                          , kaa_status_t *status
                                          , kaa_channel_manager_t *channel_manager
                                          , kaa_logger_t *logger);
extern void        kaa_log_collector_destroy(kaa_log_collector_t *self);

extern kaa_error_t kaa_logging_request_serialize(kaa_log_collector_t *self, kaa_platform_message_writer_t *writer);
extern kaa_error_t kaa_logging_handle_server_sync(kaa_log_collector_t *self
                                                , kaa_platform_message_reader_t *reader
                                                , uint16_t extension_options
                                                , size_t extension_length);
extern kaa_error_t kaa_logging_request_get_size(kaa_log_collector_t *self, size_t *expected_size);



static kaa_context_t kaa_context;
static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_channel_manager_t *channel_manager = NULL;

static const kaa_service_t OPERATIONS_SERVICES[] = { KAA_SERVICE_PROFILE
                                                   , KAA_SERVICE_USER
                                                   , KAA_SERVICE_CONFIGURATION
                                                   , KAA_SERVICE_EVENT
                                                   , KAA_SERVICE_LOGGING
                                                   , KAA_SERVICE_NOTIFICATION
                                                    };
static const int OPERATIONS_SERVICES_COUNT = sizeof(OPERATIONS_SERVICES) / sizeof(kaa_service_t);

#define TEST_LOG_BUFFER  "log_record"



typedef struct {
    size_t timeout;
    size_t max_parallel_uploads;
    bool on_timeout_count;
    bool on_failure_count;
    ext_log_upload_decision_t decision;
} mock_strategy_context_t;

typedef struct {
    kaa_list_t *logs;
    size_t record_count;
    size_t total_size;
    bool on_remove_by_id_count;
    bool on_unmark_by_id_count;
} mock_storage_context_t;

typedef struct {
    size_t on_sync_count;
} mock_transport_channel_context_t;



/*
 * TRANSPORT_CHANNEL INTERFACE
 */
static kaa_error_t test_kaa_init_channel(void *channel_context
                                          , kaa_transport_context_t *transport_context)
{
    return KAA_ERR_NONE;
}

static kaa_error_t test_kaa_set_access_point(void *channel_context
                                              , kaa_access_point_t *access_point)
{
    return KAA_ERR_NONE;
}

static kaa_error_t test_kaa_get_protocol_id(void *context
                                             , kaa_transport_protocol_id_t *protocol_info)
{
    protocol_info->id = 1;
    protocol_info->version = 1;
    return KAA_ERR_NONE;
}

static kaa_error_t test_kaa_get_supported_services(void *context
                                                    , kaa_service_t **supported_services
                                                    , size_t *service_count)
{
    *supported_services = (kaa_service_t *)OPERATIONS_SERVICES;
    *service_count = OPERATIONS_SERVICES_COUNT;
    return KAA_ERR_NONE;
}

static kaa_error_t test_kaa_sync_handler(void *context
                                          , const kaa_service_t services[]
                                          , size_t service_count)
{
    ((mock_transport_channel_context_t *)context)->on_sync_count++;
    return KAA_ERR_NONE;
}

static kaa_error_t test_kaa_channel_destroy(void *context)
{
    KAA_FREE(context);
    return KAA_ERR_NONE;
}

static void test_kaa_channel_create(kaa_transport_channel_interface_t *self)
{
    self->context = KAA_CALLOC(1, sizeof(mock_transport_channel_context_t));

    self->get_protocol_id = test_kaa_get_protocol_id;
    self->get_supported_services = test_kaa_get_supported_services;
    self->destroy = test_kaa_channel_destroy;
    self->sync_handler = test_kaa_sync_handler;
    self->init = test_kaa_init_channel;
    self->set_access_point = test_kaa_set_access_point;
}



/*
 * STRATEGY INTERFACE
 */
ext_log_upload_decision_t ext_log_upload_strategy_decide(void *context, const void *log_storage_context)
{
    return ((mock_strategy_context_t *)context)->decision;
}

size_t ext_log_upload_strategy_get_timeout(void *context)
{
    return ((mock_strategy_context_t *)context)->timeout;
}

kaa_error_t ext_log_upload_strategy_on_timeout(void *context)
{
    ((mock_strategy_context_t *)context)->on_timeout_count++;
    return KAA_ERR_NONE;
}

kaa_error_t ext_log_upload_strategy_on_failure(void *context, logging_delivery_error_code_t error_code)
{
    ((mock_strategy_context_t *)context)->on_failure_count++;
    return KAA_ERR_NONE;
}

size_t ext_log_upload_strategy_get_max_parallel_uploads(void *context)
{
    return ((mock_strategy_context_t *)context)->max_parallel_uploads;
}

void ext_log_upload_strategy_destroy(void *context)
{

}

bool ext_log_upload_strategy_is_timeout_strategy(void *strategy)
{
    return false;
}


/*
 * STORAGE INTERFACE
 */

typedef struct {
    size_t size;
    char* data;
} test_log_record_t;

kaa_error_t ext_log_storage_allocate_log_record_buffer(void *context, kaa_log_record_t *record)
{
    record->data = KAA_MALLOC(record->size);
    return KAA_ERR_NONE;
}

kaa_error_t ext_log_storage_deallocate_log_record_buffer(void *context, kaa_log_record_t *record)
{
    KAA_FREE(record->data);
    return KAA_ERR_NONE;
}

kaa_error_t ext_log_storage_add_log_record(void *context, kaa_log_record_t *record)
{
    mock_storage_context_t *self = (mock_storage_context_t *)context;

    kaa_log_record_t *rec = KAA_MALLOC(sizeof(kaa_log_record_t));
    *rec = *record;

    kaa_list_push_back(self->logs, rec);

    self->record_count++;
    self->total_size += rec->size;

    return KAA_ERR_NONE;
}

kaa_error_t ext_log_storage_write_next_record(void *context, char *buffer, size_t buffer_len, uint16_t *bucket_id, size_t *record_len)
{
    mock_storage_context_t *self = (mock_storage_context_t *)context;
    KAA_RETURN_IF_NIL2(self, self->logs, KAA_ERR_NOT_FOUND);

    kaa_log_record_t *record = kaa_list_get_data(kaa_list_begin(self->logs));

    if (buffer_len < record->size) {
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    *record_len = record->size;
    *bucket_id = 1;
    memcpy(buffer, record->data, *record_len);
    return KAA_ERR_NONE;
}

kaa_error_t ext_log_storage_remove_by_bucket_id(void *context, uint16_t bucket_id)
{
    ((mock_storage_context_t *)context)->on_remove_by_id_count++;
    return KAA_ERR_NONE;
}

kaa_error_t ext_log_storage_unmark_by_bucket_id(void *context, uint16_t bucket_id)
{
    ((mock_storage_context_t *)context)->on_unmark_by_id_count++;
    return KAA_ERR_NONE;
}

size_t ext_log_storage_get_total_size(const void *context)
{
    return ((mock_storage_context_t *)context)->total_size;
}

size_t ext_log_storage_get_records_count(const void *context)
{
    return ((mock_storage_context_t *)context)->record_count;
}

static void destroy_log_record(void *record_p)
{
    if (record_p) {
        KAA_FREE(((kaa_log_record_t*)record_p)->data);
        KAA_FREE(record_p);
    }
}

mock_storage_context_t *create_mock_storage(void)
{
    mock_storage_context_t *storage = (mock_storage_context_t *)KAA_CALLOC(1, sizeof(mock_storage_context_t));
    KAA_RETURN_IF_NIL(storage, NULL);
    storage->logs = kaa_list_create();

    return storage;
}

kaa_error_t ext_log_storage_destroy(void *context)
{
    if (context) {
        kaa_list_destroy(((mock_storage_context_t *)context)->logs, &destroy_log_record);
        KAA_FREE(context);
    }
    return KAA_ERR_NONE;
}



void test_create_request(void)
{
    KAA_TRACE_IN(logger);

    kaa_error_t error_code = KAA_ERR_NONE;

    kaa_user_log_record_t *test_log_record = kaa_test_log_record_create();
    test_log_record->data = kaa_string_copy_create(TEST_LOG_BUFFER);
    size_t test_log_record_size = test_log_record->get_size(test_log_record);

    kaa_log_collector_t *log_collector = NULL;
    error_code = kaa_log_collector_create(&log_collector, status, channel_manager, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    mock_strategy_context_t strategy;
    memset(&strategy, 0, sizeof(mock_strategy_context_t));
    strategy.decision = NOOP;
    strategy.max_parallel_uploads = UINT32_MAX;

    kaa_log_bucket_constraints_t constraints = {
        .max_bucket_size = 2 * test_log_record_size,
        .max_bucket_log_count = UINT32_MAX,
    };

    error_code = kaa_logging_init(log_collector, create_mock_storage(), &strategy, &constraints);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_add_record(log_collector, test_log_record, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t expected_size = 0;
    error_code = kaa_logging_request_get_size(log_collector, &expected_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    char buffer[expected_size];
    kaa_platform_message_writer_t *writer = NULL;
    error_code = kaa_platform_message_writer_create(&writer, buffer, expected_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(writer);

    error_code = kaa_logging_request_serialize(log_collector, writer);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    kaa_platform_message_writer_destroy(writer);

    char *buf_cursor = buffer;
    ASSERT_EQUAL(KAA_LOGGING_EXTENSION_TYPE, KAA_HTONS(*(uint16_t*)buf_cursor));
    buf_cursor += sizeof(uint16_t);

    char options[] = { 0x00, 0x01 };
    ASSERT_EQUAL(memcmp(buf_cursor, options, 2), 0);
    buf_cursor += 2;

    ASSERT_EQUAL(*(uint32_t *) buf_cursor, KAA_HTONL(20));
    buf_cursor += sizeof(uint32_t);

    char request_id_records_count[]  = { 0x00, 0x01, 0x00, 0x01 };
    ASSERT_EQUAL(memcmp(buf_cursor, request_id_records_count, 4), 0);
    buf_cursor += 4;

    char record_buf[test_log_record_size];
    avro_writer_t avro_writer = avro_writer_memory(record_buf, test_log_record_size);
    test_log_record->serialize(avro_writer, test_log_record);
    avro_writer_free(avro_writer);

    ASSERT_EQUAL(*(uint32_t *) buf_cursor, KAA_HTONL(test_log_record_size));
    buf_cursor += sizeof(uint32_t);

    ASSERT_EQUAL(memcmp(buf_cursor, record_buf, test_log_record_size), 0);

    kaa_log_collector_destroy(log_collector);
    test_log_record->destroy(test_log_record);

    KAA_TRACE_OUT(logger);
}



void test_response(void)
{
    KAA_TRACE_IN(logger);

    srand(time(NULL));

    kaa_error_t error_code = KAA_ERR_NONE;

    kaa_log_collector_t *log_collector = NULL;
    error_code = kaa_log_collector_create(&log_collector, status, channel_manager, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    mock_strategy_context_t strategy;
    memset(&strategy, 0, sizeof(mock_strategy_context_t));

    mock_storage_context_t *storage = create_mock_storage();

    kaa_log_bucket_constraints_t constraints = {
        .max_bucket_size = 1024,
        .max_bucket_log_count = UINT32_MAX,
    };

    error_code = kaa_logging_init(log_collector, storage, &strategy, &constraints);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint32_t response_count = 2;
    size_t response_buffer_size = sizeof(uint32_t) + sizeof(uint32_t) * response_count;
    char response_buffer[response_buffer_size];

    char *response = response_buffer;
    *((uint32_t *)response) = KAA_HTONL(response_count);
    response += sizeof(uint32_t);

    /* First response */
    *((uint16_t *)response) = KAA_HTONS(rand());
    response += sizeof(uint16_t);
    *((uint8_t *)response) = 0x0; // SUCCESS
    response += sizeof(uint8_t);
    *((uint8_t *)response) = 0;
    response += sizeof(uint8_t);

    /* Second response */
    *((uint16_t *)response) = KAA_HTONS(rand());
    response += sizeof(uint16_t);
    *((uint8_t *)response) = 0x1; // FAILURE
    response += sizeof(uint8_t);
    *((uint8_t *)response) = rand() % 4;
    response += sizeof(uint8_t);

    kaa_platform_message_reader_t *reader = NULL;
    error_code = kaa_platform_message_reader_create(&reader, response_buffer, response_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(reader);

    error_code = kaa_logging_handle_server_sync(log_collector, reader, 0, response_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_TRUE(strategy.on_failure_count);
    ASSERT_TRUE(storage->on_remove_by_id_count);
    ASSERT_TRUE(storage->on_unmark_by_id_count);

    kaa_platform_message_reader_destroy(reader);
    kaa_log_collector_destroy(log_collector);

    KAA_TRACE_OUT(logger);
}



void test_timeout(void)
{
    KAA_TRACE_IN(logger);

    kaa_error_t error_code = KAA_ERR_NONE;

    size_t TEST_TIMEOUT = 2;

    kaa_log_collector_t *log_collector = NULL;
    error_code = kaa_log_collector_create(&log_collector, status, channel_manager, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_user_log_record_t *test_log_record = kaa_test_log_record_create();
    test_log_record->data = kaa_string_copy_create(TEST_LOG_BUFFER);
    size_t test_log_record_size = test_log_record->get_size(test_log_record);

    mock_strategy_context_t strategy;
    memset(&strategy, 0, sizeof(mock_strategy_context_t));
    strategy.timeout = TEST_TIMEOUT;
    strategy.decision = NOOP;
    strategy.max_parallel_uploads = UINT32_MAX;

    kaa_log_bucket_constraints_t constraints = {
        .max_bucket_size = 2 * test_log_record_size,
        .max_bucket_log_count = UINT32_MAX,
    };

    error_code = kaa_logging_init(log_collector, create_mock_storage(), &strategy, &constraints);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_add_record(log_collector, test_log_record, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t request_buffer_size = 256;
    char request_buffer[request_buffer_size];
    kaa_platform_message_writer_t *writer = NULL;
    error_code = kaa_platform_message_writer_create(&writer, request_buffer, request_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_request_serialize(log_collector, writer);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    sleep(TEST_TIMEOUT + 1);

    error_code = kaa_logging_add_record(log_collector, test_log_record, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_TRUE(strategy.on_timeout_count);

    test_log_record->destroy(test_log_record);
    kaa_platform_message_writer_destroy(writer);
    kaa_log_collector_destroy(log_collector);

    KAA_TRACE_OUT(logger);
}

void test_decline_timeout(void)
{
    KAA_TRACE_IN(logger);

    kaa_error_t error_code = KAA_ERR_NONE;

    size_t TEST_TIMEOUT = 2;

    kaa_log_collector_t *log_collector = NULL;
    error_code = kaa_log_collector_create(&log_collector, status, channel_manager, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_user_log_record_t *test_log_record = kaa_test_log_record_create();
    test_log_record->data = kaa_string_copy_create(TEST_LOG_BUFFER);
    size_t test_log_record_size = test_log_record->get_size(test_log_record);

    mock_strategy_context_t strategy;
    memset(&strategy, 0, sizeof(mock_strategy_context_t));
    strategy.timeout = TEST_TIMEOUT;
    strategy.decision = NOOP;
    strategy.max_parallel_uploads = UINT32_MAX;

    mock_storage_context_t *storage = create_mock_storage();
    ASSERT_NOT_NULL(storage);

    kaa_log_bucket_constraints_t constraints = {
        .max_bucket_size = 2 * test_log_record_size,
        .max_bucket_log_count = UINT32_MAX,
    };

    error_code = kaa_logging_init(log_collector, storage, &strategy, &constraints);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_add_record(log_collector, test_log_record, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t request_buffer_size = 256;
    char request_buffer[request_buffer_size];
    kaa_platform_message_writer_t *writer = NULL;
    error_code = kaa_platform_message_writer_create(&writer, request_buffer, request_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_request_serialize(log_collector, writer);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    sleep(TEST_TIMEOUT + 1);

    uint16_t bucket_id = *((uint16_t *)(request_buffer + KAA_EXTENSION_HEADER_SIZE));
    bucket_id = KAA_NTOHS(bucket_id);

    uint32_t response_count = 1;
    size_t response_buffer_size = sizeof(uint32_t) + sizeof(uint32_t) * response_count;
    char response_buffer[response_buffer_size];

    char *response = response_buffer;
    *((uint32_t *)response) = KAA_HTONL(response_count);
    response += sizeof(uint32_t);

    /* First response */
    *((uint16_t *)response) = KAA_HTONS(bucket_id);
    response += sizeof(uint16_t);
    *((uint8_t *)response) = 0x0; // SUCCESS
    response += sizeof(uint8_t);
    *((uint8_t *)response) = 0;
    response += sizeof(uint8_t);

    kaa_platform_message_reader_t *reader = NULL;
    error_code = kaa_platform_message_reader_create(&reader, response_buffer, response_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(reader);

    error_code = kaa_logging_handle_server_sync(log_collector, reader, 0, response_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_TRUE(storage->on_remove_by_id_count);

    error_code = kaa_logging_add_record(log_collector, test_log_record, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_FALSE(strategy.on_timeout_count);

    test_log_record->destroy(test_log_record);
    kaa_platform_message_writer_destroy(writer);
    kaa_platform_message_reader_destroy(reader);
    kaa_log_collector_destroy(log_collector);

    KAA_TRACE_OUT(logger);
}

void test_max_parallel_uploads_with_log_sync(void)
{
    KAA_TRACE_IN(logger);

    kaa_error_t error_code = KAA_ERR_NONE;

    uint32_t channel_id = 0;
    kaa_transport_channel_interface_t transport_context;
    test_kaa_channel_create(&transport_context);

    error_code = kaa_channel_manager_add_transport_channel(channel_manager, &transport_context, &channel_id);

    kaa_log_collector_t *log_collector = NULL;
    error_code = kaa_log_collector_create(&log_collector, status, channel_manager, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_user_log_record_t *test_log_record = kaa_test_log_record_create();
    test_log_record->data = kaa_string_copy_create(TEST_LOG_BUFFER);
    size_t test_log_size = test_log_record->get_size(test_log_record);

    mock_strategy_context_t strategy;
    memset(&strategy, 0, sizeof(mock_strategy_context_t));
    strategy.timeout = UINT32_MAX;
    strategy.decision = UPLOAD;

    mock_storage_context_t *storage = create_mock_storage();
    ASSERT_NOT_NULL(storage);

    kaa_log_bucket_constraints_t constraints = {
        .max_bucket_size = 2 * test_log_size,
        .max_bucket_log_count = UINT32_MAX,
    };

    error_code = kaa_logging_init(log_collector, storage, &strategy, &constraints);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    /*
     * Ensure the log delivery is forbidden at all.
     */
    strategy.max_parallel_uploads = 0;
    error_code = kaa_logging_add_record(log_collector, test_log_record, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_EQUAL(((mock_transport_channel_context_t *)transport_context.context)->on_sync_count, 0);

    /*
     * Ensure the first request is allowed.
     */
    strategy.max_parallel_uploads = 1;
    error_code = kaa_logging_add_record(log_collector, test_log_record, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(((mock_transport_channel_context_t *)transport_context.context)->on_sync_count, 1);

    /*
     * Do the first request to remember the delivery timeout of the log batch.
     */
    size_t request_buffer_size = 256;
    char request_buffer[request_buffer_size];
    kaa_platform_message_writer_t *writer = NULL;
    error_code = kaa_platform_message_writer_create(&writer, request_buffer, request_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_request_serialize(log_collector, writer);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_add_record(log_collector, test_log_record, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    /*
     * Ensure the second request is forbidden.
     */
    ASSERT_EQUAL(((mock_transport_channel_context_t *)transport_context.context)->on_sync_count, 1);

    /*
     * Clean up.
     */
    error_code = kaa_channel_manager_remove_transport_channel(channel_manager, channel_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    test_log_record->destroy(test_log_record);
    kaa_log_collector_destroy(log_collector);

    KAA_TRACE_OUT(logger);
}

void test_max_parallel_uploads_with_sync_all(void)
{
    KAA_TRACE_IN(logger);

    kaa_error_t error_code = KAA_ERR_NONE;

    uint32_t channel_id = 0;
    kaa_transport_channel_interface_t transport_context;
    test_kaa_channel_create(&transport_context);

    error_code = kaa_channel_manager_add_transport_channel(channel_manager, &transport_context, &channel_id);

    kaa_log_collector_t *log_collector = NULL;
    error_code = kaa_log_collector_create(&log_collector, status, channel_manager, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_user_log_record_t *test_log_record = kaa_test_log_record_create();
    test_log_record->data = kaa_string_copy_create(TEST_LOG_BUFFER);
    size_t test_log_size = test_log_record->get_size(test_log_record);

    mock_strategy_context_t strategy;
    memset(&strategy, 0, sizeof(mock_strategy_context_t));
    strategy.timeout = UINT32_MAX;
    strategy.decision = UPLOAD;

    mock_storage_context_t *storage = create_mock_storage();
    ASSERT_NOT_NULL(storage);

    kaa_log_bucket_constraints_t constraints = {
        .max_bucket_size = 2 * test_log_size,
        .max_bucket_log_count = UINT32_MAX,
    };

    error_code = kaa_logging_init(log_collector, storage, &strategy, &constraints);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    /*
     * Ensure the log delivery is forbidden at all.
     */
    strategy.max_parallel_uploads = 0;
    error_code = kaa_logging_add_record(log_collector, test_log_record, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t expected_size = 0;
    error_code = kaa_logging_request_get_size(log_collector, &expected_size);
    ASSERT_FALSE(expected_size);

    /*
     * Ensure the first request is allowed.
     */
    strategy.max_parallel_uploads = 1;
    error_code = kaa_logging_add_record(log_collector, test_log_record, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    /*
     * Do the first request to remember the delivery timeout of the log batch.
     */
    error_code = kaa_logging_request_get_size(log_collector, &expected_size);
    ASSERT_TRUE(expected_size);
    size_t request_buffer_size = 256;
    char request_buffer[request_buffer_size];
    kaa_platform_message_writer_t *writer = NULL;
    error_code = kaa_platform_message_writer_create(&writer, request_buffer, request_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_request_serialize(log_collector, writer);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_add_record(log_collector, test_log_record, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    /*
     * Ensure the second request is forbidden.
     */
    error_code = kaa_logging_request_get_size(log_collector, &expected_size);
    ASSERT_FALSE(expected_size);

    /*
     * Clean up.
     */
    error_code = kaa_channel_manager_remove_transport_channel(channel_manager, channel_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    test_log_record->destroy(test_log_record);
    kaa_log_collector_destroy(log_collector);

    KAA_TRACE_OUT(logger);
}

/* ---------------------------------------------------------------------------*/
/* Log delivery tests                                                         */
/* ---------------------------------------------------------------------------*/

/* Server chunk, managed by a corresponding reader object.
 * Perfectly packed. Packed attribute is intentionally avoided. */
struct response_chunk
{
    uint8_t bucket_id[2];  /* 16 bits for bucket ID */
    uint8_t resp_code;     /* 8 bits for response code. 0 == SUCCESS, 1 == FAILURE */
    uint8_t reserved;      /* Should be 0 */
};

struct response_packet
{
    uint8_t resp_cnt[4];           /* 32 bits for amount of responces in buffer */
    struct response_chunk resps[]; /* Responses itself */
};

#define RESP_PACKETS               2 /* Amount of response packets */
#define RESP_SUCCESS_IDX           0 /* Index of successfull response */
#define RESP_FAILURE_IDX           1 /* Index of failed response */
#define TEST_BUFFER_SIZE           1024
#define TEST_EXT_OP                0 /* Simple stub */
#define TEST_TIMEOUT               2

static mock_strategy_context_t         test_strategy1;
static mock_strategy_context_t         test_strategy2;
static mock_storage_context_t          *test_storage1;
static mock_storage_context_t          *test_storage2;
static kaa_log_collector_t             *log_collector;
static size_t                          test_log_record_size = TEST_BUFFER_SIZE;
/* Will contain response_packet. Thus required to be aligned. */
_Alignas(4) static char                test_reader_buffer[TEST_BUFFER_SIZE];
_Alignas(4) static char                test_writer_buffer[TEST_BUFFER_SIZE];
/* Portion of the test buffer filled with valid data */
static size_t                          test_filled_size;
static kaa_platform_message_reader_t   *test_reader;
static kaa_platform_message_writer_t   *test_writer;
static kaa_user_log_record_t           *test_log_record;

/* Values to be checked inside mock event function */
static void     *expected_ctx;
static int      check_bucket;
static uint16_t expected_bucked_id;

/* Required to trace generic mock function calls */
static int      call_is_expected;
static int      call_completed;

/* Required to trace on fail mock function calls */
static int      failed_call_is_expected;
static int      failed_call_completed;

/* Required to trace on success mock function calls */
static int      success_call_is_expected;
static int      success_call_completed;

/* Required to trace on timeout mock function calls */
static int      timeout_call_is_expected;
static int      timeout_call_completed;

/* Mock event functions */

static void mock_log_event_generic_fn(void *ctx, const kaa_log_bucket_info_t *bucket)
{
    ASSERT_TRUE(call_is_expected);
    ASSERT_NOT_NULL(bucket); /* Shouldn't be NULL no matter what */

    if (check_bucket) {
        ASSERT_EQUAL(expected_bucked_id, bucket->bucket_id);
    }

    ASSERT_EQUAL(expected_ctx, ctx);

    call_completed++;
}

static void mock_log_event_failed_fn(void *ctx, const kaa_log_bucket_info_t *bucket)
{
    ASSERT_TRUE(failed_call_is_expected);
    /* Make sure that generic function will not fail */
    call_is_expected = 1;
    mock_log_event_generic_fn(ctx, bucket);

    failed_call_completed++;
}

static void mock_log_event_success_fn(void *ctx, const kaa_log_bucket_info_t *bucket)
{
    ASSERT_TRUE(success_call_is_expected);
    /* Make sure that generic function will not fail */
    call_is_expected = 1;
    mock_log_event_generic_fn(ctx, bucket);

    success_call_completed++;
}

static void mock_log_event_timeout_fn(void *ctx, const kaa_log_bucket_info_t *bucket)
{
    ASSERT_TRUE(timeout_call_is_expected);
    /* Make sure that generic function will not fail */
    call_is_expected = 1;
    mock_log_event_generic_fn(ctx, bucket);

    timeout_call_completed++;
}

/* ---------------------------------------------------------------------------*/
/* Log setters test group                                                     */
/* ---------------------------------------------------------------------------*/

KAA_GROUP_SETUP(log_setters)
{
    kaa_error_t rc;

    KAA_TRACE_IN(logger);
    rc = kaa_log_collector_create(&log_collector,
                                  status,
                                  channel_manager,
                                  logger);

    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    /* Test objects for strategy test */

    memset(&test_strategy1, 0, sizeof(test_strategy1));
    memset(&test_strategy2, 0, sizeof(test_strategy2));

    kaa_log_bucket_constraints_t constraints = {
        .max_bucket_size = 2 * test_log_record_size,
        .max_bucket_log_count = UINT32_MAX,
    };

    /* Test objects for storage tests */

    test_storage1 = create_mock_storage();
    test_storage2 = create_mock_storage();

    rc = kaa_logging_init(log_collector, test_storage1,
                                  &test_strategy1, &constraints);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    expected_ctx        = NULL;
    expected_bucked_id  = 0;
    call_is_expected    = 0;

    KAA_TRACE_OUT(logger);
}

KAA_GROUP_TEARDOWN(log_setters)
{
    KAA_TRACE_IN(logger);

    /* If tests will pass, one of storages will be destroyed.
     * Each test in this will decide which one should be deleted. */
    kaa_log_collector_destroy(log_collector);
    log_collector = NULL;

    KAA_TRACE_OUT(logger);
}


KAA_TEST_CASE_EX(log_setters, set_strategy_invalid_parameters)
{
    kaa_error_t rc;

    KAA_TRACE_IN(logger);

    rc = kaa_logging_set_strategy(log_collector, NULL);
    ASSERT_EQUAL(KAA_ERR_BADPARAM, rc);

    rc = kaa_logging_set_strategy(NULL, &test_strategy2);
    ASSERT_EQUAL(KAA_ERR_BADPARAM, rc);

    ext_log_storage_destroy(test_storage2);
    ext_log_upload_strategy_destroy(&test_strategy2);

    KAA_TRACE_OUT(logger);
}


KAA_TEST_CASE_EX(log_setters, set_storage_invalid_parameters)
{
    kaa_error_t rc;
    KAA_TRACE_IN(logger);

    rc = kaa_logging_set_storage(log_collector, NULL);
    ASSERT_EQUAL(KAA_ERR_BADPARAM, rc);

    rc = kaa_logging_set_storage(NULL, test_storage2);
    ASSERT_EQUAL(KAA_ERR_BADPARAM, rc);

    ext_log_storage_destroy(test_storage2);
    ext_log_upload_strategy_destroy(&test_strategy2);

    KAA_TRACE_OUT(logger);
}

KAA_TEST_CASE_EX(log_setters, set_strategy_valid_parameters)
{
    kaa_error_t rc;

    KAA_TRACE_IN(logger);

    /* First strategy will be internally deleted */
    rc = kaa_logging_set_strategy(log_collector, &test_strategy2);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    ext_log_storage_destroy(test_storage2);

    /* Second strategy will be deleted on test teardown */

    KAA_TRACE_OUT(logger);
}


KAA_TEST_CASE_EX(log_setters, set_storage_valid_parameters)
{
    kaa_error_t rc;

    KAA_TRACE_IN(logger);

    /* First storage will be internally deleted */
    rc = kaa_logging_set_storage(log_collector, test_storage2);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    ext_log_upload_strategy_destroy(&test_strategy2);

    /* Second storage will be deleted on test teardown */

    KAA_TRACE_OUT(logger);
}


/* ---------------------------------------------------------------------------*/
/* Log delivery callback basic test group                                     */
/* ---------------------------------------------------------------------------*/


KAA_GROUP_SETUP(log_callback_basic)
{
    kaa_error_t error_code;

    KAA_TRACE_IN(logger);
    error_code = kaa_log_collector_create(&log_collector,
                                          status,
                                          channel_manager,
                                          logger);

    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    memset(&test_strategy1, 0, sizeof(test_strategy1));

    kaa_log_bucket_constraints_t constraints = {
        .max_bucket_size = 2 * test_log_record_size,
        .max_bucket_log_count = UINT32_MAX,
    };

    error_code = kaa_logging_init(log_collector, create_mock_storage(),
                                  &test_strategy1, &constraints);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    expected_ctx        = NULL;
    expected_bucked_id  = 0;
    call_is_expected    = 0;

    KAA_TRACE_OUT(logger);
}

KAA_GROUP_TEARDOWN(log_callback_basic)
{
    KAA_TRACE_IN(logger);
    kaa_log_collector_destroy(log_collector);
    log_collector = NULL;

    KAA_TRACE_OUT(logger);
}

KAA_TEST_CASE_EX(log_callback_basic, invalid_parameters)
{
    KAA_TRACE_IN(logger);
    kaa_log_delivery_listener_t listeners;

    /* NULL parameters case */

    kaa_error_t rc = kaa_logging_set_listeners(log_collector, NULL);
    ASSERT_EQUAL(KAA_ERR_BADPARAM, rc);
    rc = kaa_logging_set_listeners(NULL, &listeners);
    ASSERT_EQUAL(KAA_ERR_BADPARAM, rc);

    KAA_TRACE_OUT(logger);
    return;
}

/* This test is also testing the case when listeners isn't called
 * if no logs added */
KAA_TEST_CASE_EX(log_callback_basic, valid_parameters)
{
    KAA_TRACE_IN(logger);

    kaa_error_t rc;

    kaa_log_delivery_listener_t listeners = {
        mock_log_event_generic_fn,
        mock_log_event_generic_fn,
        mock_log_event_generic_fn,
        NULL,
    };

    /* Any of listeners can be NULL */

    rc = kaa_logging_set_listeners(log_collector, &listeners);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    listeners.on_failed = NULL;
    rc = kaa_logging_set_listeners(log_collector, &listeners);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    listeners.on_success = NULL;
    rc = kaa_logging_set_listeners(log_collector, &listeners);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    listeners.on_timeout = NULL;
    rc = kaa_logging_set_listeners(log_collector, &listeners);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    /* Special macro should work too */
    rc = kaa_logging_set_listeners(log_collector, &KAA_LOG_EMPTY_LISTENERS);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    /* Post conditions check */

    /* No callbacks should be called */
    ASSERT_FALSE(call_completed);

    KAA_TRACE_OUT(logger);
}

/* ---------------------------------------------------------------------------*/
/* Log delivery callback extended test group                                  */
/* ---------------------------------------------------------------------------*/

KAA_GROUP_SETUP(log_callback_with_storage)
{
    kaa_error_t error_code;

    KAA_TRACE_IN(logger);
    error_code = kaa_log_collector_create(&log_collector,
                                          status,
                                          channel_manager,
                                          logger);

    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    memset(&test_strategy1, 0, sizeof(mock_strategy_context_t));
    memset(test_reader_buffer, 0, sizeof(test_reader_buffer));
    memset(test_writer_buffer, 0, sizeof(test_writer_buffer));

    expected_ctx             = NULL;
    expected_bucked_id       = 0;
    call_is_expected         = 0;
    call_completed           = 0;
    failed_call_completed    = 0;
    failed_call_is_expected  = 0;
    success_call_completed   = 0;
    success_call_is_expected = 0;
    check_bucket             = 0;

    test_storage1 = create_mock_storage();
    kaa_log_bucket_constraints_t constraints = {
        .max_bucket_size = 2 * test_log_record_size,
        .max_bucket_log_count = UINT32_MAX,
    };

    error_code = kaa_logging_init(log_collector, test_storage1,
                                  &test_strategy1, &constraints);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);


    uint32_t response_count = 2;
    struct response_packet *response = (struct response_packet *) test_reader_buffer;
    *(uint32_t *) response->resp_cnt = KAA_HTONL(response_count);

    /* First response */

    /* Later on we'll override that */
    *(uint16_t *) response->resps[RESP_SUCCESS_IDX].bucket_id = 0;
    response->resps[RESP_SUCCESS_IDX].resp_code = 0; // SUCCESS

    /* Second response */

    /* Later on we'll override that */
    *(uint16_t *) response->resps[RESP_FAILURE_IDX].bucket_id = 0;
    response->resps[RESP_FAILURE_IDX].resp_code = 1; // FAILURE

    test_filled_size = sizeof(struct response_packet)
            + sizeof(struct response_chunk) * 2;

    error_code = kaa_platform_message_reader_create(&test_reader,
                                                    test_reader_buffer,
                                                    test_filled_size);

    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(test_reader);

    KAA_TRACE_OUT(logger);
}

KAA_GROUP_TEARDOWN(log_callback_with_storage)
{
    KAA_TRACE_IN(logger);

    kaa_platform_message_reader_destroy(test_reader);
    test_reader = NULL;
    /* Destroys mock storage as well */
    kaa_log_collector_destroy(log_collector);
    log_collector = NULL;

    KAA_TRACE_OUT(logger);
}

KAA_TEST_CASE_EX(log_callback_with_storage, on_fail_called)
{
    KAA_TRACE_IN(logger);

    kaa_error_t rc;
    int dummy_ctx;

    kaa_log_delivery_listener_t listeners = {
        NULL,
        mock_log_event_failed_fn,
        NULL,
        &dummy_ctx,
    };

    /* Notify mocks about test intentions */
    failed_call_is_expected = 1;
    expected_ctx = &dummy_ctx;
    check_bucket = 1;
    expected_bucked_id = 42;

    rc = kaa_logging_set_listeners(log_collector, &listeners);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    /* Response packet is passed internally via test reader */
    struct response_packet *response = (struct response_packet *) test_reader_buffer;
    *(uint16_t *) response->resps[RESP_FAILURE_IDX].bucket_id
            = KAA_HTONS(expected_bucked_id);

    /* Test itself */
    rc = kaa_logging_handle_server_sync(log_collector,
                                        test_reader,
                                        TEST_EXT_OP,
                                        test_filled_size
                                        );

    ASSERT_EQUAL(rc, KAA_ERR_NONE);

    /* Post-conditions check */

    ASSERT_FALSE(success_call_completed);
    ASSERT_TRUE(failed_call_completed);
    ASSERT_FALSE(timeout_call_completed);

    KAA_TRACE_OUT(logger);
}

KAA_TEST_CASE_EX(log_callback_with_storage, on_success_called)
{
    KAA_TRACE_IN(logger);

    kaa_error_t rc;
    int dummy_ctx;

    kaa_log_delivery_listener_t listeners = {
        mock_log_event_success_fn,
        NULL,
        NULL,
        &dummy_ctx,
    };

    /* Notify mocks about test intentions */
    check_bucket = 1;
    expected_bucked_id = 42;
    success_call_is_expected = 1;
    expected_ctx = &dummy_ctx;

    rc = kaa_logging_set_listeners(log_collector, &listeners);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    /* Response packet is passed internally via test reader */
    struct response_packet *response = (struct response_packet *) test_reader_buffer;
    *(uint16_t *) response->resps[RESP_SUCCESS_IDX].bucket_id
            = KAA_HTONS(expected_bucked_id);

    /* Test itself */
    rc = kaa_logging_handle_server_sync(log_collector,
                                        test_reader,
                                        TEST_EXT_OP,
                                        test_filled_size
                                        );

    ASSERT_EQUAL(rc, KAA_ERR_NONE);

    /* Post-conditions check */

    ASSERT_TRUE(success_call_completed);
    ASSERT_FALSE(failed_call_completed);
    ASSERT_FALSE(timeout_call_completed);

    KAA_TRACE_OUT(logger);
}

KAA_TEST_CASE_EX(log_callback_with_storage, on_fail_and_success_called)
{
    KAA_TRACE_IN(logger);

    kaa_error_t rc;
    int dummy_ctx;

    kaa_log_delivery_listener_t listeners = {
        mock_log_event_success_fn,
        mock_log_event_failed_fn,
        NULL,
        &dummy_ctx,
    };

    /* Notify mocks about test intentions */
    check_bucket = 1;
    expected_bucked_id = 42;
    failed_call_is_expected = 1;
    success_call_is_expected = 1;
    expected_ctx = &dummy_ctx;

    rc = kaa_logging_set_listeners(log_collector, &listeners);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    /* Response packet is passed internally via test reader */
    struct response_packet *response = (struct response_packet *) test_reader_buffer;
    *(uint16_t *) response->resps[RESP_SUCCESS_IDX].bucket_id
            = KAA_HTONS(expected_bucked_id);
    *(uint16_t *) response->resps[RESP_FAILURE_IDX].bucket_id
            = KAA_HTONS(expected_bucked_id);

    /* Test itself */
    rc = kaa_logging_handle_server_sync(log_collector,
                                        test_reader,
                                        TEST_EXT_OP,
                                        test_filled_size
                                        );

    ASSERT_EQUAL(rc, KAA_ERR_NONE);

    /* Post-conditions check */

    ASSERT_TRUE(success_call_completed);
    ASSERT_TRUE(failed_call_completed);
    ASSERT_FALSE(timeout_call_completed);

    KAA_TRACE_OUT(logger);
}

/* ---------------------------------------------------------------------------*/
/* Log delivery callback group with valid mock strategy.                      */
/* The initialized mock strategy is essential for timeouts                    */
/* ---------------------------------------------------------------------------*/

KAA_GROUP_SETUP(log_callback_with_storage_and_strategy)
{
    kaa_error_t error_code;
    size_t test_log_record_size;

    KAA_TRACE_IN(logger);
    error_code = kaa_log_collector_create(&log_collector,
                                          status,
                                          channel_manager,
                                          logger);

    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    memset(&test_strategy1, 0, sizeof(mock_strategy_context_t));
    test_strategy1.timeout = TEST_TIMEOUT;
    test_strategy1.decision = NOOP;
    test_strategy1.max_parallel_uploads = UINT32_MAX;

    kaa_log_bucket_constraints_t constraints = {
        .max_bucket_size = TEST_BUFFER_SIZE,
        .max_bucket_log_count = UINT32_MAX,
    };

    expected_ctx             = NULL;
    expected_bucked_id       = 0;
    call_is_expected         = 0;
    call_completed           = 0;
    failed_call_completed    = 0;
    failed_call_is_expected  = 0;
    success_call_completed   = 0;
    success_call_is_expected = 0;
    check_bucket             = 0;

    error_code = kaa_logging_init(log_collector, create_mock_storage(),
                                  &test_strategy1, &constraints);

    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint32_t response_count = 2;

    struct response_packet *response = (struct response_packet *) test_reader_buffer;
    *(uint32_t *) response->resp_cnt = KAA_HTONL(response_count);

    /* First response */

    /* Later on we'll override that */
    *(uint16_t *) response->resps[RESP_SUCCESS_IDX].bucket_id = 0;
    response->resps[RESP_SUCCESS_IDX].resp_code = 0; // SUCCESS

    /* Second response */

    /* Later on we'll override that */
    *(uint16_t *) response->resps[RESP_FAILURE_IDX].bucket_id = 0;
    response->resps[RESP_FAILURE_IDX].resp_code = 1; // FAILURE

    test_filled_size = sizeof(struct response_packet)
            + sizeof(struct response_chunk) * 2;

    test_log_record = kaa_test_log_record_create();
    test_log_record->data = kaa_string_copy_create(TEST_LOG_BUFFER);
    test_log_record_size = test_log_record->get_size(test_log_record);

    error_code = kaa_platform_message_reader_create(&test_reader,
                                                    test_reader_buffer,
                                                    test_filled_size);

    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(test_reader);

    error_code = kaa_platform_message_writer_create(&test_writer,
                                                    test_writer_buffer,
                                                    test_log_record_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    KAA_TRACE_OUT(logger);
}

KAA_GROUP_TEARDOWN(log_callback_with_storage_and_strategy)
{
    KAA_TRACE_IN(logger);
    test_log_record->destroy(test_log_record);

    kaa_platform_message_writer_destroy(test_writer);
    test_writer = NULL;
    kaa_platform_message_reader_destroy(test_reader);
    test_reader = NULL;

    /* Destroys mock storage as well */
    kaa_log_collector_destroy(log_collector);
    log_collector = NULL;

    KAA_TRACE_OUT(logger);
}

KAA_TEST_CASE_EX(log_callback_with_storage_and_strategy, on_timeout_called)
{
    KAA_TRACE_IN(logger);

    kaa_error_t rc;
    int dummy_ctx;

    kaa_log_delivery_listener_t listeners = {
        NULL,
        NULL,
        mock_log_event_timeout_fn,
        &dummy_ctx,
    };

    kaa_log_record_info_t bucket;

    timeout_call_is_expected = 1;
    expected_ctx = &dummy_ctx;

    rc = kaa_logging_set_listeners(log_collector, &listeners);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    rc = kaa_logging_add_record(log_collector, test_log_record, &bucket);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    /* Notify mocks about bucket */
    check_bucket = 1;
    expected_bucked_id = bucket.bucket_id;

    /* Test itself */

    rc = kaa_logging_request_serialize(log_collector, test_writer);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    sleep(TEST_TIMEOUT + 1);

    rc = kaa_logging_add_record(log_collector, test_log_record, NULL);
    ASSERT_EQUAL(KAA_ERR_NONE, rc);

    /* Post-conditions check */

    /* Timeout callback should be called once */
    ASSERT_EQUAL(1, timeout_call_completed);

    KAA_TRACE_OUT(logger);
}

/* ---------------------------------------------------------------------------*/
/* End of log delivery test groups                                            */
/* ---------------------------------------------------------------------------*/

#endif

int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger)
        return error;

    kaa_context.logger = logger;

#ifndef KAA_DISABLE_FEATURE_LOGGING
    error = kaa_status_create(&status);
    if (error || !status)
        return error;

    error = kaa_channel_manager_create(&channel_manager, &kaa_context);
    if (error || !channel_manager)
        return error;
#endif
    return 0;
}



int test_deinit(void)
{
#ifndef KAA_DISABLE_FEATURE_LOGGING
    kaa_channel_manager_destroy(channel_manager);
    kaa_status_destroy(status);
#endif
    kaa_log_destroy(logger);

    return 0;
}


KAA_SUITE_MAIN(Log, test_init, test_deinit
#ifndef KAA_DISABLE_FEATURE_LOGGING
        ,
        KAA_TEST_CASE(create_request, test_create_request)
        KAA_TEST_CASE(process_response, test_response)
        KAA_TEST_CASE(process_timeout, test_timeout)
        KAA_TEST_CASE(decline_timeout, test_decline_timeout)
        KAA_TEST_CASE(max_parallel_uploads_with_log_sync, test_max_parallel_uploads_with_log_sync)
        KAA_TEST_CASE(max_parallel_uploads_with_sync_all, test_max_parallel_uploads_with_sync_all)
        KAA_RUN_TEST(log_setters, set_strategy_invalid_parameters);
        KAA_RUN_TEST(log_setters, set_strategy_valid_parameters);
        KAA_RUN_TEST(log_setters, set_storage_invalid_parameters);
        KAA_RUN_TEST(log_setters, set_storage_valid_parameters);
        KAA_RUN_TEST(log_callback_basic, valid_parameters);
        KAA_RUN_TEST(log_callback_basic, invalid_parameters);
        KAA_RUN_TEST(log_callback_basic, valid_parameters);
        KAA_RUN_TEST(log_callback_with_storage, on_success_called);
        KAA_RUN_TEST(log_callback_with_storage, on_fail_called);
        KAA_RUN_TEST(log_callback_with_storage, on_fail_and_success_called);
        KAA_RUN_TEST(log_callback_with_storage_and_strategy, on_timeout_called);
#endif
        )

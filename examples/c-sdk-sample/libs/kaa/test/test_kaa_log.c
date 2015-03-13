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

#include <stdio.h>
#include <string.h>
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
                                                , uint32_t extension_options
                                                , size_t extension_length);
extern kaa_error_t kaa_logging_request_get_size(kaa_log_collector_t *self, size_t *expected_size);

extern kaa_error_t ext_unlimited_log_storage_create(void **log_storage_context_p, kaa_logger_t *logger);



static kaa_context_t kaa_context;
static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_channel_manager_t *channel_manager = NULL;

#define TEST_LOG_BUFFER  "log_record"



typedef struct {
    size_t timeout;
    size_t batch_size;
    bool on_timeout_count;
    bool on_failure_count;
} mock_strategy_context_t;

typedef struct {
    kaa_list_t *logs;
    size_t record_count;
    size_t total_size;
    bool on_remove_by_id_count;
    bool on_unmark_by_id_count;
} mock_storage_context_t;



/*
 * STRATEGY INTERFACE
 */
ext_log_upload_decision_t ext_log_upload_strategy_decide(void *context, const void *log_storage_context)
{
    return NOOP;
}

size_t ext_log_upload_strategy_get_bucket_size(void *context)
{
    return ((mock_strategy_context_t *)context)->batch_size;
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

void ext_log_upload_strategy_destroy(void *context)
{

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

    kaa_log_record_t *rec = KAA_MALLOC(sizeof(kaa_log_record_t *));
    *rec = *record;

    if (self->logs) {
        kaa_list_push_back(self->logs, rec);
    } else {
        self->logs = kaa_list_create(rec);
    }

    self->record_count++;
    self->total_size += rec->size;

    return KAA_ERR_NONE;
}

kaa_error_t ext_log_storage_write_next_record(void *context, char *buffer, size_t buffer_len, uint16_t bucket_id, size_t *record_len)
{
    mock_storage_context_t *self = (mock_storage_context_t *)context;
    KAA_RETURN_IF_NIL2(self, self->logs, KAA_ERR_NOT_FOUND);

    kaa_log_record_t *record = kaa_list_get_data(self->logs);

    if (buffer_len < record->size) {
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    *record_len = record->size;
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

kaa_error_t ext_log_storage_destroy(void *context)
{
    if (context) {
        kaa_list_destroy(((mock_storage_context_t *)context)->logs, &destroy_log_record);
    }
    return KAA_ERR_NONE;
}



void test_create_request()
{
    KAA_TRACE_IN(logger);

    kaa_error_t error_code;

    kaa_user_log_record_t *test_log_record = kaa_test_log_record_create();
    test_log_record->data = kaa_string_copy_create(TEST_LOG_BUFFER);
    size_t test_log_record_size = test_log_record->get_size(test_log_record);

    kaa_log_collector_t *log_collector = NULL;
    error_code = kaa_log_collector_create(&log_collector, status, channel_manager, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    mock_strategy_context_t strategy;
    memset(&strategy, 0, sizeof(mock_strategy_context_t));
    strategy.batch_size = 2 * test_log_record_size;

    mock_storage_context_t storage;
    memset(&storage, 0, sizeof(mock_storage_context_t));

    error_code = kaa_logging_init(log_collector, &storage, &strategy);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_add_record(log_collector, test_log_record);
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
    ASSERT_EQUAL(KAA_LOGGING_EXTENSION_TYPE, *buf_cursor);
    ++buf_cursor;

    char options[] = { 0x00, 0x00, 0x01 };
    ASSERT_EQUAL(memcmp(buf_cursor, options, 3), 0);
    buf_cursor += 3;

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



void test_response()
{
    KAA_TRACE_IN(logger);

    srand(time(NULL));

    kaa_error_t error_code;

    kaa_log_collector_t *log_collector = NULL;
    error_code = kaa_log_collector_create(&log_collector, status, channel_manager, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    mock_strategy_context_t strategy;
    memset(&strategy, 0, sizeof(mock_strategy_context_t));

    mock_storage_context_t storage;
    memset(&storage, 0, sizeof(mock_storage_context_t));

    error_code = kaa_logging_init(log_collector, &storage, &strategy);
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

    ASSERT_NOT_NULL(strategy.on_failure_count);
    ASSERT_NOT_NULL(storage.on_remove_by_id_count);
    ASSERT_NOT_NULL(storage.on_unmark_by_id_count);

    kaa_platform_message_reader_destroy(reader);
    kaa_log_collector_destroy(log_collector);

    KAA_TRACE_OUT(logger);
}



void test_timeout()
{
    KAA_TRACE_IN(logger);

    kaa_error_t error_code;

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
    strategy.batch_size = 2 * test_log_record_size;

    mock_storage_context_t storage;
    memset(&storage, 0, sizeof(mock_storage_context_t));

    error_code = kaa_logging_init(log_collector, &storage, &strategy);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_add_record(log_collector, test_log_record);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t request_buffer_size = 256;
    char request_buffer[request_buffer_size];
    kaa_platform_message_writer_t *writer = NULL;
    error_code = kaa_platform_message_writer_create(&writer, request_buffer, request_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_request_serialize(log_collector, writer);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    sleep(TEST_TIMEOUT + 1);

    error_code = kaa_logging_add_record(log_collector, test_log_record);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_NOT_NULL(strategy.on_timeout_count);

    test_log_record->destroy(test_log_record);
    kaa_platform_message_writer_destroy(writer);
    kaa_log_collector_destroy(log_collector);

    KAA_TRACE_OUT(logger);
}

void test_decline_timeout()
{
    KAA_TRACE_IN(logger);

    kaa_error_t error_code;

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
    strategy.batch_size = 2 * test_log_record_size;

    mock_storage_context_t storage;
    memset(&storage, 0, sizeof(mock_storage_context_t));

    error_code = kaa_logging_init(log_collector, &storage, &strategy);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_logging_add_record(log_collector, test_log_record);
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
    ASSERT_NOT_NULL(storage.on_remove_by_id_count);

    error_code = kaa_logging_add_record(log_collector, test_log_record);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_NULL(strategy.on_timeout_count);

    test_log_record->destroy(test_log_record);
    kaa_platform_message_writer_destroy(writer);
    kaa_platform_message_reader_destroy(reader);
    kaa_log_collector_destroy(log_collector);

    KAA_TRACE_OUT(logger);
}

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
#endif
        )

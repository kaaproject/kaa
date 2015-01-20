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
#include "platform/sha.h"

#include "kaa_logging.h"

#ifndef KAA_DISABLE_FEATURE_LOGGING

#include "kaa_test.h"
#include "kaa_platform_protocol.h"
#include "kaa_channel_manager.h"
#include "kaa_platform_utils.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"



extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void        kaa_status_destroy(kaa_status_t *self);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_logger_t *logger);
extern void        kaa_channel_manager_destroy(kaa_channel_manager_t *self);

extern kaa_error_t ext_log_storage_create(void **log_storage_p, kaa_logger_t *logger);

kaa_error_t ext_log_upload_strategy_by_volume_create(void **strategy_p
        , size_t max_upload_threshold, size_t max_log_bucket_size
        , size_t max_cleanup_threshold);
void ext_log_upload_strategy_by_volume_destroy(void *self);


extern kaa_error_t kaa_log_collector_create(kaa_log_collector_t ** log_collector_p
        , kaa_status_t *status, kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void        kaa_log_collector_destroy(kaa_log_collector_t *self);

extern kaa_error_t kaa_logging_request_serialize(kaa_log_collector_t *self, kaa_platform_message_writer_t *writer);
extern kaa_error_t kaa_logging_handle_server_sync(kaa_log_collector_t *self
                                                , kaa_platform_message_reader_t *reader
                                                , uint32_t extension_options
                                                , size_t extension_length);
extern kaa_error_t kaa_logging_request_get_size(kaa_log_collector_t *self, size_t *expected_size);


static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_channel_manager_t *channel_manager = NULL;
static void *log_upload_strategy = NULL;
static kaa_log_collector_t *log_collector = NULL;

#define TEST_LOG_BUFFER  "log_record"
static kaa_user_log_record_t *test_log_record;



void test_create_request()
{
    ASSERT_EQUAL(kaa_logging_add_record(log_collector, test_log_record), KAA_ERR_NONE);

    size_t expected_size = 0;
    ASSERT_EQUAL(kaa_logging_request_get_size(log_collector, &expected_size), KAA_ERR_NONE);

    char buffer[expected_size];
    kaa_platform_message_writer_t *writer = NULL;
    ASSERT_EQUAL(kaa_platform_message_writer_create(&writer, buffer, expected_size), KAA_ERR_NONE);
    ASSERT_NOT_NULL(writer);

    ASSERT_EQUAL(kaa_logging_request_serialize(log_collector, writer), KAA_ERR_NONE);
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


    size_t test_log_record_size = test_log_record->get_size(test_log_record);
    char record_buf[test_log_record_size];
    avro_writer_t avro_writer = avro_writer_memory(record_buf, test_log_record_size);
    test_log_record->serialize(avro_writer, test_log_record);
    avro_writer_free(avro_writer);

    ASSERT_EQUAL(*(uint32_t *) buf_cursor, KAA_HTONL(test_log_record_size));
    buf_cursor += sizeof(uint32_t);

    ASSERT_EQUAL(memcmp(buf_cursor, record_buf, test_log_record_size), 0);
}

void test_response()
{
    char response[] = { 0x00, 0x01, 0x00, 0x00 };
    kaa_platform_message_reader_t *reader = NULL;
    ASSERT_EQUAL(kaa_platform_message_reader_create(&reader, response, 4), KAA_ERR_NONE);
    ASSERT_NOT_NULL(reader);

    ASSERT_EQUAL(kaa_logging_handle_server_sync(log_collector, reader, 0, 4), KAA_ERR_NONE);

    kaa_platform_message_reader_destroy(reader);

}


#endif


int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger)
        return error;


#ifndef KAA_DISABLE_FEATURE_LOGGING
    error = kaa_status_create(&status);
    if (error || !status)
        return error;

    error = kaa_channel_manager_create(&channel_manager, logger);
    if (error || !channel_manager)
        return error;

    error = kaa_log_collector_create(&log_collector, status, channel_manager, logger);
    if (error || !log_collector)
        return error;

    void *storage = NULL;
    error = ext_log_storage_create(&storage, logger);
    if (error || !storage)
        return error;

    error = ext_log_upload_strategy_by_volume_create(&log_upload_strategy, 1, 1024, 0);
    if (error || !log_upload_strategy)
        return error;

    error = kaa_logging_init(log_collector, storage, log_upload_strategy);
    if (error)
        return error;

    test_log_record = kaa_test_log_record_create();
    test_log_record->data = kaa_string_move_create(TEST_LOG_BUFFER, NULL);
#endif
    return 0;
}



int test_deinit(void)
{
#ifndef KAA_DISABLE_FEATURE_LOGGING
    kaa_log_collector_destroy(log_collector);
    ext_log_upload_strategy_by_volume_destroy(log_upload_strategy);
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
#endif
        )

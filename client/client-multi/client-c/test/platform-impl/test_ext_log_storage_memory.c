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

#include <kaa_private.h>

#include <stdio.h>
#include <stdint.h>
#include <unistd.h>
#include <string.h>

#include "kaa_test.h"

#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"

#include "kaa_context.h"
#include "kaa_bootstrap_manager.h"
#include "kaa_channel_manager.h"

#include "platform/ext_log_storage.h"
#include "platform/ext_log_upload_strategy.h"

#include "kaa_logging_private.h"

#define TEST_RECORD_BUCKET_ID 1

static kaa_logger_t *logger = NULL;



void test_create_unlimited_storage(void **state)
{
    (void)state;

    kaa_error_t error_code;
    void *storage;

    error_code = ext_unlimited_log_storage_create(NULL, NULL);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_unlimited_log_storage_create(&storage, NULL);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_unlimited_log_storage_create(NULL, logger);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_unlimited_log_storage_create(&storage, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ext_log_storage_destroy(storage);
}



void test_create_limited_storage(void **state)
{
    (void)state;

    kaa_error_t error_code;
    void *storage;

    const size_t OVERFLOW_PERCENTAGE = 152;
    const size_t ALLOWED_PERCENTAGE = 99;
    const size_t ALL_LOGS_PERCENTAGE = 100;

    error_code = ext_limited_log_storage_create(NULL, NULL, 0, 0);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_limited_log_storage_create(&storage, NULL, 0, 0);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_limited_log_storage_create(&storage, logger, 0, 0);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_limited_log_storage_create(&storage, logger, SIZE_MAX, 0);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_limited_log_storage_create(&storage, logger, SIZE_MAX, OVERFLOW_PERCENTAGE);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_limited_log_storage_create(&storage, logger, 100, ALLOWED_PERCENTAGE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ext_log_storage_destroy(storage);

    error_code = ext_limited_log_storage_create(&storage, logger, 100, ALL_LOGS_PERCENTAGE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ext_log_storage_destroy(storage);
}



void test_allocate_log_record_buffer(void **state)
{
    (void)state;

    kaa_error_t error_code;
    void *storage;

    error_code = ext_unlimited_log_storage_create(&storage, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_storage_allocate_log_record_buffer(storage, NULL);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_log_record_t record1 = { NULL, 0, 0, };
    error_code = ext_log_storage_allocate_log_record_buffer(storage, &record1);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NULL(record1.data);

    kaa_log_record_t record2 = { NULL, 256, 0, };
    error_code = ext_log_storage_allocate_log_record_buffer(storage, &record2);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(record2.data);

    ext_log_storage_deallocate_log_record_buffer(storage, &record2);
    ext_log_storage_destroy(storage);
}



static char* copy_data(const char* data, size_t data_size)
{
    KAA_RETURN_IF_NIL2(data, data_size, NULL);
    char *new_data = (char *)KAA_MALLOC(data_size);
    KAA_RETURN_IF_NIL(new_data, NULL);
    memcpy(new_data, data, data_size);
    return new_data;
}

static kaa_error_t add_log_record(void *storage,
                                  const char *data,
                                  size_t data_size,
                                  uint16_t bucket_id)
{
    KAA_RETURN_IF_NIL3(storage, data, data_size, KAA_ERR_BADPARAM);
    kaa_log_record_t record = { copy_data(data, data_size), data_size, bucket_id };
    return ext_log_storage_add_log_record(storage, &record);
}

void test_add_log_record(void **state)
{
    (void)state;

    kaa_error_t error_code;
    void *storage;

    error_code = ext_unlimited_log_storage_create(&storage, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t record_count = 0;
    const char *data = "DATA";
    size_t data_size = strlen("DATA");

    error_code = add_log_record(storage, data, data_size, TEST_RECORD_BUCKET_ID);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ++record_count;

    ASSERT_EQUAL(ext_log_storage_get_records_count(storage), record_count);

    error_code = add_log_record(storage, data, data_size, TEST_RECORD_BUCKET_ID);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ++record_count;

    ASSERT_EQUAL(ext_log_storage_get_records_count(storage), record_count);
    ASSERT_EQUAL(ext_log_storage_get_total_size(storage), record_count * data_size);

    ext_log_storage_destroy(storage);
}



void test_write_next_log_record(void **state)
{
    (void)state;

    kaa_error_t error_code;
    void *storage;

    error_code = ext_unlimited_log_storage_create(&storage, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint16_t bucket_id = 0;
    size_t record_len = 0;

    error_code = ext_log_storage_write_next_record(storage, NULL, 0, 0, NULL);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_storage_write_next_record(storage, NULL, 33, 0, NULL);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_storage_write_next_record(storage, NULL, 33, &bucket_id, NULL);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_storage_write_next_record(storage, NULL, 33, &bucket_id, &record_len);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    size_t record_count = 0;
    const char *data = "DATA";
    size_t data_size = strlen("DATA");

    error_code = add_log_record(storage, data, data_size, TEST_RECORD_BUCKET_ID);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ++record_count;
    error_code = add_log_record(storage, data, data_size, TEST_RECORD_BUCKET_ID);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ++record_count;

    size_t buffer_size = (record_count + 1) * data_size;
    char buffer[buffer_size];

    error_code = ext_log_storage_write_next_record(storage, buffer, 1, &bucket_id, &record_len);
    ASSERT_EQUAL(error_code, KAA_ERR_INSUFFICIENT_BUFFER);

    error_code = ext_log_storage_write_next_record(storage, buffer, buffer_size, &bucket_id, &record_len);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(record_len, data_size);

    error_code = ext_log_storage_write_next_record(storage, buffer + record_len, buffer_size - record_len, &bucket_id, &record_len);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(record_len, data_size);

    error_code = ext_log_storage_write_next_record(storage, buffer + 2 * record_len, buffer_size - 2 * record_len, &bucket_id, &record_len);
    ASSERT_EQUAL(error_code, KAA_ERR_NOT_FOUND);

    int res = memcmp(buffer, data, data_size);
    ASSERT_EQUAL(res, 0);
    res = memcmp(buffer + data_size, data, data_size);
    ASSERT_EQUAL(res, 0);

    ext_log_storage_destroy(storage);
}



void test_remove_by_bucket_id(void **state)
{
    (void)state;

    kaa_error_t error_code;
    void *storage;

    error_code = ext_unlimited_log_storage_create(&storage, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t record_len = 0;
    size_t TEST_RECORD_COUNT = 10;
    size_t DIVIDER = TEST_RECORD_COUNT / 2;
    size_t record_count = 0;
    const char *data = "DATA";
    size_t data_size = strlen("DATA");

    while (record_count < TEST_RECORD_COUNT) {
        // Half of items with bucket #1, other half - bucket #2
        error_code = add_log_record(storage, data, data_size, record_count / DIVIDER + 1);
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);
        ++record_count;
    }

    ASSERT_EQUAL(ext_log_storage_get_records_count(storage), record_count);
    ASSERT_EQUAL(ext_log_storage_get_total_size(storage), record_count * data_size);

    size_t buffer_size = data_size;
    char buffer[buffer_size];

    uint16_t bucket_id_1 = 1;
    size_t i;
    for (i = 0; i < record_count / 2; ++i) {
        error_code = ext_log_storage_write_next_record(storage, buffer, buffer_size, &bucket_id_1, &record_len);
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    }

    ASSERT_EQUAL(ext_log_storage_get_records_count(storage), record_count / 2);
    ASSERT_EQUAL(ext_log_storage_get_total_size(storage), (record_count / 2) * data_size);

    uint16_t bucket_id_2 = 2;
    for (i = 0; i < record_count / 2; ++i) {
        error_code = ext_log_storage_write_next_record(storage, buffer, buffer_size, &bucket_id_2, &record_len);
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    }

    uint16_t bucket_id_3 = 3;
    error_code = ext_log_storage_write_next_record(storage, buffer, buffer_size, &bucket_id_3, &record_len);
    ASSERT_EQUAL(error_code, KAA_ERR_NOT_FOUND);

    error_code = ext_log_storage_remove_by_bucket_id(storage, bucket_id_2);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_storage_remove_by_bucket_id(storage, bucket_id_1);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_EQUAL(ext_log_storage_get_records_count(storage), 0);
    ASSERT_EQUAL(ext_log_storage_get_total_size(storage), 0);

    ext_log_storage_destroy(storage);
}



void test_unmark_by_bucket_id(void **state)
{
    (void)state;

    kaa_error_t error_code;
    void *storage;

    error_code = ext_unlimited_log_storage_create(&storage, logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t record_len = 0;
    size_t TEST_RECORD_COUNT = 10;
    size_t DIVIDER = TEST_RECORD_COUNT / 2;
    size_t record_count = 0;
    const char *data = "DATA";
    size_t data_size = strlen("DATA");

    while (record_count < TEST_RECORD_COUNT) {
        // Half of items with bucket #1, other half - bucket #2
        error_code = add_log_record(storage, data, data_size, record_count / DIVIDER + 1);
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);
        ++record_count;
    }

    size_t buffer_size = data_size;
    char buffer[buffer_size];

    uint16_t bucket_id_1 = 1;
    size_t i;
    for (i = 0; i < record_count / 2; ++i) {
        error_code = ext_log_storage_write_next_record(storage, buffer, buffer_size, &bucket_id_1, &record_len);
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    }

    uint16_t bucket_id_2 = 2;
    for (i = 0; i < record_count / 2; ++i) {
        error_code = ext_log_storage_write_next_record(storage, buffer, buffer_size, &bucket_id_2, &record_len);
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    }

    uint16_t bucket_id_3 = 3;
    error_code = ext_log_storage_write_next_record(storage, buffer, buffer_size, &bucket_id_3, &record_len);
    ASSERT_EQUAL(error_code, KAA_ERR_NOT_FOUND);

    error_code = ext_log_storage_remove_by_bucket_id(storage, bucket_id_2);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_storage_write_next_record(storage, buffer, buffer_size, &bucket_id_3, &record_len);
    ASSERT_EQUAL(error_code, KAA_ERR_NOT_FOUND);

    error_code = ext_log_storage_unmark_by_bucket_id(storage, bucket_id_1);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint16_t bucket_id_4 = 4;
    for (i = 0; i < record_count / 2; ++i) {
        error_code = ext_log_storage_write_next_record(storage, buffer, buffer_size, &bucket_id_4, &record_len);
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    }

    error_code = ext_log_storage_write_next_record(storage, buffer, buffer_size, &bucket_id_3, &record_len);
    ASSERT_EQUAL(error_code, KAA_ERR_NOT_FOUND);

    error_code = ext_log_storage_remove_by_bucket_id(storage, bucket_id_4);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_EQUAL(ext_log_storage_get_records_count(storage), 0);
    ASSERT_EQUAL(ext_log_storage_get_total_size(storage), 0);

    ext_log_storage_destroy(storage);
}



void test_shrink_to_size(void **state)
{
    (void)state;

    kaa_error_t error_code;
    void *storage;

    const char *data = "DATA";
    size_t data_size = strlen("DATA");
    size_t TEST_RECORD_COUNT = 10;

    size_t STORAGE_SIZE = data_size * TEST_RECORD_COUNT;
    size_t PERCENT_TO_DELETE = 75;
    size_t NEW_STORAGE_SIZE = (STORAGE_SIZE * (100 - PERCENT_TO_DELETE) / 100);

    error_code = ext_limited_log_storage_create(&storage, logger, STORAGE_SIZE, PERCENT_TO_DELETE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t record_count = 0;

    while (record_count <= TEST_RECORD_COUNT) {
        error_code = add_log_record(storage, data, data_size, TEST_RECORD_BUCKET_ID);
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);
        ++record_count;
    }

    size_t occupied_size_after_removal = 0;
    size_t record_count_after_removal = 0;
    while (occupied_size_after_removal + data_size <= NEW_STORAGE_SIZE) {
        occupied_size_after_removal += data_size;
        ++record_count_after_removal;
    }

    ASSERT_EQUAL(ext_log_storage_get_records_count(storage), record_count_after_removal + 1 /* new record added after removal */);
    ASSERT_EQUAL(ext_log_storage_get_total_size(storage), occupied_size_after_removal + data_size);

    ext_log_storage_destroy(storage);
}



int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger) {
        return error;
    }

    return 0;
}

int test_deinit(void)
{
    kaa_log_destroy(logger);
    return 0;
}



KAA_SUITE_MAIN(MetaExtension, test_init, test_deinit,
        KAA_TEST_CASE(create_unlimited_storage, test_create_unlimited_storage)
        KAA_TEST_CASE(create_limited_storage, test_create_limited_storage)
        KAA_TEST_CASE(allocate_log_record_buffer, test_allocate_log_record_buffer)
        KAA_TEST_CASE(add_log_record, test_add_log_record)
        KAA_TEST_CASE(write_next_log_record, test_write_next_log_record)
        KAA_TEST_CASE(remove_by_bucket_id, test_remove_by_bucket_id)
        KAA_TEST_CASE(unmark_by_bucket_id, test_unmark_by_bucket_id)
        KAA_TEST_CASE(shrink_to_size, test_shrink_to_size)
)

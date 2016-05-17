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
#include <stdio.h>
#include <time.h>
#include <string.h>

#include "kaa_test.h"

#include "kaa_common_schema.h"
#include "utilities/kaa_log.h"
#include "utilities/kaa_mem.h"
#include "avro_src/encoding.h"
#include "avro_src/avro/io.h"
#include "kaa_common.h"



static kaa_logger_t *logger = NULL;



static void test_string_move_create(void **state)
{
    (void)state;

    ASSERT_NULL(kaa_string_move_create(NULL, NULL));

    const char *plain_test_str1 = "test";

    kaa_string_t *kaa_str1 = kaa_string_move_create(plain_test_str1, NULL);

    ASSERT_NOT_NULL(kaa_str1);
    ASSERT_NOT_NULL(kaa_str1->data);
    ASSERT_NULL(kaa_str1->destroy);
    ASSERT_EQUAL(strcmp(kaa_str1->data, plain_test_str1), 0);

    kaa_string_destroy(kaa_str1);
    kaa_str1 = NULL;

    char *plain_test_str2 = (char *)KAA_MALLOC(strlen(plain_test_str1) + 1);
    ASSERT_NOT_NULL(plain_test_str2);
    strcpy(plain_test_str2, plain_test_str1);

    kaa_string_t *kaa_str2 = kaa_string_move_create(plain_test_str2, &kaa_data_destroy);

    ASSERT_NOT_NULL(kaa_str2);
    ASSERT_NOT_NULL(kaa_str2->data);
    ASSERT_EQUAL(kaa_str2->destroy, &kaa_data_destroy);
    ASSERT_EQUAL(strcmp(kaa_str2->data, plain_test_str1), 0);

    kaa_string_destroy(kaa_str2);
}



static void test_string_copy_create(void **state)
{
    (void)state;

    const char *plain_test_str1 = "test";

    ASSERT_NULL(kaa_string_copy_create(NULL));

    kaa_string_t *kaa_str1 = kaa_string_copy_create(plain_test_str1);
    ASSERT_NOT_NULL(kaa_str1);
    ASSERT_NOT_NULL(kaa_str1->data);
    ASSERT_EQUAL(kaa_str1->destroy, &kaa_data_destroy);
    ASSERT_EQUAL(strcmp(kaa_str1->data, plain_test_str1), 0);

    kaa_string_destroy(kaa_str1);
}



static void test_string_get_size(void **state)
{
    (void)state;

    ASSERT_EQUAL(kaa_string_get_size(NULL), 0);

    const char *plain_test_str1 = "test";
    kaa_string_t *kaa_str1 = kaa_string_copy_create(plain_test_str1);
    ASSERT_NOT_NULL(kaa_str1);

    size_t plain_test_str1_len = strlen(plain_test_str1);
    ASSERT_EQUAL(kaa_string_get_size(kaa_str1), avro_long_get_size(plain_test_str1_len) + plain_test_str1_len);

    kaa_string_destroy(kaa_str1);
}



static void test_string_serialize(void **state)
{
    (void)state;

    const char *plain_test_str1 = "test";
    kaa_string_t *kaa_str1 = kaa_string_copy_create(plain_test_str1);
    ASSERT_NOT_NULL(kaa_str1);

    size_t expected_size = kaa_string_get_size(kaa_str1);
    char auto_buffer[expected_size];
    avro_writer_t auto_avro_writer = avro_writer_memory(auto_buffer, expected_size);
    char manual_buffer[expected_size];
    avro_writer_t manual_avro_writer = avro_writer_memory(manual_buffer, expected_size);

    ASSERT_EQUAL(auto_avro_writer->buf, auto_buffer);
    ASSERT_EQUAL(auto_avro_writer->written, 0);
    ASSERT_EQUAL((size_t)auto_avro_writer->len, expected_size);

    kaa_string_serialize(auto_avro_writer, NULL);

    ASSERT_EQUAL(auto_avro_writer->buf, auto_buffer);
    ASSERT_EQUAL(auto_avro_writer->written, 0);
    ASSERT_EQUAL((size_t)auto_avro_writer->len, expected_size);

    kaa_string_t fake_kaa_str = { NULL, NULL};

    kaa_string_serialize(auto_avro_writer, &fake_kaa_str);

    ASSERT_EQUAL(auto_avro_writer->buf, auto_buffer);
    ASSERT_EQUAL(auto_avro_writer->written, 0);
    ASSERT_EQUAL((size_t)auto_avro_writer->len, expected_size);

    /*
     * REAL DATA
     */
    kaa_string_serialize(auto_avro_writer, kaa_str1);

    avro_binary_encoding.write_string(manual_avro_writer, plain_test_str1);

    ASSERT_EQUAL(memcmp(auto_buffer, manual_buffer, expected_size), 0);

    kaa_string_destroy(kaa_str1);
    avro_writer_free(manual_avro_writer);
    avro_writer_free(auto_avro_writer);
}



static void test_string_deserialize(void **state)
{
    (void)state;

    const char *plain_test_str1 = "test";
    kaa_string_t *kaa_str1 = kaa_string_copy_create(plain_test_str1);
    ASSERT_NOT_NULL(kaa_str1);

    size_t expected_size = kaa_string_get_size(kaa_str1);
    char buffer[expected_size];
    avro_writer_t avro_writer = avro_writer_memory(buffer, expected_size);

    kaa_string_serialize(avro_writer, kaa_str1);

    avro_reader_t avro_reader = avro_reader_memory(buffer, expected_size);

    kaa_string_t *kaa_str2 = kaa_string_deserialize(avro_reader);
    ASSERT_NOT_NULL(kaa_str2);

    ASSERT_EQUAL(strcmp(kaa_str2->data, plain_test_str1), 0);
    ASSERT_EQUAL(strcmp(kaa_str2->data, kaa_str1->data), 0);

    kaa_string_destroy(kaa_str2);
    avro_reader_free(avro_reader);
    avro_writer_free(avro_writer);
    kaa_string_destroy(kaa_str1);
}



static void test_bytes_move_create(void **state)
{
    (void)state;

    const uint8_t unused_bytes[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };

    ASSERT_NULL(kaa_bytes_move_create(NULL, 0, NULL));
    ASSERT_NULL(kaa_bytes_move_create(NULL, 1 + rand(), NULL));
    ASSERT_NULL(kaa_bytes_move_create(unused_bytes, 0, NULL));

    const uint8_t plain_bytes1[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };
    size_t plain_bytes1_size = sizeof(plain_bytes1) / sizeof(char);

    kaa_bytes_t *kaa_bytes1 = kaa_bytes_move_create(plain_bytes1, plain_bytes1_size, NULL);

    ASSERT_NOT_NULL(kaa_bytes1);
    ASSERT_EQUAL((size_t)kaa_bytes1->size, plain_bytes1_size);
    ASSERT_NULL(kaa_bytes1->destroy);
    ASSERT_EQUAL(memcmp(kaa_bytes1->buffer, plain_bytes1, plain_bytes1_size), 0);

    kaa_bytes_destroy(kaa_bytes1);
    kaa_bytes1 = NULL;

    uint8_t *plain_bytes2 = (uint8_t *)KAA_MALLOC(plain_bytes1_size);
    ASSERT_NOT_NULL(plain_bytes2);
    memcpy(plain_bytes2, plain_bytes1, plain_bytes1_size);

    kaa_bytes_t *kaa_bytes2 = kaa_bytes_move_create(plain_bytes2, plain_bytes1_size, &kaa_data_destroy);

    ASSERT_NOT_NULL(kaa_bytes2);
    ASSERT_EQUAL((size_t)kaa_bytes2->size, plain_bytes1_size);
    ASSERT_EQUAL(kaa_bytes2->destroy, &kaa_data_destroy);
    ASSERT_EQUAL(memcmp(kaa_bytes2->buffer, plain_bytes1, plain_bytes1_size), 0);

    kaa_bytes_destroy(kaa_bytes2);
}



static void test_bytes_copy_create(void **state)
{
    (void)state;

    const uint8_t unused_bytes[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };

    ASSERT_NULL(kaa_bytes_copy_create(NULL, 1 + rand()));
    ASSERT_NULL(kaa_bytes_copy_create(unused_bytes, 0));

    const uint8_t plain_bytes1[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };
    size_t plain_bytes1_size = sizeof(plain_bytes1) / sizeof(char);

    kaa_bytes_t *kaa_bytes1 = kaa_bytes_copy_create(plain_bytes1, plain_bytes1_size);

    ASSERT_NOT_NULL(kaa_bytes1);
    ASSERT_EQUAL((size_t)kaa_bytes1->size, plain_bytes1_size);
    ASSERT_EQUAL(kaa_bytes1->destroy, &kaa_data_destroy);
    ASSERT_EQUAL(memcmp(kaa_bytes1->buffer, plain_bytes1, plain_bytes1_size), 0);

    kaa_bytes_destroy(kaa_bytes1);
}



static void test_bytes_get_size(void **state)
{
    (void)state;

    ASSERT_EQUAL(kaa_bytes_get_size(NULL), 0);

    const uint8_t plain_bytes1[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };
    size_t plain_bytes1_size = sizeof(plain_bytes1) / sizeof(char);

    kaa_bytes_t *kaa_bytes1 = kaa_bytes_copy_create(plain_bytes1, plain_bytes1_size);
    ASSERT_EQUAL(kaa_bytes_get_size(kaa_bytes1), avro_long_get_size(kaa_bytes1->size) + kaa_bytes1->size);

    kaa_bytes_destroy(kaa_bytes1);
}



static void test_bytes_serialize(void **state)
{
    (void)state;

    const uint8_t plain_bytes1[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };
    size_t plain_bytes1_size = sizeof(plain_bytes1) / sizeof(char);

    kaa_bytes_t *kaa_bytes1 = kaa_bytes_copy_create(plain_bytes1, plain_bytes1_size);

    size_t expected_size = kaa_bytes_get_size(kaa_bytes1);
    char auto_buffer[expected_size];
    avro_writer_t auto_avro_writer = avro_writer_memory(auto_buffer, expected_size);
    char manual_buffer[expected_size];
    avro_writer_t manual_avro_writer = avro_writer_memory(manual_buffer, expected_size);

    ASSERT_EQUAL(auto_avro_writer->buf, auto_buffer);
    ASSERT_EQUAL(auto_avro_writer->written, 0);
    ASSERT_EQUAL((size_t)auto_avro_writer->len, expected_size);

    kaa_bytes_serialize(auto_avro_writer, NULL);

    ASSERT_EQUAL(auto_avro_writer->buf, auto_buffer);
    ASSERT_EQUAL(auto_avro_writer->written, 0);
    ASSERT_EQUAL((size_t)auto_avro_writer->len, expected_size);

    kaa_bytes_t fake_kaa_bytes = { NULL, 0, NULL };

    kaa_bytes_serialize(auto_avro_writer, &fake_kaa_bytes);

    ASSERT_EQUAL(auto_avro_writer->buf, auto_buffer);
    ASSERT_EQUAL(auto_avro_writer->written, 0);
    ASSERT_EQUAL((size_t)auto_avro_writer->len, expected_size);

    /*
     * REAL DATA
     */
    kaa_bytes_serialize(auto_avro_writer, kaa_bytes1);

    avro_binary_encoding.write_bytes(manual_avro_writer, (const char *)plain_bytes1, plain_bytes1_size);

    ASSERT_EQUAL(memcmp(auto_buffer, manual_buffer, expected_size), 0);

    kaa_bytes_destroy(kaa_bytes1);
    avro_writer_free(manual_avro_writer);
    avro_writer_free(auto_avro_writer);
}



static void test_bytes_deserialize(void **state)
{
    (void)state;

    const uint8_t plain_bytes1[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };
    size_t plain_bytes1_size = sizeof(plain_bytes1) / sizeof(char);

    kaa_bytes_t *kaa_bytes1 = kaa_bytes_copy_create(plain_bytes1, plain_bytes1_size);
    ASSERT_NOT_NULL(kaa_bytes1);

    size_t expected_size = kaa_bytes_get_size(kaa_bytes1);
    char buffer[expected_size];
    avro_writer_t avro_writer = avro_writer_memory(buffer, expected_size);

    kaa_bytes_serialize(avro_writer, kaa_bytes1);

    avro_reader_t avro_reader = avro_reader_memory(buffer, expected_size);

    kaa_bytes_t *kaa_bytes2 = kaa_bytes_deserialize(avro_reader);
    ASSERT_NOT_NULL(kaa_bytes2);

    ASSERT_EQUAL(memcmp(kaa_bytes2->buffer, plain_bytes1, plain_bytes1_size), 0);
    ASSERT_EQUAL(memcmp(kaa_bytes2->buffer, kaa_bytes1->buffer, plain_bytes1_size), 0);

    kaa_bytes_destroy(kaa_bytes2);
    avro_reader_free(avro_reader);
    avro_writer_free(avro_writer);
    kaa_bytes_destroy(kaa_bytes1);
}



static void test_fixed_move_create(void **state)
{
    (void)state;

    const uint8_t unused_fixed[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };

    ASSERT_NULL(kaa_fixed_move_create(NULL, 0, NULL));
    ASSERT_NULL(kaa_fixed_move_create(NULL, 1 + rand(), NULL));
    ASSERT_NULL(kaa_fixed_move_create(unused_fixed, 0, NULL));

    const uint8_t plain_fixed1[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };
    size_t plain_fixed1_size = sizeof(plain_fixed1) / sizeof(char);

    kaa_bytes_t *kaa_fixed1 = kaa_fixed_move_create(plain_fixed1, plain_fixed1_size, NULL);

    ASSERT_NOT_NULL(kaa_fixed1);
    ASSERT_EQUAL((size_t)kaa_fixed1->size, plain_fixed1_size);
    ASSERT_NULL(kaa_fixed1->destroy);
    ASSERT_EQUAL(memcmp(kaa_fixed1->buffer, plain_fixed1, plain_fixed1_size), 0);

    kaa_fixed_destroy(kaa_fixed1);
    kaa_fixed1 = NULL;

    uint8_t *plain_fixed2 = (uint8_t *)KAA_MALLOC(plain_fixed1_size);
    ASSERT_NOT_NULL(plain_fixed2);
    memcpy(plain_fixed2, plain_fixed1, plain_fixed1_size);

    kaa_bytes_t *kaa_fixed2 = kaa_fixed_move_create(plain_fixed2, plain_fixed1_size, &kaa_data_destroy);

    ASSERT_NOT_NULL(kaa_fixed2);
    ASSERT_EQUAL((size_t)kaa_fixed2->size, plain_fixed1_size);
    ASSERT_EQUAL(kaa_fixed2->destroy, &kaa_data_destroy);
    ASSERT_EQUAL(memcmp(kaa_fixed2->buffer, plain_fixed1, plain_fixed1_size), 0);

    kaa_fixed_destroy(kaa_fixed2);
}



static void test_fixed_copy_create(void **state)
{
    (void)state;

    const uint8_t unused_fixed[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };

    ASSERT_NULL(kaa_fixed_copy_create(NULL, 1 + rand()));
    ASSERT_NULL(kaa_fixed_copy_create(unused_fixed, 0));

    const uint8_t plain_fixed1[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };
    size_t plain_fixed1_size = sizeof(plain_fixed1) / sizeof(char);

    kaa_bytes_t *kaa_fixed1 = kaa_fixed_copy_create(plain_fixed1, plain_fixed1_size);

    ASSERT_NOT_NULL(kaa_fixed1);
    ASSERT_EQUAL((size_t)kaa_fixed1->size, plain_fixed1_size);
    ASSERT_EQUAL(kaa_fixed1->destroy, &kaa_data_destroy);
    ASSERT_EQUAL(memcmp(kaa_fixed1->buffer, plain_fixed1, plain_fixed1_size), 0);

    kaa_fixed_destroy(kaa_fixed1);
}



static void test_fixed_get_size(void **state)
{
    (void)state;

    ASSERT_EQUAL(kaa_fixed_get_size(NULL), 0);

    const uint8_t plain_fixed1[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };
    size_t plain_fixed1_size = sizeof(plain_fixed1) / sizeof(char);

    kaa_bytes_t *kaa_fixed1 = kaa_fixed_copy_create(plain_fixed1, plain_fixed1_size);
    ASSERT_EQUAL(kaa_fixed_get_size(kaa_fixed1), (size_t)kaa_fixed1->size);

    kaa_fixed_destroy(kaa_fixed1);
}



static void test_fixed_serialize(void **state)
{
    (void)state;

    uint8_t plain_fixed1[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };
    size_t plain_fixed1_size = sizeof(plain_fixed1) / sizeof(char);

    kaa_bytes_t *kaa_fixed1 = kaa_fixed_copy_create(plain_fixed1, plain_fixed1_size);

    size_t expected_size = kaa_fixed_get_size(kaa_fixed1);
    char auto_buffer[expected_size];
    avro_writer_t auto_avro_writer = avro_writer_memory(auto_buffer, expected_size);
    char manual_buffer[expected_size];
    avro_writer_t manual_avro_writer = avro_writer_memory(manual_buffer, expected_size);

    ASSERT_EQUAL(auto_avro_writer->buf, auto_buffer);
    ASSERT_EQUAL(auto_avro_writer->written, 0);
    ASSERT_EQUAL((size_t)auto_avro_writer->len, expected_size);

    kaa_fixed_serialize(auto_avro_writer, NULL);

    ASSERT_EQUAL(auto_avro_writer->buf, auto_buffer);
    ASSERT_EQUAL(auto_avro_writer->written, 0);
    ASSERT_EQUAL((size_t)auto_avro_writer->len, expected_size);

    kaa_bytes_t fake_kaa_fixed = { NULL, 0, NULL };

    kaa_fixed_serialize(auto_avro_writer, &fake_kaa_fixed);

    ASSERT_EQUAL(auto_avro_writer->buf, auto_buffer);
    ASSERT_EQUAL(auto_avro_writer->written, 0);
    ASSERT_EQUAL((size_t)auto_avro_writer->len, expected_size);

    /*
     * REAL DATA
     */
    kaa_fixed_serialize(auto_avro_writer, kaa_fixed1);

    avro_write(manual_avro_writer, plain_fixed1, plain_fixed1_size);

    ASSERT_EQUAL(memcmp(auto_buffer, manual_buffer, expected_size), 0);

    kaa_fixed_destroy(kaa_fixed1);
    avro_writer_free(manual_avro_writer);
    avro_writer_free(auto_avro_writer);
}



static void test_fixed_deserialize(void **state)
{
    (void)state;

    const uint8_t plain_fixed1[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };
    size_t plain_fixed1_size = sizeof(plain_fixed1) / sizeof(char);

    kaa_bytes_t *kaa_fixed1 = kaa_fixed_copy_create(plain_fixed1, plain_fixed1_size);
    ASSERT_NOT_NULL(kaa_fixed1);

    size_t expected_size = kaa_fixed_get_size(kaa_fixed1);
    char buffer[expected_size];
    avro_writer_t avro_writer = avro_writer_memory(buffer, expected_size);

    kaa_fixed_serialize(avro_writer, kaa_fixed1);

    avro_reader_t avro_reader = avro_reader_memory(buffer, expected_size);

    kaa_bytes_t *kaa_fixed2 = kaa_fixed_deserialize(avro_reader, &expected_size);
    ASSERT_NOT_NULL(kaa_fixed2);

    ASSERT_EQUAL(memcmp(kaa_fixed2->buffer, plain_fixed1, plain_fixed1_size), 0);
    ASSERT_EQUAL(memcmp(kaa_fixed2->buffer, kaa_fixed1->buffer, plain_fixed1_size), 0);

    kaa_fixed_destroy(kaa_fixed2);
    avro_reader_free(avro_reader);
    avro_writer_free(avro_writer);
    kaa_fixed_destroy(kaa_fixed1);
}



static void test_boolean_get_size(void **state)
{
    (void)state;

    srand(time(NULL));
    ASSERT_EQUAL(kaa_boolean_get_size(NULL), 0);

    int8_t boolean_value = true;
    ASSERT_EQUAL(kaa_boolean_get_size(&boolean_value), avro_long_get_size(boolean_value));
}



static void test_boolean_serialize(void **state)
{
    (void)state;

    int8_t boolean_value = true;
    size_t expected_size = kaa_boolean_get_size(&boolean_value);
    char auto_buffer[expected_size];
    avro_writer_t auto_avro_writer = avro_writer_memory(auto_buffer, expected_size);
    char manual_buffer[expected_size];
    avro_writer_t manual_avro_writer = avro_writer_memory(manual_buffer, expected_size);

    kaa_boolean_serialize(auto_avro_writer, &boolean_value);
    avro_binary_encoding.write_boolean(manual_avro_writer, boolean_value);

    ASSERT_EQUAL(memcmp(auto_buffer, manual_buffer, expected_size), 0);

    avro_writer_free(manual_avro_writer);
    avro_writer_free(auto_avro_writer);
}



static void test_boolean_deserialize(void **state)
{
    (void)state;

    int8_t boolean_value1 = true;
    size_t expected_size = kaa_boolean_get_size(&boolean_value1);
    char buffer[expected_size];
    avro_writer_t avro_writer = avro_writer_memory(buffer, expected_size);

    kaa_boolean_serialize(avro_writer, &boolean_value1);

    avro_reader_t avro_reader = avro_reader_memory(buffer, expected_size);

    int8_t *boolean_value2 = kaa_boolean_deserialize(avro_reader);

    ASSERT_EQUAL(*boolean_value2, boolean_value1);

    kaa_data_destroy(boolean_value2);
    avro_reader_free(avro_reader);
    avro_writer_free(avro_writer);
}



static void test_int_get_size(void **state)
{
    (void)state;

    srand(time(NULL));
    ASSERT_EQUAL(kaa_int_get_size(NULL), 0);

    int32_t int_value = rand();
    ASSERT_EQUAL(kaa_int_get_size(&int_value), avro_long_get_size(int_value));
}



static void test_int_serialize(void **state)
{
    (void)state;

    int32_t int_value = rand();
    size_t expected_size = kaa_int_get_size(&int_value);
    char auto_buffer[expected_size];
    avro_writer_t auto_avro_writer = avro_writer_memory(auto_buffer, expected_size);
    char manual_buffer[expected_size];
    avro_writer_t manual_avro_writer = avro_writer_memory(manual_buffer, expected_size);

    kaa_int_serialize(auto_avro_writer, &int_value);
    avro_binary_encoding.write_int(manual_avro_writer, int_value);

    ASSERT_EQUAL(memcmp(auto_buffer, manual_buffer, expected_size), 0);

    avro_writer_free(manual_avro_writer);
    avro_writer_free(auto_avro_writer);
}



static void test_int_deserialize(void **state)
{
    (void)state;

    int32_t int_value1 = rand();
    size_t expected_size = kaa_int_get_size(&int_value1);
    char buffer[expected_size];
    avro_writer_t avro_writer = avro_writer_memory(buffer, expected_size);

    kaa_int_serialize(avro_writer, &int_value1);

    avro_reader_t avro_reader = avro_reader_memory(buffer, expected_size);

    int32_t *int_value2 = kaa_int_deserialize(avro_reader);

    ASSERT_EQUAL(*int_value2, int_value1);

    kaa_data_destroy(int_value2);
    avro_reader_free(avro_reader);
    avro_writer_free(avro_writer);
}



static void test_long_get_size(void **state)
{
    (void)state;

    srand(time(NULL));
    ASSERT_EQUAL(kaa_long_get_size(NULL), 0);

    int64_t long_value = rand();
    ASSERT_EQUAL(kaa_long_get_size(&long_value), avro_long_get_size(long_value));
}



static void test_long_serialize(void **state)
{
    (void)state;

    int64_t long_value = rand();
    size_t expected_size = kaa_long_get_size(&long_value);
    char auto_buffer[expected_size];
    avro_writer_t auto_avro_writer = avro_writer_memory(auto_buffer, expected_size);
    char manual_buffer[expected_size];
    avro_writer_t manual_avro_writer = avro_writer_memory(manual_buffer, expected_size);

    kaa_long_serialize(auto_avro_writer, &long_value);
    avro_binary_encoding.write_long(manual_avro_writer, long_value);

    ASSERT_EQUAL(memcmp(auto_buffer, manual_buffer, expected_size), 0);

    avro_writer_free(manual_avro_writer);
    avro_writer_free(auto_avro_writer);
}



static void test_long_deserialize(void **state)
{
    (void)state;

    int64_t long_value1 = rand();
    size_t expected_size = kaa_long_get_size(&long_value1);
    char buffer[expected_size];
    avro_writer_t avro_writer = avro_writer_memory(buffer, expected_size);

    kaa_long_serialize(avro_writer, &long_value1);

    avro_reader_t avro_reader = avro_reader_memory(buffer, expected_size);

    int64_t *long_value2 = kaa_long_deserialize(avro_reader);

    ASSERT_EQUAL(*long_value2, long_value1);

    kaa_data_destroy(long_value2);
    avro_reader_free(avro_reader);
    avro_writer_free(avro_writer);
}



typedef enum {
    TEST_VAL_1 = 0,
    TEST_VAL_2,
    TEST_VAL_3,
    TEST_VAL_4,
    TEST_VAL_5
} test_enum_t;



static void test_enum_get_size(void **state)
{
    (void)state;

    srand(time(NULL));
    ASSERT_EQUAL(kaa_enum_get_size(NULL), 0);

    test_enum_t enum_value = (test_enum_t)rand() % TEST_VAL_5;
    ASSERT_EQUAL(kaa_enum_get_size(&enum_value), avro_long_get_size(enum_value));
}



static void test_enum_serialize(void **state)
{
    (void)state;

    test_enum_t enum_value = (test_enum_t)rand() % TEST_VAL_5;
    size_t expected_size = kaa_enum_get_size(&enum_value);
    char auto_buffer[expected_size];
    avro_writer_t auto_avro_writer = avro_writer_memory(auto_buffer, expected_size);
    char manual_buffer[expected_size];
    avro_writer_t manual_avro_writer = avro_writer_memory(manual_buffer, expected_size);

    kaa_enum_serialize(auto_avro_writer, &enum_value);
    avro_binary_encoding.write_long(manual_avro_writer, (int)enum_value);

    ASSERT_EQUAL(memcmp(auto_buffer, manual_buffer, expected_size), 0);

    avro_writer_free(manual_avro_writer);
    avro_writer_free(auto_avro_writer);
}



static void test_enum_deserialize(void **state)
{
    (void)state;

    test_enum_t enum_value1 = (test_enum_t)rand() % TEST_VAL_5;
    size_t expected_size = kaa_enum_get_size(&enum_value1);
    char buffer[expected_size];
    avro_writer_t avro_writer = avro_writer_memory(buffer, expected_size);

    kaa_enum_serialize(avro_writer, &enum_value1);

    avro_reader_t avro_reader = avro_reader_memory(buffer, expected_size);

    test_enum_t *enum_value2 = (test_enum_t *)kaa_enum_deserialize(avro_reader);

    ASSERT_EQUAL(*enum_value2, enum_value1);

    kaa_data_destroy(enum_value2);
    avro_reader_free(avro_reader);
    avro_writer_free(avro_writer);
}



static void test_float_get_size(void **state)
{
    (void)state;

    srand(time(NULL));
    ASSERT_EQUAL(kaa_float_get_size(NULL), 0);

    float float_value = rand() / rand();
    ASSERT_EQUAL(kaa_float_get_size(&float_value), AVRO_FLOAT_SIZE);
}



static void test_float_serialize(void **state)
{
    (void)state;

    float float_value = rand() / rand();
    size_t expected_size = kaa_float_get_size(&float_value);
    char auto_buffer[expected_size];
    avro_writer_t auto_avro_writer = avro_writer_memory(auto_buffer, expected_size);
    char manual_buffer[expected_size];
    avro_writer_t manual_avro_writer = avro_writer_memory(manual_buffer, expected_size);

    kaa_float_serialize(auto_avro_writer, &float_value);
    avro_binary_encoding.write_float(manual_avro_writer, float_value);

    ASSERT_EQUAL(memcmp(auto_buffer, manual_buffer, expected_size), 0);

    avro_writer_free(manual_avro_writer);
    avro_writer_free(auto_avro_writer);
}



static void test_float_deserialize(void **state)
{
    (void)state;

    float float_value1 = rand() / rand();
    size_t expected_size = kaa_float_get_size(&float_value1);
    char buffer[expected_size];
    avro_writer_t avro_writer = avro_writer_memory(buffer, expected_size);

    kaa_float_serialize(avro_writer, &float_value1);

    avro_reader_t avro_reader = avro_reader_memory(buffer, expected_size);

    float *float_value2 = kaa_float_deserialize(avro_reader);

    ASSERT_EQUAL(*float_value2, float_value1);

    kaa_data_destroy(float_value2);
    avro_reader_free(avro_reader);
    avro_writer_free(avro_writer);
}



static void test_double_get_size(void **state)
{
    (void)state;

    srand(time(NULL));
    ASSERT_EQUAL(kaa_double_get_size(NULL), 0);

    double double_value = rand() / rand();
    ASSERT_EQUAL(kaa_double_get_size(&double_value), AVRO_DOUBLE_SIZE);
}



static void test_double_serialize(void **state)
{
    (void)state;

    double double_value = rand() / rand();
    size_t expected_size = kaa_double_get_size(&double_value);
    char auto_buffer[expected_size];
    avro_writer_t auto_avro_writer = avro_writer_memory(auto_buffer, expected_size);
    char manual_buffer[expected_size];
    avro_writer_t manual_avro_writer = avro_writer_memory(manual_buffer, expected_size);

    kaa_double_serialize(auto_avro_writer, &double_value);
    avro_binary_encoding.write_double(manual_avro_writer, double_value);

    ASSERT_EQUAL(memcmp(auto_buffer, manual_buffer, expected_size), 0);

    avro_writer_free(manual_avro_writer);
    avro_writer_free(auto_avro_writer);
}



static void test_double_deserialize(void **state)
{
    (void)state;

    double double_value1 = rand() / rand();
    size_t expected_size = kaa_double_get_size(&double_value1);
    char buffer[expected_size];
    avro_writer_t avro_writer = avro_writer_memory(buffer, expected_size);

    kaa_double_serialize(avro_writer, &double_value1);

    avro_reader_t avro_reader = avro_reader_memory(buffer, expected_size);

    double *double_value2 = kaa_double_deserialize(avro_reader);

    ASSERT_EQUAL(*double_value2, double_value1);

    kaa_data_destroy(double_value2);
    avro_reader_free(avro_reader);
    avro_writer_free(avro_writer);
}



static void test_null_get_size(void **state)
{
    (void)state;

    srand(time(NULL));
    ASSERT_EQUAL(kaa_null_get_size(NULL), 0);

    ASSERT_EQUAL(kaa_null_get_size(NULL), AVRO_NULL_SIZE);
}



static void test_null_serialize(void **state)
{
    (void)state;

    size_t some_data = rand();
    size_t expected_size = rand() % 10;
    char auto_buffer[expected_size];
    avro_writer_t auto_avro_writer = avro_writer_memory(auto_buffer, expected_size);
    char manual_buffer[expected_size];
    avro_writer_t manual_avro_writer = avro_writer_memory(manual_buffer, expected_size);

    int seed = rand();
    memset(auto_buffer, seed, expected_size);
    memset(manual_buffer, seed, expected_size);

    kaa_null_serialize(auto_avro_writer, &some_data);
    avro_binary_encoding.write_null(manual_avro_writer);

    ASSERT_EQUAL(memcmp(auto_buffer, manual_buffer, expected_size), 0);

    avro_writer_free(manual_avro_writer);
    avro_writer_free(auto_avro_writer);
}



static void test_null_deserialize(void **state)
{
    (void)state;

    size_t expected_size = rand() % 10;
    char buffer[expected_size];
    memset(buffer, rand(), expected_size);
    avro_reader_t avro_reader = avro_reader_memory(buffer, expected_size);

    ASSERT_NULL(kaa_null_deserialize(avro_reader));

    avro_reader_free(avro_reader);
}



static void test_array_get_size(void **state)
{
    (void)state;

    srand(time(NULL));
    ASSERT_EQUAL(kaa_array_get_size(NULL, NULL), 0);
    ASSERT_EQUAL(kaa_array_get_size(NULL, (get_size_fn)kaa_null_get_size), avro_long_get_size(0));

    const char *plain_str = "data";
    kaa_string_t *reference_kaa_str = kaa_string_copy_create(plain_str);
    size_t element_size = kaa_string_get_size(reference_kaa_str);
    kaa_string_destroy(reference_kaa_str);

    size_t array_size = 1 + rand() % 10;

    kaa_list_t *avro_array = kaa_list_create();
    size_t i = 0;
    for (i = 0; i < array_size; ++i) {
        kaa_list_push_back(avro_array, kaa_string_copy_create(plain_str));
    }

    size_t expected_size = avro_long_get_size(array_size)
                         + array_size * element_size
                         + avro_long_get_size(0);

    ASSERT_EQUAL(kaa_array_get_size(avro_array, &kaa_string_get_size), expected_size);

    kaa_list_destroy(avro_array, kaa_string_destroy);
}



static void test_null_array_serialize(void **state)
{
    (void)state;

    size_t empty_array_buffer_size = 1;
    char empty_array_buffer[empty_array_buffer_size];
    memset(empty_array_buffer, 1 + rand(), empty_array_buffer_size);

    avro_writer_t avro_writer = avro_writer_memory(empty_array_buffer, empty_array_buffer_size);

    kaa_array_serialize(avro_writer, NULL, NULL);

    ASSERT_EQUAL((int)empty_array_buffer[0], 0);

    avro_writer_free(avro_writer);
}



static void test_empty_array_serialize(void **state)
{
    (void)state;

    size_t empty_array_buffer_size = 1;
    char empty_array_buffer[empty_array_buffer_size];
    memset(empty_array_buffer, 1 + rand(), empty_array_buffer_size);

    avro_writer_t avro_writer = avro_writer_memory(empty_array_buffer, empty_array_buffer_size);
    kaa_list_t *empty_array = kaa_list_create();

    kaa_array_serialize(avro_writer, empty_array, NULL);

    ASSERT_EQUAL((int)empty_array_buffer[0], 0);

    kaa_list_destroy(empty_array, NULL);
    avro_writer_free(avro_writer);
}



static void test_array_serialize(void **state)
{
    (void)state;

    const char *plain_str = "data";
    kaa_string_t *reference_kaa_str = kaa_string_copy_create(plain_str);
    size_t element_size = kaa_string_get_size(reference_kaa_str);
    kaa_string_destroy(reference_kaa_str);

    size_t array_size = 1 + rand() % 10;
    size_t expected_size = avro_long_get_size(array_size)
                         + array_size * element_size
                         + avro_long_get_size(0);
    char manual_buffer[expected_size];
    avro_writer_t manual_avro_writer = avro_writer_memory(manual_buffer, expected_size);
    avro_binary_encoding.write_long(manual_avro_writer, array_size);

    kaa_list_t *avro_array = kaa_list_create();
    size_t i = 0;
    for (i = 0; i < array_size; ++i) {
        kaa_string_t *array_data = kaa_string_copy_create(plain_str);
        kaa_list_push_back(avro_array, array_data);
        avro_binary_encoding.write_string(manual_avro_writer, array_data->data);
    }

    avro_binary_encoding.write_long(manual_avro_writer, 0);

    size_t actual_size = kaa_array_get_size(avro_array, kaa_string_get_size);

    ASSERT_EQUAL(actual_size, expected_size);

    char auto_buffer[actual_size];
    avro_writer_t auto_avro_writer = avro_writer_memory(auto_buffer, actual_size);

    kaa_array_serialize(auto_avro_writer, avro_array, kaa_string_serialize);

    ASSERT_EQUAL(memcmp(auto_buffer, manual_buffer, expected_size), 0);

    kaa_list_destroy(avro_array, kaa_string_destroy);
    avro_writer_free(manual_avro_writer);
    avro_writer_free(auto_avro_writer);
}



static float *create_float(void)
{
    float *float_value = (float *)KAA_MALLOC(sizeof(float));
    KAA_RETURN_IF_NIL(float_value, NULL);
    *float_value = rand() % rand();
    return float_value;
}

static void test_array_deserialize_wo_ctx(void **state)
{
    (void)state;

    size_t array_size = 1 + rand() % 10;

    kaa_list_t *avro_array1 = kaa_list_create();
    size_t i = 0;
    for (i = 0; i < array_size; ++i) {
        kaa_list_push_back(avro_array1, create_float());
    }

    size_t buffer_size = kaa_array_get_size(avro_array1, kaa_float_get_size);
    char buffer[buffer_size];
    avro_writer_t avro_writer = avro_writer_memory(buffer, buffer_size);

    kaa_array_serialize(avro_writer, avro_array1, kaa_float_serialize);

    avro_reader_t avro_reader = avro_reader_memory(buffer, buffer_size);
    kaa_list_t *avro_array2 = kaa_array_deserialize_wo_ctx(avro_reader, (deserialize_wo_ctx_fn)kaa_float_deserialize);

    ASSERT_NOT_NULL(avro_array2);
    ASSERT_EQUAL(kaa_list_get_size(avro_array2), array_size);

//    /*
//     * Deserialized array has a back order, so reverse the original.
//     */
//    kaa_list_t *reverse_avro_array = NULL;
//    kaa_list_t *it = avro_array1;
//    while (it) {
//        if (reverse_avro_array) {
//            reverse_avro_array = kaa_list_push_front(reverse_avro_array, kaa_list_get_data(it));
//        } else {
//            reverse_avro_array = kaa_list_create(kaa_list_get_data(it));
//        }
//        it  = kaa_list_next(it);
//    }
//
//    kaa_list_destroy(avro_array1, kaa_null_destroy);
//    avro_array1 = reverse_avro_array;

    /*
     * Compare origin and deserialized arrays.
     */
    kaa_list_node_t *it1 = kaa_list_begin(avro_array1);
    kaa_list_node_t *it2 = kaa_list_begin(avro_array2);
    while (it1 && it2) {
        float *float_value1 = kaa_list_get_data(it1);
        float *float_value2 = kaa_list_get_data(it2);

        ASSERT_NOT_NULL(float_value1);
        ASSERT_NOT_NULL(float_value2);
        ASSERT_EQUAL(*float_value2, *float_value1);

        it1  = kaa_list_next(it1);
        it2  = kaa_list_next(it2);
    }

    kaa_list_destroy(avro_array2, kaa_data_destroy);
    avro_reader_free(avro_reader);
    avro_writer_free(avro_writer);
    kaa_list_destroy(avro_array1, kaa_data_destroy);
}



static void test_array_deserialize_w_ctx(void **state)
{
    (void)state;

    const uint8_t plain_fixed[] = { 0x0, 0x1, 0x2, 0x3, 0x4 };
    size_t plain_fixed_size = sizeof(plain_fixed) / sizeof(char);

    size_t array_size = 1 + rand() % 10;

    kaa_list_t *avro_array1 = kaa_list_create();
    size_t i = 0;
    for (i = 0; i < array_size; ++i) {
        kaa_list_push_back(avro_array1, kaa_fixed_copy_create(plain_fixed, plain_fixed_size));
    }

    size_t buffer_size = kaa_array_get_size(avro_array1, kaa_fixed_get_size);
    char buffer[buffer_size];
    avro_writer_t avro_writer = avro_writer_memory(buffer, buffer_size);

    kaa_array_serialize(avro_writer, avro_array1, kaa_fixed_serialize);

    avro_reader_t avro_reader = avro_reader_memory(buffer, buffer_size);
    kaa_list_t *avro_array2 = kaa_array_deserialize_w_ctx(avro_reader, (deserialize_w_ctx_fn)kaa_fixed_deserialize, &plain_fixed_size);

    ASSERT_NOT_NULL(avro_array2);
    ASSERT_EQUAL(kaa_list_get_size(avro_array2), array_size);

    kaa_list_node_t *it = kaa_list_begin(avro_array2);
    while (it) {
        kaa_bytes_t *fixed = kaa_list_get_data(it);
        ASSERT_NOT_NULL(fixed);
        ASSERT_EQUAL((size_t)fixed->size, plain_fixed_size);
        ASSERT_EQUAL(memcmp(fixed->buffer, plain_fixed, plain_fixed_size), 0);
        it  = kaa_list_next(it);
    }

    kaa_list_destroy(avro_array2, kaa_fixed_destroy);
    avro_reader_free(avro_reader);
    avro_writer_free(avro_writer);
    kaa_list_destroy(avro_array1, kaa_fixed_destroy);
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

KAA_SUITE_MAIN(Log, test_init, test_deinit
       ,
       KAA_TEST_CASE(string_move_create, test_string_move_create)
       KAA_TEST_CASE(string_copy_create, test_string_copy_create)
       KAA_TEST_CASE(string_get_size, test_string_get_size)
       KAA_TEST_CASE(string_serialize, test_string_serialize)
       KAA_TEST_CASE(string_deserialize, test_string_deserialize)

       KAA_TEST_CASE(bytes_move_create, test_bytes_move_create)
       KAA_TEST_CASE(bytes_copy_create, test_bytes_copy_create)
       KAA_TEST_CASE(bytes_get_size, test_bytes_get_size)
       KAA_TEST_CASE(bytes_serialize, test_bytes_serialize)
       KAA_TEST_CASE(bytes_deserialize, test_bytes_deserialize)

       KAA_TEST_CASE(fixed_move_create, test_fixed_move_create)
       KAA_TEST_CASE(fixed_copy_create, test_fixed_copy_create)
       KAA_TEST_CASE(fixed_get_size, test_fixed_get_size)
       KAA_TEST_CASE(fixed_serialize, test_fixed_serialize)
       KAA_TEST_CASE(fixed_deserialize, test_fixed_deserialize)

       KAA_TEST_CASE(boolean_get_size, test_boolean_get_size)
       KAA_TEST_CASE(boolean_serialize, test_boolean_serialize)
       KAA_TEST_CASE(boolean_deserialize, test_boolean_deserialize)

       KAA_TEST_CASE(int_get_size, test_int_get_size)
       KAA_TEST_CASE(int_serialize, test_int_serialize)
       KAA_TEST_CASE(int_deserialize, test_int_deserialize)

       KAA_TEST_CASE(long_get_size, test_long_get_size)
       KAA_TEST_CASE(long_serialize, test_long_serialize)
       KAA_TEST_CASE(long_deserialize, test_long_deserialize)

       KAA_TEST_CASE(enum_get_size, test_enum_get_size)
       KAA_TEST_CASE(enum_serialize, test_enum_serialize)
       KAA_TEST_CASE(enum_deserialize, test_enum_deserialize)

       KAA_TEST_CASE(float_get_size, test_float_get_size)
       KAA_TEST_CASE(float_serialize, test_float_serialize)
       KAA_TEST_CASE(float_deserialize, test_float_deserialize)

       KAA_TEST_CASE(double_get_size, test_double_get_size)
       KAA_TEST_CASE(double_serialize, test_double_serialize)
       KAA_TEST_CASE(double_deserialize, test_double_deserialize)

       KAA_TEST_CASE(null_get_size, test_null_get_size)
       KAA_TEST_CASE(null_serialize, test_null_serialize)
       KAA_TEST_CASE(null_deserialize, test_null_deserialize)

       KAA_TEST_CASE(array_get_size, test_array_get_size)
       KAA_TEST_CASE(null_array_serialize, test_null_array_serialize)
       KAA_TEST_CASE(empty_array_serialize, test_empty_array_serialize)
       KAA_TEST_CASE(array_serialize, test_array_serialize)
       KAA_TEST_CASE(array_deserialize_wo_ctx, test_array_deserialize_wo_ctx)
       KAA_TEST_CASE(array_deserialize_w_ctx, test_array_deserialize_w_ctx)
        )

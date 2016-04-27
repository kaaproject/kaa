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

#include <stdbool.h>
#include <sys/types.h>
#include <stdint.h>
#include "kaa_test.h"

#include <stddef.h>
#include <string.h>

#include "kaa_platform_utils.h"
#include "utilities/kaa_mem.h"

void test_get_aligned_size(void **state)
{
    (void)state;
    ASSERT_EQUAL(KAA_ALIGNMENT, kaa_aligned_size_get(KAA_ALIGNMENT));
    ASSERT_EQUAL(KAA_ALIGNMENT, kaa_aligned_size_get(KAA_ALIGNMENT - 1));
    ASSERT_EQUAL(2 * KAA_ALIGNMENT, kaa_aligned_size_get(KAA_ALIGNMENT + 1));
}

void test_create_destroy_writer(void **state)
{
    (void)state;
    kaa_platform_message_writer_t *writer = NULL;
    uint8_t buffer[16];
    size_t buffer_size = sizeof(buffer) / sizeof(char);

    kaa_error_t error_code = kaa_platform_message_writer_create(NULL, buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    ASSERT_NULL(writer);

    error_code = kaa_platform_message_writer_create(&writer, NULL, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    ASSERT_NULL(writer);

    error_code = kaa_platform_message_writer_create(&writer, buffer, 0);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    ASSERT_NULL(writer);

    error_code = kaa_platform_message_writer_create(&writer, buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(writer);
    ASSERT_EQUAL((size_t)(writer->end - writer->begin), buffer_size);
    ASSERT_EQUAL(writer->begin, writer->current);

    kaa_platform_message_writer_destroy(writer);
}

void test_write(void **state)
{
    (void)state;
    uint8_t buffer[16];
    size_t buffer_size = sizeof(buffer) / sizeof(char);
    kaa_platform_message_writer_t *writer = NULL;

    kaa_error_t error_code = kaa_platform_message_writer_create(&writer, buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    const char *test_data = "test data";
    size_t test_data_len = strlen(test_data);

    error_code = kaa_platform_message_write(writer, test_data, test_data_len);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL((size_t)(writer->current - writer->begin), test_data_len);

    error_code = (memcmp(writer->begin, test_data, test_data_len) == 0 ? KAA_ERR_NONE : KAA_ERR_WRITE_FAILED);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_platform_message_writer_destroy(writer);
}

void test_aligned_write(void **state)
{
    (void)state;
    uint8_t buffer[3 * KAA_ALIGNMENT];
    size_t buffer_size = sizeof(buffer) / sizeof(char);
    kaa_platform_message_writer_t *writer = NULL;

    kaa_error_t error_code = kaa_platform_message_writer_create(&writer, buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    char *match_alignment_data[KAA_ALIGNMENT];
    size_t match_alignment_data_len = KAA_ALIGNMENT;

    char *unmatch_alignment_data[KAA_ALIGNMENT + 1];
    size_t unmatch_alignment_data_len = KAA_ALIGNMENT + 1;

    error_code = kaa_platform_message_write_aligned(writer, unmatch_alignment_data, unmatch_alignment_data_len);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL((size_t)(writer->current - writer->begin), kaa_aligned_size_get(unmatch_alignment_data_len));
    error_code = (memcmp(writer->begin, unmatch_alignment_data, unmatch_alignment_data_len) == 0 ? KAA_ERR_NONE : KAA_ERR_WRITE_FAILED);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t i = 0;
    size_t padding = kaa_aligned_size_get(unmatch_alignment_data_len) - unmatch_alignment_data_len;
    for (; i < padding; ++i) {
        if (*(writer->begin + unmatch_alignment_data_len + i) != 0) {
            error_code = KAA_ERR_WRITE_FAILED;
        }
    }
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint8_t *begin = writer->current;
    error_code = kaa_platform_message_write_aligned(writer, match_alignment_data, match_alignment_data_len);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL((size_t)(writer->current - begin), kaa_aligned_size_get(match_alignment_data_len));
    error_code = (memcmp(begin, match_alignment_data, kaa_aligned_size_get(match_alignment_data_len)) == 0 ? KAA_ERR_NONE : KAA_ERR_WRITE_FAILED);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_platform_message_writer_destroy(writer);
}

void test_write_buffer_overflow(void **state)
{
    (void)state;
    uint8_t buffer[2 * KAA_ALIGNMENT];
    size_t buffer_size = sizeof(buffer) / sizeof(char);
    kaa_platform_message_writer_t *writer = NULL;

    kaa_error_t error_code = kaa_platform_message_writer_create(&writer, buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint8_t *data[KAA_ALIGNMENT];
    size_t data_len = KAA_ALIGNMENT;

    error_code = kaa_platform_message_write_aligned(writer, data, data_len);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = kaa_platform_message_write_aligned(writer, data, data_len);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = kaa_platform_message_write_aligned(writer, data, data_len);
    ASSERT_EQUAL(error_code, KAA_ERR_WRITE_FAILED);

    kaa_platform_message_writer_destroy(writer);
}

void test_write_protocol_message_header(void **state)
{
    (void)state;
    uint8_t buffer[KAA_PROTOCOL_ID_SIZE + KAA_PROTOCOL_VERSION_SIZE];
    size_t buffer_size = sizeof(buffer) / sizeof(char);
    kaa_platform_message_writer_t *writer = NULL;

    const uint8_t serialized_header[KAA_PROTOCOL_ID_SIZE + KAA_PROTOCOL_VERSION_SIZE] = {0x00, 0x00, 0x30, 0x39, 0x01, 0x00};
    uint32_t protocol_id = 12345;
    uint16_t protocol_version = 256;

    kaa_error_t error_code = kaa_platform_message_writer_create(&writer, buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_message_header_write(writer, protocol_id, protocol_version);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = (memcmp(writer->begin, serialized_header, KAA_PROTOCOL_ID_SIZE + KAA_PROTOCOL_VERSION_SIZE) == 0? KAA_ERR_NONE : KAA_ERR_WRITE_FAILED);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_platform_message_writer_destroy(writer);
}

void test_write_extension_header(void **state)
{
    (void)state;
    uint8_t buffer[KAA_EXTENSION_HEADER_SIZE];
    size_t buffer_size = sizeof(buffer) / sizeof(char);
    kaa_platform_message_writer_t *writer = NULL;

    const uint8_t serialized_header[KAA_EXTENSION_HEADER_SIZE] = {0x00, 0xfa, 0x11, 0x12, 0xaa, 0xbb, 0xcc, 0xff};
    uint16_t extension_type = 250;
    uint16_t extension_options = (0x11 << 8) | 0x12;
    uint32_t extension_payload_length = 0xaabbccff;

    kaa_error_t error_code = kaa_platform_message_writer_create(&writer, buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_message_write_extension_header(
            writer, extension_type, extension_options, extension_payload_length);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = (memcmp(writer->begin, serialized_header, KAA_EXTENSION_HEADER_SIZE) == 0? KAA_ERR_NONE : KAA_ERR_WRITE_FAILED);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_platform_message_writer_destroy(writer);
}

void test_create_destroy_reader(void **state)
{
    (void)state;
    kaa_platform_message_reader_t *reader = NULL;
    uint8_t buffer[16];
    size_t buffer_size = sizeof(buffer) / sizeof(char);

    kaa_error_t error_code = kaa_platform_message_reader_create(NULL, buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    ASSERT_NULL(reader);

    error_code = kaa_platform_message_reader_create(&reader, NULL, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    ASSERT_NULL(reader);

    error_code = kaa_platform_message_reader_create(&reader, buffer, 0);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    ASSERT_NULL(reader);

    error_code = kaa_platform_message_reader_create(&reader, buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(reader);
    ASSERT_EQUAL((size_t)(reader->end - reader->begin), buffer_size);
    ASSERT_EQUAL(reader->begin, reader->current);

    kaa_platform_message_reader_destroy(reader);
}

void test_read(void **state)
{
    (void)state;
    kaa_platform_message_reader_t *reader = NULL;
    uint8_t write_buffer[16];
    uint8_t read_buffer[16];
    size_t buffer_size = sizeof(write_buffer) / sizeof(char);

    const char *serialized_data = "big serialized data";
    memcpy(write_buffer, serialized_data, buffer_size);

    kaa_error_t error_code = kaa_platform_message_reader_create(&reader, write_buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t sub_buffer_size = buffer_size / 2;
    error_code = kaa_platform_message_read(reader, read_buffer, sub_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = (memcmp(read_buffer, serialized_data, sub_buffer_size) == 0 ? KAA_ERR_NONE : KAA_ERR_READ_FAILED);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_message_read(reader, read_buffer + sub_buffer_size, sub_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = (memcmp(read_buffer + sub_buffer_size, serialized_data + sub_buffer_size, sub_buffer_size) == 0 ? KAA_ERR_NONE : KAA_ERR_READ_FAILED);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_platform_message_reader_destroy(reader);
}

void test_read_aligned(void **state)
{
    (void)state;
    kaa_platform_message_reader_t *reader = NULL;
    uint8_t write_buffer[3 * KAA_ALIGNMENT];
    uint8_t read_buffer[2 * KAA_ALIGNMENT];
    size_t buffer_size = sizeof(write_buffer) / sizeof(char);

    kaa_error_t error_code = kaa_platform_message_reader_create(&reader, write_buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_message_read_aligned(reader, read_buffer, KAA_ALIGNMENT + 1);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL((reader->current - reader->begin), 2 * KAA_ALIGNMENT);

    const uint8_t *begin = reader->current;
    error_code = kaa_platform_message_read_aligned(reader, read_buffer, KAA_ALIGNMENT);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL((reader->current - begin), KAA_ALIGNMENT);

    kaa_platform_message_reader_destroy(reader);
}

void test_read_eof(void **state)
{
    (void)state;
    kaa_platform_message_reader_t *reader = NULL;
    uint8_t write_buffer[2 * KAA_ALIGNMENT];
    uint8_t read_buffer[KAA_ALIGNMENT];
    size_t buffer_size = sizeof(write_buffer) / sizeof(char);

    kaa_error_t error_code = kaa_platform_message_reader_create(&reader, write_buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_message_read(reader, read_buffer, KAA_ALIGNMENT);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = kaa_platform_message_read(reader, read_buffer, KAA_ALIGNMENT);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_message_read(reader, read_buffer, KAA_ALIGNMENT);
    ASSERT_EQUAL(error_code, KAA_ERR_READ_FAILED);

    kaa_platform_message_reader_destroy(reader);
}

void test_read_protocol_message_header(void **state)
{
    (void)state;
    kaa_platform_message_reader_t *reader = NULL;

    const uint8_t serialized_header[KAA_PROTOCOL_MESSAGE_HEADER_SIZE] = {0x00, 0x00, 0x30, 0x39, 0x01, 0x00, 0x00, 0x05};

    kaa_error_t error_code = kaa_platform_message_reader_create(&reader, serialized_header, KAA_PROTOCOL_MESSAGE_HEADER_SIZE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    const uint32_t protocol_id = 12345;
    const uint16_t protocol_version = 256;
    const uint16_t extension_count = 5;

    uint32_t read_protocol_id = 0;
    uint16_t read_protocol_version = 0;
    uint16_t read_extension_count = 0;

    error_code = kaa_platform_message_header_read(reader, &read_protocol_id, &read_protocol_version, &read_extension_count);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_EQUAL(read_protocol_id, protocol_id);
    ASSERT_EQUAL(read_protocol_version, protocol_version);
    ASSERT_EQUAL(read_extension_count, extension_count);

    kaa_platform_message_reader_destroy(reader);
}

void test_read_extension_header(void **state)
{
    (void)state;
    kaa_platform_message_reader_t *reader = NULL;

    const uint8_t serialized_header[KAA_EXTENSION_HEADER_SIZE] = {0x00, 0xfa, 0x11, 0x12, 0xaa, 0xbb, 0xcc, 0xff};

    kaa_error_t error_code = kaa_platform_message_reader_create(&reader, serialized_header,
            KAA_PROTOCOL_MESSAGE_HEADER_SIZE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    const uint16_t extension_type = 250;
    const uint16_t extension_options = (0x11 << 8) | 0x12;
    const uint32_t extension_payload_length = 0xaabbccff;

    uint16_t read_extension_type = 0;
    uint16_t read_extension_options = 0;
    uint32_t read_extension_payload_length = 0;

    error_code = kaa_platform_message_read_extension_header(
            reader, &read_extension_type, &read_extension_options, &read_extension_payload_length);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_EQUAL(read_extension_type, extension_type);
    ASSERT_EQUAL(read_extension_options, extension_options);
    ASSERT_EQUAL(read_extension_payload_length, extension_payload_length);

    kaa_platform_message_reader_destroy(reader);
}

KAA_SUITE_MAIN(PlatformUtils, NULL, NULL
        ,
        KAA_TEST_CASE(get_aligned_size, test_get_aligned_size)
        KAA_TEST_CASE(create_destroy_writer, test_create_destroy_writer)
        KAA_TEST_CASE(raw_write, test_write)
        KAA_TEST_CASE(raw_aligned_write, test_aligned_write)
        KAA_TEST_CASE(buffer_overflow_write, test_write_buffer_overflow)
        KAA_TEST_CASE(write_protocol_message_header, test_write_protocol_message_header)
        KAA_TEST_CASE(write_extension_header, test_write_extension_header)
        KAA_TEST_CASE(create_destroy_writer, test_create_destroy_reader)
        KAA_TEST_CASE(raw_read, test_read)
        KAA_TEST_CASE(raw_read_aligned, test_read_aligned)
        KAA_TEST_CASE(read_eof, test_read_eof)
        KAA_TEST_CASE(read_protocol_message_header, test_read_protocol_message_header)
        KAA_TEST_CASE(read_extension_header, test_read_extension_header)
)


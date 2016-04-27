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
#include <stdbool.h>
#include <string.h>
#include <stdio.h>

#include "platform/ext_sha.h"
#include "kaa_event.h"
#ifndef KAA_DISABLE_FEATURE_EVENTS

#include "kaa_test.h"

#include "kaa_context.h"
#include "utilities/kaa_log.h"
#include "utilities/kaa_mem.h"
#include "kaa_status.h"
#include "kaa_channel_manager.h"
#include "kaa_platform_utils.h"
#include "platform/sock.h"

#include "kaa_private.h"


static int global_events_counter = 0;
static int specific_events_counter = 0;

static kaa_context_t kaa_context;
static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_channel_manager_t *channel_manager = NULL;
static kaa_event_manager_t *event_manager = NULL;



static int test_init(void);
static int test_deinit(void);



void test_kaa_create_event_manager(void **state)
{
    (void)state;

    kaa_status_t* status = NULL;
    kaa_status_create(&status);
    kaa_event_manager_t *event_manager = NULL;
    kaa_error_t err_code = kaa_event_manager_create(&event_manager, status, NULL, logger);
    ASSERT_EQUAL(err_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(event_manager);
    kaa_event_manager_destroy(event_manager);

    KAA_FREE(status);
}

static kaa_endpoint_id endpoint_id1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
static kaa_endpoint_id endpoint_id2 = { 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40 };
static bool is_event_listeners_cb_called = false;

static kaa_error_t event_listeners_callback(void *context, const kaa_endpoint_id listeners[], size_t listeners_count)
{
    (void)context;
    ASSERT_EQUAL(listeners_count, 2);
    ASSERT_EQUAL(memcmp(listeners[0], endpoint_id1, KAA_ENDPOINT_ID_LENGTH), 0);
    ASSERT_EQUAL(memcmp(listeners[1], endpoint_id2, KAA_ENDPOINT_ID_LENGTH), 0);
    is_event_listeners_cb_called = true;
    return KAA_ERR_NONE;
}

static kaa_error_t event_listeners_request_failed(void *context)
{
    (void)context;
    return KAA_ERR_NONE;
}

void test_kaa_event_listeners_serialize_request(void **state)
{
    (void)state;

    size_t expected_size = KAA_EXTENSION_HEADER_SIZE + sizeof(uint32_t); // header + field id + reserved + listeners count
    expected_size += sizeof(uint32_t); // request id + fqns count
    expected_size += 2 * sizeof(uint32_t); // fqn length + reserved

    const char *fqns[] = { "test.fqn1", "test.fqn2" };
    const size_t fqn1_len = strlen(fqns[0]);
    const size_t fqn2_len = strlen(fqns[1]);

    expected_size += kaa_aligned_size_get(fqn1_len) + kaa_aligned_size_get(fqn2_len);

    kaa_event_listeners_callback_t callback = { NULL, NULL, NULL };
    ASSERT_NOT_EQUAL(kaa_event_manager_find_event_listeners(event_manager, fqns, 2, &callback), KAA_ERR_NONE);

    callback = (kaa_event_listeners_callback_t) { NULL, &event_listeners_callback, &event_listeners_request_failed };
    ASSERT_EQUAL(kaa_event_manager_find_event_listeners(event_manager, fqns, 2, &callback), KAA_ERR_NONE);

    size_t actual_size = 0;
    ASSERT_EQUAL(kaa_event_request_get_size(event_manager, &actual_size), KAA_ERR_NONE);
    ASSERT_EQUAL(actual_size, expected_size);

    uint8_t buffer[actual_size];
    kaa_platform_message_writer_t *writer;
    ASSERT_EQUAL(kaa_platform_message_writer_create(&writer, buffer, actual_size), KAA_ERR_NONE);

    ASSERT_EQUAL(kaa_event_request_serialize(event_manager, 1, writer), KAA_ERR_NONE);

    uint8_t *cursor = buffer + KAA_EXTENSION_HEADER_SIZE;
    ASSERT_EQUAL((*(uint8_t *) cursor), 0); // verifying field id 0
    cursor += sizeof(uint16_t); // skipping field id + reserved

    ASSERT_EQUAL(KAA_NTOHS(*(uint16_t *) cursor), 1); // verifying listeners count = 1
    cursor += sizeof(uint16_t);

    ASSERT_EQUAL(KAA_NTOHS(*(uint16_t *) cursor), 1); // verifying request id = 1
    cursor += sizeof(uint16_t);

    ASSERT_EQUAL(KAA_NTOHS(*(uint16_t *) cursor), 2); // verifying FQNs count = 2
    cursor += sizeof(uint16_t);

    ASSERT_EQUAL(KAA_NTOHS(*(uint16_t *) cursor), fqn1_len); // verifying FQN 1 length
    cursor += sizeof(uint32_t); // skipping FQN length + reserved

    ASSERT_EQUAL(memcmp(cursor, fqns[0], fqn1_len), 0); // verifying FQN 1
    cursor += kaa_aligned_size_get(fqn1_len);

    ASSERT_EQUAL(KAA_NTOHS(*(uint16_t *) cursor), fqn2_len); // verifying FQN 2 length
    cursor += sizeof(uint32_t); // skipping FQN length + reserved

    ASSERT_EQUAL(memcmp(cursor, fqns[1], fqn2_len), 0); // verifying FQN 2
    cursor += kaa_aligned_size_get(fqn2_len);

    ASSERT_EQUAL(cursor, buffer + actual_size);

    kaa_platform_message_writer_destroy(writer);
}

void test_kaa_event_listeners_handle_sync(void **state)
{
    (void)state;

    const uint32_t extension_size = 52;
    uint8_t buffer[extension_size];

    uint8_t *cursor = buffer;
    *cursor = 0; // field id (0)
    cursor += sizeof(uint16_t); // skipping field id + reserved

    *((uint16_t *) cursor) = KAA_HTONS(1); // responses count = 1
    cursor += sizeof(uint16_t);

    *((uint16_t *) cursor) = KAA_HTONS(1); // request id = 1
    cursor += sizeof(uint16_t);

    *((uint16_t *) cursor) = KAA_HTONS(0); // listener result = 0 (SUCCESS)
    cursor += sizeof(uint16_t);

    *((uint32_t *) cursor) = KAA_HTONL(2); // listener count = 2
    cursor += sizeof(uint32_t);

    memcpy(cursor, endpoint_id1, KAA_ENDPOINT_ID_LENGTH); // copying endpoint id 1
    cursor += KAA_ENDPOINT_ID_LENGTH;

    memcpy(cursor, endpoint_id2, KAA_ENDPOINT_ID_LENGTH); // copying endpoint id 2
    cursor += KAA_ENDPOINT_ID_LENGTH;

    ASSERT_EQUAL(cursor, buffer + extension_size);

    kaa_platform_message_reader_t *reader;
    ASSERT_EQUAL(kaa_platform_message_reader_create(&reader, buffer, extension_size), KAA_ERR_NONE);

    ASSERT_FALSE(is_event_listeners_cb_called);
    ASSERT_EQUAL(kaa_event_handle_server_sync(event_manager, reader, 0, extension_size, 1), KAA_ERR_NONE);
    ASSERT_TRUE(is_event_listeners_cb_called);
    ASSERT_EQUAL(reader->current, reader->end);

    // Verifying that callback was removed from the manager
    is_event_listeners_cb_called = false;
    reader->current = reader->begin;
    ASSERT_EQUAL(kaa_event_handle_server_sync(event_manager, reader, 0, extension_size, 1), KAA_ERR_NONE);
    ASSERT_FALSE(is_event_listeners_cb_called);

    kaa_platform_message_reader_destroy(reader);
}

void test_kaa_event_sync_get_size(void **state)
{
    (void)state;

    kaa_error_t error_code;
    size_t expected_size = KAA_EXTENSION_HEADER_SIZE
                         + sizeof(uint8_t)
                         + sizeof(uint8_t)
                         + sizeof(uint16_t);

    const char *fqn = "test fqn";
    const size_t fqn_len = strlen(fqn);

    const size_t event_data_size = 16;
    /**
     * Allocated data will be freed automatically.
     */
    const char *event_data = (const char *) KAA_MALLOC(event_data_size);

    kaa_endpoint_id target = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };

    const size_t buffer_size = KAA_EXTENSION_HEADER_SIZE + sizeof(uint32_t);
    uint8_t buffer[buffer_size];
    kaa_platform_message_writer_t *writer;
    error_code = kaa_platform_message_writer_create(&writer, buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    const uint32_t sequence_number = KAA_HTONL(12345);
    error_code = kaa_platform_message_write(writer, &sequence_number, sizeof(uint32_t));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_platform_message_reader_t *reader;
    error_code = kaa_platform_message_reader_create(&reader, buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_event_manager_send_event(event_manager, fqn, event_data, event_data_size, target);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    expected_size += sizeof(uint32_t)
                   + sizeof(uint16_t)
                   + sizeof(uint16_t)
                   + sizeof(uint32_t)
                   + KAA_ENDPOINT_ID_LENGTH
                   + kaa_aligned_size_get(fqn_len)
                   + kaa_aligned_size_get(event_data_size);

    error_code = kaa_event_manager_send_event(event_manager, fqn, NULL, 0, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    expected_size += sizeof(uint32_t)
                   + sizeof(uint16_t)
                   + sizeof(uint16_t)
                   + kaa_aligned_size_get(fqn_len);

    size_t event_sync_size;
    error_code = kaa_event_request_get_size(event_manager, &event_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_EQUAL(event_sync_size, KAA_EXTENSION_HEADER_SIZE);

    error_code = kaa_event_handle_server_sync(event_manager, reader, 0x1, sizeof(uint32_t), 1);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_event_request_get_size(event_manager, &event_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(event_sync_size, expected_size);

    kaa_platform_message_writer_destroy(writer);
    kaa_platform_message_reader_destroy(reader);
}

static kaa_error_t serialize_event(kaa_platform_message_writer_t *writer
                                 , const char *fqn
                                 , const char *event_data
                                 , size_t event_data_size
                                 , kaa_endpoint_id_p target
                                 , size_t sequence_number
                                 , bool need_sequence_number)
{
    KAA_RETURN_IF_NIL2(writer, fqn, KAA_ERR_BADPARAM);

    kaa_error_t error_code;

    uint16_t event_options = (event_data ? 0x2 : 0) | (target ? 0x1 : 0);
    uint16_t fqn_len = strlen(fqn);

    uint16_t network_order_16;
    uint32_t network_order_32;

    if (need_sequence_number) {
        network_order_32 = KAA_HTONL(sequence_number);
        error_code = kaa_platform_message_write(writer, &network_order_32, sizeof(uint32_t));
        KAA_RETURN_IF_ERR(error_code);
    }

    network_order_16 = KAA_HTONS(event_options);
    error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
    KAA_RETURN_IF_ERR(error_code);

    network_order_16 = KAA_HTONS(fqn_len);
    error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
    KAA_RETURN_IF_ERR(error_code);

    if (event_data) {
        network_order_32 = KAA_HTONL(event_data_size);
        error_code = kaa_platform_message_write(writer, &network_order_32, sizeof(uint32_t));
        KAA_RETURN_IF_ERR(error_code);
    }

    if (target) {
        error_code = kaa_platform_message_write_aligned(writer, target, KAA_ENDPOINT_ID_LENGTH);
        KAA_RETURN_IF_ERR(error_code);
    }

    error_code = kaa_platform_message_write_aligned(writer, fqn, fqn_len);
    KAA_RETURN_IF_ERR(error_code);

    if (event_data) {
        error_code = kaa_platform_message_write_aligned(writer, event_data, event_data_size);
        KAA_RETURN_IF_ERR(error_code);
    }

    return error_code;
}



void test_event_sync_serialize(void **state)
{
    (void)state;
    test_deinit();
    test_init();

    kaa_error_t error_code;

    const uint8_t event_field = 1;
    const uint8_t reserved_field = 0;
    uint16_t event_count = 0;

    const char *fqn = "test fqn";
    /**
     * Allocated data will be freed automatically.
     */
    const size_t event_data_size = 16;
    const char *event_data = (const char *) KAA_MALLOC(event_data_size);

    kaa_endpoint_id target = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };

    const size_t server_sync_buffer_size = KAA_EXTENSION_HEADER_SIZE + sizeof(uint32_t);
    uint8_t server_sync_buffer[server_sync_buffer_size];
    kaa_platform_message_writer_t *server_sync_writer;
    error_code = kaa_platform_message_writer_create(&server_sync_writer, server_sync_buffer, server_sync_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint32_t sequence_number = KAA_HTONL(12345);
    error_code = kaa_platform_message_write(server_sync_writer, &sequence_number, sizeof(uint32_t));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    sequence_number = KAA_NTOHL(sequence_number);

    kaa_platform_message_reader_t *server_sync_reader;
    error_code = kaa_platform_message_reader_create(&server_sync_reader, server_sync_buffer, server_sync_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_event_handle_server_sync(event_manager, server_sync_reader, 0x1, sizeof(uint32_t), 1);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_event_manager_send_event(event_manager, fqn, event_data, event_data_size, target);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ++event_count;
    error_code = kaa_event_manager_send_event(event_manager, fqn, NULL, 0, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ++event_count;

    size_t event_sync_size = 0;
    error_code = kaa_event_request_get_size(event_manager, &event_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint8_t manual_buffer[event_sync_size];
    uint8_t auto_buffer[event_sync_size];

    uint8_t *manual_buffer_ptr = manual_buffer;
    uint8_t *auto_buffer_ptr = auto_buffer;

    kaa_platform_message_writer_t *manual_writer;
    error_code = kaa_platform_message_writer_create(&manual_writer, manual_buffer, event_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    kaa_platform_message_writer_t *auto_writer;
    error_code = kaa_platform_message_writer_create(&auto_writer, auto_buffer, event_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_message_write_extension_header(manual_writer
                                                           , KAA_EXTENSION_EVENT
                                                           , 0x1
                                                           , event_sync_size - KAA_EXTENSION_HEADER_SIZE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = kaa_platform_message_write(manual_writer, &event_field, sizeof(uint8_t));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = kaa_platform_message_write(manual_writer, &reserved_field, sizeof(uint8_t));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    event_count = KAA_HTONS(event_count);
    error_code = kaa_platform_message_write(manual_writer, &event_count, sizeof(uint16_t));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = serialize_event(manual_writer, fqn, event_data, event_data_size, target, ++sequence_number, true);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = serialize_event(manual_writer, fqn, NULL, 0, NULL, ++sequence_number, true);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_event_request_serialize(event_manager, 1, auto_writer);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = (memcmp(auto_buffer_ptr, manual_buffer_ptr, KAA_EXTENSION_HEADER_SIZE) == 0 ? KAA_ERR_NONE : KAA_ERR_BADDATA);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    auto_buffer_ptr += KAA_EXTENSION_HEADER_SIZE;
    manual_buffer_ptr += KAA_EXTENSION_HEADER_SIZE;
    event_sync_size -= KAA_EXTENSION_HEADER_SIZE;

    ASSERT_EQUAL(*auto_buffer_ptr, *manual_buffer_ptr);

    auto_buffer_ptr += sizeof(uint16_t);
    manual_buffer_ptr += sizeof(uint16_t);
    event_sync_size -= sizeof(uint16_t);

    error_code = (memcmp(auto_buffer_ptr, manual_buffer_ptr, event_sync_size) == 0 ? KAA_ERR_NONE : KAA_ERR_BADDATA);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_platform_message_writer_destroy(manual_writer);
    kaa_platform_message_writer_destroy(auto_writer);
    kaa_platform_message_reader_destroy(server_sync_reader);
    kaa_platform_message_writer_destroy(server_sync_writer);
}



void global_event_cb(const char *fqn, const char *data, size_t size, kaa_endpoint_id_p source)
{
    (void)fqn;
    (void)data;
    (void)size;
    (void)source;
    global_events_counter++;
}



void specific_event_cb(const char *fqn, const char *data, size_t size, kaa_endpoint_id_p source)
{
    (void)fqn;
    (void)data;
    (void)size;
    (void)source;
    specific_events_counter++;
}

static size_t event_get_size(const char *fqn
                           , const char *event_data
                           , size_t event_data_size
                           , kaa_endpoint_id_p source)
{
    (void)source;
    size_t size = 0;

    size += sizeof(uint16_t) /* event options */
          + sizeof(uint16_t);  /* fqn */

    if (event_data) {
        size += sizeof(uint32_t);
    }

    size += KAA_ENDPOINT_ID_LENGTH
          + kaa_aligned_size_get(strlen(fqn));

    if (event_data) {
        size += kaa_aligned_size_get(event_data_size);
    }

    return size;
}

void test_event_blocks(void **state)
{
    (void)state;

    test_deinit();
    test_init();

    size_t server_sync_buffer_size = sizeof(uint32_t);

    uint8_t server_sync_buffer[server_sync_buffer_size];
    memset(server_sync_buffer, 0, server_sync_buffer_size);

    kaa_platform_message_reader_t *server_sync_reader;
    kaa_error_t error_code = kaa_platform_message_reader_create(&server_sync_reader, server_sync_buffer, server_sync_buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    // Synchronizing sequence number
    error_code = kaa_event_handle_server_sync(event_manager, server_sync_reader, 0x01, server_sync_buffer_size, 1);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_event_block_id trx_id = 0;
    error_code = kaa_event_create_transaction(event_manager, &trx_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_TRUE(trx_id);

    const size_t event1_size = 6;
    char *event1 = (char *) KAA_MALLOC(event1_size + 1);
    strcpy(event1, "event1");
    error_code = kaa_event_manager_add_event_to_transaction(event_manager, trx_id, "test.fqn1", event1, event1_size, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    const size_t event2_size = 6;
    char *event2 = (char *) KAA_MALLOC(event2_size + 1);
    strcpy(event2, "event2");
    error_code = kaa_event_manager_add_event_to_transaction(event_manager, trx_id, "test.fqn2", event2, event2_size, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_event_manager_add_event_to_transaction(event_manager, trx_id, "test.fqn3", NULL, 0, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_event_finish_transaction(event_manager, trx_id);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t expected_size = KAA_EXTENSION_HEADER_SIZE
                         + sizeof(uint32_t)      // Events count
                         + 3 * sizeof(uint32_t)  // Event sequence numbers
                         + 3 * sizeof(uint32_t)  // Event options + FQN length
                         + 2 * sizeof(uint32_t)  // Event data sizes
                         + kaa_aligned_size_get(event1_size) + kaa_aligned_size_get(strlen("test.fqn1"))
                         + kaa_aligned_size_get(event2_size) + kaa_aligned_size_get(strlen("test.fqn2"))
                         + kaa_aligned_size_get(strlen("test.fqn3"));

    size_t request_size = 0;
    error_code = kaa_event_request_get_size(event_manager, &request_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(request_size, expected_size);

    kaa_platform_message_reader_destroy(server_sync_reader);
}

void test_kaa_server_sync_with_event_callbacks(void **state)
{
    (void)state;

    test_deinit();
    test_init();

    kaa_error_t error_code;

    const uint8_t event_field = 1;
    const uint8_t reserved_field = 0;
    uint16_t event_count = 2;

    const char *important_fqn = "important fqn";
    const char *unimportant_fqn = "unimportant fqn";

    /**
     * Allocated data will be freed automatically.
     */
    const size_t event_data_size = 14;
    const char *event_data = (const char *) KAA_MALLOC(event_data_size);

    kaa_endpoint_id source = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 };

     error_code = kaa_event_manager_add_on_event_callback(event_manager, important_fqn, specific_event_cb);
     ASSERT_EQUAL(error_code, KAA_ERR_NONE);
     error_code = kaa_event_manager_add_on_event_callback(event_manager, NULL, global_event_cb);
     ASSERT_EQUAL(error_code, KAA_ERR_NONE);

     size_t server_sync_buffer_size = 0;

     server_sync_buffer_size += sizeof(uint32_t)
                              + sizeof(uint8_t)
                              + sizeof(uint8_t)
                              + sizeof(uint16_t)
                              + event_get_size(unimportant_fqn, NULL, 0, source)
                              + event_get_size(important_fqn, event_data, event_data_size, source);

     uint8_t server_sync_buffer[server_sync_buffer_size];
     kaa_platform_message_writer_t *server_sync_writer;
     error_code = kaa_platform_message_writer_create(&server_sync_writer, server_sync_buffer, server_sync_buffer_size);
     ASSERT_EQUAL(error_code, KAA_ERR_NONE);

     uint32_t sequence_number = KAA_HTONL(12345);
     error_code = kaa_platform_message_write(server_sync_writer, &sequence_number, sizeof(uint32_t));
     ASSERT_EQUAL(error_code, KAA_ERR_NONE);
     sequence_number = KAA_NTOHL(sequence_number);

     error_code = kaa_platform_message_write(server_sync_writer, &event_field, sizeof(uint8_t));
     ASSERT_EQUAL(error_code, KAA_ERR_NONE);

     error_code = kaa_platform_message_write(server_sync_writer, &reserved_field, sizeof(uint8_t));
     ASSERT_EQUAL(error_code, KAA_ERR_NONE);

     event_count = KAA_HTONS(event_count);
     error_code = kaa_platform_message_write(server_sync_writer, &event_count, sizeof(uint16_t));
     ASSERT_EQUAL(error_code, KAA_ERR_NONE);

     error_code = serialize_event(server_sync_writer, unimportant_fqn, NULL, 0, source, 0, false);
     ASSERT_EQUAL(error_code, KAA_ERR_NONE);

     error_code = serialize_event(server_sync_writer, important_fqn, event_data, event_data_size, source, 0, false);
     ASSERT_EQUAL(error_code, KAA_ERR_NONE);

     kaa_platform_message_reader_t *server_sync_reader;
     error_code = kaa_platform_message_reader_create(&server_sync_reader, server_sync_buffer, server_sync_buffer_size);
     ASSERT_EQUAL(error_code, KAA_ERR_NONE);

     error_code = kaa_event_handle_server_sync(event_manager, server_sync_reader, 0x1, server_sync_buffer_size, 1);
     ASSERT_EQUAL(error_code, KAA_ERR_NONE);

     ASSERT_EQUAL(global_events_counter, 1);
     ASSERT_EQUAL(specific_events_counter, 1);

     kaa_platform_message_reader_destroy(server_sync_reader);
     kaa_platform_message_writer_destroy(server_sync_writer);
     KAA_FREE((void *) event_data);
}
#endif



static int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger) {
        return error;
    }

    kaa_context.logger = logger;

#ifndef KAA_DISABLE_FEATURE_EVENTS
    error = kaa_status_create(&status);
    if (error || !status) {
        return error;
    }

    error = kaa_channel_manager_create(&channel_manager, &kaa_context);
    if (error || !channel_manager) {
        return error;
    }

    error = kaa_event_manager_create(&event_manager, status, channel_manager, logger);
    if (error || !event_manager) {
        return error;
    }
#endif
    return 0;
}



static int test_deinit(void)
{
#ifndef KAA_DISABLE_FEATURE_EVENTS
    kaa_event_manager_destroy(event_manager);
    kaa_channel_manager_destroy(channel_manager);
    kaa_status_destroy(status);
#endif
    kaa_log_destroy(logger);
    return 0;
}



#ifndef KAA_DISABLE_FEATURE_EVENTS
KAA_SUITE_MAIN(Event, test_init, test_deinit,
          KAA_TEST_CASE(create_event_manager, test_kaa_create_event_manager)
          KAA_TEST_CASE(compile_event_request, test_kaa_event_sync_get_size)
          KAA_TEST_CASE(event_sync_serialize, test_event_sync_serialize)
          KAA_TEST_CASE(add_on_event_callback, test_kaa_server_sync_with_event_callbacks)
          KAA_TEST_CASE(event_listeners_serialize_request, test_kaa_event_listeners_serialize_request)
          KAA_TEST_CASE(event_listeners_handle_sync, test_kaa_event_listeners_handle_sync)
          KAA_TEST_CASE(event_test_blocks, test_event_blocks)
        )
#else
KAA_SUITE_MAIN(Event, test_init, test_deinit)
#endif

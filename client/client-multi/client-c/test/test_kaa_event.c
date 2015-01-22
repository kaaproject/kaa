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

#include "kaa_event.h"
#ifndef KAA_DISABLE_FEATURE_EVENTS

#include <string.h>

#include "kaa_test.h"
#include "utilities/kaa_log.h"
#include "utilities/kaa_mem.h"
#include "kaa_status.h"
#include "kaa_channel_manager.h"
#include "kaa_platform_utils.h"


extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void        kaa_status_destroy(kaa_status_t *self);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_logger_t *logger);
extern void        kaa_channel_manager_destroy(kaa_channel_manager_t *self);

extern kaa_error_t kaa_event_manager_create(kaa_event_manager_t **event_manager_p, kaa_status_t *status
        , kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void        kaa_event_manager_destroy(kaa_event_manager_t *self);

extern kaa_error_t kaa_event_manager_send_event(kaa_event_manager_t *self
                                              , const char *fqn
                                              , const char *event_data
                                              , size_t event_data_size
                                              , kaa_endpoint_id_p target);

extern kaa_error_t kaa_event_request_get_size(kaa_event_manager_t *self, size_t *expected_size);
extern kaa_error_t kaa_event_handle_server_sync(kaa_event_manager_t *self, kaa_platform_message_reader_t *reader, uint32_t extension_options, size_t extension_length, size_t request_id);
extern kaa_error_t kaa_event_request_serialize(kaa_event_manager_t *self, size_t request_id, kaa_platform_message_writer_t *writer);
extern kaa_error_t kaa_event_manager_add_on_event_callback(kaa_event_manager_t *self, const char *fqn, kaa_event_callback_t callback);

static int global_events_counter = 0;
static int specific_events_counter = 0;

static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_channel_manager_t *channel_manager = NULL;
static kaa_event_manager_t *event_manager = NULL;



static int test_init(void);
static int test_deinit(void);



void test_kaa_create_event_manager()
{
    KAA_TRACE_IN(logger);

    kaa_status_t* status = NULL;
    kaa_status_create(&status);
    kaa_event_manager_t *event_manager = NULL;
    kaa_error_t err_code = kaa_event_manager_create(&event_manager, status, NULL, logger);
    ASSERT_EQUAL(err_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(event_manager);
    kaa_event_manager_destroy(event_manager);

    KAA_FREE(status);
}



void test_kaa_event_sync_get_size()
{
    KAA_TRACE_IN(logger);

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
    char buffer[buffer_size];
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



void test_event_sync_serialize()
{
    test_deinit();
    test_init();

    KAA_TRACE_IN(logger);

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
    char server_sync_buffer[server_sync_buffer_size];
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

    size_t event_sync_size;
    error_code = kaa_event_request_get_size(event_manager, &event_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    char manual_buffer[event_sync_size];
    char auto_buffer[event_sync_size];

    kaa_platform_message_writer_t *manual_writer;
    error_code = kaa_platform_message_writer_create(&manual_writer, manual_buffer, event_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    kaa_platform_message_writer_t *auto_writer;
    error_code = kaa_platform_message_writer_create(&auto_writer, auto_buffer, event_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_message_write_extension_header(manual_writer
                                                           , KAA_EVENT_EXTENSION_TYPE
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

    error_code = (memcmp(auto_buffer, manual_buffer, event_sync_size) == 0 ? KAA_ERR_NONE : KAA_ERR_BADDATA);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_platform_message_writer_destroy(manual_writer);
    kaa_platform_message_writer_destroy(auto_writer);
    kaa_platform_message_reader_destroy(server_sync_reader);
    kaa_platform_message_writer_destroy(server_sync_writer);
}



void global_event_cb(const char *fqn, const char *data, size_t size, kaa_endpoint_id_p source)
{
    global_events_counter++;
}



void specific_event_cb(const char *fqn, const char *data, size_t size, kaa_endpoint_id_p source)
{
    specific_events_counter++;
}

static size_t event_get_size(const char *fqn
                           , const char *event_data
                           , size_t event_data_size
                           , kaa_endpoint_id_p source)
{
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

void test_kaa_server_sync_with_event_callbacks()
{
    KAA_TRACE_IN(logger);

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

     char server_sync_buffer[server_sync_buffer_size];
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
}
#endif



static int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger) {
        return error;
    }

#ifndef KAA_DISABLE_FEATURE_EVENTS
    error = kaa_status_create(&status);
    if (error || !status) {
        return error;
    }

    error = kaa_channel_manager_create(&channel_manager, logger);
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



KAA_SUITE_MAIN(Event, test_init, test_deinit
#ifndef KAA_DISABLE_FEATURE_EVENTS
        ,
          KAA_TEST_CASE(create_event_manager, test_kaa_create_event_manager)
          KAA_TEST_CASE(compile_event_request, test_kaa_event_sync_get_size)
          KAA_TEST_CASE(event_sync_serialize, test_event_sync_serialize)
          KAA_TEST_CASE(add_on_event_callback, test_kaa_server_sync_with_event_callbacks)
#endif
        )

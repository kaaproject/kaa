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

#include <string.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>

#include "kaa_status.h"
#include "kaa_test.h"

#include "kaa_context.h"
#include "kaa_status.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_context.h"
#include "kaa_profile.h"
#include "kaa_defaults.h"
#include "gen/kaa_profile_gen.h"
#include "kaa_platform_utils.h"
#include "platform/ext_status.h"
#include "platform/ext_sha.h"
#include "platform/ext_key_utils.h"
#include "platform/sock.h"


extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void        kaa_status_destroy(kaa_status_t *self);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_context_t *context);
extern void        kaa_channel_manager_destroy(kaa_channel_manager_t *self);

extern kaa_error_t kaa_profile_manager_create(kaa_profile_manager_t **profile_manager_p, kaa_status_t *status
        , kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void        kaa_profile_manager_destroy(kaa_profile_manager_t *self);

extern kaa_error_t kaa_profile_need_profile_resync(kaa_profile_manager_t *kaa_context, bool *result);
extern kaa_error_t kaa_profile_request_get_size(kaa_profile_manager_t *self, size_t *expected_size);
extern kaa_error_t kaa_profile_handle_server_sync(kaa_profile_manager_t *self, kaa_platform_message_reader_t *reader, uint16_t extension_options, size_t extension_length);
extern kaa_error_t kaa_profile_request_serialize(kaa_profile_manager_t *self, kaa_platform_message_writer_t* writer);
extern bool kaa_profile_manager_is_profile_set(kaa_profile_manager_t *self);


static kaa_context_t kaa_context;
static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_channel_manager_t *channel_manager = NULL;
static kaa_profile_manager_t *profile_manager = NULL;


#define TEST_PUB_KEY_SIZE 20
static const char test_ep_key[TEST_PUB_KEY_SIZE] = {0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x10, 0x11, 0x12, 0x13, 0x14};



void kaa_read_status_ext(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
}

void kaa_store_status_ext(const char *buffer, size_t buffer_size)
{
}

void kaa_get_endpoint_public_key(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    *buffer = (char *) KAA_MALLOC(TEST_PUB_KEY_SIZE * sizeof(char));
    if (*buffer) {
        memcpy(*buffer, test_ep_key, TEST_PUB_KEY_SIZE);
        *buffer_size = TEST_PUB_KEY_SIZE;
        *needs_deallocation = true;
    } else {
        *buffer_size = 0;
        *needs_deallocation = false;
    }
}

void test_profile_is_set(void)
{
    KAA_TRACE_IN(logger);

#if KAA_PROFILE_SCHEMA_VERSION > 0
    ASSERT_FALSE(kaa_profile_manager_is_profile_set(profile_manager));
    kaa_profile_t *profile = kaa_profile_basic_endpoint_profile_test_create();
    profile->profile_body = kaa_string_copy_create("test");
    kaa_error_t error = kaa_profile_manager_update_profile(profile_manager, profile);
    profile->destroy(profile);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_TRUE(kaa_profile_manager_is_profile_set(profile_manager));
#else
    ASSERT_TRUE(kaa_profile_manager_is_profile_set(profile_manager));
#endif

    KAA_TRACE_OUT(logger);
}

void test_profile_update(void)
{
    KAA_TRACE_IN(logger);

    kaa_profile_t *profile1 = kaa_profile_basic_endpoint_profile_test_create();
    profile1->profile_body = kaa_string_copy_create("dummy");
    kaa_error_t error = kaa_profile_manager_update_profile(profile_manager, profile1);
    ASSERT_EQUAL(error, KAA_ERR_NONE);

    bool need_resync = false;
    error = kaa_profile_need_profile_resync(profile_manager, &need_resync);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_TRUE(need_resync);

    error = kaa_profile_manager_update_profile(profile_manager, profile1);
    ASSERT_EQUAL(error, KAA_ERR_NONE);

    error = kaa_profile_need_profile_resync(profile_manager, &need_resync);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_FALSE(need_resync);

    profile1->destroy(profile1);

    kaa_profile_t *profile2 = kaa_profile_basic_endpoint_profile_test_create();
    profile2->profile_body = kaa_string_copy_create("new_dummy");
    error = kaa_profile_manager_update_profile(profile_manager, profile2);
    ASSERT_EQUAL(error, KAA_ERR_NONE);

    error = kaa_profile_need_profile_resync(profile_manager, &need_resync);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_TRUE(need_resync);

    profile2->destroy(profile2);

    KAA_TRACE_OUT(logger);
}

void test_profile_sync_get_size(void)
{
    KAA_TRACE_IN(logger);

    kaa_error_t error_code = KAA_ERR_NONE;
    kaa_profile_t *profile = kaa_profile_basic_endpoint_profile_test_create();
    profile->profile_body = kaa_string_copy_create("dummy");

    size_t serialized_profile_size = profile->get_size(profile);
    char *serialized_profile = (char *) KAA_MALLOC(serialized_profile_size * sizeof(char));
    avro_writer_t writer = avro_writer_memory(serialized_profile, serialized_profile_size);
    profile->serialize(writer, profile);

    size_t expected_size = KAA_EXTENSION_HEADER_SIZE
                         + sizeof(uint32_t)  // profile size
                         + kaa_aligned_size_get(serialized_profile_size);

    size_t profile_sync_size = 0;

    error_code = kaa_profile_manager_update_profile(profile_manager, profile);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    status->is_registered = true;

    error_code = kaa_profile_request_get_size(profile_manager, &profile_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(expected_size, profile_sync_size);

    status->is_registered = false;

    expected_size += sizeof(uint32_t)
                   + TEST_PUB_KEY_SIZE;

    error_code = kaa_profile_request_get_size(profile_manager, &profile_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(expected_size, profile_sync_size);

    const char *access_token = "access token";
    error_code = kaa_profile_manager_set_endpoint_access_token(profile_manager, access_token);

    expected_size += sizeof(uint32_t)
                   + strlen(access_token);

    error_code = kaa_profile_request_get_size(profile_manager, &profile_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(expected_size, profile_sync_size);

    avro_writer_free(writer);
    KAA_FREE(serialized_profile);
    profile->destroy(profile);

    KAA_TRACE_OUT(logger);
}

void test_profile_sync_serialize(void)
{
    KAA_TRACE_IN(logger);

    kaa_error_t error_code;
    kaa_platform_message_writer_t *manual_writer;
    kaa_platform_message_writer_t *auto_writer;

    const char *access_token = "access token";
    const size_t access_token_size = strlen(access_token);
    kaa_profile_t *profile = kaa_profile_basic_endpoint_profile_test_create();
    profile->profile_body = kaa_string_copy_create("dummy");
    size_t serialized_profile_size = profile->get_size(profile);
    char *serialized_profile = (char *) KAA_MALLOC(serialized_profile_size * sizeof(char));
    avro_writer_t avro_writer = avro_writer_memory(serialized_profile, serialized_profile_size);

    profile->serialize(avro_writer, profile);

    error_code = kaa_profile_manager_update_profile(profile_manager, profile);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    status->is_registered = false;
    error_code = kaa_profile_manager_set_endpoint_access_token(profile_manager, access_token);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    size_t profile_sync_size;
    error_code = kaa_profile_request_get_size(profile_manager, &profile_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    char buffer[profile_sync_size];
    error_code = kaa_platform_message_writer_create(&manual_writer, buffer, profile_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    uint32_t network_order_32;

    error_code = kaa_platform_message_write_extension_header(manual_writer
                                                           , KAA_PROFILE_EXTENSION_TYPE
                                                           , 0
                                                           , profile_sync_size - KAA_EXTENSION_HEADER_SIZE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    bool need_resync = true;
    ASSERT_EQUAL(kaa_profile_need_profile_resync(profile_manager, &need_resync), KAA_ERR_NONE);

    network_order_32 = KAA_HTONL(0);
    if (need_resync)
        network_order_32 = KAA_HTONL(serialized_profile_size);
    error_code = kaa_platform_message_write(manual_writer, &network_order_32, sizeof(uint32_t));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    if (need_resync) {
        error_code = kaa_platform_message_write_aligned(manual_writer, serialized_profile, serialized_profile_size);
        ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    }

    network_order_32 = KAA_HTONS(TEST_PUB_KEY_SIZE) << 16 | PUB_KEY_VALUE;
    error_code = kaa_platform_message_write(manual_writer, &network_order_32, sizeof(uint32_t));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_message_write_aligned(manual_writer, test_ep_key, TEST_PUB_KEY_SIZE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    network_order_32 = KAA_HTONS(access_token_size) << 16 | ACCESS_TOKEN_VALUE;
    error_code = kaa_platform_message_write(manual_writer, &network_order_32, sizeof(uint32_t));
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_message_write_aligned(manual_writer, access_token, access_token_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    char buffer2[profile_sync_size];
    error_code = kaa_platform_message_writer_create(&auto_writer, buffer2, profile_sync_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_profile_request_serialize(profile_manager, auto_writer);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = (memcmp(buffer, buffer2, profile_sync_size) == 0 ? KAA_ERR_NONE : KAA_ERR_BADDATA);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    KAA_FREE(serialized_profile);
    avro_writer_free(avro_writer);
    profile->destroy(profile);
    kaa_platform_message_writer_destroy(auto_writer);
    kaa_platform_message_writer_destroy(manual_writer);

    KAA_TRACE_OUT(logger);
}

void test_profile_handle_sync(void)
{
    KAA_TRACE_IN(logger);

    bool need_resync = false;
    kaa_error_t error_code = KAA_ERR_NONE;
    uint16_t extension_options = 0x1; /* Need resync */

    const size_t buffer_size = 6;
    char buffer[buffer_size];
    kaa_platform_message_reader_t *reader;
    error_code = kaa_platform_message_reader_create(&reader, buffer, buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_profile_handle_server_sync(profile_manager, reader, extension_options, 0);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = kaa_profile_need_profile_resync(profile_manager, &need_resync);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_TRUE(need_resync);

    extension_options = 0x0; /* Need resync */
    error_code = kaa_profile_handle_server_sync(profile_manager, reader, extension_options, 0);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = kaa_profile_need_profile_resync(profile_manager, &need_resync);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_FALSE(need_resync);

    kaa_platform_message_reader_destroy(reader);

    KAA_TRACE_OUT(logger);
}

int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger) {
        return error;
    }

    kaa_context.logger = logger;

    error = kaa_status_create(&status);
    if (error || !status) {
        return error;
    }

    error = kaa_channel_manager_create(&channel_manager, &kaa_context);
    if (error || !channel_manager) {
        return error;
    }

    error = kaa_profile_manager_create(&profile_manager, status, channel_manager, logger);
    if (error || !profile_manager) {
        return error;
    }

    return 0;
}



int test_deinit(void)
{
    kaa_profile_manager_destroy(profile_manager);
    kaa_channel_manager_destroy(channel_manager);
    kaa_status_destroy(status);
    kaa_log_destroy(logger);
    return 0;
}



KAA_SUITE_MAIN(Profile, test_init, test_deinit,
        KAA_TEST_CASE(profile_is_set, test_profile_is_set)
        KAA_TEST_CASE(profile_update, test_profile_update)
        KAA_TEST_CASE(profile_request, test_profile_sync_get_size)
        KAA_TEST_CASE(profile_sync_serialize, test_profile_sync_serialize)
        KAA_TEST_CASE(profile_handle_sync, test_profile_handle_sync)
)

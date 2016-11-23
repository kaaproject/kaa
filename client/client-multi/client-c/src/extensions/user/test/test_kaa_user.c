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
#include <string.h>
#include "platform/ext_sha.h"

#include "kaa_user.h"
#include "kaa_user_private.h"

#include "kaa_test.h"

#include "kaa.h"
#include "kaa_context.h"
#include "kaa_platform_protocol.h"
#include "kaa_channel_manager.h"
#include "kaa_status.h"
#include "kaa_platform_utils.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "platform/sock.h"

#include "kaa_private.h"

#define USER_EXTERNAL_ID    "user@id"
#define ACCESS_TOKEN        "token"
#define USER_VERIFIER       "user_verifier"

#define ATTACH_ERROR_REASON "Bad user credentials"

static kaa_context_t kaa_context;
static kaa_user_manager_t *user_manager = NULL;
static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_channel_manager_t *channel_manager = NULL;

static bool is_on_attached_invoked = false;
static bool is_on_detached_invoked = false;
static bool is_attach_success_invoked = false;
static bool is_attach_failed_invoked = false;
static bool last_is_attached_result = false;



static kaa_error_t on_attached(void *context, const char *user_external_id, const char *endpoint_access_token)
{
    (void)context;

    ASSERT_EQUAL(strcmp(ACCESS_TOKEN, endpoint_access_token), 0);
    ASSERT_EQUAL(strcmp(USER_EXTERNAL_ID, user_external_id), 0);
    is_on_attached_invoked = true;
    return KAA_ERR_NONE;
}

static kaa_error_t on_detached(void *context, const char *endpoint_access_token)
{
    (void)context;

    ASSERT_EQUAL(strcmp(ACCESS_TOKEN, endpoint_access_token), 0);
    is_on_detached_invoked = true;
    return KAA_ERR_NONE;
}

static kaa_error_t on_attach_success(void *context)
{
    (void)context;

    last_is_attached_result = true;
    is_attach_success_invoked = true;
    return KAA_ERR_NONE;
}

static kaa_error_t on_attach_failed(void *context, user_verifier_error_code_t error_code, const char *reason)
{
    (void)context;
    is_attach_failed_invoked = true;
    ASSERT_EQUAL(error_code, CONNECTION_ERROR);
    ASSERT_EQUAL(memcmp(reason, ATTACH_ERROR_REASON, strlen(ATTACH_ERROR_REASON)), 0);
    return KAA_ERR_NONE;
}

void test_specified_user_verifier(void **state)
{
    (void)state;

    ASSERT_EQUAL(kaa_user_manager_attach_to_user(user_manager, USER_EXTERNAL_ID, ACCESS_TOKEN, USER_VERIFIER), KAA_ERR_NONE);

    size_t expected_size = 0;
    ASSERT_EQUAL(kaa_user_request_get_size(user_manager, &expected_size), KAA_ERR_NONE);

    uint8_t buffer[expected_size];
    kaa_platform_message_writer_t *writer = NULL;
    ASSERT_EQUAL(kaa_platform_message_writer_create(&writer, buffer, expected_size), KAA_ERR_NONE);
    ASSERT_NOT_NULL(writer);

    ASSERT_EQUAL(kaa_user_request_serialize(user_manager, writer), KAA_ERR_NONE);

    uint8_t *buf_cursor = buffer;
    ASSERT_EQUAL(KAA_EXTENSION_USER, KAA_HTONS(*(uint16_t*)buf_cursor));
    buf_cursor += sizeof(uint16_t);

    char options[] = { 0x00, 0x01 };
    ASSERT_EQUAL(memcmp(buf_cursor, options, 2), 0);
    buf_cursor += 2;

    ASSERT_EQUAL(*(uint32_t * ) buf_cursor, KAA_HTONL(2 * sizeof(uint32_t)
                                                    + kaa_aligned_size_get(strlen(USER_EXTERNAL_ID))
                                                    + kaa_aligned_size_get(strlen(ACCESS_TOKEN)
                                                    + kaa_aligned_size_get(strlen(USER_VERIFIER)))));
    buf_cursor += sizeof(uint32_t);

    ASSERT_EQUAL(0, *buf_cursor);
    ++buf_cursor;

    ASSERT_EQUAL(strlen(USER_EXTERNAL_ID), *buf_cursor);
    ++buf_cursor;

    ASSERT_EQUAL(KAA_HTONS(strlen(ACCESS_TOKEN)), *(uint16_t *) buf_cursor);
    buf_cursor += sizeof(uint16_t);

    ASSERT_EQUAL(KAA_HTONS(strlen(USER_VERIFIER)), *(uint16_t *) buf_cursor);
    buf_cursor += sizeof(uint32_t); // + reserved 16B

    ASSERT_EQUAL(memcmp(buf_cursor, USER_EXTERNAL_ID, strlen(USER_EXTERNAL_ID)), 0);
    buf_cursor += kaa_aligned_size_get(strlen(USER_EXTERNAL_ID));

    ASSERT_EQUAL(memcmp(buf_cursor, ACCESS_TOKEN, strlen(ACCESS_TOKEN)), 0);
    buf_cursor += kaa_aligned_size_get(strlen(ACCESS_TOKEN));

    ASSERT_EQUAL(memcmp(buf_cursor, USER_VERIFIER, strlen(USER_VERIFIER)), 0);
    buf_cursor += kaa_aligned_size_get(strlen(USER_VERIFIER));

    kaa_platform_message_writer_destroy(writer);
}

void test_success_response(void **state)
{
    (void)state;

    uint8_t success_response[] = {
            /*  bit 0   */   0x00, 0x00, 0x00, 0x00,    /* User attach response field. Result - success */
            /*  bit 32  */   0x01, 0x07, 0x00, 0x05,    /* User attach notification field */
            /*  bit 64  */   'u', 's', 'e', 'r',
            /*  bit 96  */   '@', 'i', 'd', 0x00,
            /*  bit 128 */   't', 'o', 'k', 'e',
            /*  bit 160 */   'n', 0x00, 0x00, 0x00,
            /*  bit 192 */   0x02, 0x00, 0x00, 0x05,    /* User detach notification field */
            /*  bit 224 */   't', 'o', 'k', 'e',
            /*  bit 256 */   'n', 0x00, 0x00, 0x00

    };

    kaa_platform_message_reader_t *reader = NULL;
    ASSERT_EQUAL(kaa_platform_message_reader_create(&reader, success_response, 36), KAA_ERR_NONE);
    ASSERT_NOT_NULL(reader);

    ASSERT_EQUAL(kaa_user_handle_server_sync(user_manager, reader, 0, 36), KAA_ERR_NONE);
    ASSERT_TRUE(is_on_attached_invoked);
    ASSERT_TRUE(is_on_detached_invoked);
    ASSERT_TRUE(is_attach_success_invoked);
    ASSERT_TRUE(last_is_attached_result);

    kaa_platform_message_reader_destroy(reader);
}

void test_failed_response(void **state)
{
    (void)state;

    uint8_t failed_response[] = {
            /*  bit 0   */   0x00, 0x00, 0x01, 0x00,    /* User attach response field. Result - success */
            /*  bit 32  */   0x00, 0x04, 0x00, 0x14,    /* User attach notification field */
            /*  bit 64  */   'B', 'a', 'd', ' ',
            /*  bit 96  */   'u', 's', 'e', 'r',
            /*  bit 128 */   ' ', 'c', 'r', 'e',
            /*  bit 160 */   'd', 'e', 'n', 't',
            /*  bit 192 */   'i', 'a', 'l', 's'
    };

    kaa_platform_message_reader_t *reader = NULL;
    ASSERT_EQUAL(kaa_platform_message_reader_create(&reader, failed_response, 28), KAA_ERR_NONE);
    ASSERT_NOT_NULL(reader);

    ASSERT_EQUAL(kaa_user_handle_server_sync(user_manager, reader, 0, 28), KAA_ERR_NONE);
    ASSERT_TRUE(is_attach_failed_invoked);

    kaa_platform_message_reader_destroy(reader);
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

    error = kaa_user_manager_create(&user_manager, status, channel_manager, logger);
    if (error || !user_manager) {
        return error;
    }

    kaa_attachment_status_listeners_t listeners = { NULL, &on_attached, &on_detached, &on_attach_success, &on_attach_failed };
    error = kaa_user_manager_set_attachment_listeners(user_manager, &listeners);
    if (error) {
        return error;
    }

    return 0;
}

int test_deinit(void)
{
    kaa_user_manager_destroy(user_manager);
    kaa_channel_manager_destroy(channel_manager);
    kaa_status_destroy(status);
    kaa_log_destroy(logger);

    return 0;
}

KAA_SUITE_MAIN(Log, test_init, test_deinit
       ,
       KAA_TEST_CASE(specified_user_verifier, test_specified_user_verifier)
       KAA_TEST_CASE(process_success_response, test_success_response)
       KAA_TEST_CASE(process_failed_response, test_failed_response)
        )

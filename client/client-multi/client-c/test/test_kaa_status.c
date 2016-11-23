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
#include <stdint.h>
#include <string.h>
#include <stdio.h>
#include "kaa_test.h"
#include "kaa_status.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "platform/ext_sha.h"
#include "platform/ext_key_utils.h"
#include "kaa_private.h"

kaa_digest test_ep_key_hash = {0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x10, 0x11, 0x12, 0x13, 0x14};
kaa_digest test_profile_hash= {0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28};

#define KAA_STATUS_STORAGE "status.conf"

static kaa_logger_t *logger = NULL;

void ext_status_read(char **buffer, size_t *buffer_size, bool *needs_deallocation)
{
    *buffer = NULL;
    *buffer_size = 0;
    *needs_deallocation = true;

    FILE* status_file = fopen(KAA_STATUS_STORAGE, "rb");

    if (!status_file) {
        return;
    }

    fseek(status_file, 0, SEEK_END);
    *buffer_size = ftell(status_file);
    *buffer = (char *) KAA_MALLOC((*buffer_size) * sizeof(char));

    if (*buffer == NULL) {
        *buffer_size = 0;
        fclose(status_file);
        return;
    }

    fseek(status_file, 0, SEEK_SET);
    if (fread(*buffer, *buffer_size, 1, status_file) == 0) {
        *buffer_size = 0;
        KAA_FREE(*buffer);
    }
    *needs_deallocation = true;
    fclose(status_file);
}

void ext_status_store(const char *buffer, size_t buffer_size)
{
    if (!buffer || buffer_size == 0) {
        return;
    }

    FILE* status_file = fopen(KAA_STATUS_STORAGE, "wb");

    if (status_file) {
        fwrite(buffer, buffer_size, 1, status_file);
        fclose(status_file);
    }
}

void ext_get_endpoint_public_key(const uint8_t **buffer, size_t *buffer_size)
{
    *buffer = NULL;
    *buffer_size = 0;
}

void test_create_status(void **state)
{
    (void)state;

    kaa_status_t *status;
    kaa_error_t err_code = kaa_status_create(&status);

    ASSERT_EQUAL(err_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(status);

    kaa_status_destroy(status);
}

void test_status_persistense(void **state)
{
    (void)state;

    kaa_status_t *status;
    kaa_error_t err_code = kaa_status_create(&status);
    assert_int_equal(KAA_ERR_NONE, err_code);

    ASSERT_NULL(status->endpoint_access_token);
    ASSERT_EQUAL(status->event_seq_n, 0);
    ASSERT_FALSE(status->is_attached);
    ASSERT_FALSE(status->is_registered);
    ASSERT_FALSE(status->is_updated);

    ASSERT_EQUAL(kaa_status_set_endpoint_access_token(status, "my_token"), KAA_ERR_NONE);
    ASSERT_EQUAL(ext_copy_sha_hash(status->endpoint_public_key_hash, test_ep_key_hash), KAA_ERR_NONE);
    ASSERT_EQUAL(ext_copy_sha_hash(status->profile_hash, test_profile_hash), KAA_ERR_NONE);
    status->is_attached = true;
    status->is_registered = true;
    status->is_updated = true;
    status->event_seq_n = 10;

    err_code = kaa_status_save(status);
    ASSERT_EQUAL(err_code, KAA_ERR_NONE);

    kaa_status_destroy(status);
    status = NULL;


    err_code = kaa_status_create(&status);
    assert_int_equal(KAA_ERR_NONE, err_code);

    ASSERT_NOT_NULL(status->endpoint_access_token);
    ASSERT_EQUAL(strcmp("my_token", status->endpoint_access_token), 0);

    ASSERT_EQUAL(status->event_seq_n, 10);
    ASSERT_TRUE(status->is_attached);
    ASSERT_TRUE(status->is_registered);
    ASSERT_TRUE(status->is_updated);

    ASSERT_EQUAL(memcmp(test_ep_key_hash, status->endpoint_public_key_hash, SHA_1_DIGEST_LENGTH), 0);
    ASSERT_EQUAL(memcmp(test_profile_hash, status->profile_hash, SHA_1_DIGEST_LENGTH), 0);

    kaa_status_destroy(status);
}

int status_test_init(void)
{
    kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    remove(KAA_STATUS_STORAGE);
    return 0;
}

int test_deinit(void)
{
    kaa_log_destroy(logger);
    return 0;
}

KAA_SUITE_MAIN(Status, status_test_init, test_deinit,
        KAA_TEST_CASE(create, test_create_status)
        KAA_TEST_CASE(persistence, test_status_persistense)
)

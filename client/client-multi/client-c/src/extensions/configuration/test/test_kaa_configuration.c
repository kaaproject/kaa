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

#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include "kaa_configuration_manager.h"
#include "kaa_configuration_manager_private.h"

#include "platform/sock.h"
#include "platform/ext_sha.h"
#include "platform/ext_configuration_persistence.h"
#include "platform/ext_configuration_receiver.h"
#include "kaa_test.h"
#include "kaa_platform_utils.h"
#include "kaa_status.h"
#include "kaa_defaults.h"
#include "kaa_channel_manager.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"

#include "kaa_private.h"

static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_configuration_manager_t *config_manager = NULL;

#define CONFIG_START_SEQ_N  5
#define CONFIG_NEW_SEQ_N  6
#define CONFIG_RESPONSE_FLAGS 0x02
#define CONFIG_DATA_FIELD "Basic configuration schema"

static const size_t CONFIG_UUID_SIZE = 16;
static const char CONFIG_UUID[] = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };

static kaa_error_t on_configuration_updated(void *context, const kaa_root_configuration_t *configuration)
{
    (void)configuration;
    bool *result = (bool *) context;
    *result = true;
    return KAA_ERR_NONE;
}

void test_create_request(void **state)
{
    (void)state;

    size_t expected_size = 0;
    ASSERT_EQUAL(kaa_configuration_manager_get_size(config_manager, &expected_size), KAA_ERR_NONE);
    ASSERT_EQUAL(expected_size, KAA_EXTENSION_HEADER_SIZE + SHA_1_DIGEST_LENGTH);

    uint8_t request_buffer[expected_size];
    kaa_platform_message_writer_t *writer = NULL;
    ASSERT_EQUAL(kaa_platform_message_writer_create(&writer, request_buffer, expected_size), KAA_ERR_NONE);
    ASSERT_EQUAL(kaa_configuration_manager_request_serialize(config_manager, writer), KAA_ERR_NONE);

    uint8_t *cursor = writer->begin;
    ASSERT_EQUAL(KAA_HTONS(*((uint16_t *) cursor)), KAA_EXTENSION_CONFIGURATION);
    cursor += sizeof(uint32_t);

    ASSERT_EQUAL(KAA_NTOHL(*((uint32_t *) cursor)),  SHA_1_DIGEST_LENGTH);    // checking payload size
    cursor += sizeof(uint32_t);

    kaa_digest check_hash;
    ext_calculate_sha_hash(KAA_CONFIGURATION_DATA, KAA_CONFIGURATION_DATA_LENGTH, check_hash);  // checking configuration hash
    ASSERT_EQUAL(memcmp(cursor, check_hash, SHA_1_DIGEST_LENGTH), 0);
    cursor += SHA_1_DIGEST_LENGTH;

    ASSERT_EQUAL(cursor, writer->end);

    kaa_platform_message_writer_destroy(writer);
}

void test_response(void **state)
{
    (void)state;
    const size_t response_size = kaa_aligned_size_get(KAA_CONFIGURATION_DATA_LENGTH) + sizeof(uint32_t);
    uint8_t response[response_size];
    uint8_t *response_cursor = response;

    *((uint32_t *) response_cursor) = KAA_HTONL(KAA_CONFIGURATION_DATA_LENGTH);
    response_cursor += sizeof(uint32_t);

    memcpy(response_cursor, KAA_CONFIGURATION_DATA, KAA_CONFIGURATION_DATA_LENGTH);

    kaa_platform_message_reader_t *reader = NULL;
    ASSERT_EQUAL(kaa_platform_message_reader_create(&reader, response, response_size), KAA_ERR_NONE);

    bool is_callback_invoked = false;
    kaa_configuration_root_receiver_t receiver = { &is_callback_invoked, &on_configuration_updated };
    ASSERT_EQUAL(kaa_configuration_manager_set_root_receiver(config_manager, &receiver), KAA_ERR_NONE);

    ASSERT_EQUAL(kaa_configuration_manager_handle_server_sync(config_manager, reader, CONFIG_RESPONSE_FLAGS, response_size), KAA_ERR_NONE);

    ASSERT_EQUAL(is_callback_invoked, true);

    const kaa_root_configuration_t *root_config = kaa_configuration_manager_get_configuration(config_manager);
    ASSERT_EQUAL(strcmp(root_config->data->data, CONFIG_DATA_FIELD), 0);

    kaa_bytes_t *uuid = (kaa_bytes_t *) root_config->__uuid->data;
    ASSERT_EQUAL(uuid->size, CONFIG_UUID_SIZE);
    ASSERT_EQUAL(memcmp(uuid->buffer, CONFIG_UUID, uuid->size), 0);

    kaa_platform_message_reader_destroy(reader);
}

int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger) {
        return error;
    }

    error = kaa_status_create(&status);
    if (error || !status) {
        return error;
    }

    error = kaa_configuration_manager_create(&config_manager, NULL, status, logger);
    if (error || config_manager) {
        return error;
    }

    return 0;
}

int test_deinit(void)
{
    kaa_status_destroy(status);
    kaa_configuration_manager_destroy(config_manager);
    kaa_log_destroy(logger);
    return 0;
}

KAA_SUITE_MAIN(Log, test_init, test_deinit,
       KAA_TEST_CASE(create_request, test_create_request)
       KAA_TEST_CASE(process_response, test_response))

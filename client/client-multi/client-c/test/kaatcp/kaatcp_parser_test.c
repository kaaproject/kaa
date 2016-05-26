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

#include "kaa_test.h"

#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_protocols/kaa_tcp/kaatcp_parser.h"



static uint8_t ping_received = 0;
static uint8_t connack_received = 0;
static uint8_t kaasync_received = 0;
static uint8_t disconnect_received = 0;

static kaa_logger_t *logger = NULL;



void connack_listener(void *context, kaatcp_connack_t message)
{
    (void)context;

    connack_received = 1;
    ASSERT_EQUAL(message.return_code, KAATCP_CONNACK_IDENTIFIER_REJECTED);
}

void disconnect_listener(void *context, kaatcp_disconnect_t message)
{
    (void)context;

    disconnect_received = 1;
    ASSERT_EQUAL(message.reason, KAATCP_DISCONNECT_BAD_REQUEST);
}

void kaasync_listener(void *context, kaatcp_kaasync_t *message)
{
    (void)context;

    kaasync_received = 1;

    ASSERT_EQUAL(message->sync_header.protocol_name_length, 6);
    ASSERT_EQUAL(strcmp(message->sync_header.protocol_name, "Kaatcp"), 0);
    ASSERT_EQUAL(message->sync_header.protocol_version, PROTOCOL_VERSION);

    ASSERT_EQUAL(message->sync_header.message_id, 5);

    ASSERT_NOT_EQUAL((message->sync_header.flags & KAA_SYNC_ENCRYPTED_BIT), 0);
    ASSERT_EQUAL((message->sync_header.flags & KAA_SYNC_ZIPPED_BIT), 0);
    ASSERT_EQUAL((message->sync_header.flags & KAA_SYNC_REQUEST_BIT), 0);

    ASSERT_EQUAL(message->sync_request_size, 1);
    ASSERT_EQUAL((uint8_t)message->sync_request[0], 0xFF);

    kaatcp_parser_kaasync_destroy(message);
}

void ping_listener(void *context)
{
    (void)context;

    ping_received = 1;
}

void test_kaatcp_parser(void **state)
{
    (void)state;

    kaatcp_parser_handlers_t handlers = { NULL, &connack_listener, &disconnect_listener, &kaasync_listener, &ping_listener };
    kaatcp_parser_t parser;

    parser.payload_buffer_size = 1;
    parser.payload = KAA_CALLOC(parser.payload_buffer_size, sizeof(char));

    kaatcp_error_t rval = kaatcp_parser_init(&parser, &handlers);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    uint8_t connack_and_ping_messages[] = { 0x20, 0x02, 0x00, 0x03, 0xD0, 0x00 };
    rval = kaatcp_parser_process_buffer(&parser, (char *)connack_and_ping_messages, 6);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    ASSERT_NOT_EQUAL(ping_received, 0);
    ASSERT_NOT_EQUAL(connack_received, 0);

    uint8_t kaa_sync_message[] = { 0xF0, 0x0D, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x00, 0x05, 0x14, 0xFF };
    rval = kaatcp_parser_process_buffer(&parser, (const char *)kaa_sync_message, 15);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    ASSERT_NOT_EQUAL(kaasync_received, 0);

    unsigned char disconnect_message[] = { 0xE0, 0x02, 0x00, 0x01 };
    rval = kaatcp_parser_process_buffer(&parser, (const char *) disconnect_message, 4);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    ASSERT_NOT_EQUAL(disconnect_received, 0);

    KAA_FREE(parser.payload);
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
       KAA_TEST_CASE(kaatcp_parser, test_kaatcp_parser)
)


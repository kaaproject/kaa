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

#include <string.h>
#include <stdio.h>
#include <stdint.h>

#include "kaa_test.h"

#include "utilities/kaa_log.h"
#include "kaa_protocols/kaa_tcp/kaatcp_request.h"



static kaa_logger_t *logger = NULL;



void test_kaatcp_connect(void **state)
{
    (void)state;

    kaatcp_connect_t connect;
    char *session_key = "session_key";
    char *signature = "signature";
    char *payload = "payload";
    kaatcp_error_t rval = kaatcp_fill_connect_message(200, 0x3553c66f, payload, strlen(payload), session_key, strlen(session_key), signature, strlen(signature), &connect);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    char connect_buf[1024];
    size_t connect_buf_size = 1024;
    rval = kaatcp_get_request_connect(&connect, connect_buf, &connect_buf_size);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    unsigned char checkConnectHeader[] = { 0x10, 0x2D, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x02, 0x35, 0x53, 0xC6, 0x6F, 0x11, 0x01, 0x00, 0xC8 };

    ASSERT_EQUAL(connect_buf_size, 47);
    ASSERT_EQUAL(memcmp(connect_buf, checkConnectHeader, 20), 0);
    ASSERT_EQUAL(memcmp(connect_buf + 20, session_key, strlen(session_key)), 0);
    ASSERT_EQUAL(memcmp(connect_buf + 20 + strlen(session_key), signature, strlen(signature)), 0);
    ASSERT_EQUAL(memcmp(connect_buf + 20 + strlen(session_key) + strlen(signature), payload, strlen(payload)), 0);
}

void test_kaatcp_connect_without_key(void **state)
{
    (void)state;

    kaatcp_connect_t connect;
    char *payload = "payload";
    kaatcp_error_t rval = kaatcp_fill_connect_message(200, 0x3553c66f, payload, strlen(payload), NULL, 0, NULL, 0, &connect);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    char connect_buf[1024];
    size_t connect_buf_size = 1024;
    rval = kaatcp_get_request_connect(&connect, connect_buf, &connect_buf_size);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    unsigned char checkConnectHeader[] = { 0x10, 0x19, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x02, 0x35, 0x53, 0xC6, 0x6F, 0x00, 0x00, 0x00, 0xC8 };

    ASSERT_EQUAL(connect_buf_size, 27);
    ASSERT_EQUAL(memcmp(connect_buf, checkConnectHeader, 20), 0);
    ASSERT_EQUAL(memcmp(connect_buf + 20, payload, strlen(payload)), 0);
}

void test_kaatcp_disconnect(void **state)
{
    (void)state;

    kaatcp_disconnect_t disconnect;
    kaatcp_error_t rval = kaatcp_fill_disconnect_message(KAATCP_DISCONNECT_BAD_REQUEST, &disconnect);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    char disconnect_buf[5];
    size_t disconnect_buf_size = 5;
    rval = kaatcp_get_request_disconnect(&disconnect, disconnect_buf, &disconnect_buf_size);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    ASSERT_EQUAL(disconnect_buf_size, 4);

    unsigned char disconnect_message[] = { 0xE0, 0x02, 0x00, 0x01 };
    ASSERT_EQUAL(memcmp(disconnect_message, disconnect_buf, 4), 0);
}

void test_kaatcp_kaasync(void **state)
{
    (void)state;

    kaatcp_kaasync_t kaasync;
    char *payload = "payload";
    kaatcp_error_t rval = kaatcp_fill_kaasync_message(payload, strlen(payload), 5, 0, 1, &kaasync);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    char kaasync_buf[128];
    size_t kaasync_buf_size = 128;
    rval = kaatcp_get_request_kaasync(&kaasync, kaasync_buf, &kaasync_buf_size);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    unsigned char kaasync_message[] = { 0xF0, 0x13, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x00, 0x05, 0x15 };

    ASSERT_EQUAL(kaasync_buf_size,  21);
    ASSERT_EQUAL(memcmp(kaasync_message, kaasync_buf, 14),  0);
    ASSERT_EQUAL(memcmp(kaasync_buf + 14, payload, 7),  0);
}

void test_kaatcp_ping(void **state)
{
    (void)state;

    char ping_buf[5];
    size_t ping_buf_size = 5;
    kaatcp_error_t rval = kaatcp_get_request_ping(ping_buf, &ping_buf_size);
    ASSERT_EQUAL(rval, KAATCP_ERR_NONE);

    unsigned char ping_message [] = { 0xC0, 0x00 };

    ASSERT_EQUAL(ping_buf_size,  2);
    ASSERT_EQUAL(memcmp(ping_buf, ping_message, 2), 0);
}

void test_get_request_kaasync_over_buff(void **state)
{
    (void)state;

    kaatcp_kaasync_t kaasync;
    char *payload = "payload";
    char kaasync_buf[100] = "";
    size_t kaasync_buf_size = 5;
    uint16_t message_id = 5;
    uint8_t zipped = 0;
    uint8_t encrypted = 1;

    kaatcp_fill_kaasync_message(payload, strlen(payload), message_id, zipped, encrypted, &kaasync);
    memset(kaasync_buf, 0xEA, sizeof(kaasync_buf));

    kaatcp_error_t rval = kaatcp_get_request_kaasync(&kaasync, kaasync_buf, &kaasync_buf_size);
    ASSERT_EQUAL(rval, KAATCP_ERR_BUFFER_NOT_ENOUGH);

    for (size_t i = 8; i < sizeof(kaasync_buf); i++) {
        ASSERT_EQUAL((uint8_t)kaasync_buf[i], 0xEA);
    }
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
       KAA_TEST_CASE(kaatcp_connect, test_kaatcp_connect)
       KAA_TEST_CASE(kaatcp_connect, test_kaatcp_connect_without_key)
       KAA_TEST_CASE(kaatcp_disconnect, test_kaatcp_disconnect)
       KAA_TEST_CASE(kaatcp_kaasync, test_kaatcp_kaasync)
       KAA_TEST_CASE(kaatcp_ping, test_kaatcp_ping)
       KAA_TEST_CASE(get_request_kaasync, test_get_request_kaasync_over_buff)
)

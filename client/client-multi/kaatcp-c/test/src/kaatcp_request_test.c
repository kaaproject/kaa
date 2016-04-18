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

#include "kaatcp_request_test.h"

#include <string.h>
#include <assert.h>
#include <stdio.h>
#include "kaatcp_request.h"

void test_kaatcp_connect()
{
    kaatcp_connect_t connect;
    char *session_key = "session_key";
    char *signature = "signature";
    char *payload = "payload";
    kaatcp_error_t rval = kaatcp_fill_connect_message(200, 0x3553c66f, payload, strlen(payload), session_key, strlen(session_key), signature, strlen(signature), &connect);
    assert(rval == KAATCP_ERR_NONE);

    char connect_buf[1024];
    uint32_t connect_buf_size = 1024;
    assert(kaatcp_get_request_connect(&connect, connect_buf, &connect_buf_size) == KAATCP_ERR_NONE);
    unsigned char checkConnectHeader[] = { 0x10, 0x2D, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x02, 0x35, 0x53, 0xC6, 0x6F, 0x11, 0x01, 0x00, 0xC8 };

    assert(connect_buf_size == 47);
    assert(memcmp(connect_buf, checkConnectHeader, 20) == 0);
    assert(memcmp(connect_buf + 20, session_key, strlen(session_key)) == 0);
    assert(memcmp(connect_buf + 20 + strlen(session_key), signature, strlen(signature)) == 0);
    assert(memcmp(connect_buf + 20 + strlen(session_key) + strlen(signature), payload, strlen(payload)) == 0);
}

void test_kaatcp_connect_without_key()
{
    kaatcp_connect_t connect;
    char *payload = "payload";
    assert(kaatcp_fill_connect_message(200, 0x3553c66f, payload, strlen(payload), NULL, 0, NULL, 0, &connect) == KAATCP_ERR_NONE);

    char connect_buf[1024];
    uint32_t connect_buf_size = 1024;
    assert(kaatcp_get_request_connect(&connect, connect_buf, &connect_buf_size) == KAATCP_ERR_NONE);
    unsigned char checkConnectHeader[] = { 0x10, 0x19, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x02, 0x35, 0x53, 0xC6, 0x6F, 0x00, 0x00, 0x00, 0xC8 };

    assert(connect_buf_size == 27);
    assert(memcmp(connect_buf, checkConnectHeader, 20) == 0);
    assert(memcmp(connect_buf + 20, payload, strlen(payload)) == 0);
}

void test_kaatcp_disconnect()
{
    kaatcp_disconnect_t disconnect;
    assert(kaatcp_fill_disconnect_message(KAATCP_DISCONNECT_BAD_REQUEST, &disconnect) == KAATCP_ERR_NONE);

    char disconnect_buf[5];
    uint32_t disconnect_buf_size = 5;
    assert(kaatcp_get_request_disconnect(&disconnect, disconnect_buf, &disconnect_buf_size) == KAATCP_ERR_NONE);

    assert(disconnect_buf_size == 4);

    unsigned char disconnect_message[] = { 0xE0, 0x02, 0x00, 0x01 };
    assert(memcmp(disconnect_message, disconnect_buf, 4) == 0);
}

void test_kaatcp_kaasync()
{
    kaatcp_kaasync_t kaasync;
    char *payload = "payload";
    assert(kaatcp_fill_kaasync_message(payload, strlen(payload), 5, 0, 1, &kaasync) == KAATCP_ERR_NONE);

    char kaasync_buf[128];
    uint32_t kaasync_buf_size = 128;
    assert(kaatcp_get_request_kaasync(&kaasync, kaasync_buf, &kaasync_buf_size) == KAATCP_ERR_NONE);

    unsigned char kaasync_message[] = { 0xF0, 0x13, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x00, 0x05, 0x15 };

    assert(kaasync_buf_size == 21);
    assert(memcmp(kaasync_message, kaasync_buf, 14) == 0);
    assert(memcmp(kaasync_buf + 14, payload, 7) == 0);
}

void test_kaatcp_ping()
{
    char ping_buf[5];
    uint32_t ping_buf_size = 5;
    assert(kaatcp_get_request_ping(ping_buf, &ping_buf_size) == KAATCP_ERR_NONE);

    unsigned char ping_message [] = { 0xD0, 0x00 };

    assert(ping_buf_size == 2);
    assert(memcmp(ping_buf, ping_message, 2));

}

void test_kaatcp_bootstrap_request()
{
    kaatcp_bootstrap_request_t bootstrap;
    char *app_token = "app_token";
    assert(kaatcp_fill_bootstrap_message(app_token, 5, &bootstrap) == KAATCP_ERR_NONE);

    char bootstrap_buf[128];
    uint32_t bootstrap_buf_size = 128;
    assert(kaatcp_get_request_bootstrap(&bootstrap, bootstrap_buf, &bootstrap_buf_size) == KAATCP_ERR_NONE);

    unsigned char bootstrap_message[] = { 0xF0, 0x15, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x00, 0x05, 0x21 };

    assert(bootstrap_buf_size == 23);
    assert(memcmp(bootstrap_message, bootstrap_buf, 14) == 0);
    assert(memcmp(bootstrap_buf + 14, app_token, 9) == 0);
}

void kaatcp_request_test_suite()
{
    test_kaatcp_connect();
    test_kaatcp_connect_without_key();
    test_kaatcp_disconnect();
    test_kaatcp_kaasync();
    test_kaatcp_ping();
    test_kaatcp_bootstrap_request();
}

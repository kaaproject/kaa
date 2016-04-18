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

#include "kaatcp_parser_test.h"

#include <assert.h>
#include <stdio.h>
#include <string.h>
#include "kaatcp_parser.h"

static uint8_t ping_received = 0;
static uint8_t connack_received = 0;
static uint8_t kaasync_received = 0;
static uint8_t disconnect_received = 0;
static uint8_t bootstrap_received = 0;

void connack_listener(kaatcp_connack_t message)
{
    connack_received = 1;
    assert(message.return_code == KAATCP_CONNACK_IDENTIFIER_REJECTED);
}

void disconnect_listener(kaatcp_disconnect_t message)
{
    disconnect_received = 1;
    assert(message.reason == KAATCP_DISCONNECT_BAD_REQUEST);
}

void kaasync_listener(kaatcp_kaasync_t *message)
{
    kaasync_received = 1;

    assert(message->sync_header.protocol_name_length == 6);
    assert(strcmp(message->sync_header.protocol_name, "Kaatcp") == 0);
    assert(message->sync_header.protocol_version == PROTOCOL_VERSION);

    assert(message->sync_header.message_id == 5);

    assert(message->sync_header.flags & KAA_SYNC_ENCRYPTED_BIT);
    assert(!(message->sync_header.flags & KAA_SYNC_ZIPPED_BIT));
    assert(!(message->sync_header.flags & KAA_SYNC_REQUEST_BIT));

    assert(message->sync_request_size == 1);
    assert((uint8_t)message->sync_request[0] == 0xFF);

    kaatcp_parser_kaasync_destroy(message);
}

void bootstrap_response_listener(kaatcp_bootstrap_response_t *message)
{
    bootstrap_received = 1;
    assert(message->sync_header.protocol_name_length == 6);
    assert(strcmp(message->sync_header.protocol_name, "Kaatcp") == 0);
    assert(message->sync_header.protocol_version == PROTOCOL_VERSION);

    assert(message->sync_header.message_id == 5);

    assert(!(message->sync_header.flags & KAA_SYNC_ENCRYPTED_BIT));
    assert(!(message->sync_header.flags & KAA_SYNC_ZIPPED_BIT));
    assert(!(message->sync_header.flags & KAA_SYNC_REQUEST_BIT));

    assert(message->server_count == 2);

    kaatcp_server_record_t *server1 = message->servers;
    assert(server1->public_key_length == 16);
    assert(server1->public_key_type == KAA_BOOTSTRAP_RSA_PKSC8);
    unsigned char key1[] = { 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3 };
    assert(memcmp(server1->public_key, key1, server1->public_key_length) == 0);
    assert(server1->server_priority == 10);
    assert(strcmp(server1->server_name, "server1") == 0);
    assert(server1->server_name_length == 7);
    assert(server1->supported_channels_count == 3);

    kaatcp_supported_channel_t *server1_channel1 = server1->supported_channels;
    assert(server1_channel1->channel_type == KAA_BOOTSTRAP_CHANNEL_HTTP);
    assert(strcmp(server1_channel1->hostname, "hostname1.example.com") == 0);
    assert(server1_channel1->hostname_length == strlen(server1_channel1->hostname));
    assert(server1_channel1->port == 1212);

    kaatcp_supported_channel_t *server1_channel2 = server1->supported_channels + 1;
    assert(server1_channel2->channel_type == KAA_BOOTSTRAP_CHANNEL_HTTPLP);
    assert(strcmp(server1_channel2->hostname, "hostname1.example.com") == 0);
    assert(server1_channel2->hostname_length == strlen(server1_channel2->hostname));
    assert(server1_channel2->port == 1213);

    kaatcp_supported_channel_t *server1_channel3 = server1->supported_channels + 2;
    assert(server1_channel3->channel_type == KAA_BOOTSTRAP_CHANNEL_KAATCP);
    assert(strcmp(server1_channel3->hostname, "hostname1.example.com") == 0);
    assert(server1_channel3->hostname_length == strlen(server1_channel3->hostname));
    assert(server1_channel3->port == 1214);

    kaatcp_server_record_t *server2 = message->servers + 1;
    assert(server2->public_key_length == 16);
    assert(server2->public_key_type == KAA_BOOTSTRAP_RSA_PKSC8);
    unsigned char key2[] = { 0x10, 0x11, 0x12, 0x13, 0x10, 0x11, 0x12, 0x13, 0x10, 0x11, 0x12, 0x13, 0x10, 0x11, 0x12, 0x13 };
    assert(memcmp(server2->public_key, key2, server2->public_key_length) == 0);
    assert(server2->server_priority == 20);
    assert(strcmp(server2->server_name, "server22") == 0);
    assert(server2->server_name_length == 8);
    assert(server2->supported_channels_count == 3);

    kaatcp_supported_channel_t *server2_channel1 = server2->supported_channels;
    assert(server2_channel1->channel_type == KAA_BOOTSTRAP_CHANNEL_HTTP);
    assert(strcmp(server2_channel1->hostname, "hostname2.example.com") == 0);
    assert(server2_channel1->hostname_length == strlen(server2_channel1->hostname));
    assert(server2_channel1->port == 1212);

    kaatcp_supported_channel_t *server2_channel2 = server2->supported_channels + 1;
    assert(server2_channel2->channel_type == KAA_BOOTSTRAP_CHANNEL_HTTPLP);
    assert(strcmp(server2_channel2->hostname, "hostname2.example.com") == 0);
    assert(server2_channel2->hostname_length == strlen(server2_channel2->hostname));
    assert(server2_channel2->port == 1213);

    kaatcp_supported_channel_t *server2_channel3 = server2->supported_channels + 2;
    assert(server2_channel3->channel_type == KAA_BOOTSTRAP_CHANNEL_KAATCP);
    assert(strcmp(server2_channel3->hostname, "hostname22.example.com") == 0);
    assert(server2_channel3->hostname_length == strlen(server2_channel3->hostname));
    assert(server2_channel3->port == 1214);

    kaatcp_parser_bootstrap_destroy(message);
}

void ping_listener()
{
    ping_received = 1;
}

void test_kaatcp_parser()
{
    kaatcp_parser_handlers_t handlers = { &connack_listener, &disconnect_listener, &kaasync_listener, &bootstrap_response_listener, &ping_listener };
    kaatcp_parser_t parser;
    assert(kaatcp_parser_init(&parser, &handlers) == 0);

    char connack_and_ping_messages[] = { 0x20, 0x02, 0x00, 0x03, 0xD0, 0x00 };
    assert(kaatcp_parser_process_buffer(&parser, connack_and_ping_messages, 6) == KAATCP_ERR_NONE);

    assert(ping_received);
    assert(connack_received);

    unsigned char kaa_sync_message[] = { 0xF0, 0x0D, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x00, 0x05, 0x14, 0xFF };
    assert(kaatcp_parser_process_buffer(&parser, (const char *)kaa_sync_message, 15) == KAATCP_ERR_NONE);

    assert(kaasync_received);

    unsigned char disconnect_message[] = { 0xE0, 0x02, 0x00, 0x01 };
    assert(kaatcp_parser_process_buffer(&parser, (const char *) disconnect_message, 4) == KAATCP_ERR_NONE);

    assert(disconnect_received);

    unsigned char bootstrap_message[] = {-16, -88, 2, 0, 6, 75, 97, 97, 116, 99, 112, 1, 0, 5, 32, 0, 0, 0, 2, 0, 0, 0, -120, 0, 0, 0, 7, 115, 101, 114, 118, 101, 114, 49, 0, 0, 0, 0, 10, 1, 0, 0, 16, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 0, 0, 3, 0, 0, 0, 25, 1, 21, 4, -68, 104, 111, 115, 116, 110, 97, 109, 101, 49, 46, 101, 120, 97, 109, 112, 108, 101, 46, 99, 111, 109, 0, 0, 0, 0, 0, 0, 25, 2, 21, 4, -67, 104, 111, 115, 116, 110, 97, 109, 101, 49, 46, 101, 120, 97, 109, 112, 108, 101, 46, 99, 111, 109, 0, 0, 0, 0, 0, 0, 25, 3, 21, 4, -66, 104, 111, 115, 116, 110, 97, 109, 101, 49, 46, 101, 120, 97, 109, 112, 108, 101, 46, 99, 111, 109, 0, 0, 0, 0, 0, 0, -120, 0, 0, 0, 8, 115, 101, 114, 118, 101, 114, 50, 50, 0, 0, 0, 20, 1, 0, 0, 16, 16, 17, 18, 19, 16, 17, 18, 19, 16, 17, 18, 19, 16, 17, 18, 19, 0, 0, 0, 3, 0, 0, 0, 25, 1, 21, 4, -68, 104, 111, 115, 116, 110, 97, 109, 101, 50, 46, 101, 120, 97, 109, 112, 108, 101, 46, 99, 111, 109, 0, 0, 0, 0, 0, 0, 25, 2, 21, 4, -67, 104, 111, 115, 116, 110, 97, 109, 101, 50, 46, 101, 120, 97, 109, 112, 108, 101, 46, 99, 111, 109, 0, 0, 0, 0, 0, 0, 26, 3, 22, 4, -66, 104, 111, 115, 116, 110, 97, 109, 101, 50, 50, 46, 101, 120, 97, 109, 112, 108, 101, 46, 99, 111, 109, 0, 0};
    assert(kaatcp_parser_process_buffer(&parser, (const char *) bootstrap_message, 299) == KAATCP_ERR_NONE);
    assert(bootstrap_received);
}

void kaatcp_parser_test_suite()
{
    test_kaatcp_parser();
}



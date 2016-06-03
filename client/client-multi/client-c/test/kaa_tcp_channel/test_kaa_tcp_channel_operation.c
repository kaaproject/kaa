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

/*
 * @file test_kaa_tcp_channel_operation.c
 */

#define _POSIX_C_SOURCE 200112L

#include <stdint.h>
#include <stdbool.h>
#include <netdb.h>
#include <string.h>

#include "kaa_test.h"

#include "utilities/kaa_log.h"
#include "utilities/kaa_mem.h"
#include "platform/ext_transport_channel.h"
#include "platform/ext_tcp_utils.h"
#include "platform-impl/common/kaa_tcp_channel.h"
#include "kaa_protocols/kaa_tcp/kaatcp_request.h"
#include <kaa_common.h>

#define ACCESS_POINT_SOCKET_FD 5

#define KEEPALIVE 1000

typedef struct {
    bool        gethostbyaddr_requested; //
    bool        new_socket_created; //
    bool        socket_connecting_error_scenario;
    bool        socket_connected; //
    bool        socket_connected_callback; //
    bool        socket_connecting_error_callback; //
    bool        fill_connect_message;//
    bool        request_connect;//
    bool        auth_packet_written;
    bool        connack_read;
    bool        disconnect_create_non_scenario;
    bool        disconnect_read;
    bool        kaasync_read_scenario;
    bool        kaasync_write;
    bool        kaasync_read;
    bool        kaasync_processed;
    bool        socket_disconnected_write;
    bool        socket_disconnected_closed;
    bool        socket_disconnected_callback;
    bool        bootstrap_manager_on_access_point_failed;
    kaa_fd_t    fd;
} set_access_point_info_t;

static set_access_point_info_t access_point_test_info;

static void reset_access_point_test_info()
{
    memset(&access_point_test_info,0,sizeof(set_access_point_info_t));
}

static kaa_logger_t *logger = NULL;

static uint8_t CONNACK[] = {0x20, 0x02, 0x00, 0x01};

static uint8_t DISCONNECT_NONE[] = {0xE0, 0x02, 0x00, 0x00};

static uint8_t KAASYNC_OP_SERV[] = {0xf0, 0x0e, 0x00, 0x06, 'K', 'a','a','t','c','p',
                                 0x01, 0x00, 0x00, 0x11, 0x34, 0x45};

static uint8_t KAASYNC_OP[] = {0xF0, 0x13, 0x00, 0x06, 'K', 'a','a','t','c','p',
                                   0x01, 0x00, 0x01, 0x10, 'K', 'a','a','t','c','p', 0x00};

static char *KAASYNC_OP_MESSAGE = "Kaatcp";

static uint8_t DISCONNECT_MESSAGE[] = {0xE0, 0x02, 0x00, 0x00};

static uint8_t CONNECT_HEAD[] = {0x35, 0x46};
static uint8_t CONNECT_PACK[] = {0x34, 0x45};

static uint8_t DESTINATION_SOCKADDR[]   = {0x02, 0x00, 0x26, 0xa0, 0xc0, 0xa8, 0x4d, 0x02,
                                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

static uint8_t CONNECTION_DATA[]   = { 0x00, 0x00, 0x01, 0x26, 0x30, 0x82, 0x01, 0x22,
                                    0x30, 0x0D, 0x06, 0x09, 0x2A, 0x86, 0x48, 0x86,
                                    0xF7, 0x0D, 0x01, 0x01, 0x01, 0x05, 0x00, 0x03,
                                    0x82, 0x01, 0x0F, 0x00, 0x30, 0x82, 0x01, 0x0A,
                                    0x02, 0x82, 0x01, 0x01, 0x00, 0x85, 0x6E, 0xF4,
                                    0xB8, 0x57, 0x68, 0x5A, 0x6A, 0x88, 0x10, 0x7E,
                                    0xC7, 0x17, 0xED, 0x41, 0xBF, 0xD4, 0xBD, 0x74,
                                    0x93, 0xF2, 0xBA, 0x3F, 0x07, 0x60, 0x4A, 0x82,
                                    0x19, 0xD7, 0x85, 0x18, 0x75, 0xDA, 0x81, 0x9F,
                                    0x2B, 0x5A, 0xFD, 0x7E, 0x00, 0x3D, 0xDA, 0xAA,
                                    0xA1, 0x74, 0x1D, 0x7E, 0x76, 0x4B, 0x8E, 0x76,
                                    0x8F, 0x56, 0x89, 0x4A, 0xFC, 0x35, 0xB2, 0xDB,
                                    0x30, 0x89, 0x9D, 0xAE, 0xF5, 0x77, 0x29, 0x98,
                                    0xBC, 0xA1, 0xC9, 0xDC, 0xA1, 0x98, 0x26, 0x25,
                                    0x62, 0xA3, 0x27, 0x3D, 0xFA, 0x3E, 0x42, 0x32,
                                    0xD5, 0x03, 0x27, 0x9C, 0xA3, 0xE6, 0x46, 0xF3,
                                    0x24, 0xD6, 0x66, 0x9E, 0x22, 0xE3, 0x9C, 0x87,
                                    0x75, 0x4F, 0x6A, 0xE4, 0xCB, 0xE1, 0x80, 0xAC,
                                    0x4D, 0x3F, 0x1C, 0x1C, 0xD4, 0x0D, 0x6A, 0x83,
                                    0xA6, 0x1C, 0x10, 0x16, 0xA1, 0xA7, 0x05, 0xF2,
                                    0x04, 0x54, 0xB0, 0x1A, 0x99, 0xC5, 0xDB, 0x69,
                                    0x06, 0x16, 0x8C, 0xA9, 0x56, 0xF0, 0x48, 0x5E,
                                    0xD3, 0xC7, 0xF7, 0x62, 0x11, 0xE5, 0x32, 0x55,
                                    0x9A, 0x65, 0xB0, 0xB6, 0xDF, 0x80, 0x17, 0x80,
                                    0x28, 0xED, 0x7E, 0x24, 0xA7, 0x89, 0x10, 0x16,
                                    0xA0, 0xBA, 0xE2, 0xB1, 0x33, 0x49, 0xC7, 0x77,
                                    0x36, 0xED, 0xE6, 0xEB, 0xB8, 0x30, 0xEF, 0x78,
                                    0xB2, 0x51, 0xFC, 0x02, 0x86, 0xDD, 0xC1, 0x3C,
                                    0xC8, 0x2F, 0x06, 0xC9, 0xBA, 0x93, 0x08, 0xAF,
                                    0x90, 0xBE, 0xFD, 0xF2, 0x37, 0x7E, 0xCB, 0x67,
                                    0x72, 0x19, 0x6E, 0x10, 0xEA, 0xAA, 0xAA, 0x63,
                                    0x3B, 0xFE, 0x56, 0x4D, 0xD6, 0x8F, 0x7E, 0x95,
                                    0xCA, 0x67, 0xEA, 0xE2, 0xE5, 0x7D, 0x45, 0x6B,
                                    0x55, 0x88, 0x84, 0xE3, 0x8B, 0xF9, 0x2B, 0xAA,
                                    0xFC, 0xC1, 0xFE, 0xD7, 0x6B, 0x4C, 0xA5, 0x29,
                                    0x0F, 0xBF, 0x56, 0x02, 0xB7, 0xB7, 0xF2, 0xD3,
                                    0x8D, 0x8E, 0xC9, 0x38, 0xB1, 0x02, 0x03, 0x01,
                                    0x00, 0x01, 0x00, 0x00, 0x00, 0x0C, 0x31, 0x39,
                                    0x32, 0x2E, 0x31, 0x36, 0x38, 0x2E, 0x37, 0x37,
                                    0x2E, 0x32, 0x00, 0x00, 0x26, 0xA0};

kaa_error_t kaa_tcp_channel_event_callback_fn(void *context, kaa_tcp_channel_event_t event_type, kaa_fd_t fd);
void test_set_access_point(kaa_transport_channel_interface_t *channel);
void test_check_channel_auth(kaa_transport_channel_interface_t *channel);
void test_send_disconnect(kaa_transport_channel_interface_t *channel);
void test_sync_exchange(kaa_transport_channel_interface_t *channel);

#define CHECK_SOCKET_RW(channel, read, write) \
    {\
        bool rd = kaa_tcp_channel_is_ready(channel, FD_READ);\
        ASSERT_EQUAL(rd, read);\
        bool wr = kaa_tcp_channel_is_ready(channel, FD_WRITE);\
        ASSERT_EQUAL(wr, write);\
    }

/**
 * UNIT TESTS
 */
/*
 * Test channel creation and destroy.
 */
void test_create_kaa_tcp_channel(void **state)
{
    (void)state;

    kaa_error_t error_code;

    kaa_transport_channel_interface_t *channel = NULL;
    channel = KAA_CALLOC(1,sizeof(kaa_transport_channel_interface_t));

    kaa_extension_id operation_services[] = {
            KAA_EXTENSION_PROFILE,
            KAA_EXTENSION_USER,
            KAA_EXTENSION_EVENT,
            KAA_EXTENSION_LOGGING};

    error_code = kaa_tcp_channel_create(channel, logger, operation_services, 4);

    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_NOT_NULL(channel->context);
    ASSERT_NOT_NULL(channel->get_protocol_id);
    ASSERT_NOT_NULL(channel->get_supported_services);
    ASSERT_NOT_NULL(channel->init);
    ASSERT_NOT_NULL(channel->destroy);
    ASSERT_NOT_NULL(channel->set_access_point);
    ASSERT_NOT_NULL(channel->sync_handler);

    kaa_transport_protocol_id_t protocol_info;
    error_code = channel->get_protocol_id(channel->context, &protocol_info);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_EQUAL(protocol_info.id, 0x56c8ff92);
    ASSERT_EQUAL(protocol_info.version, 1);

    kaa_extension_id *r_supported_services;
    size_t r_supported_service_count = 0;
    error_code = channel->get_supported_services(channel->context, &r_supported_services, &r_supported_service_count);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_EQUAL(r_supported_service_count, 4);
    ASSERT_EQUAL(r_supported_services[0], KAA_EXTENSION_PROFILE);
    ASSERT_EQUAL(r_supported_services[1], KAA_EXTENSION_USER);
    ASSERT_EQUAL(r_supported_services[2], KAA_EXTENSION_EVENT);
    ASSERT_EQUAL(r_supported_services[3], KAA_EXTENSION_LOGGING);

    channel->destroy(channel->context);

    KAA_FREE(channel);
}


/**
 * Test Operations success flow:
 *  1. Set access point, check connecting and successful connect
 *  2. Authorize, send CONNECT and receive CONACK
 *  3. Receive Disconnect message, check connection drop and notify Bootstrap of AP failure
 */
void test_kaa_tcp_channel_success_flow(void **state)
{
    (void)state;

    kaa_error_t error_code;

    kaa_transport_channel_interface_t *channel = NULL;
    channel = KAA_CALLOC(1,sizeof(kaa_transport_channel_interface_t));

    kaa_extension_id operation_services[] = {
            KAA_EXTENSION_PROFILE,
            KAA_EXTENSION_USER,
            KAA_EXTENSION_EVENT,
            KAA_EXTENSION_LOGGING};

    error_code = kaa_tcp_channel_create(channel, logger, operation_services, 4);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    test_set_access_point(channel);

    test_check_channel_auth(channel);

    test_send_disconnect(channel);

    channel->destroy(channel->context);

    KAA_FREE(channel);
}

/**
 * Test Operations sync flow:
 *  1. Set access point, check connecting and successful connect
 *  2. Authorize, send CONNECT and receive CONACK
 *  3. Call Sync for EVENT than EVENT,LOGGING, check send SYNC for EVENT,LOGGING, receive SYNC.
 *  4. Receive Disconnect message, check connection drop and notify Bootstrap manager of AP failure
 */
void test_kaa_tcp_channel_sync_flow(void **state)
{
    (void)state;

    kaa_error_t error_code;

    kaa_transport_channel_interface_t *channel = NULL;
    channel = KAA_CALLOC(1,sizeof(kaa_transport_channel_interface_t));

    kaa_extension_id operation_services[] = {
            KAA_EXTENSION_PROFILE,
            KAA_EXTENSION_USER,
            KAA_EXTENSION_EVENT,
            KAA_EXTENSION_LOGGING};

    error_code = kaa_tcp_channel_create(channel, logger, operation_services, 4);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    test_set_access_point(channel);

    test_check_channel_auth(channel);

    //Call sync
    kaa_extension_id services1[] = {KAA_EXTENSION_PROFILE, KAA_EXTENSION_USER};
    channel->sync_handler(channel->context, services1, 2);

    kaa_extension_id services2[] = {KAA_EXTENSION_PROFILE, KAA_EXTENSION_EVENT};
    channel->sync_handler(channel->context, services2, 2);

    kaa_extension_id services3[] = {KAA_EXTENSION_LOGGING};
    channel->sync_handler(channel->context, services3, 1);

    //Check correct RD,WR operation, in this point we waiting for RD operations true
    //and WR true, pending services and empty buffer.
    CHECK_SOCKET_RW(channel, true, true);

    test_sync_exchange(channel);

    test_send_disconnect(channel);

    channel->destroy(channel->context);

    KAA_FREE(channel);
}

/**
 * Test Operations flow on IO error:
 *  1. Set access point, check connecting and successful connect
 *  2. Authorize, send CONNECT and receive CONACK
 *  4. Imitate IO error on read.
 *  5. check disconnect notification
 */
void test_kaa_tcp_channel_io_error_flow(void **state)
{
    (void)state;

    kaa_error_t error_code;

    kaa_transport_channel_interface_t *channel = NULL;
    channel = KAA_CALLOC(1,sizeof(kaa_transport_channel_interface_t));

    kaa_extension_id operation_services[] = {
            KAA_EXTENSION_PROFILE,
            KAA_EXTENSION_USER,
            KAA_EXTENSION_EVENT,
            KAA_EXTENSION_LOGGING};

    error_code = kaa_tcp_channel_create(channel, logger, operation_services, 4);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    test_set_access_point(channel);

    test_check_channel_auth(channel);

    //Imitate error
    //Imitate socket ready for reading, and got IO error during read
    access_point_test_info.socket_connecting_error_scenario = true;
    access_point_test_info.bootstrap_manager_on_access_point_failed = false;
    error_code = kaa_tcp_channel_process_event(channel, FD_READ);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(access_point_test_info.socket_disconnected_callback, true);
    ASSERT_EQUAL(access_point_test_info.socket_disconnected_closed, true);
    ASSERT_EQUAL(access_point_test_info.bootstrap_manager_on_access_point_failed, true);

    kaa_fd_t fd = -1;

    error_code = kaa_tcp_channel_get_descriptor(channel, &fd);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    //Check assigned fd
    ASSERT_EQUAL(fd, -1);


    channel->destroy(channel->context);

    KAA_FREE(channel);
}

/**
 * Test authorization sequence according to bug KAA-362
 * 1. Set access point
 * 2. Call sync several times before authorization complete, check that CONNECT is generated only once.
 * 3. Disconnect.
 */
void test_kaa_tcp_channel_auth_double_sync_flow(void **state)
{
    (void)state;

    kaa_error_t error_code;

    kaa_transport_channel_interface_t *channel = NULL;
    channel = KAA_CALLOC(1, sizeof(kaa_transport_channel_interface_t));

    kaa_extension_id operation_services[] = {
            KAA_EXTENSION_PROFILE,
            KAA_EXTENSION_USER,
            KAA_EXTENSION_EVENT,
            KAA_EXTENSION_LOGGING};

    error_code = kaa_tcp_channel_create(channel, logger, operation_services, 4);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    test_set_access_point(channel);


    access_point_test_info.socket_connected          = false;
    access_point_test_info.socket_connected_callback = false;
    access_point_test_info.fill_connect_message      = false;
    access_point_test_info.request_connect           = false;
    access_point_test_info.auth_packet_written       = false;
    access_point_test_info.connack_read              = false;

    //Imitate WR event, and wait socket connect call, channel should start authorization, prepare
    //CONNECT message
    error_code = kaa_tcp_channel_process_event(channel, FD_WRITE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(access_point_test_info.socket_connected, true);
    ASSERT_EQUAL(access_point_test_info.socket_connected_callback, true);
    ASSERT_EQUAL(access_point_test_info.fill_connect_message, true);
    ASSERT_EQUAL(access_point_test_info.request_connect, true);

    //Check correct RD,WR operation, in this point we waiting for RD,WR operations true
    CHECK_SOCKET_RW(channel, true, true);

    //Test first sync during AUTH waiting
    access_point_test_info.fill_connect_message      = false;
    access_point_test_info.request_connect           = false;

    //Call sync
    kaa_extension_id services1[] = {KAA_EXTENSION_PROFILE, KAA_EXTENSION_USER};
    channel->sync_handler(channel->context, services1, 2);
    ASSERT_EQUAL(access_point_test_info.fill_connect_message, false);
    ASSERT_EQUAL(access_point_test_info.request_connect, false);

    //Imitate socket ready for writing, and writing CONNECT message
    error_code = kaa_tcp_channel_process_event(channel, FD_WRITE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(access_point_test_info.auth_packet_written, true);
    ASSERT_EQUAL(access_point_test_info.fill_connect_message, false);
    ASSERT_EQUAL(access_point_test_info.request_connect, false);
    //Check correct RD,WR operation, in this point we waiting for RD operations true
    //and WR false, no pending services and empty buffer.
    CHECK_SOCKET_RW(channel, true, false);

    //Test second sync during AUTH waiting
    access_point_test_info.fill_connect_message      = false;
    access_point_test_info.request_connect           = false;
    access_point_test_info.auth_packet_written       = false;

    kaa_extension_id services2[] = {KAA_EXTENSION_PROFILE, KAA_EXTENSION_EVENT};
    channel->sync_handler(channel->context, services2, 2);
    ASSERT_EQUAL(access_point_test_info.fill_connect_message, false);
    ASSERT_EQUAL(access_point_test_info.request_connect, false);
    //Check correct RD,WR operation, in this point we waiting for RD operations true
    //and WR false, no pending services and empty buffer.
    CHECK_SOCKET_RW(channel, true, false);

    //Imitate socket ready for reading, and read CONNACK message
    error_code = kaa_tcp_channel_process_event(channel, FD_READ);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(access_point_test_info.connack_read, true);

    //Check correct RD,WR operation, in this point we waiting for RD operations true
    //and WR true, number of pending services and empty buffer.
    //Checking receiving KAA_SYNC message
    CHECK_SOCKET_RW(channel, true, true);

    //Third sync
    kaa_extension_id services3[] = {KAA_EXTENSION_LOGGING};
    channel->sync_handler(channel->context, services3, 1);

    //Check correct RD,WR operation, in this point we waiting for RD operations true
    //and WR true, pending services and empty buffer.
    CHECK_SOCKET_RW(channel, true, true);

    test_sync_exchange(channel);

    test_send_disconnect(channel);

    channel->destroy(channel->context);

    KAA_FREE(channel);
}


void test_sync_exchange(kaa_transport_channel_interface_t *channel)
{
    ASSERT_NOT_NULL(channel);

    access_point_test_info.kaasync_write     = false;
    access_point_test_info.kaasync_read      = false;
    access_point_test_info.kaasync_processed = false;

    //Imitate socket ready for writing, and writing KAASYNC message
    access_point_test_info.kaasync_read_scenario = true;
    kaa_error_t error_code = kaa_tcp_channel_process_event(channel, FD_WRITE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(access_point_test_info.kaasync_write, true);

    //Check correct RD,WR operation, in this point we waiting for RD operations true
    //and WR false, no pending services and empty buffer.
    CHECK_SOCKET_RW(channel, true, false);

    //Imitate socket ready for reading, and read KAA_SYNC Bootstrap message
    error_code = kaa_tcp_channel_process_event(channel, FD_READ);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(access_point_test_info.kaasync_read, true);
    ASSERT_EQUAL(access_point_test_info.kaasync_processed, true);

}

void test_send_disconnect(kaa_transport_channel_interface_t *channel)
{
    ASSERT_NOT_NULL(channel);

    access_point_test_info.disconnect_read                          = false;
    access_point_test_info.socket_disconnected_callback             = false;
    access_point_test_info.socket_disconnected_closed               = false;
    access_point_test_info.bootstrap_manager_on_access_point_failed = false;



    //Check correct RD,WR operation, in this point we waiting for RD operations true
    //and WR false, no pending services and empty buffer.
    CHECK_SOCKET_RW(channel, true, false);

    //Imitate sending Disconnect message with reason None from Operation Server
    access_point_test_info.disconnect_create_non_scenario = true;
    kaa_error_t error_code = kaa_tcp_channel_process_event(channel, FD_READ);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(access_point_test_info.disconnect_read, true);

    ASSERT_EQUAL(access_point_test_info.socket_disconnected_callback, true);
    ASSERT_EQUAL(access_point_test_info.socket_disconnected_closed, true);
    ASSERT_EQUAL(access_point_test_info.bootstrap_manager_on_access_point_failed, true);
}

void test_check_channel_auth(kaa_transport_channel_interface_t *channel)
{
    ASSERT_NOT_NULL(channel);

    access_point_test_info.socket_connected          = false;
    access_point_test_info.socket_connected_callback = false;
    access_point_test_info.fill_connect_message      = false;
    access_point_test_info.request_connect           = false;
    access_point_test_info.auth_packet_written       = false;
    access_point_test_info.connack_read              = false;

    //Imitate WR event, and wait socket connect call, channel should start authorization, prepare
    //CONNECT message
    kaa_error_t error_code = kaa_tcp_channel_process_event(channel, FD_WRITE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(access_point_test_info.socket_connected, true);
    ASSERT_EQUAL(access_point_test_info.socket_connected_callback, true);
    ASSERT_EQUAL(access_point_test_info.fill_connect_message, true);
    ASSERT_EQUAL(access_point_test_info.request_connect, true);

    //Check correct RD,WR operation, in this point we waiting for RD,WR operations true
    CHECK_SOCKET_RW(channel, true, true);

    //Imitate socket ready for writing, and writing CONNECT message
    error_code = kaa_tcp_channel_process_event(channel, FD_WRITE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(access_point_test_info.auth_packet_written, true);
    //Check correct RD,WR operation, in this point we waiting for RD operations true
    //and WR false, no pending services and empty buffer.
    CHECK_SOCKET_RW(channel, true, false);


    //Imitate socket ready for reading, and read CONNACK message
    error_code = kaa_tcp_channel_process_event(channel, FD_READ);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(access_point_test_info.connack_read, true);

    //Check correct RD,WR operation, in this point we waiting for RD operations true
    //and WR false, no pending services and empty buffer.
    //Checking receiving KAA_SYNC message
    CHECK_SOCKET_RW(channel, true, false);

}

void test_set_access_point(kaa_transport_channel_interface_t *channel)
{
    ASSERT_NOT_NULL(channel);
    //Fill with fake pointers, just for non null
    kaa_transport_context_t transport_context;
    transport_context.kaa_context = (kaa_context_t*)calloc(1, sizeof(kaa_context_t));
    transport_context.kaa_context->platform_protocol = (kaa_platform_protocol_t *)CONNECTION_DATA;
    transport_context.kaa_context->bootstrap_manager = (kaa_bootstrap_manager_t *)CONNECTION_DATA;

    kaa_error_t error_code = channel->init(channel->context, &transport_context);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_tcp_channel_set_socket_events_callback(channel, kaa_tcp_channel_event_callback_fn, channel);
    assert_int_equal(KAA_ERR_NONE, error_code);

    //Use connection data to destination 192.168.77.2:9888
    kaa_access_point_t access_point;
    access_point.id = 10;
    access_point.connection_data = (char *)CONNECTION_DATA;
    access_point.connection_data_len = sizeof(CONNECTION_DATA);

    reset_access_point_test_info();

    error_code = channel->set_access_point(channel->context, &access_point);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code= kaa_tcp_channel_check_keepalive(channel);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    //Check correct call of gethostbyaddr
    ASSERT_EQUAL(access_point_test_info.gethostbyaddr_requested, true);
    //Check if new socket created
    ASSERT_EQUAL(access_point_test_info.new_socket_created, true);

    kaa_fd_t fd = -1;

    error_code = kaa_tcp_channel_get_descriptor(channel, &fd);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    //Check assigned fd
    ASSERT_EQUAL(fd, ACCESS_POINT_SOCKET_FD);

    access_point_test_info.fd = fd;


    //Check correct RD,WR operation, in this point we waiting for correct Connect to destination
    //So RD should be false, WR true
    CHECK_SOCKET_RW(channel, false, true);
}

kaa_error_t kaa_bootstrap_manager_on_access_point_failed(kaa_bootstrap_manager_t *self
                                                       , kaa_transport_protocol_id_t *protocol_id
                                                       , kaa_server_type_t type)
{
    (void)self;
    ASSERT_EQUAL(protocol_id->id, 0x56c8ff92);
    ASSERT_EQUAL(protocol_id->version, 1);
    ASSERT_EQUAL(type, KAA_SERVER_OPERATIONS);
    access_point_test_info.bootstrap_manager_on_access_point_failed  = true;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_tcp_channel_event_callback_fn(void *context, kaa_tcp_channel_event_t event_type, kaa_fd_t fd)
{
    (void)context;
    if (fd != access_point_test_info.fd) {
        return KAA_ERR_BADPARAM;
    }
    switch (event_type) {
        case SOCKET_CONNECTED:
            access_point_test_info.socket_connected_callback = true;
            break;
        case SOCKET_DISCONNECTED:
            access_point_test_info.socket_disconnected_callback = true;
            break;
        case SOCKET_CONNECTION_ERROR:
            access_point_test_info.socket_connecting_error_callback = true;
            break;
    }
    return KAA_ERR_NONE;
}

kaatcp_error_t kaatcp_get_request_connect(const kaatcp_connect_t *message
                                        , char *buf
                                        , size_t *buf_size)
{
    if (message->protocol_name_length != KAA_TCP_NAME_LENGTH) {
        return KAATCP_ERR_BAD_PARAM;
    }
    if (message->protocol_version != PROTOCOL_VERSION) {
        return KAATCP_ERR_BAD_PARAM;
    }
    if (message->next_ptorocol_id != 0x3553c66f) {
        return KAATCP_ERR_BAD_PARAM;
    }
    if (message->connect_flags != KAA_CONNECT_FLAGS) {
        return KAATCP_ERR_BAD_PARAM;
    }
    if (message->keep_alive != (KEEPALIVE * 1.2)) {
        return KAATCP_ERR_BAD_PARAM;
    }
    if (message->sync_request && message->sync_request_size == sizeof(CONNECT_PACK)) {
        if (!memcmp(CONNECT_PACK, message->sync_request, message->sync_request_size)) {
            *buf_size = 0;
            memcpy(buf, CONNECT_HEAD, sizeof(CONNECT_HEAD));
            *buf_size += sizeof(CONNECT_HEAD);
            memcpy(buf+(*buf_size), message->sync_request, message->sync_request_size);
            *buf_size += message->sync_request_size;
            access_point_test_info.request_connect = true;
            return KAATCP_ERR_NONE;
        }
    }
    return KAATCP_ERR_BAD_PARAM;
}

kaatcp_error_t kaatcp_fill_connect_message(uint16_t keepalive, uint32_t next_protocol_id
                                         , char *sync_request, size_t sync_request_size
                                         , char *session_key, size_t session_key_size
                                         , char *signature, size_t signature_size
                                         , kaatcp_connect_t *message)
{
    if (keepalive != (KEEPALIVE * 1.2)) {
        return KAATCP_ERR_BAD_PARAM;
    }
    if (next_protocol_id != 0x3553c66f) {
        return KAATCP_ERR_BAD_PARAM;
    }
    if (sync_request && sync_request_size == sizeof(CONNECT_PACK)) {
        if (!memcmp(CONNECT_PACK, sync_request, sync_request_size)) {
            memset(message, 0, sizeof(kaatcp_connect_t));

            message->protocol_name_length = KAA_TCP_NAME_LENGTH;
            memcpy(message->protocol_name, KAA_TCP_NAME, KAA_TCP_NAME_LENGTH);

            message->protocol_version = PROTOCOL_VERSION;
            message->connect_flags = KAA_CONNECT_FLAGS;

            message->next_ptorocol_id = next_protocol_id;

            if (session_key) {
               message->session_key = session_key;
               message->session_key_size = session_key_size;
               message->session_key_flags = KAA_CONNECT_KEY_AES_RSA;
            }
            if (signature) {
               message->signature = signature;
               message->signature_size = signature_size;
               message->signature_flags = KAA_CONNECT_SIGNATURE_SHA1;
            }

            message->keep_alive = keepalive;

            message->sync_request_size = sync_request_size;
            message->sync_request = sync_request;

            access_point_test_info.fill_connect_message = true;

            return KAATCP_ERR_NONE;
        }
    }
    return KAATCP_ERR_BAD_PARAM;
}

kaa_error_t kaa_platform_protocol_process_server_sync(kaa_platform_protocol_t *self
                                                    , const char *buffer
                                                    , size_t buffer_size)
{
    (void)self;
    (void)buffer_size;
    KAA_RETURN_IF_NIL(buffer, KAA_ERR_BADPARAM);

    if (!memcmp(buffer, KAASYNC_OP_MESSAGE, strlen(KAASYNC_OP_MESSAGE))) {
        access_point_test_info.kaasync_processed = true;
        return KAA_ERR_NONE;
    }
    return KAA_ERR_BADPARAM;
}

kaa_error_t kaa_platform_protocol_alloc_serialize_client_sync(kaa_platform_protocol_t *self,
        const extension_id *services, size_t services_count,
        char **buffer, size_t *buffer_size)
{

    if (services_count == 4
            && services[0] == KAA_EXTENSION_PROFILE
            && services[1] == KAA_EXTENSION_USER
            && services[2] == KAA_EXTENSION_EVENT
            && services[3] == KAA_EXTENSION_LOGGING) {
        char *alloc_buffer = KAA_MALLOC(sizeof(CONNECT_PACK));
        if (alloc_buffer) {
            memcpy(alloc_buffer, CONNECT_PACK, sizeof(CONNECT_PACK));
            *buffer = alloc_buffer;
            *buffer_size = sizeof(CONNECT_PACK);
            return KAA_ERR_NONE;
        }
    }

    return KAA_ERR_BADPARAM;
}


ext_tcp_socket_state_t ext_tcp_utils_tcp_socket_check(kaa_fd_t fd, const kaa_sockaddr_t *destination, kaa_socklen_t destination_size)
{

    if (fd == ACCESS_POINT_SOCKET_FD) {
        unsigned char *dst = (unsigned char*)destination;
        if(!memcmp(dst, DESTINATION_SOCKADDR, destination_size)) {
            if (access_point_test_info.socket_connecting_error_scenario) {
                return KAA_TCP_SOCK_ERROR;
            } else {
                access_point_test_info.socket_connected = true;
            }
        }
    }
    return KAA_TCP_SOCK_CONNECTED;
}

ext_tcp_utils_function_return_state_t ext_tcp_utils_getaddrbyhost(kaa_dns_resolve_listener_t *resolve_listener, const kaa_dns_resolve_info_t *resolve_props, kaa_sockaddr_t *result, kaa_socklen_t *result_size)
{
    (void)resolve_listener;
    KAA_RETURN_IF_NIL4(resolve_props, resolve_props->hostname, result, result_size, RET_STATE_VALUE_ERROR);
    if (*result_size < sizeof(struct sockaddr_in))
        return RET_STATE_BUFFER_NOT_ENOUGH;


    char hostname_str[resolve_props->hostname_length + 1];
    memcpy(hostname_str, resolve_props->hostname, resolve_props->hostname_length);
    hostname_str[resolve_props->hostname_length] = '\0';

    KAA_LOG_INFO(logger,KAA_ERR_NONE,"getaddrbyhost() Hostname=%s:%d", hostname_str, resolve_props->port);


    struct addrinfo hints;
    memset(&hints, 0 , sizeof(struct addrinfo));
    hints.ai_socktype = SOCK_STREAM;
    if (*result_size < sizeof(struct sockaddr_in6))
        hints.ai_family = AF_INET;

    struct addrinfo *resolve_result = NULL;
    int resolve_error = 0;

    if (resolve_props->port) {
        char port_str[6];
        snprintf(port_str, 6, "%u", resolve_props->port);
        resolve_error = getaddrinfo(hostname_str, port_str, &hints, &resolve_result);
    } else {
        resolve_error = getaddrinfo(hostname_str, NULL, &hints, &resolve_result);
    }

    if (resolve_error || !resolve_result)
        return RET_STATE_VALUE_ERROR;

    if (resolve_result->ai_addrlen > *result_size) {
        freeaddrinfo(resolve_result);
        return RET_STATE_BUFFER_NOT_ENOUGH;
    }

    memcpy(result, resolve_result->ai_addr, resolve_result->ai_addrlen);
    *result_size = resolve_result->ai_addrlen;
    freeaddrinfo(resolve_result);

    access_point_test_info.gethostbyaddr_requested = true;

    return RET_STATE_VALUE_READY;
}

kaa_error_t ext_tcp_utils_tcp_socket_close(kaa_fd_t fd)
{
    if (fd != access_point_test_info.fd) {
        return KAA_ERR_BADPARAM;
    }
    access_point_test_info.socket_disconnected_closed = true;
    return KAA_ERR_NONE;
}

ext_tcp_socket_io_errors_t ext_tcp_utils_tcp_socket_read(kaa_fd_t fd, char *buffer, size_t buffer_size, size_t *bytes_read)
{
    (void)buffer_size;
    KAA_RETURN_IF_NIL(buffer,KAA_TCP_SOCK_IO_ERROR);

    if (fd != access_point_test_info.fd) {
        return KAA_TCP_SOCK_IO_ERROR;
    }

    if (access_point_test_info.socket_connecting_error_scenario) {
        *bytes_read = 0;
        return KAA_TCP_SOCK_IO_ERROR;
    } else if(access_point_test_info.disconnect_create_non_scenario) {
        memcpy(buffer, DISCONNECT_NONE, sizeof(DISCONNECT_NONE));
        *bytes_read = sizeof(DISCONNECT_NONE);
        access_point_test_info.disconnect_read = true;
    } else if (!access_point_test_info.connack_read) {
        memcpy(buffer, CONNACK, sizeof(CONNACK));
        *bytes_read = sizeof(CONNACK);
        access_point_test_info.connack_read = true;
    } else if (!access_point_test_info.kaasync_read) {
        memcpy(buffer, KAASYNC_OP, sizeof(KAASYNC_OP));
        *bytes_read = sizeof(KAASYNC_OP);
        access_point_test_info.kaasync_read = true;
    }

    return KAA_TCP_SOCK_IO_OK;
}

ext_tcp_socket_io_errors_t ext_tcp_utils_tcp_socket_write(kaa_fd_t fd, const char *buffer, size_t buffer_size, size_t *bytes_written)
{
    KAA_RETURN_IF_NIL(buffer,KAA_TCP_SOCK_IO_ERROR);

    if (fd != access_point_test_info.fd) {
        return KAA_TCP_SOCK_IO_ERROR;
    }

    if (access_point_test_info.kaasync_read_scenario) {
        if (!memcmp(buffer, KAASYNC_OP_SERV, sizeof(KAASYNC_OP_SERV))) {
            access_point_test_info.kaasync_write = true;
            *bytes_written = sizeof(KAASYNC_OP_SERV);
            return KAA_TCP_SOCK_IO_OK;
        }

        *bytes_written = buffer_size;
        return KAA_TCP_SOCK_IO_OK;
    } else if (!access_point_test_info.auth_packet_written) {
        if (buffer_size != (sizeof(CONNECT_HEAD) + sizeof(CONNECT_PACK))) {
            return KAA_TCP_SOCK_IO_ERROR;
        }
        *bytes_written = 0;
        if (!memcmp(buffer, CONNECT_HEAD, sizeof(CONNECT_HEAD))) {
            if (!memcmp(buffer+sizeof(CONNECT_HEAD), CONNECT_PACK, sizeof(CONNECT_PACK))) {
                access_point_test_info.auth_packet_written = true;
                *bytes_written = sizeof(CONNECT_HEAD) + sizeof(CONNECT_PACK);
                return KAA_TCP_SOCK_IO_OK;
            }
        }
    } else if (!access_point_test_info.socket_disconnected_write) {
        if (buffer_size != sizeof(DISCONNECT_MESSAGE)) {
            return KAA_TCP_SOCK_IO_ERROR;
        }
        if (!memcmp(buffer, DISCONNECT_MESSAGE, sizeof(DISCONNECT_MESSAGE))) {
            access_point_test_info.socket_disconnected_write = true;
            *bytes_written = sizeof(DISCONNECT_MESSAGE);
            return KAA_TCP_SOCK_IO_OK;
        }
    }

    return KAA_TCP_SOCK_IO_ERROR;
}

kaa_error_t ext_tcp_utils_open_tcp_socket(kaa_fd_t *fd, const kaa_sockaddr_t *destination, kaa_socklen_t destination_size)
{
    KAA_RETURN_IF_NIL3(fd, destination, destination_size, KAA_ERR_BADPARAM);

    unsigned char *dst = (unsigned char*)destination;

    if(!memcmp(dst, DESTINATION_SOCKADDR, destination_size)) {
        access_point_test_info.new_socket_created = true;
        *fd = ACCESS_POINT_SOCKET_FD;
        return KAA_ERR_NONE;
    }

    return KAA_ERR_BADPARAM;
}

int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger)
        return error;


    return 0;
}

int test_deinit(void)
{
    kaa_log_destroy(logger);
    return 0;
}

KAA_SUITE_MAIN(Log, test_init, test_deinit,
        KAA_TEST_CASE(create_kaa_tcp_channel, test_create_kaa_tcp_channel)
        KAA_TEST_CASE(create_kaa_tcp_channel_success_flow, test_kaa_tcp_channel_success_flow)
        KAA_TEST_CASE(create_kaa_tcp_channel_sync_flow, test_kaa_tcp_channel_sync_flow)
        KAA_TEST_CASE(create_kaa_tcp_channel_io_error_flow, test_kaa_tcp_channel_io_error_flow)
        KAA_TEST_CASE(create_kaa_tcp_channel_auth_double_sync_flow, test_kaa_tcp_channel_auth_double_sync_flow)
        )

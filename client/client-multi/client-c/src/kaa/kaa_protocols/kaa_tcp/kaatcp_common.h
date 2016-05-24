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

#ifndef KAATCP_COMMON_H_
#define KAATCP_COMMON_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <platform/defaults.h>

#define KAATCP_PROTOCOL_NAME_MAX_SIZE 10

#define FIRST_BIT                0x80
#define MAX_MESSAGE_TYPE_LENGTH  0x0F
#define MAX_MESSAGE_LENGTH       0x0FFFFFFF
#define PROTOCOL_VERSION         0x01

#define KAA_SYNC_HEADER_LENGTH 12
#define KAA_SYNC_ZIPPED_BIT    0x02
#define KAA_SYNC_ENCRYPTED_BIT 0x04
#define KAA_SYNC_REQUEST_BIT   0x01
#define KAA_SYNC_SYNC_BIT      0x10
#define KAA_SYNC_BOOTSTRAP_BIT 0x20

#define KAA_BOOTSTRAP_RSA_PKSC8         0x01
#define KAA_BOOTSTRAP_CHANNEL_HTTP      0x01
#define KAA_BOOTSTRAP_CHANNEL_HTTPLP    0x02
#define KAA_BOOTSTRAP_CHANNEL_KAATCP    0x03

#define KAA_CONNECT_FLAGS          0x02
#define KAA_CONNECT_HEADER_LENGTH  18

#define KAA_CONNECT_KEY_AES_RSA    0x11
#define KAA_CONNECT_SIGNATURE_SHA1 0x01

#define KAA_TCP_NAME        "Kaatcp"
#define KAA_TCP_NAME_LENGTH 6

#define KAA_PING_MESSAGE_SIZE 2
#define KAA_DISCONNECT_MESSAGE_SIZE 4

typedef enum {
    KAATCP_ERR_NONE              = 0,
    KAATCP_ERR_NOMEM             = -1,
    KAATCP_ERR_BUFFER_NOT_ENOUGH = -2,
    KAATCP_ERR_BAD_PARAM         = -3,
    KAATCP_ERR_INVALID_STATE     = -4,
    KAATCP_ERR_INVALID_PROTOCOL  = -5
} kaatcp_error_t;

typedef enum {
    KAATCP_MESSAGE_UNKNOWN    = 0x00,
    KAATCP_MESSAGE_CONNECT    = 0x01,
    KAATCP_MESSAGE_CONNACK    = 0x02,
    KAATCP_MESSAGE_PINGREQ    = 0x0C,
    KAATCP_MESSAGE_PINGRESP   = 0x0D,
    KAATCP_MESSAGE_DISCONNECT = 0x0E,
    KAATCP_MESSAGE_KAASYNC    = 0x0F
} kaatcp_message_type_t;

typedef struct {
    uint16_t protocol_name_length;
    char protocol_name[KAATCP_PROTOCOL_NAME_MAX_SIZE];

    uint8_t protocol_version;

    uint8_t connect_flags;

    uint32_t next_ptorocol_id;

    uint8_t session_key_flags;
    uint8_t signature_flags;

    uint16_t keep_alive;

    size_t session_key_size;
    char *session_key;

    size_t signature_size;
    char *signature;

    size_t sync_request_size;
    char *sync_request;

} kaatcp_connect_t;

typedef enum {
    KAATCP_CONNACK_UNKNOWN                    = 0x00,
    KAATCP_CONNACK_SUCCESS                    = 0x01,
    KAATCP_CONNACK_UNACCEPTABLE_VERSION       = 0x02,
    KAATCP_CONNACK_IDENTIFIER_REJECTED        = 0x03,
    KAATCP_CONNACK_SERVER_UNAVAILABLE         = 0x04,
    KAATCP_CONNACK_REFUSE_BAD_CREDENTIALS     = 0x05,
    KAATCP_CONNACK_NOT_AUTHORIZED             = 0x06,
    KAATCP_CONNACK_REFUSE_VERIFICATION_FAILED = 0x10,
} kaatcp_connack_code_t;

typedef struct {
    uint16_t return_code;
} kaatcp_connack_t;

typedef enum {
    KAATCP_DISCONNECT_NONE                = 0x00,
    KAATCP_DISCONNECT_BAD_REQUEST         = 0x01,
    KAATCP_DISCONNECT_INTERNAL_ERROR      = 0x02,
    KAATCP_DISCONNECT_CREDENTIALS_REVOKED = 0x03,
} kaatcp_disconnect_reason_t;

typedef struct {
    uint16_t reason;
} kaatcp_disconnect_t;

typedef struct {
    uint16_t protocol_name_length;
    char protocol_name[KAATCP_PROTOCOL_NAME_MAX_SIZE];

    uint8_t protocol_version;
    uint16_t message_id;
    uint8_t flags;
} kaatcp_kaasync_header_t;

typedef struct {
    kaatcp_kaasync_header_t sync_header;

    size_t sync_request_size;
    char *sync_request;
} kaatcp_kaasync_t;

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAATCP_COMMON_H_ */

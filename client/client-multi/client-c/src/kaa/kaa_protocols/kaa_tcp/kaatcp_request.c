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
#include <stddef.h>
#include <stdint.h>
#include <sys/types.h>

#include "kaatcp_request.h"

#include "kaa_common.h"
#include <platform/sock.h>



static uint8_t create_basic_header(uint8_t message_type, size_t length, char *message)
{
    if (length <= MAX_MESSAGE_LENGTH && message_type <= MAX_MESSAGE_TYPE_LENGTH) {
        uint8_t size = 1;
        *(message++) = (char) (message_type << 4);
        do {
            uint8_t byte = length % FIRST_BIT;
            length /= FIRST_BIT;
            if (length) {
                byte |= FIRST_BIT;
            }
            *(message++) = (char) byte;
            ++size;
        } while (length);
        return size;
    }
    return 0;
}

// TODO(KAA-1089): Remove weak linkage
__attribute__((weak))
kaatcp_error_t kaatcp_fill_connect_message(uint16_t keepalive, uint32_t next_protocol_id
                                         , char *sync_request, size_t sync_request_size
                                         , char *session_key, size_t session_key_size
                                         , char *signature, size_t signature_size
                                         , kaatcp_connect_t *message)
{
    KAA_RETURN_IF_NIL2(sync_request, sync_request_size, KAATCP_ERR_BAD_PARAM);
    if ((session_key_size > 0 && !session_key) || (signature_size > 0 && !signature))
        return KAATCP_ERR_BAD_PARAM;
    KAA_RETURN_IF_NIL(message, KAATCP_ERR_BAD_PARAM);

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

    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_get_request_size(const kaatcp_connect_t *message, kaatcp_message_type_t type, size_t *size)
{
    KAA_RETURN_IF_NIL2(message, size, KAATCP_ERR_BAD_PARAM);

    size_t payload_size = message->sync_request_size
                        + message->session_key_size
                        + message->signature_size
                        + KAA_CONNECT_HEADER_LENGTH;

    if (payload_size <= MAX_MESSAGE_LENGTH && type <= MAX_MESSAGE_TYPE_LENGTH) {
        uint8_t header_size = 1;
        do {
            payload_size /= FIRST_BIT;
            ++header_size;
        } while (payload_size);

        *size = payload_size + header_size;
        return (kaatcp_error_t)KAA_ERR_NONE;
    }

    return (kaatcp_error_t)KAA_ERR_BADPARAM;

}

// TODO: Remove weak linkage
__attribute__((weak))
kaatcp_error_t kaatcp_get_request_connect(const kaatcp_connect_t *message
                                        , char *buf
                                        , size_t *buf_size)
{
    KAA_RETURN_IF_NIL3(message, buf, buf_size, KAATCP_ERR_BAD_PARAM);

    char header[6];
    size_t payload_size = message->sync_request_size
                        + message->session_key_size
                        + message->signature_size
                        + KAA_CONNECT_HEADER_LENGTH;

    uint8_t header_size = create_basic_header(KAATCP_MESSAGE_CONNECT
                                            , payload_size
                                            , header);

    if ((*buf_size) < payload_size + header_size) {
        return KAATCP_ERR_BUFFER_NOT_ENOUGH;
    }

    char *cursor = buf;

    memcpy(cursor, header, header_size);
    cursor += header_size;

    uint16_t name_length = KAA_HTONS(message->protocol_name_length);
    memcpy(cursor, &name_length, sizeof(uint16_t));
    cursor += sizeof(uint16_t);

    memcpy(cursor, message->protocol_name, message->protocol_name_length);
    cursor += message->protocol_name_length;

    *(cursor++) = PROTOCOL_VERSION;
    *(cursor++) = message->connect_flags;

    uint32_t next_protocol_id = KAA_HTONL(message->next_ptorocol_id);
    memcpy(cursor, &next_protocol_id, sizeof(uint32_t));
    cursor += sizeof(uint32_t);

    *(cursor++) = message->session_key_flags;
    *(cursor++) = message->signature_flags;

    uint16_t keep_alive = KAA_HTONS(message->keep_alive);
    memcpy(cursor, &keep_alive, sizeof(uint16_t));
    cursor += sizeof(uint16_t);

    if (message->session_key && message->session_key_flags) {
        memcpy(cursor, message->session_key, message->session_key_size);
        cursor += message->session_key_size;
    }
    if (message->signature && message->signature_flags) {
        memcpy(cursor, message->signature, message->signature_size);
        cursor += message->signature_size;
    }

    if (message->sync_request) {
        memcpy(cursor, message->sync_request, message->sync_request_size);
        cursor += message->sync_request_size;
    }
    *buf_size = cursor - buf;
    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_fill_disconnect_message(kaatcp_disconnect_reason_t return_code, kaatcp_disconnect_t *message)
{
    KAA_RETURN_IF_NIL(message, KAATCP_ERR_BAD_PARAM);
    message->reason = return_code;
    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_get_request_disconnect(const kaatcp_disconnect_t *message, char *buf, size_t *buf_size)
{
    KAA_RETURN_IF_NIL3(message, buf, buf_size, KAATCP_ERR_BAD_PARAM);

    if (*buf_size < KAA_DISCONNECT_MESSAGE_SIZE) {
        return KAATCP_ERR_BUFFER_NOT_ENOUGH;
    }
    char *cursor = buf;
    create_basic_header(KAATCP_MESSAGE_DISCONNECT, 2, cursor);
    cursor += 2;

    *(cursor++) = 0;
    *(cursor++) = (message->reason & 0xFF);

    *buf_size = KAA_DISCONNECT_MESSAGE_SIZE;
    return KAATCP_ERR_NONE;
}

static void kaatcp_fill_kaasync_header(uint16_t message_id, uint8_t zipped, uint8_t encrypted, kaatcp_kaasync_header_t *header)
{
    KAA_RETURN_IF_NIL(header,);

    header->protocol_name_length = KAA_TCP_NAME_LENGTH;
    memcpy(header->protocol_name, KAA_TCP_NAME, KAA_TCP_NAME_LENGTH);

    header->protocol_version = PROTOCOL_VERSION;

    header->message_id = message_id;

    header->flags |= KAA_SYNC_REQUEST_BIT;
    if (zipped) {
        header->flags |= KAA_SYNC_ZIPPED_BIT;
    }
    if (encrypted) {
        header->flags |= KAA_SYNC_ENCRYPTED_BIT;
    }
}

static kaatcp_error_t kaatcp_get_kaasync_header(const kaatcp_kaasync_header_t *sync_header
                                              , size_t payload_length
                                              , char *buf
                                              , size_t *buf_size
                                              , char **end)
{
    KAA_RETURN_IF_NIL5(sync_header, payload_length, buf, buf_size, end, KAATCP_ERR_BAD_PARAM);

    char header[6];
    size_t payload_size = payload_length + KAA_SYNC_HEADER_LENGTH;
    uint8_t header_size = create_basic_header(KAATCP_MESSAGE_KAASYNC, payload_size, header);

    if ((*buf_size) < payload_size + header_size) {
        return KAATCP_ERR_BUFFER_NOT_ENOUGH;
    }

    char *cursor = buf;

    memcpy(cursor, header, header_size);
    cursor += header_size;

    uint16_t name_length = KAA_HTONS(sync_header->protocol_name_length);
    memcpy(cursor, &name_length, sizeof(uint16_t));
    cursor += sizeof(uint16_t);

    memcpy(cursor, sync_header->protocol_name, sync_header->protocol_name_length);
    cursor += sync_header->protocol_name_length;

    *(cursor++) = PROTOCOL_VERSION;

    uint16_t message_id = KAA_HTONS(sync_header->message_id);
    memcpy(cursor, &message_id, sizeof(uint16_t));
    cursor += sizeof(uint16_t);

    *(cursor++) = sync_header->flags;
    *end = cursor;
    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_fill_kaasync_message(char *sync_request, size_t sync_request_size
                                         , uint16_t message_id, uint8_t zipped, uint8_t encrypted
                                         , kaatcp_kaasync_t *message)
{
    KAA_RETURN_IF_NIL3(sync_request, sync_request_size, message, KAATCP_ERR_BAD_PARAM);

    memset(message, 0, sizeof(kaatcp_kaasync_t));
    kaatcp_fill_kaasync_header(message_id, zipped, encrypted, &message->sync_header);
    message->sync_header.flags |= KAA_SYNC_SYNC_BIT;
    message->sync_request = sync_request;
    message->sync_request_size = sync_request_size;

    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_get_request_kaasync(const kaatcp_kaasync_t *message, char *buf, size_t *buf_size)
{
    KAA_RETURN_IF_NIL3(message, buf, buf_size, KAATCP_ERR_BAD_PARAM);

    size_t first_buf_size = *buf_size;
    char *cursor = NULL;
    kaatcp_error_t rval = kaatcp_get_kaasync_header(&message->sync_header
                                                  , message->sync_request_size
                                                  , buf
                                                  , buf_size
                                                  , &cursor);
    KAA_RETURN_IF_ERR(rval);

    if (message->sync_request) {
        if (cursor + message->sync_request_size <= buf + first_buf_size) {
            memcpy(cursor, message->sync_request, message->sync_request_size);
            cursor += message->sync_request_size;
        }
    }
    *buf_size = cursor - buf;
    return first_buf_size < *buf_size ? KAATCP_ERR_BUFFER_NOT_ENOUGH : KAATCP_ERR_NONE;
}


kaatcp_error_t kaatcp_get_request_ping(char *buf, size_t *buf_size)
{
    KAA_RETURN_IF_NIL2(buf, buf_size, KAATCP_ERR_BAD_PARAM);

    if (*buf_size < KAA_PING_MESSAGE_SIZE) {
        return KAATCP_ERR_BUFFER_NOT_ENOUGH;
    }
    create_basic_header(KAATCP_MESSAGE_PINGREQ, 0, buf);
    *buf_size = KAA_PING_MESSAGE_SIZE;
    return KAATCP_ERR_NONE;
}



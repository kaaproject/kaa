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

#include "kaatcp_request.h"

#include <string.h>
#include <arpa/inet.h>

static uint8_t create_basic_header(uint8_t message_type, uint32_t length, char *message)
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

kaatcp_error_t kaatcp_fill_connect_message(uint16_t keepalive, uint32_t next_protocol_id,
        char *sync_request, uint32_t sync_request_size,
        char *session_key, uint32_t session_key_size,
        char *signature, uint32_t signature_size,
        kaatcp_connect_t *message)
{
    if (!message || (sync_request_size && !sync_request)
            || (session_key_size && !session_key) || (signature_size && !signature)) {
        return KAATCP_ERR_BAD_PARAM;
    }
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

kaatcp_error_t kaatcp_get_request_connect(const kaatcp_connect_t *message, char *buf, uint32_t *buf_size)
{
    if (!message || !buf || !buf_size) {
        return KAATCP_ERR_BAD_PARAM;
    }
    char header[6];
    uint32_t payload_size = message->sync_request_size + message->session_key_size + message->signature_size + KAA_CONNECT_HEADER_LENGTH;
    uint8_t header_size = create_basic_header(KAATCP_MESSAGE_CONNECT, payload_size, header);

    if ((*buf_size) < payload_size + header_size) {
        return KAATCP_ERR_BUFFER_NOT_ENOUGH;
    }

    char *cursor = buf;

    memcpy(cursor, header, header_size);
    cursor += header_size;

    uint16_t name_length = htons(message->protocol_name_length);
    memcpy(cursor, &name_length, sizeof(uint16_t));
    cursor += sizeof(uint16_t);

    memcpy(cursor, message->protocol_name, message->protocol_name_length);
    cursor += message->protocol_name_length;

    *(cursor++) = PROTOCOL_VERSION;
    *(cursor++) = message->connect_flags;

    uint32_t next_protocol_id = htonl(message->next_ptorocol_id);
    memcpy(cursor, &next_protocol_id, sizeof(uint32_t));
    cursor += sizeof(uint32_t);

    *(cursor++) = message->session_key_flags;
    *(cursor++) = message->signature_flags;

    uint16_t keep_alive = htons(message->keep_alive);
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
    if (!message) {
        return KAATCP_ERR_BAD_PARAM;
    }
    message->reason = return_code;
    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_get_request_disconnect(const kaatcp_disconnect_t *message, char *buf, uint32_t *buf_size)
{
    if (!message || !buf || !buf_size) {
        return KAATCP_ERR_BAD_PARAM;
    }
    if (*buf_size < 4) {
        return KAATCP_ERR_BUFFER_NOT_ENOUGH;
    }
    char *cursor = buf;
    create_basic_header(KAATCP_MESSAGE_DISCONNECT, 2, cursor);
    cursor += 2;

    *(cursor++) = 0;
    *(cursor++) = (message->reason & 0xFF);

    *buf_size = 4;
    return KAATCP_ERR_NONE;
}

static void kaatcp_fill_kaasync_header(uint16_t message_id, uint8_t zipped, uint8_t encrypted, kaatcp_kaasync_header_t *header)
{
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

static kaatcp_error_t kaatcp_get_kaasync_header(const kaatcp_kaasync_header_t *sync_header, uint32_t payload_length, char *buf, uint32_t *buf_size, char **end)
{
    char header[6];
    uint32_t payload_size = payload_length + KAA_SYNC_HEADER_LENGTH;
    uint8_t header_size = create_basic_header(KAATCP_MESSAGE_KAASYNC, payload_size, header);

    if ((*buf_size) < payload_size + header_size) {
        return KAATCP_ERR_BUFFER_NOT_ENOUGH;
    }

    char *cursor = buf;

    memcpy(cursor, header, header_size);
    cursor += header_size;

    uint16_t name_length = htons(sync_header->protocol_name_length);
    memcpy(cursor, &name_length, sizeof(uint16_t));
    cursor += sizeof(uint16_t);

    memcpy(cursor, sync_header->protocol_name, sync_header->protocol_name_length);
    cursor += sync_header->protocol_name_length;

    *(cursor++) = PROTOCOL_VERSION;

    uint16_t message_id = htons(sync_header->message_id);
    memcpy(cursor, &message_id, sizeof(uint16_t));
    cursor += sizeof(uint16_t);

    *(cursor++) = sync_header->flags;
    *end = cursor;
    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_fill_kaasync_message(char *sync_request,
        uint32_t sync_request_size, uint16_t message_id, uint8_t zipped,
        uint8_t encrypted, kaatcp_kaasync_t *message)
{
    if (!message || (sync_request_size && !sync_request)) {
        return KAATCP_ERR_BAD_PARAM;
    }
    memset(message, 0, sizeof(kaatcp_kaasync_t));
    kaatcp_fill_kaasync_header(message_id, zipped, encrypted, &message->sync_header);
    message->sync_header.flags |= KAA_SYNC_SYNC_BIT;
    message->sync_request = sync_request;
    message->sync_request_size = sync_request_size;
    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_get_request_kaasync(const kaatcp_kaasync_t *message, char *buf, uint32_t *buf_size)
{
    if (!message || !buf || !buf_size) {
        return KAATCP_ERR_BAD_PARAM;
    }
    char *cursor = NULL;
    kaatcp_error_t code = kaatcp_get_kaasync_header(&message->sync_header, message->sync_request_size, buf, buf_size, &cursor);
    if (code) {
        return code;
    }
    if (message->sync_request) {
        memcpy(cursor, message->sync_request, message->sync_request_size);
        cursor += message->sync_request_size;
    }
    *buf_size = cursor - buf;
    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_fill_bootstrap_message(char *application_token, uint16_t message_id, kaatcp_bootstrap_request_t *message)
{
    if (!message || !application_token) {
        return KAATCP_ERR_BAD_PARAM;
    }
    memset(message, 0, sizeof(kaatcp_bootstrap_request_t));
    kaatcp_fill_kaasync_header(message_id, 0, 0, &message->sync_header);
    message->sync_header.flags |= KAA_SYNC_BOOTSTRAP_BIT;
    message->application_token = application_token;
    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_get_request_bootstrap(const kaatcp_bootstrap_request_t *message, char *buf, uint32_t *buf_size)
{
    if (!message || !message->application_token || !buf || !buf_size) {
        return KAATCP_ERR_BAD_PARAM;
    }
    uint16_t app_token_length = strlen(message->application_token);

    char *cursor = NULL;
    kaatcp_error_t code = kaatcp_get_kaasync_header(&message->sync_header, app_token_length, buf, buf_size, &cursor);
    if (code) {
        return code;
    }
    memcpy(cursor, message->application_token, app_token_length);
    cursor += app_token_length;
    *buf_size = cursor - buf;
    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_get_request_ping(char *buf, uint32_t *buf_size)
{
    if (!buf || !buf_size) {
        return KAATCP_ERR_BAD_PARAM;
    }
    if (*buf_size < 2) {
        return KAATCP_ERR_BUFFER_NOT_ENOUGH;
    }
    create_basic_header(KAATCP_MESSAGE_PINGREQ, 0, buf);
    *buf_size = 2;
    return KAATCP_ERR_NONE;
}



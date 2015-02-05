/*
 * Copyright 2014 CyberVision, Inc.
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
#include <stdlib.h>
#include <arpa/inet.h>
#include <stddef.h>
#include "kaatcp_parser.h"




static kaatcp_error_t kaatcp_parser_message_done(kaatcp_parser_t *parser)
{
    switch (parser->message_type) {
        case KAATCP_MESSAGE_CONNACK:
            if (parser->handlers.connack_handler) {
                kaatcp_connack_t connack = { *(parser->payload + 1) };
                parser->handlers.connack_handler(parser->handlers.handlers_context,connack);
            }
            break;
        case KAATCP_MESSAGE_DISCONNECT:
            if (parser->handlers.disconnect_handler) {
                kaatcp_disconnect_t disconnect = { *(parser->payload + 1) };
                parser->handlers.disconnect_handler(parser->handlers.handlers_context,disconnect);
            }
            break;
        case KAATCP_MESSAGE_PINGRESP:
            if (parser->handlers.pingresp_handler) {
                parser->handlers.pingresp_handler(parser->handlers.handlers_context);
            }
            break;
        case KAATCP_MESSAGE_KAASYNC:
        {
            kaatcp_kaasync_header_t sync_header;
            const char *cursor = parser->payload;

            sync_header.protocol_name_length = ntohs(*(uint16_t *) cursor);
            cursor += 2;

            memcpy(sync_header.protocol_name, cursor, sync_header.protocol_name_length);
            sync_header.protocol_name[sync_header.protocol_name_length] = '\0';
            if (strcmp(sync_header.protocol_name, KAA_TCP_NAME)) {
                return KAATCP_ERR_INVALID_PROTOCOL;
            }
            cursor += sync_header.protocol_name_length;

            sync_header.protocol_version = *(cursor++);
            if (sync_header.protocol_version != PROTOCOL_VERSION) {
                return KAATCP_ERR_INVALID_PROTOCOL;
            }

            sync_header.message_id = ntohs(*(uint16_t *) cursor);
            cursor += 2;

            sync_header.flags = *(cursor++);

            if ((sync_header.flags & KAA_SYNC_SYNC_BIT) && parser->handlers.kaasync_handler) {
                kaatcp_kaasync_t *kaasync = (kaatcp_kaasync_t *) malloc(sizeof(kaatcp_kaasync_t));
                if (!kaasync) {
                    return KAATCP_ERR_NOMEM;
                }
                kaasync->sync_header = sync_header;
                kaasync->sync_request_size = parser->message_length - KAA_SYNC_HEADER_LENGTH;
                if (kaasync->sync_request_size) {
                    kaasync->sync_request = (char *) malloc(kaasync->sync_request_size);
                    if (!kaasync->sync_request) {
                        return KAATCP_ERR_NOMEM;
                    }
                    memcpy(kaasync->sync_request, cursor, kaasync->sync_request_size);
                } else {
                    kaasync->sync_request = NULL;
                }
                parser->handlers.kaasync_handler(parser->handlers.handlers_context, kaasync);
            }
            break;
        }
        default:
            break;
    }
    return kaatcp_parser_reset(parser);
}

static void kaatcp_parser_retrieve_message_type(kaatcp_parser_t *parser, uint8_t byte)
{
    parser->message_type = ((byte & 0xFF) >> 4);
}

static kaatcp_error_t kaatcp_parser_process_byte(kaatcp_parser_t *parser, uint8_t byte)
{
    switch (parser->state) {
        case KAATCP_PARSER_STATE_NONE:
            kaatcp_parser_retrieve_message_type(parser, byte);
            parser->state = KAATCP_PARSER_STATE_PROCESSING_LENGTH;
            break;
        case KAATCP_PARSER_STATE_PROCESSING_LENGTH:
            parser->message_length += ((byte & ~FIRST_BIT) * parser->length_multiplier);
            parser->length_multiplier *= FIRST_BIT;
            if (!(byte & FIRST_BIT)) {
                if (parser->message_length) {
                    parser->state = KAATCP_PARSER_STATE_PROCESSING_PAYLOAD;
                } else {
                    return kaatcp_parser_message_done(parser);
                }
            }
            break;
        default:
            return KAATCP_ERR_INVALID_STATE;
    }
    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_parser_reset(kaatcp_parser_t *parser)
{
    if (!parser) {
        return KAATCP_ERR_BAD_PARAM;
    }
    parser->state = KAATCP_PARSER_STATE_NONE;
    parser->message_type = KAATCP_MESSAGE_UNKNOWN;
    parser->message_length = 0;
    parser->processed_payload_length = 0;
    parser->length_multiplier = 1;
    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_parser_init(kaatcp_parser_t *parser, const kaatcp_parser_handlers_t *handlers)
{
    if (kaatcp_parser_reset(parser) || !handlers) {
        return KAATCP_ERR_BAD_PARAM;
    }
    parser->handlers = *handlers;
    return KAATCP_ERR_NONE;
}

kaatcp_error_t kaatcp_parser_process_buffer(kaatcp_parser_t *parser, const char *buf, size_t buf_size)
{
    if (!parser || !buf || !buf_size) {
        return KAATCP_ERR_BAD_PARAM;
    }
    kaatcp_error_t rval = KAATCP_ERR_NONE;
    const char *buf_cursor = buf;
    while (buf_cursor != buf + buf_size) {
        if (parser->state == KAATCP_PARSER_STATE_PROCESSING_PAYLOAD) {
            uint32_t remaining_size = parser->message_length - parser->processed_payload_length;
            uint32_t buffer_remaining_size = buf + buf_size - buf_cursor;
            uint32_t bytes_to_read = (remaining_size > buffer_remaining_size) ? buffer_remaining_size : remaining_size;
            memcpy(parser->payload + parser->processed_payload_length, buf_cursor, bytes_to_read);
            parser->processed_payload_length += bytes_to_read;
            buf_cursor += bytes_to_read;
            if (parser->message_length == parser->processed_payload_length) {
                rval = kaatcp_parser_message_done(parser);
                if (rval) {
                    return rval;
                }
            }
        } else {
            rval = kaatcp_parser_process_byte(parser, *(buf_cursor++));
            if (rval) {
                return rval;
            }
        }
    }
    return rval;
}

void kaatcp_parser_kaasync_destroy(kaatcp_kaasync_t *message)
{
    if (message) {
        if (message->sync_request) {
            free(message->sync_request);
        }
        free(message);
    }
}


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

#include "kaatcp_parser.h"

#include <string.h>
#include <stdlib.h>
#include <arpa/inet.h>

static uint8_t kaatcp_get_padding_from_length(uint32_t length)
{
    uint8_t padding = length % 4;
    return padding ? 4 - padding : 0;
}

static const char * kaatcp_parser_parse_supported_channel(kaatcp_supported_channel_t *channel, const char *begin)
{
    const char *cursor = begin;
    uint32_t channel_size = ntohl(*(uint32_t *) cursor);
    cursor += 4;

    channel->channel_type = *(cursor++);
    channel->hostname_length = *(cursor++);

    channel->port = ntohs(*(uint16_t *)cursor);
    cursor += 2;

    channel->hostname = (char *) malloc(channel->hostname_length + 1);
    if (!channel->hostname) {
        return NULL;
    }
    memcpy(channel->hostname, cursor, channel->hostname_length);
    channel->hostname[channel->hostname_length] = '\0';
    cursor += channel->hostname_length;

    return cursor + kaatcp_get_padding_from_length(channel_size);
}

static const char * kaatcp_parser_parse_server_record(kaatcp_server_record_t *server_record, const char *begin)
{
    const char *cursor = begin;
    cursor += 4; // passing record's length. Record's length is not used in current parser implementation.

    server_record->server_name_length = ntohl(*(uint32_t *) cursor);
    cursor += 4;
    server_record->server_name = (char *) malloc(server_record->server_name_length + 1);
    if (!server_record->server_name) {
        return NULL;
    }
    memcpy(server_record->server_name, cursor, server_record->server_name_length);
    server_record->server_name[server_record->server_name_length] = '\0';
    cursor += server_record->server_name_length;
    cursor += kaatcp_get_padding_from_length(server_record->server_name_length);

    server_record->server_priority = ntohl(*(uint32_t *) cursor);
    cursor += 4;

    server_record->public_key_type = *(cursor++);
    server_record->public_key_unused = *(cursor++);
    server_record->public_key_length = ntohs(*(uint16_t *) cursor);
    cursor += 2;

    if (server_record->public_key_length) {
        server_record->public_key = (char *) malloc(server_record->public_key_length);
        if (!server_record->public_key) {
            return NULL;
        }
        memcpy(server_record->public_key, cursor, server_record->public_key_length);
        cursor += server_record->public_key_length;
    }

    server_record->supported_channels_count = ntohl(*(uint32_t *) cursor);
    cursor += 4;
    if (server_record->supported_channels_count) {
        server_record->supported_channels = (kaatcp_supported_channel_t *) malloc(sizeof(kaatcp_supported_channel_t) * server_record->supported_channels_count);
        if (!server_record->supported_channels) {
            return NULL;
        }
        for (int i = 0; i < server_record->supported_channels_count; ++i) {
            cursor = kaatcp_parser_parse_supported_channel(server_record->supported_channels + i, cursor);
            if (!cursor) {
                return NULL;
            }
        }
    }
    return cursor;
}

static kaatcp_error_t kaatcp_parser_parse_bootstrap_message(kaatcp_bootstrap_response_t *bootstrap, const char *begin, const char *end)
{
    const char *cursor = begin;
    bootstrap->server_count = ntohl(*(uint32_t *) cursor);
    bootstrap->servers = (kaatcp_server_record_t *) malloc(sizeof(kaatcp_server_record_t) * bootstrap->server_count);
    if (!bootstrap->servers) {
        return KAATCP_ERR_NOMEM;
    }
    cursor += 4;
    for (int i = 0; i < bootstrap->server_count; ++i) {
        cursor = kaatcp_parser_parse_server_record(bootstrap->servers + i, cursor);
        if (!cursor) {
            return KAATCP_ERR_NOMEM;
        }
    }
    return KAATCP_ERR_NONE;
}

static kaatcp_error_t kaatcp_parser_message_done(kaatcp_parser_t *parser)
{
    switch (parser->message_type) {
        case KAATCP_MESSAGE_CONNACK:
            if (parser->handlers.connack_handler) {
                kaatcp_connack_t connack = { *(parser->payload + 1) };
                parser->handlers.connack_handler(connack);
            }
            break;
        case KAATCP_MESSAGE_DISCONNECT:
            if (parser->handlers.disconnect_handler) {
                kaatcp_disconnect_t disconnect = { *(parser->payload + 1) };
                parser->handlers.disconnect_handler(disconnect);
            }
            break;
        case KAATCP_MESSAGE_PINGRESP:
            if (parser->handlers.pingresp_handler) {
                parser->handlers.pingresp_handler();
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
                        free(kaasync);
                        return KAATCP_ERR_NOMEM;
                    }
                    memcpy(kaasync->sync_request, cursor, kaasync->sync_request_size);
                } else {
                    kaasync->sync_request = NULL;
                }
                parser->handlers.kaasync_handler(kaasync);
            } else if ((sync_header.flags & KAA_SYNC_BOOTSTRAP_BIT) && parser->handlers.bootstrap_handler) {
                kaatcp_bootstrap_response_t *bootstrap = (kaatcp_bootstrap_response_t *) malloc(sizeof(kaatcp_bootstrap_response_t));
                if (!bootstrap) {
                    return KAATCP_ERR_NOMEM;
                }
                bootstrap->sync_header = sync_header;
                kaatcp_error_t rval = kaatcp_parser_parse_bootstrap_message(bootstrap, cursor, parser->payload + parser->message_length);
                if (rval) {
                    free(bootstrap);
                    return rval;
                }
                parser->handlers.bootstrap_handler(bootstrap);
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

kaatcp_error_t kaatcp_parser_process_buffer(kaatcp_parser_t *parser, const char *buf, uint32_t buf_size)
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

void kaatcp_parser_bootstrap_destroy(kaatcp_bootstrap_response_t *message)
{
    if (message) {
        if (message->servers) {
            for (int i = 0; i < message->server_count; ++i) {
                kaatcp_server_record_t *server = message->servers + i;
                if (server->supported_channels) {
                    for (int j = 0; j < server->supported_channels_count; ++j) {
                        kaatcp_supported_channel_t *channel = server->supported_channels + j;
                        if (channel->hostname) {
                            free(channel->hostname);
                        }
                    }
                    free(server->supported_channels);
                }
                if (server->public_key) {
                    free(server->public_key);
                }
                if (server->server_name) {
                    free(server->server_name);
                }
            }
            free(message->servers);
        }
        free(message);
    }
}

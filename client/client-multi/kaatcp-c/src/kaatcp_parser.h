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

#ifndef KAATCP_PARSER_H_
#define KAATCP_PARSER_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "kaatcp_common.h"

#define KAATCP_PARSER_MAX_MESSAGE_LENGTH 1024

typedef enum
{
    KAATCP_PARSER_STATE_NONE = 0x00,
    KAATCP_PARSER_STATE_PROCESSING_LENGTH = 0x01,
    KAATCP_PARSER_STATE_PROCESSING_PAYLOAD = 0x02,
} kaatcp_parser_state_t;

typedef void (*connack_message_handler_t) (kaatcp_connack_t message);
typedef void (*disconnect_message_handler_t) (kaatcp_disconnect_t message);
typedef void (*kaasync_message_handler_t) (kaatcp_kaasync_t *message);
typedef void (*bootstrap_message_handler_t) (kaatcp_bootstrap_response_t *message);
typedef void (*pingresp_message_handler_t) ();

typedef struct kaatcp_parser_handlers_t
{
    connack_message_handler_t connack_handler;
    disconnect_message_handler_t disconnect_handler;
    kaasync_message_handler_t kaasync_handler;
    bootstrap_message_handler_t bootstrap_handler;
    pingresp_message_handler_t pingresp_handler;
} kaatcp_parser_handlers_t;

typedef struct kaatcp_parser_t
{
    kaatcp_parser_state_t state;
    kaatcp_message_type_t message_type;
    uint32_t message_length;
    uint32_t processed_payload_length;
    uint32_t length_multiplier;
    char payload[KAATCP_PARSER_MAX_MESSAGE_LENGTH];

    kaatcp_parser_handlers_t handlers;
} kaatcp_parser_t;

kaatcp_error_t kaatcp_parser_init(kaatcp_parser_t *parser, const kaatcp_parser_handlers_t *handlers);
kaatcp_error_t kaatcp_parser_reset(kaatcp_parser_t *parser);
kaatcp_error_t kaatcp_parser_process_buffer(kaatcp_parser_t *parser, const char *buf, uint32_t buf_size);
void kaatcp_parser_kaasync_destroy(kaatcp_kaasync_t *message);
void kaatcp_parser_bootstrap_destroy(kaatcp_bootstrap_response_t *message);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAATCP_PARSER_H_ */

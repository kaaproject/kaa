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


typedef void (*on_connack_message_fn)(void *context, kaatcp_connack_t message);
typedef void (*on_disconnect_message_fn)(void *context, kaatcp_disconnect_t message);
typedef void (*on_kaasync_message_fn)(void *context, kaatcp_kaasync_t *message);
typedef void (*on_pingresp_message_fn)(void *context);



typedef enum {
    KAATCP_PARSER_STATE_NONE               = 0x00,
    KAATCP_PARSER_STATE_PROCESSING_LENGTH  = 0x01,
    KAATCP_PARSER_STATE_PROCESSING_PAYLOAD = 0x02,
} kaatcp_parser_state_t;

typedef struct {
    void                        *handlers_context;
    on_connack_message_fn       connack_handler;
    on_disconnect_message_fn    disconnect_handler;
    on_kaasync_message_fn       kaasync_handler;
    on_pingresp_message_fn      pingresp_handler;
} kaatcp_parser_handlers_t;

typedef struct {
    kaatcp_parser_state_t    state;
    kaatcp_message_type_t    message_type;
    uint32_t                 message_length;
    uint32_t                 processed_payload_length;
    uint32_t                 length_multiplier;
    uint32_t                 payload_buffer_size;
    char                    *payload;

    kaatcp_parser_handlers_t handlers;
} kaatcp_parser_t;



kaatcp_error_t kaatcp_parser_init(kaatcp_parser_t *parser
                                , const kaatcp_parser_handlers_t *handlers);

kaatcp_error_t kaatcp_parser_reset(kaatcp_parser_t *parser);

kaatcp_error_t kaatcp_parser_process_buffer(kaatcp_parser_t *parser
                                          , const char *buf
                                          , size_t buf_size);

void kaatcp_parser_kaasync_destroy(kaatcp_kaasync_t *message);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAATCP_PARSER_H_ */

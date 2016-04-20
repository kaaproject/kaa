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

#ifndef KAATCP_REQUEST_H_
#define KAATCP_REQUEST_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "kaatcp_common.h"



kaatcp_error_t kaatcp_fill_connect_message(uint16_t keepalive, uint32_t next_protocol_id
                                         , char *sync_request, size_t sync_request_size
                                         , char *session_key, size_t session_key_size
                                         , char *signature, size_t signature_size
                                         , kaatcp_connect_t *message);

kaatcp_error_t kaatcp_get_request_connect(const kaatcp_connect_t *message
                                        , char *buf
                                        , size_t *buf_size);

kaatcp_error_t kaatcp_fill_disconnect_message(kaatcp_disconnect_reason_t reason
                                            , kaatcp_disconnect_t *message);

kaatcp_error_t kaatcp_get_request_disconnect(const kaatcp_disconnect_t *message
                                           , char *buf
                                           , size_t *buf_size);

kaatcp_error_t kaatcp_fill_kaasync_message(char *sync_request, size_t sync_request_size
                                         , uint16_t message_id, uint8_t zipped, uint8_t encrypted
                                         , kaatcp_kaasync_t *message);

kaatcp_error_t kaatcp_get_request_kaasync(const kaatcp_kaasync_t *message
                                        , char *buf
                                        , size_t *buf_size);

kaatcp_error_t kaatcp_get_request_ping(char *buf, size_t *buf_size);

kaatcp_error_t kaatcp_get_request_size(const kaatcp_connect_t *message, kaatcp_message_type_t type, size_t *size);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAATCP_REQUEST_H_ */

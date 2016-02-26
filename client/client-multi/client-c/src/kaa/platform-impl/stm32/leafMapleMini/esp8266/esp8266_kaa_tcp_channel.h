/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * @file esp8266_kaa_tcp_channel.h
 *
 *  Created on: Apr 17, 2015
 *      Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
 */

#ifndef ESP8266_ESP8266_KAA_TCP_CHANNEL_H_
#define ESP8266_KAA_TCP_CHANNEL_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "../../../../kaa_error.h"
#include "../../../../platform/ext_transport_channel.h"
#include "../../../../platform/defaults.h"

kaa_error_t kaa_tcp_channel_create(kaa_transport_channel_interface_t *self
                                 , kaa_logger_t *logger
                                 , kaa_service_t *supported_services
                                 , size_t supported_service_count);

kaa_error_t kaa_tcp_channel_get_access_point(kaa_transport_channel_interface_t *self
                                 , char **hostname, size_t *hostname_size, uint16_t *port);

kaa_error_t kaa_tcp_channel_get_buffer_for_send(kaa_transport_channel_interface_t *self
                                 , uint8 **buffer, size_t *buffer_size);

kaa_error_t kaa_tcp_channel_free_send_buffer(kaa_transport_channel_interface_t *self, size_t bytes_written);

kaa_error_t kaa_tcp_channel_read_bytes(kaa_transport_channel_interface_t *self, const uint8 *buffer, const size_t buffer_size);

kaa_error_t kaa_tcp_channel_disconnected(kaa_transport_channel_interface_t *self);

kaa_error_t kaa_tcp_channel_connected(kaa_transport_channel_interface_t *self);

bool kaa_tcp_channel_connection_is_ready_to_terminate(kaa_transport_channel_interface_t *self);

kaa_error_t kaa_tcp_channel_check_keepalive(kaa_transport_channel_interface_t *self);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* ESP8266_KAA_TCP_CHANNEL_H_ */

/*
 * Copyright 2014-2015 CyberVision, Inc.
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
 * kaa_tcp_channel.h
 *
 *  Created on: Jan 16, 2015
 *      Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
 */

#ifndef KAA_TCP_CHANNEL_H_
#define KAA_TCP_CHANNEL_H_

#include "../kaa_error.h"
#include "../platform/ext_transport_channel.h"

#include "../platform/ext_tcp_utils.h"

typedef enum {
    FD_READ, FD_WRITE, FD_EXEPTION
} fd_event_t;

typedef enum {
    SOCKET_CONNECTED, SOCKET_DISCONNECTED, SOCKET_CONNECTION_ERROR
} kaa_tcp_channel_event_t;

typedef kaa_error_t (*kaa_tcp_channel_event_fn)(void *context
                                         , kaa_tcp_channel_event_t event_type
                                         , kaa_fd fd);
/*
 * @brief Create Kaa tcp channel implementation.
 * Create Kaa tcp channel implementation.
 * @param[in]   channel interface object.
 *
 * @return Error code
 */
kaa_error_t kaa_tcp_channel_create(kaa_transport_channel_interface_t * channel, kaa_logger_t *logger, kaa_service_t *supported_services, size_t supported_service_count);

/*
 * @brief Get socket for specified event type
 * Return socket descriptor for specified event type if channel need this types of event.
 * @param[in]   channel     Kaa tcp channel object.
 * @param[in]   event_type  Event type: FD_READ, FD_WRITE, FD_EXEPTION.
 * @param[out]  fd_p        Socket descriptor or KAA_TCP_SOCKET_NOT_SET if this event not need.
 *
 * @return Error code.
 */
kaa_error_t kaa_tcp_channel_get_socket_for_event(kaa_transport_channel_interface_t * channel, fd_event_t event_type, kaa_fd * fd_p);

/*
 * @brief Process socket event,
 * Process socket event by Kaa tcp channel.
 * @param[in]   channel     Kaa tcp channel object.
 * @param[in]   event_type  Event type: FD_READ, FD_WRITE, FD_EXEPTION.
 * @param[in]   fd_p        Socket descriptor to which event occurred.
 *                          kaa_tcp_channel_process_event should check fd_p with own socket
 *                          descriptor and process event if it equals, or just do nothing if not. (don't return any error).
 *
 * @return Error code.
 */
kaa_error_t kaa_tcp_channel_process_event(kaa_transport_channel_interface_t * channel, fd_event_t event_type, kaa_fd fd_p);

/**
 * @brief Return maximum timeout necessary for next timer event.
 * To Keepalive support on Kaa tcp channel, channel have to send Ping request messages to server
 *      and check that response is returned To generate Ping message, channel should be periodically notified
 *      on timer events. This method return maximum seconds to wait until keepalive check must be invoked.
 * @param[in]   channel     Kaa tcp channel object.
 * @param[out]  maxTimeout  maximum seconds to wait, 0 - mean not event is necessary.
 *
 * @return Error code.
 */
kaa_error_t kaa_tcp_channel_get_max_timeout(kaa_transport_channel_interface_t * channel, uint16_t *maxTimeout);

/**
 * @brief Invoke Keepalive check
 * Check time since last Ping request was sent, and if it necessary send next Ping request. If time from last ping
 * is grater than 20% of keepalive value, channel threaten as disconnected.
 *
 * @param[in]   channel     Kaa tcp channel object.
 *
 * @return Error code.
 */
kaa_error_t kaa_tcp_channel_check_keepalive(kaa_transport_channel_interface_t * channel);
/*
 * @brief Set socket events callback
 * Set callback for following events:
 *      SOCKET_CONNECTED, SOCKET_DISCONNECTED, SOCKET_CONNECTION_ERROR.
 *      Should know that events callback would be call inside  kaa_tcp_channel_process_event.
 *      Need avoid possible recursions.
 * @param[in]   channel     Kaa tcp channel object.
 * @param[in]   callback    Callback function
 * @param[in]   context     Callback context.
 *
 * @return Error code.
 */
kaa_error_t kaa_tcp_channel_set_socket_events_callback(kaa_transport_channel_interface_t * channel, kaa_tcp_channel_event_fn callback, void * context);

/**
 * @brief Set Kaa tcp protocol keepalive
 * Keepalive used to check that Kaa tcp channel is alive, every keepalive interval (in seconds)
 *      Kaa tcp channel send to server keepalive message, and wait response, if response don't back
 *      during keepalive period, channel become disconnected.
 * @param[in]    channel     Kaa tcp channel object.
 * @param[in]    keepalive   Keepalive, value in seconds. 0 - mean no keepalive.
 *
 * @return Error code
 */
kaa_error_t kaa_tcp_channel_set_keepalive_timeout(kaa_transport_channel_interface_t * channel, uint16_t keepalive);

/**
 * @brief Disconnect channel
 * Send Kaa tcp disconnect channel with reason
 * @param[in]    channel     Kaa tcp channel object.
 * @param[in]    return_code Disconnect code.
 *
 * @return Error code
 */
kaa_error_t kaa_tcp_channel_disconnect(kaa_transport_channel_interface_t  *channel, kaatcp_disconnect_reason_t return_code);

#endif /* KAA_TCP_CHANNEL_H_ */

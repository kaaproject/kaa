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


#ifndef KAA_TCP_CHANNEL_H_
#define KAA_TCP_CHANNEL_H_

#include "../kaa_error.h"
#include "../platform/ext_transport_channel.h"

#include "../platform/ext_tcp_utils.h"


typedef enum {
    FD_READ,
    FD_WRITE,
    FD_EXCEPTION
} fd_event_t;


typedef enum {
    SOCKET_CONNECTED,
    SOCKET_DISCONNECTED,
    SOCKET_CONNECTION_ERROR
} kaa_tcp_channel_event_t;


/**
 * @brief Notifies about the current channel's state. Used by @link kaa_tcp_channel_set_socket_events_callback @endlink .
 *
 * @param[in]   context                     Callback's context
 * @param[in]   event_type                  The current state of the channel (SOCKET_CONNECTED, SOCKET_DISCONNECTED or SOCKET_CONNECTION_ERROR).
 * @param[in]   fd                          The socket descriptor to which the event occurred.
 *
 * @return Error code
 */
typedef kaa_error_t (*on_kaa_tcp_channel_event_fn)(void *context, kaa_tcp_channel_event_t event_type, kaa_fd_t fd);


/**
 * @brief Creates Kaa tcp channel implementation.
 *
 * @param[in]   channel                     The pointer to the channel's instance.
 * @param[in]   logger                      The pointer to Kaa logger instance.
 * @param[in]   supported_services          The list of supported services for this channel.
 * @param[in]   supported_service_count     The count of services in the list.
 *
 * @return Error code
 */
kaa_error_t kaa_tcp_channel_create(kaa_transport_channel_interface_t *self, kaa_logger_t *logger, kaa_service_t *supported_services, size_t supported_service_count);


/**
 * @brief Retrieves socket descriptor of the given channel.
 *
 * @param[in]   channel         Channel's instance.
 * @param[out]  fd_p            Socket descriptor or KAA_TCP_SOCKET_NOT_SET if there is no open descriptor.
 *
 * @return Error code.
 */
kaa_error_t kaa_tcp_channel_get_descriptor(kaa_transport_channel_interface_t *self, kaa_fd_t *fd_p);


/**
 * @brief Checks if the given channel is ready for the specified event.
 *
 * @param[in]   channel         Channel's instance.
 * @param[out]  event_type      Event type: FD_READ, FD_WRITE, FD_EXCEPTION.
 *
 * @return                      true - if the channel is ready for the specified event, false - otherwise.
 */
bool kaa_tcp_channel_is_ready(kaa_transport_channel_interface_t *self, fd_event_t event_type);


/**
 * @brief Notifies the channel about the I/O event.
 *
 * @param[in]   channel         Channel's instance.
 * @param[in]   event_type      Event type: FD_READ, FD_WRITE, FD_EXEPTION.
 *
 * @return Error code.
 */
kaa_error_t kaa_tcp_channel_process_event(kaa_transport_channel_interface_t *self, fd_event_t event_type);


/**
 * @brief Retrieves the maximum timeout for the Multiplexing I/O like select/poll.
 * Used for @link kaa_tcp_channel_check_keepalive @endlink needs.
 *
 * @param[in]   channel      Channel's instance.
 * @param[out]  max_timeout  The maximum timeout value (in seconds), 0 - means timeout is not used by this channel.
 *
 * @return Error code.
 */
kaa_error_t kaa_tcp_channel_get_max_timeout(kaa_transport_channel_interface_t *self, uint16_t *max_timeout);


/**
 * @brief Checks if the keepalive timeout occurred. If so, sends keepalive message to a server.
 * Should be called on Multiplexing I/O (like select/poll) timeouts.
 *
 * @param[in]   channel     Channel's instance.
 *
 * @return Error code.
 */
kaa_error_t kaa_tcp_channel_check_keepalive(kaa_transport_channel_interface_t *self);


/**
 * @brief Sets callback for the current channel connection state. See @link kaa_tcp_channel_event_fn @endlink .
 *
 * @param[in]   channel     Channel's instance.
 * @param[in]   callback    Connection event callback.
 * @param[in]   context     Callback context.
 *
 * @return Error code.
 */
kaa_error_t kaa_tcp_channel_set_socket_events_callback(kaa_transport_channel_interface_t *self, on_kaa_tcp_channel_event_fn callback, void *context);


/**
 * @brief Sets a keepalive timeout for the current channel.
 *
 * @param[in]    channel     Channel's instance.
 * @param[in]    keepalive   Keepalive timeout (in seconds). 0 - means keepalive is not used.
 *
 * @return Error code
 */
kaa_error_t kaa_tcp_channel_set_keepalive_timeout(kaa_transport_channel_interface_t *self, uint16_t keepalive);


/**
 * @brief Disconnects current channel. This channel's instance could not be used any more.
 *
 * @param[in]    channel     Channel's instance.
 *
 * @return Error code
 */
kaa_error_t kaa_tcp_channel_disconnect(kaa_transport_channel_interface_t *self);


#endif /* KAA_TCP_CHANNEL_H_ */

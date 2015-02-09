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


#include <stdbool.h>
#include <stdint.h>
#include <stddef.h>
#include <string.h>
#include <time.h>
#include "../platform/stdio.h"

#include "../kaa_common.h"
#include "../utilities/kaa_mem.h"
#include "../utilities/kaa_buffer.h"
#include "../utilities/kaa_log.h"
#include "../kaa_protocols/kaa_tcp/kaatcp.h"
#include "../platform/ext_system_logger.h"
#include "kaa_tcp_channel.h"

#define KAA_TCP_CHANNEL_IN_BUFFER_SIZE 1024
#define KAA_TCP_CHANNEL_OUT_BUFFER_SIZE 2024

#define KAA_TCP_CHANNEL_TRANSPORT_PROTOCOL_ID 0x56c8ff92
#define KAA_TCP_CHANNEL_TRANSPORT_PROTOCOL_VERSION 1

#define KAA_TCP_CHANNEL_NEXT_PROTOCOL_BINARY_ID 0x3553c66f

#define KAA_TCP_CHANNEL_KEEPALIVE 300

typedef time_t kaa_time_t;


typedef enum {
    AP_UNDEFINED = 0,
    AP_NOT_SET, AP_SET, AP_RESOLVED, AP_CONNECTING, AP_CONNECTED, AP_DISCONNECTED
} access_point_state_t;

typedef enum {
    KAA_TCP_CHANNEL_UNDEFINED, KAA_TCP_CHANNEL_AUTHORIZING, KAA_TCP_CHANNEL_AUTHORIZED, KAA_TCP_CHANNEL_DISCONNECTING
} kaa_tcp_channel_state_t;

typedef struct {
    access_point_state_t state;
    uint32_t    id;
    char *public_key;
    uint32_t public_key_length;
    char *hostname;
    uint32_t hostname_length;
    kaa_sockaddr_storage_t sockaddr;
    kaa_socklen_t  sockaddr_length;
    kaa_fd_t  socket_descriptor;
} kaa_tcp_access_point_t;

typedef struct {
    uint16_t        keepalive_interval;
    kaa_time_t      last_sent_keepalive;
    kaa_time_t      last_receive_keepalive;
} kaa_tcp_keepalive_t ;

typedef struct {
    char *aes_session_key;
    size_t aes_session_key_size;
    char *signature;
    size_t signature_size;
} kaa_tcp_encrypt_t;

typedef struct {
    kaa_logger_t *logger;
    kaa_tcp_channel_state_t channel_state;
    kaa_transport_protocol_id_t protocol_id;
    kaa_transport_context_t transport_context;
    on_kaa_tcp_channel_event_fn event_callback;
    void *event_context;
    kaa_tcp_access_point_t access_point;
    kaa_service_t *pending_request_services;
    size_t pending_request_service_count;
    kaa_service_t *supported_services;
    size_t supported_service_count;
    kaa_buffer_t    *in_buffer;
    kaa_buffer_t    *out_buffer;
    kaatcp_parser_t *parser;
    uint16_t message_id;
    kaa_tcp_keepalive_t keepalive;
    kaa_tcp_encrypt_t encryption;
} kaa_tcp_channel_t;


kaa_error_t kaa_tcp_channel_get_transport_protocol_info(void *context, kaa_transport_protocol_id_t *protocol_info);
kaa_error_t kaa_tcp_channel_get_supported_services(void *context, kaa_service_t **supported_services, size_t *service_count);
kaa_error_t kaa_tcp_channel_sync_handler(void *context, const kaa_service_t services[], size_t service_count);
kaa_error_t kaa_tcp_channel_release_context(void *context);
kaa_error_t kaa_tcp_channel_init(void *context, kaa_transport_context_t *transport_context);
kaa_error_t kaa_tcp_channel_set_access_point(void *context, kaa_access_point_t *access_point);

/*
 * Parser handlers
 */
void kaa_tcp_channel_connack_message_callback(void *context, kaatcp_connack_t message);
void kaa_tcp_channel_disconnect_message_callback(void *context, kaatcp_disconnect_t message);
void kaa_tcp_channel_kaasync_message_callback(void *context, kaatcp_kaasync_t *message);
void kaa_tcp_channel_pingresp_message_callback(void *context);

/*
 * Internal functions
 */
kaa_error_t kaa_tcp_channel_socket_io_error(kaa_tcp_channel_t *self);
kaa_error_t kaa_tcp_channel_authorize(kaa_tcp_channel_t *self);
bool is_service_pending(kaa_tcp_channel_t *self, const kaa_service_t service);
kaa_error_t kaa_tcp_channel_delete_pending_services(kaa_tcp_channel_t *self, const kaa_service_t services[], size_t service_count);
kaa_error_t kaa_tcp_channel_update_pending_services(kaa_tcp_channel_t *self, const kaa_service_t services[], size_t service_count);
kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolved(void *context, const kaa_sockaddr_t *addr, kaa_socklen_t addr_size);
kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolve_failed(void *context);
uint32_t get_uint32_t(const char *buffer);
kaa_error_t kaa_tcp_channel_connect_access_point(kaa_tcp_channel_t *self);
kaa_error_t kaa_tcp_channel_release_access_point(kaa_tcp_channel_t *self);
kaa_error_t kaa_tcp_channel_write_pending_services(kaa_tcp_channel_t *self, kaa_service_t *service, size_t services_count);
kaa_error_t kaa_tcp_write_buffer(kaa_tcp_channel_t *self);
char* kaa_tcp_write_pending_services_allocator_fn(void *context, size_t buffer_size);
kaa_error_t kaa_tcp_channel_ping(kaa_tcp_channel_t *self);
kaa_error_t kaa_tcp_channel_disconnect_internal(kaa_tcp_channel_t *self, kaatcp_disconnect_reason_t return_code);

/*
 * Create TCP channel object
 */
kaa_error_t kaa_tcp_channel_create(kaa_transport_channel_interface_t *self, kaa_logger_t *logger, kaa_service_t *supported_services, size_t supported_service_count)
{
    KAA_RETURN_IF_NIL2(self, logger, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(logger, KAA_ERR_NONE, "Kaa TCP channel creating....");

    kaa_error_t error_code = KAA_ERR_NONE;

    kaa_tcp_channel_t *kaa_tcp_channel = (kaa_tcp_channel_t *) KAA_CALLOC(1, sizeof(kaa_tcp_channel_t));
    KAA_RETURN_IF_NIL(kaa_tcp_channel, KAA_ERR_NOMEM);

    kaa_tcp_channel->channel_state = KAA_TCP_CHANNEL_UNDEFINED;
    kaa_tcp_channel->access_point.state = AP_NOT_SET;
    kaa_tcp_channel->access_point.socket_descriptor = KAA_TCP_SOCKET_NOT_SET;

    if (supported_service_count > 0) {
        kaa_tcp_channel->supported_services = (kaa_service_t *) KAA_MALLOC(supported_service_count * sizeof(kaa_service_t));
        if (!kaa_tcp_channel->supported_services) {
            kaa_tcp_channel_release_context(kaa_tcp_channel);
            return KAA_ERR_NOMEM;
        }

        int i = 0;
        for(; i < supported_service_count; ++i) {
            kaa_tcp_channel->supported_services[i] = supported_services[i];
        }
        kaa_tcp_channel->supported_service_count = supported_service_count;
    }

    error_code = kaa_buffer_create_buffer(&kaa_tcp_channel->in_buffer, KAA_TCP_CHANNEL_IN_BUFFER_SIZE);
    if (error_code) {
        kaa_tcp_channel_release_context(kaa_tcp_channel);
        return error_code;
    }
    error_code = kaa_buffer_create_buffer(&kaa_tcp_channel->out_buffer, KAA_TCP_CHANNEL_OUT_BUFFER_SIZE);
    if (error_code) {
        kaa_tcp_channel_release_context(kaa_tcp_channel);
        return error_code;
    }

    kaa_tcp_channel->parser = (kaatcp_parser_t *) KAA_CALLOC(1, sizeof(kaatcp_parser_t));
    if (!kaa_tcp_channel->parser) {
        kaa_tcp_channel_release_context(kaa_tcp_channel);
        return KAA_ERR_NOMEM;
    }

    kaa_tcp_channel->keepalive.keepalive_interval = KAA_TCP_CHANNEL_KEEPALIVE;
    kaa_tcp_channel->keepalive.last_sent_keepalive = (kaa_time_t) ext_get_systime();
    kaa_tcp_channel->keepalive.last_receive_keepalive = kaa_tcp_channel->keepalive.last_sent_keepalive;
    KAA_LOG_TRACE(logger,KAA_ERR_NONE,"Kaa TCP channel keepalive is %d ",kaa_tcp_channel->keepalive.keepalive_interval);

    kaa_tcp_channel->protocol_id.id = KAA_TCP_CHANNEL_TRANSPORT_PROTOCOL_ID;
    kaa_tcp_channel->protocol_id.version = KAA_TCP_CHANNEL_TRANSPORT_PROTOCOL_VERSION;
    KAA_LOG_TRACE(logger, KAA_ERR_NONE, "Kaa TCP channel transport protocol id 0x%08x", kaa_tcp_channel->protocol_id);

    kaa_tcp_channel->logger = logger;

    kaatcp_parser_handlers_t parser_handler;
    parser_handler.connack_handler = kaa_tcp_channel_connack_message_callback;
    parser_handler.disconnect_handler = kaa_tcp_channel_disconnect_message_callback;
    parser_handler.kaasync_handler = kaa_tcp_channel_kaasync_message_callback;
    parser_handler.pingresp_handler = kaa_tcp_channel_pingresp_message_callback;
    parser_handler.handlers_context = (void *) kaa_tcp_channel;
    kaatcp_error_t parser_error_code = kaatcp_parser_init(kaa_tcp_channel->parser, &parser_handler);
    if (parser_error_code) {
        kaa_tcp_channel_release_context(kaa_tcp_channel);
        return KAA_ERR_TCPCHANNEL_PARSER_INIT_FAILED;
    }

    self->context = (void*) kaa_tcp_channel;
    self->get_protocol_id = kaa_tcp_channel_get_transport_protocol_info;
    self->get_supported_services = kaa_tcp_channel_get_supported_services;
    self->release_context = kaa_tcp_channel_release_context;
    self->sync_handler = kaa_tcp_channel_sync_handler;
    self->init = kaa_tcp_channel_init;
    self->set_access_point = kaa_tcp_channel_set_access_point;

    KAA_LOG_INFO(logger, KAA_ERR_NONE, "Kaa TCP channel created");

    return error_code;
}

/*
 * Return transport protocol id constant
 */
kaa_error_t kaa_tcp_channel_get_transport_protocol_info(void *context, kaa_transport_protocol_id_t *protocol_info)
{
    KAA_RETURN_IF_NIL2(context, protocol_info, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;
    *protocol_info = channel->protocol_id;
    return KAA_ERR_NONE;
}

/*
 * Return supported services list
 */
kaa_error_t kaa_tcp_channel_get_supported_services(void * context, kaa_service_t **supported_services, size_t *service_count) {
    KAA_RETURN_IF_NIL3(context, supported_services, service_count, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;

    *supported_services = channel->supported_services;
    *service_count = channel->supported_service_count;

    return KAA_ERR_NONE;
}

/*
 * Sync specified services list with server side.
 */
kaa_error_t kaa_tcp_channel_sync_handler(void *context, const kaa_service_t services[], size_t service_count) {
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;

    kaa_error_t error_code = KAA_ERR_NONE;

    if (services && service_count > 0) {
        error_code = kaa_tcp_channel_update_pending_services(channel, services, service_count);
        KAA_RETURN_IF_ERR(error_code);
    }

    return error_code;
}

/*
 * Release Kaa TCP channel context
 */
kaa_error_t kaa_tcp_channel_release_context(void *context) {
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;
    kaa_error_t error_code = KAA_ERR_NONE;

    if (channel->parser) {
        KAA_FREE(channel->parser);
        channel->parser = NULL;
    }


    kaa_tcp_channel_release_access_point(channel);

    kaa_buffer_destroy(channel->in_buffer);

    kaa_buffer_destroy(channel->out_buffer);


    if (channel->pending_request_services) {
        KAA_FREE(channel->pending_request_services);
        channel->pending_request_services = NULL;
    }

    if (channel->encryption.aes_session_key) {
        KAA_FREE(channel->encryption.aes_session_key);
        channel->encryption.aes_session_key = NULL;
        channel->encryption.aes_session_key_size = 0;
    }

    if (channel->encryption.signature) {
        KAA_FREE(channel->encryption.signature);
        channel->encryption.signature = NULL;
        channel->encryption.signature_size = 0;
    }

    if (channel->supported_services) {
        KAA_FREE(channel->supported_services);
        channel->supported_services = NULL;
        channel->supported_service_count = 0;
    }

    channel->access_point.state = AP_UNDEFINED;

    KAA_FREE(context);


    return error_code;
}

/*
 * Init Kaa TCP channel transport context
 */
kaa_error_t kaa_tcp_channel_init(void *context, kaa_transport_context_t *transport_context)
{
    KAA_RETURN_IF_NIL2(context, transport_context, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL2(transport_context->platform_protocol, transport_context->bootstrap_manager, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;

    channel->transport_context = *transport_context;

    return KAA_ERR_NONE;
}

/*
 * Set access point to Kaa TCP channel.
 */
kaa_error_t kaa_tcp_channel_set_access_point(void *context, kaa_access_point_t *access_point)
{
    KAA_RETURN_IF_NIL2(context, access_point, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;

    kaa_error_t error_code = KAA_ERR_NONE;

    KAA_LOG_INFO(channel->logger, KAA_ERR_NONE, "Kaa TCP channel setting access point.");

    if (channel->access_point.state != AP_NOT_SET) {
        KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE, "Kaa TCP channel removing previous access point (%d)", channel->access_point.id);
        error_code = kaa_tcp_channel_release_access_point(channel);
        KAA_RETURN_IF_ERR(error_code);
    }
    channel->access_point.state = AP_SET;
    channel->access_point.id = access_point->id;

    KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE, "Kaa TCP channel new access point (%d), connection data length %d",
            channel->access_point.id,
            access_point->connection_data_len);


    char * connection_data = access_point->connection_data;
    size_t connection_data_len = access_point->connection_data_len;

    int position = 0;
    int remaining_to_read = 4;
    if ((position + remaining_to_read) <= connection_data_len) {
        channel->access_point.public_key_length = get_uint32_t(connection_data);
        position += remaining_to_read;
    } else {
        KAA_LOG_ERROR(channel->logger, KAA_ERR_INSUFFICIENT_BUFFER, "Kaa TCP channel new access point (%d), "
                "insufficient connection data length  %d, position %d",
                    channel->access_point.id,
                    connection_data_len,
                    (position + remaining_to_read));
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    remaining_to_read = channel->access_point.public_key_length;
    if ((position + remaining_to_read) <= connection_data_len) {
        channel->access_point.public_key = (char *) KAA_MALLOC(channel->access_point.public_key_length);
        KAA_RETURN_IF_NIL(channel->access_point.public_key, KAA_ERR_NOMEM);
        memcpy(channel->access_point.public_key,
                connection_data + position,
                remaining_to_read);
        position += remaining_to_read;
    } else {
        KAA_LOG_ERROR(channel->logger, KAA_ERR_INSUFFICIENT_BUFFER, "Kaa TCP channel new access point (%d), "
                "insufficient connection data length  %d, position %d",
                    channel->access_point.id,
                    connection_data_len,
                    (position + remaining_to_read));
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    remaining_to_read = 4;
    if ((position + remaining_to_read) <= connection_data_len) {
        channel->access_point.hostname_length = get_uint32_t(connection_data + position);
        position += remaining_to_read;
    } else {
        KAA_LOG_ERROR(channel->logger, KAA_ERR_INSUFFICIENT_BUFFER, "Kaa TCP channel new access point (%d), "
                "insufficient connection data length  %d, position %d",
                    channel->access_point.id,
                    connection_data_len,
                    (position + remaining_to_read));
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    remaining_to_read = channel->access_point.hostname_length;
    if ((position + remaining_to_read) <= connection_data_len) {
        channel->access_point.hostname = (char *) KAA_MALLOC(channel->access_point.hostname_length);
        KAA_RETURN_IF_NIL(channel->access_point.hostname, KAA_ERR_NOMEM);
        memcpy(channel->access_point.hostname,
                connection_data + position,
                remaining_to_read);
        position += remaining_to_read;
    } else {
        KAA_LOG_ERROR(channel->logger, KAA_ERR_INSUFFICIENT_BUFFER, "Kaa TCP channel new access point (%d), "
                "insufficient connection data length  %d, position %d",
                    channel->access_point.id,
                    connection_data_len,
                    (position + remaining_to_read));
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    remaining_to_read = 4;
    int access_point_socket_port = 0;
    if ((position + remaining_to_read) <= connection_data_len) {
        access_point_socket_port = (uint16_t) get_uint32_t(connection_data + position);
        position += remaining_to_read;
    } else {
        KAA_LOG_ERROR(channel->logger, KAA_ERR_INSUFFICIENT_BUFFER, "Kaa TCP channel new access point (%d), "
                "insufficient connection data length  %d, position %d",
                    channel->access_point.id,
                    connection_data_len,
                    (position + remaining_to_read));

        return KAA_ERR_INSUFFICIENT_BUFFER;
    }
#ifdef KAA_LOG_LEVEL_TRACE_ENABLED
    char ap_hostname[channel->access_point.hostname_length + 1];
    memcpy(ap_hostname, channel->access_point.hostname, channel->access_point.hostname_length);
    ap_hostname[channel->access_point.hostname_length] = '\0';

    KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE, "Kaa TCP channel new access point (%d) destination %s:%d",
            channel->access_point.id,
            ap_hostname,
            access_point_socket_port);
#endif
    kaa_dns_resolve_listener_t resolve_listener;
    resolve_listener.context = (void *) channel;
    resolve_listener.on_host_resolved = kaa_tcp_channel_set_access_point_hostname_resolved;
    resolve_listener.on_resolve_failed = kaa_tcp_channel_set_access_point_hostname_resolve_failed;

    kaa_dns_resolve_info_t resolve_props;
    resolve_props.hostname = channel->access_point.hostname;
    resolve_props.hostname_length = channel->access_point.hostname_length;
    resolve_props.port = access_point_socket_port;

    channel->access_point.sockaddr_length = sizeof(kaa_sockaddr_storage_t);
    ext_tcp_utils_function_return_state_t resolve_state = ext_tcp_utils_gethostbyaddr(
            &resolve_listener,
            &resolve_props,
            (kaa_sockaddr_t *) &channel->access_point.sockaddr,
            &channel->access_point.sockaddr_length);

    switch (resolve_state) {
        case RET_STATE_VALUE_IN_PROGRESS:
            KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE, "Kaa TCP channel new access point (%d) destination name resolve pending...", channel->access_point.id);
            break;
        case RET_STATE_VALUE_READY:
            channel->access_point.state = AP_RESOLVED;
            error_code = kaa_tcp_channel_connect_access_point(channel);
            if (error_code) {
                if (channel->event_callback) {
                    channel->event_callback(channel->event_context, SOCKET_CONNECTION_ERROR, channel->access_point.socket_descriptor);
                }
            }
            KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE, "Kaa TCP channel new access point (%d) destination resolved", channel->access_point.id);
            break;
        case RET_STATE_VALUE_ERROR:
            channel->access_point.state = AP_NOT_SET;
            error_code = KAA_ERR_TCPCHANNEL_AP_RESOLVE_FAILED;
            KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE, "Kaa TCP channel new access point (%d) hostname resolve failed", channel->access_point.id);
            break;
        case RET_STATE_BUFFER_NOT_ENOUGH:
            channel->access_point.state = AP_NOT_SET;
            error_code = KAA_ERR_TCPCHANNEL_AP_RESOLVE_FAILED;
            KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE, "Kaa TCP channel new access point (%d) hostname resolve failed. Address buffer is not enough", channel->access_point.id);
            break;
    }

    return error_code;
}

kaa_error_t kaa_tcp_channel_get_descriptor(kaa_transport_channel_interface_t *self, kaa_fd_t *fd_p)
{
    KAA_RETURN_IF_NIL3(self, fd_p, self->context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *tcp_channel = (kaa_tcp_channel_t *) self->context;

    *fd_p = tcp_channel->access_point.socket_descriptor;

    return KAA_ERR_NONE;
}

bool kaa_tcp_channel_is_ready(kaa_transport_channel_interface_t *self, fd_event_t event_type)
{
    KAA_RETURN_IF_NIL2(self, self->context, false);
    kaa_tcp_channel_t *tcp_channel = (kaa_tcp_channel_t *) self->context;

    kaa_error_t error_code = KAA_ERR_NONE;

    switch (event_type) {
        case FD_READ:
            KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) checking socket for event READ", tcp_channel->access_point.id);
            if (tcp_channel->access_point.state == AP_CONNECTED) {
                char *buf = NULL;
                size_t buf_size = 0;
                error_code = kaa_buffer_allocate_space(tcp_channel->in_buffer, &buf, &buf_size);
                KAA_RETURN_IF_ERR(error_code);
                if (buf_size > 0)
                    return true;
            }
            break;
        case FD_WRITE:
            KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) checking socket for event WRITE", tcp_channel->access_point.id);
            if (tcp_channel->access_point.state == AP_CONNECTING) {
                return true;
            } else  if (tcp_channel->access_point.state == AP_CONNECTED) {
                //If there are some pending sync services put W into fd_set
                if (tcp_channel->pending_request_service_count > 0) {
                    if (is_service_pending(tcp_channel, KAA_SERVICE_BOOTSTRAP)
                        || tcp_channel->channel_state == KAA_TCP_CHANNEL_AUTHORIZED) {
                        return true;
                    }
                }
                //If out buffer have some bytes to transmit
                char * buf = NULL;
                size_t buf_size = 0;
                error_code = kaa_buffer_get_unprocessed_space(tcp_channel->out_buffer, &buf, &buf_size);
                KAA_RETURN_IF_ERR(error_code);
                if (buf_size > 0)
                    return true;
            }
            break;
        case FD_EXCEPTION:
            KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) checking socket for event EXCEPTION", tcp_channel->access_point.id);
            if (tcp_channel->access_point.socket_descriptor > KAA_TCP_SOCKET_NOT_SET)
                return true;
            break;
    }

    return false;
}

kaa_error_t kaa_tcp_channel_process_event(kaa_transport_channel_interface_t *self, fd_event_t event_type)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL(self->context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *tcp_channel = (kaa_tcp_channel_t *) self->context;

    kaa_error_t error_code = kaa_tcp_channel_check_keepalive(self);
    KAA_RETURN_IF_ERR(error_code);

    kaa_fd_t fd = tcp_channel->access_point.socket_descriptor;

    switch (event_type) {
        case FD_READ:
            KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) processing event READ", tcp_channel->access_point.id);
            if (tcp_channel->access_point.state == AP_CONNECTED) {
                char * buf = NULL;
                size_t buf_size = 0;
                size_t bytes_read = 0;
                error_code = kaa_buffer_allocate_space(tcp_channel->in_buffer, &buf, &buf_size);
                KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) empty buffer size is %d", tcp_channel->access_point.id, buf_size);
                KAA_RETURN_IF_ERR(error_code);
                if (buf_size > 0) {
                    ext_tcp_socket_io_errors_t ioe = ext_tcp_utils_tcp_socket_read(fd, buf, buf_size, &bytes_read);
                    switch (ioe) {
                        case KAA_TCP_SOCK_IO_OK:
                            KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) successfully read %d bytes", tcp_channel->access_point.id, bytes_read);
                            error_code = kaa_buffer_lock_space(tcp_channel->in_buffer, bytes_read);
                            error_code = kaa_buffer_get_unprocessed_space(tcp_channel->in_buffer, &buf, &buf_size);
                            //TODO Modify parser errors code
                            kaatcp_error_t kaatcp_error_code = kaatcp_parser_process_buffer(tcp_channel->parser, buf, buf_size);
                            if (kaatcp_error_code) {
                                error_code = KAA_ERR_TCPCHANNEL_PARSER_ERROR;
                                KAA_LOG_ERROR(tcp_channel->logger, error_code, "Kaa TCP channel(%d) failed to parse the buffer (error_code=%d)", tcp_channel->access_point.id, kaatcp_error_code);
                                kaa_tcp_channel_socket_io_error(tcp_channel);
                            } else {
                                error_code = kaa_buffer_free_allocated_space(tcp_channel->in_buffer, buf_size);
                            }
                            break;
                        default:
                            error_code = kaa_tcp_channel_socket_io_error(tcp_channel);
                            break;
                    }
                }
            }
            break;
        case FD_WRITE:
            KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) processing event WRITE", tcp_channel->access_point.id);
            if (tcp_channel->access_point.state == AP_CONNECTING) {
                ext_tcp_socket_state_t socket_state = ext_tcp_utils_tcp_socket_check(fd, (kaa_sockaddr_t *) &tcp_channel->access_point.sockaddr, tcp_channel->access_point.sockaddr_length);
                switch (socket_state) {
                    case KAA_TCP_SOCK_ERROR:
                        KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) connection failed", tcp_channel->access_point.id);
                        tcp_channel->access_point.state = AP_RESOLVED;
                        if (tcp_channel->event_callback)
                            tcp_channel->event_callback(tcp_channel->event_context, SOCKET_CONNECTION_ERROR, fd);
                        break;
                    case KAA_TCP_SOCK_CONNECTED:
                        KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) socket was successfully connected", tcp_channel->access_point.id);
                        tcp_channel->access_point.state = AP_CONNECTED;

                        if (tcp_channel->event_callback)
                            tcp_channel->event_callback(tcp_channel->event_context, SOCKET_CONNECTED, fd);
                        error_code = kaa_tcp_channel_authorize(tcp_channel);
                        break;
                    case KAA_TCP_SOCK_CONNECTING:
                        KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) socket is still connecting...", tcp_channel->access_point.id);
                        break;
                }
            } else if (tcp_channel->access_point.state == AP_CONNECTED) {
                error_code = kaa_tcp_write_buffer(tcp_channel);
                KAA_RETURN_IF_ERR(error_code);

                if (tcp_channel->channel_state == KAA_TCP_CHANNEL_DISCONNECTING) {
                    KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) disconnecting...", tcp_channel->access_point.id);
                    //Check if buffer is empty, close socket.
                    char *buf = NULL;
                    size_t buf_size = 0;
                    error_code = kaa_buffer_get_unprocessed_space(tcp_channel->out_buffer, &buf, &buf_size);
                    if (error_code || !buf_size)
                        error_code = kaa_tcp_channel_socket_io_error(tcp_channel);
                    else
                        KAA_LOG_TRACE(tcp_channel->logger, error_code, "Kaa TCP channel(%d) can't disconnect right now (%d bytes are unprocessed)", tcp_channel->access_point.id, buf_size);
                } else if (tcp_channel->pending_request_service_count > 0) {
                    if ((tcp_channel->pending_request_service_count == 1)
                            && is_service_pending(tcp_channel, KAA_SERVICE_BOOTSTRAP)) {
                        kaa_service_t boostrap_service = {KAA_SERVICE_BOOTSTRAP};
                        KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) going to sync Bootstrap service", tcp_channel->access_point.id);
                        error_code = kaa_tcp_channel_write_pending_services(tcp_channel, &boostrap_service, 1);
                    } else if (tcp_channel->channel_state == KAA_TCP_CHANNEL_AUTHORIZED) {
                        KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) going to sync all services", tcp_channel->access_point.id);
                        error_code = kaa_tcp_channel_write_pending_services(
                                tcp_channel,
                                tcp_channel->pending_request_services,
                                tcp_channel->pending_request_service_count);
                    } else {
                        KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) authorizing channel", tcp_channel->access_point.id);
                        error_code = kaa_tcp_channel_authorize(tcp_channel);
                    }
                } else {
                    KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) there is no pending services to sync", tcp_channel->access_point.id);
                }
            }
            break;
        case FD_EXCEPTION:
            KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) processing event EXCEPTION", tcp_channel->access_point.id);
            error_code = kaa_tcp_channel_socket_io_error(tcp_channel);
            break;
    }
    KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) event processing complete", tcp_channel->access_point.id);
    return error_code;
}


kaa_error_t kaa_tcp_channel_get_max_timeout(kaa_transport_channel_interface_t *self, uint16_t *max_timeout)
{
    KAA_RETURN_IF_NIL2(self, self->context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *tcp_channel = (kaa_tcp_channel_t *) self->context;

    *max_timeout = tcp_channel->keepalive.keepalive_interval / 2;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_tcp_channel_check_keepalive(kaa_transport_channel_interface_t *self)
{
    KAA_RETURN_IF_NIL2(self, self->context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *tcp_channel = (kaa_tcp_channel_t *) self->context;

    kaa_error_t error_code = KAA_ERR_NONE;

    KAA_LOG_INFO(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) checking keepalive", tcp_channel->access_point.id);

    if (tcp_channel->keepalive.keepalive_interval == 0
            || tcp_channel->channel_state != KAA_TCP_CHANNEL_AUTHORIZED) {
        return error_code;
    }

    kaa_time_t interval = (kaa_time_t) ext_get_systime() - (kaa_time_t) tcp_channel->keepalive.last_sent_keepalive;

    if (interval >= (tcp_channel->keepalive.keepalive_interval / 2)) {
        //Send ping request
        error_code = kaa_tcp_channel_ping(tcp_channel);
    }

    return error_code;
}

/*
 * Set socket events callbacks.
 */
kaa_error_t kaa_tcp_channel_set_socket_events_callback(kaa_transport_channel_interface_t *self, on_kaa_tcp_channel_event_fn callback, void *context)
{
    KAA_RETURN_IF_NIL2(self, self->context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *tcp_channel = (kaa_tcp_channel_t *) self->context;

    tcp_channel->event_callback = callback;
    tcp_channel->event_context = context;

    KAA_LOG_INFO(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) set socket events callbacks", tcp_channel->access_point.id);

    return KAA_ERR_NONE;

}

kaa_error_t kaa_tcp_channel_set_keepalive_timeout(kaa_transport_channel_interface_t *self, uint16_t keepalive)
{
    KAA_RETURN_IF_NIL2(self, self->context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *tcp_channel = (kaa_tcp_channel_t *)self->context;

    tcp_channel->keepalive.keepalive_interval = keepalive;

    KAA_LOG_INFO(tcp_channel->logger,KAA_ERR_NONE,"Kaa TCP channel(%d) keepalive is set to %d seconds", tcp_channel->access_point.id, tcp_channel->keepalive.keepalive_interval);

    return KAA_ERR_NONE;
}

kaa_error_t kaa_tcp_channel_disconnect(kaa_transport_channel_interface_t  *self)
{
    KAA_RETURN_IF_NIL2(self, self->context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * tcp_channel = (kaa_tcp_channel_t *) self->context;

    return kaa_tcp_channel_disconnect_internal(tcp_channel, KAATCP_DISCONNECT_NONE);
}

void kaa_tcp_channel_connack_message_callback(void *context, kaatcp_connack_t message)
{
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;
    KAA_RETURN_IF_NIL(channel,);

    if (channel->channel_state == KAA_TCP_CHANNEL_AUTHORIZING) {
        if (message.return_code == (uint16_t) KAATCP_CONNACK_SUCCESS) {
            channel->channel_state = KAA_TCP_CHANNEL_AUTHORIZED;
            KAA_LOG_INFO(channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) successfully authorized", channel->access_point.id);

            if (channel->keepalive.keepalive_interval > 0) {
                channel->keepalive.last_receive_keepalive = (kaa_time_t)ext_get_systime();
                channel->keepalive.last_sent_keepalive = channel->keepalive.last_receive_keepalive;
            }

        } else {
            KAA_LOG_INFO(channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) authorization failed", channel->access_point.id);
            kaa_tcp_channel_socket_io_error(channel);
        }
    } else {
        KAA_LOG_INFO(channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) CONACK message received in incorrect state", channel->access_point.id);
        kaa_tcp_channel_socket_io_error(channel);
    }
}

void kaa_tcp_channel_disconnect_message_callback(void *context, kaatcp_disconnect_t message)
{
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;
    KAA_RETURN_IF_NIL(channel,);

    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa TCP channel(%d) DISCONNECT message received", channel->access_point.id);
    kaa_tcp_channel_socket_io_error(channel);
}

void kaa_tcp_channel_kaasync_message_callback(void *context, kaatcp_kaasync_t *message)
{
    //TODO create code
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;
    KAA_RETURN_IF_NIL(channel,);

    KAA_LOG_INFO(channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) KAASYNC message received", channel->access_point.id);

    uint8_t zipped = message->sync_header.flags & KAA_SYNC_ZIPPED_BIT;
    uint8_t encrypted = message->sync_header.flags & KAA_SYNC_ENCRYPTED_BIT;

    if (!zipped && !encrypted) {
        kaa_error_t error_code = kaa_platform_protocol_process_server_sync(
                    channel->transport_context.platform_protocol,
                    message->sync_request,
                    message->sync_request_size);
        if (error_code)
            KAA_LOG_ERROR(channel->logger, error_code, "Kaa TCP channel(%d) failed to process server sync", channel->access_point.id);
    } else {
        KAA_LOG_INFO(channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) received unsupported flags: zipped(%d), encrypted(%d)", channel->access_point.id, zipped, encrypted);
    }

    kaatcp_parser_kaasync_destroy(message);

    //Check if service supports only bootstrap, after sync it disconnects.
    if ((channel->supported_service_count == 1)
       && (channel->supported_services[0] == KAA_SERVICE_BOOTSTRAP)) {
        kaa_tcp_channel_disconnect_internal(channel, KAATCP_DISCONNECT_NONE);
    }
}

void kaa_tcp_channel_pingresp_message_callback(void *context)
{
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *)context;
    KAA_RETURN_IF_NIL(channel,);

    channel->keepalive.last_receive_keepalive = (kaa_time_t)ext_get_systime();

    KAA_LOG_INFO(channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) PING message received", channel->access_point.id);
}

/*
 * Close Kaa TCP channel socket and reset state of channel
 */
kaa_error_t kaa_tcp_channel_socket_io_error(kaa_tcp_channel_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) closing socket", self->access_point.id);

    self->access_point.state = AP_SET;

    self->channel_state = KAA_TCP_CHANNEL_UNDEFINED;

    if (self->access_point.socket_descriptor >= 0)
        error_code = ext_tcp_utils_tcp_socket_close(self->access_point.socket_descriptor);

    self->access_point.socket_descriptor = KAA_TCP_SOCKET_NOT_SET;

    kaa_buffer_reset(self->in_buffer);
    kaa_buffer_reset(self->out_buffer);

    if (self->event_callback)
        self->event_callback(self->event_context, SOCKET_DISCONNECTED, self->access_point.socket_descriptor);

    kaatcp_parser_reset(self->parser);

    return error_code;
}

/*
 * Put Kaa TCP connect message to out buffer.
 */
kaa_error_t kaa_tcp_channel_authorize(kaa_tcp_channel_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    char *buffer = NULL;
    size_t buffer_size = 0;
    error_code = kaa_buffer_allocate_space(self->out_buffer, &buffer, &buffer_size);
    KAA_RETURN_IF_ERR(error_code);

    kaa_serialize_info_t serialize_info;
    serialize_info.services = self->supported_services;
    serialize_info.services_count = self->supported_service_count;
    serialize_info.allocator = kaa_tcp_write_pending_services_allocator_fn;
    serialize_info.allocator_context = (void*) self;

    char *sync_buffer = NULL;
    size_t sync_size = 0;

    error_code = kaa_platform_protocol_serialize_client_sync(
            self->transport_context.platform_protocol,
            &serialize_info,
            &sync_buffer,
            &sync_size);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) going to send CONNECT message (%d bytes)", self->access_point.id, sync_size);


    kaa_tcp_channel_delete_pending_services(self, self->pending_request_services, self->pending_request_service_count);

    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Kaa TCP channel(%d) failed to serialize supported services",
                            self->access_point.id);
        if (sync_buffer)
            KAA_FREE(sync_buffer);
        return error_code;
    }

    kaatcp_connect_t connect_message;
    kaatcp_error_t kaatcp_error_code = kaatcp_fill_connect_message(
            self->keepalive.keepalive_interval * 1.2,
            KAA_TCP_CHANNEL_NEXT_PROTOCOL_BINARY_ID,
            sync_buffer,
            sync_size,
            self->encryption.aes_session_key,
            self->encryption.aes_session_key_size,
            self->encryption.signature,
            self->encryption.signature_size,
            &connect_message);


    if (kaatcp_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel(%d) failed to fill CONNECT message",
                                    self->access_point.id);
        if (sync_buffer)
            KAA_FREE(sync_buffer);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    kaatcp_error_code = kaatcp_get_request_connect(&connect_message, buffer, &buffer_size);

    if (kaatcp_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel(%d) failed to get serialize CONNECT message",
                                        self->access_point.id);
        if (sync_buffer)
            KAA_FREE(sync_buffer);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) created CONNECT message (%d bytes)", self->access_point.id, buffer_size);

    error_code = kaa_buffer_lock_space(self->out_buffer, buffer_size);

    if (sync_buffer) {
        KAA_FREE(sync_buffer);
        sync_buffer = NULL;
    }

    KAA_RETURN_IF_ERR(error_code);

    self->channel_state = KAA_TCP_CHANNEL_AUTHORIZING;

    return error_code;
}


/*
 * Checks is specified service pending to sync.
 */
bool is_service_pending(kaa_tcp_channel_t *self, const kaa_service_t service)
{
    KAA_RETURN_IF_NIL2(self, self->pending_request_services, false);

    int i = 0;
    for(; i < self->pending_request_service_count; ++i) {
        if (self->pending_request_services[i] == service) {
            return true;
        }
    }
    return false;
}

/*
 * Delete specified services from pending list.
 */
kaa_error_t kaa_tcp_channel_delete_pending_services(kaa_tcp_channel_t *self, const kaa_service_t services[], size_t service_count)
{
    KAA_RETURN_IF_NIL2(self, services, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) %d pending services, going to delete %d services",
            self->access_point.id,
            self->pending_request_service_count,
            service_count);

    //Check if services to delete point to himself
    if (services == self->pending_request_services) {
        //Remove all
        KAA_FREE(self->pending_request_services); //free previous pending services array.
        self->pending_request_services = NULL;
        self->pending_request_service_count = 0;
        return KAA_ERR_NONE;
    }

    int services_to_del = 0; // how much new services need to delete, necessary to calculate new array size
    int i = 0;
    for(; i<service_count; ++i) {
        if(is_service_pending(self, services[i])) {
            services_to_del++;
        }
    }
    int new_services_count = self->pending_request_service_count - services_to_del;
    kaa_service_t *new_services = NULL;
    if (new_services_count > 0) {
        kaa_service_t *new_services = (kaa_service_t *) KAA_CALLOC(new_services_count, sizeof(kaa_service_t));
        KAA_RETURN_IF_NIL(new_services, KAA_ERR_NOMEM);
        bool found = false;
        int new_count = 0;
        int i = 0;
        for(; i < self->pending_request_service_count; ++i) {
            found = false;
            int j = 0;
            for(; j < service_count; ++j) {
                if (self->pending_request_services[i] == services[j]) {
                    found = true;
                    break;
                }
                if (!found) {
                    new_services[new_count] = self->pending_request_services[i];
                    new_count++;
                }
            }
        }
    }
    KAA_FREE(self->pending_request_services); //free previous pending services array.
    self->pending_request_services = new_services;
    self->pending_request_service_count = new_services_count;


    return KAA_ERR_NONE;
}

/*
 * Update pending service list with specified list. Pending service list should have only unique services.
 */
kaa_error_t kaa_tcp_channel_update_pending_services(kaa_tcp_channel_t *self, const kaa_service_t services[], size_t service_count)
{
    KAA_RETURN_IF_NIL2(self, services, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(self->logger,KAA_ERR_NONE,"Kaa TCP channel(%d) %d pending services, going to update %d services",
                    self->access_point.id,
                    self->pending_request_service_count,
                    service_count);

    /** First call of sync handlers services, no one services wait */
    if (self->pending_request_service_count == 0) {
        self->pending_request_services = (kaa_service_t *) KAA_CALLOC(service_count,sizeof(kaa_service_t));
        KAA_RETURN_IF_NIL(self->pending_request_services, KAA_ERR_NOMEM);
        int i = 0;
        for(; i < service_count; ++i) {
            self->pending_request_services[i] = services[i];
        }
        self->pending_request_service_count = service_count;
    } else {
    /** Some services waiting to sync with service, need merge with other services */
        int services_to_add = 0; // how much new services need to add, necessary to calculate new array size
        int i = 0;
        for(; i < service_count; ++i) {
            if(!is_service_pending(self,services[i])) {
                services_to_add++;
            }
        }
        if (services_to_add > 0) {
            int new_services_count = self->pending_request_service_count + services_to_add;
            kaa_service_t *new_services = (kaa_service_t *) KAA_CALLOC(new_services_count, sizeof(kaa_service_t));
            KAA_RETURN_IF_NIL(new_services, KAA_ERR_NOMEM);
            int i = 0;
            for(; i < self->pending_request_service_count; ++i) {
                new_services[i] = self->pending_request_services[i];
            }
            int new_count = self->pending_request_service_count;
            for(i = 0; i < service_count; ++i) {
                if(!is_service_pending(self,services[i])) {
                    if (new_count < new_services_count) {
                        new_services[new_count] = services[i];
                        new_count++;
                    }
                }
            }
            KAA_FREE(self->pending_request_services); //free previous pending services array.
            self->pending_request_services = new_services;
            self->pending_request_service_count = new_services_count;
        }
    }

    return KAA_ERR_NONE;
}

/*
 * Callback function, when access point hostname resolved.
 */
kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolved(void *context, const kaa_sockaddr_t *addr, kaa_socklen_t addr_size)
{
    KAA_RETURN_IF_NIL2(context, addr, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;

    memcpy(&channel->access_point.sockaddr, addr, addr_size);
    channel->access_point.sockaddr_length = addr_size;

    channel->access_point.state = AP_RESOLVED;

    KAA_LOG_INFO(channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) hostname resolved",
                                channel->access_point.id);

    kaa_error_t error_code = kaa_tcp_channel_connect_access_point(channel);
    if (error_code && channel->event_callback)
        channel->event_callback(channel->event_context, SOCKET_CONNECTION_ERROR, channel->access_point.socket_descriptor);
    return error_code;
}

/*
 * Callback function, when access point hostname resolve failed.
 */
kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolve_failed(void *context)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;

    KAA_LOG_INFO(channel->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) hostname resolve failed", channel->access_point.id);

    channel->access_point.state = AP_NOT_SET;

    return KAA_ERR_NONE;
}

/*
 * Read uint32 value from buffer.
 */
uint32_t get_uint32_t(const char * buffer)
{
    return KAA_NTOHL(*(uint32_t *) buffer);
}

/*
 * Connect access point.
 */
kaa_error_t kaa_tcp_channel_connect_access_point(kaa_tcp_channel_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    if (self->access_point.state != AP_RESOLVED)
        return KAA_ERR_BAD_STATE;

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) connecting to the server...",
                            self->access_point.id);
    kaa_error_t error_code = ext_tcp_utils_open_tcp_socket(
                &self->access_point.socket_descriptor,
                (kaa_sockaddr_t *) &self->access_point.sockaddr,
                self->access_point.sockaddr_length);

    KAA_RETURN_IF_ERR(error_code);
    self->access_point.state = AP_CONNECTING;
    return KAA_ERR_NONE;
}

/*
 * Release access point.
 */
kaa_error_t kaa_tcp_channel_release_access_point(kaa_tcp_channel_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    kaa_error_t error_code = KAA_ERR_NONE;

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) releasing channel's resources", self->access_point.id);

    if (self->access_point.state == AP_CONNECTED
            || self->access_point.state == AP_CONNECTING) {
        ext_tcp_utils_tcp_socket_close(self->access_point.socket_descriptor);
    }

    self->access_point.socket_descriptor = KAA_TCP_SOCKET_NOT_SET;
    self->access_point.state = AP_NOT_SET;
    self->access_point.id = 0;

    if (self->access_point.hostname) {
        KAA_FREE(self->access_point.hostname);
        self->access_point.hostname = NULL;
        self->access_point.hostname_length = 0;
    }
    if (self->access_point.public_key) {
        KAA_FREE(self->access_point.public_key);
        self->access_point.public_key = NULL;
        self->access_point.public_key_length = 0;
    }

    return error_code;
}

/*
 * Write to socket sync services.
 */
kaa_error_t kaa_tcp_channel_write_pending_services(kaa_tcp_channel_t *self, kaa_service_t *service, size_t services_count)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) going to serialize %d pending services",
                        self->access_point.id,
                        self->pending_request_service_count);

    KAA_RETURN_IF_NIL(services_count, KAA_ERR_NONE);

    char *buffer = NULL;
    size_t buffer_size = 0;
    kaa_error_t error_code = kaa_buffer_allocate_space(self->out_buffer, &buffer, &buffer_size);
    KAA_RETURN_IF_ERR(error_code);

    kaa_serialize_info_t serialize_info;
    serialize_info.services = service;
    serialize_info.services_count = services_count;
    serialize_info.allocator = kaa_tcp_write_pending_services_allocator_fn;
    serialize_info.allocator_context = (void*) self;

    char *sync_buffer = NULL;
    size_t sync_size = 0;

    error_code = kaa_platform_protocol_serialize_client_sync(
            self->transport_context.platform_protocol,
            &serialize_info,
            &sync_buffer,
            &sync_size);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) serialized client sync (%d bytes)", self->access_point.id, sync_size);

    kaa_tcp_channel_delete_pending_services(self, service, services_count);

    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Kaa TCP channel(%d) failed to serialize client sync", self->access_point.id);
        if (sync_buffer)
            KAA_FREE(sync_buffer);
        return error_code;
    }

    kaatcp_kaasync_t kaa_sync_message;

    bool zipped = false;
    bool encrypted = false;

    kaatcp_error_t pareser_error_code = kaatcp_fill_kaasync_message(
            sync_buffer,
            sync_size,
            self->message_id++,
            zipped,
            encrypted,
            &kaa_sync_message);

    if (pareser_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel(%d) failed to fill KAASYNC message", self->access_point.id);
        if (sync_buffer)
            KAA_FREE(sync_buffer);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    pareser_error_code = kaatcp_get_request_kaasync(&kaa_sync_message, buffer, &buffer_size);
    if (pareser_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel(%d) failed to serialize KAASYNC message", self->access_point.id);
        if (sync_buffer)
            KAA_FREE(sync_buffer);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Kaa TCP channel(%d) going to send KAASYNC message (%d bytes)", self->access_point.id, sync_size);

    error_code = kaa_buffer_lock_space(self->out_buffer, buffer_size);

    if (sync_buffer) {
        KAA_FREE(sync_buffer);
        sync_buffer = NULL;
    }
    KAA_RETURN_IF_ERR(error_code);

    error_code = kaa_tcp_write_buffer(self);
    return error_code;
}

/*
 * Write to socket all unprocessed bytes from out_buffer.
 */
kaa_error_t kaa_tcp_write_buffer(kaa_tcp_channel_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    kaa_error_t error_code = KAA_ERR_NONE;
    char *buf = NULL;
    size_t buf_size = 0;
    size_t bytes_written = 0;
    error_code = kaa_buffer_get_unprocessed_space(self->out_buffer, &buf, &buf_size);
    KAA_LOG_INFO(self->logger, error_code, "Kaa TCP channel(%d) writing %d bytes to the socket", self->access_point.id, buf_size);
    KAA_RETURN_IF_ERR(error_code);
    if (buf_size > 0) {
        ext_tcp_socket_io_errors_t ioe = ext_tcp_utils_tcp_socket_write(
                self->access_point.socket_descriptor,
                buf, buf_size, &bytes_written);
        switch (ioe) {
            case KAA_TCP_SOCK_IO_OK:
                error_code = kaa_buffer_free_allocated_space(self->out_buffer, bytes_written);
                KAA_LOG_TRACE(self->logger, error_code, "Kaa TCP channel(%d) %d bytes were successfully written", self->access_point.id, bytes_written);
                break;
            default:
                KAA_LOG_TRACE(self->logger, KAA_ERR_SOCKET_ERROR, "Kaa TCP channel(%d) write failed", self->access_point.id);
                error_code = kaa_tcp_channel_socket_io_error(self);
                error_code = KAA_ERR_SOCKET_ERROR;
                break;
        }
    }
    return error_code;
}

/*
 * Memory allocator for kaa_platform_protocol_serialize_client_sync() method.
 */
char *kaa_tcp_write_pending_services_allocator_fn(void *context, size_t buffer_size)
{
    KAA_RETURN_IF_NIL(context, NULL);
    char *buffer = (char *) KAA_MALLOC(buffer_size);
    return buffer;
}

/*
 * Send Ping request message
 */
kaa_error_t kaa_tcp_channel_ping(kaa_tcp_channel_t * self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    kaa_error_t error_code = KAA_ERR_NONE;
    char *buffer = NULL;
    size_t buffer_size = 0;
    error_code = kaa_buffer_allocate_space(self->out_buffer, &buffer, &buffer_size);
    KAA_RETURN_IF_ERR(error_code);

    kaatcp_error_t kaatcp_error_code = kaatcp_get_request_ping(buffer, &buffer_size);

    if (kaatcp_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel(%d) failed to serialize PING message",
                self->access_point.id);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    error_code = kaa_buffer_lock_space(self->out_buffer, buffer_size);

    self->keepalive.last_sent_keepalive = (kaa_time_t) ext_get_systime();

    KAA_LOG_INFO(self->logger,KAA_ERR_NONE,"Kaa TCP channel(%d) going to send PING message (%d bytes)", self->access_point.id, buffer_size);

    return error_code;
}

/*
 * Send Kaa TCP disconnect message
 */
kaa_error_t kaa_tcp_channel_disconnect_internal(kaa_tcp_channel_t  *self, kaatcp_disconnect_reason_t return_code)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    char *buffer = NULL;
    size_t buffer_size = 0;
    error_code = kaa_buffer_allocate_space(self->out_buffer, &buffer, &buffer_size);
    KAA_RETURN_IF_ERR(error_code);

    kaatcp_disconnect_t disconnect_message;
    kaatcp_error_t kaatcp_error_code = kaatcp_fill_disconnect_message(return_code, &disconnect_message);

    if (kaatcp_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel(%d) failed to fill DISCONNECT message", self->access_point.id);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    kaatcp_error_code = kaatcp_get_request_disconnect(&disconnect_message, buffer, &buffer_size);

    if (kaatcp_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel(%d) failed to serialize DISCONNECT message", self->access_point.id);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    error_code = kaa_buffer_lock_space(self->out_buffer, buffer_size);

    self->channel_state = KAA_TCP_CHANNEL_DISCONNECTING;

    KAA_LOG_INFO(self->logger,KAA_ERR_NONE,"Kaa TCP channel(%d) going to send DISCONNECT message (%d bytes)", self->access_point.id, buffer_size);

    return error_code;
}

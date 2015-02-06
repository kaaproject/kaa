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
 * @file kaa_tcp_channel.c
 *
 *  Created on: Jan 16, 2015
 *      Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
 */

#include <stdbool.h>
#include <stdint.h>
#include <stddef.h>
#include <string.h>
#include "kaa_tcp_channel.h"
#include "../kaa_common.h"
#include "../utilities/kaa_mem.h"
#include "../utilities/kaa_buffer.h"
#include "../utilities/kaa_log.h"
#include "../kaa_protocols/kaa_tcp/kaatcp.h"

#define KAA_TCP_CHANNEL_IN_BUFFER_SIZE 1024
#define KAA_TCP_CHANNEL_OUT_BUFFER_SIZE 2024

#define KAA_TCP_CHANNEL_TRANSPORT_PROTOCOL_ID 0x56c8ff92
#define KAA_TCP_CHANNEL_TRANSPORT_PROTOCOL_VERSION 1

typedef time_t kaa_time_t;


typedef enum {
    AP_UNDEFINED = 0,
    AP_NOT_SET, AP_SET, AP_RESOLVED, AP_CONNECTING, AP_CONNECTED, AP_DISCONNECTED
} access_point_state_t;

typedef enum {
    KAA_TCP_CHANNEL_UNDEFINED, KAA_TCP_CHANNEL_AUTHORIZING, KAA_TCP_CHANNEL_AUTHORIZED
} kaa_tcp_channel_state_t;

typedef struct {
    access_point_state_t state;
    uint32_t    id;
    char * public_key;
    uint32_t public_key_length;
    char * hostname;
    uint32_t hostname_length;
    kaa_sockaddr_t sockaddr;
    kaa_socklen_t  sockaddr_length;
    kaa_fd  socket_descriptor;
} kaa_tcp_access_point_t;

typedef struct {
    uint16_t        keepalive_interval;
    kaa_time_t      last_sent_keepalive;
} kaa_tcp_keepalive_t ;

typedef struct {
    char * aes_session_key;
    size_t aes_session_key_size;
    char * signature;
    size_t signature_size;
} kaa_tcp_encrypt_t;

typedef struct {
    kaa_logger_t *logger;
    kaa_tcp_channel_state_t channel_state;
    kaa_transport_protocol_id_t protocol_id;
    kaa_transport_context_t transport_context;
    kaa_tcp_channel_event_fn event_callback;
    void * event_context;
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


kaa_error_t kaa_tcp_channel_get_transport_protocol_info(void * context, kaa_transport_protocol_id_t *protocol_info);
kaa_error_t kaa_tcp_channel_get_supported_services(void * context, kaa_service_t **supported_services, size_t *service_count);
kaa_error_t kaa_tcp_channel_sync_handler(void * context, const kaa_service_t services[], size_t service_count);
kaa_error_t kaa_tcp_channel_release_context(void * context);
kaa_error_t kaa_tcp_channel_init(void *context, kaa_transport_context_t *transport_context);
kaa_error_t kaa_tcp_channel_set_access_point(void *context, kaa_access_point_t *access_point);

/**
 * Specififc kaa_tcp_channel functions
 */
kaa_error_t kaa_tcp_channel_get_socket_for_event(kaa_transport_channel_interface_t * channel, fd_event_t event_type, kaa_fd * fd_p);
kaa_error_t kaa_tcp_channel_process_event(kaa_transport_channel_interface_t * channel, fd_event_t event_type, kaa_fd fd_p);
kaa_error_t kaa_tcp_channel_set_socket_events_callback(kaa_transport_channel_interface_t * channel, kaa_tcp_channel_event_fn callback, void * context);

/*
 * Parser handlers
 */
void kaa_tcp_channel_connack_message_callback(void * context, kaatcp_connack_t message);
void kaa_tcp_channel_disconnect_message_callback(void * context, kaatcp_disconnect_t message);
void kaa_tcp_channel_kaasync_message_callback(void * context, kaatcp_kaasync_t *message);
void kaa_tcp_channel_pingresp_message_callback(void * context);
/**
 * Internal functions
 */
kaa_error_t kaa_tcp_channel_socket_io_error(kaa_tcp_channel_t * channel);
kaa_error_t kaa_tcp_channel_authorize(kaa_tcp_channel_t * channel);
bool is_service_pending(kaa_tcp_channel_t * channel, const kaa_service_t service);
kaa_error_t kaa_tcp_channel_delete_pending_services(kaa_tcp_channel_t * channel, const kaa_service_t services[], size_t service_count);
kaa_error_t kaa_tcp_channel_update_pending_services(kaa_tcp_channel_t * channel, const kaa_service_t services[], size_t service_count);
kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolved(void *context, const kaa_sockaddr_t *addr, kaa_socklen_t addr_size);
kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolve_failed(void *context);
uint32_t get_uint32_t(const char * buffer);
kaa_error_t kaa_tcp_channel_connect_access_point(kaa_tcp_channel_t * channel);
kaa_error_t kaa_tcp_channel_release_access_point(kaa_tcp_channel_t * channel);
kaa_error_t kaa_tcp_channel_write_pending_services(kaa_tcp_channel_t * channel, kaa_service_t *service, size_t services_count);
kaa_error_t kaa_tcp_write_buffer(kaa_tcp_channel_t * channel);
char* kaa_tcp_write_pending_services_allocator_fn(void *context, size_t buffer_size);

/**
 * Create TCP channel object
 */
kaa_error_t kaa_tcp_channel_create(kaa_transport_channel_interface_t * channel, kaa_logger_t *logger, kaa_service_t *supported_services, size_t supported_service_count)
{
    KAA_RETURN_IF_NIL2(channel,logger,KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(logger,KAA_ERR_NONE,"Kaa tcp channel creating....");

    kaa_error_t ret = KAA_ERR_NONE;

    kaa_tcp_channel_t * kaa_tcp_channel = KAA_CALLOC(1,sizeof(kaa_tcp_channel_t));
    KAA_RETURN_IF_NIL(kaa_tcp_channel,KAA_ERR_NOMEM);

    kaa_tcp_channel->channel_state = KAA_TCP_CHANNEL_UNDEFINED;
    kaa_tcp_channel->access_point.state = AP_NOT_SET;
    kaa_tcp_channel->access_point.socket_descriptor = KAA_TCP_SOCKET_NOT_SET;

    if (supported_service_count > 0) {
        kaa_tcp_channel->supported_services = KAA_CALLOC(supported_service_count, sizeof(kaa_service_t));
        KAA_RETURN_IF_NIL(kaa_tcp_channel->supported_services,KAA_ERR_NOMEM);

        for(int i=0;i<supported_service_count;i++) {
            kaa_tcp_channel->supported_services[i] = supported_services[i];
        }
        kaa_tcp_channel->supported_service_count = supported_service_count;
    }

    ret = kaa_buffer_create_buffer(&kaa_tcp_channel->in_buffer, KAA_TCP_CHANNEL_IN_BUFFER_SIZE);
    KAA_RETURN_IF_ERR(ret);
    ret = kaa_buffer_create_buffer(&kaa_tcp_channel->out_buffer, KAA_TCP_CHANNEL_OUT_BUFFER_SIZE);
    KAA_RETURN_IF_ERR(ret);

    kaa_tcp_channel->parser = KAA_CALLOC(1,sizeof(kaatcp_parser_t));
    KAA_RETURN_IF_NIL(kaa_tcp_channel->parser,KAA_ERR_NOMEM);

    kaa_tcp_channel->keepalive.keepalive_interval = 1000; //TODO create setter.
    KAA_LOG_TRACE(logger,KAA_ERR_NONE,"Kaa tcp channel set keepalive to %d ",kaa_tcp_channel->keepalive.keepalive_interval);

    kaa_tcp_channel->protocol_id.id = KAA_TCP_CHANNEL_TRANSPORT_PROTOCOL_ID;
    kaa_tcp_channel->protocol_id.version = KAA_TCP_CHANNEL_TRANSPORT_PROTOCOL_VERSION;
    KAA_LOG_TRACE(logger,KAA_ERR_NONE,"Kaa tcp channel set transport protocol id 0x%08x ",kaa_tcp_channel->protocol_id);

    kaa_tcp_channel->logger = logger;

    kaatcp_parser_handlers_t parser_handler;
    parser_handler.connack_handler = kaa_tcp_channel_connack_message_callback;
    parser_handler.disconnect_handler = kaa_tcp_channel_disconnect_message_callback;
    parser_handler.kaasync_handler = kaa_tcp_channel_kaasync_message_callback;
    parser_handler.pingresp_handler = kaa_tcp_channel_pingresp_message_callback;
    parser_handler.handlers_context = (void*) kaa_tcp_channel;
    kaatcp_error_t parser_ret = kaatcp_parser_init(kaa_tcp_channel->parser, &parser_handler);
    if (parser_ret) {
        return KAA_ERR_TCPCHANNEL_PARSER_INIT_FAILED;
    }

    channel->context = (void*) kaa_tcp_channel;
    channel->get_protocol_id = kaa_tcp_channel_get_transport_protocol_info;
    channel->get_supported_services = kaa_tcp_channel_get_supported_services;
    channel->release_context = kaa_tcp_channel_release_context;
    channel->sync_handler = kaa_tcp_channel_sync_handler;
    channel->init = kaa_tcp_channel_init;
    channel->set_access_point = kaa_tcp_channel_set_access_point;

    KAA_LOG_INFO(logger,KAA_ERR_NONE,"Kaa tcp channel created");

    return ret;
}

/**
 * Return transport protocol id constant
 */
kaa_error_t kaa_tcp_channel_get_transport_protocol_info(void * context, kaa_transport_protocol_id_t *protocol_info)
{
    KAA_RETURN_IF_NIL2(context,protocol_info,KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;
    kaa_error_t ret = KAA_ERR_NONE;
    *protocol_info = channel->protocol_id;
    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel return transport protocol id 0x%08x ",*protocol_info);

    return ret;
}

/**
 * Return supported services list
 */
kaa_error_t kaa_tcp_channel_get_supported_services(void * context, kaa_service_t **supported_services, size_t *service_count) {
    KAA_RETURN_IF_NIL3(context,supported_services,service_count,KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;

    kaa_error_t ret = KAA_ERR_NONE;

    *supported_services = channel->supported_services;
    *service_count = channel->supported_service_count;

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel return supported services");

    return ret;
}

/**
 * Sync specified services list with server side.
 */
kaa_error_t kaa_tcp_channel_sync_handler(void * context, const kaa_service_t services[], size_t service_count) {
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;

    kaa_error_t ret = KAA_ERR_NONE;

    if (services && service_count > 0) {
        ret = kaa_tcp_channel_update_pending_services(channel, services, service_count);
        KAA_RETURN_IF_ERR(ret);
    }

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel sync_handler ready to sync %d services", channel->pending_request_service_count);

    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel sync_handler complete.");

    return ret;
}

/**
 * Release Kaa tcp channel context
 */
kaa_error_t kaa_tcp_channel_release_context(void * context) {
    KAA_RETURN_IF_NIL(context,KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;
    kaa_error_t ret = KAA_ERR_NONE;

    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel release_context.");

    if (channel->parser) {
        KAA_FREE(channel->parser);
        channel->parser = NULL;
    }


    ret = kaa_tcp_channel_release_access_point(channel);

    ret = kaa_buffer_destroy(channel->in_buffer);

    ret = kaa_buffer_destroy(channel->out_buffer);


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


    return ret;
}

/**
 * Init Kaa tcp channel transport context
 */
kaa_error_t kaa_tcp_channel_init(void *context, kaa_transport_context_t *transport_context)
{
    KAA_RETURN_IF_NIL2(context, transport_context, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL2(transport_context->platform_protocol, transport_context->bootstrap_manager, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;

    kaa_error_t ret = KAA_ERR_NONE;

    channel->transport_context = *transport_context;

    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel transport context initialized.");

    return ret;
}

/**
 * Set access point to Kaa tcp channel.
 */
kaa_error_t kaa_tcp_channel_set_access_point(void *context, kaa_access_point_t *access_point)
{
    KAA_RETURN_IF_NIL2(context, access_point, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;

    kaa_error_t error_code = KAA_ERR_NONE;

    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel setting access point.");

    if (channel->access_point.state != AP_NOT_SET) {
        KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel previous access point (%d) remove", channel->access_point.id);
        error_code = kaa_tcp_channel_release_access_point(channel);
        KAA_RETURN_IF_ERR(error_code);
    }
    channel->access_point.state = AP_SET;
    channel->access_point.id = access_point->id;

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel new access point (%d)", channel->access_point.id);

    /*
     * Format of connection_data KAA TCP:
     *  byte[] interfaceData = toUTF8Bytes(context.getConfiguration().getBindInterface());
     *  byte[] publicKeyData = context.getServerKey().getEncoded();
     *  ByteBuffer buf = ByteBuffer.wrap(new byte[SIZE_OF_INT*3 + interfaceData.length + publicKeyData.length]);
     *  buf.putInt(publicKeyData.length);
     *  buf.put(publicKeyData);
     *  buf.putInt(interfaceData.length);
     *  buf.put(interfaceData);
     *  buf.putInt(context.getConfiguration().getBindPort());
     *
     */
    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel new access point (%d), connection data length %d",
            channel->access_point.id,
            access_point->connection_data_len);


    char * connection_data = access_point->connection_data;
    size_t connection_data_len = access_point->connection_data_len;

    int position = 0;
    int remaining_to_read = 4;
    if ((position + remaining_to_read) <= connection_data_len) {
        channel->access_point.public_key_length = get_uint32_t(connection_data);
        position += remaining_to_read;
        KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel new access point (%d), 0x%02X, 0x%02X, 0x%02X, 0x%02X",
                channel->access_point.id,
                connection_data[0],
                connection_data[1],
                connection_data[2],
                connection_data[3]);
    } else {
        KAA_LOG_ERROR(channel->logger,KAA_ERR_INSUFFICIENT_BUFFER,"Kaa tcp channel new access point (%d), "
                "insufficient connection data length  %d, position %d",
                    channel->access_point.id,
                    connection_data_len,
                    (position + remaining_to_read));
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel new access point (%d),"
            " public key length %d",
                channel->access_point.id,
                channel->access_point.public_key_length);

    remaining_to_read = channel->access_point.public_key_length;
    if ((position + remaining_to_read) <= connection_data_len) {
        channel->access_point.public_key = KAA_MALLOC(channel->access_point.public_key_length);
        KAA_RETURN_IF_NIL(channel->access_point.public_key, KAA_ERR_NOMEM);
        memcpy(channel->access_point.public_key,
                connection_data + position,
                remaining_to_read);
        position += remaining_to_read;
    } else {
        KAA_LOG_ERROR(channel->logger,KAA_ERR_INSUFFICIENT_BUFFER,"Kaa tcp channel new access point (%d), "
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
        KAA_LOG_ERROR(channel->logger,KAA_ERR_INSUFFICIENT_BUFFER,"Kaa tcp channel new access point (%d), "
                "insufficient connection data length  %d, position %d",
                    channel->access_point.id,
                    connection_data_len,
                    (position + remaining_to_read));
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel new access point (%d),"
                " hostname length %d",
                    channel->access_point.id,
                    channel->access_point.hostname_length);

    remaining_to_read = channel->access_point.hostname_length;
    if ((position + remaining_to_read) <= connection_data_len) {
        channel->access_point.hostname = KAA_MALLOC(channel->access_point.hostname_length);
        KAA_RETURN_IF_NIL(channel->access_point.hostname, KAA_ERR_NOMEM);
        memcpy(channel->access_point.hostname,
                connection_data + position,
                remaining_to_read);
        position += remaining_to_read;
    } else {
        KAA_LOG_ERROR(channel->logger,KAA_ERR_INSUFFICIENT_BUFFER,"Kaa tcp channel new access point (%d), "
                "insufficient connection data length  %d, position %d",
                    channel->access_point.id,
                    connection_data_len,
                    (position + remaining_to_read));
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    remaining_to_read = 4;
    int access_point_socket_port = 0;
    if ((position + remaining_to_read) <= connection_data_len) {
        access_point_socket_port = (uint16_t)get_uint32_t(connection_data + position);
        position += remaining_to_read;
    } else {
        KAA_LOG_ERROR(channel->logger,KAA_ERR_INSUFFICIENT_BUFFER,"Kaa tcp channel new access point (%d), "
                "insufficient connection data length  %d, position %d",
                    channel->access_point.id,
                    connection_data_len,
                    (position + remaining_to_read));

        return KAA_ERR_INSUFFICIENT_BUFFER;
    }
#ifdef KAA_LOG_LEVEL_TRACE_ENABLED
    char * ap_hostname = KAA_CALLOC(1,channel->access_point.hostname_length+1);
    memcpy(ap_hostname,channel->access_point.hostname,channel->access_point.hostname_length+1);

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel new access point (%d) destination %s:%d",
            channel->access_point.id,
            ap_hostname,
            access_point_socket_port);
    KAA_FREE(ap_hostname);
#endif
    kaa_dns_resolve_listener_t resolve_listener;
    resolve_listener.context = (void*)channel;
    resolve_listener.on_host_resolved = kaa_tcp_channel_set_access_point_hostname_resolved;
    resolve_listener.on_resolve_failed = kaa_tcp_channel_set_access_point_hostname_resolve_failed;

    kaa_dns_resolve_info_t resolve_props;
    resolve_props.hostname = channel->access_point.hostname;
    resolve_props.hostname_length = channel->access_point.hostname_length;
    resolve_props.port = access_point_socket_port;

    ext_tcp_utils_function_return_state_t resolve_state = ext_tcp_utils_gethostbyaddr(
            &resolve_listener,
            &resolve_props,
            &channel->access_point.sockaddr,
            &channel->access_point.sockaddr_length);
    switch (resolve_state) {
        case RET_STATE_VALUE_IN_PROGRESS:
            KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel new access point (%d) destination name resolve pending..",channel->access_point.id);
            break;
        case RET_STATE_VALUE_READY:
            channel->access_point.state = AP_RESOLVED;
            error_code = kaa_tcp_channel_connect_access_point(channel);
            if (error_code) {
                if (channel->event_callback) {
                    channel->event_callback(channel->event_context, SOCKET_CONNECTION_ERROR, channel->access_point.socket_descriptor);
                }
            }
            KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel new access point (%d) destination resolved.",channel->access_point.id);
            break;
        case RET_STATE_VALUE_ERROR:
            channel->access_point.state = AP_NOT_SET;
            error_code = KAA_ERR_TCPCHANNEL_AP_RESOLVE_FAILED;
            KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel new access point (%d) destination name resolve failed.",channel->access_point.id);
            break;
        case RET_STATE_BUFFER_NOT_ENOUGH:
            channel->access_point.state = AP_NOT_SET;
            error_code = KAA_ERR_TCPCHANNEL_AP_RESOLVE_FAILED;
            KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel new access point (%d) destination name resolve failed. Failed to set sockaddr indicate buffer length not enough",channel->access_point.id);
            break;
    }

    return error_code;
}

/** Kaa tcp channel specific API *************************/

/**
 * Return socket for specified event type
 */
kaa_error_t kaa_tcp_channel_get_socket_for_event(kaa_transport_channel_interface_t * channel, fd_event_t event_type, kaa_fd * fd_p)
{
    KAA_RETURN_IF_NIL2(channel, fd_p, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL(channel->context,KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * tcp_channel = (kaa_tcp_channel_t *)channel->context;

    kaa_error_t ret = KAA_ERR_NONE;

    //If nothing socket to put on specified event set as -1 (KAA_TCP_SOCKET_NOT_SET)
    *fd_p = KAA_TCP_SOCKET_NOT_SET;

    switch (event_type) {
        case FD_READ:
            KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) get socket for event READ", tcp_channel->access_point.id);
            if (tcp_channel->access_point.state == AP_CONNECTED) {
                char * buf = NULL;
                size_t buf_size = 0;
                ret = kaa_buffer_get_unprocessed_space(tcp_channel->in_buffer, &buf, &buf_size);
                KAA_RETURN_IF_ERR(ret);
                if (buf_size > 0) {
                    *fd_p = tcp_channel->access_point.socket_descriptor;
                }
            }
            break;
        case FD_WRITE:
            KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) get socket for event WRITE", tcp_channel->access_point.id);
            if (tcp_channel->access_point.state == AP_CONNECTING) {
                //Wait socket connect event.
                *fd_p = tcp_channel->access_point.socket_descriptor;
                KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) get socket for event WRITE, state CONNECTING", tcp_channel->access_point.id);
            } else  if (tcp_channel->access_point.state == AP_CONNECTED) {
                //If there are some pending sync services put W into fd_set
                if (tcp_channel->pending_request_service_count > 0) {
                    if (is_service_pending(tcp_channel, KAA_SERVICE_BOOTSTRAP)
                        || tcp_channel->channel_state == KAA_TCP_CHANNEL_AUTHORIZED){
                        *fd_p = tcp_channel->access_point.socket_descriptor;
                        break;
                    }
                }
                //If out buffer have some bytes to transmit put
                char * buf = NULL;
                size_t buf_size = 0;
                ret = kaa_buffer_get_unprocessed_space(tcp_channel->out_buffer, &buf, &buf_size);
                KAA_RETURN_IF_ERR(ret);
                if (buf_size > 0) {
                    *fd_p = tcp_channel->access_point.socket_descriptor;
                }
            }
            break;
        case FD_EXEPTION:
            KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) get socket for event EXCEPTION", tcp_channel->access_point.id);
            if (tcp_channel->access_point.socket_descriptor > KAA_TCP_SOCKET_NOT_SET) {
                *fd_p = tcp_channel->access_point.socket_descriptor;
            }
            break;
    }

    return ret;
}


/**
 * Process specified event
 */
kaa_error_t kaa_tcp_channel_process_event(kaa_transport_channel_interface_t * channel, fd_event_t event_type, kaa_fd fd_p)
{
    KAA_RETURN_IF_NIL(channel, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL(channel->context,KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * tcp_channel = (kaa_tcp_channel_t *)channel->context;

    if (tcp_channel->access_point.socket_descriptor != fd_p) {
        return KAA_ERR_NONE;
    }

    kaa_error_t ret = KAA_ERR_NONE;

    switch (event_type) {
        case FD_READ:
            KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event READ", tcp_channel->access_point.id);
            if (tcp_channel->access_point.state == AP_CONNECTED) {
                char * buf = NULL;
                size_t buf_size = 0;
                size_t bytes_read = 0;
                ret = kaa_buffer_allocate_space(tcp_channel->in_buffer,&buf,&buf_size);
                KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event READ, empty buffer size %i", tcp_channel->access_point.id, buf_size);
                KAA_RETURN_IF_ERR(ret);
                if (buf_size > 0) {
                    ext_tcp_socket_io_errors_t ioe = ext_tcp_utils_tcp_socket_read(fd_p, buf, buf_size, &bytes_read);
                    switch (ioe) {
                        case KAA_TCP_SOCK_IO_OK:
                            KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event READ, successfully read %i bytes", tcp_channel->access_point.id, bytes_read);
                            ret = kaa_buffer_lock_space(tcp_channel->in_buffer, bytes_read);
                            ret = kaa_buffer_get_unprocessed_space(tcp_channel->in_buffer, &buf, &buf_size);
                            //TODO Modify parser errors code
                            kaatcp_error_t kaatcp_ret = kaatcp_parser_process_buffer(tcp_channel->parser, buf, buf_size);
                            if (kaatcp_ret) {
                                ret = KAA_ERR_TCPCHANNEL_PARSER_ERROR;
                                KAA_LOG_ERROR(tcp_channel->logger, ret, "Kaa tcp channel(%d) process event READ, parser error %d",tcp_channel->access_point.id,kaatcp_ret);
                                kaa_tcp_channel_socket_io_error(tcp_channel);
                            } else {
                                ret = kaa_buffer_free_allocated_space(tcp_channel->in_buffer, buf_size);
                            }
                            break;
                        default:
                            ret = kaa_tcp_channel_socket_io_error(tcp_channel);
                            break;
                    }
                }
            }
            break;
        case FD_WRITE:
            KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event WRITE", tcp_channel->access_point.id);
            if (tcp_channel->access_point.state == AP_CONNECTING) {
                ext_tcp_socket_state_t s = ext_tcp_utils_tcp_socket_check(fd_p, &tcp_channel->access_point.sockaddr, tcp_channel->access_point.sockaddr_length);
                switch (s) {
                    case KAA_TCP_SOCK_ERROR:
                        KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event WRITE, connection failed", tcp_channel->access_point.id);
                        tcp_channel->access_point.state = AP_RESOLVED;
                        if (tcp_channel->event_callback) {
                            tcp_channel->event_callback(tcp_channel->event_context, SOCKET_CONNECTION_ERROR, fd_p);
                        }
                        break;
                    case KAA_TCP_SOCK_CONNECTED:
                        KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event WRITE, connection successfully connected.", tcp_channel->access_point.id);
                        tcp_channel->access_point.state = AP_CONNECTED;
                        if (tcp_channel->event_callback) {
                            tcp_channel->event_callback(tcp_channel->event_context, SOCKET_CONNECTED, fd_p);
                        }

                        //Check supported services, if at least one is not Bootstrap, authorize channel

                        bool need_auth = false;
                        for(int i=0;i<tcp_channel->supported_service_count;i++) {
                            if (tcp_channel->supported_services[i] != KAA_SERVICE_BOOTSTRAP) {
                                //Authorize channel
                                need_auth = true;
                                break;
                            }
                        }
                        if (need_auth) {
                            ret = kaa_tcp_channel_authorize(tcp_channel);
                        }
                        break;
                    case KAA_TCP_SOCK_CONNECTING:
                        KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event WRITE, still connecting....", tcp_channel->access_point.id);
                        break;
                }
            } else if (tcp_channel->access_point.state == AP_CONNECTED) {

                if (tcp_channel->pending_request_service_count > 0) {
                    if ((tcp_channel->pending_request_service_count == 1)
                            && is_service_pending(tcp_channel, KAA_SERVICE_BOOTSTRAP)) {
                        kaa_service_t boostrap_service = {KAA_SERVICE_BOOTSTRAP};
                        KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event WRITE, sync Bootstrap service.", tcp_channel->access_point.id);
                        ret = kaa_tcp_channel_write_pending_services(tcp_channel, &boostrap_service, 1);
                    } else if (tcp_channel->channel_state == KAA_TCP_CHANNEL_AUTHORIZED) {
                        KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event WRITE, sync all services.", tcp_channel->access_point.id);
                        ret = kaa_tcp_channel_write_pending_services(
                                tcp_channel,
                                tcp_channel->pending_request_services,
                                tcp_channel->pending_request_service_count);
                    } else {
                        KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event WRITE, authorize channel.", tcp_channel->access_point.id);
                        ret = kaa_tcp_channel_authorize(tcp_channel);
                    }

                } else {
                    KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event WRITE, no sync pending services.", tcp_channel->access_point.id);
                }


            }
            break;
        case FD_EXEPTION:
            KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event EXCEPION", tcp_channel->access_point.id);
            ret = kaa_tcp_channel_socket_io_error(tcp_channel);
            break;
    }
    KAA_LOG_TRACE(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) process event complete", tcp_channel->access_point.id);
    return ret;
}

/**
 * Set socket events callbacks.
 */
kaa_error_t kaa_tcp_channel_set_socket_events_callback(kaa_transport_channel_interface_t * channel, kaa_tcp_channel_event_fn callback, void * context)
{
    KAA_RETURN_IF_NIL(channel, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL(channel->context,KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * tcp_channel = (kaa_tcp_channel_t *)channel->context;

    kaa_error_t ret = KAA_ERR_NONE;

    tcp_channel->event_callback = callback;
    tcp_channel->event_context = context;

    KAA_LOG_INFO(tcp_channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) set socket events callbacks.", tcp_channel->access_point.id);

    return ret;

}

/**** Parser handlers **************/


void kaa_tcp_channel_connack_message_callback(void * context, kaatcp_connack_t message)
{
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;
    if (!channel) {
        return;
    }

    if (channel->channel_state == KAA_TCP_CHANNEL_AUTHORIZING) {
        if (message.return_code == (uint16_t)KAATCP_CONNACK_SUCCESS) {
            channel->channel_state = KAA_TCP_CHANNEL_AUTHORIZED;
            KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Authorized successfully.", channel->access_point.id);
        } else {
            KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Authorization failed.", channel->access_point.id);
            kaa_tcp_channel_socket_io_error(channel);
        }
    } else {
        KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Got Conack message in incorrect state.", channel->access_point.id);
        kaa_tcp_channel_socket_io_error(channel);
    }
}

void kaa_tcp_channel_disconnect_message_callback(void * context, kaatcp_disconnect_t message)
{
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;
    if (!channel) {
        return;
    }
    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Got Disconnect message.", channel->access_point.id);
    kaa_tcp_channel_socket_io_error(channel);
}

void kaa_tcp_channel_kaasync_message_callback(void * context, kaatcp_kaasync_t *message)
{
    //TODO create code
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;
    if (!channel) {
        return;
    }
    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Got KaaSync message.", channel->access_point.id);

    uint8_t zipped = message->sync_header.flags & KAA_SYNC_ZIPPED_BIT;
    uint8_t encrypted = message->sync_header.flags & KAA_SYNC_ENCRYPTED_BIT;

    if (!zipped && !encrypted) {
        kaa_error_t ret = kaa_platform_protocol_process_server_sync(
                    channel->transport_context.platform_protocol,
                    message->sync_request,
                    message->sync_request_size);
        if (ret) {
            KAA_LOG_ERROR(channel->logger,ret,"Kaa tcp channel(%d) KaaSync message, Error server sync", channel->access_point.id);
        }

    } else {
        KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) KaaSync message, flags unsupported: zipped(%d), encrypted(%d).", channel->access_point.id, zipped, encrypted);
    }

    kaatcp_parser_kaasync_destroy(message);
}

void kaa_tcp_channel_pingresp_message_callback(void * context)
{
    //TODO create code
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;
    if (!channel) {
        return;
    }

    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Got PingResponse message.", channel->access_point.id);
}


/**** Internal functions *****************************/


/**
 * Close Kaa tcp channel socket and reset state of channel
 */
kaa_error_t kaa_tcp_channel_socket_io_error(kaa_tcp_channel_t * channel)
{
    KAA_RETURN_IF_NIL(channel, KAA_ERR_BADPARAM);

    kaa_error_t ret = KAA_ERR_NONE;

    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Close socket.", channel->access_point.id);

    channel->access_point.state = AP_SET;

    channel->channel_state = KAA_TCP_CHANNEL_UNDEFINED;

    if (channel->access_point.socket_descriptor >= 0) {
        ret = ext_tcp_utils_tcp_socket_close(channel->access_point.socket_descriptor);
    }

    channel->access_point.socket_descriptor = KAA_TCP_SOCKET_NOT_SET;

    ret = kaa_buffer_reset(channel->in_buffer);
    ret = kaa_buffer_reset(channel->out_buffer);

    if (channel->event_callback) {
        channel->event_callback(channel->event_context, SOCKET_DISCONNECTED,channel->access_point.socket_descriptor);
    }

    kaatcp_parser_reset(channel->parser);

    return ret;
}

/**
 * Put Kaa tcp connect message to out buffer.
 */
kaa_error_t kaa_tcp_channel_authorize(kaa_tcp_channel_t * channel)
{
    KAA_RETURN_IF_NIL(channel, KAA_ERR_BADPARAM);

    kaa_error_t ret = KAA_ERR_NONE;

    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Authorize channel.", channel->access_point.id);

    char *buffer = NULL;
    size_t buffer_size = 0;
    ret = kaa_buffer_allocate_space(
                channel->out_buffer,
                &buffer,
                &buffer_size);
    KAA_RETURN_IF_ERR(ret);



    kaa_serialize_info_t serialize_info;
    serialize_info.services = channel->supported_services;
    serialize_info.services_count = channel->supported_service_count;
    serialize_info.allocator = kaa_tcp_write_pending_services_allocator_fn;
    serialize_info.allocator_context = (void*)channel;

    char *sync_buffer = NULL;
    size_t sync_size = 0;

    ret = kaa_platform_protocol_serialize_client_sync(
            channel->transport_context.platform_protocol,
            &serialize_info,
            &sync_buffer,
            &sync_size);

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Authorize channel sync supported services, got %d bytes.",
                    channel->access_point.id,
                    sync_size);


    if (ret) {
        KAA_LOG_ERROR(channel->logger,ret,"Kaa tcp channel(%d) Authorize channel failed to serialize supported services .",
                            channel->access_point.id);
        if (sync_buffer) {
            KAA_FREE(sync_buffer);
        }
        return ret;
    }

    kaatcp_connect_t connect_message;
    kaatcp_error_t kaatcp_ret = kaatcp_fill_connect_message(
            channel->keepalive.keepalive_interval,
            channel->protocol_id.id,
            sync_buffer,
            sync_size,
            channel->encryption.aes_session_key,
            channel->encryption.aes_session_key_size,
            channel->encryption.signature,
            channel->encryption.signature_size,
            &connect_message);


    if (kaatcp_ret) {
        KAA_LOG_ERROR(channel->logger,KAA_ERR_TCPCHANNEL_PARSER_ERROR,"Kaa tcp channel(%d) Failed to fill connect message from serialized supported services.",
                                    channel->access_point.id);
        if (sync_buffer) {
            KAA_FREE(sync_buffer);
        }
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    kaatcp_ret = kaatcp_get_request_connect(&connect_message, buffer, &buffer_size);


    if (kaatcp_ret) {
        KAA_LOG_ERROR(channel->logger,KAA_ERR_TCPCHANNEL_PARSER_ERROR,"Kaa tcp channel(%d) Failed to get connect message from serialized supported services.",
                                        channel->access_point.id);
        if (sync_buffer) {
            KAA_FREE(sync_buffer);
        }
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Create connect message, got %d bytes.",
                            channel->access_point.id,
                            buffer_size);

    ret = kaa_buffer_lock_space(channel->out_buffer, buffer_size);

    if (sync_buffer) {
        KAA_FREE(sync_buffer);
        sync_buffer = NULL;
    }

    KAA_RETURN_IF_ERR(ret);

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Authorize channel, put to buffer %d bytes.", channel->access_point.id, buffer_size);

    channel->channel_state = KAA_TCP_CHANNEL_AUTHORIZING;

    return ret;
}


/**
 * Checks is specified service pending to sync.
 */
bool is_service_pending(kaa_tcp_channel_t * channel, const kaa_service_t service)
{
    KAA_RETURN_IF_NIL(channel, false);
    KAA_RETURN_IF_NIL(channel->pending_request_services, false);

    for(int i=0; i<channel->pending_request_service_count; i++) {
        if (channel->pending_request_services[i] == service) {
            KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Service %d pending.", channel->access_point.id, service);
            return true;
        }
    }
    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Service %d don't pending.", channel->access_point.id, service);
    return false;
}

/**
 * Delete specified services from pending list.
 */
kaa_error_t kaa_tcp_channel_delete_pending_services(kaa_tcp_channel_t * channel, const kaa_service_t services[], size_t service_count)
{
    KAA_RETURN_IF_NIL2(channel, services, KAA_ERR_BADPARAM);

    kaa_error_t ret = KAA_ERR_NONE;
    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Now %d services pending, delete %d services",
            channel->access_point.id,
            channel->pending_request_service_count,
            service_count);

    //Check if services to delete point to himself
    if (services == channel->pending_request_services) {
        //Remove all
        KAA_FREE(channel->pending_request_services); //free previous pending services array.
        channel->pending_request_services = NULL;
        channel->pending_request_service_count = 0;
        return ret;
    }

    int services_to_del = 0; // how much new services need to delete, necessary to calculate new array size
    for(int i=0; i<service_count; i++) {
        if(is_service_pending(channel,services[i])) {
            services_to_del++;
        }
    }
    int new_services_count = channel->pending_request_service_count - services_to_del;
    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) New %d services",
                channel->access_point.id,
                new_services_count);
    kaa_service_t * new_services = NULL;
    if (new_services_count > 0) {
        kaa_service_t * new_services = KAA_CALLOC(new_services_count, sizeof(kaa_service_t));
        KAA_RETURN_IF_NIL(new_services, KAA_ERR_NOMEM);
        bool found = false;
        int new_count = 0;
        for(int i=0; i<channel->pending_request_service_count;i++) {
            found = false;
            for(int j=0;j<service_count;j++) {
                if (channel->pending_request_services[i] == services[j]) {
                    found = true;
                    break;
                }
                if (!found) {
                    new_services[new_count] = channel->pending_request_services[i];
                    new_count++;
                }
            }
        }
    }
    KAA_FREE(channel->pending_request_services); //free previous pending services array.
    channel->pending_request_services = new_services;
    channel->pending_request_service_count = new_services_count;


    return ret;
}

/**
 * Update pending service list with specified list. Pending service list should have only unique services.
 */
kaa_error_t kaa_tcp_channel_update_pending_services(kaa_tcp_channel_t * channel, const kaa_service_t services[], size_t service_count)
{
    KAA_RETURN_IF_NIL2(channel, services, KAA_ERR_BADPARAM);

    kaa_error_t ret = KAA_ERR_NONE;

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Pending %d services, updates %d services",
                    channel->access_point.id,
                    channel->pending_request_service_count,
                    service_count);
    /** First call of sync handlers services, no one services wait */
    if (channel->pending_request_service_count == 0) {
        channel->pending_request_services = KAA_CALLOC(service_count,sizeof(kaa_service_t));
        KAA_RETURN_IF_NIL(channel->pending_request_services, KAA_ERR_NOMEM);
        for(int i=0; i<service_count; i++) {
            channel->pending_request_services[i] = services[i];
        }
        channel->pending_request_service_count = service_count;
        KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) No one existing pending services, added new %d services",
                            channel->access_point.id,
                            service_count);
    } else {
    /** Some services waiting to sync with service, need merge with other services */
        int services_to_add = 0; // how much new services need to add, necessary to calculate new array size
        for(int i=0; i<service_count; i++) {
            if(!is_service_pending(channel,services[i])) {
                services_to_add++;
            }
        }
        KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) adding new %d services",
                            channel->access_point.id,
                            services_to_add);
        if (services_to_add > 0) {
            int new_services_count = channel->pending_request_service_count + services_to_add;
            kaa_service_t * new_services = KAA_CALLOC(new_services_count, sizeof(kaa_service_t));
            KAA_RETURN_IF_NIL(new_services, KAA_ERR_NOMEM);
            for(int i=0; i<channel->pending_request_service_count; i++) {
                new_services[i] = channel->pending_request_services[i];
            }
            int new_count = channel->pending_request_service_count;
            for(int i=0; i<service_count; i++) {
                if(!is_service_pending(channel,services[i])) {
                    if (new_count < new_services_count) {
                        new_services[new_count] = services[i];
                        new_count++;
                    }
                }
            }
            KAA_FREE(channel->pending_request_services); //free previous pending services array.
            channel->pending_request_services = new_services;
            channel->pending_request_service_count = new_services_count;
        }
    }

    return ret;
}

/**
 * Callback function, when access point hostname resolved.
 */
kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolved(void *context, const kaa_sockaddr_t *addr, kaa_socklen_t addr_size)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;

    kaa_error_t ret = KAA_ERR_NONE;

    //TODO create correct sockaddr

//    channel->access_point.sockaddr = *addr;
    channel->access_point.sockaddr_length = addr_size;

    channel->access_point.state = AP_RESOLVED;

    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Access point host name resolved.",
                                channel->access_point.id);

    ret = kaa_tcp_channel_connect_access_point(channel);
    if (ret) {
        if (channel->event_callback) {
            channel->event_callback(channel->event_context, SOCKET_CONNECTION_ERROR, channel->access_point.socket_descriptor);
        }
    }
    return ret;
}

/**
 * Callback function, when access point hostname resolve failed.
 */
kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolve_failed(void *context)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;

    kaa_error_t ret = KAA_ERR_NONE;

    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Access point host name resolve failed.",
                                    channel->access_point.id);

    channel->access_point.state = AP_NOT_SET;

    return ret;
}

/**
 * Read uint32 value from buffer.
 */
uint32_t get_uint32_t(const char * buffer)
{
    return KAA_NTOHL(*(uint32_t *) buffer);
}

/**
 * Connect access point.
 */
kaa_error_t kaa_tcp_channel_connect_access_point(kaa_tcp_channel_t * channel)
{
    KAA_RETURN_IF_NIL(channel,KAA_ERR_BADPARAM);
    if (channel->access_point.state != AP_RESOLVED) {
        return KAA_ERR_BAD_STATE;
    }
    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Connecting access point.",
                            channel->access_point.id);
    kaa_error_t ret = ext_tcp_utils_open_tcp_socket(
                &channel->access_point.socket_descriptor,
                &channel->access_point.sockaddr,
                channel->access_point.sockaddr_length);

    KAA_RETURN_IF_ERR(ret);
    channel->access_point.state = AP_CONNECTING;
    return ret;
}

/**
 * Release access point.
 */
kaa_error_t kaa_tcp_channel_release_access_point(kaa_tcp_channel_t * channel)
{
    KAA_RETURN_IF_NIL(channel,KAA_ERR_BADPARAM);
    kaa_error_t ret = KAA_ERR_NONE;

    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Release access point.",
                            channel->access_point.id);

    if (channel->access_point.state == AP_CONNECTED
            || channel->access_point.state == AP_CONNECTING) {
        ext_tcp_utils_tcp_socket_close(channel->access_point.socket_descriptor);
    }

    channel->access_point.socket_descriptor = KAA_TCP_SOCKET_NOT_SET;
    channel->access_point.state = AP_NOT_SET;
    channel->access_point.id = 0;

    if (channel->access_point.hostname) {
        KAA_FREE(channel->access_point.hostname);
        channel->access_point.hostname = NULL;
        channel->access_point.hostname_length = 0;
    }
    if (channel->access_point.public_key) {
        KAA_FREE(channel->access_point.public_key);
        channel->access_point.public_key = NULL;
        channel->access_point.public_key_length = 0;
    }

    return ret;
}

/**
 * Write to socket sync services.
 */
kaa_error_t kaa_tcp_channel_write_pending_services(kaa_tcp_channel_t * channel, kaa_service_t *service, size_t services_count)
{
    KAA_RETURN_IF_NIL(channel, KAA_ERR_BADPARAM);

    kaa_error_t ret = KAA_ERR_NONE;

    KAA_LOG_INFO(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Write to socket %d pending services.",
                        channel->access_point.id,
                        channel->pending_request_service_count);

    ret = kaa_tcp_write_buffer(channel);
    KAA_RETURN_IF_ERR(ret);



    if (channel->pending_request_service_count <= 0) {
        return ret;
    }

    char *buffer = NULL;
    size_t buffer_size = 0;
    ret = kaa_buffer_allocate_space(
                channel->out_buffer,
                &buffer,
                &buffer_size);
    KAA_RETURN_IF_ERR(ret);



    kaa_serialize_info_t serialize_info;
    serialize_info.services = service;
    serialize_info.services_count = services_count;
    serialize_info.allocator = kaa_tcp_write_pending_services_allocator_fn;
    serialize_info.allocator_context = (void*)channel;

    char *sync_buffer = NULL;
    size_t sync_size = 0;

    ret = kaa_platform_protocol_serialize_client_sync(
            channel->transport_context.platform_protocol,
            &serialize_info,
            &sync_buffer,
            &sync_size);

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Serialize pending services sync, got %d bytes.",
                    channel->access_point.id,
                    sync_size);

    kaa_tcp_channel_delete_pending_services(channel,service,services_count);

    if (ret) {
        KAA_LOG_ERROR(channel->logger,ret,"Kaa tcp channel(%d) Failed to serialize pending services.",
                            channel->access_point.id);
        if (sync_buffer) {
            KAA_FREE(sync_buffer);
        }
        return ret;
    }

    kaatcp_kaasync_t kaa_sync_message;

    bool zipped = false;
    bool encrypted = false;

    kaatcp_error_t kaa_parser_ret = kaatcp_fill_kaasync_message(
            sync_buffer,
            sync_size,
            channel->message_id++,
            zipped,
            encrypted,
            &kaa_sync_message);

    if (kaa_parser_ret) {
        KAA_LOG_ERROR(channel->logger,KAA_ERR_TCPCHANNEL_PARSER_ERROR,"Kaa tcp channel(%d) Failed to fill sync message from serialized pending services.",
                                    channel->access_point.id);
        if (sync_buffer) {
            KAA_FREE(sync_buffer);
        }
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    kaa_parser_ret = kaatcp_get_request_kaasync(
            &kaa_sync_message,
            buffer,
            &buffer_size);

    if (kaa_parser_ret) {
        KAA_LOG_ERROR(channel->logger,KAA_ERR_TCPCHANNEL_PARSER_ERROR,"Kaa tcp channel(%d) Failed to get sync message from serialized pending services.",
                                        channel->access_point.id);
        if (sync_buffer) {
            KAA_FREE(sync_buffer);
        }
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Create sync message, got %d bytes.",
                        channel->access_point.id,
                        sync_size);

    ret = kaa_buffer_lock_space(channel->out_buffer, buffer_size);

    if (sync_buffer) {
        KAA_FREE(sync_buffer);
        sync_buffer = NULL;
    }

    KAA_RETURN_IF_ERR(ret);

    ret = kaa_tcp_write_buffer(channel);
    KAA_RETURN_IF_ERR(ret);

    return ret;
}


/**
 * Write to socket all unprocessed bytes from out_buffer.
 */
kaa_error_t kaa_tcp_write_buffer(kaa_tcp_channel_t * channel)
{
    KAA_RETURN_IF_NIL(channel,KAA_ERR_BADPARAM);
    kaa_error_t ret = KAA_ERR_NONE;
    char * buf = NULL;
    size_t buf_size = 0;
    size_t bytes_written = 0;
    ret = kaa_buffer_get_unprocessed_space(channel->out_buffer, &buf, &buf_size);
    KAA_LOG_INFO(channel->logger,ret,"Kaa tcp channel(%d) Write to socket, have a %d bytes.",
                            channel->access_point.id,
                            buf_size);
    KAA_RETURN_IF_ERR(ret);
    if (buf_size > 0) {
        ext_tcp_socket_io_errors_t ioe = ext_tcp_utils_tcp_socket_write(
                channel->access_point.socket_descriptor,
                buf, buf_size, &bytes_written);
        switch (ioe) {
            case KAA_TCP_SOCK_IO_OK:
                ret = kaa_buffer_free_allocated_space(channel->out_buffer, bytes_written);
                KAA_LOG_TRACE(channel->logger,ret,"Kaa tcp channel(%d) Written to socket %d bytes.",
                                            channel->access_point.id,
                                            bytes_written);
                break;
            default:
                KAA_LOG_TRACE(channel->logger,KAA_ERR_SOCKET_ERROR,"Kaa tcp channel(%d) Write to socket failed.",
                                            channel->access_point.id);
                ret = kaa_tcp_channel_socket_io_error(channel);
                ret = KAA_ERR_SOCKET_ERROR;
                break;
        }
    }
    return ret;
}

/**
 * Memory allocator for kaa_platform_protocol_serialize_client_sync() method.
 */
char* kaa_tcp_write_pending_services_allocator_fn(void *context, size_t buffer_size)
{
    KAA_RETURN_IF_NIL(context,NULL);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *)context;

    char *buffer = KAA_MALLOC(buffer_size);

    KAA_LOG_TRACE(channel->logger,KAA_ERR_NONE,"Kaa tcp channel(%d) Allocate %d bytes for serialize sync.",
                                        channel->access_point.id,
                                        buffer_size);

    return buffer;
}

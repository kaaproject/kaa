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


#include "kaa_tcp_channel.h"
#include "../kaa_common.h"
#include "../utilities/kaa_mem.h"
#include "../utilities/kaa_buffer.h"
#include "../kaa_protocols/kaa_tcp/kaatcp.h"

#define KAA_TCP_CHANNEL_IN_BUFFER_SIZE 1024
#define KAA_TCP_CHANNEL_OUT_BUFFER_SIZE 2024

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
    size_t public_key_length;
    char * hostname;
    size_t hostname_length;
    kaa_sockaddr_v4_t sockaddr;
    kaa_fd  socket_descriptor;
} kaa_tcp_access_point_t;


typedef struct {
    kaa_tcp_channel_state_t channel_state;
    kaa_transport_protocol_id_t protocol_id;
    kaa_transport_context_t transport_context;
    kaa_tcp_channel_event_fn event_callback;
    void * event_context;
    kaa_tcp_access_point_t access_point;
    kaa_service_t pending_request_services[];
    size_t pending_request_service_count;
    kaa_buffer_t    * in_buffer;
    kaa_buffer_t    * out_buffer;
    kaatcp_parser_t * parser;
} kaa_tcp_channel_t;



kaa_error_t kaa_tcp_channel_get_transport_protocol_id(void * context, kaa_transport_protocol_id_t *protocol_info);
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


/**
 * Internal functions
 */
bool is_service_pending(kaa_tcp_channel_t * channel, const kaa_service_t service);
kaa_error_t kaa_tcp_channel_authorize(kaa_tcp_channel_t * channel);
kaa_error_t kaa_tcp_channel_sync_bootstrap(kaa_tcp_channel_t * channel);
kaa_error_t kaa_tcp_channel_delete_pending_services(kaa_tcp_channel_t * channel, const kaa_service_t services[], size_t service_count);
kaa_error_t kaa_tcp_channel_update_pending_services(kaa_tcp_channel_t * channel, const kaa_service_t services[], size_t service_count);
kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolved(void *context);
kaa_error_t kaa_tcp_channel_flush_pending_services(kaa_tcp_channel_t * channel);
kaa_error_t kaa_tcp_channel_socket_io_error(kaa_tcp_channel_t * channel);
uint32_t get_uint32_t(const char * buffer);
kaa_error_t kaa_tcp_channel_connect_access_point(kaa_tcp_channel_t * channel);
kaa_error_t kaa_tcp_channel_release_access_point(kaa_tcp_channel_t * channel);

kaa_error_t kaa_tcp_channel_create(kaa_transport_channel_interface_t * channel)
{
    KAA_RETURN_IF_NIL(channel,KAA_ERR_BADPARAM);

    kaa_error_t ret = KAA_ERR_NONE;

    kaa_tcp_channel_t * kaa_tcp_channel = KAA_CALLOC(1,sizeof(kaa_tcp_channel_t));
    KAA_RETURN_IF_NIL(kaa_tcp_channel,KAA_ERR_NOMEM);

    kaa_tcp_channel->channel_state = KAA_TCP_CHANNEL_UNDEFINED;
    kaa_tcp_channel->access_point.state = AP_NOT_SET;
    kaa_tcp_channel->access_point.socket_descriptor = KAA_TCP_SOCKET_NOT_SET;

    ret = kaa_buffer_create_buffer(*kaa_tcp_channel->in_buffer, KAA_TCP_CHANNEL_IN_BUFFER_SIZE);
    KAA_RETURN_IF_ERR(ret);
    ret = kaa_buffer_create_buffer(*kaa_tcp_channel->out_buffer, KAA_TCP_CHANNEL_OUT_BUFFER_SIZE);
    KAA_RETURN_IF_ERR(ret);

    kaa_tcp_channel->parser = KAA_CALLOC(1,sizeof(kaatcp_parser_t));
    KAA_RETURN_IF_NIL(kaa_tcp_channel->parser,KAA_ERR_NOMEM);

    kaatcp_parser_handlers_t parser_handler;

    kaatcp_error_t parser_ret = kaatcp_parser_init(kaa_tcp_channel->parser, &parser_handler);
    if (parser_ret) {
        //TODO make correct error code
        return KAA_ERR_BADDATA;
    }

    channel->context = (void*) kaa_tcp_channel;
    channel->get_protocol_id = kaa_tcp_channel_get_transport_protocol_id;
    channel->get_supported_services = kaa_tcp_channel_get_supported_services;
    channel->release_context = kaa_tcp_channel_release_context;
    channel->sync_handler = kaa_tcp_channel_sync_handler;
    channel->init = kaa_tcp_channel_init;
    channel->set_access_point = kaa_tcp_channel_set_access_point;



    return ret;
}

kaa_error_t kaa_tcp_channel_get_transport_protocol_info(void * context, kaa_transport_protocol_id_t *protocol_id)
{
    KAA_RETURN_IF_NIL2(context,protocol_id,KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;
    kaa_error_t ret = KAA_ERR_NONE;
    *protocol_id = channel->protocol_id;
    return ret;
}

kaa_error_t kaa_tcp_channel_get_supported_services(void * context, kaa_service_t **supported_services, size_t *service_count) {
    KAA_RETURN_IF_NIL3(context,supported_services,service_count,KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;

    kaa_error_t ret = KAA_ERR_NONE;



    return ret;
}

kaa_error_t kaa_tcp_channel_authorize(kaa_tcp_channel_t * channel)
{
    KAA_RETURN_IF_NIL(channel, KAA_ERR_BADPARAM);

    kaa_error_t ret = KAA_ERR_NONE;

    //TODO create connect message

    return ret;
}

kaa_error_t kaa_tcp_channel_sync_bootstrap(kaa_tcp_channel_t * channel)
{
    kaa_error_t ret = KAA_ERR_NONE;

    //TODO create bootstrap message

    return ret;
}

bool is_service_pending(kaa_tcp_channel_t * channel, const kaa_service_t service)
{
    KAA_RETURN_IF_NIL(channel, false);
    KAA_RETURN_IF_NIL(channel->pending_request_services, false);

    for(int i=0; i<channel->pending_request_service_count; i++) {
        if (channel->pending_request_services[i] == service) {
            return true;
        }
    }
    return false;
}

kaa_error_t kaa_tcp_channel_delete_pending_services(kaa_tcp_channel_t * channel, const kaa_service_t services[], size_t service_count)
{
    KAA_RETURN_IF_NIL2(channel, services, KAA_ERR_BADPARAM);

    kaa_error_t ret = KAA_ERR_NONE;

    int services_to_del = 0; // how much new services need to delete, necessary to calculate new array size
    for(int i=0; i<service_count; i++) {
        if(is_service_pending(channel,services[i])) {
            services_to_del++;
        }
    }
    int new_services_count = channel->pending_request_service_count - services_to_del;
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

kaa_error_t kaa_tcp_channel_update_pending_services(kaa_tcp_channel_t * channel, const kaa_service_t services[], size_t service_count)
{
    KAA_RETURN_IF_NIL2(channel, services, KAA_ERR_BADPARAM);

    kaa_error_t ret = KAA_ERR_NONE;

    /** First call of sync handlers services, no one services wait */
    if (channel->pending_request_service_count == 0) {
        channel->pending_request_services = KAA_CALLOC(service_count,sizeof(kaa_service_t));
        KAA_RETURN_IF_NIL(channel->pending_request_services, KAA_ERR_NOMEM);
        for(int i=0; i<service_count; i++) {
            channel->pending_request_services[i] = services[i];
        }
        channel->pending_request_service_count = service_count;
    } else {
    /** Some services waiting to sync with service, need merge with other services */
        int services_to_add = 0; // how much new services need to add, necessary to calculate new array size
        for(int i=0; i<service_count; i++) {
            if(!is_service_pending(channel,services[i])) {
                services_to_add++;
            }
        }
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

kaa_error_t kaa_tcp_channel_sync_handler(void * context, const kaa_service_t services[], size_t service_count) {
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;

    kaa_error_t ret = KAA_ERR_NONE;

    if (services && service_count > 0) {
        ret = kaa_tcp_channel_update_pending_services(channel, services, service_count);
        KAA_RETURN_IF_ERR(ret);
    }

    if (channel->pending_request_service_count > 0) {
        /*
         * All services processed to byte arrays on WR event if session Authorized, except Bootstrap.
         * Bootstrap don't need authorization, so it processed on sync event.
         */
        if (is_service_pending(channel, KAA_SERVICE_BOOTSTRAP)){
            ret = kaa_tcp_channel_sync_bootstrap(channel);
            KAA_RETURN_IF_ERR(ret);

            const kaa_service_t bootstrap_sync_services[1] = { KAA_SERVICE_BOOTSTRAP };
            ret = kaa_tcp_channel_delete_pending_services(channel, &bootstrap_sync_services, 1);
            KAA_RETURN_IF_ERR(ret);
        }

        if ((channel->pending_request_service_count > 0)
                && (channel->channel_state == KAA_TCP_CHANNEL_UNDEFINED)) {
            //Start authorizing
            ret = kaa_tcp_channel_authorize(channel);
        }
    }



    return ret;
}

kaa_error_t kaa_tcp_channel_release_context(void * context) {
    KAA_RETURN_IF_NIL(context,KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;
    kaa_error_t ret = KAA_ERR_NONE;

    KAA_FREE(channel->parser);


    ret = kaa_tcp_channel_release_access_point(channel);

    ret = kaa_buffer_destroy(channel->in_buffer);

    ret = kaa_buffer_destroy(channel->out_buffer);

    if (channel->pending_request_services) {
        KAA_FREE(channel->pending_request_services);
    }

    channel->access_point.state = AP_UNDEFINED;

    KAA_FREE(context);
    return ret;
}

kaa_error_t kaa_tcp_channel_init(void *context, kaa_transport_context_t *transport_context)
{
    KAA_RETURN_IF_NIL2(context, transport_context, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL2(transport_context->platform_protocol, transport_context->bootstrap_maanger, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;

    kaa_error_t ret = KAA_ERR_NONE;

    channel->transport_context = *transport_context;

    return ret;
}

kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolved(void *context) {
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;

    kaa_error_t ret = KAA_ERR_NONE;
    channel->access_point.state = AP_RESOLVED;
    ret = kaa_tcp_channel_connect_access_point(channel);
    if (ret) {
        if (channel->event_callback) {
            channel->event_callback(channel->event_context, SOCKET_CONNECTION_ERROR, channel->access_point.socket_descriptor);
        }
    }
    return ret;
}

kaa_error_t kaa_tcp_channel_set_access_point(void *context, kaa_access_point_t *access_point)
{
    KAA_RETURN_IF_NIL2(context, access_point, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * channel = (kaa_tcp_channel_t *)context;

    kaa_error_t ret = KAA_ERR_NONE;

    if (channel->access_point.state != AP_NOT_SET) {
        ret = kaa_tcp_channel_release_access_point(channel);
        KAA_RETURN_IF_ERR(ret);
    }
    channel->access_point.state = AP_SET;
    channel->access_point.id = access_point->id;
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
    int position = 0;
    int remaining_to_read = 4;
    if ((position + remaining_to_read) <= access_point->connection_data_len) {
        channel->access_point.public_key_length = get_uint32_t(access_point->connection_data);
        position += remaining_to_read;
    } else {
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    remaining_to_read = channel->access_point.public_key_length;
    if ((position + remaining_to_read) <= access_point->connection_data_len) {
        channel->access_point.public_key = MALLOC(channel->access_point.public_key_length);
        KAA_RETURN_IF_NIL(channel->access_point.public_key, KAA_ERR_NOMEM);
        memcpy(channel->access_point.public_key,
                access_point->connection_data + position,
                remaining_to_read);
        position += remaining_to_read;
    } else {
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    remaining_to_read = 4;
    if ((position + remaining_to_read) <= access_point->connection_data_len) {
        channel->access_point.hostname_length = get_uint32_t(access_point->connection_data + position);
        position += remaining_to_read;
    } else {
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    remaining_to_read = channel->access_point.hostname_length;
    if ((position + remaining_to_read) <= access_point->connection_data_len) {
        channel->access_point.hostname = MALLOC(channel->access_point.hostname_length);
        KAA_RETURN_IF_NIL(channel->access_point.hostname, KAA_ERR_NOMEM);
        memcpy(channel->access_point.hostname,
                access_point->connection_data + position,
                remaining_to_read);
        position += remaining_to_read;
    } else {
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    remaining_to_read = 4;
    if ((position + remaining_to_read) <= access_point->connection_data_len) {
        channel->access_point.sockaddr.port = (uint16_t)get_uint32_t(access_point->connection_data + position);
        position += remaining_to_read;
    } else {
        return KAA_ERR_INSUFFICIENT_BUFFER;
    }

    kaa_function_return_state_t r = kaa_tcp_utils_gethostbyaddr_v4(
            (void*)channel,
            kaa_tcp_channel_set_access_point_hostname_resolved,
            &channel->access_point.sockaddr.ip_addr,
            channel->access_point.hostname,
            channel->access_point.hostname_length);
    switch (r) {
        case RET_STATE_VALUE_IN_PROGRESS:
            channel->access_point.state = AP_NOT_SET;
            ret = KAA_ERR_ACCESS_POINT_RESOLVE_FAILED;
        case RET_STATE_VALUE_READY:
            channel->access_point.state = AP_RESOLVED;
            ret = kaa_tcp_channel_connect_access_point(channel);
            if (ret) {
                if (channel->event_callback) {
                    channel->event_callback(channel->event_context, SOCKET_CONNECTION_ERROR, channel->access_point.socket_descriptor);
                }
            }
            break;
        case RET_STATE_VALUE_IN_PROGRESS:
            break;
    }

    return ret;
}

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
            if (tcp_channel->access_point.state == AP_CONNECTING) {
                //Wait socket connect event.
                *fd_p = tcp_channel->access_point.socket_descriptor;
            } else  if (tcp_channel->access_point.state == AP_CONNECTED) {
                //If there are some pending sync services put W into fd_set
                if ((tcp_channel->pending_request_service_count > 0)
                        && tcp_channel->channel_state == KAA_TCP_CHANNEL_AUTHORIZED){
                    *fd_p = tcp_channel->access_point.socket_descriptor;
                    break;
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
            if (tcp_channel->access_point.socket_descriptor > KAA_TCP_SOCKET_NOT_SET) {
                *fd_p = tcp_channel->access_point.socket_descriptor;
            }
            break;
    }

    return ret;
}

kaa_error_t kaa_tcp_channel_flush_pending_services(kaa_tcp_channel_t * channel)
{
    KAA_RETURN_IF_NIL(channel, KAA_ERR_BADPARAM);

    kaa_error_t ret = KAA_ERR_NONE;
    //TODO create flush pending service

    return ret;
}

kaa_error_t kaa_tcp_channel_socket_io_error(kaa_tcp_channel_t * channel)
{
    KAA_RETURN_IF_NIL(channel, KAA_ERR_BADPARAM);

    kaa_error_t ret = KAA_ERR_NONE;

    if (channel->access_point.sockaddr.ip_addr > 0) {
        channel->access_point.state = AP_SET;
    } else {
        channel->access_point.state = AP_NOT_SET;
    }

    channel->channel_state = KAA_TCP_CHANNEL_UNDEFINED;

    ret = kaa_tcp_utils_v4_tcp_socket_close(channel->access_point.socket_descriptor);

    channel->access_point.socket_descriptor = KAA_TCP_SOCKET_NOT_SET;

    ret = kaa_buffer_reset(channel->in_buffer);
    ret = kaa_buffer_reset(channel->out_buffer);

    if (channel->event_callback) {
        channel->event_callback(channel->event_context, SOCKET_DISCONNECTED,channel->access_point.socket_descriptor);
    }

    return ret;
}

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
            if (tcp_channel->access_point.state == AP_CONNECTED) {
                char * buf = NULL;
                size_t buf_size = 0;
                size_t bytes_read = 0;
                ret = kaa_buffer_get_unprocessed_space(tcp_channel->in_buffer, &buf, &buf_size);
                KAA_RETURN_IF_ERR(ret);
                if (buf_size > 0) {
                    kaa_tcp_socket_io_errors_t ioe = kaa_tcp_utils_v4_tcp_socket_read(fd_p, buf, buf_size, &bytes_read);
                    switch (ioe) {
                        case KAA_TCP_SOCK_IO_OK:

                            //TODO push bytes into parser
                            ret = kaa_buffer_free_allicated_space(tcp_channel->out_buffer, bytes_read);
                            break;
                        default:
                            ret = kaa_tcp_channel_socket_io_error(tcp_channel);
                            break;
                    }
                }
            }
            break;
        case FD_WRITE:
            if (tcp_channel->access_point.state == AP_CONNECTING) {
                kaa_tcp_socket_state_t s = kaa_tcp_utils_v4_tcp_socket_check(fd_p);
                switch (s) {
                    case KAA_TCP_SOCK_ERROR:
                        tcp_channel->access_point.state = AP_RESOLVED;
                        if (tcp_channel->event_callback) {
                            tcp_channel->event_callback(tcp_channel->event_context, SOCKET_CONNECTION_ERROR, fd_p);
                        }
                        break;
                    case KAA_TCP_SOCK_CONNECTED:
                        tcp_channel->access_point.state = AP_CONNECTED;
                        if (tcp_channel->event_callback) {
                            tcp_channel->event_callback(tcp_channel->event_context, SOCKET_CONNECTED, fd_p);
                        }
                        break;
                    case KAA_TCP_SOCK_CONNECTING:

                        break;
                }
            } else if (tcp_channel->access_point.state == AP_CONNECTED) {

                if (tcp_channel->pending_request_service_count > 0) {
                    ret = kaa_tcp_channel_flush_pending_services(tcp_channel);

                }

                char * buf = NULL;
                size_t buf_size = 0;
                size_t bytes_written = 0;
                ret = kaa_buffer_get_unprocessed_space(tcp_channel->out_buffer, &buf, &buf_size);
                KAA_RETURN_IF_ERR(ret);
                if (buf_size > 0) {
                    kaa_tcp_socket_io_errors_t ioe = kaa_tcp_utils_v4_tcp_socket_write(fd_p, buf, buf_size, &bytes_written);
                    switch (ioe) {
                        case KAA_TCP_SOCK_IO_OK:
                            ret = kaa_buffer_free_allicated_space(tcp_channel->out_buffer, bytes_written);
                            break;
                        default:
                            ret = kaa_tcp_channel_socket_io_error(tcp_channel);
                            break;
                    }
                }
            }
            break;
        case FD_EXEPTION:

            break;
    }

    return ret;
}

kaa_error_t kaa_tcp_channel_set_socket_events_callback(kaa_transport_channel_interface_t * channel, kaa_tcp_channel_event_fn callback, void * context)
{
    KAA_RETURN_IF_NIL(channel, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL(channel->context,KAA_ERR_BADPARAM);
    kaa_tcp_channel_t * tcp_channel = (kaa_tcp_channel_t *)channel->context;

    kaa_error_t ret = KAA_ERR_NONE;

    tcp_channel->event_callback = callback;
    tcp_channel->event_context = context;

    return ret;

}

uint32_t get_uint32_t(const char * buffer)
{
    uint32_t value = ((uint32_t)buffer[0] << 24)
            + ((uint32_t)buffer[1] << 16)
            + ((uint32_t)buffer[2] << 8)
            +(uint32_t)buffer[3];

    return NTOHL(value);
}

kaa_error_t kaa_tcp_channel_connect_access_point(kaa_tcp_channel_t * channel)
{
    KAA_RETURN_IF_NIL(channel,KAA_ERR_BADPARAM);
    if (channel->access_point.state != AP_RESOLVED) {
        return KAA_ERR_BAD_STATE;
    }
    kaa_error_t ret = kaa_tcp_utils_open_v4_tcp_socket(&channel->access_point.socket_descriptor, &channel->access_point.sockaddr);
    KAA_RETURN_IF_ERR(ret);
    channel->access_point.state = AP_CONNECTING;
    return ret;
}

kaa_error_t kaa_tcp_channel_release_access_point(kaa_tcp_channel_t * channel)
{
    KAA_RETURN_IF_NIL(channel,KAA_ERR_BADPARAM);
    kaa_error_t ret = KAA_ERR_NONE;

    if (channel->access_point.state == AP_CONNECTED
            || channel->access_point.state == AP_CONNECTING) {
        kaa_tcp_utils_v4_tcp_socket_close(channel->access_point.socket_descriptor);
    }

    channel->access_point.socket_descriptor = KAA_TCP_SOCKET_NOT_SET;
    channel->access_point.state = AP_NOT_SET;
    channel->access_point.id = 0;
    channel->access_point.sockaddr.ip_addr = 0;
    channel->access_point.sockaddr.port = 0;
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

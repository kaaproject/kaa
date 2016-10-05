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

#include "kaa_private.h"

#include <stdbool.h>
#include <stdint.h>
#include <stddef.h>
#include <string.h>
#include <time.h>
#include <stdarg.h>

#include <platform/stdio.h>

#include "kaa_common.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_buffer.h"
#include "utilities/kaa_log.h"
#include "kaa_protocols/kaa_tcp/kaatcp.h"
#include "platform/ext_system_logger.h"
#include <platform/time.h>
#include "kaa_platform_common.h"
#include "platform/ext_encryption_utils.h"
#include "kaa_tcp_channel.h"
#include "kaa_context.h"
#include "kaa_channel_manager.h"


#define KAA_TCP_CHANNEL_TRANSPORT_PROTOCOL_ID         0x56c8ff92
#define KAA_TCP_CHANNEL_TRANSPORT_PROTOCOL_VERSION    1




typedef enum {
    AP_UNDEFINED = 0,
    AP_NOT_SET,
    AP_SET,
    AP_IN_PROGRESS,
    AP_RESOLVED,
    AP_CONNECTING,
    AP_CONNECTED,
    AP_DISCONNECTED
} access_point_state_t;

typedef enum {
    KAA_TCP_CHANNEL_UNDEFINED = 0,
    KAA_TCP_CHANNEL_AUTHORIZING,
    KAA_TCP_CHANNEL_AUTHORIZED,
    KAA_TCP_CHANNEL_DISCONNECTING
} kaa_tcp_channel_state_t;

typedef enum {
    KAA_TCP_CHANNEL_SYNC_OP_UNDEFINED = 0,
    KAA_TCP_CHANNEL_SYNC_OP_STARTED,
    KAA_TCP_CHANNEL_SYNC_OP_FINISHED
} kaa_tcp_channel_sync_state_t;

typedef struct {
    access_point_state_t    state;
    uint32_t                id;
    uint8_t                 *public_key;
    uint32_t                public_key_length;
    char                    *hostname;
    uint32_t                hostname_length;
    uint16_t                port;
    kaa_sockaddr_storage_t  sockaddr;
    kaa_socklen_t           sockaddr_length;
    kaa_fd_t                socket_descriptor;
} kaa_tcp_access_point_t;

typedef struct {
    kaa_time_t last_sent_keepalive;
    kaa_time_t last_receive_keepalive;
} kaa_tcp_keepalive_t ;

typedef struct {
    uint8_t *aes_session_key;
    size_t   aes_session_key_size;
    uint8_t *signature;
    size_t   signature_size;
} kaa_tcp_encrypt_t;

typedef struct {
    kaa_logger_t                   *logger;
    kaa_tcp_channel_state_t        channel_state;
    kaa_tcp_channel_sync_state_t   sync_state;
    kaa_server_type_t              channel_operation_type;
    kaa_transport_protocol_id_t    protocol_id;
    kaa_transport_context_t        transport_context;
    on_kaa_tcp_channel_event_fn    event_callback;
    void                           *event_context;
    kaa_tcp_access_point_t         access_point;
    kaa_extension_id               *pending_request_services;
    size_t                         pending_request_service_count;
    kaa_extension_id               *supported_services;
    size_t                         supported_service_count;
    kaa_buffer_t                   *in_buffer;
    kaa_buffer_t                   *out_buffer;
    kaatcp_parser_t                *parser;
    uint16_t                       message_id;
    kaa_tcp_keepalive_t            keepalive;
    kaa_tcp_encrypt_t              encryption;
} kaa_tcp_channel_t;

static kaa_error_t kaa_tcp_channel_get_transport_protocol_info(void *context, kaa_transport_protocol_id_t *protocol_info);
static kaa_error_t kaa_tcp_channel_get_supported_services(void *context, const kaa_extension_id **supported_services, size_t *service_count);
static kaa_error_t kaa_tcp_channel_sync_handler(void *context, const kaa_extension_id services[], size_t service_count);
static kaa_error_t kaa_tcp_channel_destroy_context(void *context);
static kaa_error_t kaa_tcp_channel_init(void *context, kaa_transport_context_t *transport_context);
static kaa_error_t kaa_tcp_channel_set_access_point(void *context, kaa_access_point_t *access_point);

/*
 * Parser handlers
 */
static void kaa_tcp_channel_connack_message_callback(void *context, kaatcp_connack_t message);
static void kaa_tcp_channel_disconnect_message_callback(void *context, kaatcp_disconnect_t message);
static void kaa_tcp_channel_kaasync_message_callback(void *context, kaatcp_kaasync_t *message);
static void kaa_tcp_channel_pingresp_message_callback(void *context);

/*
 * Internal functions
 */
static kaa_error_t kaa_tcp_channel_socket_io_error(kaa_tcp_channel_t *self, kaa_failover_reason reason_code);
static kaa_error_t kaa_tcp_channel_authorize(kaa_tcp_channel_t *self);
static bool is_service_pending(kaa_tcp_channel_t *self, const kaa_extension_id service);
static kaa_error_t kaa_tcp_channel_delete_pending_services(kaa_tcp_channel_t *self, const kaa_extension_id services[], size_t service_count);
static kaa_error_t kaa_tcp_channel_update_pending_services(kaa_tcp_channel_t *self, const kaa_extension_id services[], size_t service_count);
static kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolved(void *context, const kaa_sockaddr_t *addr, kaa_socklen_t addr_size);
static kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolve_failed(void *context);
static inline uint32_t get_uint32_t(const uint8_t *buffer);
static kaa_error_t kaa_tcp_channel_connect_access_point(kaa_tcp_channel_t *self);
static kaa_error_t kaa_tcp_channel_release_access_point(kaa_tcp_channel_t *self);
static kaa_error_t kaa_tcp_channel_write_pending_services(kaa_tcp_channel_t *self, kaa_extension_id *service, size_t services_count);
static kaa_error_t kaa_tcp_write_buffer(kaa_tcp_channel_t *self);
static kaa_error_t kaa_tcp_channel_ping(kaa_tcp_channel_t *self);
static kaa_error_t kaa_tcp_channel_disconnect_internal(kaa_tcp_channel_t *self, kaatcp_disconnect_reason_t return_code);

/*
 * Create TCP channel object
 */
kaa_error_t kaa_tcp_channel_create(kaa_transport_channel_interface_t *self,
        kaa_logger_t *logger, kaa_extension_id *supported_services,
        size_t supported_service_count)
{
    KAA_RETURN_IF_NIL4(self, logger, supported_services, supported_service_count, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(logger, KAA_ERR_NONE, "Kaa TCP channel creating....");

    kaa_error_t error_code = KAA_ERR_NONE;

    //Check supported services, as Bootstrap channel we accept only one service which is bootstrap.
    //From other hand bootstrap can't be as service in operations service.
    bool bootstrap_found = false;

    for (size_t i = 0; i < supported_service_count; i++) {
        if (supported_services[i] == KAA_EXTENSION_BOOTSTRAP) {
            bootstrap_found = true;
            break;
        }
    }

    if (bootstrap_found && supported_service_count > 1) {
        //unsupported configuration
        KAA_LOG_ERROR(logger,KAA_ERR_BADPARAM,"Kaa TCP channel creating, error unsupported configuration,  "
                "supports: one Bootstrap service or all other in any combination");
        KAA_RETURN_IF_ERR(KAA_ERR_BADPARAM);
    }

    kaa_tcp_channel_t *kaa_tcp_channel = (kaa_tcp_channel_t *) KAA_CALLOC(1, sizeof(kaa_tcp_channel_t));
    KAA_RETURN_IF_NIL(kaa_tcp_channel, KAA_ERR_NOMEM);

    kaa_tcp_channel->logger = logger;

    kaa_tcp_channel->channel_state = KAA_TCP_CHANNEL_UNDEFINED;
    kaa_tcp_channel->access_point.state = AP_NOT_SET;
    kaa_tcp_channel->access_point.socket_descriptor = KAA_TCP_SOCKET_NOT_SET;

    /*
     * Copies supported services.
     */
    kaa_tcp_channel->supported_services = KAA_MALLOC(supported_service_count * sizeof(kaa_extension_id));
    if (!kaa_tcp_channel->supported_services) {
        KAA_LOG_ERROR(logger, KAA_ERR_NOMEM, "Failed to copy supported services");
        kaa_tcp_channel_destroy_context(kaa_tcp_channel);
        return KAA_ERR_NOMEM;
    }

    kaa_tcp_channel->supported_service_count = supported_service_count;

    memcpy(kaa_tcp_channel->supported_services, supported_services, sizeof(kaa_extension_id) * supported_service_count);

    /*
     * Define type of channel (bootstrap or operations)
     */
    kaa_tcp_channel->channel_operation_type = KAA_SERVER_OPERATIONS;
    if (bootstrap_found) {
        kaa_tcp_channel->channel_operation_type = KAA_SERVER_BOOTSTRAP;
    }

    /*
     * Creates read/write buffers.
     */
    error_code = kaa_buffer_create_buffer(&kaa_tcp_channel->in_buffer,
            KAA_TCP_CHANNEL_IN_BUFFER_SIZE);
    if (error_code) {
        KAA_LOG_ERROR(logger, error_code, "Failed to create IN buffer for channel");
        kaa_tcp_channel_destroy_context(kaa_tcp_channel);
        return error_code;
    }
    error_code = kaa_buffer_create_buffer(&kaa_tcp_channel->out_buffer,
            KAA_TCP_CHANNEL_OUT_BUFFER_SIZE);
    if (error_code) {
        KAA_LOG_ERROR(logger, error_code, "Failed to create OUT buffer for channel");
        kaa_tcp_channel_destroy_context(kaa_tcp_channel);
        return error_code;
    }

    /*
     * Initializes keepalive configuration.
     */
    kaa_tcp_channel->keepalive.last_sent_keepalive = KAA_TIME();
    kaa_tcp_channel->keepalive.last_receive_keepalive = kaa_tcp_channel->keepalive.last_sent_keepalive;

    KAA_LOG_TRACE(logger, KAA_ERR_NONE, "Kaa TCP channel keepalive is %u",
            KAA_TCP_CHANNEL_MAX_TIMEOUT);

    /*
     * Assigns supported transport protocol id.
     */
    kaa_tcp_channel->protocol_id.id = KAA_TCP_CHANNEL_TRANSPORT_PROTOCOL_ID;
    kaa_tcp_channel->protocol_id.version = KAA_TCP_CHANNEL_TRANSPORT_PROTOCOL_VERSION;

    KAA_LOG_TRACE(logger, KAA_ERR_NONE, "Kaa TCP channel (protocol: id=0x%08X, version=%u)",
            kaa_tcp_channel->protocol_id.id, kaa_tcp_channel->protocol_id.version);

    /*
     * Creates Kaa TCP parser.
     */
    kaa_tcp_channel->parser = KAA_MALLOC(sizeof(kaatcp_parser_t));

    if (kaa_tcp_channel->parser) {
        kaa_tcp_channel->parser->payload_buffer_size = KAATCP_PARSER_MAX_MESSAGE_LENGTH;
        kaa_tcp_channel->parser->payload = KAA_MALLOC(kaa_tcp_channel->parser->payload_buffer_size);
    }

    if (!kaa_tcp_channel->parser || !kaa_tcp_channel->parser->payload) {
        KAA_LOG_ERROR(logger, KAA_ERR_NOMEM, "Failed to create Kaa TCP parser");
        kaa_tcp_channel_destroy_context(kaa_tcp_channel);
        return KAA_ERR_NOMEM;
    }

    kaatcp_parser_handlers_t parser_handler;

    parser_handler.connack_handler    = kaa_tcp_channel_connack_message_callback;
    parser_handler.disconnect_handler = kaa_tcp_channel_disconnect_message_callback;
    parser_handler.kaasync_handler    = kaa_tcp_channel_kaasync_message_callback;
    parser_handler.pingresp_handler   = kaa_tcp_channel_pingresp_message_callback;
    parser_handler.handlers_context   = kaa_tcp_channel;

    kaatcp_error_t parser_error_code = kaatcp_parser_init(kaa_tcp_channel->parser, &parser_handler);
    if (parser_error_code) {
        KAA_LOG_ERROR(logger, KAA_ERR_TCPCHANNEL_PARSER_INIT_FAILED, "Failed to initialize Kaa TCP parser (error_code %d)", parser_error_code);
        kaa_tcp_channel_destroy_context(kaa_tcp_channel);
        return KAA_ERR_TCPCHANNEL_PARSER_INIT_FAILED;
    }

    /*
     * Initializes a transport channel interface.
     */
    self->context = kaa_tcp_channel;
    self->get_protocol_id = kaa_tcp_channel_get_transport_protocol_info;
    self->get_supported_services = kaa_tcp_channel_get_supported_services;
    self->destroy = kaa_tcp_channel_destroy_context;
    self->sync_handler = kaa_tcp_channel_sync_handler;
    self->init = kaa_tcp_channel_init;
    self->set_access_point = kaa_tcp_channel_set_access_point;

    KAA_LOG_TRACE(logger, KAA_ERR_NONE, "Kaa TCP channel created");

    return error_code;
}



static kaa_error_t kaa_tcp_channel_on_access_point_failed(kaa_tcp_channel_t *self, kaa_failover_reason reason_code)
{
    kaa_error_t error_code = kaa_bootstrap_manager_on_access_point_failed(self->transport_context.kaa_context->bootstrap_manager,
            &self->protocol_id, self->channel_operation_type, reason_code);

    if (error_code != KAA_ERR_EVENT_NOT_ATTACHED) {
        KAA_LOG_ERROR(self->logger, error_code, "Kaa TCP channel [0x%08X] "
                "error notifying bootstrap manager on access point failure",
                self->access_point.id);
    }
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
kaa_error_t kaa_tcp_channel_get_supported_services(void *context, const kaa_extension_id **supported_services, size_t *service_count)
{
    KAA_RETURN_IF_NIL3(context, supported_services, service_count, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;

    *supported_services = channel->supported_services;
    *service_count = channel->supported_service_count;

    return KAA_ERR_NONE;
}



/*
 * Sync specified services list with server side.
 */
kaa_error_t kaa_tcp_channel_sync_handler(void *context, const kaa_extension_id services[], size_t service_count)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    kaa_error_t error_code = KAA_ERR_NONE;

    KAA_LOG_TRACE(((kaa_tcp_channel_t *) context)->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] sync for %zu services",
            ((kaa_tcp_channel_t *) context)->access_point.id, service_count);

    if (services && service_count > 0) {
        error_code = kaa_tcp_channel_update_pending_services((kaa_tcp_channel_t *) context, services, service_count);
        KAA_RETURN_IF_ERR(error_code);
    }

    //If channel pending sync only bootstrap, at sync it should initiate new connection if access point resolved
    if (((kaa_tcp_channel_t *) context)->access_point.state == AP_RESOLVED
            && ((kaa_tcp_channel_t *) context)->pending_request_service_count > 0
            && ((kaa_tcp_channel_t *) context)->channel_state == KAA_TCP_CHANNEL_UNDEFINED) {
        KAA_LOG_INFO(((kaa_tcp_channel_t *) context)->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] connection down but access point resolved, trying to connect....",
                ((kaa_tcp_channel_t *) context)->access_point.id);
        error_code = kaa_tcp_channel_connect_access_point(((kaa_tcp_channel_t *) context));
        if (error_code) {
            KAA_LOG_ERROR(((kaa_tcp_channel_t *) context)->logger, error_code, "Kaa TCP channel [0x%08X] failed to connect",
                    ((kaa_tcp_channel_t *) context)->access_point.id);
            if (((kaa_tcp_channel_t *) context)->event_callback)
                ((kaa_tcp_channel_t *) context)->event_callback(((kaa_tcp_channel_t *) context)->event_context,
                        SOCKET_CONNECTION_ERROR,((kaa_tcp_channel_t *) context)->access_point.socket_descriptor);

            error_code = kaa_tcp_channel_on_access_point_failed(context, KAA_CHANNEL_NA);
        }
    }

    return error_code;
}



/*
 * Release Kaa TCP channel context
 */
kaa_error_t kaa_tcp_channel_destroy_context(void *context)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);

    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;
    kaa_error_t error_code = KAA_ERR_NONE;

    if (channel->parser) {
        KAA_FREE(channel->parser->payload);
        KAA_FREE(channel->parser);
        channel->parser = NULL;
    }

    kaa_tcp_channel_release_access_point(channel);

    kaa_buffer_destroy(channel->in_buffer);
    kaa_buffer_destroy(channel->out_buffer);

    KAA_FREE(channel->pending_request_services);
    channel->pending_request_services = NULL;

    KAA_FREE(channel->supported_services);
    channel->supported_services = NULL;
    channel->supported_service_count = 0;

    if (channel->encryption.aes_session_key) {
        channel->encryption.aes_session_key = NULL;
        channel->encryption.aes_session_key_size = 0;
    }

    if (channel->encryption.signature) {
        channel->encryption.signature = NULL;
        channel->encryption.signature_size = 0;
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
    KAA_RETURN_IF_NIL3(context, transport_context, transport_context->kaa_context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;
    channel->transport_context = *transport_context;
    return KAA_ERR_NONE;
}

/*
 * Set access point to Kaa TCP channel.
 *
 * The purpose of this function is parsing of
 * the raw connection data buffer and filling in
 * the appropriate structure inside of kaa_tcp_channel.
 */
kaa_error_t kaa_tcp_channel_set_access_point(void *context, kaa_access_point_t *access_point)
{
    if (!context || !access_point) {
        return KAA_ERR_BADPARAM;
    }

    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;

    kaa_error_t error = KAA_ERR_NONE;

    KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE, "Kaa TCP channel setting access point...");
    if (channel->access_point.state != AP_NOT_SET) {
        KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE, "Kaa TCP channel removing previous access point [0x%08X] ", channel->access_point.id);
        error = kaa_tcp_channel_release_access_point(channel);
        if (error) {
            return error;
        }
    }

    channel->access_point.state = AP_SET;
    channel->access_point.id = access_point->id;

    channel->access_point.public_key = NULL;
    channel->access_point.hostname = NULL;

    KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE, "Kaa TCP channel new access point [0x%08X], connection data length %u",
            channel->access_point.id, access_point->connection_data_len);

    uint8_t *connection_data = (uint8_t *)access_point->connection_data;
    size_t connection_data_len = access_point->connection_data_len;
    size_t position = 0;

    /* Size of remote public key */
    size_t remaining_to_read = sizeof(uint32_t);

    /* Read remote key length */
    if ((position + remaining_to_read) > connection_data_len) {
        error = KAA_ERR_INSUFFICIENT_BUFFER;
        goto cleanup;
    }
    channel->access_point.public_key_length = get_uint32_t(connection_data);
    position += remaining_to_read;

    /* Read remote public key */
    remaining_to_read = channel->access_point.public_key_length;
    if ((position + remaining_to_read) > connection_data_len) {
        error = KAA_ERR_INSUFFICIENT_BUFFER;
        goto cleanup;
    }
    channel->access_point.public_key = KAA_MALLOC(channel->access_point.public_key_length);
    if (!channel->access_point.public_key) {
        error = KAA_ERR_NOMEM;
        goto cleanup;
    }
    memcpy(channel->access_point.public_key, connection_data + position, remaining_to_read);
    position += remaining_to_read;

    /* Read hostname length */
    remaining_to_read = sizeof(uint32_t);
    if ((position + remaining_to_read) > connection_data_len) {
        error = KAA_ERR_INSUFFICIENT_BUFFER;
        goto cleanup;
    }
    channel->access_point.hostname_length = get_uint32_t(connection_data + position);
    position += remaining_to_read;

    /* Read hostname */
    remaining_to_read = channel->access_point.hostname_length;
    if ((position + remaining_to_read) > connection_data_len) {
        error = KAA_ERR_INSUFFICIENT_BUFFER;
        goto cleanup;
    }
    channel->access_point.hostname = KAA_MALLOC(channel->access_point.hostname_length);
    if (!channel->access_point.hostname) {
        error = KAA_ERR_NOMEM;
        goto cleanup;
    }

    memcpy(channel->access_point.hostname, connection_data + position, remaining_to_read);
    position += remaining_to_read;

    /* Read access point's port */
    remaining_to_read = sizeof(uint32_t);
    int access_point_socket_port = 0;
    if ((position + remaining_to_read) > connection_data_len) {
        error = KAA_ERR_INSUFFICIENT_BUFFER;
        goto cleanup;
    }
    access_point_socket_port = (uint16_t) get_uint32_t(connection_data + position);
    position += remaining_to_read;
    channel->access_point.port = (uint16_t) access_point_socket_port;

    KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE, "Kaa TCP channel new access point [0x%08X] destination %.*s:%d",
            channel->access_point.id, channel->access_point.hostname_length,
            channel->access_point.hostname, access_point_socket_port);

#ifdef KAA_ENCRYPTION
    /* Setting session key */
    error = ext_get_encrypted_session_key(&channel->encryption.aes_session_key,
            &channel->encryption.aes_session_key_size,
            channel->access_point.public_key,
            channel->access_point.public_key_length);

    if (error) {
        KAA_LOG_ERROR(channel->logger, error, "Can't get session key");
        goto cleanup;
    }

    /* In case if  channel */
    if (*channel->supported_services != KAA_EXTENSION_BOOTSTRAP) {
        error = ext_get_signature(channel->encryption.aes_session_key, channel->encryption.aes_session_key_size,
                &channel->encryption.signature, &channel->encryption.signature_size);
        if (error) {
            KAA_LOG_ERROR(channel->logger, error, "Can't get signature");
            goto cleanup;
        }
    }
#endif
    return error;

cleanup:
    KAA_FREE(channel->access_point.public_key);
    KAA_FREE(channel->access_point.hostname);
    return error;
}



kaa_error_t kaa_tcp_channel_get_descriptor(kaa_transport_channel_interface_t *self,
        kaa_fd_t *fd_p)
{
    KAA_RETURN_IF_NIL3(self, fd_p, self->context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *tcp_channel = (kaa_tcp_channel_t *) self->context;

    *fd_p = tcp_channel->access_point.socket_descriptor;

    return KAA_ERR_NONE;
}



bool kaa_tcp_channel_is_ready(kaa_transport_channel_interface_t *self,
        fd_event_t event_type)
{
    KAA_RETURN_IF_NIL2(self, self->context, false);
    kaa_tcp_channel_t *tcp_channel = (kaa_tcp_channel_t *) self->context;

    kaa_error_t error_code = KAA_ERR_NONE;

    switch (event_type) {
        case FD_READ:
            KAA_LOG_TRACE_LDB(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] checking socket for event READ, AP=%d, CH=%d"
                    , tcp_channel->access_point.id
                    , tcp_channel->access_point.state
                    , tcp_channel->channel_state);
            if (tcp_channel->access_point.state == AP_CONNECTED) {
                char *buf = NULL;
                size_t buf_size = 0;
                error_code = kaa_buffer_allocate_space(tcp_channel->in_buffer, &buf, &buf_size);
                KAA_RETURN_IF_ERR(error_code);
                if (buf_size > 0) {
                    return true;
                }
            }
            break;
        case FD_WRITE:
            KAA_LOG_TRACE_LDB(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] checking socket for event WRITE"
                    , tcp_channel->access_point.id);
            if (tcp_channel->access_point.state == AP_CONNECTING) {
                return true;
            } else  if (tcp_channel->access_point.state == AP_CONNECTED) {
                //If there are some pending sync services put W into fd_set
                if (tcp_channel->pending_request_service_count > 0) {
                    if (is_service_pending(tcp_channel, KAA_EXTENSION_BOOTSTRAP)
                            || tcp_channel->channel_state == KAA_TCP_CHANNEL_AUTHORIZED) {
                        return true;
                    }
                }
                //If out buffer have some bytes to transmit
                char *buf = NULL;
                size_t buf_size = 0;
                error_code = kaa_buffer_get_unprocessed_space(tcp_channel->out_buffer, &buf, &buf_size);
                KAA_RETURN_IF_ERR(error_code);
                if (buf_size > 0) {
                    return true;
                }
            }
            break;
        case FD_EXCEPTION:
            KAA_LOG_TRACE_LDB(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] checking socket for event EXCEPTION"
                    , tcp_channel->access_point.id);
            if (tcp_channel->access_point.socket_descriptor > KAA_TCP_SOCKET_NOT_SET) {
                return true;
            }
            break;
    }

    return false;
}

kaa_error_t kaa_tcp_channel_process_event(kaa_transport_channel_interface_t *self,
        fd_event_t event_type)
{
    KAA_RETURN_IF_NIL2(self, self->context, KAA_ERR_BADPARAM);

    kaa_tcp_channel_t *tcp_channel = (kaa_tcp_channel_t *) self->context;

    kaa_error_t error_code = kaa_tcp_channel_check_keepalive(self);
    KAA_RETURN_IF_ERR(error_code);

    kaa_fd_t fd = tcp_channel->access_point.socket_descriptor;

    switch (event_type) {
        case FD_READ:
            KAA_LOG_TRACE_LDB(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] processing event READ"
                    , tcp_channel->access_point.id);
            if (tcp_channel->access_point.state == AP_CONNECTED) {
                char *buf = NULL;
                size_t buf_size = 0;
                size_t bytes_read = 0;
                error_code = kaa_buffer_allocate_space(tcp_channel->in_buffer, &buf, &buf_size);
                if (error_code) {
                    KAA_LOG_ERROR(tcp_channel->logger, error_code, "Kaa TCP channel [0x%08X] error allocate space %zu"
                            , tcp_channel->access_point.id, buf_size);
                }
                KAA_RETURN_IF_ERR(error_code);
                if (buf_size > 0) {

                    ext_tcp_socket_io_errors_t io_error = ext_tcp_utils_tcp_socket_read(fd, buf, buf_size, &bytes_read);

                    switch (io_error) {
                        case KAA_TCP_SOCK_IO_OK:
                            KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] successfully read %zu bytes"
                                    , tcp_channel->access_point.id, bytes_read);
                            error_code = kaa_buffer_lock_space(tcp_channel->in_buffer, bytes_read);
                            if (error_code) {
                                KAA_LOG_ERROR(tcp_channel->logger, error_code, "Kaa TCP channel [0x%08X] error lock %zu bytes in buffer"
                                        , tcp_channel->access_point.id, bytes_read);
                            }
                            KAA_RETURN_IF_ERR(error_code);

                            error_code = kaa_buffer_get_unprocessed_space(tcp_channel->in_buffer, &buf, &buf_size);
                            if (error_code) {
                                KAA_LOG_ERROR(tcp_channel->logger, error_code, "Kaa TCP channel [0x%08X] error get unprocessed %zu bytes"
                                        , tcp_channel->access_point.id, bytes_read);
                            }
                            KAA_RETURN_IF_ERR(error_code);
                            //TODO Modify parser errors code
                            kaatcp_error_t kaatcp_error_code = kaatcp_parser_process_buffer(tcp_channel->parser, buf, buf_size);
                            if (kaatcp_error_code) {
                                error_code = KAA_ERR_TCPCHANNEL_PARSER_ERROR;
                                KAA_LOG_ERROR(tcp_channel->logger, error_code, "Kaa TCP channel [0x%08X] failed to parse the buffer (kaatcp_error_code=%d)"
                                        , tcp_channel->access_point.id, kaatcp_error_code);
                                kaa_tcp_channel_socket_io_error(tcp_channel, KAA_CHANNEL_NA);
                            } else {
                                //Need to check AP state to avoid free space on closed connection.
                                if (tcp_channel->access_point.state == AP_CONNECTED) {
                                    error_code = kaa_buffer_free_allocated_space(tcp_channel->in_buffer, buf_size);
                                    if (error_code) {
                                        KAA_LOG_ERROR(tcp_channel->logger, error_code, "Kaa TCP channel [0x%08X] error free allocated buffer %zu bytes"
                                                , tcp_channel->access_point.id, buf_size);
                                    }
                                }
                            }
                            break;
                        default:
                            error_code = kaa_tcp_channel_socket_io_error(tcp_channel, KAA_CHANNEL_NA);
                            break;
                    }
                }
            }
            break;
        case FD_WRITE:
            KAA_LOG_TRACE_LDB(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] processing event WRITE"
                    , tcp_channel->access_point.id);
            if (tcp_channel->access_point.state == AP_CONNECTING) {
                ext_tcp_socket_state_t socket_state =
                    ext_tcp_utils_tcp_socket_check(fd
                            , (kaa_sockaddr_t *) &tcp_channel->access_point.sockaddr
                            , tcp_channel->access_point.sockaddr_length);

                switch (socket_state) {
                    case KAA_TCP_SOCK_ERROR:
                        KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] connection failed"
                                , tcp_channel->access_point.id);
                        tcp_channel->access_point.state = AP_RESOLVED;
                        if (tcp_channel->event_callback) {
                            tcp_channel->event_callback(tcp_channel->event_context, SOCKET_CONNECTION_ERROR, fd);
                        }
                        error_code = kaa_tcp_channel_socket_io_error(tcp_channel, KAA_CHANNEL_NA);
                        break;
                    case KAA_TCP_SOCK_CONNECTED:
                        KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] socket was successfully connected"
                                , tcp_channel->access_point.id);
                        tcp_channel->access_point.state = AP_CONNECTED;

                        if (tcp_channel->event_callback) {
                            tcp_channel->event_callback(tcp_channel->event_context, SOCKET_CONNECTED, fd);
                        }

                        error_code = kaa_tcp_channel_authorize(tcp_channel);
                        break;
                    case KAA_TCP_SOCK_CONNECTING:
                        KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] socket is still connecting..."
                                , tcp_channel->access_point.id);
                        break;
                }
            } else if (tcp_channel->access_point.state == AP_CONNECTED) {
                error_code = kaa_tcp_write_buffer(tcp_channel);
                KAA_RETURN_IF_ERR(error_code);

                if (tcp_channel->channel_state == KAA_TCP_CHANNEL_DISCONNECTING) {
                    KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] disconnecting..."
                            , tcp_channel->access_point.id);
                    //Check if buffer is empty, close socket.
                    char *buf = NULL;
                    size_t buf_size = 0;
                    error_code = kaa_buffer_get_unprocessed_space(tcp_channel->out_buffer, &buf, &buf_size);
                    if (error_code || !buf_size) {
                        error_code = kaa_tcp_channel_socket_io_error(tcp_channel, KAA_CHANNEL_NA);
                    } else {
                        KAA_LOG_TRACE(tcp_channel->logger, error_code, "Kaa TCP channel [0x%08X] can't disconnect right now (%d bytes are unprocessed)"
                                , tcp_channel->access_point.id, buf_size);
                    }
                } else if (tcp_channel->pending_request_service_count > 0) {
                    if (tcp_channel->channel_operation_type == KAA_SERVER_BOOTSTRAP) {
                        KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] going to sync Bootstrap service"
                                , tcp_channel->access_point.id);
                        error_code = kaa_tcp_channel_authorize(tcp_channel);
                    } else if (tcp_channel->channel_state == KAA_TCP_CHANNEL_AUTHORIZED) {
                        KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] going to sync all services"
                                , tcp_channel->access_point.id);
                        error_code = kaa_tcp_channel_write_pending_services(tcp_channel
                                , tcp_channel->pending_request_services
                                , tcp_channel->pending_request_service_count);
                    } else {
                        KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] authorizing channel"
                                , tcp_channel->access_point.id);
                        error_code = kaa_tcp_channel_authorize(tcp_channel);
                    }
                } else {
                    KAA_LOG_TRACE_LDB(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] there is no pending services to sync"
                            , tcp_channel->access_point.id);
                }
            }
            break;
        case FD_EXCEPTION:
            KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] processing event EXCEPTION"
                    , tcp_channel->access_point.id);
            error_code = kaa_tcp_channel_socket_io_error(tcp_channel, KAA_CHANNEL_NA);
            break;
    }
    KAA_LOG_TRACE_LDB(tcp_channel->logger, error_code, "Kaa TCP channel [0x%08X] event processing complete"
            , tcp_channel->access_point.id);
    return error_code;
}



kaa_error_t kaa_tcp_channel_get_max_timeout(kaa_transport_channel_interface_t *self,
        uint16_t *max_timeout)
{
    KAA_RETURN_IF_NIL3(self, self->context, max_timeout, KAA_ERR_BADPARAM);

    *max_timeout = KAA_TCP_CHANNEL_PING_TIMEOUT;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_tcp_channel_check_keepalive(kaa_transport_channel_interface_t *self)
{
    KAA_RETURN_IF_NIL2(self, self->context, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;
    kaa_tcp_channel_t *tcp_channel = (kaa_tcp_channel_t *) self->context;

    if (tcp_channel->access_point.state == AP_SET) {
        kaa_dns_resolve_listener_t resolve_listener;
        resolve_listener.context = (void *) tcp_channel;
        resolve_listener.on_host_resolved = kaa_tcp_channel_set_access_point_hostname_resolved;
        resolve_listener.on_resolve_failed = kaa_tcp_channel_set_access_point_hostname_resolve_failed;

        kaa_dns_resolve_info_t resolve_props;
        resolve_props.hostname = tcp_channel->access_point.hostname;
        resolve_props.hostname_length = tcp_channel->access_point.hostname_length;
        resolve_props.port = tcp_channel->access_point.port;

        tcp_channel->access_point.sockaddr_length = sizeof(kaa_sockaddr_storage_t);
        ext_tcp_utils_function_return_state_t resolve_state =
            ext_tcp_utils_getaddrbyhost(&resolve_listener,
                    &resolve_props,
                    (kaa_sockaddr_t *) &tcp_channel->access_point.sockaddr,
                    &tcp_channel->access_point.sockaddr_length);

        switch (resolve_state) {
            case RET_STATE_VALUE_IN_PROGRESS:
                KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel new access point [0x%08X] destination name resolve pending...",
                        tcp_channel->access_point.id);
                tcp_channel->access_point.state = AP_IN_PROGRESS;
                break;
            case RET_STATE_VALUE_READY:
                tcp_channel->access_point.state = AP_RESOLVED;
                KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel new access point [0x%08X] destination resolved",
                        tcp_channel->access_point.id);
                error_code = kaa_tcp_channel_connect_access_point(tcp_channel);
                if (error_code) {
                    KAA_LOG_ERROR(tcp_channel->logger, error_code, "Kaa TCP channel new access point [0x%08X] failed to connect",
                            tcp_channel->access_point.id);
                    if (tcp_channel->event_callback)
                        tcp_channel->event_callback(tcp_channel->event_context,
                                SOCKET_CONNECTION_ERROR,
                                tcp_channel->access_point.socket_descriptor);

                    error_code = kaa_tcp_channel_on_access_point_failed(tcp_channel, KAA_CHANNEL_NA);
                }
                break;
            case RET_STATE_VALUE_ERROR:
                tcp_channel->access_point.state = AP_NOT_SET;
                error_code = KAA_ERR_TCPCHANNEL_AP_RESOLVE_FAILED;
                KAA_LOG_ERROR(tcp_channel->logger, error_code, "Kaa TCP channel new access point [0x%08X] hostname resolve failed",
                        tcp_channel->access_point.id);
                error_code = kaa_tcp_channel_on_access_point_failed(tcp_channel, KAA_CHANNEL_NA);
                break;
            case RET_STATE_BUFFER_NOT_ENOUGH:
                tcp_channel->access_point.state = AP_NOT_SET;
                error_code = KAA_ERR_TCPCHANNEL_AP_RESOLVE_FAILED;
                KAA_LOG_ERROR(tcp_channel->logger, error_code, "Kaa TCP channel new access point [0x%08X] hostname resolve failed. "
                        "Address buffer is not enough", tcp_channel->access_point.id);
                break;
        }
    } else {
        KAA_LOG_TRACE_LDB(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] checking keepalive",
                tcp_channel->access_point.id);

        if (tcp_channel->channel_state != KAA_TCP_CHANNEL_AUTHORIZED) {
            return error_code;
        }

        kaa_time_t interval = KAA_TIME() - tcp_channel->keepalive.last_sent_keepalive;

        if (interval >= (kaa_time_t)KAA_TCP_CHANNEL_PING_TIMEOUT) {
            //Send ping request

            if (tcp_channel->keepalive.last_sent_keepalive > tcp_channel->keepalive.last_receive_keepalive) {
                if (tcp_channel->channel_operation_type == KAA_SERVER_BOOTSTRAP) {
                    error_code = kaa_bootstrap_manager_on_access_point_failed(tcp_channel->transport_context.kaa_context->bootstrap_manager,
                            &tcp_channel->protocol_id, tcp_channel->channel_operation_type,
                            KAA_BOOTSTRAP_SERVERS_NA);
                } else {
                    error_code = kaa_bootstrap_manager_on_access_point_failed(tcp_channel->transport_context.kaa_context->bootstrap_manager,
                            &tcp_channel->protocol_id, tcp_channel->channel_operation_type,
                            KAA_OPERATION_SERVERS_NA);
                }

                if (error_code) {
                    return error_code;
                }

                return KAA_ERR_TIMEOUT;
            }

            error_code = kaa_tcp_channel_ping(tcp_channel);
        }
    }

    return error_code;
}



/*
 * Set socket events callbacks.
 */
kaa_error_t kaa_tcp_channel_set_socket_events_callback(kaa_transport_channel_interface_t *self,
        on_kaa_tcp_channel_event_fn callback, void *context)
{
    KAA_RETURN_IF_NIL2(self, self->context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *tcp_channel = (kaa_tcp_channel_t *) self->context;

    tcp_channel->event_callback = callback;
    tcp_channel->event_context = context;

    KAA_LOG_TRACE(tcp_channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] set socket events callbacks",
            tcp_channel->access_point.id);

    return KAA_ERR_NONE;

}

kaa_error_t kaa_tcp_channel_disconnect(kaa_transport_channel_interface_t  *self)
{
    KAA_RETURN_IF_NIL2(self, self->context, KAA_ERR_BADPARAM);
    return kaa_tcp_channel_disconnect_internal((kaa_tcp_channel_t *)self, KAATCP_DISCONNECT_NONE);
}



void kaa_tcp_channel_connack_message_callback(void *context, kaatcp_connack_t message)
{
    KAA_RETURN_IF_NIL(context,);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;

    if (channel->channel_state == KAA_TCP_CHANNEL_AUTHORIZING) {
        if (message.return_code == KAATCP_CONNACK_SUCCESS) {
            channel->channel_state = KAA_TCP_CHANNEL_AUTHORIZED;
            KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE,
                    "Kaa TCP channel [0x%08X] successfully authorized",
                    channel->access_point.id);

            channel->keepalive.last_receive_keepalive = KAA_TIME();
            channel->keepalive.last_sent_keepalive
                = channel->keepalive.last_receive_keepalive;

        } else if (message.return_code == KAATCP_CONNACK_REFUSE_BAD_CREDENTIALS) {
            kaa_context_set_status_registered(channel->transport_context.kaa_context, false);
            KAA_LOG_WARN(channel->logger, KAA_ERR_NONE,
                    "Kaa TCP channel [0x%08X] received KAATCP_CONNACK_REFUSE_BAD_CREDENTIALS",
                    channel->access_point.id);
            channel->channel_state = KAA_TCP_CHANNEL_UNDEFINED;
            kaa_channel_manager_process_auth_failure(channel->transport_context.kaa_context->channel_manager,
                    KAA_AUTH_STATUS_BAD_CREDENTIALS);
        } else if (message.return_code == KAATCP_CONNACK_REFUSE_VERIFICATION_FAILED) {
            KAA_LOG_WARN(channel->logger, KAA_ERR_NONE,
                    "Kaa TCP channel receiver KAATCP_CONNACK_REFUSE_VERIFICATION_FAILED",
                    channel->access_point.id);
            kaa_channel_manager_process_auth_failure(channel->transport_context.kaa_context->channel_manager,
                    KAA_AUTH_STATUS_VERIFICATION_FAILED);
            kaa_tcp_channel_socket_io_error(channel, KAA_ENDPOINT_NOT_REGISTERED);
        } else {
            KAA_LOG_ERROR(channel->logger, KAA_ERR_BAD_STATE,
                    "Kaa TCP channel [0x%08X] authorization failed, code %d",
                    channel->access_point.id, message.return_code);
            kaa_channel_manager_process_auth_failure(channel->transport_context.kaa_context->channel_manager,
                    KAA_AUTH_STATUS_UNKNOWN);
            kaa_tcp_channel_socket_io_error(channel, KAA_CHANNEL_NA);
        }
    } else {
        KAA_LOG_WARN(channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] CONACK message received in incorrect state",
                channel->access_point.id);
        kaa_tcp_channel_socket_io_error(channel, KAA_CHANNEL_NA);
    }
}



void kaa_tcp_channel_disconnect_message_callback(void *context, kaatcp_disconnect_t message)
{
    KAA_RETURN_IF_NIL(context,);

    KAA_LOG_TRACE(((kaa_tcp_channel_t *) context)->logger, KAA_ERR_NONE,"Kaa TCP channel [0x%08X] DISCONNECT message received",
            ((kaa_tcp_channel_t *) context)->access_point.id);
    if (message.reason == KAATCP_DISCONNECT_CREDENTIALS_REVOKED) {
        kaa_tcp_channel_socket_io_error(context, KAA_CREDENTIALS_REVOKED);
    }
    if (message.reason != KAATCP_DISCONNECT_INTERNAL_ERROR && message.reason != KAATCP_DISCONNECT_CREDENTIALS_REVOKED) {
        ((kaa_tcp_channel_t *) context)->sync_state = KAA_TCP_CHANNEL_SYNC_OP_STARTED;
    }
    kaa_tcp_channel_socket_io_error(context, KAA_CHANNEL_NA);
}



void kaa_tcp_channel_kaasync_message_callback(void *context, kaatcp_kaasync_t *message)
{
    KAA_RETURN_IF_NIL2(context, message, );
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;

    KAA_LOG_TRACE(channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] KAASYNC message received",
            channel->access_point.id);

    uint8_t zipped = message->sync_header.flags & KAA_SYNC_ZIPPED_BIT;
    uint8_t encrypted = message->sync_header.flags & KAA_SYNC_ENCRYPTED_BIT;
    (void)encrypted;

#ifdef KAA_ENCRYPTION
    if (encrypted) {
        ext_decrypt_data((uint8_t *)message->sync_request, message->sync_request_size,
                (uint8_t *)message->sync_request, &message->sync_request_size);
    }
#endif
    /* Currently not supported */
    if (!zipped) {
        kaa_error_t error_code = kaa_platform_protocol_process_server_sync(channel->transport_context.kaa_context->platform_protocol,
                (const uint8_t *)message->sync_request,
                message->sync_request_size);
        if (error_code)
            KAA_LOG_ERROR(channel->logger, error_code, "Kaa TCP channel [0x%08X] failed to process server sync",
                    channel->access_point.id);
    } else {
        KAA_LOG_WARN(channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] received unsupported flags: zipped %d, encrypted %d",
                channel->access_point.id, zipped, encrypted);
    }

    kaatcp_parser_kaasync_destroy(message);

    //Check if service supports only bootstrap, after sync it disconnects.
    if (channel->channel_operation_type == KAA_SERVER_BOOTSTRAP) {
        channel->sync_state = KAA_TCP_CHANNEL_SYNC_OP_FINISHED;
        kaa_tcp_channel_disconnect_internal(channel, KAATCP_DISCONNECT_NONE);
    }
}



void kaa_tcp_channel_pingresp_message_callback(void *context)
{
    KAA_RETURN_IF_NIL(context,);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *)context;

    channel->keepalive.last_receive_keepalive = KAA_TIME();

    KAA_LOG_INFO(channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] PING message received",
            channel->access_point.id);
}



/*
 * Close Kaa TCP channel socket and reset state of channel
 */
kaa_error_t kaa_tcp_channel_socket_io_error(kaa_tcp_channel_t *self, kaa_failover_reason reason_code)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE_LDB(self->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] closing socket",
            self->access_point.id);

    kaa_error_t error_code = KAA_ERR_NONE;

    self->access_point.state = AP_RESOLVED;
    self->channel_state = KAA_TCP_CHANNEL_UNDEFINED;


    if (self->access_point.socket_descriptor >= 0) {
        if (self->event_callback) {
            self->event_callback(self->event_context, SOCKET_DISCONNECTED, self->access_point.socket_descriptor);
        }
        error_code = ext_tcp_utils_tcp_socket_close(self->access_point.socket_descriptor);
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Kaa TCP channel [0x%08X] closing socket, "
                    "error closing socket",  self->access_point.id);
            error_code = KAA_ERR_NONE;
        }

        self->access_point.socket_descriptor = KAA_TCP_SOCKET_NOT_SET;

    }

    if (self->sync_state == KAA_TCP_CHANNEL_SYNC_OP_STARTED) {
        error_code = kaa_tcp_channel_on_access_point_failed(self, reason_code);
    }

    kaa_buffer_reset(self->in_buffer);
    kaa_buffer_reset(self->out_buffer);



    kaatcp_parser_reset(self->parser);

    self->sync_state = KAA_TCP_CHANNEL_SYNC_OP_UNDEFINED;

    return error_code;
}

#ifdef KAA_ENCRYPTION
static kaa_error_t kaa_tcp_channel_encrypt(kaa_tcp_channel_t *self, uint8_t **data, size_t *data_size)
{
    if (!data || !data_size) {
        return KAA_ERR_BADPARAM;
    }

    size_t encrypted_size = ext_get_encrypted_data_size(*data_size);

    char *enc_buff = KAA_MALLOC(encrypted_size);
    if (!enc_buff) {
        return KAA_ERR_NOMEM;
    }

    memcpy(enc_buff, *data, *data_size);
    kaa_error_t error_code = ext_encrypt_data((uint8_t *)enc_buff, *data_size, (uint8_t *)enc_buff);
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Can't encrypt the data");
        KAA_FREE(enc_buff);
        return KAA_ERR_BADDATA;
    }
    KAA_FREE(*data);

    *data = (uint8_t *)enc_buff;
    *data_size = encrypted_size;
    return KAA_ERR_NONE;
}
#endif

/*
 * Put Kaa TCP connect message to out buffer.
 */
kaa_error_t kaa_tcp_channel_authorize(kaa_tcp_channel_t *self)
{
    if (!self) {
        return KAA_ERR_BADPARAM;
    }

    char *buffer = NULL;
    size_t buffer_size = 0;
    size_t request_size = 0;

    uint8_t *sync_buffer = NULL;
    size_t sync_size = 0;

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Calling kaa_platform_protocol_alloc_serialize_client_sync");
    kaa_error_t error_code = kaa_platform_protocol_alloc_serialize_client_sync(
            self->transport_context.kaa_context->platform_protocol,
            self->supported_services,
            self->supported_service_count,
            &sync_buffer,
            &sync_size);

    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to allocate and serialize client sync");
        KAA_FREE(sync_buffer);
        return error_code;
    }

#ifdef KAA_ENCRYPTION
    /* TODO(KAA-1246): Rework the solution to cipher without additional allocations */
    error_code = kaa_tcp_channel_encrypt(self, (uint8_t **)&sync_buffer, &sync_size);
    if (error_code) {
        KAA_FREE(sync_buffer);
        return error_code;
    }
#endif

    KAA_LOG_TRACE(self->logger, error_code, "Kaa TCP channel [0x%08X] going to send CONNECT message (%zu bytes)",
            self->access_point.id, sync_size);

    kaa_error_t delete_error_code = kaa_tcp_channel_delete_pending_services(self, self->pending_request_services,
            self->pending_request_service_count);
    if (delete_error_code) {
        KAA_LOG_ERROR(self->logger, delete_error_code, "Kaa TCP channel [0x%08X] failed to delete pending services",
                self->access_point.id);
    }

    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Kaa TCP channel [0x%08X] failed to serialize supported services",
                self->access_point.id);
    }

    if (error_code || delete_error_code) {
        KAA_FREE(sync_buffer);

        return error_code ? error_code : delete_error_code;
    }


    kaatcp_connect_t connect_message;
    kaatcp_error_t kaatcp_error_code =
        kaatcp_fill_connect_message(KAA_TCP_CHANNEL_MAX_TIMEOUT,  KAA_PLATFORM_PROTOCOL_ID,
                (char *)sync_buffer, sync_size, (char *)self->encryption.aes_session_key,
                self->encryption.aes_session_key_size, (char *)self->encryption.signature,
                self->encryption.signature_size, &connect_message);

    if (kaatcp_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel [0x%08X] failed to fill CONNECT message",
                self->access_point.id);
        KAA_FREE(sync_buffer);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    kaa_buffer_get_free_space(self->out_buffer, &buffer_size);
    kaatcp_get_request_size(&connect_message, KAATCP_MESSAGE_CONNECT, &request_size);

    if (buffer_size < request_size) {
        error_code = kaa_buffer_reallocate_space(self->out_buffer, request_size);
    }

    if (!error_code) {
        error_code = kaa_buffer_allocate_space(self->out_buffer, &buffer, &buffer_size);
    }

    if (error_code) {
        KAA_FREE(sync_buffer);
        return KAA_ERR_NOMEM;
    }

    kaatcp_error_code = kaatcp_get_request_connect(&connect_message, buffer, &buffer_size);

    if (kaatcp_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel [0x%08X] failed to get serialize CONNECT message",
                self->access_point.id);
        KAA_FREE(sync_buffer);

        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] created CONNECT message (%zu bytes)",
            self->access_point.id, buffer_size);

    error_code = kaa_buffer_lock_space(self->out_buffer, buffer_size);

    KAA_FREE(sync_buffer);

    if (error_code) {
        return error_code;
    }

    self->channel_state = KAA_TCP_CHANNEL_AUTHORIZING;

    self->sync_state = KAA_TCP_CHANNEL_SYNC_OP_STARTED;

    return error_code;
}



/*
 * Checks is specified service pending to sync.
 */
bool is_service_pending(kaa_tcp_channel_t *self, const kaa_extension_id service)
{
    KAA_RETURN_IF_NIL2(self, self->pending_request_services, false);

    size_t i = 0;
    for (; i < self->pending_request_service_count; ++i) {
        if (self->pending_request_services[i] == service) {
            return true;
        }
    }
    return false;
}


/* Appends destination array with only new members from source array.
 * Capacity is a maximum amount of items that can be stored in the destination
 * array. Destination count is a current amount of items in destination array.
 *
 * Returns new array count.
 */
static size_t append_with_new_services(kaa_extension_id *dest,
        size_t dest_count,
        size_t dest_capacity,
        const kaa_extension_id *src,
        size_t src_count)
{
    KAA_RETURN_IF_NIL2(dest, src, 0);

    for (size_t i = 0; i < src_count; ++i) {
        bool found = false;
        for (size_t j = 0; j < dest_count; ++j) {
            if (src[i] == dest[j]) {
                found = true;
                break;
            }
        }

        /* Only add new services that doesn't yet exist in destination array */
        if (!found) {
            dest[dest_count++] = src[i];
        }

        /* No more empty space in array */
        if (dest_count == dest_capacity) {
            break;
        }
    }

    return dest_count;
}

/*
 * Delete specified services from pending list.
 */
kaa_error_t kaa_tcp_channel_delete_pending_services(kaa_tcp_channel_t *self,
        const kaa_extension_id services[], size_t service_count)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    // These cases are normal.
    if (!self->pending_request_services || !self->pending_request_service_count || !services || !service_count) {
        return KAA_ERR_NONE;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] %zu pending services, going to delete %zu services",
            self->access_point.id, self->pending_request_service_count, service_count);

    //Check if services to delete point to himself
    if (services == self->pending_request_services) {
        //Remove all
        KAA_FREE(self->pending_request_services); //free previous pending services array.
        self->pending_request_services = NULL;
        self->pending_request_service_count = 0;
        return KAA_ERR_NONE;
    }

    size_t new_service_count = 0;
    kaa_extension_id temp_new_services[self->pending_request_service_count];

    new_service_count = append_with_new_services(temp_new_services,
            new_service_count,
            self->pending_request_service_count,
            self->pending_request_services,
            self->pending_request_service_count);

    KAA_FREE(self->pending_request_services); //free previous pending services array.
    self->pending_request_services = NULL;
    self->pending_request_service_count = 0;

    self->pending_request_services = KAA_MALLOC(new_service_count * sizeof(kaa_extension_id));
    KAA_RETURN_IF_NIL(self->pending_request_services, KAA_ERR_NOMEM);

    if (new_service_count > 0) {
        self->pending_request_service_count = new_service_count;
        memcpy(self->pending_request_services, temp_new_services, new_service_count * sizeof(kaa_extension_id));
    }

    return KAA_ERR_NONE;
}



/*
 * Update pending service list with specified list. Pending service list should have only unique services.
 */
kaa_error_t kaa_tcp_channel_update_pending_services(kaa_tcp_channel_t *self,
        const kaa_extension_id services[], size_t service_count)
{
    KAA_RETURN_IF_NIL3(self, services, service_count, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(self->logger,KAA_ERR_NONE,"Kaa TCP channel [0x%08X] %zu pending services, going to update %zu services",
            self->access_point.id, self->pending_request_service_count, service_count);

    size_t new_service_count = 0;
    kaa_extension_id temp_new_services[self->pending_request_service_count + service_count];

    /* First call of sync handlers services, no one services wait */
    if (!self->pending_request_services || !self->pending_request_service_count) {
        temp_new_services[new_service_count++] = services[0];
    } else {
        /* Some services waiting to sync with service, need merge with other services */
        new_service_count = self->pending_request_service_count;
        memcpy(temp_new_services, self->pending_request_services, new_service_count * sizeof(kaa_extension_id));

        KAA_FREE(self->pending_request_services); //free previous pending services array.
        self->pending_request_services = NULL;
        self->pending_request_service_count = 0;
    }

    new_service_count = append_with_new_services(temp_new_services,
            new_service_count,
            self->pending_request_service_count + service_count,
            services,
            service_count);

    self->pending_request_services = KAA_MALLOC(new_service_count * sizeof(kaa_extension_id));
    if (!self->pending_request_services) {
        return KAA_ERR_NOMEM;
    }

    if (new_service_count > 0) {
        self->pending_request_service_count = new_service_count;
        memcpy(self->pending_request_services, temp_new_services, new_service_count * sizeof(kaa_extension_id));
    }

    return KAA_ERR_NONE;
}



/*
 * Callback function, when access point hostname resolved.
 */
kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolved(void *context,
        const kaa_sockaddr_t *addr, kaa_socklen_t addr_size)
{
    KAA_RETURN_IF_NIL2(context, addr, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;

    memcpy(&channel->access_point.sockaddr, addr, addr_size);
    channel->access_point.sockaddr_length = addr_size;

    channel->access_point.state = AP_RESOLVED;

    KAA_LOG_INFO(channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] hostname resolved",
            channel->access_point.id);

    kaa_error_t error_code = kaa_tcp_channel_connect_access_point(channel);
    if (error_code) {
        if (channel->event_callback) {
            channel->event_callback(channel->event_context,
                    SOCKET_CONNECTION_ERROR, channel->access_point.socket_descriptor);
        }
        error_code = kaa_tcp_channel_on_access_point_failed((kaa_tcp_channel_t *) context, KAA_CHANNEL_NA);
    }
    return error_code;
}



/*
 * Callback function, when access point hostname resolve failed.
 */
kaa_error_t kaa_tcp_channel_set_access_point_hostname_resolve_failed(void *context)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    kaa_tcp_channel_t *channel = (kaa_tcp_channel_t *) context;

    KAA_LOG_INFO(channel->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] hostname resolve failed",
            channel->access_point.id);

    channel->access_point.state = AP_NOT_SET;

    return KAA_ERR_NONE;
}



/*
 * Read uint32 value from buffer.
 */
static uint32_t get_uint32_t(const uint8_t *buffer)
{
    if (!buffer) {
        return 0;
    }

    uint32_t value = 0;
    memcpy(&value, buffer, sizeof(value));
    return KAA_NTOHL(value);
}



/*
 * Connect access point.
 */
kaa_error_t kaa_tcp_channel_connect_access_point(kaa_tcp_channel_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    if (self->access_point.state != AP_RESOLVED) {
        return KAA_ERR_BAD_STATE;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] connecting to the server...",
            self->access_point.id);
    kaa_error_t error_code = ext_tcp_utils_open_tcp_socket(&self->access_point.socket_descriptor,
            (kaa_sockaddr_t *) &self->access_point.sockaddr, self->access_point.sockaddr_length);

    KAA_RETURN_IF_ERR(error_code);

    if (self->access_point.socket_descriptor < 0) {
        return KAA_ERR_SOCKET_CONNECT_ERROR;
    }

    self->access_point.state = AP_CONNECTING;
    self->sync_state = KAA_TCP_CHANNEL_SYNC_OP_STARTED;
    return KAA_ERR_NONE;
}



/*
 * Release access point.
 */
kaa_error_t kaa_tcp_channel_release_access_point(kaa_tcp_channel_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    kaa_error_t error_code = KAA_ERR_NONE;

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] releasing channel's resources",
            self->access_point.id);

    if (self->access_point.state == AP_CONNECTED || self->access_point.state == AP_CONNECTING) {
        ext_tcp_utils_tcp_socket_close(self->access_point.socket_descriptor);
    }

    self->access_point.socket_descriptor = KAA_TCP_SOCKET_NOT_SET;
    self->access_point.state = AP_NOT_SET;
    self->access_point.id = 0;

    KAA_FREE(self->access_point.hostname);
    self->access_point.hostname = NULL;
    self->access_point.hostname_length = 0;

    KAA_FREE(self->access_point.public_key);
    self->access_point.public_key = NULL;
    self->access_point.public_key_length = 0;

    return error_code;
}



/*
 * Write to socket sync services.
 */
kaa_error_t kaa_tcp_channel_write_pending_services(kaa_tcp_channel_t *self,
        kaa_extension_id *service, size_t services_count)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] going to serialize %zu pending services"
            , self->access_point.id, self->pending_request_service_count);

    KAA_RETURN_IF_NIL2(service, services_count, KAA_ERR_NONE);

    char *buffer = NULL;
    uint8_t *sync_buffer = NULL;
    size_t sync_size = 0;
    size_t buffer_size = 0;
    bool zipped = false;
    kaatcp_kaasync_t kaa_sync_message;

    kaa_error_t error_code = kaa_buffer_allocate_space(self->out_buffer, &buffer, &buffer_size);
    KAA_RETURN_IF_ERR(error_code);

    error_code = kaa_platform_protocol_alloc_serialize_client_sync(
            self->transport_context.kaa_context->platform_protocol,
            service,
            services_count,
            &sync_buffer,
            &sync_size);

    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Kaa TCP channel [0x%08X] Failed to allocate and serialize client sync",
                self->access_point.id);
        KAA_FREE(sync_buffer);
        return error_code;
    }

#ifdef KAA_ENCRYPTION
    /* TODO(KAA-1246): Rework the solution to cipher without additional allocations */
    bool encrypted = true;
    error_code = kaa_tcp_channel_encrypt(self,(uint8_t **) &sync_buffer, &sync_size);
    if (error_code) {
        KAA_FREE(sync_buffer);
        return error_code;
    }
#else
    bool encrypted = false;
#endif
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] serialized client sync (%zu bytes)",
            self->access_point.id, sync_size);

    kaa_tcp_channel_delete_pending_services(self, service, services_count);

    if (!error_code) {
        kaa_buffer_get_free_space(self->out_buffer, &buffer_size);
        if ( buffer_size < (sync_size + sizeof(kaatcp_kaasync_header_t)) ) {
            error_code = kaa_buffer_reallocate_space( self->out_buffer, (sync_size + sizeof(kaatcp_kaasync_header_t)) );
            if (!error_code) {
                kaa_buffer_allocate_space(self->out_buffer, &buffer, &buffer_size);
            }
        }
    }

    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Kaa TCP channel [0x%08X] failed to serialize client sync",
                self->access_point.id);
        KAA_FREE(sync_buffer);
        return error_code;
    }

    kaatcp_error_t parser_error_code = kaatcp_fill_kaasync_message((char *)sync_buffer, sync_size, self->message_id++,
            zipped, encrypted, &kaa_sync_message);

    if (parser_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel [0x%08X] failed to fill KAASYNC message",
                self->access_point.id);
        KAA_FREE(sync_buffer);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    parser_error_code = kaatcp_get_request_kaasync(&kaa_sync_message, buffer, &buffer_size);
    if (parser_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel [0x%08X] failed to serialize KAASYNC message",
                self->access_point.id);
        KAA_FREE(sync_buffer);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Kaa TCP channel [0x%08X] going to send KAASYNC message (%d bytes)",
            self->access_point.id, sync_size);

    error_code = kaa_buffer_lock_space(self->out_buffer, buffer_size);

    KAA_FREE(sync_buffer);
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
    char *buf = NULL;
    size_t buf_size = 0;
    size_t bytes_written = 0;
    kaa_error_t error_code = kaa_buffer_get_unprocessed_space(self->out_buffer, &buf, &buf_size);
    KAA_LOG_TRACE(self->logger, error_code, "Kaa TCP channel [0x%08X] writing %zu bytes to the socket",
            self->access_point.id, buf_size);
    KAA_RETURN_IF_ERR(error_code);
    if (buf_size > 0) {
        ext_tcp_socket_io_errors_t io_error =
            ext_tcp_utils_tcp_socket_write(self->access_point.socket_descriptor,
                    buf, buf_size, &bytes_written);
        switch (io_error) {
            case KAA_TCP_SOCK_IO_OK:
                error_code = kaa_buffer_free_allocated_space(self->out_buffer, bytes_written);
                KAA_LOG_TRACE(self->logger, error_code, "Kaa TCP channel [0x%08X] %zu bytes were successfully written",
                        self->access_point.id, bytes_written);
                break;
            default:
                KAA_LOG_WARN(self->logger, KAA_ERR_SOCKET_ERROR, "Kaa TCP channel [0x%08X] write failed",
                        self->access_point.id);
                error_code = kaa_tcp_channel_socket_io_error(self, KAA_CHANNEL_NA);
                break;
        }
    }

    return error_code;
}


/*
 * Send Ping request message
 */
kaa_error_t kaa_tcp_channel_ping(kaa_tcp_channel_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    kaa_error_t error_code = KAA_ERR_NONE;
    char *buffer = NULL;
    size_t buffer_size = 0;

    kaa_buffer_get_free_space(self->out_buffer, &buffer_size);
    if (buffer_size < KAA_PING_MESSAGE_SIZE) {
        error_code = kaa_buffer_reallocate_space(self->out_buffer, KAA_PING_MESSAGE_SIZE);
    }

    error_code = kaa_buffer_allocate_space(self->out_buffer, &buffer, &buffer_size);
    KAA_RETURN_IF_ERR(error_code);

    kaatcp_error_t kaatcp_error_code = kaatcp_get_request_ping(buffer, &buffer_size);

    if (kaatcp_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel [0x%08X] failed to serialize PING message",
                self->access_point.id);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    error_code = kaa_buffer_lock_space(self->out_buffer, buffer_size);

    self->keepalive.last_sent_keepalive = KAA_TIME();

    KAA_LOG_INFO(self->logger,KAA_ERR_NONE,"Kaa TCP channel [0x%08X] going to send PING message (%zu bytes)",
            self->access_point.id, buffer_size);

    return error_code;
}



/*
 * Send Kaa TCP disconnect message
 */
kaa_error_t kaa_tcp_channel_disconnect_internal(kaa_tcp_channel_t  *self,
        kaatcp_disconnect_reason_t return_code)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    char *buffer = NULL;
    size_t buffer_size = 0;

    kaa_buffer_get_free_space(self->out_buffer, &buffer_size);
    if (buffer_size < KAA_DISCONNECT_MESSAGE_SIZE) {
        error_code = kaa_buffer_reallocate_space(self->out_buffer, KAA_DISCONNECT_MESSAGE_SIZE);
    }

    error_code = kaa_buffer_allocate_space(self->out_buffer, &buffer, &buffer_size);
    KAA_RETURN_IF_ERR(error_code);

    kaatcp_disconnect_t disconnect_message;
    kaatcp_error_t kaatcp_error_code = kaatcp_fill_disconnect_message(return_code
            , &disconnect_message);

    if (kaatcp_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel [0x%08X] failed to fill DISCONNECT message",
                self->access_point.id);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    kaatcp_error_code = kaatcp_get_request_disconnect(&disconnect_message, buffer, &buffer_size);

    if (kaatcp_error_code) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_TCPCHANNEL_PARSER_ERROR, "Kaa TCP channel [0x%08X] failed to serialize DISCONNECT message",
                self->access_point.id);
        return KAA_ERR_TCPCHANNEL_PARSER_ERROR;
    }

    error_code = kaa_buffer_lock_space(self->out_buffer, buffer_size);

    self->channel_state = KAA_TCP_CHANNEL_DISCONNECTING;

    KAA_LOG_TRACE(self->logger,KAA_ERR_NONE,"Kaa TCP channel [0x%08X] going to send DISCONNECT message (%zu bytes)",
            self->access_point.id, buffer_size);

    return error_code;
}

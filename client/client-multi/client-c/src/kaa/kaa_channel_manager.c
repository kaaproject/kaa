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

#include <stddef.h>
#include <stdint.h>

#include "kaa_channel_manager.h"
#include "collections/kaa_list.h"
#include "utilities/kaa_log.h"
#include "utilities/kaa_mem.h"
#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"
// This header conflicts with stdio.h and should be put last
#include "platform/sock.h"

typedef struct {
    uint32_t                             channel_id;
    kaa_server_type_t                    server_type;
    kaa_transport_channel_interface_t    channel;
} kaa_transport_channel_wrapper_t;

typedef struct {
    bool        is_up_to_date;
    uint16_t    request_id;
    uint32_t    payload_size;
    uint16_t    channel_count;
} kaa_sync_info_t;

typedef struct {
    kaa_auth_failure_fn callback;
    void *context;
} kaa_auth_failure_handler;

struct kaa_channel_manager_t {
    kaa_list_t                  *transport_channels;
    kaa_context_t               *kaa_context;
    kaa_logger_t                *logger;
    kaa_sync_info_t             sync_info;
    kaa_auth_failure_handler    auth_failure_handler;
};



static void destroy_channel(void *data)
{
    KAA_RETURN_IF_NIL(data,);

    kaa_transport_channel_wrapper_t *channel_wrapper = (kaa_transport_channel_wrapper_t *)data;

    if (channel_wrapper->channel.destroy) {
        channel_wrapper->channel.destroy(channel_wrapper->channel.context);
    }

    KAA_FREE(channel_wrapper);
}

static bool find_channel_by_channel_id(/* current channel */void *data, /* channel-matcher */void *context)
{
    KAA_RETURN_IF_NIL2(data, context, false);
    return (((kaa_transport_channel_wrapper_t *)data)->channel_id == *((uint32_t *)context));
}

static bool find_channel_by_protocol_id(/* current channel */void *data, /* channel-matcher */void *context)
{
    KAA_RETURN_IF_NIL2(data, context, false);

    kaa_transport_protocol_id_t channel_info;
    kaa_error_t error_code = ((kaa_transport_channel_wrapper_t *)data)->channel.
                    get_protocol_id(((kaa_transport_channel_wrapper_t *)data)->channel.context, &channel_info);
    if (error_code)
        return false;

    return kaa_transport_protocol_id_equals((kaa_transport_protocol_id_t *)context, &channel_info);
}

kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p,
        kaa_context_t *context)
{
    if (!channel_manager_p || !context) {
        return KAA_ERR_BADPARAM;
    }

    kaa_channel_manager_t *channel_manager = KAA_MALLOC(sizeof(kaa_channel_manager_t));
    if (!channel_manager) {
        return KAA_ERR_NOMEM;
    }

    channel_manager->transport_channels = kaa_list_create();
    if (!channel_manager->transport_channels) {
        KAA_FREE(channel_manager);
        return KAA_ERR_NOMEM;
    }

    channel_manager->kaa_context                   = context;
    channel_manager->sync_info.request_id          = 0;
    channel_manager->sync_info.is_up_to_date       = false;
    channel_manager->logger                        = context->logger;
    channel_manager->auth_failure_handler.callback = NULL;
    channel_manager->auth_failure_handler.context  = NULL;

    *channel_manager_p = channel_manager;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_transport_channel_id_calculate(kaa_transport_channel_interface_t *channel
                                             , uint32_t *channel_id)
{
    KAA_RETURN_IF_NIL2(channel, channel_id, KAA_ERR_BADPARAM);

    const uint32_t prime = 31;

    *channel_id = 1;
    *channel_id = prime * (*channel_id) + (ptrdiff_t)channel->context;
    *channel_id = prime * (*channel_id) + (ptrdiff_t)channel->destroy;
    *channel_id = prime * (*channel_id) + (ptrdiff_t)channel->init;
    *channel_id = prime * (*channel_id) + (ptrdiff_t)channel->set_access_point;
    *channel_id = prime * (*channel_id) + (ptrdiff_t)channel->sync_handler;
    *channel_id = prime * (*channel_id) + (ptrdiff_t)channel->get_protocol_id;
    kaa_transport_protocol_id_t protoco_id = { 0, 0 };
    channel->get_protocol_id(channel->context, &protoco_id);
    *channel_id = prime * (*channel_id) + protoco_id.id;
    *channel_id = prime * (*channel_id) + protoco_id.version;

    *channel_id = prime * (*channel_id) + (ptrdiff_t)channel->get_supported_services;
    size_t services_count = 0;
    const kaa_extension_id *services = NULL;
    channel->get_supported_services(channel->context, &services, &services_count);
    if (services) {
        size_t i = 0;
        for (; i < services_count; ++i)
            *channel_id = prime * (*channel_id) + (int) services[i];
    }

    return KAA_ERR_NONE;
}

void kaa_channel_manager_destroy(kaa_channel_manager_t *self)
{
    if (self) {
        kaa_list_destroy(self->transport_channels, destroy_channel);
        KAA_FREE(self);
    }
}

static bool is_bootstrap_service_supported(kaa_transport_channel_interface_t *channel)
{
    KAA_RETURN_IF_NIL(channel, false);

    const kaa_extension_id *services;
    size_t service_count;
    kaa_error_t error_code = channel->get_supported_services(channel->context
                                                           , &services
                                                           , &service_count);

    if (!error_code) {
        size_t i = 0;
        for (; i < service_count; ++i) {
            if (services[i] == KAA_EXTENSION_BOOTSTRAP) {
                return true;
            }
        }
    }

    return false;
}

static kaa_error_t add_channel(kaa_channel_manager_t *self
                             , kaa_transport_channel_interface_t *channel
                             , uint32_t *channel_id)
{
    KAA_RETURN_IF_NIL2(self, channel, KAA_ERR_BADPARAM);

    kaa_transport_protocol_id_t protocol_id = { 0, 0 };
    channel->get_protocol_id(channel->context, &protocol_id);

    uint32_t id;
    kaa_transport_channel_id_calculate(channel, &id);

    kaa_list_node_t *it = kaa_list_find_next(kaa_list_begin(self->transport_channels), &find_channel_by_channel_id, &id);
    if (it) {
        KAA_LOG_WARN(self->kaa_context->logger, KAA_ERR_ALREADY_EXISTS,
                            "Transport channel [0x%08X] already exists (protocol: id=0x%08X, version=%u)"
                                                                    , id, protocol_id.id, protocol_id.version);
        return KAA_ERR_ALREADY_EXISTS;
    }

    kaa_transport_channel_wrapper_t *wrapper =
            (kaa_transport_channel_wrapper_t *)KAA_MALLOC(sizeof(kaa_transport_channel_wrapper_t));
    KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);

    wrapper->channel_id = id;
    wrapper->channel = *channel;
    wrapper->server_type = is_bootstrap_service_supported(channel) ? KAA_SERVER_BOOTSTRAP : KAA_SERVER_OPERATIONS;

    it = kaa_list_push_front(self->transport_channels, wrapper);
    if (!it) {
        KAA_LOG_ERROR(self->kaa_context->logger, KAA_ERR_NOMEM,
                "Failed to add new transport channel [0x%08X] (protocol: id=0x%08X, version=%u)"
                                                        , id, protocol_id.id, protocol_id.version);
        KAA_FREE(wrapper);
        return KAA_ERR_NOMEM;
    }

    if (channel_id) {
        *channel_id = id;
    }

    self->sync_info.is_up_to_date = false;

    KAA_LOG_INFO(self->kaa_context->logger, KAA_ERR_NONE,
            "%s transport channel [0x%08X] added (protocol: id=0x%08X, version=%u)"
               , (wrapper->server_type == KAA_SERVER_BOOTSTRAP) ? "Bootstrap" : "Operations"
               , id, protocol_id.id, protocol_id.version);

    return KAA_ERR_NONE;
}

static kaa_error_t init_channel(kaa_channel_manager_t *self
                              , kaa_transport_channel_interface_t *channel)
{
    KAA_RETURN_IF_NIL3(self, self->kaa_context, channel, KAA_ERR_BADPARAM);

    static kaa_transport_context_t transport_context = { NULL };
    transport_context.kaa_context = self->kaa_context;

    channel->init(channel->context, &transport_context);

    kaa_transport_protocol_id_t protocol_id = { 0, 0 };
    kaa_error_t error_code = channel->get_protocol_id(channel->context, &protocol_id);

    if (!error_code) {
        kaa_access_point_t *access_point;

        uint32_t id;
        kaa_transport_channel_id_calculate(channel, &id);

        kaa_list_node_t *it = kaa_list_find_next(kaa_list_begin(self->transport_channels), &find_channel_by_channel_id, &id);
        KAA_RETURN_IF_NIL(it, KAA_ERR_NOT_FOUND);

        bool is_bootstrap_channel = ((kaa_transport_channel_wrapper_t *)kaa_list_get_data(it))->
                                                                server_type == KAA_SERVER_BOOTSTRAP;

        if (is_bootstrap_channel) {
            access_point = kaa_bootstrap_manager_get_bootstrap_access_point(self->kaa_context->bootstrap_manager, &protocol_id);
        } else {
            access_point = kaa_bootstrap_manager_get_operations_access_point(self->kaa_context->bootstrap_manager, &protocol_id);
        }

        if (access_point) {
            KAA_LOG_TRACE(self->kaa_context->logger, KAA_ERR_NONE, "Found %s access point [0x%08X] for channel [0x%08X] "
                                "(protocol: id=0x%08X, version=%u)", (is_bootstrap_channel ? "Bootstrap" : "Operations")
                                , access_point->id, id, protocol_id.id, protocol_id.version);

            channel->set_access_point(channel->context, access_point);
        } else {
            if (is_bootstrap_channel) {
                KAA_LOG_WARN(self->kaa_context->logger, KAA_ERR_NOT_FOUND, "Could not find access point for Bootstrap channel [0x%08X] "
                                    "(protocol: id=0x%08X, version=%u)", id, protocol_id.id, protocol_id.version);
            } else {
                KAA_LOG_INFO(self->kaa_context->logger, KAA_ERR_NOT_FOUND, "Could not find access point for Operations channel [0x%08X] "
                                    "(protocol: id=0x%08X, version=%u)", id, protocol_id.id, protocol_id.version);
            }
            return KAA_ERR_BAD_STATE;
        }
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_channel_manager_add_transport_channel(kaa_channel_manager_t *self
                                                    , kaa_transport_channel_interface_t *channel
                                                    , uint32_t *channel_id)
{
    KAA_RETURN_IF_NIL2(self, channel, KAA_ERR_BADPARAM);

    kaa_error_t error_code = add_channel(self, channel, channel_id);
    if (!error_code) {
        error_code = init_channel(self, channel);
    }

    return error_code;
}

kaa_error_t kaa_channel_manager_remove_transport_channel(kaa_channel_manager_t *self
                                                       , uint32_t channel_id)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_error_t error_code = kaa_list_remove_first(self->transport_channels
                                                 , &find_channel_by_channel_id
                                                 , &channel_id
                                                 , &destroy_channel);

    if (!error_code) {
        self->sync_info.is_up_to_date = false;
        KAA_LOG_INFO(self->kaa_context->logger, KAA_ERR_NONE, "Transport channel [0x%08X] was removed", channel_id);
        return KAA_ERR_NONE;
    }

    KAA_LOG_WARN(self->kaa_context->logger, error_code, "Transport channel [0x%08X] was not found", channel_id);

    return error_code;
}

kaa_transport_channel_interface_t *kaa_channel_manager_get_transport_channel(kaa_channel_manager_t *self
                                                                           , kaa_extension_id service_type)
{
    KAA_RETURN_IF_NIL(self, NULL);

    kaa_transport_channel_wrapper_t *channel_wrapper;
    const kaa_extension_id *services;
    size_t service_count;

    kaa_list_node_t *it = kaa_list_begin(self->transport_channels);
    while (it) {
        channel_wrapper = kaa_list_get_data(it);

        kaa_error_t error_code = channel_wrapper->channel.get_supported_services(channel_wrapper->channel.context,
                                                                                 &services,
                                                                                 &service_count);
        if (error_code || !services || !service_count) {
            KAA_LOG_WARN(self->kaa_context->logger, error_code, "Failed to retrieve list of supported services "
                                        "for transport channel [0x%08X]", channel_wrapper->channel_id);
            continue;
        }

        while (service_count--) {
            if (*services++ == service_type) {
                KAA_LOG_TRACE(self->kaa_context->logger, KAA_ERR_NONE, "Transport channel "
                        "[0x%08X] for service %u was found"
                        , channel_wrapper->channel_id, service_type);
                return &channel_wrapper->channel;
            }
        }

        it = kaa_list_next(it);
    }

    KAA_LOG_DEBUG(self->kaa_context->logger, KAA_ERR_NOT_FOUND,
            "Failed to find transport channel for service %u", service_type);

    return NULL;
}

kaa_error_t kaa_channel_manager_bootstrap_request_get_size(kaa_channel_manager_t *self
                                                         , size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);

    *expected_size = 0;

    if (!self->sync_info.is_up_to_date) {
        size_t channel_count = kaa_list_get_size(self->transport_channels);

        if (channel_count > 0) {
            *expected_size += KAA_EXTENSION_HEADER_SIZE
                            + sizeof(uint16_t) /* Request ID */
                            + sizeof(uint16_t) /* Supported protocols count */
                            + channel_count * (sizeof(uint32_t) /* Supported protocol ID */
                                             + sizeof(uint16_t) /* Supported protocol version */
                                             + sizeof(uint16_t) /* Reserved */);

            self->sync_info.payload_size = *expected_size - KAA_EXTENSION_HEADER_SIZE;
            self->sync_info.channel_count = channel_count;
            self->sync_info.is_up_to_date = true;
        }
    } else {
        *expected_size = self->sync_info.payload_size + KAA_EXTENSION_HEADER_SIZE;
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_channel_manager_bootstrap_request_serialize(kaa_channel_manager_t *self
                                                          , kaa_platform_message_writer_t* writer)
{
    KAA_RETURN_IF_NIL2(self, writer, KAA_ERR_BADPARAM);
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to serialize client bootstrap sync");

    kaa_error_t error_code = KAA_ERR_NONE;

    if (self->sync_info.payload_size > 0 && self->sync_info.channel_count > 0) {
        error_code = kaa_platform_message_write_extension_header(writer
                                                               , KAA_EXTENSION_BOOTSTRAP
                                                               , 0
                                                               , self->sync_info.payload_size);
        KAA_RETURN_IF_ERR(error_code);

        uint16_t network_order_16;
        uint32_t network_order_32;

        ++self->sync_info.request_id;
        network_order_16 = KAA_HTONS(self->sync_info.request_id);
        error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);

        network_order_16 = KAA_HTONS(self->sync_info.channel_count);
        error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);

        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Serializing %u supported protocol(s), request id %u", self->sync_info.channel_count, self->sync_info.request_id);
        kaa_transport_channel_wrapper_t *channel_wrapper;
        kaa_transport_protocol_id_t protocol_info;

        kaa_list_node_t *it = kaa_list_begin(self->transport_channels);
        while (it) {
            channel_wrapper = (kaa_transport_channel_wrapper_t *)kaa_list_get_data(it);

            error_code = channel_wrapper->channel.get_protocol_id(channel_wrapper->channel.context
                                                                , &protocol_info);
            KAA_RETURN_IF_ERR(error_code);

            network_order_32 = KAA_HTONL(protocol_info.id);
            error_code = kaa_platform_message_write(writer, &network_order_32, sizeof(uint32_t));
            KAA_RETURN_IF_ERR(error_code);

            network_order_16 = KAA_HTONS(protocol_info.version);
            error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
            KAA_RETURN_IF_ERR(error_code);

            network_order_16 = 0; /* Reserved */
            error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
            KAA_RETURN_IF_ERR(error_code);

            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Serialized protocol: id '%u', version '%u'", protocol_info.id, protocol_info.version);
            it = kaa_list_next(it);
        }
    }

    return error_code;
}

kaa_error_t kaa_channel_manager_on_new_access_point(kaa_channel_manager_t *self
                                                  , kaa_transport_protocol_id_t *protocol_id
                                                  , kaa_server_type_t server_type
                                                  , kaa_access_point_t *access_point)
{
    KAA_RETURN_IF_NIL3(self, protocol_id, access_point, KAA_ERR_BADPARAM);

    kaa_list_node_t *channel_it = kaa_list_find_next(kaa_list_begin(self->transport_channels)
                                                   , &find_channel_by_protocol_id
                                                   , protocol_id);

    while (channel_it) {
        kaa_transport_channel_wrapper_t *channel_wrapper = kaa_list_get_data(channel_it);
        if (channel_wrapper->server_type == server_type) {
            KAA_LOG_TRACE(self->kaa_context->logger, KAA_ERR_NONE, "Set new %s access point [0x%08X] for channel [0x%08X] "
                                 "(protocol: id=0x%08X, version=%u)"
                                , (channel_wrapper->server_type == KAA_SERVER_BOOTSTRAP ? "Bootstrap" : "Operations")
                                , access_point->id, channel_wrapper->channel_id, protocol_id->id, protocol_id->version);
            channel_wrapper->channel.set_access_point(
                    channel_wrapper->channel.context, access_point);
        }

        channel_it = kaa_list_find_next(kaa_list_next(channel_it)
                                      , &find_channel_by_protocol_id
                                      , protocol_id);
    }

    return KAA_ERR_NONE;
}

void kaa_channel_manager_set_auth_failure_handler(kaa_channel_manager_t *self,
        kaa_auth_failure_fn handler, void *context)
{
    if (!self) {
        return;
    }

    self->auth_failure_handler.callback = handler;
    self->auth_failure_handler.context  = context;
}

void kaa_channel_manager_process_auth_failure(kaa_channel_manager_t *self,
        kaa_auth_failure_reason reason)
{
    if (!self || !self->auth_failure_handler.callback) {
        return;
    }

    self->auth_failure_handler.callback(reason, self->auth_failure_handler.context);
}

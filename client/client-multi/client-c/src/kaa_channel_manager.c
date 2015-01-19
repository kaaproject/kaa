/*
 * Copyright 2014 CyberVision, Inc.
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

#include <string.h>

#include "kaa_channel_manager.h"
#include "collections/kaa_list.h"
#include "utilities/kaa_log.h"
#include "utilities/kaa_mem.h"
#include "kaa_common_schema.h"
#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"



typedef struct {
    bool        is_up_to_date;
    uint16_t    request_id;
    uint32_t    payload_size;
    uint16_t    channel_count;
} kaa_sync_info_t;

struct kaa_channel_manager_t {
    kaa_list_t                 *transport_channels;
    kaa_logger_t               *logger;
    kaa_sync_info_t            sync_info;
};



static void destroy_channel(void *data)
{
    KAA_RETURN_IF_NIL(data,);

    kaa_transport_channel_interface_t *channel =
            (kaa_transport_channel_interface_t *)data;

    if (channel->release_context) {
        channel->release_context(channel->context);
    }

    KAA_FREE(channel);
}


static bool find_channel(/* current channel */void *data, /* channel-matcher */void *context)
{
    KAA_RETURN_IF_NIL2(data, context, false);

    kaa_error_t error_code;
    kaa_transport_protocol_info_t info1;
    kaa_transport_protocol_info_t info2;

    kaa_transport_channel_interface_t *channel = (kaa_transport_channel_interface_t *)data;
    kaa_transport_channel_interface_t *matcher = (kaa_transport_channel_interface_t *)context;

    error_code = channel->get_protocol_info(channel->context, &info1);
    KAA_RETURN_IF_ERR(error_code);
    error_code = matcher->get_protocol_info(matcher->context, &info2);
    KAA_RETURN_IF_ERR(error_code);

    return (0 == memcmp(&info1, &info2, sizeof(kaa_transport_protocol_info_t)));
}

kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p
                                     , kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL2(channel_manager_p, logger, KAA_ERR_BADPARAM);

    *channel_manager_p = (kaa_channel_manager_t *) KAA_MALLOC(sizeof(kaa_channel_manager_t));
    if (!(*channel_manager_p))
        return KAA_ERR_NOMEM;

    (*channel_manager_p)->transport_channels      = NULL;
    (*channel_manager_p)->logger                  = logger;
    (*channel_manager_p)->sync_info.request_id    = 0;
    (*channel_manager_p)->sync_info.is_up_to_date = false;

    return KAA_ERR_NONE;
}

void kaa_channel_manager_destroy(kaa_channel_manager_t *self)
{
    if (self) {
        kaa_list_destroy(self->transport_channels, destroy_channel);
        KAA_FREE(self);
    }
}

kaa_error_t kaa_channel_manager_add_transport_channel(kaa_channel_manager_t *self
                                                    , kaa_transport_channel_interface_t *channel)
{
    KAA_RETURN_IF_NIL2(self, channel, KAA_ERR_BADPARAM);

    kaa_transport_protocol_info_t protocol_info;
    channel->get_protocol_info(channel->context, &protocol_info);

    kaa_list_t *it = kaa_list_find_next(self->transport_channels, &find_channel, channel);
    if (it) {
        KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS,
                "Failed to add transport channel (id=0x%X, version=%u): already exists"
                                                 , protocol_info.id, protocol_info.version);
        return KAA_ERR_ALREADY_EXISTS;
    }

    kaa_transport_channel_interface_t *copy =
            (kaa_transport_channel_interface_t *)KAA_MALLOC(sizeof(kaa_transport_channel_interface_t));
    KAA_RETURN_IF_NIL(copy, KAA_ERR_NOMEM);

    *copy = *channel;

    it = self->transport_channels ?
                 kaa_list_push_front(self->transport_channels, copy) :
                 kaa_list_create(copy);
    if (!it) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM,
                "Failed to add new transport channel (id=0x%X, version=%u)"
                                                     , protocol_info.id, protocol_info.version);
        KAA_FREE(copy);
        return KAA_ERR_NOMEM;
    }

    self->transport_channels = it;
    self->sync_info.is_up_to_date = false;

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "New transport channel (id=0x%X, version=%u) added"
                                                                        , protocol_info.id, protocol_info.version);

    return KAA_ERR_NONE;
}

kaa_error_t kaa_channel_manager_remove_transport_channel(kaa_channel_manager_t *self
                                                       , kaa_transport_channel_interface_t *channel)
{
    KAA_RETURN_IF_NIL2(self, channel, KAA_ERR_BADPARAM);

    kaa_error_t error_code;
    kaa_transport_protocol_info_t protocol_info;

    channel->get_protocol_info(channel->context, &protocol_info);

    error_code = kaa_list_remove_first(&self->transport_channels
                                     , &find_channel
                                     , channel
                                     , destroy_channel);

    if (!error_code) {
        self->sync_info.is_up_to_date = false;
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Transport channel (id=0x%X, version=%u) removed"
                                                            , protocol_info.id, protocol_info.version);
        return KAA_ERR_NONE;
    }

    KAA_LOG_WARN(self->logger, error_code, "Transport channel (id=0x%X, version=%u) was not found"
                                                          , protocol_info.id, protocol_info.version);

    return error_code;
}

kaa_transport_channel_interface_t *kaa_channel_manager_get_transport_channel(kaa_channel_manager_t *self
                                                                           , kaa_service_t service_type)
{
    KAA_RETURN_IF_NIL(self, NULL);

    kaa_error_t error_code = KAA_ERR_NONE;

    kaa_list_t *it = self->transport_channels;
    kaa_transport_channel_interface_t *channel =
            (kaa_transport_channel_interface_t *) kaa_list_get_data(it);

    kaa_service_t *services;
    size_t service_count;

    while (channel) {
        error_code = channel->get_supported_services(channel->context
                                                   , &services
                                                   , &service_count);
        if (error_code || !services || !service_count) {
            KAA_LOG_WARN(self->logger, error_code, "Failed to retrieve list of supported services", error_code);
            continue;
        }

        for (; service_count--;) {
            if (*services++ == service_type) {
                kaa_transport_protocol_info_t protocol_info;
                channel->get_protocol_info(channel->context, &protocol_info);

                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Transport channel "
                        "(id=0x%X, version=%u) for service %u was found"
                        , service_type, protocol_info.id, protocol_info.version);
                return channel;
            }
        }
        it = kaa_list_next(it);
        channel = (kaa_transport_channel_interface_t *) kaa_list_get_data(it);
    }

    KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Failed to find transport channel for service %u", service_type);
    return NULL;
}

kaa_error_t kaa_channel_manager_bootstrap_request_get_size(kaa_channel_manager_t *self
                                                         , size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);

    *expected_size = 0;

    if (!self->sync_info.is_up_to_date) {
        ssize_t channel_count = kaa_list_get_size(self->transport_channels);

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
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_channel_manager_bootstrap_request_serialize(kaa_channel_manager_t *self
                                                          , kaa_platform_message_writer_t* writer)
{
    KAA_RETURN_IF_NIL2(self, writer, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;

    if (self->sync_info.payload_size > 0 && self->sync_info.channel_count > 0) {
        error_code = kaa_platform_message_write_extension_header(writer
                                                               , KAA_BOOTSTRAP_EXTENSION_TYPE
                                                               , 0
                                                               , self->sync_info.payload_size);
        KAA_RETURN_IF_ERR(error_code);

        uint16_t network_order_16;
        uint32_t network_order_32;

        network_order_16 = KAA_HTONS(++self->sync_info.request_id);
        error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);

        network_order_16 = KAA_HTONS(self->sync_info.channel_count);
        error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);

        kaa_transport_channel_interface_t *channel;
        kaa_transport_protocol_info_t protocol_info;

        kaa_list_t *it = self->transport_channels;

        while (it) {
            channel = (kaa_transport_channel_interface_t *)kaa_list_get_data(it);
            if (channel) {
                error_code = channel->get_protocol_info(channel->context, &protocol_info);
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
            }

            it = kaa_list_next(it);
        }
    }

    return error_code;
}

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

#include <stdlib.h>
#include <string.h>

#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"
#include "collections/kaa_list.h"
#include "kaa_bootstrap_manager.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_channel_manager.h"



typedef struct {
    kaa_transport_protocol_id_t    protocol_info;
    kaa_list_t                       *access_points;
    kaa_list_t                       *current_access_points;
} kaa_channel_access_points_t;

struct kaa_bootstrap_manager_t {
    kaa_channel_manager_t    *channel_manager;
    kaa_list_t               *channel_access_points;
    kaa_logger_t             *logger;
};


extern kaa_transport_channel_interface_t *kaa_channel_manager_get_transport_channel(kaa_channel_manager_t *self
                                                                                  , kaa_service_t service_type);



static kaa_service_t bootstrap_sync_services[1] = { KAA_SERVICE_BOOTSTRAP };



static void destroy_access_point(void *data)
{
    KAA_RETURN_IF_NIL(data,);
    kaa_access_point_t *access_point = (kaa_access_point_t *)data;
    if (access_point->connection_data) {
        KAA_FREE(access_point->connection_data);
    }
    KAA_FREE(access_point);
}

static void destroy_channel_access_points(void *data)
{
    KAA_RETURN_IF_NIL(data,);
    kaa_channel_access_points_t *channel_access_points =
                            (kaa_channel_access_points_t *)data;
    kaa_list_destroy(channel_access_points->access_points, destroy_access_point);
    KAA_FREE(channel_access_points);
}

static bool find_channel_access_points(void *data, void *context)
{
    KAA_RETURN_IF_NIL2(data, context, false);
    return (0 == memcmp(&((kaa_channel_access_points_t *)data)->protocol_info
                      , (kaa_transport_protocol_id_t *)context
                      , sizeof(kaa_transport_protocol_id_t)));
}

kaa_error_t kaa_bootstrap_manager_create(kaa_bootstrap_manager_t **bootstrap_manager_p
                                       , kaa_channel_manager_t *channel_manager
                                       , kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL3(bootstrap_manager_p, channel_manager, logger, KAA_ERR_BADPARAM);

    *bootstrap_manager_p = (kaa_bootstrap_manager_t*) KAA_MALLOC(sizeof(kaa_bootstrap_manager_t));
    KAA_RETURN_IF_NIL(*bootstrap_manager_p, KAA_ERR_NOMEM);

    (*bootstrap_manager_p)->channel_manager = channel_manager;
    (*bootstrap_manager_p)->logger = logger;
    (*bootstrap_manager_p)->channel_access_points = NULL;

    return KAA_ERR_NONE;
}

void kaa_bootstrap_manager_destroy(kaa_bootstrap_manager_t *self)
{
    KAA_RETURN_IF_NIL(self,);
    kaa_list_destroy(self->channel_access_points, destroy_channel_access_points);
    KAA_FREE(self);
}

kaa_access_point_t *kaa_bootstrap_manager_get_current_access_point(kaa_bootstrap_manager_t *self
                                                                 , kaa_transport_protocol_id_t *protocol_info)
{
    KAA_RETURN_IF_NIL2(self, protocol_info, NULL);

    kaa_list_t *channel_access_points_it = kaa_list_find_next(self->channel_access_points
                                                            , find_channel_access_points
                                                            , protocol_info);
    KAA_RETURN_IF_NIL(channel_access_points_it, NULL);

    kaa_channel_access_points_t *channel_access_points =
            (kaa_channel_access_points_t *)kaa_list_get_data(channel_access_points_it);
    KAA_RETURN_IF_NIL(channel_access_points, NULL);

    return (kaa_access_point_t *)kaa_list_get_data(channel_access_points->current_access_points);
}

static kaa_error_t do_sync(kaa_bootstrap_manager_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_transport_channel_interface_t *channel =
            kaa_channel_manager_get_transport_channel(
                            self->channel_manager, KAA_SERVICE_BOOTSTRAP);
    if (channel) {
        channel->sync_handler(channel->context, bootstrap_sync_services, 1);
        return KAA_ERR_NONE;
    }

    return KAA_ERR_NOT_FOUND;
}

kaa_access_point_t *kaa_bootstrap_manager_get_next_access_point(kaa_bootstrap_manager_t *self
                                                              , kaa_transport_protocol_id_t *protocol_info)
{
    KAA_RETURN_IF_NIL2(self, protocol_info, NULL);

    kaa_list_t *channel_access_points_it = kaa_list_find_next(self->channel_access_points
                                                            , find_channel_access_points
                                                            , protocol_info);
    KAA_RETURN_IF_NIL(channel_access_points_it, NULL);

    kaa_channel_access_points_t *channel_access_points =
            (kaa_channel_access_points_t *)kaa_list_get_data(channel_access_points_it);
    KAA_RETURN_IF_NIL(channel_access_points, NULL);

    channel_access_points->current_access_points =
            kaa_list_next(channel_access_points->current_access_points);
    kaa_access_point_t *access_point =
            (kaa_access_point_t *)kaa_list_get_data(channel_access_points->current_access_points);

    if (!access_point) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Could not find next access point "
                "(protocol info: id=%X, version=%u). Going to sync..."
                , protocol_info->id, protocol_info->version);
        do_sync(self);
    }

    return access_point;
}

static kaa_error_t add_access_point(kaa_bootstrap_manager_t *self
                                  , kaa_transport_protocol_id_t *protocol_info
                                  , kaa_access_point_t *access_point)
{
    KAA_RETURN_IF_NIL2(protocol_info, access_point, KAA_ERR_BADPARAM);

    kaa_channel_access_points_t *channel_access_points;
    kaa_list_t *channel_it = kaa_list_find_next(self->channel_access_points
                                              , find_channel_access_points
                                              , protocol_info);

    if (channel_it) {
        channel_access_points = kaa_list_get_data(channel_it);
        KAA_RETURN_IF_NIL(channel_access_points, KAA_ERR_BADDATA);

        kaa_list_t *access_point_it = kaa_list_push_front(channel_access_points->access_points
                                                        , access_point);
        KAA_RETURN_IF_NIL(access_point_it, KAA_ERR_NOMEM);

        channel_access_points->current_access_points =
                channel_access_points->access_points = access_point_it;
    } else {
        channel_access_points = (kaa_channel_access_points_t *)KAA_MALLOC(sizeof(kaa_channel_access_points_t));
        KAA_RETURN_IF_NIL(channel_access_points, KAA_ERR_NOMEM);

        channel_access_points->protocol_info = *protocol_info;
        channel_access_points->current_access_points =
                channel_access_points->access_points = kaa_list_create(access_point);

        if (!channel_access_points->access_points) {
            KAA_FREE(channel_access_points);
            KAA_LOG_WARN(self->logger, KAA_ERR_NOMEM, "Failed to add new access point: "
                                "id=%lu, protocol info: id=%X, version=%u"
                                , access_point->id, protocol_info->id, protocol_info->version);
            return KAA_ERR_NOMEM;
        }

        kaa_list_t *channel_access_points_it = self->channel_access_points ?
                        kaa_list_push_front(self->channel_access_points, channel_access_points) :
                        kaa_list_create(channel_access_points);

        if (!channel_access_points_it) {
            KAA_FREE(channel_access_points);
            KAA_LOG_WARN(self->logger, KAA_ERR_NOMEM, "Failed to add new access point: "
                                "id=%lu, protocol info: id=%X, version=%u"
                                , access_point->id, protocol_info->id, protocol_info->version);
            return KAA_ERR_NOMEM;
        }

        self->channel_access_points = channel_access_points_it;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Added new access point: "
                        "id=%lu, protocol info: id=%X, version=%u"
                        , access_point->id, protocol_info->id, protocol_info->version);

    return KAA_ERR_NONE;
}

kaa_error_t kaa_bootstrap_manager_handle_server_sync(kaa_bootstrap_manager_t *self
                                                   , kaa_platform_message_reader_t *reader
                                                   , uint32_t extension_options
                                                   , size_t extension_length)
{
    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);

    kaa_list_destroy(self->channel_access_points, destroy_channel_access_points);

    kaa_error_t error_code = KAA_ERR_NONE;

    uint16_t request_id;
    error_code = kaa_platform_message_read(reader, &request_id, sizeof(uint16_t));
    KAA_RETURN_IF_ERR(error_code);
    request_id = KAA_NTOHS(request_id);

    uint16_t access_point_count;
    error_code = kaa_platform_message_read(reader, &access_point_count, sizeof(uint16_t));
    KAA_RETURN_IF_ERR(error_code);
    access_point_count = KAA_NTOHS(access_point_count);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received %u access points (request_id=%u)"
                                                        , access_point_count, request_id);

    uint32_t access_point_id;
    kaa_transport_protocol_id_t protocol_info;
    uint16_t connection_data_len;

    while (access_point_count--) {
        error_code = kaa_platform_message_read(reader, &access_point_id, sizeof(uint32_t));
        KAA_RETURN_IF_ERR(error_code);
        access_point_id = KAA_NTOHL(access_point_id);

        error_code = kaa_platform_message_read(reader, &protocol_info.id, sizeof(uint32_t));
        KAA_RETURN_IF_ERR(error_code);
        protocol_info.id = KAA_NTOHL(protocol_info.id);

        error_code = kaa_platform_message_read(reader, &protocol_info.version, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);
        protocol_info.version = KAA_NTOHS(protocol_info.version);

        error_code = kaa_platform_message_read(reader, &connection_data_len, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);
        connection_data_len = KAA_NTOHS(connection_data_len);

        if (!connection_data_len) {
            KAA_LOG_ERROR(self->logger, KAA_ERR_BADDATA, "Connection data length is 0");
            return KAA_ERR_BADDATA;
        }

        char *connection_data = (char *)KAA_MALLOC(connection_data_len);
        KAA_RETURN_IF_NIL(connection_data, KAA_ERR_NOMEM);

        kaa_access_point_t *new_access_point =
                (kaa_access_point_t *)KAA_MALLOC(sizeof(kaa_access_point_t));
        KAA_RETURN_IF_NIL(new_access_point, KAA_ERR_NOMEM);

        error_code = kaa_platform_message_read_aligned(reader
                                                     , connection_data
                                                     , connection_data_len);
        if (error_code) {
            KAA_FREE(connection_data);
            KAA_FREE(new_access_point);
            KAA_LOG_ERROR(self->logger, error_code, "Failed to read connection data");
            return error_code;
        }

        new_access_point->id = access_point_id;
        new_access_point->connection_data = connection_data;
        new_access_point->connection_data_len = connection_data_len;

        error_code = add_access_point(self, &protocol_info, new_access_point);
        if (error_code) {
            KAA_LOG_WARN(self->logger, error_code, "Failed to add new access point "
                    "to channel (id=%X, version=%u)", protocol_info.id, protocol_info.version);
            KAA_FREE(new_access_point->connection_data);
            KAA_FREE(new_access_point);
        }
    }

    kaa_list_t *channel_it = self->channel_access_points;
    while (channel_it) {
        kaa_channel_access_points_t *channel = kaa_list_get_data(channel_it);
        KAA_LOG_DEBUG(self->logger, 0, "%zu access point(s) for channel (id=%X, version=%u)",
                kaa_list_get_size(channel->access_points), channel->protocol_info.id, channel->protocol_info.version)

        channel_it = kaa_list_next(channel_it);
    }

    return error_code;
}


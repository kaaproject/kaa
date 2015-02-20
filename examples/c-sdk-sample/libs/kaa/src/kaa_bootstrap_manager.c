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
#include <stdbool.h>
#include "platform/stdio.h"
#include "kaa_defaults.h"
#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"
#include "collections/kaa_list.h"
#include "kaa_bootstrap_manager.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_channel_manager.h"



extern kaa_transport_channel_interface_t *kaa_channel_manager_get_transport_channel(kaa_channel_manager_t *self
                                                                                  , kaa_service_t service_type);

extern kaa_error_t kaa_channel_manager_on_new_access_point(kaa_channel_manager_t *self
                                                         , kaa_transport_protocol_id_t *protocol_id
                                                         , kaa_server_type_t server_type
                                                         , kaa_access_point_t *access_point);



typedef struct {
    kaa_transport_protocol_id_t    protocol_id;
    size_t                         index;
} kaa_bootstrap_access_points_t;

typedef struct {
    kaa_transport_protocol_id_t    protocol_id;
    kaa_list_t                     *access_points;
    kaa_list_t                     *current_access_points;
} kaa_operations_access_points_t;

struct kaa_bootstrap_manager_t {
    kaa_channel_manager_t    *channel_manager;
    kaa_list_t               *operations_access_points;
    kaa_list_t               *bootstrap_access_points;
    kaa_logger_t             *logger;
};



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

static void destroy_operations_access_points(void *data)
{
    KAA_RETURN_IF_NIL(data,);
    kaa_operations_access_points_t *operations_access_points =
                            (kaa_operations_access_points_t *)data;
    kaa_list_destroy(operations_access_points->access_points, destroy_access_point);
    KAA_FREE(operations_access_points);
}

static bool find_operations_access_points(void *data, void *context)
{
    KAA_RETURN_IF_NIL2(data, context, false);
    kaa_transport_protocol_id_t *matcher = (kaa_transport_protocol_id_t *) context;
    kaa_transport_protocol_id_t *source = &(((kaa_operations_access_points_t *)data)->protocol_id);
    return kaa_transport_protocol_id_equals(matcher, source);
}

static bool find_bootstrap_access_points(void *data, void *context)
{
    KAA_RETURN_IF_NIL2(data, context, false);
    kaa_transport_protocol_id_t *matcher = (kaa_transport_protocol_id_t *) context;
    kaa_transport_protocol_id_t *source = &(((kaa_bootstrap_access_points_t *)data)->protocol_id);
    return kaa_transport_protocol_id_equals(matcher, source);
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

static kaa_error_t kaa_bootstrap_manager_on_server_sync(kaa_bootstrap_manager_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_access_point_t *access_point = NULL;
    kaa_operations_access_points_t *operations_access_points;
    kaa_list_t *channel_it = self->operations_access_points;

    while (channel_it) {
        operations_access_points = kaa_list_get_data(channel_it);
        access_point = (kaa_access_point_t *)kaa_list_get_data(operations_access_points->access_points);

        kaa_channel_manager_on_new_access_point(self->channel_manager
                                              , &operations_access_points->protocol_id
                                              , KAA_SERVER_OPERATIONS
                                              , access_point);

        channel_it = kaa_list_next(channel_it);
    }

    return KAA_ERR_NONE;
}

static kaa_error_t add_operations_access_point(kaa_bootstrap_manager_t *self
                                             , kaa_transport_protocol_id_t *protocol_id
                                             , kaa_access_point_t *access_point)
{
    KAA_RETURN_IF_NIL2(protocol_id, access_point, KAA_ERR_BADPARAM);

    kaa_list_t *channel_it = kaa_list_find_next(self->operations_access_points
                                              , find_operations_access_points
                                              , protocol_id);

    if (channel_it) {
        kaa_operations_access_points_t *operations_access_points = kaa_list_get_data(channel_it);
        KAA_RETURN_IF_NIL(operations_access_points, KAA_ERR_BADDATA);

        kaa_list_t *access_point_it = kaa_list_push_front(operations_access_points->access_points
                                                        , access_point);
        KAA_RETURN_IF_NIL(access_point_it, KAA_ERR_NOMEM);

        operations_access_points->current_access_points =
                operations_access_points->access_points = access_point_it;
    } else {
        kaa_operations_access_points_t *operations_access_points =
                (kaa_operations_access_points_t *)KAA_MALLOC(sizeof(kaa_operations_access_points_t));
        KAA_RETURN_IF_NIL(operations_access_points, KAA_ERR_NOMEM);

        operations_access_points->protocol_id = *protocol_id;
        operations_access_points->current_access_points =
                operations_access_points->access_points = kaa_list_create(access_point);

        if (!operations_access_points->access_points) {
            KAA_FREE(operations_access_points);
            KAA_LOG_WARN(self->logger, KAA_ERR_NOMEM, "Failed to add new access point: "
                                "id=0x%08X, protocol: id=0x%08X, version=%u"
                                , access_point->id, protocol_id->id, protocol_id->version);
            return KAA_ERR_NOMEM;
        }

        kaa_list_t *operations_access_points_it = self->operations_access_points ?
                        kaa_list_push_front(self->operations_access_points, operations_access_points) :
                        kaa_list_create(operations_access_points);

        if (!operations_access_points_it) {
            KAA_FREE(operations_access_points);
            KAA_LOG_WARN(self->logger, KAA_ERR_NOMEM, "Failed to add new access point: "
                                "id=0x%08X, protocol: id=0x%08X, version=%u"
                                , access_point->id, protocol_id->id, protocol_id->version);
            return KAA_ERR_NOMEM;
        }

        self->operations_access_points = operations_access_points_it;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received new Operations access point [0x%08X] "
                        "(protocol: id=0x%08X, version=%u)"
                        , access_point->id, protocol_id->id, protocol_id->version);

    return KAA_ERR_NONE;
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
    (*bootstrap_manager_p)->operations_access_points = NULL;
    (*bootstrap_manager_p)->bootstrap_access_points  = NULL;

    return KAA_ERR_NONE;
}

void kaa_bootstrap_manager_destroy(kaa_bootstrap_manager_t *self)
{
    KAA_RETURN_IF_NIL(self,);
    kaa_list_destroy(self->bootstrap_access_points, NULL);
    kaa_list_destroy(self->operations_access_points, destroy_operations_access_points);
    KAA_FREE(self);
}

kaa_access_point_t *kaa_bootstrap_manager_get_operations_access_point(kaa_bootstrap_manager_t *self
                                                                    , kaa_transport_protocol_id_t *protocol_id)
{
    KAA_RETURN_IF_NIL2(self, protocol_id, NULL);

    kaa_list_t *operations_access_points_it = kaa_list_find_next(self->operations_access_points
                                                              , &find_operations_access_points
                                                              , protocol_id);
    KAA_RETURN_IF_NIL(operations_access_points_it, NULL);

    kaa_operations_access_points_t *operations_access_points =
            (kaa_operations_access_points_t *)kaa_list_get_data(operations_access_points_it);
    KAA_RETURN_IF_NIL(operations_access_points, NULL);

    return (kaa_access_point_t *)kaa_list_get_data(operations_access_points->current_access_points);
}

static kaa_error_t get_next_bootstrap_access_point_index(kaa_transport_protocol_id_t *protocol_id
                                                       , size_t index_from
                                                       , size_t *next_index)
{
    KAA_RETURN_IF_NIL2(protocol_id,next_index, KAA_ERR_BADPARAM);

    if (index_from < KAA_BOOTSTRAP_ACCESS_POINT_COUNT) {
        size_t i = index_from;
        for (; i < KAA_BOOTSTRAP_ACCESS_POINT_COUNT; ++i) {
            if (kaa_transport_protocol_id_equals(&(KAA_BOOTSTRAP_ACCESS_POINTS[i].protocol_id), protocol_id)) {
                *next_index = i;
                return KAA_ERR_NONE;
            }
        }
    }

    return KAA_ERR_NOT_FOUND;
}

static kaa_error_t add_bootstrap_access_point(kaa_bootstrap_manager_t *self
                                            , size_t index)
{
    if (index < KAA_BOOTSTRAP_ACCESS_POINT_COUNT) {
        kaa_bootstrap_access_points_t *bootstrap_access_point =
                    (kaa_bootstrap_access_points_t *)KAA_MALLOC(sizeof(kaa_bootstrap_access_points_t));
        KAA_RETURN_IF_NIL(bootstrap_access_point, KAA_ERR_NOMEM);

        bootstrap_access_point->protocol_id = KAA_BOOTSTRAP_ACCESS_POINTS[index].protocol_id;
        bootstrap_access_point->index = index;

        kaa_list_t *bootstrap_access_point_it = self->bootstrap_access_points ?
                kaa_list_push_front(self->bootstrap_access_points, bootstrap_access_point) :
                kaa_list_create(bootstrap_access_point);

        if (!bootstrap_access_point_it) {
            KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to allocate memory "
                                                "for Bootstrap access point info");
            KAA_FREE(bootstrap_access_point);
            return KAA_ERR_NOMEM;
        }

        self->bootstrap_access_points = bootstrap_access_point_it;
        return KAA_ERR_NONE;
    }

    return KAA_ERR_BADDATA;
}

kaa_access_point_t *kaa_bootstrap_manager_get_bootstrap_access_point(kaa_bootstrap_manager_t *self
                                                                   , kaa_transport_protocol_id_t *protocol_id)
{
    KAA_RETURN_IF_NIL2(self, protocol_id, NULL);

    kaa_list_t *bootstrap_access_points_it = kaa_list_find_next(self->operations_access_points
                                                              , &find_bootstrap_access_points
                                                              , protocol_id);

    size_t index;
    if (bootstrap_access_points_it) {
        index = ((kaa_bootstrap_access_points_t *)kaa_list_get_data(bootstrap_access_points_it))->index;
    } else {
        kaa_error_t error_code = get_next_bootstrap_access_point_index(protocol_id
                                                                     , 0
                                                                     , &index);
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Could not find Bootstrap access point "
                            "(protocol: id=0x%08X, version=%u)", protocol_id->id, protocol_id->version);
            return NULL;
        }

        error_code = add_bootstrap_access_point(self, index);
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to add Bootstrap access point index"
                            "(protocol: id=0x%08X, version=%u)", protocol_id->id, protocol_id->version);
            return NULL;
        }
    }

    return (kaa_access_point_t *)&(KAA_BOOTSTRAP_ACCESS_POINTS[index].access_point);
}

kaa_error_t kaa_bootstrap_manager_handle_server_sync(kaa_bootstrap_manager_t *self
                                                   , kaa_platform_message_reader_t *reader
                                                   , uint32_t extension_options
                                                   , size_t extension_length)
{
    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);

    kaa_list_destroy(self->operations_access_points, destroy_operations_access_points);
    self->operations_access_points = NULL;

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
    kaa_transport_protocol_id_t protocol_id;
    uint16_t connection_data_len;

    while (access_point_count--) {
        error_code = kaa_platform_message_read(reader, &access_point_id, sizeof(uint32_t));
        KAA_RETURN_IF_ERR(error_code);
        access_point_id = KAA_NTOHL(access_point_id);

        error_code = kaa_platform_message_read(reader, &protocol_id.id, sizeof(uint32_t));
        KAA_RETURN_IF_ERR(error_code);
        protocol_id.id = KAA_NTOHL(protocol_id.id);

        error_code = kaa_platform_message_read(reader, &protocol_id.version, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);
        protocol_id.version = KAA_NTOHS(protocol_id.version);

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

        error_code = add_operations_access_point(self, &protocol_id, new_access_point);
        if (error_code) {
            KAA_LOG_WARN(self->logger, error_code, "Failed to add new access point "
                    "to channel (protocol: id=0x%08X, version=%u)", protocol_id.id, protocol_id.version);
            KAA_FREE(new_access_point->connection_data);
            KAA_FREE(new_access_point);
        }
    }

    kaa_bootstrap_manager_on_server_sync(self);

    return error_code;
}

kaa_error_t kaa_bootstrap_manager_on_access_point_failed(kaa_bootstrap_manager_t *self
                                                       , kaa_transport_protocol_id_t *protocol_id
                                                       , kaa_server_type_t type)
{
    KAA_RETURN_IF_NIL2(self, protocol_id, KAA_ERR_BADPARAM);

    kaa_access_point_t *access_point = NULL;

    if (type == KAA_SERVER_BOOTSTRAP) {
        kaa_list_t *bootstrap_access_points_it = kaa_list_find_next(self->bootstrap_access_points
                                                                  , &find_bootstrap_access_points
                                                                  , protocol_id);

        size_t index_from = 0;
        if (bootstrap_access_points_it) {
            index_from = ((kaa_bootstrap_access_points_t *)kaa_list_get_data(bootstrap_access_points_it))->index + 1;
        }

        size_t next_index;
        kaa_error_t error_code = get_next_bootstrap_access_point_index(protocol_id, index_from, &next_index);

        if (!error_code) {
            access_point = (kaa_access_point_t *)&(KAA_BOOTSTRAP_ACCESS_POINTS[next_index].access_point);

            if (bootstrap_access_points_it) {
                ((kaa_bootstrap_access_points_t *)kaa_list_get_data(bootstrap_access_points_it))->index = next_index;
            } else {
                error_code = add_bootstrap_access_point(self, next_index);
                KAA_RETURN_IF_ERR(error_code);
            }
        }
    } else {
        kaa_list_t *operations_access_points_it = kaa_list_find_next(self->operations_access_points
                                                                   , &find_operations_access_points
                                                                   , protocol_id);
        KAA_RETURN_IF_NIL(operations_access_points_it, KAA_ERR_NOT_FOUND);

        kaa_operations_access_points_t *operations_access_points =
                (kaa_operations_access_points_t *)kaa_list_get_data(operations_access_points_it);
        KAA_RETURN_IF_NIL(operations_access_points, KAA_ERR_NOT_FOUND);

        operations_access_points->current_access_points =
                kaa_list_next(operations_access_points->current_access_points);
        access_point = (kaa_access_point_t *)kaa_list_get_data(
                            operations_access_points->current_access_points);
    }

    if (access_point) {
        kaa_channel_manager_on_new_access_point(self->channel_manager
                                              , protocol_id
                                              , type
                                              , access_point);
    } else if (type == KAA_SERVER_OPERATIONS) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Could not find next Operations access point "
                "(protocol: id=0x%08X, version=%u). Going to sync..."
                , protocol_id->id, protocol_id->version);
        do_sync(self);
    } else if (type == KAA_SERVER_BOOTSTRAP) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Could not find next Bootstrap access point "
                "(protocol: id=0x%08X, version=%u). Try to sync later..."
                , protocol_id->id, protocol_id->version);
    }

    return KAA_ERR_NONE;
}


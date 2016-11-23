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

#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <stdint.h>
#include <sys/types.h>
#include "platform/stdio.h"
#include "platform/sock.h"
#include "kaa_defaults.h"
#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"
#include "collections/kaa_list.h"
#include "kaa_bootstrap_manager.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_channel_manager.h"
#include "platform/ext_kaa_failover_strategy.h"
#include "kaa_protocols/kaa_tcp/kaatcp_common.h"

typedef struct {
    kaa_transport_protocol_id_t    protocol_id;
    size_t                         index;
} kaa_bootstrap_access_points_t;

typedef struct {
    kaa_transport_protocol_id_t    protocol_id;
    kaa_list_t                     *access_points;
    kaa_list_node_t                *current_access_points;
} kaa_operations_access_points_t;

typedef struct {
    bool                        is_failover;
    kaa_access_point_t          *acess_point;
    kaa_server_type_t           server;
    kaa_transport_protocol_id_t protocol_id;
    kaa_time_t                  next_execution_time;
    kaa_failover_reason         reason;
} failover_meta_info;

struct kaa_bootstrap_manager_t {
    kaa_channel_manager_t    *channel_manager;
    kaa_list_t               *operations_access_points;
    kaa_list_t               *bootstrap_access_points;
    kaa_context_t            *kaa_context;
    kaa_logger_t             *logger;
    kaa_time_t                next_operations_request_time;
    failover_meta_info        failover_meta_info;
};

static kaa_extension_id bootstrap_sync_services[1] = { KAA_EXTENSION_BOOTSTRAP };

kaa_error_t kaa_extension_bootstrap_init(kaa_context_t *kaa_context, void **context)
{
    kaa_error_t result = kaa_bootstrap_manager_create((kaa_bootstrap_manager_t **)context, kaa_context);
    kaa_context->bootstrap_manager = *context;
    return result;
}

kaa_error_t kaa_extension_bootstrap_deinit(void *context)
{
    kaa_bootstrap_manager_destroy(context);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_extension_bootstrap_request_get_size(void *context, size_t *expected_size)
{
    return kaa_channel_manager_bootstrap_request_get_size(
            ((kaa_bootstrap_manager_t *)context)->channel_manager, expected_size);
}

kaa_error_t kaa_extension_bootstrap_request_serialize(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *need_resync)
{
    (void)request_id;

    // TODO(KAA-982): Use asserts
    if (!context || !size || !need_resync) {
        return KAA_ERR_BADPARAM;
    }

    *need_resync = true;

    size_t size_needed;
    kaa_error_t error = kaa_channel_manager_bootstrap_request_get_size(
            ((kaa_bootstrap_manager_t *)context)->channel_manager, &size_needed);
    if (error) {
        return error;
    }

    if (!buffer || *size < size_needed) {
        *size = size_needed;
        return KAA_ERR_BUFFER_IS_NOT_ENOUGH;
    }

    *size = size_needed;

    kaa_platform_message_writer_t writer = KAA_MESSAGE_WRITER(buffer, *size);
    error = kaa_bootstrap_manager_bootstrap_request_serialize(context, &writer);
    if (error) {
        return error;
    }

    *size = writer.current - buffer;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_extension_bootstrap_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{
    (void)request_id;

    // TODO(KAA-982): Use asserts
    if (!context || !buffer) {
        return KAA_ERR_BADPARAM;
    }

    kaa_platform_message_reader_t reader = KAA_MESSAGE_READER(buffer, size);
    return kaa_bootstrap_manager_handle_server_sync(context, &reader, extension_options, size);
}

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
                            self->channel_manager, KAA_EXTENSION_BOOTSTRAP);
    if (channel) {
        channel->sync_handler(channel->context, bootstrap_sync_services, 1);
        return KAA_ERR_NONE;
    }

    return KAA_ERR_NOT_FOUND;
}

static kaa_error_t kaa_bootstrap_manager_on_server_sync(kaa_bootstrap_manager_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_list_node_t *channel_it = kaa_list_begin(self->operations_access_points);
    while (channel_it) {
        kaa_operations_access_points_t *operations_access_points = kaa_list_get_data(channel_it);
        kaa_access_point_t *access_point = kaa_list_get_data(
                kaa_list_begin(operations_access_points->access_points));

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

    kaa_list_node_t *channel_it = kaa_list_find_next(kaa_list_begin(self->operations_access_points)
                                                   , find_operations_access_points
                                                   , protocol_id);

    if (channel_it) {
        kaa_operations_access_points_t *operations_access_points = kaa_list_get_data(channel_it);
        kaa_list_node_t *access_point_it = kaa_list_push_front(operations_access_points->access_points
                                                             , access_point);
        KAA_RETURN_IF_NIL(access_point_it, KAA_ERR_NOMEM);

        operations_access_points->current_access_points = access_point_it;
    } else {
        kaa_operations_access_points_t *operations_access_points =
                (kaa_operations_access_points_t *)KAA_CALLOC(1, sizeof(kaa_operations_access_points_t));
        KAA_RETURN_IF_NIL(operations_access_points, KAA_ERR_NOMEM);

        operations_access_points->protocol_id = *protocol_id;
        operations_access_points->access_points = kaa_list_create();
        operations_access_points->current_access_points = kaa_list_push_front(operations_access_points->access_points
                                                                            , access_point);

        if (!operations_access_points->access_points || !operations_access_points->current_access_points) {
            destroy_operations_access_points(operations_access_points);
            KAA_LOG_WARN(self->logger, KAA_ERR_NOMEM, "Failed to add new access point: "
                                "id=0x%08X, protocol: id=0x%08X, version=%u"
                                , access_point->id, protocol_id->id, protocol_id->version);
            return KAA_ERR_NOMEM;
        }

        kaa_list_node_t *operations_access_points_it = kaa_list_push_front(self->operations_access_points, operations_access_points);
        if (!operations_access_points_it) {
            destroy_operations_access_points(operations_access_points);
            KAA_LOG_WARN(self->logger, KAA_ERR_NOMEM, "Failed to add new access point: "
                                "id=0x%08X, protocol: id=0x%08X, version=%u"
                                , access_point->id, protocol_id->id, protocol_id->version);
            return KAA_ERR_NOMEM;
        }
    }

    return KAA_ERR_NONE;
}

/** @deprecated Use kaa_extension_manager_init(). */
kaa_error_t kaa_bootstrap_manager_create(kaa_bootstrap_manager_t **bootstrap_manager_p, kaa_context_t *kaa_context)
{
    KAA_RETURN_IF_NIL2(bootstrap_manager_p, kaa_context, KAA_ERR_BADPARAM);

    *bootstrap_manager_p = (kaa_bootstrap_manager_t*) KAA_CALLOC(1, sizeof(kaa_bootstrap_manager_t));
    KAA_RETURN_IF_NIL(*bootstrap_manager_p, KAA_ERR_NOMEM);

    (*bootstrap_manager_p)->channel_manager = kaa_context->channel_manager;
    (*bootstrap_manager_p)->logger = kaa_context->logger;
    (*bootstrap_manager_p)->kaa_context = kaa_context;

    (*bootstrap_manager_p)->bootstrap_access_points = kaa_list_create();
    KAA_RETURN_IF_NIL((*bootstrap_manager_p)->bootstrap_access_points, KAA_ERR_NOMEM);

    (*bootstrap_manager_p)->operations_access_points = kaa_list_create();
    KAA_RETURN_IF_NIL((*bootstrap_manager_p)->operations_access_points, KAA_ERR_NOMEM);

    return KAA_ERR_NONE;
}

/** @deprecated Use kaa_extension_manager_deinit(). */
void kaa_bootstrap_manager_destroy(kaa_bootstrap_manager_t *self)
{
    KAA_RETURN_IF_NIL(self,);
    kaa_list_destroy(self->bootstrap_access_points, NULL);
    kaa_list_destroy(self->operations_access_points, destroy_operations_access_points);
    KAA_FREE(self);
}


static kaa_error_t kaa_bootstrap_manager_schedule_failover(kaa_bootstrap_manager_t *self, kaa_access_point_t* current_access_point, kaa_access_point_t* next_access_point,
                                             kaa_transport_protocol_id_t *protocol_id, kaa_server_type_t type, kaa_failover_reason reason)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    kaa_failover_decision_t decision = kaa_failover_strategy_on_failover(self->kaa_context->failover_strategy, reason);
    switch (decision.action) {
    case KAA_NOOP:
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Nothing to be done...");
        return KAA_ERR_NONE;
    case KAA_RETRY:
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to retry in %u seconds...", decision.retry_period);
        self->failover_meta_info.acess_point = current_access_point;
        break;
    case KAA_USE_NEXT_BOOTSTRAP:
    case KAA_USE_NEXT_OPERATIONS:
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to retry with another access point in %u seconds...", decision.retry_period);
        self->failover_meta_info.acess_point = next_access_point;
        break;
    case KAA_STOP_APP:
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Stopping SDK according to the failover strategy...");
        return KAA_ERR_SDK_STOP;
    }
    self->failover_meta_info.next_execution_time = KAA_TIME() + decision.retry_period;
    self->failover_meta_info.server = type;
    if (protocol_id) {
        self->failover_meta_info.protocol_id = *protocol_id;
    }
    self->failover_meta_info.reason = reason;
    self->failover_meta_info.is_failover = true;

    return KAA_ERR_NONE;
}

kaa_access_point_t *kaa_bootstrap_manager_get_operations_access_point(kaa_bootstrap_manager_t *self
                                                                    , kaa_transport_protocol_id_t *protocol_id)
{
    KAA_RETURN_IF_NIL2(self, protocol_id, NULL);

    kaa_list_node_t *operations_access_points_it = kaa_list_find_next(kaa_list_begin(self->operations_access_points)
                                                                     , &find_operations_access_points
                                                                     , protocol_id);
    KAA_RETURN_IF_NIL(operations_access_points_it, NULL);

    kaa_operations_access_points_t *operations_access_points =
            (kaa_operations_access_points_t *)kaa_list_get_data(operations_access_points_it);
    return (kaa_access_point_t *)kaa_list_get_data(operations_access_points->current_access_points);
}

static kaa_error_t get_next_bootstrap_access_point_index(kaa_transport_protocol_id_t *protocol_id
                                                       , size_t index_from
                                                       , size_t *next_index, bool *execute_failover)
{
    KAA_RETURN_IF_NIL4(protocol_id, next_index, execute_failover, KAA_BOOTSTRAP_ACCESS_POINT_COUNT, KAA_ERR_BADPARAM);

    size_t i = index_from;
    if (index_from < KAA_BOOTSTRAP_ACCESS_POINT_COUNT) {
        for (; i < KAA_BOOTSTRAP_ACCESS_POINT_COUNT; ++i) {
            if (kaa_transport_protocol_id_equals(&(KAA_BOOTSTRAP_ACCESS_POINTS[i].protocol_id), protocol_id)) {
                *next_index = i;
                return KAA_ERR_NONE;
            }
        }
    }
    i = 0; // from the beginning
    for (; i < KAA_BOOTSTRAP_ACCESS_POINT_COUNT; ++i) {
         if (kaa_transport_protocol_id_equals(&(KAA_BOOTSTRAP_ACCESS_POINTS[i].protocol_id), protocol_id)) {
             *next_index = i;
             *execute_failover = true; //execute failover
             return KAA_ERR_NONE;
         }
    }

    return KAA_ERR_NOT_FOUND;
}

static kaa_error_t add_bootstrap_access_point(kaa_bootstrap_manager_t *self
                                            , size_t index)
{
    if (index >= KAA_BOOTSTRAP_ACCESS_POINT_COUNT)
        return KAA_ERR_BADDATA;

    kaa_bootstrap_access_points_t *bootstrap_access_point =
                (kaa_bootstrap_access_points_t *)KAA_MALLOC(sizeof(kaa_bootstrap_access_points_t));
    KAA_RETURN_IF_NIL(bootstrap_access_point, KAA_ERR_NOMEM);

    bootstrap_access_point->protocol_id = KAA_BOOTSTRAP_ACCESS_POINTS[index].protocol_id;
    bootstrap_access_point->index = index;

    kaa_list_node_t *bootstrap_access_point_it = kaa_list_push_front(self->bootstrap_access_points, bootstrap_access_point);
    if (!bootstrap_access_point_it) {
        KAA_FREE(bootstrap_access_point);
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to allocate memory for Bootstrap access point info");
        return KAA_ERR_NOMEM;
    }

    return KAA_ERR_NONE;
}

kaa_access_point_t *kaa_bootstrap_manager_get_bootstrap_access_point(kaa_bootstrap_manager_t *self
                                                                   , kaa_transport_protocol_id_t *protocol_id)
{
    KAA_RETURN_IF_NIL2(self, protocol_id, NULL);

    kaa_list_node_t *bootstrap_access_points_it = kaa_list_find_next(kaa_list_begin(self->bootstrap_access_points)
                                                                   , &find_bootstrap_access_points
                                                                   , protocol_id);

    size_t index;
    bool execute_failover = false;

    if (bootstrap_access_points_it) {
        index = ((kaa_bootstrap_access_points_t *)kaa_list_get_data(bootstrap_access_points_it))->index;
    } else {
        kaa_error_t error_code = get_next_bootstrap_access_point_index(protocol_id, 0, &index, &execute_failover);
        if (error_code) {
            KAA_LOG_FATAL(self->logger, error_code, "Error: No bootstrap services been found. Please regenerate SDK.");
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
                                                   , uint16_t extension_options
                                                   , size_t extension_length)
{
    // Only used for logging
    (void)extension_options;
    (void)extension_length;

    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);
    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received bootstrap service sync: options %u, payload size %u", extension_options, extension_length);

    kaa_list_clear(self->operations_access_points, destroy_operations_access_points);


    uint16_t request_id;
    kaa_error_t error_code = kaa_platform_message_read(reader, &request_id, sizeof(uint16_t));
    KAA_RETURN_IF_ERR(error_code);
    request_id = KAA_NTOHS(request_id);

    uint16_t access_point_count;
    error_code = kaa_platform_message_read(reader, &access_point_count, sizeof(uint16_t));
    KAA_RETURN_IF_ERR(error_code);
    access_point_count = KAA_NTOHS(access_point_count);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received %u access points (request_id %u)"
                                                        , access_point_count, request_id);

    if (!access_point_count) {
        kaa_transport_channel_interface_t *channel = kaa_channel_manager_get_transport_channel(self->channel_manager, KAA_EXTENSION_BOOTSTRAP);
        kaa_transport_protocol_id_t protocol_id  = {0, 0};
        error_code = channel->get_protocol_id(channel->context, &protocol_id);
        KAA_RETURN_IF_ERR(error_code);
        kaa_access_point_t *acc_point = kaa_bootstrap_manager_get_bootstrap_access_point(self, &protocol_id);

        if (!acc_point) {
            return KAA_ERR_SDK_STOP;
        }

        kaa_list_node_t *bootstrap_access_points_it = kaa_list_find_next(kaa_list_begin(self->bootstrap_access_points)
                                                                       , &find_bootstrap_access_points
                                                                       , &protocol_id);
        size_t next_index = 0;
        bool execute_failover = false;
        get_next_bootstrap_access_point_index(&protocol_id, ((kaa_bootstrap_access_points_t *)
                                              kaa_list_get_data(bootstrap_access_points_it))->index + 1, &next_index, &execute_failover);

        kaa_access_point_t *next_acc_point = (kaa_access_point_t *)&(KAA_BOOTSTRAP_ACCESS_POINTS[next_index].access_point);

        error_code = kaa_bootstrap_manager_schedule_failover(self->kaa_context->bootstrap_manager, acc_point, next_acc_point, &protocol_id, KAA_SERVER_BOOTSTRAP, KAA_NO_OPERATION_SERVERS_RECEIVED);
        if (error_code) {
            return error_code;
        }

        return KAA_ERR_EVENT_NOT_ATTACHED;
    }

    kaa_transport_protocol_id_t protocol_id;

    while (access_point_count--) {
        kaa_access_point_t *new_access_point = (kaa_access_point_t *)KAA_MALLOC(sizeof(kaa_access_point_t));
        KAA_RETURN_IF_NIL(new_access_point, KAA_ERR_NOMEM);

        error_code = kaa_platform_message_read(reader, &new_access_point->id, sizeof(uint32_t));
        KAA_RETURN_IF_ERR(error_code);
        new_access_point->id = KAA_NTOHL(new_access_point->id);

        error_code = kaa_platform_message_read(reader, &protocol_id.id, sizeof(uint32_t));
        KAA_RETURN_IF_ERR(error_code);
        protocol_id.id = KAA_NTOHL(protocol_id.id);

        error_code = kaa_platform_message_read(reader, &protocol_id.version, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);
        protocol_id.version = KAA_NTOHS(protocol_id.version);

        error_code = kaa_platform_message_read(reader, &new_access_point->connection_data_len, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);
        new_access_point->connection_data_len = KAA_NTOHS(new_access_point->connection_data_len);

        new_access_point->connection_data = (char *)KAA_MALLOC(new_access_point->connection_data_len);

        if (!new_access_point->connection_data || !new_access_point->connection_data_len) {
            KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to allocate buffer for connection data, size %u"
                                                                        , new_access_point->connection_data_len);
            destroy_access_point(new_access_point);
            return KAA_ERR_NOMEM;
        }

        error_code = kaa_platform_message_read_aligned(reader
                                                     , new_access_point->connection_data
                                                     , new_access_point->connection_data_len);
        if (error_code) {
            destroy_access_point(new_access_point);
            KAA_LOG_ERROR(self->logger, error_code, "Failed to read connection data");
            return error_code;
        }

        error_code = add_operations_access_point(self, &protocol_id, new_access_point);
        if (error_code) {
            destroy_access_point(new_access_point);
            KAA_LOG_WARN(self->logger, error_code, "Failed to add new access point "
                    "to channel (protocol: id=0x%08X, version=%u)", protocol_id.id, protocol_id.version);
        }
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Added access point: access point id '%u', protocol id '0x%08X', protocol version '%u', connection data length '%u'"
                    , new_access_point->id, protocol_id.id, protocol_id.version, new_access_point->connection_data_len);
    }

    kaa_bootstrap_manager_on_server_sync(self);
    self->next_operations_request_time = 0;

    return error_code;
}

// TODO(KAA-1089): Remove weak linkage
__attribute__((weak))
kaa_error_t kaa_bootstrap_manager_on_access_point_failed(kaa_bootstrap_manager_t *self,
                                                         kaa_transport_protocol_id_t *protocol_id,
                                                         kaa_server_type_t type,
                                                         kaa_failover_reason reason_code)
{
    KAA_RETURN_IF_NIL2(self, protocol_id, KAA_ERR_BADPARAM);

    kaa_access_point_t *access_point = NULL;
    kaa_access_point_t *prev_access_point = NULL;
    bool execute_failover = false;

    if (type == KAA_SERVER_BOOTSTRAP) {
        kaa_list_node_t *bootstrap_access_points_it = kaa_list_find_next(kaa_list_begin(self->bootstrap_access_points)
                                                                       , &find_bootstrap_access_points
                                                                       , protocol_id);

        size_t index_from = 0;
        if (bootstrap_access_points_it)
            index_from = ((kaa_bootstrap_access_points_t *)kaa_list_get_data(bootstrap_access_points_it))->index + 1;

        prev_access_point = (kaa_access_point_t *)&(KAA_BOOTSTRAP_ACCESS_POINTS[index_from].access_point);
        size_t next_index = 0;
        kaa_error_t error_code = get_next_bootstrap_access_point_index(protocol_id, index_from, &next_index, &execute_failover);

        if (error_code) {
            KAA_LOG_FATAL(self->logger, error_code, "Error: No bootstrap services been found. Please regenerate SDK.");
            return KAA_ERR_SDK_STOP;
        }

        access_point = (kaa_access_point_t *)&(KAA_BOOTSTRAP_ACCESS_POINTS[next_index].access_point);

        if (bootstrap_access_points_it){
            ((kaa_bootstrap_access_points_t *)kaa_list_get_data(bootstrap_access_points_it))->index = next_index;
        } else {
            error_code = add_bootstrap_access_point(self, next_index);
            KAA_RETURN_IF_ERR(error_code);
        }
    } else {
        kaa_list_node_t *operations_access_points_it = kaa_list_find_next(kaa_list_begin(self->operations_access_points)
                                                                        , &find_operations_access_points
                                                                        , protocol_id);
        KAA_RETURN_IF_NIL(operations_access_points_it, KAA_ERR_NOT_FOUND);

        kaa_operations_access_points_t *operations_access_points =
                (kaa_operations_access_points_t *)kaa_list_get_data(operations_access_points_it);
        if (kaa_list_get_data(operations_access_points->current_access_points)) {
            prev_access_point = (kaa_access_point_t *)kaa_list_get_data(operations_access_points->current_access_points);
        }
        operations_access_points->current_access_points =
                kaa_list_next(operations_access_points->current_access_points);

        access_point = (kaa_access_point_t *)kaa_list_get_data(operations_access_points->current_access_points);

        if (!access_point)
            execute_failover = true;
    }

    if (execute_failover) {
        kaa_error_t error_code = kaa_bootstrap_manager_schedule_failover(self, prev_access_point, access_point, protocol_id,
                                                                         type, reason_code);

        if (error_code) {
            return error_code;
        }
    }

    if (access_point && !execute_failover) {
        kaa_channel_manager_on_new_access_point(self->channel_manager
                                              , protocol_id
                                              , type
                                              , access_point);
        return KAA_ERR_EVENT_NOT_ATTACHED;
    } else if (type == KAA_SERVER_OPERATIONS) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Could not find next Operations access point "
                "(protocol: id=0x%08X, version=%u)"
                , protocol_id->id, protocol_id->version);
    } else if (type == KAA_SERVER_BOOTSTRAP) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Could not find next Bootstrap access point "
                "(protocol: id=0x%08X, version=%u)"
                , protocol_id->id, protocol_id->version);
    }

    return KAA_ERR_NOT_FOUND;
}

bool kaa_bootstrap_manager_process_failover(kaa_bootstrap_manager_t *self)
{
    KAA_RETURN_IF_NIL2(self, self->kaa_context->failover_strategy, false);

    kaa_failover_decision_t decision = kaa_failover_strategy_on_failover(self->kaa_context->failover_strategy, self->failover_meta_info.reason);
    if (decision.action == KAA_NOOP) {
       return false;
    }
    kaa_error_t error_code = KAA_ERR_NONE;
    kaa_time_t current_time = KAA_TIME();
    if (!self->failover_meta_info.is_failover) {
        if (self->next_operations_request_time && current_time >= self->next_operations_request_time) {
            KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Response bootstrap time expired.");
            kaa_bootstrap_access_points_t * acc_point = (kaa_bootstrap_access_points_t *) kaa_list_get_data(kaa_list_begin(self->bootstrap_access_points));
            error_code = kaa_bootstrap_manager_on_access_point_failed(self, &acc_point->protocol_id, KAA_SERVER_BOOTSTRAP, KAA_BOOTSTRAP_SERVERS_NA);
            self->next_operations_request_time = 0;
            if (error_code == KAA_ERR_EVENT_NOT_ATTACHED) {
                do_sync(self);
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    if (current_time >= self->failover_meta_info.next_execution_time) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Executing failover strategy...");
        switch (self->failover_meta_info.server) {
        case KAA_SERVER_BOOTSTRAP:
            KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Processing failover of bootstraps");
            if (decision.action == KAA_RETRY) {
                error_code = do_sync(self);
            } else {
                kaa_channel_manager_on_new_access_point(self->channel_manager, &self->failover_meta_info.protocol_id, self->failover_meta_info.server, self->failover_meta_info.acess_point);
            }
            break;
        case KAA_SERVER_OPERATIONS: {
            KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Processing failover of operations");
            if (decision.action == KAA_RETRY) {
                kaa_channel_manager_on_new_access_point(self->channel_manager, &self->failover_meta_info.protocol_id, self->failover_meta_info.server, self->failover_meta_info.acess_point);
            } else {
                error_code = do_sync(self);
            }
            if (error_code) {
                KAA_LOG_WARN(self->logger, KAA_ERR_NONE, "Failed to connect to bootstrap");
            }
            break;
        }
        default:
            KAA_LOG_ERROR(self->logger, KAA_ERR_BADDATA, "Failed to execute failover strategy: unknown server type");
            break;
        }
        self->failover_meta_info.is_failover = false;
    }

    return true;
}

kaa_error_t kaa_bootstrap_manager_bootstrap_request_serialize(kaa_bootstrap_manager_t *self, kaa_platform_message_writer_t* writer)
{
    KAA_RETURN_IF_NIL2(self, writer, KAA_ERR_BADPARAM);
    self->next_operations_request_time = KAA_TIME() + KAA_BOOTSTRAP_RESPONSE_PERIOD;
    return kaa_channel_manager_bootstrap_request_serialize(self->channel_manager, writer);
}

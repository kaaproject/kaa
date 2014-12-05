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

#ifndef KAA_DISABLE_FEATURE_LOGGING
#include "kaa_logging.h"

#include <stddef.h>
#include <string.h>
#include "kaa_list.h"
#include "kaa_mem.h"
#include "kaa_uuid.h"
#include "kaa_common.h"
#include "kaa_status.h"
#include "kaa_channel_manager.h"

#include "avro_src/avro/io.h"

extern kaa_sync_handler_fn kaa_channel_manager_get_sync_handler(kaa_channel_manager_t *self, kaa_service_t service_type);

static const kaa_service_t logging_sync_services[1] = {KAA_SERVICE_LOGGING};
static uint32_t log_bucket_id   = 0;
struct kaa_log_collector {
    kaa_log_storage_t           *   log_storage;
    kaa_log_upload_properties_t *   log_properties;
    kaa_storage_status_t        *   log_storage_status;
    log_upload_decision_fn          is_upload_needed_fn;
    kaa_status_t                *   status;
    kaa_channel_manager_t       *   channel_manager;
};

void destroy_log_record(void *record_p)
{
    if (record_p == NULL) {
        return;
    }
    kaa_log_entry_t * record = (kaa_log_entry_t *) record_p;
    if (record->data)
    {
        kaa_bytes_destroy(record->data);
        KAA_FREE(record->data);
    }
}

kaa_error_t kaa_log_collector_create(kaa_log_collector_t ** log_collector_p, kaa_status_t *status, kaa_channel_manager_t *channel_manager)
{
    KAA_RETURN_IF_NIL(log_collector_p, KAA_ERR_BADPARAM);
    kaa_log_collector_t * collector = (kaa_log_collector_t *) KAA_MALLOC(sizeof(kaa_log_collector_t));
    KAA_RETURN_IF_NIL(collector, KAA_ERR_NOMEM);

    collector->log_storage          = NULL;
    collector->log_storage_status   = NULL;
    collector->log_properties       = NULL;
    collector->status               = status;
    collector->channel_manager      = channel_manager;

    *log_collector_p = collector;
    return KAA_ERR_NONE;
}

void kaa_log_collector_destroy(kaa_log_collector_t *self)
{
    if (self) {
        if (self->log_storage != NULL) {
            (*self->log_storage->destroy)();
        }
        KAA_FREE(self);
    }
}

kaa_error_t kaa_logging_init(
                              kaa_log_collector_t *collector
                            , kaa_log_storage_t * storage
                            , kaa_log_upload_properties_t *properties
                            , kaa_storage_status_t * status
                            , log_upload_decision_fn need_upl
                           )
{
    KAA_RETURN_IF_NIL(collector, KAA_ERR_NOT_INITIALIZED);

    if (storage == NULL || status == NULL || need_upl == NULL || properties == NULL) {
        return KAA_ERR_BADPARAM;
    }

    if (collector->log_storage != NULL) {
        (*collector->log_storage->destroy)();
    }

    collector->log_storage = storage;
    collector->log_properties = properties;
    collector->log_storage_status = status;
    collector->is_upload_needed_fn = need_upl;

    return KAA_ERR_NONE;
}

static void update_storage(kaa_log_collector_t *self)
{
    kaa_log_upload_decision_t decision = (*self->is_upload_needed_fn)(self->log_storage_status);
    switch (decision) {
        case CLEANUP:
            (*self->log_storage->shrink_to_size)(self->log_properties->max_log_storage_volume);
            break;
        case UPLOAD: {
            kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(self->channel_manager, logging_sync_services[0]);
            if (sync)
                (*sync)(logging_sync_services, 1);
            break;
        }
        default:
            break;
     }
}

kaa_error_t kaa_logging_add_record(kaa_log_collector_t *self, kaa_user_log_record_t *entry)
{
    KAA_RETURN_IF_NIL2(self, entry, KAA_ERR_BADPARAM);

    if (self->log_storage && self->is_upload_needed_fn && self->log_storage_status) {
        kaa_log_entry_t *record = kaa_log_entry_create();
        KAA_RETURN_IF_NIL(record, KAA_ERR_NOMEM);

        record->data = (kaa_bytes_t *) KAA_CALLOC(1, sizeof(kaa_bytes_t));
        if (!record->data) {
            record->destroy(record);
            return KAA_ERR_NOMEM;
        }

        record->data->destroy = kaa_data_destroy;
        record->data->size = entry->get_size(entry);

        if (record->data->size > 0) {
            record->data->buffer = (uint8_t *) KAA_MALLOC(record->data->size * sizeof(uint8_t));
        }

        if (!record->data->buffer) {
            record->destroy(record);
            return KAA_ERR_NOMEM;
        }

        avro_writer_t writer = avro_writer_memory((char *)record->data->buffer, record->data->size);
        if (!writer) {
            record->destroy(record);
            return KAA_ERR_NOMEM;
        }

        entry->serialize(writer, entry);
        avro_writer_free(writer);

        (*self->log_storage->add_log_record)(record);
        update_storage(self);
        return KAA_ERR_NONE;
    }
    return KAA_ERR_BAD_STATE;
}

static void noop(void *p) {
    (void)p;
}

kaa_error_t kaa_logging_compile_request(kaa_log_collector_t *self, kaa_log_sync_request_t ** result)
{
    KAA_RETURN_IF_NIL2(self, result, KAA_ERR_BADPARAM);
    kaa_log_sync_request_t * request = NULL;

    if (self->log_storage) {
        kaa_uuid_t uuid;

        if (!log_bucket_id && kaa_status_get_log_bucket_id(self->status, &log_bucket_id))
            return KAA_ERR_BAD_STATE;

        log_bucket_id++;
        kaa_uuid_fill(&uuid, log_bucket_id);
        kaa_list_t * logs = (*self->log_storage->get_records)(uuid, self->log_properties->max_log_block_size);
        if (logs) {
            if (kaa_status_set_log_bucket_id(self->status, log_bucket_id))
                return KAA_ERR_BAD_STATE;

            request = kaa_log_sync_request_create();
            if (!request) {
                (*self->log_storage->upload_failed)(uuid);
                return KAA_ERR_NOMEM;
            }
            request->log_entries = kaa_array_log_entry_null_union_array_branch_create();
            if (!request->log_entries) {
                request->destroy(request);
                (*self->log_storage->upload_failed)(uuid);
                return KAA_ERR_NOMEM;
            }

            request->log_entries->data = logs;
            request->log_entries->destroy = &noop;
            request->request_id = kaa_string_null_union_string_branch_create();
            if (!request->request_id) {
                request->destroy(request);
                (*self->log_storage->upload_failed)(uuid);
                return KAA_ERR_NOMEM;
            }
            kaa_uuid_to_string((char **)&request->request_id->data, &uuid);
        }
    }

    *result = request;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_logging_handle_sync(kaa_log_collector_t *self, kaa_log_sync_response_t *response)
{
    KAA_RETURN_IF_NIL2(self, response, KAA_ERR_BADPARAM);

    if (self->log_storage) {
        kaa_uuid_t uuid;
        kaa_uuid_from_string(response->request_id->data, &uuid);

        if (response->result == ENUM_SYNC_RESPONSE_RESULT_TYPE_SUCCESS) {
            (*self->log_storage->upload_succeeded)(uuid);
        } else {
            (*self->log_storage->upload_failed)(uuid);
        }
        update_storage(self);
    }

    return KAA_ERR_NONE;
}

#endif


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
#include "kaa_context.h"

#include "avro_src/avro/io.h"

static const kaa_service_t logging_sync_services[1] = {KAA_SERVICE_LOGGING};
static uint32_t log_bucket_id   = 0;
struct kaa_log_collector {
    kaa_log_storage_t           *   log_storage;
    kaa_log_upload_properties_t *   log_properties;
    kaa_storage_status_t        *   log_storage_status;
    log_upload_decision_fn          is_upload_needed_fn;
};

void destroy_log_record(void *record_p)
{
    if (record_p == NULL) {
        return;
    }
    kaa_log_entry_t * record = (kaa_log_entry_t *) record_p;
    if (record->data)
    {
        kaa_destroy_bytes(record->data);
        KAA_FREE(record->data);
    }
}

kaa_error_t kaa_create_log_collector(kaa_log_collector_t ** collector_p)
{
    if (collector_p == NULL) {
        return KAA_ERR_BADPARAM;
    }
    kaa_log_collector_t * collector = KAA_CALLOC(1, sizeof(kaa_log_collector_t));
    if (collector == NULL) {
        return KAA_ERR_NOMEM;
    }

    collector->log_storage          = NULL;
    collector->log_storage_status   = NULL;
    collector->log_properties       = NULL;

    *collector_p = collector;
    return KAA_ERR_NONE;
}

void kaa_destroy_log_collector(kaa_log_collector_t *collector)
{
    if (collector != NULL) {
        if (collector->log_storage != NULL) {
            (*collector->log_storage->destroy)();
        }
        KAA_FREE(collector);
    }
}

kaa_error_t kaa_init_log_collector(
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

static void update_storage(kaa_context_t *context)
{
    kaa_log_collector_t *collector = context->log_collector;
    kaa_log_upload_decision_t decision = (* collector->is_upload_needed_fn)(collector->log_storage_status);
    switch (decision) {
        case CLEANUP:
            (* collector->log_storage->shrink_to_size)(collector->log_properties->max_log_storage_volume);
            break;
        case UPLOAD: {
            kaa_sync_t sync = kaa_channel_manager_get_sync_handler(context, logging_sync_services[0]);
            if (sync) {
                (*sync)(1, logging_sync_services);
            }
            break;
        }
        default:
            break;
     }
}

kaa_error_t kaa_add_log_record(void *ctx, kaa_user_log_record_t *entry)
{
    KAA_RETURN_IF_NIL2(ctx, entry, KAA_ERR_BADPARAM);

    kaa_context_t *context = (kaa_context_t *)ctx;
    KAA_RETURN_IF_NIL(context->log_collector, KAA_ERR_NOT_INITIALIZED);

    kaa_log_collector_t * collector = context->log_collector;
    if (collector->log_storage && collector->is_upload_needed_fn && collector->log_storage_status) {
        kaa_log_entry_t *record = kaa_create_log_entry();

        KAA_RETURN_IF_NIL(record, KAA_ERR_NOMEM);

        record->data = KAA_CALLOC(1, sizeof(kaa_bytes_t));

        if (record->data == NULL) {
            KAA_FREE(record);
            return KAA_ERR_NOMEM;
        }

        record->data->size = entry->get_size(entry);
        record->data->buffer = KAA_CALLOC(record->data->size, sizeof(uint8_t));
        if (record->data->buffer == NULL) {
            KAA_FREE(record->data);
            KAA_FREE(record);
            return KAA_ERR_NOMEM;
        }

        avro_writer_t writer = avro_writer_memory((char *)record->data->buffer, record->data->size);
        entry->serialize(writer, entry);
        avro_writer_free(writer);

        (* collector->log_storage->add_log_record)(record);
        update_storage(context);
    }
    return KAA_ERR_NONE;
}

static void noop(void *p) {
    (void)p;
}

kaa_error_t kaa_logging_compile_request(void *ctx, kaa_log_sync_request_t ** request_p)
{
    KAA_RETURN_IF_NIL2(ctx, request_p, KAA_ERR_NOT_INITIALIZED);
    kaa_log_sync_request_t * request = NULL;
    kaa_context_t *context = (kaa_context_t *)ctx;

    KAA_RETURN_IF_NIL(context->log_collector, KAA_ERR_NOT_INITIALIZED)
    kaa_log_collector_t *collector = context->log_collector;
    if (collector->log_storage) {
        kaa_uuid_t uuid;

        if (log_bucket_id == 0) {
            log_bucket_id = kaa_status_get_log_bucket_id(context->status);
        }
        log_bucket_id++;
        kaa_uuid_fill(&uuid, log_bucket_id);
        kaa_list_t * logs = (* collector->log_storage->get_records)(uuid, collector->log_properties->max_log_block_size);
        if (logs) {
            kaa_status_set_log_bucket_id(context->status, log_bucket_id);

            request = kaa_create_log_sync_request();
            if (request == NULL) {
                (* collector->log_storage->upload_failed)(uuid);
                return KAA_ERR_NOMEM;
            }
            request->log_entries = kaa_create_array_log_entry_null_union_array_branch();
            if (request->log_entries == NULL) {
                KAA_FREE(request);
                (* collector->log_storage->upload_failed)(uuid);
                return KAA_ERR_NOMEM;
            }
            request->log_entries->data = logs;
            request->log_entries->destruct = &noop;
            request->request_id = kaa_create_string_null_union_string_branch();
            if (request->request_id == NULL) {
                KAA_FREE(request->log_entries);
                KAA_FREE(request);
                (* collector->log_storage->upload_failed)(uuid);
                return KAA_ERR_NOMEM;
            }
            kaa_uuid_to_string((char **)&request->request_id->data, &uuid);
        }
    }

    *request_p = request;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_logging_handle_sync(void *ctx, kaa_log_sync_response_t *response)
{
    KAA_RETURN_IF_NIL2(ctx, response, KAA_ERR_BADPARAM);

    kaa_context_t *context = (kaa_context_t *)ctx;
    KAA_RETURN_IF_NIL(context->log_collector, KAA_ERR_NOT_INITIALIZED);
    kaa_log_collector_t *collector = context->log_collector;

    if (collector->log_storage != NULL) {
        kaa_uuid_t uuid;
        kaa_uuid_from_string(response->request_id, &uuid);

        if (response->result == ENUM_SYNC_RESPONSE_RESULT_TYPE_SUCCESS) {
            (* collector->log_storage->upload_succeeded)(uuid);
        } else {
            (* collector->log_storage->upload_failed)(uuid);
        }
        update_storage(context);
    }

    return KAA_ERR_NONE;
}

#endif


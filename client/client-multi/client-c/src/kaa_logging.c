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
#include "collections/kaa_list.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_uuid.h"
#include "utilities/kaa_log.h"
#include "kaa_common.h"
#include "kaa_status.h"
#include "kaa_channel_manager.h"

#include "avro_src/avro/io.h"

extern kaa_sync_handler_fn kaa_channel_manager_get_sync_handler(kaa_channel_manager_t *self, kaa_service_t service_type);

static const kaa_service_t logging_sync_services[1] = {KAA_SERVICE_LOGGING};
struct kaa_log_collector {
    uint32_t                        log_bucket_id;
    kaa_log_storage_t           *   log_storage;
    kaa_log_upload_properties_t *   log_properties;
    kaa_storage_status_t        *   log_storage_status;
    log_upload_decision_fn          is_upload_needed_fn;
    kaa_status_t                *   status;
    kaa_channel_manager_t       *   channel_manager;
    kaa_logger_t                *   logger;
};

void destroy_log_record(void *record_p)
{
    if (record_p ) {
        kaa_log_entry_t * record = (kaa_log_entry_t *) record_p;
        record->destroy(record);

    }
}

kaa_error_t kaa_log_collector_create(kaa_log_collector_t ** log_collector_p, kaa_status_t *status, kaa_channel_manager_t *channel_manager, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL(log_collector_p, KAA_ERR_BADPARAM);
    kaa_log_collector_t * collector = (kaa_log_collector_t *) KAA_MALLOC(sizeof(kaa_log_collector_t));
    KAA_RETURN_IF_NIL(collector, KAA_ERR_NOMEM);

    collector->log_bucket_id        = 0;
    collector->log_storage          = NULL;
    collector->log_storage_status   = NULL;
    collector->log_properties       = NULL;
    collector->status               = status;
    collector->channel_manager      = channel_manager;
    collector->logger               = logger;

    *log_collector_p = collector;
    return KAA_ERR_NONE;
}

void kaa_log_collector_destroy(kaa_log_collector_t *self)
{
    if (self) {
        if (self->log_storage)
            (*self->log_storage->destroy)();
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
    KAA_RETURN_IF_NIL(collector, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL4(storage, status, need_upl, properties, KAA_ERR_BADPARAM);

    if (collector->log_storage)
        (*collector->log_storage->destroy)();

    collector->log_storage = storage;
    collector->log_properties = properties;
    collector->log_storage_status = status;
    collector->is_upload_needed_fn = need_upl;

    KAA_LOG_INFO(collector->logger, KAA_ERR_NONE, "Initialized log collector with: "
                "log storage {%p}, log properties {%p}, log storage status {%p}, is uploaded needed func {%p}"
            , storage, properties, status, need_upl);

    return KAA_ERR_NONE;
}

static void update_storage(kaa_log_collector_t *self)
{
    kaa_log_upload_decision_t decision = (*self->is_upload_needed_fn)(self->log_storage_status);
    switch (decision) {
        case CLEANUP:
            KAA_LOG_WARN(self->logger, KAA_ERR_NONE, "Need to cleanup log storage. Current size: %zu, Maximal volume: %zu"
                    , (*self->log_storage_status->get_total_size)()
                    , self->log_properties->max_log_storage_volume
                    );
            (*self->log_storage->shrink_to_size)(self->log_properties->max_log_storage_volume);
            break;
        case UPLOAD: {
            KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Initiating log upload...");
            kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(self->channel_manager, logging_sync_services[0]);
            if (sync)
                (*sync)(logging_sync_services, 1);
            break;
        }
        default:
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Upload shall not be triggered now.");
            break;
     }
}

kaa_error_t kaa_logging_add_record(kaa_log_collector_t *self, kaa_user_log_record_t *entry)
{
    KAA_RETURN_IF_NIL2(self, entry, KAA_ERR_BADPARAM);

    KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Adding new log record {%p}", entry);

    if (self->log_storage && self->is_upload_needed_fn && self->log_storage_status) {
        kaa_log_entry_t *record = kaa_log_entry_create();
        KAA_RETURN_IF_NIL(record, KAA_ERR_NOMEM);

        record->data = (kaa_bytes_t *) KAA_MALLOC(sizeof(kaa_bytes_t));
        if (!record->data) {
            record->destroy(record);
            return KAA_ERR_NOMEM;
        }

        record->data->destroy = &kaa_data_destroy;
        record->data->size = entry->get_size(entry);

        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Record size is %d", record->data->size);

        if (record->data->size > 0) {
            record->data->buffer = (uint8_t *) KAA_MALLOC(record->data->size * sizeof(uint8_t));
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

            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Adding serialized record to log storage {%p}", self->log_storage);
            (*self->log_storage->add_log_record)(record);
            update_storage(self);
            return KAA_ERR_NONE;
        } else {
            record->data->buffer = NULL;
            record->destroy(record);
            return KAA_ERR_BADPARAM;
        }

    }
    return KAA_ERR_BAD_STATE;
}

kaa_error_t kaa_logging_compile_request(kaa_log_collector_t *self, kaa_log_sync_request_t ** result)
{
    KAA_RETURN_IF_NIL2(self, result, KAA_ERR_BADPARAM);
    kaa_log_sync_request_t * request = NULL;

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to compile log sync request");

    if (self->log_storage && self->log_properties) {
        kaa_uuid_t uuid;

        if (!self->log_bucket_id && kaa_status_get_log_bucket_id(self->status, &self->log_bucket_id)) {
            return KAA_ERR_BAD_STATE;
        }

        ++self->log_bucket_id;
        kaa_uuid_fill(&uuid, self->log_bucket_id);

        size_t block_size = self->log_properties->max_log_block_size;
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Extracting log records... (Block size is %zu)", block_size);

        kaa_list_t * logs = NULL;
        while (block_size > 0) {
            kaa_log_entry_t * entry = (*self->log_storage->get_record)(uuid);
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Got record {%p}", entry);
            if (!entry)
                break;
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Entry size: %zu", entry->get_size(entry));
            if (logs) {
                logs = kaa_list_push_front(logs, entry);
                if (!logs) {
                    kaa_list_destroy_no_data_cleanup(logs);
                    (*self->log_storage->upload_failed)(uuid);
                    --self->log_bucket_id;
                    return KAA_ERR_NOMEM;
                }
            } else {
                logs = kaa_list_create(entry);
                if (!logs) {
                    (*self->log_storage->upload_failed)(uuid);
                    --self->log_bucket_id;
                    return KAA_ERR_NOMEM;
                }
            }
            block_size -= entry->get_size(entry);
        }

        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Extracted log records. Remaining size in block = %ld", (ssize_t)block_size);

        if (logs) {
            if (kaa_get_max_log_level(self->logger) >= KAA_LOG_LEVEL_TRACE) {
                ssize_t log_count = kaa_list_get_size(logs);
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "%ld log records will be uploaded with id %u.", log_count, self->log_bucket_id);
            }

            if (kaa_status_set_log_bucket_id(self->status, self->log_bucket_id)) {
                kaa_list_destroy_no_data_cleanup(logs);
                (*self->log_storage->upload_failed)(uuid);
                return KAA_ERR_BAD_STATE;
            }

            request = kaa_log_sync_request_create();

            if (!request) {
                if (kaa_get_max_log_level(self->logger) >= KAA_LOG_LEVEL_ERROR) {
                    char *buf = NULL;
                    kaa_uuid_to_string(&buf, &uuid);
                    KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to create log sync request object."
                                                            " Triggerinng upload_failed for log storage {%p} (%s)."
                        , self->log_storage, buf);
                    KAA_FREE(buf);
                }
                (*self->log_storage->upload_failed)(uuid);
                return KAA_ERR_NOMEM;
            }

            request->request_id = kaa_union_string_or_null_branch_0_create();
            if (!request->request_id) {
                KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to allocate buffer for \"requestId\" field in log sync request.");
                --self->log_bucket_id;
                request->destroy(request);
                return KAA_ERR_NOMEM;
            }

            char *uuid_buffer = NULL;
            kaa_uuid_to_string(&uuid_buffer, &uuid);
            request->request_id->data = kaa_string_move_create(uuid_buffer, &kaa_data_destroy);

            request->log_entries = kaa_union_array_log_entry_or_null_branch_0_create();
            if (!request->log_entries) {
                if (kaa_get_max_log_level(self->logger) >=KAA_LOG_LEVEL_ERROR) {
                    char *buf = NULL;
                    kaa_uuid_to_string(&buf, &uuid);
                    KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to fill log sync request object."
                                                            " Triggerinng upload_failed for log storage {%p} (%s)."
                        , self->log_storage, buf);
                    KAA_FREE(buf);
                }
                request->destroy(request);
                (*self->log_storage->upload_failed)(uuid);
                return KAA_ERR_NOMEM;
            }

            request->log_entries->data = logs;
            request->log_entries->destroy = &kaa_list_destroy_no_data_cleanup;
        }
    }

    *result = request;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_logging_handle_sync(kaa_log_collector_t *self, kaa_log_sync_response_t *response)
{
    KAA_RETURN_IF_NIL2(self, response, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received log sync response. Log storage is {%p}", self->log_storage);

    if (self->log_storage) {
        kaa_uuid_t uuid;
        kaa_uuid_from_string(response->request_id->data, &uuid);

        KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Log block with id %s : %s"
                , response->request_id->data
                , (response->result == ENUM_SYNC_RESPONSE_RESULT_TYPE_SUCCESS ? "uploaded succesfully." : "upload failed.")
        );

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


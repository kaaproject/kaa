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

#include <string.h>

#include "platform/stdio.h"
#include "platform/time.h"
#include "platform/ext_sha.h"
#include "kaa_logging.h"
#include "collections/kaa_list.h"
#include "kaa_common.h"
#include "kaa_status.h"
#include "kaa_channel_manager.h"
#include "kaa_platform_utils.h"
#include "kaa_platform_common.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "avro_src/avro/io.h"



#define KAA_LOGGING_RECEIVE_UPDATES_FLAG   0x01
#define KAA_MAX_PADDING_LENGTH             (KAA_ALIGNMENT - 1)



extern kaa_transport_channel_interface_t *kaa_channel_manager_get_transport_channel(kaa_channel_manager_t *self
                                                                                  , kaa_service_t service_type);



typedef enum {
    LOGGING_RESULT_SUCCESS = 0x00,
    LOGGING_RESULT_FAILURE = 0x01
} logging_sync_result_t;

typedef struct {
    uint16_t     log_bucket_id;
    kaa_time_t   timeout;
} timeout_info_t;

struct kaa_log_collector {
    uint16_t                    log_bucket_id;
    void                       *log_storage_context;
    void                       *log_upload_strategy_context;
    kaa_status_t               *status;
    kaa_channel_manager_t      *channel_manager;
    kaa_logger_t               *logger;
    kaa_list_t                 *timeouts;
    bool                        is_sync_ignored;
};



static const kaa_service_t logging_sync_services[1] = {KAA_SERVICE_LOGGING};



static kaa_error_t remember_request(kaa_log_collector_t *self, uint16_t bucket_id)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    timeout_info_t *info = (timeout_info_t *)KAA_MALLOC(sizeof(timeout_info_t));
    KAA_RETURN_IF_NIL(info, KAA_ERR_NOMEM);

    info->log_bucket_id = bucket_id;
    info->timeout = KAA_TIME() + (kaa_time_t)ext_log_upload_strategy_get_timeout(self->log_upload_strategy_context);

    kaa_list_t *it = self->timeouts ? kaa_list_push_front(self->timeouts, info) : kaa_list_create(info);
    KAA_RETURN_IF_NIL(it, KAA_ERR_NOMEM);

    self->timeouts = it;

    return KAA_ERR_NONE;
}



static bool find_by_bucket_id(void *data, void *context)
{
    KAA_RETURN_IF_NIL2(data, context, KAA_ERR_BADPARAM);
    return (((timeout_info_t *)data)->log_bucket_id = *((uint16_t *)context));
}



static kaa_error_t remove_request(kaa_log_collector_t *self, uint16_t bucket_id)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    kaa_list_remove_first(&self->timeouts, &find_by_bucket_id, &bucket_id, NULL);
    return KAA_ERR_NONE;
}



static bool is_timeout(kaa_log_collector_t *self)
{
    KAA_RETURN_IF_NIL2(self, self->timeouts, false);

    bool is_timeout = false;
    kaa_list_t *it = self->timeouts;
    kaa_time_t now = KAA_TIME();

    while (it) {
        timeout_info_t *info = (timeout_info_t *)kaa_list_get_data(it);
        if (now >= info->timeout) {
            KAA_LOG_WARN(self->logger, KAA_ERR_TIMEOUT, "Log delivery timeout occurred (bucket_id %u)", info->log_bucket_id);
            is_timeout = true;
            break;
        }
        it = kaa_list_next(it);
    }

    if (is_timeout) {
        kaa_list_t *it = self->timeouts;
        while (it) {
            timeout_info_t *info = (timeout_info_t *)kaa_list_get_data(it);
            ext_log_storage_unmark_by_bucket_id(self->log_storage_context, info->log_bucket_id);
            it = kaa_list_next(it);
        }

        kaa_list_destroy(self->timeouts, NULL);
        self->timeouts = NULL;
        ext_log_upload_strategy_on_timeout(self->log_upload_strategy_context);
    }

    return is_timeout;
}

kaa_error_t kaa_log_collector_create(kaa_log_collector_t **log_collector_p
                                   , kaa_status_t *status
                                   , kaa_channel_manager_t *channel_manager
                                   , kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL(log_collector_p, KAA_ERR_BADPARAM);
    kaa_log_collector_t * collector = (kaa_log_collector_t *) KAA_MALLOC(sizeof(kaa_log_collector_t));
    KAA_RETURN_IF_NIL(collector, KAA_ERR_NOMEM);

    collector->log_bucket_id               = 0;
    collector->log_storage_context         = NULL;
    collector->log_upload_strategy_context = NULL;
    collector->status                      = status;
    collector->channel_manager             = channel_manager;
    collector->logger                      = logger;
    collector->timeouts                    = NULL;
    collector->is_sync_ignored             = false;

    *log_collector_p = collector;
    return KAA_ERR_NONE;
}



void kaa_log_collector_destroy(kaa_log_collector_t *self)
{
    if (self) {
        ext_log_upload_strategy_destroy(self->log_upload_strategy_context);
        ext_log_storage_destroy(self->log_storage_context);
        kaa_list_destroy(self->timeouts, NULL);
        KAA_FREE(self);
    }
}



kaa_error_t kaa_logging_init(kaa_log_collector_t *self, void *log_storage_context, void *log_upload_strategy_context)
{
    KAA_RETURN_IF_NIL3(self, log_storage_context, log_upload_strategy_context, KAA_ERR_BADPARAM);

    ext_log_storage_destroy(self->log_storage_context);

    self->log_storage_context = log_storage_context;
    self->log_upload_strategy_context = log_upload_strategy_context;

    KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Initialized log collector with log storage {%p}, log upload strategy {%p}"
            , log_storage_context, log_upload_strategy_context);

    return KAA_ERR_NONE;
}



static void update_storage(kaa_log_collector_t *self)
{
    switch (ext_log_upload_strategy_decide(self->log_upload_strategy_context, self->log_storage_context)) {
        case UPLOAD:
            KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Initiating log upload...");
            kaa_transport_channel_interface_t *channel =
                    kaa_channel_manager_get_transport_channel(self->channel_manager, logging_sync_services[0]);
            if (channel)
                channel->sync_handler(channel->context, logging_sync_services, 1);
            break;
        default:
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Upload will not be triggered now.");
            break;
     }
}



kaa_error_t kaa_logging_add_record(kaa_log_collector_t *self, kaa_user_log_record_t *entry)
{
    KAA_RETURN_IF_NIL2(self, entry, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL(self->log_storage_context, KAA_ERR_NOT_INITIALIZED);

    KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Adding new log record {%p}", entry);

    kaa_log_record_t record = { NULL, entry->get_size(entry) };
    if (!record.size) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BADDATA, "Failed to add log record: serialized record size is null."
                                                                                "Maybe log record schema is empty");
        return KAA_ERR_BADDATA;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Record size is %zu", record.size);

    kaa_error_t error = ext_log_storage_allocate_log_record_buffer(self->log_storage_context, &record);
    if (error)
        return error;

    avro_writer_t writer = avro_writer_memory((char *)record.data, record.size);
    if (!writer) {
        ext_log_storage_deallocate_log_record_buffer(self->log_storage_context, &record);
        return KAA_ERR_NOMEM;
    }

    entry->serialize(writer, entry);
    avro_writer_free(writer);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Adding serialized record to the log storage");
    error = ext_log_storage_add_log_record(self->log_storage_context, &record);
    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to add log record to storage");
        ext_log_storage_deallocate_log_record_buffer(self->log_storage_context, &record);
        return error;
    }

    if (!is_timeout(self))
        update_storage(self);

    return KAA_ERR_NONE;
}



kaa_error_t kaa_logging_request_get_size(kaa_log_collector_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL(self->log_storage_context, KAA_ERR_NOT_INITIALIZED);

    size_t records_count = ext_log_storage_get_records_count(self->log_storage_context);
    size_t total_size = ext_log_storage_get_total_size(self->log_storage_context);

    self->is_sync_ignored = (!records_count || !total_size);

    if (!self->is_sync_ignored) {
        *expected_size = KAA_EXTENSION_HEADER_SIZE;
        *expected_size += sizeof(uint32_t); // request id + log records count

        size_t actual_size = records_count * sizeof(uint32_t) + records_count * KAA_MAX_PADDING_LENGTH + total_size;
        size_t bucket_size = ext_log_upload_strategy_get_bucket_size(self->log_upload_strategy_context);

        *expected_size += ((actual_size > bucket_size) ? bucket_size : actual_size);
    } else {
        *expected_size = 0;
    }

    return KAA_ERR_NONE;
}



kaa_error_t kaa_logging_request_serialize(kaa_log_collector_t *self, kaa_platform_message_writer_t *writer)
{
    KAA_RETURN_IF_NIL2(self, writer, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL(self->log_storage_context, KAA_ERR_NOT_INITIALIZED);

    if (self->is_sync_ignored) {
        return KAA_ERR_NONE;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to compile log client sync");

    kaa_platform_message_writer_t tmp_writer = *writer;

    char *extension_size_p = tmp_writer.current + sizeof(uint32_t); // Pointer to the extension size. Will be filled in later.
    kaa_error_t error = kaa_platform_message_write_extension_header(&tmp_writer
                                                                  , KAA_LOGGING_EXTENSION_TYPE
                                                                  , KAA_LOGGING_RECEIVE_UPDATES_FLAG
                                                                  , 0);
    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to write log extension header");
        return KAA_ERR_WRITE_FAILED;
    }

    if (!self->log_bucket_id)
        self->log_bucket_id = self->status->log_bucket_id;
    ++self->log_bucket_id;

    *((uint16_t *) tmp_writer.current) = KAA_HTONS(self->log_bucket_id);
    tmp_writer.current += sizeof(uint16_t);
    char *records_count_p = tmp_writer.current; // Pointer to the records count. Will be filled in later.
    tmp_writer.current += sizeof(uint16_t);

    size_t bucket_size = ext_log_upload_strategy_get_bucket_size(self->log_upload_strategy_context);
    size_t actual_size = (tmp_writer.end - tmp_writer.current);

    bucket_size = (actual_size > bucket_size ? bucket_size : actual_size);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Extracting log records... (bucket size %zu)", bucket_size);

    uint16_t records_count = 0;

    while (!error && bucket_size > sizeof(uint32_t)) {
        size_t record_len = 0;
        error = ext_log_storage_write_next_record(self->log_storage_context
                                                , tmp_writer.current + sizeof(uint32_t)
                                                , bucket_size - sizeof(uint32_t)
                                                , self->log_bucket_id
                                                , &record_len);
        switch (error) {
        case KAA_ERR_NONE:
            ++records_count;
            *((uint32_t *) tmp_writer.current) = KAA_HTONL(record_len);
            tmp_writer.current += (sizeof(uint32_t) + record_len);
            kaa_platform_message_write_alignment(&tmp_writer);
            bucket_size -= (kaa_aligned_size_get(record_len) + sizeof(uint32_t));
            break;
        case KAA_ERR_NOT_FOUND:
        case KAA_ERR_INSUFFICIENT_BUFFER:
            // These errors are normal if they appear after at least one record got serialized
            if (!records_count) {
                KAA_LOG_ERROR(self->logger, error, "Failed to write the log record");
                return error;
            }
            break;
        default:
            KAA_LOG_ERROR(self->logger, error, "Failed to write the log record");
            return error;
        }
    }

    size_t payload_size = tmp_writer.current - writer->current - KAA_EXTENSION_HEADER_SIZE;
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Extracted %u log records; total payload size %zu", records_count, payload_size);

    *((uint32_t *) extension_size_p) = KAA_HTONL(payload_size);
    *((uint16_t *) records_count_p) = KAA_HTONS(records_count);
    *writer = tmp_writer;

    error = remember_request(self, self->log_bucket_id);
    if (error) {
        KAA_LOG_WARN(self->logger, error, "Failed to remember request time stamp");
    }

    return KAA_ERR_NONE;
}



kaa_error_t kaa_logging_handle_server_sync(kaa_log_collector_t *self
                                         , kaa_platform_message_reader_t *reader
                                         , uint32_t extension_options
                                         , size_t extension_length)
{
    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);

    uint32_t delivery_status_count;
    kaa_error_t error_code = kaa_platform_message_read(reader, &delivery_status_count, sizeof(uint32_t));
    KAA_RETURN_IF_ERR(error_code);
    delivery_status_count = KAA_NTOHL(delivery_status_count);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received %lu log delivery statuses", delivery_status_count);

    uint16_t bucket_id;
    int8_t delivery_result;
    int8_t delivery_error_code;

    while (delivery_status_count-- > 0) {
        error_code = kaa_platform_message_read(reader, &bucket_id, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);
        bucket_id = KAA_NTOHS(bucket_id);

        error_code = kaa_platform_message_read(reader, &delivery_result, sizeof(int8_t));
        KAA_RETURN_IF_ERR(error_code);

        error_code = kaa_platform_message_read(reader, &delivery_error_code, sizeof(int8_t));
        KAA_RETURN_IF_ERR(error_code);

        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Log bucket with ID %u: %s (delivery_error_code %d)"
                , bucket_id
                , (delivery_result == LOGGING_RESULT_SUCCESS ? "uploaded successfully" : "upload failed")
                , delivery_error_code);

        remove_request(self, bucket_id);

        if (delivery_result == LOGGING_RESULT_SUCCESS) {
            ext_log_storage_remove_by_bucket_id(self->log_storage_context, bucket_id);
        } else {
            ext_log_storage_unmark_by_bucket_id(self->log_storage_context, bucket_id);
            ext_log_upload_strategy_on_failure(self->log_upload_strategy_context
                                             , (logging_delivery_error_code_t)delivery_error_code);
        }
    }

    update_storage(self);

    return error_code;

}

#endif


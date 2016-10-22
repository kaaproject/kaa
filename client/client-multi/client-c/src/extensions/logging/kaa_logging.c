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

#include "kaa_logging_private.h"
#include "kaa_logging.h"

#include "kaa_private.h"

#include <string.h>
#include <inttypes.h>
#include <sys/types.h>
#include "platform/stdio.h"
#include "platform/sock.h"
#include "platform/time.h"
#include "platform/ext_sha.h"
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

typedef enum {
    LOGGING_RESULT_SUCCESS = 0x00,
    LOGGING_RESULT_FAILURE = 0x01
} logging_sync_result_t;

typedef struct {
    /** Bucket must be threated as expired after this deadline.
     *  Already expired buckets marked with deadline equal to 0.
     */
    kaa_time_t   deadline;
    uint16_t     log_bucket_id;     /**< ID of bucket present in storage. */
    uint16_t     log_count;         /**< Current logs count. */
} timeout_info_t;

typedef struct {
    size_t    size;                 /**< Bucket size in bytes. */
    size_t    log_count;            /**< Count of logs in bucket. */
    uint16_t  id;                   /**< Bucket id. */
} last_bucket_info_t;

struct kaa_log_collector_t {
    kaa_log_bucket_constraints_t   bucket_size;
    void                           *log_storage_context;
    void                           *log_upload_strategy_context;
    kaa_status_t                   *status;
    kaa_channel_manager_t          *channel_manager;
    kaa_logger_t                   *logger;
    kaa_list_t                     *timeouts;
    kaa_log_delivery_listener_t    log_delivery_listeners;
    bool                           is_sync_ignored;
    uint32_t                       log_last_id;         /**< Last log record ID */
    uint16_t                       log_bucket_id;
    last_bucket_info_t             last_bucket;
};

static const kaa_extension_id logging_sync_services[] = {KAA_EXTENSION_LOGGING};

kaa_error_t kaa_extension_logging_init(kaa_context_t *kaa_context, void **context)
{
    kaa_error_t error = kaa_log_collector_create(&kaa_context->log_collector,
            kaa_context->status->status_instance,
            kaa_context->channel_manager, kaa_context->logger);
    *context = kaa_context->log_collector;
    return error;
}

kaa_error_t kaa_extension_logging_deinit(void *context)
{
    kaa_log_collector_destroy(context);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_extension_logging_request_get_size(void *context, size_t *expected_size)
{
    return kaa_logging_request_get_size(context, expected_size);
}

kaa_error_t kaa_extension_logging_request_serialize(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *need_resync)
{
    (void)request_id;

    // TODO(KAA-982): Use asserts
    if (!context || !size || !need_resync) {
        return KAA_ERR_BADPARAM;
    }

    kaa_error_t error;

    size_t size_needed;
    error = kaa_logging_request_get_size(context, &size_needed);
    if (error) {
        return error;
    }

    *need_resync = false;
    error = kaa_logging_need_logging_resync(context, need_resync);
    if (error) {
        return error;
    }

    if (!*need_resync) {
        *size = 0;
        return KAA_ERR_NONE;
    }

    if (!buffer || *size < size_needed) {
        *size = size_needed;
        return KAA_ERR_BUFFER_IS_NOT_ENOUGH;
    }

    *size = size_needed;

    kaa_platform_message_writer_t writer = KAA_MESSAGE_WRITER(buffer, *size);
    error = kaa_logging_request_serialize(context, &writer);
    if (error) {
        return error;
    }

    *size = writer.current - buffer;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_extension_logging_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{
    (void)request_id;

    // TODO(KAA-982): Use asserts
    if (!context || !buffer) {
        return KAA_ERR_BADPARAM;
    }

    kaa_platform_message_reader_t reader = KAA_MESSAGE_READER(buffer, size);
    return kaa_logging_handle_server_sync(context, &reader, extension_options, size);
}

kaa_error_t kaa_logging_need_logging_resync(kaa_log_collector_t *self, bool *result)
{
    // TODO(KAA-982): Use asserts
    if (!self || !result) {
        return KAA_ERR_BADPARAM;
    }

    if (self->is_sync_ignored) {
        *result = false;
        return KAA_ERR_NONE;
    }

    *result = true;
    return KAA_ERR_NONE;
}

static kaa_error_t remember_request(kaa_log_collector_t *self, uint16_t bucket_id, uint16_t count)
{
    // TODO(KAA-982): Use asserts
    if (!self) {
        return KAA_ERR_BADPARAM;
    }

    timeout_info_t *info = KAA_MALLOC(sizeof(*info));
    if (!info) {
        return KAA_ERR_NOMEM;
    }

    info->log_bucket_id = bucket_id;
    info->deadline = KAA_TIME() + (kaa_time_t)ext_log_upload_strategy_get_timeout(self->log_upload_strategy_context);
    info->log_count = count;

    kaa_list_node_t *it = kaa_list_push_back(self->timeouts, info);
    if (!it) {
        KAA_FREE(info);
        return KAA_ERR_NOMEM;
    }

    return KAA_ERR_NONE;
}

static bool find_by_bucket_id(void *data, void *context)
{
    // TODO(KAA-982): Use asserts
    if (!data || !context) {
        return false;
    }

    uint16_t bucket_id = *(uint16_t *) context;
    timeout_info_t *timeout_info = data;

    if (timeout_info->log_bucket_id == bucket_id) {
        return true;
    }

    return false;
}


/* Returns amount of logs in bucket */
static size_t remove_request(kaa_log_collector_t *self, uint16_t bucket_id)
{
    kaa_list_node_t     *node;
    timeout_info_t      *info;
    size_t              logs_sent = 0;

    node = kaa_list_find_next(kaa_list_begin(self->timeouts), find_by_bucket_id, &bucket_id);

    if (node) {
        info = kaa_list_get_data(node);

        if (info) {
            logs_sent = info->log_count;
        }

        kaa_list_remove_at(self->timeouts, node, NULL);
    }

    return logs_sent;
}

/** Handles timeout event. Must be called if timeout is occurred. */
static void handle_timeout(kaa_log_collector_t *self)
{
    // TODO(KAA-982): Use asserts
    if (!self || !self->timeouts) {
        return;
    }

    kaa_error_t err = ext_log_upload_strategy_on_timeout(self->log_upload_strategy_context);

    bool expire_every_entry = (err == KAA_ERR_EVENT_NOT_ATTACHED);

    if (expire_every_entry) {
        /* Upload strategy decided to switch an access point. All pending logs are now deemed as
         * timeouted.
         */
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Access point has been switched. All buckets are expired.");
    }

    for (kaa_list_node_t *it = kaa_list_begin(self->timeouts); it; it = kaa_list_next(it)) {
        timeout_info_t *info = kaa_list_get_data(it);

        if (expire_every_entry || !info->deadline) {
            ext_log_storage_unmark_by_bucket_id(self->log_storage_context, info->log_bucket_id);
            if (self->log_delivery_listeners.on_timeout) {
                kaa_log_bucket_info_t log_bucket_info = {
                    .bucket_id = info->log_bucket_id,
                    .log_count = info->log_count,
                };

                self->log_delivery_listeners.on_timeout(self->log_delivery_listeners.ctx,
                        &log_bucket_info);
            }
        }
    }

    kaa_list_clear(self->timeouts, NULL);
}

static bool is_timeout(kaa_log_collector_t *self)
{
    kaa_time_t now = KAA_TIME();
    bool timeout_occur = false;

    for (kaa_list_node_t *it = kaa_list_begin(self->timeouts); it; it = kaa_list_next(it)) {
        timeout_info_t *info = kaa_list_get_data(it);
        if (now >= info->deadline) {
            KAA_LOG_ERROR(self->logger, KAA_ERR_TIMEOUT,
                    "Log delivery timeout occurred (bucket_id %u)", info->log_bucket_id);
            timeout_occur = true;
            /* Mark bucket as expired */
            info->deadline = 0;
        }
    }

    return timeout_occur;
}

static bool is_upload_allowed(kaa_log_collector_t *self)
{
    size_t pendingCount = kaa_list_get_size(self->timeouts);
    size_t allowedCount = ext_log_upload_strategy_get_max_parallel_uploads(self->log_upload_strategy_context);

    if (pendingCount >= allowedCount) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Ignore log upload: too much pending requests %zu, max allowed %zu",
                pendingCount,  allowedCount);
        return false;
    }

    return true;
}

/** @deprecated Use kaa_extension_logging_deinit(). */
void kaa_log_collector_destroy(kaa_log_collector_t *self)
{
    // TODO(KAA-982): Use asserts
    if (!self) {
        return;
    }

    ext_log_upload_strategy_destroy(self->log_upload_strategy_context);
    ext_log_storage_destroy(self->log_storage_context);
    kaa_list_destroy(self->timeouts, NULL);
    KAA_FREE(self);
}

/** @deprecated Use kaa_extension_logging_init(). */
kaa_error_t kaa_log_collector_create(kaa_log_collector_t **log_collector_p,
        kaa_status_t *status, kaa_channel_manager_t *channel_manager, kaa_logger_t *logger)
{
    // TODO(KAA-982): Use asserts
    if (!log_collector_p) {
        return KAA_ERR_BADPARAM;
    }

    kaa_log_collector_t *collector = KAA_MALLOC(sizeof(*collector));
    if (!collector) {
        return KAA_ERR_NOMEM;
    }

    collector->log_bucket_id                    = 0;
    collector->log_last_id                      = 0;
    collector->log_storage_context              = NULL;
    collector->log_upload_strategy_context      = NULL;
    collector->status                           = status;
    collector->channel_manager                  = channel_manager;
    collector->logger                           = logger;
    collector->is_sync_ignored                  = false;
    collector->last_bucket.log_count            = 0;
    collector->last_bucket.size                 = 0;
    collector->last_bucket.id                   = 1;

    /* Must be overriden in _init() */
    collector->bucket_size.max_bucket_log_count = 0;
    collector->bucket_size.max_bucket_size      = 0;

    collector->timeouts = kaa_list_create();
    if (!collector->timeouts) {
        KAA_FREE(collector);
        return KAA_ERR_NOMEM;
    }

    *log_collector_p = collector;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_logging_init(kaa_log_collector_t *self, void *log_storage_context,
        void *log_upload_strategy_context, const kaa_log_bucket_constraints_t *bucket_sizes)
{
    // TODO(KAA-982): Use asserts
    if (!self || !log_storage_context || !log_upload_strategy_context) {
        return KAA_ERR_BADPARAM;
    }

    ext_log_storage_destroy(self->log_storage_context);
    ext_log_upload_strategy_destroy(self->log_upload_strategy_context);

    self->log_storage_context = log_storage_context;
    self->log_upload_strategy_context = log_upload_strategy_context;
    self->log_delivery_listeners = KAA_LOG_EMPTY_LISTENERS;
    self->bucket_size = *bucket_sizes;

    KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Initialized log collector with log storage {%p}, log upload strategy {%p}"
            , log_storage_context, log_upload_strategy_context);

    return KAA_ERR_NONE;
}

kaa_error_t kaa_logging_set_strategy(kaa_log_collector_t *self, void *log_upload_strategy_context)
{
    // TODO(KAA-982): Use asserts
    if (!self || !log_upload_strategy_context) {
        return KAA_ERR_BADPARAM;
    }

    if (self->log_upload_strategy_context) {
        ext_log_upload_strategy_destroy(self->log_upload_strategy_context);
    }

    self->log_upload_strategy_context = log_upload_strategy_context;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_logging_set_storage(kaa_log_collector_t *self, void *log_storage_context)
{
    // TODO(KAA-982): Use asserts
    if (!self || !log_storage_context) {
        return KAA_ERR_BADPARAM;
    }

    if (self->log_storage_context) {
        ext_log_storage_destroy(self->log_storage_context);
    }

    self->log_storage_context = log_storage_context;

    return KAA_ERR_NONE;
}

static void do_sync(kaa_log_collector_t *self)
{
    // TODO(KAA-982): Use asserts
    if (!self) {
        return;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Initiating log upload...");
    kaa_transport_channel_interface_t *channel =
        kaa_channel_manager_get_transport_channel(self->channel_manager, logging_sync_services[0]);
    if (channel) {
        channel->sync_handler(channel->context, logging_sync_services, 1);
    }
}

static void update_storage(kaa_log_collector_t *self)
{
    // TODO(KAA-982): Use asserts
    if (!self) {
        return;
    }

    switch (ext_log_upload_strategy_decide(self->log_upload_strategy_context, self->log_storage_context)) {
        case UPLOAD:
            if (is_upload_allowed(self)) {
                do_sync(self);
            }
            break;
        default:
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Upload will not be triggered now.");
            break;
    }
}

/** Checks if there is a place for a new record */
static bool no_more_space_in_bucket(kaa_log_collector_t *self, kaa_log_record_t *new_record)
{
    if (self->last_bucket.log_count + 1 > self->bucket_size.max_bucket_log_count
            || self->last_bucket.size + new_record->size > self->bucket_size.max_bucket_size) {
        return true;
    }

    return false;
}

/** Checks if the given bucket is the last bucket */
static bool last_bucket(kaa_log_collector_t *self, uint16_t bucket)
{
    return self->last_bucket.id == bucket;
}

/** Creates a new empty bucket */
static void form_new_bucket(kaa_log_collector_t *self)
{
    self->last_bucket.id++;
    // Avoid hitting reserved values
    if (!self->last_bucket.id) {
        self->last_bucket.id++;
    }

    self->last_bucket.log_count = 0;
    self->last_bucket.size = 0;
}

/** Puts the given record in the current bucket */
static void place_record_in_current_bucket(kaa_log_collector_t *self, kaa_log_record_t *new_record)
{
    new_record->bucket_id = self->last_bucket.id;
    self->last_bucket.log_count++;
    self->last_bucket.size += new_record->size;
}

/** Creates the next bucket and puts the given record there */
static void place_record_in_next_bucket(kaa_log_collector_t *self, kaa_log_record_t *new_record)
{
    form_new_bucket(self);
    place_record_in_current_bucket(self, new_record);
}

kaa_error_t kaa_logging_add_record(kaa_log_collector_t *self, kaa_user_log_record_t *entry, kaa_log_record_info_t *log_info)
{
    // TODO(KAA-982): Use asserts
    if (!self || !entry) {
        return KAA_ERR_BADPARAM;
    }

    // TODO(KAA-982): Use asserts
    if (!self->log_storage_context) {
        return KAA_ERR_NOT_INITIALIZED;
    }

    // Bucket ID will be incremented only if it will be added without errors
    kaa_log_record_t record = {
        .data = NULL,
        .size = entry->get_size(entry),
        .bucket_id = 0,
    };

    if (!record.size) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BADDATA,
                "Failed to add log record: serialized record size is null. "
                "Maybe log record schema is empty");
        return KAA_ERR_BADDATA;
    }

    bool next_bucket_required = no_more_space_in_bucket(self, &record);

    // To restore last bucket state in case of error
    last_bucket_info_t fallback = self->last_bucket;

    // Put log in the next bucket if required
    if (next_bucket_required) {
        place_record_in_next_bucket(self, &record);
    } else {
        place_record_in_current_bucket(self, &record);
    }

    kaa_error_t error = ext_log_storage_allocate_log_record_buffer(self->log_storage_context,
            &record);
    if (error) {
        KAA_LOG_ERROR(self->logger, error,
                "Failed to add log record: cannot allocate buffer for log record");
        goto err_buffer;
    }

    avro_writer_t writer = avro_writer_memory((char *)record.data, record.size);
    if (!writer) {
        error = KAA_ERR_NOMEM;
        KAA_LOG_ERROR(self->logger, error,
                "Failed to add log record: cannot create serializer");
        ext_log_storage_deallocate_log_record_buffer(self->log_storage_context, &record);
        goto err_avro;
    }

    entry->serialize(writer, entry);
    avro_writer_free(writer);

    error = ext_log_storage_add_log_record(self->log_storage_context, &record);
    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to add log record to storage");
        ext_log_storage_deallocate_log_record_buffer(self->log_storage_context, &record);
        goto err_add;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Added log record, size %zu, bucket %u",
            record.size,
            record.bucket_id);

    if (is_timeout(self)) {
        handle_timeout(self);
    } else {
        update_storage(self);
    }

    if (log_info) {
        log_info->bucket_id = self->last_bucket.id;
        log_info->log_id = self->log_last_id++;
    }

    return KAA_ERR_NONE;

err_add:
err_avro:
    ext_log_storage_deallocate_log_record_buffer(self->log_storage_context, &record);
err_buffer:
    self->last_bucket = fallback;
    return error;
}

kaa_error_t kaa_logging_set_listeners(kaa_log_collector_t *self, const kaa_log_delivery_listener_t *listeners)
{
    // TODO(KAA-982): Use asserts
    if (!self || !listeners) {
        return KAA_ERR_BADPARAM;
    }

    self->log_delivery_listeners = *listeners;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_logging_request_get_size(kaa_log_collector_t *self, size_t *expected_size)
{
    // TODO(KAA-982): Use asserts
    if (!self || !expected_size) {
        return KAA_ERR_BADPARAM;
    }

    // TODO(KAA-982): Use asserts
    if (!self->log_storage_context) {
        return KAA_ERR_NOT_INITIALIZED;
    }

    *expected_size = 0;

    if (!is_upload_allowed(self)) {
        self->is_sync_ignored = true;
        return KAA_ERR_NONE;
    }

    size_t records_count = ext_log_storage_get_records_count(self->log_storage_context);
    size_t total_size = ext_log_storage_get_total_size(self->log_storage_context);

    self->is_sync_ignored = (!records_count || !total_size);

    if (!self->is_sync_ignored) {
        *expected_size = KAA_EXTENSION_HEADER_SIZE;
        *expected_size += sizeof(uint32_t); // request id + log records count

        size_t actual_size = records_count * sizeof(uint32_t) + records_count * KAA_MAX_PADDING_LENGTH + total_size;
        size_t bucket_size = self->bucket_size.max_bucket_size;

        *expected_size += ((actual_size > bucket_size) ? bucket_size : actual_size);
    }

    return KAA_ERR_NONE;
}



kaa_error_t kaa_logging_request_serialize(kaa_log_collector_t *self, kaa_platform_message_writer_t *writer)
{
    // TODO(KAA-982): Use asserts
    if (!self || !writer) {
        return KAA_ERR_BADPARAM;
    }

    // TODO(KAA-982): Use asserts
    if (!self->log_storage_context) {
        return KAA_ERR_NOT_INITIALIZED;
    }

    if (self->is_sync_ignored) {
        return KAA_ERR_NONE;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to serialize client logging sync");

    kaa_platform_message_writer_t tmp_writer = *writer;

    uint8_t *extension_size_p = tmp_writer.current + sizeof(uint32_t); // Pointer to the extension size. Will be filled in later.
    kaa_error_t error = kaa_platform_message_write_extension_header(&tmp_writer,
            KAA_EXTENSION_LOGGING,
            KAA_LOGGING_RECEIVE_UPDATES_FLAG,
            0);
    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to write log extension header");
        return KAA_ERR_WRITE_FAILED;
    }

    uint8_t *bucket_id_p = tmp_writer.current;
    tmp_writer.current += sizeof(uint16_t);
    uint8_t *records_count_p = tmp_writer.current; // Pointer to the records count. Will be filled in later.
    tmp_writer.current += sizeof(uint16_t);

    uint16_t bucket_id;
    uint16_t first_bucket = 0;

    /* Bucket size constraints */

    size_t bucket_size = self->bucket_size.max_bucket_size;
    size_t max_log_count = self->bucket_size.max_bucket_log_count;
    size_t actual_size = (tmp_writer.end - tmp_writer.current);

    bucket_size = (actual_size > bucket_size ? bucket_size : actual_size);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE,
            "Extracting log records... (bucket size %zu)", bucket_size);

    uint16_t records_count = 0;

    while (!error && bucket_size > sizeof(uint32_t) && records_count < max_log_count) {
        size_t record_len = 0;
        error = ext_log_storage_write_next_record(self->log_storage_context,
                (char *)tmp_writer.current + sizeof(uint32_t),
                bucket_size - sizeof(uint32_t),
                &bucket_id,
                &record_len);

        switch (error) {
            case KAA_ERR_NONE:
                if (first_bucket == 0) {
                    first_bucket = bucket_id;
                } else if (bucket_id != first_bucket) {
                    // Put back log item if it is in another bucket
                    ext_log_storage_unmark_by_bucket_id(self->log_storage_context, bucket_id);
                    break;
                }

                ++records_count;
                *((uint32_t *) tmp_writer.current) = KAA_HTONL(record_len);
                tmp_writer.current += (sizeof(uint32_t) + record_len);
                kaa_platform_message_write_alignment(&tmp_writer);
                bucket_size -= kaa_aligned_size_get(record_len) + sizeof(uint32_t);
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

    *((uint16_t *) bucket_id_p) = KAA_HTONS(first_bucket);

    size_t payload_size = tmp_writer.current - writer->current - KAA_EXTENSION_HEADER_SIZE;
    KAA_LOG_INFO(self->logger, KAA_ERR_NONE,
            "Created log bucket: id '%u', log records count %u, payload size %zu",
            first_bucket, records_count, payload_size);

    *((uint32_t *) extension_size_p) = KAA_HTONL(payload_size);
    *((uint16_t *) records_count_p) = KAA_HTONS(records_count);
    *writer = tmp_writer;

    error = remember_request(self, first_bucket, records_count);
    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to remember request time stamp");
    }

    // Incomplete bucket sent, so let's go and step to the next bucket
    if (last_bucket(self, first_bucket)) {
        form_new_bucket(self);
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_logging_handle_server_sync(kaa_log_collector_t *self,
        kaa_platform_message_reader_t *reader, uint16_t extension_options, size_t extension_length)
{
    // Only used for logging
    (void)extension_options;
    (void)extension_length;

    // TODO(KAA-982): Use asserts
    if (!self || !reader) {
        return KAA_ERR_BADPARAM;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received logging server sync: options 0, payload size %u", extension_length);

    uint32_t delivery_status_count;
    kaa_error_t error_code = kaa_platform_message_read(reader, &delivery_status_count, sizeof(uint32_t));
    if (error_code) {
        return error_code;
    }

    delivery_status_count = KAA_NTOHL(delivery_status_count);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received %lu log delivery statuses", delivery_status_count);

    uint16_t bucket_id;
    int8_t delivery_result;
    int8_t delivery_error_code;

    for (uint32_t i = 0; i < delivery_status_count; ++i) {
        error_code = kaa_platform_message_read(reader, &bucket_id, sizeof(uint16_t));
        if (error_code) {
            return error_code;
        }

        bucket_id = KAA_NTOHS(bucket_id);

        error_code = kaa_platform_message_read(reader, &delivery_result, sizeof(int8_t));
        if (error_code) {
            return error_code;
        }

        error_code = kaa_platform_message_read(reader, &delivery_error_code, sizeof(int8_t));
        if (error_code) {
            return error_code;
        }

        if (delivery_result == LOGGING_RESULT_SUCCESS) {
            KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Log bucket uploaded successfully, id '%u'", bucket_id);
        } else {
            KAA_LOG_ERROR(self->logger, KAA_ERR_WRITE_FAILED,
                    "Failed to upload log bucket, id '%u' (delivery error code '%u')",
                    bucket_id, delivery_error_code);
        }

        size_t uploaded_count = remove_request(self, bucket_id);

        kaa_log_bucket_info_t log_bucket_info = {
            .log_count = uploaded_count,
            .bucket_id = bucket_id,
        };

        if (delivery_result == LOGGING_RESULT_SUCCESS) {
            if (self->log_delivery_listeners.on_success) {
                self->log_delivery_listeners.on_success(self->log_delivery_listeners.ctx,
                        &log_bucket_info);
            }
            ext_log_storage_remove_by_bucket_id(self->log_storage_context, bucket_id);
        } else {
            if (self->log_delivery_listeners.on_failed) {
                self->log_delivery_listeners.on_failed(self->log_delivery_listeners.ctx,
                        &log_bucket_info);
            }
            ext_log_storage_unmark_by_bucket_id(self->log_storage_context, bucket_id);
            ext_log_upload_strategy_on_failure(self->log_upload_strategy_context,
                    (logging_delivery_error_code_t)delivery_error_code);
        }
    }

    update_storage(self);

    return error_code;
}


void ext_log_upload_timeout(kaa_log_collector_t *self)
{
    if (!is_timeout(self)
            || ext_log_upload_strategy_is_timeout_strategy(self->log_upload_strategy_context)) {
        update_storage(self);
    } else {
        handle_timeout(self);
    }
}

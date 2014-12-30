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

#include "kaa_memory_log_storage.h"

#include "collections/kaa_deque.h"
#include "collections/kaa_list.h"
#include "utilities/kaa_mem.h"
#include <stdint.h>
#include <string.h>


static kaa_log_entry_t empty_entry = { NULL, 0 };



struct kaa_memory_log_storage_t {
    size_t        occupied_size;    /**< Currently occupied logs volume */
    kaa_deque_t  *logs;             /**< List of @c kaa_log_entry_t */
    kaa_list_t   *log_buckets;      /**< List of @c kaa_memory_log_block_t */
    kaa_logger_t *logger;           /**< Logger instance */
};



typedef struct {
    uint16_t     bucket_id;
    kaa_deque_t *logs;          /**< List of @c kaa_log_entry_t */
    size_t       bucket_size;
} kaa_log_bucket_t;



static kaa_log_bucket_t* create_log_bucket(uint16_t id)
{
    kaa_log_bucket_t *block = (kaa_log_bucket_t *) KAA_MALLOC(sizeof(kaa_log_bucket_t));
    if (!block)
        return NULL;
    block->bucket_id = id;
    kaa_deque_create(&block->logs);  // FIXME: handle error if any;
    block->bucket_size = 0;
    return block;
}



void destroy_log_record(void *record_p)
{
    if (record_p)
        KAA_FREE(record_p);
}



static void destroy_log_bucket(void *bucket_ptr)
{
    if (bucket_ptr) {
        kaa_log_bucket_t *bucket = (kaa_log_bucket_t *) bucket_ptr;
        kaa_deque_destroy(bucket->logs, &destroy_log_record);
        KAA_FREE(bucket);
    }
}



kaa_error_t kaa_memory_log_storage_create(kaa_memory_log_storage_t** log_storage_p, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL(log_storage_p, KAA_ERR_BADPARAM);

    *log_storage_p = KAA_MALLOC(sizeof(kaa_memory_log_storage_t));
    KAA_RETURN_IF_NIL(*log_storage_p, KAA_ERR_NOMEM);

    kaa_error_t error = kaa_deque_create(&(*log_storage_p)->logs);
    if (error) {
        KAA_FREE(*log_storage_p);
        *log_storage_p = NULL;
        return error;
    }
    (*log_storage_p)->occupied_size = 0;
    (*log_storage_p)->log_buckets = NULL;
    (*log_storage_p)->logger = logger;

    return KAA_ERR_NONE;
}



static void kaa_memory_log_storage_add_log_record(void *context, kaa_log_entry_t record)
{
    KAA_RETURN_IF_NIL(context,);
    kaa_memory_log_storage_t *log_storage = (kaa_memory_log_storage_t *)context;

    kaa_log_entry_t *new_entry = (kaa_log_entry_t *) KAA_MALLOC(sizeof(kaa_log_entry_t));
    *new_entry = record;
    kaa_deque_push_back_data(log_storage->logs, new_entry); // FIXME: handle error if any;
    log_storage->occupied_size += (size_t) record.record_size;
}



static bool find_log_block_by_id(void *block_p, void *context)
{
    kaa_log_bucket_t *block = (kaa_log_bucket_t *) block_p;
    uint16_t *matcher = (uint16_t *) context;
    return (block && matcher) ? (block->bucket_id == (*matcher)) : false;
}



static kaa_log_entry_t kaa_memory_log_storage_get_next_record(void *context, uint16_t bucket_id, size_t size_limit)
{
    KAA_RETURN_IF_NIL(context, empty_entry);
    kaa_memory_log_storage_t *self = (kaa_memory_log_storage_t *)context;

    kaa_deque_iterator_t *single_log_record = NULL;

    kaa_error_t error_code = kaa_deque_pop_front(self->logs, &single_log_record);
    KAA_LOG_DEBUG(self->logger, error_code, "Received record (iterator {%p}, pointing to {%p})"
            , single_log_record
            , kaa_deque_iterator_get_data(single_log_record));
    if (error_code)
        return empty_entry;

    kaa_log_entry_t *entry = (kaa_log_entry_t *) kaa_deque_iterator_get_data(single_log_record);
    if (entry->record_size > size_limit) // remaining size is not enough for the record
        return empty_entry;

    kaa_log_bucket_t *bucket = NULL;

    kaa_log_bucket_t *top_log_bucket = (kaa_log_bucket_t*) kaa_list_get_data(self->log_buckets);
    if (top_log_bucket && bucket_id == top_log_bucket->bucket_id) {
        bucket = top_log_bucket;
    } else {
        bucket = create_log_bucket(bucket_id);
        if (!bucket) {
            KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to create log bucket");
            return empty_entry;
        }

        if (self->log_buckets) {
            kaa_list_t * new_head = kaa_list_push_front(self->log_buckets, bucket);
            if (!new_head) {
                KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to insert new log bucket");
                destroy_log_bucket(bucket);
                return empty_entry;
            }
            self->log_buckets = new_head;
        } else {
            self->log_buckets = kaa_list_create(bucket);
            if (!self->log_buckets) {
                KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to insert new log bucket");
                destroy_log_bucket(bucket);
                return empty_entry;
            }
        }
    }

    kaa_deque_push_back_iterator(bucket->logs, single_log_record);  // FIXME: handle error if any;
    return *entry;
}



static void kaa_memory_log_storage_upload_succeeded(void *context, uint16_t bucket_id)
{
    KAA_RETURN_IF_NIL(context,);
    kaa_memory_log_storage_t *log_storage = (kaa_memory_log_storage_t *)context;

    kaa_list_t * block = kaa_list_find_next(log_storage->log_buckets, &find_log_block_by_id, &bucket_id);
    if (block) {
        kaa_list_remove_at(&log_storage->log_buckets, block, &destroy_log_bucket);
    }
}



static void kaa_memory_log_storage_upload_failed(void *context, uint16_t bucket_id)
{
    KAA_RETURN_IF_NIL(context,);
    kaa_memory_log_storage_t *log_storage = (kaa_memory_log_storage_t *)context;

    kaa_list_t * it = kaa_list_find_next(log_storage->log_buckets, &find_log_block_by_id, &bucket_id);
    if (it) {
        kaa_log_bucket_t *block = kaa_list_get_data(it);
        kaa_list_remove_at(&log_storage->log_buckets, it, &kaa_null_destroy);
        log_storage->logs = kaa_deque_merge_move(block->logs, log_storage->logs);
    }
}



static void kaa_memory_log_storage_shrink_to_size(void *context, size_t allowed_size)
{
    KAA_RETURN_IF_NIL(context,);
    kaa_memory_log_storage_t *log_storage = (kaa_memory_log_storage_t *)context;

    while (log_storage->occupied_size > allowed_size) {
        kaa_deque_iterator_t *it = NULL;
        kaa_deque_pop_front(log_storage->logs, &it); // FIXME: handle error if any;
        kaa_log_entry_t *record = (kaa_log_entry_t *) kaa_deque_iterator_get_data(it);
        log_storage->occupied_size -= record->record_size;
        kaa_deque_iterator_destroy(it, &destroy_log_record);
    }
}



static size_t kaa_memory_log_storage_get_total_size(void *context)
{
    KAA_RETURN_IF_NIL(context, 0);
    kaa_memory_log_storage_t *log_storage = (kaa_memory_log_storage_t *)context;

    return log_storage->occupied_size;
}



static uint16_t kaa_memory_log_storage_get_records_count(void *context)
{
    KAA_RETURN_IF_NIL(context, 0);
    kaa_memory_log_storage_t *log_storage = (kaa_memory_log_storage_t *)context;

    return kaa_deque_size(log_storage->logs);
}



static void kaa_memory_log_storage_destroy(void *context)
{
    KAA_RETURN_IF_NIL(context,);
    kaa_memory_log_storage_t *log_storage = (kaa_memory_log_storage_t *)context;

    kaa_deque_destroy(log_storage->logs, &destroy_log_record);
    kaa_list_destroy(log_storage->log_buckets, &destroy_log_bucket);
    KAA_FREE(log_storage);
}



kaa_error_t kaa_memory_log_storage_get_interface(kaa_memory_log_storage_t *self, kaa_log_storage_t *interface)
{
    KAA_RETURN_IF_NIL2(self, interface, KAA_ERR_BADPARAM);

     *interface = (kaa_log_storage_t) {
        self,
        &kaa_memory_log_storage_add_log_record,
        &kaa_memory_log_storage_get_next_record,
        &kaa_memory_log_storage_upload_succeeded,
        &kaa_memory_log_storage_upload_failed,
        &kaa_memory_log_storage_shrink_to_size,
        &kaa_memory_log_storage_get_total_size,
        &kaa_memory_log_storage_get_records_count,
        &kaa_memory_log_storage_destroy
    };
    return KAA_ERR_NONE;
}



/*
 * Log upload strategy
 */


static const kaa_log_upload_properties_t kaa_memory_log_upload_properties = {
      128   /**< max_log_block_size */
    , 256   /**< max_log_upload_threshold */
    , 1024  /**< max_log_storage_volume */
};



static kaa_log_upload_decision_t memory_log_storage_is_upload_needed(void *context, const kaa_log_storage_t *log_storage)
{
    KAA_RETURN_IF_NIL(log_storage, NOOP);

    if ((*log_storage->get_total_size)(log_storage->context) > kaa_memory_log_upload_properties.max_log_storage_volume)
        return CLEANUP;

    if ((*log_storage->get_total_size)(log_storage->context) >= kaa_memory_log_upload_properties.max_log_upload_threshold)
        return UPLOAD;

    return NOOP;
}



kaa_error_t kaa_memory_log_storage_get_strategy(kaa_memory_log_storage_t *self, kaa_log_upload_strategy_t *strategy)
{
    KAA_RETURN_IF_NIL2(self, strategy, KAA_ERR_BADPARAM);

     *strategy = (kaa_log_upload_strategy_t) {
        NULL,
        &memory_log_storage_is_upload_needed
    };
    return KAA_ERR_NONE;
}

#endif

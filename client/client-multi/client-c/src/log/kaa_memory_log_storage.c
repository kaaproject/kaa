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
#include "kaa_list.h"
#include "kaa_uuid.h"
#include "kaa_mem.h"
#include <stdint.h>
#include <string.h>


static kaa_logger_t *logger = NULL;
void set_memory_log_storage_logger(kaa_logger_t *log)
{
    logger = log;
}

static kaa_log_upload_properties_t kaa_memory_log_upload_properties = {
          /* .max_log_block_size = */       128
        , /* .max_log_upload_threshold = */ 256
        , /* .max_log_storage_volume = */   1024
};

typedef struct kaa_memory_log_storage_t {
    size_t          occupied_size;
    kaa_deque_t *   logs;               // list of kaa_log_entry_t
    kaa_list_t *    uploading_blocks;   // list of kaa_memory_log_block_t
} kaa_memory_log_storage_t;

static kaa_memory_log_storage_t * log_storage = NULL;

typedef struct kaa_memory_log_block_t {
    kaa_uuid_t          uuid;
    kaa_deque_t *       logs; // list of kaa_log_entry_t
    size_t              block_size;
} kaa_memory_log_block_t;

static kaa_memory_log_block_t * create_memory_log_block(kaa_uuid_t uuid)
{
    kaa_memory_log_block_t *block = (kaa_memory_log_block_t *) KAA_MALLOC(sizeof(kaa_memory_log_block_t));
    if (!block)
        return NULL;
    kaa_uuid_copy(&block->uuid, &uuid);
    kaa_deque_create(&block->logs);  // FIXME: handle error if any;
    block->block_size = 0;
    return block;
}

static void destroy_memory_log_block(void * block_p)
{
    if (block_p != NULL) {
        kaa_memory_log_block_t * block = (kaa_memory_log_block_t *) block_p;
        kaa_deque_destroy(block->logs, destroy_log_record);
        KAA_FREE(block);
    }
}

static void memory_log_storage_add_log_record(kaa_log_entry_t * record)
{
    kaa_deque_push_back_data(log_storage->logs, record); // FIXME: handle error if any;
    log_storage->occupied_size += (size_t)record->data->size;
}

static bool find_log_block_by_uuid(void * block_p, void *context)
{
    kaa_memory_log_block_t *block = (kaa_memory_log_block_t *) block_p;
    kaa_uuid_t *matcher = (kaa_uuid_t *) context;
    return (block && matcher) ? (kaa_uuid_compare(&block->uuid, matcher) == 0) : false;
}

static kaa_log_entry_t * memory_log_storage_get_record(kaa_uuid_t uuid)
{
    kaa_deque_iterator_t *single_log_record = NULL;

    kaa_error_t error_code = kaa_deque_pop_front(log_storage->logs, &single_log_record);
    KAA_LOG_DEBUG(logger, error_code, "Received record (iterator {%p}, pointing to {%p})"
            , single_log_record
            , kaa_deque_iterator_get_data(single_log_record));
    if (error_code) {
        return NULL;
    }
    kaa_memory_log_block_t *block = NULL;

    kaa_memory_log_block_t * top_log_block = (kaa_memory_log_block_t * ) kaa_list_get_data(log_storage->uploading_blocks);
    if (top_log_block && (kaa_uuid_compare(&uuid, &top_log_block->uuid) == 0)) {
        block = top_log_block;
    } else {
        block = create_memory_log_block(uuid);
        if (!block) {
            KAA_LOG_ERROR(logger, KAA_ERR_NOMEM, "Failed to create log block");
            return NULL;
        }

        if (log_storage->uploading_blocks) {
            kaa_list_t * new_head = kaa_list_push_front(log_storage->uploading_blocks, block);
            if (!new_head) {
                KAA_LOG_ERROR(logger, KAA_ERR_NOMEM, "Failed to insert new log block");
                destroy_memory_log_block(block);
                return NULL;
            }
            log_storage->uploading_blocks = new_head;
        } else {
            log_storage->uploading_blocks = kaa_list_create(block);
            if (!log_storage->uploading_blocks) {
                KAA_LOG_ERROR(logger, KAA_ERR_NOMEM, "Failed to insert new log block");
                destroy_memory_log_block(block);
                return NULL;
            }
        }
    }

    kaa_deque_push_back_iterator(block->logs, single_log_record);  // FIXME: handle error if any;
    return kaa_deque_iterator_get_data(single_log_record);
}


static void memory_log_storage_upload_succeeded(kaa_uuid_t uuid)
{
    kaa_list_t * block = kaa_list_find_next(log_storage->uploading_blocks, &find_log_block_by_uuid, &uuid);
    if (block) {
        kaa_list_remove_at(&log_storage->uploading_blocks, block, &destroy_memory_log_block);
    }
}

static void memory_log_storage_upload_failed(kaa_uuid_t uuid)
{
    kaa_list_t * it = kaa_list_find_next(log_storage->uploading_blocks, &find_log_block_by_uuid, &uuid);
    if (it) {
        kaa_memory_log_block_t *block = kaa_list_get_data(it);
        kaa_list_remove_at(&log_storage->uploading_blocks, it, &kaa_null_destroy);
        log_storage->logs = kaa_deque_merge_move(block->logs, log_storage->logs);
    }
}

static void memory_log_storage_shrink_to_size(size_t allowed_size)
{
    while (log_storage->occupied_size > allowed_size) {
        kaa_deque_iterator_t *it = NULL;
        kaa_deque_pop_front(log_storage->logs, &it); // FIXME: handle error if any;
        kaa_log_entry_t * record = (kaa_log_entry_t *) kaa_deque_iterator_get_data(it);
        if (record) {
            log_storage->occupied_size -= record->data->size;
        } else {
            break;
        }
    }
}

static void memory_log_storage_destroy()
{
    if (log_storage) {
        kaa_deque_destroy(log_storage->logs, &destroy_log_record);
        kaa_list_destroy(log_storage->uploading_blocks, &destroy_memory_log_block);
        KAA_FREE(log_storage);
    }
}

static kaa_log_storage_t public_log_storage_interface = {
        /* add_log_record */    &memory_log_storage_add_log_record,
        /* get_record */        &memory_log_storage_get_record,
        /* upload_succeeded */  &memory_log_storage_upload_succeeded,
        /* upload_failed */     &memory_log_storage_upload_failed,
        /* shrink_to_size */    &memory_log_storage_shrink_to_size,
        /* destroy */           &memory_log_storage_destroy
};

static size_t memory_log_storage_get_total_size()
{
    return log_storage->occupied_size;
}

static size_t memory_log_storage_get_records_count()
{
    return kaa_deque_size(log_storage->logs);
}

static kaa_storage_status_t public_log_storage_status_interface = {
        /* get_total_size */        &memory_log_storage_get_total_size,
        /* get_records_count */     &memory_log_storage_get_records_count
};

static void init_memory_log_storage()
{
    if (log_storage != NULL) {
        return;
    }
    log_storage = (kaa_memory_log_storage_t *) KAA_MALLOC(sizeof(kaa_memory_log_storage_t));
    log_storage->occupied_size = 0;
    log_storage->uploading_blocks = NULL;
    kaa_deque_create(&log_storage->logs); // FIXME: handle error if any;
}

kaa_log_storage_t * get_memory_log_storage()
{
    init_memory_log_storage();
    return &public_log_storage_interface;
}

kaa_storage_status_t * get_memory_log_storage_status()
{
    init_memory_log_storage();
    return &public_log_storage_status_interface;
}

kaa_log_upload_properties_t * get_memory_log_upload_properties()
{
    return &kaa_memory_log_upload_properties;
}

kaa_log_upload_decision_t memory_log_storage_is_upload_needed(kaa_storage_status_t *status)
{
    if (status != NULL) {
        if ((*status->get_total_size)() > kaa_memory_log_upload_properties.max_log_storage_volume) {
            return CLEANUP;
        }
        if ((*status->get_total_size)() >= kaa_memory_log_upload_properties.max_log_upload_threshold) {
            return UPLOAD;
        }
    }
    return NOOP;
}

#endif

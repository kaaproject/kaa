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
    uint16_t            id;
    kaa_deque_t *       logs; // list of kaa_log_entry_t
    size_t              block_size;
} kaa_memory_log_block_t;

static kaa_memory_log_block_t * create_memory_log_block(uint16_t id)
{
    kaa_memory_log_block_t *block = (kaa_memory_log_block_t *) KAA_MALLOC(sizeof(kaa_memory_log_block_t));
    if (!block)
        return NULL;
    block->id = id;
    kaa_deque_create(&block->logs);  // FIXME: handle error if any;
    block->block_size = 0;
    return block;
}

void destroy_log_record(void *record_p)
{
    if (record_p ) {
        KAA_FREE(record_p);
    }
}

static void destroy_memory_log_block(void * block_p)
{
    if (block_p != NULL) {
        kaa_memory_log_block_t * block = (kaa_memory_log_block_t *) block_p;
        kaa_deque_destroy(block->logs, destroy_log_record);
        KAA_FREE(block);
    }
}

static void memory_log_storage_add_log_record(kaa_log_entry_t record)
{
    kaa_log_entry_t *new_entry = (kaa_log_entry_t *) KAA_MALLOC(sizeof(kaa_log_entry_t));
    *new_entry = record;
    kaa_deque_push_back_data(log_storage->logs, new_entry); // FIXME: handle error if any;
    log_storage->occupied_size += (size_t) record.record_size;
}

static bool find_log_block_by_id(void * block_p, void *context)
{
    kaa_memory_log_block_t *block = (kaa_memory_log_block_t *) block_p;
    uint16_t *matcher = (uint16_t *) context;
    return (block && matcher) ? (block->id == (*matcher)) : false;
}

static kaa_log_entry_t memory_log_storage_get_record(uint16_t id, size_t remaining_size)
{
    kaa_deque_iterator_t *single_log_record = NULL;

    kaa_error_t error_code = kaa_deque_pop_front(log_storage->logs, &single_log_record);
    KAA_LOG_DEBUG(logger, error_code, "Received record (iterator {%p}, pointing to {%p})"
            , single_log_record
            , kaa_deque_iterator_get_data(single_log_record));
    if (error_code) {
        return empty_entry;
    }
    kaa_memory_log_block_t *block = NULL;

    kaa_memory_log_block_t * top_log_block = (kaa_memory_log_block_t * ) kaa_list_get_data(log_storage->uploading_blocks);
    if (top_log_block && id == top_log_block->id) {
        block = top_log_block;
    } else {
        block = create_memory_log_block(id);
        if (!block) {
            KAA_LOG_ERROR(logger, KAA_ERR_NOMEM, "Failed to create log block");
            return empty_entry;
        }

        if (log_storage->uploading_blocks) {
            kaa_list_t * new_head = kaa_list_push_front(log_storage->uploading_blocks, block);
            if (!new_head) {
                KAA_LOG_ERROR(logger, KAA_ERR_NOMEM, "Failed to insert new log block");
                destroy_memory_log_block(block);
                return empty_entry;
            }
            log_storage->uploading_blocks = new_head;
        } else {
            log_storage->uploading_blocks = kaa_list_create(block);
            if (!log_storage->uploading_blocks) {
                KAA_LOG_ERROR(logger, KAA_ERR_NOMEM, "Failed to insert new log block");
                destroy_memory_log_block(block);
                return empty_entry;
            }
        }
    }

    kaa_deque_push_back_iterator(block->logs, single_log_record);  // FIXME: handle error if any;
    return *((kaa_log_entry_t *)kaa_deque_iterator_get_data(single_log_record));
}


static void memory_log_storage_upload_succeeded(uint16_t id)
{
    kaa_list_t * block = kaa_list_find_next(log_storage->uploading_blocks, &find_log_block_by_id, &id);
    if (block) {
        kaa_list_remove_at(&log_storage->uploading_blocks, block, &destroy_memory_log_block);
    }
}

static void memory_log_storage_upload_failed(uint16_t id)
{
    kaa_list_t * it = kaa_list_find_next(log_storage->uploading_blocks, &find_log_block_by_id, &id);
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
            log_storage->occupied_size -= record->record_size;
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

static uint16_t memory_log_storage_get_records_count()
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

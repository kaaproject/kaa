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
#include "kaa_list.h"
#include "kaa_uuid.h"
#include "kaa_mem.h"
#include <stdint.h>
#include <string.h>

typedef struct kaa_memory_log_storage_t {
    size_t          occupied_size;
    kaa_list_t *    logs;               // list of kaa_log_entry_t
    kaa_list_t *    uploading_blocks;   // list of kaa_memory_log_block_t
} kaa_memory_log_storage_t;

static kaa_memory_log_storage_t * log_storage = NULL;

typedef struct kaa_memory_log_block_t {
    kaa_uuid_t          uuid;
    kaa_list_t *        logs; // list of kaa_log_entry_t
    size_t              block_size;
} kaa_memory_log_block_t;

static kaa_memory_log_block_t * create_memory_log_block(kaa_uuid_t uuid, kaa_list_t *logs)
{
    kaa_memory_log_block_t *block = (kaa_memory_log_block_t *) KAA_CALLOC(1, sizeof(kaa_memory_log_block_t));
    kaa_uuid_copy(&block->uuid, &uuid);
    block->logs = logs;
    block->block_size = 0;
    kaa_log_entry_t *log = NULL;
    while ((log = kaa_list_get_data(logs)) != NULL) {
        block->block_size += log->data->size;
        logs = kaa_list_next(logs);
    }
    return block;
}

static void destroy_memory_log_block(void * block_p)
{
    if (block_p != NULL) {
        kaa_memory_log_block_t * block = (kaa_memory_log_block_t *) block_p;
        kaa_list_destroy(block->logs, destroy_log_record);
    }
}

static void noop(void * block_p)
{
}

static void memory_log_storage_add_log_record(kaa_log_entry_t * record)
{
    if (log_storage->logs == NULL) {
        log_storage->logs = kaa_list_create(record);
    } else {
        kaa_list_push_back(log_storage->logs, record);
    }
    log_storage->occupied_size += (size_t)record->data->size;
}

static kaa_list_t * memory_log_storage_get_records(kaa_uuid_t uuid, size_t max_size)
{
    kaa_list_t * logs = log_storage->logs, * ret_head = log_storage->logs;
    size_t block_size = 0;

    kaa_log_entry_t * record = kaa_list_get_data(logs);
    while (record != NULL) {
        block_size += record->data->size;
        kaa_list_t * next = kaa_list_next(logs);
        kaa_log_entry_t * next_record = kaa_list_get_data(next);
        if (next_record != NULL && (block_size + next_record->data->size > max_size)) {
            break;
        }
        logs = next;
        record = next_record;
    }
    if (logs != NULL) {
        kaa_list_split_after(ret_head, logs, &log_storage->logs);
    } else {
        log_storage->logs = NULL;
    }

    kaa_memory_log_block_t *block = create_memory_log_block(uuid, ret_head);
    if (log_storage->uploading_blocks == NULL) {
        log_storage->uploading_blocks = kaa_list_create(block);
    } else {
        log_storage->uploading_blocks = kaa_list_push_front(log_storage->uploading_blocks, block);
    }

    log_storage->occupied_size -= block->block_size;

    return ret_head;
}

static kaa_uuid_t uuid_for_search;
static int find_log_block_by_uuid(void * block_p)
{
    kaa_memory_log_block_t * block = (kaa_memory_log_block_t *) block_p;
    if (block != NULL) {
        if (kaa_uuid_compare(&block->uuid, &uuid_for_search) == 0) {
            return 1;
        }
    }
    return 0;
}

static void memory_log_storage_upload_succeeded(kaa_uuid_t uuid)
{
    kaa_uuid_copy(&uuid_for_search, &uuid);
    kaa_list_t * block = kaa_list_find_next(log_storage->uploading_blocks, &find_log_block_by_uuid);
    if (block) {
        kaa_list_remove_at(&log_storage->uploading_blocks, block, &destroy_memory_log_block);
    }
}

static void memory_log_storage_upload_failed(kaa_uuid_t uuid)
{
    kaa_uuid_copy(&uuid_for_search, &uuid);
    kaa_list_t * it = kaa_list_find_next(log_storage->uploading_blocks, &find_log_block_by_uuid);
    if (it) {
        kaa_memory_log_block_t *block = kaa_list_get_data(it);
        kaa_list_remove_at(&log_storage->uploading_blocks, it, &noop);
        kaa_lists_merge(block->logs, log_storage->logs);
        log_storage->logs = block->logs;
    }
}

static void memory_log_storage_shrink_to_size(size_t allowed_size)
{
    kaa_list_t *head = NULL;
    while (log_storage->occupied_size > allowed_size) {
        head = log_storage->logs;
        kaa_log_entry_t * record = kaa_list_get_data(head);
        if (record) {
            log_storage->occupied_size -= record->data->size;
            kaa_list_remove_at(&log_storage->logs, head, &destroy_log_record);
        } else {
            break;
        }
    }
}

static void memory_log_storage_destroy()
{
    if (log_storage) {
        kaa_list_destroy(log_storage->logs, &destroy_log_record);
        kaa_list_destroy(log_storage->uploading_blocks, &destroy_memory_log_block);
        KAA_FREE(log_storage);
    }
}

static kaa_log_storage_t public_log_storage_interface = {
        /* add_log_record */    &memory_log_storage_add_log_record,
        /* get_records */       &memory_log_storage_get_records,
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
    return kaa_list_get_size(log_storage->logs);
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
    log_storage = KAA_CALLOC(1, sizeof(kaa_memory_log_storage_t));
    log_storage->occupied_size = 0;
    log_storage->logs = NULL;
    log_storage->uploading_blocks = NULL;
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

kaa_log_upload_decision_t memory_log_storage_is_upload_needed(kaa_storage_status_t *status)
{
    if (status != NULL) {
        if ((*status->get_total_size)() > KAA_MAX_LOG_STORAGE_VOLUME) {
            return CLEANUP;
        }
        if ((*status->get_total_size)() >= KAA_LOG_UPLOAD_THRESHOLD) {
            return UPLOAD;
        }
    }
    return NOOP;
}

#endif

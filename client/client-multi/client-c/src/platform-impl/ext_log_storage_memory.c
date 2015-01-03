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

#include "platform/ext_log_storage.h"

#include <stdint.h>
#include <string.h>

#include "kaa_logging.h"    // FIXME: remove this dependency
#include "collections/kaa_list.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"



typedef struct {
    char       *data;       /**< Serialized data */
    size_t      size;       /**< Size of data */
    uint16_t    bucket_id;  /**< Bucket ID */
} ext_log_record_t;



struct ext_log_storage_t {
    kaa_list_t     *logs;           /**< List of @link ext_log_record_t @endlink */
    size_t          occupied_size;  /**< Currently occupied logs volume */
    kaa_logger_t   *logger;         /**< Logger instance */
};



void log_record_destroy(void *record_p)
{
    if (record_p) {
        KAA_FREE(((ext_log_record_t*)record_p)->data);
        KAA_FREE(record_p);
    }
}



kaa_error_t ext_log_storage_create(ext_log_storage_t** log_storage_p, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL(log_storage_p, KAA_ERR_BADPARAM);

    *log_storage_p = (ext_log_storage_t *) KAA_MALLOC(sizeof(ext_log_storage_t));
    KAA_RETURN_IF_NIL(*log_storage_p, KAA_ERR_NOMEM);

    (*log_storage_p)->logs = NULL;
    (*log_storage_p)->occupied_size = 0;
    (*log_storage_p)->logger = logger;

    return KAA_ERR_NONE;
}



void ext_log_storage_destroy(ext_log_storage_t *self)
{
    if (self) {
        kaa_list_destroy(self->logs, &log_record_destroy);
        KAA_FREE(self);
    }
}



kaa_error_t ext_log_storage_allocate_log_record_buffer(ext_log_storage_t *self, kaa_log_record_t *record)
{
    KAA_RETURN_IF_NIL2(record, record->size, KAA_ERR_BADPARAM);

    record->data = (char *) KAA_MALLOC(record->size * sizeof(char));
    return KAA_ERR_NONE;
}



kaa_error_t ext_log_storage_add_log_record(ext_log_storage_t *self, kaa_log_record_t *record)
{
    KAA_RETURN_IF_NIL2(self, record, KAA_ERR_BADPARAM);

    ext_log_record_t *new_record = (ext_log_record_t *) KAA_MALLOC(sizeof(ext_log_record_t));
    KAA_RETURN_IF_NIL(new_record, KAA_ERR_NOMEM);

    new_record->data = record->data;
    new_record->size = record->size;
    new_record->bucket_id = 0;

    kaa_list_t *record_position = self->logs
            ? kaa_list_push_back(self->logs, new_record)
            : (self->logs = kaa_list_create(new_record));
    if (!record_position) {
        KAA_FREE(new_record);
        return KAA_ERR_NOMEM;
    }

    record->data = NULL;
    record->size = 0;
    self->occupied_size += new_record->size;
    return KAA_ERR_NONE;
}



kaa_error_t ext_log_storage_deallocate_log_record_buffer(ext_log_storage_t *self, kaa_log_record_t *record)
{
    KAA_RETURN_IF_NIL2(record, record->data, KAA_ERR_BADPARAM);

    KAA_FREE(record->data);
    record->data = NULL;
    return KAA_ERR_NONE;
}



bool logs_list_find_by_bucket_id(void *log_record_p, void *bucket_id_p)
{
    return ((ext_log_record_t *)log_record_p)->bucket_id == *((uint16_t *)bucket_id_p);
}



kaa_error_t ext_log_storage_write_next_record(ext_log_storage_t *self, char *buffer, size_t buffer_len
        , uint16_t bucket_id, size_t *record_len)
{
    KAA_RETURN_IF_NIL5(self, buffer, buffer_len, bucket_id, record_len, KAA_ERR_BADPARAM);

    uint16_t zero_bucket_id = 0;
    kaa_list_t *record_position = kaa_list_find_next(self->logs, &logs_list_find_by_bucket_id, &zero_bucket_id);
    if (!record_position) {
        *record_len = 0;
        return KAA_ERR_NOT_FOUND;
    }

    ext_log_record_t *record = (ext_log_record_t *) kaa_list_get_data(record_position);
    *record_len = record->size;
    if (*record_len > buffer_len)
        return KAA_ERR_INSUFFICIENT_BUFFER;

    memcpy((void *)buffer, record->data, record->size);
    record->bucket_id = bucket_id;

    return KAA_ERR_NONE;
}



kaa_error_t ext_log_storage_remove_by_bucket_id(ext_log_storage_t *self, uint16_t bucket_id)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_list_t *record_position = kaa_list_find_next(self->logs, logs_list_find_by_bucket_id, &bucket_id);
    while (record_position) {
        self->occupied_size -= ((ext_log_record_t *)kaa_list_get_data(record_position))->size;
        record_position = kaa_list_remove_at(&self->logs, record_position, &log_record_destroy);
        record_position = kaa_list_find_next(record_position, &logs_list_find_by_bucket_id, &bucket_id);
    }

    return KAA_ERR_NONE;
}



kaa_error_t ext_log_storage_unmark_by_bucket_id(ext_log_storage_t *self, uint16_t bucket_id)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_list_t *record_position = kaa_list_find_next(self->logs, logs_list_find_by_bucket_id, &bucket_id);
    while (record_position) {
        ((ext_log_record_t *)kaa_list_get_data(record_position))->bucket_id = 0;
        record_position = kaa_list_find_next(record_position, &logs_list_find_by_bucket_id, &bucket_id);
    }

    return KAA_ERR_NONE;
}



kaa_error_t ext_log_storage_shrink_to_size(ext_log_storage_t *self, size_t size)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    while (self->occupied_size > size) {
        // May delete records already marked with bucket_id. C'est la vie...
        self->occupied_size -= ((ext_log_record_t *)kaa_list_get_data(self->logs))->size;
        kaa_list_remove_at(&self->logs, self->logs, &log_record_destroy);
    }
    return KAA_ERR_NONE;
}



size_t ext_log_storage_get_total_size(const ext_log_storage_t *self)
{
    KAA_RETURN_IF_NIL(self, 0);
    return self->occupied_size;
}



size_t ext_log_storage_get_records_count(ext_log_storage_t *self)
{
    KAA_RETURN_IF_NIL(self, 0);
    return kaa_list_get_size(self->logs);
}



kaa_error_t ext_log_storage_release(ext_log_storage_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    ext_log_storage_destroy(self);
    return KAA_ERR_NONE;
}






/* FIXME: move into a separate interface
 * Log upload strategy
 */


static const kaa_log_upload_properties_t kaa_memory_log_upload_properties = {
      128   /**< max_log_block_size */
    , 256   /**< max_log_upload_threshold */
    , 1024  /**< max_log_storage_volume */
};



static kaa_log_upload_decision_t memory_log_storage_is_upload_needed(void *context, const ext_log_storage_t *log_storage)
{
    KAA_RETURN_IF_NIL(log_storage, NOOP);

    if (ext_log_storage_get_total_size(log_storage) > kaa_memory_log_upload_properties.max_log_storage_volume)
        return CLEANUP;

    if (ext_log_storage_get_total_size(log_storage) >= kaa_memory_log_upload_properties.max_log_upload_threshold)
        return UPLOAD;

    return NOOP;
}



kaa_error_t kaa_memory_log_storage_get_strategy(ext_log_storage_t *self, kaa_log_upload_strategy_t *strategy)
{
    KAA_RETURN_IF_NIL2(self, strategy, KAA_ERR_BADPARAM);

     *strategy = (kaa_log_upload_strategy_t) {
        NULL,
        &memory_log_storage_is_upload_needed
    };
    return KAA_ERR_NONE;
}

#endif

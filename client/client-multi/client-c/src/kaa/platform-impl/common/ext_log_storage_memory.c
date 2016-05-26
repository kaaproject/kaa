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

#ifndef KAA_DISABLE_FEATURE_LOGGING

#include <stdint.h>
#include <string.h>
#include <platform/ext_log_storage.h>

#include "kaa_common.h"
#include "collections/kaa_list.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"

#include <assert.h>



typedef struct {
    char       *data;       /**< Serialized data */
    size_t      size;       /**< Size of data */
    uint16_t    bucket_id;  /**< Bucket ID */
    bool        mark;       /**< Mark of this record. True means that record is marked */
} ext_log_record_t;

typedef struct {
    kaa_list_t         *logs;                  /**< List of @link ext_log_record_t @endlink */
    kaa_list_node_t    *first_unmarked;        /**< Pointer to the first unmarked record position (with zero bucket_id) */
    size_t             max_storage_size;       /**< Max size of the log storage */
    size_t             total_occupied_size;    /**< Volume occupied by all logs */
    size_t             unmarked_occupied_size ;/**< Volume occupied by unmarked logs */
    size_t             unmarked_record_count;  /**< Number of unmarked logs */
    size_t             shrinked_size;          /**< Percent of elder logs to delete in case max log storage size will be exceeded. */
    kaa_logger_t      *logger;                 /**< Logger instance */
} ext_log_storage_memory_t;



/**
 * @brief Creates the size-unlimited instance of the memory log storage.
 *
 * @param[out]    log_storage_context_p    The pointer to the new storage instance.
 * @param[in]     logger                   The logger.
 *
 * @return    Error code.
 */
kaa_error_t ext_unlimited_log_storage_create(void **log_storage_context_p, kaa_logger_t *logger);



/**
 * @brief Creates the size-limited instance of the memory log storage.
 *
 * @param[out]    log_storage_context_p    The pointer to the new storage instance.
 * @param[in]     logger                   The logger.
 * @param[in]     max_storage_size             The maximum storage size.
 * @param[in]     percent_to_delete        The percentage of elder logs to delete if the maximum storage size.
 *
 * @return    Error code.
 */
kaa_error_t ext_limited_log_storage_create(void **log_storage_context_p
                                         , kaa_logger_t *logger
                                         , size_t storage_size
                                         , size_t percent_to_delete);



/**
 * @brief Destroys the instance of the memory log storage.
 *
 * @param[in]   context The log storage context.
 * @return    Error code.
 */
kaa_error_t ext_log_storage_destroy(void *context);



static void log_record_destroy(void *record_p)
{
    KAA_RETURN_IF_NIL(record_p, );
    KAA_FREE(((ext_log_record_t*)record_p)->data);
    KAA_FREE(record_p);
}



kaa_error_t ext_unlimited_log_storage_create(void **log_storage_context_p, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL2(log_storage_context_p, logger, KAA_ERR_BADPARAM);

    ext_log_storage_memory_t *log_storage = KAA_MALLOC(sizeof(ext_log_storage_memory_t));
    KAA_RETURN_IF_NIL(log_storage, KAA_ERR_NOMEM);

    log_storage->logger                 = logger;
    log_storage->first_unmarked         = NULL;
    log_storage->max_storage_size       = 0;
    log_storage->total_occupied_size    = 0;
    log_storage->unmarked_occupied_size = 0;
    log_storage->unmarked_record_count  = 0;
    log_storage->shrinked_size          = 0;

    log_storage->logs = kaa_list_create();
    if (!log_storage->logs) {
        ext_log_storage_destroy(log_storage);
        return KAA_ERR_NOMEM;
    }

    *log_storage_context_p = (void *)log_storage;
    return KAA_ERR_NONE;
}



kaa_error_t ext_limited_log_storage_create(void **log_storage_context_p
                                         , kaa_logger_t *logger
                                         , size_t storage_size
                                         , size_t percent_to_delete)
{
    KAA_RETURN_IF_NIL4(log_storage_context_p, logger, storage_size, percent_to_delete, KAA_ERR_BADPARAM);

    if (percent_to_delete > 100) {
        KAA_LOG_WARN(logger, KAA_ERR_BADPARAM, "Failed to create log storage: percentage of logs "
                                                    "to remove is more than 100%% (%u%%)", percent_to_delete);
        return KAA_ERR_BADPARAM;
    }

    kaa_error_t error_code = ext_unlimited_log_storage_create(log_storage_context_p, logger);
    KAA_RETURN_IF_ERR(error_code);

    ext_log_storage_memory_t *log_storage = (ext_log_storage_memory_t *)*log_storage_context_p;

    log_storage->max_storage_size = storage_size;
    log_storage->shrinked_size    = (log_storage->max_storage_size * (100 - percent_to_delete)) / 100;

    return KAA_ERR_NONE;
}



kaa_error_t ext_log_storage_allocate_log_record_buffer(void *context, kaa_log_record_t *record)
{
    (void)context;
    KAA_RETURN_IF_NIL2(record, record->size, KAA_ERR_BADPARAM);

    record->data = (char *) KAA_MALLOC(record->size * sizeof(char));
    KAA_RETURN_IF_NIL(record->data, KAA_ERR_NOMEM);

    return KAA_ERR_NONE;
}



static kaa_error_t shrink_to_size(void *context, size_t size)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    ext_log_storage_memory_t *self = (ext_log_storage_memory_t *)context;
    size_t removed_record_count = 0;

    kaa_list_node_t *it = kaa_list_begin(self->logs);
    while (it && self->total_occupied_size > size) {
        // May delete records already marked. C'est la vie...
        ext_log_record_t *log_record = kaa_list_get_data(it);

        self->total_occupied_size -= log_record->size;
        ++removed_record_count;

        kaa_list_node_t *next_it = kaa_list_next(it);
        if (!log_record->mark) {
            self->unmarked_occupied_size -= log_record->size;
            --self->unmarked_record_count;
            self->first_unmarked = next_it;
        }

        kaa_list_remove_at(self->logs, it, &log_record_destroy);
        it = next_it;
    }

    if (kaa_list_get_size(self->logs) > 0) {
        self->first_unmarked = NULL;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "%zu records forcibly removed", removed_record_count);

    return KAA_ERR_NONE;
}



kaa_error_t ext_log_storage_add_log_record(void *context, kaa_log_record_t *record)
{
    KAA_RETURN_IF_NIL2(context, record, KAA_ERR_BADPARAM);
    ext_log_storage_memory_t *self = (ext_log_storage_memory_t *)context;

    if (self->max_storage_size && (self->total_occupied_size + record->size) > self->max_storage_size) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Log storage is full (occupied %zu, max %zu, record size %zu). "
                            "Going to delete elder logs", self->total_occupied_size, self->max_storage_size, record->size);

        shrink_to_size(self, self->shrinked_size);
    }

    ext_log_record_t *new_record = KAA_MALLOC(sizeof(*new_record));
    KAA_RETURN_IF_NIL(new_record, KAA_ERR_NOMEM);

    new_record->data = record->data;
    new_record->size = record->size;
    new_record->bucket_id = record->bucket_id;
    assert(record->bucket_id);
    new_record->mark = false;

    if (!kaa_list_push_back(self->logs, new_record)) {
        KAA_FREE(new_record);
        return KAA_ERR_NOMEM;
    }

    self->total_occupied_size += new_record->size;
    self->unmarked_occupied_size += new_record->size;
    self->unmarked_record_count++;

    record->data = NULL;
    record->size = 0;

    return KAA_ERR_NONE;
}



kaa_error_t ext_log_storage_deallocate_log_record_buffer(void *context, kaa_log_record_t *record)
{
    (void)context;
    KAA_RETURN_IF_NIL2(record, record->data, KAA_ERR_BADPARAM);

    KAA_FREE(record->data);
    record->data = NULL;
    return KAA_ERR_NONE;
}

static bool find_by_bucket_id(void *log_record_p, void *bucket_id_p)
{
    KAA_RETURN_IF_NIL2(log_record_p, bucket_id_p, false);
    ext_log_record_t *record = log_record_p;

    // Iterate only marked log records
    return record->mark && record->bucket_id == *(uint16_t *)bucket_id_p;
}

static bool find_marked(void *log_record_p, void *mark)
{
    return ((ext_log_record_t *)log_record_p)->mark == *(bool *)mark;
}

kaa_error_t ext_log_storage_write_next_record(void *context
                                            , char *buffer
                                            , size_t buffer_len
                                            , uint16_t *bucket_id
                                            , size_t *record_len)
{
    KAA_RETURN_IF_NIL4(context, buffer, buffer_len, record_len, KAA_ERR_BADPARAM);
    ext_log_storage_memory_t *self = context;

    kaa_list_node_t *it = self->first_unmarked;
    if (!it) {
        bool mark = false;
        it = kaa_list_find_next(kaa_list_begin(self->logs), find_marked, &mark);
    }

    if (!it) {
        *record_len = 0;
        return KAA_ERR_NOT_FOUND;
    }

    ext_log_record_t *record = kaa_list_get_data(it);
    *record_len = record->size;
    if (*record_len > buffer_len)
        return KAA_ERR_INSUFFICIENT_BUFFER;

    // Only unmarked buckets with valid id can be written
    assert(record->bucket_id);
    assert(!record->mark);

    memcpy(buffer, record->data, record->size);

    if (bucket_id) {
        *bucket_id = record->bucket_id;
    }

    record->mark = true;

    self->first_unmarked = kaa_list_next(it);
    self->unmarked_record_count--;
    self->unmarked_occupied_size -= record->size;

    return KAA_ERR_NONE;
}



kaa_error_t ext_log_storage_remove_by_bucket_id(void *context, uint16_t bucket_id)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    ext_log_storage_memory_t *self = context;

    kaa_list_node_t *it = kaa_list_find_next(kaa_list_begin(self->logs), find_by_bucket_id, &bucket_id);
    KAA_RETURN_IF_NIL(it, KAA_ERR_NOT_FOUND);

    while (it) {
        self->total_occupied_size -= ((ext_log_record_t *)kaa_list_get_data(it))->size;

        it = kaa_list_remove_at(self->logs, it, &log_record_destroy);
        it = kaa_list_find_next(it, &find_by_bucket_id, &bucket_id);
    }

    if (!kaa_list_get_size(self->logs)) {
        self->first_unmarked = NULL;
    }

    return KAA_ERR_NONE;
}



kaa_error_t ext_log_storage_unmark_by_bucket_id(void *context, uint16_t bucket_id)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    ext_log_storage_memory_t *self = context;

    kaa_list_node_t *it = kaa_list_find_next(kaa_list_begin(self->logs), find_by_bucket_id, &bucket_id);
    KAA_RETURN_IF_NIL(it, KAA_ERR_NOT_FOUND);

    while (it) {
        ext_log_record_t *log_record = kaa_list_get_data(it);

        log_record->mark = false;
        self->unmarked_record_count++;
        self->unmarked_occupied_size += log_record->size;

        it = kaa_list_find_next(it, &find_by_bucket_id, &bucket_id);
    }

    self->first_unmarked = NULL;

    return KAA_ERR_NONE;
}



size_t ext_log_storage_get_total_size(const void *context)
{
    KAA_RETURN_IF_NIL(context, 0);
    return ((ext_log_storage_memory_t *)context)->unmarked_occupied_size;
}



size_t ext_log_storage_get_records_count(const void *context)
{
    KAA_RETURN_IF_NIL(context, 0);
    return ((ext_log_storage_memory_t *)context)->unmarked_record_count;
}



kaa_error_t ext_log_storage_destroy(void *context)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    ext_log_storage_memory_t *self = context;
    kaa_list_destroy(self->logs, &log_record_destroy);
    KAA_FREE(self);
    return KAA_ERR_NONE;
}

#endif

/* ISO C forbids an empty translation unit */
typedef int make_iso_compilers_happy;

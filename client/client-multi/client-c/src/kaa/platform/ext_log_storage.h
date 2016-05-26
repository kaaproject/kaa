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

/**
 * @file ext_log_storage.h
 * @brief External log storage interface used by Kaa data collection subsystem to temporarily store the logs
 * before sending them to Operations server.
 * Must be implemented in a concrete application for the data collection feature to function.
 */

#ifndef EXT_LOG_STORAGE_H_
#define EXT_LOG_STORAGE_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <stddef.h>

#include "kaa_error.h"


/**
 * Wrapper for a serialized log entry.
 */
typedef struct {
    char        *data;      /**< Serialized data */
    size_t      size;       /**< Size of data */
    uint16_t    bucket_id;  /**< Bucket ID of this record */
} kaa_log_record_t;



/**
 * @brief Allocates the data buffer to serialize a log entry into.
 *
 * Allocates the @c data buffer of @c size bytes in the @c record.
 *
 * @param[in]       context     Log storage context.
 * @param[in,out]   record      Log record to allocate buffer to.
 *
 * @return Error code.
 */
kaa_error_t ext_log_storage_allocate_log_record_buffer(void *context, kaa_log_record_t *record);



/**
 * @brief Deallocates the data buffer of a log record.
 *
 * Deallocates the @c data buffer in the @c record and sets @c size to 0.
 *
 * @param[in]       context     Log storage context.
 * @param[in,out]   record      Log record to deallocate buffer of.
 *
 * @return Error code.
 */
kaa_error_t ext_log_storage_deallocate_log_record_buffer(void *context, kaa_log_record_t *record);



/**
 * @brief Adds a log entry to the log storage.
 *
 * In case of success assumes ownership of the @c record @c data buffer.
 * (No need to call @link ext_log_storage_deallocate_log_record_buffer @endlink.)
 *
 * @param[in]       context     Log storage context.
 * @param[in]       record      Log record to add to log storage.
 *
 * @return Error code.
 */
kaa_error_t ext_log_storage_add_log_record(void *context, kaa_log_record_t *record);

/**
 * @brief Writes the next unmarked log entry into the supplied buffer
 * and marks it with @p bucket_id the record size does not exceed the
 * @p buffer_len.
 *
 * @param[in]       context     Log storage context.
 * @param[in]       buffer      Buffer to write next unmarked record into.
 * @param[in]       buffer_len  Buffer length in bytes.
 * @param[out]      bucket_id   Optional bucket ID of the next record.
 * @param[out]      record_len  Size of the record data.
 *
 * @return Error code
 *
 * @retval KAA_ERR_NONE a record of @p record_len was successfully
 * written into the buffer.
 *
 * @retval KAA_ERR_NOT_FOUND there were no unmarked records in the
 * storage. @p record_len is set to 0 in this case.
 *
 * @retval KAA_ERR_INSUFFICIENT_BUFFER the buffer size was not
 * sufficient to fit in the next unmarked entry. @c record_len is set
 * to the size of the record that was not written.
 */
kaa_error_t ext_log_storage_write_next_record(void *context, char *buffer, size_t buffer_len, uint16_t *bucket_id, size_t *record_len);



/**
 * @brief Removes from the storage all records marked with the provided @c bucket_id.
 *
 * @param[in]       context     Log storage context.
 * @param[in]       bucket_id   Non-zero bucket ID to search for records to be removed.
 *
 * @return Error code
 */
kaa_error_t ext_log_storage_remove_by_bucket_id(void *context, uint16_t bucket_id);



/**
 * @brief Unmarks all records marked with the provided @c bucket_id.
 *
 * @param[in]       context     Log storage context.
 * @param[in]       bucket_id   Non-zero bucket ID to search for records to be unmarked.
 *
 * @return Error code
 */
kaa_error_t ext_log_storage_unmark_by_bucket_id(void *context, uint16_t bucket_id);



/**
 * @brief Returns total size occupied by logs in the log storage.
 *
 * @param[in]       context     Log storage context.
 *
 * @return Total log storage size in bytes, occupied by log records. Zero in case of errors.
 */
size_t ext_log_storage_get_total_size(const void *context);



/**
 * @brief Returns total log records count.
 *
 * @param[in]       context     Log storage context.
 *
 * @return Total amount of log records in the storage. Zero in case of errors.
 */
size_t ext_log_storage_get_records_count(const void *context);



/**
 * @brief Destroys the log storage (which may decide to self-destroy).
 *
 * @param[in]       context     Log storage context.
 *
 * @return Error code
 */
kaa_error_t ext_log_storage_destroy(void *context);

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* EXT_LOG_STORAGE_H_ */

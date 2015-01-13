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

/**
 * @file kaa_logging.h
 * @brief Kaa data logging subsystem API
 *
 * Supplies API for Kaa data collection / logging subsystem.
 */

# ifndef KAA_LOGGING_H_
# define KAA_LOGGING_H_

# ifndef KAA_DISABLE_FEATURE_LOGGING

# include <stddef.h>
# include "gen/kaa_logging_gen.h"

# ifdef __cplusplus
extern "C" {
# endif



/**
 * Log records structure generated based on the specified data schema.
 */
typedef kaa_logging_log_data_t kaa_user_log_record_t;



/**
 * Private log collector structure.
 */
typedef struct kaa_log_collector kaa_log_collector_t;



/**
 * Wrapper for a serialized log entry.
 */
typedef struct {
    uint8_t  *record_data;  /**< Serialized data */
    uint32_t  record_size;  /**< Size of data */
} kaa_log_entry_t;



/**
 * Interface for the client log storage implementation. Kaa logging subsystem uses this interface to temporarily store
 * the logs before sending them to Operations server.
 */
typedef struct {
    void             * context;                                                                     /**< Context to pass to all functions below. */
    // FIXME: return kaa_error_t instead of void everywhere
    void            (* add_log_record)    (void *context, kaa_log_entry_t record);                  /**< Adds log entry to the log storage. */
    kaa_log_entry_t (* get_next_record)   (void *context, uint16_t bucket_id, size_t size_limit);   /**< Returns the next log entry and marks it with @c bucket_id @b only if the record size does not exceed the @c size_limit. */
    void            (* upload_succeeded)  (void *context, uint16_t bucket_id);                      /**< Removes from the storage all records marked with the provided @c bucket_id. */
    void            (* upload_failed)     (void *context, uint16_t bucket_id);                      /**< Unmarks all records marked with the provided @c bucket_id. */
    void            (* shrink_to_size)    (void *context, size_t size);                             /**< Shrinks log storage to the specified size. */
    size_t          (* get_total_size)    (void *context);                                          /**< Returns total size occupied by logs in the log storage. */
    uint16_t        (* get_records_count) (void *context);                                          /**< Returns total log records count. */
    void            (* release)           (void *context);                                          /**< Releases the log storage (which may self-destroy). */
} kaa_log_storage_t;



/**
 * Log upload decisions.
 */
typedef enum {
    NOOP    = 0, /**< Nothing to do yet. */
    UPLOAD  = 1, /**< Trigger log upload. */
    CLEANUP = 2  /**< Trigger log storage cleanup. */
} kaa_log_upload_decision_t;



/**
 * Interface for the client log upload strategy.
 */
typedef struct {
    void                       * context;                                                                       /**< Context to pass to all functions below. */
    kaa_log_upload_decision_t (* log_upload_decision_fn) (void *context, const kaa_log_storage_t *log_storage); /**< Makes a decision whether to upload logs or cleanup storage. */
} kaa_log_upload_strategy_t;



/**
 * Logging subsystem settings.
 */
typedef struct {
    uint32_t max_log_bucket_size;       /**< Maximum volume of logs in a single upload request (in bytes). */
    uint32_t max_log_upload_threshold; /**< Volume of the collected logs (in bytes) to trigger upload. */
    uint32_t max_log_storage_volume;   /**< Maximum allowed log records volume (in bytes). */
} kaa_log_upload_properties_t;



/**
 * @brief Initializes data collection module with the storage interface, upload strategy, and other settings.
 *
 * @param[in] self              Pointer to a @link kaa_log_collector_t @endlink instance.
 * @param[in] storage           Log storage interface.
 * @param[in] upload_strategy   Log upload strategy interface.
 * @param[in] properties        Log upload properties structure.
 *
 * @return      Error code.
 */
kaa_error_t kaa_logging_init(kaa_log_collector_t *self
                           , const kaa_log_storage_t *storage
                           , const kaa_log_upload_strategy_t *upload_strategy
                           , const kaa_log_upload_properties_t *properties);



/**
 * @brief Serializes and adds a log record to the log storage.
 *
 * @param[in] self    Pointer to a @link kaa_log_collector_t @endlink instance.
 * @param[in] entry   Valid pointer to log entry to be added to the storage.
 *
 * @return      Error code.
 *
 */
kaa_error_t kaa_logging_add_record(kaa_log_collector_t *self, kaa_user_log_record_t *entry);

# ifdef __cplusplus
}      /* extern "C" */
# endif

# endif /* KAA_DISABLE_FEATURE_LOGGING */

# endif /* KAA_LOGGING_H_ */

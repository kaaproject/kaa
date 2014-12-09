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

#ifndef KAA_LOGGING_H_
#define KAA_LOGGING_H_

#ifndef KAA_DISABLE_FEATURE_LOGGING
#include <stddef.h>
#include "gen/kaa_endpoint_gen.h"
#include "gen/kaa_logging_gen.h"
#include "kaa_uuid.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef kaa_test_log_record_t               kaa_user_log_record_t;
typedef struct kaa_log_collector            kaa_log_collector_t;

void                destroy_log_record(void *record_p);

/**
 * Interface for log storage.
 * Functions:
 * 1. Add log enrty to log storage.
 *      void add_log_record(kaa_log_entry_t * record);
 * 2. Return kaa_list_t * of record objects total records size should be LESS OR EQUAL than max_size.
 *    Log records list will be further addressed using kaa_uuid_t object.
 *      kaa_list_t * get_records(kaa_uuid_t uuid, size_t max_size);
 * 3. Remove records when upload is successful.
 *      void upload_succeeded(kaa_uuid_t uuid);
 * 4. Notify records block upload failed.
 *      void upload_failed(kaa_uuid_t uuid);
 * 5. Shrink log storage to specified size.
 *      void shrink_to_size(size_t size);
 * 6. Destroy log storage.
 *      void destroy(void);
 */
typedef struct kaa_log_storage_t {
    void            (* add_log_record)  (kaa_log_entry_t * record);
    kaa_list_t *    (* get_records)     (kaa_uuid_t uuid, size_t max_size);
    void            (* upload_succeeded)(kaa_uuid_t uuid);
    void            (* upload_failed)   (kaa_uuid_t uuid);
    void            (* shrink_to_size)  (size_t size);
    void            (* destroy)         (void);
} kaa_log_storage_t;

/**
 * Interface for accessing log storage actual state information.
 * Functions:
 * 1. Get total size occupied by logs in log storage.
 *      size_t get_total_size(void);
 * 2. Get total log records count.
 *      size_t get_records_count)(void);
 */
typedef struct kaa_storage_status_t {
    size_t          (* get_total_size)(void);
    size_t          (* get_records_count)(void);
} kaa_storage_status_t;

typedef struct kaa_log_upload_properties_t {
    /**
     * Maximal amount of logs for single upload request (in bytes).
     */
    uint32_t    max_log_block_size;
    /**
     * Amount of collected logs (in bytes) when upload should be triggered.
     */
    uint32_t    max_log_upload_threshold;
    /**
     * Maximal size which can be occupied by log records (in bytes).
     */
    uint32_t    max_log_storage_volume;
} kaa_log_upload_properties_t;

typedef enum kaa_log_upload_decision_t {
    NOOP        = 0, // No operation should be performed now.
    UPLOAD      = 1, // Log upload should be triggered.
    CLEANUP     = 2  // Need to cleanup log storage to fit available space.
} kaa_log_upload_decision_t;

typedef kaa_log_upload_decision_t (* log_upload_decision_fn)(kaa_storage_status_t *);

/**
 * @brief Provide log storage to Kaa.
 *
 * @param[in]   i_storage   Structure containing pointers to functions which are used
 * to manage log storage.
 * @param[in]   i_status      Structure containing pointers to functions describing
 * state of the storage (occupied size, records count etc.)
 * @param[in]   upload_properties     Properties which are used to control log storage
 * size and log upload neediness.
 * @param[in]   s_upload_needed  Pointer to function which will be used to decide
 * which operation (NO_OPERATION, UPLOAD or CLEANUP) should be performed on log storage.
 *
 * @return      Error code.
 */
kaa_error_t                 kaa_logging_init(
                                                    kaa_log_collector_t *
                                                  , kaa_log_storage_t *
                                                  , kaa_log_upload_properties_t *
                                                  , kaa_storage_status_t *
                                                  , log_upload_decision_fn
                                                  );

/**
 * @brief Add log record to log storage.
 *
 * Use this to add the log entry to the predefined log storage.
 * Log record will be serialized and pushed to a log storage interface via
 * <pre>
 * void            (* add_log_record)  (kaa_log_entry_t * record);
 * </pre>
 * See also @see kaa_log_storage_t.
 *
 * @param[in]   self    Valid pointer to log collector instance.
 * @param[in]   entry   Valid pointer to log entry which is going to be added.
 *
 * @return      Error code.
 *
 */
kaa_error_t                 kaa_logging_add_record(kaa_log_collector_t *self, kaa_user_log_record_t *entry);

#ifdef __cplusplus
} // extern "C"
#endif

#endif

#endif /* KAA_LOGGING_H_ */

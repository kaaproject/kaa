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
 * @file kaa_logging.h
 * @brief Kaa data logging subsystem API
 *
 * Supplies API for Kaa data collection / logging subsystem.
 */

#ifndef KAA_LOGGING_H_
#define KAA_LOGGING_H_


#include "gen/kaa_logging_definitions.h"
#include "platform/ext_log_storage.h"
#include "platform/ext_log_upload_strategy.h"
#include "platform/ext_log_delivery_listener.h"

#ifdef __cplusplus
extern "C" {
#endif


/**
 * Private log collector structure.
 */
struct kaa_log_collector_t;
typedef struct kaa_log_collector_t kaa_log_collector_t;

/**
 * @brief Log record info.
 *
 * Each log is contained in the bucket. Bucket is used to agreggate
 * multiple logs into one entity that will be atomically sent to the server.
 * Bucket can either be entirely successfully sent or be entirely failed.
 * Corresponding events are generated. User may subscribe to them.
 * @sa kaa_log_event_fn
 * @sa kaa_log_listeners_t
 * @sa kaa_logging_set_listeners
 */
typedef struct {
    uint32_t log_id;    /**< Id of a log record processed by kaa_logging_add_record() */
    uint16_t bucket_id; /**< Id of a bucket where a log record contained */
} kaa_log_record_info_t;

/** Constraints applied to log buckets */
typedef struct {
    size_t max_bucket_size;         /**< The maximum bucket size in bytes */
    size_t max_bucket_log_count;    /**< The maximum log count within a single bucket */
} kaa_log_bucket_constraints_t;

/**
 * @brief Initializes data collection module with the storage interface, upload strategy, and other settings.
 *
 * @param[in] self                          Pointer to a @link kaa_log_collector_t @endlink instance.
 * @param[in] log_storage_context           Log storage context.
 * @param[in] log_upload_strategy_context   Log upload strategy context.
 * @param[in] bucket_sizes                  Bucket size constraints.
 *
 * @return  Error code.
 */
kaa_error_t kaa_logging_init(kaa_log_collector_t *self, void *log_storage_context, void *log_upload_strategy_context, const kaa_log_bucket_constraints_t *bucket_sizes);

/**
 * @brief Sets custom strategy for given collector.
 *
 * If a strategy has been assigned to collector previously then it will be
 * destroyed and new strategy will be assigned.
 *
 * @param[in] self                          Pointer to a @link kaa_log_collector_t @endlink instance.
 * @param[in] log_upload_strategy_context   Log storage context.
 *
 * @return  Error code.
 */
kaa_error_t kaa_logging_set_strategy(kaa_log_collector_t *self, void *log_upload_strategy_context);

/**
 * @brief Sets custom storage for given collector.
 *
 * If a storage has been assigned to collector previously then it will be
 * destroyed and new storage will be assigned. Be aware that all items from
 * previous storage will be deleted.
 *
 * @param[in] self                          Pointer to a @link kaa_log_collector_t @endlink instance.
 * @param[in] log_storage_context           Log storage context.
 *
 * @return  Error code.
 */
kaa_error_t kaa_logging_set_storage(kaa_log_collector_t *self, void *log_storage_context);

/**
 * @brief Serializes and adds a log record to the log storage.
 *
 * @param[in]  self      Pointer to a @link kaa_log_collector_t @endlink instance.
 * @param[in]  entry     Pointer to log entry to be added to the storage.
 * @param[out] log_info  Pointer to log info. May be @c NULL.
 *
 * @return  Error code.
 */
kaa_error_t kaa_logging_add_record(kaa_log_collector_t *self, kaa_user_log_record_t *entry, kaa_log_record_info_t *log_info);

/**
 * @brief Sets listeners of log events.
 *
 * @param[in] self       Pointer to a @link kaa_log_collector_t @endlink instance.
 * @param[in] listeners  Pointer to listeners that will be used to handle
 *                       various log delivery events. @sa KAA_LOG_EMPTY_LISTENERS
 *                       can be used to unsubscribe from log events.
 * @return  Error code.
 */
kaa_error_t kaa_logging_set_listeners(kaa_log_collector_t *self, const kaa_log_delivery_listener_t *listeners);

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* KAA_LOGGING_H_ */

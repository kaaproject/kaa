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

#ifndef KAA_LOGGING_H_
#define KAA_LOGGING_H_


#include "gen/kaa_logging_definitions.h"
#include "platform/ext_log_storage.h"
#include "platform/ext_log_upload_strategy.h"

#ifdef __cplusplus
extern "C" {
#endif


#ifndef KAA_LOG_COLLECTOR_T
# define KAA_LOG_COLLECTOR_T
    /**
     * Private log collector structure.
     */
    typedef struct kaa_log_collector        kaa_log_collector_t;
#endif

/** Log bucket information structure. */
typedef struct
{
    uint16_t bucket_id;     /**< ID of bucket present in storage. */
    size_t   log_cnt;       /**< Current logs count. */
} kaa_log_bucket_info_t;

/**
 * @brief Event handler type.
 *
 * @param[in,out]  ctx    User-definied context. @sa kaa_logging_add_record
 * @param[in]      bucket Log bucket for which event was triggered.
 */
typedef void (*kaa_log_event_fn)(void *ctx, const kaa_log_bucket_info_t *bucket);

/** Listeners aggreate */
typedef struct
{
    kaa_log_event_fn on_success; /**< Handler called upon successfull log delivery. */
    kaa_log_event_fn on_failed;  /**< Handler called upon  failed delivery. */
    kaa_log_event_fn on_timeout; /**< Handler called upon  timeouted delivery. */
    void *ctx;                   /**< User-defined context. */
} kaa_log_listeners_t;

/** Special macro that can be used to disable event handling. */
#define KAA_LOG_EMPTY_LISTENERS ((kaa_log_listener_t){NULL, NULL, NULL, NULL})

/**
 * @brief Initializes data collection module with the storage interface, upload strategy, and other settings.
 *
 * @param[in] self                          Pointer to a @link kaa_log_collector_t @endlink instance.
 * @param[in] log_storage_context           Log storage context.
 * @param[in] log_upload_strategy_context   Log upload strategy context.
 *
 * @return  Error code.
 */
kaa_error_t kaa_logging_init(kaa_log_collector_t *self, void *log_storage_context, void *log_upload_strategy_context);

/**
 * @brief Serializes and adds a log record to the log storage.
 *
 * @param[in]  self    Pointer to a @link kaa_log_collector_t @endlink instance.
 * @param[in]  entry   Pointer to log entry to be added to the storage.
 * @param[out] bucket  Pointer to log bucket info. May be NULL.
 *
 * @return  Error code.
 */
kaa_error_t kaa_logging_add_record(kaa_log_collector_t *self, kaa_user_log_record_t *entry, uint16_t *bucket_id);

/**
 * @brief Sets listeners of log events.
 *
 * @param[in] self       Pointer to a @link kaa_log_collector_t @endlink instance.
 * @param[in] listeners  Pointer to listeners that will be used to handle
 *                       various log delivery events. @sa KAA_LOG_EMPTY_LISTENERS
 *                       can be used to unsubscribe from log events.
 * @return  Error code.
 */
kaa_error_t kaa_logging_set_listeners(kaa_log_collector_t *self, const kaa_log_listeners_t *listeners);

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* KAA_LOGGING_H_ */

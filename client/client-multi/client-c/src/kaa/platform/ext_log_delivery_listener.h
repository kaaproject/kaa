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
 * @file
 * @brief External log delivery listener interfaces.
 *
 * Listener callbacks could be used to notify about log-releated events:
 * success, fail or timeout.
 *
 */

#ifndef EXT_LOG_DELIVERY_LISTENER_
#define EXT_LOG_DELIVERY_LISTENER_


#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Log bucket information structure.
 * One or more log records are aggregated into the single bucket.
 */
typedef struct {
    size_t   log_count;     /**< Logs associated with a bucket. */
    uint16_t bucket_id;     /**< ID of bucket present in storage. */
} kaa_log_bucket_info_t;

/**
 * @brief Event handler type.
 *
 * Bucket information can be used to retrieve a amount of logs that are
 * pending to upload.
 *
 * @param[in,out]  context    User-definied context. @sa kaa_logging_add_record
 * @param[in]      bucket     Log bucket for which event was triggered.
 */
typedef void (*kaa_log_event_fn)(void *context, const kaa_log_bucket_info_t *bucket);

/** Listeners aggreate */
typedef struct {
    kaa_log_event_fn on_success; /**< Handler called upon successfull log delivery. */
    kaa_log_event_fn on_failed;  /**< Handler called upon failed delivery. */
    kaa_log_event_fn on_timeout; /**< Handler called upon timeouted delivery. */
    void *ctx;                   /**< User-defined context. */
} kaa_log_delivery_listener_t;

/** Special macro that can be used to disable event handling. */
#define KAA_LOG_EMPTY_LISTENERS ((kaa_log_delivery_listener_t){NULL, NULL, NULL, NULL})

#ifdef __cplusplus
}      /* extern "C" */
#endif



#endif // EXT_LOG_DELIVERY_LISTENER_

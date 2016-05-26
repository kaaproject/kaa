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
 * @file ext_log_upload_strategy.h
 * @brief External log upload strategy interface used by Kaa data collection subsystem to decide when to upload or
 * cleanup logs.
 * Must be implemented in a concrete application for the data collection feature to function.
 */

#ifndef EXT_LOG_UPLOAD_STRATEGY_H_
#define EXT_LOG_UPLOAD_STRATEGY_H_

#include "platform/ext_log_storage.h"

#ifdef __cplusplus
extern "C" {
#endif



/**
 * Log upload decisions.
 */
typedef enum {
    NOOP    = 0, /**< Nothing to do yet. */
    UPLOAD  = 1  /**< Trigger log upload. */
} ext_log_upload_decision_t;

/**
 * Log delivery error codes.
 */
typedef enum {
    NO_APPENDERS_CONFIGURED = 0x00,
    APPENDER_INTERNAL_ERROR = 0x01,
    REMOTE_CONNECTION_ERROR = 0x02,
    REMOTE_INTERNAL_ERROR   = 0x03
} logging_delivery_error_code_t;

/**
 * @brief Creates the new instance of the log upload strategy based on volume and count of collected logs.
 *
 * @param   context       The Kaa context.
 * @param   strategy_p    The pointer to a new strategy instance.
 * @param   type          The strategy type.
 * @return Error code.
 */
struct kaa_context_s;
kaa_error_t ext_log_upload_strategy_create(struct kaa_context_s *context, void **strategy_p, uint8_t type);

/**
 * @brief Makes a decision whether to upload logs or cleanup the storage.
 *
 * @param[in]       context             Log upload strategy context.
 * @param[in]       log_storage_context Log storage instance to operate against.
 *
 * @return Log upload decision.
 */
ext_log_upload_decision_t ext_log_upload_strategy_decide(void *context, const void *log_storage_context);

/**
 * @brief The maximum time to wait a log delivery response.
 *
 * @param[in]   context    Log upload strategy context.
 * @return                 Time in seconds.
 */
size_t ext_log_upload_strategy_get_timeout(void *context);



/**
 * @brief Max amount of log batches allowed to be uploaded parallel.
 *
 * @param[in]   context    Log upload strategy context.
 * @return                 Amount of batches.
 */
size_t ext_log_upload_strategy_get_max_parallel_uploads(void *context);



/**
 * @brief Handles timeout of a log delivery.
 *
 * @param[in]   context    Log upload strategy context.
 * @return Error code.
 */
kaa_error_t ext_log_upload_strategy_on_timeout(void *context);



/**
 * @brief Handles failure of a log delivery.
 *
 * @param[in]   context       Log upload strategy context.
 * @param[in]   error_code    Delivery error code.
 * @return Error code.
 */
kaa_error_t ext_log_upload_strategy_on_failure(void *context, logging_delivery_error_code_t error_code);



/**
 * @brief Destroys the instance of the log upload strategy.
 *
 * @param[in]   context The log strategy context.
 * @return    Error code.
 */
void ext_log_upload_strategy_destroy(void *context);



#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* EXT_LOG_UPLOAD_STRATEGY_H_ */

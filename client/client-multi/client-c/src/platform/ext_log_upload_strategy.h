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
 * @file ext_log_upload_strategy.h
 * @brief External log upload strategy interface used by Kaa data collection subsystem to decide when to upload or
 * cleanup logs.
 * Must be implemented in a concrete application.
 */

#ifndef EXT_LOG_UPLOAD_STRATEGY_H_
#define EXT_LOG_UPLOAD_STRATEGY_H_

#include "../platform/ext_log_storage.h"



/**
 * Private log upload strategy structure. Must be defined in the concrete log upload strategy implementation.
 */
typedef struct ext_log_upload_strategy_t ext_log_upload_strategy_t;



/**
 * Log upload decisions.
 */
typedef enum {
    NOOP    = 0, /**< Nothing to do yet. */
    UPLOAD  = 1, /**< Trigger log upload. */
    CLEANUP = 2  /**< Trigger log storage cleanup. */
} ext_log_upload_decision_t;



/**
 * @brief Makes a decision whether to upload logs or cleanup the storage.
 *
 * @param[in]       self        Log upload strategy instance.
 * @param[in]       log_storage Log storage instance to operate against.
 * @param[out]      volume      Volume of logs to process (in bytes). Zero if decision is @c NOOP.
 *
 * @return Log upload decision.
 */
ext_log_upload_decision_t ext_log_upload_strategy_decide(ext_log_upload_strategy_t *self
        , const ext_log_storage_t *log_storage, size_t *volume);

#endif /* EXT_LOG_UPLOAD_STRATEGY_H_ */

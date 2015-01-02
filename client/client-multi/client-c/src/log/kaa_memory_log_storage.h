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
 * @file kaa_memory_log_storage.h
 * @brief Kaa in-memory logs storage implementation
 *
 * Provides a sample implementation of Kaa C EP SDK log storage that does in-memory temporary persistence of log records.
 */

#ifndef KAA_MEMORY_LOG_STORAGE_H_
#define KAA_MEMORY_LOG_STORAGE_H_

#ifndef KAA_DISABLE_FEATURE_LOGGING

#ifdef __cplusplus
extern "C" {
#endif

#include "kaa_logging.h"
#include "utilities/kaa_log.h"



/**
 * Private in-memory log storage structure
 */
typedef struct kaa_memory_log_storage_t kaa_memory_log_storage_t;



/**
 * @brief Creates an in-memory log storage
 *
 * @param[in,out] log_storage_p     Address to return a pointer to @c kaa_memory_log_storage_t to.
 * @param[in]     logger            Kaa logger instance to use for internal logging.
*
 * @return Error code.
 */
kaa_error_t kaa_memory_log_storage_create(kaa_memory_log_storage_t **log_storage_p, kaa_logger_t *logger);



/**
 * @brief Destroys an in-memory log storage
 *
 * @param[in] self      Pointer to a @c kaa_memory_log_storage_t instance.
 */
void kaa_memory_log_storage_destroy(kaa_memory_log_storage_t *self);



/**
 * @brief Returns a standard log storage interface for use in the Kaa data collection subsystem.
 *
 * @param[in]       self      Pointer to a @c kaa_memory_log_storage_t instance.
 * @param[in,out]   interface Pointer to a @link kaa_log_storage_t @endlink structure to fill in.
*
 * @return Error code.
 */
kaa_error_t kaa_memory_log_storage_get_interface(kaa_memory_log_storage_t *self, kaa_log_storage_t *interface);



/**
 * @brief Returns a sample log upload strategy for use in the Kaa data collection subsystem.
 *
 * @param[in]       self      Pointer to a @c kaa_memory_log_storage_t instance.
 * @param[in,out]   strategy  Pointer to a @link kaa_log_upload_strategy_t @endlink structure to fill in.
*
 * @return Error code.
 */
kaa_error_t kaa_memory_log_storage_get_strategy(kaa_memory_log_storage_t *self, kaa_log_upload_strategy_t *strategy);

#ifdef __cplusplus
} // extern "C"
#endif

#endif /* KAA_DISABLE_FEATURE_LOGGING */

#endif /* KAA_MEMORY_LOG_STORAGE_H_ */

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

#ifndef KAA_EXT_LOG_UPLOAD_STRATEGIES_H_
#define KAA_EXT_LOG_UPLOAD_STRATEGIES_H_

#include <stdint.h>
#include "kaa_common.h"

#ifdef __cplusplus
extern "C" {
#endif

#define THRESHOLD_VOLUME_FLAG 0x01
#define THRESHOLD_COUNT_FLAG  0x02
#define TIMEOUT_FLAG          0x04

#define KAA_LOG_UPLOAD_VOLUME_STRATEGY               (THRESHOLD_VOLUME_FLAG | THRESHOLD_COUNT_FLAG)
#define KAA_LOG_UPLOAD_BY_TIMEOUT_STRATEGY           (TIMEOUT_FLAG)
#define KAA_LOG_UPLOAD_BY_RECORD_COUNT               (THRESHOLD_COUNT_FLAG)
#define KAA_LOG_UPLOAD_BY_RECORD_COUNT_AND_TIMELIMIT (THRESHOLD_COUNT_FLAG | TIMEOUT_FLAG)
#define KAA_LOG_UPLOAD_BY_STORAGE_SIZE               (THRESHOLD_VOLUME_FLAG)
#define KAA_LOG_UPLOAD_BY_STORAGE_SIZE_AND_TIMELIMIT (THRESHOLD_VOLUME_FLAG | TIMEOUT_FLAG)


kaa_error_t ext_log_upload_strategy_change_strategy(void *strategy, uint8_t type);

/**
 * @brief Sets the new threshold log volume to the strategy.
 *
 * @param   strategy            The strategy instance.
 * @param   threshold_volume    The new threshold volume value in bytes.
 * @return Error code.
 */
kaa_error_t ext_log_upload_strategy_set_threshold_volume(void *strategy, size_t threshold_volume);



/**
 * @brief Sets the new threshold log count to the strategy.
 *
 * @param   strategy           The strategy instance.
 * @param   threshold_count    The new threshold log count.
 * @return Error code.
 */
kaa_error_t ext_log_upload_strategy_set_threshold_count(void *strategy, size_t threshold_count);



/**
 * @brief Sets the new upload timeout to the strategy.
 *
 * @param   strategy          The strategy instance.
 * @param   upload_timeout    The new upload timeout in seconds.
 * @return Error code.
 */
kaa_error_t ext_log_upload_strategy_set_upload_timeout(void *strategy, size_t upload_timeout);



/**
 * @brief Sets the max amount of log batches allowed to be uploaded parallel.
 *
 * @param   strategy    The strategy instance.
 * @param   count       The new max amount.
 * @return Error code.
 */
kaa_error_t ext_log_upload_strategy_set_max_parallel_uploads(void *strategy, size_t count);


/**
 * @brief Sets the new upload retry period to the strategy.
 *
 * @param   strategy               The strategy instance.
 * @param   upload_retry_period    The new upload retry period value in seconds.
 * @return Error code.
 */
kaa_error_t ext_log_upload_strategy_set_upload_retry_period(void *strategy, size_t upload_retry_period);

#ifdef __cplusplus
}
#endif

#endif /* KAA_EXT_LOG_UPLOAD_STRATEGIES_H_ */

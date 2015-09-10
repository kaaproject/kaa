/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#ifndef KAA_EXT_LOG_UPLOAD_STRATEGY_BY_VOLUME_H_
#define KAA_EXT_LOG_UPLOAD_STRATEGY_BY_VOLUME_H_

#include "../../platform/platform.h"
#include "../../kaa_common.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Sets the new threshold log volume to the strategy.
 *
 * @param   strategy            The strategy instance.
 * @param   threshold_volume    The new threshold volume value in bytes.
 * @return Error code.
 */
kaa_error_t ext_log_upload_strategy_by_volume_set_threshold_volume(void *strategy, size_t threshold_volume);



/**
 * @brief Sets the new threshold log count to the strategy.
 *
 * @param   strategy           The strategy instance.
 * @param   threshold_count    The new threshold log count value in bytes.
 * @return Error code.
 */
kaa_error_t ext_log_upload_strategy_by_volume_set_threshold_count(void *strategy, size_t threshold_count);



/**
 * @brief Sets the new log batch size to the strategy.
 *
 * @param   strategy          The strategy instance.
 * @param   log_batch_size    The new log batch size in bytes.
 * @return Error code.
 */
kaa_error_t ext_log_upload_strategy_by_volume_set_batch_size(void *strategy, size_t log_batch_size);



/**
 * @brief Sets the new upload timeout to the strategy.
 *
 * @param   strategy          The strategy instance.
 * @param   upload_timeout    The new upload timeout in bytes.
 * @return Error code.
 */
kaa_error_t ext_log_upload_strategy_by_volume_set_upload_timeout(void *strategy, size_t upload_timeout);



/**
 * @brief Sets the new upload retry period to the strategy.
 *
 * @param   strategy               The strategy instance.
 * @param   upload_retry_period    The new upload retry period value in bytes.
 * @return Error code.
 */
kaa_error_t ext_log_upload_strategy_by_volume_set_upload_retry_period(void *strategy, size_t upload_retry_period);

#ifdef __cplusplus
}
#endif

#endif /* KAA_EXT_LOG_UPLOAD_STRATEGY_BY_VOLUME_H_ */

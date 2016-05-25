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

#ifndef KAA_CONFIGURATION_MANAGER_H_
#define KAA_CONFIGURATION_MANAGER_H_

#include "gen/kaa_configuration_definitions.h"
#include "platform/ext_configuration_receiver.h"

#ifdef __cplusplus
extern "C" {
#endif

struct kaa_configuration_manager_t;
typedef struct kaa_configuration_manager_t kaa_configuration_manager_t;

/**
 * @brief Retrieves the current configuration data.
 *
 * @param[in] self      The valid pointer to @link kaa_configuration_manager_t @endlink instance.
 *
 * @return  The current configuration data (NOTE: don't modify this instance), or NULL if something went wrong.
 *          Don't cache this pointer, it could become invalid after the next configuration update.
 */
const kaa_root_configuration_t *kaa_configuration_manager_get_configuration(kaa_configuration_manager_t *self);

/**
 * @brief Sets the new receiver of updated configuration data. See @link kaa_configuration_root_receiver_t @endlink .
 *
 * @param[in] self      The valid pointer to @link kaa_configuration_manager_t @endlink instance.
 * @param[in] receiver  The new receiver instance. This callback will be called each time when the new configuration arrives.
 *
 * @return  Error code.
 */
kaa_error_t kaa_configuration_manager_set_root_receiver(kaa_configuration_manager_t *self, const kaa_configuration_root_receiver_t *receiver);

#ifdef __cplusplus
} // extern "C"
#endif

#endif /* KAA_CONFIGURATION_MANAGER_H_ */

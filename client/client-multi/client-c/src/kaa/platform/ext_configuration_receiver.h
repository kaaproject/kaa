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
 * @file ext_configuration_receiver.h
 * @brief External interface for receiving configuration updates used by Kaa Configuration subsystem.
 * Should be implemented to automatically receive configuration updates.
 */

#ifndef EXT_CONFIGURATION_RECEIVER_H_
#define EXT_CONFIGURATION_RECEIVER_H_

#include "kaa_error.h"
#include "gen/kaa_configuration_definitions.h"

#ifdef __cplusplus
extern "C" {
#endif



/**
 * @brief Notifies about the new configuration data. See @link kaa_configuration_manager_set_root_receiver @endlink .
 *
 * @param[in] context           Callback's context.
 * @param[in] configuration     The latest configuration data. NOTE: don't modify this instance.
 *
 * @return  Error code.
 */
typedef kaa_error_t (*on_configuration_updated_fn)(void *context, const kaa_root_configuration_t *configuration);



/**
 * @brief Interface for the configuration receiver.
 */
typedef struct
{
    void *context;                                              /**< Context to pass to the function below. */
    on_configuration_updated_fn on_configuration_updated;       /**< Called when the new configuration data arrives. */
} kaa_configuration_root_receiver_t;




#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* EXT_CONFIGURATION_RECEIVER_H_ */

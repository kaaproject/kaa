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
 * @file kaa_channel_manager.h
 * @brief User defined channels manager for Kaa C SDK.
 *
 * Manages sync request handler functions for Kaa services that are used by Kaa SDK to indicate to user defined protocol
 * implementations that Kaa services have data to sync with Operations server.
 */

#ifndef KAA_CHANNEL_MANAGER_H_
#define KAA_CHANNEL_MANAGER_H_

#include "kaa_common.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Kaa channel manager structure.
 */
typedef struct kaa_channel_manager_t kaa_channel_manager_t;

/**
 * @brief Kaa sync request handler function for specific services.
 */
typedef void (*kaa_sync_handler_fn)(const kaa_service_t services[], size_t service_count);

/**
 * @brief General Kaa sync request handler function for all available services.
 */
typedef void (*kaa_sync_all_handler_fn)();

/**
 * @brief Adds user-defined transport channel implementation as a sync request handler for the given list of services.
 *
 * Kaa library will call the supplied @c handler when there is data to be sent to Operations server for one of the
 * specified services.
 *
 * @b NOTE: It is possible to register more than one sync handler for the same service. In such event Kaa library will
 * use the last registered one.
 *
 * @b NOTE: Channel manager does not check if the handler was already registered. Calling this functions more than once
 * with the same @c handler will lead to several instances registered in the channel manager.
 *
 * @param[in]   this                Valid pointer to the channel manager instance.
 * @param[in]   handler             Pointer to a sync handler function.
 * @param[in]   supported_services  Array of services to use sync handler for.
 * @param[in]   services_count      Size of the @c supported_services array.
 *
 * @return      Error code.
 */
kaa_error_t kaa_channel_manager_add_sync_handler(kaa_channel_manager_t *this
        , kaa_sync_handler_fn handler, const kaa_service_t *supported_services, size_t services_count);


/**
 * @brief Removes sync request handler from the currently registered list.
 *
 * @b NOTE: If the @c handler was registered more than once, only the @b last registered instance will be removed.
 *
 * @param[in]   this                Valid pointer to the channel manager instance.
 * @param[in]   handler             Pointer to a sync handler function.
 *
 * @return      Error code.
 */
kaa_error_t kaa_channel_manager_remove_sync_handler(kaa_channel_manager_t *this, kaa_sync_handler_fn handler);


/**
 * @brief Sets general sync request handler for all services.
 *
 * Only one general sync request handler is allowed at the time. Second invocation of this function will override
 * the previously set handler.
 *
 * @param[in]   this                Valid pointer to the channel manager instance.
 * @param[in]   handler             Pointer to a sync handler function.
 *
 * @return      Error code.
 */
kaa_error_t kaa_channel_manager_set_sync_all_handler(kaa_channel_manager_t *this, kaa_sync_all_handler_fn handler);


/**
 * @brief Returns general sync request handler for all services.
 *
 * @param[in]   this                Valid pointer to the channel manager instance.
 *
 * @return      Pointer to the general sync request handler. @c NULL if @c this is @c NULL.
 */
kaa_sync_all_handler_fn kaa_channel_manager_get_sync_all_handler(kaa_channel_manager_t *this);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_CHANNEL_MANAGER_H_ */

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


# ifndef KAA_DEVICE_EVENT_CLASS_FAMILY_H
# define KAA_DEVICE_EVENT_CLASS_FAMILY_H

# include "kaa_event.h"
# include "kaa_error.h"
#include "../../../kaa/src/event/kaa_device_event_class_family_definitions.h"


/**
 * @brief Listener of device_info_request events.
 */
typedef void (* on_kaa_device_event_class_family_device_info_request)(kaa_device_event_class_family_device_info_request_t *event, kaa_endpoint_id_p source);

/**
 * @brief Listener of device_info_response events.
 */
typedef void (* on_kaa_device_event_class_family_device_info_response)(kaa_device_event_class_family_device_info_response_t *event, kaa_endpoint_id_p source);


/**
 * @brief Set listener for device_info_request events.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       listener    Listener callback.
 * @return Error code.
 */
kaa_error_t kaa_event_manager_set_kaa_device_event_class_family_device_info_request_listener(kaa_event_manager_t *self, on_kaa_device_event_class_family_device_info_request listener);

/**
 * @brief Set listener for device_info_response events.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       listener    Listener callback.
 * @return Error code.
 */
kaa_error_t kaa_event_manager_set_kaa_device_event_class_family_device_info_response_listener(kaa_event_manager_t *self, on_kaa_device_event_class_family_device_info_response listener);


/**
 * @brief Send event of type device_info_request.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       event       Pointer to the event object.
 * @param[in]       target      The target endpoint of the event (null-terminated string). The size of
 *                              the target parameter should be equal to @link KAA_ENDPOINT_ID_LENGTH @endlink .
 *                              If @code NULL @endcode event will be broadcasted.
 * 
 * @return Error code.
 */
kaa_error_t kaa_event_manager_send_kaa_device_event_class_family_device_info_request(kaa_event_manager_t *self, kaa_device_event_class_family_device_info_request_t *event, kaa_endpoint_id_p target);

/**
 * @brief Send event of type device_info_response.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       event       Pointer to the event object.
 * @param[in]       target      The target endpoint of the event (null-terminated string). The size of
 *                              the target parameter should be equal to @link KAA_ENDPOINT_ID_LENGTH @endlink .
 *                              If @code NULL @endcode event will be broadcasted.
 * 
 * @return Error code.
 */
kaa_error_t kaa_event_manager_send_kaa_device_event_class_family_device_info_response(kaa_event_manager_t *self, kaa_device_event_class_family_device_info_response_t *event, kaa_endpoint_id_p target);


/**
 * @brief Add event of type device_info_request to the events block.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       event       Pointer to the event object.
 * @param[in]       target      Target of the event (null-terminated string). If NULL - event will be broadcasted.
 * @param[in]       trx_id      Event block id.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_manager_add_kaa_device_event_class_family_device_info_request_event_to_block(kaa_event_manager_t *self, kaa_device_event_class_family_device_info_request_t *event, kaa_endpoint_id_p target, kaa_event_block_id trx_id);

/**
 * @brief Add event of type device_info_response to the events block.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       event       Pointer to the event object.
 * @param[in]       target      Target of the event (null-terminated string). If NULL - event will be broadcasted.
 * @param[in]       trx_id      Event block id.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_manager_add_kaa_device_event_class_family_device_info_response_event_to_block(kaa_event_manager_t *self, kaa_device_event_class_family_device_info_response_t *event, kaa_endpoint_id_p target, kaa_event_block_id trx_id);

# endif // KAA_DEVICE_EVENT_CLASS_FAMILY_H
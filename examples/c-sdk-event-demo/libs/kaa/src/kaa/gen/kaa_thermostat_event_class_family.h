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


# ifndef KAA_THERMOSTAT_EVENT_CLASS_FAMILY_H
# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_H

# include "kaa_thermostat_event_class_family_definitions.h" 
# include "../kaa_event.h"
# include "../kaa_error.h"


/**
 * @brief Listener of thermostat_info_request events.
 */
typedef void (* on_kaa_thermostat_event_class_family_thermostat_info_request)(void *context, kaa_thermostat_event_class_family_thermostat_info_request_t *event, kaa_endpoint_id_p source);

/**
 * @brief Listener of thermostat_info_response events.
 */
typedef void (* on_kaa_thermostat_event_class_family_thermostat_info_response)(void *context, kaa_thermostat_event_class_family_thermostat_info_response_t *event, kaa_endpoint_id_p source);

/**
 * @brief Listener of change_degree_request events.
 */
typedef void (* on_kaa_thermostat_event_class_family_change_degree_request)(void *context, kaa_thermostat_event_class_family_change_degree_request_t *event, kaa_endpoint_id_p source);


/**
 * @brief Set listener for thermostat_info_request events.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       listener    Listener callback.
 * @param[in]       context     Listener's context.
 * @return Error code.
 */
kaa_error_t kaa_event_manager_set_kaa_thermostat_event_class_family_thermostat_info_request_listener(kaa_event_manager_t *self, on_kaa_thermostat_event_class_family_thermostat_info_request listener, void *context);

/**
 * @brief Set listener for thermostat_info_response events.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       listener    Listener callback.
 * @param[in]       context     Listener's context.
 * @return Error code.
 */
kaa_error_t kaa_event_manager_set_kaa_thermostat_event_class_family_thermostat_info_response_listener(kaa_event_manager_t *self, on_kaa_thermostat_event_class_family_thermostat_info_response listener, void *context);

/**
 * @brief Set listener for change_degree_request events.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       listener    Listener callback.
 * @param[in]       context     Listener's context.
 * @return Error code.
 */
kaa_error_t kaa_event_manager_set_kaa_thermostat_event_class_family_change_degree_request_listener(kaa_event_manager_t *self, on_kaa_thermostat_event_class_family_change_degree_request listener, void *context);


/**
 * @brief Send event of type thermostat_info_request.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       event       Pointer to the event object.
 * @param[in]       target      The target endpoint of the event (null-terminated string). The size of
 *                              the target parameter should be equal to @link KAA_ENDPOINT_ID_LENGTH @endlink .
 *                              If @code NULL @endcode event will be broadcasted.
 * 
 * @return Error code.
 */
kaa_error_t kaa_event_manager_send_kaa_thermostat_event_class_family_thermostat_info_request(kaa_event_manager_t *self, kaa_thermostat_event_class_family_thermostat_info_request_t *event, kaa_endpoint_id_p target);

/**
 * @brief Send event of type thermostat_info_response.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       event       Pointer to the event object.
 * @param[in]       target      The target endpoint of the event (null-terminated string). The size of
 *                              the target parameter should be equal to @link KAA_ENDPOINT_ID_LENGTH @endlink .
 *                              If @code NULL @endcode event will be broadcasted.
 * 
 * @return Error code.
 */
kaa_error_t kaa_event_manager_send_kaa_thermostat_event_class_family_thermostat_info_response(kaa_event_manager_t *self, kaa_thermostat_event_class_family_thermostat_info_response_t *event, kaa_endpoint_id_p target);

/**
 * @brief Send event of type change_degree_request.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       event       Pointer to the event object.
 * @param[in]       target      The target endpoint of the event (null-terminated string). The size of
 *                              the target parameter should be equal to @link KAA_ENDPOINT_ID_LENGTH @endlink .
 *                              If @code NULL @endcode event will be broadcasted.
 * 
 * @return Error code.
 */
kaa_error_t kaa_event_manager_send_kaa_thermostat_event_class_family_change_degree_request(kaa_event_manager_t *self, kaa_thermostat_event_class_family_change_degree_request_t *event, kaa_endpoint_id_p target);


/**
 * @brief Add event of type thermostat_info_request to the events block.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       event       Pointer to the event object.
 * @param[in]       target      Target of the event (null-terminated string). If NULL - event will be broadcasted.
 * @param[in]       trx_id      Event block id.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_manager_add_kaa_thermostat_event_class_family_thermostat_info_request_event_to_block(kaa_event_manager_t *self, kaa_thermostat_event_class_family_thermostat_info_request_t *event, kaa_endpoint_id_p target, kaa_event_block_id trx_id);

/**
 * @brief Add event of type thermostat_info_response to the events block.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       event       Pointer to the event object.
 * @param[in]       target      Target of the event (null-terminated string). If NULL - event will be broadcasted.
 * @param[in]       trx_id      Event block id.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_manager_add_kaa_thermostat_event_class_family_thermostat_info_response_event_to_block(kaa_event_manager_t *self, kaa_thermostat_event_class_family_thermostat_info_response_t *event, kaa_endpoint_id_p target, kaa_event_block_id trx_id);

/**
 * @brief Add event of type change_degree_request to the events block.
 * 
 * @param[in]       self        Valid pointer to event manager.
 * @param[in]       event       Pointer to the event object.
 * @param[in]       target      Target of the event (null-terminated string). If NULL - event will be broadcasted.
 * @param[in]       trx_id      Event block id.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_manager_add_kaa_thermostat_event_class_family_change_degree_request_event_to_block(kaa_event_manager_t *self, kaa_thermostat_event_class_family_change_degree_request_t *event, kaa_endpoint_id_p target, kaa_event_block_id trx_id);

# endif // KAA_THERMOSTAT_EVENT_CLASS_FAMILY_H

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

#ifndef KAA_KAA_NOTIFICATION_H_
#define KAA_KAA_NOTIFICATION_H_
#include <stddef.h>
#include <stdio.h>
#include <stdint.h>
#include "stdbool.h"
#include "kaa_status.h"
#include "kaa_error.h"
#include "kaa_platform_common.h"
#include "utilities/kaa_mem.h"
#include "kaa_channel_manager.h"
#include "kaa_common.h"
#include "utilities/kaa_log.h"
#include "kaa_platform_utils.h"
#include "platform/ext_notification_receiver.h"


/**
* @brief Calculates the topic listener id to manage topic listeners.
*
* @param[in]   listener     Pointer to the listener, which id should be calculated.
* @param[out]  listener_id  Pointer to the variable, which will be initialized with calculated id.
*
* @return Error code.
*/
kaa_error_t kaa_calculate_topic_listener_id(kaa_topic_listener_t *listener, uint32_t *listener_id);

/**
* @brief Calculates the notification listener id to manage topic listeners.
*
* @param[in]   listener     Pointer to the listener, which id should be calculated.
* @param[out]  listener_id  Pointer to the variable, which will be initialized with calculated id.
*
* @return Error code.
*/
kaa_error_t kaa_calculate_notification_listener_id(kaa_notification_listener_t *listener, uint32_t *listener_id);

/**
* @brief Adds a mandatory notification listener.
*
* @param[in]  self         Pointer to the notification manager instance.
* @param[in]  listener     Pointer to the listener, which callback will be called as soon as a notification received.
* @param[out] listener_id  Pointer to the variable, which will be initialized with calculated id.
*
* @return Error code.
*/
kaa_error_t kaa_add_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener, uint32_t *listener_id);

/**
* @brief Adds an optional notification listener.
*
* @param[in]  self        Pointer to the notification manager instance.
* @param[in]  listener    Pointer to a listener, which callback will be called as soon as a notification received.
* @param[in]  topic_id    Pointer to an id of topic, listener wants to be notified about.
* @param[out] listener_id Pointer to an variable, which will be initialized with calculated id. If NULL listener_id won't be initialized
*
* @return Error code.
*/
kaa_error_t kaa_add_optional_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener
                                                 , uint64_t *topic_id, uint32_t *listener_id);

/**
* @brief Removes a mandatory notification listener
*
* @param[in]  self         Pointer to the notification manager instance.
* @param[in]  listener_id  Pointer to the listener id, which will be used to find listener that should be removed from notification listeners' list. If NULL listener_id won't be initialized
*
* @return Error code.
*/
kaa_error_t kaa_remove_notification_listener(kaa_notification_manager_t *self, uint32_t *listener_id);

/**
* @brief Removes a optional notification listener.
*
* @param[in]  self         Pointer to the notification manager instance.
* @param[in]  topic_id     Pointer to the id of topic listener don't want to be notified about.
* @param[in]  listener_id  Pointer to the variable, which will be used to find listener that should be removed from notification listeners' list.
*
* @return Error code.
*/
kaa_error_t kaa_remove_optional_notification_listener(kaa_notification_manager_t *self, uint64_t *topic_id, uint32_t *listener_id);

/**
* @brief Adds a topic list listener.
*
* @param[in]  self               Pointer to the notification manager instance.
* @param[in]  listener           Pointer to a listener, which callback will be called as soon as a notification received.
* @param[out] topic_listener_id  Pointer to a variable, which will be initialized with calculated id.
*
* @return Error code.
*/
kaa_error_t kaa_add_topic_list_listener(kaa_notification_manager_t *self, kaa_topic_listener_t *listener, uint32_t *topic_listener_id);

/**
* @brief Removes the topic list listener.
*
* @param[in]  self         Pointer to the notification manager instance.
* @param[in]  topic_listener_id  Pointer to the integer variable, which will be used to find listener that should be removed from topic listeners' list.. If NULL topic_listener_id won't be initialized
*
* @return Error code.
*/
kaa_error_t kaa_remove_topic_list_listener(kaa_notification_manager_t *self, uint32_t *topic_listener_id);

/**
* @brief Retrieves topic list.
*
* @param[in]  self         Pointer to the notification manager instance.
* @param[in]  topics       Pointer to the pointer that will be initialized with topic list.
*
* @return Error code.
*/
kaa_error_t kaa_get_topics(kaa_notification_manager_t *self, kaa_list_t **topics);

/**
* @brief Subscribes to topic.
*
* @param[in]  self         Pointer to the notification manager instance.
* @param[in]  topic_id     Pointer to topic id it is should be subscribed to.
* @param[in]  force_sync   Indicates whether subscription should be done immediately (true) or could be postponed (false).
*
* @return Error code.
*/
kaa_error_t kaa_subscribe_to_topic(kaa_notification_manager_t *self, uint64_t *topic_id, bool force_sync);

/**
* @brief Subscribes to topics.
*
* @param[in]  self         Pointer to the notification manager instance.
* @param[in]  topic_ids    Array of topic ids it is should be subscribed to.
* @param[in]  size         Size of the topic ids' array
* @param[in]  force_sync   Indicates whether subscription should be done immediately (true) or could be postponed (false).
*
* @return Error code.
*/
kaa_error_t kaa_subscribe_to_topics(kaa_notification_manager_t *self, uint64_t *topic_ids, size_t size, bool force_sync);

/**
* @brief Unsubscribes from topic.
*
* @param[in]  self         Pointer to the notification manager instance.
* @param[in]  topic_id     Pointer to topic id it is should be unsubscribed from.

*
* @return Error code.
*/
kaa_error_t kaa_unsubscribe_from_topic(kaa_notification_manager_t *self, uint64_t *topic_id, bool force_sync);

/**
* @brief Unsubscribes to topics.
*
* @param[in]  self         Pointer to the notification manager instance.
* @param[in]  topic_ids    Array of topic ids it is should be unsubscribed from.
* @param[in]  size         Size of the topic ids' array
* @param[in]  force_sync   Indicates whether subscription should be done immediately (true) or could be postponed (false).
*
* @return Error code.
*/
kaa_error_t kaa_unsubscribe_from_topics(kaa_notification_manager_t *self, uint64_t *topic_ids, size_t size, bool force_sync);

/**
* @brief Sends sync request to the server
*
* @param[in]  self         Pointer to the notification manager instance.
*
* @return Error code.
*/
kaa_error_t kaa_sync_topic_subscriptions(kaa_notification_manager_t *self);

#endif /* KAA_KAA_NOTIFICATION_H_ */

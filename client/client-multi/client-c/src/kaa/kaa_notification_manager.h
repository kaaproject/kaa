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

#ifndef KAA_KAA_NOTIFICATION_H_
#define KAA_KAA_NOTIFICATION_H_

#include <stdint.h>
#include <stdbool.h>
#include <stddef.h>

#include "kaa_error.h"
#include "platform/ext_notification_receiver.h"
#include "collections/kaa_list.h"



#ifdef __cplusplus
extern "C" {
#endif

#ifndef KAA_NOTIFICATION_MANAGER_T
# define KAA_NOTIFICATION_MANAGER_T
    /**
     * @brief Kaa notification manager structure.
     */
    typedef struct kaa_notification_manager_t       kaa_notification_manager_t;
#endif



/**
* @brief Calculates the topic listener id to manage this topic listener.
*
* @param[in]   listener     The pointer to the listener whose id is calculated.
* @param[out]  listener_id  The pointer to the variable which is initialized with the calculated id.
*
* @return The error code.
*/
kaa_error_t kaa_calculate_topic_listener_id(const kaa_topic_listener_t *listener, uint32_t *listener_id);

/**
* @brief Calculates the notification listener id to manage this notification listener.
*
* @param[in]   listener     The pointer to the listener whose id is calculated.
* @param[out]  listener_id  The pointer to the variable which is initialized with the calculated id.
*
* @return The error code.
*/
kaa_error_t kaa_calculate_notification_listener_id(const kaa_notification_listener_t *listener, uint32_t *listener_id);

/**
* @brief Adds a mandatory notification listener to receive notifications on mandatory topics.
*
* @param[in]  self         The pointer to the notification manager instance.
* @param[in]  listener     The pointer to the listener whose callback is called as soon as a notification is received.
* @param[out] listener_id  The pointer to the variable which is initialized with the calculated id.
*
* @return The error code.
*/
kaa_error_t kaa_add_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener, uint32_t *listener_id);

/**
* @brief Adds an optional notification listener to receive notifications on optional topics.
*
* @param[in]  self        The pointer to the notification manager instance.
* @param[in]  listener    The pointer to the listener whose callback is called as soon as a notification is received.
* @param[in]  topic_id    The pointer to the id of the topic about which the listener is notified.
* @param[out] listener_id The pointer to the variable which is initialized with the calculated id. If @c NULL, @p listener_id is not initialized.
*
* @return The error code.
*/
kaa_error_t kaa_add_optional_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener
                                                 , uint64_t *topic_id, uint32_t *listener_id);

/**
* @brief Removes the mandatory notification listener.
*
* @param[in]  self         The pointer to the notification manager instance.
* @param[in]  listener_id  The pointer to the listener id which is used to find the listener that should be removed from the notification listeners list. If @c NULL, @p listener_id is not initialized
*
* @return The error code.
*/
kaa_error_t kaa_remove_notification_listener(kaa_notification_manager_t *self, uint32_t *listener_id);

/**
* @brief Removes the optional notification listener.
*
* @param[in]  self         The pointer to the notification manager instance.
* @param[in]  topic_id     The pointer to the id of the topic the listener should not be notified about anymore.
* @param[in]  listener_id  The pointer to the variable which is used to find the listener that should be removed from the notification listeners list.
*
* @return The error code.
*/
kaa_error_t kaa_remove_optional_notification_listener(kaa_notification_manager_t *self, uint64_t *topic_id, uint32_t *listener_id);

/**
* @brief Adds a topic list listener.
*
* @param[in]  self               The pointer to the notification manager instance.
* @param[in]  listener           The pointer to the listener whose callback is called as soon as a notification is received.
* @param[out] topic_listener_id  The pointer to the variable which is initialized with the calculated id.
*
* @return The error code.
*/
kaa_error_t kaa_add_topic_list_listener(kaa_notification_manager_t *self, kaa_topic_listener_t *listener, uint32_t *topic_listener_id);

/**
* @brief Removes the topic list listener.
*
* @param[in]  self         The pointer to the notification manager instance.
* @param[in]  topic_listener_id  The pointer to the variable which is used to find the listener that should be removed from the topic listeners list. If @c NULL, @p topic_listener_id is not initialized.
*
* @return The error code.
*/
kaa_error_t kaa_remove_topic_list_listener(kaa_notification_manager_t *self, uint32_t *topic_listener_id);

/**
* @brief Retrieves the topic list.
*
* @param[in]  self         The pointer to the notification manager instance.
* @param[out] topics       The pointer to the pointer that is initialized with the topic list.
*
* @return The error code.
*/
kaa_error_t kaa_get_topics(kaa_notification_manager_t *self, kaa_list_t **topics);

/**
* @brief Subscribes to the topic.
*
* @param[in]  self         The pointer to the notification manager instance.
* @param[in]  topic_id     The pointer to the id of the topic to which the endpoint should be subscribed.
* @param[in]  force_sync   Indicates whether subscription should be performed immediately (true) or should be postponed (false).
*
* @return The error code.
*/
kaa_error_t kaa_subscribe_to_topic(kaa_notification_manager_t *self, uint64_t *topic_id, bool force_sync);

/**
* @brief Subscribes to the topics.
*
* @param[in]  self         The pointer to the notification manager instance.
* @param[in]  topic_ids    An array of the ids of the topics to which the endpoint should be subscribed.
* @param[in]  size         The size of the topic ids array.
* @param[in]  force_sync   Indicates whether subscription should be performed immediately (true) or should be postponed (false).
*
* @return The error code.
*/
kaa_error_t kaa_subscribe_to_topics(kaa_notification_manager_t *self, uint64_t *topic_ids, size_t size, bool force_sync);

/**
* @brief Unsubscribes from the topic.
*
* @param[in]  self         The pointer to the notification manager instance.
* @param[in]  topic_id     The pointer to the id of the topic from which the endpoint should be unsubscribed.
* @param[in]  force_sync   Sync topic unsubscription
*
* @return The error code.
*/
kaa_error_t kaa_unsubscribe_from_topic(kaa_notification_manager_t *self, uint64_t *topic_id, bool force_sync);

/**
* @brief Unsubscribes from the topics.
*
* @param[in]  self         The pointer to the notification manager instance.
* @param[in]  topic_ids    An array of the ids of the topics from which the endpoint should be unsubscribed.
* @param[in]  size         The size of the topic ids array.
* @param[in]  force_sync   Indicates whether unsubscription should be performed immediately (true) or should be postponed (false).
*
* @return The error code.
*/
kaa_error_t kaa_unsubscribe_from_topics(kaa_notification_manager_t *self, uint64_t *topic_ids, size_t size, bool force_sync);

/**
* @brief Sends the sync request to the server.
*
* @param[in]  self         The pointer to the notification manager instance.
*
* @return The error code.
*/
kaa_error_t kaa_sync_topic_subscriptions(kaa_notification_manager_t *self);

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* KAA_KAA_NOTIFICATION_H_ */

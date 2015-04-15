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
#include "utilities/kaa_htonll.h"
#include "stdbool.h"
#include "kaa_status.h"
#include "platform/sock.h"
#include "kaa_error.h"
#include "kaa_platform_common.h"
#include "utilities/kaa_mem.h"
#include "kaa_channel_manager.h"
#include "kaa_common.h"
#include "utilities/kaa_log.h"
#include "kaa_platform_utils.h"
#include "ext_notification_receiver.h"


kaa_error_t kaa_calculate_topic_listener_id(kaa_topic_listener_t *listener, uint32_t *listener_id);

kaa_error_t kaa_calculate_notification_listener_id(kaa_notification_listener_t *listener, uint32_t *listener_id);


kaa_error_t kaa_add_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener, uint32_t *listener_id);

kaa_error_t kaa_add_optional_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener
                                                 , uint64_t *topic_id,uint32_t *listener_id);

kaa_error_t kaa_remove_notification_listener(kaa_notification_manager_t *self, uint32_t *listener_id);

kaa_error_t kaa_remove_optional_notification_listener(kaa_notification_manager_t *self, uint64_t *topic_id, uint32_t *listener_id);

kaa_error_t kaa_add_topic_list_listener(kaa_notification_manager_t *self, kaa_topic_listener_t *listener, uint32_t *topic_listener_id);

kaa_error_t kaa_remove_topic_list_listener(kaa_notification_manager_t *self, uint32_t *topic_listener_id);

kaa_error_t kaa_get_topics(kaa_notification_manager_t *self, kaa_list_t **topics);

kaa_error_t kaa_subscribe_to_topic(kaa_notification_manager_t *self, uint64_t *topic_id, bool force_sync);

kaa_error_t kaa_subscribe_to_topics(kaa_notification_manager_t *self, kaa_list_t *topic_ids, bool force_sync);

kaa_error_t kaa_unsubscribe_from_topic(kaa_notification_manager_t *self, uint64_t *topic_id, bool force_sync);

kaa_error_t kaa_unsubscribe_from_topics(kaa_notification_manager_t *self, kaa_list_t *topic_ids, bool force_sync);

kaa_error_t kaa_sync_topic_subscriptions(kaa_notification_manager_t *self);

#endif /* KAA_KAA_NOTIFICATION_H_ */

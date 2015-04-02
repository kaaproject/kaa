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

#ifndef CLIENT_CLIENT_MULTI_CLIENT_C_SRC_KAA_KAA_NOTIFICATION_H_
#define CLIENT_CLIENT_MULTI_CLIENT_C_SRC_KAA_KAA_NOTIFICATION_H_
#include "kaa_error.h"
#include "kaa_platform_common.h"
#include "utilities/kaa_mem.h"

#ifndef KAA_NOTIFICATION_DEFINITIONS
    #define KAA_NOTIFICATION_DEFINETIONS
    #define KAA_RECEIVE_NOTIFICATIONS               0x01
    #define KAA_SUBSCRIBED_TOPIC_LIST_HASH          0x02
    #define KAA_TOPIC_STATES                           0
    #define KAA_UNICAST_NOTIFICATIONS                  1
    #define KAA_SUBSCRIPTION                           2
    #define KAA_UNSUBSCRIBTION                         3
#endif

#ifndef KAA_NOTIFICATION_MANAGER_T
    #define KAA_NOTIFICATION_MANAGER_T
typedef struct kaa_notification_manager_t       kaa_notification_manager_t;
#endif

#ifndef KAA_NOTIFICATION_LISTENER_T
# define KAA_NOTIFICATION_LISTENER_T
    typedef struct kaa_notification_listener_t      kaa_notification_listener_t;
#endif

#ifndef KAA_NOTIFICATOIN_T
    #define KAA_NOTIFICATION_T
    typedef struct kaa_notification_t kaa_notification_t;
    typedef void (*callback_t)(const char *topic_id,kaa_notification_t *notification);
#endif

kaa_error_t add_notification_listener(kaa_notification_listener_t *listener);

kaa_error_t kaa_notification_manager_get_size(kaa_notification_manager_t *self, size_t *expected_size);

kaa_error_t kaa_notification_manager_create(kaa_notification_manager_t **self, kaa_status_t *status
                                          , kaa_channel_manager_t *channel_manager
                                          , kaa_logger_t *logger);

kaa_error_t kaa_notification_listener_create(kaa_notification_listener_t **listener, callback_t callback);

kaa_error_t kaa_add_notification_listener(kaa_notification_manager_t* self, kaa_notification_listener_t* listener);

#endif /* CLIENT_CLIENT_MULTI_CLIENT_C_SRC_KAA_KAA_NOTIFICATION_H_ */

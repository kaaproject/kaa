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

#ifndef KAA_NOTIFICATION_RECEIVER
#define KAA_NOTIFICATION_RECEIVER

#include "gen/kaa_notification_definitions.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
    MANDATORY_SUBSCRIPTION = 0x00,
    OPTIONAL_SUBSCRIPTION  = 0x01
} kaa_topic_subscription_type_t;

/**
 * @brief Pointer to the function that should be called when notification received.
 *
 * @param[in] context    pointer to any user's data.
 * @param[in] topic_id   pointer to notification's topic id.
 * @param[in] notification   pointer to received notification.
 *
 */
typedef void (*on_notification_callback)(void *context, uint64_t *topic_id, kaa_notification_t *notification);

typedef struct {
    on_notification_callback  callback;
    void *context;
} kaa_notification_listener_t;

typedef struct {
    uint64_t id;
    kaa_topic_subscription_type_t subscription_type;
    uint16_t name_length;
    char *name;
} kaa_topic_t;

/**
 * @brief Pointer to the function that should be called when notification received.
 *
 * @param[in] context    pointer to any user's data.
 * @param[in] topics     pointer to topic list.
 *
 */
typedef void (*on_topic_list_callback)(void *context, kaa_list_t *topics);

typedef struct {
    on_topic_list_callback callback;
    void *context;
} kaa_topic_listener_t;


#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif


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
#ifndef KAA_NOTIFICATION_RECEIVER
#define KAA_NOTIFICATION_RECEIVER

#include "gen/kaa_notification_definitions.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
    MANDATORY = 0x00,
    OPTIONAL  = 0x01
} kaa_topic_subscription_type_t;

typedef enum {
    SYSTEM = 0x0,
    CUSTOM = 0x1
} kaa_notification_type;

typedef void (*on_notification_callback)(void *context, const uint32_t *topic_id, const kaa_notification_t *notification);
typedef struct {
    on_notification_callback  callback;
    void* context;
} kaa_notification_listener_t;

typedef struct {
    uint64_t id;
    kaa_topic_subscription_type_t subscription_type; // + reserved
    uint16_t name_length;
    char* name;
} kaa_topic_t;

typedef void (*on_topic_list_callback)(void *context, const kaa_list_t *topics);
typedef struct {
    on_topic_list_callback callback;
    void *context;
} kaa_topic_listener_t;


#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif


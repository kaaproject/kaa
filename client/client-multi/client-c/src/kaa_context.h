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

#ifndef KAA_CONTEXT_H_
#define KAA_CONTEXT_H_

#ifdef __cplusplus
extern "C" {
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

#include "kaa_error.h"
#include "kaa_user.h"
#include "kaa_event.h"
#include "kaa_profile.h"
#include "kaa_bootstrap.h"
#include "kaa_status.h"
#include "kaa_logging.h"
#include "kaa_channel_manager.h"

typedef struct kaa_context_t {
    kaa_profile_manager_t   *   profile_manager;
    kaa_user_manager_t      *   user_manager;
#ifndef KAA_DISABLE_FEATURE_EVENTS
    kaa_event_manager_t     *   event_manager;
#endif
    kaa_bootstrap_manager_t *   bootstrap_manager;
    kaa_status_t            *   status;
    kaa_channel_manager_t   *   channel_manager;
#ifndef KAA_DISABLE_FEATURE_LOGGING
    kaa_log_collector_t     *   log_collector;
#endif
} kaa_context_t;

kaa_error_t kaa_create_context(kaa_context_t ** context);
kaa_error_t kaa_destroy_context(kaa_context_t * context);

// Kaa channel manager API
kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **);
void kaa_channel_manager_destroy(kaa_context_t *context);
void kaa_channel_manager_set_sync_handler(kaa_context_t *context, kaa_sync_t handler, size_t services_count, const kaa_service_t supported_services[]);
void kaa_channel_manager_set_sync_all_handler(kaa_context_t *context, kaa_sync_all_t handler);
kaa_sync_t kaa_channel_manager_get_sync_handler(kaa_context_t *context, kaa_service_t service);
kaa_sync_all_t kaa_channel_manager_get_sync_all_handler(kaa_context_t *context);

CLOSE_EXTERN
#endif /* KAA_CONTEXT_H_ */

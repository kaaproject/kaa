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

#include "kaa_context.h"
#include "kaa_mem.h"

kaa_error_t kaa_create_context(kaa_context_t ** context_p)
{
    kaa_context_t *context = KAA_MALLOC(kaa_context_t);

    KAA_NOT_VOID(context, KAA_ERR_NOMEM)

    kaa_error_t error = kaa_create_status(&(context->status));
    if (error != KAA_ERR_NONE) {
        goto error_status;
    }
    error = kaa_create_user_manager(&(context->user_manager));
    if (error != KAA_ERR_NONE) {
        goto error_user_manager;
    }
    error = kaa_create_profile_manager(&(context->profile_manager));
    if (error != KAA_ERR_NONE) {
        goto error_profile_manager;
    }

#ifndef KAA_DISABLE_FEATURE_EVENTS
    error = kaa_create_event_manager(&(context->event_manager));
    if (error != KAA_ERR_NONE) {
        goto error_event_manager;
    }
#endif

    error = kaa_create_bootstrap_manager(&(context->bootstrap_manager));
    if (error != KAA_ERR_NONE) {
        goto error_bootstrap_manager;
    }

#ifndef KAA_DISABLE_FEATURE_LOGGING
    error = kaa_create_log_collector(&(context->log_collector));
    if (error != KAA_ERR_NONE) {
        goto error_log_collector;
    }
#endif

    error = kaa_channel_manager_create(&(context->channel_manager));
    if (error != KAA_ERR_NONE) {
        goto error_channel_manager;
    }
    *context_p = context;
    return KAA_ERR_NONE;

error_channel_manager:

#ifndef KAA_DISABLE_FEATURE_LOGGING
    kaa_destroy_log_collector(context->log_collector);
error_log_collector:
#endif
    kaa_destroy_bootstrap_manager(context->bootstrap_manager);
error_bootstrap_manager:

#ifndef KAA_DISABLE_FEATURE_EVENTS
    kaa_destroy_event_manager(context->event_manager);
error_event_manager:
#endif
    kaa_destroy_profile_manager(context->profile_manager);
error_profile_manager:
    kaa_destroy_user_manager(context->user_manager);
error_user_manager:
    kaa_destroy_status(context->status);
error_status:
    KAA_FREE(context);
    return error;
}

kaa_error_t kaa_destroy_context(kaa_context_t * context)
{
    KAA_NOT_VOID(context, KAA_ERR_BADPARAM)

    kaa_destroy_user_manager(context->user_manager);
#ifndef KAA_DISABLE_FEATURE_EVENTS
    kaa_destroy_event_manager(context->event_manager);
#endif
    kaa_destroy_profile_manager(context->profile_manager);
    kaa_destroy_bootstrap_manager(context->bootstrap_manager);
    kaa_channel_manager_destroy(context);
    kaa_destroy_status(context->status);
#ifndef KAA_DISABLE_FEATURE_LOGGING
    kaa_destroy_log_collector(context->log_collector);
#endif
    KAA_FREE(context);
    return KAA_ERR_NONE;
}

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
    int error_c = 0;
    kaa_context_t *context = KAA_MALLOC(kaa_context_t);

    if (!context) {
        return KAA_ERR_NOMEM;
    }

    error_c |= kaa_create_status(&(context->status));
    error_c |= kaa_create_user_manager(&(context->user_manager));
    error_c |= kaa_create_profile_manager(&(context->profile_manager));
#ifndef KAA_DISABLE_FEATURE_EVENTS
    error_c |= kaa_create_event_manager(&(context->event_manager));
#endif
    error_c |= kaa_create_bootstrap_manager(&(context->bootstrap_manager));
    error_c |= kaa_channel_manager_create(&(context->channel_manager));

    if (error_c) {
        return KAA_ERR_NOMEM;
    }

    *context_p = context;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_destroy_context(kaa_context_t * context)
{
    kaa_destroy_user_manager(context->user_manager);
#ifndef KAA_DISABLE_FEATURE_EVENTS
    kaa_destroy_event_manager(context->event_manager);
#endif
    kaa_destroy_profile_manager(context->profile_manager);
    kaa_destroy_bootstrap_manager(context->bootstrap_manager);
    kaa_channel_manager_destroy(context);
    kaa_destroy_status(context->status);

    KAA_FREE(context);
    return KAA_ERR_NONE;
}

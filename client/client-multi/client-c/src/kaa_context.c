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

/*
 * External constructors and destructors from around the Kaa SDK
 */
extern kaa_error_t kaa_user_manager_create(kaa_user_manager_t **user_manager_p, kaa_status_t *status, kaa_channel_manager_t *channel_manager);
extern void        kaa_user_manager_destroy(kaa_user_manager_t *user_manager);
extern kaa_error_t kaa_create_profile_manager(kaa_profile_manager_t ** profile_manager_p, kaa_status_t *status, kaa_channel_manager_t *channel_manager);
extern void kaa_destroy_profile_manager(kaa_profile_manager_t *);
extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_logger_t *logger);
extern void        kaa_channel_manager_destroy(kaa_channel_manager_t *this);


kaa_error_t kaa_context_create(kaa_context_t ** context_p, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL2(context_p, logger, KAA_ERR_BADPARAM);

    *context_p = (kaa_context_t *) KAA_MALLOC(sizeof(kaa_context_t));
    KAA_RETURN_IF_NIL(*context_p, KAA_ERR_NOMEM);

    (*context_p)->logger = logger;
    (*context_p)->global_request_id = 0;

    static const char failed_msg[] = "Failed to create Kaa %s";

    kaa_error_t error = kaa_create_status(&((*context_p)->status));
    if (error) {
        KAA_LOG_ERROR(logger, error, failed_msg, "status");
    } else {
        error = kaa_channel_manager_create(&((*context_p)->channel_manager), logger);
        if (error) {
            KAA_LOG_ERROR(logger, error, failed_msg, "channel manager");
        } else {
            error = kaa_create_profile_manager(&((*context_p)->profile_manager), (*context_p)->status, (*context_p)->channel_manager);
            if (error) {
                KAA_LOG_ERROR(logger, error, failed_msg, "profile manager");
            } else {
#ifndef KAA_DISABLE_FEATURE_EVENTS
                error = kaa_create_event_manager(&((*context_p)->event_manager));
                if (error) {
                    KAA_LOG_ERROR(logger, error, failed_msg, "event manager");
                } else {
#endif
                    error = kaa_create_bootstrap_manager(&((*context_p)->bootstrap_manager));
                    if (error) {
                        KAA_LOG_ERROR(logger, error, failed_msg, "bootstrap manager");
                    } else {
#ifndef KAA_DISABLE_FEATURE_LOGGING
                        error = kaa_create_log_collector(&((*context_p)->log_collector));
                        if (error) {
                            KAA_LOG_ERROR(logger, error, failed_msg, "log collector");
                        } else {
#endif
                            error = kaa_user_manager_create(&((*context_p)->user_manager), (*context_p)->status, (*context_p)->channel_manager);
                            if (error) {
                                KAA_LOG_ERROR(logger, error, failed_msg, "user manager");
#ifndef KAA_DISABLE_FEATURE_LOGGING
                                kaa_destroy_log_collector((*context_p)->log_collector);
                            }
#endif
                        }
                        if (error)
                            kaa_destroy_bootstrap_manager((*context_p)->bootstrap_manager);
                    }
#ifndef KAA_DISABLE_FEATURE_EVENTS
                    if (error)
                        kaa_destroy_event_manager((*context_p)->event_manager);
                }
#endif
                if (error)
                    kaa_destroy_profile_manager((*context_p)->profile_manager);
            }
            if (error)
                kaa_channel_manager_destroy((*context_p)->channel_manager);
        }
        if (error)
            kaa_destroy_status((*context_p)->status);
    }
    if (error) {
        KAA_FREE(*context_p);
        *context_p = NULL;
    }
    return error;
}

kaa_error_t kaa_context_destroy(kaa_context_t * context)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);

    kaa_user_manager_destroy(context->user_manager);
#ifndef KAA_DISABLE_FEATURE_EVENTS
    kaa_destroy_event_manager(context->event_manager);
#endif
    kaa_destroy_profile_manager(context->profile_manager);
    kaa_destroy_bootstrap_manager(context->bootstrap_manager);
    kaa_channel_manager_destroy(context->channel_manager);
    kaa_destroy_status(context->status);
#ifndef KAA_DISABLE_FEATURE_LOGGING
    kaa_destroy_log_collector(context->log_collector);
#endif
    KAA_FREE(context);
    return KAA_ERR_NONE;
}

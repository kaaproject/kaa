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

extern kaa_error_t kaa_profile_manager_create(kaa_profile_manager_t ** profile_manager_p, kaa_status_t *status, kaa_channel_manager_t *channel_manager);
extern void        kaa_profile_manager_destroy(kaa_profile_manager_t *self);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_logger_t *logger);
extern void        kaa_channel_manager_destroy(kaa_channel_manager_t *self);

extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void        kaa_status_destroy(kaa_status_t *self);

extern kaa_error_t kaa_event_manager_create(kaa_event_manager_t **event_manager_p, kaa_status_t *status, kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void        kaa_event_manager_destroy(kaa_event_manager_t *self);

extern kaa_error_t kaa_log_collector_create(kaa_log_collector_t ** log_collector_p, kaa_status_t *status, kaa_channel_manager_t *channel_manager);
extern void        kaa_log_collector_destroy(kaa_log_collector_t *self);

extern kaa_error_t kaa_bootstrap_manager_create(kaa_bootstrap_manager_t **bootstrap_manager_p, kaa_logger_t *logger);
extern void        kaa_bootstrap_manager_destroy(kaa_bootstrap_manager_t *self);


kaa_error_t kaa_context_create(kaa_context_t ** context_p, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL2(context_p, logger, KAA_ERR_BADPARAM);

    *context_p = (kaa_context_t *) KAA_MALLOC(sizeof(kaa_context_t));
    KAA_RETURN_IF_NIL(*context_p, KAA_ERR_NOMEM);

    (*context_p)->logger = logger;
    (*context_p)->global_request_id = 0;

    static const char failed_msg[] = "Failed to create Kaa %s";

    kaa_error_t error = kaa_status_create(&((*context_p)->status));
    if (error) {
        KAA_LOG_ERROR(logger, error, failed_msg, "status");
    } else {
        error = kaa_channel_manager_create(&((*context_p)->channel_manager), logger);
        if (error) {
            KAA_LOG_ERROR(logger, error, failed_msg, "channel manager");
        } else {
            error = kaa_profile_manager_create(&((*context_p)->profile_manager), (*context_p)->status, (*context_p)->channel_manager);
            if (error) {
                KAA_LOG_ERROR(logger, error, failed_msg, "profile manager");
            } else {
#ifndef KAA_DISABLE_FEATURE_EVENTS
                error = kaa_event_manager_create(&((*context_p)->event_manager), (*context_p)->status, (*context_p)->channel_manager, (*context_p)->logger);
                if (error) {
                    KAA_LOG_ERROR(logger, error, failed_msg, "event manager");
                } else {
#endif
                    error = kaa_bootstrap_manager_create(&((*context_p)->bootstrap_manager), (*context_p)->logger);
                    if (error) {
                        KAA_LOG_ERROR(logger, error, failed_msg, "bootstrap manager");
                    } else {
#ifndef KAA_DISABLE_FEATURE_LOGGING
                        error = kaa_log_collector_create(&((*context_p)->log_collector), (*context_p)->status, (*context_p)->channel_manager);
                        if (error) {
                            KAA_LOG_ERROR(logger, error, failed_msg, "log collector");
                        } else {
#endif
                            error = kaa_user_manager_create(&((*context_p)->user_manager), (*context_p)->status, (*context_p)->channel_manager);
                            if (error) {
                                KAA_LOG_ERROR(logger, error, failed_msg, "user manager");
#ifndef KAA_DISABLE_FEATURE_LOGGING
                                kaa_log_collector_destroy((*context_p)->log_collector);
                            }
#endif
                        }
                        if (error)
                            kaa_bootstrap_manager_destroy((*context_p)->bootstrap_manager);
                    }
#ifndef KAA_DISABLE_FEATURE_EVENTS
                    if (error)
                        kaa_event_manager_destroy((*context_p)->event_manager);
                }
#endif
                if (error)
                    kaa_profile_manager_destroy((*context_p)->profile_manager);
            }
            if (error)
                kaa_channel_manager_destroy((*context_p)->channel_manager);
        }
        if (error)
            kaa_status_destroy((*context_p)->status);
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
    kaa_event_manager_destroy(context->event_manager);
#endif
    kaa_profile_manager_destroy(context->profile_manager);
    kaa_bootstrap_manager_destroy(context->bootstrap_manager);
    kaa_channel_manager_destroy(context->channel_manager);
    kaa_status_destroy(context->status);
#ifndef KAA_DISABLE_FEATURE_LOGGING
    kaa_log_collector_destroy(context->log_collector);
#endif
    KAA_FREE(context);
    return KAA_ERR_NONE;
}

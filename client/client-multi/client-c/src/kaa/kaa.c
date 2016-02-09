/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include <stddef.h>
#include <stdbool.h>
#include <stdint.h>
#include "platform/stdio.h"
#include "platform/ext_sha.h"
#include "kaa_status.h"
#include "kaa.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"

#include "kaa_common.h"

#include "kaa_context.h"
#include "kaa_defaults.h"
#include "platform/ext_transport_channel.h"
#include "platform/ext_key_utils.h"

/*
 * External constructors and destructors from around the Kaa SDK
 */
extern kaa_error_t kaa_user_manager_create(kaa_user_manager_t **user_manager_p, kaa_status_t *status,
                                           kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);

extern void kaa_user_manager_destroy(kaa_user_manager_t *user_manager);

extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern kaa_error_t kaa_status_save(kaa_status_t *kaa_status_p);
extern void kaa_status_destroy(kaa_status_t *self);

extern kaa_error_t kaa_profile_manager_create(kaa_profile_manager_t **profile_manager_p, kaa_status_t *status,
                                              kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void kaa_profile_manager_destroy(kaa_profile_manager_t *self);
extern bool kaa_profile_manager_is_profile_set(kaa_profile_manager_t *self);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_context_t *context);
extern void kaa_channel_manager_destroy(kaa_channel_manager_t *self);
extern kaa_transport_channel_interface_t *kaa_channel_manager_get_transport_channel(kaa_channel_manager_t *self,
                                                                                    kaa_service_t service_type);

#ifndef KAA_DISABLE_FEATURE_EVENTS
extern kaa_error_t kaa_event_manager_create(kaa_event_manager_t **event_manager_p, kaa_status_t *status,
                                            kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void kaa_event_manager_destroy(kaa_event_manager_t *self);
#endif

#ifndef KAA_DISABLE_FEATURE_LOGGING
extern kaa_error_t kaa_log_collector_create(kaa_log_collector_t ** log_collector_p, kaa_status_t *status,
                                            kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void kaa_log_collector_destroy(kaa_log_collector_t *self);
#endif

#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
extern kaa_error_t kaa_configuration_manager_create(kaa_configuration_manager_t **configuration_manager_p,
                                                    kaa_channel_manager_t *channel_manager, kaa_status_t *status,
                                                    kaa_logger_t *logger);
extern void kaa_configuration_manager_destroy(kaa_configuration_manager_t *self);
#endif

extern kaa_error_t kaa_bootstrap_manager_create(kaa_bootstrap_manager_t **bootstrap_manager_p, kaa_context_t *kaa_context);

extern void kaa_bootstrap_manager_destroy(kaa_bootstrap_manager_t *self);

extern kaa_error_t kaa_platform_protocol_create(kaa_platform_protocol_t **platform_protocol_p, kaa_context_t *context,
                                                kaa_status_t *status);
extern void kaa_platform_protocol_destroy(kaa_platform_protocol_t *self);

struct kaa_status_holder_t {
    kaa_status_t *status_instance;
};

extern kaa_error_t kaa_status_set_registered(kaa_status_t *self, bool is_registered);

#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
extern kaa_error_t kaa_notification_manager_create(kaa_notification_manager_t **self, kaa_status_t *status
                                                 , kaa_channel_manager_t *channel_manager
                                                 , kaa_logger_t *logger);
extern void kaa_notification_manager_destroy(kaa_notification_manager_t *self);
#endif

extern kaa_error_t kaa_failover_strategy_create(kaa_failover_strategy_t** strategy, kaa_logger_t *logger);
extern void kaa_failover_strategy_destroy(kaa_failover_strategy_t* strategy);
extern bool kaa_bootstrap_manager_process_failover(kaa_bootstrap_manager_t *self);

/* Forward declaration */
static kaa_error_t kaa_context_destroy(kaa_context_t *context);

static kaa_error_t kaa_context_create(kaa_context_t **context_p, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL2(context_p, logger, KAA_ERR_BADPARAM);

    kaa_context_t *context = KAA_MALLOC(sizeof(*context));
    KAA_RETURN_IF_NIL(context, KAA_ERR_NOMEM);

    context->logger = logger;

    kaa_error_t error = KAA_ERR_NONE;
    context->status = KAA_MALLOC(sizeof(*context->status));
    if (!context->status)
        error = KAA_ERR_NOMEM;

    if (!error)
        error = kaa_status_create(&context->status->status_instance);

    if (!error)
        error = kaa_platform_protocol_create(&context->platform_protocol, context,
                                             context->status->status_instance);

    if (!error)
        error = kaa_channel_manager_create(&context->channel_manager, context);

    if (!error)
        error = kaa_bootstrap_manager_create(&context->bootstrap_manager, context);

    if (!error)
        error = kaa_profile_manager_create(&context->profile_manager, context->status->status_instance,
                                           context->channel_manager, context->logger);

    if (!error)
        error = kaa_failover_strategy_create(&context->failover_strategy, logger);

#ifndef KAA_DISABLE_FEATURE_EVENTS
    if (!error)
        error = kaa_event_manager_create(&context->event_manager, context->status->status_instance,
                                         context->channel_manager, context->logger);
#else
    context->event_manager = NULL;
#endif

#ifndef KAA_DISABLE_FEATURE_LOGGING
    if (!error)
        error = kaa_log_collector_create(&context->log_collector, context->status->status_instance,
                                         context->channel_manager, context->logger);
#else
    context->log_collector = NULL;
#endif

#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
    if (!error)
        error = kaa_configuration_manager_create(&context->configuration_manager, context->channel_manager,
                                                 context->status->status_instance, context->logger);
#else
    context->configuration_manager = NULL;
#endif

#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
    if (!error)
        error = kaa_notification_manager_create(&context->notification_manager, context->status->status_instance,
                                                context->channel_manager, context->logger);
#else
    context->notification_manager = NULL;
#endif

    if (!error)
        error = kaa_user_manager_create(&context->user_manager, context->status->status_instance,
                                        context->channel_manager, context->logger);


    if (error) {
        kaa_context_destroy(context);
        *context_p = NULL;
    } else {
        *context_p = context;
    }

    return error;
}

static kaa_error_t kaa_context_destroy(kaa_context_t *context)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);

    kaa_user_manager_destroy(context->user_manager);
#ifndef KAA_DISABLE_FEATURE_EVENTS
    kaa_event_manager_destroy(context->event_manager);
#endif
    kaa_profile_manager_destroy(context->profile_manager);
    kaa_bootstrap_manager_destroy(context->bootstrap_manager);
    kaa_channel_manager_destroy(context->channel_manager);
    kaa_status_destroy(context->status->status_instance);
    kaa_failover_strategy_destroy(context->failover_strategy);
    KAA_FREE(context->status);
#ifndef KAA_DISABLE_FEATURE_LOGGING
    kaa_log_collector_destroy(context->log_collector);
#endif
#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
    kaa_configuration_manager_destroy(context->configuration_manager);
#endif
#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
    kaa_notification_manager_destroy(context->notification_manager);
#endif
    kaa_platform_protocol_destroy(context->platform_protocol);
    KAA_FREE(context);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_init(kaa_context_t **kaa_context_p)
{
    KAA_RETURN_IF_NIL(kaa_context_p, KAA_ERR_BADPARAM);

    // Initialize logger
    kaa_logger_t *logger = NULL;
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL); // TODO: make log destination configurable
    if (error)
        return error;

    KAA_LOG_INFO(logger, KAA_ERR_NONE, "Kaa SDK version %s, commit hash %s", KAA_BUILD_VERSION, KAA_BUILD_COMMIT_HASH);

    // Initialize general Kaa context
    error = kaa_context_create(kaa_context_p, logger);
    if (error) {
        KAA_LOG_FATAL(logger, error, "Failed to create Kaa context");
        kaa_log_destroy(logger);
        *kaa_context_p = NULL;
        return error;
    }

    // Initialize endpoint identity
    char *pub_key_buffer = NULL;
    size_t pub_key_buffer_size = 0;
    bool need_deallocation = false;

    ext_get_endpoint_public_key(&pub_key_buffer, &pub_key_buffer_size, &need_deallocation);

    kaa_digest pub_key_hash;
    error = ext_calculate_sha_hash(pub_key_buffer, pub_key_buffer_size, pub_key_hash);

    if (need_deallocation && pub_key_buffer_size > 0) {
        KAA_FREE(pub_key_buffer);
    }

    if (error) {
        KAA_LOG_FATAL(logger, error, "Failed to calculate EP ID");
        kaa_context_destroy(*kaa_context_p);
        *kaa_context_p = NULL;
        kaa_log_destroy(logger);
        return error;
    }

    error = ext_copy_sha_hash((*kaa_context_p)->status->status_instance->endpoint_public_key_hash, pub_key_hash);
    if (error) {
        KAA_LOG_FATAL(logger, error, "Failed to set Endpoint public key");
        kaa_context_destroy(*kaa_context_p);
        *kaa_context_p = NULL;
        kaa_log_destroy(logger);
        return error;
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_start(kaa_context_t *kaa_context)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(kaa_context->logger, KAA_ERR_NONE, "Going to start Kaa endpoint");

    kaa_transport_channel_interface_t *bootstrap_channel = kaa_channel_manager_get_transport_channel(
            kaa_context->channel_manager, KAA_SERVICE_BOOTSTRAP);
    if (bootstrap_channel) {
        const kaa_service_t bootstrap_service[] = { KAA_SERVICE_BOOTSTRAP };
        kaa_error_t error = bootstrap_channel->sync_handler(bootstrap_channel->context, bootstrap_service, 1);
        if (error) {
            KAA_LOG_ERROR(kaa_context->logger, error, "Failed to sync Bootstrap service. Try again later");
            return error;
        }
    } else {
        KAA_LOG_FATAL(kaa_context->logger, KAA_ERR_NOT_FOUND, "Could not find Bootstrap transport channel");
        return KAA_ERR_NOT_FOUND;
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_stop(kaa_context_t *kaa_context)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_status_save(kaa_context->status->status_instance);
}

kaa_error_t kaa_deinit(kaa_context_t *kaa_context)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);

    kaa_logger_t *logger = kaa_context->logger;
    kaa_error_t error = kaa_context_destroy(kaa_context);
    if (error)
        KAA_LOG_ERROR(logger, error, "Failed to destroy Kaa context");
    kaa_log_destroy(logger);
    return error;
}

bool kaa_process_failover(kaa_context_t *kaa_context)
{
    KAA_RETURN_IF_NIL(kaa_context, false);
    return kaa_bootstrap_manager_process_failover(kaa_context->bootstrap_manager);
}

kaa_error_t kaa_context_set_status_registered(kaa_context_t *kaa_context, bool is_registered)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_status_set_registered(kaa_context->status->status_instance, is_registered);
}

kaa_error_t kaa_check_readiness(kaa_context_t *kaa_context)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    if (!kaa_profile_manager_is_profile_set(kaa_context->profile_manager)) {
        KAA_LOG_ERROR(kaa_context->logger, KAA_ERR_PROFILE_IS_NOT_SET, "Profile isn't set");
        return KAA_ERR_PROFILE_IS_NOT_SET;
    }

    return KAA_ERR_NONE;
}

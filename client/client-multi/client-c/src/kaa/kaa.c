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

#include "kaa_private.h"

#include <stddef.h>
#include <stdbool.h>
#include <stdint.h>
#include "platform/stdio.h"
#include "platform/ext_sha.h"
#include "kaa_status.h"
#include "kaa.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"

#include "kaa_channel_manager.h"
#include "kaa_defaults.h"
#include "platform/ext_key_utils.h"

#ifdef KAA_ENCRYPTION
#include "platform/ext_encryption_utils.h"
#endif

#include <kaa_extension.h>

#ifndef KAA_DISABLE_FEATURE_PROFILE
#include "kaa_profile_private.h"
#endif

static kaa_error_t kaa_context_destroy(kaa_context_t *context);

static kaa_error_t kaa_context_create(kaa_context_t **context_p, kaa_logger_t *logger)
{
    // TODO(KAA-982): use asserts
    if (!context_p || !logger) {
        return KAA_ERR_BADPARAM;
    }

    kaa_context_t *context = KAA_CALLOC(1, sizeof(*context));
    if (!context) {
        return KAA_ERR_NOMEM;
    }

    context->logger = logger;

    kaa_error_t error = KAA_ERR_NONE;
    context->status = KAA_CALLOC(1, sizeof(*context->status));
    if (!context->status) {
        error = KAA_ERR_NOMEM;
        goto exit;
    }

    error = kaa_status_create(&context->status->status_instance);
    if (error) {
        goto exit;
    }

    error = kaa_platform_protocol_create(&context->platform_protocol, context->logger,
            context->status->status_instance);
    if (error) {
        goto exit;
    }

    error = kaa_channel_manager_create(&context->channel_manager, context);
    if (error) {
        goto exit;
    }

    error = kaa_extension_init_all(context);
    if (error) {
        goto exit;
    }

    error = kaa_failover_strategy_create(&context->failover_strategy, logger);
    if (error) {
        goto extensions_deinit;
    }

    *context_p = context;

    return KAA_ERR_NONE;

extensions_deinit:
    kaa_extension_deinit_all();

exit:
    kaa_context_destroy(context);

    return error;
}

static kaa_error_t kaa_context_destroy(kaa_context_t *context)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);

    kaa_channel_manager_destroy(context->channel_manager);
    kaa_status_destroy(context->status->status_instance);
    kaa_failover_strategy_destroy(context->failover_strategy);
    KAA_FREE(context->status);
    kaa_platform_protocol_destroy(context->platform_protocol);
    KAA_FREE(context);
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_init_keys(void)
{
    kaa_error_t error = kaa_init_rsa_keypair();
    if (error) {
        return error;
    }
#ifdef KAA_ENCRYPTION
    error = kaa_init_session_key();
    if (error) {
        return error;
    }
#endif
    return error;
}

static void kaa_deinit_keys(void)
{
    kaa_deinit_rsa_keypair();
}

kaa_error_t kaa_init(kaa_context_t **kaa_context_p)
{
    KAA_RETURN_IF_NIL(kaa_context_p, KAA_ERR_BADPARAM);

    // Initialize logger
    kaa_logger_t *logger = NULL;
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL); // TODO: make log destination configurable
    if (error) {
        return error;
    }

    KAA_LOG_INFO(logger, KAA_ERR_NONE, "Kaa SDK version %s, commit hash %s", KAA_BUILD_VERSION, KAA_BUILD_COMMIT_HASH);

    // Initialize general Kaa context
    error = kaa_context_create(kaa_context_p, logger);
    if (error) {
        KAA_LOG_FATAL(logger, error, "Failed to create Kaa context");
        *kaa_context_p = NULL;
        return error;
    }

    // Initialize endpoint identity
    uint8_t *sha1 = NULL;
    size_t sha1_size = 0;

    error = kaa_init_keys();
    if (error) {
        KAA_LOG_ERROR(logger, error, "Failed to initialize keys");
        return error;
    }

    ext_get_sha1_public(&sha1, &sha1_size);
    ext_copy_sha_hash((*kaa_context_p)->status->status_instance->endpoint_public_key_hash, sha1);

    return kaa_status_set_updated((*kaa_context_p)->status->status_instance, true);
}

kaa_error_t kaa_start(kaa_context_t *kaa_context)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(kaa_context->logger, KAA_ERR_NONE, "Going to start Kaa endpoint");

    kaa_transport_channel_interface_t *bootstrap_channel = kaa_channel_manager_get_transport_channel(
            kaa_context->channel_manager, KAA_EXTENSION_BOOTSTRAP);
    if (bootstrap_channel) {
        const kaa_extension_id bootstrap_service[] = { KAA_EXTENSION_BOOTSTRAP };
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
    if (error) {
        KAA_LOG_ERROR(logger, error, "Failed to destroy Kaa context");
    }
    kaa_log_destroy(logger);
    kaa_deinit_keys();
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

#ifndef KAA_DISABLE_FEATURE_PROFILE
    if (!kaa_profile_manager_is_profile_set(kaa_context->profile_manager)) {
        KAA_LOG_ERROR(kaa_context->logger, KAA_ERR_PROFILE_IS_NOT_SET, "Profile isn't set");
        return KAA_ERR_PROFILE_IS_NOT_SET;
    }
#endif

    return KAA_ERR_NONE;
}

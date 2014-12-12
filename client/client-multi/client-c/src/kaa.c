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

#include <string.h>
#include <stdint.h>

#include "kaa.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_common.h"
#include "kaa_context.h"
#include "kaa_external.h"
#include "kaa_defaults.h"
#include "kaa_status.h"


extern kaa_error_t kaa_context_create(kaa_context_t **context, kaa_logger_t *logger);
extern kaa_error_t kaa_context_destroy(kaa_context_t * context);


kaa_error_t kaa_init(kaa_context_t **kaa_context_p)
{
    KAA_RETURN_IF_NIL(kaa_context_p, KAA_ERR_BADPARAM);

    // Initialize logger
    kaa_logger_t *logger = NULL;
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);  // TODO: make log destination configurable
    if (error)
        return error;

    KAA_LOG_INFO(logger, KAA_ERR_NONE, "Kaa SDK version %s, commit hash %s", BUILD_VERSION, BUILD_COMMIT_HASH);

    // Initialize general Kaa context
    error = kaa_context_create(kaa_context_p, logger);
    if (error) {
        KAA_LOG_FATAL(logger, error, "Failed to create Kaa context");
        kaa_log_destroy(logger);
        return error;
    }

    // Initialize endpoint identity
    char *pub_key_buffer = NULL;
    size_t pub_key_buffer_size = 0;
    bool need_deallocation = false;

    kaa_get_endpoint_public_key(&pub_key_buffer, &pub_key_buffer_size, &need_deallocation);
    kaa_digest d;
    error = kaa_calculate_sha_hash(pub_key_buffer, pub_key_buffer_size, d);

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

    return kaa_status_set_endpoint_public_key_hash((*kaa_context_p)->status, d);
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

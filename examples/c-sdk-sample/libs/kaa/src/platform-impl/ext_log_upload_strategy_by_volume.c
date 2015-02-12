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

/**
 * @file ext_log_upload_strategy_by_volume.h
 * @brief Simple sample implementation of the log upload strategy interface defined in ext_log_upload_strategy.h.
 * Makes decisions purely based on the amount of logs collected in the storage.
 */

#include "../platform/platform.h"

#include "../platform/ext_log_upload_strategy.h"
#include "../kaa_common.h"
#include "../utilities/kaa_mem.h"



typedef struct {
    size_t  max_upload_threshold;
    size_t  max_log_bucket_size;
    size_t  max_cleanup_threshold;
} ext_log_upload_strategy_t;



kaa_error_t ext_log_upload_strategy_by_volume_create(void **strategy_p
        , size_t max_upload_threshold, size_t max_log_bucket_size
        , size_t max_cleanup_threshold)
{
    KAA_RETURN_IF_NIL3(strategy_p, max_upload_threshold, max_log_bucket_size, KAA_ERR_BADPARAM);
    if (max_cleanup_threshold && ((max_cleanup_threshold < max_log_bucket_size) || (max_cleanup_threshold < max_upload_threshold)))
        return KAA_ERR_BADPARAM;

    ext_log_upload_strategy_t *strategy = (ext_log_upload_strategy_t *) KAA_MALLOC(sizeof(ext_log_upload_strategy_t));
    KAA_RETURN_IF_NIL(strategy, KAA_ERR_NOMEM);

    strategy->max_upload_threshold = max_upload_threshold;
    strategy->max_log_bucket_size = max_log_bucket_size;
    strategy->max_cleanup_threshold = max_cleanup_threshold;
    *strategy_p = strategy;

    return KAA_ERR_NONE;
}



void ext_log_upload_strategy_by_volume_destroy(void *self)
{
    if (self)
        KAA_FREE((ext_log_upload_strategy_t *)self);
}



ext_log_upload_decision_t ext_log_upload_strategy_decide(void *context, const void *log_storage_context, size_t *volume)
{
    KAA_RETURN_IF_NIL3(context, log_storage_context, volume, NOOP);
    ext_log_upload_strategy_t *self = (ext_log_upload_strategy_t *)context;
    size_t storage_size = ext_log_storage_get_total_size(log_storage_context);

    if (self->max_cleanup_threshold && (storage_size > self->max_cleanup_threshold)) {
        // Request to cleanup below the threshold by max bucket size to prevent cleanup dead loop
        *volume = storage_size - self->max_cleanup_threshold + self->max_log_bucket_size;
        return CLEANUP;
    }

    if (storage_size >= self->max_upload_threshold) {
        *volume = self->max_log_bucket_size;
        return UPLOAD;
    }

    *volume = 0;
    return NOOP;
}

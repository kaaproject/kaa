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

#include "platform/ext_log_upload_strategy.h"
#include <stddef.h>
#include "kaa_common.h"
#include "utilities/kaa_mem.h"




struct ext_log_upload_strategy_t {
    size_t  max_upload_threshold;
    size_t  max_log_bucket_size;
    size_t  max_cleanup_threshold;
};



kaa_error_t ext_log_upload_strategy_create(ext_log_upload_strategy_t **strategy_p
        , size_t max_upload_threshold, size_t max_log_bucket_size
        , size_t max_cleanup_threshold)
{
    KAA_RETURN_IF_NIL3(strategy_p, max_upload_threshold, max_log_bucket_size, KAA_ERR_BADPARAM);
    if (max_cleanup_threshold && ((max_cleanup_threshold < max_log_bucket_size) || (max_cleanup_threshold < max_upload_threshold)))
        return KAA_ERR_BADPARAM;

    *strategy_p = (ext_log_upload_strategy_t *) KAA_MALLOC(sizeof(ext_log_upload_strategy_t));
    KAA_RETURN_IF_NIL(*strategy_p, KAA_ERR_NOMEM);

    (*strategy_p)->max_upload_threshold = max_upload_threshold;
    (*strategy_p)->max_log_bucket_size = max_log_bucket_size;
    (*strategy_p)->max_cleanup_threshold = max_cleanup_threshold;

    return KAA_ERR_NONE;
}



void ext_log_upload_strategy_destroy(ext_log_upload_strategy_t *self)
{
    if (self)
        KAA_FREE(self);
}



ext_log_upload_decision_t ext_log_upload_strategy_decide(ext_log_upload_strategy_t *self
        , const ext_log_storage_t *log_storage, size_t *volume)
{
    KAA_RETURN_IF_NIL2(log_storage, volume, NOOP);
    size_t storage_size = ext_log_storage_get_total_size(log_storage);

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

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

#ifndef KAA_DISABLE_FEATURE_LOGGING

#include "kaa_private.h"
#include "ext_log_upload_strategies.h"
#include <platform/ext_log_upload_strategy.h>
#include <platform/ext_transport_channel.h>
#include <platform/time.h>
#include "utilities/kaa_mem.h"
#include "kaa_channel_manager.h"

/**
 * @brief The default value (in seconds) for time to wait a log delivery response.
 */
#define KAA_DEFAULT_UPLOAD_TIMEOUT             2 * 60

/**
 * @brief The default value (in seconds) for time to postpone log upload.
 */
#define KAA_DEFAULT_RETRY_PERIOD               5 * 60

/**
 * @brief The default value (in bytes) for log volume to initiate the log upload.
 */
#define KAA_DEFAULT_UPLOAD_VOLUME_THRESHOLD    8 * 1024

/**
 * @brief The default value for the log count to initiate the log upload.
 */
#define KAA_DEFAULT_UPLOAD_COUNT_THRESHOLD     64

/**
 * @brief The default value for Max amount of log batches allowed to be uploaded parallel.
 */
#define KAA_DEFAULT_MAX_PARALLEL_UPLOADS       INT32_MAX



typedef struct {
    uint8_t   type;
    size_t    threshold_volume;
    size_t    threshold_count;
    size_t    upload_timeout;
    size_t    max_parallel_uploads;
    size_t    timeout;

    size_t    log_batch_size;
    size_t    upload_retry_period;

    time_t    upload_retry_ts;

    kaa_channel_manager_t   *channel_manager;
    kaa_bootstrap_manager_t *bootstrap_manager;
} ext_log_upload_strategy_t;

/*
 * Strategy implementation.
 */

kaa_error_t ext_log_upload_strategy_create(struct kaa_context_s *context, void **strategy_p, uint8_t type)
{
    KAA_RETURN_IF_NIL5(strategy_p, context, context->channel_manager, context->bootstrap_manager, type, KAA_ERR_BADPARAM);

    ext_log_upload_strategy_t *strategy = (ext_log_upload_strategy_t *) KAA_MALLOC(sizeof(ext_log_upload_strategy_t));
    KAA_RETURN_IF_NIL(strategy, KAA_ERR_NOMEM);

    KAA_RETURN_IF_ERR( ext_log_upload_strategy_set_threshold_volume(strategy, KAA_DEFAULT_UPLOAD_VOLUME_THRESHOLD) );
    KAA_RETURN_IF_ERR( ext_log_upload_strategy_set_threshold_count(strategy, KAA_DEFAULT_UPLOAD_COUNT_THRESHOLD) );
    KAA_RETURN_IF_ERR( ext_log_upload_strategy_set_upload_timeout(strategy, KAA_DEFAULT_UPLOAD_TIMEOUT) );
    KAA_RETURN_IF_ERR( ext_log_upload_strategy_set_upload_retry_period(strategy, KAA_DEFAULT_RETRY_PERIOD) );
    KAA_RETURN_IF_ERR( ext_log_upload_strategy_set_max_parallel_uploads(strategy, KAA_DEFAULT_MAX_PARALLEL_UPLOADS) );

    strategy->type = type;
    strategy->upload_retry_ts = 0;
    strategy->timeout = KAA_TIME() + strategy->upload_timeout;

    strategy->bootstrap_manager = context->bootstrap_manager;
    strategy->channel_manager   = context->channel_manager;

    *strategy_p = strategy;

    return KAA_ERR_NONE;
}



void ext_log_upload_strategy_destroy(void *self)
{
    if (self)
        KAA_FREE((ext_log_upload_strategy_t *)self);
}



ext_log_upload_decision_t ext_log_upload_strategy_decide(void *context, const void *log_storage_context)
{
    KAA_RETURN_IF_NIL2(context, log_storage_context, NOOP);

    ext_log_upload_decision_t decision = NOOP;
    ext_log_upload_strategy_t *self = (ext_log_upload_strategy_t *)context;

    if (self->upload_retry_ts) {
        if (KAA_TIME() >= (kaa_time_t)self->upload_retry_ts) {
            // force upload after retry timeout has elapsed
            self->upload_retry_ts = 0;
            decision = UPLOAD;
        }
        return decision;
    }

    if ((self->type & 0x04) && KAA_TIME() >= (kaa_time_t)self->timeout) {
        decision = UPLOAD;
        self->timeout = KAA_TIME() + self->upload_timeout;
    } else if ((self->type & 0x01) && ext_log_storage_get_total_size(log_storage_context) >= self->threshold_volume) {
        decision = UPLOAD;
    } else if ((self->type & 0x02) && ext_log_storage_get_records_count(log_storage_context) >= self->threshold_count) {
        decision = UPLOAD;
    }

    return decision;
}

size_t ext_log_upload_strategy_get_timeout(void *context)
{
    KAA_RETURN_IF_NIL(context, 0);
    return ((ext_log_upload_strategy_t *)context)->upload_timeout;
}



size_t ext_log_upload_strategy_get_max_parallel_uploads(void *context)
{
    KAA_RETURN_IF_NIL(context, 0);
    return ((ext_log_upload_strategy_t *)context)->max_parallel_uploads;
}



kaa_error_t ext_log_upload_strategy_on_timeout(void *context)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);

    ext_log_upload_strategy_t *self = (ext_log_upload_strategy_t *)context;
    kaa_transport_channel_interface_t *channel = kaa_channel_manager_get_transport_channel(self->channel_manager
                                                                                         , KAA_EXTENSION_LOGGING);
    if (channel) {
        self->upload_retry_ts = 0;
        kaa_transport_protocol_id_t protocol_id;
        kaa_error_t error_code = channel->get_protocol_id(channel->context, &protocol_id);
        KAA_RETURN_IF_ERR(error_code);
        error_code = kaa_bootstrap_manager_on_access_point_failed(self->bootstrap_manager,
                                                                  &protocol_id,
                                                                  KAA_SERVER_OPERATIONS,
                                                                  KAA_OPERATION_SERVERS_NA);
        return error_code;
    }

    return KAA_ERR_NOT_FOUND;
}



kaa_error_t ext_log_upload_strategy_on_failure(void *context, logging_delivery_error_code_t error_code)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);
    ext_log_upload_strategy_t *self = (ext_log_upload_strategy_t *)context;

    switch (error_code) {
    case NO_APPENDERS_CONFIGURED:
    case APPENDER_INTERNAL_ERROR:
    case REMOTE_CONNECTION_ERROR:
    case REMOTE_INTERNAL_ERROR:
        self->upload_retry_ts = KAA_TIME() + self->upload_retry_period;
        break;
    default:
        break;
    }

    return KAA_ERR_NONE;
}



kaa_error_t ext_log_upload_strategy_change_strategy(void *strategy, uint8_t type)
{
    KAA_RETURN_IF_NIL2(strategy, type, KAA_ERR_BADPARAM);
    ((ext_log_upload_strategy_t *)strategy)->type = type;
    return KAA_ERR_NONE;
}

bool ext_log_upload_strategy_is_timeout_strategy(void *strategy)
{
    KAA_RETURN_IF_NIL(strategy, KAA_ERR_BADPARAM);
    ext_log_upload_strategy_t *self = (ext_log_upload_strategy_t *)strategy;
    if (self->type & TIMEOUT_FLAG)
        return true;
    return false;
}

kaa_error_t ext_log_upload_strategy_set_threshold_volume(void *strategy, size_t threshold_volume)
{
    KAA_RETURN_IF_NIL2(strategy, threshold_volume, KAA_ERR_BADPARAM);
    ((ext_log_upload_strategy_t *)strategy)->threshold_volume = threshold_volume;
    return KAA_ERR_NONE;
}



kaa_error_t ext_log_upload_strategy_set_threshold_count(void *strategy, size_t threshold_count)
{
    KAA_RETURN_IF_NIL2(strategy, threshold_count, KAA_ERR_BADPARAM);
    ((ext_log_upload_strategy_t *)strategy)->threshold_count = threshold_count;
    return KAA_ERR_NONE;
}



kaa_error_t ext_log_upload_strategy_set_batch_size(void *strategy, size_t log_batch_size)
{
    KAA_RETURN_IF_NIL2(strategy, log_batch_size, KAA_ERR_BADPARAM);
    ((ext_log_upload_strategy_t *)strategy)->log_batch_size = log_batch_size;
    return KAA_ERR_NONE;
}



kaa_error_t ext_log_upload_strategy_set_upload_timeout(void *strategy, size_t upload_timeout)
{
    KAA_RETURN_IF_NIL2(strategy, upload_timeout, KAA_ERR_BADPARAM);
    ((ext_log_upload_strategy_t *)strategy)->upload_timeout = upload_timeout;
    ((ext_log_upload_strategy_t *)strategy)->timeout = KAA_TIME() + upload_timeout;
    return KAA_ERR_NONE;
}



kaa_error_t ext_log_upload_strategy_set_max_parallel_uploads(void *strategy, size_t count)
{
    KAA_RETURN_IF_NIL2(strategy, count, KAA_ERR_BADPARAM);
    ((ext_log_upload_strategy_t *)strategy)->max_parallel_uploads = count;
    return KAA_ERR_NONE;
}



kaa_error_t ext_log_upload_strategy_set_upload_retry_period(void *strategy, size_t upload_retry_period)
{
    KAA_RETURN_IF_NIL2(strategy, upload_retry_period, KAA_ERR_BADPARAM);
    ((ext_log_upload_strategy_t *)strategy)->upload_retry_period = upload_retry_period;
    return KAA_ERR_NONE;
}

#endif

/* ISO C forbids an empty translation unit */
typedef int make_iso_compilers_happy;

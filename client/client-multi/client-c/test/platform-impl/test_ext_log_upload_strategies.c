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

#include <stdint.h>
#include <stdio.h>
#include <unistd.h>

#include "kaa_test.h"

#include "utilities/kaa_log.h"

#include "kaa_context.h"
#include "kaa_bootstrap_manager.h"
#include "kaa_channel_manager.h"

#include "platform/ext_log_storage.h"
#include "platform/ext_log_upload_strategy.h"

#include "platform-impl/common/ext_log_upload_strategies.h"

typedef struct {
    uint8_t type;
    size_t  total_size;
    size_t  record_count;
    size_t  upload_timeout;
} test_log_storage_context_t;



static kaa_context_t kaa_context;
static kaa_logger_t *logger = NULL;
static kaa_channel_manager_t *channel_manager = NULL;
static kaa_bootstrap_manager_t *bootstrap_manager = NULL;
static void *strategy = NULL;



size_t ext_log_storage_get_total_size(const void *context)
{
    KAA_RETURN_IF_NIL(context, 0);
    return ((test_log_storage_context_t *)context)->total_size;
}


size_t ext_log_storage_get_records_count(const void *context)
{
    KAA_RETURN_IF_NIL(context, 0);
    return ((test_log_storage_context_t *)context)->record_count;
}


void test_create_strategy(void **state)
{
    (void)state;
    (void)state;

    void *tmp_strategy = NULL;

    kaa_error_t error_code = ext_log_upload_strategy_create(&kaa_context, NULL,
            KAA_LOG_UPLOAD_VOLUME_STRATEGY);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_create(NULL, &tmp_strategy, KAA_LOG_UPLOAD_VOLUME_STRATEGY);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_create(&kaa_context, &tmp_strategy, 0);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_create(&kaa_context, &tmp_strategy, KAA_LOG_UPLOAD_VOLUME_STRATEGY);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(tmp_strategy);

    ext_log_upload_strategy_destroy(tmp_strategy);
}

void test_set_upload_timeout(void **state)
{
    (void)state;
    (void)state;

    size_t DEFAULT_UPLOAD_TIMEOUT = 2 * 60;

    kaa_error_t error_code = ext_log_upload_strategy_change_strategy(strategy,
            KAA_LOG_UPLOAD_VOLUME_STRATEGY);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_set_upload_timeout(strategy, DEFAULT_UPLOAD_TIMEOUT);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    ASSERT_EQUAL(ext_log_upload_strategy_get_timeout(strategy), DEFAULT_UPLOAD_TIMEOUT);
}

void test_upload_decision_by_volume(void **state)
{
    (void)state;

    size_t DEFAULT_UPLOAD_VOLUME_THRESHOLD = 8 * 1024;

    kaa_error_t error_code = ext_log_upload_strategy_change_strategy(strategy,
            KAA_LOG_UPLOAD_BY_STORAGE_SIZE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_set_threshold_volume(strategy, DEFAULT_UPLOAD_VOLUME_THRESHOLD);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    test_log_storage_context_t log_storage_context;

    log_storage_context.total_size = DEFAULT_UPLOAD_VOLUME_THRESHOLD - 1;
    log_storage_context.record_count = 0;
    ext_log_upload_decision_t upload_decision = ext_log_upload_strategy_decide(strategy,
            &log_storage_context);
    ASSERT_EQUAL(upload_decision, NOOP);

    log_storage_context.total_size = DEFAULT_UPLOAD_VOLUME_THRESHOLD;
    log_storage_context.record_count = 0;
    upload_decision = ext_log_upload_strategy_decide(strategy, &log_storage_context);
    ASSERT_EQUAL(upload_decision, UPLOAD);

    log_storage_context.total_size = DEFAULT_UPLOAD_VOLUME_THRESHOLD + 1;
    log_storage_context.record_count = 0;
    upload_decision = ext_log_upload_strategy_decide(strategy, &log_storage_context);
    ASSERT_EQUAL(upload_decision, UPLOAD);
}

void test_upload_decision_by_count(void **state)
{
    (void)state;

    size_t DEFAULT_UPLOAD_COUNT_THRESHOLD  = 64;

    kaa_error_t error_code = ext_log_upload_strategy_change_strategy(strategy, KAA_LOG_UPLOAD_BY_RECORD_COUNT);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_set_threshold_count(strategy, DEFAULT_UPLOAD_COUNT_THRESHOLD);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    test_log_storage_context_t log_storage_context;

    log_storage_context.total_size = 0;
    log_storage_context.record_count = DEFAULT_UPLOAD_COUNT_THRESHOLD - 1;
    ext_log_upload_decision_t upload_decision = ext_log_upload_strategy_decide(strategy,
            &log_storage_context);
    ASSERT_EQUAL(upload_decision, NOOP);

    log_storage_context.total_size = 0;
    log_storage_context.record_count = DEFAULT_UPLOAD_COUNT_THRESHOLD;
    upload_decision = ext_log_upload_strategy_decide(strategy, &log_storage_context);
    ASSERT_EQUAL(upload_decision, UPLOAD);

    log_storage_context.total_size = 0;
    log_storage_context.record_count = DEFAULT_UPLOAD_COUNT_THRESHOLD + 1;
    upload_decision = ext_log_upload_strategy_decide(strategy, &log_storage_context);
    ASSERT_EQUAL(upload_decision, UPLOAD);
}

void test_upload_decision_by_timeout(void **state)
{
    (void)state;

    size_t DEFAULT_UPLOAD_TIMEOUT_THRESHOLD  = 1;// in sec.

    kaa_error_t error_code = ext_log_upload_strategy_change_strategy(strategy,
            KAA_LOG_UPLOAD_BY_TIMEOUT_STRATEGY);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_set_upload_timeout(strategy, DEFAULT_UPLOAD_TIMEOUT_THRESHOLD);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    test_log_storage_context_t log_storage_context;

    log_storage_context.total_size = 0;
    log_storage_context.record_count = 0;
    log_storage_context.upload_timeout = DEFAULT_UPLOAD_TIMEOUT_THRESHOLD;
    ext_log_upload_decision_t upload_decision = ext_log_upload_strategy_decide(strategy,
            &log_storage_context);
    ASSERT_EQUAL(upload_decision, NOOP);

    log_storage_context.upload_timeout = DEFAULT_UPLOAD_TIMEOUT_THRESHOLD + 1;

    sleep(DEFAULT_UPLOAD_TIMEOUT_THRESHOLD);

    upload_decision = ext_log_upload_strategy_decide(strategy, &log_storage_context);
    ASSERT_EQUAL(upload_decision, UPLOAD);
}

void test_noop_decision_on_failure(void **state)
{
    (void)state;

    size_t DEFAULT_UPLOAD_VOLUME_THRESHOLD = 8 * 1024;
    size_t DEFAULT_UPLOAD_COUNT_THRESHOLD  = 64;

    kaa_error_t error_code = ext_log_upload_strategy_change_strategy(strategy,
            KAA_LOG_UPLOAD_VOLUME_STRATEGY);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_set_threshold_volume(strategy, DEFAULT_UPLOAD_VOLUME_THRESHOLD);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = ext_log_upload_strategy_set_threshold_count(strategy, DEFAULT_UPLOAD_COUNT_THRESHOLD);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_on_failure(NULL, NO_APPENDERS_CONFIGURED);
    ASSERT_NOT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_on_failure(strategy, NO_APPENDERS_CONFIGURED);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    test_log_storage_context_t log_storage_context;

    log_storage_context.total_size = DEFAULT_UPLOAD_VOLUME_THRESHOLD;
    log_storage_context.record_count = DEFAULT_UPLOAD_COUNT_THRESHOLD;
    ext_log_upload_decision_t upload_decision = ext_log_upload_strategy_decide(strategy,
            &log_storage_context);
    ASSERT_EQUAL(upload_decision, NOOP);
}

void test_upload_decision_on_failure(void **state)
{
    (void)state;

    size_t DEFAULT_RETRY_PERIOD = 1;
    size_t DEFAULT_UPLOAD_VOLUME_THRESHOLD = 8 * 1024;
    size_t DEFAULT_UPLOAD_COUNT_THRESHOLD  = 64;

    kaa_error_t error_code = ext_log_upload_strategy_change_strategy(strategy,
            KAA_LOG_UPLOAD_VOLUME_STRATEGY);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_set_threshold_volume(strategy, DEFAULT_UPLOAD_VOLUME_THRESHOLD);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = ext_log_upload_strategy_set_threshold_count(strategy, DEFAULT_UPLOAD_COUNT_THRESHOLD);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = ext_log_upload_strategy_set_upload_retry_period(strategy, DEFAULT_RETRY_PERIOD);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_on_failure(strategy, NO_APPENDERS_CONFIGURED);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    test_log_storage_context_t log_storage_context;

    log_storage_context.total_size = DEFAULT_UPLOAD_VOLUME_THRESHOLD;
    log_storage_context.record_count = DEFAULT_UPLOAD_COUNT_THRESHOLD;
    ext_log_upload_decision_t upload_decision = ext_log_upload_strategy_decide(strategy,
            &log_storage_context);
    ASSERT_EQUAL(upload_decision, NOOP);

    sleep(DEFAULT_RETRY_PERIOD);

    log_storage_context.total_size = DEFAULT_UPLOAD_VOLUME_THRESHOLD;
    log_storage_context.record_count = DEFAULT_UPLOAD_COUNT_THRESHOLD;
    upload_decision = ext_log_upload_strategy_decide(strategy, &log_storage_context);
    ASSERT_EQUAL(upload_decision, UPLOAD);
}

int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger) {
        return error;
    }

    kaa_context.logger = logger;

    error = kaa_channel_manager_create(&channel_manager, &kaa_context);
    if (error || !channel_manager) {
        return error;
    }
    kaa_context.channel_manager = channel_manager;

    error = kaa_bootstrap_manager_create(&bootstrap_manager, &kaa_context);
    if (error || !bootstrap_manager) {
        return error;
    }
    kaa_context.bootstrap_manager = bootstrap_manager;

    return ext_log_upload_strategy_create(&kaa_context, &strategy, KAA_LOG_UPLOAD_VOLUME_STRATEGY);
}

int test_deinit(void)
{
    kaa_bootstrap_manager_destroy(bootstrap_manager);
    kaa_channel_manager_destroy(channel_manager);
    kaa_log_destroy(logger);
    ext_log_upload_strategy_destroy(strategy);

    return 0;
}



KAA_SUITE_MAIN(MetaExtension, test_init, test_deinit,
        KAA_TEST_CASE(create_strategy, test_create_strategy)
        KAA_TEST_CASE(set_upload_timeout, test_set_upload_timeout)
        KAA_TEST_CASE(upload_decision_by_volume, test_upload_decision_by_volume)
        KAA_TEST_CASE(upload_decision_by_count, test_upload_decision_by_count)
        KAA_TEST_CASE(upload_decision_by_timeout, test_upload_decision_by_timeout)
        KAA_TEST_CASE(noop_decision_on_failure, test_noop_decision_on_failure)
        KAA_TEST_CASE(upload_decision_on_failure, test_upload_decision_on_failure)
)

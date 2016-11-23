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

#include <stdio.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include <stdint.h>
#include <arpa/inet.h>
#include "platform/ext_sha.h"

#include "kaa.h"
#include "kaa_test.h"
#include "kaa_context.h"
#include "kaa_platform_protocol.h"
#include "kaa_channel_manager.h"
#include "kaa_platform_utils.h"
#include "kaa_status.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "platform/ext_log_storage.h"
#include "platform/ext_log_upload_strategy.h"
#include "platform-impl/common/ext_log_upload_strategies.h"

#include "kaa_private.h"

#include "kaa_logging.h"
#include "kaa_logging_private.h"

void test_empty_log_collector_extension_count(void **state)
{
    kaa_context_t *kaa_context = *state;

    kaa_extension_id services[] = { KAA_EXTENSION_LOGGING };

    void *log_storage_context = NULL;
    void *log_upload_strategy_context = NULL;

    kaa_error_t error_code = ext_unlimited_log_storage_create(&log_storage_context,
            kaa_context->logger);
    assert_int_equal(KAA_ERR_NONE, error_code);

    error_code = ext_log_upload_strategy_create(kaa_context, &log_upload_strategy_context,
            KAA_LOG_UPLOAD_VOLUME_STRATEGY);
    assert_int_equal(KAA_ERR_NONE, error_code);


    kaa_log_bucket_constraints_t constraints = {
        .max_bucket_size = 1024,
        .max_bucket_log_count = UINT32_MAX,
    };

    error_code = kaa_logging_init(kaa_context->log_collector, log_storage_context,
            log_upload_strategy_context, &constraints);
    assert_int_equal(KAA_ERR_NONE, error_code);

    uint8_t *buffer = NULL;
    size_t buffer_size = 0;
    error_code = kaa_platform_protocol_alloc_serialize_client_sync(kaa_context->platform_protocol,
            services, 1,
            &buffer, &buffer_size);
    assert_int_equal(KAA_ERR_NONE, error_code);

    uint16_t count_of_extensions = ntohs(*(uint16_t *)(buffer + 6));
    assert_int_equal(1, count_of_extensions);

    KAA_FREE(buffer);
}

int test_init(void **state)
{
    return kaa_init((kaa_context_t **)state);
}

int test_deinit(void **state)
{
    kaa_deinit(*state);
    return 0;
}

int main(void)
{
    const struct CMUnitTest tests[] = {
        cmocka_unit_test(test_empty_log_collector_extension_count),
    };

    return cmocka_run_group_tests(tests, test_init, test_deinit);
}

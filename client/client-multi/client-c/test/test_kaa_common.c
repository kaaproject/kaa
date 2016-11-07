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
#include <stdint.h>

#include "platform/ext_sha.h"
#include "kaa_test.h"
#include "kaa_common.h"
#include "kaa_error.h"
#include "utilities/kaa_log.h"

static kaa_logger_t *logger = NULL;

void test_profile_update(void **state)
{
    (void)state;

    char* pattern = "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3";

    kaa_error_t err;
    char* body = "test";
    kaa_digest calculated_hash;

    err = ext_calculate_sha_hash(NULL, 50, calculated_hash);
    ASSERT_NOT_EQUAL(err, KAA_ERR_NONE);
    err = ext_calculate_sha_hash(body, 0, calculated_hash);
    ASSERT_EQUAL(err, KAA_ERR_NONE);
    err = ext_calculate_sha_hash(body, 4, NULL);
    ASSERT_NOT_EQUAL(err, KAA_ERR_NONE);

    err = ext_calculate_sha_hash(body, 4, calculated_hash);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    char buf[SHA_1_DIGEST_LENGTH * 2 + 1];
    for (int i = 0; i < SHA_1_DIGEST_LENGTH; ++i) {
        sprintf(&buf[i * 2], "%02x", calculated_hash[i]);
    }

    ASSERT_EQUAL(0, memcmp(buf, pattern, SHA_1_DIGEST_LENGTH * 2));
}

int test_init(void)
{
    kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    return 0;
}

int test_deinit(void)
{
    kaa_log_destroy(logger);
    return 0;
}

KAA_SUITE_MAIN(Common, test_init, test_deinit
        , KAA_TEST_CASE(calculate_hash, test_profile_update)
)

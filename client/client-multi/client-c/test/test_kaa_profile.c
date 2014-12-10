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

#include "kaa_status.h"
#include "kaa_test.h"
#include "kaa_mem.h"
#include "kaa_log.h"
#include "kaa_context.h"
#include "kaa_profile.h"
#include "gen/kaa_profile_gen.h"

extern kaa_error_t kaa_context_create(kaa_context_t **context, kaa_logger_t *logger);
extern kaa_error_t kaa_context_destroy(kaa_context_t * context);

static kaa_logger_t *logger = NULL;

extern kaa_error_t kaa_profile_need_profile_resync(kaa_profile_manager_t *kaa_context, bool *result);

void test_profile_update()
{
    KAA_TRACE_IN(logger);

    kaa_context_t * context = NULL;
    kaa_error_t err_code = kaa_context_create(&context, logger);
    ASSERT_EQUAL(err_code, KAA_ERR_NONE);

    kaa_profile_t *profile1 = kaa_profile_basic_endpoint_profile_test_create();
    profile1->profile_body = kaa_string_copy_create("dummy", kaa_data_destroy);
    kaa_profile_update_profile(context->profile_manager, profile1);

    bool need_resync = false;
    ASSERT_EQUAL(kaa_profile_need_profile_resync(context->profile_manager, &need_resync), KAA_ERR_NONE);
    ASSERT_TRUE(need_resync);

    kaa_profile_update_profile(context->profile_manager, profile1);

    ASSERT_EQUAL(kaa_profile_need_profile_resync(context->profile_manager, &need_resync), KAA_ERR_NONE);
    ASSERT_FALSE(need_resync);

    profile1->destroy(profile1);

    kaa_profile_t *profile2 = kaa_profile_basic_endpoint_profile_test_create();
    profile2->profile_body = kaa_string_copy_create("new_dummy", kaa_data_destroy);
    kaa_profile_update_profile(context->profile_manager, profile2);

    ASSERT_EQUAL(kaa_profile_need_profile_resync(context->profile_manager, &need_resync), KAA_ERR_NONE);
    ASSERT_TRUE(need_resync);

    profile2->destroy(profile2);

    kaa_context_destroy(context);
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

KAA_SUITE_MAIN(Profile, test_init, test_deinit,
        KAA_TEST_CASE(profile_update, test_profile_update)
)

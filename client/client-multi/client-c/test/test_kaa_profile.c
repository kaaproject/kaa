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
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_context.h"
#include "kaa_profile.h"
#include "gen/kaa_profile_gen.h"



extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void        kaa_status_destroy(kaa_status_t *self);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_logger_t *logger);
extern void        kaa_channel_manager_destroy(kaa_channel_manager_t *self);

extern kaa_error_t kaa_profile_manager_create(kaa_profile_manager_t **profile_manager_p, kaa_status_t *status
        , kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void        kaa_profile_manager_destroy(kaa_profile_manager_t *self);

extern kaa_error_t kaa_profile_need_profile_resync(kaa_profile_manager_t *kaa_context, bool *result);



static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_channel_manager_t *channel_manager = NULL;
static kaa_profile_manager_t *profile_manager = NULL;



void test_profile_update()
{
    KAA_TRACE_IN(logger);

    kaa_profile_t *profile1 = kaa_profile_basic_endpoint_profile_test_create();
    profile1->profile_body = kaa_string_copy_create("dummy", kaa_data_destroy);
    kaa_error_t error = kaa_profile_update_profile(profile_manager, profile1);
    ASSERT_EQUAL(error, KAA_ERR_NONE);

    bool need_resync = false;
    error = kaa_profile_need_profile_resync(profile_manager, &need_resync);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_TRUE(need_resync);

    error = kaa_profile_update_profile(profile_manager, profile1);
    ASSERT_EQUAL(error, KAA_ERR_NONE);

    error = kaa_profile_need_profile_resync(profile_manager, &need_resync);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_FALSE(need_resync);

    profile1->destroy(profile1);

    kaa_profile_t *profile2 = kaa_profile_basic_endpoint_profile_test_create();
    profile2->profile_body = kaa_string_copy_create("new_dummy", kaa_data_destroy);
    error = kaa_profile_update_profile(profile_manager, profile2);
    ASSERT_EQUAL(error, KAA_ERR_NONE);

    error = kaa_profile_need_profile_resync(profile_manager, &need_resync);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_TRUE(need_resync);

    profile2->destroy(profile2);
}



int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_NOT_NULL(logger);

    error = kaa_status_create(&status);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_NOT_NULL(status);

    error = kaa_channel_manager_create(&channel_manager, logger);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_NOT_NULL(channel_manager);

    error = kaa_profile_manager_create(&profile_manager, status, channel_manager, logger);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_NOT_NULL(profile_manager);

    return 0;
}



int test_deinit(void)
{
    kaa_profile_manager_destroy(profile_manager);
    kaa_channel_manager_destroy(channel_manager);
    kaa_status_destroy(status);
    kaa_log_destroy(logger);
    return 0;
}



KAA_SUITE_MAIN(Profile, test_init, test_deinit,
        KAA_TEST_CASE(profile_update, test_profile_update)
)

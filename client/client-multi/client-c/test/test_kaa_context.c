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

#include "kaa_context.h"
#include "kaa_test.h"
#include "kaa_log.h"

void test_create_bootstrap_manager()
{
    KAA_TRACE_IN;

    kaa_context_t * context = NULL;

    kaa_error_t err_code = kaa_create_context(&context);
    CU_ASSERT_EQUAL_FATAL(err_code, KAA_ERR_NONE);
    CU_ASSERT_PTR_NOT_NULL_FATAL(context);
    CU_ASSERT_PTR_NOT_NULL_FATAL(context->bootstrap_manager);
    CU_ASSERT_PTR_NOT_NULL_FATAL(context->channel_manager);
#ifndef KAA_DISABLE_FEATURE_EVENTS
    CU_ASSERT_PTR_NOT_NULL_FATAL(context->event_manager);
#endif
    CU_ASSERT_PTR_NOT_NULL_FATAL(context->profile_manager);
    CU_ASSERT_PTR_NOT_NULL_FATAL(context->status);
    CU_ASSERT_PTR_NOT_NULL_FATAL(context->user_manager);

    kaa_destroy_context(context);
}

KAA_SUITE_MAIN(Context, NULL, NULL
        , KAA_TEST_CASE(create_context, test_create_bootstrap_manager)
)

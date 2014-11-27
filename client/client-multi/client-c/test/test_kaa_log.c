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

#include "kaa_logging.h"

#ifndef KAA_DISABLE_FEATURE_LOGGING

#include "kaa_context.h"
#include "log/kaa_memory_log_storage.h"
#include "kaa_test.h"
#include "kaa_mem.h"
#include <stdio.h>

void test_create_log_collector()
{
    kaa_log_collector_t * collector = NULL;
    kaa_error_t err_code = kaa_create_log_collector(&collector);
    ASSERT_EQUAL(err_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(collector);
    kaa_destroy_log_collector(collector);
}

static kaa_service_t services[4] = { KAA_SERVICE_PROFILE, KAA_SERVICE_USER, KAA_SERVICE_EVENT, KAA_SERVICE_LOGGING };
void test_create_request()
{
    kaa_init();
    kaa_sync_request_t *request = NULL;
    size_t s;
    kaa_error_t err_code = kaa_compile_request(&request, &s, 4, services);
    ASSERT_EQUAL(err_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(request);
    ASSERT_NOT_NULL(request->log_sync_request);
    ASSERT_EQUAL(request->log_sync_request->type ,KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_NULL_BRANCH);

    request->destruct(request);
    KAA_FREE(request);
    kaa_deinit();
}

static kaa_uuid_t test_uuid;
static uint32_t stub_upload_uuid_check_call_count = 0;
void stub_upload_uuid_check(kaa_uuid_t uuid)
{
    stub_upload_uuid_check_call_count++;
    ASSERT_EQUAL(kaa_uuid_compare(&uuid, &test_uuid), 0);
}

void test_response()
{
    kaa_log_sync_response_t log_sync_response;
    log_sync_response.result = ENUM_SYNC_RESPONSE_RESULT_TYPE_SUCCESS;
    log_sync_response.request_id = "42";
    kaa_uuid_fill(&test_uuid, 42);

    kaa_context_t *ctx = NULL;
    kaa_create_context(&ctx);

    kaa_log_storage_t *ls = get_memory_log_storage();
    ls->upload_failed = &stub_upload_uuid_check;
    ls->upload_succeeded = &stub_upload_uuid_check;

    kaa_storage_status_t *ss = get_memory_log_storage_status();
    kaa_log_upload_properties_t *lp = get_memory_log_upload_properties();

    kaa_init_log_collector(ctx->log_collector, ls, lp, ss, &memory_log_storage_is_upload_needed);

    kaa_logging_handle_sync(ctx, &log_sync_response);
    ASSERT_EQUAL(stub_upload_uuid_check_call_count,1);

    kaa_destroy_context(ctx);
}

#define DEAFULT_LOG_RECORD 0
#if DEAFULT_LOG_RECORD
static kaa_log_upload_decision_t decision(kaa_storage_status_t *status)
{
    if ((* status->get_records_count)() == 2) {
        return UPLOAD;
    }
    return NOOP;
}

static void handler(size_t service_count, const kaa_service_t services[])
{
    ASSERT_EQUAL(1, service_count);
    ASSERT_EQUAL(services[0], KAA_SERVICE_LOGGING);

    kaa_sync_request_t *request = NULL;
    kaa_compile_request(&request, service_count, services);
    ASSERT_NOT_NULL(request);
    ASSERT_NOT_NULL(request->log_sync_request);
    ASSERT_EQUAL(request->log_sync_request->type ,KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_LOG_SYNC_REQUEST_BRANCH);

    request->destruct(request);
    KAA_FREE(request);
}
void test_add_log()
{
    kaa_init();
    kaa_set_sync_handler(&handler, 4, services);
    kaa_init_log_storage(get_memory_log_storage(), get_memory_log_storage_status(), get_memory_log_upload_properties(), &decision);

    kaa_user_log_record_t *record = kaa_create_test_log_record();
    for (int i = 1000000; i--; ) {
        kaa_add_log(record);
    }
    kaa_deinit();

    KAA_FREE(record);
}
#endif

#endif

KAA_SUITE_MAIN(Log, NULL, NULL
#ifndef KAA_DISABLE_FEATURE_LOGGING
       ,
       KAA_TEST_CASE(create_log_collector, test_create_log_collector)
       KAA_TEST_CASE(create_request, test_create_request)
       KAA_TEST_CASE(process_response, test_response)
#if DEAFULT_LOG_RECORD
       KAA_TEST_CASE(add_log_record, test_add_log)
#endif
#endif
        )

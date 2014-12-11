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

#include <stdio.h>

#include "kaa.h"
#include "kaa_platform_protocol.h"
#include "log/kaa_memory_log_storage.h"
#include "kaa_test.h"
#include "kaa_mem.h"
#include "kaa_log.h"
#include "kaa_channel_manager.h"
#include "kaa_profile.h"

extern kaa_error_t kaa_context_create(kaa_context_t **context, kaa_logger_t *logger);
extern kaa_error_t kaa_context_destroy(kaa_context_t * context);

extern kaa_error_t kaa_logging_handle_sync(kaa_log_collector_t *self
                                         , kaa_log_sync_response_t *response);

static kaa_logger_t *logger = NULL;

static const kaa_service_t services[4] = { KAA_SERVICE_PROFILE
                                         , KAA_SERVICE_USER
                                         , KAA_SERVICE_EVENT
                                         , KAA_SERVICE_LOGGING };


void test_create_request()
{
    kaa_context_t *kaa_context = NULL;
    kaa_error_t error = kaa_init(&kaa_context);
    ASSERT_EQUAL(error, KAA_ERR_NONE);

    kaa_profile_t *profile = kaa_profile_basic_endpoint_profile_test_create();
    profile->profile_body = kaa_string_move_create("body", NULL);
    kaa_profile_update_profile(kaa_context->profile_manager, profile);

    size_t s;
    kaa_sync_request_t *request = NULL;
    error = kaa_compile_request(kaa_context, &request, &s, 4, services);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_NOT_NULL(request);
    ASSERT_NOT_NULL(request->log_sync_request);
    ASSERT_EQUAL(request->log_sync_request->type, KAA_UNION_LOG_SYNC_REQUEST_OR_NULL_BRANCH_1);

    request->destroy(request);
    profile->destroy(profile);
    kaa_deinit(kaa_context);
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

    log_sync_response.request_id = kaa_string_move_create("42", NULL);
    kaa_uuid_fill(&test_uuid, 42);

    kaa_context_t *ctx = NULL;
    kaa_context_create(&ctx, logger);

    kaa_log_storage_t *ls = get_memory_log_storage();
    ls->upload_failed = &stub_upload_uuid_check;
    ls->upload_succeeded = &stub_upload_uuid_check;

    kaa_storage_status_t *ss = get_memory_log_storage_status();
    kaa_log_upload_properties_t *lp = get_memory_log_upload_properties();

    kaa_logging_init(ctx->log_collector, ls, lp, ss, &memory_log_storage_is_upload_needed);

    kaa_logging_handle_sync(ctx->log_collector, &log_sync_response);
    ASSERT_EQUAL(stub_upload_uuid_check_call_count,1);

    kaa_context_destroy(ctx);
    kaa_string_destroy(log_sync_response.request_id);
}

#define DEFAULT_LOG_RECORD 0
#if DEFAULT_LOG_RECORD
static kaa_context_t *kaa_context_ = NULL;
static kaa_log_upload_decision_t decision(kaa_storage_status_t *status)
{
    if ((* status->get_records_count)() == 1000) {
        return UPLOAD;
    }
    return NOOP;
}

static void handler(const kaa_service_t services[], size_t service_count)
{
    ASSERT_EQUAL(1, service_count);
    ASSERT_EQUAL(services[0], KAA_SERVICE_LOGGING);

    kaa_sync_request_t *request = NULL;
    size_t request_size = 0;
    kaa_compile_request(kaa_context_, &request, &request_size, service_count, services);
    ASSERT_NOT_NULL(request);
    ASSERT_NOT_NULL(request->log_sync_request);

    request->destroy(request);

}
void test_add_log()
{
    ASSERT_TRUE(1);
    kaa_init(&kaa_context_);

    kaa_channel_manager_add_sync_handler(kaa_context_->channel_manager, &handler, services, 4);
    set_memory_log_storage_logger(kaa_context_->logger);
    kaa_logging_init(kaa_context_->log_collector, get_memory_log_storage(), get_memory_log_upload_properties(), get_memory_log_storage_status(), &decision);

    kaa_user_log_record_t *record = kaa_test_log_record_create();
    record->data = kaa_string_move_create("Super Log Record", NULL);
    for (int i = 1000000; i--; ) {
        kaa_logging_add_record(kaa_context_->log_collector, record);
    }
    record->destroy(record);
    kaa_deinit(kaa_context_);
}
#endif

#endif

int test_init(void)
{
    kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    set_memory_log_storage_logger(logger);
    return 0;
}

int test_deinit(void)
{
    kaa_log_destroy(logger);
    set_memory_log_storage_logger(NULL);
    return 0;
}

KAA_SUITE_MAIN(Log, test_init, test_deinit
#ifndef KAA_DISABLE_FEATURE_LOGGING
       ,
       KAA_TEST_CASE(create_request, test_create_request)
       KAA_TEST_CASE(process_response, test_response)
#if DEFAULT_LOG_RECORD
       KAA_TEST_CASE(add_log_record, test_add_log)
#endif
#endif
        )

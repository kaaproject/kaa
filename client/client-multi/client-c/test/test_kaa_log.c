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

#include "kaa_test.h"
#include "kaa.h"
#include "kaa_platform_protocol.h"
#include "log/kaa_memory_log_storage.h"
#include "kaa_channel_manager.h"
#include "kaa_profile.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"



extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void        kaa_status_destroy(kaa_status_t *self);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_logger_t *logger);
extern void        kaa_channel_manager_destroy(kaa_channel_manager_t *self);

extern kaa_error_t kaa_log_collector_create(kaa_log_collector_t ** log_collector_p
        , kaa_status_t *status, kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void        kaa_log_collector_destroy(kaa_log_collector_t *self);

extern kaa_error_t kaa_logging_handle_sync(kaa_log_collector_t *self, kaa_log_sync_response_t *response);



static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_channel_manager_t *channel_manager = NULL;
static kaa_log_collector_t *log_collector = NULL;

#define NUM_OF_SERVICES 4
static const kaa_service_t services[NUM_OF_SERVICES] = {
        KAA_SERVICE_PROFILE
        , KAA_SERVICE_USER
        , KAA_SERVICE_EVENT
        , KAA_SERVICE_LOGGING
};



static const char* allocate_buffer(void* context, size_t buffer_size)
{
    KAA_LOG_DEBUG(logger, KAA_ERR_NONE, "In allocate_buffer(), requested size: %u", buffer_size);
    char **buffer_to_alloc_p = (char**) context;
    *buffer_to_alloc_p = KAA_MALLOC(buffer_size * sizeof(char));
    return *buffer_to_alloc_p;
}



void test_create_request()
{
    kaa_context_t *kaa_context = NULL;
    kaa_error_t error = kaa_init(&kaa_context);
    ASSERT_EQUAL(error, KAA_ERR_NONE);

    kaa_profile_t *profile = kaa_profile_basic_endpoint_profile_test_create();
    profile->profile_body = kaa_string_move_create("body", NULL);
    kaa_profile_update_profile(kaa_context->profile_manager, profile);

    char *buffer = NULL;
    error = kaa_platform_protocol_serialize_client_sync(kaa_context->platfrom_protocol, services, NUM_OF_SERVICES, allocate_buffer, &buffer);

    ASSERT_EQUAL(error, KAA_ERR_NONE);

    KAA_FREE(buffer);
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

    kaa_log_storage_t *ls = get_memory_log_storage();
    ls->upload_failed = &stub_upload_uuid_check;
    ls->upload_succeeded = &stub_upload_uuid_check;

    kaa_storage_status_t *ss = get_memory_log_storage_status();
    kaa_log_upload_properties_t *lp = get_memory_log_upload_properties();

    kaa_logging_init(log_collector, ls, lp, ss, &memory_log_storage_is_upload_needed);

    kaa_logging_handle_sync(log_collector, &log_sync_response);
    ASSERT_EQUAL(stub_upload_uuid_check_call_count,1);

    kaa_string_destroy(log_sync_response.request_id);
}


#endif


int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger) {
        return error;
    }
    set_memory_log_storage_logger(logger);


#ifndef KAA_DISABLE_FEATURE_LOGGING
    error = kaa_status_create(&status);
    if (error || !status) {
        return error;
    }

    error = kaa_channel_manager_create(&channel_manager, logger);
    if (error || !channel_manager) {
        return error;
    }

    error = kaa_log_collector_create(&log_collector, status, channel_manager, logger);
    if (error || !log_collector) {
        return error;
    }
#endif
    return 0;
}



int test_deinit(void)
{
#ifndef KAA_DISABLE_FEATURE_LOGGING
    kaa_log_collector_destroy(log_collector);
    kaa_channel_manager_destroy(channel_manager);
    kaa_status_destroy(status);
#endif
    kaa_log_destroy(logger);
    set_memory_log_storage_logger(NULL);
    return 0;
}



KAA_SUITE_MAIN(Log, test_init, test_deinit
#ifndef KAA_DISABLE_FEATURE_LOGGING
       ,
       KAA_TEST_CASE(create_request, test_create_request)
       KAA_TEST_CASE(process_response, test_response)
#endif
        )

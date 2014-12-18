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
#include "kaa_defaults.h"
#include "kaa_external.h"
#include "gen/kaa_profile_gen.h"



extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void        kaa_status_destroy(kaa_status_t *self);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_logger_t *logger);
extern void        kaa_channel_manager_destroy(kaa_channel_manager_t *self);

extern kaa_error_t kaa_profile_manager_create(kaa_profile_manager_t **profile_manager_p, kaa_status_t *status
        , kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void        kaa_profile_manager_destroy(kaa_profile_manager_t *self);

extern kaa_error_t kaa_profile_need_profile_resync(kaa_profile_manager_t *kaa_context, bool *result);

extern kaa_error_t kaa_profile_compile_request(kaa_profile_manager_t *self, kaa_profile_sync_request_t **result);
extern kaa_error_t kaa_profile_handle_sync(kaa_profile_manager_t *self, kaa_profile_sync_response_t *response);


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

void test_profile_compile_request()
{
    KAA_TRACE_IN(logger);

    kaa_profile_t *profile1 = kaa_profile_basic_endpoint_profile_test_create();
    profile1->profile_body = kaa_string_copy_create("dummy2", kaa_data_destroy);

    size_t serialized_profile_size = profile1->get_size(profile1);
    char *serialized_profile = (char *) KAA_MALLOC(serialized_profile_size * sizeof(char));
    avro_writer_t writer = avro_writer_memory(serialized_profile, serialized_profile_size);
    profile1->serialize(writer, profile1);
    avro_writer_free(writer);

    ASSERT_EQUAL(kaa_status_set_endpoint_access_token(status, "token1"), KAA_ERR_NONE);
    ASSERT_EQUAL(kaa_profile_update_profile(profile_manager, profile1), KAA_ERR_NONE);

    kaa_profile_sync_request_t *profile_request = NULL;
    ASSERT_EQUAL(kaa_profile_compile_request(profile_manager, &profile_request), KAA_ERR_NONE);
    ASSERT_NOT_NULL(profile_manager);

    ASSERT_EQUAL(profile_request->version_info->config_version, CONFIG_SCHEMA_VERSION);
    ASSERT_EQUAL(profile_request->version_info->log_schema_version, LOG_SCHEMA_VERSION);
    ASSERT_EQUAL(profile_request->version_info->profile_version, PROFILE_SCHEMA_VERSION);
    ASSERT_EQUAL(profile_request->version_info->system_nf_version, SYSTEM_NF_SCHEMA_VERSION);
    ASSERT_EQUAL(profile_request->version_info->user_nf_version, USER_NF_SCHEMA_VERSION);

#if KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0
    kaa_list_t *event_versions = (kaa_list_t *) profile_request->version_info->event_family_versions->data;
    kaa_event_class_family_version_info_t *ecfv1 = (kaa_event_class_family_version_info_t *) kaa_list_get_data(event_versions);
    ASSERT_EQUAL(strcmp(ecfv1->name->data, KAA_EVENT_SCHEMA_VERSIONS[0].name), 0);
    ASSERT_EQUAL(ecfv1->version, KAA_EVENT_SCHEMA_VERSIONS[0].version);
#endif
    ASSERT_EQUAL(memcmp(profile_request->profile_body->buffer, serialized_profile, profile_request->profile_body->size), 0);
    kaa_string_t *token = (kaa_string_t *)profile_request->endpoint_access_token->data;
    ASSERT_EQUAL(strcmp(token->data, "token1"), 0);

    char *pub_key_buffer = NULL;
    size_t buf_size = 0;
    bool pub_key_dealloc = false;
    kaa_get_endpoint_public_key(&pub_key_buffer, &buf_size, &pub_key_dealloc);

    kaa_bytes_t *pub_key_bytes = (kaa_bytes_t *) profile_request->endpoint_public_key->data;
    ASSERT_EQUAL(memcmp(pub_key_bytes->buffer, pub_key_buffer, buf_size), 0);
    if (pub_key_dealloc)
        KAA_FREE(pub_key_buffer);


    profile_request->destroy(profile_request);
    KAA_FREE(serialized_profile);
    profile1->destroy(profile1);
}

void test_profile_compile_request_when_registered()
{
    KAA_TRACE_IN(logger);

    kaa_set_endpoint_registered(status, true);
    kaa_profile_t *profile1 = kaa_profile_basic_endpoint_profile_test_create();
    profile1->profile_body = kaa_string_copy_create("dummy3", kaa_data_destroy);

    ASSERT_EQUAL(kaa_profile_update_profile(profile_manager, profile1), KAA_ERR_NONE);

    kaa_profile_sync_request_t *profile_request = NULL;
    ASSERT_EQUAL(kaa_profile_compile_request(profile_manager, &profile_request), KAA_ERR_NONE);
    ASSERT_NOT_NULL(profile_manager);
    ASSERT_NULL(profile_request->endpoint_public_key->data);

    profile_request->destroy(profile_request);
    kaa_set_endpoint_registered(status, false);

    profile1->destroy(profile1);
}

void test_profile_handle_sync()
{
    KAA_TRACE_IN(logger);

    kaa_profile_t *profile1 = kaa_profile_basic_endpoint_profile_test_create();
    profile1->profile_body = kaa_string_copy_create("dummy4", kaa_data_destroy);

    ASSERT_EQUAL(kaa_profile_update_profile(profile_manager, profile1), KAA_ERR_NONE);

    bool is_registered = false;
    ASSERT_EQUAL(kaa_is_endpoint_registered(status, &is_registered), KAA_ERR_NONE);
    ASSERT_FALSE(is_registered);

    kaa_profile_sync_response_t response;
    response.response_status = ENUM_SYNC_RESPONSE_STATUS_NO_DELTA;
    kaa_profile_handle_sync(profile_manager, &response);
    ASSERT_EQUAL(kaa_is_endpoint_registered(status, &is_registered), KAA_ERR_NONE);
    ASSERT_TRUE(is_registered);

    profile1->destroy(profile1);
}

int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if (error || !logger) {
        return error;
    }

    error = kaa_status_create(&status);
    if (error || !status) {
        return error;
    }

    error = kaa_channel_manager_create(&channel_manager, logger);
    if (error || !channel_manager) {
        return error;
    }

    error = kaa_profile_manager_create(&profile_manager, status, channel_manager, logger);
    if (error || !profile_manager) {
        return error;
    }

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
        KAA_TEST_CASE(profile_request, test_profile_compile_request)
        KAA_TEST_CASE(profile_request_when_registered, test_profile_compile_request_when_registered)
        KAA_TEST_CASE(profile_handle_sync, test_profile_handle_sync)
)

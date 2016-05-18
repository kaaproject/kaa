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

#include <kaa_extension.h>
#include <kaa_common.h>

#include "kaa_test.h"

static void test_kaa_extension_get_all_extensions(void **state)
{
    (void)state;
    assert_non_null(kaa_extension_get(KAA_EXTENSION_BOOTSTRAP));

#ifndef KAA_DISABLE_FEATURE_PROFILE
    assert_non_null(kaa_extension_get(KAA_EXTENSION_PROFILE));
#else
    assert_null(kaa_extension_get(KAA_EXTENSION_PROFILE));
#endif

#ifndef KAA_DISABLE_FEATURE_USER
    assert_non_null(kaa_extension_get(KAA_EXTENSION_USER));
#else
    assert_null(kaa_extension_get(KAA_EXTENSION_USER));
#endif

#ifndef KAA_DISABLE_FEATURE_LOGGING
    assert_non_null(kaa_extension_get(KAA_EXTENSION_LOGGING));
#else
    assert_null(kaa_extension_get(KAA_EXTENSION_LOGGING));
#endif

#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
    assert_non_null(kaa_extension_get(KAA_EXTENSION_CONFIGURATION));
#else
    assert_null(kaa_extension_get(KAA_EXTENSION_CONFIGURATION));
#endif

#ifndef KAA_DISABLE_FEATURE_EVENTS
    assert_non_null(kaa_extension_get(KAA_EXTENSION_EVENT));
#else
    assert_null(kaa_extension_get(KAA_EXTENSION_EVENT));
#endif

#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
    assert_non_null(kaa_extension_get(KAA_EXTENSION_NOTIFICATION));
#else
    assert_null(kaa_extension_get(KAA_EXTENSION_NOTIFICATION));
#endif
}

int main(void)
{
    const struct CMUnitTest tests[] = {
        cmocka_unit_test(test_kaa_extension_get_all_extensions),
    };

    return cmocka_run_group_tests(tests, NULL, NULL);
}

kaa_error_t kaa_extension_bootstrap_init(struct kaa_context_s *kaa_context, void **context)
{ (void)kaa_context; (void)context; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_profile_init(struct kaa_context_s *kaa_context, void **context)
{ (void)kaa_context; (void)context; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_event_init(struct kaa_context_s *kaa_context, void **context)
{ (void)kaa_context; (void)context; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_logging_init(struct kaa_context_s *kaa_context, void **context)
{ (void)kaa_context; (void)context; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_configuration_init(struct kaa_context_s *kaa_context, void **context)
{ (void)kaa_context; (void)context; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_notification_init(struct kaa_context_s *kaa_context, void **context)
{ (void)kaa_context; (void)context; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_user_init(struct kaa_context_s *kaa_context, void **context)
{ (void)kaa_context; (void)context; return KAA_ERR_NONE; }

kaa_error_t kaa_extension_bootstrap_deinit(void *context) { (void)context; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_profile_deinit(void *context) { (void)context; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_event_deinit(void *context) { (void)context; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_logging_deinit(void *context) { (void)context; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_configuration_deinit(void *context) { (void)context; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_notification_deinit(void *context) { (void)context; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_user_deinit(void *context) { (void)context; return KAA_ERR_NONE; }

kaa_error_t kaa_extension_bootstrap_request_get_size(void *self, size_t *expected_size)
{ (void)self; (void)expected_size; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_profile_request_get_size(void *self, size_t *expected_size)
{ (void)self; (void)expected_size; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_event_request_get_size(void *self, size_t *expected_size)
{ (void)self; (void)expected_size; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_logging_request_get_size(void *self, size_t *expected_size)
{ (void)self; (void)expected_size; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_configuration_request_get_size(void *self, size_t *expected_size)
{ (void)self; (void)expected_size; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_notification_request_get_size(void *self, size_t *expected_size)
{ (void)self; (void)expected_size; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_user_request_get_size(void *self, size_t *expected_size)
{ (void)self; (void)expected_size; return KAA_ERR_NONE; }

kaa_error_t kaa_extension_bootstrap_request_serialize(void *context, uint8_t *buffer, size_t *size,
        bool *need_resync)
{ (void)context; (void)buffer; (void)size; (void)need_resync; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_profile_request_serialize(void *context, uint8_t *buffer, size_t *size,
        bool *need_resync)
{ (void)context; (void)buffer; (void)size; (void)need_resync; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_event_request_serialize(void *context, uint8_t *buffer, size_t *size,
        bool *need_resync)
{ (void)context; (void)buffer; (void)size; (void)need_resync; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_logging_request_serialize(void *context, uint8_t *buffer, size_t *size,
        bool *need_resync)
{ (void)context; (void)buffer; (void)size; (void)need_resync; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_configuration_request_serialize(void *context, uint8_t *buffer, size_t *size,
        bool *need_resync)
{ (void)context; (void)buffer; (void)size; (void)need_resync; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_notification_request_serialize(void *context, uint8_t *buffer, size_t *size,
        bool *need_resync)
{ (void)context; (void)buffer; (void)size; (void)need_resync; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_user_request_serialize(void *context, uint8_t *buffer, size_t *size,
        bool *need_resync)
{ (void)context; (void)buffer; (void)size; (void)need_resync; return KAA_ERR_NONE; }

kaa_error_t kaa_extension_bootstrap_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{ (void)context; (void)request_id; (void)extension_options; (void)buffer; (void)size; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_profile_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{ (void)context; (void)request_id; (void)extension_options; (void)buffer; (void)size; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_event_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{ (void)context; (void)request_id; (void)extension_options; (void)buffer; (void)size; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_logging_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{ (void)context; (void)request_id; (void)extension_options; (void)buffer; (void)size; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_configuration_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{ (void)context; (void)request_id; (void)extension_options; (void)buffer; (void)size; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_notification_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{ (void)context; (void)request_id; (void)extension_options; (void)buffer; (void)size; return KAA_ERR_NONE; }
kaa_error_t kaa_extension_user_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{ (void)context; (void)request_id; (void)extension_options; (void)buffer; (void)size; return KAA_ERR_NONE; }

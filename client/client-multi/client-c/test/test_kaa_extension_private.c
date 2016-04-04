#include <kaa_extension.h>
#include <kaa_common.h>

#include "kaa_test.h"

static void test_kaa_extension_get_all_extensions(void **state)
{
    (void)state;
    assert_non_null(kaa_extension_get(KAA_EXTENSION_BOOTSTRAP));
    assert_non_null(kaa_extension_get(KAA_EXTENSION_PROFILE));
    assert_non_null(kaa_extension_get(KAA_EXTENSION_USER));
    assert_non_null(kaa_extension_get(KAA_EXTENSION_LOGGING));
    assert_non_null(kaa_extension_get(KAA_EXTENSION_CONFIGURATION));
    assert_non_null(kaa_extension_get(KAA_EXTENSION_EVENT));
    assert_non_null(kaa_extension_get(KAA_EXTENSION_NOTIFICATION));
}

int main(void)
{
    const struct CMUnitTest tests[] = {
        cmocka_unit_test(test_kaa_extension_get_all_extensions),
    };

    return cmocka_run_group_tests(tests, NULL, NULL);
}

static kaa_error_t stub(struct kaa_context_s *kaa_context, void **context)
{
    (void)kaa_context;
    (void)context;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_extension_bootstrap_init(struct kaa_context_s *kaa_context, void **context)
{
    return stub(kaa_context, context);
}

kaa_error_t kaa_extension_profile_init(struct kaa_context_s *kaa_context, void **context)
{
    return stub(kaa_context, context);
}

kaa_error_t kaa_extension_event_init(struct kaa_context_s *kaa_context, void **context)
{
    return stub(kaa_context, context);
}

kaa_error_t kaa_extension_logging_init(struct kaa_context_s *kaa_context, void **context)
{
    return stub(kaa_context, context);
}

kaa_error_t kaa_extension_configuration_init(struct kaa_context_s *kaa_context, void **context)
{
    return stub(kaa_context, context);
}

kaa_error_t kaa_extension_notification_init(struct kaa_context_s *kaa_context, void **context)
{
    return stub(kaa_context, context);
}

kaa_error_t kaa_extension_user_init(struct kaa_context_s *kaa_context, void **context)
{
    return stub(kaa_context, context);
}

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

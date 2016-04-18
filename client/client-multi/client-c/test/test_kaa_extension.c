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
#include <kaa_extension_private.h>

#include "kaa_test.h"

#define FAKE_EXTENSION1_ID 13
#define FAKE_EXTENSION2_ID 5
#define FAKE_EXTENSION3_ID 17

#define BAD_EXTENSION_ID 3

static void test_kaa_extension_get_wrong_extension(void **state)
{
    (void)state;
    assert_null(kaa_extension_get(0));
    assert_null(kaa_extension_get(4));
    assert_null(kaa_extension_get(100500));
}

static void test_kaa_extension_get_ok(void **state)
{
    (void)state;
    assert_ptr_equal(&fake_extension1, kaa_extension_get(FAKE_EXTENSION1_ID));
    assert_ptr_equal(&fake_extension2, kaa_extension_get(FAKE_EXTENSION2_ID));
    assert_ptr_equal(&fake_extension3, kaa_extension_get(FAKE_EXTENSION3_ID));
}

static void test_kaa_extension_get_context_wrong_extension(void **state)
{
    (void)state;
    assert_null(kaa_extension_get_context(0));
    assert_null(kaa_extension_get_context(4));
    assert_null(kaa_extension_get_context(100500));
}

static void test_kaa_extension_set_context_wrong_extension(void **state)
{
    (void)state;
    int ctx;
    assert_int_equal(KAA_ERR_NOT_FOUND, kaa_extension_set_context(0, &ctx));
}

static void test_kaa_extension_set_context_ok(void **state)
{
    (void)state;
    int ctx1, ctx2;

    assert_ptr_equal(NULL, kaa_extension_get_context(FAKE_EXTENSION1_ID));
    assert_int_equal(KAA_ERR_NONE, kaa_extension_set_context(FAKE_EXTENSION1_ID, &ctx1));
    assert_ptr_equal(&ctx1, kaa_extension_get_context(FAKE_EXTENSION1_ID));

    assert_ptr_equal(NULL, kaa_extension_get_context(FAKE_EXTENSION2_ID));
    assert_int_equal(KAA_ERR_NONE, kaa_extension_set_context(FAKE_EXTENSION2_ID, &ctx2));
    assert_ptr_equal(&ctx2, kaa_extension_get_context(FAKE_EXTENSION2_ID));

    assert_ptr_equal(&ctx1, kaa_extension_get_context(FAKE_EXTENSION1_ID));
}

static void test_kaa_extension_init_all_ok(void **state)
{
    (void)state;

    int fake_kaa_context;

    int fake_context1;
    int fake_context2;
    int fake_context3;

    expect_string(called, name, "fake_init1");
    expect_value(fake_init1, kaa_context, &fake_kaa_context);
    will_return(fake_init1, &fake_context1);
    will_return(fake_init1, KAA_ERR_NONE);

    expect_string(called, name, "fake_init2");
    expect_value(fake_init2, kaa_context, &fake_kaa_context);
    will_return(fake_init2, &fake_context2);
    will_return(fake_init2, KAA_ERR_NONE);

    expect_string(called, name, "fake_init3");
    expect_value(fake_init3, kaa_context, &fake_kaa_context);
    will_return(fake_init3, &fake_context3);
    will_return(fake_init3, KAA_ERR_NONE);

    assert_int_equal(KAA_ERR_NONE, kaa_extension_init_all((struct kaa_context_s *)&fake_kaa_context));

    assert_ptr_equal(&fake_context1, kaa_extension_get_context(FAKE_EXTENSION1_ID));
    assert_ptr_equal(&fake_context2, kaa_extension_get_context(FAKE_EXTENSION2_ID));
    assert_ptr_equal(&fake_context3, kaa_extension_get_context(FAKE_EXTENSION3_ID));
}

static void test_kaa_extension_init_all_fail(void **state)
{
    (void)state;

    int fake_context1;
    int fake_context2;
    int fake_context3;

    expect_string(called, name, "fake_init1");
    expect_any(fake_init1, kaa_context);
    will_return(fake_init1, &fake_context1);
    will_return(fake_init1, KAA_ERR_NONE);

    expect_string(called, name, "fake_init2");
    expect_any(fake_init2, kaa_context);
    will_return(fake_init2, &fake_context2);
    will_return(fake_init2, KAA_ERR_NONE);

    expect_string(called, name, "fake_init3");
    expect_any(fake_init3, kaa_context);
    will_return(fake_init3, &fake_context3);
    will_return(fake_init3, KAA_ERR_BADPARAM);

    // Cleanup in reverse order
    expect_string(called, name, "fake_deinit2");
    expect_value(fake_deinit2, context, &fake_context2);
    will_return(fake_deinit2, KAA_ERR_NONE);

    expect_string(called, name, "fake_deinit1");
    expect_value(fake_deinit1, context, &fake_context1);
    will_return(fake_deinit1, KAA_ERR_NONE);

    assert_int_equal(KAA_ERR_BADPARAM, kaa_extension_init_all(NULL));
}

static void test_kaa_extension_deinit_all_ok(void **state)
{
    (void)state;
    int fake_context1, fake_context2, fake_context3;

    kaa_extension_set_context(FAKE_EXTENSION1_ID, &fake_context1);
    kaa_extension_set_context(FAKE_EXTENSION2_ID, &fake_context2);
    kaa_extension_set_context(FAKE_EXTENSION3_ID, &fake_context3);

    expect_string(called, name, "fake_deinit3");
    expect_value(fake_deinit3, context, &fake_context3);
    will_return(fake_deinit3, KAA_ERR_NONE);

    expect_string(called, name, "fake_deinit2");
    expect_value(fake_deinit2, context, &fake_context2);
    will_return(fake_deinit2, KAA_ERR_NONE);

    expect_string(called, name, "fake_deinit1");
    expect_value(fake_deinit1, context, &fake_context1);
    will_return(fake_deinit1, KAA_ERR_NONE);

    assert_int_equal(KAA_ERR_NONE, kaa_extension_deinit_all());
}

static void test_kaa_extension_deinit_all_fail(void **state)
{
    (void)state;
    int fake_context1, fake_context2, fake_context3;

    kaa_extension_set_context(FAKE_EXTENSION1_ID, &fake_context1);
    kaa_extension_set_context(FAKE_EXTENSION2_ID, &fake_context2);
    kaa_extension_set_context(FAKE_EXTENSION3_ID, &fake_context3);

    expect_string(called, name, "fake_deinit3");
    expect_value(fake_deinit3, context, &fake_context3);
    will_return(fake_deinit3, KAA_ERR_NONE);

    expect_string(called, name, "fake_deinit2");
    expect_value(fake_deinit2, context, &fake_context2);
    will_return(fake_deinit2, KAA_ERR_NOMEM);

    expect_string(called, name, "fake_deinit1");
    expect_value(fake_deinit1, context, &fake_context1);
    will_return(fake_deinit1, KAA_ERR_NONE);

    assert_int_equal(KAA_ERR_NOMEM, kaa_extension_deinit_all());
}

static void test_kaa_extension_request_serialize_not_found(void **state)
{
    (void)state;

    assert_int_equal(KAA_ERR_NOT_FOUND,
            kaa_extension_request_serialize(BAD_EXTENSION_ID, 0, NULL, NULL, NULL));
}

static void test_kaa_extension_request_serialize_ok(void **state)
{
    (void)state;
    int fake_context1, fake_context2, fake_context3;
    uint8_t buffer;
    size_t size = 0;
    bool sync_needed = true;

    kaa_extension_set_context(FAKE_EXTENSION1_ID, &fake_context1);
    expect_string(called, name, "fake_request_serialize1");
    expect_value(fake_request_serialize1, request_id, 13);
    expect_value(fake_request_serialize1, context, &fake_context1);
    expect_value(fake_request_serialize1, buffer, &buffer);
    will_return(fake_request_serialize1, 143);
    will_return(fake_request_serialize1, true);
    will_return(fake_request_serialize1, KAA_ERR_NONE);
    assert_int_equal(KAA_ERR_NONE,
            kaa_extension_request_serialize(FAKE_EXTENSION1_ID, 13, &buffer, &size, &sync_needed));
    assert_int_equal(143, size);
    assert_true(sync_needed);

    kaa_extension_set_context(FAKE_EXTENSION2_ID, &fake_context2);
    expect_string(called, name, "fake_request_serialize2");
    expect_value(fake_request_serialize2, request_id, 7);
    expect_value(fake_request_serialize2, context, &fake_context2);
    expect_value(fake_request_serialize2, buffer, &buffer);
    will_return(fake_request_serialize2, 512);
    will_return(fake_request_serialize2, false);
    will_return(fake_request_serialize2, KAA_ERR_NONE);
    assert_int_equal(KAA_ERR_NONE,
            kaa_extension_request_serialize(FAKE_EXTENSION2_ID, 7, &buffer, &size, &sync_needed));
    assert_int_equal(512, size);
    assert_false(sync_needed);

    kaa_extension_set_context(FAKE_EXTENSION3_ID, &fake_context3);
    expect_string(called, name, "fake_request_serialize3");
    expect_value(fake_request_serialize3, request_id, 19);
    expect_value(fake_request_serialize3, context, &fake_context3);
    expect_value(fake_request_serialize3, buffer, &buffer);
    will_return(fake_request_serialize3, 0);
    will_return(fake_request_serialize3, true);
    will_return(fake_request_serialize3, KAA_ERR_NONE);
    assert_int_equal(KAA_ERR_NONE,
            kaa_extension_request_serialize(FAKE_EXTENSION3_ID, 19, &buffer, &size, &sync_needed));
    assert_int_equal(0, size);
    assert_true(sync_needed);
}

static void test_kaa_extension_request_serialize_fail(void **state)
{
    (void)state;
    size_t size = 0;
    bool sync_needed = true;
    uint8_t buffer;

    kaa_extension_set_context(FAKE_EXTENSION1_ID, NULL);
    expect_string(called, name, "fake_request_serialize1");
    expect_value(fake_request_serialize1, request_id, 13);
    expect_value(fake_request_serialize1, buffer, &buffer);
    expect_value(fake_request_serialize1, context, NULL);
    will_return(fake_request_serialize1, 0);
    will_return(fake_request_serialize1, true);
    will_return(fake_request_serialize1, KAA_ERR_NOMEM);
    assert_int_equal(KAA_ERR_NOMEM,
            kaa_extension_request_serialize(FAKE_EXTENSION1_ID, 13, &buffer, &size, &sync_needed));
}

static void test_kaa_extension_server_sync_not_found(void **state)
{
    (void)state;

    assert_int_equal(KAA_ERR_NOT_FOUND,
            kaa_extension_server_sync(BAD_EXTENSION_ID, 0, 0, NULL, 0));
}

static void test_kaa_extension_server_sync_ok(void **state)
{
    (void)state;
    int fake_context1, fake_context2, fake_context3;
    uint8_t buffer;

    kaa_extension_set_context(FAKE_EXTENSION1_ID, &fake_context1);
    expect_string(called, name, "fake_server_sync1");
    expect_value(fake_server_sync1, context, &fake_context1);
    expect_value(fake_server_sync1, request_id, 13);
    expect_value(fake_server_sync1, extension_options, 2);
    expect_value(fake_server_sync1, buffer, &buffer);
    expect_value(fake_server_sync1, size, 113);
    will_return(fake_server_sync1, KAA_ERR_NONE);
    assert_int_equal(KAA_ERR_NONE,
            kaa_extension_server_sync(FAKE_EXTENSION1_ID, 13, 2, &buffer, 113));

    kaa_extension_set_context(FAKE_EXTENSION2_ID, &fake_context2);
    expect_string(called, name, "fake_server_sync2");
    expect_value(fake_server_sync2, context, &fake_context2);
    expect_value(fake_server_sync2, request_id, 7);
    expect_value(fake_server_sync2, extension_options, 14);
    expect_value(fake_server_sync2, buffer, &buffer);
    expect_value(fake_server_sync2, size, 9);
    will_return(fake_server_sync2, KAA_ERR_NONE);
    assert_int_equal(KAA_ERR_NONE,
            kaa_extension_server_sync(FAKE_EXTENSION2_ID, 7, 14, &buffer, 9));

    kaa_extension_set_context(FAKE_EXTENSION3_ID, &fake_context3);
    expect_string(called, name, "fake_server_sync3");
    expect_value(fake_server_sync3, context, &fake_context3);
    expect_value(fake_server_sync3, request_id, 0);
    expect_value(fake_server_sync3, extension_options, 0);
    expect_value(fake_server_sync3, buffer, NULL);
    expect_value(fake_server_sync3, size, 0);
    will_return(fake_server_sync3, KAA_ERR_NONE);
    assert_int_equal(KAA_ERR_NONE,
            kaa_extension_server_sync(FAKE_EXTENSION3_ID, 0, 0, NULL, 0));
}

static void test_kaa_extension_server_sync_fail(void **state)
{
    (void)state;

    kaa_extension_set_context(FAKE_EXTENSION1_ID, NULL);
    expect_string(called, name, "fake_server_sync1");
    expect_value(fake_server_sync1, context, NULL);
    expect_value(fake_server_sync1, request_id, 0);
    expect_value(fake_server_sync1, extension_options, 0);
    expect_value(fake_server_sync1, buffer, NULL);
    expect_value(fake_server_sync1, size, 0);
    will_return(fake_server_sync1, KAA_ERR_NOMEM);
    assert_int_equal(KAA_ERR_NOMEM,
            kaa_extension_server_sync(FAKE_EXTENSION1_ID, 0, 0, NULL, 0));
}

int main(void)
{
    // Note that this kaa_extensions is a different instance from
    // kaa_extensions used by kaa_extension.c.
    (void)kaa_extensions;

    const struct CMUnitTest tests[] = {
        cmocka_unit_test(test_kaa_extension_get_wrong_extension),
        cmocka_unit_test(test_kaa_extension_get_ok),

        cmocka_unit_test(test_kaa_extension_get_context_wrong_extension),

        cmocka_unit_test(test_kaa_extension_set_context_wrong_extension),
        cmocka_unit_test(test_kaa_extension_set_context_ok),

        cmocka_unit_test(test_kaa_extension_init_all_ok),
        cmocka_unit_test(test_kaa_extension_init_all_fail),

        cmocka_unit_test(test_kaa_extension_deinit_all_ok),
        cmocka_unit_test(test_kaa_extension_deinit_all_fail),

        cmocka_unit_test(test_kaa_extension_request_serialize_not_found),
        cmocka_unit_test(test_kaa_extension_request_serialize_ok),
        cmocka_unit_test(test_kaa_extension_request_serialize_fail),

        cmocka_unit_test(test_kaa_extension_server_sync_not_found),
        cmocka_unit_test(test_kaa_extension_server_sync_ok),
        cmocka_unit_test(test_kaa_extension_server_sync_fail),
    };

    return cmocka_run_group_tests(tests, NULL, NULL);
}

static void called(const char *name)
{
    check_expected(name);
}

static kaa_error_t fake_init1(struct kaa_context_s *kaa_context, void **context)
{
    called("fake_init1");
    check_expected_ptr(kaa_context);
    *context = mock_ptr_type(void *);
    return mock_type(kaa_error_t);
}

static kaa_error_t fake_init2(struct kaa_context_s *kaa_context, void **context)
{
    called("fake_init2");
    check_expected_ptr(kaa_context);
    *context = mock_ptr_type(void *);
    return mock_type(kaa_error_t);
}

static kaa_error_t fake_init3(struct kaa_context_s *kaa_context, void **context)
{
    called("fake_init3");
    check_expected_ptr(kaa_context);
    *context = mock_ptr_type(void *);
    return mock_type(kaa_error_t);
}

static kaa_error_t fake_deinit1(void *context)
{
    called("fake_deinit1");
    check_expected_ptr(context);
    return mock_type(kaa_error_t);
}

static kaa_error_t fake_deinit2(void *context)
{
    called("fake_deinit2");
    check_expected_ptr(context);
    return mock_type(kaa_error_t);
}

static kaa_error_t fake_deinit3(void *context)
{
    called("fake_deinit3");
    check_expected_ptr(context);
    return mock_type(kaa_error_t);
}

static kaa_error_t fake_request_serialize1(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *sync_needed)
{
    called("fake_request_serialize1");
    check_expected(request_id);
    check_expected_ptr(context);
    check_expected_ptr(buffer);
    *size = mock_type(size_t);
    *sync_needed = mock_type(bool);
    return mock_type(kaa_error_t);
}

static kaa_error_t fake_request_serialize2(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *sync_needed)
{
    called("fake_request_serialize2");
    check_expected(request_id);
    check_expected_ptr(context);
    check_expected_ptr(buffer);
    *size = mock_type(size_t);
    *sync_needed = mock_type(bool);
    return mock_type(kaa_error_t);
}

static kaa_error_t fake_request_serialize3(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *sync_needed)
{
    called("fake_request_serialize3");
    check_expected(request_id);
    check_expected_ptr(context);
    check_expected_ptr(buffer);
    *size = mock_type(size_t);
    *sync_needed = mock_type(bool);
    return mock_type(kaa_error_t);
}

static kaa_error_t fake_server_sync1(void *context, uint32_t request_id,
            uint16_t extension_options, const uint8_t *buffer, size_t size)
{
    called("fake_server_sync1");
    check_expected_ptr(context);
    check_expected(request_id);
    check_expected(extension_options);
    check_expected_ptr(buffer);
    check_expected(size);
    return mock_type(kaa_error_t);
}

static kaa_error_t fake_server_sync2(void *context, uint32_t request_id,
            uint16_t extension_options, const uint8_t *buffer, size_t size)
{
    called("fake_server_sync2");
    check_expected_ptr(context);
    check_expected(request_id);
    check_expected(extension_options);
    check_expected_ptr(buffer);
    check_expected(size);
    return mock_type(kaa_error_t);
}

static kaa_error_t fake_server_sync3(void *context, uint32_t request_id,
            uint16_t extension_options, const uint8_t *buffer, size_t size)
{
    called("fake_server_sync3");
    check_expected_ptr(context);
    check_expected(request_id);
    check_expected(extension_options);
    check_expected_ptr(buffer);
    check_expected(size);
    return mock_type(kaa_error_t);
}

const struct kaa_extension fake_extension1 = {
    .id = FAKE_EXTENSION1_ID,
    .init = fake_init1,
    .deinit = fake_deinit1,
    .request_serialize = fake_request_serialize1,
    .server_sync = fake_server_sync1,
};

const struct kaa_extension fake_extension2 = {
    .id = FAKE_EXTENSION2_ID,
    .init = fake_init2,
    .deinit = fake_deinit2,
    .request_serialize = fake_request_serialize2,
    .server_sync = fake_server_sync2,
};

const struct kaa_extension fake_extension3 = {
    .id = FAKE_EXTENSION3_ID,
    .init = fake_init3,
    .deinit = fake_deinit3,
    .request_serialize = fake_request_serialize3,
    .server_sync = fake_server_sync3,
};

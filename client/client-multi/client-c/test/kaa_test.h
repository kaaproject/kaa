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

#ifndef KAA_TEST_H_
#define KAA_TEST_H_

#include <stdarg.h>
#include <stddef.h>
#include <setjmp.h>
#include <cmocka.h>

/* These macroses are temporary solution, until all tests will use
 * mocka library.
 */

#define ASSERT_NOT_NULL(P)      assert_ptr_not_equal(P, NULL)
#define ASSERT_NULL(P)          assert_ptr_equal(P, NULL)
#define ASSERT_EQUAL(L, R)      assert_int_equal(L, R)
#define ASSERT_NOT_EQUAL(L, R)  assert_int_not_equal(L, R)

#define ASSERT_TRUE(Exp)        assert_true(Exp)
#define ASSERT_FALSE(Exp)       assert_false(Exp)

/* Adapter Kaa test case -> cmocka test case */

#define KAA_BEGIN_TEST_SUITE(SUITE_NAME, INIT_FN, CLEANUP_FN)  \
    static int setup_fn(void **state) \
    { \
        (void)state; \
        int (*fn)(void) = INIT_FN; \
        if (fn) return fn(); \
        return 0; \
    } \
    \
    static int teardown_fn(void **state) \
    { \
        (void)state; \
        int (*fn)(void) = CLEANUP_FN; \
        if (fn) return fn(); \
        return 0; \
    } \
    int main(void) \
    { \
        const char suite_name[] = #SUITE_NAME; \
        const struct CMUnitTest tests[] = { \

#define KAA_TEST_CASE(TEST_NAME, TEST_FN) \
            cmocka_unit_test(TEST_FN),

/* Helper macro to control setup and teardown process per each test in group.
 * Must be placed in the exact suite.
 */
#define KAA_RUN_TEST(GROUP, NAME) \
            cmocka_unit_test_setup_teardown(GROUP##_##NAME##_test, GROUP##_group_setup, GROUP##_group_teardown),

#define KAA_END_TEST_SUITE \
        }; \
        return cmocka_run_group_tests_name(suite_name, tests, setup_fn, teardown_fn); \
     }

/* Defines test case in the given group */
#define KAA_TEST_CASE_EX(GROUP, NAME) \
    void GROUP##_##NAME##_test(void **state)

/* Defines a setup process for given group.
 * It runs before each test to make sure sytem is in predictable state
 */
#define KAA_GROUP_SETUP(GROUP) \
    int GROUP##_group_setup(void **state)

/* Defines a teardown process for given group
 * Reverts any changes made by setup routine and makes sure no side effects
 * will stay after test
 */
#define KAA_GROUP_TEARDOWN(GROUP) \
    int GROUP##_group_teardown(void **state)


#define KAA_SUITE_MAIN(SUITE_NAME, INIT_FN, CLEANUP_FN, ...) \
    KAA_BEGIN_TEST_SUITE(SUITE_NAME, INIT_FN, CLEANUP_FN) \
    __VA_ARGS__ \
    KAA_END_TEST_SUITE \

#endif /* KAA_TEST_H_ */

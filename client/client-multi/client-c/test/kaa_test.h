/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#ifndef KAA_TEST_H_
#define KAA_TEST_H_

#if defined(KAA_TEST_CUNIT_ENABLED)

#include <CUnit/CUnit.h>
#include <CUnit/Automated.h>

#define ASSERT_NOT_NULL(P)      CU_ASSERT_PTR_NOT_NULL_FATAL(P)
#define ASSERT_NULL(P)          CU_ASSERT_PTR_NULL_FATAL(P)
#define ASSERT_EQUAL(L, R)      CU_ASSERT_EQUAL_FATAL(L, R)
#define ASSERT_NOT_EQUAL(L, R)  CU_ASSERT_NOT_EQUAL_FATAL(L, R)

#define ASSERT_TRUE(Exp)        CU_ASSERT_TRUE_FATAL(Exp)
#define ASSERT_FALSE(Exp)       CU_ASSERT_FALSE_FATAL(Exp)

#define KAA_BEGIN_TEST_SUITE(SUITE_NAME, INIT_FN, CLEANUP_FN)  \
    int main(int argc, char ** argv) \
    { \
        CU_initialize_registry(); \
        CU_set_output_filename(#SUITE_NAME); \
        CU_pSuite testSuite = CU_add_suite(#SUITE_NAME, INIT_FN, CLEANUP_FN); \

#define KAA_TEST_CASE(TEST_NAME, TEST_FN) \
        CU_add_test(testSuite, #TEST_NAME, &TEST_FN); \

#define KAA_RUN_TESTS \
        CU_automated_run_tests(); \

/* Helper macro to control setup and teardown process per each test in group.
 * Must be placed in the exact suite.
 */
#define KAA_RUN_TEST(GROUP, NAME) \
        KAA_TEST_CASE(GROUP##_##NAME##_setup, GROUP##_group_setup); \
        KAA_TEST_CASE(GROUP##_##NAME##_test, GROUP##_##NAME##_test) \
        KAA_TEST_CASE(GROUP##_##NAME##_teardown, GROUP##_group_teardown)

#define KAA_END_TEST_SUITE \
        unsigned int failed_tests = CU_get_number_of_failure_records(); \
        CU_cleanup_registry(); \
        return failed_tests; \
    }
#else

#pragma message "Unit tests will not generate xml reports. Install CUnit library (apt-get install libcunit or install from sources http://cunit.sourceforge.net/index.html)."

#include <assert.h>
#define __ASSERT(EXPRESSION)      assert(EXPRESSION)
#define __KAA_EXPRESSION_EQUAL(P1, P2)      (P1 == P2)
#define __KAA_EXPRESSION_GT(P1, P2)         (P1 > P2)
#define __KAA_EXPRESSION_GE(P1, P2)         (P1 >= P2)
#define __KAA_EXPRESSION_LT(P1, P2)         (P1 < P2)
#define __KAA_EXPRESSION_LE(P1, P2)         (P1 <= P2)
#define ASSERT_NOT_NULL(P)      __ASSERT(!__KAA_EXPRESSION_EQUAL(P, NULL))
#define ASSERT_NULL(P)          __ASSERT(__KAA_EXPRESSION_EQUAL(P, NULL))
#define ASSERT_EQUAL(L, R)      __ASSERT(__KAA_EXPRESSION_EQUAL(L, R))
#define ASSERT_NOT_EQUAL(L, R)  __ASSERT(!__KAA_EXPRESSION_EQUAL(L, R))

#define ASSERT_TRUE(Exp)        __ASSERT(Exp)
#define ASSERT_FALSE(Exp)       __ASSERT(!(Exp))

typedef int (*init_fn)(void);
typedef int (*cleanup_fn)(void);

#define KAA_BEGIN_TEST_SUITE(SUITE_NAME, INIT_FN, CLEANUP_FN)  \
    int main(int argc, char ** argv) \
    { \
        int init_ret_code = 0; \
        int cleanup_ret_code = 0; \
        init_fn init = INIT_FN; \
        cleanup_fn cleanup = CLEANUP_FN; \
        if (init != NULL) { \
            init_ret_code = init(); \
        }

#define KAA_TEST_CASE(TEST_NAME, TEST_FN) \
        if (!init_ret_code)  \
            TEST_FN();

/* Helper macro to control setup and teardown process per each test in group.
 * Must be placed in the exact suite.
 */
#define KAA_RUN_TEST(GROUP, NAME) \
    do { \
        if (!init_ret_code) { \
            GROUP##_group_setup(); \
            GROUP##_##NAME##_test(); \
            GROUP##_group_teardown(); \
        } \
    } while (0)

#define KAA_RUN_TESTS

#define KAA_END_TEST_SUITE \
        if (cleanup != NULL) { \
            cleanup_ret_code = cleanup(); \
        } \
        return (init_ret_code || cleanup_ret_code) ? -1 : 0; \
    }
#endif


/* Bunch of macroses that required execute setup() (initialization)
 * and teardown() (cleanup) procedures per each test in so called "group".
 *
 * Group is a set of tests with common setup() and teardown() routines.
 * You may have as many groups as you want inside test application.
 * This contrasts with suite, which can be only one per each executable.
 *
 * NOTE: THIS IS INTERMIDIATE SOLUTION THAT WILL BE USED PRIOR TO INTEGRATION
 * OF APPROPRIATE TEST FRAMEWORK WHICH SUPPORTS SUCH FEATURES.
 */

/* Defines test case in the given group */
#define KAA_TEST_CASE_EX(GROUP, NAME) \
    void GROUP##_##NAME##_test(void)

/* Defines a setup process for given group.
 * It runs before each test to make sure sytem is in predictable state
 */
#define KAA_GROUP_SETUP(GROUP) \
    void GROUP##_group_setup()

/* Defines a teardown process for given group
 * Reverts any changes made by setup routine and makes sure no side effects
 * will stay after test
 */
#define KAA_GROUP_TEARDOWN(GROUP) \
    void GROUP##_group_teardown()


#define KAA_SUITE_MAIN(SUITE_NAME, INIT_FN, CLEANUP_FN, ...) \
    KAA_BEGIN_TEST_SUITE(SUITE_NAME, INIT_FN, CLEANUP_FN) \
    __VA_ARGS__ \
    KAA_RUN_TESTS \
    KAA_END_TEST_SUITE \

#endif /* KAA_TEST_H_ */

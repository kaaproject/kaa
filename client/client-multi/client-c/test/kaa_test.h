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

#define KAA_END_TEST_SUITE \
        unsigned int failed_tests = CU_get_number_of_failures(); \
        CU_cleanup_registry(); \
        return failed_tests; \
    }
#else

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
        init_fn init = INIT_FN; \
        cleanup_fn cleanup = CLEANUP_FN; \
        if (init != NULL) { \
            init(); \
        } \

#define KAA_TEST_CASE(TEST_NAME, TEST_FN) \
        TEST_FN();

#define KAA_RUN_TESTS

#define KAA_END_TEST_SUITE \
        if (cleanup != NULL) { \
            cleanup(); \
        } \
        return 0; \
    }

#endif


#define KAA_SUITE_MAIN(SUITE_NAME, INIT_FN, CLEANUP_FN, ...) \
    KAA_BEGIN_TEST_SUITE(SUITE_NAME, INIT_FN, CLEANUP_FN) \
    __VA_ARGS__ \
    KAA_RUN_TESTS \
    KAA_END_TEST_SUITE \


#endif /* KAA_TEST_H_ */

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


#endif /* KAA_TEST_H_ */

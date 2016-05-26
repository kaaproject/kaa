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

#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <stdint.h>
#include <time.h>

#include "kaa_test.h"
#include "collections/kaa_list.h"
#include "utilities/kaa_log.h"
#include "utilities/kaa_mem.h"

typedef struct {
    uint64_t id;
} test_list_node_t;

static bool test_kaa_list_predicate(void *node_1, void *node_2)
{
    test_list_node_t *wrapper_1 = (test_list_node_t *)node_1;
    test_list_node_t *wrapper_2 = (test_list_node_t *)node_2;
    return wrapper_1->id < wrapper_2->id;
}

static uint64_t test_kaa_list_hash(void *node)
{
    test_list_node_t *wrapper = (test_list_node_t *)node;
    return wrapper->id;
}


static void test_list_create()
{
    kaa_list_t *list = kaa_list_create();
    ASSERT_NOT_NULL(list);

    ASSERT_EQUAL(kaa_list_get_size(list), 0);
    ASSERT_NULL(kaa_list_begin(list));
    ASSERT_NULL(kaa_list_back(list));

    kaa_list_destroy(list, NULL);
}

static void test_list_push_front()
{
    kaa_list_t *list = kaa_list_create();
    ASSERT_NOT_NULL(list);

    int32_t *number;
    size_t node_number = 2;
    for (size_t i = 0; i < node_number; ++i) {
        number = KAA_MALLOC(sizeof(int32_t *));
        ASSERT_NOT_NULL(number);
        *number = rand();
        kaa_list_push_front(list, number);
    }

    ASSERT_EQUAL(kaa_list_get_size(list), node_number);

    ASSERT_NOT_NULL(kaa_list_begin(list));
    ASSERT_NULL(kaa_list_prev(kaa_list_begin(list)));

    ASSERT_EQUAL((*(int32_t *)kaa_list_get_data(kaa_list_begin(list))), *number);

    kaa_list_destroy(list, NULL);
}

static void test_list_push_back()
{
    kaa_list_t *list = kaa_list_create();
    ASSERT_NOT_NULL(list);

    int32_t *number;
    int node_number = 2;
    for (int i = 0; i < node_number; ++i) {
        number = KAA_MALLOC(sizeof(int32_t *));
        ASSERT_NOT_NULL(number);
        *number = rand();
        kaa_list_push_back(list, number);
    }

    ASSERT_EQUAL(kaa_list_get_size(list), node_number);
    ASSERT_EQUAL((*(int32_t *)kaa_list_get_data(kaa_list_back(list))), *number);

    kaa_list_destroy(list, NULL);
}

static void test_list_sort()
{
    kaa_list_t *list = kaa_list_create();
    ASSERT_NOT_NULL(list);

    uint64_t node_number = 100;
    for (uint64_t i = 0; i < node_number; ++i) {
        test_list_node_t *node = KAA_MALLOC(sizeof(test_list_node_t));
        ASSERT_NOT_NULL(node);
        node->id = (uint64_t) rand();
        kaa_list_push_back(list, node);
    }

    ASSERT_EQUAL(kaa_list_get_size(list), node_number);

    kaa_list_sort(list,&test_kaa_list_predicate);
    kaa_list_node_t *it,*next;
    it = kaa_list_begin(list);
    next = kaa_list_next(it);
    while (it && next) {
        ASSERT_TRUE(((test_list_node_t*)kaa_list_get_data(it))->id <=
                ((test_list_node_t*)kaa_list_get_data(next))->id);
        it = next;
        next = kaa_list_next(it);
    }
    kaa_list_destroy(list, NULL);
}

static void test_list_empty_sort()
{
    /* Purpose of this test is to show that no crash occur if
     * list was initially empty.
     */

    kaa_list_t *list = kaa_list_create();
    ASSERT_NOT_NULL(list);
    kaa_list_sort(list, &test_kaa_list_predicate);
    kaa_list_destroy(list, NULL);
}


static void test_list_hash()
{
    kaa_list_t *list = kaa_list_create();
    ASSERT_NOT_NULL(list);

    uint64_t node_number = 100;
    for (uint64_t i = 0; i < node_number; ++i) {
        test_list_node_t *node = KAA_MALLOC(sizeof(test_list_node_t));
        ASSERT_NOT_NULL(node);
        node->id = (uint64_t) node_number - i;
        kaa_list_push_back(list, node);
    }

    ASSERT_EQUAL(kaa_list_get_size(list), node_number);

    kaa_list_sort(list,&test_kaa_list_predicate);

    ASSERT_EQUAL(kaa_list_hash(list,&test_kaa_list_hash),-974344717);

    kaa_list_destroy(list, NULL);
}

static void test_process_data(int32_t *value, int32_t *new_value)
{
    *value = *new_value;
}

static void test_list_for_each()
{
    kaa_list_t *list = kaa_list_create();
    ASSERT_NOT_NULL(list);

    int node_number = 4;
    for (int i = 0; i < node_number; ++i) {
        int32_t *number1_ptr = KAA_MALLOC(sizeof(int32_t *));
        ASSERT_NOT_NULL(number1_ptr);
        *number1_ptr = rand();
        kaa_list_push_back(list, number1_ptr);
    }

    int number2 = rand();
    kaa_list_for_each(kaa_list_begin(list), kaa_list_back(list), (process_data)&test_process_data, &number2);

    kaa_list_node_t *it = kaa_list_begin(list);
    while (it) {
        ASSERT_EQUAL(*(int32_t *)kaa_list_get_data(kaa_list_begin(list)), number2);
        it = kaa_list_next(it);
    }

    kaa_list_destroy(list, NULL);
}

static int test_init(void)
{
    srand(time(NULL));
    return 0;
}

static int test_deinit(void)
{
    return 0;
}

KAA_SUITE_MAIN(List, test_init, test_deinit,
        KAA_TEST_CASE(list_create, test_list_create)
        KAA_TEST_CASE(list_push_front, test_list_push_front)
        KAA_TEST_CASE(list_push_back, test_list_push_back)
        KAA_TEST_CASE(list_for_each, test_list_for_each)
        KAA_TEST_CASE(list_sort, test_list_sort)
        KAA_TEST_CASE(list_sort, test_list_empty_sort)
        KAA_TEST_CASE(list_hash, test_list_hash)
)

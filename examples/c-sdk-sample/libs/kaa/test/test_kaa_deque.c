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

#include "collections/kaa_deque.h"
#include <string.h>
#include "../../kaa/test/kaa_test.h"

void test_kaa_deque_create()
{
    kaa_error_t error_code = kaa_deque_create(NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);

    kaa_deque_t *deque = NULL;
    error_code = kaa_deque_create(&deque);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(deque);

    kaa_deque_destroy(deque, NULL);
}

static void test_kaa_deque_destroy_stub(void *data)
{
    ASSERT_NOT_NULL(data);
}
void test_kaa_deque_destroy()
{
    kaa_deque_t *deque = NULL;
    kaa_error_t error_code = kaa_deque_create(&deque);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(deque);

    kaa_deque_push_back_data(deque, "data");

    kaa_deque_destroy(deque, &test_kaa_deque_destroy_stub);
}

void test_kaa_deque_first_last()
{
    kaa_error_t error_code = KAA_ERR_NONE;

    error_code = kaa_deque_first(NULL, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    error_code = kaa_deque_last(NULL, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);

    kaa_deque_t *deque = NULL;
    error_code = kaa_deque_create(&deque);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(deque);

    error_code = kaa_deque_first(deque, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    error_code = kaa_deque_last(deque, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);

    kaa_deque_iterator_t *it = NULL;
    error_code = kaa_deque_first(NULL, &it);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    error_code = kaa_deque_last(NULL, &it);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);

    error_code = kaa_deque_first(deque, &it);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NULL(it);
    error_code = kaa_deque_last(deque, &it);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NULL(it);

    kaa_deque_push_back_data(deque, "data");
    ASSERT_EQUAL(kaa_deque_size(deque), 1);

    kaa_deque_iterator_t *it1 = NULL;
    error_code = kaa_deque_first(deque, &it1);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(it1);
    kaa_deque_iterator_t *it2 = NULL;
    error_code = kaa_deque_last(deque, &it2);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(it2);
    ASSERT_EQUAL(it1, it2);

    kaa_deque_push_back_data(deque, "data");
    ASSERT_EQUAL(kaa_deque_size(deque), 2);

    error_code = kaa_deque_first(deque, &it1);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(it1);

    error_code = kaa_deque_last(deque, &it2);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(it2);
    ASSERT_NOT_EQUAL(it1, it2);

    kaa_deque_destroy(deque, &test_kaa_deque_destroy_stub);
}

void test_kaa_deque_pop_front_back()
{
    kaa_error_t error_code = KAA_ERR_NONE;
    kaa_deque_t *deque = NULL;
    error_code = kaa_deque_create(&deque);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(deque);

    error_code = kaa_deque_pop_front(NULL,NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    error_code = kaa_deque_pop_back(NULL,NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);

    error_code = kaa_deque_pop_front(deque,NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    error_code = kaa_deque_pop_back(deque,NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);

    kaa_deque_iterator_t *it_front = NULL;
    kaa_deque_iterator_t *it_back  = NULL;

    error_code = kaa_deque_pop_front(NULL, &it_front);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    error_code = kaa_deque_pop_back(NULL, &it_back);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);

    error_code = kaa_deque_pop_front(deque, &it_front);
    ASSERT_EQUAL(error_code, KAA_ERR_NOT_FOUND);
    error_code = kaa_deque_pop_front(deque, &it_back);
    ASSERT_EQUAL(error_code, KAA_ERR_NOT_FOUND);

    kaa_deque_push_back_data(deque, "data1");
    ASSERT_EQUAL(kaa_deque_size(deque), 1);

    error_code = kaa_deque_pop_front(deque, &it_front);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(it_front);
    ASSERT_EQUAL(kaa_deque_size(deque), 0);

    error_code = kaa_deque_pop_back(deque, &it_back);
    ASSERT_EQUAL(error_code, KAA_ERR_NOT_FOUND);

    kaa_deque_push_back_iterator(deque, it_front);
    it_front = NULL;
    ASSERT_EQUAL(kaa_deque_size(deque), 1);

    error_code = kaa_deque_pop_back(deque, &it_back);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(it_back);
    ASSERT_EQUAL(kaa_deque_size(deque), 0);
    error_code = kaa_deque_pop_front(deque, &it_front);
    ASSERT_EQUAL(error_code, KAA_ERR_NOT_FOUND);

    kaa_deque_push_back_iterator(deque, it_back);
    it_back = NULL;
    ASSERT_EQUAL(kaa_deque_size(deque), 1);

    kaa_deque_push_back_data(deque, "data2");
    ASSERT_EQUAL(kaa_deque_size(deque), 2);

    error_code = kaa_deque_pop_back(deque, &it_back);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(it_back);
    ASSERT_EQUAL(kaa_deque_size(deque), 1);

    error_code = kaa_deque_pop_front(deque, &it_front);

    ASSERT_EQUAL(kaa_deque_size(deque), 0);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(it_back);
    ASSERT_NOT_EQUAL(it_front, it_back);

    kaa_deque_push_front_iterator(deque, it_front);
    kaa_deque_push_back_iterator(deque, it_back);

    kaa_deque_destroy(deque, &test_kaa_deque_destroy_stub);
}

void test_kaa_deque_push_back()
{
    kaa_error_t error_code = KAA_ERR_NONE;
    kaa_deque_t *deque = NULL;
    error_code = kaa_deque_create(&deque);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(deque);

    error_code = kaa_deque_push_back_data(NULL, NULL);

    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);

    char *data1 = "data1";
    char *data2 = "data2";

    error_code = kaa_deque_push_back_data(deque, data1);

    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(kaa_deque_size(deque), 1);

    error_code = kaa_deque_push_back_data(deque, data2);

    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(kaa_deque_size(deque), 2);

    error_code = kaa_deque_push_back_iterator(NULL, NULL);

    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    error_code = kaa_deque_push_back_iterator(deque, NULL);

    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    ASSERT_EQUAL(kaa_deque_size(deque), 2);

    kaa_deque_iterator_t * it1 = NULL;
    kaa_deque_iterator_t * it2 = NULL;
    kaa_deque_pop_front(deque, &it1);

    ASSERT_NOT_NULL(it1);
    ASSERT_EQUAL(strcmp((char *)kaa_deque_iterator_get_data(it1), data1), 0);

    kaa_deque_pop_back(deque, &it2);

    ASSERT_NOT_NULL(it2);
    ASSERT_EQUAL(strcmp((char *)kaa_deque_iterator_get_data(it2), data2), 0);

    ASSERT_EQUAL(kaa_deque_size(deque), 0);

    kaa_deque_iterator_t * it3 = NULL;
    error_code = kaa_deque_first(deque, &it3);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NULL(it3);

    error_code = kaa_deque_push_back_iterator(deque, it1);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(kaa_deque_size(deque), 1);

    kaa_deque_iterator_t *it1_copy = NULL;
    error_code = kaa_deque_first(deque, &it1_copy);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(it1_copy);
    ASSERT_EQUAL(it1, it1_copy);

    error_code = kaa_deque_push_back_iterator(deque, it2);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(kaa_deque_size(deque), 2);

    error_code = kaa_deque_first(deque, &it1_copy);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(it1, it1_copy);

    kaa_deque_iterator_t *it2_copy = NULL;
    error_code = kaa_deque_last(deque, &it2_copy);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(it2, it2_copy);

    kaa_deque_destroy(deque, NULL);
}

void test_kaa_deque_push_front()
{
    kaa_error_t error_code = KAA_ERR_NONE;
    kaa_deque_t *deque = NULL;
    error_code = kaa_deque_create(&deque);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(deque);

    error_code = kaa_deque_push_front_data(NULL, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);

    error_code = kaa_deque_push_front_data(deque, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(kaa_deque_size(deque), 1);

    error_code = kaa_deque_push_front_data(deque, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(kaa_deque_size(deque), 2);

    error_code = kaa_deque_push_front_iterator(NULL, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    error_code = kaa_deque_push_front_iterator(deque, NULL);
    ASSERT_EQUAL(error_code, KAA_ERR_BADPARAM);
    ASSERT_EQUAL(kaa_deque_size(deque), 2);

    kaa_deque_iterator_t * it1 = NULL;
    kaa_deque_iterator_t * it2 = NULL;
    kaa_deque_pop_front(deque, &it1);
    kaa_deque_pop_back(deque, &it2);
    ASSERT_EQUAL(kaa_deque_size(deque), 0);

    error_code = kaa_deque_push_front_iterator(deque, it1);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(kaa_deque_size(deque), 1);

    kaa_deque_iterator_t *it1_copy = NULL;
    error_code = kaa_deque_first(deque, &it1_copy);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(it1, it1_copy);

    error_code = kaa_deque_push_front_iterator(deque, it2);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(kaa_deque_size(deque), 2);

    error_code = kaa_deque_last(deque, &it1_copy);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(it1, it1_copy);

    kaa_deque_iterator_t *it2_copy = NULL;
    error_code = kaa_deque_first(deque, &it2_copy);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_EQUAL(it2, it2_copy);

    kaa_deque_destroy(deque, NULL);
}

void test_kaa_deque_size()
{
    kaa_error_t error_code = KAA_ERR_NONE;

    ASSERT_EQUAL(kaa_deque_size(NULL), -1);

    kaa_deque_t *deque = NULL;
    error_code = kaa_deque_create(&deque);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(deque);
    ASSERT_EQUAL(kaa_deque_size(deque), 0);

    kaa_deque_push_back_data(deque, NULL);
    ASSERT_EQUAL(kaa_deque_size(deque), 1);
    kaa_deque_push_back_data(deque, NULL);
    ASSERT_EQUAL(kaa_deque_size(deque), 2);

    kaa_deque_destroy(deque, NULL);
}

void test_kaa_deque_merge_move()
{
    kaa_error_t error_code = KAA_ERR_NONE;

    ASSERT_NULL(kaa_deque_merge_move(NULL, NULL));

    kaa_deque_t *deque1 = NULL;
    error_code = kaa_deque_create(&deque1);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(deque1);

    ASSERT_EQUAL(kaa_deque_merge_move(deque1, NULL), deque1);
    ASSERT_EQUAL(kaa_deque_merge_move(NULL, deque1), deque1);

    kaa_deque_t *deque2 = NULL;
    error_code = kaa_deque_create(&deque2);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(deque2);

    kaa_deque_push_back_data(deque1, "data10");
    kaa_deque_push_back_data(deque1, "data11");

    kaa_deque_t * test_deque = kaa_deque_merge_move(deque1, deque2);

    ASSERT_EQUAL(kaa_deque_size(test_deque), 2);
    ASSERT_EQUAL(kaa_deque_size(deque1), 2);
    ASSERT_EQUAL(kaa_deque_size(deque2), 0);

    kaa_deque_push_back_data(deque2, "data20");

    test_deque = kaa_deque_merge_move(deque1, deque2);

    ASSERT_EQUAL(kaa_deque_size(test_deque), 3);
    ASSERT_EQUAL(kaa_deque_size(deque1), 3);
    ASSERT_EQUAL(kaa_deque_size(deque2), 0);

    test_deque = kaa_deque_merge_move(deque2, deque1);
    ASSERT_EQUAL(test_deque, deque2);
    ASSERT_EQUAL(kaa_deque_size(deque2), 3);
    ASSERT_EQUAL(kaa_deque_size(deque1), 0);

    kaa_deque_destroy(deque1, NULL);
    kaa_deque_destroy(deque2, NULL);
}

void test_kaa_deque_iterator_api()
{
    kaa_deque_iterator_t *it = NULL;

    ASSERT_NULL(kaa_deque_iterator_get_data(it));
    ASSERT_NULL(kaa_deque_iterator_previous(it));
    ASSERT_NULL(kaa_deque_iterator_next(it));

    kaa_error_t error_code = KAA_ERR_NONE;

    kaa_deque_t *deque = NULL;
    error_code = kaa_deque_create(&deque);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(deque);

    kaa_deque_push_back_data(deque, "data");
    kaa_deque_first(deque, &it);

    ASSERT_NOT_NULL(kaa_deque_iterator_get_data(it));
    ASSERT_NULL(kaa_deque_iterator_previous(it));
    ASSERT_NULL(kaa_deque_iterator_next(it));

    kaa_deque_push_back_data(deque, "data1");
    kaa_deque_first(deque, &it);

    ASSERT_NOT_NULL(kaa_deque_iterator_get_data(it));
    ASSERT_NULL(kaa_deque_iterator_previous(it));
    ASSERT_NOT_NULL(kaa_deque_iterator_next(it));

    kaa_deque_last(deque, &it);
    ASSERT_NOT_NULL(kaa_deque_iterator_get_data(it));
    ASSERT_NOT_NULL(kaa_deque_iterator_previous(it));
    ASSERT_NULL(kaa_deque_iterator_next(it));
    kaa_deque_destroy(deque, NULL);
}

KAA_SUITE_MAIN(Deque, NULL, NULL
        ,
        KAA_TEST_CASE(create, test_kaa_deque_create)
        KAA_TEST_CASE(destroy, test_kaa_deque_destroy)
        KAA_TEST_CASE(first_last, test_kaa_deque_first_last)
        KAA_TEST_CASE(pop_front_back, test_kaa_deque_pop_front_back)
        KAA_TEST_CASE(push_back, test_kaa_deque_push_back)
        KAA_TEST_CASE(push_front, test_kaa_deque_push_front)
        KAA_TEST_CASE(size, test_kaa_deque_size)
        KAA_TEST_CASE(merge_move, test_kaa_deque_merge_move)
        KAA_TEST_CASE(iterator_api, test_kaa_deque_iterator_api)

)

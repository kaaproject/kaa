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

#include "kaa_event.h"
#ifndef KAA_DISABLE_FEATURE_EVENTS

#include <string.h>

#include "kaa_test.h"
#include "utilities/kaa_log.h"
#include "utilities/kaa_mem.h"
#include "gen/kaa_endpoint_gen.h"
#include "kaa_status.h"
#include "kaa_channel_manager.h"


extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern void        kaa_status_destroy(kaa_status_t *self);

extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_logger_t *logger);
extern void        kaa_channel_manager_destroy(kaa_channel_manager_t *self);

extern kaa_error_t kaa_event_manager_create(kaa_event_manager_t **event_manager_p, kaa_status_t *status
        , kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
extern void        kaa_event_manager_destroy(kaa_event_manager_t *self);

extern kaa_error_t kaa_event_compile_request(kaa_event_manager_t *self
        , kaa_event_sync_request_t** request_p, size_t requestId);

extern kaa_error_t kaa_event_handle_sync(kaa_event_manager_t *self
        , size_t request_id, kaa_event_sequence_number_response_t *event_sn_response, kaa_list_t *events);



static int global_events_counter = 0;
static int specific_events_counter = 0;

static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_channel_manager_t *channel_manager = NULL;
static kaa_event_manager_t *event_manager = NULL;



static void kaa_event_destroy(void* data)
{
    if (data) {
        kaa_event_t *record = (kaa_event_t*)data;
        record->destroy(record);
    }
}



void test_kaa_create_event_manager()
{
    KAA_TRACE_IN(logger);

    kaa_status_t* status = NULL;
    kaa_status_create(&status);
    kaa_event_manager_t *event_manager = NULL;
    kaa_error_t err_code = kaa_event_manager_create(&event_manager, status, NULL, logger);
    ASSERT_EQUAL(err_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(event_manager);
    kaa_event_manager_destroy(event_manager);

    KAA_FREE(status);
}



void test_kaa_event_compile_request()
{
    KAA_TRACE_IN(logger);

    kaa_event_sync_request_t* sync_request1 = NULL;
    kaa_event_compile_request(event_manager, &sync_request1, 100499);

    sync_request1->destroy(sync_request1);

    kaa_event_sequence_number_response_t seq_n_resp;
    seq_n_resp.seq_num = 0;

    kaa_event_handle_sync(event_manager, 100499, &seq_n_resp, NULL);
    kaa_error_t err_code = KAA_ERR_NONE;

    err_code = kaa_event_compile_request(event_manager, &sync_request1, 100500);
    ASSERT_EQUAL(err_code ,KAA_ERR_NONE);
    ASSERT_NOT_NULL(sync_request1);
    ASSERT_EQUAL(sync_request1->event_listeners_requests->type, KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_1);
    ASSERT_EQUAL(sync_request1->events->type, KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_1);
    sync_request1->destroy(sync_request1);

    kaa_add_event(event_manager, "fqn", "data", 4, "target", 6);
    kaa_event_sync_request_t* sync_request2 = NULL;
    err_code = kaa_event_compile_request(event_manager, &sync_request2, 100501);
    ASSERT_EQUAL(err_code ,KAA_ERR_NONE);
    ASSERT_NOT_NULL(sync_request2);
    ASSERT_EQUAL(sync_request2->event_listeners_requests->type, KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_1);
    ASSERT_EQUAL(sync_request2->events->type, KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0);
    ASSERT_EQUAL(kaa_list_get_size(sync_request2->events->data), 1);
    sync_request2->destroy(sync_request2);

    kaa_add_event(event_manager, "fqn", "data", 4, "target", 6);
    kaa_event_sync_request_t* sync_request3 = NULL;
    err_code = kaa_event_compile_request(event_manager, &sync_request3, 100502);
    ASSERT_EQUAL(err_code ,KAA_ERR_NONE);
    ASSERT_NOT_NULL(sync_request3);
    ASSERT_EQUAL(sync_request3->event_listeners_requests->type, KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_1);
    ASSERT_EQUAL(sync_request3->events->type, KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_0);
    size_t events_count = kaa_list_get_size(sync_request3->events->data);
    ASSERT_EQUAL(events_count, 2);
    sync_request3->destroy(sync_request3);

    kaa_event_handle_sync(event_manager, 100502, NULL, NULL);

    kaa_event_sync_request_t* sync_request4 = NULL;
    err_code = kaa_event_compile_request(event_manager, &sync_request4, 100503);
    ASSERT_EQUAL(err_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(sync_request4);
    ASSERT_EQUAL(sync_request4->event_listeners_requests->type, KAA_UNION_ARRAY_EVENT_LISTENERS_REQUEST_OR_NULL_BRANCH_1);
    ASSERT_EQUAL(sync_request4->events->type, KAA_UNION_ARRAY_EVENT_OR_NULL_BRANCH_1);
    sync_request4->destroy(sync_request4);
}



void global_event_cb(const char *fqn, const char *data, size_t size, const char *source)
{
    global_events_counter++;
}



void specific_event_cb(const char *fqn, const char *data, size_t size, const char *source)
{
    specific_events_counter++;
}



void test_kaa_add_on_event_callback()
{
    KAA_TRACE_IN(logger);

    kaa_add_on_event_callback(event_manager, "fqn", specific_event_cb);
    kaa_add_on_event_callback(event_manager, NULL, global_event_cb);

    ASSERT_EQUAL(global_events_counter, 0);
    ASSERT_EQUAL(specific_events_counter, 0);

    kaa_event_t * event1 = kaa_event_create();
    event1->event_class_fqn = kaa_string_move_create("fqn", NULL);
    event1->event_data = (kaa_bytes_t *) KAA_CALLOC(1, sizeof(kaa_bytes_t));
    event1->event_data->buffer = NULL;
    event1->event_data->size = 0;
    event1->source = kaa_union_string_or_null_branch_1_create();
    event1->target = kaa_union_string_or_null_branch_1_create();
    kaa_list_t * list1 = kaa_list_create(event1);
    kaa_event_handle_sync(event_manager, 1, NULL, list1);

    kaa_list_destroy(list1, &kaa_event_destroy);
    list1 = NULL;

    ASSERT_EQUAL(global_events_counter, 0);
    ASSERT_EQUAL(specific_events_counter, 1);

    kaa_event_t * event2 = kaa_event_create();
    event2->event_class_fqn = kaa_string_move_create("fqn2", NULL);
    event2->event_data = (kaa_bytes_t *) KAA_CALLOC(1, sizeof(kaa_bytes_t));
    event2->source = kaa_union_string_or_null_branch_1_create();
    event2->target = kaa_union_string_or_null_branch_1_create();
    kaa_list_t * list2 = kaa_list_create(event2);
    kaa_event_handle_sync(event_manager, 2, NULL, list2);

    ASSERT_EQUAL(global_events_counter, 1);
    ASSERT_EQUAL(specific_events_counter, 1);

    kaa_list_destroy(list2, &kaa_event_destroy);
}
#endif



int test_init(void)
{
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_NOT_NULL(logger);

#ifndef KAA_DISABLE_FEATURE_EVENTS
    error = kaa_status_create(&status);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_NOT_NULL(status);

    error = kaa_channel_manager_create(&channel_manager, logger);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_NOT_NULL(channel_manager);

    error = kaa_event_manager_create(&event_manager, status, channel_manager, logger);
    ASSERT_EQUAL(error, KAA_ERR_NONE);
    ASSERT_NOT_NULL(event_manager);
#endif
    return 0;
}



int test_deinit(void)
{
#ifndef KAA_DISABLE_FEATURE_EVENTS
    kaa_event_manager_destroy(event_manager);
    kaa_channel_manager_destroy(channel_manager);
    kaa_status_destroy(status);
#endif
    kaa_log_destroy(logger);
    return 0;
}



KAA_SUITE_MAIN(Event, test_init, test_deinit
#ifndef KAA_DISABLE_FEATURE_EVENTS
        ,
          KAA_TEST_CASE(create_event_manager, test_kaa_create_event_manager)
          KAA_TEST_CASE(compile_event_request, test_kaa_event_compile_request)
          KAA_TEST_CASE(add_on_event_callback, test_kaa_add_on_event_callback)
#endif
        )

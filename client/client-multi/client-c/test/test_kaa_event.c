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

#include "kaa_test.h"
#include "kaa_log.h"

#include "kaa_context.h"
#include "kaa_mem.h"
#include <string.h>

static int global_events_counter = 0;
static int specific_events_counter = 0;

static kaa_logger_t *logger = NULL;

void test_kaa_create_event_manager()
{
    KAA_TRACE_IN(logger);

    kaa_event_manager_t *event_manager = NULL;
    kaa_error_t err_code = kaa_create_event_manager(&event_manager);
    ASSERT_EQUAL(err_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(event_manager);
    kaa_destroy_event_manager(event_manager);
}

void test_kaa_event_compile_request()
{
    KAA_TRACE_IN(logger);

    kaa_context_t *context;
    kaa_context_create(&context, logger);

    kaa_event_sync_request_t* sync_request = NULL;
    kaa_event_compile_request(context, &sync_request, 100499);
    kaa_event_sequence_number_response_t seq_n_resp;
    seq_n_resp.seq_num = 0;

    kaa_event_handle_sync(context, 100499, &seq_n_resp, NULL);
    kaa_error_t err_code = KAA_ERR_NONE;
    err_code = kaa_event_compile_request(context, &sync_request, 100500);
    ASSERT_EQUAL(err_code ,KAA_ERR_NONE);
    ASSERT_NOT_NULL(sync_request);
    ASSERT_EQUAL(sync_request->event_listeners_requests->type, KAA_ARRAY_EVENT_LISTENERS_REQUEST_NULL_UNION_NULL_BRANCH);
    ASSERT_EQUAL(sync_request->events->type, KAA_ARRAY_EVENT_NULL_UNION_NULL_BRANCH);
    sync_request->destruct(sync_request);
    KAA_FREE(sync_request);

    kaa_add_event(context, "fqn", 3, "data", 4, "target", 6);
    kaa_event_sync_request_t* sync_request1 = NULL;
    err_code = kaa_event_compile_request(context, &sync_request1, 100501);
    ASSERT_EQUAL(err_code ,KAA_ERR_NONE);
    ASSERT_NOT_NULL(sync_request1);
    ASSERT_EQUAL(sync_request1->event_listeners_requests->type, KAA_ARRAY_EVENT_LISTENERS_REQUEST_NULL_UNION_NULL_BRANCH);
    ASSERT_EQUAL(sync_request1->events->type, KAA_ARRAY_EVENT_NULL_UNION_ARRAY_BRANCH);
    ASSERT_EQUAL(kaa_list_get_size(sync_request1->events->data), 1);
    sync_request1->destruct(sync_request1);
    KAA_FREE(sync_request1);

    kaa_add_event(context, "fqn", 3, "data", 4, "target", 6);
    kaa_event_sync_request_t* sync_request2 = NULL;
    err_code = kaa_event_compile_request(context, &sync_request2, 100502);
    ASSERT_EQUAL(err_code ,KAA_ERR_NONE);
    ASSERT_NOT_NULL(sync_request2);
    ASSERT_EQUAL(sync_request2->event_listeners_requests->type, KAA_ARRAY_EVENT_LISTENERS_REQUEST_NULL_UNION_NULL_BRANCH);
    ASSERT_EQUAL(sync_request2->events->type, KAA_ARRAY_EVENT_NULL_UNION_ARRAY_BRANCH);
    ASSERT_EQUAL(kaa_list_get_size(sync_request2->events->data), 2);
    sync_request2->destruct(sync_request2);
    KAA_FREE(sync_request2);

    kaa_event_handle_sync(context, 100502, NULL, NULL);

    kaa_event_sync_request_t* sync_request3 = NULL;
    err_code = kaa_event_compile_request(context, &sync_request3, 100503);
    ASSERT_EQUAL(err_code ,KAA_ERR_NONE);
    ASSERT_NOT_NULL(sync_request3);
    ASSERT_EQUAL(sync_request3->event_listeners_requests->type, KAA_ARRAY_EVENT_LISTENERS_REQUEST_NULL_UNION_NULL_BRANCH);
    ASSERT_EQUAL(sync_request3->events->type, KAA_ARRAY_EVENT_NULL_UNION_NULL_BRANCH);
    sync_request3->destruct(sync_request3);
    KAA_FREE(sync_request3);

    kaa_context_destroy(context);
    context = NULL;
}

void global_event_cb(const char *fqn, const char *data, size_t size)
{
    global_events_counter++;
}

void specific_event_cb(const char *fqn, const char *data, size_t size)
{
    specific_events_counter++;
}

static void kaa_destroy_event(void* data)
{
    kaa_event_t* record = (kaa_event_t*)data;

    KAA_FREE(record->event_class_fqn);
    kaa_destroy_bytes(record->event_data);
    KAA_FREE(record->event_data);
    record->source->destruct(record->source);
    KAA_FREE(record->source);
    record->target->destruct(record->target);
    KAA_FREE(record->target);
}

void test_kaa_add_on_event_callback()
{
    KAA_TRACE_IN(logger);

    kaa_context_t *context;
    kaa_context_create(&context, logger);

    kaa_add_on_event_callback(context->event_manager, "fqn", 3, specific_event_cb);
    kaa_add_on_event_callback(context->event_manager, NULL, 0, global_event_cb);

    ASSERT_EQUAL(global_events_counter, 0);
    ASSERT_EQUAL(specific_events_counter, 0);

    kaa_event_t * event1 = kaa_create_event();
    event1->event_class_fqn = KAA_CALLOC(4, sizeof(char));
    event1->event_data = KAA_CALLOC(1, sizeof(kaa_bytes_t));
    event1->event_data->buffer = NULL;
    event1->event_data->size = 0;
    event1->source = kaa_create_string_null_union_null_branch();
    event1->target = kaa_create_string_null_union_null_branch();
    memcpy(event1->event_class_fqn, "fqn", 3);
    kaa_list_t * list1 = kaa_list_create(event1);
    kaa_event_handle_sync(context, 1, NULL, list1);

    kaa_list_destroy(list1, &kaa_destroy_event);
    list1 = NULL;

    ASSERT_EQUAL(global_events_counter, 0);
    ASSERT_EQUAL(specific_events_counter, 1);

    kaa_event_t * event2 = kaa_create_event();
    event2->event_class_fqn = KAA_CALLOC(5, sizeof(char));
    event2->event_data = KAA_CALLOC(1, sizeof(kaa_bytes_t));
    event2->event_data->buffer = NULL;
    event2->event_data->size = 0;
    event2->source = kaa_create_string_null_union_null_branch();
    event2->target = kaa_create_string_null_union_null_branch();
    memcpy(event2->event_class_fqn, "fqn2", 4);
    kaa_list_t * list2 = kaa_list_create(event2);
    kaa_event_handle_sync(context, 2, NULL, list2);

    ASSERT_EQUAL(global_events_counter, 1);
    ASSERT_EQUAL(specific_events_counter, 1);

    kaa_list_destroy(list2, &kaa_destroy_event);

    kaa_context_destroy(context);
    context = NULL;
}

#endif
int main(int argc, char **argv)
{
    kaa_log_create(&logger, KAA_LOG_TRACE, NULL);

#ifndef KAA_DISABLE_FEATURE_EVENTS
    test_kaa_create_event_manager();
    test_kaa_event_compile_request();
    test_kaa_add_on_event_callback();
#endif

    kaa_log_destroy(logger);
    return 0;
}

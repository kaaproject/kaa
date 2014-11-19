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
#include <stddef.h>
#include <string.h>

#include "kaa_list.h"
#include "kaa_context.h"
#include "kaa_mem.h"

static KAA_INT32T event_sequence_number = 0;

typedef struct event_t {
    size_t      seq_number;
    char *      fqn;
    size_t      fqn_size;
    char *      data;
    size_t      data_size;
    char *      target;
    size_t      target_size;
} event_t;

static event_t * create_event(const char *FQN, size_t fqn_len, const char *DATA, size_t data_len, const char *TARGET, size_t target_len)
{
    event_t * event = KAA_MALLOC(event_t);
    event->seq_number = ++event_sequence_number;
    if (fqn_len > 0) {
        event->fqn_size = fqn_len;
        event->fqn = KAA_CALLOC(sizeof(char), fqn_len);
        memcpy(event->fqn, FQN, fqn_len);
    } else {
        event->fqn_size = 0;
        event->fqn  = NULL;
    }
    if (data_len > 0) {
        event->data_size = data_len;
        event->data = KAA_CALLOC(sizeof(char), data_len);
        memcpy(event->data, DATA, data_len);
    } else {
        event->data_size = 0;
        event->data  = NULL;
    }
    if (target_len > 0) {
        event->target_size = target_len;
        event->target = KAA_CALLOC(sizeof(char), target_len);
        memcpy(event->target, TARGET, target_len);
    } else {
        event->target_size = 0;
        event->target  = NULL;
    }

    return event;
}

static void destroy_event(void *event_t_ptr)
{
    event_t * event = (event_t *)event_t_ptr;
    if (event->fqn_size > 0) {
        KAA_FREE(event->fqn);
    }
    if (event->data_size > 0) {
        KAA_FREE(event->data);
    }
    if (event->target_size > 0) {
        KAA_FREE(event->target);
    }
}
static kaa_service_t event_sync_services[1] = {KAA_SERVICE_EVENT};

typedef struct sent_events_tuple_t {
    size_t request_id;
    kaa_list_t *sent_events;
} sent_events_tuple_t;

static sent_events_tuple_t * create_events_tuple(size_t id, kaa_list_t *events_head)
{
    sent_events_tuple_t * tuple = KAA_MALLOC(sent_events_tuple_t);
    tuple->request_id = id;
    tuple->sent_events = events_head;

    return tuple;
}

static void destroy_events_tuple(void * tuple_p)
{
    sent_events_tuple_t * tuple = (sent_events_tuple_t *)tuple_p;
    kaa_list_destroy(tuple->sent_events, &destroy_event);
}

typedef struct event_callback_pair_t_ {
    char *fqn;
    event_callback_t cb;
} event_callback_pair_t;

static event_callback_pair_t * create_event_callback_pair(const char * fqn, size_t fqn_length, event_callback_t callback)
{
    event_callback_pair_t * pair = KAA_MALLOC(event_callback_pair_t);
    pair->fqn = KAA_CALLOC(fqn_length + 1, sizeof(char));
    memcpy(pair->fqn, fqn, fqn_length);
    pair->cb = callback;
    return pair;
}

static void destroy_event_callback_pair(void *pair_p)
{
    event_callback_pair_t * pair = (event_callback_pair_t *)pair_p;
    KAA_FREE(pair->fqn);
}

static event_callback_t find_event_callback(kaa_list_t *head, const char * fqn)
{
    while (head) {
        event_callback_pair_t *pair = (event_callback_pair_t *)kaa_list_get_data(head);
        if (strcmp(fqn, pair->fqn) == 0) {
            return pair->cb;
        }
        head = kaa_list_next(head);
    }
    return NULL;
}

typedef enum kaa_event_sequence_number_status_t {
    KAA_EVENT_SEQUENCE_NUMBER_UNSYNCHRONIZED,
    KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS,
    KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED
} kaa_event_sequence_number_status_t;

/* Public stuff */
struct kaa_event_manager_t {
    kaa_list_t *        sent_events;
    kaa_list_t *        pending_events;
    kaa_list_t *        event_callbacks;
    event_callback_t    global_event_callback;
    kaa_event_sequence_number_status_t sequence_number_status;
};

kaa_error_t kaa_create_event_manager(kaa_event_manager_t ** event_manager_p)
{
    kaa_event_manager_t * event_manager = KAA_MALLOC(kaa_event_manager_t);
    if (event_manager == NULL) {
        return KAA_ERR_NOMEM;
    }
    event_manager->pending_events = NULL;
    event_manager->sent_events = NULL;
    event_manager->event_callbacks = NULL;
    event_manager->global_event_callback = NULL;
    event_manager->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_UNSYNCHRONIZED;
    *event_manager_p = event_manager;
    return KAA_ERR_NONE;
}

void kaa_destroy_event_manager(kaa_event_manager_t *event_manager)
{
    kaa_list_destroy(event_manager->sent_events, &destroy_events_tuple);
    kaa_list_destroy(event_manager->pending_events, &destroy_event);
    kaa_list_destroy(event_manager->event_callbacks, &destroy_event_callback_pair);
    KAA_FREE(event_manager);
}

void kaa_add_event(void *ctx, const char * fqn, size_t fqn_length, const char * event_data, size_t event_data_size, const char * target, size_t target_size)
{
    event_t *event = create_event(fqn, fqn_length, event_data, event_data_size, target, target_size);

    kaa_context_t * context = (kaa_context_t*)ctx;
    kaa_event_manager_t * event_manager = context->event_manager;

    if (event_manager->pending_events != NULL) {
        kaa_list_push_back(event_manager->pending_events, event);
    } else {
        event_manager->pending_events = kaa_list_create(event);
    }
    kaa_sync_t sync = kaa_channel_manager_get_sync_handler(context, event_sync_services[0]);
    if (sync) {
        (*sync)(1, event_sync_services);
    }
}

kaa_event_sync_request_t* kaa_event_compile_request(void *ctx, size_t requestId)
{
    kaa_context_t * context = (kaa_context_t *)ctx;
    kaa_event_manager_t * event_manager = context->event_manager;

    kaa_event_sync_request_t* request = kaa_create_event_sync_request();
    request->event_listeners_requests = kaa_create_array_event_listeners_request_array_null_union_null_branch();

    if (event_manager->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_UNSYNCHRONIZED) {
        request->event_sequence_number_request = kaa_create_record_event_sequence_number_request_null_union_event_sequence_number_request_branch();
        request->event_sequence_number_request->data = kaa_create_event_sequence_number_request();
        request->events = kaa_create_array_event_null_union_null_branch();
        event_manager->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS;
        return request;
    } else {
        request->event_sequence_number_request = kaa_create_record_event_sequence_number_request_null_union_null_branch();
        if (event_manager->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS) {
            request->events = kaa_create_array_event_null_union_null_branch();
            return request;
        }
    }

    kaa_list_t *events_to_resend_head = event_manager->sent_events;
    event_manager->sent_events = NULL;
    kaa_list_t *events_to_resend = events_to_resend_head;

    kaa_list_t *new_events = event_manager->pending_events;
    event_manager->pending_events = NULL;

    while (events_to_resend) {
        sent_events_tuple_t * tuple = (sent_events_tuple_t *)kaa_list_get_data(events_to_resend);

        if (tuple) {
            new_events = kaa_lists_merge(new_events, tuple->sent_events);
            tuple->sent_events = NULL;
        }

        events_to_resend = kaa_list_next(events_to_resend);
    }
    kaa_list_destroy(events_to_resend_head, &destroy_events_tuple);

    event_manager->sent_events = kaa_list_create(create_events_tuple(requestId, new_events));

    kaa_list_t *new_events_copy = NULL;
    while (new_events) {
        event_t * event_source = (event_t *)kaa_list_get_data(new_events);
        kaa_event_t * event_copy = kaa_create_event();

        event_copy->seq_num = event_source->seq_number;
        event_copy->event_class_fqn = KAA_CALLOC(event_source->fqn_size + 1, sizeof(char));
        memcpy(event_copy->event_class_fqn, event_source->fqn, event_source->fqn_size);
        event_copy->event_data = KAA_MALLOC(kaa_bytes_t);
        event_copy->event_data->size = event_source->data_size;
        event_copy->event_data->buffer = KAA_CALLOC(event_source->data_size, sizeof(char));
        memcpy(event_copy->event_data->buffer, event_source->data, event_source->data_size);

        if (event_source->target_size > 0) {
            event_copy->target = kaa_create_string_null_union_string_branch();
            event_copy->target->data = KAA_CALLOC(event_source->target_size + 1, sizeof(char));
            memcpy(event_copy->target->data, event_source->target, event_source->target_size);
        } else {
            event_copy->target = kaa_create_string_null_union_null_branch();
        }
        event_copy->source = kaa_create_string_null_union_null_branch();

        if (new_events_copy == NULL) {
            new_events_copy = kaa_list_create(event_copy);
        } else {
            kaa_list_push_back(new_events_copy, event_copy);
        }
        new_events = kaa_list_next(new_events);
    }

    if (new_events_copy == NULL) {
        request->events = kaa_create_array_event_array_null_union_null_branch();
        request->events->data = NULL;
    } else {
        request->events = kaa_create_array_event_array_null_union_array_branch();
        request->events->data = new_events_copy;
    }
    return request;
}

void kaa_event_handle_sync(void *ctx, size_t request_id, kaa_event_sequence_number_response_t *event_sn_response, kaa_list_t *events)
{
    kaa_context_t * context = (kaa_context_t *)ctx;
    kaa_event_manager_t * event_manager = context->event_manager;

    if (event_manager->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS
            && event_sn_response != NULL) {
        int32_t server_sn = event_sn_response->seq_num > 0 ? event_sn_response->seq_num : 0;
        kaa_list_t *pending_events = event_manager->pending_events;
        size_t events_count = kaa_list_get_size(pending_events);
        if (event_sequence_number - events_count != server_sn) {
            event_sequence_number = server_sn;
            while (pending_events) {
                event_t *event = kaa_list_get_data(pending_events);
                event->seq_number = ++event_sequence_number;
                pending_events = kaa_list_next(pending_events);
            }
        }
        event_manager->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED;
    }


    kaa_list_t * sent_events = event_manager->sent_events;
    while (sent_events) {
        sent_events_tuple_t * tuple = (sent_events_tuple_t *)kaa_list_get_data(sent_events);
        if (tuple->request_id == request_id) {
            kaa_list_remove_at(&event_manager->sent_events, sent_events, &destroy_events_tuple);
            break;
        }
        sent_events = kaa_list_next(sent_events);
    }

    kaa_event_t * event = (kaa_event_t *)kaa_list_get_data(events);
    while (event) {
        event_callback_t cb = find_event_callback(event_manager->event_callbacks, event->event_class_fqn);
        if (cb) {
            (*cb)((const char *)(event->event_class_fqn), (const char *)(event->event_data->buffer), event->event_data->size);
        } else if (event_manager->global_event_callback != NULL) {
            (*event_manager->global_event_callback)((const char *)(event->event_class_fqn), (const char *)(event->event_data->buffer), event->event_data->size);
        }

        events = kaa_list_next(events);
        event = (kaa_event_t *)kaa_list_get_data(events);
    }

}

void kaa_add_on_event_callback(kaa_event_manager_t *event_manager, const char *fqn, size_t fqn_length, event_callback_t callback)
{
    if (event_manager != NULL && callback != NULL) {
        if (fqn != NULL && fqn_length > 0) {
            event_callback_pair_t * pair = create_event_callback_pair(fqn, fqn_length, callback);
            if (event_manager->event_callbacks == NULL) {
                event_manager->event_callbacks = kaa_list_create(pair);
            } else {
                kaa_list_t * head = event_manager->event_callbacks;
                while (head) {
                    event_callback_pair_t *data = (event_callback_pair_t *)kaa_list_get_data(head);
                    if (strcmp(fqn, data->fqn) == 0) {
                        kaa_list_set_data_at(head, pair, destroy_event_callback_pair);
                        return;
                    }
                    head = kaa_list_next(head);
                }
                kaa_list_push_back(event_manager->event_callbacks, pair);
            }
        } else {
            event_manager->global_event_callback = callback;
        }
    }
}

typedef struct event_class_family_t {
    char *      ecf_name;
    size_t      supported_incoming_fqns_count;
    char **      supported_incoming_fqns;
} event_class_family_t;

# define SUPPORTED_EVENT_CLASS_FAMILIES_SIZE 1
static const char *TestEventFamilyFQNS[8] = {
        "org.kaaproject.kaa.example.audio.PlayCommand",
        "org.kaaproject.kaa.example.audio.RewindCommand",
        "org.kaaproject.kaa.example.audio.PauseCommand",
        "org.kaaproject.kaa.example.audio.StopCommand",
        "org.kaaproject.kaa.example.audio.PlaybackStatus",
        "org.kaaproject.kaa.example.audio.BatteryChargingStatus",
        "org.kaaproject.kaa.example.audio.BatteryStatus",
        "org.kaaproject.kaa.example.audio.StatusEvent"
};
static const event_class_family_t SUPPORTED_EVENT_CLASS_FAMILIES[SUPPORTED_EVENT_CLASS_FAMILIES_SIZE] =
{
    {
        /* .ecf_name = */                       "TestEventFamily",
        /* .supported_incoming_fqns_count = */  8,
        /* .supported_incoming_fqns = */        (char **)TestEventFamilyFQNS
    }
};

const char * kaa_find_class_family_name(const char *fqn)
{
    size_t i = 0;
    for (;SUPPORTED_EVENT_CLASS_FAMILIES_SIZE - i++;)
    {
        size_t fqn_count = SUPPORTED_EVENT_CLASS_FAMILIES[SUPPORTED_EVENT_CLASS_FAMILIES_SIZE - i].supported_incoming_fqns_count;
        char **fqns = SUPPORTED_EVENT_CLASS_FAMILIES[SUPPORTED_EVENT_CLASS_FAMILIES_SIZE - i].supported_incoming_fqns;
        for (;fqn_count--;)
        {
            if (strcmp(fqn, fqns[fqn_count]) == 0)
            {
                return SUPPORTED_EVENT_CLASS_FAMILIES[SUPPORTED_EVENT_CLASS_FAMILIES_SIZE - i].ecf_name;
            }
        }
    }
    return NULL;
}

#endif

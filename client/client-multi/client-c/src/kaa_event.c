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
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <string.h>

#include "kaa_list.h"
#include "kaa_context.h"
#include "kaa_mem.h"

extern kaa_sync_handler_fn kaa_channel_manager_get_sync_handler(kaa_channel_manager_t *this, kaa_service_t service_type);

static int32_t event_sequence_number = 0;

typedef struct event_t {
    size_t      seq_number;
    char *      fqn;
    size_t      fqn_size;
    char *      data;
    size_t      data_size;
    char *      target;
    size_t      target_size;
} event_t;

static event_t * create_event(size_t seq_n, const char *FQN, size_t fqn_len, const char *DATA, size_t data_len, const char *TARGET, size_t target_len, kaa_error_t *error_code)
{
    event_t * event = KAA_MALLOC(event_t);
    if (event == NULL) {
        *error_code = KAA_ERR_NOMEM;
        return NULL;
    }

    event->seq_number = seq_n;

    event->fqn_size = fqn_len;
    event->fqn = KAA_CALLOC(sizeof(char), fqn_len);
    if (event->fqn == NULL) {
        *error_code = KAA_ERR_NOMEM;
        KAA_FREE(event);
        return NULL;
    }
    memcpy(event->fqn, FQN, fqn_len);

    if (data_len > 0) {
        event->data_size = data_len;
        event->data = KAA_CALLOC(sizeof(char), data_len);
        if (event->data == NULL) {
            *error_code = KAA_ERR_NOMEM;
            KAA_FREE(event->fqn);
            KAA_FREE(event);
            return NULL;
        }
        memcpy(event->data, DATA, data_len);
    } else {
        event->data_size = 0;
        event->data  = NULL;
    }
    if (target_len > 0) {
        event->target_size = target_len;
        event->target = KAA_CALLOC(sizeof(char), target_len);
        if (event->target == NULL) {
            *error_code = KAA_ERR_NOMEM;
            KAA_FREE(event->data);
            KAA_FREE(event->fqn);
            KAA_FREE(event);
            return NULL;
        }
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
    if (tuple == NULL) {
        return NULL;
    }
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
    if (pair == NULL) {
        return NULL;
    }
    pair->fqn = KAA_CALLOC(fqn_length + 1, sizeof(char));
    if (pair->fqn == NULL) {
        KAA_FREE(pair);
        return NULL;
    }
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

typedef struct event_transaction_t_ {
    kaa_trx_id      id;
    kaa_list_t *    events;
} event_transaction_t;

static event_transaction_t * create_transaction(kaa_trx_id id)
{
    event_transaction_t * trx = KAA_MALLOC(event_transaction_t);
    trx->id = id;
    trx->events = NULL;
    return trx;
}

static void destroy_transaction(void * trx_p)
{
    event_transaction_t * trx = (event_transaction_t *)trx_p;
    kaa_list_destroy(trx->events, &destroy_event);
}

static kaa_trx_id trx_counter = 0;

static kaa_trx_id trx_search_arg0 = 0;
static bool transaction_search_by_id_predicate(void *trx_p)
{
    event_transaction_t * trx = (event_transaction_t *)trx_p;
    return trx_search_arg0 == trx->id;
}

/* Public stuff */
struct kaa_event_manager_t {
    kaa_list_t *        sent_events;
    kaa_list_t *        pending_events;
    kaa_list_t *        event_callbacks;
    kaa_list_t *        transactions;
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
    event_manager->transactions = NULL;
    event_manager->global_event_callback = NULL;
    event_manager->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_UNSYNCHRONIZED;
    *event_manager_p = event_manager;
    return KAA_ERR_NONE;
}

void kaa_destroy_event_manager(kaa_event_manager_t *event_manager)
{
    if (event_manager != NULL) {
        kaa_list_destroy(event_manager->sent_events, &destroy_events_tuple);
        kaa_list_destroy(event_manager->pending_events, &destroy_event);
        kaa_list_destroy(event_manager->event_callbacks, &destroy_event_callback_pair);
        kaa_list_destroy(event_manager->transactions, &destroy_transaction);
        KAA_FREE(event_manager);
    }
}

kaa_error_t kaa_add_event(void *ctx, const char * fqn, size_t fqn_length, const char * event_data, size_t event_data_size, const char * target, size_t target_size)
{
    KAA_RETURN_IF_NIL(ctx, KAA_ERR_NOT_INITIALIZED);
    if (fqn == NULL || fqn_length == 0) {
        return KAA_ERR_EVENT_BAD_FQN;
    }
    kaa_context_t * context = (kaa_context_t*)ctx;

    KAA_RETURN_IF_NIL(context->event_manager, KAA_ERR_NOT_INITIALIZED);
    kaa_event_manager_t * event_manager = context->event_manager;

    kaa_error_t error_code = KAA_ERR_NONE;
    event_t *event = create_event(++event_sequence_number, fqn, fqn_length, event_data, event_data_size, target, target_size, &error_code);
    if (event == NULL) {
        return error_code;
    }
    if (event_manager->pending_events != NULL) {
        kaa_list_push_back(event_manager->pending_events, event);
    } else {
        event_manager->pending_events = kaa_list_create(event);
    }
    kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(context->channel_manager, event_sync_services[0]);
    if (sync) {
        (*sync)(event_sync_services, 1);
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_compile_request(void *ctx, kaa_event_sync_request_t** request_p, size_t requestId)
{
    KAA_RETURN_IF_NIL2(ctx, request_p, KAA_ERR_BADPARAM);
    kaa_context_t * context = (kaa_context_t *)ctx;

    KAA_RETURN_IF_NIL(context->event_manager, KAA_ERR_NOT_INITIALIZED);
    kaa_event_manager_t * event_manager = context->event_manager;

    kaa_event_sync_request_t* request = kaa_create_event_sync_request();
    request->event_listeners_requests = kaa_create_array_event_listeners_request_null_union_null_branch();

    if (event_manager->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_UNSYNCHRONIZED) {
        request->event_sequence_number_request = kaa_create_record_event_sequence_number_request_null_union_event_sequence_number_request_branch();
        request->event_sequence_number_request->data = kaa_create_event_sequence_number_request();
        request->events = kaa_create_array_event_null_union_null_branch();
        event_manager->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS;
        *request_p = request;
        return KAA_ERR_NONE;
    } else {
        request->event_sequence_number_request = kaa_create_record_event_sequence_number_request_null_union_null_branch();
        if (event_manager->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS) {
            request->events = kaa_create_array_event_null_union_null_branch();
            *request_p = request;
            return KAA_ERR_NONE;
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

    sent_events_tuple_t *sent_events = create_events_tuple(requestId, new_events);
    if (sent_events != NULL) {
        event_manager->sent_events = kaa_list_create(sent_events);
    }

    kaa_list_t *new_events_copy = NULL;
    while (new_events) {
        event_t * event_source = (event_t *)kaa_list_get_data(new_events);
        kaa_event_t * event_copy = kaa_create_event();

        if (event_source->seq_number == (size_t)-1) {
            event_source->seq_number = ++event_sequence_number;
        }

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
        request->events = kaa_create_array_event_null_union_null_branch();
        request->events->data = NULL;
    } else {
        request->events = kaa_create_array_event_null_union_array_branch();
        request->events->data = new_events_copy;
    }

    *request_p = request;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_handle_sync(void *ctx, size_t request_id, kaa_event_sequence_number_response_t *event_sn_response, kaa_list_t *events)
{
    KAA_RETURN_IF_NIL(ctx, KAA_ERR_NOT_INITIALIZED);
    kaa_context_t * context = (kaa_context_t *)ctx;

    KAA_RETURN_IF_NIL(context->event_manager, KAA_ERR_NOT_INITIALIZED);
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

    return KAA_ERR_NONE;
}

kaa_error_t kaa_add_on_event_callback(kaa_event_manager_t *event_manager, const char *fqn, size_t fqn_length, event_callback_t callback)
{
    KAA_RETURN_IF_NIL2(event_manager, callback, KAA_ERR_BADPARAM);

    if (fqn != NULL && fqn_length > 0) {
        event_callback_pair_t * pair = create_event_callback_pair(fqn, fqn_length, callback);
        if (pair == NULL) {
            return KAA_ERR_NOMEM;
        }
        if (event_manager->event_callbacks == NULL) {
            event_manager->event_callbacks = kaa_list_create(pair);
        } else {
            kaa_list_t * head = event_manager->event_callbacks;
            while (head) {
                event_callback_pair_t *data = (event_callback_pair_t *)kaa_list_get_data(head);
                if (strcmp(fqn, data->fqn) == 0) {
                    kaa_list_set_data_at(head, pair, destroy_event_callback_pair);
                    return KAA_ERR_NONE;
                }
                head = kaa_list_next(head);
            }
            kaa_list_push_back(event_manager->event_callbacks, pair);
        }
    } else {
        event_manager->global_event_callback = callback;
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_create_transaction(void *ctx, kaa_trx_id *trx_id)
{
    KAA_RETURN_IF_NIL2(ctx, trx_id, KAA_ERR_NOT_INITIALIZED);
    kaa_context_t * context = (kaa_context_t *)ctx;

    KAA_RETURN_IF_NIL(context->event_manager, KAA_ERR_NOT_INITIALIZED);
    kaa_event_manager_t * event_manager = context->event_manager;

    kaa_trx_id new_id = ++trx_counter;

    if (event_manager->transactions == NULL) {
        event_manager->transactions = kaa_list_create(create_transaction(new_id));
    } else {
        kaa_list_push_back(event_manager->transactions, create_transaction(new_id));
    }

    *trx_id = new_id;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_finish_transaction(void *ctx, kaa_trx_id trx_id)
{
    KAA_RETURN_IF_NIL(ctx, KAA_ERR_BADPARAM);
    kaa_context_t * context = (kaa_context_t *)ctx;

    KAA_RETURN_IF_NIL(context->event_manager, KAA_ERR_NOT_INITIALIZED);
    kaa_event_manager_t * event_manager = context->event_manager;
    if (event_manager->transactions != NULL) {
        trx_search_arg0 = trx_id;
        kaa_list_t * it = kaa_list_find_next(event_manager->transactions, transaction_search_by_id_predicate);
        if (it != NULL) {
            event_transaction_t * trx = kaa_list_get_data(it);
            bool need_sync = false;
            if (trx->events != NULL && kaa_list_get_size(trx->events) > 0) {
                kaa_lists_merge(event_manager->pending_events, trx->events);
                need_sync = true;
                trx->events = NULL;
            }
            kaa_list_remove_at(&event_manager->transactions, it, &destroy_transaction);
            kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(context->channel_manager, event_sync_services[0]);
            if (need_sync && sync) {
                (*sync)(event_sync_services, 1);
            }
            return KAA_ERR_NONE;
        }
    }
    return KAA_ERR_NOT_FOUND;
}

kaa_error_t kaa_event_remove_transaction(void *ctx, kaa_trx_id trx_id)
{
    KAA_RETURN_IF_NIL(ctx, KAA_ERR_BADPARAM);
    kaa_context_t * context = (kaa_context_t *)ctx;

    KAA_RETURN_IF_NIL(context->event_manager, KAA_ERR_NOT_INITIALIZED);
    kaa_event_manager_t * event_manager = context->event_manager;
    if (event_manager->transactions != NULL) {
        trx_search_arg0 = trx_id;
        kaa_list_t * it = kaa_list_find_next(event_manager->transactions, transaction_search_by_id_predicate);
        if (it != NULL) {
            kaa_list_remove_at(&event_manager->transactions, it, &destroy_transaction);
            return KAA_ERR_NONE;
        }
    }
    return KAA_ERR_NOT_FOUND;
}

kaa_error_t kaa_add_event_to_transaction(void *ctx, kaa_trx_id trx_id, const char * fqn, size_t fqn_length, const char * event_data, size_t event_data_size, const char * target, size_t target_size)
{
    KAA_RETURN_IF_NIL(ctx, KAA_ERR_NOT_INITIALIZED);
    if (fqn == NULL || fqn_length == 0) {
        return KAA_ERR_EVENT_BAD_FQN;
    }

    kaa_context_t * context = (kaa_context_t *)ctx;

    KAA_RETURN_IF_NIL(context->event_manager, KAA_ERR_NOT_INITIALIZED);
    kaa_event_manager_t * event_manager = context->event_manager;
    if (event_manager->transactions != NULL) {
        trx_search_arg0 = trx_id;
        kaa_list_t * it = kaa_list_find_next(event_manager->transactions, transaction_search_by_id_predicate);
        if (it != NULL) {
            event_transaction_t * trx = kaa_list_get_data(it);
            kaa_error_t error_code = KAA_ERR_NONE;
            event_t * event = create_event((size_t)-1, fqn, fqn_length, event_data, event_data_size, target, target_size, &error_code);
            if (event == NULL) {
                return error_code;
            }
            if (trx->events == NULL) {
                trx->events = kaa_list_create(event);
            } else {
                kaa_list_push_back(trx->events, event);
            }
            return KAA_ERR_NONE;
        }
    }
    return KAA_ERR_EVENT_TRX_NOT_FOUND;
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

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
#include "gen/kaa_endpoint_gen.h"

extern kaa_sync_handler_fn kaa_channel_manager_get_sync_handler(kaa_channel_manager_t *self, kaa_service_t service_type);

static void kaa_event_list_destroy_no_cleanup(void *data) {}

static void destroy_event(void *event_t_ptr)
{
    kaa_event_t * event = (kaa_event_t *)event_t_ptr;
    event->destroy(event);
}

static kaa_service_t event_sync_services[1] = {KAA_SERVICE_EVENT};

typedef struct sent_events_tuple_t {
    size_t request_id;
    kaa_list_t *sent_events;
} sent_events_tuple_t;

static sent_events_tuple_t * create_events_tuple(size_t id, kaa_list_t *events_head)
{
    sent_events_tuple_t * tuple = (sent_events_tuple_t *) KAA_MALLOC(sizeof(sent_events_tuple_t));
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
    kaa_event_callback_t cb;
} event_callback_pair_t;

static event_callback_pair_t * create_event_callback_pair(const char * fqn, kaa_event_callback_t callback)
{
    event_callback_pair_t * pair = (event_callback_pair_t *) KAA_MALLOC(sizeof(event_callback_pair_t));
    if (pair == NULL) {
        return NULL;
    }
    size_t fqn_length = strlen(fqn);
    pair->fqn = (char *) KAA_MALLOC((fqn_length + 1) * sizeof(char));
    if (pair->fqn == NULL) {
        KAA_FREE(pair);
        return NULL;
    }
    memcpy(pair->fqn, fqn, fqn_length);
    pair->fqn[fqn_length] = '\0';
    pair->cb = callback;
    return pair;
}

static void destroy_event_callback_pair(void *pair_p)
{
    event_callback_pair_t * pair = (event_callback_pair_t *)pair_p;
    KAA_FREE(pair->fqn);
}

static kaa_event_callback_t find_event_callback(kaa_list_t *head, const char * fqn)
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
    kaa_event_block_id      id;
    kaa_list_t *    events;
} event_transaction_t;

static event_transaction_t * create_transaction(kaa_event_block_id id)
{
    event_transaction_t * trx = (event_transaction_t *) KAA_MALLOC(sizeof(event_transaction_t));
    trx->id = id;
    trx->events = NULL;
    return trx;
}

static void destroy_transaction(void * trx_p)
{
    event_transaction_t * trx = (event_transaction_t *)trx_p;
    kaa_list_destroy(trx->events, &destroy_event);
}

static kaa_event_block_id trx_search_arg0 = 0;
static bool transaction_search_by_id_predicate(void *trx_p)
{
    event_transaction_t * trx = (event_transaction_t *)trx_p;
    return trx_search_arg0 == trx->id;
}

/* Public stuff */
struct kaa_event_manager_t {
    kaa_list_t *                sent_events;
    kaa_list_t *                pending_events;
    kaa_list_t *                event_callbacks;
    kaa_list_t *                transactions;
    kaa_event_block_id          trx_counter;
    kaa_event_callback_t        global_event_callback;
    size_t                      event_sequence_number;
    kaa_event_sequence_number_status_t sequence_number_status;

    kaa_status_t *              status;
    kaa_channel_manager_t *     channel_manager;
    kaa_logger_t *              logger;
};

kaa_error_t kaa_event_manager_create(kaa_event_manager_t **event_manager_p, kaa_status_t *status, kaa_channel_manager_t *channel_manager, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL(event_manager_p, KAA_ERR_BADPARAM)

    *event_manager_p = (kaa_event_manager_t *) KAA_MALLOC(sizeof(kaa_event_manager_t));
    if (!(*event_manager_p)) {
        return KAA_ERR_NOMEM;
    }

    (*event_manager_p)->pending_events = NULL;
    (*event_manager_p)->sent_events = NULL;
    (*event_manager_p)->event_callbacks = NULL;
    (*event_manager_p)->transactions = NULL;
    (*event_manager_p)->trx_counter = 0;
    (*event_manager_p)->global_event_callback = NULL;
    (*event_manager_p)->event_sequence_number = kaa_status_get_event_sequence_number(status);

    (*event_manager_p)->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_UNSYNCHRONIZED;

    (*event_manager_p)->status = status;
    (*event_manager_p)->channel_manager = channel_manager;
    (*event_manager_p)->logger = logger;
    return KAA_ERR_NONE;
}

void kaa_event_manager_destroy(kaa_event_manager_t *self)
{
    if (self) {
        kaa_list_destroy(self->sent_events, &destroy_events_tuple);
        kaa_list_destroy(self->pending_events, &destroy_event);
        kaa_list_destroy(self->event_callbacks, &destroy_event_callback_pair);
        kaa_list_destroy(self->transactions, &destroy_transaction);
        KAA_FREE(self);
    }
}

static kaa_error_t kaa_fill_event_structure(kaa_event_t *event, size_t sequence_number, const char * fqn, const char * event_data, size_t event_data_size, const char * target, size_t target_size)
{
    event->seq_num = sequence_number;
    event->source = kaa_create_string_null_union_null_branch();
    if (!event->source) {
        return KAA_ERR_NOMEM;
    }

    size_t fqn_length = strlen(fqn);
    // FIXME: event->event_class_fqn string destructor
    event->event_class_fqn = (char *) KAA_MALLOC(sizeof(char) * (fqn_length + 1));
    if (!event->event_class_fqn) {
        KAA_FREE(event->source);
        return KAA_ERR_NOMEM;
    }
    memcpy(event->event_class_fqn, fqn, fqn_length);
    event->event_class_fqn[fqn_length] = '\0';

    if (event_data && event_data_size > 0) {
        event->event_data = (kaa_bytes_t *) KAA_MALLOC(sizeof(kaa_bytes_t));
        if (!event->event_data) {
            KAA_FREE(event->event_class_fqn);
            KAA_FREE(event->source);
            return KAA_ERR_NOMEM;
        }
        event->event_data->size = event_data_size;
        event->event_data->buffer = KAA_MALLOC(sizeof(uint8_t) * event_data_size);
        if (!event->event_data->buffer) {
            KAA_FREE(event->event_data);
            KAA_FREE(event->event_class_fqn);
            KAA_FREE(event->source);
            return KAA_ERR_NOMEM;
        }
        memcpy(event->event_data->buffer, event_data, event_data_size);
    }

    if (target && target_size > 0) {
        // FIXME: event->target string destructor
        event->target = kaa_create_string_null_union_string_branch();
        if (!event->target) {
            KAA_FREE(event->event_data->buffer);
            KAA_FREE(event->event_data);
            KAA_FREE(event->event_class_fqn);
            KAA_FREE(event->source);
            return KAA_ERR_NOMEM;
        }
        event->target->data = KAA_MALLOC(sizeof(char) * (target_size + 1));
        memcpy(event->target->data, target, target_size);
        ((char *)event->target->data)[target_size] = '\0';
    } else {
        event->target = kaa_create_string_null_union_null_branch();
        if (!event->target) {
            KAA_FREE(event->event_data->buffer);
            KAA_FREE(event->event_data);
            KAA_FREE(event->event_class_fqn);
            KAA_FREE(event->source);
            return KAA_ERR_NOMEM;
        }
    }
    return KAA_ERR_NONE;
}


kaa_error_t kaa_add_event(kaa_event_manager_t *self, const char * fqn, const char * event_data, size_t event_data_size, const char * target, size_t target_size)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);
    KAA_RETURN_IF_NIL(fqn, KAA_ERR_EVENT_BAD_FQN);

    kaa_event_t *event = kaa_create_event();
    if (!event) {
        return KAA_ERR_NOMEM;
    }

    kaa_error_t error_code = kaa_fill_event_structure(event, ++self->event_sequence_number, fqn, event_data, event_data_size, target, target_size);
    if (error_code) {
        KAA_FREE(event);
        return KAA_ERR_NOMEM;
    }

    if (self->pending_events != NULL) {
        kaa_list_push_back(self->pending_events, event);
    } else {
        self->pending_events = kaa_list_create(event);
    }
    kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(self->channel_manager, event_sync_services[0]);
    if (sync) {
        (*sync)(event_sync_services, 1);
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_compile_request(kaa_event_manager_t *self, kaa_event_sync_request_t** request_p, size_t requestId)
{
    KAA_RETURN_IF_NIL2(self, request_p, KAA_ERR_BADPARAM);

    kaa_event_sync_request_t* request = kaa_create_event_sync_request();
    request->event_listeners_requests = kaa_create_array_event_listeners_request_null_union_null_branch();

    if (self->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_UNSYNCHRONIZED) {
        request->event_sequence_number_request = kaa_create_record_event_sequence_number_request_null_union_event_sequence_number_request_branch();
        request->event_sequence_number_request->data = kaa_create_event_sequence_number_request();
        request->events = kaa_create_array_event_null_union_null_branch();
        self->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS;
        *request_p = request;
        return KAA_ERR_NONE;
    } else {
        request->event_sequence_number_request = kaa_create_record_event_sequence_number_request_null_union_null_branch();
        if (self->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS) {
            request->events = kaa_create_array_event_null_union_null_branch();
            *request_p = request;
            return KAA_ERR_NONE;
        }
    }

    kaa_list_t *events_to_resend_head = self->sent_events;
    self->sent_events = NULL;
    kaa_list_t *events_to_resend = events_to_resend_head;

    kaa_list_t *new_events = self->pending_events;
    self->pending_events = NULL;

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
        self->sent_events = kaa_list_create(sent_events);
    }

    kaa_list_t *new_events_head = new_events;
    while (new_events) {
        kaa_event_t * event_source = (kaa_event_t *)kaa_list_get_data(new_events);

        if (event_source->seq_num == (size_t)-1) {
            event_source->seq_num = ++self->event_sequence_number;
        }

        new_events = kaa_list_next(new_events);
    }

    if (new_events_head == NULL) {
        request->events = kaa_create_array_event_null_union_null_branch();
        request->events->data = NULL;
    } else {
        request->events = kaa_create_array_event_null_union_array_branch();
        request->events->data = new_events_head;
        request->events->destroy = &kaa_event_list_destroy_no_cleanup;
    }

    *request_p = request;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_handle_sync(kaa_event_manager_t *self, size_t request_id, kaa_event_sequence_number_response_t *event_sn_response, kaa_list_t *events)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    if (self->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS
            && event_sn_response != NULL) {
        int32_t server_sn = event_sn_response->seq_num > 0 ? event_sn_response->seq_num : 0;
        kaa_list_t *pending_events = self->pending_events;
        size_t events_count = kaa_list_get_size(pending_events);
        if (self->event_sequence_number - events_count != server_sn) {
            self->event_sequence_number = server_sn;
            while (pending_events) {
                kaa_event_t *event = kaa_list_get_data(pending_events);
                event->seq_num = ++self->event_sequence_number;
                pending_events = kaa_list_next(pending_events);
            }
        }
        self->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED;
    }


    kaa_list_t * sent_events = self->sent_events;
    while (sent_events) {
        sent_events_tuple_t * tuple = (sent_events_tuple_t *)kaa_list_get_data(sent_events);
        if (tuple->request_id == request_id) {
            kaa_list_remove_at(&self->sent_events, sent_events, &destroy_events_tuple);
            break;
        }
        sent_events = kaa_list_next(sent_events);
    }

    kaa_event_t * event = (kaa_event_t *)kaa_list_get_data(events);
    while (event) {
        const char *event_source = (event->source->type == KAA_STRING_NULL_UNION_STRING_BRANCH) ? (const char *)(event->source->data) : NULL;
        kaa_event_callback_t cb = find_event_callback(self->event_callbacks, event->event_class_fqn);
        if (cb) {
            (*cb)((const char *)(event->event_class_fqn), (const char *)(event->event_data->buffer), event->event_data->size, event_source);
        } else if (self->global_event_callback != NULL) {
            (*self->global_event_callback)((const char *)(event->event_class_fqn), (const char *)(event->event_data->buffer), event->event_data->size, event_source);
        }

        events = kaa_list_next(events);
        event = (kaa_event_t *)kaa_list_get_data(events);
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_add_on_event_callback(kaa_event_manager_t *self, const char *fqn, kaa_event_callback_t callback)
{
    KAA_RETURN_IF_NIL2(self, callback, KAA_ERR_BADPARAM);

    if (fqn) {
        event_callback_pair_t * pair = create_event_callback_pair(fqn, callback);
        if (pair == NULL) {
            return KAA_ERR_NOMEM;
        }
        if (self->event_callbacks == NULL) {
            self->event_callbacks = kaa_list_create(pair);
        } else {
            kaa_list_t * head = self->event_callbacks;
            while (head) {
                event_callback_pair_t *data = (event_callback_pair_t *)kaa_list_get_data(head);
                if (strcmp(fqn, data->fqn) == 0) {
                    kaa_list_set_data_at(head, pair, destroy_event_callback_pair);
                    return KAA_ERR_NONE;
                }
                head = kaa_list_next(head);
            }
            kaa_list_push_back(self->event_callbacks, pair);
        }
    } else {
        self->global_event_callback = callback;
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_create_transaction(kaa_event_manager_t *self, kaa_event_block_id *trx_id)
{
    KAA_RETURN_IF_NIL2(self, trx_id, KAA_ERR_NOT_INITIALIZED);

    kaa_event_block_id new_id = ++self->trx_counter;

    if (self->transactions == NULL) {
        self->transactions = kaa_list_create(create_transaction(new_id));
    } else {
        kaa_list_push_back(self->transactions, create_transaction(new_id));
    }

    *trx_id = new_id;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_finish_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);
    if (self->transactions != NULL) {
        trx_search_arg0 = trx_id;
        kaa_list_t * it = kaa_list_find_next(self->transactions, transaction_search_by_id_predicate);
        if (it != NULL) {
            event_transaction_t * trx = kaa_list_get_data(it);
            bool need_sync = false;
            if (trx->events != NULL && kaa_list_get_size(trx->events) > 0) {
                kaa_lists_merge(self->pending_events, trx->events);
                need_sync = true;
                trx->events = NULL;
            }
            kaa_list_remove_at(&self->transactions, it, &destroy_transaction);
            kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(self->channel_manager, event_sync_services[0]);
            if (need_sync && sync) {
                (*sync)(event_sync_services, 1);
            }
            return KAA_ERR_NONE;
        }
    }
    return KAA_ERR_NOT_FOUND;
}

kaa_error_t kaa_event_remove_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);
    if (self->transactions != NULL) {
        trx_search_arg0 = trx_id;
        kaa_list_t * it = kaa_list_find_next(self->transactions, transaction_search_by_id_predicate);
        if (it != NULL) {
            kaa_list_remove_at(&self->transactions, it, &destroy_transaction);
            return KAA_ERR_NONE;
        }
    }
    return KAA_ERR_NOT_FOUND;
}

kaa_error_t kaa_add_event_to_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id, const char * fqn, size_t fqn_length, const char * event_data, size_t event_data_size, const char * target, size_t target_size)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);
    if (fqn == NULL || fqn_length == 0) {
        return KAA_ERR_EVENT_BAD_FQN;
    }

    if (self->transactions != NULL) {
        trx_search_arg0 = trx_id;
        kaa_list_t * it = kaa_list_find_next(self->transactions, transaction_search_by_id_predicate);
        if (it != NULL) {
            event_transaction_t * trx = kaa_list_get_data(it);
            kaa_event_t * event = kaa_create_event();

            if (!event) {
                return KAA_ERR_NOMEM;
            }

            kaa_error_t error_code = kaa_fill_event_structure(event, (size_t)-1, fqn, event_data, event_data_size, target, target_size);
            if (error_code) {
                KAA_FREE(event);
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

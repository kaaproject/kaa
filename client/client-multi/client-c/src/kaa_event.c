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

#include "collections/kaa_list.h"
#include "kaa_status.h"
#include "kaa_channel_manager.h"
#include "kaa_mem.h"
#include "kaa_log.h"
#include "gen/kaa_endpoint_gen.h"

extern kaa_sync_handler_fn kaa_channel_manager_get_sync_handler(kaa_channel_manager_t *self
                                                              , kaa_service_t service_type);

static void kaa_event_list_destroy_no_cleanup(void *data)
{
    kaa_data_destroy(data);
}

static void destroy_event(void *event_t_ptr)
{
    kaa_event_t *record = (kaa_event_t *) event_t_ptr;
    record->destroy(record);
}

static kaa_service_t event_sync_services[1] = {KAA_SERVICE_EVENT};

typedef struct sent_events_tuple_t {
    size_t request_id;
    kaa_list_t *sent_events;
} sent_events_tuple_t;

typedef struct event_callback_pair_t_ {
    char *fqn;
    kaa_event_callback_t cb;
} event_callback_pair_t;

static event_callback_pair_t *create_event_callback_pair(const char *fqn
                                                        , kaa_event_callback_t callback)
{
    event_callback_pair_t *pair = (event_callback_pair_t *) KAA_MALLOC(sizeof(event_callback_pair_t));
    KAA_RETURN_IF_NIL(pair, NULL);

    size_t fqn_length = strlen(fqn);
    pair->fqn = (char *) KAA_MALLOC((fqn_length + 1) * sizeof(char));
    if (!pair->fqn) {
        KAA_FREE(pair);
        return NULL;
    }
    strcpy(pair->fqn, fqn);
    pair->cb = callback;
    return pair;
}

static void destroy_event_callback_pair(void *pair_p)
{
    event_callback_pair_t *pair = (event_callback_pair_t *) pair_p;
    KAA_FREE(pair->fqn);
    KAA_FREE(pair);
}

static kaa_event_callback_t find_event_callback(kaa_list_t *head, const char *fqn)
{
    while (head) {
        event_callback_pair_t *pair = (event_callback_pair_t *) kaa_list_get_data(head);
        if (strcmp(fqn, pair->fqn) == 0)
            return pair->cb;
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
    kaa_list_t             *events;
} event_transaction_t;

static event_transaction_t *create_transaction(kaa_event_block_id id)
{
    event_transaction_t *trx = (event_transaction_t *) KAA_MALLOC(sizeof(event_transaction_t));
    trx->id = id;
    trx->events = NULL;
    return trx;
}

static void destroy_transaction(void *trx_p)
{
    event_transaction_t *trx = (event_transaction_t *) trx_p;
    kaa_list_destroy(trx->events, &destroy_event);
    KAA_FREE(trx);
}

static bool transaction_search_by_id_predicate(void *trx_p, void *context)
{
    event_transaction_t *trx = (event_transaction_t *) trx_p;
    kaa_event_block_id *matcher = (kaa_event_block_id *) context;
    return (matcher && trx) ? ((*matcher) == trx->id) : false;
}

/* Public stuff */
struct kaa_event_manager_t {
    sent_events_tuple_t         events_awaiting_response;
    kaa_list_t                 *pending_events;
    kaa_list_t                 *event_callbacks;
    kaa_list_t                 *transactions;
    kaa_event_block_id          trx_counter;
    kaa_event_callback_t        global_event_callback;
    size_t                      event_sequence_number;
    kaa_event_sequence_number_status_t sequence_number_status;

    kaa_status_t                *status;
    kaa_channel_manager_t       *channel_manager;
    kaa_logger_t                *logger;
};

kaa_error_t kaa_event_manager_create(kaa_event_manager_t **event_manager_p
                                   , kaa_status_t *status
                                   , kaa_channel_manager_t *channel_manager
                                   , kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL(event_manager_p, KAA_ERR_BADPARAM)

    *event_manager_p = (kaa_event_manager_t *) KAA_MALLOC(sizeof(kaa_event_manager_t));
    KAA_RETURN_IF_NIL(*event_manager_p, KAA_ERR_NOMEM);

    (*event_manager_p)->pending_events = NULL;
    (*event_manager_p)->events_awaiting_response.sent_events = NULL;
    (*event_manager_p)->events_awaiting_response.request_id =  (size_t) -1;
    (*event_manager_p)->event_callbacks = NULL;
    (*event_manager_p)->transactions = NULL;
    (*event_manager_p)->trx_counter = 0;
    (*event_manager_p)->global_event_callback = NULL;
    (*event_manager_p)->event_sequence_number = 0;

    if (kaa_status_get_event_sequence_number(status, (uint32_t *) &(*event_manager_p)->event_sequence_number)) {
        KAA_FREE(*event_manager_p);
        return KAA_ERR_BAD_STATE;
    }

    (*event_manager_p)->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_UNSYNCHRONIZED;

    (*event_manager_p)->status = status;
    (*event_manager_p)->channel_manager = channel_manager;
    (*event_manager_p)->logger = logger;
    return KAA_ERR_NONE;
}

void kaa_event_manager_destroy(kaa_event_manager_t *self)
{
    if (self) {
        kaa_list_destroy(self->events_awaiting_response.sent_events, &destroy_event);
        kaa_list_destroy(self->pending_events, &destroy_event);
        kaa_list_destroy(self->event_callbacks, &destroy_event_callback_pair);
        kaa_list_destroy(self->transactions, &destroy_transaction);
        KAA_FREE(self);
    }
}

static kaa_error_t kaa_fill_event_structure(kaa_event_t *event
                                          , size_t sequence_number
                                          , const char *fqn
                                          , const char *event_data
                                          , size_t event_data_size
                                          , const char *target
                                          , size_t target_size)
{
    event->seq_num = sequence_number;
    event->source = kaa_union_string_or_null_branch_1_create();
    KAA_RETURN_IF_NIL(event->source, KAA_ERR_NOMEM);

    event->source->data = NULL;

    event->event_class_fqn = kaa_string_copy_create(fqn, &kaa_data_destroy);
    if (!event->event_class_fqn) {
        event->source->destroy(event->source);
        return KAA_ERR_NOMEM;
    }

    event->event_data = kaa_bytes_copy_create((const uint8_t *) event_data, event_data_size, &kaa_data_destroy);
    if (!event->event_data) {
        kaa_string_destroy(event->event_class_fqn);
        event->source->destroy(event->source);
        return KAA_ERR_NOMEM;
    }

    bool is_target_specified = (target && target_size > 0);
    if (is_target_specified) {
        event->target = kaa_union_string_or_null_branch_0_create();
        if (event->target) {
            event->target->data = kaa_string_copy_create(target, &kaa_data_destroy);
        }
    } else {
        event->target = kaa_union_string_or_null_branch_1_create();
    }

    if (!event->target || (is_target_specified && !event->target->data)) {
        if (event->target) {
            event->target->destroy(event->target);
        }
        kaa_bytes_destroy(event->event_data);
        kaa_string_destroy(event->event_class_fqn);
        event->source->destroy(event->source);
        return KAA_ERR_NOMEM;
    }

    return KAA_ERR_NONE;
}


kaa_error_t kaa_add_event(kaa_event_manager_t *self
                        , const char *fqn
                        , const char *event_data
                        , size_t event_data_size
                        , const char *target
                        , size_t target_size)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);
    KAA_RETURN_IF_NIL(fqn, KAA_ERR_EVENT_BAD_FQN);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Adding new event %s", fqn);

    kaa_event_t *event = kaa_event_create();
    KAA_RETURN_IF_NIL(event, KAA_ERR_NOMEM);

    size_t new_sequence_number = (self->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED ?
                                        ++self->event_sequence_number :
                                        (size_t) -1);

    kaa_error_t error_code = kaa_fill_event_structure(event
                                                    , new_sequence_number
                                                    , fqn, event_data
                                                    , event_data_size
                                                    , target
                                                    , target_size);
    if (error_code) {
        event->destroy(event);
        return KAA_ERR_NOMEM;
    }

    if (self->pending_events) {
        if (!kaa_list_push_back(self->pending_events, event)) {
            event->destroy(event);
            return KAA_ERR_NOMEM;
        }
    } else {
        self->pending_events = kaa_list_create(event);
        if (!self->pending_events) {
            event->destroy(event);
            return KAA_ERR_NOMEM;
        }
    }
    kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(self->channel_manager, event_sync_services[0]);
    if (sync)
        (*sync)(event_sync_services, 1);

    return KAA_ERR_NONE;
}

static kaa_error_t kaa_event_sync_request_allocate(
          kaa_event_sync_request_t **request_p
        , bool is_sequence_number_request
        , bool is_events_request
        )
{
    (*request_p) = kaa_event_sync_request_create();
    KAA_RETURN_IF_NIL((*request_p), KAA_ERR_NOMEM);

    (*request_p)->event_listeners_requests = kaa_union_array_event_listeners_request_or_null_branch_1_create();
    if (!(*request_p)->event_listeners_requests) {
        (*request_p)->destroy(*request_p);
        *request_p = NULL;
        return KAA_ERR_NOMEM;
    }

    if (is_sequence_number_request) {
        (*request_p)->events = kaa_union_array_event_or_null_branch_1_create();
        if (!(*request_p)->events) {
            (*request_p)->destroy(*request_p);
            *request_p = NULL;
            return KAA_ERR_NOMEM;
        }

        (*request_p)->event_sequence_number_request =
                kaa_union_event_sequence_number_request_or_null_branch_0_create();
        if (!(*request_p)->event_sequence_number_request) {
            (*request_p)->destroy(*request_p);
            *request_p = NULL;
            return KAA_ERR_NOMEM;
        }

    } else if (is_events_request) {
        (*request_p)->event_sequence_number_request = kaa_union_event_sequence_number_request_or_null_branch_1_create();
        if (!(*request_p)->event_sequence_number_request) {
            (*request_p)->destroy(*request_p);
            *request_p = NULL;
            return KAA_ERR_NOMEM;
        }

        (*request_p)->events = kaa_union_array_event_or_null_branch_0_create();
        if (!(*request_p)->events) {
            (*request_p)->destroy(*request_p);
            *request_p = NULL;
            return KAA_ERR_NOMEM;
        }

    } else {
        (*request_p)->events = kaa_union_array_event_or_null_branch_1_create();
        if (!(*request_p)->events) {
            (*request_p)->destroy(*request_p);
            *request_p = NULL;
            return KAA_ERR_NOMEM;
        }

        (*request_p)->event_sequence_number_request = kaa_union_event_sequence_number_request_or_null_branch_1_create();
        if (!(*request_p)->event_sequence_number_request) {
            (*request_p)->destroy(*request_p);
            *request_p = NULL;
            return KAA_ERR_NOMEM;
        }

    }
    return KAA_ERR_NONE;
}


kaa_error_t kaa_event_compile_request(kaa_event_manager_t *self
                                    , kaa_event_sync_request_t **request_p
                                    , size_t requestId)
{
    KAA_RETURN_IF_NIL2(self, request_p, KAA_ERR_BADPARAM);
    kaa_error_t error_code = KAA_ERR_NONE;

    KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Going to compile event sync request (%zu)", requestId);

    if (kaa_get_max_log_level(self->logger) >= KAA_LOG_LEVEL_TRACE) {
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Event sequence number synchronized: %s"
                                              ", event sequence number sync in progress: %s"
            , (self->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED ? "true" : "false")
            , (self->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS ? "true" : "false")
            );
        size_t events_size = kaa_list_get_size(self->pending_events);
        size_t sent_events_size = kaa_list_get_size(self->events_awaiting_response.sent_events);
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Have %zu new events"
                                              ", and %zu events sent with request id %zu"
            , (events_size == (size_t) -1 ? 0 : events_size)
            , (sent_events_size == (size_t) -1 ? 0 : sent_events_size)
            , (self->events_awaiting_response.request_id == (size_t) -1 ? 0 : self->events_awaiting_response.request_id)
            );
    }

    if (self->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_UNSYNCHRONIZED) {
        error_code = kaa_event_sync_request_allocate(request_p, true, false);
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to allocate memory for event sync request.");
            return error_code;
        }
        self->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS;
        return error_code;
    } else if (self->sequence_number_status != KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS
            && (self->events_awaiting_response.sent_events || self->pending_events)) {

        error_code = kaa_event_sync_request_allocate(request_p, false, true);
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to allocate memory for event sync request.");
            return error_code;
        }

        kaa_list_t *new_events = self->pending_events;
        self->pending_events = NULL;

        if (self->events_awaiting_response.sent_events) {
            new_events = kaa_lists_merge(new_events, self->events_awaiting_response.sent_events);
        }
        self->events_awaiting_response.request_id = requestId;
        self->events_awaiting_response.sent_events = new_events;

        kaa_list_t *new_events_head = new_events;
        while (new_events) {
            kaa_event_t *event_source = (kaa_event_t *) kaa_list_get_data(new_events);

            if (event_source->seq_num == (size_t) -1) {
                event_source->seq_num = ++self->event_sequence_number;
            }

            new_events = kaa_list_next(new_events);
        }

        (*request_p)->events->data = new_events_head;
        (*request_p)->events->destroy = &kaa_event_list_destroy_no_cleanup;
    } else {
        KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Still synchronizing event sequence number or there are no events to send.");
        error_code = kaa_event_sync_request_allocate(request_p, false, false);
        if (error_code)
            KAA_LOG_ERROR(self->logger, error_code, "Failed to allocate memory for event sync request.");
    }
    return error_code;
}

kaa_error_t kaa_event_handle_sync(kaa_event_manager_t *self
                                , size_t request_id
                                , kaa_event_sequence_number_response_t *event_sn_response
                                , kaa_list_t *events)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Processing event sync response (%zu)", request_id);

    if (self->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS && event_sn_response) {
        int32_t server_sn = event_sn_response->seq_num > 0 ? event_sn_response->seq_num : 0;

        if (self->event_sequence_number != server_sn) {
            self->event_sequence_number = server_sn;
            kaa_list_t *pending_events = self->pending_events;
            while (pending_events) {
                kaa_event_t *event = (kaa_event_t *) kaa_list_get_data(pending_events);
                event->seq_num = ++self->event_sequence_number;
                pending_events = kaa_list_next(pending_events);
            }
        }
        self->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED;
    }

    if (request_id == self->events_awaiting_response.request_id) {
        kaa_list_destroy(self->events_awaiting_response.sent_events, &destroy_event);
        self->events_awaiting_response.sent_events = NULL;
        self->events_awaiting_response.request_id = (size_t) -1;
    }

    kaa_event_t *event = (kaa_event_t *) kaa_list_get_data(events);
    while (event) {
        const char *event_source = (event->source->type == KAA_UNION_STRING_OR_NULL_BRANCH_0) ?
                                       (const char *) (event->source->data) :
                                       NULL;

        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received event with fqn \"%s\" from \"%s\""
                                                , event->event_class_fqn->data, event_source);

        kaa_event_callback_t cb = find_event_callback(self->event_callbacks, event->event_class_fqn->data);
        if (cb) {
            (*cb)(event->event_class_fqn->data, (const char *) (event->event_data->buffer), event->event_data->size, event_source);
        } else if (self->global_event_callback) {
            (*self->global_event_callback)(event->event_class_fqn->data,
                    (const char *) (event->event_data->buffer), event->event_data->size, event_source);
        }
        events = kaa_list_next(events);
        event = (kaa_event_t *) kaa_list_get_data(events);
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_add_on_event_callback(kaa_event_manager_t *self, const char *fqn, kaa_event_callback_t callback)
{
    KAA_RETURN_IF_NIL2(self, callback, KAA_ERR_BADPARAM);
    if (fqn) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Adding callback for events with fqn \"%s\"", fqn);
        event_callback_pair_t *pair = create_event_callback_pair(fqn, callback);
        KAA_RETURN_IF_NIL(pair, KAA_ERR_NOMEM);
        if (!self->event_callbacks) {
            self->event_callbacks = kaa_list_create(pair);
            if (!self->event_callbacks) {
                destroy_event_callback_pair(pair);
                return KAA_ERR_NOMEM;
            }
        } else {
            kaa_list_t *head = self->event_callbacks;
            while (head) {
                event_callback_pair_t *data = (event_callback_pair_t *) kaa_list_get_data(head);
                if (strcmp(fqn, data->fqn) == 0) {
                    kaa_list_set_data_at(head, pair, &destroy_event_callback_pair);
                    return KAA_ERR_NONE;
                }
                head = kaa_list_next(head);
            }
            if (!kaa_list_push_back(self->event_callbacks, pair)) {
                destroy_event_callback_pair(pair);
                return KAA_ERR_NOMEM;
            }
        }
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Adding global event callback");
        self->global_event_callback = callback;
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_create_transaction(kaa_event_manager_t *self, kaa_event_block_id *trx_id)
{
    KAA_RETURN_IF_NIL2(self, trx_id, KAA_ERR_NOT_INITIALIZED);

    kaa_event_block_id new_id = ++self->trx_counter;

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Creating new events batch with id %zu", new_id);

    event_transaction_t *new_transaction = create_transaction(new_id);
    if (!new_transaction) {
        --self->trx_counter;
        return KAA_ERR_NOMEM;
    }

    if (!self->transactions) {
        self->transactions = kaa_list_create(new_transaction);
        if (!self->transactions) {
            destroy_transaction(new_transaction);
            --self->trx_counter;
            return KAA_ERR_NOMEM;
        }
    } else {
        if (!kaa_list_push_back(self->transactions, new_transaction)) {
            destroy_transaction(new_transaction);
            --self->trx_counter;
            return KAA_ERR_NOMEM;
        }
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Created new events batch with id %zu", new_id);

    *trx_id = new_id;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_finish_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to send events from event batch with id %zu", trx_id);

    if (self->transactions) {
        kaa_list_t *it = kaa_list_find_next(self->transactions, &transaction_search_by_id_predicate, &trx_id);
        if (it) {
            event_transaction_t *trx = kaa_list_get_data(it);
            bool need_sync = false;
            if (kaa_get_max_log_level(self->logger) >= KAA_LOG_LEVEL_TRACE) {
                size_t events_count = kaa_list_get_size(trx->events);
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Events batch with id %zu has %zu events", trx_id, events_count);
            }
            if (trx->events && kaa_list_get_size(trx->events) > 0) {
                kaa_lists_merge(self->pending_events, trx->events);
                need_sync = true;
                trx->events = NULL;
            }
            kaa_list_remove_at(&self->transactions, it, &destroy_transaction);
            kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(self->channel_manager, event_sync_services[0]);
            if (need_sync && sync)
                (*sync)(event_sync_services, 1);

            return KAA_ERR_NONE;
        }
    }

    KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Events batch with id %zu was not created before", trx_id);

    return KAA_ERR_NOT_FOUND;
}

kaa_error_t kaa_event_remove_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to remove events batch with id %zu", trx_id);

    if (self->transactions) {
        kaa_list_t *it = kaa_list_find_next(self->transactions, &transaction_search_by_id_predicate, &trx_id);
        if (it) {
            kaa_list_remove_at(&self->transactions, it, &destroy_transaction);
            return KAA_ERR_NONE;
        }
    }

    KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Events batch with id %zu was not created before", trx_id);

    return KAA_ERR_NOT_FOUND;
}

kaa_error_t kaa_add_event_to_transaction(kaa_event_manager_t *self
                                       , kaa_event_block_id trx_id
                                       , const char *fqn
                                       , size_t fqn_length
                                       , const char *event_data
                                       , size_t event_data_size
                                       , const char *target
                                       , size_t target_size)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to add event to events batch with id %zu", trx_id);

    KAA_RETURN_IF_NIL2(fqn, fqn_length, KAA_ERR_EVENT_BAD_FQN);

    if (self->transactions) {
        kaa_list_t *it = kaa_list_find_next(self->transactions, &transaction_search_by_id_predicate, &trx_id);
        if (it) {
            event_transaction_t *trx = kaa_list_get_data(it);
            kaa_event_t *event = kaa_event_create();
            KAA_RETURN_IF_NIL(event, KAA_ERR_NOMEM);

            kaa_error_t error_code = kaa_fill_event_structure(
                    event, (size_t)-1, fqn, event_data, event_data_size, target, target_size);
            if (error_code) {
                event->destroy(event);
                return error_code;
            }
            if (!trx->events) {
                trx->events = kaa_list_create(event);
                if (!trx->events) {
                    event->destroy(event);
                    return KAA_ERR_NOMEM;
                }
            } else if (!kaa_list_push_back(trx->events, event)) {
                event->destroy(event);
                return KAA_ERR_NOMEM;
            }
            return KAA_ERR_NONE;
        }
    }

    KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Can not add event to events batch with id %zu.", trx_id);

    return KAA_ERR_EVENT_TRX_NOT_FOUND;
}

typedef struct event_class_family_t {
    char       *ecf_name;
    size_t      supported_incoming_fqns_count;
    char      **supported_incoming_fqns;
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

const char *kaa_find_class_family_name(const char *fqn)
{
    size_t i = 0;
    while (SUPPORTED_EVENT_CLASS_FAMILIES_SIZE - i++) {
        size_t fqn_count = SUPPORTED_EVENT_CLASS_FAMILIES[SUPPORTED_EVENT_CLASS_FAMILIES_SIZE - i].supported_incoming_fqns_count;
        char **fqns = SUPPORTED_EVENT_CLASS_FAMILIES[SUPPORTED_EVENT_CLASS_FAMILIES_SIZE - i].supported_incoming_fqns;
        while (fqn_count--) {
            if (strcmp(fqn, fqns[fqn_count]) == 0) {
                return SUPPORTED_EVENT_CLASS_FAMILIES[SUPPORTED_EVENT_CLASS_FAMILIES_SIZE - i].ecf_name;
            }
        }
    }
    return NULL;
}

#endif

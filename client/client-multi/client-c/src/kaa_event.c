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

# include "kaa_event.h"

# ifndef KAA_DISABLE_FEATURE_EVENTS
# include <stdbool.h>
# include <stddef.h>
# include <stdint.h>
# include <string.h>

# include "collections/kaa_list.h"
# include "kaa_status.h"
# include "kaa_channel_manager.h"
# include "utilities/kaa_mem.h"
# include "utilities/kaa_log.h"
# include "gen/kaa_endpoint_gen.h"
# include "kaa_platform_utils.h"
# include "kaa_platform_common.h"

# define KAA_ENDPOINT_ID_LENGTH      20

# define KAA_EVENT_CLIENT_SYNC_EXTENSION_FLAG_RECEIVE_EVENTS         0x1
# define KAA_EVENT_CLIENT_SYNC_EXTENSION_FLAG_SEQUENCE_NUMBER_SYNC   0x2

# define KAA_EVENT_SERVER_SYNC_EXTENSION_FLAG_SEQUENCE_NUMBER_PRESENT   0x1
# define KAA_EVENT_OPTION_TARGET_ID_PRESENT              0x1

// TODO: Unsupported yet.
// static const uint8_t KAA_EVENT_FIELD_EVENT_LISTENERS_LIST       = 0;
static const uint8_t KAA_EVENT_FIELD_ID_EVENT_LIST                 = 1;



extern kaa_sync_handler_fn kaa_channel_manager_get_sync_handler(kaa_channel_manager_t *self
                                                              , kaa_service_t service_type);

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
                                          , const char *target)
{
    event->seq_num = sequence_number;
    event->source = kaa_union_string_or_null_branch_1_create();
    KAA_RETURN_IF_NIL(event->source, KAA_ERR_NOMEM);

    event->source->data = NULL;

    event->event_class_fqn = kaa_string_copy_create(fqn, &kaa_data_destroy);
    if (!event->event_class_fqn) {
        event->source->destroy(event->source);
        event->source = NULL;
        return KAA_ERR_NOMEM;
    }

    event->event_data = kaa_bytes_copy_create((const uint8_t *) event_data, event_data_size, &kaa_data_destroy);
    if (!event->event_data && event_data_size > 0) {
        kaa_string_destroy(event->event_class_fqn);
        event->event_class_fqn = NULL;
        event->source->destroy(event->source);
        event->source = NULL;
        return KAA_ERR_NOMEM;
    }

    if (target) {
        event->target = kaa_union_string_or_null_branch_0_create();
        if (event->target) {
            event->target->data = kaa_string_copy_create(target, &kaa_data_destroy);
        }
    } else {
        event->target = kaa_union_string_or_null_branch_1_create();
    }

    if (!event->target || (target && !event->target->data)) {
        if (event->target) {
            event->target->destroy(event->target);
            event->target = NULL;
        }
        kaa_bytes_destroy(event->event_data);
        event->event_data = NULL;
        kaa_string_destroy(event->event_class_fqn);
        event->event_class_fqn = NULL;
        event->source->destroy(event->source);
        event->source = NULL;
        return KAA_ERR_NOMEM;
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_manager_send_event(kaa_event_manager_t *self
                        , const char *fqn
                        , const char *event_data
                        , size_t event_data_size
                        , const char *target)
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
                                                    , target);
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



static size_t kaa_event_list_get_request_size(kaa_list_t *events)
{
    size_t expected_size = 0;
    kaa_event_t *event = (kaa_event_t *)kaa_list_get_data(events);
    while (event) {
        expected_size  += sizeof(uint32_t) /*Event sequence number*/
                        + sizeof(uint16_t) /*Event options*/
                        + sizeof(uint16_t) /*Event class FQN length */
                        + sizeof(uint32_t) /*Event data size*/;

        if (event->target->data /*have Target Endpoint ID*/) {
            expected_size += 5 * sizeof(uint32_t); /*Target Endpoint ID*/
        }

        expected_size += kaa_aligned_size_get(strlen(event->event_class_fqn->data)); /*Event class FQN + padding */
        expected_size += kaa_aligned_size_get(event->event_data->size);/*Event data + padding*/

        events = kaa_list_next(events);
        event = (kaa_event_t *) kaa_list_get_data(events);
    }
    return expected_size;
}



static kaa_error_t kaa_event_request_get_size_no_header(kaa_event_manager_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);

    *expected_size = 0;
    if (self->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED) {
        kaa_list_t *pending_events = self->pending_events;
        kaa_list_t *resending_events = self->events_awaiting_response.sent_events;
        bool have_events = (kaa_list_get_size(pending_events) > 0) || (kaa_list_get_size(resending_events) > 0);
        if (have_events) {
            *expected_size += sizeof(uint8_t) /*field id(1)*/
                            + sizeof(uint8_t) /*reserved*/
                            + sizeof(uint16_t) /* events count */;
            *expected_size += kaa_event_list_get_request_size(pending_events);
            *expected_size += kaa_event_list_get_request_size(resending_events);
        }
    }
    return KAA_ERR_NONE;
}


kaa_error_t kaa_event_request_get_size(kaa_event_manager_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);

    kaa_error_t error_code = kaa_event_request_get_size_no_header(self, expected_size);
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to get event extension length");
        *expected_size = 0;
        return error_code;
    }
    *expected_size += KAA_EXTENSION_HEADER_SIZE;
    return KAA_ERR_NONE;
}



static kaa_error_t kaa_event_list_serialize(kaa_event_manager_t *self, kaa_list_t *events, kaa_platform_message_writer_t *writer)
{
    kaa_event_t * event = (kaa_event_t *) kaa_list_get_data(events);
    kaa_error_t error_code = KAA_ERR_NONE;
    while (event) {
        if (event->seq_num == -1) {
            event->seq_num = ++self->event_sequence_number;
        }

        error_code = kaa_platform_message_write(writer, &event->seq_num, sizeof(uint32_t));
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to write event sequence number");
            return error_code;
        }
        uint32_t event_options = (event->target->data == NULL ? 0 : (KAA_EVENT_OPTION_TARGET_ID_PRESENT << 16) & 0xffff0000);

        // TODO: store event class fqn length in a separate field to avoid calling strlen(...) too often
        size_t event_class_fqn_size = strlen(event->event_class_fqn->data);
        event_options |= event_class_fqn_size & 0xffff;

        error_code = kaa_platform_message_write(writer, &event_options, sizeof(uint32_t));
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to write event options and event class fqn length");
            return error_code;
        }

        error_code = kaa_platform_message_write(writer, &event->event_data->size, sizeof(uint32_t));
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to write event data size");
            return error_code;
        }

        if (event->target->data != NULL) {
            error_code = kaa_platform_message_write(writer, ((kaa_string_t *)event->target->data)->data, KAA_ENDPOINT_ID_LENGTH * sizeof(uint8_t));
            if (error_code) {
                KAA_LOG_ERROR(self->logger, error_code, "Failed to write event target id");
                return error_code;
            }
        }

        size_t real_len = event_class_fqn_size * sizeof(uint8_t);
        error_code = kaa_platform_message_write_aligned(writer, event->event_class_fqn->data, real_len);
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to write event class fqn aligned");
            return error_code;
        }

        real_len = ((size_t)event->event_data->size) * sizeof(uint8_t);
        error_code = kaa_platform_message_write_aligned(writer, event->event_data->buffer, real_len);
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to write event data aligned");
            return error_code;
        }

        events = kaa_list_next(events);
        event = (kaa_event_t *) kaa_list_get_data(events);
    }
    return KAA_ERR_NONE;
}



kaa_error_t kaa_event_request_serialize(kaa_event_manager_t *self, size_t request_id, kaa_platform_message_writer_t *writer)
{
    KAA_RETURN_IF_NIL2(self, writer, KAA_ERR_BADPARAM);

    /* write extension header */
    uint32_t extension_options = 0;
    if (self->sequence_number_status != KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED) {
        extension_options |= KAA_EVENT_CLIENT_SYNC_EXTENSION_FLAG_SEQUENCE_NUMBER_SYNC;
    } else {
        extension_options |= KAA_EVENT_CLIENT_SYNC_EXTENSION_FLAG_RECEIVE_EVENTS;
    }

    size_t payload_size = 0;
    kaa_error_t error_code = kaa_event_request_get_size_no_header(self, &payload_size);
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to calculate event extension length");
        return error_code;
    }

    error_code = kaa_platform_message_extension_header_write(writer, KAA_EVENT_EXTENSION_TYPE, extension_options, payload_size);
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to write event extension header (ext type %u, options %X, payload size %u)"
                                        , KAA_EVENT_EXTENSION_TYPE, extension_options, payload_size);
        return error_code;
    }

    /* write events */
    if (payload_size) {
        size_t events_count = 0;
        kaa_list_t *pending_events = self->pending_events;
        kaa_list_t *resending_events = self->events_awaiting_response.sent_events;

        ssize_t pending_events_count = kaa_list_get_size(pending_events);
        ssize_t sent_events_count = kaa_list_get_size(resending_events);

        events_count +=  pending_events_count > 0 ? pending_events_count : 0;
        events_count +=  sent_events_count > 0 ? sent_events_count : 0;

        if (events_count) {
            uint32_t events_list_field_header = (KAA_EVENT_FIELD_ID_EVENT_LIST << 24);
            events_list_field_header |= (events_count & 0xFFFF);

            error_code = kaa_platform_message_write(writer, &events_list_field_header, sizeof(uint32_t));
            if (error_code) {
                KAA_LOG_ERROR(self->logger, error_code, "Failed to write event list command field and events count");
                return error_code;
            }

            error_code = kaa_event_list_serialize(self, resending_events, writer);
            if (error_code) {
                KAA_LOG_ERROR(self->logger, error_code, "Failed to write events");
                return error_code;
            }

            error_code = kaa_event_list_serialize(self, pending_events, writer);
            if (error_code) {
                KAA_LOG_ERROR(self->logger, error_code, "Failed to write events");
                return error_code;
            }

            self->events_awaiting_response.request_id = request_id;
            self->events_awaiting_response.sent_events = kaa_lists_merge(self->events_awaiting_response.sent_events, self->pending_events);
            self->pending_events = NULL;
        }
    }
    return KAA_ERR_NONE;
}



static kaa_error_t kaa_event_read_event(kaa_event_manager_t *self, kaa_platform_message_reader_t *reader)
{
    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);

    uint32_t event_sequence_number = 0;
    kaa_error_t error_code = kaa_platform_message_read(reader, &event_sequence_number, sizeof(uint32_t));
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to read event sequence number field");
        return error_code;
    }

    uint16_t event_options = 0;
    error_code = kaa_platform_message_read(reader, &event_options, sizeof(uint16_t));
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to read event options field");
        return error_code;
    }

    uint16_t event_class_fqn_length = 0;
    error_code = kaa_platform_message_read(reader, &event_class_fqn_length, sizeof(uint16_t));
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to read event class fqn length field");
        return error_code;
    }

    uint32_t event_data_size = 0;
    error_code = kaa_platform_message_read(reader, &event_data_size, sizeof(uint32_t));
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to read event data size field");
        return error_code;
    }

    char event_source[KAA_ENDPOINT_ID_LENGTH];
    error_code = kaa_platform_message_read(reader, event_source, KAA_ENDPOINT_ID_LENGTH * sizeof(uint8_t));
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to read event source endpoint id field");
        return error_code;
    }

    bool is_enough = kaa_platform_message_is_buffer_large_enough(reader, kaa_aligned_size_get(event_class_fqn_length));
    if (!is_enough) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_READ_FAILED, "Buffer size is less than event class fqn length value");
        return KAA_ERR_READ_FAILED;
    }
    char event_fqn[event_class_fqn_length + 1];
    error_code = kaa_platform_message_read_aligned(reader, event_fqn, event_class_fqn_length * sizeof(uint8_t));
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to read event class fqn field");
        return error_code;
    }
    event_fqn[event_class_fqn_length] = '\0';

    is_enough = kaa_platform_message_is_buffer_large_enough(reader, kaa_aligned_size_get(event_data_size));
    if (!is_enough) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_READ_FAILED, "Buffer size is less than event data size value");
        return KAA_ERR_READ_FAILED;
    }
    char event_data[event_data_size];
    error_code = kaa_platform_message_read_aligned(reader, event_data, event_data_size * sizeof(uint8_t));
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to read event data field");
        return error_code;
    }

    kaa_event_callback_t cb = find_event_callback(self->event_callbacks, event_fqn);
    if (cb) {
       (*cb)(event_fqn, event_data, event_data_size, event_source);
    } else if (self->global_event_callback) {
       (*self->global_event_callback)(event_fqn, event_data, event_data_size, event_source);
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_handle_server_sync(kaa_event_manager_t *self, kaa_platform_message_reader_t *reader, uint32_t extension_options, size_t extension_length, size_t request_id)
{
    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);

    if (extension_options & KAA_EVENT_SERVER_SYNC_EXTENSION_FLAG_SEQUENCE_NUMBER_PRESENT) {
        uint32_t event_sequence_number = 0;
        kaa_error_t error_code = kaa_platform_message_read(reader, &event_sequence_number, sizeof(uint32_t));
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to read event_sequence number field");
            return error_code;
        }
        extension_length -= sizeof(uint32_t);

        bool have_pending_events = kaa_list_get_size(self->pending_events) > 0;

        if (self->event_sequence_number != KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED) {
            if (self->event_sequence_number != event_sequence_number) {
                KAA_LOG_WARN(self->logger, KAA_ERR_BAD_STATE, "Stored event sequence number is not correct (stored %u, received %u).", self->event_sequence_number, event_sequence_number);
                self->event_sequence_number = event_sequence_number;
                kaa_list_t *events = self->pending_events;
                kaa_event_t *event = (kaa_event_t *)kaa_list_get_data(events);
                while (event) {
                    event->seq_num = ++self->event_sequence_number;
                    events = kaa_list_next(events);
                    event = (kaa_event_t *)kaa_list_get_data(events);
                }
            }
        }
        if (have_pending_events) {
            kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(self->channel_manager, event_sync_services[0]);
            if (sync)
                (*sync)(event_sync_services, 1);
        }
    }

    if (request_id == self->events_awaiting_response.request_id) {
        kaa_list_destroy(self->events_awaiting_response.sent_events, &destroy_event);
        self->events_awaiting_response.sent_events = NULL;
        self->events_awaiting_response.request_id = (size_t) -1;
    }

    if (extension_length > 0) {
        uint8_t field_id = 0;
        kaa_error_t error_code = kaa_platform_message_read(reader, &field_id, sizeof(uint8_t)); //read field id
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to read field id in Event server sync message");
            return error_code;
        }

        error_code = kaa_platform_message_skip(reader, sizeof(uint8_t)); // skip reserved
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to read field id in Event server sync message");
            return error_code;
        }

        uint16_t events_count = 0;
        error_code = kaa_platform_message_read(reader, &events_count, sizeof(uint16_t)); // read events count
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to read field id in Event server sync message");
            return error_code;
        }

        for (;events_count--;) {
            error_code = kaa_event_read_event(self, reader);
            if (error_code) {
                KAA_LOG_ERROR(self->logger, error_code, "Failed to read event from server sync");
                return error_code;
            }
        }

    }

    return KAA_ERR_NONE;
}



kaa_error_t kaa_event_manager_add_on_event_callback(kaa_event_manager_t *self, const char *fqn, kaa_event_callback_t callback)
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

kaa_error_t kaa_event_manager_add_event_to_transaction(kaa_event_manager_t *self
                                       , kaa_event_block_id trx_id
                                       , const char *fqn
                                       , const char *event_data
                                       , size_t event_data_size
                                       , const char *target)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to add event to events batch with id %zu", trx_id);

    KAA_RETURN_IF_NIL(fqn, KAA_ERR_EVENT_BAD_FQN);

    if (self->transactions) {
        kaa_list_t *it = kaa_list_find_next(self->transactions, &transaction_search_by_id_predicate, &trx_id);
        if (it) {
            event_transaction_t *trx = kaa_list_get_data(it);
            kaa_event_t *event = kaa_event_create();
            KAA_RETURN_IF_NIL(event, KAA_ERR_NOMEM);

            kaa_error_t error_code = kaa_fill_event_structure(
                    event, (size_t)-1, fqn, event_data, event_data_size, target);
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

# endif

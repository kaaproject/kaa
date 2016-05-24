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

#include "kaa_event.h"
#include "kaa_event_private.h"

#include "kaa_private.h"

#include <stdbool.h>
#include <stdint.h>
#include <string.h>
#include <stdarg.h>
#include <sys/types.h>
#include "platform/stdio.h"
#include "platform/sock.h"
#include "platform/ext_sha.h"
#include "kaa_status.h"
#include "kaa_channel_manager.h"
#include "kaa_platform_utils.h"
#include "kaa_platform_common.h"
#include "kaa_common_schema.h"
#include "collections/kaa_list.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "platform/ext_system_logger.h"
#include "gen/kaa_event_fqn_definitions.h"

#define KAA_EVENT_CLIENT_SYNC_EXTENSION_FLAG_RECEIVE_EVENTS         0x1
#define KAA_EVENT_CLIENT_SYNC_EXTENSION_FLAG_SEQUENCE_NUMBER_SYNC   0x2

#define KAA_EVENT_SERVER_SYNC_EXTENSION_FLAG_SEQUENCE_NUMBER_PRESENT   0x1

#define KAA_EVENT_OPTION_TARGET_ID_PRESENT    0x1
#define KAA_EVENT_OPTION_EVENT_HAS_DATA       0x2

typedef enum {
    EVENT_LISTENERS_FIELD = 0x00,
    EVENTS_FIELD = 0x01,
} event_server_sync_field_t;

typedef enum {
    EVENT_LISTENERS_SUCCESS = 0x00,
    EVENT_LISTENERS_FAILURE = 0x01
} event_listeners_result_t;

typedef struct {
    int32_t          seq_num;
    /**
     * Use kaa_bytes_t for the fqn parameter (string type) to reduce strlen overhead.
     * The kaa_bytes_t buffer will be null terminated.
     */
    kaa_bytes_t*    event_class_fqn;
    kaa_bytes_t*    event_data;
    kaa_bytes_t*    target;
} kaa_event_t;

typedef struct {
    size_t        request_id;
    kaa_list_t    *sent_events;
} sent_events_tuple_t;

typedef struct {
    char *fqn;
    kaa_event_callback_t  cb;
} event_callback_pair_t;

typedef struct {
    kaa_event_block_id    id;
    kaa_list_t            *events;
} event_transaction_t;

typedef enum {
    KAA_EVENT_SEQUENCE_NUMBER_UNSYNCHRONIZED,
    KAA_EVENT_SEQUENCE_NUMBER_SYNC_IN_PROGRESS,
    KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED
} kaa_event_sequence_number_status_t;

typedef struct {
    uint16_t                        request_id;
    bool                            is_sent;
    kaa_bytes_t                   **fqns;
    size_t                          fqns_count;
    kaa_event_listeners_callback_t  callback;
} kaa_event_listeners_request_t;

/* Public stuff */
struct kaa_event_manager_t {
    sent_events_tuple_t         events_awaiting_response;
    kaa_list_t                 *pending_events;
    kaa_list_t                 *event_callbacks;
    kaa_list_t                 *transactions;
    kaa_list_t                 *event_listeners_requests;
    kaa_event_block_id          trx_counter;
    kaa_event_callback_t        global_event_callback;
    size_t                      event_sequence_number;
    size_t                      extension_payload_size;
    kaa_event_sequence_number_status_t sequence_number_status;

    kaa_status_t                *status;
    kaa_channel_manager_t       *channel_manager;
    kaa_logger_t                *logger;

    uint16_t                     event_listeners_request_id;

    kaa_endpoint_id_p            event_source;
};

static kaa_extension_id event_sync_services[1] = { KAA_EXTENSION_EVENT };

kaa_error_t kaa_extension_event_init(kaa_context_t *kaa_context, void **context)
{
    kaa_error_t error = kaa_event_manager_create(&kaa_context->event_manager, kaa_context->status->status_instance,
        kaa_context->channel_manager, kaa_context->logger);
    *context = kaa_context->event_manager;
    return error;
}

kaa_error_t kaa_extension_event_deinit(void *context)
{
    kaa_event_manager_destroy(context);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_extension_event_request_get_size(void *context, size_t *expected_size)
{
    return kaa_event_request_get_size(context, expected_size);
}

kaa_error_t kaa_extension_event_request_serialize(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *need_resync)
{
    // TODO(KAA-982): Use asserts
    if (!context || !size || !need_resync) {
        return KAA_ERR_BADPARAM;
    }

    *need_resync = true;

    size_t size_needed;
    kaa_error_t error = kaa_extension_event_request_get_size(context, &size_needed);
    if (error) {
        return error;
    }

    if (!buffer || *size < size_needed) {
        *size = size_needed;
        return KAA_ERR_BUFFER_IS_NOT_ENOUGH;
    }

    *size = size_needed;

    kaa_platform_message_writer_t writer = KAA_MESSAGE_WRITER(buffer, *size);
    error = kaa_event_request_serialize(context, request_id, &writer);
    if (error) {
        return error;
    }

    *size = writer.current - buffer;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_extension_event_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{
    // TODO(KAA-982): Use asserts
    if (!context || !buffer) {
        return KAA_ERR_BADPARAM;
    }

    kaa_platform_message_reader_t reader = KAA_MESSAGE_READER(buffer, size);
    return kaa_event_handle_server_sync(context, &reader, extension_options, size, request_id);
}

static void destroy_event_listener_request(void *request_p)
{
    KAA_RETURN_IF_NIL(request_p,);
    kaa_event_listeners_request_t *subscriber = (kaa_event_listeners_request_t *) request_p;
    if (subscriber->fqns) {
        size_t i = 0;
        for (; i < subscriber->fqns_count; ++i) {
            if (subscriber->fqns[i]) {
                kaa_bytes_destroy(subscriber->fqns[i]);
            }
        }
        KAA_FREE(subscriber->fqns);
    }
    KAA_FREE(subscriber);
}

static kaa_event_listeners_request_t *create_event_listener_request(kaa_event_manager_t *self, const char *fqns[], size_t fqns_count, const kaa_event_listeners_callback_t *callback)
{
    kaa_event_listeners_request_t *result = (kaa_event_listeners_request_t *) KAA_MALLOC(sizeof(kaa_event_listeners_request_t));
    KAA_RETURN_IF_NIL(result, NULL);

    result->is_sent = false;
    result->callback = *callback;
    result->request_id = self->event_listeners_request_id + 1;

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to create event listeners request: fqns count %zu, request id '%u'", fqns_count, result->request_id);

    result->fqns = (kaa_bytes_t **) KAA_CALLOC(fqns_count, sizeof(kaa_bytes_t *));
    if (!result->fqns) {
        destroy_event_listener_request(result);
        return NULL;
    }
    result->fqns_count = fqns_count;

    size_t i = 0;
    for (; i < fqns_count; ++i) {
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Target fqn[%d]: '%s'", i+1, fqns[i]);
        result->fqns[i] = kaa_bytes_copy_create((const uint8_t *) fqns[i], strlen(fqns[i]));
        if (!result->fqns[i]) {
            destroy_event_listener_request(result);
            return NULL;
        }
    }

    return result;
}

static bool find_listeners_request_by_id(void *request_p, void *context)
{
    kaa_event_listeners_request_t *request = (kaa_event_listeners_request_t *) request_p;
    uint16_t *request_id = (uint16_t *) context;
    return (request && request_id && ((*request_id) == request->request_id));
}

static void kaa_event_destroy(void* data)
{
    if (data) {
        kaa_event_t* record = (kaa_event_t*)data;

        kaa_bytes_destroy(record->event_class_fqn);
        kaa_bytes_destroy(record->event_data);
        kaa_bytes_destroy(record->target);

        KAA_FREE(record);
    }
}

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

static void kaa_event_destroy_callback_pair(void *pair_p)
{
    KAA_RETURN_IF_NIL(pair_p, );
    event_callback_pair_t *pair = (event_callback_pair_t *) pair_p;
    KAA_FREE(pair->fqn);
    KAA_FREE(pair);
}

static kaa_event_callback_t find_event_callback(kaa_list_t *head, const char *fqn)
{
    kaa_list_node_t *it = kaa_list_begin(head);
    while (it) {
        event_callback_pair_t *pair = (event_callback_pair_t *) kaa_list_get_data(it);
        if (strcmp(fqn, pair->fqn) == 0)
            return pair->cb;
        it = kaa_list_next(it);
    }
    return NULL;
}

static event_transaction_t *create_transaction(kaa_event_block_id id)
{
    event_transaction_t *trx = (event_transaction_t *) KAA_MALLOC(sizeof(event_transaction_t));
    KAA_RETURN_IF_NIL(trx, NULL);

    trx->id = id;
    trx->events = kaa_list_create();
    if (!trx->events) {
        KAA_FREE(trx);
        return NULL;
    }

    return trx;
}

static void destroy_transaction(void *trx_p)
{
    KAA_RETURN_IF_NIL(trx_p, );
    event_transaction_t *trx = (event_transaction_t *) trx_p;
    kaa_list_destroy(trx->events, &kaa_event_destroy);
    KAA_FREE(trx);
}

static bool transaction_search_by_id_predicate(void *trx_p, void *context)
{
    event_transaction_t *trx = (event_transaction_t *) trx_p;
    kaa_event_block_id *matcher = (kaa_event_block_id *) context;
    return (matcher && trx) ? ((*matcher) == trx->id) : false;
}

/** @deprecated Use kaa_extension_event_deinit(). */
void kaa_event_manager_destroy(kaa_event_manager_t *self)
{
    if (self) {
        kaa_list_destroy(self->pending_events, &kaa_event_destroy);
        kaa_list_destroy(self->events_awaiting_response.sent_events, &kaa_event_destroy);
        kaa_list_destroy(self->event_callbacks, &kaa_event_destroy_callback_pair);
        kaa_list_destroy(self->transactions, &destroy_transaction);
        kaa_list_destroy(self->event_listeners_requests, &destroy_event_listener_request);

        if (self->event_source) {
            KAA_FREE((void*)self->event_source);
        }
        KAA_FREE(self);
    }
}

/** @deprecated Use kaa_extension_event_init(). */
kaa_error_t kaa_event_manager_create(kaa_event_manager_t **context, kaa_status_t *status,
    kaa_channel_manager_t *channel_manager, kaa_logger_t *logger)
{
    kaa_event_manager_t **event_manager_p = (kaa_event_manager_t **)context;
    KAA_RETURN_IF_NIL(event_manager_p, KAA_ERR_BADPARAM);

    *event_manager_p = (kaa_event_manager_t *) KAA_MALLOC(sizeof(kaa_event_manager_t));
    KAA_RETURN_IF_NIL(*event_manager_p, KAA_ERR_NOMEM);

    (*event_manager_p)->events_awaiting_response.request_id =  (size_t) -1;
    (*event_manager_p)->event_listeners_request_id = 0;
    (*event_manager_p)->trx_counter = 0;
    (*event_manager_p)->global_event_callback = NULL;
    (*event_manager_p)->event_sequence_number = status->event_seq_n;

    (*event_manager_p)->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_UNSYNCHRONIZED;

    (*event_manager_p)->status = status;
    (*event_manager_p)->channel_manager = channel_manager;
    (*event_manager_p)->logger = logger;

    (*event_manager_p)->event_source = (kaa_endpoint_id_p)KAA_MALLOC(sizeof(kaa_endpoint_id));
    KAA_RETURN_IF_NIL((*event_manager_p)->event_source, KAA_ERR_NOMEM);

    (*event_manager_p)->pending_events = kaa_list_create();
    (*event_manager_p)->events_awaiting_response.sent_events = kaa_list_create();
    (*event_manager_p)->event_callbacks = kaa_list_create();
    (*event_manager_p)->transactions = kaa_list_create();
    (*event_manager_p)->event_listeners_requests = kaa_list_create();

    if (!(*event_manager_p)->pending_events || !(*event_manager_p)->events_awaiting_response.sent_events ||
        !(*event_manager_p)->event_callbacks || !(*event_manager_p)->transactions ||
        !(*event_manager_p)->event_listeners_requests)
    {
        kaa_event_manager_destroy(*event_manager_p);
        return KAA_ERR_NOMEM;
    }

    return KAA_ERR_NONE;
}

static kaa_error_t kaa_fill_event_structure(kaa_event_t *event
                                          , size_t sequence_number
                                          , const char *fqn
                                          , const char *event_data
                                          , size_t event_data_size
                                          , kaa_endpoint_id_p target)
{
    KAA_RETURN_IF_NIL2(event, fqn, KAA_ERR_BADPARAM);

    event->seq_num = sequence_number;
    event->event_class_fqn = kaa_bytes_copy_create((const uint8_t *)fqn
                                                 , strlen(fqn) + 1);
    --event->event_class_fqn->size;
    KAA_RETURN_IF_NIL(event->event_class_fqn, KAA_ERR_NOMEM);

    if (event_data && event_data_size > 0) {
        event->event_data = kaa_bytes_move_create((const uint8_t *) event_data
                                                , event_data_size
                                                , &kaa_data_destroy);
        KAA_RETURN_IF_NIL(event->event_data, KAA_ERR_NOMEM);
    }

    if (target) {
        event->target = kaa_bytes_copy_create(target, KAA_ENDPOINT_ID_LENGTH);
        KAA_RETURN_IF_NIL(event->target, KAA_ERR_NOMEM);
    }

    return KAA_ERR_NONE;
}

/*
 * @brief Sends raw event
 *
 * It is not recommended to use this function directly. Instead you should use
 * functions contained in EventClassFamily auto-generated headers (placed at src/event/)
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in]       fqn                 Fully-qualified name of the event (null-terminated string).
 * @param[in]       event_data          Serialized event object.
 * @param[in]       event_data_size     Size of data in event_data parameter.
 * @param[in]       target              The target endpoint of the event (null-terminated string). The size of
 *                                      the target parameter should be equal to @link KAA_ENDPOINT_ID_LENGTH @endlink .
 *                                      If @code NULL @endcode event will be broadcasted.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_manager_send_event(kaa_event_manager_t *self
                                       , const char *fqn
                                       , const char *event_data
                                       , size_t event_data_size
                                       , kaa_endpoint_id_p target)
{
    /**
     * Both the event data + its size and the target may be left unspecified (null).
     */
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);
    KAA_RETURN_IF_NIL(fqn, KAA_ERR_EVENT_BAD_FQN);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Adding a new event \"%s\"", fqn);

    /**
     * KAA_CALLOC is really needed there.
     */
    kaa_event_t *event = (kaa_event_t*)KAA_CALLOC(1, sizeof(kaa_event_t));
    if (!event) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to allocate a new event structure");
        return KAA_ERR_NOMEM;
    }

    size_t new_sequence_number = (self->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED ?
                                        ++self->event_sequence_number :
                                        (size_t) -1);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Filling a new event with data size %u", event_data_size);
# ifdef KAA_LOG_LEVEL_TRACE_ENABLED
    if (target) {
        char target_string[2 * KAA_ENDPOINT_ID_LENGTH + 1];
        int i = 0;
        for (; i < KAA_ENDPOINT_ID_LENGTH; ++i) {
            ext_snpintf(&target_string[2 * i], 3, "%02X", target[i]);
        }
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Event target = %s", target_string);
    }
# endif
    kaa_error_t error = kaa_fill_event_structure(event
                                                    , new_sequence_number
                                                    , fqn
                                                    , event_data
                                                    , event_data_size
                                                    , target);
    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to fill a new event (size=%u)", event_data_size);
        kaa_event_destroy(event);
        return error;
    }

    if (!kaa_list_push_back(self->pending_events, event)) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to save a new event");
        kaa_event_destroy(event);
        return KAA_ERR_NOMEM;
    }

    kaa_transport_channel_interface_t *channel =
            kaa_channel_manager_get_transport_channel(self->channel_manager, event_sync_services[0]);
    if (channel)
        channel->sync_handler(channel->context, event_sync_services, 1);

    return KAA_ERR_NONE;
}

static size_t kaa_event_list_get_request_size(kaa_list_t *events)
{
    size_t expected_size = 0;

    kaa_list_node_t *it = kaa_list_begin(events);
    while (it) {
        kaa_event_t *event = (kaa_event_t *)kaa_list_get_data(it);

        expected_size  += sizeof(uint32_t) /*Event sequence number*/
                        + sizeof(uint16_t) /*Event options*/
                        + sizeof(uint16_t); /*Event class FQN length */

        if (event->event_data) {
            expected_size  += sizeof(uint32_t); /*Event data size*/
        }

        if (event->target /*have Target Endpoint ID*/) {
            expected_size += KAA_ENDPOINT_ID_LENGTH; /*Target Endpoint ID*/
        }

        expected_size += kaa_aligned_size_get(event->event_class_fqn->size); /*Event class FQN + padding */

        if (event->event_data) {
            expected_size += kaa_aligned_size_get(event->event_data->size);/*Event data + padding*/
        }

        it = kaa_list_next(it);
    }
    return expected_size;
}

static kaa_error_t kaa_event_request_get_size_no_header(kaa_event_manager_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);

    *expected_size = 0;
    if (self->sequence_number_status == KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED) {
        bool have_events = (kaa_list_get_size(self->pending_events) > 0) ||
                (kaa_list_get_size(self->events_awaiting_response.sent_events) > 0);

        if (have_events) {
            *expected_size += sizeof(uint32_t); // field id(1) + reserved + events count
            *expected_size += kaa_event_list_get_request_size(self->pending_events);
            *expected_size += kaa_event_list_get_request_size(self->events_awaiting_response.sent_events);
        }
    }

    if (kaa_list_get_size(self->event_listeners_requests) > 0) {
        *expected_size += sizeof(uint32_t); // field id(0) + reserved + listeners count

        kaa_list_node_t *it = kaa_list_begin(self->event_listeners_requests);
        while (it) {
            kaa_event_listeners_request_t *request = (kaa_event_listeners_request_t *) kaa_list_get_data(it);
            if (!request->is_sent) {
                *expected_size += sizeof(uint32_t); // request id + fqns count
                *expected_size += sizeof(uint32_t) * request->fqns_count; // fqn length + reserved
                for (size_t i = 0; i < request->fqns_count; ++i) {
                    *expected_size += kaa_aligned_size_get(request->fqns[i]->size);
                }
            }
            it = kaa_list_next(it);
        }
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_request_get_size(kaa_event_manager_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);

    kaa_error_t error = kaa_event_request_get_size_no_header(self, expected_size);
    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to get event extension length");
        *expected_size = 0;
        return error;
    }

    self->extension_payload_size = *expected_size;
    *expected_size += KAA_EXTENSION_HEADER_SIZE;

    return KAA_ERR_NONE;
}

static kaa_error_t kaa_event_list_serialize(kaa_event_manager_t *self, kaa_list_t *events, kaa_platform_message_writer_t *writer)
{
    uint16_t temp_network_order_16 = 0;
    uint16_t options = 0;
    uint32_t temp_network_order_32 = 0;

    kaa_list_node_t *it = kaa_list_begin(events);
    while (it) {
        kaa_event_t *event = (kaa_event_t *) kaa_list_get_data(it);

        if (event->seq_num == -1) {
            event->seq_num = ++self->event_sequence_number;
        }

        temp_network_order_32 = KAA_HTONL(event->seq_num);
        kaa_error_t error = kaa_platform_message_write(writer, &temp_network_order_32, sizeof(uint32_t));
        if (error) {
            KAA_LOG_ERROR(self->logger, error, "Failed to write event sequence number");
            return error;
        }

        /**
         * Event options
         */
        options = (!event->target ? 0 : KAA_EVENT_OPTION_TARGET_ID_PRESENT)
                   | (event->event_data ? KAA_EVENT_OPTION_EVENT_HAS_DATA : 0);
        options = KAA_HTONS(options);

        error = kaa_platform_message_write(writer, &options, sizeof(uint16_t));
        if (error) {
            KAA_LOG_ERROR(self->logger, error, "Failed to write event options");
            return error;
        }

        temp_network_order_16 = KAA_HTONS(event->event_class_fqn->size);
        error = kaa_platform_message_write(writer, &temp_network_order_16, sizeof(uint16_t));
        if (error) {
            KAA_LOG_ERROR(self->logger, error, "Failed to write event class fqn length");
            return error;
        }

        if (event->event_data) {
            temp_network_order_32 = KAA_HTONL(event->event_data->size);
            error = kaa_platform_message_write(writer, &temp_network_order_32, sizeof(uint32_t));
            if (error) {
                KAA_LOG_ERROR(self->logger, error, "Failed to write event data size");
                return error;
            }
        }

        if (event->target) {
            error = kaa_platform_message_write_aligned(writer,
                                                            event->target->buffer
                                                          , KAA_ENDPOINT_ID_LENGTH);
            if (error) {
                KAA_LOG_ERROR(self->logger, error, "Failed to write event target id");
                return error;
            }
        }

        error = kaa_platform_message_write_aligned(writer
                                                      , event->event_class_fqn->buffer
                                                      , event->event_class_fqn->size);
        if (error) {
            KAA_LOG_ERROR(self->logger, error, "Failed to write event class fqn aligned");
            return error;
        }


        if (event->event_data) {
            error = kaa_platform_message_write_aligned(writer
                                                          , event->event_data->buffer
                                                          , event->event_data->size);
            if (error) {
                KAA_LOG_ERROR(self->logger, error, "Failed to write event data aligned");
                return error;
            }
        }
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Serialized event: sqn '%u', options '%u', data size '%u', fqn '%s'"
                    , event->seq_num, KAA_NTOHS(options), event->event_data ? event->event_data->size : 0, event->event_class_fqn->buffer);
        it = kaa_list_next(it);
    }
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_event_listeners_request_serialize(kaa_event_manager_t *self, kaa_platform_message_writer_t *writer, uint16_t *serialized_listeners_count)
{
    kaa_list_node_t *it = kaa_list_begin(self->event_listeners_requests);
    *serialized_listeners_count = 0;
    while (it) {
        kaa_event_listeners_request_t *request = (kaa_event_listeners_request_t *) kaa_list_get_data(it);
        if (!request->is_sent) {
            *((uint16_t *) writer->current) = KAA_HTONS(request->request_id);
            writer->current += sizeof(uint16_t);
            *((uint16_t *) writer->current) = KAA_HTONS((uint16_t) request->fqns_count);
            writer->current += sizeof(uint16_t);
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to serialize event listeners: request id '%u', fqn count '%u'"
                        , request->request_id, request->fqns_count);
            for (size_t i = 0; i < request->fqns_count; ++i) {
                size_t fqn_length = request->fqns[i]->size;
                *((uint16_t *) writer->current) = KAA_HTONS((uint16_t) fqn_length);
                writer->current += sizeof(uint32_t); // fqn length + reserved
                kaa_error_t error = kaa_platform_message_write_aligned(writer, request->fqns[i]->buffer, fqn_length);
                if (error) {
                    KAA_LOG_ERROR(self->logger, error, "Failed to write event listener request");
                    *serialized_listeners_count = 0;
                    return error;
                }
            }

            request->is_sent = true;
            (*serialized_listeners_count)++;
        }
        it = kaa_list_next(it);
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_request_serialize(kaa_event_manager_t *self, size_t request_id, kaa_platform_message_writer_t *writer)
{
    KAA_RETURN_IF_NIL2(self, writer, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to serialize client event sync");

    /* write extension header */
    uint16_t extension_options = 0;
    if (self->sequence_number_status != KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED) {
        extension_options |= KAA_EVENT_CLIENT_SYNC_EXTENSION_FLAG_SEQUENCE_NUMBER_SYNC;
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to sync event SQN");
    } else {
        extension_options |= KAA_EVENT_CLIENT_SYNC_EXTENSION_FLAG_RECEIVE_EVENTS;
    }

    kaa_error_t error = kaa_platform_message_write_extension_header(writer
                                                           , KAA_EXTENSION_EVENT
                                                           , extension_options
                                                           , self->extension_payload_size);
    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to write event extension header (ext type %u, options %X, payload size %u)"
                                        , KAA_EXTENSION_EVENT, extension_options, self->extension_payload_size);
        return error;
    }

    /* write events */
    if (self->extension_payload_size) {
        kaa_list_t *pending_events = self->pending_events;
        kaa_list_t *resending_events = self->events_awaiting_response.sent_events;
        uint16_t events_count = kaa_list_get_size(pending_events) + kaa_list_get_size(resending_events);

        if (events_count) {
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Serializing %u events", events_count);
            *((uint8_t *) writer->current) = EVENTS_FIELD;
            writer->current += sizeof(uint16_t); // field id + reserved
            *((uint16_t *) writer->current) = KAA_HTONS(events_count);
            writer->current += sizeof(uint16_t);

            error = kaa_event_list_serialize(self, resending_events, writer);
            if (error) {
                KAA_LOG_ERROR(self->logger, error, "Failed to write events");
                return error;
            }

            error = kaa_event_list_serialize(self, pending_events, writer);
            if (error) {
                KAA_LOG_ERROR(self->logger, error, "Failed to write events");
                return error;
            }

            self->events_awaiting_response.request_id = request_id;
            self->events_awaiting_response.sent_events = kaa_lists_merge(self->events_awaiting_response.sent_events
                                                                       , self->pending_events);
        }

        if (kaa_list_get_size(self->event_listeners_requests)) {
            *((uint8_t *) writer->current) = EVENT_LISTENERS_FIELD;
            writer->current += sizeof(uint16_t); // field id + reserved
            uint8_t *listeners_count_p = writer->current; // Pointer to the listeners count. Will be filled in later
            writer->current += sizeof(uint16_t);

            uint16_t listeners_count = 0;
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Serializing event listeners");
            error = kaa_event_listeners_request_serialize(self, writer, &listeners_count);
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Serialized %u event listeners", listeners_count);
            if (error) {
                KAA_LOG_ERROR(self->logger, error, "Failed to serialize event listeners request");
                return error;
            }
            *((uint16_t *) listeners_count_p) = KAA_HTONS(listeners_count);
        }
    }
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_event_read_event(kaa_event_manager_t *self, kaa_platform_message_reader_t *reader)
{

    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);

    uint16_t event_options = 0;
    kaa_error_t error = kaa_platform_message_read(reader, &event_options, sizeof(uint16_t));
    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to read event options field");
        return error;
    }
    event_options = KAA_NTOHS(event_options);

    uint16_t event_class_fqn_length = 0;
    error = kaa_platform_message_read(reader, &event_class_fqn_length, sizeof(uint16_t));
    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to read event class fqn length field");
        return error;
    }
    event_class_fqn_length = KAA_NTOHS(event_class_fqn_length);

    uint32_t event_data_size = 0;
    if (event_options & KAA_EVENT_OPTION_EVENT_HAS_DATA) {
        error = kaa_platform_message_read(reader, &event_data_size, sizeof(uint32_t));
        if (error) {
            KAA_LOG_ERROR(self->logger, error, "Failed to read event data size field");
            return error;
        }
        event_data_size = KAA_NTOHL(event_data_size);
    }

    error = kaa_platform_message_read(reader, (void*)self->event_source, sizeof(kaa_endpoint_id));

    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to read event source endpoint id field");
        return error;
    }

    bool is_enough = kaa_platform_message_is_buffer_large_enough(reader, kaa_aligned_size_get(event_class_fqn_length));

    if (!is_enough) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_READ_FAILED, "Buffer size is less than event class fqn length value");
        return KAA_ERR_READ_FAILED;
    }
    char* event_fqn = (char *)KAA_MALLOC(event_class_fqn_length + 1);

    KAA_RETURN_IF_NIL(event_fqn, KAA_ERR_NOMEM);

    error = kaa_platform_message_read_aligned(reader, event_fqn, event_class_fqn_length);

    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to read event class fqn field");
        KAA_FREE(event_fqn);
        return error;
    }
    event_fqn[event_class_fqn_length] = '\0';

    KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Processing event: options '%u', fqn length '%u', fqn '%s' ", event_options, event_class_fqn_length, event_fqn);

    kaa_event_callback_t callback = find_event_callback(self->event_callbacks, event_fqn);
    if (!callback)
        callback = self->global_event_callback;

    if (event_options & KAA_EVENT_OPTION_EVENT_HAS_DATA) {
        const uint8_t *event_data = reader->current;
        kaa_error_t error = kaa_platform_message_skip(reader, kaa_aligned_size_get(event_data_size));
        if (error) {
             KAA_LOG_ERROR(self->logger, error, "Failed to read event data, size %u", event_data_size);
             KAA_FREE(event_fqn);
             return error;
        }
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Successfully retrieved event data size=%u", event_data_size);
        if (callback)
            (*callback)(event_fqn, (const char *)event_data, event_data_size, self->event_source);
    } else if (callback) {
        (*callback)(event_fqn, NULL, 0, self->event_source);
    }
    KAA_FREE(event_fqn);
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_event_read_listeners_response(kaa_event_manager_t *self, kaa_platform_message_reader_t *reader)
{
    uint16_t request_id = KAA_NTOHS(*(uint16_t *) reader->current);
    reader->current += sizeof(uint16_t);
    uint16_t listeners_result = KAA_NTOHS(*(uint16_t *) reader->current);
    reader->current += sizeof(uint16_t);

    uint32_t listeners_count =  KAA_NTOHL(*(uint32_t *) reader->current);
    reader->current += sizeof(uint32_t);

    KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Received %u event listener(s) on %u request"
                                                                , listeners_count, request_id);

    kaa_list_node_t *request_node = kaa_list_find_next(kaa_list_begin(self->event_listeners_requests)
                                                     , &find_listeners_request_by_id
                                                     , &request_id);
    if (request_node) {
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Found event listeners callback with request id %u", request_id);
        if (reader->current + (listeners_count * KAA_ENDPOINT_ID_LENGTH) > reader->end) {
            KAA_LOG_ERROR(self->logger, KAA_ERR_READ_FAILED, "Failed to read endpoint ids for request id %u", request_id);
            return KAA_ERR_READ_FAILED;
        }
        kaa_event_listeners_request_t *request = (kaa_event_listeners_request_t *) kaa_list_get_data(request_node);
        if (listeners_result == EVENT_LISTENERS_SUCCESS) {
            request->callback.on_event_listeners(request->callback.context, (const kaa_endpoint_id *) reader->current, listeners_count);
            KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Success event listeners response for request id %u", request_id);
        } else {
            request->callback.on_event_listeners_failed(request->callback.context);
            KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Failed to find event listeners, request id %u", request_id);
        }
        kaa_list_remove_at(self->event_listeners_requests, request_node, &destroy_event_listener_request);
    } else {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Failed to find event listeners callback with request id %u", request_id);
    }
    reader->current += listeners_count * KAA_ENDPOINT_ID_LENGTH;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_handle_server_sync(kaa_event_manager_t *self
                                       , kaa_platform_message_reader_t *reader
                                       , uint16_t extension_options
                                       , size_t extension_length
                                       , size_t request_id)
{
    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received event server sync: options %u, payload size %u", extension_options, extension_length);

    if (extension_options & KAA_EVENT_SERVER_SYNC_EXTENSION_FLAG_SEQUENCE_NUMBER_PRESENT) {
        uint32_t event_sequence_number = 0;
        kaa_error_t error = kaa_platform_message_read(reader, &event_sequence_number, sizeof(uint32_t));
        if (error) {
            KAA_LOG_ERROR(self->logger, error, "Failed to read event_sequence number field");
            return error;
        }

        event_sequence_number = KAA_HTONL(event_sequence_number);
        extension_length -= sizeof(uint32_t);

        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received event sequence number '%u'", event_sequence_number);
        if (self->sequence_number_status != KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED) {
            self->sequence_number_status = KAA_EVENT_SEQUENCE_NUMBER_SYNCHRONIZED;

            if (self->event_sequence_number != event_sequence_number) {
                KAA_LOG_WARN(self->logger, KAA_ERR_BAD_STATE, "Stored event sequence number is not correct (stored %u, received %u).", self->event_sequence_number, event_sequence_number);
                self->event_sequence_number = event_sequence_number;
                kaa_list_node_t *it = kaa_list_begin(self->pending_events);
                while (it) {
                    kaa_event_t *event = (kaa_event_t *)kaa_list_get_data(it);
                    event->seq_num = ++self->event_sequence_number;
                    it = kaa_list_next(it);
                }
            }
        }
        if (kaa_list_get_size(self->pending_events) > 0) {
            kaa_transport_channel_interface_t *channel =
                    kaa_channel_manager_get_transport_channel(self->channel_manager, event_sync_services[0]);
            if (channel)
                channel->sync_handler(channel->context, event_sync_services, 1);
        }
    }

    if (request_id == self->events_awaiting_response.request_id) {
        kaa_list_clear(self->events_awaiting_response.sent_events, &kaa_event_destroy);
        self->events_awaiting_response.request_id = (size_t) -1;
    }

    while (extension_length > 0) {
        uint8_t field_id = 0;
        const uint8_t *field_id_pos = reader->current;

        kaa_error_t error = kaa_platform_message_read(reader, &field_id, sizeof(uint8_t)); //read field id
        if (error) {
            KAA_LOG_ERROR(self->logger, error, "Failed to read field id in Event server sync message");
            return error;
        }

        error = kaa_platform_message_skip(reader, sizeof(uint8_t)); // skip reserved
        if (error) {
            KAA_LOG_ERROR(self->logger, error, "Failed to skip reserved field in Event server sync message");
            return error;
        }
        switch (field_id) {
            case EVENTS_FIELD: {
                uint16_t events_count = 0;
                error = kaa_platform_message_read(reader, &events_count, sizeof(uint16_t)); // read events count
                if (error) {
                    KAA_LOG_ERROR(self->logger, error, "Failed to read field id in Event server sync message");
                    return error;
                }
                events_count = KAA_NTOHS(events_count);
                KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received %u events", events_count);
                while (events_count--) {
                    error = kaa_event_read_event(self, reader);
                    if (error) {
                        KAA_LOG_ERROR(self->logger, error, "Failed to read event from server sync");
                        return error;
                    }
                }
                break;
            }
            case EVENT_LISTENERS_FIELD: {
                uint16_t event_listeners_responses_count = 0;
                error = kaa_platform_message_read(reader, &event_listeners_responses_count, sizeof(uint16_t)); // read events count
                if (error) {
                    KAA_LOG_ERROR(self->logger, error, "Failed to read event listener response in Event server sync message");
                    return error;
                }

                event_listeners_responses_count = KAA_NTOHS(event_listeners_responses_count);
                KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received %u event listeners response(s)"
                                                                    , event_listeners_responses_count);

                while (event_listeners_responses_count--) {
                    error = kaa_event_read_listeners_response(self, reader);
                    if (error) {
                       KAA_LOG_ERROR(self->logger, error, "Failed to read event listeners response");
                       return error;
                   }
                }
                break;
            }
            default:
                KAA_LOG_ERROR(self->logger, KAA_ERR_BADDATA, "Unexpected field id %u", field_id);
                return KAA_ERR_BADDATA;
        }

        extension_length -= (reader->current - field_id_pos);
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_manager_find_event_listeners(kaa_event_manager_t *self, const char *fqns[], size_t fqns_count, const kaa_event_listeners_callback_t *callback)
{
    KAA_RETURN_IF_NIL5(self, fqns_count, callback, callback->on_event_listeners, callback->on_event_listeners_failed, KAA_ERR_BADPARAM);

    kaa_event_listeners_request_t *subscriber = create_event_listener_request(self, fqns, fqns_count, callback);
    KAA_RETURN_IF_NIL(subscriber, KAA_ERR_NOMEM);

    kaa_list_node_t *request_it = kaa_list_push_front(self->event_listeners_requests, subscriber);
    if (!request_it) {
        destroy_event_listener_request(subscriber);
        return KAA_ERR_NOMEM;
    }

    ++self->event_listeners_request_id;

    kaa_transport_channel_interface_t *channel = kaa_channel_manager_get_transport_channel(self->channel_manager, event_sync_services[0]);
    if (channel)
        channel->sync_handler(channel->context, event_sync_services, 1);

    return KAA_ERR_NONE;
}

/*
 * @brief Register listener to an event.
 *
 * It is not recommended to use this function directly. Instead you should use
 * functions contained in EventClassFamily auto-generated headers (placed at src/event/)
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in]       fqn                 Fully-qualified name of the event (null-terminated string).
 *                                      If @code NULL @endcode, this callback will be invoked for
 *                                      all events which do not have registered specific callback.
 * @param[in]       callback            Event callback function.
 *
 * @return  Error code.
 */
kaa_error_t kaa_event_manager_add_on_event_callback(kaa_event_manager_t *self, const char *fqn, kaa_event_callback_t callback)
{
    KAA_RETURN_IF_NIL2(self, callback, KAA_ERR_BADPARAM);
    if (fqn) {
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Adding callback for events, fqn '%s'", fqn);
        event_callback_pair_t *pair = create_event_callback_pair(fqn, callback);
        KAA_RETURN_IF_NIL(pair, KAA_ERR_NOMEM);
        kaa_list_node_t *it = kaa_list_begin(self->event_callbacks);
        while (it) {
            event_callback_pair_t *data = (event_callback_pair_t *) kaa_list_get_data(it);
            if (strcmp(fqn, data->fqn) == 0) {
                kaa_list_set_data_at(it, pair, &kaa_event_destroy_callback_pair);
                return KAA_ERR_NONE;
            }
            it = kaa_list_next(it);
        }
        if (!kaa_list_push_back(self->event_callbacks, pair)) {
            kaa_event_destroy_callback_pair(pair);
            return KAA_ERR_NOMEM;
        }
    } else {
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Adding global event callback");
        self->global_event_callback = callback;
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_create_transaction(kaa_event_manager_t *self, kaa_event_block_id *trx_id)
{
    KAA_RETURN_IF_NIL2(self, trx_id, KAA_ERR_NOT_INITIALIZED);

    *trx_id = self->trx_counter + 1;
    event_transaction_t *new_transaction = create_transaction(*trx_id);
    KAA_RETURN_IF_NIL(new_transaction, KAA_ERR_NOMEM);

    if (!kaa_list_push_back(self->transactions, new_transaction)) {
        destroy_transaction(new_transaction);
        return KAA_ERR_NOMEM;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Creating new events batch, id %zu", *trx_id);

    self->trx_counter = *trx_id;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_event_finish_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to send events from event batch, id %zu", trx_id);

    kaa_list_node_t *it = kaa_list_find_next(kaa_list_begin(self->transactions), &transaction_search_by_id_predicate, &trx_id);
    if (it) {
        event_transaction_t *trx = kaa_list_get_data(it);
        bool need_sync = false;
        if (kaa_get_max_log_level(self->logger) >= KAA_LOG_LEVEL_TRACE) {
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Events batch with id %zu has %zu events", trx_id, kaa_list_get_size(trx->events));
        }
        if (kaa_list_get_size(trx->events) > 0) {
            self->pending_events = kaa_lists_merge(self->pending_events, trx->events);
            need_sync = true;
        }
        kaa_list_remove_at(self->transactions, it, &destroy_transaction);

        kaa_transport_channel_interface_t *channel =
                kaa_channel_manager_get_transport_channel(self->channel_manager, event_sync_services[0]);
        if (need_sync && channel)
            channel->sync_handler(channel->context, event_sync_services, 1);

        return KAA_ERR_NONE;
    }

    KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Events batch with id %zu was not created before", trx_id);

    return KAA_ERR_NOT_FOUND;
}

kaa_error_t kaa_event_remove_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to remove events batch with id %zu", trx_id);

    kaa_error_t error = kaa_list_remove_first(self->transactions, &transaction_search_by_id_predicate, &trx_id, &destroy_transaction);
    if (error) {
        KAA_LOG_WARN(self->logger, error, "Events batch with id %zu was not created before", trx_id);
    }

    return error;
}

/*
 * @brief Adds a raw event to the transaction.
 *
 * It is not recommended to use this function directly. Instead you should use
 * functions contained in EventClassFamily auto-generated headers (@code kaa_event_manager_add_*_event_to_block(...) @endcode)
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in]       trx_id              The ID of the event block to be sent.
 * @param[in]       fqn                 Fully-qualified name of the event (null-terminated string).
 * @param[in]       event_data          Serialized event object.
 * @param[in]       event_data_size     Size of data in event_data parameter.
 * @param[in]       target              The target endpoint of the event. If @code NULL @endcode event will be broadcasted.
 * @param[in]       target_size         Size of data in target parameter.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_manager_add_event_to_transaction(kaa_event_manager_t *self
                                                     , kaa_event_block_id trx_id
                                                     , const char *fqn
                                                     , const char *event_data
                                                     , size_t event_data_size
                                                     , kaa_endpoint_id_p target)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_NOT_INITIALIZED);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to add event to events batch, id %zu", trx_id);

    KAA_RETURN_IF_NIL(fqn, KAA_ERR_EVENT_BAD_FQN);

    kaa_list_node_t *it = kaa_list_find_next(kaa_list_begin(self->transactions), &transaction_search_by_id_predicate, &trx_id);
    if (it) {
        /**
         * KAA_CALLOC is really needed there.
         */
        kaa_event_t *event = (kaa_event_t*)KAA_CALLOC(1, sizeof(kaa_event_t));
        KAA_RETURN_IF_NIL(event, KAA_ERR_NOMEM);

        kaa_error_t error = kaa_fill_event_structure(event
                                                   , (size_t)-1
                                                   , fqn
                                                   , event_data
                                                   , event_data_size
                                                   , target);
        if (error) {
            kaa_event_destroy(event);
            return error;
        }

        event_transaction_t *trx = kaa_list_get_data(it);
        if (!kaa_list_push_back(trx->events, event)) {
            kaa_event_destroy(event);
            return KAA_ERR_NOMEM;
        }

        return KAA_ERR_NONE;
    }

    KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Can not add event to events batch, id %zu.", trx_id);

    return KAA_ERR_EVENT_TRX_NOT_FOUND;
}

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

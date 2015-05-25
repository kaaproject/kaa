
/*
 * Copyright 2014-2015 CyberVision, Inc.
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
#ifndef KAA_DISABLE_FEATURE_NOTIFICATION

#include "kaa_notification_manager.h"

#include <string.h>
#include <stdio.h>

#include "kaa_status.h"
#include "kaa_platform_common.h"
#include "utilities/kaa_mem.h"
#include "kaa_common.h"
#include "utilities/kaa_log.h"
#include "kaa_platform_utils.h"
#include "kaa_channel_manager.h"
#include "platform-impl/kaa_htonll.h"

#include "platform/sock.h"


extern kaa_transport_channel_interface_t *kaa_channel_manager_get_transport_channel(kaa_channel_manager_t *self
                                                                                  , kaa_service_t service_type);



struct kaa_notification_manager_t {
    kaa_list_t                 *mandatory_listeners;
    kaa_list_t                 *topics_listeners;
    kaa_list_t                 *optional_listeners;
    kaa_list_t                 *subscriptions;
    kaa_list_t                 *unsubscriptions;
    kaa_list_t                 *topics;
    kaa_list_t                 *uids;
    size_t                      notification_sequence_number;
    size_t                      extension_payload_size;

    kaa_status_t               *status;
    kaa_channel_manager_t      *channel_manager;
    kaa_logger_t               *logger;
};

typedef enum {
    KAA_CLIENT_WANTS_TO_RECEIVE_NOTIFICATIONS = 0x01,
    KAA_SUBSCRIBED_TOPIC_HASH_IS_PRESENT = 0x02
} kaa_notification_extension_flags_t;


typedef enum {
    SYSTEM = 0x0,
    CUSTOM = 0x1
} kaa_notification_type;

typedef struct {
    uint16_t length;
    void *data;
} kaa_notifications_uid_t;

#define   TOPICS                 0x0
#define   NOTIFICATIONS          0x1
#define   TOPICS_STATE_ID        0x0
#define   UID_ID                 0x1
#define   SUBSCRIPTION_ID        0x2
#define   UNSUBSCRIPTION_ID      0x3

typedef struct {
    uint64_t topic_id;
    kaa_list_t *listeners;
} kaa_optional_notification_listeners_wrapper_t ;

typedef struct {
    uint32_t id;
    kaa_notification_listener_t listener;
} kaa_notification_listener_wrapper_t;

typedef struct {
    uint32_t id;
    kaa_topic_listener_t listener;
} kaa_topic_listener_wrapper_t;

static void shift_and_sub_extension(kaa_platform_message_reader_t *reader, uint32_t *extension_length, size_t size)
{
    KAA_RETURN_IF_NIL2(reader, extension_length,);
    reader->current += size;
    *extension_length -= size;
}

static kaa_service_t notification_sync_services[1] = { KAA_SERVICE_NOTIFICATION };

static bool kaa_find_notification_listener_by_id(void *listener, void *context)
{
    KAA_RETURN_IF_NIL2(listener, context, KAA_ERR_NONE);
    return ((kaa_notification_listener_wrapper_t *) listener)->id == *(uint32_t *)context;
}
static bool kaa_find_topic_listener_by_id(void *listener, void *context)
{
    KAA_RETURN_IF_NIL2(listener, context, KAA_ERR_NONE);
    return ((kaa_topic_listener_wrapper_t *) listener)->id == *(uint32_t *)context;
}

static bool kaa_find_optional_notification_listener_by_id (void *optional_listener_list, void *topic_id)
{
    KAA_RETURN_IF_NIL2(optional_listener_list, topic_id, KAA_ERR_NONE);
    kaa_optional_notification_listeners_wrapper_t *optional_listener_node = (kaa_optional_notification_listeners_wrapper_t *) optional_listener_list;
    return optional_listener_node->topic_id == *(uint64_t *)topic_id;
}

static bool kaa_find_topic_by_id(void *topic, void *id)
{
    KAA_RETURN_IF_NIL2(topic, id, KAA_ERR_NONE);
    kaa_topic_t* notification_topic = (kaa_topic_t *) topic;
    return notification_topic->id == *(uint64_t *)id;
}

static kaa_error_t kaa_find_topic(kaa_notification_manager_t *self, kaa_topic_t **topic, uint64_t *topic_id)
{
    KAA_RETURN_IF_NIL2(topic_id, topic, KAA_ERR_BADPARAM);
    kaa_list_node_t *topic_node = kaa_list_find_next(kaa_list_begin(self->topics), &kaa_find_topic_by_id, topic_id);
    if (!topic_node) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOT_FOUND, "Topic with id = %lu not found.", *topic_id);
        return KAA_ERR_NOT_FOUND;
    }
    *topic = kaa_list_get_data(topic_node);
    return KAA_ERR_NONE;
}

static bool kaa_find_topic_state_by_id(void *topic_state, void *id)
{
    KAA_RETURN_IF_NIL2(topic_state, id, KAA_ERR_BADPARAM);
    kaa_topic_state_t *state = (kaa_topic_state_t *)topic_state;
    return state->topic_id == *(uint64_t *)id;
}

kaa_error_t kaa_calculate_topic_listener_id(kaa_topic_listener_t *listener, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL2(listener, listener_id, KAA_ERR_BADPARAM);

    const uint32_t prime = 31;

    *listener_id = 1;
    *listener_id = prime * (*listener_id) + (ptrdiff_t)listener->context;
    *listener_id = prime * (*listener_id) + (ptrdiff_t)listener->callback;
     return KAA_ERR_NONE;
}

kaa_error_t kaa_calculate_notification_listener_id(kaa_notification_listener_t *listener, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL2(listener, listener_id, KAA_ERR_BADPARAM);

    const uint32_t prime = 31;

    *listener_id = 1;
    *listener_id = prime * (*listener_id) + (ptrdiff_t)listener->context;
    *listener_id = prime * (*listener_id) + (ptrdiff_t)listener->callback;
    return KAA_ERR_NONE;
}

static size_t get_subscriptions_size(kaa_list_t *subscriptions)
{
    if (kaa_list_get_size(subscriptions) > 0) {
        size_t expected_size = sizeof(uint32_t); //meta data for subscribe commands
        expected_size += kaa_list_get_size(subscriptions) * sizeof(uint64_t);
        return expected_size;
    }

    return 0;
}

kaa_error_t kaa_notification_manager_get_size(kaa_notification_manager_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);
    *expected_size = 0;
    *expected_size += sizeof(uint32_t); //state sqn size

    if (kaa_list_get_size(self->status->topic_states) > 0) {
        *expected_size += sizeof(uint32_t); //field id + reserved + count
        *expected_size += (sizeof(uint64_t) + sizeof(uint32_t)) * kaa_list_get_size(self->status->topic_states);
    }

    if (kaa_list_get_size(self->uids) > 0) {
        *expected_size += sizeof(uint32_t); //field id + reserved + count
        *expected_size += sizeof(uint32_t) * kaa_list_get_size(self->uids);

        kaa_list_node_t *uid_node = kaa_list_begin(self->uids);
        while (uid_node) {
            kaa_notifications_uid_t *uid = (kaa_notifications_uid_t *)kaa_list_get_data(uid_node);
            *expected_size += kaa_aligned_size_get(uid->length);
            uid_node = kaa_list_next(uid_node);
        }
    }

    *expected_size += get_subscriptions_size(self->subscriptions);
    *expected_size += get_subscriptions_size(self->unsubscriptions);

    self->extension_payload_size = *expected_size;
    *expected_size += KAA_EXTENSION_HEADER_SIZE;
    return KAA_ERR_NONE;
}

static void serialize_topic_state(kaa_topic_state_t *state, kaa_platform_message_writer_t *writer)
{
    KAA_RETURN_IF_NIL2(state, writer, );
    *(uint64_t *)writer->current = KAA_HTONLL(state->topic_id);
    writer->current += sizeof(uint64_t);
    *(uint32_t *)writer->current = KAA_HTONL((uint32_t)state->sqn_number);
    writer->current += sizeof(uint32_t);
}

static void serialize_notifications_uid(kaa_notifications_uid_t * uid, kaa_platform_message_writer_t *writer)
{
    KAA_RETURN_IF_NIL2(uid, writer, );
    *(uint32_t *)writer->current = KAA_HTONL(uid->length);
    writer->current += sizeof(uint32_t);
    kaa_platform_message_write_aligned(writer, uid->data, uid->length);
}

static void serialize_subscription(uint64_t *topic_id, kaa_platform_message_writer_t *writer)
{
    KAA_RETURN_IF_NIL2(topic_id, writer, );
    *(uint64_t *)writer->current = KAA_HTONLL(*topic_id);
    writer->current += sizeof(uint64_t);
}

static void serialize_subscriptions(kaa_platform_message_writer_t *writer, kaa_list_t *subscriptions, uint8_t subscription_type)
{
    KAA_RETURN_IF_NIL2(subscriptions, kaa_list_get_size(subscriptions), );

    *(uint8_t *)writer->current = (uint8_t)subscription_type;
    writer->current += sizeof(uint16_t);
    *(uint16_t *)writer->current = KAA_HTONS((uint16_t)kaa_list_get_size(subscriptions));
    writer->current += sizeof(uint16_t);

    kaa_list_for_each(kaa_list_begin(subscriptions)
                    , kaa_list_back(subscriptions)
                    , (process_data)&serialize_subscription
                    , writer);
}

kaa_error_t kaa_notification_manager_request_serialize(kaa_notification_manager_t *self, kaa_platform_message_writer_t *writer)
{
    KAA_RETURN_IF_NIL2(self, writer, KAA_ERR_BADPARAM);

    kaa_error_t err = kaa_platform_message_write_extension_header(writer
                                                    , KAA_NOTIFICATION_EXTENSION_TYPE
                                                    , KAA_CLIENT_WANTS_TO_RECEIVE_NOTIFICATIONS
                                                    , self->extension_payload_size);
    if (err) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BADPARAM, "Failed to write notification header.");
        return KAA_ERR_BADPARAM;
    }

    *(uint32_t *)writer->current = KAA_HTONL((uint32_t)self->status->notification_seq_n);
    writer->current += sizeof(uint32_t);

    if (kaa_list_get_size(self->status->topic_states) > 0) {
        *(uint8_t *)writer->current = (uint8_t)TOPICS_STATE_ID;
        writer->current += sizeof(uint16_t);
        *(uint16_t *)writer->current = KAA_HTONS((uint16_t)kaa_list_get_size(self->status->topic_states));
        writer->current += sizeof(uint16_t);

        kaa_list_for_each(kaa_list_begin(self->status->topic_states)
                        , kaa_list_back(self->status->topic_states)
                        , (process_data)&serialize_topic_state
                        , writer);
    }

    if (kaa_list_get_size(self->uids) > 0) {
        KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Going to serialize uids.");
        *(uint8_t *)writer->current = (uint8_t)UID_ID;
        writer->current += sizeof(uint16_t);
        *(uint16_t *)writer->current = KAA_HTONS(kaa_list_get_size(self->uids));
        writer->current += sizeof(uint16_t);

        kaa_list_for_each(kaa_list_begin(self->uids)
                        , kaa_list_back(self->uids)
                        , (process_data)&serialize_notifications_uid
                        , writer);
    }

    serialize_subscriptions(writer, self->subscriptions, SUBSCRIPTION_ID);
    serialize_subscriptions(writer, self->unsubscriptions, UNSUBSCRIPTION_ID);

    return KAA_ERR_NONE;
}

static void destroy_notifications_uid(void *data)
{
   KAA_RETURN_IF_NIL(data,);
   kaa_notifications_uid_t *uid = (kaa_notifications_uid_t *)data;
   if (uid->data) {
       KAA_FREE(uid->data);
   }
   KAA_FREE(uid);
}

static void destroy_topic(void *data)
{
   KAA_RETURN_IF_NIL(data,);
   kaa_topic_t *topic = (kaa_topic_t *)data;
   if (topic->name) {
       KAA_FREE(topic->name);
   }
   KAA_FREE(data);
}

static void destroy_optional_listeners_wrapper(void *data)
{
    KAA_RETURN_IF_NIL(data,);
    kaa_optional_notification_listeners_wrapper_t *wrapper = (kaa_optional_notification_listeners_wrapper_t *)data;
    kaa_list_destroy(wrapper->listeners, NULL);
    KAA_FREE(wrapper);
}

void kaa_notification_manager_destroy(kaa_notification_manager_t *self)
{
    KAA_RETURN_IF_NIL(self,);

    kaa_list_destroy(self->mandatory_listeners, &kaa_data_destroy);
    kaa_list_destroy(self->topics_listeners, &kaa_data_destroy);
    kaa_list_destroy(self->subscriptions, &kaa_data_destroy);
    kaa_list_destroy(self->unsubscriptions, &kaa_data_destroy);
    kaa_list_destroy(self->optional_listeners, &destroy_optional_listeners_wrapper);
    kaa_list_destroy(self->uids, &destroy_notifications_uid);
    kaa_list_destroy(self->topics, &destroy_topic);

    KAA_FREE(self);
}

kaa_error_t kaa_notification_manager_create(kaa_notification_manager_t **self, kaa_status_t *status
                                          , kaa_channel_manager_t *channel_manager
                                          , kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL4(self, status, channel_manager, logger, KAA_ERR_BADPARAM);

    *self = (kaa_notification_manager_t *) KAA_MALLOC( sizeof(kaa_notification_manager_t) );
    KAA_RETURN_IF_NIL(self,KAA_ERR_NOMEM);

    (*self)->mandatory_listeners =  kaa_list_create();
    (*self)->topics_listeners    =  kaa_list_create();
    (*self)->optional_listeners  =  kaa_list_create();
    (*self)->subscriptions       =  kaa_list_create();
    (*self)->unsubscriptions     =  kaa_list_create();
    (*self)->topics              =  kaa_list_create();
    (*self)->uids                =  kaa_list_create();

    if (!(*self)->mandatory_listeners || !(*self)->topics_listeners || !(*self)->optional_listeners ||
            !(*self)->subscriptions || !(*self)->unsubscriptions || !(*self)->topics || !(*self)->uids)
    {
        kaa_notification_manager_destroy(*self);
        return KAA_ERR_NOMEM;
    }

    (*self)->extension_payload_size = 0;

    (*self)->status              =  status;
    (*self)->channel_manager     =  channel_manager;
    (*self)->logger              =  logger;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_add_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener, uint32_t* listener_id)
{
    KAA_RETURN_IF_NIL2(self, listener, KAA_ERR_BADPARAM);
    if (!listener->callback) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to add mandatory notification listener: NULL callback");
        return KAA_ERR_BADPARAM;
    }

    uint32_t id;
    kaa_error_t err = kaa_calculate_notification_listener_id(listener, &id);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to calculate mandatory listener id.");
        return err;
    }

    if (kaa_list_find_next(kaa_list_begin(self->mandatory_listeners), &kaa_find_notification_listener_by_id, &id)) {
        KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS, "Failed to add mandatory listener: listener is already subscribed.");
        return KAA_ERR_ALREADY_EXISTS;
    }

    kaa_notification_listener_wrapper_t* wrapper = (kaa_notification_listener_wrapper_t *)
                                                        KAA_MALLOC(sizeof(kaa_notification_listener_wrapper_t));
    KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);

    if (!kaa_list_push_front(self->mandatory_listeners, wrapper)) {
        KAA_FREE(wrapper);
        KAA_LOG_WARN(self->logger, KAA_ERR_NOMEM, "Failed to add mandatory listener.");
        return KAA_ERR_NOMEM;
    }

    wrapper->listener = *listener;
    wrapper->id = id; // for user to have convenient way to address notification listener*/

    if (listener_id) {
        *listener_id = id;
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_notification_listener(kaa_notification_manager_t *self, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL2(self, listener_id, KAA_ERR_BADPARAM);
    return kaa_list_remove_first(self->mandatory_listeners
                               , &kaa_find_notification_listener_by_id
                               , listener_id
                               , &kaa_data_destroy);;
}

kaa_error_t kaa_add_optional_notification_listener(kaa_notification_manager_t *self
                                                 , kaa_notification_listener_t *listener
                                                 , uint64_t *topic_id
                                                 , uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL4(self, listener, listener->callback, topic_id, KAA_ERR_BADPARAM);

    if (!kaa_list_find_next(kaa_list_begin(self->topics), &kaa_find_topic_by_id, topic_id)) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Failed to add optional notification listener: "
                                                            "topic with id = %lu not found", *topic_id);
        return KAA_ERR_NOT_FOUND;
    }

    uint32_t id;
    kaa_error_t err = kaa_calculate_notification_listener_id(listener, &id);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to calculate optional listener id.");
        return err;
    }

    kaa_notification_listener_wrapper_t *wrapper = (kaa_notification_listener_wrapper_t *) KAA_MALLOC(sizeof(kaa_notification_listener_wrapper_t));
    KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);

    kaa_list_node_t *opt_listeners_node = kaa_list_find_next(kaa_list_begin(self->optional_listeners)
                                                           , &kaa_find_optional_notification_listener_by_id
                                                           , topic_id);
    if (!opt_listeners_node) {
        kaa_optional_notification_listeners_wrapper_t* optional_wrapper = (kaa_optional_notification_listeners_wrapper_t *)
                                                                  KAA_MALLOC(sizeof(kaa_optional_notification_listeners_wrapper_t));
        if (!optional_wrapper) {
            KAA_FREE(wrapper);
            return KAA_ERR_NOMEM;
        }

        optional_wrapper->listeners = kaa_list_create();
        if (!kaa_list_push_front(optional_wrapper->listeners, wrapper)) {
            KAA_FREE(wrapper);
            destroy_optional_listeners_wrapper(optional_wrapper);
            return KAA_ERR_NOMEM;
        }

        if (!kaa_list_push_front(self->optional_listeners, optional_wrapper)) {
            KAA_FREE(wrapper);
            destroy_optional_listeners_wrapper(optional_wrapper);
            return KAA_ERR_NOMEM;
        }

        optional_wrapper->topic_id = *topic_id;
    } else {
        kaa_optional_notification_listeners_wrapper_t* optional_wrapper =
                (kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(opt_listeners_node);

        if (kaa_list_find_next(kaa_list_begin(optional_wrapper->listeners), &kaa_find_notification_listener_by_id, &id)) {
            KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS, "Failed to add the optional listener: the listener is already subscribed.");
            KAA_FREE(wrapper);
            return KAA_ERR_ALREADY_EXISTS;
        }

        if (!kaa_list_push_front(optional_wrapper->listeners, wrapper)) {
            KAA_FREE(wrapper);
            return KAA_ERR_NOMEM;
        }
    }

    wrapper->listener = *listener;
    wrapper->id = id; // for user to have convenient way to address notification listener
    if (listener_id) {
        *listener_id = id;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "The optional listener with id = %lu has been added", id);

    return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_optional_notification_listener(kaa_notification_manager_t *self, uint64_t *topic_id, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL3(self, topic_id, listener_id, KAA_ERR_BADPARAM);

    kaa_list_node_t *opt_listeners_node = kaa_list_find_next(kaa_list_begin(self->optional_listeners)
                                                           , &kaa_find_optional_notification_listener_by_id
                                                           , topic_id);
    if (!opt_listeners_node) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Failed to remove the optional listener: there is no listeners subscribed on this topic (topic id = %lu).", *topic_id);
        return KAA_ERR_NOT_FOUND;
    }

    kaa_optional_notification_listeners_wrapper_t *optional_wrapper =
            (kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(opt_listeners_node);

    kaa_error_t error = kaa_list_remove_first(optional_wrapper->listeners
                                            , &kaa_find_notification_listener_by_id
                                            , listener_id
                                            , &kaa_data_destroy);
    if (error) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Failed to remove the optional listener: the listener with id = %lu is not found.", *listener_id);
        return error;
    } else {
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "The optional listener with id = %lu has been removed.", *listener_id);
        if (!kaa_list_get_size(optional_wrapper->listeners)) {
            kaa_list_remove_at(self->optional_listeners, opt_listeners_node, &destroy_optional_listeners_wrapper);
        }
        return KAA_ERR_NONE;
    }
}

kaa_error_t kaa_add_topic_list_listener(kaa_notification_manager_t *self, kaa_topic_listener_t *listener, uint32_t *topic_listener_id)
{
    KAA_RETURN_IF_NIL3(self, listener, listener->callback, KAA_ERR_BADPARAM);

    uint32_t id;
    kaa_error_t err = kaa_calculate_topic_listener_id(listener, &id);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to calculate mandatory listener's id.");
        return err;
    }

    kaa_topic_listener_wrapper_t *wrapper = (kaa_topic_listener_wrapper_t *) KAA_MALLOC(sizeof(kaa_topic_listener_wrapper_t));
    KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);

    if (kaa_list_find_next(kaa_list_begin(self->topics_listeners), &kaa_find_topic_listener_by_id, &id)) {
        KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS, "Failed to add topic the listener: the listener is already subscribed.");
        KAA_FREE(wrapper);
        return KAA_ERR_ALREADY_EXISTS;
    }

    if (!kaa_list_push_front(self->topics_listeners, wrapper)) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to add the topic listener.");
        KAA_FREE(wrapper);
        return KAA_ERR_NOMEM;
    }

    wrapper->listener = *listener;
    wrapper->id = id;// for user to have convenient way to address notification listener
    if (topic_listener_id) {
        *topic_listener_id = id;
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_topic_list_listener(kaa_notification_manager_t *self, uint32_t *topic_listener_id)
{
    KAA_RETURN_IF_NIL2(self, topic_listener_id, KAA_ERR_BADPARAM);
    return kaa_list_remove_first(self->topics_listeners
                               , &kaa_find_topic_listener_by_id
                               , topic_listener_id
                               , &kaa_data_destroy);
}

kaa_error_t kaa_get_topics(kaa_notification_manager_t *self, kaa_list_t **topics)
{
    KAA_RETURN_IF_NIL2(self, topics, KAA_ERR_BADPARAM);
    *topics = self->topics;
    return KAA_ERR_NONE;
}

static void notify_topic_update_subscriber(kaa_topic_listener_wrapper_t *wrapper, kaa_list_t *topics)
{
    KAA_RETURN_IF_NIL2(wrapper, topics, );
    wrapper->listener.callback(wrapper->listener.context, topics);
}

static kaa_error_t kaa_notify_topic_update_subscribers(kaa_notification_manager_t *self, kaa_list_t *topics)
{
    KAA_RETURN_IF_NIL2(self, topics, KAA_ERR_BADPARAM);
    kaa_list_for_each(kaa_list_begin(self->topics_listeners)
                    , kaa_list_back(self->topics_listeners)
                    , (process_data)notify_topic_update_subscriber
                    , topics);
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_notify_mandatory_notification_subscribers(kaa_notification_manager_t *self
                                                               , uint64_t *topic_id
                                                               , kaa_notification_t *notification)
{
    KAA_RETURN_IF_NIL2(self, notification, KAA_ERR_BADPARAM);
    kaa_notification_listener_wrapper_t *wrapper = NULL;
    kaa_list_node_t *current_listener_node = kaa_list_begin(self->mandatory_listeners);
    while (current_listener_node) {
        wrapper = (kaa_notification_listener_wrapper_t *) kaa_list_get_data(current_listener_node);
        wrapper->listener.callback(wrapper->listener.context, topic_id, notification);
        current_listener_node = kaa_list_next(current_listener_node);
    }
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_notify_optional_notification_subscribers(kaa_notification_manager_t *self
                                                              , uint64_t *topic_id
                                                              , kaa_notification_t *notification)
{
    KAA_RETURN_IF_NIL3(self, topic_id, notification, KAA_ERR_BADPARAM);
    kaa_list_node_t *opt_listeners_node = kaa_list_find_next(kaa_list_begin(self->optional_listeners)
                                                           , &kaa_find_optional_notification_listener_by_id
                                                           , topic_id);
    KAA_RETURN_IF_NIL(opt_listeners_node, KAA_ERR_NOT_FOUND);

    kaa_optional_notification_listeners_wrapper_t* optional_wrapper =
            (kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(opt_listeners_node);

    kaa_notification_listener_wrapper_t *wrapper = NULL;
    kaa_list_node_t *optional_listener_node = kaa_list_begin(optional_wrapper->listeners);
    while (optional_listener_node) {
        wrapper = (kaa_notification_listener_wrapper_t *) kaa_list_get_data(optional_listener_node);
        wrapper->listener.callback(wrapper->listener.context, topic_id, notification);
        optional_listener_node = kaa_list_next(optional_listener_node);
    }
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_add_subscribtion_or_unsubscribtion(kaa_list_t *target, uint64_t *topic_id)
{
    KAA_RETURN_IF_NIL2(target, topic_id, KAA_ERR_BADPARAM);
    uint64_t *subs_topic_id = (uint64_t *) KAA_MALLOC(sizeof(uint64_t));
    KAA_RETURN_IF_NIL(subs_topic_id, KAA_ERR_NOMEM);
    *subs_topic_id = *topic_id;
    if (!kaa_list_push_front(target, subs_topic_id)) {
        KAA_FREE(subs_topic_id);
        return KAA_ERR_NOMEM;
    }

    return KAA_ERR_NONE;
}

static kaa_error_t kaa_add_subscribtions_or_unsubscribtions(kaa_list_t *target, uint64_t *topic_ids, size_t size)
{
    KAA_RETURN_IF_NIL2(target, topic_ids, KAA_ERR_BADPARAM);
    kaa_list_t *new_subscriptions = kaa_list_create();
    KAA_RETURN_IF_NIL(new_subscriptions, KAA_ERR_NOMEM);

    while (size--) {
         uint64_t *subs_topic_id = (uint64_t *) KAA_MALLOC(sizeof(uint64_t));
         KAA_RETURN_IF_NIL(subs_topic_id, KAA_ERR_NOMEM);
         *subs_topic_id = topic_ids[size];
         if (!kaa_list_push_front(new_subscriptions, subs_topic_id)) {
             KAA_FREE(subs_topic_id);
             kaa_list_destroy(new_subscriptions, &kaa_data_destroy);
             return KAA_ERR_NOMEM;
         }
   }
   kaa_lists_merge(target, new_subscriptions);
   kaa_list_destroy(new_subscriptions, NULL);
   return KAA_ERR_NONE;
}
kaa_error_t kaa_subscribe_to_topic(kaa_notification_manager_t *self, uint64_t *topic_id, bool force_sync)
{
    KAA_RETURN_IF_NIL2(self, topic_id, KAA_ERR_BADPARAM);
    kaa_topic_t* topic = NULL;
    kaa_error_t err = kaa_find_topic(self, &topic, topic_id);
    if (err) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topic with id = %lu.", *topic_id);
        return err;
    }
    if (topic->subscription_type == MANDATORY_SUBSCRIPTION) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topic with id = %lu. Topic isn't optional.", *topic_id);
        return KAA_ERR_BADPARAM;
    }
    err = kaa_add_subscribtion_or_unsubscribtion(self->subscriptions, topic_id);
    KAA_RETURN_IF_ERR(err);

    if (force_sync) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to subscribe to topic %lu", *topic_id);
        kaa_sync_topic_subscriptions(self);
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Subscription to topic %lu is postponed till sync", *topic_id);
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_subscribe_to_topics(kaa_notification_manager_t *self, uint64_t *topic_ids, size_t size, bool force_sync)
{
    KAA_RETURN_IF_NIL2(self, topic_ids, KAA_ERR_BADPARAM);
    kaa_topic_t *topic = NULL;
    kaa_error_t err = KAA_ERR_NONE;
    size_t size_copy = size;
    while (size_copy--) {
        err = kaa_find_topic(self, &topic, topic_ids + size_copy);
        if (err) {
            KAA_LOG_WARN(self->logger, err, "Failed to subscribe to the topic with id = %lu.", topic_ids[size_copy]);
            return err;
        } else {
            if (topic->subscription_type == MANDATORY_SUBSCRIPTION) {
                KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topics. Topic with id = %lu. Topic isn't optional.", topic_ids[size_copy]);
                return KAA_ERR_BADPARAM;
            }
        }
    }
    err = kaa_add_subscribtions_or_unsubscribtions(self->subscriptions, topic_ids, size);
    KAA_RETURN_IF_ERR(err);

    if (force_sync) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to subscribe to the topics");
        kaa_sync_topic_subscriptions(self);
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Subscription to topics is postponed till sync");
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_unsubscribe_from_topic(kaa_notification_manager_t *self, uint64_t *topic_id, bool force_sync)
{
    KAA_RETURN_IF_NIL2(self, topic_id, KAA_ERR_BADPARAM);
    kaa_topic_t* topic = NULL;
    kaa_error_t err = kaa_find_topic(self, &topic, topic_id);
    if (err) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topic with id = %lu.", *topic_id);
        return err;
    }
    if (topic->subscription_type == MANDATORY_SUBSCRIPTION) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topic with id = %lu. Topic isn't optional.", *topic_id);
        return KAA_ERR_BADPARAM;
    }
    err = kaa_add_subscribtion_or_unsubscribtion(self->unsubscriptions, topic_id);
    KAA_RETURN_IF_ERR(err);

    if (force_sync) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to unsubscribe from the topic %lu", *topic_id);
        kaa_sync_topic_subscriptions(self);
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Unsubscription from the topic %lu is postponed till sync", *topic_id);
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_unsubscribe_from_topics(kaa_notification_manager_t *self, uint64_t *topic_ids, size_t size, bool force_sync)
{
    KAA_RETURN_IF_NIL2(self, topic_ids, KAA_ERR_BADPARAM);
    kaa_topic_t *topic = NULL;
    kaa_error_t err = KAA_ERR_NONE;
    size_t size_copy = size;
    while (size_copy--) {
        err = kaa_find_topic(self, &topic, topic_ids + size_copy);
        if (err) {
            KAA_LOG_WARN(self->logger, err, "Failed to unsubscribe from the topic with id = %lu.", topic_ids[size_copy]);
            return err;
        } else {
            if (topic->subscription_type == MANDATORY_SUBSCRIPTION) {
                KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topics. Topic with id = %lu. Topic isn't optional.", topic_ids[size_copy]);
                return KAA_ERR_BADPARAM;
            }
        }
    }
    err = kaa_add_subscribtions_or_unsubscribtions(self->unsubscriptions, topic_ids, size);
    KAA_RETURN_IF_ERR(err);

    if (force_sync) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to unsubscribe from the topics");
        kaa_sync_topic_subscriptions(self);
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Unsubscription from the topics is postponed till sync");
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_sync_topic_subscriptions(kaa_notification_manager_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM)
    kaa_transport_channel_interface_t *channel =
                 kaa_channel_manager_get_transport_channel(self->channel_manager, notification_sync_services[0]);
    if (channel) {
        channel->sync_handler(channel->context, notification_sync_services, 1);
    } else {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BADDATA, "Filed to retrieve channel");
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_topic_list_updated(kaa_notification_manager_t *self, kaa_list_t *topics)
{
    KAA_RETURN_IF_NIL2(self, topics, KAA_ERR_BADPARAM);
    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "New list of available topics received (topic_count = %lu).", kaa_list_get_size(topics));
    kaa_topic_t *topic = NULL;
    kaa_list_node_t  *topic_node = kaa_list_begin(topics);
    while (topic_node && self->topics) {
        topic = (kaa_topic_t *) kaa_list_get_data(topic_node);
        KAA_RETURN_IF_NIL(topic, KAA_ERR_NOMEM);
        kaa_list_remove_first(self->topics, &kaa_find_topic_by_id, &topic->id, &destroy_topic);
        topic_node = kaa_list_next(topic_node);
    }
    if (kaa_list_get_size(self->topics)) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to remove optional listener(s) from obsolete %lu topics", self->topics);
        kaa_list_node_t *outdated_topics = kaa_list_begin(self->topics);
        while (outdated_topics) {
            topic = (kaa_topic_t *) kaa_list_get_data(outdated_topics);
            kaa_list_remove_first(self->optional_listeners
                                , &kaa_find_optional_notification_listener_by_id
                                , &topic->id
                                , &destroy_optional_listeners_wrapper);
            outdated_topics = kaa_list_next(outdated_topics);
        }
    }

    kaa_list_destroy(self->topics, &destroy_topic);
    self->topics = topics;
    return kaa_notify_topic_update_subscribers(self, topics);
}

kaa_error_t kaa_notification_received(kaa_notification_manager_t *self, kaa_notification_t *notification, uint64_t* topic_id)
{
    KAA_RETURN_IF_NIL2(self, notification, KAA_ERR_BADDATA);
    kaa_topic_t* topic = NULL;
    kaa_error_t err = kaa_find_topic(self, &topic, topic_id);
    if (err) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Unknown notification received(topic id = %lu) ", *topic_id);
    }
    if (kaa_notify_optional_notification_subscribers(self, topic_id, notification)) {
        kaa_notify_mandatory_notification_subscribers(self,topic_id, notification);
    }
    return KAA_ERR_NONE;
}
kaa_error_t kaa_notification_manager_handle_server_sync(kaa_notification_manager_t *self
                                                      , kaa_platform_message_reader_t *reader
                                                      , uint32_t extension_length)
{
    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);

    kaa_list_clear(self->subscriptions, &kaa_data_destroy);
    kaa_list_clear(self->unsubscriptions, &kaa_data_destroy);
    kaa_list_clear(self->uids, &destroy_notifications_uid);

    kaa_error_t err = KAA_ERR_NONE;
    if (extension_length > 0) {
        self->status->notification_seq_n = KAA_NTOHL(*((uint32_t *) reader->current)); // State SQN
        shift_and_sub_extension(reader, &extension_length, sizeof(uint32_t));
        uint8_t field_id = 0;
        while (extension_length > 0) {
            field_id = *((uint8_t *) reader->current); //field id
            shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t)); // + reserved
            switch (field_id) {
            case NOTIFICATIONS: {
                kaa_notification_t *notification = NULL;
                uint16_t uid_length = 0;
                uint64_t topic_id = 0;
                kaa_topic_t *topic_found = NULL;
                uint32_t seq_number = 0;
                uint32_t notification_size = 0;
                uint16_t notifications_count = KAA_NTOHS(*((uint16_t *) reader->current)); // notifications count
                shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t));
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Notifications count is %u", notifications_count);
                while (notifications_count--) {
                    if (!notifications_count) {
                        seq_number = KAA_NTOHL(*((uint32_t *) reader->current)); // sqn of the last received notification
                    }
                    shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t) + sizeof(uint32_t)); // + notification type
                    uid_length = KAA_NTOHS(*((uint16_t *) reader->current)); // uid length
                    shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t));
                    notification_size = KAA_NTOHL(*((uint32_t *) reader->current)); // notification body size
                    shift_and_sub_extension(reader, &extension_length, sizeof(uint32_t));
                    topic_id =  KAA_NTOHLL(*((uint64_t *) reader->current)); // topic id
                    shift_and_sub_extension(reader, &extension_length, sizeof(uint64_t));
                    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Topic id is %lu", topic_id);
                    err = kaa_find_topic(self, &topic_found, &topic_id);
                    if (err) {
                        size_t skiped_size = kaa_aligned_size_get(uid_length) + kaa_aligned_size_get(notification_size);
                        shift_and_sub_extension(reader, &extension_length, skiped_size);
                        continue;
                    }
                    if (uid_length) {
                        kaa_notifications_uid_t *uid = (kaa_notifications_uid_t *) KAA_MALLOC(sizeof(kaa_notifications_uid_t));
                        KAA_RETURN_IF_NIL(uid, KAA_ERR_NOMEM);

                        uid->length = uid_length;
                        uid->data = KAA_MALLOC(uid_length);
                        if (!uid->data) {
                            destroy_notifications_uid(uid);
                            return KAA_ERR_NOMEM;
                        }
                        err = kaa_platform_message_read_aligned(reader, uid->data, uid->length);
                        if (err) {
                            KAA_LOG_WARN(self->logger, KAA_ERR_BADDATA, "Failed to read UID body");
                        }
                        extension_length -= kaa_aligned_size_get(uid->length);

                        if (!kaa_list_push_front(self->uids, uid)) {
                            destroy_notifications_uid(uid);
                            return KAA_ERR_NOMEM;
                        }
                    }
                    if (notification_size) {
                        avro_reader_t avro_reader = avro_reader_memory(reader->current, notification_size);
                        if (!avro_reader) {
                            return KAA_ERR_NOMEM;
                        }
                        notification = KAA_NOTIFICATION_DESERIALIZE(avro_reader);
                        avro_reader_free(avro_reader);
                        if (!notification) {
                            KAA_LOG_ERROR(self->logger, KAA_ERR_NONE, "Failed to deserialize notification.");
                            return KAA_ERR_NOMEM;
                        }
                    }
                    shift_and_sub_extension(reader, &extension_length, kaa_aligned_size_get(notification_size));
                    if (!notifications_count) {
                        kaa_list_node_t *state_found = kaa_list_find_next(kaa_list_begin(self->status->topic_states)
                                                                        , &kaa_find_topic_state_by_id
                                                                        , &topic_id);
                        if (!state_found) {
                            kaa_topic_state_t *state = (kaa_topic_state_t *) KAA_MALLOC(sizeof(kaa_topic_state_t));
                            if (!state) {
                                notification->destroy(notification);
                                return KAA_ERR_NOMEM;
                            }

                            state->sqn_number = seq_number;
                            state->topic_id = topic_id;

                            if (!kaa_list_push_front(self->status->topic_states, state)) {
                                notification->destroy(notification);
                                KAA_FREE(state);
                                return KAA_ERR_NOMEM;
                            }
                        } else {
                            ((kaa_topic_state_t *)kaa_list_get_data(state_found))->sqn_number = seq_number;
                        }
                    }
                    err = kaa_notification_received(self, notification, &topic_id);
                    notification->destroy(notification);
                    if (err) {
                        KAA_LOG_WARN(self->logger, err, "Failed to notify listeners.");
                    }
                }
                break;
            }
            case TOPICS: {
                kaa_list_t *new_topics = kaa_list_create();
                KAA_RETURN_IF_NIL(new_topics, KAA_ERR_NOMEM);

                uint16_t topic_count = KAA_NTOHS(*((uint16_t *) reader->current)); // topics count
                KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Topic count is %u", topic_count);
                shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t));

                kaa_topic_t *topic = NULL;
                while (topic_count--) {
                   topic = (kaa_topic_t *) KAA_MALLOC(sizeof(kaa_topic_t));
                   KAA_RETURN_IF_NIL(topic, KAA_ERR_NOMEM);
                   topic->id = KAA_NTOHLL(*((uint64_t *) reader->current)); // topic id
                   shift_and_sub_extension(reader, &extension_length, sizeof(uint64_t));
                   topic->subscription_type = (*((uint8_t *) reader->current) == MANDATORY_SUBSCRIPTION) ? MANDATORY_SUBSCRIPTION : OPTIONAL_SUBSCRIPTION;
                   shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t)); // + reserved
                   topic->name_length = KAA_NTOHS(*((uint16_t *) reader->current)); // name length
                   shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t));

                   topic->name = (char *) KAA_MALLOC(topic->name_length + 1); // Topic name
                   if (!topic->name) {
                       destroy_topic(topic);
                       return KAA_ERR_NOMEM;
                   }

                   err = kaa_platform_message_read_aligned(reader, topic->name, topic->name_length);
                   if (err) {
                       KAA_LOG_WARN(self->logger, KAA_ERR_BADDATA, "Failed to read topic's name");
                       destroy_topic(topic);
                       return err;
                   }

                   topic->name[topic->name_length] = '\0';
                   extension_length -= kaa_aligned_size_get(topic->name_length);

                   if (!kaa_list_push_front(new_topics, topic)) {
                       destroy_topic(topic);
                       return KAA_ERR_NOMEM;
                   }
                }
                err = kaa_topic_list_updated(self, new_topics);
                if (err) {
                    KAA_LOG_WARN(self->logger, err, "Failed to notify topic list listeners");
                }
                break;
            }
            default:
                KAA_LOG_ERROR(self->logger, KAA_ERR_NONE, "Bad field ID type.");
                return KAA_ERR_BADDATA;
            }
        }
    }
    return err;
}
#endif

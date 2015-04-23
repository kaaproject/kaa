
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

typedef struct {
    uint32_t seq_num;
    kaa_topic_subscription_type_t type;
    uint32_t size;
    uint64_t topic_id;
    kaa_notification_t *notification_body;
} kaa_notification_wrapper_t;

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

static kaa_service_t notification_sync_services[1] = {KAA_SERVICE_NOTIFICATION};

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
    kaa_list_t *topic_node = kaa_list_find_next(self->topics, &kaa_find_topic_by_id, topic_id);
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

kaa_error_t kaa_notification_manager_get_size(kaa_notification_manager_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);
    *expected_size = 0;
    *expected_size += sizeof(uint32_t); //state sqn size

    if (self->status->topic_states) {
        *expected_size += sizeof(uint32_t); //field id + reserved + count
        *expected_size += (sizeof(uint64_t) + sizeof(uint32_t)) * kaa_list_get_size(self->status->topic_states);
    }

    if (self->uids) {
        *expected_size += sizeof(uint32_t); //field id + reserved + count
        kaa_list_t *uid_node = self->uids;
        size_t uids_size = sizeof(uint32_t) * kaa_list_get_size(self->uids);
        while (uid_node) {
            kaa_notifications_uid_t *uid = (kaa_notifications_uid_t *)kaa_list_get_data(uid_node);
            uids_size += kaa_aligned_size_get(uid->length);
            uid_node = kaa_list_next(uid_node);
        }
        *expected_size += uids_size;
    }

    if (self->subscriptions) {
        *expected_size += sizeof(uint32_t); //meta data for subscribe commands
        *expected_size += kaa_list_get_size(self->subscriptions) * sizeof(uint64_t);
    }
    if (self->unsubscriptions) {
        *expected_size += sizeof(uint32_t); //meta data for unsubscribe commands
        *expected_size += kaa_list_get_size(self->unsubscriptions) * sizeof(uint64_t);
    }
    self->extension_payload_size = *expected_size;
    *expected_size += KAA_EXTENSION_HEADER_SIZE;
    return KAA_ERR_NONE;
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

    if (self->status->topic_states) {
        *(uint8_t *)writer->current = (uint8_t)TOPICS_STATE_ID;
        writer->current += sizeof(uint16_t);
        *(uint16_t *)writer->current = KAA_HTONS((uint16_t)kaa_list_get_size(self->status->topic_states));
        writer->current += sizeof(uint16_t);
        kaa_list_t *topic_state_node = self->status->topic_states;
        while (topic_state_node) {
            kaa_topic_state_t *state = (kaa_topic_state_t *)kaa_list_get_data(topic_state_node);
            KAA_RETURN_IF_NIL(state, KAA_ERR_BADDATA);
            *(uint64_t *)writer->current = KAA_HTONLL(state->topic_id);
            writer->current += sizeof(uint64_t);
            *(uint32_t *)writer->current = KAA_HTONL((uint32_t)state->sqn_number);
            writer->current += sizeof(uint32_t);
            topic_state_node = kaa_list_next(topic_state_node);
        }
    }
    if (self->uids) {
        KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Going to serialize uids.");
        *(uint8_t *)writer->current = (uint8_t)UID_ID;
        writer->current += sizeof(uint16_t);
        *(uint16_t *)writer->current = KAA_HTONS(kaa_list_get_size(self->uids));
        writer->current += sizeof(uint16_t);
        kaa_list_t *uid_node = self->uids;
        while (uid_node) {
            kaa_notifications_uid_t * uid = (kaa_notifications_uid_t *)kaa_list_get_data(uid_node);
            KAA_RETURN_IF_NIL(uid, KAA_ERR_BAD_STATE);
            *(uint32_t *)writer->current = KAA_HTONL(uid->length);
            writer->current += sizeof(uint32_t);
            err = kaa_platform_message_write_aligned(writer, uid->data, uid->length);
            if (err) {
                KAA_LOG_ERROR(self->logger, KAA_ERR_BADDATA, "Filed to serialize uids.");
                return KAA_ERR_BADDATA;
            }
            uid_node = kaa_list_next(uid_node);
        }
    }

    if (self->subscriptions) {
        KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Going to serialize subscriptions.");
        *(uint8_t *)writer->current = (uint8_t)SUBSCRIPTION_ID;
        writer->current += sizeof(uint16_t);
        *(uint16_t *)writer->current = KAA_HTONS((uint16_t)kaa_list_get_size(self->subscriptions));
        writer->current += sizeof(uint16_t);
        kaa_list_t *subscription_node = self->subscriptions;
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to serialize %d subscriptions.", kaa_list_get_size(subscription_node));
        while (subscription_node) {
            *(uint64_t *)writer->current = KAA_HTONLL(*(uint64_t *)kaa_list_get_data(subscription_node));
            writer->current += sizeof(uint64_t);
            subscription_node = kaa_list_next(subscription_node);
        }
    }

    if (self->unsubscriptions) {
        KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Going to serialize unsubscriptions.");
        *(uint8_t *)writer->current = (uint8_t)UNSUBSCRIPTION_ID;
        writer->current += sizeof(uint16_t);
        *(uint16_t *)writer->current = KAA_HTONS((uint16_t)kaa_list_get_size(self->unsubscriptions));
        writer->current += sizeof(uint16_t);
        kaa_list_t *unsubscription_node = self->unsubscriptions;
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to serialize %d subscriptions.", kaa_list_get_size(unsubscription_node));
        while (unsubscription_node) {
            *(uint64_t *)writer->current = KAA_HTONLL(*(uint64_t *)kaa_list_get_data(unsubscription_node));
            writer->current += sizeof(uint64_t);
            unsubscription_node = kaa_list_next(unsubscription_node);
        }
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_notification_manager_create(kaa_notification_manager_t **self, kaa_status_t *status
                                          , kaa_channel_manager_t *channel_manager
                                          , kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL4(self, status, channel_manager, logger, KAA_ERR_BADPARAM);

    *self = (kaa_notification_manager_t *) KAA_MALLOC( sizeof(kaa_notification_manager_t) );
    KAA_RETURN_IF_NIL(self,KAA_ERR_NOMEM);

    (*self)->mandatory_listeners =  NULL;
    (*self)->topics_listeners    =  NULL;
    (*self)->optional_listeners  =  NULL;
    (*self)->subscriptions       =  NULL;
    (*self)->unsubscriptions     =  NULL;
    (*self)->topics              =  NULL;
    (*self)->uids                =  NULL;

    (*self)->extension_payload_size = 0;

    (*self)->status              =  status;
    (*self)->channel_manager     =  channel_manager;
    (*self)->logger              =  logger;

    return KAA_ERR_NONE;
}


void kaa_uids_destroy(void *data)
{
   KAA_RETURN_IF_NIL(data,);
   kaa_notifications_uid_t *uid = (kaa_notifications_uid_t *)data;
   KAA_RETURN_IF_NIL(uid->data,);
   KAA_FREE(uid->data);
   KAA_FREE(uid);
}

void kaa_topics_destroy(void *data)
{
   KAA_RETURN_IF_NIL(data,);
   kaa_topic_t *topic = (kaa_topic_t *)data;
   KAA_RETURN_IF_NIL(topic->name,);
   KAA_FREE(topic->name);
   KAA_FREE(data);
}

void kaa_notification_manager_destroy(kaa_notification_manager_t *self)
{
    KAA_RETURN_IF_NIL(self,);
    if (self->mandatory_listeners) {
        kaa_list_destroy(self->mandatory_listeners, &kaa_data_destroy);
    }
    if (self->topics_listeners) {
        kaa_list_destroy(self->topics_listeners, &kaa_data_destroy);
    }
    if(self->subscriptions) {
        kaa_list_destroy(self->subscriptions, &kaa_data_destroy);
    }
    if(self->unsubscriptions) {
        kaa_list_destroy(self->unsubscriptions, &kaa_data_destroy);
    }
    if (self->optional_listeners) {
        kaa_list_t* wrappers = self->optional_listeners;
        while (wrappers) {
             kaa_optional_notification_listeners_wrapper_t* wrapper = (kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(wrappers);
             kaa_list_destroy(wrapper->listeners, &kaa_data_destroy);
             wrappers = kaa_list_next(wrappers);
        }
        kaa_list_destroy(self->optional_listeners, &kaa_data_destroy);
    }
    if (self->uids) {
        kaa_list_destroy(self->uids, &kaa_uids_destroy);
    }
    if (self->topics) {
        kaa_list_destroy(self->topics, &kaa_topics_destroy);
    }

    KAA_FREE(self);
}

kaa_error_t kaa_add_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener, uint32_t* listener_id)
{
    KAA_RETURN_IF_NIL2(self, listener, KAA_ERR_BADPARAM);
    if (!listener->callback) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BADPARAM, "Expected callback.Got NULL.");
        return KAA_ERR_BADPARAM;
    }
    uint32_t id;
    kaa_error_t err = kaa_calculate_notification_listener_id(listener, &id);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to calculate mandatory listener id.");
        return err;
    }
    if (kaa_list_find_next(self->mandatory_listeners, &kaa_find_notification_listener_by_id, &id)) {
        KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS, "Failed to add mandatory listener: listener is already subscribed.");
        return KAA_ERR_ALREADY_EXISTS;
    }
    kaa_notification_listener_wrapper_t* wrapper = (kaa_notification_listener_wrapper_t *) KAA_MALLOC(sizeof(kaa_notification_listener_wrapper_t));
    KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);
    wrapper->listener = *listener;
    kaa_list_t *new_listener = NULL;
    if (!self->mandatory_listeners) {
        new_listener = kaa_list_create(wrapper);
    } else {
        new_listener = kaa_list_push_front(self->mandatory_listeners, wrapper);
    }
    if (!new_listener) {
        KAA_FREE(wrapper);
        KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS, "Failed to add mandatory listener.");
        return KAA_ERR_NOMEM;
    }
    self->mandatory_listeners = new_listener;
    *listener_id = wrapper->id = id;; // for user to have convenient way to address notification listener*/
    return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_notification_listener(kaa_notification_manager_t *self, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL2(self, listener_id, KAA_ERR_BADPARAM);
    kaa_error_t err = kaa_list_remove_first(&self->mandatory_listeners, &kaa_find_notification_listener_by_id, listener_id, &kaa_data_destroy);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to remove mandatory listener: listener with id = %lu is not found.", *listener_id);
    }
    if (!kaa_list_get_size(self->mandatory_listeners)) {
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Mandatory listeners list is empty now.");
    }
    return err;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_add_optional_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener
                                                 , uint64_t *topic_id, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL4(self, listener, topic_id, listener_id, KAA_ERR_BADPARAM);
    if (!listener->callback) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BADPARAM, "Expected callback.Got NULL.");
        return KAA_ERR_BADPARAM;
    }
    kaa_list_t *topic = kaa_list_find_next(self->topics, &kaa_find_topic_by_id, topic_id);
    if (!topic) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOT_FOUND, "Topic with id = %lu not found.", *topic_id);
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
    wrapper->listener = *listener;
    kaa_list_t *opt_listeners_node  = kaa_list_find_next(self->optional_listeners, &kaa_find_optional_notification_listener_by_id, topic_id);
       if (!opt_listeners_node) {
           kaa_optional_notification_listeners_wrapper_t* optional_wrapper = (kaa_optional_notification_listeners_wrapper_t *)
                                                                     KAA_MALLOC(sizeof(kaa_optional_notification_listeners_wrapper_t));
           if (!optional_wrapper) {
               KAA_FREE(wrapper);
               return KAA_ERR_NOMEM;
           }
           optional_wrapper->listeners = kaa_list_create(wrapper);
           if (!optional_wrapper->listeners) {
               KAA_FREE(optional_wrapper);
               KAA_FREE(wrapper);
               return KAA_ERR_NOMEM;
           }
           if (!self->optional_listeners) {
               self->optional_listeners = kaa_list_create(optional_wrapper);
               if (!self->optional_listeners) {
                   KAA_FREE(optional_wrapper);
                   KAA_FREE(wrapper);
                   return KAA_ERR_NOMEM;
               }
           } else {
               kaa_list_t * new_opt_topic_listener_list = kaa_list_push_front(self->optional_listeners, optional_wrapper);
               if (!new_opt_topic_listener_list) {
                   KAA_FREE(wrapper);
                   KAA_FREE(optional_wrapper);
                   return KAA_ERR_NOMEM;
               }
               self->optional_listeners = new_opt_topic_listener_list;
           }
           optional_wrapper->topic_id = *topic_id;
           *listener_id = wrapper->id = id; // for user to have convenient way to address notification listener
       } else {
           kaa_optional_notification_listeners_wrapper_t* optional_wrapper = (kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(opt_listeners_node);
           if (kaa_list_find_next(optional_wrapper->listeners, &kaa_find_notification_listener_by_id, &id)) {
               KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS, "Failed to add the optional listener: the listener is already subscribed.");
               KAA_FREE(wrapper);
               return KAA_ERR_ALREADY_EXISTS;
           }
           kaa_list_t *new_opt_topic_listener_list = kaa_list_push_front(optional_wrapper->listeners, wrapper);
           if (!new_opt_topic_listener_list) {
               KAA_FREE(wrapper);
               return KAA_ERR_NOMEM;
           }
           optional_wrapper->listeners = new_opt_topic_listener_list;
           *listener_id = wrapper->id = id; // for user to have convenient way to address notification listener
       }
       KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "The optional listener with id = %lu has been added.", *listener_id);
       return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_optional_notification_listener(kaa_notification_manager_t *self, uint64_t *topic_id, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL3(self, topic_id, listener_id, KAA_ERR_BADPARAM);
    kaa_list_t *topic = kaa_list_find_next(self->topics, &kaa_find_topic_by_id, topic_id);
    if (!topic) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOT_FOUND, "Topic with id = %u not found.", *topic_id);
        return KAA_ERR_NOT_FOUND;
    }
    kaa_list_t *opt_listeners_node = kaa_list_find_next(self->optional_listeners, &kaa_find_optional_notification_listener_by_id, topic_id);
    if (!opt_listeners_node) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Failed to remove the optional listener: there is no listeners subscribed on this topic (topic id = %lu).", *topic_id);
        return KAA_ERR_NOT_FOUND;
    }
    kaa_optional_notification_listeners_wrapper_t* optional_wrapper = (kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(opt_listeners_node);
    kaa_error_t error = kaa_list_remove_first(&optional_wrapper->listeners, &kaa_find_notification_listener_by_id, listener_id, &kaa_data_destroy);
    if (error) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Failed to remove the optional listener: the listener with id = %lu is not found.", *listener_id);
        return error;
    } else {
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "The optional listener with id = %lu has been removed.", *listener_id);
        if (!kaa_list_get_size(optional_wrapper->listeners)) {
            if (!kaa_list_remove_at(&self->optional_listeners, opt_listeners_node, &kaa_data_destroy)) {
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "The optional listeners list is empty.");
                self->optional_listeners = NULL;
                return KAA_ERR_NONE;
            }
        }
        return KAA_ERR_NONE;
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_add_topic_list_listener(kaa_notification_manager_t *self, kaa_topic_listener_t *listener, uint32_t *topic_listener_id)
{
    KAA_RETURN_IF_NIL2(self, listener, KAA_ERR_BADPARAM);
    if (!listener->callback) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BADPARAM, "Expected callback.Got NULL.");
        return KAA_ERR_BADPARAM;
    }
    uint32_t id;
    kaa_error_t err = kaa_calculate_topic_listener_id(listener, &id);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to calculate mandatory listener's id.");
        return err;
    }
    kaa_topic_listener_wrapper_t *wrapper = NULL;
    kaa_list_t *new_listener = NULL;
    wrapper = (kaa_topic_listener_wrapper_t *) KAA_MALLOC(sizeof(kaa_topic_listener_wrapper_t));
    KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);
    wrapper->listener = *listener;
    if (!self->topics_listeners) {
        new_listener = kaa_list_create(wrapper);
    } else {
       if (kaa_list_find_next(self->topics_listeners, &kaa_find_topic_listener_by_id, &id)) {
           KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS, "Failed to add topic the listener: the listener is already subscribed.");
           KAA_FREE(wrapper);
           return KAA_ERR_ALREADY_EXISTS;
       }
       new_listener = kaa_list_push_front(self->topics_listeners, wrapper);
    }
    if (!new_listener) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to add the topic listener.");
        KAA_FREE(wrapper);
        return KAA_ERR_NOMEM;
    }
    self->topics_listeners = new_listener;
    *topic_listener_id = wrapper->id = id;// for user to have convenient way to address notification listener
    return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_topic_list_listener(kaa_notification_manager_t *self, uint32_t *topic_listener_id)
{
    KAA_RETURN_IF_NIL2(self, topic_listener_id, KAA_ERR_BADPARAM);
    kaa_error_t err = kaa_list_remove_first(&self->topics_listeners, &kaa_find_topic_listener_by_id, topic_listener_id, &kaa_data_destroy);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to remove the topic listener: the listener is not found");
    }
    if (!self->topics_listeners) {
        KAA_LOG_TRACE(self->logger, err, "Topic listeners list is empty now.");
    }
    return err;
}

kaa_error_t kaa_get_topics(kaa_notification_manager_t *self, kaa_list_t **topics)
{
    KAA_RETURN_IF_NIL2(self, topics, KAA_ERR_BADPARAM);
    if (!self->topics) {
        *topics = NULL;
        KAA_LOG_WARN(self->logger, KAA_ERR_BAD_STATE, "Failed retrieve the topic list.The topic list is empty.");
        return KAA_ERR_BAD_STATE;
    }
    *topics = self->topics;
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_notify_topic_update_subscribers(kaa_notification_manager_t *self, kaa_list_t *topics)
{
    KAA_RETURN_IF_NIL2(self, topics, KAA_ERR_BADPARAM);
    kaa_list_t *current_listener_node = NULL;
    if (!self->topics_listeners) {
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "There is no topic listeners to be notified");
        return KAA_ERR_NONE;
    } else {
        current_listener_node = self->topics_listeners;
    }
    while (current_listener_node) {
        kaa_topic_listener_wrapper_t *wrapper = (kaa_topic_listener_wrapper_t *)kaa_list_get_data(current_listener_node);
        wrapper->listener.callback(wrapper->listener.context, topics);
        current_listener_node = kaa_list_next(current_listener_node);
    }
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_notify_mandatory_notification_subscribers(kaa_notification_manager_t *self, uint64_t *topic_id, kaa_notification_t *notification)
{
    KAA_RETURN_IF_NIL2(self, notification, KAA_ERR_BADPARAM);
    kaa_list_t *current_listener_node = self->mandatory_listeners;
    KAA_RETURN_IF_NIL(current_listener_node,KAA_ERR_BAD_STATE);
    kaa_notification_listener_wrapper_t *wrapper = NULL;
    while (current_listener_node) {
        wrapper = (kaa_notification_listener_wrapper_t *) kaa_list_get_data(current_listener_node);
        wrapper->listener.callback(wrapper->listener.context, topic_id, notification);
        current_listener_node = kaa_list_next(current_listener_node);
    }
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_notify_optional_notification_subscribers(kaa_notification_manager_t *self, uint64_t *topic_id, kaa_notification_t *notification)
{
    KAA_RETURN_IF_NIL3(self, topic_id, notification, KAA_ERR_BADPARAM);
    kaa_list_t *opt_listeners_node = kaa_list_find_next(self->optional_listeners, &kaa_find_optional_notification_listener_by_id, topic_id);

    if (!opt_listeners_node) {
        return KAA_ERR_NOT_FOUND;
    }
    KAA_RETURN_IF_NIL(opt_listeners_node, KAA_ERR_BADPARAM);
    kaa_optional_notification_listeners_wrapper_t* optional_wrapper = (kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(opt_listeners_node);
    kaa_list_t *optional_listener_node = optional_wrapper->listeners;
    kaa_notification_listener_wrapper_t *wrapper = NULL;
    while (optional_listener_node) {
        wrapper = (kaa_notification_listener_wrapper_t *) kaa_list_get_data(optional_listener_node);
        wrapper->listener.callback(wrapper->listener.context, topic_id, notification);
        optional_listener_node = kaa_list_next(optional_listener_node);
    }
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
    if (topic->subscription_type == MANDATORY) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topic with id = %lu. Topic isn't optional.", *topic_id);
        return KAA_ERR_BADPARAM;
    }
    uint64_t *subs_topic_id = (uint64_t *) KAA_MALLOC(sizeof(uint64_t));
    KAA_RETURN_IF_NIL(subs_topic_id, KAA_ERR_NOMEM);
    *subs_topic_id = *topic_id;
    kaa_list_t *new_subscription = NULL;
    if (!self->subscriptions) {
        new_subscription = kaa_list_create(subs_topic_id);
    } else {
        new_subscription = kaa_list_push_front(self->subscriptions, subs_topic_id);
    }
    if (!new_subscription) {
        KAA_FREE(subs_topic_id);
        return KAA_ERR_NOMEM;
    }
    self->subscriptions = new_subscription;
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
    kaa_list_t *new_subscription = NULL;
    kaa_topic_t *topic = NULL;
    while (size--) {
        kaa_error_t err = kaa_find_topic(self, &topic, topic_ids + size);
        if (err) {
            KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topic with id = %lu.", topic_ids[size]);
            return err;
        } else {
            if (topic->subscription_type == MANDATORY) {
                KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topic with id = %lu. Topic isn't optional.", topic_ids[size]);
                return KAA_ERR_BADPARAM;
            }
            uint64_t *subs_topic_id = (uint64_t *) KAA_MALLOC(sizeof(uint64_t));
            KAA_RETURN_IF_NIL(subs_topic_id, KAA_ERR_NOMEM);
            *subs_topic_id = topic_ids[size];
            if (!self->subscriptions) {
                new_subscription = kaa_list_create(subs_topic_id);
            } else {
                new_subscription = kaa_list_push_front(self->subscriptions, subs_topic_id);
            }
            if (!new_subscription) {
                KAA_FREE(subs_topic_id);
                return KAA_ERR_NOMEM;
            }
            self->subscriptions = new_subscription;
        }
    }
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
    if (topic->subscription_type == MANDATORY) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topic with id = %lu. Topic isn't optional.", *topic_id);
        return KAA_ERR_BADPARAM;
    }
    uint64_t *unsubs_topic_id = (uint64_t *) KAA_MALLOC(sizeof(uint64_t));
    KAA_RETURN_IF_NIL(unsubs_topic_id, KAA_ERR_NOMEM);
    *unsubs_topic_id = *topic_id;
    kaa_list_t *new_unsubscription = NULL;
    if (!self->unsubscriptions) {
        new_unsubscription = kaa_list_create(unsubs_topic_id);
    } else {
        new_unsubscription = kaa_list_push_front(self->unsubscriptions, unsubs_topic_id);
    }
    if (!new_unsubscription) {
        KAA_FREE(unsubs_topic_id);
        return KAA_ERR_NOMEM;
    }
    self->unsubscriptions = new_unsubscription;
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
    kaa_list_t *new_unsubscription = NULL;
    kaa_topic_t *topic = NULL;
    while (size--) {
        kaa_error_t err = kaa_find_topic(self, &topic, topic_ids + size);
        if (err) {
            KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topic with id = %lu.", topic_ids[size]);
            return err;
        } else {
            if (topic->subscription_type == MANDATORY) {
                KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topic with id = %lu. Topic isn't optional.", topic_ids[size]);
                return KAA_ERR_BADPARAM;
            }
            uint64_t *unsubs_topic_id = (uint64_t *) KAA_MALLOC(sizeof(uint64_t));
            KAA_RETURN_IF_NIL(unsubs_topic_id, KAA_ERR_NOMEM);
            *unsubs_topic_id = topic_ids[size];
            if (!self->subscriptions) {
                new_unsubscription = kaa_list_create(unsubs_topic_id);
            } else {
                new_unsubscription = kaa_list_push_front(self->subscriptions, unsubs_topic_id);
            }
            if (!new_unsubscription) {
                KAA_FREE(unsubs_topic_id);
                return KAA_ERR_NOMEM;
            }
            self->subscriptions = new_unsubscription;
        }
    }
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
    kaa_list_t  *topic_node = topics;
    kaa_topic_t *topic = NULL;
    while (topic_node && self->topics) {
        topic = (kaa_topic_t *) kaa_list_get_data(topic_node);
        KAA_RETURN_IF_NIL(topic, KAA_ERR_NOMEM);
        kaa_list_remove_first(&self->topics, &kaa_find_topic_by_id, &topic->id, &kaa_topics_destroy);
        topic_node = kaa_list_next(topic_node);
    }
    if (kaa_list_get_size(self->topics)) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to remove optional listener(s) from obsolete %lu topics", self->topics);
        kaa_list_t *outdated_topics = self->topics;
        while (outdated_topics) {
            topic = (kaa_topic_t *) kaa_list_get_data(outdated_topics);
            kaa_list_t *optional_listener_node = kaa_list_find_next(self->optional_listeners, &kaa_find_optional_notification_listener_by_id, &topic->id);
            if (optional_listener_node) {
                kaa_list_t *notification_listeners = ((kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(optional_listener_node))->listeners;
                kaa_list_destroy(notification_listeners, &kaa_data_destroy);
                kaa_list_remove_at(&self->optional_listeners, optional_listener_node, &kaa_data_destroy);
            }
            outdated_topics = kaa_list_next(outdated_topics);
        }
    }
    if (self->topics) {
        kaa_list_destroy(self->topics, &kaa_topics_destroy);
    }
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

    kaa_list_destroy(self->subscriptions, &kaa_data_destroy);
    kaa_list_destroy(self->uids, &kaa_uids_destroy);
    self->uids = NULL;
    self->subscriptions = NULL;

    kaa_error_t err = KAA_ERR_NONE;
    if (extension_length > 0) {
        self->status->notification_seq_n = KAA_NTOHL(*((uint32_t *) reader->current));
        reader->current += sizeof(uint32_t);
        extension_length -= sizeof(uint32_t);
        uint8_t field_id = 0;
        while (extension_length > 0) {
            field_id = *((uint8_t *) reader->current);
            reader->current += sizeof(uint16_t);
            extension_length -= sizeof(uint16_t);
            switch (field_id) {
            case NOTIFICATIONS: {
                kaa_notification_t *notification = NULL;
                uint16_t uid_length = 0;
                uint64_t topic_id = 0;
                kaa_topic_t *topic_found = NULL;
                uint32_t seq_number = 0;
                uint32_t notification_size = 0;
                uint16_t notifications_count = KAA_NTOHS(*((uint16_t *) reader->current));
                reader->current += sizeof(uint16_t);
                extension_length -= sizeof(uint16_t);
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Notifications count is %u", notifications_count);
                if (notifications_count) {
                    while (notifications_count--) {
                        if (!notifications_count) {
                            seq_number = KAA_NTOHL(*((uint32_t *) reader->current));
                        }
                        reader->current += sizeof(uint32_t);
                        extension_length -= sizeof(uint32_t);
                        reader->current += sizeof(uint16_t);
                        extension_length -= sizeof(uint16_t);
                        uid_length = KAA_NTOHS(*((uint16_t *) reader->current));
                        reader->current += sizeof(uint16_t);
                        extension_length -= sizeof(uint16_t);
                        notification_size = KAA_NTOHL(*((uint32_t *) reader->current));
                        reader->current += sizeof(uint32_t);
                        extension_length -= sizeof(uint32_t);
                        topic_id =  KAA_NTOHLL(*((uint64_t *) reader->current));
                        reader->current += sizeof(uint64_t);
                        extension_length -= sizeof(uint64_t);
                        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Topic id is %lu", topic_id);
                        err = kaa_find_topic(self, &topic_found, &topic_id);
                        if (err) {
                            reader->current += kaa_aligned_size_get(uid_length);
                            reader->current += kaa_aligned_size_get(notification_size);
                            continue;
                        }
                        if (uid_length) {
                            kaa_notifications_uid_t *uid = (kaa_notifications_uid_t *) KAA_MALLOC(sizeof(kaa_notifications_uid_t));
                            KAA_RETURN_IF_NIL(uid, KAA_ERR_NOMEM);
                            uid->length = uid_length;
                            uid->data = KAA_MALLOC(uid_length);
                            if (!uid->data) {
                                KAA_FREE(uid);
                                return KAA_ERR_NOMEM;
                            }
                            err = kaa_platform_message_read_aligned(reader, uid->data, uid->length);
                            if (err) {
                                KAA_LOG_WARN(self->logger, KAA_ERR_BADDATA, "Failed to read UID body");
                            }
                            extension_length -= kaa_aligned_size_get(uid->length);
                            kaa_list_t * new_uid = NULL;
                            if (!self->uids) {
                                new_uid = kaa_list_create(uid);
                            } else {
                                new_uid = kaa_list_push_front(self->uids, uid);
                            }
                            if (!new_uid) {
                                KAA_FREE(uid->data);
                                KAA_FREE(uid);
                                return KAA_ERR_NOMEM;
                            }
                            self->uids = new_uid;
                        }
                        if (notification_size) {
                            avro_reader_t avro_reader = avro_reader_memory(reader->current, notification_size);
                            if (!avro_reader) {
                                return KAA_ERR_NOMEM;
                            }
                            notification = KAA_NOTIFICATION_DESERIALIZE(avro_reader);
                            if (!notification) {
                                KAA_LOG_ERROR(self->logger, KAA_ERR_NONE, "Failed to deserialize notification.");
                                avro_reader_free(avro_reader);
                                return KAA_ERR_NOMEM;
                            }
                            avro_reader_free(avro_reader);
                        }
                        reader->current += kaa_aligned_size_get(notification_size);
                        extension_length -= kaa_aligned_size_get(notification_size);
                        if (!notifications_count) {
                            kaa_list_t *state_found = kaa_list_find_next(self->status->topic_states, &kaa_find_topic_state_by_id, &topic_id);
                            if (!state_found) {
                                kaa_list_t *new_topic_state = NULL;
                                kaa_topic_state_t *state = (kaa_topic_state_t *) KAA_MALLOC(sizeof(kaa_topic_state_t));
                                if (!state) {
                                    KAA_FREE(notification);
                                    return KAA_ERR_NOMEM;
                                }
                                state->sqn_number = seq_number;
                                state->topic_id = topic_id;
                                if (!self->status->topic_states) {
                                    new_topic_state = kaa_list_create(state);
                                } else {
                                    new_topic_state = kaa_list_push_front(self->status->topic_states, state);
                                }
                                if (!new_topic_state) {
                                    KAA_FREE(state);
                                    return KAA_ERR_NOMEM;
                                }
                                self->status->topic_states = new_topic_state;
                            } else {
                                kaa_topic_state_t *state = (kaa_topic_state_t *)kaa_list_get_data(state_found);
                                state->sqn_number = seq_number;
                            }
                        }
                        err = kaa_notification_received(self, notification, &topic_id);
                        notification->destroy(notification);
                        if (err) {
                            KAA_LOG_WARN(self->logger, err, "Failed to notify listeners.");
                        }
                    }
                }
                break;
            }
            case TOPICS: {
                KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received topics");
                uint16_t topic_count = KAA_NTOHS(*((uint16_t *) reader->current));
                KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Topic count is %u", topic_count);
                reader->current += sizeof(uint16_t);
                extension_length -= sizeof(uint16_t);
                kaa_list_t *topics = NULL;
                kaa_topic_t *topic = NULL;
                kaa_list_t *new_topic = NULL;
                while (topic_count--) {
                   topic = (kaa_topic_t *) KAA_MALLOC(sizeof(kaa_topic_t));
                   KAA_RETURN_IF_NIL(topic, KAA_ERR_NOMEM);
                   topic->id = KAA_NTOHLL(*((uint64_t *) reader->current));
                   reader->current += sizeof(uint64_t);
                   extension_length -= sizeof(uint64_t);
                   topic->subscription_type =(*((uint8_t *) reader->current) == MANDATORY) ? MANDATORY : OPTIONAL;
                   reader->current += sizeof(uint16_t);
                   extension_length -= sizeof(uint16_t);
                   topic->name_length = KAA_NTOHS(*((uint16_t *) reader->current));
                   reader->current += sizeof(uint16_t);
                   extension_length -= sizeof(uint16_t);
                   topic->name = (char *) KAA_MALLOC(topic->name_length + 1);
                   if (!topic->name) {
                       KAA_FREE(topic);
                       return KAA_ERR_NOMEM;
                   }
                   memset(topic->name, 0, topic->name_length + 1);
                   err = kaa_platform_message_read_aligned(reader, topic->name, topic->name_length);
                   if (err) {
                       KAA_LOG_WARN(self->logger, KAA_ERR_BADDATA, "Failed to read topic's name");
                       KAA_FREE(topic->name);
                       KAA_FREE(topic);
                       return err;
                   }
                   extension_length -= kaa_aligned_size_get(topic->name_length);

                   if (!topics) {
                       new_topic = kaa_list_create(topic);
                   } else {
                       new_topic = kaa_list_push_front(topics, topic);
                   }
                   if (!new_topic) {
                       KAA_FREE(topic->name);
                       KAA_FREE(topic);
                       return KAA_ERR_NOMEM;
                   }
                   topics = new_topic;
                }
                err = kaa_topic_list_updated(self, topics);
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

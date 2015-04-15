
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

extern kaa_transport_channel_interface_t *kaa_channel_manager_get_transport_channel(kaa_channel_manager_t *self
                                                                           , kaa_service_t service_type);

struct kaa_notification_manager_t {
    kaa_list_t                 *mandatory_listeners;
    kaa_list_t                 *topics_listeners;
    kaa_list_t                 *optional_listeners;
    kaa_list_t                 *subscriptions;
    kaa_list_t                 *topics;
    kaa_list_t                 *uids;
    kaa_list_t                 *topic_states;
    size_t                      notification_sequence_number;

    kaa_status_t               *status;
    kaa_channel_manager_t      *channel_manager;
    kaa_logger_t               *logger;
};

typedef struct {
    uint16_t length;
    void *data;
} kaa_notifications_uid_t;

typedef struct {
    uint64_t topic_id;
    uint32_t sqn_number;
} kaa_topic_state_t;

typedef struct {
    uint32_t seq_num;
    kaa_topic_subscription_type_t type;
    uint32_t size;
    uint64_t topic_id;
    kaa_notification_t *notification_body;
} kaa_notification_wrapper_t;

typedef enum {
    ADD,
    REMOVE
} kaa_subscription_command_type_t;

typedef enum {
    TOPICS = 0x0,
    NOTIFICATIONS = 0x1
} kaa_notification_field_id_t;

typedef struct {
    uint64_t id;
    kaa_subscription_command_type_t command;
} kaa_subscription_command_t;

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

static bool kaa_find_notification_listener_by_id(void *listener, uint32_t *context)
{
    KAA_RETURN_IF_NIL2(listener, context, KAA_ERR_NONE);
    return ((kaa_notification_listener_wrapper_t *) listener)->id == *context;
}
static bool kaa_find_topic_listener_by_id(void *listener, uint32_t *context)
{
    KAA_RETURN_IF_NIL2(listener, context, KAA_ERR_NONE);
    return ((kaa_topic_listener_wrapper_t *) listener)->id == *context;
}

static bool kaa_find_optional_notification_listener_by_id (void *optional_listener_list, uint64_t *topic_id)
{
    KAA_RETURN_IF_NIL2(optional_listener_list, topic_id, KAA_ERR_NONE);
    kaa_optional_notification_listeners_wrapper_t *optional_listener_node = (kaa_optional_notification_listeners_wrapper_t *) optional_listener_list;
    return optional_listener_node->topic_id == *topic_id;
}

static bool kaa_find_topic_by_id(void *topic, uint64_t *id)
{
    KAA_RETURN_IF_NIL2(topic, id, KAA_ERR_NONE);
    kaa_topic_t* notification_topic = (kaa_topic_t *) topic;
    return notification_topic->id == *id;

}
static void kaa_optional_notification_wrapper_destroy(void *data)
{
    KAA_RETURN_IF_NIL(data,);
    kaa_optional_notification_listeners_wrapper_t * wrapper = (kaa_optional_notification_listeners_wrapper_t *) data;
    KAA_FREE(wrapper);
}
static void kaa_notification_listener_destroy(void *data)
{
    KAA_RETURN_IF_NIL(data,);
    kaa_notification_listener_wrapper_t * wrapper = (kaa_notification_listener_wrapper_t *) data;
    KAA_FREE(wrapper);
}

static void kaa_topic_listener_destroy(void *data)
{
    KAA_RETURN_IF_NIL(data,);
    kaa_topic_listener_wrapper_t * listener = (kaa_topic_listener_wrapper_t  *) data;
    KAA_FREE(listener);
}

static void kaa_topic_destroy(void *data)
{
    KAA_RETURN_IF_NIL(data,);
    kaa_topic_t * topic = (kaa_topic_t *) data;
    KAA_FREE(topic);
}

static void kaa_subscriptions_destroy(void *data)
{
    KAA_RETURN_IF_NIL(data,);
    kaa_subscription_command_t *cmd = (kaa_subscription_command_t *) data;
    KAA_FREE(cmd);
}

static void kaa_notification_wrapper_destroy(void *data)
{
    kaa_notification_wrapper_t *wrapper = (kaa_notification_wrapper_t *) data;
    wrapper->notification_body->destroy(wrapper->notification_body);
    KAA_FREE(wrapper);
}

static void kaa_topic_states_destroy(void *data )
{
    kaa_topic_state_t *state = (kaa_topic_state_t *)data;
    KAA_FREE(state);
}
static kaa_error_t kaa_find_topic(kaa_notification_manager_t *self, kaa_topic_t **topic, uint64_t *topic_id)
{
    KAA_RETURN_IF_NIL2(topic_id, topic, KAA_ERR_BADPARAM);
    kaa_list_t *topic_node = kaa_list_find_next(self->topics, &kaa_find_topic_by_id, topic_id);
    if (!topic_node) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOT_FOUND, "Topic with id = %d not found.", *topic_id);
        return KAA_ERR_NOT_FOUND;
    }
    *topic = kaa_list_get_data(topic_node);
    return KAA_ERR_NONE;
}

static bool kaa_find_topic_state_by_id(void *topic_state, uint64_t *id)
{
    kaa_topic_state_t *state = (kaa_topic_state_t *)topic_state;
    return state->topic_id == *id;
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
    *expected_size = KAA_EXTENSION_HEADER_SIZE;
    *expected_size += sizeof(uint32_t); //state sqn size

    if (self->topic_states) {
        *expected_size += sizeof(uint32_t); //field id + reserved + count
        *expected_size += (sizeof(uint64_t) + sizeof(uint32_t)) * kaa_list_get_size(self->topic_states);
    }

    if (self->uids) {
        *expected_size += sizeof(uint32_t); //field id + reserved + count
        kaa_list_t *uid_node = self->uids;
        size_t uids_size = 0;
        while (uid_node) {
            kaa_notifications_uid_t *uid = (kaa_notifications_uid_t *)kaa_list_get_data(uid_node);
            uids_size += sizeof(uint32_t); // uid_length
            uids_size += kaa_aligned_size_get(uid->length);
            uid_node = kaa_list_next(uid_node);
        }
        *expected_size += uids_size;
    }

    if (self->subscriptions) {
        *expected_size += sizeof(uint64_t); //meta data for subscribe & unsubscribe commands
        *expected_size += kaa_list_get_size(self->subscriptions) * sizeof(uint64_t);
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
    (*self)->topics              =  NULL;
    (*self)->topic_states        =  NULL;

    (*self)->status              =  status;
    (*self)->channel_manager     =  channel_manager;
    (*self)->logger              =  logger;

    return KAA_ERR_NONE;
}

void kaa_notification_manager_destroy(kaa_notification_manager_t *self)
{
    KAA_RETURN_IF_NIL(self,);
    if (self->mandatory_listeners) {
        kaa_list_destroy(self->mandatory_listeners, &kaa_notification_listener_destroy);
    }
    if (self->topics_listeners) {
        kaa_list_destroy(self->topics_listeners, &kaa_topic_listener_destroy);
    }
    if(self->subscriptions) {
        kaa_list_destroy(self->subscriptions, &kaa_subscriptions_destroy);
    }
    if (self->optional_listeners) {
        kaa_list_t* wrappers = self->optional_listeners;
        while (wrappers) {
             kaa_optional_notification_listeners_wrapper_t* wrapper = (kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(wrappers);
             kaa_list_destroy(wrapper->listeners, &kaa_notification_listener_destroy);
             wrappers = kaa_list_next(wrappers);
        }
        kaa_list_destroy(self->optional_listeners, &kaa_optional_notification_wrapper_destroy);
    }
    if (self->topics) {
        kaa_list_destroy(self->topics, &kaa_topic_destroy);
    }
    if (self->topic_states) {
        kaa_list_destroy(self->topic_states, &kaa_topic_states_destroy);
    }
    KAA_FREE(self);
}

kaa_error_t kaa_add_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener, uint32_t* listener_id)
{
    KAA_RETURN_IF_NIL2(self, listener, KAA_ERR_BADPARAM);
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
    if (!self->mandatory_listeners) {
        self->mandatory_listeners = kaa_list_create(wrapper);
        KAA_RETURN_IF_NIL(self->mandatory_listeners, KAA_ERR_NOMEM);
    } else {
        KAA_RETURN_IF_NIL((kaa_list_push_back(self->mandatory_listeners, wrapper)), KAA_ERR_NOMEM);
    }
    *listener_id = wrapper->id = id;; // for user to have convenient way to address notification listener
    return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_notification_listener(kaa_notification_manager_t *self, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL2(self, listener_id, KAA_ERR_BADPARAM);
    kaa_error_t err = kaa_list_remove_first(&self->mandatory_listeners, &kaa_find_notification_listener_by_id, listener_id, &kaa_notification_listener_destroy);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to remove mandatory listener: listener with id=%d is not found.", *listener_id);
    }
    if (!kaa_list_get_size(self->mandatory_listeners)) {
        self->mandatory_listeners = NULL;
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Mandatory listeners list is empty now.");
    }
    return err;
}

kaa_error_t kaa_add_optional_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener
                                                 , uint64_t *topic_id, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL4(self, listener, topic_id, listener_id, KAA_ERR_BADPARAM);
//    kaa_list_t *topic = kaa_list_find_next(self->topics, &kaa_find_topic_by_id, topic_id);
//    if (!topic) {
//        KAA_LOG_ERROR(self->logger, KAA_ERR_NOT_FOUND, "Topic with id = %d not found.", *topic_id);
//        return KAA_ERR_NOT_FOUND;
//    }
    uint32_t id;
    kaa_error_t err = kaa_calculate_notification_listener_id(listener, &id);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to calculate optional listener id.");
        return err;
    }
       if (!self->optional_listeners) {
           kaa_notification_listener_wrapper_t *wrapper = (kaa_notification_listener_wrapper_t *) KAA_MALLOC(sizeof(kaa_notification_listener_wrapper_t));
           KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);
           wrapper->listener = *listener;
           kaa_optional_notification_listeners_wrapper_t* optional_wrapper = (kaa_optional_notification_listeners_wrapper_t *)
                                                                     KAA_MALLOC(sizeof(kaa_optional_notification_listeners_wrapper_t));
           KAA_RETURN_IF_NIL(optional_wrapper, KAA_ERR_NOMEM);
           self->optional_listeners = kaa_list_create(optional_wrapper);
           KAA_RETURN_IF_NIL(self->optional_listeners, KAA_ERR_NOMEM);
           optional_wrapper->listeners = kaa_list_create(wrapper);
           KAA_RETURN_IF_NIL(optional_wrapper->listeners, KAA_ERR_NOMEM);
           optional_wrapper->topic_id = *topic_id;
           *listener_id = wrapper->id = id; // for user to have convenient way to address notification listener
       } else {
           kaa_list_t *opt_listeners_node  = kaa_list_find_next(self->optional_listeners, &kaa_find_optional_notification_listener_by_id, topic_id);
           if (!opt_listeners_node) {
               kaa_optional_notification_listeners_wrapper_t* optional_wrapper = (kaa_optional_notification_listeners_wrapper_t *)
                                                                         KAA_MALLOC(sizeof(kaa_optional_notification_listeners_wrapper_t));
               KAA_RETURN_IF_NIL(optional_wrapper, KAA_ERR_NOMEM);
               kaa_list_t * new_opt_topic_listener_list = kaa_list_push_back(self->optional_listeners, optional_wrapper);
               KAA_RETURN_IF_NIL(new_opt_topic_listener_list, KAA_ERR_NOMEM);
               kaa_notification_listener_wrapper_t *wrapper = (kaa_notification_listener_wrapper_t *) KAA_MALLOC(sizeof(kaa_notification_listener_wrapper_t));
               KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);
               wrapper->listener = *listener;
               optional_wrapper->listeners  = kaa_list_create(wrapper);
               KAA_RETURN_IF_NIL(optional_wrapper->listeners, KAA_ERR_NOMEM);
               optional_wrapper->topic_id = *topic_id;
               *listener_id = wrapper->id = id; // for user to have convenient way to address notification listener
           } else {
               kaa_optional_notification_listeners_wrapper_t* optional_wrapper = (kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(opt_listeners_node);
               if (kaa_list_find_next(optional_wrapper->listeners, &kaa_find_notification_listener_by_id, &id)) {
                   KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS, "Failed to add the optional listener: the listener is already subscribed.");
                   return KAA_ERR_ALREADY_EXISTS;
               }
               kaa_notification_listener_wrapper_t *wrapper = (kaa_notification_listener_wrapper_t *) KAA_MALLOC(sizeof(kaa_notification_listener_wrapper_t));
               KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);
               wrapper->listener = *listener;
               kaa_list_t *inserted_listener = kaa_list_push_back(optional_wrapper->listeners, wrapper);
               KAA_RETURN_IF_NIL(inserted_listener, KAA_ERR_NOMEM);
               *listener_id = wrapper->id = id; // for user to have convenient way to address notification listener
           }
       }
       KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "The optional listener with id = %d has been added.", *listener_id);
       return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_optional_notification_listener(kaa_notification_manager_t *self, uint64_t *topic_id, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL3(self, topic_id, listener_id, KAA_ERR_BADPARAM);
//    kaa_list_t *topic = kaa_list_find_next(self->topics, &kaa_find_topic_by_id, topic_id);
//    if (!topic) {
//        KAA_LOG_ERROR(self->logger, KAA_ERR_NOT_FOUND, "Topic with id = %d not found.", *topic_id);
//        return KAA_ERR_NOT_FOUND;
//    }
    kaa_list_t *opt_listeners_node = kaa_list_find_next(self->optional_listeners, &kaa_find_optional_notification_listener_by_id, topic_id);
    if (!opt_listeners_node) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Failed to remove the optional listener: there is no listeners subscribed on this topic (topic id = %d).", *topic_id);
        return KAA_ERR_NOT_FOUND;
    }
    kaa_optional_notification_listeners_wrapper_t* optional_wrapper = (kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(opt_listeners_node);
    kaa_error_t error = kaa_list_remove_first(&optional_wrapper->listeners, &kaa_find_notification_listener_by_id, listener_id, &kaa_notification_listener_destroy);
    if (error) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Failed to remove the optional listener: the listener with id=%d is not found.", *listener_id);
        return error;
    } else {
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "The optional listener with id = %d has been removed.", *listener_id);
        if (!kaa_list_get_size(optional_wrapper->listeners)) {
            if (!kaa_list_remove_at(&self->optional_listeners, opt_listeners_node, &kaa_optional_notification_wrapper_destroy)) {
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "The optional listeners list is empty.");
                self->optional_listeners = NULL;
                return KAA_ERR_NONE;
            }
        }
        return KAA_ERR_NONE;
    }
}

kaa_error_t kaa_add_topic_list_listener(kaa_notification_manager_t *self, kaa_topic_listener_t *listener, uint32_t *topic_listener_id)
{
    KAA_RETURN_IF_NIL2(self, listener, KAA_ERR_BADPARAM);
    uint32_t id;
    kaa_error_t err = kaa_calculate_topic_listener_id(listener, &id);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to calculate mandatory listener's id.");
        return err;
    }
    kaa_topic_listener_wrapper_t *wrapper = NULL;
    if (!self->topics_listeners) {
        wrapper = (kaa_topic_listener_wrapper_t *) KAA_MALLOC(sizeof(kaa_topic_listener_wrapper_t));
        KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);
        wrapper->listener = *listener;
        self->topics_listeners = kaa_list_create(wrapper);
        if (!self->topics_listeners) {
            KAA_LOG_TRACE(self->logger, KAA_ERR_NOMEM, "Failed to create storage for topic listeners.");
            return KAA_ERR_NOMEM;
        }
    } else {
        if (kaa_list_find_next(self->topics_listeners, &kaa_find_topic_listener_by_id, &id)) {
            KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS, "Failed to add topic the listener: the listener is already subscribed.");
            return KAA_ERR_ALREADY_EXISTS;
        }
        wrapper = (kaa_topic_listener_wrapper_t *) KAA_MALLOC(sizeof(kaa_topic_listener_wrapper_t));
        KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);
        wrapper->listener = *listener;
        if (!kaa_list_push_back(self->topics_listeners, wrapper)) {
            KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to add the topic listener.");
            return KAA_ERR_NOMEM;
        }
    }
    *topic_listener_id = wrapper->id = id;// for user to have convenient way to address notification listener
    return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_topic_list_listener(kaa_notification_manager_t *self, uint32_t *topic_listener_id)
{
    KAA_RETURN_IF_NIL2(self, topic_listener_id, KAA_ERR_BADPARAM);
    kaa_error_t err = kaa_list_remove_first(&self->topics_listeners, &kaa_find_topic_listener_by_id, topic_listener_id, &kaa_topic_listener_destroy);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to remove the topic listener: the listener is not found");
    }
    if (!kaa_list_get_size(self->topics_listeners)) {
        KAA_LOG_TRACE(self->logger, err, "Topic listeners list is empty now.");
        self->topics_listeners = NULL;
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

static kaa_error_t kaa_notify_topic_update_subscribers(kaa_notification_manager_t *self, const kaa_list_t *topics)
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
        if (wrapper->listener.callback) {
            wrapper->listener.callback(wrapper->listener.context, topics);
        } else {
            KAA_LOG_WARN(self->logger, KAA_ERR_BADDATA, "Get NULL, expected callback, listener's id = %d.", wrapper->id);
        }
        current_listener_node = kaa_list_next(current_listener_node);
    }
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_notify_mandatory_notification_subscribers(kaa_notification_manager_t *self, uint64_t *topic_id, const kaa_notification_t *notification)
{
    KAA_RETURN_IF_NIL2(self, notification, KAA_ERR_BADPARAM);
    kaa_list_t *current_listener_node = self->mandatory_listeners;
    KAA_RETURN_IF_NIL(current_listener_node,KAA_ERR_BAD_STATE);
    kaa_notification_listener_wrapper_t *wrapper = NULL;
    while (current_listener_node) {
         wrapper = (kaa_notification_listener_wrapper_t *) kaa_list_get_data(current_listener_node);
        if (wrapper->listener.callback) {
            wrapper->listener.callback(wrapper->listener.context, topic_id, notification);
        } else {
            KAA_LOG_ERROR(self->logger, KAA_ERR_BADDATA, "Get NULL, expected callback, listener's id = %d.", wrapper->id);
        }
        current_listener_node = kaa_list_next(current_listener_node);
    }
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_notify_optional_notification_subscribers(kaa_notification_manager_t *self, const uint64_t *topic_id, const kaa_notification_t *notification)
{
    KAA_RETURN_IF_NIL3(self, topic_id, notification, KAA_ERR_BADPARAM);
    kaa_list_t *opt_listeners_node = kaa_list_find_next(self->optional_listeners, &kaa_find_optional_notification_listener_by_id, topic_id);

    if (!opt_listeners_node) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "No listeners were notified, because nobody's subscribed on this topic (topic id = %d).", *topic_id);
        return KAA_ERR_NOT_FOUND;
    }
    KAA_RETURN_IF_NIL(opt_listeners_node, KAA_ERR_BADPARAM);
    kaa_optional_notification_listeners_wrapper_t* optional_wrapper = (kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(opt_listeners_node);
    kaa_list_t *optional_listener_node = optional_wrapper->listeners;
    kaa_notification_listener_wrapper_t *wrapper = NULL;
    while (optional_listener_node) {
        wrapper = (kaa_notification_listener_wrapper_t *) kaa_list_get_data(optional_listener_node);
        if (wrapper->listener.callback) {
            wrapper->listener.callback(wrapper->listener.context, topic_id, notification);
        } else {
            KAA_LOG_ERROR(self->logger, KAA_ERR_BADDATA, "Get NULL, expected callback, listener's id = %d.", wrapper->id);
        }
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
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topic with id = %d.", *topic_id);
        return err;
    }
    if (topic->subscription_type == MANDATORY) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topic with id = %d. Topic isn't optional.", *topic_id);
        return KAA_ERR_BADPARAM;
    }
    kaa_subscription_command_t *command = (kaa_subscription_command_t *) KAA_MALLOC(sizeof(kaa_subscription_command_t));
    KAA_RETURN_IF_NIL(command, KAA_ERR_NOMEM);
    command->command = ADD;
    command->id = *topic_id;
    if (!self->subscriptions) {
        self->subscriptions = kaa_list_create(command);
        KAA_RETURN_IF_NIL(self->subscriptions, KAA_ERR_NOMEM);
    } else {
        KAA_RETURN_IF_NIL(kaa_list_push_back(self->subscriptions, command), KAA_ERR_NOMEM);
    }

    if (force_sync) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to subscribe to topics %d", *topic_id);
        kaa_sync_topic_subscriptions(self);
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Subscription to topic %d is postponed till sync", *topic_id);
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_subscribe_to_topics(kaa_notification_manager_t *self, kaa_list_t *topic_ids, bool force_sync)
{
    KAA_RETURN_IF_NIL2(self, topic_ids, KAA_ERR_BADPARAM);
    while (topic_ids) {
        uint64_t *topic_id =(uint64_t *) kaa_list_get_data(topic_ids);
        if (!topic_id) {
            KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topic with id = %d. Id is NULL.", *topic_id);
        } else {
            kaa_topic_t *topic = NULL;
            kaa_error_t err = kaa_find_topic(self, &topic, topic_id);
            if (err) {
                KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topic with id = %d.", *topic_id);
                return err;
            } else {
                if (topic->subscription_type == MANDATORY) {
                    KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topic with id = %d. Topic isn't optional.", *topic_id);
                    return KAA_ERR_BADPARAM;
                }
                kaa_subscription_command_t *command = (kaa_subscription_command_t *) KAA_MALLOC(sizeof(kaa_subscription_command_t));
                KAA_RETURN_IF_NIL(command, KAA_ERR_NOMEM);
                command->command = ADD;
                command->id = *topic_id;
                if (!self->subscriptions) {
                    self->subscriptions = kaa_list_create(command);
                    KAA_RETURN_IF_NIL(self->subscriptions, KAA_ERR_NOMEM);
                } else {
                    KAA_RETURN_IF_NIL(kaa_list_push_back(self->subscriptions, command), KAA_ERR_NOMEM);
                }
            }
        }
        topic_ids = kaa_list_next(topic_ids);
    }
    if (force_sync) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to subscribe to topics");
        kaa_sync_topic_subscriptions(self);
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Subscription to topics is postponed till sync");
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_unsubscribe_from_topic(kaa_notification_manager_t *self, uint64_t *topic_id, bool force_sync)
{
    kaa_topic_t* topic = NULL;
    kaa_error_t err = kaa_find_topic(self, &topic, topic_id);
    if (err) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topic with id = %d.", *topic_id);
        return err;
    }
    if (topic->subscription_type == MANDATORY) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topic with id = %d. Topic isn't optional.", *topic_id);
        return KAA_ERR_BADPARAM;
    }
    kaa_subscription_command_t *command = (kaa_subscription_command_t *) KAA_MALLOC(sizeof(kaa_subscription_command_t));
    KAA_RETURN_IF_NIL(command, KAA_ERR_NOMEM);
    command->command = REMOVE;
    command->id = *topic_id;
    if (!self->subscriptions) {
        self->subscriptions = kaa_list_create(command);
        KAA_RETURN_IF_NIL(self->subscriptions, KAA_ERR_NOMEM);
    } else {
        KAA_RETURN_IF_NIL(kaa_list_push_back(self->subscriptions, command), KAA_ERR_NOMEM);
    }

    if (force_sync) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to unsubscribe from topic ", *topic_id);
        kaa_sync_topic_subscriptions(self);
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Unsubscription from topic %d is postponed till sync", *topic_id);
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_unsubscribe_from_topics(kaa_notification_manager_t *self, kaa_list_t *topic_ids, bool force_sync)
{
    KAA_RETURN_IF_NIL2(self, topic_ids, KAA_ERR_BADPARAM);
    while (topic_ids) {
        uint64_t *topic_id =(uint64_t *) kaa_list_get_data(topic_ids);
        if (!topic_id) {
            KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topic. Id is NULL.");
        } else {
            kaa_topic_t *topic = NULL;
            kaa_error_t err = kaa_find_topic(self, &topic, topic_id);
            if (err) {
                KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topic with id = %d.", *topic_id);
                return err;
            } else {
                if (topic->subscription_type == MANDATORY) {
                    KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topic with id = %d. Topic isn't optional.", *topic_id);
                    return KAA_ERR_BADPARAM;
                }
                kaa_subscription_command_t *command = (kaa_subscription_command_t *) KAA_MALLOC(sizeof(kaa_subscription_command_t));
                KAA_RETURN_IF_NIL(command, KAA_ERR_NOMEM);
                command->command = REMOVE;
                command->id = *topic_id;
                if (!self->subscriptions) {
                    self->subscriptions = kaa_list_create(command);
                    KAA_RETURN_IF_NIL(self->subscriptions, KAA_ERR_NOMEM);
                } else {
                    KAA_RETURN_IF_NIL(kaa_list_push_back(self->subscriptions, command), KAA_ERR_NOMEM);
                }
            }
        }
        topic_ids = kaa_list_next(topic_ids);
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
    if (channel)
        channel->sync_handler(channel->context, notification_sync_services, 1);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_topic_list_updated(kaa_notification_manager_t *self, kaa_list_t *topics)
{
    KAA_RETURN_IF_NIL2(self, topics, KAA_ERR_BADPARAM);
    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "New list of available topics received (topic_count=%d).", kaa_list_get_size(topics));
    kaa_list_t  *topic_node = topics;
    kaa_topic_t *topic = NULL;
    while (topic_node && self->topics) {
        topic = (kaa_topic_t *) kaa_list_get_data(topic_node);
        KAA_RETURN_IF_NIL(topic, KAA_ERR_NOMEM);
        kaa_list_remove_first(&self->topics, &kaa_find_topic_by_id, &topic->id, &kaa_topic_destroy);
        topic_node = kaa_list_next(topic_node);
    }
    if (kaa_list_get_size(self->topics)) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to remove optional listener(s) from obsolete %d topics", self->topics);
        kaa_list_t *outdated_topics = self->topics;
        while (outdated_topics) {
            topic = (kaa_topic_t *) kaa_list_get_data(outdated_topics);
            kaa_list_t *optional_listener_node = kaa_list_find_next(self->optional_listeners, &kaa_find_optional_notification_listener_by_id, &topic->id);
            if (optional_listener_node) {
                kaa_list_t *notification_listeners = ((kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(optional_listener_node))->listeners;
                kaa_list_destroy(notification_listeners, &kaa_notification_listener_destroy);
                kaa_list_remove_at(&self->optional_listeners, optional_listener_node, &kaa_optional_notification_wrapper_destroy);
            }
            outdated_topics = kaa_list_next(outdated_topics);
        }
    }
    if (self->topics) {
        kaa_list_destroy(self->topics, &kaa_topic_destroy);
    }
    self->topics = topics;
    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Now topic_count=%d.", kaa_list_get_size(self->topics));
    return kaa_notify_topic_update_subscribers(self ,topics);
}

kaa_error_t kaa_notification_received(kaa_notification_manager_t *self, kaa_list_t *notifications)
{
    KAA_RETURN_IF_NIL2(self, notifications, KAA_ERR_BADDATA);
    kaa_topic_t* topic = NULL;
    while (notifications) {
        kaa_notification_wrapper_t *wrapper = kaa_list_get_data(notifications);
        kaa_error_t err = kaa_find_topic(self, &topic, &wrapper->topic_id);
        if (err) {
            KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Unknown notification received(topic id=%d) ", wrapper->topic_id);
        }
            if (kaa_notify_optional_notification_subscribers(self, &wrapper->topic_id, wrapper->notification_body)) {
                kaa_notify_mandatory_notification_subscribers(self, &wrapper->topic_id, wrapper->notification_body);
            }
            notifications = kaa_list_next(notifications);
    }
    return KAA_ERR_NONE;
}
kaa_error_t kaa_notification_manager_handle_server_sync(kaa_notification_manager_t *self
                                                      , kaa_platform_message_reader_t *reader
                                                      , uint32_t extension_length)
{
    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);
    kaa_error_t err = KAA_ERR_NONE;
    if (extension_length > 0) {
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "EXTENSION SIZE IS %d", extension_length);
        self->status->notification_seq_n = KAA_NTOHL(*((uint32_t *) reader->current));
        reader->current += sizeof(uint32_t);
        extension_length -= sizeof(uint32_t);
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "NSQ is %d", self->status->notification_seq_n);
        uint8_t field_id = 0;
        while(extension_length > 0) {
            field_id = *((uint8_t *) reader->current);
            reader->current += sizeof(uint16_t);
            extension_length -= sizeof(uint16_t);
            switch (field_id) {
            case NOTIFICATIONS: {
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "NONTIFICATIONS RECEIVED");
                kaa_list_t *notifications = NULL;
                kaa_notification_wrapper_t *notification = NULL;
                uint16_t uid_length = 0;
                uint16_t notifications_count = KAA_NTOHS(*((uint16_t *) reader->current));
                reader->current += sizeof(uint16_t);
                extension_length -= sizeof(uint16_t);
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "NONTIFICATIONS COUNT IS %u", notifications_count);
                if (notifications_count) {
                    while (notifications_count--) {
                        notification = (kaa_notification_wrapper_t *) KAA_MALLOC(sizeof(kaa_notification_wrapper_t));
                        KAA_RETURN_IF_NIL(notification, KAA_ERR_NOMEM);
                        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "NONTIFICATIONS WRAPPER CREATED")
                        notification->seq_num = KAA_NTOHL(*((uint32_t *) reader->current));
                        reader->current += sizeof(uint32_t);
                        extension_length -= sizeof(uint32_t);
                        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "NONTIFICATIONS SQ IS %u", notification->seq_num);
                        notification->type = (*((uint8_t *) reader->current) == SYSTEM)? SYSTEM : CUSTOM;
                        reader->current += sizeof(uint16_t);
                        extension_length -= sizeof(uint16_t);
                        uid_length = KAA_NTOHS(*((uint16_t *) reader->current));
                        reader->current += sizeof(uint16_t);
                        extension_length -= sizeof(uint16_t);
                        notification->size = KAA_NTOHL(*((uint32_t *) reader->current));
                        reader->current += sizeof(uint32_t);
                        extension_length -= sizeof(uint32_t);
                        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "NONTIFICATIONS BODY SIZE IS %u", notification->size);
                        notification->topic_id =  htonll(*((uint64_t *) reader->current));
                        reader->current += sizeof(uint64_t);
                        extension_length -= sizeof(uint64_t);
                        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "TOPIC ID IS %u", notification->topic_id);
                        if (uid_length) {
                            kaa_notifications_uid_t *uid = (kaa_notifications_uid_t *) KAA_MALLOC(sizeof(kaa_notifications_uid_t));
                            KAA_RETURN_IF_NIL(uid, KAA_ERR_NOMEM);
                            uid->length = uid_length;
                            uid->data = KAA_MALLOC(sizeof(uid_length));
                            err = kaa_platform_message_read_aligned(reader, uid->data, uid->length);
                            if (err) {
                                KAA_LOG_WARN(self->logger, KAA_ERR_BADDATA, "Failed to read UID body");
                            }
                            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "UID HAS BEEN READ");
                            //reader->current += (kaa_aligned_size_get(uid->length) - uid->length);
                            extension_length -= kaa_aligned_size_get(uid->length);
                            if (!self->uids) {
                                KAA_RETURN_IF_NIL(self->uids = kaa_list_create(uid), KAA_ERR_NOMEM);
                            } else {
                                KAA_RETURN_IF_NIL(kaa_list_push_back(self->uids, uid), KAA_ERR_NOMEM);
                            }
                        }
                        if (notification->size) {
                            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "GOING TO READ NOTIFICATION BODY");
                            avro_reader_t avro_reader = avro_reader_memory(reader->current, notification->size);
                            KAA_RETURN_IF_NIL(avro_reader, KAA_ERR_NOMEM);
                            notification->notification_body = KAA_NOTIFICATION_DESERIALIZE(avro_reader);
                            KAA_RETURN_IF_NIL(notification->notification_body, KAA_ERR_NOMEM);
                            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "FINISHED READING NOTIFICATION BODY");
                            avro_reader_free(avro_reader);
                        }
                        reader->current += kaa_aligned_size_get(notification->size);
                        extension_length -= kaa_aligned_size_get(notification->size);
                        if (!notifications) {
                            KAA_RETURN_IF_NIL(notifications = kaa_list_create(notification), KAA_ERR_NOMEM);
                        } else {
                            KAA_RETURN_IF_NIL(kaa_list_push_back(notifications, notification), KAA_ERR_NOMEM);
                        }
                        if (kaa_list_find_next(self->topics, &kaa_find_topic_by_id, &notification->topic_id)) {
                            kaa_list_t* state_found = NULL;
                            if (!self->topic_states) {
                                kaa_topic_state_t *state = (kaa_topic_state_t *) KAA_MALLOC(sizeof(kaa_topic_state_t));
                                KAA_RETURN_IF_NIL(state, KAA_ERR_NOMEM);
                                state->sqn_number = notification->seq_num;
                                state->topic_id = notification->topic_id;
                                self->topic_states = kaa_list_create(state);
                            } else {
                                if (state_found = kaa_list_find_next(self->topic_states, &kaa_find_topic_state_by_id, &notification->topic_id)) {
                                    kaa_topic_state_t * state = (kaa_topic_state_t *)kaa_list_get_data(state_found);
                                    state->sqn_number = notification->seq_num;
                                    state->topic_id = notification->topic_id;
                                } else {
                                    kaa_topic_state_t *state = (kaa_topic_state_t *) KAA_MALLOC(sizeof(kaa_topic_state_t));
                                    KAA_RETURN_IF_NIL(state, KAA_ERR_NOMEM);
                                    state->sqn_number = notification->seq_num;
                                    state->topic_id = notification->topic_id;
                                    KAA_RETURN_IF_NIL(kaa_list_push_back(self->topic_states, state), KAA_ERR_NOMEM);
                                }
                            }
                        }
                    }
                    err = kaa_notification_received(self, notifications);
                    if (err) {
                        KAA_LOG_WARN(self->logger, err, "Failed to notify listeners.");
                    }
                    kaa_list_destroy(notifications, &kaa_notification_wrapper_destroy);
                    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "REST OF EXTENSION IS %d", extension_length);
                }
                break;
            }
            case TOPICS: {
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "TOPICS RECEIVED");
                uint16_t topic_count = KAA_NTOHS(*((uint16_t *) reader->current));
                reader->current += sizeof(uint16_t);
                extension_length -= sizeof(uint16_t);
                kaa_list_t *topics = NULL;
                kaa_topic_t *topic = NULL;
                while (topic_count--) {
                   KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "EXTENSION SIZE IIS %d",extension_length);
                   topic = (kaa_topic_t *) KAA_MALLOC(sizeof(kaa_topic_t));
                   KAA_RETURN_IF_NIL(topic, KAA_ERR_NOMEM);
                   topic->id = htonll(*((uint64_t *) reader->current));
                   reader->current += sizeof(uint64_t);
                   extension_length -= sizeof(uint64_t);
                   topic->subscription_type =(*((uint8_t *) reader->current) == MANDATORY) ? MANDATORY : OPTIONAL;
                   reader->current += sizeof(uint16_t);
                   extension_length -= sizeof(uint16_t);
                   topic->name_length = KAA_NTOHS(*((uint16_t *) reader->current));
                   reader->current += sizeof(uint16_t);
                   extension_length -= sizeof(uint16_t);
                   topic->name = (char *) KAA_MALLOC(topic->name_length);
                   KAA_RETURN_IF_NIL(topic->name, KAA_ERR_NOMEM);
                   err = kaa_platform_message_read_aligned(reader, topic->name, topic->name_length);
                   if (err) {
                       KAA_LOG_WARN(self->logger, KAA_ERR_BADDATA, "Failed to read UID body");
                   }
                   extension_length -= kaa_aligned_size_get(topic->name_length);
                   if (!topics) {
                       KAA_RETURN_IF_NIL(topics = kaa_list_create(topic), KAA_ERR_NOMEM);
                   } else {
                       KAA_RETURN_IF_NIL(kaa_list_push_back(topics, topic), KAA_ERR_NOMEM);
                   }
                }
                err = kaa_topic_list_updated(self, topics);
                if (err) {
                    KAA_LOG_WARN(self->logger, err, "Failed to notify topic list listeners");
                }
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "EXTENSION SIZE IS %d",extension_length);
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

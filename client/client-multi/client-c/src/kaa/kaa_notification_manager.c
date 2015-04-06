
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



struct kaa_notification_manager_t {
    kaa_list_t                 *mandatory_listeners;
    kaa_list_t                 *topics_listeners;
    kaa_list_t                 *optional_listeners;
    kaa_list_t                 *commands;
    kaa_list_t                 *topics;
    size_t                      notification_sequence_number;

    kaa_status_t               *status;
    kaa_channel_manager_t      *channel_manager;
    kaa_logger_t               *logger;
};

struct kaa_topic_t{
    uint32_t id;
    uint16_t subscription_type; // + reserved
    uint16_t name_length;
    char* name;
};

struct kaa_notification_listener_t {
    callback_t      callback;
    uint32_t         id;
};

struct kaa_optional_notification_listeners_wrapper_t {
    uint32_t topic_id;
    kaa_list_t* listeners;
};

struct kaa_notification_t {
    void            *data;
};

static bool kaa_compare_id_predicate(void *listener, uint32_t *context)
{
    return ((kaa_notification_listener_t *) listener)->id == *context;
}

static bool kaa_compare_optional_listener_list_topic_id (void *optional_listener_list, uint32_t *topic_id)
{
    kaa_optional_notification_listeners_wrapper_t *optional_listener_node = (kaa_optional_notification_listeners_wrapper_t *) optional_listener_list;
    return optional_listener_node->topic_id == *topic_id;
}

static bool kaa_compare_topic_id(void *topic, uint32_t *id)
{
    kaa_topic_t* notification_topic = (kaa_topic_t *) topic;
    return notification_topic->id == *id;

}
static void kaa_listener_deallocator(void *data)
{
    kaa_notification_listener_t * listener = (kaa_notification_listener_t *) data;
    KAA_FREE(listener);
}

static uint32_t kaa_generate_notification_listener_id(void)
{
    static uint32_t id = 0;
    return id++;
}
kaa_error_t kaa_notification_manager_get_size(kaa_notification_manager_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);
    *expected_size = KAA_EXTENSION_HEADER_SIZE;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_notification_listener_create(kaa_notification_listener_t **listener, callback_t callback)
{
    KAA_RETURN_IF_NIL2(listener, callback, KAA_ERR_BADPARAM);
    *listener = (kaa_notification_listener_t *) KAA_MALLOC(sizeof(kaa_notification_listener_t));
    KAA_RETURN_IF_NIL(*listener, KAA_ERR_NOMEM);
    (*listener)->id = kaa_generate_notification_listener_id();
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
    (*self)->commands            =  NULL;
    (*self)->topics              =  NULL;

    (*self)->status              =  status;
    (*self)->channel_manager     =  channel_manager;
    (*self)->logger              =  logger;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_add_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener, uint32_t* listener_id)
{
    KAA_RETURN_IF_NIL2(self, listener, KAA_ERR_BADPARAM);
    if (!self->mandatory_listeners) {
        self->mandatory_listeners = kaa_list_create(listener);
        KAA_RETURN_IF_NIL(self->mandatory_listeners, KAA_ERR_NOMEM);
    } else {
        KAA_RETURN_IF_NIL((kaa_list_push_back(self->mandatory_listeners, listener)), KAA_ERR_NOMEM);
    }
    *listener_id = listener->id; // for user to have convenient way to address notification listener
    return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_notification_listener(kaa_notification_manager_t *self, uint32_t *id)
{
    KAA_RETURN_IF_NIL2(self, id, KAA_ERR_BADPARAM);
    return kaa_list_remove_first(&self->mandatory_listeners, &kaa_compare_id_predicate, id, &kaa_listener_deallocator);
}

kaa_error_t kaa_add_optional_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener
                                                 , uint32_t *topic_id,uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL4(self, listener, topic_id, listener_id, KAA_ERR_BADPARAM);
//    kaa_list_t* topic = kaa_list_find_next(self->topics, &kaa_compare_topic_id, topic_id);
//    KAA_RETURN_IF_NIL(topic, KAA_ERR_BADPARAM);
       if (!self->optional_listeners) {
           kaa_optional_notification_listeners_wrapper_t* wrapper = (kaa_optional_notification_listeners_wrapper_t *)
                                                                     KAA_MALLOC(sizeof(kaa_optional_notification_listeners_wrapper_t));
           KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);
           self->optional_listeners = kaa_list_create(wrapper);
           KAA_RETURN_IF_NIL(self->optional_listeners, KAA_ERR_NOMEM);
           wrapper->listeners = kaa_list_create(listener);
           KAA_RETURN_IF_NIL(wrapper->listeners, KAA_ERR_NOMEM);
           wrapper->topic_id = *topic_id;
       } else {
           kaa_list_t *opt_listeners_node  = kaa_list_find_next(self->optional_listeners, &kaa_compare_optional_listener_list_topic_id, topic_id);
           if (!opt_listeners_node) {
               kaa_optional_notification_listeners_wrapper_t* wrapper = (kaa_optional_notification_listeners_wrapper_t *)
                                                                         KAA_MALLOC(sizeof(kaa_optional_notification_listeners_wrapper_t));
               KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);
               kaa_list_t * new_opt_topic_listener_list = kaa_list_push_back(self->optional_listeners, wrapper);
               KAA_RETURN_IF_NIL(new_opt_topic_listener_list, KAA_ERR_NOMEM);
               wrapper->listeners = kaa_list_create(listener);
               KAA_RETURN_IF_NIL(wrapper->listeners, KAA_ERR_NOMEM);
               wrapper->topic_id = *topic_id;
           } else {
               kaa_list_t *inserted_listener = kaa_list_push_back(kaa_list_get_data(opt_listeners_node), listener);
               KAA_RETURN_IF_NIL(inserted_listener, KAA_ERR_NOMEM);
           }
       }
       *listener_id = listener->id; // for user to have convenient way to address notification listener
       return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_optional_notification_listener(kaa_notification_manager_t *self, uint32_t *topic_id, uint32_t *listener_id)
{
//    KAA_RETURN_IF_NIL3(self, topic_id, listener_id, KAA_ERR_BADPARAM);
//    kaa_list_t* topic = kaa_list_find_next(self->topics, &kaa_compare_topic_id, topic_id);
//    KAA_RETURN_IF_NIL(topic, KAA_ERR_BADPARAM);
    kaa_list_t *opt_listeners_node = kaa_list_find_next(self->optional_listeners, &kaa_compare_optional_listener_list_topic_id, topic_id);
    KAA_RETURN_IF_NIL(opt_listeners_node, KAA_ERR_BADPARAM);
    kaa_optional_notification_listeners_wrapper_t* wrapper = (kaa_optional_notification_listeners_wrapper_t *) kaa_list_get_data(opt_listeners_node);
    return kaa_list_remove_first(&wrapper->listeners, &kaa_compare_id_predicate, listener_id, &kaa_listener_deallocator);
}
#endif

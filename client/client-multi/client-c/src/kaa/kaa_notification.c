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
#include "kaa_notification.h"



struct kaa_notification_manager_t {
    kaa_list_t                 *mandatory_listeners;
    kaa_list_t                 *topics_listeners;
    kaa_list_t                 *optional_listeners;
    kaa_list_t                 *commands;
    size_t                      notification_sequence_number;

    kaa_status_t               *status;
    kaa_channel_manager_t      *channel_manager;
    kaa_logger_t               *logger;
};

struct kaa_topic_t{
    uint32_t id;
    uint16_t subscription_type; // + reserved
    uint16_t name_lengh;
    char* name;
};

struct kaa_notification_listener_t {
    callback_t      callback;
    int32_t         id;
};

struct kaa_notification_t {
    void            *data;
};

static bool kaa_compare_pointers(int32_t id, int32_t context)
{
    return id = context;
}
static int32_t kaa_generate_notification_listener_id(void)
{
    static int32_t id = 0;
    return id++;
}
kaa_error_t kaa_notification_manager_get_size(kaa_notification_manager_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);
    *expected_size = KAA_EXTENSION_HEADER_SIZE;
}

kaa_error_t kaa_notification_listener_create(kaa_notification_listener_t **listener, callback_t callback)
{
    KAA_RETURN_IF_NIL2(listener, callback, KAA_ERR_BADPARAM);
    *listener = (kaa_notification_listener_t *) KAA_MALLOC(sizeof(kaa_notification_listener_t));
    KAA_RETURN_IF_NILL(*listener, KAA_ERR_NOMEM);
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

    (*self)->status              =  status;
    (*self)->channel_manager     =  channel_manager;
    (*self)->logger              =  logger;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_add_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener, int32_t* id)
{
    KAA_RETURN_IF_NIL2(self, listener, KAA_ERR_BADPARAM);
    if (!self->mandatory_listeners) {
        self->mandatory_listeners = kaa_list_create(listener);
        KAA_RETURN_IF_NILL(self->mandatory_listeners, KAA_ERR_NOMEM);
    } else {
        KAA_RETURN_IF_NILL((kaa_list_push_back(self->mandatory_listeners, listener)), KAA_ERR_NOMEM);
    }
    id = &listener->id; // for user to have convenient way to address notification listener
    return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_notification_listener(kaa_notification_manager_t *self, int32_t *id)
{
    KAA_RETURN_IF_NIL2(self, id, KAA_ERR_BADPARAM);


}


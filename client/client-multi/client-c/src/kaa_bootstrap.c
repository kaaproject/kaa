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

#include "kaa_bootstrap.h"

#include <stdlib.h>
#include <string.h>

#include "collections/kaa_list.h"
#include "kaa_mem.h"
#include "kaa_log.h"

struct kaa_bootstrap_manager_t {
    kaa_list_t     *ops_list[KAA_CHANNEL_TYPE_COUNT];       /*!< Operations servers lists by channel type, sorted by increasing priority */
    kaa_list_t     *current_server[KAA_CHANNEL_TYPE_COUNT]; /*!< Pointers to the last returned operations servers in the ops_list */
    kaa_logger_t   *logger;                                 /*!< Logger instance */
};


kaa_error_t kaa_bootstrap_manager_create(kaa_bootstrap_manager_t **bootstrap_manager_p, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL(bootstrap_manager_p, KAA_ERR_BADPARAM);

    *bootstrap_manager_p = (kaa_bootstrap_manager_t*) KAA_MALLOC(sizeof(kaa_bootstrap_manager_t));
    KAA_RETURN_IF_NIL(*bootstrap_manager_p, KAA_ERR_NOMEM);

    memset((*bootstrap_manager_p)->ops_list, 0, sizeof((*bootstrap_manager_p)->ops_list));
    memset((*bootstrap_manager_p)->current_server, 0, sizeof((*bootstrap_manager_p)->current_server));
    (*bootstrap_manager_p)->logger = logger;

    return KAA_ERR_NONE;
}


void kaa_bootstrap_manager_destroy(kaa_bootstrap_manager_t *self)
{
    KAA_RETURN_IF_NIL(self,);
    for (size_t i = 0; i < KAA_CHANNEL_TYPE_COUNT; ++i) {
        if (self->ops_list[i]) {
            // Since operations server data is not copied by Kaa SDK,
            // memory management of kaa_ops_t * provided using #kaa_add_operation_server(...)
            // belongs to the user of the library.
            kaa_list_destroy_no_data_cleanup(self->ops_list[i]);
        }
    }

    KAA_FREE(self);
}


kaa_error_t kaa_bootstrap_manager_add_operations_server(kaa_bootstrap_manager_t *self, kaa_ops_t *server)
{
    KAA_RETURN_IF_NIL2(self, server, KAA_ERR_BADPARAM);

    kaa_list_t *ops_list = self->ops_list[server->channel_type];
    if (!ops_list) {
        self->ops_list[server->channel_type] = kaa_list_create(server);
        KAA_RETURN_IF_NIL(self->ops_list[server->channel_type], KAA_ERR_NOMEM);
    } else {
        kaa_list_t *current_server = ops_list
                , *previous_server = NULL;

        while (current_server) {
            // Ensure sorting by increasing priority
            if (((kaa_ops_t*) kaa_list_get_data(current_server))->priority >= server->priority)
                break;

            previous_server = current_server;
            current_server = kaa_list_next(current_server);
        }

        if (!previous_server) {
            // Add to the front of the list
            self->ops_list[server->channel_type] = kaa_list_push_front(current_server, server);
            KAA_RETURN_IF_NIL(self->ops_list[server->channel_type], KAA_ERR_NOMEM);
        } else if (!current_server) {
            // Add to the end of the list
            if (!kaa_list_push_back(previous_server, server))
                return KAA_ERR_NOMEM;
        } else {
            // Insert in the middle of the list
            if (!kaa_list_insert_after(previous_server, server))
                return KAA_ERR_NOMEM;
        }
    }

    return KAA_ERR_NONE;
}


kaa_ops_t* kaa_bootstrap_manager_get_current_operations_server(kaa_bootstrap_manager_t *self, kaa_channel_type_t channel_type)
{
    KAA_RETURN_IF_NIL(self, NULL);
    if (channel_type >= KAA_CHANNEL_TYPE_COUNT)
        return NULL;

    if (self->current_server[channel_type]) {
        return (kaa_ops_t*) kaa_list_get_data(self->current_server[channel_type]);
    } else if (self->ops_list[channel_type]) {
        self->current_server[channel_type] = self->ops_list[channel_type];
        return (kaa_ops_t*) kaa_list_get_data(self->ops_list[channel_type]);
    }
    return NULL;
}


kaa_ops_t* kaa_bootstrap_manager_get_next_operations_server(kaa_bootstrap_manager_t *self, kaa_channel_type_t channel_type)
{
    KAA_RETURN_IF_NIL(self, NULL);
    if (channel_type >= KAA_CHANNEL_TYPE_COUNT)
        return NULL;

    if (!self->current_server[channel_type]) {
        return kaa_bootstrap_manager_get_current_operations_server(self, channel_type);
    } else {
        kaa_list_t *next_server_node = kaa_list_next(self->current_server[channel_type]);
        if (next_server_node) {
            self->current_server[channel_type] = next_server_node;
            return (kaa_ops_t*) kaa_list_get_data(next_server_node);
        }
    }
    return NULL;
}

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

#include "kaa_channel_manager.h"
#include "kaa_log.h"
#include "kaa_list.h"
#include "kaa_mem.h"

struct kaa_channel_manager_t {
    kaa_list_t                 *sync_handlers;
    kaa_sync_all_handler_fn     on_sync_all;
    kaa_logger_t               *logger;
};

typedef struct kaa_sync_details {
    kaa_sync_handler_fn sync_fn;
    kaa_service_t      *supported_services;
    size_t              supported_services_size;
} kaa_sync_details;

static void destroy_sync_details(void *data)
{
    if (data) {
        kaa_sync_details *sync_details = (kaa_sync_details *)data;
        sync_details->sync_fn = NULL;
        sync_details->supported_services_size = 0;
        KAA_FREE(sync_details->supported_services);
        sync_details->supported_services = NULL;
    }
}

kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL(channel_manager_p, KAA_ERR_BADPARAM);

    *channel_manager_p = (kaa_channel_manager_t *) KAA_MALLOC(sizeof(kaa_channel_manager_t));
    if (!(*channel_manager_p))
        return KAA_ERR_NOMEM;

    (*channel_manager_p)->sync_handlers = NULL;
    (*channel_manager_p)->on_sync_all = NULL;
    (*channel_manager_p)->logger = logger;
    return KAA_ERR_NONE;
}

void kaa_channel_manager_destroy(kaa_channel_manager_t *self)
{
    if (self) {
        kaa_list_destroy(self->sync_handlers, destroy_sync_details);
        KAA_FREE(self);
    }
}

kaa_error_t kaa_channel_manager_add_sync_handler(kaa_channel_manager_t *self
        , kaa_sync_handler_fn handler, const kaa_service_t *supported_services, size_t services_count)
{
    KAA_RETURN_IF_NIL4(self, handler, supported_services, services_count, KAA_ERR_BADPARAM);

    kaa_sync_details *sync = (kaa_sync_details *) KAA_MALLOC(sizeof(kaa_sync_details));
    if (!sync)
        return KAA_ERR_NOMEM;

    sync->sync_fn = handler;
    sync->supported_services_size = services_count;
    sync->supported_services = (kaa_service_t *) KAA_MALLOC(services_count * sizeof(kaa_service_t));
    while (services_count--)
        sync->supported_services[services_count] = supported_services[services_count];

    // Add to the list of handlers (or create the list if there's none yet)
    self->sync_handlers = self->sync_handlers
            ? kaa_list_push_front(self->sync_handlers, sync)
            : kaa_list_create(sync);
    if (!self->sync_handlers) {
        KAA_FREE(sync->supported_services);
        KAA_FREE(sync);
        return KAA_ERR_NOMEM;
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_channel_manager_remove_sync_handler(kaa_channel_manager_t *self, kaa_sync_handler_fn handler)
{
    kaa_list_t *handlers = self->sync_handlers;
    kaa_sync_details *details = kaa_list_get_data(handlers);

    while (details) {
        if (details->sync_fn == handler) {
            if (!kaa_list_remove_at(&self->sync_handlers, handlers, destroy_sync_details))
                return KAA_ERR_BAD_STATE;
            return KAA_ERR_NONE;
        }
        handlers = kaa_list_next(handlers);
        details = kaa_list_get_data(handlers);
    }

    return KAA_ERR_NOT_FOUND;
}

kaa_sync_handler_fn kaa_channel_manager_get_sync_handler(kaa_channel_manager_t *self, kaa_service_t service_type)
{
    KAA_RETURN_IF_NIL(self, NULL);

    kaa_list_t *handlers = self->sync_handlers;
    kaa_sync_details *details = (kaa_sync_details *) kaa_list_get_data(handlers);
    while (details) {
        size_t service_count = details->supported_services_size;
        kaa_service_t* services = details->supported_services;
        for (;service_count--;) {
            if (*services++ == service_type)
                return details->sync_fn;
        }
        handlers = kaa_list_next(handlers);
        details = (kaa_sync_details *) kaa_list_get_data(handlers);
    }

    return NULL;
}

kaa_error_t kaa_channel_manager_set_sync_all_handler(kaa_channel_manager_t *self, kaa_sync_all_handler_fn handler)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    self->on_sync_all = handler;
    return KAA_ERR_NONE;
}

kaa_sync_all_handler_fn kaa_channel_manager_get_sync_all_handler(kaa_channel_manager_t *self)
{
    return self ? self->on_sync_all : NULL;
}

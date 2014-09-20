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
#include "kaa_context.h"
#include "kaa_mem.h"

typedef struct kaa_sync_details {
    kaa_sync_t          sync_fn;
    kaa_service_t *     supported_services;
    size_t              supported_services_size;
} kaa_sync_details;

static void destroy_sync_details(void *sync_details)
{
    if (sync_details) {
        kaa_sync_details * details = (kaa_sync_details *)sync_details;
        details->sync_fn = NULL;
        details->supported_services_size = 0;
        KAA_FREE(details->supported_services);
    }
}

struct kaa_channel_manager_t {

    kaa_list_t *sync_handlers;
    kaa_sync_t on_sync;
    kaa_sync_all_t on_sync_all;

};

static kaa_error_t register_sync_handler(kaa_channel_manager_t* manager, size_t services_count, const kaa_service_t services[], kaa_sync_t sync_fn)
{
    if (sync_fn != NULL && services_count > 0 && services != NULL) {

        kaa_sync_details * sync = KAA_CALLOC(1, sizeof(kaa_sync_details));
        if (sync == NULL) {
            return KAA_ERR_NOMEM;
        }
        sync->sync_fn = sync_fn;
        sync->supported_services_size = services_count;
        sync->supported_services = KAA_CALLOC(services_count, sizeof(kaa_service_t));
        for (;services_count--;) {
            sync->supported_services[services_count] = services[services_count];
        }

        if (manager->sync_handlers == NULL) {
            manager->sync_handlers = kaa_list_create(sync); // FIXME: check return value
        } else {
            kaa_list_push_front(&manager->sync_handlers, sync); // FIXME: check return value
        }

        return KAA_ERR_NONE;
    }
    return KAA_ERR_BADPARAM;
}

static kaa_error_t remove_sync_handler(kaa_channel_manager_t* manager, kaa_sync_t handler)
{
    kaa_list_t * handlers = manager->sync_handlers;
    kaa_sync_details * details = kaa_list_get_data(handlers);
    while (details != NULL) {
        if (details->sync_fn == handler) {
            kaa_list_remove_at(&manager->sync_handlers, handlers, destroy_sync_details);
            return KAA_ERR_NONE;
        }
        handlers = kaa_list_next(handlers);
        details = kaa_list_get_data(handlers);
    }
    return KAA_ERR_NOT_FOUND;
}

static kaa_sync_t get_sync_handler_by_service_type(kaa_channel_manager_t* manager, kaa_service_t service_type)
{
    kaa_list_t *handlers = manager->sync_handlers;

    kaa_sync_details * details = kaa_list_get_data(handlers);
    while (details != NULL) {
        size_t service_count = details->supported_services_size;
        kaa_service_t* services = details->supported_services;
        for (;service_count--;) {
            if (*services++ == service_type) {
                return details->sync_fn;
            }
        }
        handlers = kaa_list_next(handlers);
        details = kaa_list_get_data(handlers);
    }

    return NULL;
}

/**
 * PUBLIC STUFF
 */
kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t ** manager_p)
{
    kaa_channel_manager_t * manager = KAA_MALLOC(kaa_channel_manager_t);
    if (manager == NULL) {
        return KAA_ERR_NOMEM;
    }
    manager->sync_handlers = NULL;
    manager->on_sync = NULL;
    manager->on_sync_all = NULL;

    *manager_p = manager;
    return KAA_ERR_NONE;
}

void kaa_channel_manager_destroy(kaa_context_t *context)
{
    kaa_list_destroy(context->channel_manager->sync_handlers, destroy_sync_details);
    KAA_FREE(context->channel_manager);
}

void kaa_channel_manager_set_sync_handler(kaa_context_t *context, kaa_sync_t handler, size_t services_count, const kaa_service_t supported_services[])
{
    register_sync_handler(context->channel_manager, services_count, supported_services, handler);
}

void kaa_channel_manager_set_sync_all_handler(kaa_context_t *context, kaa_sync_all_t handler)
{
    context->channel_manager->on_sync_all = handler;
}

kaa_sync_t kaa_channel_manager_get_sync_handler(kaa_context_t *context, kaa_service_t service)
{
    return get_sync_handler_by_service_type(context->channel_manager, service);
}

kaa_sync_all_t kaa_channel_manager_get_sync_all_handler(kaa_context_t *context)
{
    return context->channel_manager->on_sync_all;
}

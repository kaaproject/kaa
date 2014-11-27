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

#include "kaa_user.h"
#include "kaa_mem.h"
#include "kaa_status.h"
#include "kaa_context.h"
#include <string.h>

typedef struct user_info_t_ {
    char *  user_external_id;
    size_t  user_external_id_len;
    char *  user_access_token;
    size_t  user_access_token_len;
} user_info_t;

struct kaa_user_manager_t {
    kaa_attachment_status_listeners_t   attachment_listeners;
    user_info_t *                       user_info;
    bool                                is_waiting_user_attach_response;
};

static kaa_service_t user_sync_services[1] = {KAA_SERVICE_USER};

static user_info_t * create_user_info(const char * external_id, const char * user_access_token)
{
    if (external_id == NULL || user_access_token == NULL) {
        return NULL;
    }

    user_info_t * user_info = KAA_CALLOC(1, sizeof(user_info_t));
    if (user_info == NULL) {
        return NULL;
    }
    user_info->user_external_id_len= strlen(external_id);
    user_info->user_access_token_len = strlen(user_access_token);

    user_info->user_external_id = KAA_CALLOC(user_info->user_external_id_len + 1, sizeof(char));
    if (user_info->user_external_id == NULL)
    {
        KAA_FREE(user_info);
        return NULL;
    }
    memcpy(user_info->user_external_id, external_id, user_info->user_external_id_len);
    user_info->user_access_token = KAA_CALLOC(user_info->user_access_token_len + 1, sizeof(char));
    if (user_info->user_access_token == NULL)
    {
        KAA_FREE(user_info->user_external_id);
        KAA_FREE(user_info);
        return NULL;
    }
    memcpy(user_info->user_access_token, user_access_token, user_info->user_access_token_len);

    return user_info;
}

static void destroy_user_info(user_info_t *user_info)
{
    if (user_info != NULL) {
        KAA_FREE(user_info->user_external_id);
        KAA_FREE(user_info->user_access_token);
        KAA_FREE(user_info);
    }
}

kaa_error_t kaa_create_user_manager(kaa_user_manager_t ** user_manager_p)
{
    kaa_user_manager_t * user_manager = KAA_MALLOC(kaa_user_manager_t);
    if (user_manager == NULL) {
        return KAA_ERR_NOMEM;
    }
    user_manager->attachment_listeners.on_attached_callback = NULL;
    user_manager->attachment_listeners.on_detached_callback = NULL;
    user_manager->attachment_listeners.on_response_callback = NULL;
    user_manager->user_info = NULL;
    user_manager->is_waiting_user_attach_response = false;

    *user_manager_p = user_manager;
    return KAA_ERR_NONE;
}

void kaa_destroy_user_manager(kaa_user_manager_t *manager)
{
    if (manager != NULL) {
        destroy_user_info(manager->user_info);
        KAA_FREE(manager);
    }
}

kaa_error_t kaa_user_attach_to_user(void *ctx, const char *ext_id, const char *acc_tok)
{
    KAA_NOT_VOID(ctx, KAA_ERR_NOT_INITED)

    kaa_context_t * context = (kaa_context_t *)ctx;
    KAA_NOT_VOID(context->user_manager, KAA_ERR_NOT_INITED)

    kaa_user_manager_t * user_manager = context->user_manager;
    if (user_manager->is_waiting_user_attach_response) {
        destroy_user_info(user_manager->user_info);
        user_manager->user_info = NULL;
        user_manager->is_waiting_user_attach_response = false;
    }
    user_manager->user_info = create_user_info(ext_id, acc_tok);
    if (user_manager->user_info) {
        kaa_sync_t sync = kaa_channel_manager_get_sync_handler(context, user_sync_services[0]);
        if (sync) {
            (*sync)(1, user_sync_services);
        }
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_set_attachment_listeners(void *ctx, kaa_attachment_status_listeners_t listeners)
{
    KAA_NOT_VOID(ctx, KAA_ERR_NOT_INITED)

    kaa_context_t * context = (kaa_context_t *)ctx;
    KAA_NOT_VOID(context->user_manager, KAA_ERR_NOT_INITED)
    KAA_NOT_VOID(context->status, KAA_ERR_NOT_INITED)

    kaa_user_manager_t *user_manager = context->user_manager;
    user_manager->attachment_listeners = listeners;

    if (listeners.on_response_callback != NULL) {
        (*listeners.on_response_callback)(kaa_is_endpoint_attached_to_user(context->status));
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_user_compile_request(void *ctx, kaa_user_sync_request_t** request_p, size_t requestId)
{
    KAA_NOT_VOID(ctx, KAA_ERR_NOT_INITED)
    KAA_NOT_VOID(request_p, KAA_ERR_BADPARAM)
    *request_p = NULL;
    kaa_context_t * context = (kaa_context_t *)ctx;

    KAA_NOT_VOID(context->user_manager, KAA_ERR_NOT_INITED)
    kaa_user_manager_t * user_manager = context->user_manager;

    kaa_user_sync_request_t *request = kaa_create_user_sync_request();
    if (request == NULL) {
        return KAA_ERR_NOMEM;
    }

    request->endpoint_attach_requests = kaa_create_array_endpoint_attach_request_null_union_null_branch();

    if (request->endpoint_attach_requests == NULL) {
        KAA_FREE(request);
        return KAA_ERR_NOMEM;
    }

    request->endpoint_detach_requests = kaa_create_array_endpoint_detach_request_null_union_null_branch();

    if (request->endpoint_detach_requests == NULL) {
        request->endpoint_attach_requests->destruct(request->endpoint_attach_requests);
        KAA_FREE(request->endpoint_attach_requests);
        KAA_FREE(request);
        return KAA_ERR_NOMEM;
    }

    if (user_manager->user_info && user_manager->is_waiting_user_attach_response == false) {
        kaa_user_attach_request_t * user_attach_request = kaa_create_user_attach_request();

        user_attach_request->user_external_id = KAA_CALLOC(user_manager->user_info->user_external_id_len + 1, sizeof(char));
        memcpy(user_attach_request->user_external_id, user_manager->user_info->user_external_id, user_manager->user_info->user_external_id_len);
        user_attach_request->user_access_token = KAA_CALLOC(user_manager->user_info->user_access_token_len + 1, sizeof(char));
        memcpy(user_attach_request->user_access_token, user_manager->user_info->user_access_token, user_manager->user_info->user_access_token_len);

        user_manager->is_waiting_user_attach_response = true;
        request->user_attach_request = kaa_create_record_user_attach_request_null_union_user_attach_request_branch();
        request->user_attach_request->data = user_attach_request;
    } else {
        request->user_attach_request = kaa_create_record_user_attach_request_null_union_null_branch();
        if (request->user_attach_request == NULL) {
            request->destruct(request);
            KAA_FREE(request);
            return KAA_ERR_NOMEM;
        }
    }

    *request_p = request;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_user_handle_sync(void *ctx, kaa_user_attach_response_t * usr_attach_response, kaa_user_attach_notification_t *attach, kaa_user_detach_notification_t *detach)
{
    KAA_NOT_VOID(ctx, KAA_ERR_NOT_INITED)
    if (attach == NULL && detach == NULL && usr_attach_response == NULL)
    {
        return KAA_ERR_NONE;
    }

    kaa_context_t * context = (kaa_context_t *)ctx;
    KAA_NOT_VOID(context->status, KAA_ERR_NOT_INITED)
    KAA_NOT_VOID(context->user_manager, KAA_ERR_NOT_INITED)

    kaa_status_t * status = context->status;
    kaa_user_manager_t * manager = context->user_manager;

    if (usr_attach_response != NULL)
    {
        destroy_user_info(manager->user_info);
        manager->user_info = NULL;
        manager->is_waiting_user_attach_response = false;
        if (usr_attach_response->result == ENUM_SYNC_RESPONSE_RESULT_TYPE_SUCCESS)
        {
            kaa_set_endpoint_attached_to_user(status, true);
        }
        if (manager->attachment_listeners.on_response_callback != NULL)
        {
            (*manager->attachment_listeners.on_response_callback)(true);
        }
    }
    if (attach != NULL)
    {
        kaa_set_endpoint_attached_to_user(status, true);
        if (manager->attachment_listeners.on_attached_callback != NULL)
        {
            (*manager->attachment_listeners.on_attached_callback)(attach->user_external_id, attach->endpoint_access_token);
        }
    }
    if (detach != NULL)
    {
        kaa_set_endpoint_attached_to_user(status, false);
        if (manager->attachment_listeners.on_detached_callback != NULL)
        {
            (*manager->attachment_listeners.on_detached_callback)(detach->endpoint_access_token);
        }
    }

    return KAA_ERR_NONE;
}

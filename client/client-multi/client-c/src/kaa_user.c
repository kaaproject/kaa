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

#include <string.h>

#include "kaa_mem.h"
#include "kaa_status.h"
#include "kaa_channel_manager.h"

extern kaa_sync_handler_fn kaa_channel_manager_get_sync_handler(kaa_channel_manager_t *self, kaa_service_t service_type);

typedef struct {
    char   *user_external_id;
    size_t  user_external_id_len;
    char   *user_access_token;
    size_t  user_access_token_len;
} user_info_t;

struct kaa_user_manager_t {
    kaa_attachment_status_listeners_t   attachment_listeners;               /*!< Client code-defined user attachment listeners */
    user_info_t                        *user_info;                          /*!< User credentials */
    bool                                is_waiting_user_attach_response;
    kaa_status_t                       *status;                             /*!< Reference to global status */
    kaa_channel_manager_t              *channel_manager;                    /*!< Reference to global channel manager */
};

static kaa_service_t user_sync_services[1] = {KAA_SERVICE_USER};

static user_info_t* create_user_info(const char *external_id, const char *user_access_token)
{
    KAA_RETURN_IF_NIL2(external_id, user_access_token, NULL);

    user_info_t *user_info = (user_info_t *) KAA_MALLOC(sizeof(user_info_t));
    KAA_RETURN_IF_NIL(user_info, NULL);

    user_info->user_external_id_len = strlen(external_id);
    user_info->user_access_token_len = strlen(user_access_token);

    user_info->user_external_id =(char *) KAA_MALLOC((user_info->user_external_id_len + 1) * sizeof(char));
    if (!user_info->user_external_id) {
        KAA_FREE(user_info);
        return NULL;
    }
    strcpy(user_info->user_external_id, external_id);

    user_info->user_access_token = (char *) KAA_MALLOC((user_info->user_access_token_len + 1) * sizeof(char));
    if (!user_info->user_access_token) {
        KAA_FREE(user_info->user_external_id);
        KAA_FREE(user_info);
        return NULL;
    }
    strcpy(user_info->user_access_token, user_access_token);

    return user_info;
}

static void destroy_user_info(user_info_t *user_info)
{
    if (user_info) {
        KAA_FREE(user_info->user_external_id);
        KAA_FREE(user_info->user_access_token);
        KAA_FREE(user_info);
    }
}

kaa_error_t kaa_user_manager_create(kaa_user_manager_t **user_manager_p, kaa_status_t *status, kaa_channel_manager_t *channel_manager)
{
    KAA_RETURN_IF_NIL2(user_manager_p, status, KAA_ERR_BADPARAM);

    *user_manager_p = (kaa_user_manager_t *) KAA_MALLOC(sizeof(kaa_user_manager_t));
    KAA_RETURN_IF_NIL((*user_manager_p), KAA_ERR_NOMEM);

    (*user_manager_p)->attachment_listeners.on_attached_callback = NULL;
    (*user_manager_p)->attachment_listeners.on_detached_callback = NULL;
    (*user_manager_p)->attachment_listeners.on_response_callback = NULL;
    (*user_manager_p)->user_info = NULL;
    (*user_manager_p)->is_waiting_user_attach_response = false;
    (*user_manager_p)->status = status;
    (*user_manager_p)->channel_manager = channel_manager;

    return KAA_ERR_NONE;
}

void kaa_user_manager_destroy(kaa_user_manager_t *self)
{
    if (self) {
        destroy_user_info(self->user_info);
        KAA_FREE(self);
    }
}

kaa_error_t kaa_user_manager_attach_to_user(kaa_user_manager_t *self, const char *user_external_id, const char *access_token)
{
    KAA_RETURN_IF_NIL3(self, user_external_id, access_token, KAA_ERR_BADPARAM);

    if (self->is_waiting_user_attach_response) {
        destroy_user_info(self->user_info);
        self->user_info = NULL;
        self->is_waiting_user_attach_response = false;
    }

    self->user_info = create_user_info(user_external_id, access_token);
    if (!self->user_info)
        return KAA_ERR_NOMEM;

    kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(self->channel_manager, user_sync_services[0]);
    if (sync)
        (*sync)(user_sync_services, 1);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_user_manager_set_attachment_listeners(kaa_user_manager_t *self, kaa_attachment_status_listeners_t listeners)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    self->attachment_listeners = listeners;

    if (listeners.on_response_callback) {
        bool is_attached = false;
        if (kaa_is_endpoint_attached_to_user(self->status, &is_attached))
            return KAA_ERR_BAD_STATE;
        (*listeners.on_response_callback)(is_attached);
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_user_compile_request(kaa_user_manager_t *self, kaa_user_sync_request_t** request_p, size_t requestId)
{
    KAA_RETURN_IF_NIL2(self, request_p, KAA_ERR_BADPARAM);
    *request_p = NULL;

    kaa_user_sync_request_t *request = kaa_user_sync_request_create();
    KAA_RETURN_IF_NIL(request, KAA_ERR_NOMEM);

    request->endpoint_attach_requests = kaa_array_endpoint_attach_request_null_union_null_branch_create();
    if (!request->endpoint_attach_requests) {
        request->destroy(request);
        return KAA_ERR_NOMEM;
    }

    request->endpoint_detach_requests = kaa_array_endpoint_detach_request_null_union_null_branch_create();
    if (!request->endpoint_detach_requests) {
        request->destroy(request);
        return KAA_ERR_NOMEM;
    }

    if (self->user_info && !self->is_waiting_user_attach_response) {
        kaa_user_attach_request_t *user_attach_request = kaa_user_attach_request_create();
        if (!user_attach_request) {
            request->destroy(request);
            return KAA_ERR_NOMEM;
        }

        user_attach_request->user_external_id =
                kaa_string_move_create(self->user_info->user_external_id, NULL); // destructor is not needed
        user_attach_request->user_access_token =
                kaa_string_move_create(self->user_info->user_access_token, NULL); // destructor is not needed

        self->is_waiting_user_attach_response = true;
        request->user_attach_request = kaa_record_user_attach_request_null_union_user_attach_request_branch_create();
        if (!request->user_attach_request) {
            request->destroy(request);
            return KAA_ERR_NOMEM;
        }
        request->user_attach_request->data = user_attach_request;
    } else {
        request->user_attach_request = kaa_record_user_attach_request_null_union_null_branch_create();
        if (!request->user_attach_request) {
            request->destroy(request);
            return KAA_ERR_NOMEM;
        }
    }

    *request_p = request;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_user_manager_handle_sync(kaa_user_manager_t *self
        , kaa_user_attach_response_t * user_attach_response, kaa_user_attach_notification_t *attach, kaa_user_detach_notification_t *detach)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    if (user_attach_response) {
        destroy_user_info(self->user_info);
        self->user_info = NULL;
        self->is_waiting_user_attach_response = false;
        if (user_attach_response->result == ENUM_SYNC_RESPONSE_RESULT_TYPE_SUCCESS)
            if (kaa_set_endpoint_attached_to_user(self->status, true))
                return KAA_ERR_BAD_STATE;
        if (self->attachment_listeners.on_response_callback)
            (*self->attachment_listeners.on_response_callback)(true);
    }
    if (attach) {
        if (kaa_set_endpoint_attached_to_user(self->status, true))
            return KAA_ERR_BAD_STATE;
        if (self->attachment_listeners.on_attached_callback)
            (*self->attachment_listeners.on_attached_callback)(attach->user_external_id->data, attach->endpoint_access_token->data);
    }
    if (detach) {
        if (kaa_set_endpoint_attached_to_user(self->status, false))
            return KAA_ERR_BAD_STATE;
        if (self->attachment_listeners.on_detached_callback)
            (*self->attachment_listeners.on_detached_callback)(detach->endpoint_access_token->data);
    }

    return KAA_ERR_NONE;
}

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

extern kaa_sync_handler_fn kaa_channel_manager_get_sync_handler(kaa_channel_manager_t *this, kaa_service_t service_type);

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

void kaa_user_manager_destroy(kaa_user_manager_t *this)
{
    if (this) {
        destroy_user_info(this->user_info);
        KAA_FREE(this);
    }
}

kaa_error_t kaa_user_manager_attach_to_user(kaa_user_manager_t *this, const char *user_external_id, const char *access_token)
{
    KAA_RETURN_IF_NIL3(this, user_external_id, access_token, KAA_ERR_BADPARAM);

    if (this->is_waiting_user_attach_response) {
        destroy_user_info(this->user_info);
        this->user_info = NULL;
        this->is_waiting_user_attach_response = false;
    }

    this->user_info = create_user_info(user_external_id, access_token);
    if (!this->user_info)
        return KAA_ERR_NOMEM;

    kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(this->channel_manager, user_sync_services[0]);
    if (sync)
        (*sync)(user_sync_services, 1);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_user_manager_set_attachment_listeners(kaa_user_manager_t *this, kaa_attachment_status_listeners_t listeners)
{
    KAA_RETURN_IF_NIL(this, KAA_ERR_BADPARAM);

    this->attachment_listeners = listeners;

    if (listeners.on_response_callback)
        (*listeners.on_response_callback)(kaa_is_endpoint_attached_to_user(this->status));
    return KAA_ERR_NONE;
}

kaa_error_t kaa_user_compile_request(kaa_user_manager_t *this, kaa_user_sync_request_t** request_p, size_t requestId)
{
    KAA_RETURN_IF_NIL2(this, request_p, KAA_ERR_BADPARAM);
    *request_p = NULL;

    kaa_user_sync_request_t *request = kaa_create_user_sync_request();
    KAA_RETURN_IF_NIL(request, KAA_ERR_NOMEM);

    request->endpoint_attach_requests = kaa_create_array_endpoint_attach_request_null_union_null_branch();
    if (!request->endpoint_attach_requests) {
        KAA_FREE(request);
        return KAA_ERR_NOMEM;
    }

    request->endpoint_detach_requests = kaa_create_array_endpoint_detach_request_null_union_null_branch();
    if (!request->endpoint_detach_requests) {
        request->endpoint_attach_requests->destroy(request->endpoint_attach_requests);
        KAA_FREE(request->endpoint_attach_requests);
        KAA_FREE(request);
        return KAA_ERR_NOMEM;
    }

    if (this->user_info && !this->is_waiting_user_attach_response) {
        // FIXME: handle out of memory
        kaa_user_attach_request_t *user_attach_request = kaa_create_user_attach_request();

        user_attach_request->user_external_id = (char *) KAA_MALLOC((this->user_info->user_external_id_len + 1) * sizeof(char));
        strcpy(user_attach_request->user_external_id, this->user_info->user_external_id);
        user_attach_request->user_access_token = (char *) KAA_MALLOC((this->user_info->user_access_token_len + 1) * sizeof(char));
        strcpy(user_attach_request->user_access_token, this->user_info->user_access_token);

        this->is_waiting_user_attach_response = true;
        request->user_attach_request = kaa_create_record_user_attach_request_null_union_user_attach_request_branch();
        request->user_attach_request->data = user_attach_request;
    } else {
        request->user_attach_request = kaa_create_record_user_attach_request_null_union_null_branch();
        if (!request->user_attach_request) {
            request->destroy(request);
            KAA_FREE(request);
            return KAA_ERR_NOMEM;
        }
    }

    *request_p = request;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_user_manager_handle_sync(kaa_user_manager_t *this
        , kaa_user_attach_response_t * user_attach_response, kaa_user_attach_notification_t *attach, kaa_user_detach_notification_t *detach)
{
    KAA_RETURN_IF_NIL(this, KAA_ERR_BADPARAM);

    if (user_attach_response) {
        destroy_user_info(this->user_info);
        this->user_info = NULL;
        this->is_waiting_user_attach_response = false;
        if (user_attach_response->result == ENUM_SYNC_RESPONSE_RESULT_TYPE_SUCCESS)
            kaa_set_endpoint_attached_to_user(this->status, true);
        if (this->attachment_listeners.on_response_callback)
            (*this->attachment_listeners.on_response_callback)(true);
    }
    if (attach) {
        kaa_set_endpoint_attached_to_user(this->status, true);
        if (this->attachment_listeners.on_attached_callback)
            (*this->attachment_listeners.on_attached_callback)(attach->user_external_id, attach->endpoint_access_token);
    }
    if (detach) {
        kaa_set_endpoint_attached_to_user(this->status, false);
        if (this->attachment_listeners.on_detached_callback)
            (*this->attachment_listeners.on_detached_callback)(detach->endpoint_access_token);
    }

    return KAA_ERR_NONE;
}

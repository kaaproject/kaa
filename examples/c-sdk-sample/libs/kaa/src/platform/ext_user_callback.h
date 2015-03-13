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

/**
 * @file ext_user_callback.h
 * @brief External interface for receiving user attachment status used by Kaa User subsystem.
 * Should be implemented to receive user attachment/detachment notifications.
 */

#ifndef EXT_USER_CALLBACK_H_
#define EXT_USER_CALLBACK_H_


#ifdef __cplusplus
extern "C" {
#endif


/**
 * @brief Notifies about the successful attachment of the current endpoint to some user.
 *
 * @param[in]   context                 Callback's context.
 * @param[in]   user_external_id        The ID of the user who has attached a current endpoint.
 * @param[in]   endpoint_access_token   The access token of the endpoint which has attached a current endpoint.
 *
 * @return  Error code
 */
typedef kaa_error_t (*on_attached_fn)(void *context, const char *user_external_id, const char *endpoint_access_token);


/**
 * @brief Indicates that current endpoint was detached from a user.
 *
 * @param[in]   context                 Callback's context.
 * @param[in]   endpoint_access_token   The access token of the endpoint which has detached a current endpoint.
 *
 * @return  Error code
 */
typedef kaa_error_t (*on_detached_fn)(void *context, const char *endpoint_access_token);


/**
 * @brief Notifies about attach status changes.
 *
 * @param[in]   context                 Callback's context.
 * @param[in]   is_attached             True - if a current endpoint was attached to user, false - otherwise.
 *
 * @return  Error code
 */
typedef kaa_error_t (*on_attach_status_changed_fn)(void *context, bool is_attached);


/**
 * @brief Interface for the user attachment status receiver.
 */
typedef struct {
    void                           *context;                /**< Context to pass to all functions below. */
    on_attached_fn                  on_attached_callback;   /**< Called when a current endpoint was attached to the user by another endpoint. */
    on_detached_fn                  on_detached_callback;   /**< Called when a current endpoint was detached from the user by another endpoint. */
    on_attach_status_changed_fn     on_response_callback;   /**< Called on user attach/detach response from the server. See @link kaa_user_manager_attach_to_user @endlink. */
} kaa_attachment_status_listeners_t;


#ifdef __cplusplus
}    /* extern "C" */
#endif

#endif /* EXT_USER_CALLBACK_H_ */

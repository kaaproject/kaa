/*
 * Copyright 2014-2016 CyberVision, Inc.
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

#include "kaa_common.h"

#ifdef __cplusplus
extern "C" {
#endif



typedef enum {
    NO_VERIFIER_CONFIGURED = 0,
    TOKEN_INVALID          = 1,
    TOKEN_EXPIRED          = 2,
    INTERNAL_ERROR         = 3,
    CONNECTION_ERROR       = 4,
    REMOTE_ERROR           = 5,
    OTHER                  = 6
} user_verifier_error_code_t;



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
typedef kaa_error_t (*on_attach_success_fn)(void *context);

/**
 * @brief Notifies about attach attempt was failed.
 *
 * @param[in]   context       Callback's context.
 * @param[in]   error_code    One of @link user_verifier_error_code_t @endlink values.
 * @param[in]   reason        Additional description for the error. May be NULL.
 *
 * @return  Error code
 */
typedef kaa_error_t (*on_attach_failed_fn)(void *context, user_verifier_error_code_t error_code, const char *reason);



/**
 * @brief Interface for the user attachment status receiver.
 */
typedef struct {
    void                           *context;           /**< Context to pass to all functions below. */
    on_attached_fn                  on_attached;       /**< Called when the current endpoint was attached to the user by another endpoint. */
    on_detached_fn                  on_detached;       /**< Called when the current endpoint was detached from the user by another endpoint. */
    on_attach_success_fn            on_attach_success; /**< Called when the current endpoint was successfully attached to the user. See @link kaa_user_manager_attach_to_user @endlink. */
    on_attach_failed_fn             on_attach_failed;  /**< Called the attach attempt is failed. */
} kaa_attachment_status_listeners_t;


/**
 * @brief Notifies about attach attempt of endpoint was sucssed.
 *
 * @param[in]   context              Callback's context.
 * @param[in]   endpoint_key_hash    endpoint_id.
 *
 * @return  Error code
 */
typedef kaa_error_t (*on_endpoint_attached_fn)(void *context, const kaa_endpoint_id_p endpoint_key_hash);


/**
 * @brief Notifies about detach attempt of endpoint was sucssed.
 *
 * @param[in]   context              Callback's context.
 *
 * @return  Error code
 */
typedef kaa_error_t (*on_endpoint_detached_fn)(void *context);


/**
 * @brief Notifies about attach attempt was failed.
 *
 * @param[in]   context       Callback's context.
 *
 * @return  Error code
 */
typedef kaa_error_t (*on_endpoint_failed_fn)(void *context);


/**
 * @brief Interface for the endpoint attachment status receiver.
 */
typedef struct {
    void                    *context;     /**< Context to pass to all functions below. */
    on_endpoint_attached_fn  on_attached; /**< Called when the current endpoint attach another endpoint to the user. */
    on_endpoint_detached_fn  on_detached; /**< Called when the current endpoint detach another endpoint from the user. */
    on_endpoint_failed_fn    on_attach_failed;  /**< Called the attach attempt is failed. */
    on_endpoint_failed_fn    on_detach_failed;  /**< Called the detach attempt is failed. */
} kaa_endpoint_status_listener_t;


#ifdef __cplusplus
}    /* extern "C" */
#endif

#endif /* EXT_USER_CALLBACK_H_ */

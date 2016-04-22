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
 * @file kaa_user.h
 * @brief Endpoint-to-user association management for Kaa C SDK.
 *
 * Manages endpoint's association with a user entity in Kaa.
 */

#ifndef KAA_USER_H_
#define KAA_USER_H_

#include <stdbool.h>

#include "kaa_error.h"
#include "kaa_defaults.h"
#include "platform/ext_user_callback.h"

#ifdef __cplusplus
extern "C" {
#endif



/**
 * @brief Kaa user manager structure.
 */
#ifndef KAA_USER_MANAGER_T
# define KAA_USER_MANAGER_T
    typedef struct kaa_user_manager_t       kaa_user_manager_t;
#endif



/**
 * @brief Attaches the endpoint to a user entity. The user verification is carried out by the default verifier.
 *
 * Use this function to request attachment of the endpoint to a user entity using the specified external authentication
 * credentials. Only endpoints associated with the same user entity can exchange events.
 *
 * @param[in]   self                 The user manager instance.
 * @param[in]   user_external_id     Null-terminated string representing external user ID.
 * @param[in]   access_token         Null-terminated string representing external access token.
 *
 * @return      Error code.
 */
#ifdef DEFAULT_USER_VERIFIER_TOKEN
kaa_error_t kaa_user_manager_default_attach_to_user(kaa_user_manager_t *self
                                                  , const char *user_external_id
                                                  , const char *access_token);
#endif



/**
 * @brief Attaches the endpoint to a user entity. The user verification is carried out by the specified verifier.
 *
 * Use this function to request attachment of the endpoint to a user entity using the specified external authentication
 * credentials. Only endpoints associated with the same user entity can exchange events.
 *
 * @param[in]   self                   The user manager instance.
 * @param[in]   user_external_id       Null-terminated string representing external user ID.
 * @param[in]   access_token           Null-terminated string representing external access token.
 * @param[in]   user_verifier_token    Null-terminated string representing user verifier token.
 *
 * @return      Error code.
 */
kaa_error_t kaa_user_manager_attach_to_user(kaa_user_manager_t *self
                                          , const char *user_external_id
                                          , const char *access_token
                                          , const char *user_verifier_token);



/**
 * @brief Checks if current endpoint is attached to user.
 *
 * @param[in]   self    The user manager instance.
 * @retval      true    The endpoint is attached to user
 * @retval      false   Otherwise
 */
bool kaa_user_manager_is_attached_to_user(kaa_user_manager_t *self);


/**
 * @brief Sets callback functions to receive notifications when the endpoint gets attached or detached to (from) user.
 *
 * @param[in]   self         The user manager instance.
 * @param[in]   listeners    A filled in @link kaa_attachment_status_listeners_t @endlink structure.
 *
 * @return      Error code.
 */
kaa_error_t kaa_user_manager_set_attachment_listeners(kaa_user_manager_t *self
                                                    , const kaa_attachment_status_listeners_t *listeners);

/**
 * @brief Attaches external endpoint by its access token.
 *
 * @param[in]   self                     The user manager instance.
 * @param[in]   endpoint_access_token    Null-terminated string representing endpoint access token.
 * @param[in]   listener                 Status listener to set.
 *
 * @return      Error code.
 */
kaa_error_t kaa_user_manager_attach_endpoint(kaa_user_manager_t *self, const char *endpoint_access_token, kaa_endpoint_status_listener_t *listener);


/**
 * @brief Detaches external endpoint by its access token.
 *
 * @param[in]   self                 The user manager instance.
 * @param[in]   endpoint_hash_key    Unique endpoint id.
 * @param[in]   listener
 *
 * @return      Error code.
 */
kaa_error_t kaa_user_manager_detach_endpoint(kaa_user_manager_t *self, const kaa_endpoint_id_p endpoint_hash_key, kaa_endpoint_status_listener_t *listener);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_USER_H_ */

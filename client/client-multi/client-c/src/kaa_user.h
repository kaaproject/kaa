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
 * @file kaa_user.h
 * @brief Endpoint-to-user association management for Kaa C SDK.
 *
 * Manages endpoint's association with a user entity in Kaa.
 */

#ifndef KAA_USER_H_
#define KAA_USER_H_

#include <stdbool.h>
#include "kaa_error.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Kaa user manager structure.
 */
typedef struct kaa_user_manager_t kaa_user_manager_t;



/**
 * @brief Attaches the endpoint to a user entity.
 *
 * Use this function to request attachment of the endpoint to a user entity using the specified external authentication
 * credentials. Only endpoints associated with the same user entity can exchange events.
 *
 * @param[in]   self                Valid pointer to the user manager instance.
 * @param[in]   user_external_id    Null-terminated string representing external user ID.
 * @param[in]   user_access_token   Null-terminated string representing external access token.
 *
 * @return      Error code.
 */
kaa_error_t kaa_user_manager_attach_to_user(kaa_user_manager_t *self, const char *user_external_id, const char *access_token);



/**
 * @brief Structure of user attachment status events listeners.
 *
 * Example functions:
 * @code
 * void on_attached(const char * user_external_id, const char * endpoint_access_token)
 * {
 *     printf("Attached to user %s by endpoint %s\n", user_external_id, endpoint_access_token);
 * }
 * void on_detached(const char * endpoint_access_token)
 * {
 *     printf("Detached from user entity by endpoint %s\n", endpoint_access_token);
 * }
 * void on_attach_status_changed(bool is_attached)
 * {
 *     printf("Attached status is %d\n", is_attached);
 * }
 * @endcode
 */
typedef struct {
    void (*on_attached_callback)(const char *user_external_id, const char *endpoint_access_token);
    void (*on_detached_callback)(const char *endpoint_access_token);
    void (*on_response_callback)(bool is_attached);
} kaa_attachment_status_listeners_t;



/**
 * @brief Sets callback functions to receive notifications when the endpoint gets attached or detached to (from) user.
 *
 * @param[in]   self                Valid pointer to the user manager instance.
 * @param[in]   listeners           A filled in @link kaa_attachment_status_listeners_t @endlink structure.
 *
 * @return      Error code.
 */
kaa_error_t kaa_user_manager_set_attachment_listeners(kaa_user_manager_t *self, kaa_attachment_status_listeners_t listeners);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_USER_H_ */

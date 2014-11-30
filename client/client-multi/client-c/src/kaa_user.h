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

#ifndef KAA_USER_H_
#define KAA_USER_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "kaa_error.h"
#include "gen/kaa_endpoint_gen.h"

typedef struct kaa_user_manager_t kaa_user_manager_t;

/**
 * Attach current endpoint to user.<br>
 * <br>
 * After Kaa is initialized and running use this to attach current endpoint to
 * user instance. Only attached endpoints are allowed to send and received events.<br>
 * <br>
 * \param user_external_id  null-terminated string representing user id
 * \param user_access_token null-terminated string representing user external id
 *
 */
kaa_error_t kaa_user_manager_attach_to_user(kaa_user_manager_t *this, const char *user_external_id, const char *access_token);


typedef struct {
    void (*on_attached_callback)(const char *user_external_id, const char *endpoint_access_token);
    void (*on_detached_callback)(const char *endpoint_access_token);
    void (*on_response_callback)(bool is_attached);
} kaa_attachment_status_listeners_t;


/**
 * Set callback functions to receive notification when current endpoint is being
 * attached or detached to (from) user.<br>
 * <br>
 * callback function example:<br>
 * <pre>
 * void on_attached(const char * user_external_id, const char * endpoint_access_token)
 * {
 *      printf("Attached to user %s by endpoint %s\n", user_external_id, endpoint_access_token);
 * }
 * void on_detached(const char * endpoint_access_token)
 * {
 *      printf("Detached from user entity by endpoint %s\n", endpoint_access_token);
 * }
 * void on_attach_status_changed(bool is_attached)
 * {
 *     printf("Attached status is %d\n", is_attached);
 * }
 * ...
 * kaa_attachment_status_listeners_t callbacks;
 * callbacks.on_attached_callback = &on_attached;
 * callbacks.on_detached_callback = &on_detached;
 * callbacks.on_response_callback = &on_attach_status_changed;
 *
 * kaa_set_user_attached_callback(callbacks);
 * </pre>
 */
kaa_error_t kaa_user_manager_set_attachment_listeners(kaa_user_manager_t *this, kaa_attachment_status_listeners_t listeners);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_USER_H_ */

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
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

#include "kaa_common.h"
#include "kaa_error.h"

#include "gen/kaa_endpoint_gen.h"

typedef struct kaa_user_manager_t kaa_user_manager_t;

kaa_error_t kaa_create_user_manager(kaa_user_manager_t **);
void        kaa_destroy_user_manager(kaa_user_manager_t *);

kaa_error_t kaa_user_attach_to_user(void *ctx, const char *, const char *);
kaa_error_t kaa_set_attachment_listeners(void *ctx, kaa_attachment_status_listeners_t);

kaa_error_t kaa_user_compile_request(void *ctx, kaa_user_sync_request_t** request_p, size_t requestId);
kaa_error_t kaa_user_handle_sync(void *ctx, kaa_user_attach_response_t *, kaa_user_attach_notification_t *, kaa_user_detach_notification_t *);

CLOSE_EXTERN
#endif /* KAA_USER_H_ */

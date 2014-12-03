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

#ifndef KAA_PROFILE_H_
#define KAA_PROFILE_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <stdbool.h>
#include "kaa_error.h"
#include "gen/kaa_profile_gen.h"
#include "gen/kaa_endpoint_gen.h"

typedef kaa_profile_basic_endpoint_profile_test_t kaa_profile_t;
typedef struct kaa_profile_manager_t kaa_profile_manager_t;

/**
 * Updates user profile.<br>
 * After profile is set a request to Operations server will be sent.<br>
 * <br>
 * Provide a valid pointer to user-defined profile structure. kaa_profile_t is
 * an alias of a given profile structure name.<br>
 * <br>
 * Use this to set profile before kaa_init() is called to provide default
 * profile value in order to perform successful registration in Operations server.
 */
kaa_error_t                     kaa_profile_update_profile(kaa_profile_manager_t *kaa_context, kaa_profile_t *profile);
kaa_error_t                     kaa_profile_need_profile_resync(kaa_profile_manager_t *kaa_context, bool *result);
kaa_error_t                     kaa_profile_compile_request(kaa_profile_manager_t *kaa_context, kaa_profile_sync_request_t **result);
kaa_error_t                     kaa_profile_handle_sync(kaa_profile_manager_t *kaa_context, kaa_profile_sync_response_t *profile);

#ifdef __cplusplus
} // extern "C"
#endif

#endif /* KAA_PROFILE_H_ */

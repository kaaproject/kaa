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

#ifndef KAA_STATUS_H_
#define KAA_STATUS_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "kaa_error.h"
#include "kaa_common.h"

typedef struct kaa_status_t kaa_status_t;

kaa_error_t kaa_is_endpoint_registered(kaa_status_t *self, bool *result);
kaa_error_t kaa_set_endpoint_registered(kaa_status_t *self, bool is_registered);

kaa_error_t kaa_is_endpoint_attached_to_user(kaa_status_t *self, bool *result);
kaa_error_t kaa_set_endpoint_attached_to_user(kaa_status_t *self, bool is_attached);

kaa_error_t kaa_status_get_endpoint_access_token(kaa_status_t *self, const char **result);
kaa_error_t kaa_status_set_endpoint_access_token(kaa_status_t *self, const char *token);

kaa_error_t kaa_status_get_endpoint_public_key_hash(kaa_status_t *self, kaa_digest_p *result);
kaa_error_t kaa_status_set_endpoint_public_key_hash(kaa_status_t *self, const kaa_digest hash);

kaa_error_t kaa_status_get_profile_hash(kaa_status_t *self, kaa_digest_p *result);
kaa_error_t kaa_status_set_profile_hash(kaa_status_t *self, const kaa_digest hash);

kaa_error_t kaa_status_get_event_sequence_number(kaa_status_t *self, uint32_t *result);
kaa_error_t kaa_status_set_event_sequence_number(kaa_status_t *self, uint32_t seq_n);

kaa_error_t kaa_status_get_log_bucket_id(kaa_status_t *self, uint16_t *result);
kaa_error_t kaa_status_set_log_bucket_id(kaa_status_t *self, uint16_t id);

kaa_error_t kaa_status_save(kaa_status_t *self);

#ifdef __cplusplus
} // extern "C"
#endif
#endif /* KAA_STATUS_H_ */

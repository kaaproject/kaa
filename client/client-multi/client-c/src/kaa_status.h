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
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

#include "kaa_error.h"
#include "kaa_common.h"

typedef struct kaa_status_t kaa_status_t;

kaa_error_t kaa_create_status(kaa_status_t **);
void        kaa_destroy_status(kaa_status_t *);

KAA_BOOL    kaa_is_endpoint_registered(kaa_status_t *);
kaa_error_t kaa_set_endpoint_registered(kaa_status_t *, KAA_BOOL);

KAA_BOOL    kaa_is_endpoint_attached_to_user(kaa_status_t *);
kaa_error_t kaa_set_endpoint_attached_to_user(kaa_status_t *, KAA_BOOL);

char *      kaa_status_get_endpoint_access_token(kaa_status_t *);
kaa_error_t kaa_status_set_endpoint_access_token(kaa_status_t *, const char *);

kaa_digest* kaa_status_get_endpoint_public_key_hash(kaa_status_t *);
kaa_error_t kaa_status_set_endpoint_public_key_hash(kaa_status_t *, const kaa_digest);

kaa_digest* kaa_status_get_profile_hash(kaa_status_t *);
kaa_error_t kaa_status_set_profile_hash(kaa_status_t *, const kaa_digest);

KAA_INT32T  kaa_status_get_event_sequence_number(kaa_status_t*);
kaa_error_t kaa_status_set_event_sequence_number(kaa_status_t*, KAA_INT32T);

kaa_error_t kaa_status_save(kaa_status_t *);

CLOSE_EXTERN
#endif /* KAA_STATUS_H_ */

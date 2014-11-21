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

#ifndef KAA_EVENT_H_
#define KAA_EVENT_H_

#ifdef __cplusplus
extern "C" {
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

#ifndef KAA_DISABLE_FEATURE_EVENTS
#include "kaa_common.h"
#include "kaa_error.h"
#include "gen/kaa_endpoint_gen.h"

typedef struct kaa_event_manager_t kaa_event_manager_t;

kaa_error_t kaa_create_event_manager(kaa_event_manager_t **);
void        kaa_destroy_event_manager(kaa_event_manager_t *);

kaa_error_t kaa_event_compile_request(void *ctx, kaa_event_sync_request_t** request_p, size_t requestId);
kaa_error_t kaa_event_handle_sync(void *ctx, size_t request_id, kaa_list_t *events);

kaa_error_t kaa_event_create_transaction(void *context, kaa_trx_id *trx_id);
kaa_error_t kaa_event_finish_transaction(void *context, kaa_trx_id trx_id);
kaa_error_t kaa_event_remove_transaction(void *context, kaa_trx_id trx_id);
kaa_error_t kaa_add_event_to_transaction(void *context, kaa_trx_id trx_id, const char * fqn, size_t fqn_length, const char * event_data, size_t event_data_size, const char * target, size_t target_size);

kaa_error_t kaa_add_event(void *context, const char * fqn, size_t fqn_length, const char * event_data, size_t event_data_size, const char * target, size_t target_size);
kaa_error_t kaa_add_on_event_callback(kaa_event_manager_t *event_manager, const char *fqn, size_t fqn_length, event_callback_t callback);

const char *kaa_find_class_family_name(const char *fqn);

#endif

CLOSE_EXTERN
#endif /* KAA_EVENT_H_ */

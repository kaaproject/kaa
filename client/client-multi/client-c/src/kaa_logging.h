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

#ifndef KAA_LOGGING_H_
#define KAA_LOGGING_H_

#ifndef KAA_DISABLE_FEATURE_LOGGING
#include "gen/kaa_endpoint_gen.h"
#include "gen/kaa_logging_gen.h"
#include <stddef.h>
#include "kaa_uuid.h"

#ifdef __cplusplus
extern "C" {
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

typedef kaa_test_log_record_t               kaa_user_log_record_t;
typedef struct kaa_log_collector            kaa_log_collector_t;

void                destroy_log_record(void *record_p);

typedef struct kaa_log_storage_t {
    void            (* add_log_record)  (kaa_log_entry_t * record);
    kaa_list_t *    (* get_records)     (kaa_uuid_t uuid, size_t max_size);
    void            (* upload_succeeded)(kaa_uuid_t uuid);
    void            (* upload_failed)   (kaa_uuid_t uuid);
    void            (* shrink_to_size)  (size_t size);
    void            (* destroy)         (void);
} kaa_log_storage_t;

typedef struct kaa_storage_status_t {
    size_t          (* get_total_size)();
    size_t          (* get_records_count)();
} kaa_storage_status_t;

typedef struct kaa_log_upload_properties_t {
    uint32_t    max_log_block_size;
    uint32_t    max_log_upload_threshold;
    uint32_t    max_log_storage_volume;
} kaa_log_upload_properties_t;

typedef enum kaa_log_upload_decision_t {
    NOOP        = 0,
    UPLOAD      = 1,
    CLEANUP     = 2
} kaa_log_upload_decision_t;

typedef kaa_log_upload_decision_t (* log_upload_decision_fn)(kaa_storage_status_t *);



kaa_error_t                 kaa_create_log_collector(kaa_log_collector_t **);
void                        kaa_destroy_log_collector(kaa_log_collector_t *);

kaa_error_t                 kaa_init_log_collector(
                                                    kaa_log_collector_t *
                                                  , kaa_log_storage_t *
                                                  , kaa_log_upload_properties_t *
                                                  , kaa_storage_status_t *
                                                  , log_upload_decision_fn
                                                  );

void                        kaa_add_log_record(void *ctx, kaa_user_log_record_t *entry);

kaa_log_sync_request_t *    kaa_logging_compile_request(void *ctx);
void                        kaa_logging_handle_sync(void *ctx, kaa_log_sync_response_t *response);
CLOSE_EXTERN

#endif

#endif /* KAA_LOGGING_H_ */

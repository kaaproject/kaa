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

#ifndef KAA_BOOTSTRAP_H_
#define KAA_BOOTSTRAP_H_

#ifdef __cplusplus
extern "C" {
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

#include "kaa_error.h"
#include "kaa_common.h"

typedef struct kaa_bootstrap_manager_t kaa_bootstrap_manager_t;

typedef struct kaa_ops_t {
    kaa_channel_type_t  channel_type;
    uint32_t            priority;
    char                data[7]; //FIXME
} kaa_ops_t;

typedef struct kaa_ops_ip_t {
    kaa_channel_type_t  channel_type;
    uint32_t            priority;
    uint16_t            port;
    char               *hostname;
    uint8_t             hostname_length;
    char               *public_key;
    uint16_t            public_key_length;
} kaa_ops_ip_t;

kaa_error_t kaa_create_bootstrap_manager(kaa_bootstrap_manager_t **);
void kaa_destroy_bootstrap_manager(kaa_bootstrap_manager_t *);

kaa_error_t kaa_add_operation_server(kaa_bootstrap_manager_t *bm, kaa_ops_t* s);

kaa_ops_t* kaa_get_current_operation_server(kaa_bootstrap_manager_t *bm, kaa_channel_type_t channel_type);
kaa_ops_t* kaa_get_next_operation_server(kaa_bootstrap_manager_t *bm, kaa_channel_type_t channel_type);

CLOSE_EXTERN
#endif /* KAA_BOOTSTRAP_H_ */

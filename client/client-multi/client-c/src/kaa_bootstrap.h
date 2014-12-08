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

#include "kaa_error.h"
#include "kaa_common.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct kaa_bootstrap_manager_t kaa_bootstrap_manager_t;

typedef struct kaa_ops_t {
    kaa_channel_type_t  channel_type;
    uint32_t            priority;
    char                data[7]; //FIXME
} kaa_ops_t;

// TODO: This structure is specific to the KaaTCP implementation of the transport protocol layer, and must be moved to
// the corresponding source files. Bootstrap manager must have no knowledge of the specific to the concrete protocol
// implementation destination structures.
typedef struct kaa_ops_ip_t {
    kaa_channel_type_t  channel_type;
    uint32_t            priority;
    uint16_t            port;
    char               *hostname;
    uint8_t             hostname_length;
    char               *public_key;
    uint16_t            public_key_length;
} kaa_ops_ip_t;


kaa_error_t kaa_bootstrap_manager_add_operations_server(kaa_bootstrap_manager_t *self, kaa_ops_t *server);

kaa_ops_t* kaa_bootstrap_manager_get_current_operations_server(kaa_bootstrap_manager_t *self, kaa_channel_type_t channel_type);
kaa_ops_t* kaa_bootstrap_manager_get_next_operations_server(kaa_bootstrap_manager_t *self, kaa_channel_type_t channel_type);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_BOOTSTRAP_H_ */

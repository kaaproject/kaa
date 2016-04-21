/*
 * Copyright 2014-2016 CyberVision, Inc.
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
 * @file
 * @brief Management of the Operations servers connection parameters.
 *
 * Manages connection parameters to Operations servers that are received from Bootstrap servers.
 */

#ifndef KAA_BOOTSTRAP_MANAGER_H_
#define KAA_BOOTSTRAP_MANAGER_H_

#include <stdbool.h>
#include "kaa_error.h"
#include "kaa_common.h"
#include "platform/ext_kaa_failover_strategy.h"

#ifdef __cplusplus
extern "C" {
#endif


#ifndef KAA_BOOTSTRAP_MANAGER_T
# define KAA_BOOTSTRAP_MANAGER_T
typedef struct kaa_bootstrap_manager_t  kaa_bootstrap_manager_t;
#endif



typedef enum {
    KAA_SERVER_BOOTSTRAP  = 0,
    KAA_SERVER_OPERATIONS = 1
} kaa_server_type_t;



/**
 * @brief Notifies some error has occurred while using an access point.
 *
 * @param[in]   self           Bootstrap manager.
 * @param[in]   protocol_id    Transport protocol id that failed access point belongs to.
 * @param[in]   type           Server type that failed access point belongs to.
 * @param[in]   reason_code    The reason of failure.
 * @return                     Error code.
 *
 * @see kaa_transport_protocol_id_t
 */
kaa_error_t kaa_bootstrap_manager_on_access_point_failed(kaa_bootstrap_manager_t *self,
                                                         kaa_transport_protocol_id_t *protocol_id,
                                                         kaa_server_type_t type,
                                                         kaa_failover_reason reason_code);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_BOOTSTRAP_MANAGER_H_ */

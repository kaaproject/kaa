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

/**
 * @file kaa_bootstrap.h
 * @brief Management of the Operations servers connection parameters.
 *
 * Manages connection parameters to Operations servers that are received from Bootstrap servers.
 */

#ifndef KAA_BOOTSTRAP_H_
#define KAA_BOOTSTRAP_H_

#include "kaa_error.h"
#include "kaa_common.h"
#include "platform/ext_transport_channel.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct kaa_bootstrap_manager_t kaa_bootstrap_manager_t;



/**
 * @brief Connection parameters used by transport channels to establish
 * connection to Operations servers.
 */
typedef struct {
    uint32_t    id;
    uint16_t    connection_data_len;
    uint8_t     *connection_data;
} kaa_access_point_t;



/**
 * @brief Retrieves connection parameters currently used by specified
 * transport protocol.
 *
 * @param[in]   self             Bootstrap manager.
 * @param[in]   protocol_info    Transport protocol information.
 * @return                       Connection parameters or NULL.
 *
 * @see kaa_access_point_t
 * @see kaa_transport_protocol_info_t
 */
kaa_access_point_t *kaa_bootstrap_manager_get_current_access_point(kaa_bootstrap_manager_t *self
                                                                 , kaa_transport_protocol_info_t *protocol_info);

/**
 * @brief Retrieves next connection parameters used by specified transport protocol.
 *
 * @param[in]   self             Bootstrap manager.
 * @param[in]   protocol_info    Transport protocol information.
 * @return                       Connection parameters or NULL.
 *
 * @see kaa_access_point_t
 * @see kaa_transport_protocol_info_t
 */
kaa_access_point_t *kaa_bootstrap_manager_get_next_access_point(kaa_bootstrap_manager_t *self
                                                              , kaa_transport_protocol_info_t *protocol_info);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_BOOTSTRAP_H_ */

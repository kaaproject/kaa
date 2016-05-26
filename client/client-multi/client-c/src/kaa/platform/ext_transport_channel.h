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

#ifndef EXT_TRANSPORT_CHANNEL_H_
#define EXT_TRANSPORT_CHANNEL_H_

#include "kaa_common.h"
#include "kaa_platform_protocol.h"

#ifdef __cplusplus
extern "C" {
#endif



/**
 * @brief Used to initialize transport channel implementation with Kaa specific
 * transport context.
 */
typedef struct {
    kaa_context_t              *kaa_context;
} kaa_transport_context_t;



/**
 * @brief Initializes the transport channel implementation.
 *
 * @param[in]   channel_context      Channel context.
 * @param[in]   transport_context    Kaa specific transport context.
 * @return                           Error code.
 *
 */
typedef kaa_error_t (*kaa_init_channel_fn)(void *channel_context
                                         , kaa_transport_context_t *transport_context);



/**
 * @brief Sets transport connection data.
 *
 * @note Copy connection data for the local usage.
 *
 * @param[in]   channel_context    Channel context.
 * @param[in]   access_point       Connection data used to establish connection
 *                                 to Operations server.
 * @return                         Error code.
 *
 */
typedef kaa_error_t (*kaa_set_access_point_fn)(void *channel_context
                                             , kaa_access_point_t *access_point);



/**
 * @brief Retrieves a transport protocol id supported by a transport channel implementation.
 *
 * @param[in]       context          Channel context.
 * @param[out]      protocol_info    Transport protocol id instance to be filled in.
 * @return                           Error code.
 *
 * @see kaa_transport_protocol_id_t
 */
typedef kaa_error_t (*kaa_get_protocol_id_fn)(void *context
                                            , kaa_transport_protocol_id_t *protocol_info);



/**
 * @brief Retrieves the list of the supported services.
 *
 * @param[in]       context               Channel context.
 * @param[out]      supported_services    List of the supported services.
 * @param[out]      service_count         Number of the supported services.
 * @return                                Error code.
 *
 * @see kaa_extension_id
 */
typedef kaa_error_t (*kaa_get_supported_services_fn)(void *context,
        const kaa_extension_id **supported_services,
        size_t *service_count);



/**
 * @brief Kaa sync request handler function for specific services.
 *
 * @param[in]   context          Channel context.
 * @param[in]   services         List of services.
 * @param[in]   service_count    Number of services.
 * @return                       Error code.
 *
 * @see kaa_extension_id
 */
typedef kaa_error_t (*kaa_sync_handler_fn)(void *context
                                         , const kaa_extension_id services[]
                                         , size_t service_count);



/**
 * @brief Releases channel context.
 *
 * @param[in]   context    Channel context.
 * @return                 Error code.
 *
 */
typedef kaa_error_t (*kaa_tcp_channel_destroy_fn)(void *context);



/**
 * @brief Interface for a client transport channel implementation.
 *
 * Every implementation should provide API to convert itself to this interface.
 */
typedef struct {
    void                              *context;
    kaa_tcp_channel_destroy_fn        destroy; /**< May be NULL */
    kaa_sync_handler_fn               sync_handler;
    kaa_init_channel_fn               init;
    kaa_set_access_point_fn           set_access_point;
    kaa_get_protocol_id_fn            get_protocol_id;
    kaa_get_supported_services_fn     get_supported_services;
} kaa_transport_channel_interface_t;



#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* EXT_TRANSPORT_CHANNEL_H_ */

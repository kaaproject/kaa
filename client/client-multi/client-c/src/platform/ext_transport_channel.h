/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#ifndef SRC_PLATFORM_EXT_TRANSPORT_CHANNEL_H_
#define SRC_PLATFORM_EXT_TRANSPORT_CHANNEL_H_

# ifdef __cplusplus
extern "C" {
# endif

/**
 * Uses to uniquely identify client transport channel implementations.
 */
typedef struct {
    uint32_t id;
    uint16_t version;
} kaa_transport_protocol_info_t;

/**
 * @brief Retrieves a transport protocol info.
 *
 * @param[in]       context          Channel context.
 * @param[in,out]   protocol_info    Transport protocol info instance to be filled in.
 * @return                           Error code.
 *
 * @see kaa_transport_protocol_info_t
 */
typedef kaa_error_t (*kaa_get_protocol_info_fn)(void *context
                                              , kaa_transport_protocol_info_t *protocol_info);

/**
 * @brief Retrieves the list of the supported services.
 *
 * @param[in]       context               Channel context.
 * @param[in,out]   supported_services    List of the supported services.
 * @param[in,out]   service_count         Number of the supported services.
 * @return                                Error code.
 *
 * @see kaa_service_t
 */
typedef kaa_error_t (*kaa_get_supported_services_fn)(void *context
                                                   , kaa_service_t **supported_services
                                                   , size_t *service_count);

/**
 * @brief Kaa sync request handler function for specific services.
 *
 * @param[in]   context          Channel context.
 * @param[in]   services         List of services.
 * @param[in]   service_count    Number of services.
 * @return                       Error code.
 *
 * @see kaa_service_t
 */
typedef kaa_error_t (*kaa_sync_handler_fn)(void *context
                                         , const kaa_service_t services[]
                                         , size_t service_count);

/**
 * @brief Releases channel context.
 *
 * @param[in]   context    Channel context.
 * @return                 Error code.
 *
 */
typedef kaa_error_t (*kaa_release_channel_context_fn)(void *context);

/**
 * @brief Interface for a client transport channel implementation.
 *
 * Every implementation should provide API to convert itself to this interface.
 */
typedef struct {
    void                              *context;
    kaa_get_protocol_info_fn          get_protocol_info;
    kaa_get_supported_services_fn     get_supported_services;
    kaa_sync_handler_fn               sync_handler;
    kaa_release_channel_context_fn    release_context; /**< May be NULL */
} kaa_transport_channel_interface_t;

# ifdef __cplusplus
}      /* extern "C" */
# endif

#endif /* SRC_PLATFORM_EXT_TRANSPORT_CHANNEL_H_ */

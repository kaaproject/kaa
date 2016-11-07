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
 * @file kaa_channel_manager.h
 * @brief User defined channels manager for Kaa C SDK.
 *
 * Manages client transport channels.
 *
 * Notifies about new access points and indicates to user defined protocol
 * implementations that Kaa services have data to sync with Operations server.
 */

#ifndef KAA_CHANNEL_MANAGER_H_
#define KAA_CHANNEL_MANAGER_H_

#include "kaa_common.h"
#include "platform/ext_transport_channel.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Kaa channel manager structure.
 */
#ifndef KAA_CHANNEL_MANAGER_T
# define KAA_CHANNEL_MANAGER_T
typedef struct kaa_channel_manager_t    kaa_channel_manager_t;
#endif



/**
 * Calculates the unique id for the transport channel implementations.
 *
 * @param[in]       channel       Interface of the transport channel implementations.
 * @param[in,out]   channel_id    Pointer to calculated channel id.
 *
 * @return                        Error code.
 */
kaa_error_t kaa_transport_channel_id_calculate(kaa_transport_channel_interface_t *channel,
        uint32_t *channel_id);

/**
 * @brief Adds user-defined transport channel implementation as a sync request
 * handler for the given list of services.
 *
 * Kaa library will call the channel's callback when there is data to be sent to
 * Operations server for one of the specified services.
 *
 * @b NOTE: It is possible to register more than one channel for the same service.
 * In such event Kaa library will use the last registered one.
 *
 * @param[in]       self          Channel manager.
 * @param[in]       channel       Transport channel implementations.
 * @param[in,out]   channel_id    Pointer to calculate channel id.
 *                                May be NULL if id isn't needed.
 *
 * @return                        Error code.
 */
kaa_error_t kaa_channel_manager_add_transport_channel(kaa_channel_manager_t *self,
        kaa_transport_channel_interface_t *channel,
        uint32_t *channel_id);

/**
 * Gets transport channel associated with the service.
 *
 * @param[in] self          Channel manager.
 * @param[in] service_type  Type of service with associated channel.
 * @return                  Channel, if found. NULL if not found.
 */
kaa_transport_channel_interface_t *kaa_channel_manager_get_transport_channel(kaa_channel_manager_t *self,
        kaa_extension_id service_type);


/**
 * @brief Removes user-defined transport channel implementation from
 * the currently registered list.
 *
 * @b NOTE: The channel manager is responsible to release all resources related
 * to this channel.
 *
 * @param[in]   self          Channel manager.
 * @param[in]   channel_id    Channel id.
 *
 * @return                    Error code.
 */
kaa_error_t kaa_channel_manager_remove_transport_channel(kaa_channel_manager_t *self,
        uint32_t channel_id);



/**
 * Specifies authorization failure reason.
 *
 */
typedef enum {
    KAA_AUTH_STATUS_UNKNOWN, /**< Authorization failed for unknown reason. */
    KAA_AUTH_STATUS_BAD_CREDENTIALS, /**< Authorization failed because credentials are invalid. */
    KAA_AUTH_STATUS_VERIFICATION_FAILED, /**< Authorization failed because of verification failure. */
} kaa_auth_failure_reason;



/**
 * Processes authorization failure.
 *
 * @param[in]  reason           Authorization failure reason.
 * @param[in]  context          Handler context.
 */
typedef void (*kaa_auth_failure_fn)(kaa_auth_failure_reason reason, void *context);



/**
 * Specify authorization failure handler
 *
 * @param[in]   self            Channel manager.
 * @param[in]   handler         Authorization failure handler.
 * @param[in]   context         Handler context.
 */
void kaa_channel_manager_set_auth_failure_handler(kaa_channel_manager_t *self,
        kaa_auth_failure_fn handler, void *context);


/**
 * Processes authorization failure if valid handler exists.
 *
 * @param[in]   self            Channel manager.
 * @param[in]   reason          Authorization failure reason.
 */
void kaa_channel_manager_process_auth_failure(kaa_channel_manager_t *self,
        kaa_auth_failure_reason reason);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_CHANNEL_MANAGER_H_ */

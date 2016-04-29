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
 * @file kaa_platform_protocol.h
 * @brief Kaa binary platform level protocol implementation
 * (<a href="https://docs.kaaproject.org/display/KAA/Binary+platform+protocol">org.kaaproject.protocol.platform.binary</a>).
 *
 * Supplies API for serializing client and server sync messages.
 */

#ifndef KAA_PLATFORM_PROTOCOL_H_
#define KAA_PLATFORM_PROTOCOL_H_

#include <stdint.h>
#include <stddef.h>

#include "kaa_error.h"
#include "kaa_context.h"
#include "kaa_common.h"

#ifdef __cplusplus
extern "C" {
#endif

#ifndef KAA_PLATFORM_PRTOCOL_T
#define KAA_PLATFORM_PRTOCOL_T
/**
 * Kaa platform protocol state structure
 */
typedef struct kaa_platform_protocol_t kaa_platform_protocol_t;
#endif

/**
 * Serialize info structure
 */
typedef struct {
    kaa_extension_id *services;     /**< Non-empty list of services to include into the sync message */
    size_t services_count;          /**< Number of elements in @c services */
} kaa_serialize_info_t;

/**
 * @brief Constructs a sync request for the specified list of services
 * based on the current state of Kaa context and serializes it into
 * the buffer.
 *
 * If buffer size is not enough, @c KAA_ERR_BUFFER_IS_NOT_ENOUGH is
 * returned. You should reallocate buffer and repeat the call.
 *
 * @param[in]  self           Pointer to a @ref kaa_platform_protocol_t instance.
 * @param[in]  services       A list of services to serialize.
 * @param[in]  services_count A number of @c services.
 * @param[out] buffer         The buffer with serialized data.
 * @param[out] buffer_size    The buffer's actual size.
 *
 * @return Error code.
 *
 * @retval KAA_ERR_BUFFER_IS_NOT_ENOUGH The buffer size is not
 * enough. In that case, @p buffer_size is updated to the required
 * buffer size.
 */
kaa_error_t kaa_platform_protocol_serialize_client_sync(kaa_platform_protocol_t *self,
        const kaa_extension_id *services, size_t services_count,
        uint8_t *buffer, size_t *buffer_size);

/**
 * Allocates the buffer of the needed size and calls
 * kaa_platform_protocol_serialize_client_sync().
 *
 * @note The caller must deallocate the buffer.
 *
 * @deprecated You should maintain buffer yourself and call
 * kaa_platform_protocol_serialize_client_sync() directly. The
 * function is defined as a transition to the new approach.
 */
kaa_error_t kaa_platform_protocol_alloc_serialize_client_sync(kaa_platform_protocol_t *self,
        const kaa_extension_id *services, size_t services_count,
        uint8_t **buffer, size_t *buffer_size);

/**
 * @brief Processes downstream data received from Operations server.
 *
 * @param[in] self        Pointer to a @ref kaa_platform_protocol_t instance.
 * @param[in] buffer      Pointer to a data buffer for processing received from server.
 * @param[in] buffer_size Size of @c buffer.
 *
 * @return Error code.
 */
kaa_error_t kaa_platform_protocol_process_server_sync(kaa_platform_protocol_t *self,
        const uint8_t *buffer, size_t buffer_size);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_PLATFORM_PROTOCOL_H_ */

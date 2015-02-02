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
 * @file kaa_platform_protocol.h
 * @brief Kaa binary platform level protocol implementation
 * (<a href="https://docs.kaaproject.org/display/KAA/Binary+platform+protocol">org.kaaproject.protocol.platform.binary</a>).
 *
 * Supplies API for serializing client sync messages to Operations server and processing server sync messages.
 */

#ifndef KAA_PLATFORM_PROTOCOL_H_
#define KAA_PLATFORM_PROTOCOL_H_

#include "kaa_error.h"
#include "kaa_context.h"
#include "kaa_common.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Kaa platform protocol state structure
 */
#ifndef KAA_PLATFORM_PRTOCOL_T
# define KAA_PLATFORM_PRTOCOL_T
    typedef struct kaa_platform_protocol_t  kaa_platform_protocol_t;
#endif

/**
 * Buffer allocation callback
 */
typedef char* (*kaa_buffer_alloc_fn)(void *context, size_t buffer_size);

/**
 * Serialize info structure
 */
typedef struct {
    kaa_service_t *services;        /**< Non-empty list of services to include into the sync message */
    size_t services_count;          /**< Number of elements in @c services */
    kaa_buffer_alloc_fn allocator;  /**< Pointer to a buffer memory allocation function */
    void *allocator_context;        /**< Context to be passed to the @c allocator callback as @c context parameter */
} kaa_serialize_info_t;

/**
 * @brief Constructs a sync request for the specified list of services based on the current state of Kaa context and
 * serializes it into the buffer returned by the allocator function.
 *
 * The required buffer size only becomes known after compiling a non-serialized sync request structure. Thus, the
 * function expects the memory allocation callback, @c allocator, to return a buffer of the requested size. It is
 * perfectly acceptable to return a pointer to a previously allocated buffer (even on the stack) if its size is sufficient.
 *
 * @param[in]  self              Pointer to a @link kaa_platform_protocol_t @endlink instance.
 * @param[in]  info              Pointer to a @link kaa_serialize_info_t @endlink instance.
 * @param[out] buffer            The buffer with serialized data.
 * @param[out] buffer_size       The buffer's actual size.
 *
 * @return Error code.
 */
kaa_error_t kaa_platform_protocol_serialize_client_sync(kaa_platform_protocol_t *self
                                                      , const kaa_serialize_info_t *info
                                                      , char **buffer
                                                      , size_t *buffer_size);

/**
 * @brief Processes downstream data received from Operations server.
 *
 * @param[in] self              Pointer to a @link kaa_platform_protocol_t @endlink instance.
 * @param[in] buffer            Pointer to a data buffer for processing received from Operations server.
 * @param[in] buffer_size       Size of @c buffer.
 *
 * @return Error code.
 */
kaa_error_t kaa_platform_protocol_process_server_sync(kaa_platform_protocol_t *self
                                                    , const char *buffer
                                                    , size_t buffer_size);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_PLATFORM_PROTOCOL_H_ */

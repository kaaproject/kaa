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

#ifndef KAA_PLATFORM_PROTOCOL_H_
#define KAA_PLATFORM_PROTOCOL_H_

#include "kaa_error.h"
#include "kaa_context.h"
#include "kaa_common.h"
#include "gen/kaa_endpoint_gen.h"

#ifdef __cplusplus
extern "C" {
#endif


/**
 * Create a Sync Request.<br>
 * <br>
 * Use this to create a valid sync request.<br>
 * Kaa library will allocate memory for *request itself.
 *
 * \return size of buffer which is needed to serialize the request
 * Example:<br>
 * <pre>
 * kaa_service_t services[1] = { KAA_SERVICE_EVENT };
 * kaa_sync_request_t *request = NULL;
 * size_t buffer_size = 0;
 * kaa_error_t error_code = kaa_compile_request(&request, &buffer_size, 1, services);
 * </pre>
 */
kaa_error_t kaa_compile_request(kaa_context_t *kaa_context, kaa_sync_request_t **request, size_t *result_size, size_t service_count, const kaa_service_t services[]);

/**
 * Serialize Sync Request.<br>
 * <br>
 * Use this to serialize a valid sync request created using @link kaa_compile_request(...) @endlink.<br>
 * Serialized request is place to a given buffer. Buffer size must be of size
 * NOT LESS THAN the value returned by @link kaa_compile_request(...) @endlink <br>
 * <br>
 * Example:<br>
 * <pre>
 * kaa_service_t services[1] = { KAA_SERVICE_EVENT };
 * kaa_sync_request_t *request = NULL;
 * size_t buffer_size = 0;
 * kaa_error_t error_code = kaa_compile_request(&request, &buffer_size, 1, services);
 *
 * char *buffer = malloc(buffer_size * sizeof(char));
 * kaa_serialize_request(request, buffer, buffer_size);
 *
 * </pre>
 */
kaa_error_t kaa_serialize_request(kaa_sync_request_t *request, char *buffer, size_t request_size);

/**
 * Process data received from Operations server.
 */
kaa_error_t kaa_response_received(kaa_context_t *kaa_context, const char *buffer, size_t buffer_size);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_PLATFORM_PROTOCOL_H_ */

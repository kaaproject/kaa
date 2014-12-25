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

#ifndef KAA_EXTERNAL_H_
#define KAA_EXTERNAL_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <stddef.h>
#include <stdbool.h>

/**
 * This header contains external functions which are needed by Kaa library.
 */

/**
 * @brief Called on Kaa library startup to restore persisted state.
 *
 * Fetch persisted state to buffer and provide a valid size of it in buffer_size.
 * If *buffer == NULL or *buffer_size == 0 Kaa library will use default values.
 * Set *needs_deallocation = true if buffer should be free'd, false otherwise.
 *
 * @param[out]  buffer              Pointer to buffer which should be filled with Kaa status data.
 * @param[out]  buffer_size         Pointer to buffer's size.
 * @param[out]  needs_deallocation  Indicates if the Kaa library should deallocate buffer by itself.
 *
 */
void    kaa_read_status_ext(char **buffer, size_t *buffer_size, bool *needs_deallocation);

/**
 * @brief Called when Kaa library is ready to persist its state.
 *
 * @param[in]   buffer          Valid pointer to buffer which contains the current Kaa status data.
 * @param[in]   buffer_size     The buffer's size.
 *
 */
void    kaa_store_status_ext(const char *buffer, size_t buffer_size);

/**
 * @brief Called to get the endpoint public key.
 *
 * @param[out]  buffer              Pointer to buffer which should be filled with public key.
 * @param[out]  buffer_size         Pointer to buffer's size.
 * @param[out]  needs_deallocation  Indicates if the Kaa library should deallocate buffer by itself.
 *
 */
void    kaa_get_endpoint_public_key(char **buffer, size_t *buffer_size, bool *needs_deallocation);

#ifdef __cplusplus
} // extern "C"
#endif
#endif /* KAA_EXTERNAL_H_ */

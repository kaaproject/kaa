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
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

/**
 * This header contains external functions which are needed by Kaa library.
 */

/**
 * Called on Kaa library startup to restore persisted state.<br>
 * <br>
 * Fetch persisted state to buffer and provide a valid size of it in buffer_size.<br>
 * <br>
 *
 * If *buffer == NULL or *buffer_size == 0 Kaa library will use default values.<br>
 * <br>
 * Set *needs_deallocation = 1 if buffer should be free'd, 0 otherwise.
 */
void    kaa_read_status_ext(char **buffer, size_t *buffer_size, int *needs_deallocation);

/**
 * Called when Kaa library is ready to persist its state.<br>
 * <br>
 */
void    kaa_store_status_ext(const char *buffer, size_t buffer_size);

/**
 * Called to get endpoint public key.<br>
 */
void    kaa_get_endpoint_public_key(char **buffer, size_t *buffer_size);

CLOSE_EXTERN
#endif /* KAA_EXTERNAL_H_ */

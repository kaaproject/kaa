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
 * @file ext_key_utils.h
 * @brief External interface for client keys manipulations.
 */

#ifndef EXT_KEY_UTILS_H_
#define EXT_KEY_UTILS_H_

#ifdef __cplusplus
extern "C" {
#endif


/**
 * @brief Called to get the endpoint public key.
 *
 * @param[out]  buffer              Pointer to buffer which should be filled with public key.
 * @param[out]  buffer_size         Pointer to buffer's size.
 * @param[out]  needs_deallocation  Indicates if the Kaa library should deallocate buffer by itself.
 *
 */
void ext_get_endpoint_public_key(char **buffer, size_t *buffer_size, bool *needs_deallocation);

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* EXT_KEY_UTILS_H_ */

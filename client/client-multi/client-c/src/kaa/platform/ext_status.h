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
 * @file ext_status.h
 * @brief External interface for storing/loading Kaa status.
 */

#ifndef EXT_STATUS_H_
#define EXT_STATUS_H_

#ifdef __cplusplus
extern "C" {
#endif


/**
 * @brief Called on Kaa startup to restore the persisted state.
 *
 * Fetch persisted state to buffer and provide a valid size of it in buffer_size.
 * If *buffer == NULL or *buffer_size == 0 Kaa library will use default values.
 * Set *needs_deallocation = true if buffer should be deallocated by Kaa, false otherwise.
 *
 * @param[out]  buffer              Pointer to buffer which should be filled with Kaa status data.
 * @param[out]  buffer_size         Pointer to buffer's size.
 * @param[out]  needs_deallocation  Indicates if the Kaa library should deallocate buffer by itself.
 *
 */
void ext_status_read(char **buffer, size_t *buffer_size, bool *needs_deallocation);


/**
 * @brief Called when Kaa is ready to persist its state.
 *
 * @param[in]   buffer          Valid pointer to buffer which contains the current Kaa status data.
 * @param[in]   buffer_size     The buffer's size.
 *
 */
void ext_status_store(const char *buffer, size_t buffer_size);

/**
 * @brief Deletes a status storage.
 */
void ext_status_delete(void);

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* SRC_PLATFORM_EXT_STATUS_H_ */

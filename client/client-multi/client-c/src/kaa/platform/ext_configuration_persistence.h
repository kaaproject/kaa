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
 * @file ext_configuration_persistence.h
 * @brief External interface for storing/loading the configuration data used by Kaa Configuration subsystem.
 */

#ifndef EXT_CONFIGURATION_PERSISTENCE_H_
#define EXT_CONFIGURATION_PERSISTENCE_H_


#ifdef __cplusplus
extern "C" {
#endif



/**
 * @brief Called on Kaa startup to restore the persisted configuration data (if present).
 *
 * Fetch configuration data to buffer and provide a valid size of it in buffer_size.
 * If *buffer == NULL or *buffer_size == 0 then there is no persisted configuration yet.
 * Set *needs_deallocation = true if buffer should be deallocated by Kaa, false otherwise.
 *
 * @param[out]  buffer              Pointer to buffer which should be filled with Kaa configuration data.
 * @param[out]  buffer_size         Pointer to buffer's size.
 * @param[out]  needs_deallocation  Indicates if the Kaa library should deallocate buffer by itself.
 *
 */
void ext_configuration_read(char **buffer, size_t *buffer_size, bool *needs_deallocation);



/**
 * @brief Called when Kaa is ready to persist configuration data.
 *
 * @param[in]   buffer          Valid pointer to buffer which contains the current Kaa configuration data.
 * @param[in]   buffer_size     The buffer's size.
 *
 */
void ext_configuration_store(const char *buffer, size_t buffer_size);


/**
 * @brief Called when Kaa need to remove configuration data.
 *
 */
void ext_configuration_delete(void);


#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* EXT_CONFIGURATION_PERSISTENCE_H_ */

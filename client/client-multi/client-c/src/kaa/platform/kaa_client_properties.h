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

/*
 * @file posix_kaa_client_properies.h
 *
 *  Created on: Apr 16, 2015
 *      Author: Andriy Panasenko <apanasenko@cybervisiontech.com>
 */

#ifndef POSIX_KAA_CLIENT_PROPERIES_H_
#define POSIX_KAA_CLIENT_PROPERIES_H_

#include <kaa_error.h>

typedef struct {
    const char *working_directory;
} kaa_client_props_t;

/**
 * @brief      Sets the client kaa properties struct by the user-defined struct
 *
 * @param[in]  self  A pointer to the user-defined structure
 * 
 * @note       since kaa_client_start() is called, the memory allocated for kaa_client_props_t
 *             shouldn't be freed and any of structure fields shouldn't be modified
 *             until kaa_client_stop() is called
 * 
 * @return     An error code
 */
kaa_error_t kaa_client_props_set(const kaa_client_props_t *self);

/**
 * @brief      Gets kaa properties struct
 *
 * @return     The pointer to kaa properties struct
 */
const kaa_client_props_t *kaa_client_props_get(void);

#endif /* POSIX_KAA_CLIENT_PROPERIES_H_ */

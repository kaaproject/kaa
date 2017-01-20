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
#ifndef PLATFORM_KAA_CLIENT_PROPERIES_H_
#define PLATFORM_KAA_CLIENT_PROPERIES_H_

#include <kaa_error.h>

typedef struct {
    /**
     * The directory to place client files into.
     *
     * Must not be @c NULL.
     */
    const char *working_directory;
} kaa_client_props_t;

/**
 * Set the current Kaa client properties to @p properties.
 *
 * If @p properties is @c NULL, resets properties to the default
 * value.
 *
 * @note Since kaa_client_start() is called, the memory allocated for
 * @p properties must not be freed and any of structure fields
 * must not be modified until kaa_client_stop() is called.
 *
 * @retval KAA_ERR_NONE     Success
 * @retval KAA_ERR_BADPARAM One of @c kaa_client_props_t conditions is
 *                          not satisfied
 */
kaa_error_t kaa_client_props_set(const kaa_client_props_t *properties);

/**
 * Return current client properties.
 */
const kaa_client_props_t *kaa_client_props_get(void);

#endif /* PLATFORM_KAA_CLIENT_PROPERIES_H_ */

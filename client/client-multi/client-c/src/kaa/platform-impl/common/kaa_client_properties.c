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

#include <platform/kaa_client_properties.h>
#include <stddef.h>

static const kaa_client_props_t kaa_default_props = {
    .working_directory = "./",
};

static const kaa_client_props_t *kaa_client_props = &kaa_default_props;

kaa_error_t kaa_client_props_set(const kaa_client_props_t *properties)
{
    if (properties == NULL) {
        kaa_client_props = &kaa_default_props;
        return KAA_ERR_NONE;
    }

    if (properties->working_directory == NULL) {
        return KAA_ERR_BADPARAM;
    }

    kaa_client_props = properties;

    return KAA_ERR_NONE;
}

const kaa_client_props_t *kaa_client_props_get(void)
{
    return kaa_client_props;
}

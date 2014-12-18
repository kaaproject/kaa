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

#include "kaa_platform_utils.h"

#include <string.h>

#include "kaa_common.h"

kaa_error_t kaa_write_buffer(char* buffer, size_t buf_size, void *data, size_t data_size)
{
    KAA_RETURN_IF_NIL4(buffer, buf_size, data, data_size, KAA_ERR_BADPARAM);

    if (buf_size >= data_size) {
        memcpy(buffer, data, data_size);
        return KAA_ERR_NONE;
    }

    return KAA_ERR_WRITE_FAILED;
}

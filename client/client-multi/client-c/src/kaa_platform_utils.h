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


#ifndef KAA_PLATFORM_UTILS_H_
#define KAA_PLATFORM_UTILS_H_

#include <stddef.h>

#include "kaa_error.h"

#define KAA_ALIGN_RATIO   4

kaa_error_t kaa_write_buffer(char* buffer, size_t buf_size, void *data, size_t data_size);

static inline size_t kaa_aligned_size_get(size_t size)
{
    return (size + (KAA_ALIGN_RATIO - (size % KAA_ALIGN_RATIO)));
}

#endif /* KAA_PLATFORM_UTILS_H_ */

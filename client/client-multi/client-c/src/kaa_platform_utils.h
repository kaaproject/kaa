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
#include <stdint.h>

#include "kaa_error.h"
#include "kaa_platform_common.h"

#ifdef __cplusplus
extern "C" {
#endif



typedef struct kaa_platform_message_writer_t_ kaa_platform_message_writer_t;



kaa_error_t kaa_platform_message_writer_create(kaa_platform_message_writer_t** writer_p
                                             , const char *buf
                                             , size_t len);

void kaa_platform_message_writer_destroy(kaa_platform_message_writer_t* writer);

kaa_error_t kaa_platform_message_write(kaa_platform_message_writer_t* writer
                                     , const void *data
                                     , size_t data_size);

const char* kaa_platform_message_writer_get_buffer(kaa_platform_message_writer_t* writer);



static inline size_t kaa_aligned_size_get(size_t size)
{
    return (size + (KAA_ALIGNMENT - (size % KAA_ALIGNMENT)));
}



#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* KAA_PLATFORM_UTILS_H_ */

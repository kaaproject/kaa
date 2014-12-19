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
#include "utilities/kaa_mem.h"



struct kaa_platform_message_writer_t_
{
    const char *buffer;
    int64_t     total;
    int64_t     used;
};



kaa_error_t kaa_platform_message_writer_create(kaa_platform_message_writer_t** writer_p
                                             , const char *buf
                                             , size_t len)
{
    KAA_RETURN_IF_NIL3(writer_p, buf, len, KAA_ERR_BADPARAM);

    *writer_p = (kaa_platform_message_writer_t*)KAA_MALLOC(sizeof(kaa_platform_message_writer_t));
    KAA_RETURN_IF_NIL(*writer_p, KAA_ERR_NOMEM);

    (*writer_p)->buffer = buf;
    (*writer_p)->total = len;
    (*writer_p)->used = 0;

    return KAA_ERR_NONE;
}



void kaa_platform_message_writer_destroy(kaa_platform_message_writer_t* writer)
{
    if (writer) {
        KAA_FREE(writer);
    }
}



kaa_error_t kaa_platform_message_write(kaa_platform_message_writer_t* writer
                                     , const void *data
                                     , size_t data_size)
{
    KAA_RETURN_IF_NIL3(writer, data, data_size, KAA_ERR_BADPARAM);

    if ((writer->total - writer->used) >= data_size) {
        memcpy((void *)(writer->buffer + writer->used), data, data_size);
        writer->used += data_size;
        return KAA_ERR_NONE;
    }

    return KAA_ERR_WRITE_FAILED;
}



const char* kaa_platform_message_writer_get_buffer(kaa_platform_message_writer_t* writer)
{
    if (writer && writer->buffer) {
        return writer->buffer;
    }
    return NULL;
}

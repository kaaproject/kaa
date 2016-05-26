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

#include <stdint.h>
#include <stddef.h>
#include "kaa_buffer.h"
#include "kaa_mem.h"
#include "kaa_common.h"


struct kaa_buffer_t {
    char    *begin;
    char    *end;
    char    *current;
};


kaa_error_t kaa_buffer_create_buffer(kaa_buffer_t **buffer_p, size_t buffer_size)
{
    KAA_RETURN_IF_NIL2(buffer_p, buffer_size, KAA_ERR_BADPARAM);

    kaa_buffer_t *buffer = (kaa_buffer_t *) KAA_MALLOC(sizeof(kaa_buffer_t));
    KAA_RETURN_IF_NIL(buffer, KAA_ERR_NOMEM);

    buffer->begin = (char *) KAA_CALLOC(buffer_size , sizeof(char));
    if (!buffer->begin) {
        KAA_FREE(buffer);
        return KAA_ERR_NOMEM;
    }

    buffer->end = buffer->begin + buffer_size;
    buffer->current = buffer->begin;
    *buffer_p = buffer;
    return KAA_ERR_NONE;
}


kaa_error_t kaa_buffer_destroy(kaa_buffer_t *buffer_p)
{
    KAA_RETURN_IF_NIL(buffer_p, KAA_ERR_BADPARAM);

    if (buffer_p->begin)
        KAA_FREE(buffer_p->begin);

    KAA_FREE(buffer_p);

    return KAA_ERR_NONE;
}


kaa_error_t kaa_buffer_allocate_space(kaa_buffer_t *buffer_p, char **buffer, size_t *free_size)
{
    KAA_RETURN_IF_NIL3(buffer_p, buffer, free_size, KAA_ERR_BADPARAM);

    *buffer = buffer_p->current;
    *free_size = buffer_p->end - buffer_p->current;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_buffer_reallocate_space(kaa_buffer_t *buffer_p, size_t size)
{
    KAA_RETURN_IF_NIL(buffer_p, KAA_ERR_BADPARAM);
    size_t locked_space    = buffer_p->current - buffer_p->begin;
    size_t total_space     = buffer_p->end     - buffer_p->begin;
    size_t new_buffer_size = size + locked_space;
    char *ptr;

    if (total_space >= new_buffer_size)
        return KAA_ERR_BADPARAM;

    ptr = KAA_REALLOC(buffer_p->begin, new_buffer_size);

    if (ptr) {
        buffer_p->begin = ptr;
        buffer_p->end = buffer_p->begin + new_buffer_size;
        buffer_p->current = buffer_p->begin + locked_space;
        return KAA_ERR_NONE;
    }

    return KAA_ERR_NOMEM;
}

kaa_error_t kaa_buffer_get_locked_space(kaa_buffer_t *buffer_p, size_t *size)
{
    KAA_RETURN_IF_NIL2(buffer_p, size, KAA_ERR_BADPARAM);

    *size = buffer_p->current - buffer_p->begin;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_buffer_get_size(kaa_buffer_t *buffer_p, size_t *size)
{
    KAA_RETURN_IF_NIL2(buffer_p, size, KAA_ERR_BADPARAM);

    *size = buffer_p->end - buffer_p->begin;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_buffer_get_free_space(kaa_buffer_t *buffer_p, size_t *size)
{
    KAA_RETURN_IF_NIL2(buffer_p, size, KAA_ERR_BADPARAM);

    *size = buffer_p->end - buffer_p->current;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_buffer_lock_space(kaa_buffer_t *buffer_p, size_t lock_size)
{
    KAA_RETURN_IF_NIL2(buffer_p, lock_size, KAA_ERR_BADPARAM);

    if (buffer_p->current + lock_size > buffer_p->end)
        return KAA_ERR_BUFFER_IS_NOT_ENOUGH;

    buffer_p->current += lock_size;
    return KAA_ERR_NONE;
}


kaa_error_t kaa_buffer_free_allocated_space(kaa_buffer_t *buffer_p, size_t size)
{
    KAA_RETURN_IF_NIL2(buffer_p, size, KAA_ERR_BADPARAM);

    if (buffer_p->begin + size > buffer_p->current)
        return KAA_ERR_BUFFER_INVALID_SIZE;

    char *byte_to_copy = buffer_p->begin + size;
    char *it = buffer_p->begin;
    for (; byte_to_copy != buffer_p->current; ++byte_to_copy, ++it)
        *it = *byte_to_copy;

    buffer_p->current -= size;

    return KAA_ERR_NONE;
}


kaa_error_t kaa_buffer_get_unprocessed_space(kaa_buffer_t *buffer_p
                                           , char **buffer
                                           , size_t *available_size)
{
    KAA_RETURN_IF_NIL3(buffer_p, buffer, available_size, KAA_ERR_BADPARAM);

    *buffer = buffer_p->begin;
    *available_size = buffer_p->current - buffer_p->begin;

    return KAA_ERR_NONE;
}


kaa_error_t kaa_buffer_reset(kaa_buffer_t *buffer_p)
{
    KAA_RETURN_IF_NIL(buffer_p, KAA_ERR_BADPARAM);
    buffer_p->current = buffer_p->begin;
    return KAA_ERR_NONE;
}


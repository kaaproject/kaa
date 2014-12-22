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



kaa_error_t kaa_platform_message_writer_create(kaa_platform_message_writer_t** writer_p
                                             , char *buf
                                             , size_t len)
{
    KAA_RETURN_IF_NIL3(writer_p, buf, len, KAA_ERR_BADPARAM);

    *writer_p = (kaa_platform_message_writer_t*)KAA_MALLOC(sizeof(kaa_platform_message_writer_t));
    KAA_RETURN_IF_NIL(*writer_p, KAA_ERR_NOMEM);

    (*writer_p)->start = buf;
    (*writer_p)->end = buf + len;
    (*writer_p)->current = buf;

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

    if ((writer->current + data_size) < writer->end) {
        memcpy((void *)writer->current, data, data_size);
        writer->current += data_size;
        return KAA_ERR_NONE;
    }

    return KAA_ERR_WRITE_FAILED;
}



kaa_error_t kaa_platform_message_write_aligned(kaa_platform_message_writer_t* writer
                                             , const void *data
                                             , size_t data_size)
{
    KAA_RETURN_IF_NIL3(writer, data, data_size, KAA_ERR_BADPARAM);

    size_t aligned_size = kaa_aligned_size_get(data_size);
    size_t alignment_length = aligned_size - data_size;

    if ((writer->current + aligned_size) < writer->end) {
        memcpy((void *)writer->current, data, data_size);
        memset((void *)(writer->current + data_size), 0, alignment_length);
        writer->current += aligned_size;
        return KAA_ERR_NONE;
    }

    return KAA_ERR_WRITE_FAILED;
}



kaa_error_t kaa_platform_message_write_extension_header(kaa_platform_message_writer_t* writer
                                                      , uint8_t extension_type
                                                      , uint32_t options
                                                      , uint32_t payload_size)
{
    KAA_RETURN_IF_NIL(writer, KAA_ERR_BADPARAM);

    if ((writer->current + KAA_EXTENSION_HEADER_SIZE) < writer->end) {
        extension_type = KAA_HTONS(extension_type);
        options = KAA_HTONL(options);
        payload_size = KAA_HTONL(payload_size);

        memcpy((void *)writer->current, &extension_type, KAA_EXTENSION_TYPE_SIZE);
        writer->current += KAA_EXTENSION_TYPE_SIZE;
        memcpy((void *)writer->current, &options, KAA_EXTENSION_OPTIONS_SIZE);
        writer->current += KAA_EXTENSION_OPTIONS_SIZE;
        memcpy((void *)writer->current, &payload_size, KAA_EXTENSION_PAYLOAD_LENGTH_SIZE);
        writer->current += KAA_EXTENSION_PAYLOAD_LENGTH_SIZE;

        return KAA_ERR_NONE;
    }

    return KAA_ERR_WRITE_FAILED;
}

const char* kaa_platform_message_writer_get_buffer(kaa_platform_message_writer_t* writer)
{
    if (writer && writer->start) {
        return writer->start;
    }
    return NULL;
}

kaa_error_t kaa_platform_message_reader_create(kaa_platform_message_reader_t **reader_p
                                                , const char *buffer
                                                , size_t len)
{
    KAA_RETURN_IF_NIL3(reader_p, buffer, len, KAA_ERR_BADPARAM);

    *reader_p = (kaa_platform_message_reader_t *) KAA_MALLOC(sizeof(kaa_platform_message_reader_t));
    KAA_RETURN_IF_NIL(*reader_p, KAA_ERR_NOMEM);

    (*reader_p)->start = buffer;
    (*reader_p)->current = buffer;
    (*reader_p)->end = buffer + len;

    return KAA_ERR_NONE;
}

void kaa_platform_message_reader_destroy(kaa_platform_message_reader_t *reader)
{
    if (reader) {
        KAA_FREE(reader);
    }
}

kaa_error_t kaa_platform_message_read(kaa_platform_message_reader_t *reader, void *buffer, size_t expected_size)
{
    KAA_RETURN_IF_NIL3(reader, buffer, expected_size, KAA_ERR_BADPARAM);
    if (reader->current + expected_size < reader->end) {
        memcpy(buffer, reader->current, expected_size);
        reader->current += expected_size;
        return KAA_ERR_NONE;
    }
    return KAA_ERR_READ_FAILED;
}

kaa_error_t kaa_platform_message_read_aligned(kaa_platform_message_reader_t *reader
                                            , void *buffer
                                            , size_t expected_size)
{
    KAA_RETURN_IF_NIL3(reader, buffer, expected_size, KAA_ERR_BADPARAM);
    size_t aligned_size = kaa_aligned_size_get(expected_size);
    if (reader->current + aligned_size < reader->end) {
        memcpy(buffer, reader->current, expected_size);
        reader->current += aligned_size;
        return KAA_ERR_NONE;
    }
    return KAA_ERR_READ_FAILED;
}

kaa_error_t kaa_platform_message_read_extension_header(kaa_platform_message_reader_t *reader
                                                     , uint8_t *extension_type
                                                     , uint32_t *extension_options
                                                     , uint32_t *extension_payload_length)
{
    KAA_RETURN_IF_NIL4(reader, extension_type, extension_options, extension_payload_length, KAA_ERR_BADPARAM);

    if (reader->current + KAA_EXTENSION_HEADER_SIZE < reader->end) {

        uint32_t ext_l1 = KAA_NTOHL(*((const uint32_t *) reader->current));
        *extension_type = (ext_l1 >> 24) & 0xff;
        *extension_options = ext_l1 & 0xffffff;
        *extension_payload_length = KAA_NTOHL(*((const uint32_t *) (reader->current + sizeof(uint32_t))));

        reader->current += KAA_EXTENSION_HEADER_SIZE;
    }

    return KAA_ERR_READ_FAILED;
}

bool kaa_platform_message_is_buffer_large_enough(kaa_platform_message_reader_t *reader
                                               , size_t size)
{
    if (!reader) {
        return false;
    }

    if (!size) {
        return true;
    }

    return (reader->current + size < reader->end);
}

kaa_error_t kaa_platform_message_skip(kaa_platform_message_reader_t *reader, size_t size)
{
    KAA_RETURN_IF_NIL2(reader, size, KAA_ERR_BADPARAM);

    if (reader->current + size < reader->end) {
        reader->current += size;
        return KAA_ERR_NONE;
    }

    return KAA_ERR_READ_FAILED;
}

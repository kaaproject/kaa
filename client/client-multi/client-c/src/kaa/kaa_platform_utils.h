/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


#ifndef KAA_PLATFORM_UTILS_H_
#define KAA_PLATFORM_UTILS_H_


#include "kaa_error.h"
#include "kaa_platform_common.h"

#ifdef __cplusplus
extern "C" {
#endif


typedef struct {
    char       *begin;
    char       *current;
    char       *end;
} kaa_platform_message_writer_t;


typedef struct {
    const char *begin;
    const char *current;
    const char *end;
} kaa_platform_message_reader_t;



kaa_error_t kaa_platform_message_writer_create(kaa_platform_message_writer_t** writer_p
                                             , char *buf
                                             , size_t len);

void kaa_platform_message_writer_destroy(kaa_platform_message_writer_t* writer);

kaa_error_t kaa_platform_message_write(kaa_platform_message_writer_t* writer
                                     , const void *data
                                     , size_t data_size);

kaa_error_t kaa_platform_message_write_alignment(kaa_platform_message_writer_t* writer);

kaa_error_t kaa_platform_message_write_aligned(kaa_platform_message_writer_t* writer
                                             , const void *data
                                             , size_t data_size);

kaa_error_t kaa_platform_message_header_write(kaa_platform_message_writer_t* writer
                                            , uint32_t protocol_id
                                            , uint16_t protocol_version);

kaa_error_t kaa_platform_message_write_extension_header(kaa_platform_message_writer_t* writer
                                                      , uint16_t extension_type
                                                      , uint16_t options
                                                      , uint32_t payload_size);



kaa_error_t kaa_platform_message_reader_create(kaa_platform_message_reader_t **reader_p
                                             , const char *buffer
                                             , size_t len);

void kaa_platform_message_reader_destroy(kaa_platform_message_reader_t *reader);

kaa_error_t kaa_platform_message_read(kaa_platform_message_reader_t *reader
                                    , void *buffer
                                    , size_t expected_size);

kaa_error_t kaa_platform_message_read_aligned(kaa_platform_message_reader_t *reader
                                            , void *buffer
                                            , size_t expected_size);

kaa_error_t kaa_platform_message_header_read(kaa_platform_message_reader_t* reader
                                           , uint32_t *protocol_id
                                           , uint16_t *protocol_version
                                           , uint16_t *extension_count);

kaa_error_t kaa_platform_message_read_extension_header(kaa_platform_message_reader_t *reader
                                                     , uint16_t *extension_type
                                                     , uint16_t *extension_options
                                                     , uint32_t *extension_payload_length);

bool kaa_platform_message_is_buffer_large_enough(kaa_platform_message_reader_t *reader
                                               , size_t size);

kaa_error_t kaa_platform_message_skip(kaa_platform_message_reader_t *reader, size_t size);

static inline size_t kaa_aligned_size_get(size_t size)
{
    return (size + (KAA_ALIGNMENT - (size % KAA_ALIGNMENT)) % KAA_ALIGNMENT);
}



#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* KAA_PLATFORM_UTILS_H_ */

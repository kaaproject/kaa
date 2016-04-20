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


#ifndef KAA_PLATFORM_UTILS_H_
#define KAA_PLATFORM_UTILS_H_

#include <stddef.h>
#include <stdint.h>
#include <stdbool.h>

#include "kaa_error.h"
#include "kaa_platform_common.h"

#ifdef __cplusplus
extern "C" {
#endif


typedef struct {
    uint8_t *begin;
    uint8_t *current;
    uint8_t *end;
} kaa_platform_message_writer_t;


typedef struct {
    const uint8_t *begin;
    const uint8_t *current;
    const uint8_t *end;
} kaa_platform_message_reader_t;

#define KAA_MESSAGE_WRITER(buffer, len) \
    (kaa_platform_message_writer_t){ (buffer), (buffer), (buffer) + (len) }

#define KAA_MESSAGE_READER(buffer, len) \
    (kaa_platform_message_reader_t){ (buffer), (buffer), (buffer) + (len) }

/**
 * @deprecated Use @ref KAA_MESSAGE_WRITER instead -- it doesn't allocate memory.
 */
kaa_error_t kaa_platform_message_writer_create(kaa_platform_message_writer_t** writer_p,
        uint8_t *buf, size_t len);

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



/**
 * @deprecated Use @ref KAA_MESSAGE_READER instead -- it doesn't allocate memory.
 */
kaa_error_t kaa_platform_message_reader_create(kaa_platform_message_reader_t **reader_p
                                             , const uint8_t *buffer
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

#define KAA_ALIGNED_SIZE(s) ((s) + (KAA_ALIGNMENT - (s) % KAA_ALIGNMENT) % KAA_ALIGNMENT)

static inline size_t kaa_aligned_size_get(size_t size)
{
    return (size + (KAA_ALIGNMENT - (size % KAA_ALIGNMENT)) % KAA_ALIGNMENT);
}

#define KAA_STATIC_ASSERT(name, expr) \
    static char static_assertion_##name[expr] __attribute__((unused))

#ifdef __cplusplus
}      /* extern "C" */
#endif

#endif /* KAA_PLATFORM_UTILS_H_ */

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

#ifndef KAA_COMMON_SCHEMA_H_
#define KAA_COMMON_SCHEMA_H_

#ifdef __cplusplus
extern "C" {
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

#include <stdint.h>

#include "avro_src/avro/io.h"

#include "kaa_list.h"

typedef void (*serialize_fn)(avro_writer_t writer, void *data);
typedef void* (*deserialize_fn)(avro_reader_t reader);
typedef size_t (*get_size_fn)(void *data);
typedef void (*destroy_fn)(void *data);

typedef struct kaa_bytes_t_ {
    uint8_t* buffer;
    int32_t  size;
} kaa_bytes_t;

typedef struct kaa_union_t_ {
    uint8_t type;
    void   *data;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_union_t;

void kaa_serialize_string(avro_writer_t writer, void* data);
char* kaa_deserialize_string(avro_reader_t reader);
size_t kaa_get_size_string(void *data);

kaa_bytes_t* kaa_deserialize_bytes(avro_reader_t reader);
void kaa_serialize_bytes(avro_writer_t writer, void* data);
void kaa_destroy_bytes(void *data);
size_t kaa_get_size_bytes(void *data);

void kaa_serialize_boolean(avro_writer_t writer, void* data);
int8_t* kaa_deserialize_boolean(avro_reader_t reader);

void kaa_serialize_int(avro_writer_t writer, void* data);
int32_t* kaa_deserialize_int(avro_reader_t reader);

void kaa_serialize_long(avro_writer_t writer, void* data);
int64_t* kaa_deserialize_long(avro_reader_t reader);
size_t size_long(int64_t l);

void kaa_serialize_array(avro_writer_t writer, kaa_list_t* array, serialize_fn s);
kaa_list_t *kaa_deserialize_array(avro_reader_t reader, deserialize_fn ds);
size_t kaa_array_size(kaa_list_t* array, get_size_fn s);

void kaa_destroy_null(void *data);

CLOSE_EXTERN
#endif /* KAA_COMMON_SCHEMA_H_ */

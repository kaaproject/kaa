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

    destroy_fn  destroy;
} kaa_bytes_t;

typedef struct kaa_string_t_ {
    char* data;

    destroy_fn  destroy;
} kaa_string_t;

typedef struct kaa_union_t_ {
    uint8_t type;
    void   *data;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_union_t;

void kaa_string_serialize(avro_writer_t writer, void* data);
kaa_string_t* kaa_string_deserialize(avro_reader_t reader);
kaa_string_t* kaa_string_move_create(const char* data, destroy_fn destroy);
kaa_string_t* kaa_string_copy_create(const char* data, destroy_fn destroy);
void kaa_string_destroy(void *data);
size_t kaa_string_get_size(void *data);

kaa_bytes_t* kaa_bytes_deserialize(avro_reader_t reader);
void kaa_bytes_serialize(avro_writer_t writer, void* data);
kaa_bytes_t* kaa_bytes_move_create(const uint8_t* data, size_t data_len, destroy_fn destroy);
kaa_bytes_t* kaa_bytes_copy_create(const uint8_t* data, size_t data_len, destroy_fn destroy);
void kaa_bytes_destroy(void *data);
size_t kaa_bytes_get_size(void *data);

void kaa_boolean_serialize(avro_writer_t writer, void* data);
int8_t* kaa_boolean_deserialize(avro_reader_t reader);

void kaa_int_serialize(avro_writer_t writer, void* data);
int32_t* kaa_int_deserialize(avro_reader_t reader);

void kaa_long_serialize(avro_writer_t writer, void* data);
int64_t* kaa_long_deserialize(avro_reader_t reader);
size_t kaa_long_get_size(int64_t l);

void kaa_array_serialize(avro_writer_t writer, kaa_list_t* array, serialize_fn serialize);
kaa_list_t *kaa_array_deserialize(avro_reader_t reader, deserialize_fn deserialize);
size_t kaa_array_get_size(kaa_list_t* array, get_size_fn get_size);

void kaa_null_serialize(avro_writer_t writer, void*);
void* kaa_null_deserialize(avro_reader_t reader);
void kaa_null_destroy(void *data);
size_t kaa_null_get_size();

void kaa_data_destroy(void *data);

CLOSE_EXTERN
#endif /* KAA_COMMON_SCHEMA_H_ */

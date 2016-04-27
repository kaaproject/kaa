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

#ifndef KAA_COMMON_SCHEMA_H_
#define KAA_COMMON_SCHEMA_H_

#ifdef __cplusplus
extern "C" {
#endif

#include <stddef.h>

#include "avro_src/avro/io.h"
#include "collections/kaa_list.h"



/*
 * Do not change neither name or value of these constants.
 */
#define AVRO_NULL_SIZE      0
#define AVRO_FLOAT_SIZE     4
#define AVRO_DOUBLE_SIZE    8



typedef void (*serialize_fn)(avro_writer_t writer, void *data);

/**
 * @brief This is like a parent class both for @ref deserialize_wo_ctx_fn
 * and @ref deserialize_w_ctx_fn that is why it has an empty parameter list instead of using @c void.
 *
 * @note It is not expected to use it explicitly. It is used under the hood of Avro Gen C.
 *
 * It is a workaround to specify different type of a deserializer. For now there is two kinds - with and without context.
 * Now a context is used to deserialize the fixed Avro type. See @ref kaa_fixed_deserialize().
 */
typedef void *(*deserialize_fn)();

/**
 * @brief See @ref deserialize_fn.
 */
typedef void *(*deserialize_wo_ctx_fn)(avro_reader_t reader);

/**
 * @brief See @ref deserialize_fn.
 */
typedef void *(*deserialize_w_ctx_fn)(avro_reader_t reader, void *context);

typedef size_t (*get_size_fn)(void *data);
typedef void (*destroy_fn)(void *data);



typedef struct {
    uint8_t* buffer;
    int32_t  size;

    destroy_fn  destroy;
} kaa_bytes_t;

typedef struct {
    char* data;

    destroy_fn  destroy;
} kaa_string_t;

typedef struct {
    uint8_t type;
    void   *data;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_union_t;



kaa_string_t *kaa_string_move_create(const char *data, destroy_fn destroy);
kaa_string_t *kaa_string_copy_create(const char *data);

void kaa_string_destroy(void *data);
void kaa_string_serialize(avro_writer_t writer, void *data);
kaa_string_t *kaa_string_deserialize(avro_reader_t reader);
size_t kaa_string_get_size(void *data);



kaa_bytes_t *kaa_bytes_move_create(const uint8_t *data, size_t data_len, destroy_fn destroy);
kaa_bytes_t *kaa_bytes_copy_create(const uint8_t *data, size_t data_len);

void kaa_bytes_destroy(void *data);
void kaa_bytes_serialize(avro_writer_t writer, void *data);
kaa_bytes_t *kaa_bytes_deserialize(avro_reader_t reader);
size_t kaa_bytes_get_size(void *data);



kaa_bytes_t *kaa_fixed_move_create(const uint8_t *data, size_t data_len, destroy_fn destroy);
kaa_bytes_t *kaa_fixed_copy_create(const uint8_t *data, size_t data_len);

void kaa_fixed_destroy(void *data);
void kaa_fixed_serialize(avro_writer_t writer, void *data);
kaa_bytes_t *kaa_fixed_deserialize(avro_reader_t reader, void *context);
size_t kaa_fixed_get_size(void *data);



void kaa_boolean_serialize(avro_writer_t writer, void *data);
int8_t *kaa_boolean_deserialize(avro_reader_t reader);
size_t kaa_boolean_get_size(void *data);



void kaa_int_serialize(avro_writer_t writer, void *data);
int32_t *kaa_int_deserialize(avro_reader_t reader);
size_t kaa_int_get_size(void *data);



void kaa_long_serialize(avro_writer_t writer, void *data);
int64_t *kaa_long_deserialize(avro_reader_t reader);
size_t kaa_long_get_size(void *data);



void kaa_enum_serialize(avro_writer_t writer, void *data);
int *kaa_enum_deserialize(avro_reader_t reader);
size_t kaa_enum_get_size(void *data);



void kaa_float_serialize(avro_writer_t writer, void *data);
float *kaa_float_deserialize(avro_reader_t reader);
size_t kaa_float_get_size(void *data);



void kaa_double_serialize(avro_writer_t writer, void *data);
double *kaa_double_deserialize(avro_reader_t reader);
size_t kaa_double_get_size(void *data);



void kaa_array_serialize(avro_writer_t writer, kaa_list_t *array, serialize_fn serialize);
kaa_list_t *kaa_array_deserialize_wo_ctx(avro_reader_t reader, deserialize_wo_ctx_fn deserialize);
kaa_list_t *kaa_array_deserialize_w_ctx(avro_reader_t reader, deserialize_w_ctx_fn deserialize, void *deserialize_context);
size_t kaa_array_get_size(kaa_list_t *array, get_size_fn get_size);



void kaa_null_serialize(avro_writer_t writer, void *data);
void *kaa_null_deserialize(avro_reader_t reader);
void kaa_null_destroy(void *data);
size_t kaa_null_get_size(void* data);

void kaa_data_destroy(void *data);

size_t avro_long_get_size(int64_t l);

#ifdef __cplusplus
} // extern "C"
#endif
#endif /* KAA_COMMON_SCHEMA_H_ */

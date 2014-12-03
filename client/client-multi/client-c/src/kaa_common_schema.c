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

#include "kaa_common_schema.h"

#include <string.h>

#include "avro_src/avro/io.h"
#include "avro_src/encoding.h"

#include "kaa_mem.h"

void kaa_serialize_string(avro_writer_t writer, void* data)
{
    avro_binary_encoding.write_string(writer, (char*)data);
}

char* kaa_deserialize_string(avro_reader_t reader)
{
    char* str;
    int64_t size;
    avro_binary_encoding.read_bytes(reader, &str, &size);
    return str;
}

size_t kaa_get_size_string(void *data)
{
    size_t len = strlen(data);
    return size_long(len) + len;
}

kaa_bytes_t* kaa_deserialize_bytes(avro_reader_t reader)
{
    kaa_bytes_t* data = (kaa_bytes_t *) KAA_MALLOC(sizeof(kaa_bytes_t));
    int64_t size;
    avro_binary_encoding.read_bytes(reader, (char**)&data->buffer, &size);
    data->size = size;
    return data;
}

void kaa_serialize_bytes(avro_writer_t writer, void* data)
{
    kaa_bytes_t* bytes = (kaa_bytes_t*)data;
    avro_binary_encoding.write_bytes(writer, (char*)bytes->buffer, bytes->size);
}

void kaa_destroy_bytes(void *data)
{
    kaa_bytes_t* bytes = (kaa_bytes_t*)data;
    KAA_FREE(bytes->buffer);
}

size_t kaa_get_size_bytes(void *data)
{
    kaa_bytes_t* bytes = (kaa_bytes_t*)data;
    return size_long(bytes->size) + bytes->size;
}

void kaa_serialize_boolean(avro_writer_t writer, void* data)
{
    int8_t* val = (int8_t*)data;
    avro_binary_encoding.write_boolean(writer, *val);
}

int8_t* kaa_deserialize_boolean(avro_reader_t reader)
{
    int8_t* data = (int8_t *) KAA_MALLOC(sizeof(int8_t));
    avro_binary_encoding.read_boolean(reader, data);
    return data;
}

void kaa_serialize_int(avro_writer_t writer, void* data)
{
    int32_t* val = (int32_t*)data;
    avro_binary_encoding.write_int(writer, *val);
}

int32_t* kaa_deserialize_int(avro_reader_t reader)
{
    int32_t* data = (int32_t *) KAA_MALLOC(sizeof(int32_t));
    avro_binary_encoding.read_int(reader, data);
    return data;
}

void kaa_serialize_long(avro_writer_t writer, void* data)
{
    int64_t* val = (int64_t*)data;
    avro_binary_encoding.write_long(writer, *val);
}

int64_t* kaa_deserialize_long(avro_reader_t reader)
{
    int64_t* data = (int64_t *) KAA_MALLOC(sizeof(int64_t));
    avro_binary_encoding.read_long(reader, data);
    return data;
}

size_t size_long(int64_t l)
{
    int64_t len = 0;
    uint64_t n = (l << 1) ^ (l >> 63);
    while (n & ~0x7F) {
        len++;
        n >>= 7;
    }
    len++;
    return len;
}

void kaa_serialize_array(avro_writer_t writer, kaa_list_t* array, serialize_fn s)
{
    int64_t element_count = kaa_list_get_size(array);
    if (element_count > 0) {
        avro_binary_encoding.write_long(writer, element_count);

        while (array) {
            s(writer, kaa_list_get_data(array));
            array = kaa_list_next(array);
        }
    }

    avro_binary_encoding.write_long(writer, 0);
}

kaa_list_t *kaa_deserialize_array(avro_reader_t reader, deserialize_fn ds)
{
    kaa_list_t *array = NULL;
    int64_t element_count;

    avro_binary_encoding.read_long(reader, &element_count);
    if (element_count > 0) {
        void* data = ds(reader);
        array = kaa_list_create(data);

        while (--element_count > 0) {
            void* data = ds(reader);
            array = kaa_list_push_front(array, data);
        }

        avro_binary_encoding.read_long(reader, &element_count);
    }

    return array;
}

size_t kaa_array_size(kaa_list_t* cursor, get_size_fn s)
{
    size_t array_size = 0;
    size_t count = 0;

    if (cursor && s) {
        while (cursor) {
            array_size += s(kaa_list_get_data(cursor));
            ++count;
            cursor = kaa_list_next(cursor);
        }
    }

    array_size += size_long(count);
    array_size += size_long(0);

    return array_size;
}

void kaa_destroy_null(void *data) {}



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

#include "kaa_private.h"

#include <stdint.h>
#include <string.h>

#include "kaa_common.h"
#include "platform/stdio.h"
#include "kaa_common_schema.h"
#include "avro_src/avro/io.h"
#include "avro_src/encoding.h"
#include "utilities/kaa_mem.h"
#include "kaa_error.h"



size_t avro_long_get_size(int64_t l)
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

void kaa_string_serialize(avro_writer_t writer, void *data)
{
    KAA_RETURN_IF_NIL2(writer, data,);

    kaa_string_t *str = (kaa_string_t *)data;
    if (str->data) {
        avro_binary_encoding.write_string(writer, str->data);
    }
}

kaa_string_t *kaa_string_move_create(const char *data, destroy_fn destroy)
{
    KAA_RETURN_IF_NIL(data, NULL);

    kaa_string_t *str = (kaa_string_t *)KAA_MALLOC(sizeof(kaa_string_t));
    KAA_RETURN_IF_NIL(str, NULL);

    str->data = (char *)data;
    str->destroy = destroy;

    return str;
}

kaa_string_t *kaa_string_copy_create(const char *data)
{
    KAA_RETURN_IF_NIL(data, NULL);

    kaa_string_t *str = (kaa_string_t *)KAA_MALLOC(sizeof(kaa_string_t));
    KAA_RETURN_IF_NIL(str, NULL);

    size_t len = strlen(data) + 1;
    str->data = (char *)KAA_MALLOC(len * sizeof(char));
    if (!str->data) {
        KAA_FREE(str);
        return NULL;
    }

    memcpy(str->data, data, len);
    str->destroy = &kaa_data_destroy;

    return str;
}

void kaa_string_destroy(void *data)
{
    KAA_RETURN_IF_NIL(data,);

    kaa_string_t *str = (kaa_string_t *)data;
    if (str->data && str->destroy) {
        str->destroy(str->data);
    }
    KAA_FREE(str);
}

kaa_string_t *kaa_string_deserialize(avro_reader_t reader)
{
    KAA_RETURN_IF_NIL(reader, NULL);

    kaa_string_t *str = (kaa_string_t *)KAA_MALLOC(sizeof(kaa_string_t));
    KAA_RETURN_IF_NIL(str, NULL);

    avro_binary_encoding.read_string(reader, &str->data, NULL);
    str->destroy = kaa_data_destroy;

    return str;
}

size_t kaa_string_get_size(void *data)
{
    KAA_RETURN_IF_NIL(data, 0);

    kaa_string_t *str = (kaa_string_t *)data;
    if (str->data) {
        size_t len = strlen(str->data);
        return avro_long_get_size(len) + len;
    }
    return 0;
}

kaa_bytes_t *kaa_bytes_move_create(const uint8_t *data, size_t data_len, destroy_fn destroy)
{
    KAA_RETURN_IF_NIL2(data, data_len, NULL);

    kaa_bytes_t *bytes_array = (kaa_bytes_t *)KAA_MALLOC(sizeof(kaa_bytes_t));
    KAA_RETURN_IF_NIL(bytes_array, NULL);

    bytes_array->buffer = (uint8_t *)data;
    bytes_array->size = data_len;
    bytes_array->destroy = destroy;

    return bytes_array;
}

kaa_bytes_t *kaa_bytes_copy_create(const uint8_t *data, size_t data_len)
{
    KAA_RETURN_IF_NIL2(data, data_len, NULL);

    kaa_bytes_t *bytes_array = (kaa_bytes_t *)KAA_MALLOC(sizeof(kaa_bytes_t));
    KAA_RETURN_IF_NIL(bytes_array, NULL);

    bytes_array->buffer = (uint8_t *)KAA_MALLOC(sizeof(uint8_t) * data_len);
    if (!bytes_array->buffer) {
        KAA_FREE(bytes_array);
        return NULL;
    }

    memcpy(bytes_array->buffer, data, data_len);
    bytes_array->size = data_len;
    bytes_array->destroy = &kaa_data_destroy;

    return bytes_array;
}

void kaa_bytes_destroy(void *data)
{
    KAA_RETURN_IF_NIL(data, );

    kaa_bytes_t *bytes = (kaa_bytes_t *)data;
    if (bytes->buffer && bytes->destroy) {
        bytes->destroy(bytes->buffer);
    }
    KAA_FREE(bytes);
}

kaa_bytes_t *kaa_bytes_deserialize(avro_reader_t reader)
{
    KAA_RETURN_IF_NIL(reader, NULL);

    kaa_bytes_t *bytes = (kaa_bytes_t *)KAA_MALLOC(sizeof(kaa_bytes_t));
    KAA_RETURN_IF_NIL(bytes, NULL);

    int64_t size;
    avro_binary_encoding.read_bytes(reader, (char **)&bytes->buffer, &size);
    bytes->size = size;
    bytes->destroy = kaa_data_destroy;

    return bytes;
}

void kaa_bytes_serialize(avro_writer_t writer, void *data)
{
    kaa_bytes_t *bytes = (kaa_bytes_t *)data;
    KAA_RETURN_IF_NIL4(writer, bytes, bytes->buffer, bytes->size, );

    avro_binary_encoding.write_bytes(writer, (char *)bytes->buffer, bytes->size);
}

size_t kaa_bytes_get_size(void *data)
{
    kaa_bytes_t *bytes = (kaa_bytes_t *)data;
    KAA_RETURN_IF_NIL3(bytes, bytes->buffer, bytes->size, 0);
    return avro_long_get_size(bytes->size) + bytes->size;

}

kaa_bytes_t *kaa_fixed_move_create(const uint8_t *data, size_t data_len, destroy_fn destroy)
{
    return kaa_bytes_move_create(data, data_len, destroy);
}

kaa_bytes_t *kaa_fixed_copy_create(const uint8_t *data, size_t data_len)
{
    return kaa_bytes_copy_create(data, data_len);
}

void kaa_fixed_destroy(void *data)
{
    kaa_bytes_destroy(data);
}

void kaa_fixed_serialize(avro_writer_t writer, void *data)
{
    kaa_bytes_t *bytes = (kaa_bytes_t *)data;
    KAA_RETURN_IF_NIL4(writer, bytes, bytes->buffer, bytes->size, );

    avro_write(writer, (char *)bytes->buffer, bytes->size);
}

kaa_bytes_t *kaa_fixed_deserialize(avro_reader_t reader, void *context)
{
    KAA_RETURN_IF_NIL2(reader, context, NULL);

    kaa_bytes_t *bytes = (kaa_bytes_t *)KAA_MALLOC(sizeof(kaa_bytes_t));
    KAA_RETURN_IF_NIL(bytes, NULL);
    bytes->buffer = (uint8_t*)KAA_MALLOC((*(size_t *)context) * sizeof(uint8_t));
    if (!bytes->buffer) {
        KAA_FREE(bytes);
        return NULL;
    }

    avro_read(reader, (void *)bytes->buffer, (*(size_t *)context));
    bytes->size = (*(size_t *)context);
    bytes->destroy = kaa_data_destroy;

    return bytes;
}

size_t kaa_fixed_get_size(void *data)
{
    kaa_bytes_t *bytes = (kaa_bytes_t *)data;
    KAA_RETURN_IF_NIL3(bytes, bytes->buffer, bytes->size, 0);
    return bytes->size;
}

void kaa_boolean_serialize(avro_writer_t writer, void *data)
{
    KAA_RETURN_IF_NIL2(writer, data,);
    avro_binary_encoding.write_boolean(writer, *((int8_t *)data));
}

int8_t* kaa_boolean_deserialize(avro_reader_t reader)
{
    KAA_RETURN_IF_NIL(reader, NULL);

    int8_t* data = (int8_t*)KAA_MALLOC(sizeof(int8_t));
    KAA_RETURN_IF_NIL(data, NULL);
    avro_binary_encoding.read_boolean(reader, data);
    return data;
}

size_t kaa_boolean_get_size(void *data)
{
    KAA_RETURN_IF_NIL(data, 0);
    return avro_long_get_size(*((int8_t *)data));
}

void kaa_int_serialize(avro_writer_t writer, void *data)
{
    KAA_RETURN_IF_NIL2(writer, data,);
    avro_binary_encoding.write_int(writer, *((int32_t *)data));
}

int32_t *kaa_int_deserialize(avro_reader_t reader)
{
    KAA_RETURN_IF_NIL(reader, NULL);

    int32_t *data = (int32_t *)KAA_MALLOC(sizeof(int32_t));
    KAA_RETURN_IF_NIL(data, NULL);
    avro_binary_encoding.read_int(reader, data);
    return data;
}

size_t kaa_int_get_size(void *data)
{
    KAA_RETURN_IF_NIL(data, 0);
    return avro_long_get_size(*((int32_t *)data));
}

void kaa_long_serialize(avro_writer_t writer, void *data)
{
    KAA_RETURN_IF_NIL2(writer, data,);
    avro_binary_encoding.write_long(writer, *((int64_t *)data));
}

int64_t *kaa_long_deserialize(avro_reader_t reader)
{
    KAA_RETURN_IF_NIL(reader, NULL);

    int64_t *data = (int64_t *)KAA_MALLOC(sizeof(int64_t));
    KAA_RETURN_IF_NIL(data, NULL);
    avro_binary_encoding.read_long(reader, data);
    return data;
}

size_t kaa_long_get_size(void *data)
{
    KAA_RETURN_IF_NIL(data, 0);
    return avro_long_get_size(*((int64_t *)data));
}

void kaa_enum_serialize(avro_writer_t writer, void *data)
{
    KAA_RETURN_IF_NIL2(writer, data,);
    avro_binary_encoding.write_long(writer, *((int *)data));
}

int *kaa_enum_deserialize(avro_reader_t reader)
{
    KAA_RETURN_IF_NIL(reader, NULL);

    int *data = (int *)KAA_MALLOC(sizeof(int));
    KAA_RETURN_IF_NIL(data, NULL);
    int64_t value;
    avro_binary_encoding.read_long(reader, &value);
    *data = value;
    return data;
}

size_t kaa_enum_get_size(void *data)
{
    KAA_RETURN_IF_NIL(data, 0);
    return avro_long_get_size(*((int *)data));
}

void kaa_float_serialize(avro_writer_t writer, void *data)
{
    KAA_RETURN_IF_NIL2(writer, data, );
    avro_binary_encoding.write_float(writer, *((float *)data));
}

float *kaa_float_deserialize(avro_reader_t reader)
{
    KAA_RETURN_IF_NIL(reader, NULL);

    float *data = (float *)KAA_MALLOC(sizeof(float));
    KAA_RETURN_IF_NIL(data, NULL);
    avro_binary_encoding.read_float(reader, data);
    return data;
}

size_t kaa_float_get_size(void *data)
{
    KAA_RETURN_IF_NIL(data, 0);
    return AVRO_FLOAT_SIZE;
}

void kaa_double_serialize(avro_writer_t writer, void *data)
{
    KAA_RETURN_IF_NIL2(writer, data,);
    avro_binary_encoding.write_double(writer, *((double *)data));
}

double *kaa_double_deserialize(avro_reader_t reader)
{
    KAA_RETURN_IF_NIL(reader, NULL);

    double* data = (double *)KAA_MALLOC(sizeof(double));
    KAA_RETURN_IF_NIL(data, NULL);
    avro_binary_encoding.read_double(reader, data);
    return data;
}

size_t kaa_double_get_size(void *data)
{
    KAA_RETURN_IF_NIL(data, 0);
    return AVRO_DOUBLE_SIZE;
}

void kaa_array_serialize(avro_writer_t writer, kaa_list_t *array, serialize_fn serialize)
{
    KAA_RETURN_IF_NIL(writer, );

    size_t element_count = kaa_list_get_size(array);

    if (element_count > 0) {
        avro_binary_encoding.write_long(writer, element_count);
        if (serialize) {
            kaa_list_node_t *it = kaa_list_begin(array);
            while (it) {
                serialize(writer, kaa_list_get_data(it));
                it = kaa_list_next(it);
            }
        }
    }

    avro_binary_encoding.write_long(writer, 0);
}

static void *do_deserialize(avro_reader_t reader, deserialize_fn deserialize, void *context)
{
    if (context) {
        return ((deserialize_w_ctx_fn)deserialize)(reader, context);
    }

    return ((deserialize_wo_ctx_fn)deserialize)(reader);
}

static kaa_list_t *kaa_array_deserialize(avro_reader_t reader, deserialize_fn deserialize, void *context)
{
    KAA_RETURN_IF_NIL2(reader, deserialize, NULL);

    kaa_list_t *array = kaa_list_create();
    KAA_RETURN_IF_NIL(array, NULL);

    int64_t element_count;
    avro_binary_encoding.read_long(reader, &element_count);

    while (element_count != 0) {
        if (element_count < 0) {
            int64_t temp;
            element_count *= (-1);
            avro_binary_encoding.read_long(reader, &temp);
        }

        while (element_count-- > 0) {
            kaa_list_push_back(array, do_deserialize(reader, deserialize, context));
        }

        avro_binary_encoding.read_long(reader, &element_count);
    }

    return array;
}

kaa_list_t *kaa_array_deserialize_wo_ctx(avro_reader_t reader, deserialize_wo_ctx_fn deserialize)
{
    KAA_RETURN_IF_NIL2(reader, deserialize, NULL);
    return kaa_array_deserialize(reader, deserialize, NULL);
}

kaa_list_t *kaa_array_deserialize_w_ctx(avro_reader_t reader, deserialize_w_ctx_fn deserialize, void *context)
{
    KAA_RETURN_IF_NIL3(reader, deserialize, context, NULL);
    return kaa_array_deserialize(reader, deserialize, context);
}

size_t kaa_array_get_size(kaa_list_t *array, get_size_fn get_size)
{
    KAA_RETURN_IF_NIL(get_size, 0);

    size_t array_size = 0;

    if (array) {
        kaa_list_node_t *it = kaa_list_begin(array);
        while (it) {
            array_size += get_size(kaa_list_get_data(it));
            it = kaa_list_next(it);
        }

        array_size += avro_long_get_size(kaa_list_get_size(array));
    }

    array_size += avro_long_get_size(0);

    return array_size;
}

void kaa_null_serialize(avro_writer_t writer, void *data)
{
    (void)writer;
    (void)data;
}

void *kaa_null_deserialize(avro_reader_t reader)
{
    (void)reader;
    return NULL;
}

void kaa_null_destroy(void *data)
{
    (void)data;
}

size_t kaa_null_get_size(void* data)
{
    (void)data;
    return AVRO_NULL_SIZE;
}

void kaa_data_destroy(void *data)
{
    KAA_RETURN_IF_NIL(data,);
    KAA_FREE(data);
}


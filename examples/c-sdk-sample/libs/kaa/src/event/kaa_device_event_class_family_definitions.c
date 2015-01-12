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

#include "../../../kaa/src/event/kaa_device_event_class_family_definitions.h"
# include <stdio.h>
# include <string.h>

# include "avro_src/avro/io.h"
# include "avro_src/encoding.h"

# include "utilities/kaa_mem.h"

/*
 * AUTO-GENERATED CODE
 */




kaa_device_event_class_family_device_info_request_t* kaa_device_event_class_family_device_info_request_create()
{
    kaa_device_event_class_family_device_info_request_t* record = 
            (kaa_device_event_class_family_device_info_request_t*)KAA_CALLOC(1, sizeof(kaa_device_event_class_family_device_info_request_t));

    if (record) {
        record->destroy = kaa_data_destroy;
    }

    return record;
}

kaa_device_event_class_family_device_info_request_t* kaa_device_event_class_family_device_info_request_deserialize(avro_reader_t reader)
{
    kaa_device_event_class_family_device_info_request_t* record = 
            (kaa_device_event_class_family_device_info_request_t*)KAA_MALLOC(sizeof(kaa_device_event_class_family_device_info_request_t));

    if (record) {
        record->destroy = kaa_data_destroy;

    }

    return record;
}


# ifndef KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_C_
# define KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_C_
static void kaa_device_event_class_family_union_device_type_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_data_destroy(kaa_union->data);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_device_event_class_family_union_device_type_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                        int64_t primitive_value = *((kaa_device_event_class_family_device_type_t *)kaa_union->data);
                union_size += kaa_long_get_size(primitive_value);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_device_event_class_family_union_device_type_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                        avro_binary_encoding.write_long(writer, *((kaa_device_event_class_family_device_type_t *)kaa_union->data));
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_device_event_class_family_union_device_type_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_device_event_class_family_union_device_type_or_null_serialize;
        kaa_union->get_size = kaa_device_event_class_family_union_device_type_or_null_get_size;
        kaa_union->destroy = kaa_device_event_class_family_union_device_type_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_device_event_class_family_union_device_type_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_device_event_class_family_union_device_type_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_device_event_class_family_union_device_type_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_device_event_class_family_union_device_type_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}

kaa_union_t* kaa_device_event_class_family_union_device_type_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_device_event_class_family_union_device_type_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_BRANCH_0:
        {
                    kaa_union->data = kaa_long_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_C_


# ifndef KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_C_
# define KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_C_
static void kaa_device_event_class_family_union_string_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_string_destroy(kaa_union->data);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_device_event_class_family_union_string_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                union_size += kaa_string_get_size(kaa_union->data);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_device_event_class_family_union_string_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_string_serialize(writer, kaa_union->data);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_device_event_class_family_union_string_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_device_event_class_family_union_string_or_null_serialize;
        kaa_union->get_size = kaa_device_event_class_family_union_string_or_null_get_size;
        kaa_union->destroy = kaa_device_event_class_family_union_string_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_device_event_class_family_union_string_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_device_event_class_family_union_string_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_device_event_class_family_union_string_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_device_event_class_family_union_string_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}

kaa_union_t* kaa_device_event_class_family_union_string_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_device_event_class_family_union_string_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_string_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_C_


static void kaa_device_event_class_family_device_info_destroy(void* data)
{
    if (data) {
        kaa_device_event_class_family_device_info_t* record = (kaa_device_event_class_family_device_info_t*)data;

        if (record->device_type && record->device_type->destroy) {
            record->device_type->destroy(record->device_type);
        }
        if (record->model && record->model->destroy) {
            record->model->destroy(record->model);
        }
        if (record->manufacturer && record->manufacturer->destroy) {
            record->manufacturer->destroy(record->manufacturer);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_device_event_class_family_device_info_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_device_event_class_family_device_info_t* record = (kaa_device_event_class_family_device_info_t*)data;

        record->device_type->serialize(writer, record->device_type);
        record->model->serialize(writer, record->model);
        record->manufacturer->serialize(writer, record->manufacturer);
    }
}

static size_t kaa_device_event_class_family_device_info_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_device_event_class_family_device_info_t* record = (kaa_device_event_class_family_device_info_t*)data;

        record_size += record->device_type->get_size(record->device_type);
        record_size += record->model->get_size(record->model);
        record_size += record->manufacturer->get_size(record->manufacturer);

        return record_size;
    }

    return 0;
}

kaa_device_event_class_family_device_info_t* kaa_device_event_class_family_device_info_create()
{
    kaa_device_event_class_family_device_info_t* record = 
            (kaa_device_event_class_family_device_info_t*)KAA_CALLOC(1, sizeof(kaa_device_event_class_family_device_info_t));

    if (record) {
        record->serialize = kaa_device_event_class_family_device_info_serialize;
        record->get_size = kaa_device_event_class_family_device_info_get_size;
        record->destroy = kaa_device_event_class_family_device_info_destroy;
    }

    return record;
}

kaa_device_event_class_family_device_info_t* kaa_device_event_class_family_device_info_deserialize(avro_reader_t reader)
{
    kaa_device_event_class_family_device_info_t* record = 
            (kaa_device_event_class_family_device_info_t*)KAA_MALLOC(sizeof(kaa_device_event_class_family_device_info_t));

    if (record) {
        record->serialize = kaa_device_event_class_family_device_info_serialize;
        record->get_size = kaa_device_event_class_family_device_info_get_size;
        record->destroy = kaa_device_event_class_family_device_info_destroy;

        record->device_type = kaa_device_event_class_family_union_device_type_or_null_deserialize(reader);
        record->model = kaa_device_event_class_family_union_string_or_null_deserialize(reader);
        record->manufacturer = kaa_device_event_class_family_union_string_or_null_deserialize(reader);
    }

    return record;
}


# ifndef KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_C_
# define KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_C_
static void kaa_device_event_class_family_union_device_info_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;

        switch (kaa_union->type) {
        case KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_device_event_class_family_device_info_t* record = (kaa_device_event_class_family_device_info_t*)kaa_union->data;
                record->destroy(record);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_device_event_class_family_union_device_info_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        size_t union_size = kaa_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_device_event_class_family_device_info_t* record = (kaa_device_event_class_family_device_info_t*)kaa_union->data;
                union_size += record->get_size(record);
            }
            break;
        }
        default:
            break;
        }

        return union_size;
    }

    return 0;
}

static void kaa_device_event_class_family_union_device_info_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t*)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_device_event_class_family_device_info_t* record = (kaa_device_event_class_family_device_info_t*)kaa_union->data;
                record->serialize(writer, record);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t* kaa_device_event_class_family_union_device_info_or_null_create()
{
    kaa_union_t* kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_device_event_class_family_union_device_info_or_null_serialize;
        kaa_union->get_size = kaa_device_event_class_family_union_device_info_or_null_get_size;
        kaa_union->destroy = kaa_device_event_class_family_union_device_info_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t* kaa_device_event_class_family_union_device_info_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_device_event_class_family_union_device_info_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t* kaa_device_event_class_family_union_device_info_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_device_event_class_family_union_device_info_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}

kaa_union_t* kaa_device_event_class_family_union_device_info_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_device_event_class_family_union_device_info_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_BRANCH_0:
        {
            kaa_union->data = kaa_device_event_class_family_device_info_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_C_


static void kaa_device_event_class_family_device_info_response_destroy(void* data)
{
    if (data) {
        kaa_device_event_class_family_device_info_response_t* record = (kaa_device_event_class_family_device_info_response_t*)data;

        if (record->device_info && record->device_info->destroy) {
            record->device_info->destroy(record->device_info);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_device_event_class_family_device_info_response_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_device_event_class_family_device_info_response_t* record = (kaa_device_event_class_family_device_info_response_t*)data;

        record->device_info->serialize(writer, record->device_info);
    }
}

static size_t kaa_device_event_class_family_device_info_response_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_device_event_class_family_device_info_response_t* record = (kaa_device_event_class_family_device_info_response_t*)data;

        record_size += record->device_info->get_size(record->device_info);

        return record_size;
    }

    return 0;
}

kaa_device_event_class_family_device_info_response_t* kaa_device_event_class_family_device_info_response_create()
{
    kaa_device_event_class_family_device_info_response_t* record = 
            (kaa_device_event_class_family_device_info_response_t*)KAA_CALLOC(1, sizeof(kaa_device_event_class_family_device_info_response_t));

    if (record) {
        record->serialize = kaa_device_event_class_family_device_info_response_serialize;
        record->get_size = kaa_device_event_class_family_device_info_response_get_size;
        record->destroy = kaa_device_event_class_family_device_info_response_destroy;
    }

    return record;
}

kaa_device_event_class_family_device_info_response_t* kaa_device_event_class_family_device_info_response_deserialize(avro_reader_t reader)
{
    kaa_device_event_class_family_device_info_response_t* record = 
            (kaa_device_event_class_family_device_info_response_t*)KAA_MALLOC(sizeof(kaa_device_event_class_family_device_info_response_t));

    if (record) {
        record->serialize = kaa_device_event_class_family_device_info_response_serialize;
        record->get_size = kaa_device_event_class_family_device_info_response_get_size;
        record->destroy = kaa_device_event_class_family_device_info_response_destroy;

        record->device_info = kaa_device_event_class_family_union_device_info_or_null_deserialize(reader);
    }

    return record;
}


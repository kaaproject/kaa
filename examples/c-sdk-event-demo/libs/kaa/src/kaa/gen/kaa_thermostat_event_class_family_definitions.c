/*
 * Copyright 2014-2015 CyberVision, Inc.
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

# include <string.h>
# include "../platform/stdio.h"
# include "kaa_thermostat_event_class_family_definitions.h"
# include "../avro_src/avro/io.h"
# include "../avro_src/encoding.h"
# include "../utilities/kaa_mem.h"

/*
 * AUTO-GENERATED CODE
 */




kaa_thermostat_event_class_family_thermostat_info_request_t *kaa_thermostat_event_class_family_thermostat_info_request_create()
{
    kaa_thermostat_event_class_family_thermostat_info_request_t *record = 
            (kaa_thermostat_event_class_family_thermostat_info_request_t *)KAA_CALLOC(1, sizeof(kaa_thermostat_event_class_family_thermostat_info_request_t));

    if (record) {
        record->serialize = kaa_null_serialize;
        record->get_size = kaa_null_get_size;
        record->destroy = kaa_data_destroy;
    }

    return record;
}

kaa_thermostat_event_class_family_thermostat_info_request_t *kaa_thermostat_event_class_family_thermostat_info_request_deserialize(avro_reader_t reader)
{
    kaa_thermostat_event_class_family_thermostat_info_request_t *record = 
            (kaa_thermostat_event_class_family_thermostat_info_request_t *)KAA_MALLOC(sizeof(kaa_thermostat_event_class_family_thermostat_info_request_t));

    if (record) {
        record->serialize = kaa_null_serialize;
        record->get_size = kaa_null_get_size;
        record->destroy = kaa_data_destroy;

    }

    return record;
}


# ifndef KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_C_
# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_C_
static void kaa_thermostat_event_class_family_union_int_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t *)data;

        switch (kaa_union->type) {
        case KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_BRANCH_0:
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

static size_t kaa_thermostat_event_class_family_union_int_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t *)data;
        size_t union_size = avro_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                union_size += kaa_int_get_size(kaa_union->data);
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

static void kaa_thermostat_event_class_family_union_int_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t *)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_int_serialize(writer, kaa_union->data);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t *kaa_thermostat_event_class_family_union_int_or_null_create()
{
    kaa_union_t *kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_thermostat_event_class_family_union_int_or_null_serialize;
        kaa_union->get_size = kaa_thermostat_event_class_family_union_int_or_null_get_size;
        kaa_union->destroy = kaa_thermostat_event_class_family_union_int_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t *kaa_thermostat_event_class_family_union_int_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_thermostat_event_class_family_union_int_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t *kaa_thermostat_event_class_family_union_int_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_thermostat_event_class_family_union_int_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}

kaa_union_t *kaa_thermostat_event_class_family_union_int_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_thermostat_event_class_family_union_int_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_BRANCH_0: {
            kaa_union->data = kaa_int_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_C_


# ifndef KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_C_
# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_C_
static void kaa_thermostat_event_class_family_union_boolean_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t *)data;

        switch (kaa_union->type) {
        case KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_BRANCH_0:
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

static size_t kaa_thermostat_event_class_family_union_boolean_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t *)data;
        size_t union_size = avro_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                union_size += kaa_boolean_get_size(kaa_union->data);
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

static void kaa_thermostat_event_class_family_union_boolean_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t *)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_boolean_serialize(writer, kaa_union->data);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t *kaa_thermostat_event_class_family_union_boolean_or_null_create()
{
    kaa_union_t *kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_thermostat_event_class_family_union_boolean_or_null_serialize;
        kaa_union->get_size = kaa_thermostat_event_class_family_union_boolean_or_null_get_size;
        kaa_union->destroy = kaa_thermostat_event_class_family_union_boolean_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t *kaa_thermostat_event_class_family_union_boolean_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_thermostat_event_class_family_union_boolean_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t *kaa_thermostat_event_class_family_union_boolean_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_thermostat_event_class_family_union_boolean_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}

kaa_union_t *kaa_thermostat_event_class_family_union_boolean_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_thermostat_event_class_family_union_boolean_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_BRANCH_0: {
            kaa_union->data = kaa_boolean_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_C_


static void kaa_thermostat_event_class_family_thermostat_info_destroy(void *data)
{
    if (data) {
        kaa_thermostat_event_class_family_thermostat_info_t *record = (kaa_thermostat_event_class_family_thermostat_info_t *)data;

        if (record->degree && record->degree->destroy) {
            record->degree->destroy(record->degree);
        }
        if (record->target_degree && record->target_degree->destroy) {
            record->target_degree->destroy(record->target_degree);
        }
        if (record->is_set_manually && record->is_set_manually->destroy) {
            record->is_set_manually->destroy(record->is_set_manually);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_thermostat_event_class_family_thermostat_info_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_thermostat_event_class_family_thermostat_info_t *record = (kaa_thermostat_event_class_family_thermostat_info_t *)data;

        record->degree->serialize(writer, record->degree);
        record->target_degree->serialize(writer, record->target_degree);
        record->is_set_manually->serialize(writer, record->is_set_manually);
    }
}

static size_t kaa_thermostat_event_class_family_thermostat_info_get_size(void *data)
{
    if (data) {
        size_t record_size = 0;
        kaa_thermostat_event_class_family_thermostat_info_t *record = (kaa_thermostat_event_class_family_thermostat_info_t *)data;

        record_size += record->degree->get_size(record->degree);
        record_size += record->target_degree->get_size(record->target_degree);
        record_size += record->is_set_manually->get_size(record->is_set_manually);

        return record_size;
    }

    return 0;
}

kaa_thermostat_event_class_family_thermostat_info_t *kaa_thermostat_event_class_family_thermostat_info_create()
{
    kaa_thermostat_event_class_family_thermostat_info_t *record = 
            (kaa_thermostat_event_class_family_thermostat_info_t *)KAA_CALLOC(1, sizeof(kaa_thermostat_event_class_family_thermostat_info_t));

    if (record) {
        record->serialize = kaa_thermostat_event_class_family_thermostat_info_serialize;
        record->get_size = kaa_thermostat_event_class_family_thermostat_info_get_size;
        record->destroy = kaa_thermostat_event_class_family_thermostat_info_destroy;
    }

    return record;
}

kaa_thermostat_event_class_family_thermostat_info_t *kaa_thermostat_event_class_family_thermostat_info_deserialize(avro_reader_t reader)
{
    kaa_thermostat_event_class_family_thermostat_info_t *record = 
            (kaa_thermostat_event_class_family_thermostat_info_t *)KAA_MALLOC(sizeof(kaa_thermostat_event_class_family_thermostat_info_t));

    if (record) {
        record->serialize = kaa_thermostat_event_class_family_thermostat_info_serialize;
        record->get_size = kaa_thermostat_event_class_family_thermostat_info_get_size;
        record->destroy = kaa_thermostat_event_class_family_thermostat_info_destroy;

        record->degree = kaa_thermostat_event_class_family_union_int_or_null_deserialize(reader);
        record->target_degree = kaa_thermostat_event_class_family_union_int_or_null_deserialize(reader);
        record->is_set_manually = kaa_thermostat_event_class_family_union_boolean_or_null_deserialize(reader);
    }

    return record;
}


# ifndef KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_C_
# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_C_
static void kaa_thermostat_event_class_family_union_thermostat_info_or_null_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t *)data;

        switch (kaa_union->type) {
        case KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_thermostat_event_class_family_thermostat_info_t *record = (kaa_thermostat_event_class_family_thermostat_info_t *)kaa_union->data;
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

static size_t kaa_thermostat_event_class_family_union_thermostat_info_or_null_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t *)data;
        size_t union_size = avro_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_thermostat_event_class_family_thermostat_info_t * record = (kaa_thermostat_event_class_family_thermostat_info_t *)kaa_union->data;
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

static void kaa_thermostat_event_class_family_union_thermostat_info_or_null_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t *)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_BRANCH_0:
        {
            if (kaa_union->data) {
                kaa_thermostat_event_class_family_thermostat_info_t * record = (kaa_thermostat_event_class_family_thermostat_info_t *)kaa_union->data;
                record->serialize(writer, record);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t *kaa_thermostat_event_class_family_union_thermostat_info_or_null_create()
{
    kaa_union_t *kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_thermostat_event_class_family_union_thermostat_info_or_null_serialize;
        kaa_union->get_size = kaa_thermostat_event_class_family_union_thermostat_info_or_null_get_size;
        kaa_union->destroy = kaa_thermostat_event_class_family_union_thermostat_info_or_null_destroy;
    }

    return kaa_union; 
}

kaa_union_t *kaa_thermostat_event_class_family_union_thermostat_info_or_null_branch_0_create()
{
    kaa_union_t *kaa_union = kaa_thermostat_event_class_family_union_thermostat_info_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t *kaa_thermostat_event_class_family_union_thermostat_info_or_null_branch_1_create()
{
    kaa_union_t *kaa_union = kaa_thermostat_event_class_family_union_thermostat_info_or_null_create();
    if (kaa_union) {
        kaa_union->type = KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_BRANCH_1;
    }
    return kaa_union;
}

kaa_union_t *kaa_thermostat_event_class_family_union_thermostat_info_or_null_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_thermostat_event_class_family_union_thermostat_info_or_null_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_BRANCH_0: {
            kaa_union->data = kaa_thermostat_event_class_family_thermostat_info_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_C_


static void kaa_thermostat_event_class_family_thermostat_info_response_destroy(void *data)
{
    if (data) {
        kaa_thermostat_event_class_family_thermostat_info_response_t *record = (kaa_thermostat_event_class_family_thermostat_info_response_t *)data;

        if (record->thermostat_info && record->thermostat_info->destroy) {
            record->thermostat_info->destroy(record->thermostat_info);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_thermostat_event_class_family_thermostat_info_response_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_thermostat_event_class_family_thermostat_info_response_t *record = (kaa_thermostat_event_class_family_thermostat_info_response_t *)data;

        record->thermostat_info->serialize(writer, record->thermostat_info);
    }
}

static size_t kaa_thermostat_event_class_family_thermostat_info_response_get_size(void *data)
{
    if (data) {
        size_t record_size = 0;
        kaa_thermostat_event_class_family_thermostat_info_response_t *record = (kaa_thermostat_event_class_family_thermostat_info_response_t *)data;

        record_size += record->thermostat_info->get_size(record->thermostat_info);

        return record_size;
    }

    return 0;
}

kaa_thermostat_event_class_family_thermostat_info_response_t *kaa_thermostat_event_class_family_thermostat_info_response_create()
{
    kaa_thermostat_event_class_family_thermostat_info_response_t *record = 
            (kaa_thermostat_event_class_family_thermostat_info_response_t *)KAA_CALLOC(1, sizeof(kaa_thermostat_event_class_family_thermostat_info_response_t));

    if (record) {
        record->serialize = kaa_thermostat_event_class_family_thermostat_info_response_serialize;
        record->get_size = kaa_thermostat_event_class_family_thermostat_info_response_get_size;
        record->destroy = kaa_thermostat_event_class_family_thermostat_info_response_destroy;
    }

    return record;
}

kaa_thermostat_event_class_family_thermostat_info_response_t *kaa_thermostat_event_class_family_thermostat_info_response_deserialize(avro_reader_t reader)
{
    kaa_thermostat_event_class_family_thermostat_info_response_t *record = 
            (kaa_thermostat_event_class_family_thermostat_info_response_t *)KAA_MALLOC(sizeof(kaa_thermostat_event_class_family_thermostat_info_response_t));

    if (record) {
        record->serialize = kaa_thermostat_event_class_family_thermostat_info_response_serialize;
        record->get_size = kaa_thermostat_event_class_family_thermostat_info_response_get_size;
        record->destroy = kaa_thermostat_event_class_family_thermostat_info_response_destroy;

        record->thermostat_info = kaa_thermostat_event_class_family_union_thermostat_info_or_null_deserialize(reader);
    }

    return record;
}


static void kaa_thermostat_event_class_family_change_degree_request_destroy(void *data)
{
    if (data) {
        kaa_thermostat_event_class_family_change_degree_request_t *record = (kaa_thermostat_event_class_family_change_degree_request_t *)data;

        if (record->degree && record->degree->destroy) {
            record->degree->destroy(record->degree);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_thermostat_event_class_family_change_degree_request_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_thermostat_event_class_family_change_degree_request_t *record = (kaa_thermostat_event_class_family_change_degree_request_t *)data;

        record->degree->serialize(writer, record->degree);
    }
}

static size_t kaa_thermostat_event_class_family_change_degree_request_get_size(void *data)
{
    if (data) {
        size_t record_size = 0;
        kaa_thermostat_event_class_family_change_degree_request_t *record = (kaa_thermostat_event_class_family_change_degree_request_t *)data;

        record_size += record->degree->get_size(record->degree);

        return record_size;
    }

    return 0;
}

kaa_thermostat_event_class_family_change_degree_request_t *kaa_thermostat_event_class_family_change_degree_request_create()
{
    kaa_thermostat_event_class_family_change_degree_request_t *record = 
            (kaa_thermostat_event_class_family_change_degree_request_t *)KAA_CALLOC(1, sizeof(kaa_thermostat_event_class_family_change_degree_request_t));

    if (record) {
        record->serialize = kaa_thermostat_event_class_family_change_degree_request_serialize;
        record->get_size = kaa_thermostat_event_class_family_change_degree_request_get_size;
        record->destroy = kaa_thermostat_event_class_family_change_degree_request_destroy;
    }

    return record;
}

kaa_thermostat_event_class_family_change_degree_request_t *kaa_thermostat_event_class_family_change_degree_request_deserialize(avro_reader_t reader)
{
    kaa_thermostat_event_class_family_change_degree_request_t *record = 
            (kaa_thermostat_event_class_family_change_degree_request_t *)KAA_MALLOC(sizeof(kaa_thermostat_event_class_family_change_degree_request_t));

    if (record) {
        record->serialize = kaa_thermostat_event_class_family_change_degree_request_serialize;
        record->get_size = kaa_thermostat_event_class_family_change_degree_request_get_size;
        record->destroy = kaa_thermostat_event_class_family_change_degree_request_destroy;

        record->degree = kaa_thermostat_event_class_family_union_int_or_null_deserialize(reader);
    }

    return record;
}


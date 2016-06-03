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

# include <inttypes.h>
# include <string.h>
# include <platform/stdio.h>
# include "kaa_configuration_gen.h"
# include "avro_src/avro/io.h"
# include "avro_src/encoding.h"
# include "utilities/kaa_mem.h"

/*
 * AUTO-GENERATED CODE
 */


# ifndef KAA_UNION_NULL_OR_FIXED_C_
# define KAA_UNION_NULL_OR_FIXED_C_
static void kaa_union_null_or_fixed_destroy(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t *)data;

        switch (kaa_union->type) {
        case KAA_UNION_NULL_OR_FIXED_BRANCH_1:
        {
            if (kaa_union->data) {
                kaa_bytes_destroy(kaa_union->data);
            }
            break;
        }
        default:
            break;
        }

        kaa_data_destroy(kaa_union);
    }
}

static size_t kaa_union_null_or_fixed_get_size(void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t *)data;
        size_t union_size = avro_long_get_size(kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_NULL_OR_FIXED_BRANCH_1:
        {
            if (kaa_union->data) {
                union_size += kaa_bytes_get_size(kaa_union->data);
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

static void kaa_union_null_or_fixed_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_union_t *kaa_union = (kaa_union_t *)data;
        avro_binary_encoding.write_long(writer, kaa_union->type);

        switch (kaa_union->type) {
        case KAA_UNION_NULL_OR_FIXED_BRANCH_1:
        {
            if (kaa_union->data) {
                kaa_bytes_serialize(writer, kaa_union->data);
            }
            break;
        }
        default:
            break;
        }
    }
}
static kaa_union_t *kaa_union_null_or_fixed_create(void)
{
    kaa_union_t *kaa_union = KAA_CALLOC(1, sizeof(kaa_union_t));

    if (kaa_union) {
        kaa_union->serialize = kaa_union_null_or_fixed_serialize;
        kaa_union->get_size = kaa_union_null_or_fixed_get_size;
        kaa_union->destroy = kaa_union_null_or_fixed_destroy;
    }

    return kaa_union; 
}

kaa_union_t *kaa_union_null_or_fixed_branch_0_create(void)
{
    kaa_union_t *kaa_union = kaa_union_null_or_fixed_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_NULL_OR_FIXED_BRANCH_0;
    }
    return kaa_union;
}

kaa_union_t *kaa_union_null_or_fixed_branch_1_create(void)
{
    kaa_union_t *kaa_union = kaa_union_null_or_fixed_create();
    if (kaa_union) {
        kaa_union->type = KAA_UNION_NULL_OR_FIXED_BRANCH_1;
    }
    return kaa_union;
}

kaa_union_t *kaa_union_null_or_fixed_deserialize(avro_reader_t reader)
{
    kaa_union_t *kaa_union = kaa_union_null_or_fixed_create();

    if (kaa_union) {
        int64_t branch;
        avro_binary_encoding.read_long(reader, &branch);
        kaa_union->type = branch;

        switch (kaa_union->type) {
        case KAA_UNION_NULL_OR_FIXED_BRANCH_1:
        {
            kaa_union->data = kaa_bytes_deserialize(reader);
            break;
        }
        default:
            break;
        }
    }

    return kaa_union;
}
# endif // KAA_UNION_NULL_OR_FIXED_C_


static void kaa_configuration_root_record_destroy(void *data)
{
    if (data) {
        kaa_configuration_root_record_t *record = (kaa_configuration_root_record_t *)data;

        kaa_string_destroy(record->data);
        if (record->__uuid && record->__uuid->destroy) {
            record->__uuid->destroy(record->__uuid);
        }
        kaa_data_destroy(record);
    }
}

static void kaa_configuration_root_record_serialize(avro_writer_t writer, void *data)
{
    if (data) {
        kaa_configuration_root_record_t *record = (kaa_configuration_root_record_t *)data;

        kaa_string_serialize(writer, record->data);
        record->__uuid->serialize(writer, record->__uuid);
    }
}

static size_t kaa_configuration_root_record_get_size(void *data)
{
    if (data) {
        size_t record_size = 0;
        kaa_configuration_root_record_t *record = (kaa_configuration_root_record_t *)data;

        record_size += kaa_string_get_size(record->data);
        record_size += record->__uuid->get_size(record->__uuid);

        return record_size;
    }

    return 0;
}

kaa_configuration_root_record_t *kaa_configuration_root_record_create(void)
{
    kaa_configuration_root_record_t *record = 
            (kaa_configuration_root_record_t *)KAA_CALLOC(1, sizeof(kaa_configuration_root_record_t));

    if (record) {
        record->serialize = kaa_configuration_root_record_serialize;
        record->get_size = kaa_configuration_root_record_get_size;
        record->destroy = kaa_configuration_root_record_destroy;
    }

    return record;
}

kaa_configuration_root_record_t *kaa_configuration_root_record_deserialize(avro_reader_t reader)
{
    kaa_configuration_root_record_t *record = 
            (kaa_configuration_root_record_t *)KAA_MALLOC(sizeof(kaa_configuration_root_record_t));

    if (record) {
        record->serialize = kaa_configuration_root_record_serialize;
        record->get_size = kaa_configuration_root_record_get_size;
        record->destroy = kaa_configuration_root_record_destroy;

        record->data = kaa_string_deserialize(reader);
        record->__uuid = kaa_union_null_or_fixed_deserialize(reader);
    }

    return record;
}


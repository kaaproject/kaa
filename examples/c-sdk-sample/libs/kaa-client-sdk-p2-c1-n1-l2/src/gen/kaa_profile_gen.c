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

# include "kaa_profile_gen.h"

# include <stdio.h>
# include <string.h>

# include "avro_src/avro/io.h"
# include "avro_src/encoding.h"

# include "utilities/kaa_mem.h"

/*
 * AUTO-GENERATED CODE
 */


static void kaa_profile_profile_destroy(void* data)
{
    if (data) {
        kaa_profile_profile_t* record = (kaa_profile_profile_t*)data;

        kaa_string_destroy(record->id);
        kaa_string_destroy(record->os_version);
        kaa_string_destroy(record->build);
        kaa_data_destroy(record);
    }
}

static void kaa_profile_profile_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_profile_profile_t* record = (kaa_profile_profile_t*)data;

        kaa_string_serialize(writer, record->id);
                avro_binary_encoding.write_long(writer, record->os);
        kaa_string_serialize(writer, record->os_version);
        kaa_string_serialize(writer, record->build);
    }
}

static size_t kaa_profile_profile_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_profile_profile_t* record = (kaa_profile_profile_t*)data;

        record_size += kaa_string_get_size(record->id);
        record_size += kaa_long_get_size((int64_t)record->os);
        record_size += kaa_string_get_size(record->os_version);
        record_size += kaa_string_get_size(record->build);

        return record_size;
    }

    return 0;
}

kaa_profile_profile_t* kaa_profile_profile_create()
{
    kaa_profile_profile_t* record = 
            (kaa_profile_profile_t*)KAA_CALLOC(1, sizeof(kaa_profile_profile_t));

    if (record) {
        record->serialize = kaa_profile_profile_serialize;
        record->get_size = kaa_profile_profile_get_size;
        record->destroy = kaa_profile_profile_destroy;
    }

    return record;
}

kaa_profile_profile_t* kaa_profile_profile_deserialize(avro_reader_t reader)
{
    kaa_profile_profile_t* record = 
            (kaa_profile_profile_t*)KAA_MALLOC(sizeof(kaa_profile_profile_t));

    if (record) {
        record->serialize = kaa_profile_profile_serialize;
        record->get_size = kaa_profile_profile_get_size;
        record->destroy = kaa_profile_profile_destroy;

        record->id = kaa_string_deserialize(reader);
        int64_t os_value;
        avro_binary_encoding.read_long(reader, &os_value);
        record->os = os_value;
        record->os_version = kaa_string_deserialize(reader);
        record->build = kaa_string_deserialize(reader);
    }

    return record;
}


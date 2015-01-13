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

# include "kaa_logging_gen.h"

# include <stdio.h>
# include <string.h>

# include "avro_src/avro/io.h"
# include "avro_src/encoding.h"

# include "utilities/kaa_mem.h"

/*
 * AUTO-GENERATED CODE
 */


static void kaa_logging_log_data_destroy(void* data)
{
    if (data) {
        kaa_logging_log_data_t* record = (kaa_logging_log_data_t*)data;

        kaa_string_destroy(record->tag);
        kaa_string_destroy(record->message);
        kaa_data_destroy(record);
    }
}

static void kaa_logging_log_data_serialize(avro_writer_t writer, void* data)
{
    if (data) {
        kaa_logging_log_data_t* record = (kaa_logging_log_data_t*)data;

                avro_binary_encoding.write_long(writer, record->level);
        kaa_string_serialize(writer, record->tag);
        kaa_string_serialize(writer, record->message);
    }
}

static size_t kaa_logging_log_data_get_size(void* data)
{
    if (data) {
        size_t record_size = 0;
        kaa_logging_log_data_t* record = (kaa_logging_log_data_t*)data;

        record_size += kaa_long_get_size((int64_t)record->level);
        record_size += kaa_string_get_size(record->tag);
        record_size += kaa_string_get_size(record->message);

        return record_size;
    }

    return 0;
}

kaa_logging_log_data_t* kaa_logging_log_data_create()
{
    kaa_logging_log_data_t* record = 
            (kaa_logging_log_data_t*)KAA_CALLOC(1, sizeof(kaa_logging_log_data_t));

    if (record) {
        record->serialize = kaa_logging_log_data_serialize;
        record->get_size = kaa_logging_log_data_get_size;
        record->destroy = kaa_logging_log_data_destroy;
    }

    return record;
}

kaa_logging_log_data_t* kaa_logging_log_data_deserialize(avro_reader_t reader)
{
    kaa_logging_log_data_t* record = 
            (kaa_logging_log_data_t*)KAA_MALLOC(sizeof(kaa_logging_log_data_t));

    if (record) {
        record->serialize = kaa_logging_log_data_serialize;
        record->get_size = kaa_logging_log_data_get_size;
        record->destroy = kaa_logging_log_data_destroy;

        int64_t level_value;
        avro_binary_encoding.read_long(reader, &level_value);
        record->level = level_value;
        record->tag = kaa_string_deserialize(reader);
        record->message = kaa_string_deserialize(reader);
    }

    return record;
}


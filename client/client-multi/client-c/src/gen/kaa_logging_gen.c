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

#include "kaa_logging_gen.h"

#include <stdio.h>
#include <string.h>

#include "avro_src/avro/io.h"
#include "avro_src/encoding.h"

#include "kaa_mem.h"

/*
 * AUTO-GENERATED CODE
 */



static void kaa_destroy_test_log_record(void* data)
{
    kaa_test_log_record_t* record = (kaa_test_log_record_t*)data;

    KAA_FREE(record->data);
}
static size_t kaa_get_size_test_log_record(void* data)
{
    size_t record_size = 0;
    kaa_test_log_record_t* record = (kaa_test_log_record_t*)data;

    record_size += kaa_get_size_string(record->data);

    return record_size;
}

static void kaa_serialize_test_log_record(avro_writer_t writer, void* data)
{
    kaa_test_log_record_t* record = (kaa_test_log_record_t*)data;

    avro_binary_encoding.write_string(writer, record->data);
}

kaa_test_log_record_t* kaa_create_test_log_record()
{
    kaa_test_log_record_t* record = (kaa_test_log_record_t *) KAA_MALLOC(sizeof(kaa_test_log_record_t));
    record->serialize = kaa_serialize_test_log_record;
    record->get_size = kaa_get_size_test_log_record;
    record->destroy = kaa_destroy_test_log_record;
    return record;
}

kaa_test_log_record_t* kaa_deserialize_test_log_record(avro_reader_t reader)
{
    kaa_test_log_record_t* record = (kaa_test_log_record_t *) KAA_MALLOC(sizeof(kaa_test_log_record_t));
    record->serialize = kaa_serialize_test_log_record;
    record->get_size = kaa_get_size_test_log_record;
    record->destroy = kaa_destroy_test_log_record;
    
        int64_t data_size;
    avro_binary_encoding.read_string(reader, &record->data, &data_size);
        
    return record;
}


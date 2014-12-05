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

#include "kaa_profile_gen.h"

#include <stdio.h>
#include <string.h>

#include "avro_src/avro/io.h"
#include "avro_src/encoding.h"

#include "kaa_mem.h"

/*
 * AUTO-GENERATED CODE
 */

static void kaa_basic_endpoint_profile_test_destroy(void* data)
{
    kaa_profile_basic_endpoint_profile_test_t* record = (kaa_profile_basic_endpoint_profile_test_t*)data;

    KAA_FREE(record->profile_body);
}
static size_t kaa_basic_endpoint_profile_test_get_size(void* data)
{
    size_t record_size = 0;
    kaa_profile_basic_endpoint_profile_test_t* record = (kaa_profile_basic_endpoint_profile_test_t*)data;

    record_size += kaa_string_get_size(record->profile_body);

    return record_size;
}

static void kaa_basic_endpoint_profile_test_serialize(avro_writer_t writer, void* data)
{
    kaa_profile_basic_endpoint_profile_test_t* record = (kaa_profile_basic_endpoint_profile_test_t*)data;

    avro_binary_encoding.write_string(writer, record->profile_body);
}

kaa_profile_basic_endpoint_profile_test_t* kaa_profile_basic_endpoint_profile_test_create()
{
    kaa_profile_basic_endpoint_profile_test_t* record = (kaa_profile_basic_endpoint_profile_test_t *) KAA_MALLOC(sizeof(kaa_profile_basic_endpoint_profile_test_t));
    record->serialize = kaa_basic_endpoint_profile_test_serialize;
    record->get_size = kaa_basic_endpoint_profile_test_get_size;
    record->destroy = kaa_basic_endpoint_profile_test_destroy;
    return record;
}

kaa_profile_basic_endpoint_profile_test_t* kaa_basic_endpoint_profile_test_deserialize(avro_reader_t reader)
{
    kaa_profile_basic_endpoint_profile_test_t* record = (kaa_profile_basic_endpoint_profile_test_t *) KAA_MALLOC(sizeof(kaa_profile_basic_endpoint_profile_test_t));
    record->serialize = kaa_basic_endpoint_profile_test_serialize;
    record->get_size = kaa_basic_endpoint_profile_test_get_size;
    record->destroy = kaa_basic_endpoint_profile_test_destroy;
    
    int64_t profile_body_size;
    avro_binary_encoding.read_string(reader, &record->profile_body, &profile_body_size);
        
    return record;
}


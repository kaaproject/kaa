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

#ifndef KAA_PROFILE_GEN_H_
#define KAA_PROFILE_GEN_H_

#ifdef __cplusplus
extern "C" {
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

#include "kaa_common_schema.h"
#include "kaa_list.h"

typedef struct kaa_profile_basic_endpoint_profile_test_t_ {
    char* profile_body; 

    serialize serialize;
    get_size  get_size;
    destruct  destruct;
} kaa_profile_basic_endpoint_profile_test_t;

kaa_profile_basic_endpoint_profile_test_t* kaa_profile_create_basic_endpoint_profile_test();
kaa_profile_basic_endpoint_profile_test_t* kaa_profile_deserialize_basic_endpoint_profile_test(avro_reader_t reader);

CLOSE_EXTERN
#endif

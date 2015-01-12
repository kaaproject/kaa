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

# ifndef KAA_DEVICE_EVENT_CLASS_FAMILY_DEFINITIONS_H_
# define KAA_DEVICE_EVENT_CLASS_FAMILY_DEFINITIONS_H_

# include "kaa_common_schema.h"
# include "collections/kaa_list.h"

# ifdef __cplusplus
extern "C" {
# endif


typedef struct {

    destroy_fn   destroy;
} kaa_device_event_class_family_device_info_request_t;

kaa_device_event_class_family_device_info_request_t* kaa_device_event_class_family_device_info_request_create();
kaa_device_event_class_family_device_info_request_t* kaa_device_event_class_family_device_info_request_deserialize(avro_reader_t reader);



typedef enum kaa_device_event_class_family_device_type_t_ {
    ENUM_DEVICE_TYPE_THERMOSTAT,
    ENUM_DEVICE_TYPE_TV,
    ENUM_DEVICE_TYPE_SOUND_SYSTEM,
    ENUM_DEVICE_TYPE_LAMP,
} kaa_device_event_class_family_device_type_t;

#ifdef GENC_ENUM_DEFINE_ALIASES
#define THERMOSTAT ENUM_DEVICE_TYPE_THERMOSTAT
#define TV ENUM_DEVICE_TYPE_TV
#define SOUND_SYSTEM ENUM_DEVICE_TYPE_SOUND_SYSTEM
#define LAMP ENUM_DEVICE_TYPE_LAMP
# endif // GENC_ENUM_DEFINE_ALIASES

#ifdef GENC_ENUM_STRING_LITERALS
const char* KAA_DEVICE_EVENT_CLASS_FAMILY_DEVICE_TYPE_SYMBOLS[4] = {
    "THERMOSTAT",
    "TV",
    "SOUND_SYSTEM",
    "LAMP"};
# endif // GENC_ENUM_STRING_LITERALS


# ifndef KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_H_
# define KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_H_

# define KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_BRANCH_0 0
# define KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_device_event_class_family_union_device_type_or_null_branch_0_create();
kaa_union_t* kaa_device_event_class_family_union_device_type_or_null_branch_1_create();

kaa_union_t* kaa_device_event_class_family_union_device_type_or_null_deserialize(avro_reader_t reader);

# endif // KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_TYPE_OR_NULL_H_


# ifndef KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_H_
# define KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_H_

# define KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_BRANCH_0 0
# define KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_device_event_class_family_union_string_or_null_branch_0_create();
kaa_union_t* kaa_device_event_class_family_union_string_or_null_branch_1_create();

kaa_union_t* kaa_device_event_class_family_union_string_or_null_deserialize(avro_reader_t reader);

# endif // KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_STRING_OR_NULL_H_


typedef struct {
    kaa_union_t* device_type;
    kaa_union_t* model;
    kaa_union_t* manufacturer;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_device_event_class_family_device_info_t;

kaa_device_event_class_family_device_info_t* kaa_device_event_class_family_device_info_create();
kaa_device_event_class_family_device_info_t* kaa_device_event_class_family_device_info_deserialize(avro_reader_t reader);


# ifndef KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_H_
# define KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_H_

# define KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_BRANCH_0 0
# define KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_BRANCH_1 1

kaa_union_t* kaa_device_event_class_family_union_device_info_or_null_branch_0_create();
kaa_union_t* kaa_device_event_class_family_union_device_info_or_null_branch_1_create();

kaa_union_t* kaa_device_event_class_family_union_device_info_or_null_deserialize(avro_reader_t reader);

# endif // KAA_DEVICE_EVENT_CLASS_FAMILY_UNION_DEVICE_INFO_OR_NULL_H_


typedef struct {
    kaa_union_t* device_info;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_device_event_class_family_device_info_response_t;

kaa_device_event_class_family_device_info_response_t* kaa_device_event_class_family_device_info_response_create();
kaa_device_event_class_family_device_info_response_t* kaa_device_event_class_family_device_info_response_deserialize(avro_reader_t reader);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif
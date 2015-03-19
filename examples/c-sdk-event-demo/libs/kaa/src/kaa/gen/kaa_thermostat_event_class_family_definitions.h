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

# ifndef KAA_THERMOSTAT_EVENT_CLASS_FAMILY_DEFINITIONS_H_
# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_DEFINITIONS_H_

# include "../kaa_common_schema.h"
# include "../collections/kaa_list.h"

# ifdef __cplusplus
extern "C" {
# endif


typedef struct {

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_thermostat_event_class_family_thermostat_info_request_t;

kaa_thermostat_event_class_family_thermostat_info_request_t *kaa_thermostat_event_class_family_thermostat_info_request_create();
kaa_thermostat_event_class_family_thermostat_info_request_t *kaa_thermostat_event_class_family_thermostat_info_request_deserialize(avro_reader_t reader);


# ifndef KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_H_
# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_H_

# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_BRANCH_0    0
# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_BRANCH_1    1

kaa_union_t *kaa_thermostat_event_class_family_union_int_or_null_branch_0_create();
kaa_union_t *kaa_thermostat_event_class_family_union_int_or_null_branch_1_create();

kaa_union_t *kaa_thermostat_event_class_family_union_int_or_null_deserialize(avro_reader_t reader);

# endif // KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_INT_OR_NULL_H_


# ifndef KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_H_
# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_H_

# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_BRANCH_0    0
# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_BRANCH_1    1

kaa_union_t *kaa_thermostat_event_class_family_union_boolean_or_null_branch_0_create();
kaa_union_t *kaa_thermostat_event_class_family_union_boolean_or_null_branch_1_create();

kaa_union_t *kaa_thermostat_event_class_family_union_boolean_or_null_deserialize(avro_reader_t reader);

# endif // KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_BOOLEAN_OR_NULL_H_


typedef struct {
    kaa_union_t * degree;
    kaa_union_t * target_degree;
    kaa_union_t * is_set_manually;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_thermostat_event_class_family_thermostat_info_t;

kaa_thermostat_event_class_family_thermostat_info_t *kaa_thermostat_event_class_family_thermostat_info_create();
kaa_thermostat_event_class_family_thermostat_info_t *kaa_thermostat_event_class_family_thermostat_info_deserialize(avro_reader_t reader);


# ifndef KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_H_
# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_H_

# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_BRANCH_0    0
# define KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_BRANCH_1    1

kaa_union_t *kaa_thermostat_event_class_family_union_thermostat_info_or_null_branch_0_create();
kaa_union_t *kaa_thermostat_event_class_family_union_thermostat_info_or_null_branch_1_create();

kaa_union_t *kaa_thermostat_event_class_family_union_thermostat_info_or_null_deserialize(avro_reader_t reader);

# endif // KAA_THERMOSTAT_EVENT_CLASS_FAMILY_UNION_THERMOSTAT_INFO_OR_NULL_H_


typedef struct {
    kaa_union_t * thermostat_info;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_thermostat_event_class_family_thermostat_info_response_t;

kaa_thermostat_event_class_family_thermostat_info_response_t *kaa_thermostat_event_class_family_thermostat_info_response_create();
kaa_thermostat_event_class_family_thermostat_info_response_t *kaa_thermostat_event_class_family_thermostat_info_response_deserialize(avro_reader_t reader);


typedef struct {
    kaa_union_t * degree;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_thermostat_event_class_family_change_degree_request_t;

kaa_thermostat_event_class_family_change_degree_request_t *kaa_thermostat_event_class_family_change_degree_request_create();
kaa_thermostat_event_class_family_change_degree_request_t *kaa_thermostat_event_class_family_change_degree_request_deserialize(avro_reader_t reader);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif
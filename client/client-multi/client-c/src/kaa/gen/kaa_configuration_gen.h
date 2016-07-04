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

# ifndef KAA_CONFIGURATION_GEN_H_
# define KAA_CONFIGURATION_GEN_H_

# include "kaa_common_schema.h"
# include "collections/kaa_list.h"

# ifdef __cplusplus
extern "C" {
# endif


# ifndef KAA_UNION_NULL_OR_FIXED_H_
# define KAA_UNION_NULL_OR_FIXED_H_

# define KAA_UNION_NULL_OR_FIXED_BRANCH_0    0
# define KAA_UNION_NULL_OR_FIXED_BRANCH_1    1

kaa_union_t *kaa_union_null_or_fixed_branch_0_create(void);
kaa_union_t *kaa_union_null_or_fixed_branch_1_create(void);

kaa_union_t *kaa_union_null_or_fixed_deserialize(avro_reader_t reader);

# endif // KAA_UNION_NULL_OR_FIXED_H_


typedef struct {
    kaa_string_t * data;
    kaa_union_t * __uuid;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_configuration_root_record_t;

kaa_configuration_root_record_t *kaa_configuration_root_record_create(void);
kaa_configuration_root_record_t *kaa_configuration_root_record_deserialize(avro_reader_t reader);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif

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

# ifndef KAA_PROFILE_GEN_H_
# define KAA_PROFILE_GEN_H_

# include "../kaa_common_schema.h"
# include "../collections/kaa_list.h"

# ifdef __cplusplus
extern "C" {
# endif



typedef enum kaa_profile_os_t_ {
    ENUM_OS_Android,
    ENUM_OS_iOS,
    ENUM_OS_Linux,
} kaa_profile_os_t;

#ifdef GENC_ENUM_DEFINE_ALIASES
#define Android ENUM_OS_Android
#define iOS ENUM_OS_iOS
#define Linux ENUM_OS_Linux
# endif // GENC_ENUM_DEFINE_ALIASES

#ifdef GENC_ENUM_STRING_LITERALS
const char* KAA_PROFILE_OS_SYMBOLS[3] = {
    "Android",
    "iOS",
    "Linux"};
# endif // GENC_ENUM_STRING_LITERALS


typedef struct {
    kaa_string_t* id;
    kaa_profile_os_t os;
    kaa_string_t* os_version;
    kaa_string_t* build;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_profile_profile_t;

kaa_profile_profile_t* kaa_profile_profile_create();
kaa_profile_profile_t* kaa_profile_profile_deserialize(avro_reader_t reader);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif
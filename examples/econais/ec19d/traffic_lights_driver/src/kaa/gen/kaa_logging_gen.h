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

# ifndef KAA_LOGGING_GEN_H_
# define KAA_LOGGING_GEN_H_

# include "../kaa_common_schema.h"
# include "../collections/kaa_list.h"

# ifdef __cplusplus
extern "C" {
# endif



typedef enum {
    ENUM_EVENT_TYPE_BUTTON,
} kaa_logging_event_type_t;

#ifdef GENC_ENUM_DEFINE_ALIASES
#define BUTTON ENUM_EVENT_TYPE_BUTTON
# endif // GENC_ENUM_DEFINE_ALIASES

#ifdef GENC_ENUM_STRING_LITERALS
const char* KAA_LOGGING_EVENT_TYPE_SYMBOLS[1] = {
    "BUTTON"};
# endif // GENC_ENUM_STRING_LITERALS


typedef struct {
    kaa_logging_event_type_t event_type;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_logging_traffic_lights_log_t;

kaa_logging_traffic_lights_log_t *kaa_logging_traffic_lights_log_create();
kaa_logging_traffic_lights_log_t *kaa_logging_traffic_lights_log_deserialize(avro_reader_t reader);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif
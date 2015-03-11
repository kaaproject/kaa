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



typedef enum kaa_logging_level_t_ {
    ENUM_LEVEL_DEBUG,
    ENUM_LEVEL_ERROR,
    ENUM_LEVEL_FATAL,
    ENUM_LEVEL_INFO,
    ENUM_LEVEL_TRACE,
    ENUM_LEVEL_WARN,
} kaa_logging_level_t;

#ifdef GENC_ENUM_DEFINE_ALIASES
#define DEBUG ENUM_LEVEL_DEBUG
#define ERROR ENUM_LEVEL_ERROR
#define FATAL ENUM_LEVEL_FATAL
#define INFO ENUM_LEVEL_INFO
#define TRACE ENUM_LEVEL_TRACE
#define WARN ENUM_LEVEL_WARN
# endif // GENC_ENUM_DEFINE_ALIASES

#ifdef GENC_ENUM_STRING_LITERALS
const char* KAA_LOGGING_LEVEL_SYMBOLS[6] = {
    "DEBUG",
    "ERROR",
    "FATAL",
    "INFO",
    "TRACE",
    "WARN"};
# endif // GENC_ENUM_STRING_LITERALS


typedef struct {
    kaa_logging_level_t level;
    kaa_string_t* tag;
    kaa_string_t* message;

    serialize_fn serialize;
    get_size_fn  get_size;
    destroy_fn   destroy;
} kaa_logging_log_data_t;

kaa_logging_log_data_t* kaa_logging_log_data_create();
kaa_logging_log_data_t* kaa_logging_log_data_deserialize(avro_reader_t reader);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif
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

# ifndef KAA_PLUGIN_GEN_H_
# define KAA_PLUGIN_GEN_H_

#include "../kaa_context.h"

# ifdef __cplusplus
extern "C" {
# endif

typedef kaa_plugin_t* (*kaa_plugin_create)(kaa_context_t *context);

typedef struct {
    kaa_plugin_create create_fn;
} kaa_plugin_info_t;

extern kaa_plugin_info_t kaa_available_plugins[];
extern size_t kaa_available_plugins_count;

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif

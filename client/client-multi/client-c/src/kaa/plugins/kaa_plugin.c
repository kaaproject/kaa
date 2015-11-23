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


#include <stdlib.h>
#include <stdio.h>

#include "kaa_plugin.h"
#include "../kaa_common.h"
#include "../gen/kaa_plugin_gen.h"
#include "../utilities/kaa_mem.h"

kaa_plugin_t **kaa_plugins;
size_t kaa_plugin_count;


kaa_error_t kaa_plugin_find_by_type(kaa_context_t *context, uint16_t type, kaa_plugin_t **plugin)
{
    KAA_RETURN_IF_NIL(plugin, KAA_ERR_BADDATA);

    for (uint32_t i = 0; i < context->kaa_plugin_count; ++i) {
        if (context->kaa_plugins[i]->extension_type == type) {
            *plugin = context->kaa_plugins[i];
            return KAA_ERR_NONE;
        }
    }

    return KAA_ERR_BADDATA;
}

kaa_error_t kaa_create_plugins(kaa_context_t *context)
{
    kaa_error_t error_code;
    context->kaa_plugin_count = kaa_available_plugins_count;
    context->kaa_plugins      = KAA_CALLOC(context->kaa_plugin_count, sizeof(kaa_plugin_info_t*));

    for (int i = 0; i < context->kaa_plugin_count; ++i) {
        context->kaa_plugins[i] = kaa_available_plugins[i].create_fn(context);
        if (context->kaa_plugins[i]) {
            error_code = context->kaa_plugins[i]->init_fn(context->kaa_plugins[i]);
            context->kaa_plugins[i]->last_error = error_code;
        }
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_destroy_plugins(kaa_context_t *context)
{
    for (int i = 0; i < context->kaa_plugin_count; ++i) {
        context->kaa_plugins[i]->deinit_fn(context->kaa_plugins[i]);
    }
    KAA_FREE(context->kaa_plugins);
    context->kaa_plugins = NULL;
    context->kaa_plugin_count = 0;
    return KAA_ERR_NONE;
}

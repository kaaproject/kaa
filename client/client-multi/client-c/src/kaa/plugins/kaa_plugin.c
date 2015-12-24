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
#include "../platform/kaa_client.h"
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
        context->kaa_plugins[i] = kaa_available_plugins[i].create(context);
        if (context->kaa_plugins[i]) {
            fprintf(stderr, "[PLUGIN] %s created \n", context->kaa_plugins[i]->plugin_name);
            error_code = context->kaa_plugins[i]->init_fn(context->kaa_plugins[i]);
            context->kaa_plugins[i]->last_error = error_code;
        }
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_destroy_plugins(kaa_context_t *context)
{
    KAA_RETURN_IF_NIL(context, KAA_ERR_BADPARAM);

    for (int i = 0; i < context->kaa_plugin_count; ++i) {
        context->kaa_plugins[i]->deinit_fn(context->kaa_plugins[i]);
    }
    KAA_FREE(context->kaa_plugins);
    context->kaa_plugins = NULL;
    context->kaa_plugin_count = 0;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_get_supported_plugins(uint16_t **array, int *size)
{
    KAA_RETURN_IF_NIL2(array, size, KAA_ERR_BADPARAM);
    *array = (uint16_t*)KAA_CALLOC(kaa_available_plugins_count, sizeof(int));
    for (int i = 0; i < kaa_available_plugins_count; ++i) {
        (*array)[i] = kaa_available_plugins[i].type;
    }
    *size=kaa_available_plugins_count;
    return KAA_ERR_NONE;
}


#define BOOTSTRAP_AUTHORIZED_ARRAY_SIZE 2

kaa_error_t kaa_get_bootstrap_authorized_array(uint16_t **array, int *size)
{
    KAA_RETURN_IF_NIL2(array, size, KAA_ERR_BADPARAM);
    *array = (uint16_t*)KAA_CALLOC(2, sizeof(int));

    *size  = 0;
    for (int i = 0; i < BOOTSTRAP_AUTHORIZED_ARRAY_SIZE; ++i) {
        if (kaa_available_plugins[i].type == KAA_PLUGIN_META_DATA) {
            (*array)[0] = kaa_available_plugins[i].type;
            *size += 1;
        } else if (kaa_available_plugins[i].type == KAA_PLUGIN_BOOTSTRAP) {
            (*array)[1] = kaa_available_plugins[i].type;
            *size += 1;
        }
    }

    if (*size < BOOTSTRAP_AUTHORIZED_ARRAY_SIZE) {
        *size = 0;
        return KAA_ERR_BADDATA;
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_get_operation_authorized_array(uint16_t **array, int *size)
{
    KAA_RETURN_IF_NIL2(array, size, KAA_ERR_BADPARAM);
    int count = kaa_available_plugins_count - 1; // without bootstrap
    int indx = 0;
    *array = (uint16_t*)KAA_CALLOC(count, sizeof(int));
    for (int i = 0; i < kaa_available_plugins_count; ++i) {
        if(kaa_available_plugins[i].type != KAA_PLUGIN_BOOTSTRAP) {
            (*array)[indx++] = kaa_available_plugins[i].type;
        }
    }
    *size=count;
    return KAA_ERR_NONE;
}

void kaa_free_supported_plugins_array(uint16_t *array)
{
    if(array)
        KAA_FREE(array);
}

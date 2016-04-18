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

#include "kaa_extension.h"

#include <kaa_extension_private.h>

#include <stddef.h>

#define EXTENSION_COUNT (sizeof(kaa_extensions)/sizeof(*kaa_extensions))

#define EXTENSION_NOT_FOUND ((size_t)-1)

/**
 * A list of kaa contexts.
 */
static void *kaa_extension_contexts[EXTENSION_COUNT] = { NULL };

/**
 * Return the index of extension in the kaa_extensions.
 */
static size_t extension_id_to_idx(kaa_extension_id id)
{
    for (size_t i = 0; i < EXTENSION_COUNT; ++i) {
        if (kaa_extensions[i]->id == id) {
            return i;
        }
    }
    return EXTENSION_NOT_FOUND;
}

const struct kaa_extension *kaa_extension_get(kaa_extension_id id)
{
    size_t idx = extension_id_to_idx(id);
    return idx == EXTENSION_NOT_FOUND ? NULL : kaa_extensions[idx];
}

void *kaa_extension_get_context(kaa_extension_id id)
{
    size_t idx = extension_id_to_idx(id);
    return idx == EXTENSION_NOT_FOUND ? NULL : kaa_extension_contexts[idx];
}

kaa_error_t kaa_extension_set_context(kaa_extension_id id, void *context)
{
    size_t idx = extension_id_to_idx(id);
    if (idx == EXTENSION_NOT_FOUND) {
        return KAA_ERR_NOT_FOUND;
    }

    kaa_extension_contexts[idx] = context;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_extension_init_all(struct kaa_context_s *kaa_context)
{
    kaa_error_t result = KAA_ERR_NONE;

    size_t i = 0;
    for (; i < EXTENSION_COUNT; ++i) {
        result = kaa_extensions[i]->init(kaa_context, &kaa_extension_contexts[i]);
        if (result != KAA_ERR_NONE) {
            break;
        }
    }

    if (result != KAA_ERR_NONE) {
        while (i > 0) {
            --i;
            kaa_extensions[i]->deinit(kaa_extension_contexts[i]);
        }
    }

    return result;
}

kaa_error_t kaa_extension_deinit_all(void)
{
    kaa_error_t result = KAA_ERR_NONE;

    for (size_t i = EXTENSION_COUNT; i > 0;) {
        --i;

        kaa_error_t res = kaa_extensions[i]->deinit(kaa_extension_contexts[i]);
        if (res != KAA_ERR_NONE) {
            result = res;
        }
    }

    return result;
}

kaa_error_t kaa_extension_request_serialize(kaa_extension_id id, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *sync_needed)
{
    size_t idx = extension_id_to_idx(id);
    if (idx == EXTENSION_NOT_FOUND) {
        return KAA_ERR_NOT_FOUND;
    }

    return kaa_extensions[idx]->request_serialize(kaa_extension_contexts[idx], request_id,
            buffer, size, sync_needed);
}

kaa_error_t kaa_extension_server_sync(kaa_extension_id id, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{
    size_t idx = extension_id_to_idx(id);
    if (idx == EXTENSION_NOT_FOUND) {
        return KAA_ERR_NOT_FOUND;
    }

    return kaa_extensions[idx]->server_sync(kaa_extension_contexts[idx], request_id,
            extension_options, buffer, size);
}

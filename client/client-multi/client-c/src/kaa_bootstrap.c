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

#include "kaa_bootstrap.h"

#include <stdlib.h>

#include "kaa_list.h"
#include "kaa_mem.h"

struct kaa_bootstrap_manager_t {
    kaa_list_t * ops_list[KAA_CHANNEL_TYPE_COUNT];
    kaa_list_t* last_servers[KAA_CHANNEL_TYPE_COUNT];
};

kaa_error_t kaa_create_bootstrap_manager(kaa_bootstrap_manager_t ** bm_p)
{
    kaa_bootstrap_manager_t * bm = KAA_CALLOC(1, sizeof(kaa_bootstrap_manager_t));
    if (bm == NULL) {
        return KAA_ERR_NOMEM;
    }

    *bm_p = bm;
    return KAA_ERR_NONE;
}

void kaa_destroy_bootstrap_manager(kaa_bootstrap_manager_t *bm)
{
    for (int i = 0; i < KAA_CHANNEL_TYPE_COUNT; ++i) {
        if (bm->ops_list[i]) {
            kaa_list_destroy(bm->ops_list[i], NULL); // FIXME: who should deallocate data in the list?
        }
    }

    KAA_FREE(bm);
}

kaa_error_t kaa_add_operation_server(kaa_bootstrap_manager_t *bm, kaa_ops_t* new_s)
{
    if (!bm || !new_s || new_s->channel_type >= KAA_CHANNEL_TYPE_COUNT) {
        return KAA_ERR_BADPARAM;
    }

    kaa_list_t* ops_list = bm->ops_list[new_s->channel_type];
    if (!ops_list) {
        bm->ops_list[new_s->channel_type] = kaa_list_create(new_s);
        // FIXME: check for return value
    } else {
        kaa_list_t* cur_s = ops_list;
        kaa_list_t* prev_s = NULL;

        while (cur_s) {
            kaa_ops_t* s = (kaa_ops_t *)kaa_list_get_data(cur_s);
            if (s->priority >= new_s->priority) {
                break;
            }

            prev_s = cur_s;
            cur_s = kaa_list_next(cur_s);
        }

        if (!prev_s) {
            kaa_list_push_front(&cur_s, new_s); // FIXME: check return value
            bm->ops_list[new_s->channel_type] = cur_s;
        } else if (!cur_s) {
            kaa_list_push_back(&prev_s, new_s); // FIXME: check return value
        } else {
            kaa_list_insert_after(prev_s, new_s); // FIXME: check return value
        }
    }

    return KAA_ERR_NONE;
}

kaa_ops_t* kaa_get_current_operation_server(kaa_bootstrap_manager_t *bm, kaa_channel_type_t channel_type)
{
    kaa_ops_t* server = NULL;

    if (bm && channel_type < KAA_CHANNEL_TYPE_COUNT) {
        kaa_list_t* current_server_node = bm->last_servers[channel_type];
        if (!current_server_node) {
            current_server_node = bm->ops_list[channel_type];
            if (current_server_node) {
                bm->last_servers[channel_type] = current_server_node;
                server = (kaa_ops_t*)kaa_list_get_data(current_server_node);
            }
        } else {
            server = (kaa_ops_t*)kaa_list_get_data(current_server_node);
        }
    }
    return server;
}

kaa_ops_t* kaa_get_next_operation_server(kaa_bootstrap_manager_t *bm, kaa_channel_type_t channel_type)
{
    kaa_ops_t* server = NULL;

    if (bm && channel_type < KAA_CHANNEL_TYPE_COUNT) {
        if (!bm->last_servers[channel_type]) {
            server = kaa_get_current_operation_server(bm, channel_type);
        } else {
            kaa_list_t* next_server_node = kaa_list_next(bm->last_servers[channel_type]);
            if (next_server_node) {
                bm->last_servers[channel_type] = next_server_node;
                server = kaa_list_get_data(next_server_node);
            }
        }
    }

    return server;
}

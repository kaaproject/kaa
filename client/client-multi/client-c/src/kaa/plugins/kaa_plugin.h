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

# ifndef KAA_PLUGIN_H_
# define KAA_PLUGIN_H_

#include <stdint.h>
#include <stdbool.h>
#include <stdlib.h>
#include "../kaa_context.h"
#include "../kaa_platform_utils.h"

# ifdef __cplusplus
extern "C" {
# endif

typedef kaa_error_t (*kaa_plugin_init)(kaa_plugin_t *self);
typedef kaa_error_t (*kaa_plugin_deinit)(kaa_plugin_t *self);
typedef kaa_error_t (*kaa_plugin_request_get_size)(kaa_plugin_t *self, size_t *expected_size);
typedef kaa_error_t (*kaa_plugin_request_serialize)(kaa_plugin_t *self, kaa_platform_message_writer_t *writer);
typedef kaa_error_t (*kaa_plugin_request_handle_server_sync)(kaa_plugin_t *self, kaa_platform_message_reader_t *reader,
                                                         uint32_t request_id, uint16_t options, uint32_t length);


#define COMMON_PLUGIN_FIELDS kaa_plugin_init                       init_fn; \
                             kaa_plugin_deinit                     deinit_fn; \
                             kaa_plugin_request_get_size           request_get_size_fn; \
                             kaa_plugin_request_serialize          request_serialize_fn; \
                             kaa_plugin_request_handle_server_sync request_handle_server_sync_fn; \
                             uint16_t                 extension_type; \
                             char                    *plugin_name; \
                             kaa_context_t           *context; \
                             kaa_error_t              last_error;

struct kaa_plugin_t {
    COMMON_PLUGIN_FIELDS
};

kaa_error_t kaa_plugin_find_by_type(kaa_context_t *context, uint16_t type, kaa_plugin_t **plugin);

kaa_error_t kaa_create_plugins(kaa_context_t *context);
kaa_error_t kaa_destroy_plugins(kaa_context_t *context);

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif

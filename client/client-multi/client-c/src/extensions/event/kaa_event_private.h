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
#ifndef KAA_EVENT_PRIVATE_H
#define KAA_EVENT_PRIVATE_H

#include <kaa_event.h>
#include <kaa_status.h>
#include <kaa_channel_manager.h>
#include <utilities/kaa_log.h>
#include <kaa_platform_utils.h>

kaa_error_t kaa_event_manager_create(kaa_event_manager_t **event_manager_p, kaa_status_t *status,
        kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
void kaa_event_manager_destroy(kaa_event_manager_t *self);
kaa_error_t kaa_event_manager_send_event(kaa_event_manager_t *self, const char *fqn, const char *event_data,
    size_t event_data_size, kaa_endpoint_id_p target);

kaa_error_t kaa_event_manager_add_event_to_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id,
    const char *fqn, const char *event_data, size_t event_data_size, kaa_endpoint_id_p target);

kaa_error_t kaa_event_manager_add_on_event_callback(kaa_event_manager_t *self, const char *fqn,
    kaa_event_callback_t callback);

kaa_error_t kaa_event_request_get_size(kaa_event_manager_t *self, size_t *expected_size);
kaa_error_t kaa_event_request_serialize(kaa_event_manager_t *self, size_t request_id,
        kaa_platform_message_writer_t *writer);
kaa_error_t kaa_event_handle_server_sync(kaa_event_manager_t *self, kaa_platform_message_reader_t *reader, uint16_t extension_options, size_t extension_length, size_t request_id);

#endif /* KAA_EVENT_PRIVATE_H */

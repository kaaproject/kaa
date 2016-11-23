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

/** @file
 * This file is a container for all private APIs. That's a temporary
 * measure---a safety net for refactoring. The header is included in
 * every C file, so compiler will catch any interface changes.
 *
 * The header will be deleted later.
 */
#ifndef KAA_PRIVATE_H
#define KAA_PRIVATE_H

#include <kaa_error.h>
#include <kaa_status.h>
#include <kaa_channel_manager.h>
#include <kaa_platform_utils.h>
#include <platform/kaa_client.h>
#include <kaa_platform_common.h>
#include <stdint.h>
#include <kaa_context.h>
#include <platform/ext_kaa_failover_strategy.h>
#include <utilities/kaa_log.h>
#include <kaa_bootstrap_manager.h>

#ifdef __cplusplus
extern "C" {
#endif // __cplusplus

kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
kaa_error_t kaa_status_save(kaa_status_t *kaa_status_p);
void kaa_status_destroy(kaa_status_t *self);

kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_context_t *context);
void kaa_channel_manager_destroy(kaa_channel_manager_t *self);
kaa_transport_channel_interface_t *kaa_channel_manager_get_transport_channel(kaa_channel_manager_t *self,
        kaa_extension_id service_type);

kaa_error_t kaa_bootstrap_manager_create(kaa_bootstrap_manager_t **bootstrap_manager_p, kaa_context_t *kaa_context);

void kaa_bootstrap_manager_destroy(kaa_bootstrap_manager_t *self);

kaa_error_t kaa_platform_protocol_create(kaa_platform_protocol_t **platform_protocol_p,
        kaa_logger_t *logger, kaa_status_t *status);
void kaa_platform_protocol_destroy(kaa_platform_protocol_t *self);

kaa_error_t kaa_status_set_registered(kaa_status_t *self, bool is_registered);

kaa_error_t kaa_failover_strategy_create(kaa_failover_strategy_t** strategy, kaa_logger_t *logger);
void kaa_failover_strategy_destroy(kaa_failover_strategy_t* strategy);
bool kaa_bootstrap_manager_process_failover(kaa_bootstrap_manager_t *self);

kaa_error_t kaa_channel_manager_on_new_access_point(kaa_channel_manager_t *self
        , kaa_transport_protocol_id_t *protocol_id
        , kaa_server_type_t server_type
        , kaa_access_point_t *access_point);

kaa_access_point_t *kaa_bootstrap_manager_get_operations_access_point(kaa_bootstrap_manager_t *self
        , kaa_transport_protocol_id_t *protocol_info);

kaa_access_point_t *kaa_bootstrap_manager_get_bootstrap_access_point(
    kaa_bootstrap_manager_t *self,
    kaa_transport_protocol_id_t *protocol_id);


kaa_error_t kaa_status_set_endpoint_access_token(kaa_status_t *self, const char *token);


kaa_error_t kaa_status_set_updated(kaa_status_t *self, bool is_updated);

kaa_error_t kaa_status_set_attached(kaa_status_t *self, bool is_attached);

kaa_error_t kaa_context_set_status_registered(kaa_context_t *kaa_context, bool is_registered);

struct kaa_status_holder_t {
    kaa_status_t *status_instance;
};

kaa_error_t kaa_extension_bootstrap_init(kaa_context_t *kaa_context, void **context);
kaa_error_t kaa_extension_profile_init(kaa_context_t *kaa_context, void **context);
kaa_error_t kaa_extension_event_init(kaa_context_t *kaa_context, void **context);
kaa_error_t kaa_extension_logging_init(kaa_context_t *kaa_context, void **context);
kaa_error_t kaa_extension_configuration_init(kaa_context_t *kaa_context, void **context);
kaa_error_t kaa_extension_notification_init(kaa_context_t *kaa_context, void **context);
kaa_error_t kaa_extension_user_init(kaa_context_t *kaa_context, void **context);

kaa_error_t kaa_extension_bootstrap_deinit(void *context);
kaa_error_t kaa_extension_profile_deinit(void *context);
kaa_error_t kaa_extension_event_deinit(void *context);
kaa_error_t kaa_extension_logging_deinit(void *context);
kaa_error_t kaa_extension_configuration_deinit(void *context);
kaa_error_t kaa_extension_notification_deinit(void *context);
kaa_error_t kaa_extension_user_deinit(void *context);

kaa_error_t kaa_extension_bootstrap_request_serialize(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *need_resync);
kaa_error_t kaa_extension_profile_request_serialize(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *need_resync);
kaa_error_t kaa_extension_event_request_serialize(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *need_resync);
kaa_error_t kaa_extension_logging_request_serialize(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *need_resync);
kaa_error_t kaa_extension_configuration_request_serialize(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *need_resync);
kaa_error_t kaa_extension_notification_request_serialize(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *need_resync);
kaa_error_t kaa_extension_user_request_serialize(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *need_resync);

kaa_error_t kaa_extension_bootstrap_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size);
kaa_error_t kaa_extension_profile_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size);
kaa_error_t kaa_extension_event_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size);
kaa_error_t kaa_extension_logging_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size);
kaa_error_t kaa_extension_configuration_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size);
kaa_error_t kaa_extension_notification_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size);
kaa_error_t kaa_extension_user_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size);

/* ALL FUNCTIONS BELOW ARE DEPRECATED
 *
 * They should be removed once we move extensions out of the core.
 */

kaa_error_t kaa_channel_manager_bootstrap_request_get_size(kaa_channel_manager_t *self, size_t *expected_size);

kaa_error_t kaa_channel_manager_bootstrap_request_serialize(kaa_channel_manager_t *self,
        kaa_platform_message_writer_t* writer);
kaa_error_t kaa_bootstrap_manager_bootstrap_request_serialize(kaa_bootstrap_manager_t *self,
        kaa_platform_message_writer_t* writer);
kaa_error_t kaa_meta_data_request_serialize(kaa_platform_protocol_t *status,
        kaa_platform_message_writer_t* writer, uint32_t request_id);

kaa_error_t kaa_bootstrap_manager_handle_server_sync(kaa_bootstrap_manager_t *self, kaa_platform_message_reader_t *reader, uint16_t extension_options, size_t extension_length);
#ifdef __cplusplus
}
#endif // __cplusplus

#endif /* KAA_PRIVATE_H */

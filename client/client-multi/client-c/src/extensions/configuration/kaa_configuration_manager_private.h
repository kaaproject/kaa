#ifndef KAA_CONFIGURATION_MANAGER_PRIVATE_H
#define KAA_CONFIGURATION_MANAGER_PRIVATE_H

#include <kaa_error.h>
#include "kaa_configuration_manager.h"
#include <kaa_channel_manager.h>
#include <kaa_status.h>
#include <utilities/kaa_log.h>
#include <kaa_platform_utils.h>

kaa_error_t kaa_configuration_manager_create(kaa_configuration_manager_t **configuration_manager_p,
        kaa_channel_manager_t *channel_manager, kaa_status_t *status, kaa_logger_t *logger);
void kaa_configuration_manager_destroy(kaa_configuration_manager_t *self);
kaa_error_t kaa_configuration_manager_get_size(kaa_configuration_manager_t *self, size_t *expected_size);
kaa_error_t kaa_configuration_manager_request_serialize(kaa_configuration_manager_t *self,
        kaa_platform_message_writer_t *writer);
kaa_error_t kaa_configuration_manager_handle_server_sync(kaa_configuration_manager_t *self, kaa_platform_message_reader_t *reader, uint16_t extension_options, size_t extension_length);

#endif /* KAA_CONFIGURATION_MANAGER_PRIVATE_H */

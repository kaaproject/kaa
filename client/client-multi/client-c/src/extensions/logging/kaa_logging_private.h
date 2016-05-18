#ifndef KAA_LOGGING_PRIVATE_H
#define KAA_LOGGING_PRIVATE_H

#include <kaa_logging.h>
#include <kaa_status.h>
#include <kaa_status.h>
#include <kaa_channel_manager.h>
#include <utilities/kaa_log.h>
#include <kaa_platform_utils.h>

kaa_error_t kaa_log_collector_create(kaa_log_collector_t ** log_collector_p, kaa_status_t *status,
        kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
void kaa_log_collector_destroy(kaa_log_collector_t *self);
kaa_error_t kaa_logging_need_logging_resync(kaa_log_collector_t *self, bool *result);

kaa_error_t kaa_logging_request_get_size(kaa_log_collector_t *self, size_t *expected_size);
kaa_error_t kaa_logging_request_serialize(kaa_log_collector_t *self,
        kaa_platform_message_writer_t *writer);
kaa_error_t kaa_logging_handle_server_sync(kaa_log_collector_t *self, kaa_platform_message_reader_t *reader, uint16_t extension_options, size_t extension_length);

void ext_log_upload_timeout(kaa_log_collector_t *self);
void ext_log_upload_timeout(kaa_log_collector_t *self);
bool ext_log_upload_strategy_is_timeout_strategy(void *strategy);

kaa_error_t ext_unlimited_log_storage_create(void **log_storage_context_p, kaa_logger_t *logger);
kaa_error_t ext_limited_log_storage_create(void **log_storage_context_p, kaa_logger_t *logger,
        size_t storage_size, size_t percent_to_delete);
kaa_error_t ext_log_storage_destroy(void *context);

#endif /* KAA_LOGGING_PRIVATE_H */

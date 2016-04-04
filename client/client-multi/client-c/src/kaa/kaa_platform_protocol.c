/*
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include "kaa_private.h"

#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include "platform/stdio.h"
#include "platform/ext_sha.h"
#include "platform/sock.h"
#include "kaa_status.h"
#include "kaa_platform_protocol.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_context.h"
#include "kaa_defaults.h"
#include "kaa_event.h"
#include "kaa_profile.h"
#include "kaa_logging.h"
#include "kaa_user.h"

#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"

#include <kaa_extension.h>

/** Resync flag indicating that profile manager should be resynced */
#define KAA_PROFILE_RESYNC_FLAG     0x1

static const size_t kaa_meta_data_request_size =
    KAA_EXTENSION_HEADER_SIZE +
    sizeof(uint32_t) +
    sizeof(uint32_t) +
    KAA_ALIGNED_SIZE(SHA_1_DIGEST_LENGTH) +
    KAA_ALIGNED_SIZE(SHA_1_DIGEST_LENGTH) +
    KAA_ALIGNED_SIZE(KAA_SDK_TOKEN_LENGTH);

struct kaa_platform_protocol_t
{
    kaa_context_t *kaa_context;
    kaa_status_t  *status;
    kaa_logger_t  *logger;
    uint32_t       request_id;
};

kaa_error_t kaa_meta_data_request_serialize(kaa_platform_protocol_t *self,
        kaa_platform_message_writer_t *writer, uint32_t request_id)
{
    // TODO(KAA-982): Use assert here
    if (!self || !self->status || !writer) {
        return KAA_ERR_BADPARAM;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to serialize client meta sync");

    uint16_t options = TIMEOUT_VALUE | PUBLIC_KEY_HASH_VALUE | PROFILE_HASH_VALUE | APP_TOKEN_VALUE;

    const size_t payload_length = kaa_meta_data_request_size - KAA_EXTENSION_HEADER_SIZE;

    kaa_error_t err_code = kaa_platform_message_write_extension_header(writer,
            KAA_EXTENSION_META_DATA, options, payload_length);
    if (err_code) {
        return err_code;
    }

    uint32_t request_id_network = KAA_HTONL(request_id);
    err_code = kaa_platform_message_write(writer, &request_id_network, sizeof(uint32_t));
    if (err_code) {
        return err_code;
    }

    uint32_t timeout = KAA_HTONL(KAA_SYNC_TIMEOUT);
    err_code = kaa_platform_message_write(writer, &timeout, sizeof(timeout));
    if (err_code) {
        return err_code;
    }

    err_code = kaa_platform_message_write_aligned(writer,
            self->status->endpoint_public_key_hash, SHA_1_DIGEST_LENGTH);
    if (err_code) {
        return err_code;
    }

    err_code = kaa_platform_message_write_aligned(writer,
            self->status->profile_hash, SHA_1_DIGEST_LENGTH);
    if (err_code) {
        return err_code;
    }

    err_code = kaa_platform_message_write_aligned(writer, KAA_SDK_TOKEN, KAA_SDK_TOKEN_LENGTH);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE,
            "Meta sync: payload length '%u', request id '%u'", payload_length, request_id);

    return err_code;
}

kaa_error_t kaa_platform_protocol_create(kaa_platform_protocol_t **platform_protocol_p,
        kaa_context_t *context, kaa_status_t *status)
{
    if (!platform_protocol_p || !context || !context->logger || !status) {
        return KAA_ERR_BADPARAM;
    }

    *platform_protocol_p = KAA_MALLOC(sizeof(**platform_protocol_p));
    if (!*platform_protocol_p) {
        return KAA_ERR_NOMEM;
    }

    **platform_protocol_p = (kaa_platform_protocol_t){
        .request_id = 0,
        .kaa_context = context,
        .status = status,
        .logger = context->logger,
    };

    return KAA_ERR_NONE;
}

void kaa_platform_protocol_destroy(kaa_platform_protocol_t *self)
{
    KAA_FREE(self);
}

static kaa_error_t kaa_client_sync_get_size(kaa_platform_protocol_t *self,
        const kaa_extension_id *extensions,
        const size_t extension_count,
        size_t *expected_size)
{
    // TODO(KAA-982): Use assert here
    if (!self || !extensions || !expected_size || extension_count == 0) {
        return KAA_ERR_BADPARAM;
    }

    *expected_size = KAA_PROTOCOL_MESSAGE_HEADER_SIZE + kaa_meta_data_request_size;

    for (size_t i = 0; i < extension_count; ++i) {
        size_t extension_size = 0;

        kaa_error_t error = kaa_extension_request_get_size(extensions[i], &extension_size);
        if (error && error != KAA_ERR_NOT_FOUND) {
            KAA_LOG_ERROR(self->logger, error,
                    "Failed to query extension size for %u", extensions[i]);
            return error;
        }

        *expected_size += extension_size;
    }

    return KAA_ERR_NONE;
}

// TODO: make buffer uint8_t
static kaa_error_t kaa_client_sync_serialize(kaa_platform_protocol_t *self,
        const kaa_extension_id services[], size_t services_count, char* buffer, size_t *size)
{
    kaa_platform_message_writer_t *writer = NULL;
    kaa_error_t error_code = kaa_platform_message_writer_create(&writer, buffer, *size);
    if (error_code) {
        return error_code;
    }

    uint16_t total_services_count = services_count + 1 /* Meta extension */;

    error_code = kaa_platform_message_header_write(writer,
            KAA_PLATFORM_PROTOCOL_ID, KAA_PLATFORM_PROTOCOL_VERSION);
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to write the client sync header");
        goto fail;
    }

    uint16_t *extension_count_p = (uint16_t *)writer->current;
    writer->current += KAA_PROTOCOL_EXTENSIONS_COUNT_SIZE;
    // TODO: assert KAA_PROTOCOL_EXTENSIONS_COUNT_SIZE == sizeof(uint16_t)

    error_code = kaa_meta_data_request_serialize(self, writer, self->request_id);
    if (error_code) {
        goto fail;
    }

    while (!error_code && services_count--) {
        switch (services[services_count]) {
            case KAA_EXTENSION_BOOTSTRAP: {
                error_code = kaa_bootstrap_manager_bootstrap_request_serialize(
                        self->kaa_context->bootstrap_manager, writer);
                if (error_code) {
                    KAA_LOG_ERROR(self->logger, error_code,
                            "Failed to serialize the bootstrap extension");
                }
                break;
            }

            case KAA_EXTENSION_PROFILE: {
                bool need_resync = false;
                error_code = kaa_profile_need_profile_resync(self->kaa_context->profile_manager,
                        &need_resync);
                if (error_code) {
                    KAA_LOG_ERROR(self->logger, error_code,
                            "Failed to read profile's 'need_resync' flag");
                    break;
                }

                if (!need_resync) {
                    --total_services_count;
                    break;
                }

                error_code = kaa_profile_request_serialize(self->kaa_context->profile_manager,
                        writer);
                if (error_code) {
                    KAA_LOG_ERROR(self->logger, error_code,
                            "Failed to serialize the profile extension");
                }
                break;
            }

            case KAA_EXTENSION_USER: {
                error_code = kaa_user_request_serialize(self->kaa_context->user_manager, writer);
                if (error_code) {
                    KAA_LOG_ERROR(self->logger, error_code,
                            "Failed to serialize the user extension");
                }
                break;
            }

#ifndef KAA_DISABLE_FEATURE_EVENTS
            case KAA_EXTENSION_EVENT: {
                error_code = kaa_event_request_serialize(self->kaa_context->event_manager,
                        self->request_id, writer);
                if (error_code) {
                    KAA_LOG_ERROR(self->logger, error_code,
                            "Failed to serialize the event extension");
                }
                break;
            }
#endif

#ifndef KAA_DISABLE_FEATURE_LOGGING
            case KAA_EXTENSION_LOGGING: {
                bool need_resync = false;
                error_code = kaa_logging_need_logging_resync(self->kaa_context->log_collector,
                        &need_resync);
                if (error_code) {
                    KAA_LOG_ERROR(self->logger, error_code,
                            "Failed to read logging's 'need_resync' flag");
                    break;
                }

                if (!need_resync) {
                    --total_services_count;
                    break;
                }

                error_code = kaa_logging_request_serialize(self->kaa_context->log_collector,
                        writer);
                if (error_code) {
                    KAA_LOG_ERROR(self->logger, error_code,
                            "Failed to serialize the logging extension");
                }
                break;
            }
#endif

#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
            case KAA_EXTENSION_CONFIGURATION: {
                error_code = kaa_configuration_manager_request_serialize(
                        self->kaa_context->configuration_manager, writer);
                if (error_code) {
                    KAA_LOG_ERROR(self->logger, error_code,
                            "Failed to serialize the configuration extension");
                }
                break;
            }
#endif

#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
            case KAA_EXTENSION_NOTIFICATION: {
                error_code = kaa_notification_manager_request_serialize(
                        self->kaa_context->notification_manager, writer);
                if (error_code) {
                    KAA_LOG_ERROR(self->logger, error_code,
                            "Failed to serialize the configuration extension");
                }
                break;
            }
#endif

            default:
                --total_services_count;
                break;
        }
    }

    *extension_count_p = KAA_HTONS(total_services_count);
    *size = writer->current - writer->begin;

fail:
    kaa_platform_message_writer_destroy(writer);

    return error_code;
}

// TODO: make buffer uint8_t
kaa_error_t kaa_platform_protocol_serialize_client_sync(kaa_platform_protocol_t *self,
        const kaa_serialize_info_t *info, char **buffer, size_t *buffer_size)
{
    if (!self || !info || !buffer || !buffer_size) {
        return KAA_ERR_BADPARAM;
    }

    if (!info->services || info->services_count == 0) {
        return KAA_ERR_BADDATA;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Serializing client sync...");

    *buffer_size = 0;
    kaa_error_t error = kaa_client_sync_get_size(self, info->services, info->services_count,
            buffer_size);
    if (error) {
        return error;
    }

    KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE,
            "Going to request sync buffer (size %zu)", *buffer_size);

    *buffer = KAA_MALLOC(*buffer_size);
    if (!*buffer) {
        return KAA_ERR_NOMEM;
    }

    self->request_id++;
    error = kaa_client_sync_serialize(self, info->services, info->services_count,
            *buffer, buffer_size);
    if (error) {
        self->request_id--;
        return error;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE,
            "Client sync serialized: request id '%u', payload size '%zu'",
            self->request_id, *buffer_size);

    return KAA_ERR_NONE;
}

kaa_error_t kaa_platform_protocol_process_server_sync(kaa_platform_protocol_t *self,
        const char *buffer, size_t buffer_size)
{
    if (!self || !buffer || buffer_size == 0) {
        return KAA_ERR_BADPARAM;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE,
            "Server sync received: payload size '%zu'", buffer_size);

    kaa_platform_message_reader_t *reader = NULL;
    kaa_error_t error_code = kaa_platform_message_reader_create(&reader, buffer, buffer_size);
    if (error_code) {
        return error_code;
    }

    uint32_t protocol_id = 0;
    uint16_t protocol_version = 0;
    uint16_t extension_count = 0;

    error_code = kaa_platform_message_header_read(reader, &protocol_id, &protocol_version,
            &extension_count);
    if (error_code) {
        goto fail;
    }

    if (protocol_id != KAA_PLATFORM_PROTOCOL_ID) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BAD_PROTOCOL_ID,
                "Unsupported protocol ID %x", protocol_id);
        error_code = KAA_ERR_BAD_PROTOCOL_ID;
        goto fail;
    }

    if (protocol_version != KAA_PLATFORM_PROTOCOL_VERSION) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BAD_PROTOCOL_VERSION,
                "Unsupported protocol version %u", protocol_version);
        error_code = KAA_ERR_BAD_PROTOCOL_VERSION;
        goto fail;
    }

    uint32_t request_id = 0;
    uint16_t extension_type = 0;
    uint16_t extension_options = 0;
    uint32_t extension_length = 0;

    while (kaa_platform_message_is_buffer_large_enough(reader, KAA_PROTOCOL_MESSAGE_HEADER_SIZE)) {

        error_code = kaa_platform_message_read_extension_header(reader, &extension_type,
                &extension_options, &extension_length);
        if (error_code) {
            goto fail;
        }

        /* Do not resync unless it is requested by the metadata extension */
        // TODO: do profile_needs_resync must be set inside of loop?
        self->status->profile_needs_resync = false;

        switch (extension_type) {
            case KAA_EXTENSION_BOOTSTRAP: {
                error_code = kaa_bootstrap_manager_handle_server_sync(
                        self->kaa_context->bootstrap_manager,
                        reader, extension_options, extension_length);
                break;
            }

            case KAA_EXTENSION_META_DATA: {
                error_code = kaa_platform_message_read(reader, &request_id, sizeof(request_id));
                if (error_code) {
                    break;
                }

                request_id = KAA_NTOHL(request_id);
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Server sync request id %u", request_id);

                /* Check if managers needs resync */

                uint32_t resync_request;
                error_code = kaa_platform_message_read(reader,
                        &resync_request, sizeof(resync_request));
                if (error_code) {
                    break;
                }

                resync_request = KAA_NTOHL(resync_request);
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE,
                        "Server resync request %u", resync_request);

                if (resync_request & KAA_PROFILE_RESYNC_FLAG) {
                    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Profile resync is requested");
                    self->status->profile_needs_resync = true;
                    error_code = kaa_profile_force_sync(self->kaa_context->profile_manager);
                }

                break;
            }

            case KAA_EXTENSION_PROFILE: {
                error_code = kaa_profile_handle_server_sync(self->kaa_context->profile_manager,
                        reader, extension_options, extension_length);
                break;
            }

            case KAA_EXTENSION_USER: {
                error_code = kaa_user_handle_server_sync(self->kaa_context->user_manager,
                        reader, extension_options, extension_length);
                break;
            }

#ifndef KAA_DISABLE_FEATURE_LOGGING
            case KAA_EXTENSION_LOGGING: {
                error_code = kaa_logging_handle_server_sync(self->kaa_context->log_collector,
                        reader, extension_options, extension_length);
                break;
            }
#endif

#ifndef KAA_DISABLE_FEATURE_EVENTS
            case KAA_EXTENSION_EVENT: {
                error_code = kaa_event_handle_server_sync(self->kaa_context->event_manager,
                        reader, extension_options, extension_length, request_id);
                break;
            }
#endif

#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
            case KAA_EXTENSION_CONFIGURATION: {
                error_code = kaa_configuration_manager_handle_server_sync(
                        self->kaa_context->configuration_manager,
                        reader, extension_options, extension_length);
                break;
            }
#endif

#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
            case KAA_EXTENSION_NOTIFICATION: {
                error_code = kaa_notification_manager_handle_server_sync(
                        self->kaa_context->notification_manager,
                        reader, extension_length);
                break;
            }
#endif

            default:
                KAA_LOG_WARN(self->logger, KAA_ERR_UNSUPPORTED,
                        "Unsupported extension received (type = %u)", extension_type);
                break;
        }

        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code,
                    "Server sync is corrupted. Failed to read extension with type %u",
                    extension_type);
            goto fail;
        }
    }

    error_code = kaa_status_save(self->status);
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to save status");
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Server sync successfully processed");

fail:
    kaa_platform_message_reader_destroy(reader);

    return error_code;
}

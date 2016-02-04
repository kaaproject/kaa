/**
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



/** External channel manager API */
extern kaa_error_t kaa_channel_manager_bootstrap_request_get_size(kaa_channel_manager_t *self
                                                                , size_t *expected_size);

extern kaa_error_t kaa_bootstrap_manager_bootstrap_request_serialize(kaa_bootstrap_manager_t *self, kaa_platform_message_writer_t* writer);

/** External bootstrap manager API */
extern kaa_error_t kaa_bootstrap_manager_handle_server_sync(kaa_bootstrap_manager_t *self
                                                          , kaa_platform_message_reader_t *reader
                                                          , uint16_t extension_options
                                                          , size_t extension_length);

/** External user manager API */
extern kaa_error_t kaa_user_request_get_size(kaa_user_manager_t *self, size_t *expected_size);
extern kaa_error_t kaa_user_request_serialize(kaa_user_manager_t *self, kaa_platform_message_writer_t* writer);
extern kaa_error_t kaa_user_handle_server_sync(kaa_user_manager_t *self, kaa_platform_message_reader_t *reader, uint16_t extension_options, size_t extension_length);

/** External profile API */
extern kaa_error_t kaa_profile_need_profile_resync(kaa_profile_manager_t *kaa_context, bool *result);
extern kaa_error_t kaa_profile_request_get_size(kaa_profile_manager_t *self, size_t *expected_size);
extern kaa_error_t kaa_profile_request_serialize(kaa_profile_manager_t *self, kaa_platform_message_writer_t* writer);
extern kaa_error_t kaa_profile_handle_server_sync(kaa_profile_manager_t *self, kaa_platform_message_reader_t *reader, uint16_t extension_options, size_t extension_length);

/** External event manager API */
#ifndef KAA_DISABLE_FEATURE_EVENTS
extern kaa_error_t kaa_event_request_get_size(kaa_event_manager_t *self, size_t *expected_size);
extern kaa_error_t kaa_event_request_serialize(kaa_event_manager_t *self, size_t request_id, kaa_platform_message_writer_t *writer);
extern kaa_error_t kaa_event_handle_server_sync(kaa_event_manager_t *self, kaa_platform_message_reader_t *reader, uint16_t extension_options, size_t extension_length, size_t request_id);
#endif

/** External logging API */
#ifndef KAA_DISABLE_FEATURE_LOGGING
extern kaa_error_t kaa_logging_need_logging_resync(kaa_log_collector_t *self, bool *result);
extern kaa_error_t kaa_logging_request_get_size(kaa_log_collector_t *self, size_t *expected_size);
extern kaa_error_t kaa_logging_request_serialize(kaa_log_collector_t *self, kaa_platform_message_writer_t *writer);
extern kaa_error_t kaa_logging_handle_server_sync(kaa_log_collector_t *self, kaa_platform_message_reader_t *reader, uint16_t extension_options, size_t extension_length);
#endif

/** External configuration API */
#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
extern kaa_error_t kaa_configuration_manager_get_size(kaa_configuration_manager_t *self, size_t *expected_size);
extern kaa_error_t kaa_configuration_manager_request_serialize(kaa_configuration_manager_t *self, kaa_platform_message_writer_t *writer);
extern kaa_error_t kaa_configuration_manager_handle_server_sync(kaa_configuration_manager_t *self, kaa_platform_message_reader_t *reader, uint16_t extension_options, size_t extension_length);
#endif

/** External notification API */
#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
extern kaa_error_t kaa_notification_manager_get_size(kaa_notification_manager_t *self, size_t *expected_size);
extern kaa_error_t kaa_notification_manager_request_serialize(kaa_notification_manager_t *self, kaa_platform_message_writer_t *writer);
extern kaa_error_t kaa_notification_manager_handle_server_sync(kaa_notification_manager_t *self, kaa_platform_message_reader_t *reader, uint32_t extension_length);
#endif

/** External status API */
extern kaa_error_t kaa_status_save(kaa_status_t *self);


struct kaa_platform_protocol_t
{
    kaa_context_t *kaa_context;
    kaa_status_t  *status;
    kaa_logger_t  *logger;
    uint32_t       request_id;
};



kaa_error_t kaa_meta_data_request_get_size(size_t *expected_size)
{
    KAA_RETURN_IF_NIL(expected_size, KAA_ERR_BADPARAM);

    static size_t size = 0;

    if (size == 0) {
        size = KAA_EXTENSION_HEADER_SIZE;
        size += sizeof(uint32_t); // request id
        size += sizeof(uint32_t); // timeout value
        size += kaa_aligned_size_get(SHA_1_DIGEST_LENGTH); // public key hash length
        size += kaa_aligned_size_get(SHA_1_DIGEST_LENGTH); // profile hash length
        size += kaa_aligned_size_get(KAA_SDK_TOKEN_LENGTH); // sdk token length
    }

    *expected_size = size;

    return KAA_ERR_NONE;
}



kaa_error_t kaa_meta_data_request_serialize(kaa_platform_protocol_t *self, kaa_platform_message_writer_t* writer, uint32_t request_id)
{
    KAA_RETURN_IF_NIL3(self, self->status, writer, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to serialize client meta sync");

    uint16_t options = TIMEOUT_VALUE | PUBLIC_KEY_HASH_VALUE | PROFILE_HASH_VALUE | APP_TOKEN_VALUE;

    size_t payload_length = 0;
    kaa_error_t err_code = kaa_meta_data_request_get_size(&payload_length);
    KAA_RETURN_IF_ERR(err_code);
    payload_length -= KAA_EXTENSION_HEADER_SIZE;

    err_code = kaa_platform_message_write_extension_header(writer
                                                         , KAA_META_DATA_EXTENSION_TYPE
                                                         , options
                                                         , payload_length);
    KAA_RETURN_IF_ERR(err_code);

    uint32_t request_id_network = KAA_HTONL(request_id);
    err_code = kaa_platform_message_write(writer, &request_id_network, sizeof(uint32_t));
    KAA_RETURN_IF_ERR(err_code);

    uint32_t timeout = KAA_HTONL(KAA_SYNC_TIMEOUT);
    err_code = kaa_platform_message_write(writer, &timeout, sizeof(timeout));
    KAA_RETURN_IF_ERR(err_code);

    err_code = kaa_platform_message_write_aligned(writer, self->status->endpoint_public_key_hash, SHA_1_DIGEST_LENGTH);
    KAA_RETURN_IF_ERR(err_code);

    err_code = kaa_platform_message_write_aligned(writer, self->status->profile_hash, SHA_1_DIGEST_LENGTH);
    KAA_RETURN_IF_ERR(err_code);

    err_code = kaa_platform_message_write_aligned(writer, KAA_SDK_TOKEN, KAA_SDK_TOKEN_LENGTH);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Meta sync: payload length '%u', request id '%u'", payload_length, request_id);

    return err_code;
}



kaa_error_t kaa_platform_protocol_create(kaa_platform_protocol_t **platform_protocol_p
                                       , kaa_context_t *context
                                       , kaa_status_t *status)
{
    KAA_RETURN_IF_NIL4(platform_protocol_p, context, context->logger, status, KAA_ERR_BADPARAM);

    *platform_protocol_p = KAA_MALLOC(sizeof(kaa_platform_protocol_t));
    KAA_RETURN_IF_NIL(*platform_protocol_p, KAA_ERR_NOMEM);

    (*platform_protocol_p)->request_id = 0;
    (*platform_protocol_p)->kaa_context = context;
    (*platform_protocol_p)->status = status;
    (*platform_protocol_p)->logger = context->logger;
    return KAA_ERR_NONE;
}



void kaa_platform_protocol_destroy(kaa_platform_protocol_t *self)
{
    if (self) {
        KAA_FREE(self);
    }
}



static kaa_error_t kaa_client_sync_get_size(kaa_platform_protocol_t *self
                                          , const kaa_service_t services[]
                                          , size_t services_count
                                          , size_t *expected_size)
{
    KAA_RETURN_IF_NIL4(self, services, services_count, expected_size, KAA_ERR_BADPARAM);

    *expected_size = KAA_PROTOCOL_MESSAGE_HEADER_SIZE;

    size_t extension_size = 0;
    kaa_error_t err_code = kaa_meta_data_request_get_size(&extension_size);
    if (err_code) {
        KAA_LOG_ERROR(self->logger, err_code, "Failed to query size for meta extension");
        return err_code;
    }

    *expected_size += extension_size;
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Calculated meta extension size %u", extension_size);

    while (!err_code && services_count--) {
        switch (services[services_count]) {
        case KAA_SERVICE_BOOTSTRAP: {
            err_code = kaa_channel_manager_bootstrap_request_get_size(self->kaa_context->channel_manager
                                                                    , &extension_size);
            if (!err_code)
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Calculated bootstrap extension size %u", extension_size);
            break;
        }
        case KAA_SERVICE_PROFILE: {
            bool need_resync = false;
            err_code = kaa_profile_need_profile_resync(self->kaa_context->profile_manager
                                                     , &need_resync);
            if (err_code) {
                KAA_LOG_ERROR(self->logger, err_code, "Failed to read 'need_resync' flag");
            }

            if (!err_code && need_resync) {
                err_code = kaa_profile_request_get_size(self->kaa_context->profile_manager
                                                      , &extension_size);
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Calculated profile extension size %u", extension_size);
            }
            break;
        }
        case KAA_SERVICE_USER: {
            err_code = kaa_user_request_get_size(self->kaa_context->user_manager
                                               , &extension_size);
            if (!err_code)
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Calculated user extension size %u", extension_size);
            break;
        }
#ifndef KAA_DISABLE_FEATURE_EVENTS
        case KAA_SERVICE_EVENT: {
            err_code = kaa_event_request_get_size(self->kaa_context->event_manager
                                                , &extension_size);
            if (!err_code)
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Calculated event extension size %u", extension_size);
            break;
        }
#endif
#ifndef KAA_DISABLE_FEATURE_LOGGING
        case KAA_SERVICE_LOGGING: {
            err_code = kaa_logging_request_get_size(self->kaa_context->log_collector
                                                , &extension_size);
            if (!err_code)
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Calculated logging extension size %u", extension_size);
            break;
        }
#endif
#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
        case KAA_SERVICE_CONFIGURATION: {
            err_code = kaa_configuration_manager_get_size(self->kaa_context->configuration_manager
                                                , &extension_size);
            if (!err_code)
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Calculated configuration extension size %u", extension_size);
            break;
        }
#endif
#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
        case KAA_SERVICE_NOTIFICATION: {
            err_code = kaa_notification_manager_get_size(self->kaa_context->notification_manager
                                                , &extension_size);
            if (!err_code)
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Calculated notification extension size %u", extension_size);
            break;
        }
#endif
        default:
            extension_size = 0;
            break;
        }

        *expected_size += extension_size;
    }

    if (err_code) {
        KAA_LOG_ERROR(self->logger, err_code, "Failed to query extension size in %u service"
                                                                , services[services_count]);
    }

    return err_code;
}



static kaa_error_t kaa_client_sync_serialize(kaa_platform_protocol_t *self
                                           , const kaa_service_t services[]
                                           , size_t services_count
                                           , char* buffer
                                           , size_t *size)
{
    kaa_platform_message_writer_t *writer = NULL;
    kaa_error_t error_code = kaa_platform_message_writer_create(&writer, buffer, *size);
    KAA_RETURN_IF_ERR(error_code);

    uint16_t total_services_count = services_count + 1 /* Meta extension */;

    error_code = kaa_platform_message_header_write(writer, KAA_PLATFORM_PROTOCOL_ID, KAA_PLATFORM_PROTOCOL_VERSION);
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to write the client sync header");
        return error_code;
    }
    char *extension_count_p = writer->current;
    writer->current += KAA_PROTOCOL_EXTENSIONS_COUNT_SIZE;

    error_code = kaa_meta_data_request_serialize(self, writer, self->request_id);

    while (!error_code && services_count--) {
        switch (services[services_count]) {
        case KAA_SERVICE_BOOTSTRAP: {
            error_code = kaa_bootstrap_manager_bootstrap_request_serialize(self->kaa_context->bootstrap_manager, writer);
            if (error_code)
                KAA_LOG_ERROR(self->logger, error_code, "Failed to serialize the bootstrap extension");
            break;
        }
        case KAA_SERVICE_PROFILE: {
            bool need_resync = false;
            error_code = kaa_profile_need_profile_resync(self->kaa_context->profile_manager
                                                     , &need_resync);
            if (!error_code) {
                if (need_resync) {
                    error_code = kaa_profile_request_serialize(self->kaa_context->profile_manager, writer);
                    if (error_code)
                        KAA_LOG_ERROR(self->logger, error_code, "Failed to serialize the profile extension");
                } else {
                    --total_services_count;
                }
            } else {
                KAA_LOG_ERROR(self->logger, error_code, "Failed to read profile's 'need_resync' flag");
            }
            break;
        }
        case KAA_SERVICE_USER: {
            error_code = kaa_user_request_serialize(self->kaa_context->user_manager, writer);
            if (error_code)
                KAA_LOG_ERROR(self->logger, error_code, "Failed to serialize the user extension");
            break;
        }
        case KAA_SERVICE_EVENT: {
#ifndef KAA_DISABLE_FEATURE_EVENTS
            error_code = kaa_event_request_serialize(self->kaa_context->event_manager, self->request_id, writer);
            if (error_code)
                KAA_LOG_ERROR(self->logger, error_code, "Failed to serialize the event extension");
#else
            --total_services_count;
#endif
            break;
        }
        case KAA_SERVICE_LOGGING: {
#ifndef KAA_DISABLE_FEATURE_LOGGING
            bool need_resync = false;
            error_code = kaa_logging_need_logging_resync(self->kaa_context->log_collector, &need_resync);
            if (!error_code) {
                if (need_resync) {
                    error_code = kaa_logging_request_serialize(self->kaa_context->log_collector, writer);
                    if (error_code)
                         KAA_LOG_ERROR(self->logger, error_code, "Failed to serialize the logging extension");
                } else {
                    --total_services_count;
                }
            } else {
                KAA_LOG_ERROR(self->logger, error_code, "Failed to read logging's 'need_resync' flag");
            }
#else
            --total_services_count;
#endif
            break;
        }
        case KAA_SERVICE_CONFIGURATION: {
#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
            error_code = kaa_configuration_manager_request_serialize(self->kaa_context->configuration_manager, writer);
            if (error_code)
                KAA_LOG_ERROR(self->logger, error_code, "Failed to serialize the configuration extension");
#else
            --total_services_count;
#endif
            break;
        }
        case KAA_SERVICE_NOTIFICATION: {
#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
            error_code = kaa_notification_manager_request_serialize(self->kaa_context->notification_manager, writer);
            if (error_code)
                KAA_LOG_ERROR(self->logger, error_code, "Failed to serialize the configuration extension");
#else
            --total_services_count;
#endif
            break;
        }
        default:
            break;
        }
    }
    *(uint16_t *) extension_count_p = KAA_HTONS(total_services_count);
    *size = writer->current - writer->begin;
    kaa_platform_message_writer_destroy(writer);

    return error_code;
}



kaa_error_t kaa_platform_protocol_serialize_client_sync(kaa_platform_protocol_t *self
                                                      , const kaa_serialize_info_t *info
                                                      , char **buffer
                                                      , size_t *buffer_size)
{
    KAA_RETURN_IF_NIL4(self, info, buffer, buffer_size, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL3(info->allocator, info->services, info->services_count, KAA_ERR_BADDATA);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Serializing client sync...");

    *buffer_size = 0;
    kaa_error_t error = kaa_client_sync_get_size(self, info->services, info->services_count, buffer_size);
    KAA_RETURN_IF_ERR(error);

    KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Going to request sync buffer (size %zu)", *buffer_size);

    *buffer = info->allocator(info->allocator_context, *buffer_size);
    if (*buffer) {
        self->request_id++;
        error = kaa_client_sync_serialize(self, info->services, info->services_count, *buffer, buffer_size);
    } else {
        error = KAA_ERR_WRITE_FAILED;
    }

    if (error) {
        self->request_id--;
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Client sync serialized: request id '%u', payload size '%zu'", self->request_id, *buffer_size);
    }

    return error;
}



kaa_error_t kaa_platform_protocol_process_server_sync(kaa_platform_protocol_t *self
                                                    , const char *buffer
                                                    , size_t buffer_size)
{
    KAA_RETURN_IF_NIL3(self, buffer, buffer_size, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Server sync received: payload size '%zu'", buffer_size);

    kaa_platform_message_reader_t *reader = NULL;
    kaa_error_t error_code = kaa_platform_message_reader_create(&reader, buffer, buffer_size);
    KAA_RETURN_IF_ERR(error_code);

    uint32_t protocol_id = 0;
    uint16_t protocol_version = 0;
    uint16_t extension_count = 0;

    error_code = kaa_platform_message_header_read(reader, &protocol_id, &protocol_version, &extension_count);
    KAA_RETURN_IF_ERR(error_code);

    if (protocol_id != KAA_PLATFORM_PROTOCOL_ID) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BAD_PROTOCOL_ID, "Unsupported protocol ID %x", protocol_id);
        return KAA_ERR_BAD_PROTOCOL_ID;
    }
    if (protocol_version != KAA_PLATFORM_PROTOCOL_VERSION) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BAD_PROTOCOL_VERSION, "Unsupported protocol version %u", protocol_version);
        return KAA_ERR_BAD_PROTOCOL_VERSION;
    }

    uint32_t request_id = 0;
    uint16_t extension_type = 0;
    uint16_t extension_options = 0;
    uint32_t extension_length = 0;

    while (!error_code && kaa_platform_message_is_buffer_large_enough(reader, KAA_PROTOCOL_MESSAGE_HEADER_SIZE)) {

        error_code = kaa_platform_message_read_extension_header(reader
                                                              , &extension_type
                                                              , &extension_options
                                                              , &extension_length);
        KAA_RETURN_IF_ERR(error_code);

        switch (extension_type) {
        case KAA_BOOTSTRAP_EXTENSION_TYPE: {
            error_code = kaa_bootstrap_manager_handle_server_sync(self->kaa_context->bootstrap_manager
                                                                , reader
                                                                , extension_options
                                                                , extension_length);
            break;
        }
        case KAA_META_DATA_EXTENSION_TYPE: {
            KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received meta server sync: options 0, payload size %u", sizeof(uint32_t));
            error_code = kaa_platform_message_read(reader, &request_id, sizeof(uint32_t));
            request_id = KAA_NTOHL(request_id);
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Server sync request id %u", request_id);
            break;
        }
        case KAA_PROFILE_EXTENSION_TYPE: {
            error_code = kaa_profile_handle_server_sync(self->kaa_context->profile_manager
                                                      , reader
                                                      , extension_options
                                                      , extension_length);
            break;
        }
        case KAA_USER_EXTENSION_TYPE: {
            error_code = kaa_user_handle_server_sync(self->kaa_context->user_manager
                                                   , reader
                                                   , extension_options
                                                   , extension_length);
            break;
        }
#ifndef KAA_DISABLE_FEATURE_LOGGING
        case KAA_LOGGING_EXTENSION_TYPE: {
            error_code = kaa_logging_handle_server_sync(self->kaa_context->log_collector
                                                    , reader
                                                    , extension_options
                                                    , extension_length);
            break;
        }
#endif
#ifndef KAA_DISABLE_FEATURE_EVENTS
        case KAA_EVENT_EXTENSION_TYPE: {
            error_code = kaa_event_handle_server_sync(self->kaa_context->event_manager
                                                    , reader
                                                    , extension_options
                                                    , extension_length
                                                    , request_id);
            break;
        }
#endif
#ifndef KAA_DISABLE_FEATURE_CONFIGURATION
        case KAA_CONFIGURATION_EXTENSION_TYPE: {
            error_code = kaa_configuration_manager_handle_server_sync(self->kaa_context->configuration_manager
                                                    , reader
                                                    , extension_options
                                                    , extension_length);
            break;
        }
#endif
#ifndef KAA_DISABLE_FEATURE_NOTIFICATION
        case KAA_NOTIFICATION_EXTENSION_TYPE: {
            error_code = kaa_notification_manager_handle_server_sync(self->kaa_context->notification_manager
                                                   , reader
                                                   , extension_length);
            break;
        }
#endif
        default:
            KAA_LOG_WARN(self->logger, KAA_ERR_UNSUPPORTED,
                    "Unsupported extension received (type = %u)", extension_type);
            break;
        }
    }

    kaa_platform_message_reader_destroy(reader);

    if (!error_code) {
        error_code = kaa_status_save(self->status);
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Server sync successfully processed");
    } else {
        KAA_LOG_ERROR(self->logger, error_code,
                "Server sync is corrupted. Failed to read extension with type %u", extension_type);
    }

    return error_code;
}

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

#include "kaa_platform_protocol.h"

#include <string.h>

#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_context.h"
#include "kaa_defaults.h"
#include "kaa_status.h"

#include "kaa_event.h"
#include "kaa_profile.h"
#include "kaa_logging.h"
#include "kaa_user.h"

#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"

/** External user manager API */
extern kaa_error_t kaa_user_request_get_size(kaa_user_manager_t *self, size_t *expected_size);
extern kaa_error_t kaa_user_request_serialize(kaa_user_manager_t *self, kaa_platform_message_writer_t* writer);
extern kaa_error_t kaa_user_handle_server_sync(kaa_user_manager_t *self, kaa_platform_message_reader_t *reader, uint32_t extension_options, size_t extension_length);

/** External event manager API */
#ifndef KAA_DISABLE_FEATURE_EVENTS
extern kaa_error_t kaa_event_request_get_size(kaa_event_manager_t *self, size_t *expected_size);
extern kaa_error_t kaa_event_request_serialize(kaa_event_manager_t *self, size_t request_id, kaa_platform_message_writer_t *writer);
extern kaa_error_t kaa_event_handle_server_sync(kaa_event_manager_t *self, kaa_platform_message_reader_t *reader, uint32_t extension_options, size_t extension_length, size_t request_id);
#endif

/** External profile API */
extern kaa_error_t kaa_profile_need_profile_resync(kaa_profile_manager_t *kaa_context, bool *result);
extern kaa_error_t kaa_profile_request_get_size(kaa_profile_manager_t *self, size_t *expected_size);
extern kaa_error_t kaa_profile_request_serialize(kaa_profile_manager_t *self, kaa_platform_message_writer_t* writer);
extern kaa_error_t kaa_profile_handle_server_sync(kaa_profile_manager_t *self, kaa_platform_message_reader_t *reader, uint32_t extension_options, size_t extension_length);

/** External logging API */
#ifndef KAA_DISABLE_FEATURE_LOGGING
extern kaa_error_t kaa_logging_request_get_size(kaa_log_collector_t *self, size_t *expected_size);
extern kaa_error_t kaa_logging_request_serialize(kaa_log_collector_t *self, kaa_platform_message_writer_t *writer);
extern kaa_error_t kaa_logging_handle_server_sync(kaa_log_collector_t *self, kaa_platform_message_reader_t *reader, uint32_t extension_options, size_t extension_length);
#endif


struct kaa_platform_protocol_t
{
    kaa_context_t *kaa_context;
    uint32_t       request_id;
    kaa_logger_t  *logger;
};



static kaa_error_t kaa_meta_data_request_get_size(size_t *expected_size)
{
    KAA_RETURN_IF_NIL(expected_size, KAA_ERR_BADPARAM);

    bool is_timeout_needed = true; // FIXME: replace with valid check
    bool is_public_key_hash_needed = true; // FIXME: replace with valid check
    bool is_profile_hash_needed = true; // FIXME: replace with valid check
    bool is_token_needed = true; // FIXME: replace with valid check

    *expected_size += KAA_EXTENSION_HEADER_SIZE;
    *expected_size += sizeof(uint32_t); // request id

    if (is_timeout_needed) {
        *expected_size += sizeof(uint32_t); // timeout value
    }

    if (is_public_key_hash_needed) {
        *expected_size += kaa_aligned_size_get(SHA_1_DIGEST_LENGTH); // public key hash length
    }

    if (is_profile_hash_needed) {
        *expected_size += kaa_aligned_size_get(SHA_1_DIGEST_LENGTH); // profile hash length
    }

    if (is_token_needed) {
        *expected_size += sizeof(uint32_t); // token length
        *expected_size += kaa_aligned_size_get(strlen(APPLICATION_TOKEN)); // token
    }

    return KAA_ERR_NONE;
}



static kaa_error_t kaa_meta_data_request_serialize(kaa_context_t *context, kaa_platform_message_writer_t* writer)
{
    KAA_RETURN_IF_NIL2(context, writer, KAA_ERR_BADPARAM);

    bool is_timeout_needed = true; // FIXME: replace with valid check
    bool is_public_key_hash_needed = true; // FIXME: replace with valid check
    bool is_profile_hash_needed = true; // FIXME: replace with valid check
    bool is_token_needed = true; // FIXME: replace with valid check

    uint32_t token_len = (is_token_needed ? strlen(APPLICATION_TOKEN) : 0);

    uint32_t options = 0;
    options |= (is_timeout_needed ? TIMEOUT_VALUE : 0);
    options |= (is_public_key_hash_needed ? PUBLIC_KEY_HASH_VALUE : 0);
    options |= (is_profile_hash_needed ? PROFILE_HASH_VALUE : 0);
    options |= (is_token_needed ? APP_TOKEN_VALUE : 0);

    uint32_t payload_length = 0;
    payload_length += (is_timeout_needed ? sizeof(uint32_t) : 0);
    payload_length += (is_public_key_hash_needed ? kaa_aligned_size_get(SHA_1_DIGEST_LENGTH) : 0);
    payload_length += (is_profile_hash_needed ? kaa_aligned_size_get(SHA_1_DIGEST_LENGTH) : 0);
    payload_length += (is_token_needed ? sizeof(uint32_t) : 0);
    payload_length += (is_token_needed ? kaa_aligned_size_get(token_len) : 0);

    kaa_error_t err_code = kaa_platform_message_write_extension_header(
                                writer, KAA_META_DATA_EXTENSION_TYPE, options, payload_length);
    KAA_RETURN_IF_ERR(err_code);

    if (is_timeout_needed) {
        uint32_t timeout = KAA_HTONL(KAA_SYNC_TIMEOUT);
        err_code = kaa_platform_message_write(writer, &timeout, sizeof(timeout));
        KAA_RETURN_IF_ERR(err_code);
    }

    if (is_public_key_hash_needed) {
        kaa_digest_p pub_key_hash = NULL;
        err_code = kaa_status_get_endpoint_public_key_hash(context->status, &pub_key_hash);
        KAA_RETURN_IF_ERR(err_code);
        KAA_RETURN_IF_NIL(pub_key_hash, err_code);
        err_code = kaa_platform_message_write_aligned(writer, pub_key_hash, SHA_1_DIGEST_LENGTH);
        KAA_RETURN_IF_ERR(err_code);
    }

    if (is_profile_hash_needed) {
        kaa_digest_p profile_hash = NULL;
        err_code = kaa_status_get_profile_hash(context->status, &profile_hash);
        KAA_RETURN_IF_ERR(err_code);
        KAA_RETURN_IF_NIL(profile_hash, err_code);
        err_code = kaa_platform_message_write_aligned(writer, profile_hash, SHA_1_DIGEST_LENGTH);
        KAA_RETURN_IF_ERR(err_code);
    }

    if (is_token_needed) {
        uint32_t net_order_token_len = KAA_HTONL(token_len);
        err_code = kaa_platform_message_write(writer, &net_order_token_len, sizeof(uint32_t));
        KAA_RETURN_IF_ERR(err_code);
        err_code = kaa_platform_message_write_aligned(writer, APPLICATION_TOKEN, token_len);
        KAA_RETURN_IF_ERR(err_code);
    }

    return err_code;
}



/* static kaa_sync_request_meta_data_t* create_sync_request_meta_data(kaa_context_t *context)
{
    kaa_sync_request_meta_data_t *request = kaa_sync_request_meta_data_create();
    request->application_token = kaa_string_move_create(APPLICATION_TOKEN, NULL);
    request->timeout = 60000L;

    kaa_digest_p pub_key_hash = NULL;
    if (kaa_status_get_endpoint_public_key_hash(context->status, &pub_key_hash)) {
        // FIXME: error handling
    }
    if (pub_key_hash) {
        request->endpoint_public_key_hash =
                kaa_bytes_copy_create(pub_key_hash, SHA_1_DIGEST_LENGTH, kaa_data_destroy);
    }

    kaa_digest_p profile_hash = NULL;
    if (kaa_status_get_profile_hash(context->status, &profile_hash)) {
        // FIXME: error handling
    }

    if (profile_hash) {
        request->profile_hash = kaa_union_bytes_or_null_branch_0_create();
        if (request->profile_hash) {
            request->profile_hash->data = kaa_bytes_copy_create(
                    profile_hash, SHA_1_DIGEST_LENGTH, kaa_data_destroy);
        }
    } else {
        request->profile_hash = kaa_union_bytes_or_null_branch_1_create();
    }

    return request;
} */


/* static kaa_error_t kaa_compile_request(kaa_platform_protocol_t *self, kaa_sync_request_t **request_p
        , size_t *result_size, size_t service_count, const kaa_service_t services[])
{
    kaa_sync_request_t *request = kaa_sync_request_create();
    KAA_RETURN_IF_NIL(request, KAA_ERR_NOMEM);

    request->request_id = kaa_union_int_or_null_branch_0_create();
    request->request_id->data = (uint32_t *) KAA_MALLOC(sizeof(uint32_t));
    *((uint32_t *)request->request_id->data) = self->request_id;

    request->sync_request_meta_data = kaa_union_sync_request_meta_data_or_null_branch_0_create();
    request->sync_request_meta_data->data = create_sync_request_meta_data(self->kaa_context);

    request->user_sync_request = kaa_union_user_sync_request_or_null_branch_0_create();
    kaa_user_compile_request(self->kaa_context->user_manager
                           , (kaa_user_sync_request_t **)&request->user_sync_request->data
                           , self->request_id);

    request->event_sync_request = kaa_union_event_sync_request_or_null_branch_1_create();
    request->log_sync_request = kaa_union_log_sync_request_or_null_branch_1_create();
    request->notification_sync_request = kaa_union_notification_sync_request_or_null_branch_1_create();
    request->configuration_sync_request = kaa_union_configuration_sync_request_or_null_branch_1_create();
    request->profile_sync_request = kaa_union_profile_sync_request_or_null_branch_1_create();

    for (;service_count--;) {
        switch (services[service_count]) {
#ifndef KAA_DISABLE_FEATURE_EVENTS
        // TODO: remove
//        case KAA_SERVICE_EVENT: {
//            request->event_sync_request->destroy(request->event_sync_request);
//            request->event_sync_request = kaa_union_event_sync_request_or_null_branch_0_create();
//            kaa_event_compile_request(self->kaa_context->event_manager
//                                    , (kaa_event_sync_request_t**)&request->event_sync_request->data
//                                    , self->request_id);
//            break;
//        }
#endif
        case KAA_SERVICE_PROFILE: {
            bool need_resync = false;
            kaa_error_t error = kaa_profile_need_profile_resync(self->kaa_context->profile_manager, &need_resync);
            if (error) {
                request->destroy(request);
                return error;
            }

            if (need_resync) {
                request->profile_sync_request->destroy(request->profile_sync_request);
                request->profile_sync_request = kaa_union_profile_sync_request_or_null_branch_0_create();

                if (!request->profile_sync_request) {
                    request->destroy(request);
                    return KAA_ERR_NOMEM;
                }

                error = kaa_profile_compile_request(self->kaa_context->profile_manager
                        , (kaa_profile_sync_request_t **)&request->profile_sync_request->data);

                if (error) {
                    request->destroy(request);
                    return error;
                }
            }
            break;
        }
#ifndef KAA_DISABLE_FEATURE_LOGGING
        case KAA_SERVICE_LOGGING: {
            kaa_log_sync_request_t *log_request = NULL;
            kaa_logging_compile_request(self->kaa_context->log_collector, &log_request);
            if (log_request) {
                request->log_sync_request->destroy(request->log_sync_request);
                request->log_sync_request =
                        kaa_union_log_sync_request_or_null_branch_0_create();
                request->log_sync_request->data = log_request;
            }
            break;
        }
#endif
        default:
            break;
        }
    }

    *request_p = request;
    *result_size = request->get_size(request);
    return KAA_ERR_NONE;
} */



kaa_error_t kaa_platform_protocol_create(kaa_platform_protocol_t **platform_protocol_p
                                       , kaa_context_t *context
                                       , kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL3(platform_protocol_p, context, logger, KAA_ERR_BADPARAM);

    *platform_protocol_p = KAA_MALLOC(sizeof(kaa_platform_protocol_t));
    KAA_RETURN_IF_NIL(*platform_protocol_p, KAA_ERR_NOMEM);

    (*platform_protocol_p)->request_id = 0;
    (*platform_protocol_p)->kaa_context = context;
    (*platform_protocol_p)->logger = logger;
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
    KAA_RETURN_IF_NIL4(self, services, services_count, expected_size, KAA_ERR_BADPARAM)

    *expected_size = KAA_PROTOCOL_MESSAGE_HEADER_SIZE;

    size_t extension_size = 0;
    kaa_error_t err_code = kaa_meta_data_request_get_size(&extension_size);

    for (;!err_code && services_count--;) {
        *expected_size += extension_size;

        switch (services[services_count]) {
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
            }
            break;
        }
        case KAA_SERVICE_USER: {
            err_code = kaa_user_request_get_size(self->kaa_context->user_manager
                                               , &extension_size);
            break;
        }
#ifndef KAA_DISABLE_FEATURE_EVENTS
        case KAA_SERVICE_EVENT: {
            err_code = kaa_event_request_get_size(self->kaa_context->event_manager
                                                , &extension_size);
            break;
        }
#endif
#ifndef KAA_DISABLE_FEATURE_LOGGING
        case KAA_SERVICE_LOGGING: {
            err_code = kaa_logging_request_get_size(self->kaa_context->log_collector
                                                , &extension_size);
            break;
        }
#endif
        default:
            extension_size = 0;
            break;
        }
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
                                           , size_t size)
{
    KAA_RETURN_IF_NIL5(self, services, services_count, buffer, size, KAA_ERR_BADPARAM);

    kaa_platform_message_writer_t *writer = NULL;
    kaa_error_t err_code = kaa_platform_message_writer_create(&writer, buffer, size);
    KAA_RETURN_IF_ERR(err_code);

    err_code = kaa_meta_data_request_serialize(self->kaa_context, writer);

    for (;!err_code && services_count--;) {
        switch (services[services_count]) {
        case KAA_SERVICE_PROFILE: {
            bool need_resync = false;
            err_code = kaa_profile_need_profile_resync(self->kaa_context->profile_manager
                                                     , &need_resync);
            if (err_code) {
                KAA_LOG_ERROR(self->logger, err_code, "Failed to read 'need_resync' flag");
            }
            err_code = kaa_profile_request_serialize(self->kaa_context->profile_manager, writer);
            break;
        }
        case KAA_SERVICE_USER: {
            err_code = kaa_user_request_serialize(self->kaa_context->user_manager, writer);
            break;
        }
#ifndef KAA_DISABLE_FEATURE_EVENTS
        case KAA_SERVICE_EVENT: {
            err_code = kaa_event_request_serialize(self->kaa_context->event_manager, self->request_id, writer);
            break;
        }
#endif
#ifndef KAA_DISABLE_FEATURE_LOGGING
        case KAA_SERVICE_LOGGING: {
            err_code = kaa_logging_request_serialize(self->kaa_context->log_collector, writer);
            break;
        }
#endif
        default:
            break;
        }
    }

    kaa_platform_message_writer_destroy(writer);

    return err_code;
}



kaa_error_t kaa_platform_protocol_serialize_client_sync(kaa_platform_protocol_t *self
                                                      , const kaa_service_t services[]
                                                      , size_t services_count
                                                      , kaa_buffer_alloc_fn allocator
                                                      , void *allocator_context)
{
    KAA_RETURN_IF_NIL4(self, services, services_count, allocator, KAA_ERR_BADPARAM);

    size_t buffer_size = 0;
    kaa_error_t error = kaa_client_sync_get_size(self, services, services_count, &buffer_size);
    KAA_RETURN_IF_ERR(error)

    char *buffer = allocator(allocator_context, buffer_size);
    if (buffer) {
        self->request_id++;
        error = kaa_client_sync_serialize(self, services, services_count, buffer, buffer_size);
    } else {
        error = KAA_ERR_WRITE_FAILED;
    }

    if (error) {
        self->request_id--;
    }

    return error;
}



kaa_error_t kaa_platform_protocol_process_server_sync(kaa_platform_protocol_t *self
                                                    , const char *buffer
                                                    , size_t buffer_size)
{
    KAA_RETURN_IF_NIL3(self, buffer, buffer_size, KAA_ERR_BADPARAM);

    kaa_platform_message_reader_t *reader = NULL;
    kaa_error_t error_code = kaa_platform_message_reader_create(&reader, buffer, buffer_size);
    KAA_RETURN_IF_ERR(error_code);

    uint32_t protocol_id = 0;
    uint16_t protocol_version = 0;
    uint16_t extension_count = 0;

    error_code = kaa_platform_message_header_read(reader, &protocol_id, &protocol_version, &extension_count);
    KAA_RETURN_IF_ERR(error_code);

    uint32_t request_id = 0;
    uint8_t extension_type = 0;
    uint32_t extension_options = 0;
    uint32_t extension_length = 0;

    while (!error_code && kaa_platform_message_is_buffer_large_enough(reader, KAA_PROTOCOL_MESSAGE_HEADER_SIZE)) {
        error_code = kaa_platform_message_read_extension_header(
                reader, &extension_type, &extension_options, &extension_length);
        KAA_RETURN_IF_ERR(error_code);

        switch (extension_type) {
        case KAA_META_DATA_EXTENSION_TYPE: {
            error_code = kaa_platform_message_read(reader, &request_id, sizeof(uint32_t));
            request_id = KAA_NTOHL(request_id);
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
        default:
            KAA_LOG_WARN(self->logger, KAA_ERR_UNSUPPORTED,
                    "Unsupported extension received (type = %u)", extension_type);
            break;
        }
    }

    kaa_platform_message_reader_destroy(reader);

    if (!error_code) {
        error_code = kaa_status_save(self->kaa_context->status);
    } else {
        KAA_LOG_ERROR(self->logger, error_code,
                "Server sync is corrupted. Failed to read extension with type %u", extension_type);
    }

    return error_code;
}

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

#include <kaa_profile.h>
#include "kaa_profile_private.h"

#include "kaa_private.h"

#include <string.h>
#include <stdbool.h>
#include <inttypes.h>
#include <sys/types.h>
#include "platform/sock.h"
#include "avro_src/avro/io.h"
#include "platform/ext_sha.h"
#include "platform/ext_key_utils.h"
#include "kaa_status.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_defaults.h"
#include "kaa_channel_manager.h"
#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"



#define KAA_PROFILE_RESYNC_OPTION 0x1

static kaa_extension_id profile_sync_services[] = { KAA_EXTENSION_PROFILE };

typedef struct {
    size_t payload_size;
    kaa_bytes_t public_key;
    kaa_bytes_t access_token;
} kaa_profile_extension_data_t;

struct kaa_profile_manager_t {
    bool need_resync;
    kaa_bytes_t profile_body;
    kaa_digest profile_hash;
    kaa_channel_manager_t *channel_manager;
    kaa_status_t *status;
    kaa_logger_t *logger;
    kaa_profile_extension_data_t *extension_data;
};

kaa_error_t kaa_extension_profile_init(kaa_context_t *kaa_context, void **context)
{
    kaa_error_t result = kaa_profile_manager_create(&kaa_context->profile_manager,
        kaa_context->status->status_instance,
        kaa_context->channel_manager, kaa_context->logger);
    *context = kaa_context->profile_manager;
    return result;
}

kaa_error_t kaa_extension_profile_deinit(void *context)
{
    kaa_profile_manager_destroy(context);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_extension_profile_request_get_size(void *context,
        size_t *expected_size)
{
    return kaa_profile_request_get_size(context, expected_size);
}

static bool resync_is_required(kaa_profile_manager_t *self)
{
    return self->need_resync || self->status->profile_needs_resync;
}

kaa_error_t kaa_extension_profile_request_serialize(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *need_resync)
{
    (void)request_id;

    // TODO(KAA-982): Use asserts
    if (!context || !size || !need_resync) {
        return KAA_ERR_BADPARAM;
    }

    *need_resync = resync_is_required(context);
    if (!*need_resync) {
        *size = 0;
        return KAA_ERR_NONE;
    }

    size_t size_needed;
    kaa_error_t error = kaa_profile_request_get_size(context, &size_needed);
    if (error) {
        return error;
    }

    if (!buffer || *size < size_needed) {
        *size = size_needed;
        return KAA_ERR_BUFFER_IS_NOT_ENOUGH;
    }

    *size = size_needed;

    kaa_platform_message_writer_t writer = KAA_MESSAGE_WRITER(buffer, *size);
    error = kaa_profile_request_serialize(context, &writer);
    if (error) {
        return error;
    }

    *size = writer.current - buffer;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_extension_profile_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{
    (void)request_id;

    // TODO(KAA-982): Use asserts
    if (!context || !buffer) {
        return KAA_ERR_BADPARAM;
    }

    kaa_platform_message_reader_t reader = KAA_MESSAGE_READER(buffer, size);
    return kaa_profile_handle_server_sync(context, &reader, extension_options, size);
}

/*
 * PUBLIC FUNCTIONS
 */
/** @deprecated Use kaa_extension_profile_init(). */
kaa_error_t kaa_profile_manager_create(kaa_profile_manager_t **profile_manager_p, kaa_status_t *status
        , kaa_channel_manager_t *channel_manager, kaa_logger_t *logger)
{
    if (!profile_manager_p || !channel_manager || !status) {
        return KAA_ERR_BADPARAM;
    }

    kaa_profile_manager_t *profile_manager = KAA_MALLOC(sizeof(kaa_profile_manager_t));
    if (!profile_manager) {
        return KAA_ERR_NOMEM;
    }

    /**
     * KAA_CALLOC is really needed.
     */
    profile_manager->extension_data = KAA_CALLOC(1, sizeof(kaa_profile_extension_data_t));
    if (!profile_manager->extension_data) {
        KAA_FREE(profile_manager);
        return KAA_ERR_NOMEM;
    }

    profile_manager->need_resync = true;

    profile_manager->profile_body.size = 0;
    profile_manager->profile_body.buffer = NULL;
    profile_manager->channel_manager = channel_manager;
    profile_manager->status = status;
    profile_manager->logger = logger;

    ext_calculate_sha_hash(NULL, 0, profile_manager->profile_hash);
    ext_copy_sha_hash(profile_manager->status->profile_hash, profile_manager->profile_hash);

    *profile_manager_p = profile_manager;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_profile_force_sync(kaa_profile_manager_t *self)
{
    if (!self)
        return KAA_ERR_BADPARAM;

    kaa_transport_channel_interface_t *channel =
            kaa_channel_manager_get_transport_channel(
                    self->channel_manager, KAA_EXTENSION_PROFILE);
    if (!channel) {
        return KAA_ERR_NOT_FOUND;
    }

    channel->sync_handler(channel->context, profile_sync_services, 1);
    return KAA_ERR_NONE;
}

bool kaa_profile_manager_is_profile_set(kaa_profile_manager_t *self)
{
#if KAA_PROFILE_SCHEMA_VERSION > 0
    return self->profile_body.buffer != NULL && self->profile_body.size != 0;
#else
    return true;
#endif
}



/** @deprecated Use kaa_extension_profile_deinit(). */
void kaa_profile_manager_destroy(kaa_profile_manager_t *self)
{
    if (self) {
        if (self->profile_body.buffer && self->profile_body.size > 0) {
            KAA_FREE(self->profile_body.buffer);
        }
        if (self->extension_data) {
            if (self->extension_data->public_key.buffer && self->extension_data->public_key.destroy) {
                self->extension_data->public_key.destroy(self->extension_data->public_key.buffer);
            }
            KAA_FREE(self->extension_data);
        }
        KAA_FREE(self);
    }
}



kaa_error_t kaa_profile_need_profile_resync(kaa_profile_manager_t *self, bool *result)
{
    KAA_RETURN_IF_NIL2(self, result, KAA_ERR_BADPARAM);
    *result = resync_is_required(self);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_profile_request_get_size(kaa_profile_manager_t *self, size_t *expected_size)
{
    // TODO(KAA-982): Use asserts
    if (!self || !expected_size) {
        return KAA_ERR_BADPARAM;
    }

    if (!resync_is_required(self)) {
        *expected_size = 0;
        return KAA_ERR_NONE;
    }

    *expected_size = KAA_EXTENSION_HEADER_SIZE;
    *expected_size += sizeof(uint32_t); // profile body size
#if KAA_PROFILE_SCHEMA_VERSION > 0
    if (resync_is_required(self))
        *expected_size += kaa_aligned_size_get(self->profile_body.size); // profile data
#endif

    if (!self->status->is_registered) {
        if (!self->extension_data->public_key.buffer) {
            ext_get_endpoint_public_key((const uint8_t **)&self->extension_data->public_key.buffer,
                                        (size_t *)&self->extension_data->public_key.size);
        }

        if (self->extension_data->public_key.buffer && self->extension_data->public_key.size > 0) {
            *expected_size += sizeof(uint32_t); // public key size
            *expected_size += kaa_aligned_size_get(self->extension_data->public_key.size); // public key

        } else {
            return KAA_ERR_BADDATA;
        }
    }

    self->extension_data->access_token.buffer = (uint8_t *) self->status->endpoint_access_token;
    if (self->extension_data->access_token.buffer) {
        self->extension_data->access_token.size = strlen((const char*)self->extension_data->access_token.buffer);
        *expected_size += sizeof(uint32_t); // access token length
        *expected_size += kaa_aligned_size_get(self->extension_data->access_token.size); // access token
    }
    self->extension_data->payload_size = *expected_size - KAA_EXTENSION_HEADER_SIZE;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_profile_request_serialize(kaa_profile_manager_t *self, kaa_platform_message_writer_t *writer)
{
    KAA_RETURN_IF_NIL2(self, writer, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to compile profile client sync");

    kaa_error_t error_code = kaa_platform_message_write_extension_header(writer
                                                                       , KAA_EXTENSION_PROFILE
                                                                       , 0
                                                                       , self->extension_data->payload_size);
    KAA_RETURN_IF_ERR(error_code);

    uint32_t network_order_32 = KAA_HTONL(0);
#if KAA_PROFILE_SCHEMA_VERSION > 0
    if (resync_is_required(self)) {
        network_order_32 = KAA_HTONL(self->profile_body.size);
        error_code = kaa_platform_message_write(writer, &network_order_32, sizeof(uint32_t));
        KAA_RETURN_IF_ERR(error_code);
        if (self->profile_body.size) {
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Writing profile body (size %u)...", self->profile_body.size);
            error_code = kaa_platform_message_write_aligned(writer, self->profile_body.buffer, self->profile_body.size);
            if (error_code) {
                KAA_LOG_ERROR(self->logger, error_code, "Failed to write profile body");
                return error_code;
            }
        }
    } else {
        error_code = kaa_platform_message_write(writer, &network_order_32, sizeof(uint32_t));
        KAA_RETURN_IF_ERR(error_code);
    }
#else
    error_code = kaa_platform_message_write(writer, &network_order_32, sizeof(uint32_t));
    KAA_RETURN_IF_ERR(error_code);
#endif

    uint16_t network_order_16 = 0;
    uint16_t field_number_with_reserved = 0;

    if (!self->status->is_registered) {
        field_number_with_reserved = KAA_HTONS(PUB_KEY_VALUE << 8);
        error_code = kaa_platform_message_write(writer
                                             , &field_number_with_reserved
                                             , sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);

        network_order_16 = KAA_HTONS(self->extension_data->public_key.size);
        error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);

        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Writing public key (size %u)...", self->extension_data->public_key.size);
        error_code = kaa_platform_message_write_aligned(writer
                                                     , (char*)self->extension_data->public_key.buffer
                                                     , self->extension_data->public_key.size);
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to write public key");
            return error_code;
        }
    }

    if (self->extension_data->access_token.buffer) {
        field_number_with_reserved = KAA_HTONS(ACCESS_TOKEN_VALUE << 8);
        error_code = kaa_platform_message_write(writer
                                             , &field_number_with_reserved
                                             , sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);

        network_order_16 = KAA_HTONS(self->extension_data->access_token.size);
        error_code = kaa_platform_message_write(writer, &network_order_16, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);

        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Writing access token (size %u)...", self->extension_data->access_token.size);
        error_code = kaa_platform_message_write_aligned(writer
                                                     , (char*)self->extension_data->access_token.buffer
                                                     , self->extension_data->access_token.size);
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to write access token");
            return error_code;
        }
    }

    return error_code;
}



kaa_error_t kaa_profile_handle_server_sync(kaa_profile_manager_t *self
                                         , kaa_platform_message_reader_t *reader
                                         , uint16_t extension_options
                                         , size_t extension_length)
{
    // Only used for logging
    (void)extension_options;
    (void)extension_length;
    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received profile server sync: options %u, payload size %zu", extension_options, extension_length);

    kaa_error_t error_code = KAA_ERR_NONE;

    self->need_resync = false;
    if (extension_options & KAA_PROFILE_RESYNC_OPTION) {
        self->need_resync = true;
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to resync profile...");

        /* Ignoring an error code: the channels can be not initialized */
        (void)kaa_profile_force_sync(self);
    }


    if (!self->status->is_registered) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Endpoint has been registered");
        self->status->is_registered = true;
    }

    return error_code;
}

kaa_error_t kaa_profile_manager_update_profile(kaa_profile_manager_t *self, kaa_profile_t *profile_body)
{
#if KAA_PROFILE_SCHEMA_VERSION > 0
    KAA_RETURN_IF_NIL2(self, profile_body, KAA_ERR_BADPARAM);

    size_t serialized_profile_size = profile_body->get_size(profile_body);
    if (!serialized_profile_size) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BADDATA,
                      "Failed to update profile: serialize profile size is null. Maybe profile schema is empty");
        return KAA_ERR_BADDATA;
    }

    char *serialized_profile = (char *) KAA_MALLOC(serialized_profile_size * sizeof(char));
    KAA_RETURN_IF_NIL(serialized_profile, KAA_ERR_NOMEM);

    avro_writer_t writer = avro_writer_memory(serialized_profile, serialized_profile_size);
    if (!writer) {
        KAA_FREE(serialized_profile);
        return KAA_ERR_NOMEM;
    }
    profile_body->serialize(writer, profile_body);
    avro_writer_free(writer);

    kaa_digest new_hash;
    ext_calculate_sha_hash(serialized_profile, serialized_profile_size, new_hash);

    if (!memcmp(new_hash, self->status->profile_hash, SHA_1_DIGEST_LENGTH)) {
        self->need_resync = false;
        KAA_FREE(serialized_profile);
        return KAA_ERR_NONE;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Endpoint profile is updated");

    if (ext_copy_sha_hash(self->status->profile_hash, new_hash)) {
        KAA_FREE(serialized_profile);
        return KAA_ERR_BAD_STATE;
    }

    if (self->profile_body.size > 0) {
        KAA_FREE(self->profile_body.buffer);
        self->profile_body.buffer = NULL;
    }

    self->profile_body.buffer = (uint8_t*)serialized_profile;
    self->profile_body.size = serialized_profile_size;

    self->need_resync = true;

    /* Ignoring an error code: the channels can be not initialized */
    (void)kaa_profile_force_sync(self);
#endif
    return KAA_ERR_NONE;
}

kaa_error_t kaa_profile_manager_set_endpoint_access_token(kaa_profile_manager_t *self, const char *token)
{
    KAA_RETURN_IF_NIL2(self, token, KAA_ERR_BADPARAM);

    kaa_error_t error =  kaa_status_set_endpoint_access_token(self->status, token);
    if (error == KAA_ERR_NONE) {
        self->need_resync = true;
        /* Ignoring an error code: the channels can be not initialized */
        (void)kaa_profile_force_sync(self);
    }

    return error;
}

kaa_error_t kaa_profile_manager_get_endpoint_id(kaa_profile_manager_t *self, kaa_endpoint_id_p result_id)
{
    KAA_RETURN_IF_NIL2(self, result_id, KAA_ERR_BADPARAM);
    return ext_copy_sha_hash((kaa_digest_p) result_id, self->status->endpoint_public_key_hash);
}

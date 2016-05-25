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
#include "kaa_defaults.h"

#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"

#include <kaa_extension.h>

#ifndef KAA_DISABLE_FEATURE_PROFILE
#include "kaa_profile_private.h"
#endif

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
    kaa_status_t *status;
    kaa_logger_t *logger;
    uint32_t      request_id;
};

/**
 * That's a function that aids transition to the new interface. Its
 * usages should be removed.
 */
static kaa_error_t get_extension_request_size(kaa_extension_id id, size_t *size);

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
        kaa_logger_t *logger, kaa_status_t *status)
{
    if (!platform_protocol_p || !logger || !status) {
        return KAA_ERR_BADPARAM;
    }

    *platform_protocol_p = KAA_MALLOC(sizeof(**platform_protocol_p));
    if (!*platform_protocol_p) {
        return KAA_ERR_NOMEM;
    }

    **platform_protocol_p = (kaa_platform_protocol_t){
        .request_id = 0,
        .status = status,
        .logger = logger,
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

        kaa_error_t error = get_extension_request_size(extensions[i], &extension_size);
        if (error && error != KAA_ERR_NOT_FOUND) {
            KAA_LOG_ERROR(self->logger, error,
                    "Failed to query extension size for %u", extensions[i]);
            return error;
        }

        *expected_size += extension_size;
    }

    return KAA_ERR_NONE;
}

static kaa_error_t kaa_client_sync_serialize(kaa_platform_protocol_t *self,
        const kaa_extension_id services[], size_t services_count, uint8_t *buffer, size_t *size)
{
    kaa_platform_message_writer_t writer = KAA_MESSAGE_WRITER(buffer, *size);

    uint16_t total_services_count = services_count + 1 /* Meta extension */;

    kaa_error_t error_code = kaa_platform_message_header_write(&writer,
            KAA_PLATFORM_PROTOCOL_ID, KAA_PLATFORM_PROTOCOL_VERSION);
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to write the client sync header");
        goto fail;
    }

    uint16_t *extension_count_p = (uint16_t *)writer.current;
    writer.current += KAA_PROTOCOL_EXTENSIONS_COUNT_SIZE;
    // TODO: static assert KAA_PROTOCOL_EXTENSIONS_COUNT_SIZE == sizeof(uint16_t)

    error_code = kaa_meta_data_request_serialize(self, &writer, self->request_id);
    if (error_code) {
        goto fail;
    }

    while (!error_code && services_count--) {
        size_t size_required = writer.end - writer.current;
        bool need_resync = false;
        error_code = kaa_extension_request_serialize(services[services_count],
                self->request_id, writer.current, &size_required, &need_resync);
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code,
                    "Failed to serialize the '%d' extension", services[services_count]);
            continue;
        }

        if (!need_resync) {
            --total_services_count;
            continue;
        }

        writer.current += size_required;
    }

    *extension_count_p = KAA_HTONS(total_services_count);
    *size = writer.current - writer.begin;

fail:
    return error_code;
}

kaa_error_t kaa_platform_protocol_serialize_client_sync(kaa_platform_protocol_t *self,
        const kaa_extension_id *services, size_t services_count,
        uint8_t *buffer, size_t *buffer_size)
{
    if (!self || !buffer_size) {
        return KAA_ERR_BADPARAM;
    }

    if (!services || services_count == 0) {
        return KAA_ERR_BADDATA;
    }

    size_t required_buffer_size = 0;
    kaa_error_t error = kaa_client_sync_get_size(self, services, services_count,
            &required_buffer_size);
    if (error) {
        KAA_LOG_ERROR(self->logger, error, "Failed to get required buffer size");
        return error;
    }

    if (*buffer_size < required_buffer_size || !buffer) {
        *buffer_size = required_buffer_size;
        return KAA_ERR_BUFFER_IS_NOT_ENOUGH;
    }
    *buffer_size = required_buffer_size;

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Serializing client sync...");

    self->request_id++;
    error = kaa_client_sync_serialize(self, services, services_count, buffer, buffer_size);
    if (error) {
        self->request_id--;
        return error;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE,
            "Client sync serialized: request id '%u', payload size '%zu'",
            self->request_id, *buffer_size);

    return KAA_ERR_NONE;
}

// TODO(KAA-1089): Remove weak linkage
__attribute__((weak))
kaa_error_t kaa_platform_protocol_process_server_sync(kaa_platform_protocol_t *self,
        const uint8_t *buffer, size_t buffer_size)
{
    if (!self || !buffer || buffer_size == 0) {
        return KAA_ERR_BADPARAM;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE,
            "Server sync received: payload size '%zu'", buffer_size);

    kaa_platform_message_reader_t reader = KAA_MESSAGE_READER(buffer, buffer_size);

    uint32_t protocol_id = 0;
    uint16_t protocol_version = 0;
    uint16_t extension_count = 0;

    kaa_error_t error_code = kaa_platform_message_header_read(&reader,
            &protocol_id, &protocol_version, &extension_count);
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

    while (kaa_platform_message_is_buffer_large_enough(&reader, KAA_PROTOCOL_MESSAGE_HEADER_SIZE)) {

        uint16_t extension_type;
        uint16_t extension_options;
        uint32_t extension_length;
        error_code = kaa_platform_message_read_extension_header(&reader, &extension_type,
                &extension_options, &extension_length);
        if (error_code) {
            KAA_LOG_ERROR(self->logger, error_code, "Failed to read extension header");
            goto fail;
        }

        /* Do not resync unless it is requested by the metadata extension */

        // TODO: must profile_needs_resync be set inside of the loop?
        self->status->profile_needs_resync = false;

        if (extension_type == KAA_EXTENSION_META_DATA) {
            error_code = kaa_platform_message_read(&reader, &request_id, sizeof(request_id));
            if (error_code) {
                KAA_LOG_ERROR(self->logger, error_code, "Failed to read meta data (request_id)");
                goto fail;
            }

            request_id = KAA_NTOHL(request_id);
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Server sync request id %u", request_id);

            /* Check if managers needs resync */

            uint32_t resync_request;
            error_code = kaa_platform_message_read(&reader,
                    &resync_request, sizeof(resync_request));
            if (error_code) {
                KAA_LOG_ERROR(self->logger, error_code, "Failed to read meta data (resync_request)");
                goto fail;
            }

            resync_request = KAA_NTOHL(resync_request);
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE,
                    "Server resync request %u", resync_request);

            if (resync_request & KAA_PROFILE_RESYNC_FLAG) {
                KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Profile resync is requested");
                self->status->profile_needs_resync = true;

#ifndef KAA_DISABLE_FEATURE_PROFILE
                void *profile_ctx = kaa_extension_get_context(KAA_EXTENSION_PROFILE);
                if (!profile_ctx) {
                    error_code = KAA_ERR_NOT_FOUND;
                    KAA_LOG_ERROR(self->logger, error_code,
                            "Profile extension is not found. Force resync can't be done");
                    goto fail;
                }

                error_code = kaa_profile_force_sync(profile_ctx);
                if (error_code) {
                    KAA_LOG_ERROR(self->logger, error_code, "Failed to force-sync profile");
                    goto fail;
                }
#endif
            }
        } else {
            error_code = kaa_extension_server_sync(extension_type, request_id,
                    extension_options, reader.current, extension_length);
            reader.current += extension_length;

            if (error_code == KAA_ERR_NOT_FOUND) {
                KAA_LOG_WARN(self->logger, KAA_ERR_UNSUPPORTED,
                        "Unsupported extension received (type = %u)", extension_type);
                error_code = KAA_ERR_NONE;
            } else if (error_code) {
                KAA_LOG_ERROR(self->logger, error_code,
                        "Server sync is corrupted. Failed to read extension with type %u",
                        extension_type);
                goto fail;
            }
        }
    }

    error_code = kaa_status_save(self->status);
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to save status");
        goto fail;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Server sync successfully processed");

fail:
    return error_code;
}

// TODO(KAA-1089): Remove weak linkage
__attribute__((weak))
kaa_error_t kaa_platform_protocol_alloc_serialize_client_sync(kaa_platform_protocol_t *self,
        const kaa_extension_id *services, size_t services_count,
        uint8_t **buffer, size_t *buffer_size)
{
    if (!self || !buffer || !buffer_size) {
        return KAA_ERR_BADPARAM;
    }

    if (!services || services_count == 0) {
        return KAA_ERR_BADDATA;
    }

    *buffer_size = 0;
    kaa_error_t error = kaa_platform_protocol_serialize_client_sync(self, services, services_count,
            NULL, buffer_size);
    // TODO(982): assert error != KAA_ERR_NONE
    if (error != KAA_ERR_BUFFER_IS_NOT_ENOUGH) {
        return error;
    }

    *buffer = KAA_MALLOC(*buffer_size);
    if (!*buffer) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "No memory for buffer");
        return KAA_ERR_NOMEM;
    }

    return kaa_platform_protocol_serialize_client_sync(self, services, services_count,
            *buffer, buffer_size);
}

static kaa_error_t get_extension_request_size(kaa_extension_id id, size_t *size)
{
    bool need_resync;
    kaa_error_t error = kaa_extension_request_serialize(id, 0, NULL, size, &need_resync);
    if (error == KAA_ERR_BUFFER_IS_NOT_ENOUGH) {
        error = KAA_ERR_NONE;
    }

    if (!need_resync) {
        *size = 0;
    }

    return error;
}

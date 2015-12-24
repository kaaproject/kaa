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

#include "plugins/kaa_plugin.h"

/** External status API */
extern kaa_error_t kaa_status_save(kaa_status_t *self);


struct kaa_platform_protocol_t
{
    kaa_context_t *kaa_context;
    kaa_status_t  *status;
    kaa_logger_t  *logger;
    uint32_t       request_id;
    uint32_t       sync_request_id;
};

kaa_error_t kaa_platform_protocol_get_context(kaa_platform_protocol_t *self, kaa_context_t **context)
{
    KAA_RETURN_IF_NIL2(self, context, KAA_ERR_BADPARAM);
    *context = self->kaa_context;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_platform_protocol_get_status(kaa_platform_protocol_t *self, kaa_status_t **status)
{
    KAA_RETURN_IF_NIL2(self, status, KAA_ERR_BADPARAM);
    *status = self->status;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_platform_protocol_get_request_id(kaa_platform_protocol_t *self, uint32_t *request_id)
{
    KAA_RETURN_IF_NIL2(self, request_id, KAA_ERR_BADPARAM);
    *request_id = self->request_id;
    return KAA_ERR_NONE;
}


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


static kaa_error_t kaa_client_sync_get_size(kaa_platform_protocol_t *self, const uint16_t plugins[], size_t plugins_count, size_t *expected_size)
{
    KAA_RETURN_IF_NIL4(self, plugins, plugins_count, expected_size, KAA_ERR_BADPARAM)

    *expected_size = KAA_PROTOCOL_MESSAGE_HEADER_SIZE;

    size_t extension_size = 0;
    uint32_t i = 0;
    kaa_plugin_t *plugin;
    kaa_error_t err_code = KAA_ERR_NONE;

    //kaa_plugins[1].request_get_size_fn(self, &extension_size);

    while (!err_code && i != plugins_count) {
        err_code = kaa_plugin_find_by_type(self->kaa_context, plugins[i++], &plugin);//&kaa_plugins[plugins[i++]];

        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "kaa_client_sync_get_size() plugin index %d plugin %s", plugin->extension_type, plugin->plugin_name);

        if (!err_code) {
            err_code = plugin->request_get_size_fn(plugin, &extension_size);

            if(!err_code)
                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Calculated %s extension size %u", plugin->plugin_name, extension_size);

            *expected_size += extension_size;
        }
    }

    if (err_code)
        KAA_LOG_ERROR(self->logger, err_code, "Failed to query extension size in %s service", plugin->plugin_name);

    return err_code;
}

static kaa_error_t kaa_client_sync_serialize(kaa_platform_protocol_t *self
                                           , const uint16_t plugins[]
                                           , size_t plugins_count
                                           , char* buffer
                                           , size_t *size)
{
    uint32_t i = 0;
    kaa_plugin_t *plugin;
    kaa_platform_message_writer_t *writer = NULL;
    kaa_error_t error_code = kaa_platform_message_writer_create(&writer, buffer, *size);
    KAA_RETURN_IF_ERR(error_code);

    uint16_t total_plugins_count = plugins_count;

    error_code = kaa_platform_message_header_write(writer, KAA_PLATFORM_PROTOCOL_ID, KAA_PLATFORM_PROTOCOL_VERSION);
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to write the client sync header");
        return error_code;
    }
    char *extension_count_p = writer->current;
    writer->current += KAA_PROTOCOL_EXTENSIONS_COUNT_SIZE;

    while (!error_code && plugins_count--) {
        error_code = kaa_plugin_find_by_type(self->kaa_context, plugins[i++], &plugin);

        if (!error_code) {
            error_code = plugin->request_serialize_fn(plugin, writer);
            if (error_code == KAA_ERR_NOTHING_TODO)
            {
                --total_plugins_count;
                error_code = KAA_ERR_NONE;
            }
            else if (error_code)
                KAA_LOG_ERROR(self->logger, error_code, "Failed to serialize the %s extension", plugin->plugin_name);
        }
    }

    total_plugins_count = KAA_HTONS(total_plugins_count);
    memcpy(extension_count_p, &total_plugins_count, sizeof(uint16_t));
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
    KAA_RETURN_IF_NIL3(info->allocator, info->plugins, info->plugin_count, KAA_ERR_BADDATA);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Serializing client sync...");

    *buffer_size = 0;
    kaa_error_t error = kaa_client_sync_get_size(self, info->plugins, info->plugin_count, buffer_size);
    KAA_RETURN_IF_ERR(error)

    KAA_LOG_DEBUG(self->logger, KAA_ERR_NONE, "Going to request sync buffer (size %u)", *buffer_size);

    *buffer = info->allocator(info->allocator_context, *buffer_size);
    if (*buffer) {
        self->request_id++;
        error = kaa_client_sync_serialize(self, info->plugins, info->plugin_count, *buffer, buffer_size);
    } else {
        error = KAA_ERR_WRITE_FAILED;
    }

    if (error) {
        self->request_id--;
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Client sync serialized: request id '%u', payload size '%u'", self->request_id, *buffer_size);
    }

    return error;
}

kaa_error_t kaa_platform_protocol_process_server_sync(kaa_platform_protocol_t *self
                                                    , const char *buffer
                                                    , size_t buffer_size)
{
    KAA_RETURN_IF_NIL3(self, buffer, buffer_size, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Server sync received: payload size '%u'", buffer_size);

    kaa_platform_message_reader_t *reader = NULL;
    kaa_error_t err_code = kaa_platform_message_reader_create(&reader, buffer, buffer_size);
    KAA_RETURN_IF_ERR(err_code);

    uint32_t protocol_id = 0;
    uint16_t protocol_version = 0;
    uint16_t extension_count = 0;

    err_code = kaa_platform_message_header_read(reader, &protocol_id, &protocol_version, &extension_count);
    KAA_RETURN_IF_ERR(err_code);

    if (protocol_id != KAA_PLATFORM_PROTOCOL_ID) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BAD_PROTOCOL_ID, "Unsupported protocol ID %x", protocol_id);
        return KAA_ERR_BAD_PROTOCOL_ID;
    }
    if (protocol_version != KAA_PLATFORM_PROTOCOL_VERSION) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BAD_PROTOCOL_VERSION, "Unsupported protocol version %u", protocol_version);
        return KAA_ERR_BAD_PROTOCOL_VERSION;
    }

    uint16_t extension_type = 0;
    uint16_t extension_options = 0;
    uint32_t extension_length = 0;

    self->sync_request_id = 0;

    while (!err_code && kaa_platform_message_is_buffer_large_enough(reader, KAA_PROTOCOL_MESSAGE_HEADER_SIZE)) {
        err_code = kaa_platform_message_read_extension_header(reader
                                                              , &extension_type
                                                              , &extension_options
                                                              , &extension_length);
        KAA_RETURN_IF_ERR(err_code);
        kaa_plugin_t *plugin;

        err_code = kaa_plugin_find_by_type(self->kaa_context, extension_type, &plugin);

        if (!err_code) {
            KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "plugin %s", plugin->plugin_name);
            plugin->request_handle_server_sync_fn(plugin, reader, self->sync_request_id, extension_options, extension_length);
        } else
            KAA_LOG_WARN(self->logger, KAA_ERR_UNSUPPORTED, "Unsupported extension received (type = %u)", extension_type);

        KAA_RETURN_IF_ERR(err_code);
    }

    kaa_platform_message_reader_destroy(reader);

    if (!err_code) {
        err_code = kaa_status_save(self->status);
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Server sync successfully processed");
    } else {
        KAA_LOG_ERROR(self->logger, err_code,
                "Server sync is corrupted. Failed to read extension with type %u", extension_type);
    }

    return err_code;
}

kaa_error_t kaa_meta_plugin_request_get_size(kaa_plugin_t *self, size_t *expected_size)
{
    return kaa_meta_data_request_get_size(expected_size);
}

kaa_error_t kaa_meta_plugin_request_serialize(kaa_plugin_t *self, kaa_platform_message_writer_t *writer)
{
    return kaa_meta_data_request_serialize(self->context->platform_protocol, writer, self->context->platform_protocol->request_id);
}

kaa_error_t kaa_meta_plugin_request_handle_server_sync(kaa_plugin_t *self, kaa_platform_message_reader_t *reader,
                                                         uint32_t request_id, uint16_t options, uint32_t length)
{
    kaa_error_t err_code;
    KAA_LOG_INFO(self->context->platform_protocol->logger, KAA_ERR_NONE, "Received meta server sync: options 0, payload size %u", sizeof(uint32_t));
    err_code = kaa_platform_message_read(reader, &request_id, sizeof(uint32_t));
    self->context->platform_protocol->sync_request_id = KAA_NTOHL(request_id);
    KAA_LOG_TRACE(self->context->platform_protocol->logger, KAA_ERR_NONE, "Server sync request id %u", self->context->platform_protocol->sync_request_id);
    return err_code;
}


kaa_error_t kaa_meta_plugin_init(kaa_plugin_t *self)
{
    return KAA_ERR_NONE;
}

kaa_error_t kaa_meta_plugin_deinit(kaa_plugin_t *self)
{
    return KAA_ERR_NONE;
}

kaa_plugin_t *kaa_meta_plugin_create(kaa_context_t *context)
{
    kaa_plugin_t *plugin = KAA_CALLOC(1, sizeof(kaa_plugin_t));

    plugin->init_fn = kaa_meta_plugin_init;
    plugin->deinit_fn = kaa_meta_plugin_deinit;
    plugin->request_get_size_fn = kaa_meta_plugin_request_get_size;
    plugin->request_serialize_fn = kaa_meta_plugin_request_serialize;
    plugin->request_handle_server_sync_fn = kaa_meta_plugin_request_handle_server_sync;

    plugin->plugin_name = "meta data";
    plugin->extension_type = KAA_PLUGIN_META_DATA;
    plugin->context = context;

    return plugin;
}

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

#include "kaa_profile.h"

#include <string.h>

#include "avro_src/avro/io.h"

#include "kaa_common.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_defaults.h"
#include "kaa_external.h"
#include "kaa_channel_manager.h"
#include "kaa_status.h"
#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"



#define KAA_PROFILE_RESYNC_OPTION 0x1



extern kaa_sync_handler_fn kaa_channel_manager_get_sync_handler(kaa_channel_manager_t *self, kaa_service_t service_type);



static kaa_service_t profile_sync_services[1] = { KAA_SERVICE_PROFILE };



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



/**
 * PUBLIC FUNCTIONS
 */
kaa_error_t kaa_profile_manager_create(kaa_profile_manager_t ** profile_manager_p, kaa_status_t *status
        , kaa_channel_manager_t *channel_manager, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL3(profile_manager_p, channel_manager, status, KAA_ERR_BADPARAM);

    kaa_profile_manager_t *profile_manager = (kaa_profile_manager_t *) KAA_MALLOC(sizeof(kaa_profile_manager_t));
    KAA_RETURN_IF_NIL(profile_manager, KAA_ERR_NOMEM);

    /**
     * KAA_CALLOC is really needed.
     */
    profile_manager->extension_data =
            (kaa_profile_extension_data_t*) KAA_CALLOC(1, sizeof(kaa_profile_extension_data_t));
    KAA_RETURN_IF_NIL(profile_manager->extension_data, KAA_ERR_NOMEM);

    profile_manager->need_resync = true;

    profile_manager->profile_body.size = 0;
    profile_manager->profile_body.buffer = NULL;
    profile_manager->channel_manager = channel_manager;
    profile_manager->status = status;
    profile_manager->logger = logger;

    *profile_manager_p = profile_manager;
    return KAA_ERR_NONE;
}

void kaa_profile_manager_destroy(kaa_profile_manager_t *self)
{
    if (self) {
        if (self->profile_body.buffer && self->profile_body.size > 0) {
            KAA_FREE(self->profile_body.buffer);
            if (self->extension_data) {
                if (self->extension_data->public_key.destroy) {
                    self->extension_data->public_key.destroy(
                            self->extension_data->public_key.buffer);
                }
                KAA_FREE(self->extension_data);
            }
        }
        KAA_FREE(self);
    }
}



kaa_error_t kaa_profile_need_profile_resync(kaa_profile_manager_t *self, bool *result)
{
    KAA_RETURN_IF_NIL2(self, result, KAA_ERR_BADPARAM);
    *result = self->need_resync;
    return KAA_ERR_NONE;
}



static size_t kaa_versions_info_get_size()
{
    static size_t size = 0;

    if (!size) {
        size += sizeof(uint32_t); // config schema version
        size += sizeof(uint32_t); // profile schema version
        size += sizeof(uint32_t); // system notification schema version
        size += sizeof(uint32_t); // user notification schema version
        size += sizeof(uint32_t); // log schema version

        if (KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0) {
            size += sizeof(uint32_t); // event family schema versions count

            size_t i = 0;
            for (; i < KAA_EVENT_SCHEMA_VERSIONS_SIZE; ++i) {
                size += sizeof(uint16_t); // event family version
                size += sizeof(uint16_t); // event family name length
                size += kaa_aligned_size_get(strlen(KAA_EVENT_SCHEMA_VERSIONS[i].name)); // event family name
            }
        }
    }

    return size;
}

kaa_error_t kaa_profile_request_get_size(kaa_profile_manager_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);

    *expected_size = KAA_EXTENSION_HEADER_SIZE;
    *expected_size += sizeof(uint32_t); // profile body size
    *expected_size += kaa_aligned_size_get(self->profile_body.size); // profile data

    *expected_size += kaa_versions_info_get_size();

    bool is_registered = false;
    if (kaa_is_endpoint_registered(self->status, &is_registered))
        return KAA_ERR_BAD_STATE;

    if (!is_registered) {
        bool need_deallocation = false;

        if (!self->extension_data->public_key.buffer) {
            kaa_get_endpoint_public_key((char **)&self->extension_data->public_key.buffer
                                      , (size_t *)&self->extension_data->public_key.size
                                      , &need_deallocation);
        }

        if (self->extension_data->public_key.buffer && self->extension_data->public_key.size > 0) {
            *expected_size += sizeof(uint32_t); // public key size
            *expected_size += kaa_aligned_size_get(self->extension_data->public_key.size); // public key

            if (need_deallocation) {
                self->extension_data->public_key.destroy = kaa_data_destroy;
            }
        } else {
            return KAA_ERR_BADDATA;
        }
    }

    kaa_error_t error_code = kaa_status_get_endpoint_access_token(
                                self->status, (const char**)&self->extension_data->access_token.buffer);
    if (!error_code) {
        if (self->extension_data->access_token.buffer) {
            self->extension_data->access_token.size = strlen((const char*)self->extension_data->access_token.buffer);
            *expected_size += sizeof(uint32_t); // access token length
            *expected_size += kaa_aligned_size_get(self->extension_data->access_token.size); // access token
        }
    } else {
        return error_code;
    }

    self->extension_data->payload_size = *expected_size - KAA_EXTENSION_HEADER_SIZE;

    return KAA_ERR_NONE;
}

#if KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0
#define KAA_SCHEMA_VERSION_NUMBER    6
#else
#define KAA_SCHEMA_VERSION_NUMBER    5
#endif


static kaa_error_t kaa_version_info_serialize(kaa_platform_message_writer_t* writer)
{
    KAA_RETURN_IF_NIL(writer, KAA_ERR_BADPARAM);

    kaa_error_t error_code = KAA_ERR_NONE;
    static uint8_t fields_number[KAA_SCHEMA_VERSION_NUMBER] = {
                                                                CONFIG_SCHEMA_VERSION_VALUE
                                                              , PROFILE_SCHEMA_VERSION_VALUE
                                                              , SYS_NF_VERSION_VALUE
                                                              , USER_NF_VERSION_VALUE
                                                              , LOG_SCHEMA_VERSION_VALUE
#if KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0
                                                              , EVENT_FAMILY_VERSIONS_COUNT_VALUE
#endif
                                                               };

    static uint16_t schema_versions[KAA_SCHEMA_VERSION_NUMBER] = {
                                                                   CONFIG_SCHEMA_VERSION
                                                                 , PROFILE_SCHEMA_VERSION
                                                                 , SYSTEM_NF_SCHEMA_VERSION
                                                                 , USER_NF_SCHEMA_VERSION
                                                                 , LOG_SCHEMA_VERSION
#if KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0
                                                                 , KAA_EVENT_SCHEMA_VERSIONS_SIZE
#endif
                                                                  };

    uint16_t field_number_with_reserved = 0;
    uint16_t network_order_schema_version = 0;

    size_t i = 0;
    for (; i < KAA_SCHEMA_VERSION_NUMBER; ++i) {
        field_number_with_reserved = KAA_HTONS(fields_number[i] << 8);
        error_code = kaa_platform_message_write(writer, &field_number_with_reserved, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);

        network_order_schema_version = KAA_HTONS(schema_versions[i]);
        error_code = kaa_platform_message_write(writer, &network_order_schema_version, sizeof(uint16_t));
        KAA_RETURN_IF_ERR(error_code);
    }

    if (KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0) {
        uint16_t network_order_family_version = 0;
        uint16_t network_order_family_name_len = 0;
        size_t event_schema_name_length = 0;

        for (i = 0; i < KAA_EVENT_SCHEMA_VERSIONS_SIZE; ++i) {
            network_order_family_version = KAA_HTONS(KAA_EVENT_SCHEMA_VERSIONS[i].version);
            error_code = kaa_platform_message_write(writer
                                                  , &network_order_family_version
                                                  , sizeof(uint16_t));
            KAA_RETURN_IF_ERR(error_code);

            event_schema_name_length = strlen(KAA_EVENT_SCHEMA_VERSIONS[i].name);
            network_order_family_name_len = KAA_HTONS(event_schema_name_length);
            error_code = kaa_platform_message_write(writer
                                                  , &network_order_family_name_len
                                                  , sizeof(uint16_t));
            KAA_RETURN_IF_ERR(error_code);

            error_code = kaa_platform_message_write_aligned(writer
                                                          , KAA_EVENT_SCHEMA_VERSIONS[i].name
                                                          , event_schema_name_length);
            KAA_RETURN_IF_ERR(error_code);
        }
    }

    return error_code;
}

kaa_error_t kaa_profile_request_serialize(kaa_profile_manager_t *self, kaa_platform_message_writer_t* writer)
{
    KAA_RETURN_IF_NIL2(self, writer, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to compile profile client sync");

    kaa_error_t error_code = kaa_platform_message_write_extension_header(writer
                                                                       , KAA_PROFILE_EXTENSION_TYPE
                                                                       , 0
                                                                       , self->extension_data->payload_size);
    KAA_RETURN_IF_ERR(error_code);

    uint32_t network_order_32 = KAA_HTONL(self->profile_body.size);
    error_code = kaa_platform_message_write(writer, &network_order_32, sizeof(uint32_t));
    KAA_RETURN_IF_ERR(error_code);
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Writing profile body (size %u)...", self->profile_body.size);
    error_code = kaa_platform_message_write_aligned(writer, self->profile_body.buffer, self->profile_body.size);
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to write profile body");
        return error_code;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Writing version info...");
    error_code = kaa_version_info_serialize(writer);
    if (error_code) {
        KAA_LOG_ERROR(self->logger, error_code, "Failed to write version info");
        return error_code;
    }

    bool is_registered = false;
    error_code = kaa_is_endpoint_registered(self->status, &is_registered);
    KAA_RETURN_IF_ERR(error_code);

    uint16_t network_order_16 = 0;
    uint16_t field_number_with_reserved = 0;

    if (!is_registered) {
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
                                         , uint32_t extension_options
                                         , size_t extension_length)
{
    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received profile server sync");

    kaa_error_t error_code = KAA_ERR_NONE;

    self->need_resync = false;
    if (extension_options & KAA_PROFILE_RESYNC_OPTION) {
        self->need_resync = true;
        kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(
                                      self->channel_manager, profile_sync_services[0]);
        if (sync)
            (*sync)(profile_sync_services, 1);
    }

    bool is_registered = false;
    error_code = kaa_is_endpoint_registered(self->status, &is_registered);
    KAA_RETURN_IF_ERR(error_code);

    if (!is_registered) {
        error_code = kaa_set_endpoint_registered(self->status, true);
        KAA_RETURN_IF_ERR(error_code);
    }

    return error_code;
}

kaa_error_t kaa_profile_update_profile(kaa_profile_manager_t *self, kaa_profile_t * profile_body)
{
    KAA_RETURN_IF_NIL2(self, profile_body, KAA_ERR_BADPARAM);

    size_t serialized_profile_size = profile_body->get_size(profile_body);
    if (!serialized_profile_size) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BADDATA, "Failed to update profile: serialize profile size is null."
                                                                                "Maybe profile schema is empty")
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
    kaa_calculate_sha_hash(serialized_profile, serialized_profile_size, new_hash);

    kaa_digest_p old_hash = NULL;
    if (kaa_status_get_profile_hash(self->status, &old_hash)) {
        KAA_FREE(serialized_profile);
        return KAA_ERR_BAD_STATE;
    }

    if (old_hash && !memcmp(new_hash, old_hash, SHA_1_DIGEST_LENGTH)) {
        self->need_resync = false;
        KAA_FREE(serialized_profile);
        return KAA_ERR_NONE;
    }

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Endpoint profile is updated")

    if (kaa_status_set_profile_hash(self->status, new_hash)) {
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

    kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(
                                    self->channel_manager, profile_sync_services[0]);
    if (sync)
        (*sync)(profile_sync_services, 1);

    return KAA_ERR_NONE;
}

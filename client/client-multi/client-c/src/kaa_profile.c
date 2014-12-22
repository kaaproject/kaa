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
#include "gen/kaa_endpoint_gen.h"

extern kaa_sync_handler_fn kaa_channel_manager_get_sync_handler(kaa_channel_manager_t *self, kaa_service_t service_type);



static kaa_service_t profile_sync_services[1] = { KAA_SERVICE_PROFILE };



typedef struct {
    size_t extension_size;
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



static kaa_endpoint_version_info_t * create_versions_info()
{
    kaa_endpoint_version_info_t *version_info = kaa_endpoint_version_info_create();
    KAA_RETURN_IF_NIL(version_info, NULL);

    version_info->config_version = CONFIG_SCHEMA_VERSION;
    version_info->log_schema_version = LOG_SCHEMA_VERSION;
    version_info->profile_version = PROFILE_SCHEMA_VERSION;
    version_info->system_nf_version = SYSTEM_NF_SCHEMA_VERSION;
    version_info->user_nf_version = USER_NF_SCHEMA_VERSION;
#if KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0
    version_info->event_family_versions = kaa_union_array_event_class_family_version_info_or_null_branch_0_create();
    if (!version_info->event_family_versions) {
        version_info->destroy(version_info);
        return NULL;
    }
    version_info->event_family_versions->data = NULL;

    size_t i = 0;
    for (; i < KAA_EVENT_SCHEMA_VERSIONS_SIZE; ++i) {
        kaa_event_class_family_version_info_t *ecfv = kaa_event_class_family_version_info_create();
        if (!ecfv) {
            version_info->destroy(version_info);
            return NULL;
        }

        ecfv->name = kaa_string_move_create(KAA_EVENT_SCHEMA_VERSIONS[i].name, NULL); // destructor is not needed
        ecfv->version = KAA_EVENT_SCHEMA_VERSIONS[i].version;
        if (version_info->event_family_versions->data) {
            if (!kaa_list_push_back(version_info->event_family_versions->data, ecfv)) {
                version_info->destroy(version_info);
                return NULL;
            }
        } else {
            version_info->event_family_versions->data = kaa_list_create(ecfv);
            if (!version_info->event_family_versions->data) {
                version_info->destroy(version_info);
                return NULL;
            }
        }
    }
#else
    version_info->event_family_versions =
            kaa_union_array_event_class_family_version_info_or_null_branch_1_create();
    if (!version_info->event_family_versions) {
        version_info->destroy(version_info);
        return NULL;
    }
#endif
    return version_info;
}

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
     * Extension data should be calloc-ed.
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



kaa_error_t kaa_profile_request_get_size(kaa_profile_manager_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);

    bool is_versions_needed = true; // FIXME: replace with valid check
    bool is_public_key_needed = true; // FIXME: replace with valid check
    bool is_access_token_needed = true; // FIXME: replace with valid check

    *expected_size = KAA_EXTENSION_HEADER_SIZE;
    *expected_size += sizeof(uint32_t); // profile body size
    *expected_size += kaa_aligned_size_get(self->profile_body.size); // profile data

    if (is_versions_needed) {
        *expected_size += sizeof(uint32_t); // config schema version
        *expected_size += sizeof(uint32_t); // profile schema version
        *expected_size += sizeof(uint32_t); // system notification schema version
        *expected_size += sizeof(uint32_t); // user notification schema version
        *expected_size += sizeof(uint32_t); // log schema version

        if (KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0) {
            *expected_size += kaa_aligned_size_get(KAA_EVENT_SCHEMA_VERSIONS_SIZE);

            size_t i = 0;
            for (; i < KAA_EVENT_SCHEMA_VERSIONS_SIZE; ++i) {
                *expected_size += sizeof(uint16_t); // event family version
                *expected_size += sizeof(uint16_t); // event family name length
                *expected_size += kaa_aligned_size_get(strlen(KAA_EVENT_SCHEMA_VERSIONS[i].name)); // event family name
            }
        }
    }

    /**
     * Retrieve public key once.
     */
    if (is_public_key_needed && !self->extension_data->public_key.buffer) {
        bool need_deallocation = false;

        kaa_get_endpoint_public_key((char **)&self->extension_data->public_key.buffer
                                  , (size_t *)&self->extension_data->public_key.size
                                  , &need_deallocation);

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

    if (is_access_token_needed) {
        kaa_error_t err_code = kaa_status_get_endpoint_access_token(
                                    self->status, (const char**)&self->extension_data->access_token.buffer);
        if (!err_code) {
            self->extension_data->access_token.size = strlen((const char*)self->extension_data->access_token.buffer);
            *expected_size += sizeof(uint32_t); // access token length
            *expected_size += kaa_aligned_size_get(self->extension_data->access_token.size); // access token
        } else {
            return err_code;
        }
    }

    self->extension_data->extension_size = *expected_size;

    return KAA_ERR_NONE;
}

#if KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0
#define KAA_SCHEMA_VERSION_NUMBER    6
#else
#define KAA_SCHEMA_VERSION_NUMBER    5
#endif


kaa_error_t kaa_profile_request_serialize(kaa_profile_manager_t *self, kaa_platform_message_writer_t* writer)
{
    KAA_RETURN_IF_NIL(writer, KAA_ERR_BADPARAM);

    bool is_versions_needed = true; // FIXME: replace with valid check
    bool is_public_key_needed = true; // FIXME: replace with valid check
    bool is_access_token_needed = true; // FIXME: replace with valid check

    kaa_error_t err_code = kaa_platform_message_write(writer, &self->profile_body.size, sizeof(uint32_t));
    KAA_RETURN_IF_ERR(err_code);
    err_code = kaa_platform_message_write_aligned(writer, self->profile_body.buffer, self->profile_body.size);
    KAA_RETURN_IF_ERR(err_code);

    if (is_versions_needed) {
        size_t schemas_number = KAA_SCHEMA_VERSION_NUMBER;
        uint16_t schema_versions[KAA_SCHEMA_VERSION_NUMBER] = { CONFIG_SCHEMA_VERSION
                                                              , PROFILE_SCHEMA_VERSION
                                                              , SYSTEM_NF_SCHEMA_VERSION
                                                              , USER_NF_SCHEMA_VERSION
                                                              , LOG_SCHEMA_VERSION
#if KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0
                                                              , KAA_EVENT_SCHEMA_VERSIONS_SIZE
#endif
                                                              };

        uint8_t fields_number[KAA_SCHEMA_VERSION_NUMBER] = { CONFIG_SCHEMA_VERSION_VALUE
                                                           , PROFILE_SCHEMA_VERSION_VALUE
                                                           , SYS_NF_VERSION_VALUE
                                                           , USER_NF_VERSION_VALUE
                                                           , LOG_SCHEMA_VERSION_VALUE
#if KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0
                                                           , EVENT_FAMILY_VERSIONS_COUNT_VALUE
#endif
                                                           };
        uint16_t field_number_with_reserved = 0;
        uint16_t network_order_schema_version = 0;

        while (schemas_number--) {
            field_number_with_reserved = fields_number[schemas_number] << 8;
            err_code = kaa_platform_message_write(writer, &field_number_with_reserved, sizeof(uint16_t));
            KAA_RETURN_IF_ERR(err_code);

            network_order_schema_version = KAA_HTONS(schema_versions[schemas_number]);
            err_code = kaa_platform_message_write(writer, &network_order_schema_version, sizeof(uint16_t));
            KAA_RETURN_IF_ERR(err_code);
        }

        if (KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0) {
            uint16_t network_order_family_version = 0;
            uint16_t network_order_family_name_len = 0;
            size_t event_schema_version_number = KAA_EVENT_SCHEMA_VERSIONS_SIZE;

            while (event_schema_version_number--) {
                network_order_family_version =
                        KAA_HTONS(KAA_EVENT_SCHEMA_VERSIONS[event_schema_version_number].version);
                err_code = kaa_platform_message_write(
                        writer, &network_order_family_version, sizeof(network_order_family_version));
                KAA_RETURN_IF_ERR(err_code);


                network_order_family_name_len =
                        KAA_HTONS(strlen(KAA_EVENT_SCHEMA_VERSIONS[event_schema_version_number].name));
                err_code = kaa_platform_message_write(
                        writer, &network_order_family_name_len, sizeof(network_order_family_name_len));
                KAA_RETURN_IF_ERR(err_code);

                err_code = kaa_platform_message_write_aligned(
                                          writer
                                        , KAA_EVENT_SCHEMA_VERSIONS[event_schema_version_number].name
                                        , KAA_NTOHS(network_order_family_name_len));
                KAA_RETURN_IF_ERR(err_code);
            }
        }

        uint16_t len = 0;

        if (is_public_key_needed) {
            field_number_with_reserved = PUB_KEY_VALUE << 8;
            err_code = kaa_platform_message_write(
                    writer, &field_number_with_reserved, sizeof(field_number_with_reserved));
            KAA_RETURN_IF_ERR(err_code);

            len = KAA_HTONS(self->extension_data->public_key.size);
            err_code = kaa_platform_message_write(writer, &len, sizeof(len));
            KAA_RETURN_IF_ERR(err_code);

            err_code = kaa_platform_message_write_aligned(
                    writer, (char*)self->extension_data->public_key.buffer, KAA_NTOHS(len));
            KAA_RETURN_IF_ERR(err_code);
        }

        if (is_access_token_needed) {
            field_number_with_reserved = ACCESS_TOKEN_VALUE << 8;
            err_code = kaa_platform_message_write(
                    writer, &field_number_with_reserved, sizeof(field_number_with_reserved));
            KAA_RETURN_IF_ERR(err_code);

            len = KAA_HTONS(self->extension_data->access_token.size);
            err_code = kaa_platform_message_write(writer, &len, sizeof(len));
            KAA_RETURN_IF_ERR(err_code);

            err_code = kaa_platform_message_write_aligned(
                    writer, (char*)self->extension_data->access_token.buffer, KAA_NTOHS(len));
            KAA_RETURN_IF_ERR(err_code);
        }
    }

    return err_code;
}

kaa_error_t kaa_profile_compile_request(kaa_profile_manager_t *self, kaa_profile_sync_request_t **result)
{
    KAA_RETURN_IF_NIL2(self, result, KAA_ERR_NOMEM);

    kaa_profile_sync_request_t *request = kaa_profile_sync_request_create();
    KAA_RETURN_IF_NIL(request, KAA_ERR_NOMEM);

    request->version_info = create_versions_info();
    if (!request->version_info) {
        request->destroy(request);
        return KAA_ERR_NOMEM;
    }

    const char * ep_acc_token = NULL;
    if (kaa_status_get_endpoint_access_token(self->status, &ep_acc_token)) {
        request->destroy(request);
        return KAA_ERR_BAD_STATE;
    }

    if (ep_acc_token) {
        request->endpoint_access_token = kaa_union_string_or_null_branch_0_create();
        if (!request->endpoint_access_token) {
            request->destroy(request);
            return KAA_ERR_NOMEM;
        }
        request->endpoint_access_token->data = kaa_string_move_create(ep_acc_token, NULL); // destructor is not needed
    } else {
        request->endpoint_access_token = kaa_union_string_or_null_branch_1_create();
        if (!request->endpoint_access_token) {
            request->destroy(request);
            return KAA_ERR_NOMEM;
        }
    }

    request->profile_body = kaa_bytes_move_create(
            self->profile_body.buffer, self->profile_body.size, NULL); // destructor for buffer is not needed
    if (!request->profile_body) {
        request->destroy(request);
        return KAA_ERR_NOMEM;
    }

    bool is_registered = false;
    if (kaa_is_endpoint_registered(self->status, &is_registered)) {
        request->destroy(request);
        return KAA_ERR_BAD_STATE;
    }

    if (is_registered) {
        request->endpoint_public_key = kaa_union_bytes_or_null_branch_1_create();
        if (!request->endpoint_public_key) {
            request->destroy(request);
            return KAA_ERR_NOMEM;
        }
    } else {
        request->endpoint_public_key = kaa_union_bytes_or_null_branch_0_create();
        if (!request->endpoint_public_key) {
            request->destroy(request);
            return KAA_ERR_NOMEM;
        }

        kaa_bytes_t pub_key;
        bool need_deallocation = false;

        kaa_get_endpoint_public_key((char **)&pub_key.buffer, (size_t *)&pub_key.size, &need_deallocation);

        if (!pub_key.buffer || pub_key.size <= 0)
            return KAA_ERR_INVALID_PUB_KEY;

        request->endpoint_public_key->data = kaa_bytes_move_create(
                    pub_key.buffer, pub_key.size, (need_deallocation ? &kaa_data_destroy : NULL));

        if (!request->endpoint_public_key->data) {
            if (need_deallocation && pub_key.buffer) {
                KAA_FREE(pub_key.buffer);
            }
            request->destroy(request);
            return KAA_ERR_NOMEM;
        }
    }
    *result = request;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_profile_handle_sync(kaa_profile_manager_t *self, kaa_profile_sync_response_t *response)
{
    KAA_RETURN_IF_NIL2(self, response, KAA_ERR_BADPARAM);

    self->need_resync = false;
    if (response->response_status == ENUM_SYNC_RESPONSE_STATUS_RESYNC) {
        self->need_resync = true;
        kaa_sync_handler_fn sync = kaa_channel_manager_get_sync_handler(
                                      self->channel_manager, profile_sync_services[0]);
        if (sync)
            (*sync)(profile_sync_services, 1);
    }
    bool is_registered = false;
    if (kaa_is_endpoint_registered(self->status, &is_registered))
        return KAA_ERR_BAD_STATE;
    if (!is_registered && kaa_set_endpoint_registered(self->status, true))
        return KAA_ERR_BAD_STATE;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_profile_update_profile(kaa_profile_manager_t *self, kaa_profile_t * profile_body)
{
    KAA_RETURN_IF_NIL2(self, profile_body, KAA_ERR_BADPARAM);

    size_t serialized_profile_size = profile_body->get_size(profile_body);
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

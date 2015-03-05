/*
 * Copyright 2014-2015 CyberVision, Inc.
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
#include <string.h>
#include "platform/stdio.h"
#include "kaa_defaults.h"
#include "kaa_user.h"
#include "platform/ext_sha.h"
#include "kaa_status.h"
#include "kaa_channel_manager.h"
#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"



#define KAA_USER_RECEIVE_UPDATES_FLAG   0x01

#define EXTERNAL_SYSTEM_AUTH_FIELD      0x00



extern kaa_transport_channel_interface_t *kaa_channel_manager_get_transport_channel(kaa_channel_manager_t *self
                                                                                  , kaa_service_t service_type);



typedef struct {
    char          *user_external_id;
    size_t        user_external_id_len;
    char          *user_access_token;
    size_t        user_access_token_len;
    char          *user_verifier_token;
    size_t        user_verifier_token_len;
} user_info_t;

struct kaa_user_manager_t {
    kaa_attachment_status_listeners_t   attachment_listeners;               /*!< Client code-defined user attachment listeners */
    user_info_t                        *user_info;                          /*!< User credentials */
    bool                                is_waiting_user_attach_response;
    kaa_status_t                       *status;                             /*!< Reference to global status */
    kaa_channel_manager_t              *channel_manager;                    /*!< Reference to global channel manager */
    kaa_logger_t                       *logger;
};

typedef enum {
    USER_RESULT_SUCCESS = 0x00,
    USER_RESULT_FAILURE = 0x01
} user_sync_result_t;

typedef enum {
    USER_ATTACH_RESPONSE_FIELD      = 0,
    USER_ATTACH_NOTIFICATION_FIELD  = 1,
    USER_DETACH_NOTIFICATION_FIELD  = 2,
    ENDPOINT_ATTACH_RESPONSES_FIELD = 3,
    ENDPOINT_DETACH_RESPONSES_FIELD = 4
} user_server_sync_field_t;



static kaa_service_t user_sync_services[1] = {KAA_SERVICE_USER};



static void destroy_user_info(user_info_t *user_info)
{
    KAA_RETURN_IF_NIL(user_info, );

    if (user_info->user_external_id)
        KAA_FREE(user_info->user_external_id);
    if (user_info->user_access_token)
        KAA_FREE(user_info->user_access_token);
    if (user_info->user_verifier_token)
        KAA_FREE(user_info->user_verifier_token);

    KAA_FREE(user_info);
}

static user_info_t *create_user_info(const char *external_id, const char *user_access_token, const char *user_verifier_token)
{
    KAA_RETURN_IF_NIL3(external_id, user_access_token, user_verifier_token, NULL);

    user_info_t *user_info = (user_info_t *) KAA_CALLOC(1, sizeof(user_info_t));
    KAA_RETURN_IF_NIL(user_info, NULL);

    user_info->user_external_id_len = strlen(external_id);
    user_info->user_access_token_len = strlen(user_access_token);
    user_info->user_verifier_token_len = strlen(user_verifier_token);

    user_info->user_external_id = (char *) KAA_MALLOC((user_info->user_external_id_len + 1) * sizeof(char));
    if (!user_info->user_external_id) {
        destroy_user_info(user_info);
        return NULL;
    }
    strcpy(user_info->user_external_id, external_id);

    user_info->user_access_token = (char *) KAA_MALLOC((user_info->user_access_token_len + 1) * sizeof(char));
    if (!user_info->user_access_token) {
        destroy_user_info(user_info);
        return NULL;
    }
    strcpy(user_info->user_access_token, user_access_token);

    user_info->user_verifier_token = (char *) KAA_MALLOC((user_info->user_verifier_token_len + 1) * sizeof(char));
    if (!user_info->user_verifier_token) {
        destroy_user_info(user_info);
        return NULL;
    }
    strcpy(user_info->user_verifier_token, user_verifier_token);

    return user_info;
}

kaa_error_t kaa_user_manager_create(kaa_user_manager_t **user_manager_p
                                  , kaa_status_t *status
                                  , kaa_channel_manager_t *channel_manager
                                  , kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL2(user_manager_p, status, KAA_ERR_BADPARAM);

    *user_manager_p = (kaa_user_manager_t *) KAA_MALLOC(sizeof(kaa_user_manager_t));
    KAA_RETURN_IF_NIL((*user_manager_p), KAA_ERR_NOMEM);

    (*user_manager_p)->attachment_listeners.on_attached = NULL;
    (*user_manager_p)->attachment_listeners.on_detached = NULL;
    (*user_manager_p)->attachment_listeners.on_attach_success = NULL;
    (*user_manager_p)->attachment_listeners.on_attach_failed  = NULL;
    (*user_manager_p)->user_info = NULL;
    (*user_manager_p)->is_waiting_user_attach_response = false;
    (*user_manager_p)->status = status;
    (*user_manager_p)->channel_manager = channel_manager;
    (*user_manager_p)->logger = logger;

    return KAA_ERR_NONE;
}

void kaa_user_manager_destroy(kaa_user_manager_t *self)
{
    if (self) {
        destroy_user_info(self->user_info);
        KAA_FREE(self);
    }
}

bool kaa_user_manager_is_attached_to_user(kaa_user_manager_t *self)
{
    KAA_RETURN_IF_NIL2(self, self->status, false);
    return self->status->is_attached;
}

kaa_error_t kaa_user_manager_attach_to_user(kaa_user_manager_t *self
                                          , const char *user_external_id
                                          , const char *access_token
                                          , const char *user_verifier_token)
{
    KAA_RETURN_IF_NIL3(self, user_external_id, access_token, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to attach to user "
                                              "(external id = \"%s\", access token = \"%s\", verifier token = \"%s\")"
                                              , user_external_id, access_token, user_verifier_token);

    if (self->is_waiting_user_attach_response) {
        destroy_user_info(self->user_info);
        self->user_info = NULL;
        self->is_waiting_user_attach_response = false;
    }

    self->user_info = create_user_info(user_external_id, access_token, user_verifier_token);
    if (!self->user_info)
        return KAA_ERR_NOMEM;

    kaa_transport_channel_interface_t *channel =
            kaa_channel_manager_get_transport_channel(self->channel_manager, user_sync_services[0]);
    if (channel)
        channel->sync_handler(channel->context, user_sync_services, 1);

    return KAA_ERR_NONE;
}

kaa_error_t kaa_user_manager_default_attach_to_user(kaa_user_manager_t *self
                                                  , const char *user_external_id
                                                  , const char *access_token)
{
    KAA_RETURN_IF_NIL3(self, user_external_id, access_token, KAA_ERR_BADPARAM);

#ifdef DEFAULT_USER_VERIFIER_TOKEN
    return kaa_user_manager_attach_to_user(self, user_external_id, access_token, DEFAULT_USER_VERIFIER_TOKEN);
#else
    KAA_LOG_WARN(self->logger, KAA_ERR_USER_VERIFIER_NOT_FOUND,
                    "Failed to attach to user: default verifier is not specified");
    return KAA_ERR_USER_VERIFIER_NOT_FOUND;
#endif
}

kaa_error_t kaa_user_manager_set_attachment_listeners(kaa_user_manager_t *self
                                                    , const kaa_attachment_status_listeners_t *listeners)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    self->attachment_listeners = *listeners;
    return KAA_ERR_NONE;
}

static size_t kaa_user_request_get_size_no_header(kaa_user_manager_t *self)
{
    size_t expected_size = 0;
    if (self->user_info && !self->is_waiting_user_attach_response) {
       expected_size += sizeof(uint32_t) //  field id + user external ID length + user access token length
                      + sizeof(uint32_t); // verifier id length + reserved
       expected_size += kaa_aligned_size_get(self->user_info->user_external_id_len);
       expected_size += kaa_aligned_size_get(self->user_info->user_access_token_len);
       expected_size += kaa_aligned_size_get(self->user_info->user_verifier_token_len);
    }
    return expected_size;
}

kaa_error_t kaa_user_request_get_size(kaa_user_manager_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);
    *expected_size = KAA_EXTENSION_HEADER_SIZE + kaa_user_request_get_size_no_header(self);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_user_request_serialize(kaa_user_manager_t *self, kaa_platform_message_writer_t* writer)
{
    KAA_RETURN_IF_NIL2(self, writer, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to compile user client sync");

    size_t size = kaa_user_request_get_size_no_header(self);
    if (kaa_platform_message_write_extension_header(writer, KAA_USER_EXTENSION_TYPE, KAA_USER_RECEIVE_UPDATES_FLAG, size)) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_WRITE_FAILED, "Failed to write the user extension header");
        return KAA_ERR_WRITE_FAILED;
    }

    if (self->user_info && !self->is_waiting_user_attach_response) {
        *(writer->current++) = EXTERNAL_SYSTEM_AUTH_FIELD;
        *(writer->current++) = (uint8_t) self->user_info->user_external_id_len;
        *((uint16_t *) writer->current) = KAA_HTONS((uint16_t) self->user_info->user_access_token_len);
        writer->current += sizeof(uint16_t);
        *((uint16_t *) writer->current) = KAA_HTONS((uint16_t) self->user_info->user_verifier_token_len);
        writer->current += sizeof(uint32_t); /* verifier token length + reserved */

        if (self->user_info->user_external_id_len) {
            if (kaa_platform_message_write_aligned(writer
                                                 , self->user_info->user_external_id
                                                 , self->user_info->user_external_id_len))
            {
                KAA_LOG_ERROR(self->logger, KAA_ERR_WRITE_FAILED, "Failed to write the user external id \"%s\""
                                                                        , self->user_info->user_external_id);
                return KAA_ERR_WRITE_FAILED;
            }
        }
        if (self->user_info->user_access_token_len) {
            if (kaa_platform_message_write_aligned(writer
                                                 , self->user_info->user_access_token
                                                 , self->user_info->user_access_token_len))
            {
                KAA_LOG_ERROR(self->logger, KAA_ERR_WRITE_FAILED, "Failed to write the user access token \"%s\""
                                                                        , self->user_info->user_access_token);
                return KAA_ERR_WRITE_FAILED;
            }
        }
        if (self->user_info->user_verifier_token_len) {
            if (kaa_platform_message_write_aligned(writer
                                                 , self->user_info->user_verifier_token
                                                 , self->user_info->user_verifier_token_len))
            {
                KAA_LOG_ERROR(self->logger, KAA_ERR_WRITE_FAILED, "Failed to write the user verifier token \"%s\""
                                                                         , self->user_info->user_verifier_token);
                return KAA_ERR_WRITE_FAILED;
            }
        }

        self->is_waiting_user_attach_response = true;
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_user_handle_server_sync(kaa_user_manager_t *self
                                      , kaa_platform_message_reader_t *reader
                                      , uint32_t extension_options
                                      , size_t extension_length)
{
    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received user server sync");

    size_t remaining_length = extension_length;
    uint32_t field_header = 0;
    user_server_sync_field_t field = 0;

    while (remaining_length > 0) {
        field_header = KAA_NTOHL(*(uint32_t *) reader->current);
        reader->current += sizeof(uint32_t);
        remaining_length -= sizeof(uint32_t);

        field = (field_header >> 24) & 0xFF;
        switch (field) {
            case USER_ATTACH_RESPONSE_FIELD: {
                user_sync_result_t result = (uint8_t) (field_header >> 8) & 0xFF;
                destroy_user_info(self->user_info);
                self->user_info = NULL;
                self->is_waiting_user_attach_response = false;

                if (result == USER_RESULT_SUCCESS) {
                    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Endpoint was successfully attached to user");
                    self->status->is_attached = true;
                    if (self->attachment_listeners.on_attach_success)
                        (self->attachment_listeners.on_attach_success)(self->attachment_listeners.context);
                } else {
                    KAA_LOG_ERROR(self->logger, KAA_ERR_BAD_STATE, "Failed to attach endpoint to user");

                    uint16_t user_verifier_error_code;
                    if (kaa_platform_message_read(reader, &user_verifier_error_code, sizeof(uint16_t))) {
                        KAA_LOG_ERROR(self->logger, KAA_ERR_READ_FAILED, "Failed to read user verifier error code");
                        return KAA_ERR_READ_FAILED;
                    }

                    user_verifier_error_code = KAA_NTOHS(user_verifier_error_code);
                    remaining_length -= sizeof(uint16_t);

                    uint16_t reason_length;
                    if (kaa_platform_message_read(reader, &reason_length, sizeof(uint16_t))) {
                        KAA_LOG_ERROR(self->logger, KAA_ERR_READ_FAILED, "Failed to read reason length");
                        return KAA_ERR_READ_FAILED;
                    }

                    reason_length = KAA_NTOHS(reason_length);
                    remaining_length -= sizeof(uint16_t);

                    if (reason_length) {
                        char reason[reason_length + 1];
                        if (kaa_platform_message_read_aligned(reader, reason, reason_length)) {
                            KAA_LOG_ERROR(self->logger, KAA_ERR_READ_FAILED, "Failed to read error reason");
                            return KAA_ERR_READ_FAILED;
                        }

                        reason[reason_length] = '\0';
                        remaining_length -= kaa_aligned_size_get(reason_length);

                        if (self->attachment_listeners.on_attach_failed) {
                            (self->attachment_listeners.on_attach_failed)(self->attachment_listeners.context
                                                                        , (user_verifier_error_code_t)user_verifier_error_code
                                                                        , reason);
                        }
                    } else {
                        if (self->attachment_listeners.on_attach_failed) {
                            (self->attachment_listeners.on_attach_failed)(self->attachment_listeners.context
                                                                        , (user_verifier_error_code_t)user_verifier_error_code
                                                                        , NULL);
                        }
                    }
                }
                break;
            }
            case USER_ATTACH_NOTIFICATION_FIELD: {
                uint8_t external_id_length = (field_header >> 16) & 0xFF;
                uint16_t access_token_length = (field_header) & 0xFFFF;
                if (external_id_length + access_token_length > remaining_length)
                    return KAA_ERR_INVALID_BUFFER_SIZE;

                char external_id[external_id_length + 1];
                char access_token[access_token_length + 1];

                if (kaa_platform_message_read_aligned(reader, external_id, external_id_length)) {
                    KAA_LOG_ERROR(self->logger, KAA_ERR_READ_FAILED, "Failed to read the external ID (length %u)"
                                                                                            , external_id_length);
                    return KAA_ERR_READ_FAILED;
                }
                external_id[external_id_length] = '\0';
                remaining_length -= kaa_aligned_size_get(external_id_length);

                if (kaa_platform_message_read_aligned(reader, access_token, access_token_length)) {
                    KAA_LOG_ERROR(self->logger, KAA_ERR_READ_FAILED, "Failed to read the access token (length %u)"
                                                                                            , access_token_length);
                    return KAA_ERR_READ_FAILED;
                }
                access_token[access_token_length] = '\0';
                remaining_length -= kaa_aligned_size_get(access_token_length);

                self->status->is_attached = true;

                if (self->attachment_listeners.on_attached)
                    (self->attachment_listeners.on_attached)(self->attachment_listeners.context
                                                                                , external_id, access_token);
                break;
            }
            case USER_DETACH_NOTIFICATION_FIELD: {
                uint16_t access_token_length = (field_header) & 0xFFFF;
                if (access_token_length > remaining_length)
                    return KAA_ERR_INVALID_BUFFER_SIZE;

                char access_token[access_token_length + 1];
                if (kaa_platform_message_read_aligned(reader, access_token, access_token_length)) {
                    KAA_LOG_ERROR(self->logger, KAA_ERR_READ_FAILED, "Failed to read the access token (length %u)"
                                                                                            , access_token_length);
                    return KAA_ERR_READ_FAILED;
                }
                access_token[access_token_length] = '\0';
                remaining_length -= kaa_aligned_size_get(access_token_length);

                self->status->is_attached = false;

                if (self->attachment_listeners.on_detached)
                    (self->attachment_listeners.on_detached)(self->attachment_listeners.context, access_token);
                break;
            }
            default:
                KAA_LOG_ERROR(self->logger, KAA_ERR_READ_FAILED, "Invalid field %u", field);
                return KAA_ERR_READ_FAILED;
        }
    }
    return KAA_ERR_NONE;
}

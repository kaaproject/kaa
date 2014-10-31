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

#include "kaa.h"
#include "kaa_mem.h"
#include "kaa_common.h"
#include "kaa_context.h"
#include "kaa_external.h"
#include "kaa_defaults.h"
#include <string.h>

static kaa_context_t * kaa_context_;
static KAA_INT32T       global_request_id = 0;

static kaa_sync_request_meta_data_t * create_sync_request_meta_data(void *ctx)
{
    kaa_context_t *context = (kaa_context_t *)ctx;

    kaa_sync_request_meta_data_t *request = kaa_create_sync_request_meta_data();
    size_t len = strlen(APPLICATION_TOKEN);
    request->application_token = KAA_CALLOC(len + 1, sizeof(char));
    memcpy(request->application_token, APPLICATION_TOKEN, len);
    request->timeout = 60000L;

    request->endpoint_public_key_hash = KAA_CALLOC(1, sizeof(kaa_bytes_t));
    request->endpoint_public_key_hash->size = SHA_1_DIGEST_LENGTH;
    request->endpoint_public_key_hash->buffer = KAA_CALLOC(SHA_1_DIGEST_LENGTH, sizeof(char));
    kaa_digest* pub_key_hash = kaa_status_get_endpoint_public_key_hash(context->status);
    if (pub_key_hash) {
        memcpy(request->endpoint_public_key_hash->buffer, *pub_key_hash, SHA_1_DIGEST_LENGTH);
    }

    kaa_digest * profile_hash = kaa_status_get_profile_hash(context->status);
    if (profile_hash) {
        request->profile_hash = kaa_create_bytes_null_union_bytes_branch();
        kaa_bytes_t * hash = KAA_CALLOC(1, sizeof(kaa_bytes_t));
        hash->size = SHA_1_DIGEST_LENGTH;
        hash->buffer = KAA_CALLOC(SHA_1_DIGEST_LENGTH, sizeof(char));
        memcpy(hash->buffer, *profile_hash, SHA_1_DIGEST_LENGTH);
        request->profile_hash->data = hash;
    } else {
        request->profile_hash = kaa_create_bytes_null_union_null_branch();
    }

    return request;
}

kaa_error_t kaa_init()
{
    kaa_error_t result = kaa_create_context(&kaa_context_);

    char *pub_key_buffer;
    size_t pub_key_buffer_size;
    kaa_get_endpoint_public_key(&pub_key_buffer, &pub_key_buffer_size);

    kaa_digest d;
    kaa_calculate_sha_hash(pub_key_buffer, pub_key_buffer_size, d);
    kaa_status_set_endpoint_public_key_hash(kaa_context_->status, d);
    return result;
}

kaa_error_t kaa_deinit()
{
    kaa_destroy_context(kaa_context_);
    return KAA_ERR_NONE;
}

void kaa_set_user_attached_callback(user_response_handler_t callback)
{
    kaa_set_attachment_callback(kaa_context_, callback);
}

void kaa_set_endpoint_access_token(const char *token)
{
    kaa_status_set_endpoint_access_token(kaa_context_->status, token);
}

void kaa_attach_to_user(const char *user_external_id, const char * user_access_token)
{
    kaa_user_attach_to_user(kaa_context_, user_external_id, user_access_token);
}

#ifndef KAA_DISABLE_FEATURE_EVENTS
void kaa_send_event(const char * fqn, size_t fqn_length, const char *event_data, size_t event_data_size, const char *event_target, size_t event_target_size)
{
    if (kaa_is_endpoint_attached_to_user(kaa_context_->status)) {
        kaa_add_event(
                  kaa_context_
                , fqn
                , fqn_length
                , event_data
                , event_data_size
                , event_target
                , event_target_size
        );
    }
}

kaa_trx_id kaa_start_events_block()
{
    return kaa_event_create_transaction(kaa_context_);
}

void kaa_event_add_to_transaction(kaa_trx_id trx_id, const char * fqn, size_t fqn_length, const char *event_data, size_t event_data_size, const char *event_target, size_t event_target_size)
{
    if (kaa_is_endpoint_attached_to_user(kaa_context_->status)) {
        kaa_add_event_to_transaction(
                  kaa_context_
                , trx_id
                , fqn
                , fqn_length
                , event_data
                , event_data_size
                , event_target
                , event_target_size
        );
    }
}

void kaa_send_events_block(kaa_trx_id trx_id)
{
    kaa_event_finish_transaction(kaa_context_, trx_id);
}

void kaa_remove_events_block(kaa_trx_id trx_id)
{
    kaa_event_remove_transaction(kaa_context_, trx_id);
}

void kaa_register_event_listener(const char *fqn, size_t fqn_length, event_callback_t listener)
{
    kaa_add_on_event_callback(
            kaa_context_->event_manager
            , fqn
            , fqn_length
            , listener
    );
}
#endif

void kaa_set_sync_handler(kaa_sync_t handler, size_t services_count, const kaa_service_t supported_services[])
{
    kaa_channel_manager_set_sync_handler(kaa_context_, handler, services_count, supported_services);
}

size_t kaa_compile_request(kaa_sync_request_t **request_p, size_t service_count, const kaa_service_t services[])
{
    ++global_request_id;
    kaa_sync_request_t *request = kaa_create_sync_request();
    if (request != NULL) {
        request->request_id = kaa_create_int_null_union_int_branch();
        request->request_id->data = KAA_MALLOC(KAA_INT32T);
        *((KAA_INT32T *)request->request_id->data) = global_request_id;

        request->sync_request_meta_data = kaa_create_record_sync_request_meta_data_null_union_sync_request_meta_data_branch();
        request->sync_request_meta_data->data = create_sync_request_meta_data(kaa_context_);

        request->user_sync_request = kaa_create_record_user_sync_request_null_union_user_sync_request_branch();
        request->user_sync_request->data = kaa_user_compile_request(kaa_context_, global_request_id);

        request->event_sync_request = kaa_create_record_event_sync_request_null_union_null_branch();
        request->log_sync_request = kaa_create_record_log_sync_request_null_union_null_branch();
        request->notification_sync_request = kaa_create_record_notification_sync_request_null_union_null_branch();
        request->configuration_sync_request = kaa_create_record_configuration_sync_request_null_union_null_branch();
        request->profile_sync_request = kaa_create_record_profile_sync_request_null_union_null_branch();

        for (;service_count--;) {
            switch (services[service_count]) {
#ifndef KAA_DISABLE_FEATURE_EVENTS
                case KAA_SERVICE_EVENT: {
                    request->event_sync_request->destruct(request->event_sync_request);
                    KAA_FREE(request->event_sync_request);
                    request->event_sync_request = kaa_create_record_event_sync_request_null_union_event_sync_request_branch();
                    request->event_sync_request->data = kaa_event_compile_request(kaa_context_, global_request_id);
                    break;
                }
#endif
                case KAA_SERVICE_PROFILE: {
                    if (kaa_profile_need_profile_resync(kaa_context_)) {
                        request->profile_sync_request->destruct(request->profile_sync_request);
                        KAA_FREE(request->profile_sync_request);
                        kaa_profile_sync_request_t *profile_request = kaa_profile_compile_request(kaa_context_);
                        request->profile_sync_request = kaa_create_record_profile_sync_request_null_union_profile_sync_request_branch();
                        request->profile_sync_request->data = profile_request;
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }
    *request_p = request;
    return request->get_size(request);
}

void kaa_serialize_request(kaa_sync_request_t *request, char *buffer, size_t request_size)
{
    avro_writer_t writer = avro_writer_memory(buffer, request_size);
    request->serialize(writer, request);
    avro_writer_free(writer);
}

void kaa_response_received(const char *buffer, size_t buffer_size)
{
    avro_reader_t reader = avro_reader_memory(buffer, buffer_size);
    kaa_sync_response_t * response = kaa_deserialize_sync_response(reader);
    avro_reader_free(reader);

    KAA_INT32T responseId =
            response->request_id != NULL && response->request_id->type == KAA_INT_NULL_UNION_INT_BRANCH
                    ? *((KAA_INT32T*)response->request_id->data)
                    : 0;
#ifndef KAA_DISABLE_FEATURE_EVENTS
    kaa_list_t * received_events = NULL;
    if (response->event_sync_response != NULL) {
        if (response->event_sync_response->type == KAA_RECORD_EVENT_SYNC_RESPONSE_NULL_UNION_EVENT_SYNC_RESPONSE_BRANCH) {
            kaa_event_sync_response_t * ev_response = response->event_sync_response->data;
            if (ev_response != NULL && ev_response->events != NULL && ev_response->events->type == KAA_ARRAY_EVENT_ARRAY_NULL_UNION_ARRAY_BRANCH) {
                received_events = (kaa_list_t *)ev_response->events->data;
            }
        }
    }
    kaa_event_handle_sync(kaa_context_, responseId, received_events);
#endif
    if (response->user_sync_response != NULL) {
        if (response->user_sync_response->type == KAA_RECORD_USER_SYNC_RESPONSE_NULL_UNION_USER_SYNC_RESPONSE_BRANCH) {
            kaa_user_sync_response_t * usr_response = response->user_sync_response->data;
            if (usr_response != NULL) {
                kaa_user_attach_response_t *     usr_attach_response = NULL;
                kaa_user_attach_notification_t * usr_attach_notif = NULL;
                kaa_user_detach_notification_t * usr_detach_notif = NULL;
                if (usr_response->user_attach_response != NULL
                        && usr_response->user_attach_response->type == KAA_RECORD_USER_ATTACH_RESPONSE_NULL_UNION_USER_ATTACH_RESPONSE_BRANCH)
                {
                    usr_attach_response = usr_response->user_attach_response->data;
                }
                if(usr_response->user_attach_notification != NULL
                        && usr_response->user_attach_notification->type == KAA_RECORD_USER_ATTACH_NOTIFICATION_NULL_UNION_USER_ATTACH_NOTIFICATION_BRANCH)
                {
                    usr_attach_notif = usr_response->user_attach_notification->data;
                }
                if (usr_response->user_detach_notification != NULL
                        && usr_response->user_detach_notification->type == KAA_RECORD_USER_DETACH_NOTIFICATION_NULL_UNION_USER_DETACH_NOTIFICATION_BRANCH)
                {
                    usr_detach_notif = usr_response->user_detach_notification->data;
                }
                kaa_user_handle_sync(kaa_context_, usr_attach_response, usr_attach_notif, usr_detach_notif);
            }
        }
    }

    if (response->profile_sync_response != NULL
            && response->profile_sync_response->type == KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_PROFILE_SYNC_REQUEST_BRANCH) {
        kaa_profile_handle_sync(kaa_context_, (kaa_profile_sync_response_t *)response->profile_sync_response->data);
    }

    kaa_status_save(kaa_context_->status);
    response->destruct(response);
    KAA_FREE(response);
}

void kaa_set_profile(kaa_profile_t *profile_body)
{
    kaa_profile_update_profile(kaa_context_, profile_body);
}


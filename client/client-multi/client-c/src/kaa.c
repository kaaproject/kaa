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

#include <string.h>
#include <stdint.h>

#include "kaa.h"
#include "kaa_mem.h"
#include "kaa_common.h"
#include "kaa_context.h"
#include "kaa_external.h"
#include "kaa_defaults.h"
#include "kaa_log.h"

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

kaa_error_t kaa_init(kaa_context_t **kaa_context_p)
{
    KAA_RETURN_IF_NIL(kaa_context_p, KAA_ERR_BADPARAM);

    // Initialize logger
    kaa_logger_t *logger = NULL;
    kaa_error_t error = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_LOG_TRACE, NULL);  // TODO: make log destination and level configurable
    if (error)
        return error;

    KAA_LOG_INFO(logger, KAA_ERR_NONE, "Kaa SDK version %s, commit hash %s", BUILD_VERSION, BUILD_COMMIT_HASH);

    // Initialize general Kaa context
    error = kaa_context_create(kaa_context_p, logger);
    if (error) {
        KAA_LOG_FATAL(logger, error, "Failed to create Kaa context");
        kaa_log_destroy(logger);
        return error;
    }

    // Initialize endpoint identity
    char *pub_key_buffer = NULL;
    size_t pub_key_buffer_size = 0;
    bool need_deallocation = false;

    kaa_get_endpoint_public_key(&pub_key_buffer, &pub_key_buffer_size, &need_deallocation);
    kaa_digest d;
    error = kaa_calculate_sha_hash(pub_key_buffer, pub_key_buffer_size, d);

    if (need_deallocation && pub_key_buffer_size > 0) {
        KAA_FREE(pub_key_buffer);
    }

    if (error) {
        KAA_LOG_FATAL(logger, error, "Failed to calculate EP ID");
        kaa_context_destroy(*kaa_context_p);
        *kaa_context_p = NULL;
        kaa_log_destroy(logger);
        return error;
    }

    kaa_status_set_endpoint_public_key_hash((*kaa_context_p)->status, d);

    return KAA_ERR_NONE;
}

kaa_error_t kaa_deinit(kaa_context_t *kaa_context)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);

    kaa_logger_t *logger = kaa_context->logger;
    kaa_error_t error = kaa_context_destroy(kaa_context);
    if (error)
        KAA_LOG_ERROR(logger, error, "Failed to destroy Kaa context");
    kaa_log_destroy(logger);
    return error;
}

kaa_error_t kaa_set_user_attached_callback(kaa_context_t *kaa_context, kaa_attachment_status_listeners_t callback)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_set_attachment_listeners(kaa_context, callback);
}

kaa_error_t kaa_set_endpoint_access_token(kaa_context_t *kaa_context, const char *token)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_status_set_endpoint_access_token(kaa_context->status, token);
}

kaa_error_t kaa_attach_to_user(kaa_context_t *kaa_context, const char *user_external_id, const char * user_access_token)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_user_attach_to_user(kaa_context, user_external_id, user_access_token);
}

#ifndef KAA_DISABLE_FEATURE_EVENTS
kaa_error_t kaa_send_event(kaa_context_t *kaa_context, const char * fqn, size_t fqn_length, const char *event_data, size_t event_data_size, const char *event_target, size_t event_target_size)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    KAA_RETURN_IF_NIL(kaa_context->status, KAA_ERR_NOT_INITIALIZED);

    if (kaa_is_endpoint_attached_to_user(kaa_context->status)) {
        return kaa_add_event(
                  kaa_context
                , fqn
                , fqn_length
                , event_data
                , event_data_size
                , event_target
                , event_target_size
        );
    }
    return KAA_ERR_EVENT_NOT_ATTACHED;
}

kaa_error_t kaa_start_event_block(kaa_context_t *kaa_context, kaa_trx_id* trx_id)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_event_create_transaction(kaa_context, trx_id);
}

kaa_error_t kaa_event_add_to_transaction(kaa_context_t *kaa_context, kaa_trx_id trx_id, const char * fqn, size_t fqn_length, const char *event_data, size_t event_data_size, const char *event_target, size_t event_target_size)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);

    if (kaa_is_endpoint_attached_to_user(kaa_context->status)) {
        return kaa_add_event_to_transaction(
                  kaa_context
                , trx_id
                , fqn
                , fqn_length
                , event_data
                , event_data_size
                , event_target
                , event_target_size
        );
    }
    return KAA_ERR_EVENT_NOT_ATTACHED;
}

kaa_error_t kaa_send_events_block(kaa_context_t *kaa_context, kaa_trx_id trx_id)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_event_finish_transaction(kaa_context, trx_id);
}

kaa_error_t kaa_remove_event_block(kaa_context_t *kaa_context, kaa_trx_id trx_id)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_event_remove_transaction(kaa_context, trx_id);
}

kaa_error_t kaa_register_event_listener(kaa_context_t *kaa_context, const char *fqn, size_t fqn_length, event_callback_t listener)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_add_on_event_callback(
            kaa_context->event_manager
            , fqn
            , fqn_length
            , listener
    );
}
#endif

kaa_error_t kaa_set_sync_handler(kaa_context_t *kaa_context, kaa_sync_t handler, size_t services_count, const kaa_service_t supported_services[])
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_channel_manager_set_sync_handler(kaa_context, handler, services_count, supported_services);
}

kaa_error_t kaa_compile_request(kaa_context_t *kaa_context, kaa_sync_request_t **request_p, size_t *result_size, size_t service_count, const kaa_service_t services[])
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    kaa_context->global_request_id++;
    kaa_sync_request_t *request = kaa_create_sync_request();
    if (request != NULL) {
        request->request_id = kaa_create_int_null_union_int_branch();
        request->request_id->data = KAA_MALLOC(uint32_t);
        *((uint32_t *)request->request_id->data) = kaa_context->global_request_id;

        request->sync_request_meta_data = kaa_create_record_sync_request_meta_data_null_union_sync_request_meta_data_branch();
        request->sync_request_meta_data->data = create_sync_request_meta_data(kaa_context);

        request->user_sync_request = kaa_create_record_user_sync_request_null_union_user_sync_request_branch();
        kaa_user_compile_request(kaa_context, (kaa_user_sync_request_t **)&request->user_sync_request->data, kaa_context->global_request_id);

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
                    kaa_event_compile_request(kaa_context, (kaa_event_sync_request_t**)&request->event_sync_request->data, kaa_context->global_request_id);
                    break;
                }
#endif
                case KAA_SERVICE_PROFILE: {
                    if (kaa_profile_need_profile_resync(kaa_context)) {
                        request->profile_sync_request->destruct(request->profile_sync_request);
                        KAA_FREE(request->profile_sync_request);
                        kaa_profile_sync_request_t *profile_request = kaa_profile_compile_request(kaa_context);
                        request->profile_sync_request = kaa_create_record_profile_sync_request_null_union_profile_sync_request_branch();
                        request->profile_sync_request->data = profile_request;
                    }
                    break;
                }
#ifndef KAA_DISABLE_FEATURE_LOGGING
                case KAA_SERVICE_LOGGING: {
                    kaa_log_sync_request_t *log_request = NULL;
                    kaa_logging_compile_request(kaa_context, &log_request);
                    if (log_request) {
                        request->log_sync_request->destruct(request->log_sync_request);
                        KAA_FREE(request->log_sync_request);
                        request->log_sync_request = kaa_create_record_log_sync_request_null_union_log_sync_request_branch();
                        request->log_sync_request->data = log_request;
                    }
                    break;
                }
#endif
                default:
                    break;
            }
        }
    } else {
        return KAA_ERR_NOMEM;
    }
    *request_p = request;
    *result_size = request->get_size(request);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_serialize_request(kaa_sync_request_t *request, char *buffer, size_t request_size)
{
    KAA_RETURN_IF_NIL2(request, buffer, KAA_ERR_BADPARAM);
    avro_writer_t writer = avro_writer_memory(buffer, request_size);
    request->serialize(writer, request);
    avro_writer_free(writer);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_response_received(kaa_context_t *kaa_context, const char *buffer, size_t buffer_size)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);

    avro_reader_t reader = avro_reader_memory(buffer, buffer_size);
    kaa_sync_response_t * response = kaa_deserialize_sync_response(reader);
    avro_reader_free(reader);

#ifndef KAA_DISABLE_FEATURE_EVENTS
    uint32_t responseId =
            response->request_id != NULL && response->request_id->type == KAA_INT_NULL_UNION_INT_BRANCH
                    ? *((uint32_t*)response->request_id->data)
                    : 0;
    kaa_list_t * received_events = NULL;
    kaa_event_sequence_number_response_t * event_sn_response = NULL;
    if (response->event_sync_response != NULL) {
        if (response->event_sync_response->type == KAA_RECORD_EVENT_SYNC_RESPONSE_NULL_UNION_EVENT_SYNC_RESPONSE_BRANCH) {
            kaa_event_sync_response_t * ev_response = response->event_sync_response->data;
            if (ev_response != NULL && ev_response->events != NULL && ev_response->events->type == KAA_ARRAY_EVENT_NULL_UNION_ARRAY_BRANCH) {
                received_events = (kaa_list_t *)ev_response->events->data;
            }
            if (ev_response->event_sequence_number_response != NULL
            		&& ev_response->event_sequence_number_response->type == KAA_RECORD_EVENT_SEQUENCE_NUMBER_RESPONSE_NULL_UNION_EVENT_SEQUENCE_NUMBER_RESPONSE_BRANCH) {
            	event_sn_response = (kaa_event_sequence_number_response_t *) ev_response->event_sequence_number_response->data;
            }
        }
    }
    kaa_event_handle_sync(kaa_context, responseId, event_sn_response, received_events);
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
                kaa_user_handle_sync(kaa_context, usr_attach_response, usr_attach_notif, usr_detach_notif);
            }
        }
    }

    if (response->profile_sync_response != NULL
            && response->profile_sync_response->type == KAA_RECORD_PROFILE_SYNC_REQUEST_NULL_UNION_PROFILE_SYNC_REQUEST_BRANCH) {
        kaa_profile_handle_sync(kaa_context, (kaa_profile_sync_response_t *)response->profile_sync_response->data);
    }

#ifndef KAA_DISABLE_FEATURE_LOGGING
    if (response->log_sync_response != NULL
            && response->log_sync_response->type == KAA_RECORD_LOG_SYNC_REQUEST_NULL_UNION_LOG_SYNC_REQUEST_BRANCH) {
        kaa_logging_handle_sync(kaa_context, (kaa_log_sync_response_t *)response->log_sync_response->data);
    }
#endif

    kaa_status_save(kaa_context->status);
    response->destruct(response);
    KAA_FREE(response);

    return KAA_ERR_NONE;
}

kaa_error_t kaa_set_profile(kaa_context_t *kaa_context, kaa_profile_t *profile_body)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_profile_update_profile(kaa_context, profile_body);
}

#ifndef KAA_DISABLE_FEATURE_LOGGING

kaa_error_t kaa_init_log_storage(
        kaa_context_t *kaa_context
      , kaa_log_storage_t * storage
      , kaa_storage_status_t * status
      , kaa_log_upload_properties_t *properties
      , log_upload_decision_fn need_upl
      )
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_init_log_collector(kaa_context->log_collector, storage, properties, status, need_upl);
}

kaa_error_t kaa_add_log(kaa_context_t *kaa_context, kaa_user_log_record_t *entry)
{
    KAA_RETURN_IF_NIL(kaa_context, KAA_ERR_BADPARAM);
    return kaa_add_log_record(kaa_context, entry);
}

#endif

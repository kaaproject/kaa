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
#include "kaa_mem.h"
#include "kaa_defaults.h"
#include "kaa_context.h"
#include "kaa_external.h"

static kaa_service_t profile_sync_services[1] = { KAA_SERVICE_PROFILE };

struct kaa_profile_manager_t {
    KAA_BOOL need_resync;
    kaa_bytes_t profile_body;
    kaa_digest profile_hash;
};

static kaa_endpoint_version_info_t * create_versions_info() {
    kaa_endpoint_version_info_t *vi = kaa_create_endpoint_version_info();
    vi->config_version = CONFIG_SCHEMA_VERSION;
    vi->log_schema_version = LOG_SCHEMA_VERSION;
    vi->profile_version = PROFILE_SCHEMA_VERSION;
    vi->system_nf_version = SYSTEM_NF_SCHEMA_VERSION;
    vi->user_nf_version = USER_NF_SCHEMA_VERSION;
    if (KAA_EVENT_SCHEMA_VERSIONS_SIZE > 0) {
        vi->event_family_versions =
                kaa_create_array_event_class_family_version_info_null_union_array_branch();
        vi->event_family_versions->data = NULL;
        size_t i = 0;
        for (; i < KAA_EVENT_SCHEMA_VERSIONS_SIZE; ++i) {
            kaa_event_class_family_version_info_t *ecfv =
                    kaa_create_event_class_family_version_info();
            size_t len = strlen(KAA_EVENT_SCHEMA_VERSIONS[i].name);
            ecfv->name = KAA_CALLOC(len + 1, sizeof(char));
            memcpy(ecfv->name, KAA_EVENT_SCHEMA_VERSIONS[i].name, len);
            ecfv->version = KAA_EVENT_SCHEMA_VERSIONS[i].version;
            if (vi->event_family_versions->data != NULL) {
                kaa_list_push_back(vi->event_family_versions->data, ecfv);
            } else {
                vi->event_family_versions->data = kaa_list_create(ecfv);
            }
        }
    } else {
        vi->event_family_versions =
                kaa_create_array_event_class_family_version_info_null_union_null_branch();
    }
    return vi;
}

/**
 * PUBLIC FUNCTIONS
 */
kaa_error_t kaa_create_profile_manager(
        kaa_profile_manager_t ** profile_manager_p) {
    kaa_profile_manager_t * profile_manager = KAA_MALLOC(kaa_profile_manager_t);
    if (profile_manager == NULL ) {
        return KAA_ERR_NOMEM;
    }
    profile_manager->need_resync = 1;

    profile_manager->profile_body.size = 0;
    profile_manager->profile_body.buffer = NULL;

    *profile_manager_p = profile_manager;
    return KAA_ERR_NONE;
}

void kaa_destroy_profile_manager(kaa_profile_manager_t *profile_manager) {
    kaa_destroy_bytes(&profile_manager->profile_body);
    KAA_FREE(profile_manager);
}

int kaa_profile_need_profile_resync(void *ctx)
{
    kaa_context_t *context = (kaa_context_t *) ctx;
    return context->profile_manager->need_resync;
}

kaa_profile_sync_request_t * kaa_profile_compile_request(void *ctx) {
    kaa_context_t *context = (kaa_context_t *) ctx;

    kaa_profile_sync_request_t * request = kaa_create_profile_sync_request();
    request->version_info = create_versions_info();
    char * ep_acc_token = kaa_status_get_endpoint_access_token(context->status);
    if (ep_acc_token) {
        request->endpoint_access_token =
                kaa_create_string_null_union_string_branch();
        size_t len = strlen(ep_acc_token);
        request->endpoint_access_token->data = KAA_CALLOC(len + 1, sizeof(char));
        memcpy(request->endpoint_access_token->data, ep_acc_token, len);
    } else {
        request->endpoint_access_token =
                kaa_create_string_null_union_null_branch();
    }

    kaa_profile_manager_t *profile_manager = context->profile_manager;

    request->profile_body = KAA_MALLOC(kaa_bytes_t);
    request->profile_body->size = profile_manager->profile_body.size;
    if (request->profile_body->size > 0) {
        request->profile_body->buffer =
                KAA_CALLOC(request->profile_body->size, sizeof(char));
        memcpy(request->profile_body->buffer,
                profile_manager->profile_body.buffer,
                profile_manager->profile_body.size);
    } else {
        request->profile_body->buffer = NULL;
    }

    if (kaa_is_endpoint_registered(context->status)) {
        request->endpoint_public_key = kaa_create_bytes_null_union_null_branch();
    } else {
        request->endpoint_public_key = kaa_create_bytes_null_union_bytes_branch();
        kaa_bytes_t *pub_key = KAA_CALLOC(1, sizeof(kaa_bytes_t));
        kaa_get_endpoint_public_key((char **)&pub_key->buffer, (size_t *)&pub_key->size);
        request->endpoint_public_key->data = pub_key;
    }
    return request;
}

void kaa_profile_handle_sync(void *ctx,
        kaa_profile_sync_response_t *response) {
    kaa_context_t *context = (kaa_context_t *) ctx;
    context->profile_manager->need_resync = 0;
    if (response != NULL ) {
        if (response->response_status == ENUM_SYNC_RESPONSE_STATUS_RESYNC) {
            context->profile_manager->need_resync = 1;
            kaa_sync_t sync = kaa_channel_manager_get_sync_handler(context,
                    profile_sync_services[0]);
            if (sync != NULL ) {
                (*sync)(1, profile_sync_services);
            }
        } else if (!kaa_is_endpoint_registered(context->status)) {
            kaa_set_endpoint_registered(context->status, 1);
        }
    }
}

kaa_error_t kaa_profile_update_profile(void *ctx, kaa_profile_t * profile_body) {
    if (!ctx || !profile_body) {
        return KAA_ERR_BADPARAM;
    }

    size_t serialized_profile_size = profile_body->get_size(profile_body);
    char* serialized_profile = KAA_CALLOC(serialized_profile_size, sizeof(char));

    avro_writer_t writer = avro_writer_memory(serialized_profile, serialized_profile_size);
    profile_body->serialize(writer, profile_body);
    avro_writer_free(writer);

    kaa_digest new_hash;
    kaa_calculate_sha_hash(serialized_profile, serialized_profile_size, new_hash);

    kaa_context_t *context = (kaa_context_t *) ctx;
    kaa_digest * old_hash = kaa_status_get_profile_hash(context->status);

    kaa_profile_manager_t * profile_manager = context->profile_manager;
    if (old_hash != NULL && 0 == memcmp(new_hash, *old_hash, SHA_1_DIGEST_LENGTH)) {
        profile_manager->need_resync = 0;
        KAA_FREE(serialized_profile);
        return KAA_ERR_NONE;
    }

    kaa_status_set_profile_hash(context->status, new_hash);

    if (profile_manager->profile_body.size > 0) {
        KAA_FREE(profile_manager->profile_body.buffer);
        profile_manager->profile_body.buffer = NULL;
    }

    profile_manager->profile_body.buffer = (uint8_t*)serialized_profile;
    profile_manager->profile_body.size = serialized_profile_size;

    profile_manager->need_resync = 1;

    kaa_sync_t sync = kaa_channel_manager_get_sync_handler(context,
            profile_sync_services[0]);
    if (sync) {
        (*sync)(1, profile_sync_services);
    }

    return KAA_ERR_NONE;
}

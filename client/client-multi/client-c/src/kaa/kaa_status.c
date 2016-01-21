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
#include <stddef.h>
#include <stdint.h>
#include <stdbool.h>
#include "platform/stdio.h"
#include "platform/ext_sha.h"
#include "platform/ext_status.h"
#include "kaa_status.h"
#include "kaa_common.h"
#include "kaa_defaults.h"
#include "utilities/kaa_mem.h"
#include <string.h>


#define KAA_STATUS_STATIC_SIZE      (sizeof(bool) + sizeof(bool) + sizeof(uint32_t) + sizeof(uint32_t) + sizeof(size_t) + sizeof(uint32_t) + sizeof(uint16_t) + SHA_1_DIGEST_LENGTH * sizeof(char) * 2 + sizeof(KAA_SDK_TOKEN))

#define READ_BUFFER(FROM, TO, SIZE) \
        memcpy(TO, FROM, SIZE); \
        FROM += SIZE

#define WRITE_BUFFER(FROM, TO, SIZE) \
        memcpy(TO, FROM, SIZE); \
        TO += SIZE

kaa_error_t kaa_status_create(kaa_status_t ** kaa_status_p)
{
    KAA_RETURN_IF_NIL(kaa_status_p, KAA_ERR_BADPARAM);

    kaa_status_t * kaa_status = (kaa_status_t *) KAA_CALLOC(1, sizeof(kaa_status_t));
    KAA_RETURN_IF_NIL(kaa_status, KAA_ERR_NOMEM);

    char token_buf[sizeof(KAA_SDK_TOKEN)];
    kaa_status->topic_states = kaa_list_create();
    KAA_RETURN_IF_NIL(kaa_status->topic_states, KAA_ERR_NOMEM);

    char *  read_buf = NULL;
    char *  read_buf_head = NULL;
    size_t  read_size = 0;
    bool    needs_deallocation = false;
    ext_status_read(&read_buf, &read_size, &needs_deallocation);
    read_buf_head = read_buf;
    if (read_size >= KAA_STATUS_STATIC_SIZE + sizeof(size_t)) {
        READ_BUFFER(read_buf, &kaa_status->is_registered, sizeof(kaa_status->is_registered));
        READ_BUFFER(read_buf, &kaa_status->is_attached, sizeof(kaa_status->is_attached));
        READ_BUFFER(read_buf, &kaa_status->event_seq_n, sizeof(kaa_status->event_seq_n));
        READ_BUFFER(read_buf, &kaa_status->log_bucket_id, sizeof(kaa_status->log_bucket_id));
        READ_BUFFER(read_buf, kaa_status->endpoint_public_key_hash, SHA_1_DIGEST_LENGTH);
        READ_BUFFER(read_buf, kaa_status->profile_hash, SHA_1_DIGEST_LENGTH);

        size_t enpoint_access_token_length = 0;
        READ_BUFFER(read_buf, &enpoint_access_token_length, sizeof(enpoint_access_token_length));

        if (enpoint_access_token_length > 0) {
            kaa_status->endpoint_access_token = (char * ) KAA_MALLOC((enpoint_access_token_length + 1) * sizeof(char));
            if (!kaa_status->endpoint_access_token) {
                KAA_FREE(kaa_status);
                return KAA_ERR_NOMEM;
            }
            READ_BUFFER(read_buf, kaa_status->endpoint_access_token, enpoint_access_token_length);
            kaa_status->endpoint_access_token[enpoint_access_token_length] = '\0';
        }

        size_t states_count = 0;
        READ_BUFFER(read_buf, &states_count, sizeof(size_t));
        while (states_count--) {
            kaa_topic_state_t *state = (kaa_topic_state_t *)KAA_MALLOC(sizeof(kaa_topic_state_t));
            KAA_RETURN_IF_NIL(state, KAA_ERR_NOMEM);
            READ_BUFFER(read_buf, &state->topic_id, sizeof(uint64_t));
            READ_BUFFER(read_buf, &state->sqn_number, sizeof(uint32_t));

            if (!kaa_list_push_back(kaa_status->topic_states, state)) {
                KAA_FREE(state);
                return KAA_ERR_NOMEM;
            }
        }

        READ_BUFFER(read_buf, token_buf, sizeof(token_buf));
        if (strcmp(token_buf, KAA_SDK_TOKEN))
            kaa_status->is_registered = false;
        else
            kaa_status->is_updated = true;
    }

    if (needs_deallocation)
        KAA_FREE(read_buf_head);

    *kaa_status_p = kaa_status;
    return KAA_ERR_NONE;
}

void kaa_status_destroy(kaa_status_t *self)
{
    if (self) {
        KAA_FREE(self->endpoint_access_token);
        kaa_list_destroy(self->topic_states, NULL);
        KAA_FREE(self);
    }
}

kaa_error_t kaa_status_set_endpoint_access_token(kaa_status_t * self, const char *token)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    if (self->endpoint_access_token)
        KAA_FREE(self->endpoint_access_token);

    size_t len = strlen(token);
    self->endpoint_access_token = (char *) KAA_MALLOC((len + 1) * sizeof(char));
    if (!self->endpoint_access_token)
        return KAA_ERR_NOMEM;
    strcpy(self->endpoint_access_token, token);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_status_set_registered(kaa_status_t *self, bool is_registered)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    self->is_registered = is_registered;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_status_save(kaa_status_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    size_t endpoint_access_token_length = self->endpoint_access_token ? strlen(self->endpoint_access_token) : 0;
    size_t states_count = kaa_list_get_size(self->topic_states);
    size_t buffer_size = KAA_STATUS_STATIC_SIZE + sizeof(endpoint_access_token_length) + endpoint_access_token_length + states_count * (sizeof(uint32_t) + sizeof(uint64_t));

    char *buffer_head = (char *) KAA_MALLOC(buffer_size * sizeof(char));
    KAA_RETURN_IF_NIL(buffer_head, KAA_ERR_NOMEM);

    char *buffer = buffer_head;

    WRITE_BUFFER(&self->is_registered, buffer, sizeof(self->is_registered));
    WRITE_BUFFER(&self->is_attached, buffer, sizeof(self->is_attached));
    WRITE_BUFFER(&self->event_seq_n, buffer, sizeof(self->event_seq_n));
    WRITE_BUFFER(&self->log_bucket_id, buffer, sizeof(self->log_bucket_id));
    WRITE_BUFFER(self->endpoint_public_key_hash, buffer, SHA_1_DIGEST_LENGTH);
    WRITE_BUFFER(self->profile_hash, buffer, SHA_1_DIGEST_LENGTH);
    WRITE_BUFFER(&endpoint_access_token_length, buffer, sizeof(endpoint_access_token_length));
    if (endpoint_access_token_length) {
        WRITE_BUFFER(self->endpoint_access_token, buffer, endpoint_access_token_length);
    }

    kaa_list_node_t *state_node = kaa_list_begin(self->topic_states);
    WRITE_BUFFER(&states_count, buffer, sizeof(size_t));
    while (state_node) {
        kaa_topic_state_t *state = (kaa_topic_state_t *)kaa_list_get_data(state_node);
        WRITE_BUFFER(&state->topic_id, buffer, sizeof(uint64_t));
        WRITE_BUFFER(&state->sqn_number, buffer, sizeof(uint32_t));
        state_node = kaa_list_next(state_node);
    }
    WRITE_BUFFER(KAA_SDK_TOKEN, buffer, sizeof(KAA_SDK_TOKEN));

    ext_status_store(buffer_head, buffer_size);

    KAA_FREE(buffer_head);

    return KAA_ERR_NONE;
}

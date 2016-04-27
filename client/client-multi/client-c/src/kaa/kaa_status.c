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

#include <stddef.h>
#include <stdint.h>
#include <stdbool.h>
#include <string.h>

#include "platform/ext_notification_receiver.h"
#include "platform/ext_sha.h"
#include "platform/ext_status.h"
#include "kaa_status.h"
#include "kaa_defaults.h"
#include "utilities/kaa_mem.h"

/*
 * KAA Status persistent content:
 *
 * is_registered                    sizeof(bool)
 * is_attached                      sizeof(bool)
 * event_seq_n                      sizeof(uint32_t)
 * endpoint_public_key_hash         SHA_1_DIGEST_LENGTH * sizeof(char)
 * profile_hash                     SHA_1_DIGEST_LENGTH * sizeof(char)
 * profile_needs_resync             sizeof(bool)
 * endpoint_access_token_length     sizeof(size_t)
 * endpoint_access_token (variable length)
 * states_count                     sizeof(size_t)
 * states (variable length)
 *  topic_id
 *  sqn_number
 * topics_count                     sizeof(size_t)
 * topics (variable length)
 *  topic_id
 *  subscription_type
 *  name_length
 *  name
 * topic_hash                       sizeof(int32_t)
 * token_buf                        sizeof(KAA_SDK_TOKEN)
 */
#define KAA_STATUS_STATIC_SIZE      (sizeof(bool) + sizeof(bool) + sizeof(size_t) + sizeof(bool) + sizeof(uint32_t) + sizeof(size_t) + SHA_1_DIGEST_LENGTH * sizeof(char) * 2 + sizeof(KAA_SDK_TOKEN))

#define READ_BUFFER(FROM, TO, SIZE) \
        memcpy(TO, FROM, SIZE); \
        FROM += SIZE

#define WRITE_BUFFER(FROM, TO, SIZE) \
        memcpy(TO, FROM, SIZE); \
        TO += SIZE

// TODO KAA-845: discuss/implement a failover, when storage is somehow broken
kaa_error_t kaa_status_create(kaa_status_t ** kaa_status_p)
{
    KAA_RETURN_IF_NIL(kaa_status_p, KAA_ERR_BADPARAM);

    kaa_status_t *kaa_status = KAA_CALLOC(1, sizeof(*kaa_status));
    KAA_RETURN_IF_NIL(kaa_status, KAA_ERR_NOMEM);

    kaa_status->topic_states = kaa_list_create();
    KAA_RETURN_IF_NIL(kaa_status->topic_states, KAA_ERR_NOMEM);
    kaa_status->topics = kaa_list_create();
    KAA_RETURN_IF_NIL(kaa_status->topics, KAA_ERR_NOMEM);

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
        READ_BUFFER(read_buf, kaa_status->endpoint_public_key_hash, SHA_1_DIGEST_LENGTH);
        READ_BUFFER(read_buf, kaa_status->profile_hash, SHA_1_DIGEST_LENGTH);
        READ_BUFFER(read_buf, &kaa_status->profile_needs_resync, sizeof(kaa_status->profile_needs_resync));

        size_t endpoint_access_token_length = 0;
        READ_BUFFER(read_buf, &endpoint_access_token_length, sizeof(endpoint_access_token_length));

        if (endpoint_access_token_length > 0) {
            kaa_status->endpoint_access_token = KAA_MALLOC((endpoint_access_token_length + 1) * sizeof(char));
            if (!kaa_status->endpoint_access_token) {
                KAA_FREE(kaa_status);
                return KAA_ERR_NOMEM;
            }
            READ_BUFFER(read_buf, kaa_status->endpoint_access_token, endpoint_access_token_length);
            kaa_status->endpoint_access_token[endpoint_access_token_length] = '\0';
        }

        size_t states_count = 0;
        READ_BUFFER(read_buf, &states_count, sizeof(size_t));
        while (states_count--) {
            kaa_topic_state_t *state = KAA_MALLOC(sizeof(kaa_topic_state_t));
            KAA_RETURN_IF_NIL(state, KAA_ERR_NOMEM);
            READ_BUFFER(read_buf, &state->topic_id, sizeof(uint64_t));
            READ_BUFFER(read_buf, &state->sqn_number, sizeof(uint32_t));

            if (!kaa_list_push_back(kaa_status->topic_states, state)) {
                KAA_FREE(state);
                return KAA_ERR_NOMEM;
            }
        }

        // Restore all topics

        size_t topics_count;
        READ_BUFFER(read_buf, &topics_count, sizeof(topics_count));

        while (topics_count--) {
            kaa_topic_t *topic = KAA_MALLOC(sizeof(*topic));
            KAA_RETURN_IF_NIL(topic, KAA_ERR_NOMEM);

            READ_BUFFER(read_buf, &topic->id, sizeof(topic->id));
            READ_BUFFER(read_buf, &topic->subscription_type,
                        sizeof(topic->subscription_type));
            READ_BUFFER(read_buf, &topic->name_length, sizeof(topic->name_length));

            if (topic->name_length) {
                topic->name = KAA_CALLOC(topic->name_length + 1, 1);
                KAA_RETURN_IF_NIL(topic->name, KAA_ERR_NOMEM);
                READ_BUFFER(read_buf, topic->name, topic->name_length);
            }

            if (!kaa_list_push_back(kaa_status->topics, topic)) {
                KAA_FREE(topic);
                return KAA_ERR_NOMEM;
            }
        }

        READ_BUFFER(read_buf, &kaa_status->topic_list_hash, sizeof(kaa_status->topic_list_hash));
        char token_buf[sizeof(KAA_SDK_TOKEN)];
        READ_BUFFER(read_buf, token_buf, sizeof(token_buf));

        // TODO: shouldn't that be memcmp?
        if (strcmp(token_buf, KAA_SDK_TOKEN)) {
            kaa_status->is_registered = false;
        } else {
            kaa_status_set_updated(kaa_status, true);
        }
    }

    if (needs_deallocation) {
        KAA_FREE(read_buf_head);
    }

    *kaa_status_p = kaa_status;
    return KAA_ERR_NONE;
}

void kaa_status_destroy(kaa_status_t *self)
{
    if (self) {
        KAA_FREE(self->endpoint_access_token);
        kaa_list_destroy(self->topic_states, NULL);
        kaa_list_destroy(self->topics, NULL);
        KAA_FREE(self);
    }
}

kaa_error_t kaa_status_set_endpoint_access_token(kaa_status_t * self, const char *token)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    char *new_token;
    size_t len = strlen(token);

    /* Do not delete old access token before new will be allocated */

    new_token = KAA_MALLOC((len + 1) * sizeof(char));
    if (!new_token)
        return KAA_ERR_NOMEM;

    strcpy(new_token, token);

    if (self->endpoint_access_token)
        KAA_FREE(self->endpoint_access_token);

    self->endpoint_access_token = new_token;
    self->has_update = true;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_status_set_registered(kaa_status_t *self, bool is_registered)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    self->is_registered = is_registered;
    self->has_update = true;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_status_set_attached(kaa_status_t *self, bool is_attached)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    self->is_attached = is_attached;
    self->has_update = true;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_status_set_updated(kaa_status_t *self, bool is_updated)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    self->is_updated = is_updated;
    self->has_update = true;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_status_save(kaa_status_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    if (!self->has_update)
        return KAA_ERR_NONE;

    size_t endpoint_access_token_length = self->endpoint_access_token ? strlen(self->endpoint_access_token) : 0;
    size_t states_count = kaa_list_get_size(self->topic_states);
    size_t topics_count = kaa_list_get_size(self->topics);

    // Calculate size of whole list of topics
    kaa_list_node_t *topic_node = kaa_list_begin(self->topics);
    size_t topics_size = 0;
    while (topic_node) {
        kaa_topic_t *topic = kaa_list_get_data(topic_node);

        topics_size += sizeof(topic->id)
                    + sizeof(topic->subscription_type)
                    + sizeof(topic->name_length)
                    + topic->name_length;

        topic_node = kaa_list_next(topic_node);
    }


    size_t buffer_size = KAA_STATUS_STATIC_SIZE
            + sizeof(endpoint_access_token_length)
            + endpoint_access_token_length
            /*               Topic ID            Sequence number  */
            + states_count * (sizeof(uint32_t) + sizeof(uint64_t))
            + topics_size
            + sizeof(int32_t);

    char *buffer_head = KAA_MALLOC(buffer_size * sizeof(char));
    KAA_RETURN_IF_NIL(buffer_head, KAA_ERR_NOMEM);

    char *buffer = buffer_head;

    WRITE_BUFFER(&self->is_registered, buffer, sizeof(self->is_registered));
    WRITE_BUFFER(&self->is_attached, buffer, sizeof(self->is_attached));
    WRITE_BUFFER(&self->event_seq_n, buffer, sizeof(self->event_seq_n));
    WRITE_BUFFER(self->endpoint_public_key_hash, buffer, SHA_1_DIGEST_LENGTH);
    WRITE_BUFFER(self->profile_hash, buffer, SHA_1_DIGEST_LENGTH);
    WRITE_BUFFER(&self->profile_needs_resync, buffer, sizeof(self->profile_needs_resync));
    WRITE_BUFFER(&endpoint_access_token_length, buffer,
                 sizeof(endpoint_access_token_length));
    if (endpoint_access_token_length) {
        WRITE_BUFFER(self->endpoint_access_token, buffer, endpoint_access_token_length);
    }

    kaa_list_node_t *state_node = kaa_list_begin(self->topic_states);
    WRITE_BUFFER(&states_count, buffer, sizeof(size_t));
    while (state_node) {
        kaa_topic_state_t *state = kaa_list_get_data(state_node);
        WRITE_BUFFER(&state->topic_id, buffer, sizeof(uint64_t));
        WRITE_BUFFER(&state->sqn_number, buffer, sizeof(uint32_t));
        state_node = kaa_list_next(state_node);
    }

    topic_node = kaa_list_begin(self->topics);
    WRITE_BUFFER(&topics_count, buffer, sizeof(topics_count));
    while (topic_node) {
        kaa_topic_t *topic = kaa_list_get_data(topic_node);

        WRITE_BUFFER(&topic->id, buffer, sizeof(topic->id));
        WRITE_BUFFER(&topic->subscription_type, buffer, sizeof(topic->subscription_type));
        WRITE_BUFFER(&topic->name_length, buffer, sizeof(topic->name_length));
        WRITE_BUFFER(topic->name, buffer, topic->name_length);
        topic_node = kaa_list_next(topic_node);
    }

    WRITE_BUFFER(&self->topic_list_hash, buffer, sizeof(self->topic_list_hash));
    WRITE_BUFFER(KAA_SDK_TOKEN, buffer, sizeof(KAA_SDK_TOKEN));

    ext_status_store(buffer_head, buffer_size);

    KAA_FREE(buffer_head);

    self->has_update = false;

    return KAA_ERR_NONE;
}

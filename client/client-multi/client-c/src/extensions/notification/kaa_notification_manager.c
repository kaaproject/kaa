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

#include "kaa_notification_manager.h"

#include <string.h>
#include <stdio.h>

#include "kaa_status.h"
#include "kaa_platform_common.h"
#include "utilities/kaa_mem.h"
#include "kaa_common.h"
#include "utilities/kaa_log.h"
#include "kaa_platform_utils.h"
#include "kaa_channel_manager.h"
#include "platform-impl/common/kaa_htonll.h"

#include "platform/sock.h"

struct kaa_notification_manager_t {
    kaa_list_t                     *mandatory_listeners;
    kaa_list_t                     *topics_listeners;
    kaa_list_t                     *optional_listeners;
    kaa_list_t                     *subscriptions;
    kaa_list_t                     *unsubscriptions;
    kaa_list_t                     *uids;
    kaa_list_t                     *notifications;
    size_t                         extension_payload_size;

    kaa_platform_message_writer_t  *writer;

    kaa_status_t                   *status;
    kaa_channel_manager_t          *channel_manager;
    kaa_logger_t                   *logger;
};

typedef enum {
    KAA_CLIENT_WANTS_TO_RECEIVE_NOTIFICATIONS = 0x01,
    KAA_SUBSCRIBED_TOPIC_HASH_IS_PRESENT = 0x02,
} kaa_notification_extension_flags_t;

typedef enum {
    KAA_NO_DELTA,
    KAA_DELTA,
    KAA_RESYNC,
} kaa_sync_response_status;

typedef enum {
    SYSTEM = 0x0,
    CUSTOM = 0x1,
} kaa_notification_type;

typedef struct {
    uint16_t length;
    void *data;
} kaa_notifications_uid_t;

#define   TOPICS                 0x0
#define   NOTIFICATIONS          0x1
#define   TOPICS_STATE_ID        0x0
#define   UID_ID                 0x1
#define   SUBSCRIPTION_ID        0x2
#define   UNSUBSCRIPTION_ID      0x3

typedef struct {
    uint64_t topic_id;
    kaa_list_t *listeners;
} kaa_optional_notification_listeners_wrapper_t;

typedef struct {
    uint32_t id;
    kaa_notification_listener_t listener;
} kaa_notification_listener_wrapper_t;

typedef struct {
    uint32_t id;
    kaa_topic_listener_t listener;
} kaa_topic_listener_wrapper_t;

static kaa_error_t kaa_notification_manager_create(kaa_notification_manager_t **self, kaa_status_t *status, kaa_channel_manager_t *channel_manager, kaa_logger_t *logger);
void kaa_notification_manager_destroy(kaa_notification_manager_t *self);

static kaa_error_t kaa_notification_manager_get_size(kaa_notification_manager_t *self, size_t *expected_size);
static kaa_error_t kaa_notification_manager_request_serialize(kaa_notification_manager_t *self,
        kaa_platform_message_writer_t *writer);
static kaa_error_t kaa_notification_manager_handle_server_sync(kaa_notification_manager_t *self, kaa_platform_message_reader_t *reader, uint32_t extension_length);

kaa_error_t kaa_extension_notification_init(kaa_context_t *kaa_context, void **context)
{
    kaa_error_t error = kaa_notification_manager_create(&kaa_context->notification_manager,
            kaa_context->status->status_instance,
            kaa_context->channel_manager,
            kaa_context->logger);
    *context = kaa_context->notification_manager;
    return error;
}

kaa_error_t kaa_extension_notification_deinit(void *context)
{
    kaa_notification_manager_destroy(context);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_extension_notification_request_get_size(void *context, size_t *expected_size)
{
    return kaa_notification_manager_get_size(context, expected_size);
}

kaa_error_t kaa_extension_notification_request_serialize(void *context, uint32_t request_id,
        uint8_t *buffer, size_t *size, bool *need_resync)
{
    (void)request_id;

    // TODO(KAA-982): Use asserts
    if (!context || !size || !need_resync) {
        return KAA_ERR_BADPARAM;
    }

    *need_resync = true;

    size_t size_needed;
    kaa_error_t error = kaa_notification_manager_get_size(context, &size_needed);
    if (error) {
        return error;
    }

    if (!buffer || *size < size_needed) {
        *size = size_needed;
        return KAA_ERR_BUFFER_IS_NOT_ENOUGH;
    }

    *size = size_needed;

    kaa_platform_message_writer_t writer = KAA_MESSAGE_WRITER(buffer, *size);
    error = kaa_notification_manager_request_serialize(context, &writer);
    if (error) {
        return error;
    }

    *size = writer.current - buffer;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_extension_notification_server_sync(void *context, uint32_t request_id,
        uint16_t extension_options, const uint8_t *buffer, size_t size)
{
    (void)request_id;
    (void)extension_options;

    // TODO(KAA-982): Use asserts
    if (!context || !buffer) {
        return KAA_ERR_BADPARAM;
    }

    kaa_platform_message_reader_t reader = KAA_MESSAGE_READER(buffer, size);
    return kaa_notification_manager_handle_server_sync(context, &reader, size);
}

static void shift_and_sub_extension(kaa_platform_message_reader_t *reader, uint32_t *extension_length, size_t size)
{
    KAA_RETURN_IF_NIL2(reader, extension_length,);
    reader->current += size;
    *extension_length -= size;
}

typedef struct {
    kaa_notification_t *notification;
    uint32_t sqn;
} kaa_notification_wrapper_t;

typedef struct {
    uint64_t    topic_id;
    kaa_list_t *notifications;
} kaa_topic_notifications_node_t;

static bool find_notifications_by_topic(void *data, void *context)
{
    kaa_topic_notifications_node_t *node = data;
    return node->topic_id == *((uint64_t *) context);
}

static bool sort_topic_by_id(void *node_1, void *node_2)
{
    KAA_RETURN_IF_NIL2(node_1, node_2, false);
    kaa_topic_t *wrapper_1 = node_1;
    kaa_topic_t *wrapper_2 = node_2;
    return wrapper_1->id < wrapper_2->id;
}

static uint64_t get_topic_id(void *node)
{
    kaa_topic_t *wrapper = node;
    return wrapper->id;
}

static void kaa_destroy_notification_wrapper(void *data)
{
    KAA_RETURN_IF_NIL(data, );
    kaa_notification_wrapper_t *wrapper = data;
    if (wrapper->notification) {
        wrapper->notification->destroy(wrapper->notification);
    }
    KAA_FREE(wrapper);
}

static void kaa_destroy_notification_node(void *data)
{
    KAA_RETURN_IF_NIL(data, );
    kaa_topic_notifications_node_t *node = data;
    if (node->notifications) {
        kaa_list_destroy(node->notifications, kaa_destroy_notification_wrapper);
    }
    KAA_FREE(node);
}

static kaa_error_t kaa_create_topic_notification_node(kaa_topic_notifications_node_t **node, kaa_notification_t *item, uint32_t *sqn, uint64_t topic_id)
{
    KAA_RETURN_IF_NIL(node, KAA_ERR_BADPARAM);

    kaa_topic_notifications_node_t *new_node = KAA_MALLOC(sizeof(*new_node));
    KAA_RETURN_IF_NIL(new_node, KAA_ERR_NOMEM);

    new_node->notifications = kaa_list_create();
    if (!new_node->notifications) {
        kaa_destroy_notification_node(new_node);
        return KAA_ERR_NOMEM;
    }

    kaa_notification_wrapper_t *wrapper = KAA_MALLOC(sizeof(*wrapper));
    if (!wrapper) {
        kaa_destroy_notification_node(new_node);
        return KAA_ERR_NOMEM;
    }

    if (!kaa_list_push_back(new_node->notifications, wrapper)) {
        kaa_destroy_notification_node(new_node);
        KAA_FREE(wrapper);
        return KAA_ERR_NOMEM;
    }

    new_node->topic_id = topic_id;
    wrapper->notification = item;
    wrapper->sqn = *sqn;
    *node = new_node;
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_add_notification_to_map(kaa_list_t *notifications, kaa_notification_t *item, uint64_t topic_id, uint32_t *sqn)
{
    KAA_RETURN_IF_NIL3(notifications, item, sqn, KAA_ERR_BADPARAM);
    kaa_list_node_t *notification_list_node = kaa_list_find_next(kaa_list_begin(notifications), find_notifications_by_topic, &topic_id);
    kaa_topic_notifications_node_t *notification_node = NULL;
    if (!notification_list_node) {
        kaa_error_t err = kaa_create_topic_notification_node(&notification_node, item, sqn, topic_id);
        if (err) {
            kaa_list_destroy(notifications, &kaa_destroy_notification_node);
            item->destroy(item);
            return err;
        }
        if (!kaa_list_push_back(notifications, notification_node)) {
            kaa_list_destroy(notifications, &kaa_destroy_notification_node);
            kaa_destroy_notification_node(notification_node);
            item->destroy(item);
            return KAA_ERR_NOMEM;
        }
    } else {
        notification_node = kaa_list_get_data(notification_list_node);
        kaa_notification_wrapper_t *new_wrapper = KAA_MALLOC(sizeof(*new_wrapper));
        if (!new_wrapper) {
            kaa_list_destroy(notifications, &kaa_destroy_notification_node);
            item->destroy(item);
            return KAA_ERR_NOMEM;
        }
        if (!kaa_list_push_back(notification_node->notifications, new_wrapper)) {
            kaa_list_destroy(notifications, &kaa_destroy_notification_node);
            item->destroy(item);
            KAA_FREE(new_wrapper);
            return KAA_ERR_NOMEM;
        }

        new_wrapper->notification = item;
        new_wrapper->sqn = *sqn;
    }

    return KAA_ERR_NONE;
}

static bool kaa_predicate_for_notifications(void *notif_1, void *notif_2)
{
    KAA_RETURN_IF_NIL2(notif_1, notif_2, false);
    kaa_notification_wrapper_t *wrapper_1 = notif_1;
    kaa_notification_wrapper_t *wrapper_2 = notif_2;
    return wrapper_1->sqn < wrapper_2->sqn;
}
static void kaa_sort_notifications_by_sqn(void *data, void *context)
{
    (void)context;
    kaa_topic_notifications_node_t *node = data;
    kaa_list_sort(node->notifications, &kaa_predicate_for_notifications);
}

static void kaa_sort_notifications(kaa_list_t *notifications)
{
    kaa_list_for_each(kaa_list_begin(notifications), kaa_list_back(notifications), &kaa_sort_notifications_by_sqn, NULL);
}

static kaa_extension_id notification_sync_services[] = { KAA_EXTENSION_NOTIFICATION };

static bool kaa_find_notification_listener_by_id(void *listener, void *context)
{
    KAA_RETURN_IF_NIL2(listener, context, KAA_ERR_NONE);
    return ((kaa_notification_listener_wrapper_t *) listener)->id == *(uint32_t *)context;
}
static bool kaa_find_topic_listener_by_id(void *listener, void *context)
{
    KAA_RETURN_IF_NIL2(listener, context, KAA_ERR_NONE);
    return ((kaa_topic_listener_wrapper_t *) listener)->id == *(uint32_t *)context;
}

static bool kaa_find_optional_notification_listener_by_id (void *optional_listener_list, void *topic_id)
{
    KAA_RETURN_IF_NIL2(optional_listener_list, topic_id, KAA_ERR_NONE);
    kaa_optional_notification_listeners_wrapper_t *optional_listener_node = optional_listener_list;
    return optional_listener_node->topic_id == *(uint64_t *)topic_id;
}

static bool kaa_find_topic_by_id(void *topic, void *id)
{
    KAA_RETURN_IF_NIL2(topic, id, KAA_ERR_NONE);
    kaa_topic_t* notification_topic = topic;
    return notification_topic->id == *(uint64_t *)id;
}

static kaa_error_t kaa_find_topic(kaa_notification_manager_t *self, kaa_topic_t **topic, uint64_t *topic_id)
{
    KAA_RETURN_IF_NIL2(topic_id, topic, KAA_ERR_BADPARAM);
    kaa_list_node_t *topic_node = kaa_list_find_next(kaa_list_begin(self->status->topics), &kaa_find_topic_by_id, topic_id);
    if (!topic_node) {
        return KAA_ERR_NOT_FOUND;
    }

    *topic = kaa_list_get_data(topic_node);
    return KAA_ERR_NONE;
}

static bool kaa_find_uid(void *data, void *context)
{
    KAA_RETURN_IF_NIL2(data, context, KAA_ERR_NONE);
    kaa_notifications_uid_t *uid = data;
    kaa_notifications_uid_t *search_context = context;
    if (uid->length != search_context->length) {
        return false;
    }

    if (!memcmp(uid->data, search_context->data, uid->length)) {
        return true;
    }
    return false;
}

static bool kaa_find_topic_state_by_id(void *topic_state, void *id)
{
    KAA_RETURN_IF_NIL2(topic_state, id, KAA_ERR_BADPARAM);
    kaa_topic_state_t *state = topic_state;
    return state->topic_id == *(uint64_t *)id;
}

kaa_error_t kaa_calculate_topic_listener_id(const kaa_topic_listener_t *listener, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL2(listener, listener_id, KAA_ERR_BADPARAM);

    const uint32_t prime = 31;

    *listener_id = 1;
    *listener_id = prime * (*listener_id) + (ptrdiff_t)listener->context;
    *listener_id = prime * (*listener_id) + (ptrdiff_t)listener->callback;
    return KAA_ERR_NONE;
}

kaa_error_t kaa_calculate_notification_listener_id(const kaa_notification_listener_t *listener, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL2(listener, listener_id, KAA_ERR_BADPARAM);

    const uint32_t prime = 31;

    *listener_id = 1;
    *listener_id = prime * (*listener_id) + (ptrdiff_t)listener->context;
    *listener_id = prime * (*listener_id) + (ptrdiff_t)listener->callback;
    return KAA_ERR_NONE;
}

static size_t get_subscriptions_size(kaa_list_t *subscriptions)
{
    if (kaa_list_get_size(subscriptions) > 0) {
        size_t expected_size = sizeof(uint32_t); //meta data for subscribe commands
        expected_size += kaa_list_get_size(subscriptions) * sizeof(uint64_t);
        return expected_size;
    }

    return 0;
}

kaa_error_t kaa_notification_manager_get_size(kaa_notification_manager_t *self, size_t *expected_size)
{
    KAA_RETURN_IF_NIL2(self, expected_size, KAA_ERR_BADPARAM);
    *expected_size = 0;
    *expected_size += sizeof(uint32_t); //state sqn size

    if (kaa_list_get_size(self->status->topic_states) > 0) {
        *expected_size += sizeof(uint32_t); //field id + reserved + count
        *expected_size += (sizeof(uint64_t) + sizeof(uint32_t)) * kaa_list_get_size(self->status->topic_states);
    }

    if (kaa_list_get_size(self->uids) > 0) {
        *expected_size += sizeof(uint32_t); //field id + reserved + count
        *expected_size += sizeof(uint32_t) * kaa_list_get_size(self->uids);

        kaa_list_node_t *uid_node = kaa_list_begin(self->uids);
        while (uid_node) {
            kaa_notifications_uid_t *uid = kaa_list_get_data(uid_node);
            *expected_size += kaa_aligned_size_get(uid->length);
            uid_node = kaa_list_next(uid_node);
        }
    }

    *expected_size += get_subscriptions_size(self->subscriptions);
    *expected_size += get_subscriptions_size(self->unsubscriptions);

    self->extension_payload_size = *expected_size;
    *expected_size += KAA_EXTENSION_HEADER_SIZE;
    return KAA_ERR_NONE;
}

static void serialize_topic_state(kaa_topic_state_t *state, kaa_notification_manager_t *self)
{
    KAA_RETURN_IF_NIL3(state, self, self->writer, );
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Serializing topic state: topic id '%llu', sqn '%lu'", state->topic_id, state->sqn_number);
    *(uint64_t *)self->writer->current = KAA_HTONLL(state->topic_id);
    self->writer->current += sizeof(uint64_t);
    *(uint32_t *)self->writer->current = KAA_HTONL((uint32_t)state->sqn_number);
    self->writer->current += sizeof(uint32_t);
}

static void serialize_notifications_uid(kaa_notifications_uid_t * uid, kaa_platform_message_writer_t *writer)
{
    KAA_RETURN_IF_NIL2(uid, writer, );
    *(uint32_t *)writer->current = KAA_HTONL(uid->length);
    writer->current += sizeof(uint32_t);
    kaa_platform_message_write_aligned(writer, uid->data, uid->length);
}

static void serialize_subscription(uint64_t *topic_id, kaa_notification_manager_t *self)
{
    KAA_RETURN_IF_NIL3(topic_id, self, self->writer, );
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Topic id '%llu'", *topic_id);
    *(uint64_t *)self->writer->current = KAA_HTONLL(*topic_id);
    self->writer->current += sizeof(uint64_t);
}

static void serialize_subscriptions(kaa_notification_manager_t *self, uint8_t subscription_type)
{
    KAA_RETURN_IF_NIL(self, );

    kaa_list_t *subscriptions = NULL;

    if (subscription_type == SUBSCRIPTION_ID) {
        KAA_RETURN_IF_NIL(kaa_list_get_size(self->subscriptions), );
        subscriptions = self->subscriptions;
    }
    if (subscription_type == UNSUBSCRIPTION_ID) {
        KAA_RETURN_IF_NIL(kaa_list_get_size(self->unsubscriptions), );
        subscriptions = self->unsubscriptions;
    }
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to serialize %s", subscription_type == SUBSCRIPTION_ID ? "subscriptions" : "unsubscriptions");

    *(uint8_t *)self->writer->current = subscription_type;
    self->writer->current += sizeof(uint16_t);
    *(uint16_t *)self->writer->current = KAA_HTONS((uint16_t)kaa_list_get_size(subscriptions));
    self->writer->current += sizeof(uint16_t);


    kaa_list_for_each(kaa_list_begin(subscriptions), kaa_list_back(subscriptions),
            (process_data)serialize_subscription, self);
}

kaa_error_t kaa_notification_manager_request_serialize(kaa_notification_manager_t *self, kaa_platform_message_writer_t *writer)
{
    KAA_RETURN_IF_NIL2(self, writer, KAA_ERR_BADPARAM);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to serialize client notification sync");

    self->writer = writer;

    kaa_error_t err = kaa_platform_message_write_extension_header(writer,
            KAA_EXTENSION_NOTIFICATION, KAA_CLIENT_WANTS_TO_RECEIVE_NOTIFICATIONS,
            self->extension_payload_size);
    if (err) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_BADPARAM, "Failed to write notification header");
        return KAA_ERR_BADPARAM;
    }

    *(int32_t *)writer->current = KAA_HTONL((int32_t)self->status->topic_list_hash);
    writer->current += sizeof(int32_t);

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to serialize %zu topic states", kaa_list_get_size(self->status->topic_states));
    if (kaa_list_get_size(self->status->topic_states) > 0) {
        *(uint8_t *)writer->current = (uint8_t)TOPICS_STATE_ID;
        writer->current += sizeof(uint16_t);
        *(uint16_t *)writer->current = KAA_HTONS((uint16_t)kaa_list_get_size(self->status->topic_states));
        writer->current += sizeof(uint16_t);

        kaa_list_for_each(kaa_list_begin(self->status->topic_states),
                kaa_list_back(self->status->topic_states),
                (process_data)serialize_topic_state, self);
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to serialize %zu uids", kaa_list_get_size(self->uids));
    if (kaa_list_get_size(self->uids) > 0) {
        *(uint8_t *)writer->current = (uint8_t)UID_ID;
        writer->current += sizeof(uint16_t);
        *(uint16_t *)writer->current = KAA_HTONS(kaa_list_get_size(self->uids));
        writer->current += sizeof(uint16_t);

        kaa_list_for_each(kaa_list_begin(self->uids), kaa_list_back(self->uids),
                (process_data)serialize_notifications_uid, writer);
    }

    serialize_subscriptions(self, SUBSCRIPTION_ID);
    serialize_subscriptions(self, UNSUBSCRIPTION_ID);

    return KAA_ERR_NONE;
}

static void destroy_notifications_uid(void *data)
{
    if (!data) {
        return;
    }

    KAA_RETURN_IF_NIL(data,);
    kaa_notifications_uid_t *uid = data;
    KAA_FREE(uid->data);
    KAA_FREE(uid);
}

static void destroy_topic(void *data)
{
    if (!data) {
        return;
    }

    kaa_topic_t *topic = data;
    KAA_FREE(topic->name);
    KAA_FREE(data);
}

static void destroy_optional_listeners_wrapper(void *data)
{
    KAA_RETURN_IF_NIL(data,);
    kaa_optional_notification_listeners_wrapper_t *wrapper = data;
    kaa_list_destroy(wrapper->listeners, NULL);
    KAA_FREE(wrapper);
}

/** @deprecated Use kaa_extension_notification_deinit(). */
void kaa_notification_manager_destroy(kaa_notification_manager_t *self)
{
    if (!self) {
        return;
    }

    kaa_list_destroy(self->mandatory_listeners, kaa_data_destroy);
    kaa_list_destroy(self->topics_listeners, kaa_data_destroy);
    kaa_list_destroy(self->subscriptions, kaa_data_destroy);
    kaa_list_destroy(self->unsubscriptions, kaa_data_destroy);
    kaa_list_destroy(self->optional_listeners, destroy_optional_listeners_wrapper);
    kaa_list_destroy(self->uids, destroy_notifications_uid);
    kaa_list_destroy(self->notifications, kaa_destroy_notification_node);

    KAA_FREE(self);
}

/** @deprecated Use kaa_extension_notification_init(). */
kaa_error_t kaa_notification_manager_create(kaa_notification_manager_t **self, kaa_status_t *status,
        kaa_channel_manager_t *channel_manager, kaa_logger_t *logger)
{
    if (!self || !status || !channel_manager || !logger) {
        return KAA_ERR_BADPARAM;
    }

    kaa_notification_manager_t *manager = KAA_MALLOC(sizeof(*manager));
    if (!manager) {
        return KAA_ERR_NOMEM;
    }

    manager->mandatory_listeners =  kaa_list_create();
    manager->topics_listeners    =  kaa_list_create();
    manager->optional_listeners  =  kaa_list_create();
    manager->subscriptions       =  kaa_list_create();
    manager->unsubscriptions     =  kaa_list_create();
    manager->notifications       =  kaa_list_create();
    manager->uids                =  kaa_list_create();

    if (!manager->mandatory_listeners || !manager->topics_listeners
            || !manager->optional_listeners ||!manager->subscriptions
            || !manager->unsubscriptions || !manager->uids)
    {
        kaa_notification_manager_destroy(manager);
        return KAA_ERR_NOMEM;
    }

    manager->extension_payload_size = 0;

    manager->writer              =  NULL;
    manager->status              =  status;
    manager->channel_manager     =  channel_manager;
    manager->logger              =  logger;

    *self = manager;

    return KAA_ERR_NONE;
}

kaa_error_t kaa_add_notification_listener(kaa_notification_manager_t *self, kaa_notification_listener_t *listener, uint32_t* listener_id)
{
    KAA_RETURN_IF_NIL2(self, listener, KAA_ERR_BADPARAM);

    if (!listener->callback) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to add mandatory notification listener: NULL callback");
        return KAA_ERR_BADPARAM;
    }

    uint32_t id;
    kaa_error_t err = kaa_calculate_notification_listener_id(listener, &id);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to calculate mandatory listener id");
        return err;
    }
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to add mandatory notification listener, id '%lu'", id);

    if (kaa_list_find_next(kaa_list_begin(self->mandatory_listeners), &kaa_find_notification_listener_by_id, &id)) {
        KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS, "Failed to add mandatory listener: listener is already subscribed");
        return KAA_ERR_ALREADY_EXISTS;
    }

    kaa_notification_listener_wrapper_t* wrapper = KAA_MALLOC(sizeof(*wrapper));
    KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);

    if (!kaa_list_push_front(self->mandatory_listeners, wrapper)) {
        KAA_FREE(wrapper);
        KAA_LOG_WARN(self->logger, KAA_ERR_NOMEM, "Failed to add mandatory listener");
        return KAA_ERR_NOMEM;
    }

    wrapper->listener = *listener;
    wrapper->id = id; // for user to have convenient way to address notification listener

    if (listener_id) {
        *listener_id = id;
    }
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Added mandatory notification listener id '%lu'", id);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_notification_listener(kaa_notification_manager_t *self, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL2(self, listener_id, KAA_ERR_BADPARAM);
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to remove mandatory notification listener, id '%lu'", *listener_id);

    kaa_error_t err =  kaa_list_remove_first(self->mandatory_listeners,
            kaa_find_notification_listener_by_id, listener_id, &kaa_data_destroy);
    if (err) {
        KAA_LOG_TRACE(self->logger, err,
                "Failed to remove mandatory notification listener: the listener with id '%lu' is not found", *listener_id);
    } else {
        KAA_LOG_TRACE(self->logger, err,
                "Removed mandatory notification listener, id '%lu'", *listener_id);
    }

    return err;
}

kaa_error_t kaa_add_optional_notification_listener(kaa_notification_manager_t *self,
        kaa_notification_listener_t *listener, uint64_t *topic_id, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL4(self, listener, listener->callback, topic_id, KAA_ERR_BADPARAM);

    if (!kaa_list_find_next(kaa_list_begin(self->status->topics), &kaa_find_topic_by_id, topic_id)) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND,
                "Failed to add optional notification listener: topic with id '%llu' not found",
                *topic_id);
        return KAA_ERR_NOT_FOUND;
    }

    uint32_t id;
    kaa_error_t err = kaa_calculate_notification_listener_id(listener, &id);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to calculate optional listener id");
        return err;
    }
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to add optional notification listener: id '%lu', topic id '%llu'", id, *topic_id);

    kaa_notification_listener_wrapper_t *wrapper = KAA_MALLOC(sizeof(*wrapper));
    KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);

    kaa_list_node_t *opt_listeners_node = kaa_list_find_next(
            kaa_list_begin(self->optional_listeners), kaa_find_optional_notification_listener_by_id,
            topic_id);
    if (!opt_listeners_node) {
        kaa_optional_notification_listeners_wrapper_t* optional_wrapper = KAA_MALLOC(sizeof(*optional_wrapper));
        if (!optional_wrapper) {
            KAA_FREE(wrapper);
            return KAA_ERR_NOMEM;
        }

        optional_wrapper->listeners = kaa_list_create();
        if (!kaa_list_push_front(optional_wrapper->listeners, wrapper)) {
            KAA_FREE(wrapper);
            destroy_optional_listeners_wrapper(optional_wrapper);
            return KAA_ERR_NOMEM;
        }

        if (!kaa_list_push_front(self->optional_listeners, optional_wrapper)) {
            KAA_FREE(wrapper);
            destroy_optional_listeners_wrapper(optional_wrapper);
            return KAA_ERR_NOMEM;
        }

        optional_wrapper->topic_id = *topic_id;
    } else {
        kaa_optional_notification_listeners_wrapper_t* optional_wrapper = kaa_list_get_data(opt_listeners_node);

        if (kaa_list_find_next(kaa_list_begin(optional_wrapper->listeners), &kaa_find_notification_listener_by_id, &id)) {
            KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS, "Failed to add the optional listener: the listener is already subscribed");
            KAA_FREE(wrapper);
            return KAA_ERR_ALREADY_EXISTS;
        }

        if (!kaa_list_push_front(optional_wrapper->listeners, wrapper)) {
            KAA_FREE(wrapper);
            return KAA_ERR_NOMEM;
        }
    }

    wrapper->listener = *listener;
    wrapper->id = id; // for user to have convenient way to address notification listener
    if (listener_id) {
        *listener_id = id;
    }

    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Added optional notification listener: id '%lu', topic id '%llu'", id, *topic_id);

    return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_optional_notification_listener(kaa_notification_manager_t *self, uint64_t *topic_id, uint32_t *listener_id)
{
    KAA_RETURN_IF_NIL3(self, topic_id, listener_id, KAA_ERR_BADPARAM);
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to remove optional notification listener: id '%u', topic id '%u'", *listener_id, *topic_id);

    kaa_list_node_t *opt_listeners_node = kaa_list_find_next(
            kaa_list_begin(self->optional_listeners),
            &kaa_find_optional_notification_listener_by_id, topic_id);
    if (!opt_listeners_node) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Failed to remove the optional listener: there is no listeners subscribed on this topic (topic id '%llu').", *topic_id);
        return KAA_ERR_NOT_FOUND;
    }

    kaa_optional_notification_listeners_wrapper_t *optional_wrapper = kaa_list_get_data(opt_listeners_node);

    kaa_error_t error = kaa_list_remove_first(optional_wrapper->listeners,
            &kaa_find_notification_listener_by_id, listener_id, &kaa_data_destroy);
    if (error) {
        KAA_LOG_WARN(self->logger, KAA_ERR_NOT_FOUND, "Failed to remove the optional listener: the listener with id '%lu' is not found", *listener_id);
        return error;
    } else {
        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Removed optional notification listener id: '%lu', topic id '%llu'", *listener_id, *topic_id);
        if (!kaa_list_get_size(optional_wrapper->listeners)) {
            kaa_list_remove_at(self->optional_listeners, opt_listeners_node, &destroy_optional_listeners_wrapper);
        }
        return KAA_ERR_NONE;
    }
}

kaa_error_t kaa_add_topic_list_listener(kaa_notification_manager_t *self, kaa_topic_listener_t *listener, uint32_t *topic_listener_id)
{
    KAA_RETURN_IF_NIL3(self, listener, listener->callback, KAA_ERR_BADPARAM);


    uint32_t id;
    kaa_error_t err = kaa_calculate_topic_listener_id(listener, &id);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to calculate mandatory listener's id");
        return err;
    }
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to add topic list listener, id '%lu'", id);

    kaa_topic_listener_wrapper_t *wrapper = (kaa_topic_listener_wrapper_t *) KAA_MALLOC(sizeof(kaa_topic_listener_wrapper_t));
    KAA_RETURN_IF_NIL(wrapper, KAA_ERR_NOMEM);

    if (kaa_list_find_next(kaa_list_begin(self->topics_listeners), &kaa_find_topic_listener_by_id, &id)) {
        KAA_LOG_WARN(self->logger, KAA_ERR_ALREADY_EXISTS, "Failed to add topic the listener: the listener is already subscribed");
        KAA_FREE(wrapper);
        return KAA_ERR_ALREADY_EXISTS;
    }

    if (!kaa_list_push_front(self->topics_listeners, wrapper)) {
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOMEM, "Failed to add the topic listener");
        KAA_FREE(wrapper);
        return KAA_ERR_NOMEM;
    }

    wrapper->listener = *listener;
    wrapper->id = id;// for user to have convenient way to address notification listener
    if (topic_listener_id) {
        *topic_listener_id = id;
    }
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Added topic list listener, id '%lu'", id);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_remove_topic_list_listener(kaa_notification_manager_t *self, uint32_t *topic_listener_id)
{
    KAA_RETURN_IF_NIL2(self, topic_listener_id, KAA_ERR_BADPARAM);
    KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Going to remove topic list listener, id '%lu'", *topic_listener_id);

    kaa_error_t err =  kaa_list_remove_first(self->topics_listeners,
            &kaa_find_topic_listener_by_id, topic_listener_id, &kaa_data_destroy);
    if (err) {
        KAA_LOG_WARN(self->logger, err,
                "Failed to remove topic list listener: listener id '%lu' not found",
                *topic_listener_id);
        return err;
    }
    KAA_LOG_TRACE(self->logger, err, "Removed topic list listener, id '%lu'", *topic_listener_id);
    return err;
}

kaa_error_t kaa_get_topics(kaa_notification_manager_t *self, kaa_list_t **topics)
{
    KAA_RETURN_IF_NIL2(self, topics, KAA_ERR_BADPARAM);
    *topics = self->status->topics;
    return KAA_ERR_NONE;
}

static void notify_topic_update_subscriber(kaa_topic_listener_wrapper_t *wrapper, kaa_list_t *topics)
{
    KAA_RETURN_IF_NIL2(wrapper, topics, );
    wrapper->listener.callback(wrapper->listener.context, topics);
}

static kaa_error_t kaa_notify_topic_update_subscribers(kaa_notification_manager_t *self, kaa_list_t *topics)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    kaa_list_for_each(kaa_list_begin(self->topics_listeners), kaa_list_back(self->topics_listeners),
            (process_data)notify_topic_update_subscriber, topics);
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_notify_mandatory_notification_subscribers(kaa_notification_manager_t *self,
        uint64_t topic_id, kaa_notification_t *notification)
{
    KAA_RETURN_IF_NIL2(self, notification, KAA_ERR_BADPARAM);
    kaa_list_node_t *current_listener_node = kaa_list_begin(self->mandatory_listeners);
    while (current_listener_node) {
        kaa_notification_listener_wrapper_t *wrapper = kaa_list_get_data(current_listener_node);
        wrapper->listener.callback(wrapper->listener.context, &topic_id, notification);
        current_listener_node = kaa_list_next(current_listener_node);
    }
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_notify_optional_notification_subscribers(kaa_notification_manager_t *self,
        uint64_t topic_id, kaa_notification_t *notification)
{
    KAA_RETURN_IF_NIL3(self, topic_id, notification, KAA_ERR_BADPARAM);
    kaa_list_node_t *opt_listeners_node = kaa_list_find_next(
            kaa_list_begin(self->optional_listeners),
            kaa_find_optional_notification_listener_by_id, &topic_id);
    KAA_RETURN_IF_NIL(opt_listeners_node, KAA_ERR_NOT_FOUND);

    kaa_optional_notification_listeners_wrapper_t* optional_wrapper =
        kaa_list_get_data(opt_listeners_node);

    kaa_list_node_t *optional_listener_node = kaa_list_begin(optional_wrapper->listeners);
    while (optional_listener_node) {
        kaa_notification_listener_wrapper_t *wrapper = kaa_list_get_data(optional_listener_node);
        wrapper->listener.callback(wrapper->listener.context, &topic_id, notification);
        optional_listener_node = kaa_list_next(optional_listener_node);
    }
    return KAA_ERR_NONE;
}

static kaa_error_t kaa_add_subscribtion_or_unsubscribtion(kaa_list_t *target, uint64_t *topic_id)
{
    KAA_RETURN_IF_NIL2(target, topic_id, KAA_ERR_BADPARAM);
    uint64_t *subs_topic_id = (uint64_t *) KAA_MALLOC(sizeof(uint64_t));
    KAA_RETURN_IF_NIL(subs_topic_id, KAA_ERR_NOMEM);
    *subs_topic_id = *topic_id;
    if (!kaa_list_push_front(target, subs_topic_id)) {
        KAA_FREE(subs_topic_id);
        return KAA_ERR_NOMEM;
    }

    return KAA_ERR_NONE;
}

static kaa_error_t kaa_add_subscribtions_or_unsubscribtions(kaa_notification_manager_t *self, uint64_t *topic_ids, size_t size, uint8_t subscription_id)
{
    KAA_RETURN_IF_NIL2(self, topic_ids, KAA_ERR_BADPARAM);

    kaa_list_t *new_subscriptions = kaa_list_create();
    KAA_RETURN_IF_NIL(new_subscriptions, KAA_ERR_NOMEM);
    while (size--) {
        uint64_t *subs_topic_id = KAA_MALLOC(sizeof(*subs_topic_id));
        KAA_RETURN_IF_NIL(subs_topic_id, KAA_ERR_NOMEM);
        *subs_topic_id = topic_ids[size];
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to add %s to topic with id '%llu'",
                subscription_id == SUBSCRIPTION_ID ? "subscription" : "unsubscription",
                *subs_topic_id);
        if (!kaa_list_push_front(new_subscriptions, subs_topic_id)) {
            KAA_FREE(subs_topic_id);
            kaa_list_destroy(new_subscriptions, &kaa_data_destroy);
            return KAA_ERR_NOMEM;
        }
    }
    kaa_list_t *subscriptions = subscription_id == SUBSCRIPTION_ID ? self->subscriptions : self->unsubscriptions;
    kaa_lists_merge(subscriptions, new_subscriptions);
    kaa_list_destroy(new_subscriptions, NULL);
    return KAA_ERR_NONE;
}

kaa_error_t kaa_subscribe_to_topic(kaa_notification_manager_t *self, uint64_t *topic_id, bool force_sync)
{
    KAA_RETURN_IF_NIL2(self, topic_id, KAA_ERR_BADPARAM);
    kaa_topic_t* topic = NULL;
    kaa_error_t err = kaa_find_topic(self, &topic, topic_id);
    if (err) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topic with id '%llu'.", *topic_id);
        return err;
    }
    if (topic->subscription_type == MANDATORY_SUBSCRIPTION) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topic with id '%llu'. Topic isn't optional.", *topic_id);
        return KAA_ERR_BADPARAM;
    }
    err = kaa_add_subscribtion_or_unsubscribtion(self->subscriptions, topic_id);
    KAA_RETURN_IF_ERR(err);

    if (force_sync) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to subscribe to topic '%llu'", *topic_id);
        kaa_sync_topic_subscriptions(self);
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Subscription to topic '%llu' is postponed till sync", *topic_id);
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_subscribe_to_topics(kaa_notification_manager_t *self, uint64_t *topic_ids, size_t size, bool force_sync)
{
    KAA_RETURN_IF_NIL2(self, topic_ids, KAA_ERR_BADPARAM);
    kaa_topic_t *topic = NULL;
    kaa_error_t err = KAA_ERR_NONE;
    size_t size_copy = size;
    while (size_copy--) {
        err = kaa_find_topic(self, &topic, topic_ids + size_copy);
        if (err) {
            KAA_LOG_WARN(self->logger, err, "Failed to subscribe to the topic: topic not found, id '%llu'", topic_ids[size_copy]);
            return err;
        } else {
            if (topic->subscription_type == MANDATORY_SUBSCRIPTION) {
                KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to subscribe to the topics. Topic with id '%llu'. Topic isn't optional.", topic_ids[size_copy]);
                return KAA_ERR_BADPARAM;
            }
        }
    }
    if (force_sync) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to subscribe to the topics");
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Subscription to topics is postponed till sync");
    }
    err = kaa_add_subscribtions_or_unsubscribtions(self, topic_ids, size, SUBSCRIPTION_ID);
    KAA_RETURN_IF_ERR(err);

    if (force_sync) {
        kaa_sync_topic_subscriptions(self);
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_unsubscribe_from_topic(kaa_notification_manager_t *self, uint64_t *topic_id, bool force_sync)
{
    KAA_RETURN_IF_NIL2(self, topic_id, KAA_ERR_BADPARAM);
    kaa_topic_t* topic = NULL;
    kaa_error_t err = kaa_find_topic(self, &topic, topic_id);
    if (err) {
        KAA_LOG_WARN(self->logger, err, "Failed to unsubscribe from the topic with id '%llu'.", *topic_id);
        return err;
    }
    if (topic->subscription_type == MANDATORY_SUBSCRIPTION) {
        KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topic with id '%llu'. Topic isn't optional.", *topic_id);
        return KAA_ERR_BADPARAM;
    }
    err = kaa_add_subscribtion_or_unsubscribtion(self->unsubscriptions, topic_id);
    KAA_RETURN_IF_ERR(err);

    if (force_sync) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to unsubscribe from the topic '%llu'", *topic_id);
        kaa_sync_topic_subscriptions(self);
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Unsubscription from the topic '%llu' is postponed till sync", *topic_id);
    }
    return KAA_ERR_NONE;
}

kaa_error_t kaa_unsubscribe_from_topics(kaa_notification_manager_t *self, uint64_t *topic_ids, size_t size, bool force_sync)
{
    KAA_RETURN_IF_NIL2(self, topic_ids, KAA_ERR_BADPARAM);
    kaa_topic_t *topic = NULL;
    kaa_error_t err = KAA_ERR_NONE;
    size_t size_copy = size;

    while (size_copy--) {
        err = kaa_find_topic(self, &topic, topic_ids + size_copy);
        if (err) {
            KAA_LOG_WARN(self->logger, err, "Failed to unsubscribe from the topic: topic not found, id '%llu'", topic_ids[size_copy]);
            return err;
        } else {
            if (topic->subscription_type == MANDATORY_SUBSCRIPTION) {
                KAA_LOG_WARN(self->logger, KAA_ERR_BADPARAM, "Failed to unsubscribe from the topics. Topic with id '%llu'. Topic isn't optional.", topic_ids[size_copy]);
                return KAA_ERR_BADPARAM;
            }
        }
    }

    if (force_sync) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Going to unsubscribe from the topics");
    } else {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Unsubscription from the topics is postponed till sync");
    }

    err = kaa_add_subscribtions_or_unsubscribtions(self, topic_ids, size, UNSUBSCRIPTION_ID);
    KAA_RETURN_IF_ERR(err);

    if (force_sync) {
        kaa_sync_topic_subscriptions(self);
    }
    return KAA_ERR_NONE;
}

static kaa_error_t do_sync(kaa_notification_manager_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    kaa_transport_channel_interface_t *channel =
        kaa_channel_manager_get_transport_channel(self->channel_manager,
                notification_sync_services[0]);
    if (channel) {
        channel->sync_handler(channel->context, notification_sync_services, 1);
    } else {
        KAA_LOG_ERROR(self->logger, KAA_ERR_NOT_FOUND, "Failed to sync: transport channel not found");
    }

    return KAA_ERR_NONE;
}

kaa_error_t kaa_sync_topic_subscriptions(kaa_notification_manager_t *self)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    return do_sync(self);
}

static kaa_error_t kaa_topic_list_update(kaa_notification_manager_t *self, kaa_list_t *new_topics)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);
    kaa_topic_t *topic = NULL;
    kaa_list_node_t *topic_node = kaa_list_begin(new_topics);

    // "Substracts" self topics from new topics based on IDs
    while (topic_node && kaa_list_get_size(self->status->topics) > 0) {
        topic = kaa_list_get_data(topic_node);
        KAA_RETURN_IF_NIL(topic, KAA_ERR_NOMEM);
        kaa_list_remove_first(self->status->topics, kaa_find_topic_by_id, &topic->id, destroy_topic);
        topic_node = kaa_list_next(topic_node);
    }

    if (kaa_list_get_size(self->status->topics)) {
        KAA_LOG_INFO(self->logger, KAA_ERR_NONE,
                "Going to remove optional listener(s) from obsolete %zu topics",
                kaa_list_get_size(self->status->topics));

        kaa_list_node_t *outdated_topics = kaa_list_begin(self->status->topics);
        while (outdated_topics) {
            topic = kaa_list_get_data(outdated_topics);
            kaa_list_remove_first(self->optional_listeners,
                    kaa_find_optional_notification_listener_by_id, &topic->id,
                    destroy_optional_listeners_wrapper);
            outdated_topics = kaa_list_next(outdated_topics);
        }
    }

    kaa_list_destroy(self->status->topics, &destroy_topic);
    kaa_list_sort(new_topics, &sort_topic_by_id);
    self->status->topic_list_hash = kaa_list_hash(new_topics, &get_topic_id);
    self->status->topics = new_topics;
    return kaa_notify_topic_update_subscribers(self, new_topics);
}

static kaa_error_t kaa_notification_received(kaa_notification_manager_t *self, kaa_notification_t *notification, uint64_t topic_id)
{
    KAA_RETURN_IF_NIL2(self, notification, KAA_ERR_BADDATA);
    if (kaa_notify_optional_notification_subscribers(self, topic_id, notification)) {
        kaa_notify_mandatory_notification_subscribers(self, topic_id, notification);
    }
    return KAA_ERR_NONE;
}

static kaa_error_t update_sequence_number(kaa_notification_manager_t *self, uint64_t topic_id, uint32_t sqn_number)
{
    KAA_RETURN_IF_NIL(self, KAA_ERR_BADPARAM);

    kaa_topic_state_t *state;
    kaa_list_node_t *it = kaa_list_find_next(kaa_list_begin(self->status->topic_states),
            kaa_find_topic_state_by_id, &topic_id);
    if (!it) {
        state = KAA_MALLOC(sizeof(*state));
        KAA_RETURN_IF_NIL(state, KAA_ERR_NOMEM);

        if (!kaa_list_push_front(self->status->topic_states, state)) {
            KAA_FREE(state);
            return KAA_ERR_NOMEM;
        }

        state->topic_id = topic_id;
        state->sqn_number = sqn_number;
        self->status->has_update = true;
    } else {
        state = kaa_list_get_data(it);
        if (sqn_number > state->sqn_number) {
            state->sqn_number = sqn_number;
            self->status->has_update = true;
        }
    }
    return KAA_ERR_NONE;
}

static void kaa_notify_notification_listeners(void *data, void* context)
{
    KAA_RETURN_IF_NIL2(data, context, );
    kaa_error_t err = KAA_ERR_NONE;
    kaa_topic_notifications_node_t *node = data;
    kaa_list_node_t *notification_list_node = kaa_list_begin(node->notifications);
    kaa_notification_manager_t *self = context;

    while(notification_list_node) {
        kaa_notification_wrapper_t *wrapper = kaa_list_get_data(notification_list_node);
        kaa_list_node_t *it = kaa_list_find_next(kaa_list_begin(self->status->topic_states),
                kaa_find_topic_state_by_id, &node->topic_id);
        if (it) {
            kaa_topic_state_t *state = kaa_list_get_data(it);
            if (wrapper->sqn > state->sqn_number) {
                err = kaa_notification_received(self, wrapper->notification, node->topic_id);
            }
        } else {
            err = kaa_notification_received(self, wrapper->notification, node->topic_id);
            if (err) {
                KAA_LOG_WARN(self->logger, err, "Failed to notify notification listener");
                return;
            }
        }

        err = update_sequence_number(self, node->topic_id, wrapper->sqn);
        if (err) {
            KAA_LOG_WARN(self->logger, err, "Failed to update notification sequence number for topic '%llu'", node->topic_id);
        }

        notification_list_node = kaa_list_next(notification_list_node);
    }
}

kaa_error_t kaa_notification_manager_handle_server_sync(kaa_notification_manager_t *self,
        kaa_platform_message_reader_t *reader, uint32_t extension_length)
{
    KAA_RETURN_IF_NIL2(self, reader, KAA_ERR_BADPARAM);

    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received notification server sync: options 0, payload size %lu", extension_length);

    kaa_list_clear(self->subscriptions, &kaa_data_destroy);
    kaa_list_clear(self->unsubscriptions, &kaa_data_destroy);

    if (extension_length > 0) {
        kaa_error_t err = KAA_ERR_NONE;
        if (KAA_NTOHL(*((uint32_t *) reader->current)) == KAA_NO_DELTA) {
            KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Received delta response: NO DELTA. Going to clear uids list...");
            kaa_list_clear(self->uids, destroy_notifications_uid);
        }
        shift_and_sub_extension(reader, &extension_length, sizeof(uint32_t));
        while (extension_length > 0) {
            uint8_t field_id = *(uint8_t *)reader->current; //field id
            shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t)); // + reserved

            switch (field_id) {
                case NOTIFICATIONS: {
                    kaa_notification_t *notification = NULL;
                    uint32_t seq_number = 0;
                    uint16_t notifications_count = KAA_NTOHS(*((uint16_t *) reader->current)); // notifications count
                    shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t));

                    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received notifications. Notifications count is %u", notifications_count);

                    while (notifications_count--) {
                        seq_number = KAA_NTOHL(*((uint32_t *) reader->current)); // sqn of the last received notification
                        shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t) + sizeof(uint32_t)); // + notification type
                        uint16_t uid_length = KAA_NTOHS(*((uint16_t *) reader->current)); // uid length
                        shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t));
                        uint32_t notification_size = KAA_NTOHL(*((uint32_t *) reader->current)); // notification body size
                        shift_and_sub_extension(reader, &extension_length, sizeof(uint32_t));
                        uint64_t topic_id = KAA_NTOHLL(*((uint64_t *) reader->current)); // topic id
                        shift_and_sub_extension(reader, &extension_length, sizeof(uint64_t));

                        kaa_topic_t *topic_found;
                        err = kaa_find_topic(self, &topic_found, &topic_id);
                        if (err) {
                            KAA_LOG_WARN(self->logger, err, "Topic with id %llu is not found. Skipping notification...", topic_id);
                            size_t skiped_size = kaa_aligned_size_get(uid_length) + kaa_aligned_size_get(notification_size);
                            shift_and_sub_extension(reader, &extension_length, skiped_size);
                            continue;
                        }
                        if (uid_length) {
                            kaa_notifications_uid_t *uid = KAA_MALLOC(sizeof(*uid));
                            KAA_RETURN_IF_NIL(uid, KAA_ERR_NOMEM);

                            uid->length = uid_length;
                            uid->data = KAA_MALLOC(uid_length);
                            if (!uid->data) {
                                destroy_notifications_uid(uid);
                                return KAA_ERR_NOMEM;
                            }
                            err = kaa_platform_message_read_aligned(reader, uid->data, uid->length);
                            if (err) {
                                KAA_LOG_WARN(self->logger, err, "Failed to read UID body, topic id %llu", topic_id);
                                return err;
                            }
                            extension_length -= kaa_aligned_size_get(uid->length);

                            kaa_list_node_t *has_been_already_received = kaa_list_find_next(kaa_list_begin(self->uids), &kaa_find_uid, uid);
                            if (has_been_already_received) {
                                KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "This unicast notification has been already received");
                                shift_and_sub_extension(reader, &extension_length, kaa_aligned_size_get(notification_size));
                                destroy_notifications_uid(uid);
                                continue;
                            }

                            if (!kaa_list_push_front(self->uids, uid)) {
                                destroy_notifications_uid(uid);
                                return KAA_ERR_NOMEM;
                            }
                        }
                        if (notification_size) {
                            avro_reader_t avro_reader = avro_reader_memory((const char *)reader->current, notification_size);
                            if (!avro_reader) {
                                return KAA_ERR_NOMEM;
                            }
                            notification = KAA_NOTIFICATION_DESERIALIZE(avro_reader);
                            avro_reader_free(avro_reader);
                            if (!notification) {
                                KAA_LOG_WARN(self->logger, KAA_ERR_NOMEM, "Failed to deserialize notification");
                                return KAA_ERR_NOMEM;
                            }
                        }
                        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Notification's sqn '%lu', topic id '%llu', type '%s', size '%lu'"
                                , seq_number, topic_id, uid_length ? "unicast" : "multicast", notification_size);
                        shift_and_sub_extension(reader, &extension_length, kaa_aligned_size_get(notification_size));
                        if (uid_length == 0) {
                            err = kaa_add_notification_to_map(self->notifications, notification, topic_id, &seq_number);
                        } else {
                            err = kaa_notification_received(self, notification, topic_id);
                            notification->destroy(notification);
                        }
                        if (err) {
                            KAA_LOG_WARN(self->logger, err, "Failed to add notification to map");
                            return KAA_ERR_NOMEM;
                        }
                    }
                    break;
                }

                case TOPICS: {
                    kaa_list_t *new_topics = kaa_list_create();
                    KAA_RETURN_IF_NIL(new_topics, KAA_ERR_NOMEM);

                    uint16_t topic_count = KAA_NTOHS(*((uint16_t *) reader->current)); // topics count
                    KAA_LOG_INFO(self->logger, KAA_ERR_NONE, "Received topics list. Topics count is %u", topic_count);
                    shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t));

                    while (topic_count--) {
                        kaa_topic_t *topic = (kaa_topic_t *) KAA_MALLOC(sizeof(kaa_topic_t));
                        KAA_RETURN_IF_NIL(topic, KAA_ERR_NOMEM);
                        topic->id = KAA_NTOHLL(*((uint64_t *) reader->current)); // topic id
                        shift_and_sub_extension(reader, &extension_length, sizeof(uint64_t));
                        topic->subscription_type = (*((uint8_t *) reader->current) == MANDATORY_SUBSCRIPTION) ? MANDATORY_SUBSCRIPTION : OPTIONAL_SUBSCRIPTION;
                        shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t)); // + reserved
                        topic->name_length = KAA_NTOHS(*((uint16_t *) reader->current)); // name length
                        shift_and_sub_extension(reader, &extension_length, sizeof(uint16_t));

                        topic->name = (char *) KAA_MALLOC(topic->name_length + 1); // Topic name
                        if (!topic->name) {
                            destroy_topic(topic);
                            return KAA_ERR_NOMEM;
                        }

                        err = kaa_platform_message_read_aligned(reader, topic->name, topic->name_length);
                        if (err) {
                            KAA_LOG_WARN(self->logger, KAA_ERR_BADDATA, "Failed to read topic's name");
                            destroy_topic(topic);
                            return err;
                        }

                        topic->name[topic->name_length] = '\0';
                        extension_length -= kaa_aligned_size_get(topic->name_length);
                        KAA_LOG_TRACE(self->logger, KAA_ERR_NONE, "Topic Id '%llu', subscription type '%s', name '%s'"
                                , topic->id, (topic->subscription_type == MANDATORY_SUBSCRIPTION) ? "mandatory": "optional", topic->name);

                        if (!kaa_list_push_front(new_topics, topic)) {
                            destroy_topic(topic);
                            return KAA_ERR_NOMEM;
                        }
                    }

                    err = kaa_topic_list_update(self, new_topics);
                    if (err) {
                        KAA_LOG_WARN(self->logger, err, "Failed to notify topic list listeners");
                    }
                    break;
                }

                default:
                    KAA_LOG_ERROR(self->logger, KAA_ERR_NONE, "Bad field ID type");
                    return KAA_ERR_BADDATA;
            }
        }
    }

    if (kaa_list_get_size(self->notifications)) {
        kaa_sort_notifications(self->notifications);
        kaa_list_for_each(kaa_list_begin(self->notifications), kaa_list_back(self->notifications), kaa_notify_notification_listeners, self);
        kaa_list_clear(self->notifications, &kaa_destroy_notification_node);
        if (!self->notifications) {
            return KAA_ERR_NOMEM;
        }
    }

    return do_sync(self);
}

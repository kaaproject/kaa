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

#include <string.h>
#include <stdio.h>

#include "kaa_test.h"

#include "kaa_notification_manager.h"
#include "kaa.h"
#include <platform/sock.h>
#include "kaa_error.h"
#include "kaa_common.h"
#include "kaa_context.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "avro_src/avro/io.h"
#include "platform/sock.h"
#include "kaa_platform_common.h"
#include "kaa_platform_utils.h"
#include "kaa_platform_protocol.h"

//----------------------------------------------------------------------------------------------
kaa_context_t *context = NULL;

kaa_notification_listener_t listener;
kaa_notification_listener_t listener_2;
kaa_logger_t* logger;

uint8_t *buffer_pointer = NULL;
kaa_list_t *topics = NULL;
uint64_t topic_id;

uint32_t id,id2;
kaa_error_t err = 0;
size_t size = 0;

uint8_t *pointer_to_sqn = NULL;

bool listener_has_been_notified = false;
kaa_topic_listener_t topic_listener;
kaa_topic_listener_t topic_listener_2;
//-----------------------------------------------------------------------------------------------
void on_notification(void* contextmock, uint64_t *topic_id, kaa_notification_t *notif)
{
    (void)contextmock;
    (void)topic_id;
    (void)notif;
    printf("\nNotification listener got his Notification\n\n");
    listener_has_been_notified = true;
}

void on_topic(void* contextmock, kaa_list_t *topics)
{
    (void)contextmock;
    (void)topics;
    printf("\nTopic list listener got his topic list.\n\n");
}

uint8_t *buffer = NULL;
size_t buffer_size = 0;

/* We should remove status file after running the test, because
 * client persists notification sequence number and if the test is run
 * two times in a row, notification manager will throw out the notification
 * which is serialized in test, because it considers that the notification
 * has already been received -> client won't received the notification and
 * the test will be failed.
 */
#define KAA_STATUS_FILE_NAME      "./kaa_status.bin"
static void remove_state_file(const char* filename)
{
    int res = remove(filename);
    if (!res) {
        printf("Status file has been successfully removed\n");
    } else {
        printf("Error: Can't remove status file!\n");
    }
}


int test_init(void)
{
    err = KAA_ERR_NONE;

    err = kaa_init(&context);
    if (err) {
        return err;
    }
    listener.callback = &on_notification;
    listener.context = NULL;
    listener_2.callback = &on_notification;
    listener_2.context = &listener;

    topic_listener.callback = &on_topic;
    topic_listener.context = NULL;
    topic_listener_2.callback = &on_topic;
    topic_listener_2.context = &topic_listener;

    topic_id = 22;
    return 0;
}

int test_deinit(void)
{
    if (context) {
        kaa_deinit(context);
    }
    if (buffer_pointer) {
        KAA_FREE(buffer_pointer);
    }

    remove_state_file(KAA_STATUS_FILE_NAME);
    return 0;
}

void test_deserializing(void **state)
{
    (void)state;

    kaa_notification_t *notification = kaa_notification_notification_create();
    const char *message = "Hello World!!!\n";
    notification->message = kaa_string_copy_create(message);
    size                  =               (sizeof(uint32_t) + sizeof(uint16_t) + sizeof(uint16_t) /*that was header*/+ sizeof(uint16_t) + sizeof(uint16_t) + sizeof(uint32_t) /*extension options
                                          and payload length*/  + sizeof(uint32_t) + sizeof(uint32_t) /*state sqn and delta status*/ + sizeof(uint16_t) + sizeof(uint16_t) /*field id and topic count*/ + sizeof(uint64_t) /*topic ID*/
                                          + sizeof(uint16_t) + sizeof(uint16_t) /*subscriptions type + topic name length*/ + sizeof(uint32_t) /*topic name + padding */ + sizeof(uint16_t) + sizeof(uint16_t) /*field id and notifications count*/
                                          + sizeof(uint32_t) /* Notification sqn */ + sizeof(uint16_t) + sizeof(uint16_t) /* Notification type + uid length */+ sizeof (uint32_t) /* notification body size*/ + sizeof (uint64_t) /*Topic Id*/
                                          + /*Not unicast notifications*/ + kaa_aligned_size_get(notification->get_size(notification)));

    uint8_t *unserialized_buffer = KAA_MALLOC(size);
    ASSERT_NOT_NULL(unserialized_buffer);
    buffer_pointer = unserialized_buffer;
    memset(unserialized_buffer, 0, size);
    *(uint32_t *)unserialized_buffer = KAA_HTONL((uint32_t) KAA_PLATFORM_PROTOCOL_ID); //KAA_HTONL(KAA_PLATFORM_PROTOCOL_ID);
    unserialized_buffer += sizeof(uint32_t);
    *(uint16_t *)unserialized_buffer = KAA_HTONS((uint16_t)1);
    unserialized_buffer += sizeof(uint16_t);
    *(uint16_t *)unserialized_buffer = KAA_HTONS((uint16_t)1); // extension count
    unserialized_buffer += sizeof(uint16_t);

    *(uint16_t *)unserialized_buffer = KAA_HTONS((uint16_t)KAA_EXTENSION_NOTIFICATION);
    unserialized_buffer += sizeof(uint16_t);
    unserialized_buffer += sizeof(uint16_t); // pass by extension options

    uint32_t payload_info = sizeof(uint32_t) /*delta status*/ + sizeof(uint16_t) + sizeof(uint16_t) /*field id and topic count*/ + sizeof(uint64_t) /*topic ID*/
                                                  + sizeof(uint16_t) + sizeof(uint16_t) /*subscriptions type + topic name length*/ + sizeof(uint32_t) /*topic name + padding */ + sizeof(uint16_t) + sizeof(uint16_t) /*field id and notifications count*/
                                                  + sizeof(uint32_t) /* Notification sqn */ + sizeof(uint16_t) + sizeof(uint16_t) /* Notification type + uid length */+ sizeof (uint32_t) /* notification body size*/ + sizeof (uint64_t) /*Topic Id*/
                                                  +   kaa_aligned_size_get(notification->get_size(notification));

    *(uint32_t *)unserialized_buffer = KAA_HTONL((uint32_t) payload_info);
    unserialized_buffer += sizeof(uint32_t);
    *(uint32_t *)unserialized_buffer = KAA_HTONL((uint32_t)2); // Delta status
    unserialized_buffer += sizeof(uint32_t);
    //TOPICS
    *(uint8_t *)unserialized_buffer = (uint8_t)0;
    unserialized_buffer += sizeof(uint16_t);
    *(uint16_t *)unserialized_buffer = KAA_HTONS ((uint16_t)1); // topics count
    unserialized_buffer += sizeof(uint16_t);
    *(uint64_t *)unserialized_buffer = KAA_HTONLL((uint64_t)22);    //topic id
    unserialized_buffer += sizeof(uint64_t);
    *(uint8_t *)unserialized_buffer = (uint8_t)OPTIONAL_SUBSCRIPTION;
    unserialized_buffer += sizeof(uint16_t);
    *(uint16_t *)unserialized_buffer = KAA_HTONS((uint16_t)4); //KAA
    unserialized_buffer += sizeof(uint16_t);
    *unserialized_buffer++ = 'K';*unserialized_buffer++ = 'A'; // topic name + padding
    *unserialized_buffer++ = 'A';*unserialized_buffer++ = 'A';
    unserialized_buffer += (4 - kaa_aligned_size_get(4));
    //-----------------------------------------------------------------------
    *(uint8_t *)unserialized_buffer = (uint8_t)1; //Notification field ID
    unserialized_buffer += sizeof(uint16_t);
    *(uint16_t *)unserialized_buffer = KAA_HTONS ((uint16_t)1); // notitifications count
    unserialized_buffer += sizeof(uint16_t);
    *(uint32_t *)unserialized_buffer = KAA_HTONL((uint32_t)99); //sqn
    pointer_to_sqn = unserialized_buffer; // To have the possibility to change sqn.
    unserialized_buffer += sizeof(uint32_t);
    *(uint8_t *)unserialized_buffer = (uint8_t)0x1; //notification type
    unserialized_buffer += sizeof(uint16_t);
    *(uint16_t *)unserialized_buffer = KAA_HTONS ((uint16_t) 0); //uid length
    unserialized_buffer += sizeof(uint16_t);
    *(uint32_t *)unserialized_buffer = KAA_HTONL ((uint32_t)notification->get_size(notification));
    unserialized_buffer += sizeof(uint32_t);
    *(uint64_t *)unserialized_buffer = KAA_HTONLL((uint64_t)22);
    unserialized_buffer += sizeof(uint64_t);

    avro_writer_t avro_writer = avro_writer_memory((const char *)unserialized_buffer, notification->get_size(notification));
    notification->serialize(avro_writer, notification);
    err = kaa_platform_protocol_process_server_sync(context->platform_protocol, buffer_pointer, size);
    avro_writer_free(avro_writer);

    ASSERT_EQUAL(err, KAA_ERR_NONE);

    notification->destroy(notification);
}

void test_notification_listeners_adding_and_removing(void **state)
{
    (void)state;

    err = kaa_add_notification_listener(context->notification_manager, &listener, &id);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_add_notification_listener(context->notification_manager, &listener, &id);
    ASSERT_NOT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_add_notification_listener(context->notification_manager, &listener_2, &id2);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_remove_notification_listener(context->notification_manager, &id);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_remove_notification_listener(context->notification_manager, &id);
    ASSERT_NOT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_remove_notification_listener(context->notification_manager, &id2);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_add_optional_notification_listener(context->notification_manager, &listener, &topic_id, &id);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_add_optional_notification_listener(context->notification_manager, &listener, &topic_id, &id);
    ASSERT_NOT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_add_optional_notification_listener(context->notification_manager, &listener_2, &topic_id, &id2);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    *(uint32_t *)pointer_to_sqn = KAA_HTONL((uint32_t) 100); // Need to change sqn
    err = kaa_platform_protocol_process_server_sync(context->platform_protocol, buffer_pointer, size);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    ASSERT_EQUAL(listener_has_been_notified, true); // whether callback has been called

    err = kaa_remove_optional_notification_listener(context->notification_manager, &topic_id, &id);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_remove_optional_notification_listener(context->notification_manager, &topic_id, &id);
    ASSERT_NOT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_remove_optional_notification_listener(context->notification_manager, &topic_id, &id2);
    ASSERT_EQUAL(err, KAA_ERR_NONE);
}

void test_topic_list_listeners_adding_and_removing(void **state)
{
    (void)state;

    err = kaa_get_topics(context->notification_manager, &topics);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_add_topic_list_listener(context->notification_manager, &topic_listener, &id);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_add_topic_list_listener(context->notification_manager, &topic_listener, &id);
    ASSERT_NOT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_add_topic_list_listener(context->notification_manager, &topic_listener_2, &id2);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_remove_topic_list_listener(context->notification_manager, &id);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_remove_topic_list_listener(context->notification_manager, &id);
    ASSERT_NOT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_remove_topic_list_listener(context->notification_manager, &id2);
    ASSERT_EQUAL(err, KAA_ERR_NONE);
}

void test_retrieving_topic_list(void **state)
{
    (void)state;

    err = kaa_get_topics(context->notification_manager, &topics);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    size_t topics_size = kaa_list_get_size(topics);
    ASSERT_EQUAL(topics_size, 1);
}

void test_serializing(void **state)
{
    (void)state;

    kaa_extension_id service[] = { KAA_EXTENSION_NOTIFICATION };

    err = kaa_platform_protocol_alloc_serialize_client_sync(context->platform_protocol, service, 1,
            &buffer, &buffer_size);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    KAA_FREE(buffer);
}

void test_subscriptions(void **state)
{
    (void)state;

    err = kaa_subscribe_to_topic(context->notification_manager, &topic_id, false);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_unsubscribe_from_topic(context->notification_manager, &topic_id, false);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    uint64_t fake_ids[2] = { 22, 11 };
    err = kaa_subscribe_to_topics(context->notification_manager, fake_ids, 2, false);
    ASSERT_NOT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_unsubscribe_from_topics(context->notification_manager, fake_ids, 2, false);
    ASSERT_NOT_EQUAL(err, KAA_ERR_NONE);

    uint64_t existing_ids[1] = { 22 };
    err = kaa_subscribe_to_topics(context->notification_manager, existing_ids, 1, false);
    ASSERT_EQUAL(err, KAA_ERR_NONE);

    err = kaa_unsubscribe_from_topics(context->notification_manager, existing_ids, 1, false);
    ASSERT_EQUAL(err, KAA_ERR_NONE);
}

KAA_SUITE_MAIN(Notification, test_init, test_deinit,
       KAA_TEST_CASE(deserializing, test_deserializing)
       KAA_TEST_CASE(removing_and_adding_notifications_listeners, test_notification_listeners_adding_and_removing)
       KAA_TEST_CASE(removing_and_adding_topic_list_listeners, test_topic_list_listeners_adding_and_removing)
       KAA_TEST_CASE(topic_list_retrieving, test_retrieving_topic_list)
       KAA_TEST_CASE(serializing, test_serializing)
       KAA_TEST_CASE(subscriptions, test_subscriptions))

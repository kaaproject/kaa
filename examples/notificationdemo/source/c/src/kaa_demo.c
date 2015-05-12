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
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <execinfo.h>
#include <stddef.h>
#include <sys/select.h>

#include <kaa/kaa.h>
#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/kaa_channel_manager.h>
#include <kaa/kaa_configuration_manager.h>
#include <kaa/kaa_notification_manager.h>
#include <kaa/kaa_user.h>
#include <kaa/kaa_defaults.h>


#include <kaa/utilities/kaa_log.h>
#include <kaa/utilities/kaa_mem.h>

#include <kaa/platform/ext_sha.h>
#include <kaa/platform/ext_transport_channel.h>
#include <kaa/platform-impl/kaa_tcp_channel.h>



static kaa_context_t *kaa_context_ = NULL;

static kaa_service_t BOOTSTRAP_SERVICE[] = { KAA_SERVICE_BOOTSTRAP };
static const int BOOTSTRAP_SERVICE_COUNT = sizeof(BOOTSTRAP_SERVICE) / sizeof(kaa_service_t);

static kaa_service_t OPERATIONS_SERVICES[] = {KAA_SERVICE_PROFILE, KAA_SERVICE_NOTIFICATION};

static const int OPERATIONS_SERVICES_COUNT = sizeof(OPERATIONS_SERVICES) / sizeof(kaa_service_t);

static kaa_transport_channel_interface_t bootstrap_channel;
static kaa_transport_channel_interface_t operations_channel;

kaa_notification_listener_t notification_listener;
kaa_topic_listener_t topic_listener;

uint32_t topic_listener_id = 0;
uint32_t notification_listener_id = 0;

static bool is_shutdown = false;


void on_notification(void *context, uint64_t *topic_id, kaa_notification_t *notification)
{
    static short notifications_received = 2;
    if (notification->message->type == KAA_NOTIFICATION_UNION_STRING_OR_NULL_BRANCH_0) {
        kaa_string_t *message = (kaa_string_t *)notification->message->data;
        printf("Notification for topic id '%lu' received\n", *topic_id);
        printf("Notification body: %s\n", message->data);
        if (!--notifications_received) {
            is_shutdown = true;
        }
    } else {
        printf("Error:Received notification's body is null\n");
    }
}

void show_topics(kaa_list_t *topics)
{
    if (!topics) {
        printf("Topic list is empty");
        return;
    }
    kaa_topic_t *topic = NULL;
    while (topics) {
        topic = (kaa_topic_t *)kaa_list_get_data(topics);
        printf("Topic: id '%lu', name: %s, type: ", topic->id, topic->name);
        if (topic->subscription_type == MANDATORY) {
            printf("MANDATORY\n");
        } else {
            printf("OPTIONAL\n");
        }
        topics = kaa_list_next(topics);
    }
}

void on_list_uploaded(void *context, kaa_list_t *topics)
{
    printf("Topic list was updated\n");
    show_topics(topics);

    kaa_error_t err = KAA_ERR_NONE;
    kaa_context_t *kaa_context = (kaa_context_t *)context;
    kaa_topic_t *topic = NULL;
    while (topics) {
        topic = (kaa_topic_t *) kaa_list_get_data(topics);
        if (topic->subscription_type == OPTIONAL) {
            printf("Subscribing to optional topic '%lu'\n", topic->id);
            err = kaa_subscribe_to_topic(kaa_context->notification_manager, &topic->id, false);
            if (err) {
                printf("Failed to subscribe.\n");
            }
        }
        topics = kaa_list_next(topics);
    }
    err = kaa_sync_topic_subscriptions(kaa_context->notification_manager);
    if (err) {
        printf("Failed to sync subscriptions\n");
    }
}
/*
 * Initializes Kaa SDK.
 */
kaa_error_t kaa_sdk_init()
{
    printf("Initializing Kaa SDK...\n");

    kaa_error_t error_code = kaa_init(&kaa_context_);
    if (error_code) {
        printf("Error during kaa context creation %d\n", error_code);
        return error_code;
    }

    error_code = kaa_tcp_channel_create(&operations_channel
                                      , kaa_context_->logger
                                      , OPERATIONS_SERVICES
                                      , OPERATIONS_SERVICES_COUNT);
    KAA_RETURN_IF_ERR(error_code);

    error_code = kaa_tcp_channel_create(&bootstrap_channel
                                      , kaa_context_->logger
                                      , BOOTSTRAP_SERVICE
                                      , BOOTSTRAP_SERVICE_COUNT);
    KAA_RETURN_IF_ERR(error_code);

    error_code = kaa_channel_manager_add_transport_channel(kaa_context_->channel_manager
                                                         , &bootstrap_channel
                                                         , NULL);
    KAA_RETURN_IF_ERR(error_code);

    error_code = kaa_channel_manager_add_transport_channel(kaa_context_->channel_manager
                                                         , &operations_channel
                                                         , NULL);
    KAA_RETURN_IF_ERR(error_code);


    notification_listener.callback = &on_notification;
    notification_listener.context = kaa_context_;

    topic_listener.callback = &on_list_uploaded;
    topic_listener.context = kaa_context_;

    error_code = kaa_add_topic_list_listener(kaa_context_->notification_manager, &topic_listener, &topic_listener_id);
    if (error_code) {
        return error_code;
    }
    error_code = kaa_add_notification_listener(kaa_context_->notification_manager, &notification_listener, &notification_listener_id);
    if (error_code) {
        return error_code;
    }

    return KAA_ERR_NONE;
}

/*
 * Kaa demo lifecycle routine.
 */
kaa_error_t kaa_demo_init()
{
    kaa_error_t error_code = kaa_sdk_init();
    if (error_code) {
        printf("Failed to init Kaa SDK. Error code : %d\n", error_code);
        return error_code;
    }
    return KAA_ERR_NONE;
}

void kaa_demo_destroy()
{
    kaa_tcp_channel_disconnect(&operations_channel);
    kaa_deinit(kaa_context_);
}

int kaa_demo_event_loop()
{
    kaa_error_t error_code = kaa_start(kaa_context_);
    if (error_code) {
        printf("Failed to start Kaa workflow\n");
        return -1;
    }

    uint16_t select_timeout;
    error_code = kaa_tcp_channel_get_max_timeout(&operations_channel, &select_timeout);
    if (error_code) {
        printf("Failed to get Operations channel keepalive timeout\n");
        return -1;
    }

    if (select_timeout > 3) {
        select_timeout = 3;
    }

    fd_set read_fds, write_fds, except_fds;
    int ops_fd = 0, bootstrap_fd = 0;
    struct timeval select_tv = { 0, 0 };
    int max_fd = 0;

    while (!is_shutdown) {
        FD_ZERO(&read_fds);
        FD_ZERO(&write_fds);
        FD_ZERO(&except_fds);

        max_fd = 0;

        kaa_tcp_channel_get_descriptor(&operations_channel, &ops_fd);
        if (max_fd < ops_fd)
            max_fd = ops_fd;
        kaa_tcp_channel_get_descriptor(&bootstrap_channel, &bootstrap_fd);
        if (max_fd < bootstrap_fd)
            max_fd = bootstrap_fd;

        if (kaa_tcp_channel_is_ready(&operations_channel, FD_READ))
            FD_SET(ops_fd, &read_fds);
        if (kaa_tcp_channel_is_ready(&operations_channel, FD_WRITE))
            FD_SET(ops_fd, &write_fds);

        if (kaa_tcp_channel_is_ready(&bootstrap_channel, FD_READ))
            FD_SET(bootstrap_fd, &read_fds);
        if (kaa_tcp_channel_is_ready(&bootstrap_channel, FD_WRITE))
            FD_SET(bootstrap_fd, &write_fds);

        select_tv.tv_sec = select_timeout;
        select_tv.tv_usec = 0;

        int poll_result = select(max_fd + 1, &read_fds, &write_fds, NULL, &select_tv);
        if (poll_result == 0) {
            kaa_tcp_channel_check_keepalive(&operations_channel);
            kaa_tcp_channel_check_keepalive(&bootstrap_channel);
        } else if (poll_result > 0) {
            if (bootstrap_fd >= 0) {
                if (FD_ISSET(bootstrap_fd, &read_fds)) {
                    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE,"Processing IN event for the Bootstrap client socket %d", bootstrap_fd);
                    error_code = kaa_tcp_channel_process_event(&bootstrap_channel, FD_READ);
                    if (error_code)
                        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_NONE,"Failed to process IN event for the Bootstrap client socket %d", bootstrap_fd);
                }
                if (FD_ISSET(bootstrap_fd, &write_fds)) {
                    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE,"Processing OUT event for the Bootstrap client socket %d", bootstrap_fd);
                    error_code = kaa_tcp_channel_process_event(&bootstrap_channel, FD_WRITE);
                    if (error_code)
                        KAA_LOG_ERROR(kaa_context_->logger, error_code,"Failed to process OUT event for the Bootstrap client socket %d", bootstrap_fd);
                }
            }
            if (ops_fd >= 0) {
                if (FD_ISSET(ops_fd, &read_fds)) {
                    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE,"Processing IN event for the Operations client socket %d", ops_fd);
                    error_code = kaa_tcp_channel_process_event(&operations_channel, FD_READ);
                    if (error_code)
                        KAA_LOG_ERROR(kaa_context_->logger, error_code,"Failed to process IN event for the Operations client socket %d", ops_fd);
                }
                if (FD_ISSET(ops_fd, &write_fds)) {
                    KAA_LOG_DEBUG(kaa_context_->logger, KAA_ERR_NONE,"Processing OUT event for the Operations client socket %d", ops_fd);
                    error_code = kaa_tcp_channel_process_event(&operations_channel, FD_WRITE);
                    if (error_code)
                        KAA_LOG_ERROR(kaa_context_->logger, error_code,"Failed to process OUT event for the Operations client socket %d", ops_fd);
                }
            }
        } else {
            KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_BAD_STATE,"Failed to poll descriptors: %s", strerror(errno));
            return -1;
        }
    }
    return 0;
}


int main(/*int argc, char *argv[]*/)
{
    printf("Notification demo started\n");

    kaa_error_t error_code = kaa_demo_init();
    if (error_code) {
        printf("Failed to initialize Kaa demo. Error code: %d\n", error_code);
        return error_code;
    }

    int rval = kaa_demo_event_loop();

    kaa_demo_destroy();

    printf("Notification demo stopped\n");

    return rval;
}


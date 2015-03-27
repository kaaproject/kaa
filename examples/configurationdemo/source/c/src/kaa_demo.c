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
#include <sys/select.h>

#include <kaa/kaa.h>
#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/kaa_profile.h>
#include <kaa/kaa_channel_manager.h>
#include <kaa/kaa_configuration_manager.h>

#include <kaa/utilities/kaa_log.h>

#include <kaa/platform/ext_sha.h>
#include <kaa/platform/ext_transport_channel.h>
#include <kaa/platform-impl/kaa_tcp_channel.h>


static bool is_shutdown = false;

static kaa_context_t *kaa_context_ = NULL;
static kaa_service_t BOOTSTRAP_SERVICE[] = { KAA_SERVICE_BOOTSTRAP };
static const int BOOTSTRAP_SERVICE_COUNT = sizeof(BOOTSTRAP_SERVICE) / sizeof(kaa_service_t);

static kaa_service_t OPERATIONS_SERVICES[] = { KAA_SERVICE_PROFILE
                                             , KAA_SERVICE_CONFIGURATION };
static const int OPERATIONS_SERVICES_COUNT = sizeof(OPERATIONS_SERVICES) / sizeof(kaa_service_t);


static kaa_transport_channel_interface_t bootstrap_channel;
static kaa_transport_channel_interface_t operations_channel;

void kaa_demo_print_configuration_message(const kaa_root_configuration_t *configuration)
{
    if (configuration->address_list->type == KAA_CONFIGURATION_UNION_ARRAY_LINK_OR_NULL_BRANCH_0) {
        printf("Configuration body:\n");
        kaa_list_t *list_of_links = (kaa_list_t*) configuration->address_list->data;
        kaa_configuration_link_t* current_link = NULL;
        while (list_of_links)
        {
            current_link = (kaa_configuration_link_t*) kaa_list_get_data(list_of_links);
            printf("%s - %s\n", current_link->label->data,current_link->url->data);
            list_of_links = kaa_list_next(list_of_links);
        }
    } else {
        printf("Configuration body: null\n");
    }
}

kaa_error_t kaa_demo_configuration_receiver(void *context, const kaa_root_configuration_t *configuration)
{
    (void) context;
    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Received configuration data");
    kaa_demo_print_configuration_message(configuration);
    is_shutdown = true;
    return KAA_ERR_NONE;
}

/*
 * Initializes the Kaa SDK.
 */
kaa_error_t kaa_sdk_init()
{
    kaa_error_t error_code = kaa_init(&kaa_context_);
    if (error_code) {
        printf("Error during kaa context creation %d", error_code);
        return error_code;
    }

    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Adding transport channels");

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

    kaa_configuration_root_receiver_t receiver = { NULL, &kaa_demo_configuration_receiver };
    error_code = kaa_configuration_manager_set_root_receiver(kaa_context_->configuration_manager, &receiver);
    KAA_RETURN_IF_ERR(error_code);

    kaa_demo_print_configuration_message(kaa_configuration_manager_get_configuration(kaa_context_->configuration_manager));

    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Kaa SDK started");
    return KAA_ERR_NONE;
}

/*
 * The Kaa demo lifecycle routine.
 */
kaa_error_t kaa_demo_init()
{
    kaa_error_t error_code = kaa_sdk_init();
    if (error_code) {
        printf("Failed to init Kaa SDK. Error code : %d", error_code);
        return error_code;
    }
    return KAA_ERR_NONE;
}

int kaa_demo_event_loop()
{
    kaa_error_t error_code = kaa_start(kaa_context_);
    if (error_code) {
        KAA_LOG_FATAL(kaa_context_->logger, error_code,"Failed to start Kaa workflow");
        return -1;
    }

    uint16_t select_timeout;
    error_code = kaa_tcp_channel_get_max_timeout(&operations_channel, &select_timeout);
    if (error_code) {
        KAA_LOG_FATAL(kaa_context_->logger, error_code,"Failed to get Operations channel keepalive timeout");
        return -1;
    }

    if (select_timeout > 1) {
        select_timeout = 1;
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

void kaa_demo_destroy()
{
    kaa_tcp_channel_disconnect(&operations_channel);
    kaa_deinit(kaa_context_);
}

int main(/*int argc, char *argv[]*/)
{
    printf("%s", "Configuration demo started\n");
    kaa_error_t error_code = kaa_demo_init();
    if (error_code) {
        printf("Failed to initialize Kaa demo. Error code: %d\n", error_code);
        return error_code;
    }
    int rval = kaa_demo_event_loop();
    kaa_demo_destroy();
    printf("Configuration demo stopped\n");
    return rval;
}


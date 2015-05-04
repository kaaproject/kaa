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
#include <kaa/kaa_logging.h>
#include <kaa/kaa_channel_manager.h>
#include <kaa/kaa_configuration_manager.h>

#include <kaa/utilities/kaa_log.h>

#include <kaa/platform/ext_sha.h>
#include <kaa/platform/ext_transport_channel.h>
#include <kaa/platform-impl/kaa_tcp_channel.h>



/*
 * Hard-coded Kaa profile body.
 */
#define KAA_DEMO_PROFILE_ID "sampleid"
#define KAA_DEMO_OS_VERSION "1.0"
#define KAA_DEMO_BUILD_INFO "3cbaf67e"

/*
 * Strategy-specific configuration parameters used by Kaa log collection feature.
 */
#define KAA_DEMO_UPLOAD_COUNT_THRESHOLD      1 /* Count of collected logs needed to initiate log upload */
#define KAA_DEMO_LOG_GENERATION_FREQUENCY    1 /* In seconds */
#define KAA_DEMO_LOGS_TO_SEND                5

/*
 * Hard-coded Kaa log entry body.
 */
#define KAA_DEMO_LOG_TAG     "TAG"
#define KAA_DEMO_LOG_MESSAGE "MESSAGE_"



static kaa_context_t *kaa_context_ = NULL;

static kaa_service_t BOOTSTRAP_SERVICE[] = { KAA_SERVICE_BOOTSTRAP };
static const int BOOTSTRAP_SERVICE_COUNT = sizeof(BOOTSTRAP_SERVICE) / sizeof(kaa_service_t);

static kaa_service_t OPERATIONS_SERVICES[] = { KAA_SERVICE_PROFILE
                                             , KAA_SERVICE_LOGGING};
static const int OPERATIONS_SERVICES_COUNT = sizeof(OPERATIONS_SERVICES) / sizeof(kaa_service_t);

static kaa_transport_channel_interface_t bootstrap_channel;
static kaa_transport_channel_interface_t operations_channel;

static void *log_storage_context         = NULL;
static void *log_upload_strategy_context = NULL;

static size_t log_record_counter = 0;

/* forward declarations */

extern kaa_error_t ext_unlimited_log_storage_create(void **log_storage_context_p
                                                    , kaa_logger_t *logger);

extern kaa_error_t ext_log_upload_strategy_by_volume_create(void **strategy_p
                                                          , kaa_channel_manager_t   *channel_manager
                                                          , kaa_bootstrap_manager_t *bootstrap_manager);
extern kaa_error_t ext_log_upload_strategy_by_volume_set_threshold_count(void *strategy, size_t threshold_count);



/*
 * Initializes Kaa log collector.
 */
kaa_error_t kaa_log_collector_init()
{
    kaa_error_t error_code = ext_unlimited_log_storage_create(&log_storage_context, kaa_context_->logger);
    if (error_code) {
        KAA_LOG_ERROR(kaa_context_->logger, error_code, "Failed to create log storage");
        return error_code;
    }

    error_code = ext_log_upload_strategy_by_volume_create(&log_upload_strategy_context
                                                        , kaa_context_->channel_manager
                                                        , kaa_context_->bootstrap_manager);

    if (error_code) {
        KAA_LOG_ERROR(kaa_context_->logger, error_code, "Failed to create log upload strategy");
        return error_code;
    }

    error_code = ext_log_upload_strategy_by_volume_set_threshold_count(log_upload_strategy_context
                                                                     , KAA_DEMO_UPLOAD_COUNT_THRESHOLD);

    error_code = kaa_logging_init(kaa_context_->log_collector
                                , log_storage_context
                                , log_upload_strategy_context);

    return error_code;
}

/*
 * Initializes Kaa SDK.
 */
kaa_error_t kaa_sdk_init()
{
    kaa_error_t error_code = kaa_init(&kaa_context_);
    if (error_code) {
        printf("Error during kaa context creation %d", error_code);
        return error_code;
    }

    error_code = kaa_log_collector_init();
    if (error_code) {
        KAA_LOG_ERROR(kaa_context_->logger, error_code, "Failed to init Kaa log collector %d", error_code);
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

    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Kaa SDK started");
    return KAA_ERR_NONE;
}

/*
 * Kaa demo lifecycle routine.
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

void kaa_demo_destroy()
{
    kaa_tcp_channel_disconnect(&operations_channel);
    kaa_deinit(kaa_context_);
}

void kaa_demo_add_log_record()
{
    ++log_record_counter;

    printf("Going to add %zuth log record\n", log_record_counter);

    kaa_user_log_record_t *log_record = kaa_logging_log_data_create();
    if (!log_record) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_NOT_INITIALIZED, "Failed to allocate log record");
        return;
    }

    log_record->level = ENUM_LEVEL_KAA_INFO;
    log_record->tag = kaa_string_move_create(KAA_DEMO_LOG_TAG, NULL);

    size_t log_message_buffer_size = strlen(KAA_DEMO_LOG_MESSAGE) + sizeof(log_record_counter);
    char log_message_buffer[log_message_buffer_size];
    snprintf(log_message_buffer, log_message_buffer_size, "%s%zu", KAA_DEMO_LOG_MESSAGE, log_record_counter);

    log_record->message = kaa_string_copy_create(log_message_buffer);

    kaa_error_t error_code = kaa_logging_add_record(kaa_context_->log_collector, log_record);
    if (error_code)
        KAA_LOG_ERROR(kaa_context_->logger, error_code, "Failed to add log record");

    log_record->destroy(log_record);
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

    if (select_timeout > KAA_DEMO_LOG_GENERATION_FREQUENCY) {
        select_timeout = KAA_DEMO_LOG_GENERATION_FREQUENCY;
    }

    fd_set read_fds, write_fds, except_fds;
    int ops_fd = 0, bootstrap_fd = 0;
    struct timeval select_tv = { 0, 0 };
    int max_fd = 0;

    while (log_record_counter < KAA_DEMO_LOGS_TO_SEND) {
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
            kaa_demo_add_log_record();
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
    printf("Data collection demo started\n");

    kaa_error_t error_code = kaa_demo_init();
    if (error_code) {
        printf("Failed to initialize Kaa demo. Error code: %d\n", error_code);
        return error_code;
    }

    int rval = kaa_demo_event_loop();
    kaa_demo_destroy();

    printf("Data collection demo stopped\n");

    return rval;
}


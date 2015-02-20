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

#include <kaa.h>
#include <kaa_error.h>
#include <kaa_context.h>
#include <kaa_profile.h>
#include <kaa_logging.h>
#include <kaa_channel_manager.h>
#include <kaa_configuration_manager.h>

#include <utilities/kaa_log.h>

#include <platform/ext_sha.h>
#include <platform/ext_transport_channel.h>
#include <platform-impl/kaa_tcp_channel.h>



/*
 * Hard-coded Kaa profile body.
 */
#define KAA_DEMO_PROFILE_ID "sampleid"
#define KAA_DEMO_OS_VERSION "1.0"
#define KAA_DEMO_BUILD_INFO "3cbaf67e"

/*
 * Strategy-specific configuration parameters used by Kaa log collection feature.
 */
#define KAA_DEMO_MAX_UPLOAD_THRESHOLD     150   /* Size of collected serialized logs needed to initiate log upload */
#define KAA_DEMO_MAX_LOG_BUCKET_SIZE      160   /* Max size of a log batch has been sent by SDK during one upload. */
#define KAA_DEMO_MAX_CLEANUP_THRESHOLD    10000 /* Max size of an inner log storage. If size is exceeded, elder logs will be removed. */

#define KAA_DEMO_LOG_GENERATION_FREQUENCY    3 /* seconds */

/*
 * Hard-coded Kaa log entry body.
 */
#define KAA_DEMO_LOG_TAG     "Log tag"
#define KAA_DEMO_LOG_MESSAGE "Sample log message"

/*
 * Kaa status and public key storage file names.
 */
#define KAA_KEY_STORAGE       "key.txt"
#define KAA_STATUS_STORAGE    "status.conf"



static kaa_context_t *kaa_context_ = NULL;

static kaa_profile_t *kaa_default_profile = NULL;

static char *kaa_public_key           = NULL;
static uint32_t kaa_public_key_length = 0;

static kaa_service_t BOOTSTRAP_SERVICE[] = { KAA_SERVICE_BOOTSTRAP };
static const int BOOTSTRAP_SERVICE_COUNT = sizeof(BOOTSTRAP_SERVICE) / sizeof(kaa_service_t);

static kaa_service_t OPERATIONS_SERVICES[] = { KAA_SERVICE_PROFILE
                                             , KAA_SERVICE_USER
                                             , KAA_SERVICE_EVENT
                                             , KAA_SERVICE_LOGGING
                                             , KAA_SERVICE_CONFIGURATION };
static const int OPERATIONS_SERVICES_COUNT = sizeof(OPERATIONS_SERVICES) / sizeof(kaa_service_t);

static kaa_transport_channel_interface_t bootstrap_channel;
static kaa_transport_channel_interface_t operations_channel;

static void *log_storage_context         = NULL;
static void *log_upload_strategy_context = NULL;

static bool is_shutdown = false;



/* forward declarations */

extern kaa_error_t ext_log_storage_create(void **log_storage_context_p
                                        , kaa_logger_t *logger);
extern kaa_error_t ext_log_upload_strategy_by_volume_create(void **strategy_p
                                                          , size_t max_upload_threshold
                                                          , size_t max_log_bucket_size
                                                          , size_t max_cleanup_threshold);


/*
 * Initializes Kaa log collector.
 */
kaa_error_t kaa_log_collector_init()
{
    kaa_error_t error_code = ext_log_storage_create(&log_storage_context, kaa_context_->logger);
    if (error_code) {
        KAA_LOG_ERROR(kaa_context_->logger, error_code, "Failed to create log storage");
        return error_code;
    }

    error_code = ext_log_upload_strategy_by_volume_create(&log_upload_strategy_context
                                                        , KAA_DEMO_MAX_UPLOAD_THRESHOLD
                                                        , KAA_DEMO_MAX_LOG_BUCKET_SIZE
                                                        , KAA_DEMO_MAX_CLEANUP_THRESHOLD);
    if (error_code) {
        KAA_LOG_ERROR(kaa_context_->logger, error_code, "Failed to create common log storage interface: error_code = %d", error_code);
        return error_code;
    }

    error_code = kaa_logging_init(kaa_context_->log_collector
                                , log_storage_context
                                , log_upload_strategy_context);

    return error_code;
}


void kaa_demo_print_configuration_message(const kaa_root_configuration_t *configuration)
{
    if (configuration->message->type == KAA_CONFIGURATION_UNION_NULL_OR_STRING_BRANCH_0) {
        KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Current configuration: null");
    } else {
        kaa_string_t *message = (kaa_string_t *) configuration->message->data;
        KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Current configuration: %s", message->data);
    }
}

kaa_error_t kaa_demo_configuration_receiver(void *context, const kaa_root_configuration_t *configuration)
{
    (void) context;
    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Received configuration data");
    kaa_demo_print_configuration_message(configuration);
    return KAA_ERR_NONE;
}

/*
 * Initializes Kaa SDK.
 */
kaa_error_t kaa_sdk_init()
{
    printf("Initializing Kaa SDK...\n");

    kaa_error_t error_code = kaa_init(&kaa_context_);
    if (error_code) {
        printf("Error during kaa context creation %d", error_code);
        return error_code;
    }

    error_code = kaa_log_collector_init();
    if (error_code) {
        KAA_LOG_ERROR(kaa_context_->logger, error_code,
                        "Failed to init Kaa log collector %d", error_code);
        return error_code;
    }

    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Creating endpoint profile");

    kaa_default_profile = kaa_profile_profile_create();
    kaa_default_profile->id = kaa_string_move_create(KAA_DEMO_PROFILE_ID, NULL);
    kaa_default_profile->os = ENUM_OS_Linux;
    kaa_default_profile->os_version = kaa_string_move_create(KAA_DEMO_OS_VERSION, NULL);
    kaa_default_profile->build = kaa_string_move_create(KAA_DEMO_BUILD_INFO, NULL);

    KAA_LOG_TRACE(kaa_context_->logger, KAA_ERR_NONE, "Setting profile");
    kaa_profile_manager_update_profile(kaa_context_->profile_manager, kaa_default_profile);

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
 * Kaa demo lifecycle routine.
 */
kaa_error_t kaa_demo_init()
{
    printf("%s", "Initializing Kaa driver...");
    kaa_error_t error_code = kaa_sdk_init();
    if (error_code) {
        printf("Failed to init Kaa SDK. Error code : %d", error_code);
        return error_code;
    }
    return KAA_ERR_NONE;
}

void kaa_demo_destroy()
{
    printf("%s", "Destroying Kaa driver...");

    kaa_tcp_channel_disconnect(&operations_channel);

    if (kaa_default_profile) {
        kaa_default_profile->destroy(kaa_default_profile);
    }
    kaa_deinit(kaa_context_);
    if (kaa_public_key) {
        free(kaa_public_key);
        kaa_public_key_length = 0;
    }
}

void kaa_demo_add_log_record()
{
    kaa_user_log_record_t *log_record = kaa_logging_log_data_create();
    if (!log_record) {
        KAA_LOG_ERROR(kaa_context_->logger, KAA_ERR_NOT_INITIALIZED, "Failed to allocate log record");
        return;
    }

    log_record->level = (kaa_logging_level_t)(rand() % 6);
    log_record->tag = kaa_string_move_create(KAA_DEMO_LOG_TAG, NULL);
    log_record->message = kaa_string_move_create(KAA_DEMO_LOG_MESSAGE, NULL);

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

    kaa_demo_add_log_record();

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
            kaa_demo_add_log_record();
            kaa_tcp_channel_check_keepalive(&operations_channel);
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
    kaa_error_t error_code = kaa_demo_init();
    if (error_code) {
        printf("Failed to initialize Kaa demo. Error code: %d\n", error_code);
        return error_code;
    }

    KAA_LOG_INFO(kaa_context_->logger, KAA_ERR_NONE, "Kaa demo started");

    int rval = kaa_demo_event_loop();
    kaa_demo_destroy();

    printf("Kaa demo stopped\n");

    return rval;
}


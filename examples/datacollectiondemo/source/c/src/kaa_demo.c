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
#include <time.h>

#include <kaa/kaa_error.h>
#include <kaa/kaa_context.h>
#include <kaa/platform/kaa_client.h>
#include <kaa/utilities/kaa_log.h>
#include <kaa/kaa_logging.h>



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



/*
 * Forward declarations.
 */
extern kaa_error_t ext_unlimited_log_storage_create(void **log_storage_context_p
                                                  , kaa_logger_t *logger);

extern kaa_error_t ext_log_upload_strategy_by_volume_create(void **strategy_p
                                                          , kaa_channel_manager_t   *channel_manager
                                                          , kaa_bootstrap_manager_t *bootstrap_manager);

extern kaa_error_t ext_log_upload_strategy_by_volume_set_threshold_count(void *strategy, size_t threshold_count);



static kaa_client_t *kaa_client = NULL;

static void *log_storage_context         = NULL;
static void *log_upload_strategy_context = NULL;
static size_t log_record_counter = 0;



#define KAA_DEMO_RETURN_IF_ERROR(error, message) \
    if ((error)) { \
        printf(message ", error code %d\n", (error)); \
        return (error); \
    }

static void kaa_demo_add_log_record(void *context)
{
    if (log_record_counter++ >= KAA_DEMO_LOGS_TO_SEND) {
        kaa_client_stop((kaa_client_t *)context);
        return;
    }

    printf("Going to add %zuth log record\n", log_record_counter);

    kaa_user_log_record_t *log_record = kaa_logging_log_data_create();
    if (!log_record) {
        printf("Failed to create log record, error code %d\n", KAA_ERR_NOMEM);
        return;
    }

    log_record->level = ENUM_LEVEL_KAA_INFO;
    log_record->tag = kaa_string_move_create(KAA_DEMO_LOG_TAG, NULL);

    char log_message_buffer[32];
    snprintf(log_message_buffer, 32, KAA_DEMO_LOG_MESSAGE"%zu", log_record_counter);

    log_record->message = kaa_string_copy_create(log_message_buffer);

    kaa_error_t error_code = kaa_logging_add_record(kaa_client_get_context(kaa_client)->log_collector, log_record);
    if (error_code) {
        printf("Failed to add log record, error code %d\n", error_code);
    }

    log_record->destroy(log_record);
}

int main(/*int argc, char *argv[]*/)
{
    printf("Data collection demo started\n");

    /**
     * Initialize Kaa client.
     */
    kaa_error_t error_code = kaa_client_create(&kaa_client, NULL);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed create Kaa client");

    error_code = ext_unlimited_log_storage_create(&log_storage_context, kaa_client_get_context(kaa_client)->logger);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to create unlimited log storage");

    error_code = ext_log_upload_strategy_by_volume_create(&log_upload_strategy_context
                                                        , kaa_client_get_context(kaa_client)->channel_manager
                                                        , kaa_client_get_context(kaa_client)->bootstrap_manager);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to create log upload strategy");

    error_code = ext_log_upload_strategy_by_volume_set_threshold_count(log_upload_strategy_context
                                                                     , KAA_DEMO_UPLOAD_COUNT_THRESHOLD);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to set threshold log record count");

    error_code = kaa_logging_init(kaa_client_get_context(kaa_client)->log_collector
                                , log_storage_context
                                , log_upload_strategy_context);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to init Kaa log collector");

    /**
     * Start Kaa client main loop.
     */
    error_code = kaa_client_start(kaa_client, &kaa_demo_add_log_record, (void *)kaa_client, KAA_DEMO_LOG_GENERATION_FREQUENCY);
    KAA_DEMO_RETURN_IF_ERROR(error_code, "Failed to start Kaa main loop");

    /**
     * Destroy Kaa client.
     */
    kaa_client_destroy(kaa_client);

    printf("Data collection demo stopped\n");

    return error_code;
}


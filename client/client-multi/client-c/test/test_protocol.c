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

#include <stdio.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include "platform/ext_sha.h"

#include "kaa_logging.h"
#include "kaa_test.h"
#include "kaa_context.h"
#include "kaa_platform_protocol.h"
#include "kaa_channel_manager.h"
#include "kaa_platform_utils.h"
#include "kaa_configuration_manager.h"
#include "kaa_status.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "platform/ext_log_storage.h"
#include "platform/ext_log_upload_strategy.h"

#ifndef KAA_DISABLE_FEATURE_LOGGING
extern kaa_error_t kaa_status_create(kaa_status_t **kaa_status_p);
extern kaa_error_t kaa_init(kaa_context_t **kaa_context_p);
extern kaa_error_t kaa_channel_manager_create(kaa_channel_manager_t **channel_manager_p, kaa_context_t *context);
extern kaa_error_t kaa_log_collector_create(kaa_log_collector_t ** log_collector_p
                                          , kaa_status_t *status
                                          , kaa_channel_manager_t *channel_manager
                                          , kaa_logger_t *logger);

extern kaa_error_t kaa_platform_protocol_create(kaa_platform_protocol_t **platform_protocol_p, kaa_context_t *context
                                              , kaa_status_t *status);

extern kaa_error_t kaa_platform_protocol_serialize_client_sync(kaa_platform_protocol_t *self, const kaa_serialize_info_t *info
                                                             , char **buffer, size_t *buffer_size);

extern kaa_error_t kaa_deinit(kaa_context_t *kaa_context);


static kaa_context_t* kaa_context= NULL;
static kaa_logger_t *logger = NULL;
static kaa_status_t *status = NULL;
static kaa_channel_manager_t *channel_manager = NULL;
static kaa_platform_protocol_t *protocol = NULL;
static kaa_serialize_info_t *info;
static kaa_log_collector_t *log_collector = NULL;
char *buffer = NULL;
size_t buffer_size = 0;
static void *mock = NULL;


char* allocator(void *mock_context, size_t size)
{
    return (char *) malloc(size);
}

typedef struct {
    size_t timeout;
    size_t batch_size;
    bool on_timeout_count;
    bool on_failure_count;
} mock_strategy_context_t;

typedef struct {
    kaa_list_t *logs;
    size_t record_count;
    size_t total_size;
    bool on_remove_by_id_count;
    bool on_unmark_by_id_count;
} mock_storage_context_t;

void test_empty_log_collector_extension_count(void)
{
    kaa_service_t service = KAA_SERVICE_LOGGING;
    info = (kaa_serialize_info_t *) malloc(sizeof(kaa_serialize_info_t));
    info->services = &service;
    info->services_count = 1;
    info->allocator = &allocator;
    info->allocator_context = mock;
    mock_strategy_context_t *strategy = (mock_strategy_context_t*) malloc(sizeof(mock_strategy_context_t));
    mock_storage_context_t *storage = (mock_storage_context_t*) malloc(sizeof(mock_storage_context_t));
    if(!storage || !strategy) {
        exit(KAA_ERR_NOMEM);
    }
    memset(storage, 0, sizeof(mock_storage_context_t));
    memset(strategy, 0, sizeof(mock_strategy_context_t));
    kaa_error_t error_code = kaa_logging_init(log_collector, storage, strategy);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    kaa_context->log_collector = log_collector;
    error_code = kaa_platform_protocol_serialize_client_sync(protocol, info, &buffer, &buffer_size);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    char count_of_extensions = *(buffer + 7);
    KAA_LOG_DEBUG(kaa_context->logger, KAA_ERR_NONE, "count of extensions is %d", count_of_extensions);
    ASSERT_EQUAL(count_of_extensions, 1);
}
int initialize()
{
    kaa_error_t error_code = kaa_init(&kaa_context);
    if(error_code) {
    	exit(error_code);
    }
    error_code = kaa_status_create(&status);
    if(error_code) {
        exit(error_code);
    }
    error_code = kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    if(error_code || !logger) {
        exit(error_code);
    }

    kaa_context->logger = logger;
    error_code = kaa_platform_protocol_create(&protocol, kaa_context,status);
    if(error_code) {
    	exit(error_code);
    }
    error_code = kaa_channel_manager_create(&channel_manager, kaa_context);
    if(error_code) {
        exit(error_code);
        }
    error_code = kaa_log_collector_create(&log_collector, status, channel_manager, logger);
    if(error_code) {
        exit(error_code);
        }
	return KAA_ERR_NONE;
}

int free_resources()
{
    kaa_deinit(kaa_context);
    return KAA_ERR_NONE;
}

#endif

KAA_SUITE_MAIN(Protocol_test,initialize,free_resources
#ifndef KAA_DISABLE_FEATURE_LOGGING
       ,
       KAA_TEST_CASE(empty_log_collector_test, test_empty_log_collector_extension_count)
#endif
        )

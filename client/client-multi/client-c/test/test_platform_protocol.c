/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include <stdio.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include <stdint.h>
#include "platform/ext_sha.h"

#include "kaa.h"
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
#include "platform-impl/common/ext_log_upload_strategies.h"

#ifndef KAA_DISABLE_FEATURE_LOGGING

static kaa_context_t* kaa_context= NULL;
static kaa_serialize_info_t *info = NULL;
char *buffer = NULL;
size_t buffer_size = 0;
static void *mock = NULL;


char* allocator(void *mock_context, size_t size)
{
    return (char *) KAA_MALLOC(size);
}

extern kaa_error_t ext_unlimited_log_storage_create(void **log_storage_context_p
                                                  , kaa_logger_t *logger);

void test_empty_log_collector_extension_count(void)
{
    kaa_service_t service = KAA_SERVICE_LOGGING;
    info = (kaa_serialize_info_t *) KAA_MALLOC(sizeof(kaa_serialize_info_t));
    info->services = &service;
    info->services_count = 1;
    info->allocator = &allocator;
    info->allocator_context = mock;

    void *log_storage_context         = NULL;
    void *log_upload_strategy_context = NULL;

    kaa_error_t error_code = ext_unlimited_log_storage_create(&log_storage_context, kaa_context->logger);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = ext_log_upload_strategy_create(kaa_context
                                              , &log_upload_strategy_context
                                              , KAA_LOG_UPLOAD_VOLUME_STRATEGY);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);


    kaa_log_bucket_constraints_t constraints = {
        .max_bucket_size = 1024,
        .max_bucket_log_count = UINT32_MAX,
    };

    error_code = kaa_logging_init(kaa_context->log_collector, log_storage_context, log_upload_strategy_context, &constraints);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_platform_protocol_serialize_client_sync(kaa_context->platform_protocol, info, &buffer, &buffer_size);
    KAA_FREE(info);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    char count_of_extensions = *(buffer + 7);
    KAA_LOG_DEBUG(kaa_context->logger, KAA_ERR_NONE, "count of extensions is %d, expected 1", count_of_extensions);
    ASSERT_EQUAL(count_of_extensions, 1);
}
int test_init(void)
{
    kaa_error_t error_code = kaa_init(&kaa_context);
    if (error_code) {
    	return KAA_ERR_NOMEM;
    }
	return KAA_ERR_NONE;
}

int test_deinit(void)
{
    kaa_deinit(kaa_context);
    KAA_FREE(buffer);
    return KAA_ERR_NONE;
}

#endif

KAA_SUITE_MAIN(plarform_protocol_test,test_init,test_deinit
#ifndef KAA_DISABLE_FEATURE_LOGGING
       ,
       KAA_TEST_CASE(empty_log_collector_test, test_empty_log_collector_extension_count)
#endif
        )

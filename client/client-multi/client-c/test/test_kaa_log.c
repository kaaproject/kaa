/*
 * Copyright 2014 CyberVision, Inc.
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

#include "kaa_logging.h"
#include "kaa_context.h"
#include "log/kaa_memory_log_storage.h"
#include "kaa_test.h"
#include "kaa_mem.h"
#include <stdio.h>

static kaa_log_upload_decision_t decision(kaa_storage_status_t *status)
{
    if ((* status->get_records_count)() == 2) {
        return UPLOAD;
    }
    return NOOP;
}

void handler(size_t service_count, const kaa_service_t services[])
{
    ASSERT_EQUAL(1, service_count);
    ASSERT_EQUAL(services[0], KAA_SERVICE_LOGGING);

    kaa_sync_request_t *request = NULL;
    kaa_compile_request(&request, service_count, services);

    request->destruct(request);
    KAA_FREE(request);
}

void test_create_log_collector()
{
    kaa_log_collector_t * collector = NULL;
    kaa_error_t err_code = kaa_create_log_collector(&collector);
    ASSERT_EQUAL(err_code, KAA_ERR_NONE);
    ASSERT_NOT_NULL(collector);
    kaa_destroy_log_collector(collector);

}

kaa_service_t services[4] = {
KAA_SERVICE_PROFILE,
KAA_SERVICE_USER,KAA_SERVICE_EVENT,
KAA_SERVICE_LOGGING
};

void test_add_log()
{
    kaa_init();
    kaa_set_sync_handler(&handler, 4, services);
    kaa_set_log_storage(get_memory_log_storage(), get_memory_log_storage_status(), &decision);

    kaa_user_log_record_t *record = kaa_create_test_log_record();
    record->data = "hello";
    for (int i = 1000000; i--; ) {
        kaa_add_log(record);
    }
    kaa_deinit();

    KAA_FREE(record);
}

int main(int argc, char ** argv)
{
    test_create_log_collector();
    test_add_log();
    return 0;
}


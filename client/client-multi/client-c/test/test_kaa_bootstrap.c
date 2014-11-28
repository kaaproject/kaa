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

#include <string.h>

#include "kaa_test.h"
#include "kaa_log.h"

#include "kaa_bootstrap.h"
#include "kaa_mem.h"

static kaa_logger_t *logger = NULL;

void test_create_bootstrap_manager()
{
    KAA_TRACE_IN(logger);

    kaa_bootstrap_manager_t* manager = NULL;
    kaa_create_bootstrap_manager(&manager);

    ASSERT_NOT_NULL(manager);

    kaa_destroy_bootstrap_manager(manager);
}

void test_null_operation_server()
{
    KAA_TRACE_IN(logger);

    kaa_ops_t* server = NULL;
    kaa_bootstrap_manager_t* manager = NULL;

    server = kaa_get_current_operation_server(manager, HTTP);
    ASSERT_NULL(server);
    server = kaa_get_next_operation_server(manager, HTTP);
    ASSERT_NULL(server);

    kaa_create_bootstrap_manager(&manager);

    server = kaa_get_current_operation_server(manager, HTTP);
    ASSERT_NULL(server);
    server = kaa_get_current_operation_server(manager, HTTP_LP);
    ASSERT_NULL(server);
    server = kaa_get_current_operation_server(manager, KAATCP);
    ASSERT_NULL(server);

    server = kaa_get_next_operation_server(manager, HTTP);
    ASSERT_NULL(server);

    int unexpected_channel_type = 30;
    server = kaa_get_current_operation_server(manager, unexpected_channel_type);
    ASSERT_NULL(server);

    kaa_destroy_bootstrap_manager(manager);
}

void test_add_get_operation_server()
{
    KAA_TRACE_IN(logger);

    kaa_bootstrap_manager_t* manager = NULL;
    kaa_create_bootstrap_manager(&manager);

    char* encoded_public_key = "a2V5";
    uint16_t key_size = strlen(encoded_public_key);

    char* const host = "test.com";
    uint8_t host_len = strlen(host);

    kaa_ops_ip_t* ops1 = KAA_MALLOC(kaa_ops_ip_t);

    ops1->channel_type = HTTP;
    ops1->priority = 5;
    ops1->hostname = host;
    ops1->hostname_length = host_len;
    ops1->port = 80;
    ops1->public_key = encoded_public_key;
    ops1->public_key_length = key_size;

    kaa_ops_ip_t* ops2 = KAA_MALLOC(kaa_ops_ip_t);

    ops2->channel_type = HTTP;
    ops2->priority = 0;
    ops2->hostname = host;
    ops2->hostname_length = host_len;
    ops2->port = 443;
    ops2->public_key = encoded_public_key;
    ops2->public_key_length = key_size;

    kaa_ops_ip_t* ops3 = KAA_MALLOC(kaa_ops_ip_t);

    ops3->channel_type = HTTP;
    ops3->priority = 3;
    ops3->hostname = host;
    ops3->hostname_length = host_len;
    ops3->port = 43;
    ops3->public_key = encoded_public_key;
    ops3->public_key_length = key_size;

    kaa_add_operation_server(manager, (kaa_ops_t*)ops1);
    kaa_add_operation_server(manager, (kaa_ops_t*)ops3);
    kaa_add_operation_server(manager, (kaa_ops_t*)ops2);

    kaa_ops_ip_t* cur_http_ops = (kaa_ops_ip_t*)kaa_get_current_operation_server(manager, HTTP);
    ASSERT_NOT_NULL(cur_http_ops);
    ASSERT_EQUAL(cur_http_ops->port, 443);

    cur_http_ops = (kaa_ops_ip_t*)kaa_get_next_operation_server(manager, HTTP);
    ASSERT_NOT_NULL(cur_http_ops);
    ASSERT_EQUAL(cur_http_ops->port, 43);

    cur_http_ops = (kaa_ops_ip_t*)kaa_get_next_operation_server(manager, HTTP);
    ASSERT_NOT_NULL(cur_http_ops);
    ASSERT_EQUAL(cur_http_ops->port, 80);

    cur_http_ops = (kaa_ops_ip_t*)kaa_get_next_operation_server(manager, HTTP);
    ASSERT_NULL(cur_http_ops);

    KAA_FREE(ops1);
    KAA_FREE(ops2);
    KAA_FREE(ops3);

    kaa_destroy_bootstrap_manager(manager);
}

int test_init(void)
{
    kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_LOG_TRACE, NULL);
    return 0;
}

int test_deinit(void)
{
    kaa_log_destroy(logger);
    return 0;
}

KAA_SUITE_MAIN(Bootstrap, test_init, test_deinit,
        KAA_TEST_CASE(create_bootstrap_manager, test_create_bootstrap_manager)
        KAA_TEST_CASE(null_operations_server, test_null_operation_server)
        KAA_TEST_CASE(add_get_operation_server, test_add_get_operation_server)
)


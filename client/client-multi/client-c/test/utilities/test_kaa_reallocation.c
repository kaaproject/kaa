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

#include <stdint.h>
#include <stdio.h>

#include "kaa_test.h"

#include "utilities/kaa_log.h"
#include "utilities/kaa_buffer.h"

#define BUFFER_SIZE 10

static kaa_logger_t *logger = NULL;


void test_reallocation(void **state)
{
    (void)state;

    kaa_buffer_t *buffer_ptr;
    kaa_error_t error_code;

    error_code = kaa_buffer_create_buffer(&buffer_ptr, BUFFER_SIZE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_buffer_lock_space(buffer_ptr, BUFFER_SIZE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_buffer_lock_space(buffer_ptr, BUFFER_SIZE);
    ASSERT_EQUAL(error_code, KAA_ERR_BUFFER_IS_NOT_ENOUGH);

    error_code = kaa_buffer_reallocate_space(buffer_ptr, BUFFER_SIZE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    error_code = kaa_buffer_lock_space(buffer_ptr, BUFFER_SIZE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    
    // Test reallocation with free space already in the buffer
    error_code = kaa_buffer_reallocate_space(buffer_ptr, BUFFER_SIZE);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = kaa_buffer_reallocate_space(buffer_ptr, BUFFER_SIZE*2);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);
    error_code = kaa_buffer_lock_space(buffer_ptr, BUFFER_SIZE*2);
    ASSERT_EQUAL(error_code, KAA_ERR_NONE);

    kaa_buffer_destroy(buffer_ptr);
}

int test_init(void)
{
    kaa_log_create(&logger, KAA_MAX_LOG_MESSAGE_LENGTH, KAA_MAX_LOG_LEVEL, NULL);
    return 0;
}

int test_deinit(void)
{
    kaa_log_destroy(logger);
    return 0;
}

KAA_SUITE_MAIN(Context, test_init, test_deinit
        , KAA_TEST_CASE(reallocation, test_reallocation)
)

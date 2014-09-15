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

#ifndef KAA_COMMON_H_
#define KAA_COMMON_H_

#ifdef __cplusplus
extern "C" {
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
#endif

#include <stddef.h>
#include <stdint.h>

#include "kaa_error.h"

typedef enum kaa_server_response_result_t {
    KAA_SUCCESS,
    KAA_FAILURE
} kaa_server_response_result_t;

#define KAA_NON_VOID(p, E) \
    if ((void *)0 == p) { return E; }

#define KAA_BOOL                int8_t
#define KAA_INT32T              int32_t
#define KAA_INT64T              int64_t

typedef void (* user_response_handler_t)(KAA_BOOL is_attached);
typedef void (* event_callback_t)(const char * event_fqn, const char * event_data, size_t event_data_size);

typedef enum {
    KAA_SERVICE_BOOTSTRAP = 0,
    KAA_SERVICE_PROFILE = 1,
    KAA_SERVICE_USER = 2,
    KAA_SERVICE_EVENT = 3,
} kaa_service_t;

typedef void (*kaa_sync_t)(size_t service_count, const kaa_service_t services[]);
typedef void (*kaa_sync_all_t)();

/**
 * Hash calculating stuff
 */
#define SHA_1_DIGEST_LENGTH 20
typedef unsigned char kaa_digest[SHA_1_DIGEST_LENGTH];
/**
 * A size of a hash buffer must be not less than SHA_1_DIGEST_LENGTH
 */
kaa_error_t kaa_calculate_sha_hash(const char *data, size_t data_size, kaa_digest digest);

typedef enum kaa_channel_type_t {
    HTTP,
    HTTP_LP,
    KAATCP
} kaa_channel_type_t;

/**
 * Define's value should be equal to a number of
 * elements in kaa_channel_type_t enumeration
 */
#define KAA_CHANNEL_TYPE_COUNT   3

CLOSE_EXTERN
#endif /* KAA_COMMON_H_ */

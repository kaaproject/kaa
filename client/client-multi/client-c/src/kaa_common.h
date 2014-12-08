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

#include <stddef.h>
#include <stdint.h>
#include <stdbool.h>

#include "kaa_error.h"

#ifdef __cplusplus
extern "C" {
#endif

#define KAA_RETURN_IF_NIL(p, E) \
    { if (!(p)) return E; }

#define KAA_RETURN_IF_NIL2(p1, p2, E) \
    { if (!(p1) || !(p2)) return E; }

#define KAA_RETURN_IF_NIL3(p1, p2, p3, E) \
    { if (!(p1) || !(p2) || !(p3)) return E; }

#define KAA_RETURN_IF_NIL4(p1, p2, p3, p4, E) \
    { if (!(p1) || !(p2) || !(p3) || !(p4)) return E; }

// TODO: move to kaa_event.h


typedef enum {
    KAA_SERVICE_BOOTSTRAP = 0,
    KAA_SERVICE_PROFILE = 1,
    KAA_SERVICE_USER = 2,
    KAA_SERVICE_EVENT = 3,
    KAA_SERVICE_LOGGING = 4,
} kaa_service_t;

/**
 * SHA1 hash
 */
#define SHA_1_DIGEST_LENGTH 20
typedef unsigned char kaa_digest[SHA_1_DIGEST_LENGTH];
typedef const unsigned char* kaa_digest_p;
kaa_error_t kaa_calculate_sha_hash(const char *data, size_t data_size, kaa_digest digest);

// TODO: Channel types must be represented as a list managed in runtime by the channel_manager
#define KAA_CHANNEL_TYPE_COUNT 3
typedef enum {
    HTTP,
    HTTP_LP,
    KAATCP
} kaa_channel_type_t;

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_COMMON_H_ */

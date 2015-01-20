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

/**
 * @file kaa_common.h
 * @brief Common C EP SDK definitions and small utilities
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


/*
 * Standard error handling macros
 */
#define KAA_RETURN_IF_ERR(E) \
    { if (E) return E; }

#define KAA_RETURN_IF_NIL(p, E) \
    { if (!(p)) return E; }

#define KAA_RETURN_IF_NIL2(p1, p2, E) \
    { if (!(p1) || !(p2)) return E; }

#define KAA_RETURN_IF_NIL3(p1, p2, p3, E) \
    { if (!(p1) || !(p2) || !(p3)) return E; }

#define KAA_RETURN_IF_NIL4(p1, p2, p3, p4, E) \
    { if (!(p1) || !(p2) || !(p3) || !(p4)) return E; }

#define KAA_RETURN_IF_NIL5(p1, p2, p3, p4, p5,E) \
    { if (!(p1) || !(p2) || !(p3) || !(p4) || !(p5)) return E; }


/**
 * @brief Types of Kaa platform services
 */
typedef enum {
    KAA_SERVICE_BOOTSTRAP = 0,
    KAA_SERVICE_PROFILE = 1,
    KAA_SERVICE_USER = 2,
    KAA_SERVICE_EVENT = 3,
    KAA_SERVICE_LOGGING = 4,
} kaa_service_t;

/**
 * @brief Unique identifier used to represent client transport channel implementations.
 */
typedef struct {
    uint32_t id;
    uint16_t version;
} kaa_transport_protocol_info_t;

/**
 * @brief Connection parameters used by transport channels to establish
 * connection both to Bootstrap and Operations servers.
 */
typedef struct {
    uint32_t    id;
    uint16_t    connection_data_len;
    char        *connection_data;
} kaa_access_point_t;



#define SHA_1_DIGEST_LENGTH    20

typedef unsigned char kaa_digest[SHA_1_DIGEST_LENGTH];
typedef const unsigned char* kaa_digest_p;

/*
 * @brief SHA-1 hash calculation routine.
 */
kaa_error_t kaa_calculate_sha_hash(const char *data, size_t data_size, kaa_digest digest);



#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_COMMON_H_ */

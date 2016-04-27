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

/**
 * @file kaa_common.h
 * @brief Common C EP SDK definitions and small utilities
 */

#ifndef KAA_COMMON_H_
#define KAA_COMMON_H_

#include <stdint.h>
#include "kaa_error.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief Kaa extensions.
 */
typedef enum {
    // Don't change numbers. They are critical to platform protocol.
    KAA_EXTENSION_BOOTSTRAP     = 0,
    KAA_EXTENSION_META_DATA     = 1,
    KAA_EXTENSION_PROFILE       = 2,
    KAA_EXTENSION_USER          = 3,
    KAA_EXTENSION_LOGGING       = 4,
    KAA_EXTENSION_CONFIGURATION = 5,
    KAA_EXTENSION_EVENT         = 7,
    KAA_EXTENSION_NOTIFICATION  = 6,
} kaa_extension_id;

/*
 * Standard error handling macros
 */
#define KAA_RETURN_IF_ERR(E) \
    do { if (E) return E; } while (0)

#define KAA_RETURN_IF_NIL(p, E) \
    do { if (!(p)) return E; } while (0)

#define KAA_RETURN_IF_NIL2(p1, p2, E) \
    do { if (!(p1) || !(p2)) return E; } while (0)

#define KAA_RETURN_IF_NIL3(p1, p2, p3, E) \
    do { if (!(p1) || !(p2) || !(p3)) return E; } while (0)

#define KAA_RETURN_IF_NIL4(p1, p2, p3, p4, E) \
    do { if (!(p1) || !(p2) || !(p3) || !(p4)) return E; } while (0)

#define KAA_RETURN_IF_NIL5(p1, p2, p3, p4, p5,E) \
    do { if (!(p1) || !(p2) || !(p3) || !(p4) || !(p5)) return E; } while (0)

/**
 * @brief Identifier used to uniquely represent transport protocol.
 */
typedef struct {
    uint32_t id;
    uint16_t version;
} kaa_transport_protocol_id_t;

static inline int kaa_transport_protocol_id_equals(const kaa_transport_protocol_id_t *first, const kaa_transport_protocol_id_t *second)
{
    return first && second && first->id == second->id && first->version == second->version;
}

/**
 * @brief Connection parameters used by transport channels to establish
 * connection both to Bootstrap and Operations servers.
 */
typedef struct {
    uint32_t    id;
    uint16_t    connection_data_len;
    char        *connection_data;
} kaa_access_point_t;

/*
 * Endpoint ID
 */
#define KAA_ENDPOINT_ID_LENGTH 20
typedef uint8_t        kaa_endpoint_id[KAA_ENDPOINT_ID_LENGTH];
typedef const uint8_t *kaa_endpoint_id_p;



#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_COMMON_H_ */

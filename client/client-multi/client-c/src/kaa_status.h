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

#ifndef KAA_STATUS_H_
#define KAA_STATUS_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "kaa_error.h"
#include "kaa_common.h"

typedef struct
{

    uint32_t        event_seq_n;
    uint16_t        log_bucket_id;
    bool            is_registered;
    bool            is_attached;
    kaa_digest      endpoint_public_key_hash;
    kaa_digest      profile_hash;

    char *          endpoint_access_token;
} kaa_status_t;


#ifdef __cplusplus
} // extern "C"
#endif
#endif /* KAA_STATUS_H_ */

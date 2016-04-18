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

#ifndef KAA_STATUS_H_
#define KAA_STATUS_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "kaa_error.h"
#include "kaa_common.h"
#include "collections/kaa_list.h"
#include "platform/ext_sha.h"

typedef struct {
    uint64_t topic_id;
    uint32_t sqn_number;
} kaa_topic_state_t;

#ifndef KAA_STATUS_T
# define KAA_STATUS_T
typedef struct
{
    uint32_t        event_seq_n;
    bool            is_registered;
    bool            is_attached;
    bool            is_updated;
    kaa_digest      endpoint_public_key_hash;
    kaa_digest      profile_hash;
    bool            profile_needs_resync;   /**< Indicates that profile should be resynced */

    kaa_list_t      *topic_states;          /**< States of topics received */
    kaa_list_t      *topics;                /**< Whole set of topics */
    int32_t         topic_list_hash;        /**< List hash */
    char            *endpoint_access_token;
    bool            has_update; /**< Indicates that status was changed on the client size */
} kaa_status_t;

#endif


#ifdef __cplusplus
} // extern "C"
#endif
#endif /* KAA_STATUS_H_ */

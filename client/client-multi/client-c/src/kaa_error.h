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

#ifndef KAA_ERROR_H_
#define KAA_ERROR_H_

#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
    KAA_ERR_NONE                = 0,

    /* General errors */
    KAA_ERR_NOMEM               = -1,
    KAA_ERR_BADDATA             = -2,
    KAA_ERR_BADPARAM            = -3,
    KAA_ERR_READ_FAILED         = -4,
    KAA_ERR_WRITE_FAILED        = -5,
    KAA_ERR_NOT_FOUND           = -6,
    KAA_ERR_NOT_INITIALIZED     = -7,
    KAA_ERR_BAD_STATE           = -8,
    KAA_ERR_INVALID_PUB_KEY     = -9,
    KAA_ERR_INVALID_BUFFER_SIZE = -10,
    KAA_ERR_UNSUPPORTED         = -11,

    KAA_ERR_EVENT_NOT_ATTACHED  = -41,
    KAA_ERR_EVENT_BAD_FQN       = -42,
    KAA_ERR_EVENT_TRX_NOT_FOUND = -43,
} kaa_error_t;

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_ERROR_H_ */

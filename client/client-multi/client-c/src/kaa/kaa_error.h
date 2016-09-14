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
 * @file kaa_error.h
 * @brief Kaa error codes
 *
 * Defines @c kaa_error_t enumeration type for standard error codes used across the C Endpoint SDK.
 */

#ifndef KAA_ERROR_H_
#define KAA_ERROR_H_

#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
    KAA_ERR_NONE                    = 0,

    /* General errors */
    KAA_ERR_GENERIC                 = -1,
    KAA_ERR_NOMEM                   = -2,
    KAA_ERR_BADDATA                 = -3,
    KAA_ERR_BADPARAM                = -4,
    KAA_ERR_READ_FAILED             = -5,
    KAA_ERR_WRITE_FAILED            = -6,
    KAA_ERR_NOT_FOUND               = -7,
    KAA_ERR_NOT_INITIALIZED         = -8,
    KAA_ERR_BAD_STATE               = -9,
    KAA_ERR_INVALID_PUB_KEY         = -10,
    KAA_ERR_INVALID_BUFFER_SIZE     = -11,
    KAA_ERR_UNSUPPORTED             = -12,
    KAA_ERR_BAD_PROTOCOL_ID         = -13,
    KAA_ERR_BAD_PROTOCOL_VERSION    = -14,
    KAA_ERR_INSUFFICIENT_BUFFER     = -15,
    KAA_ERR_ALREADY_EXISTS          = -16,
    KAA_ERR_TIMEOUT                 = -17,
    KAA_ERR_PROFILE_IS_NOT_SET      = -18,
    /* TODO(KAA-924): temporary solution to initiate SDK stop in certian cases */
    KAA_ERR_SDK_STOP                = -19,

    KAA_ERR_EVENT_NOT_ATTACHED      = -41,
    KAA_ERR_EVENT_BAD_FQN           = -42,
    KAA_ERR_EVENT_TRX_NOT_FOUND     = -43,

    KAA_ERR_BUFFER_IS_NOT_ENOUGH    = -51,
    KAA_ERR_BUFFER_INVALID_SIZE     = -52,

    KAA_ERR_SOCKET_ERROR            = -91,
    KAA_ERR_SOCKET_CONNECT_ERROR    = -92,
    KAA_ERR_SOCKET_INVALID_FAMILY   = -93,

    KAA_ERR_TCPCHANNEL_AP_RESOLVE_FAILED   = -101,
    KAA_ERR_TCPCHANNEL_PARSER_INIT_FAILED  = -102,
    KAA_ERR_TCPCHANNEL_PARSER_ERROR        = -103,
} kaa_error_t;

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_ERROR_H_ */

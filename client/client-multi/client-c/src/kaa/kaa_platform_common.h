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

#ifndef KAA_PLATFORM_COMMON_H_
#define KAA_PLATFORM_COMMON_H_

#ifdef __cplusplus
extern "C" {
#endif

#define KAA_PLATFORM_PROTOCOL_VERSION        1
#define KAA_PLATFORM_PROTOCOL_ID             0x3553c66f

#define KAA_ALIGNMENT                        4

/*
 * All definitions related to a field size specify in bytes.
 */
#define KAA_PROTOCOL_ID_SIZE                 4
#define KAA_PROTOCOL_VERSION_SIZE            2
#define KAA_PROTOCOL_EXTENSIONS_COUNT_SIZE   2
#define KAA_PROTOCOL_MESSAGE_HEADER_SIZE     (KAA_PROTOCOL_ID_SIZE + KAA_PROTOCOL_VERSION_SIZE + KAA_PROTOCOL_EXTENSIONS_COUNT_SIZE)

#define KAA_EXTENSION_TYPE_SIZE              2
#define KAA_EXTENSION_OPTIONS_SIZE           2
#define KAA_EXTENSION_PAYLOAD_LENGTH_SIZE    4
#define KAA_EXTENSION_HEADER_SIZE            (KAA_EXTENSION_TYPE_SIZE + KAA_EXTENSION_OPTIONS_SIZE + KAA_EXTENSION_PAYLOAD_LENGTH_SIZE)


#define KAA_SDK_TOKEN_LENGTH    27

/**
 * Constants used for a meta data extension.
 */
typedef enum {
    TIMEOUT_VALUE         = 0x1,
    PUBLIC_KEY_HASH_VALUE = 0x2,
    PROFILE_HASH_VALUE    = 0x4,
    APP_TOKEN_VALUE       = 0x8
} kaa_meta_data_extension_options_t;

/**
 * Constants used for a profile extension.
 */
typedef enum {
    CONFIG_SCHEMA_VERSION_VALUE       = 0x0,
    PROFILE_SCHEMA_VERSION_VALUE      = 0x1,
    SYS_NF_VERSION_VALUE              = 0x2,
    USER_NF_VERSION_VALUE             = 0x3,
    LOG_SCHEMA_VERSION_VALUE          = 0x4,
    EVENT_FAMILY_VERSIONS_COUNT_VALUE = 0x5,
    PUB_KEY_VALUE                     = 0x6,
    ACCESS_TOKEN_VALUE                = 0x7
} kaa_profile_extension_options_t;



#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* SRC_KAA_PLATFORM_COMMON_H_ */

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

#ifndef KAA_DEFAULTS_H_
#define KAA_DEFAULTS_H_

#ifdef __cplusplus
extern "C" {
#define CLOSE_EXTERN }
#else
#define CLOSE_EXTERN
# endif

#define BUILD_VERSION                   "0.6.1-SNAPSHOT"
#define BUILD_COMMIT_HASH               "N/A"

#define APPLICATION_TOKEN               "token"

#define CONFIG_SCHEMA_VERSION           6
#define PROFILE_SCHEMA_VERSION          7
#define SYSTEM_NF_SCHEMA_VERSION        1
#define USER_NF_SCHEMA_VERSION          5
#define LOG_SCHEMA_VERSION              4

#include "kaa_common.h"

typedef struct kaa_events_schema_version_ {
    const char *    name;
    KAA_INT32T      version;
} kaa_events_schema_version;

#define KAA_EVENT_SCHEMA_VERSIONS_SIZE    2

static const kaa_events_schema_version KAA_EVENT_SCHEMA_VERSIONS[KAA_EVENT_SCHEMA_VERSIONS_SIZE] = {
    {
          /* .name = */   "1ecf"
        , /* .version = */3
    },
    {
          /* .name = */   "2ecf"
        , /* .version = */2
    }};

/**
 * Bootstrap server info
 */

typedef struct kaa_channel_info_t {
    char*              host;
    uint16_t           port;
} kaa_channel_info_t;

typedef struct
 {
    const char *   encoded_public_key; /* Base 64 encoded */
    uint16_t       encoded_public_key_len;
    kaa_channel_info_t channels[KAA_CHANNEL_TYPE_COUNT];
} kaa_bootstrap_server_info_t;

#define KAA_BOOTSTRAP_SERVER_COUNT    2

static kaa_bootstrap_server_info_t KAA_BOOTSTRAP_SERVERS[KAA_BOOTSTRAP_SERVER_COUNT] = {
    {
          /* .encoded_public_key = */    "AQID"
        , /* .encoded_public_key_len = */4
        , /* .channels = */              {
                                            { "http1.com", 80 },
                                            { NULL, 0},
                                            { "kaatcp1.com", 9999 }
                                         }
    },
    {
            /* .encoded_public_key = */    "AQID"
          , /* .encoded_public_key_len = */4
          , /* .channels = */              {
                                              { NULL, 0 },
                                              { "http_lp.com", 80 },
                                              { "kaatcp2.com", 8888 }
                                           }
}};

CLOSE_EXTERN
# endif /* KAA_DEFAULTS_H_ */

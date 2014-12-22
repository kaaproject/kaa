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
 * @file kaa_context.h
 * @brief Kaa endpoint context
 *
 * Defines general Kaa endpoint context.
 */

#ifndef KAA_CONTEXT_H_
#define KAA_CONTEXT_H_

#ifdef __cplusplus
extern "C" {
#endif

typedef struct kaa_status_t             kaa_status_t;
typedef struct kaa_platform_protocol_t  kaa_platform_protocol_t;
typedef struct kaa_bootstrap_manager_t  kaa_bootstrap_manager_t;
typedef struct kaa_channel_manager_t    kaa_channel_manager_t;
typedef struct kaa_profile_manager_t    kaa_profile_manager_t;
typedef struct kaa_user_manager_t       kaa_user_manager_t;

#ifndef KAA_DISABLE_FEATURE_EVENTS
typedef struct kaa_event_manager_t      kaa_event_manager_t;
#endif

#ifndef KAA_DISABLE_FEATURE_LOGGING
typedef struct kaa_log_collector        kaa_log_collector_t;
#endif

typedef struct kaa_logger_t             kaa_logger_t;



typedef struct {
    kaa_status_t               *status;
    kaa_platform_protocol_t    *platfrom_protocol;
    kaa_bootstrap_manager_t    *bootstrap_manager;
    kaa_channel_manager_t      *channel_manager;
    kaa_profile_manager_t      *profile_manager;
    kaa_user_manager_t         *user_manager;
#ifndef KAA_DISABLE_FEATURE_EVENTS
    kaa_event_manager_t        *event_manager;
#endif
#ifndef KAA_DISABLE_FEATURE_LOGGING
    kaa_log_collector_t        *log_collector;
#endif
    kaa_logger_t               *logger;
} kaa_context_t;

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_CONTEXT_H_ */

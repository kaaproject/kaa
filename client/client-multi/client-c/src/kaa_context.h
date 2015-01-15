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
 * @brief Kaa endpoint context definition
 *
 * Defines the general Kaa endpoint context.
 */

#ifndef KAA_CONTEXT_H_
#define KAA_CONTEXT_H_

#ifdef __cplusplus
extern "C" {
#endif

#include "utilities/kaa_log.h"
#include "kaa_status.h"
#include "kaa_event.h"
#include "kaa_profile.h"
#include "kaa_logging.h"
#include "kaa_user.h"

//typedef struct kaa_status_t             kaa_status_t;
typedef struct kaa_platform_protocol_t  kaa_platform_protocol_t;
typedef struct kaa_bootstrap_manager_t  kaa_bootstrap_manager_t;
typedef struct kaa_channel_manager_t    kaa_channel_manager_t;
//typedef struct kaa_profile_manager_t    kaa_profile_manager_t;
//typedef struct kaa_user_manager_t       kaa_user_manager_t;

#ifndef KAA_DISABLE_FEATURE_EVENTS
//typedef struct kaa_event_manager_t      kaa_event_manager_t;
#endif

#ifndef KAA_DISABLE_FEATURE_LOGGING
//typedef struct kaa_log_collector        kaa_log_collector_t;
#endif

//typedef struct kaa_logger_t             kaa_logger_t;



/**
 * General Kaa endpoint context. Contains private structures of all Kaa endpoint SDK subsystems that can be used
 * independently to perform API calls to specific subsystems.
 */
typedef struct {
    kaa_status_t               *status;             /**< See @link kaa_status.h @endlink. */
    kaa_platform_protocol_t    *platfrom_protocol;  /**< See @link kaa_platform_protocol.h @endlink. */
    kaa_bootstrap_manager_t    *bootstrap_manager;  /**< See @link kaa_bootstrap.h @endlink. */
    kaa_channel_manager_t      *channel_manager;    /**< See @link kaa_channel_manager.h @endlink. */
    kaa_profile_manager_t      *profile_manager;    /**< See @link kaa_profile.h @endlink. */
    kaa_user_manager_t         *user_manager;       /**< See @link kaa_user.h @endlink. */
#ifndef KAA_DISABLE_FEATURE_EVENTS
    kaa_event_manager_t        *event_manager;      /**< See @link kaa_event.h @endlink. */
#endif
#ifndef KAA_DISABLE_FEATURE_LOGGING
    kaa_log_collector_t        *log_collector;      /**< See @link kaa_logging.h @endlink. */
#endif
    kaa_logger_t               *logger;             /**< See @link kaa_log.h @endlink. */
} kaa_context_t;

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_CONTEXT_H_ */

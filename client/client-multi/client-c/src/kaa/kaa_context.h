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

// Forward-declare
struct kaa_status_t;
struct kaa_platform_protocol_t;
struct kaa_status_holder_t;
struct kaa_bootstrap_manager_t;
struct kaa_channel_manager_t;
struct kaa_profile_manager_t;
struct kaa_user_manager_t;
struct kaa_event_manager_t;
struct kaa_log_collector;
struct kaa_configuration_manager;
struct kaa_notification_manager_t;
struct kaa_logger_t;
struct kaa_failover_strategy_t;

/**
 * General Kaa endpoint context. Contains private structures of all Kaa endpoint SDK subsystems that can be used
 * independently to perform API calls to specific subsystems.
 */
typedef struct kaa_context_s {
    struct kaa_status_holder_t         *status;                 /**< See @link kaa_status.h @endlink. */
    struct kaa_platform_protocol_t     *platform_protocol;      /**< See @link kaa_platform_protocol.h @endlink. */
    struct kaa_bootstrap_manager_t     *bootstrap_manager;      /**< See @link kaa_bootstrap_manager.h @endlink. */
    struct kaa_channel_manager_t       *channel_manager;        /**< See @link kaa_channel_manager.h @endlink. */
    struct kaa_profile_manager_t       *profile_manager;        /**< See @link kaa_profile.h @endlink. */
    struct kaa_user_manager_t          *user_manager;           /**< See @link kaa_user.h @endlink. */
    struct kaa_event_manager_t         *event_manager;          /**< See @link kaa_event.h @endlink. */
    struct kaa_log_collector_t         *log_collector;          /**< See @link kaa_logging.h @endlink. */
    struct kaa_configuration_manager_t *configuration_manager;  /**< See @link kaa_configuration_manager.h @endlink. */
    struct kaa_logger_t                *logger;                 /**< See @link kaa_log.h @endlink. */
    struct kaa_notification_manager_t  *notification_manager;   /**< See @link kaa_notification_manager.h @endlink. */
    struct kaa_failover_strategy_t     *failover_strategy;
} kaa_context_t;

#ifdef __cplusplus
}      /* extern "C" */
#endif
#endif /* KAA_CONTEXT_H_ */

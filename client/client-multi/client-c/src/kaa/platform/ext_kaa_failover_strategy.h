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

#ifndef EXT_FAILOVER_STRATEGY_H_
#define EXT_FAILOVER_STRATEGY_H_

#include <stddef.h>
#include "kaa_error.h"
#include <platform/time.h>

#define KAA_FAILOVER_RETRY_PERIOD     2
#define KAA_BOOTSTRAP_RESPONSE_PERIOD 5

typedef enum {
    KAA_NOOP = 0,
    KAA_RETRY,
    KAA_USE_NEXT_BOOTSTRAP,
    KAA_USE_NEXT_OPERATIONS,
    KAA_STOP_APP
} kaa_failover_strategy_action_t;

typedef enum {
    KAA_CHANNEL_NA = 0,
    KAA_BOOTSTRAP_SERVERS_NA,
    KAA_OPERATION_SERVERS_NA,
    KAA_NO_OPERATION_SERVERS_RECEIVED,
    KAA_NO_CONNECTIVITY,
    KAA_ENDPOINT_NOT_REGISTERED,
    KAA_CREDENTIALS_REVOKED,

} kaa_failover_reason;

typedef struct {
    kaa_failover_strategy_action_t action;
    kaa_time_t retry_period;
} kaa_failover_decision_t;

struct kaa_failover_strategy_t;
typedef struct kaa_failover_strategy_t kaa_failover_strategy_t;

/**
* @brief Returns the decision, depending on the failover reason.
*
* @param[in]   self         Pointer to the failover strategy instance.
* @param[in]   reason       Reason that caused failover strategy to be executed.
*
* @return kaa_failover_decision_t
*/
kaa_failover_decision_t kaa_failover_strategy_on_failover(void *self, kaa_failover_reason reason);

#endif /* EXT_FAILOVER_STRATEGY_H_ */

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

#include <time.h>
#include <stdint.h>
#include <stdio.h>
#include "kaa_common.h"
#include "utilities/kaa_mem.h"
#include "utilities/kaa_log.h"
#include "kaa_bootstrap_manager.h"
#include "platform/ext_kaa_failover_strategy.h"
#include "kaa_protocols/kaa_tcp/kaatcp_common.h"
#include "platform/ext_kaa_failover_strategy.h"
#include "kaa_context.h"


struct kaa_failover_strategy_t {
    kaa_failover_decision_t decision;
    kaa_logger_t            *logger;
};

kaa_error_t kaa_failover_strategy_create(kaa_failover_strategy_t** strategy, kaa_logger_t *logger)
{
    KAA_RETURN_IF_NIL2(strategy, logger, KAA_ERR_BADPARAM);
    *strategy = (kaa_failover_strategy_t *) KAA_MALLOC(sizeof(kaa_failover_strategy_t));
    KAA_RETURN_IF_NIL(*strategy, KAA_ERR_NOMEM);
    (*strategy)->decision.action = KAA_USE_NEXT_BOOTSTRAP;
    (*strategy)->decision.retry_period = KAA_FAILOVER_RETRY_PERIOD;
    (*strategy)->logger = logger;

    return KAA_ERR_NONE;
}

void kaa_failover_strategy_destroy(kaa_failover_strategy_t* strategy)
{
    KAA_RETURN_IF_NIL(strategy,);
    KAA_FREE(strategy);
}

kaa_failover_decision_t kaa_failover_strategy_on_failover(void *self, kaa_failover_reason reason)
{
    kaa_failover_strategy_t *strategy = self;

    switch (reason) {
        case KAA_BOOTSTRAP_SERVERS_NA:
            strategy->decision.action = KAA_RETRY;
            break;

        case KAA_NO_OPERATION_SERVERS_RECEIVED:
            strategy->decision.action = KAA_USE_NEXT_BOOTSTRAP;
            break;

        case KAA_OPERATION_SERVERS_NA:
            strategy->decision.action = KAA_USE_NEXT_OPERATIONS;
            break;

        case KAA_NO_CONNECTIVITY:
            strategy->decision.action = KAA_RETRY;
            break;

        case KAA_CHANNEL_NA:
        case KAA_CREDENTIALS_REVOKED:
        case KAA_ENDPOINT_NOT_REGISTERED:
            strategy->decision.action = KAA_RETRY;
            break;

        default:
            strategy->decision.action = KAA_NOOP;
    }
    return strategy->decision;
}

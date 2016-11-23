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

#include "kaa/failover/DefaultFailoverStrategy.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

const std::size_t DefaultFailoverStrategy::DEFAULT_RETRY_PERIOD;

FailoverStrategyDecision DefaultFailoverStrategy::onFailover(KaaFailoverReason failover)
{
    FailoverStrategyAction action = FailoverStrategyAction::NOOP;

    switch (failover) {
        case KaaFailoverReason::ALL_BOOTSTRAP_SERVERS_NA:
        case KaaFailoverReason::CURRENT_BOOTSTRAP_SERVER_NA:
        case KaaFailoverReason::NO_OPERATIONS_SERVERS_RECEIVED:
        case KaaFailoverReason::ALL_OPERATIONS_SERVERS_NA:
            action = FailoverStrategyAction::USE_NEXT_BOOTSTRAP_SERVER;
            break;
        case KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA:
            action = FailoverStrategyAction::USE_NEXT_OPERATIONS_SERVER;
            break;
        case KaaFailoverReason::NO_CONNECTIVITY:
            action = FailoverStrategyAction::RETRY_CURRENT_SERVER;
            break;
        case KaaFailoverReason::ENDPOINT_NOT_REGISTERED:
            action = FailoverStrategyAction::RETRY_CURRENT_SERVER;
            break;
        case KaaFailoverReason::CREDENTIALS_REVOKED:
            action = FailoverStrategyAction::STOP_CLIENT;
            break;
        default:
            // Use NOOP
            break;
    }

    FailoverStrategyDecision decision(action, retryPeriod_);

    KAA_LOG_INFO(boost::format("Use '%s' decision for '%s' failover")
                                            % LoggingUtils::toString(decision)
                                            % LoggingUtils::toString(failover));

    return decision;
}

}

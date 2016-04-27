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

namespace kaa {

const std::size_t DefaultFailoverStrategy::DEFAULT_BOOTSTRAP_SERVERS_RETRY_PERIOD;
const std::size_t DefaultFailoverStrategy::DEFAULT_OPERATION_SERVERS_RETRY_PERIOD;
const std::size_t DefaultFailoverStrategy::DEFAULT_NO_OPERATION_SERVERS_RETRY_PERIOD;
const std::size_t DefaultFailoverStrategy::DEFAULT_CURRENT_BOOTSTRAP_SERVER_NA;
const std::size_t DefaultFailoverStrategy::DEFAULT_NO_CONNECTIVITY_RETRY_PERIOD;

FailoverStrategyDecision DefaultFailoverStrategy::onFailover(KaaFailoverReason failover)
{
        switch (failover) {
        case KaaFailoverReason::BOOTSTRAP_SERVERS_NA:
            return FailoverStrategyDecision(FailoverStrategyAction::RETRY,
                                            bootstrapServersRetryPeriod_);

        case KaaFailoverReason::NO_OPERATION_SERVERS_RECEIVED:
            return FailoverStrategyDecision(FailoverStrategyAction::USE_NEXT_BOOTSTRAP,
                                            noOperationServersRetryPeriod_);

        case KaaFailoverReason::OPERATION_SERVERS_NA:
            return FailoverStrategyDecision(FailoverStrategyAction::RETRY,
                                            operationServersRetryPeriod_);

        case KaaFailoverReason::NO_CONNECTIVITY:
            return FailoverStrategyDecision(FailoverStrategyAction::RETRY,
                                            noConnectivityRetryPeriod_);

        case KaaFailoverReason::CREDENTIALS_REVOKED:
        case KaaFailoverReason::ENDPOINT_NOT_REGISTERED:
            return FailoverStrategyDecision(FailoverStrategyAction::RETRY);

        default:
            return FailoverStrategyDecision(FailoverStrategyAction::NOOP);
        }
}

}

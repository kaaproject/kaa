/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#ifndef DEFAULTFAILOVERSTRATEGY_HPP_
#define DEFAULTFAILOVERSTRATEGY_HPP_

#include "kaa/failover/IFailoverStrategy.hpp"

namespace kaa {

class DefaultFailoverStrategy: public IFailoverStrategy {
public:

	virtual FailoverStrategyDecision onFailover(Failover failover);

public:
    static const std::size_t DEFAULT_BOOTSTRAP_SERVERS_RETRY_PERIOD = 2;

    static const std::size_t DEFAULT_OPERATION_SERVERS_RETRY_PERIOD = 2;

    static const std::size_t DEFAULT_NO_OPERATION_SERVERS_RETRY_PERIOD = 2;

    static const std::size_t DEFAULT_CURRENT_BOOTSTRAP_SERVER_NA = 2;

    static const std::size_t DEFAULT_NO_CONNECTIVITY_RETRY_PERIOD = 5;

private:
    std::size_t bootstrapServersRetryPeriod_   = DEFAULT_BOOTSTRAP_SERVERS_RETRY_PERIOD;
    std::size_t operationServersRetryPeriod_   = DEFAULT_OPERATION_SERVERS_RETRY_PERIOD;
    std::size_t noOperationServersRetryPeriod_ = DEFAULT_NO_OPERATION_SERVERS_RETRY_PERIOD;
    std::size_t noCurrentBootstrapServer_      = DEFAULT_CURRENT_BOOTSTRAP_SERVER_NA;
    std::size_t noConnectivityRetryPeriod_     = DEFAULT_NO_CONNECTIVITY_RETRY_PERIOD;
};

}

#endif /* DEFAULTFAILOVERSTRATEGY_HPP_ */

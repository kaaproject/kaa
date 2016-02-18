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

#ifndef IFAILOVERSTRATEGY_HPP_
#define IFAILOVERSTRATEGY_HPP_

#include <memory>
#include <cstdint>

namespace kaa {

enum class Failover {
    BOOTSTRAP_SERVERS_NA = 0, /*!< No accessible bootstrap servers. */
    NO_OPERATION_SERVERS_RECEIVED,
    OPERATION_SERVERS_NA,
    CURRENT_BOOTSTRAP_SERVER_NA,
    NO_CONNECTIVITY
};

enum class FailoverStrategyAction {
    NOOP = 0, /*!< Nothing to be done. */
    RETRY,    /*!< Initiate log upload. */
    USE_NEXT_BOOTSTRAP,
    USE_NEXT_OPERATIONS,
    STOP_APP
};

class FailoverStrategyDecision {

public:
	FailoverStrategyDecision(const FailoverStrategyAction& action, const std::int32_t& retryPeriod)
        : action_(action), retryPeriod_(retryPeriod) {}

	FailoverStrategyDecision(const FailoverStrategyAction& action)
        : action_(action), retryPeriod_(0) {}

	FailoverStrategyAction getAction() const {
        return action_;
    }

    std::size_t getRetryPeriod() const {
        return retryPeriod_;
    }

private:
	FailoverStrategyAction action_;
	std::size_t retryPeriod_;
};

class IFailoverStrategy {
public:

	virtual FailoverStrategyDecision onFailover(Failover failover) = 0;

	virtual ~IFailoverStrategy() {}

};

typedef std::shared_ptr<IFailoverStrategy> IFailoverStrategyPtr;

}

#endif /* IFAILOVERSTRATEGY_HPP_ */

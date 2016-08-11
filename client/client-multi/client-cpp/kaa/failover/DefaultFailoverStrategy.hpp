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

#ifndef DEFAULTFAILOVERSTRATEGY_HPP_
#define DEFAULTFAILOVERSTRATEGY_HPP_

#include <cstddef>

#include "kaa/failover/IFailoverStrategy.hpp"

namespace kaa {

class IKaaClientContext;

class DefaultFailoverStrategy: public IFailoverStrategy {
public:
    DefaultFailoverStrategy(IKaaClientContext& context, std::size_t retryPeriod = DEFAULT_RETRY_PERIOD)
        : context_(context), retryPeriod_(retryPeriod)
    {

    }

    virtual FailoverStrategyDecision onFailover(KaaFailoverReason failover);

public:
    static const std::size_t DEFAULT_RETRY_PERIOD = 5;

protected:
    IKaaClientContext &context_;

    std::size_t retryPeriod_ = DEFAULT_RETRY_PERIOD;
};

}

#endif /* DEFAULTFAILOVERSTRATEGY_HPP_ */

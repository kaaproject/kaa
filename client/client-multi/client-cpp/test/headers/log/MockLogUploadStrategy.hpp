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

#ifndef MOCKLOGUPLOADSTRATEGY_HPP_
#define MOCKLOGUPLOADSTRATEGY_HPP_

#include <cstdint>

#include "kaa/log/ILogUploadStrategy.hpp"
#include "kaa/log/ILogFailoverCommand.hpp"

namespace kaa {

class MockLogUploadStrategy: public ILogUploadStrategy {
public:
    virtual LogUploadStrategyDecision isUploadNeeded(ILogStorageStatus& status) { ++onIsUploadNeeded_; return decision_; }

    virtual std::size_t getTimeout() { ++onGetTimeout_; return timeout_; }

    virtual void onTimeout(ILogFailoverCommand& controller) {
        controller.switchAccessPoint();
        ++onTimeout_;
    }

    virtual void onFailure(ILogFailoverCommand& controller, LogDeliveryErrorCode code) { ++onFailure_; }

    virtual std::size_t getTimeoutCheckPeriod() { ++onGetTimeoutCheckPeriod_ ; return timeoutCheckPeriod_; }
    virtual std::size_t getLogUploadCheckPeriod() { ++onGetUploadCheckPeriod_; return logUploadCheckPeriod_; }
    virtual std::size_t getMaxParallelUploads()  { ++onGetMaxParallelUploads_; return maxParallelUploads_; }

public:
    LogUploadStrategyDecision decision_ = LogUploadStrategyDecision::NOOP;
    std::size_t timeout_ = 0;
    std::size_t timeoutCheckPeriod_ = 0;
    std::size_t logUploadCheckPeriod_ = 0;
    std::size_t retryTimeout_ = 0;
    std::size_t maxParallelUploads_ = 0;

    std::size_t onIsUploadNeeded_ = 0;
    std::size_t onGetTimeout_ = 0;
    std::size_t onTimeout_ = 0;
    std::size_t onFailure_ = 0;
    std::size_t onGetTimeoutCheckPeriod_ = 0;
    std::size_t onGetUploadCheckPeriod_ = 0;
    std::size_t onGetMaxParallelUploads_ = 0;
};

} /* namespace kaa */

#endif /* MOCKLOGUPLOADSTRATEGY_HPP_ */

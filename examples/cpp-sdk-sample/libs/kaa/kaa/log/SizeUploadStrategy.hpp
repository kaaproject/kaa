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

#ifndef SIZEUPLOADSTRATEGY_HPP_
#define SIZEUPLOADSTRATEGY_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_LOGGING

#include "kaa/log/ILogUploadStrategy.hpp"

namespace kaa {

class SizeUploadStrategy : public ILogUploadStrategy {
public:
    SizeUploadStrategy() {}

    LogUploadStrategyDecision isUploadNeeded(const ILogUploadConfiguration* configuration, const ILogStorageStatus* status)
    {
        if (configuration != nullptr && status != nullptr) {
            if (status->getConsumedVolume() >= configuration->getMaxStorageVolume()) {
                return LogUploadStrategyDecision::CLEANUP;
            } else if (status->getConsumedVolume() >= configuration->getVolumeThreshold()) {
                return LogUploadStrategyDecision::UPLOAD;
            }
        }
        return LogUploadStrategyDecision::NOOP;
    }
};

}  // namespace kaa

#endif

#endif /* SIZEUPLOADSTRATEGY_HPP_ */

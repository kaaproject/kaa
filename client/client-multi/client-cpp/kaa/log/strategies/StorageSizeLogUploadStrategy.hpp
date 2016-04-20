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


#ifndef STORAGESIZELOGUPLOADSTRATEGY_HPP_
#define STORAGESIZELOGUPLOADSTRATEGY_HPP_

#include <cstdlib>

#include "kaa/logging/Log.hpp"
#include "kaa/log/ILogStorageStatus.hpp"
#include "kaa/log/DefaultLogUploadStrategy.hpp"

namespace kaa {

class StorageSizeLogUploadStrategy : public DefaultLogUploadStrategy {
public:
    StorageSizeLogUploadStrategy(std::size_t volumeThreshold, IKaaClientContext &context)
        : DefaultLogUploadStrategy(context)
    {
        setVolumeThreshold(volumeThreshold);
    }

    virtual LogUploadStrategyDecision isUploadNeeded(ILogStorageStatus& status) override
    {
        auto currentConsumedVolume =  status.getConsumedVolume();

        if (currentConsumedVolume >= uploadVolumeThreshold_) {
            KAA_LOG_INFO(boost::format("Need to upload logs - current size: %llu, threshold: %llu")
                                                    % currentConsumedVolume % uploadVolumeThreshold_);
            return LogUploadStrategyDecision::UPLOAD;
        }

        return LogUploadStrategyDecision::NOOP;
    }
};

}

#endif /* STORAGESIZELOGUPLOADSTRATEGY_HPP_ */

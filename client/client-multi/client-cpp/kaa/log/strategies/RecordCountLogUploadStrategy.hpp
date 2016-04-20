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


#ifndef RECORDCOUNTLOGUPLOADSTRATEGY_HPP_
#define RECORDCOUNTLOGUPLOADSTRATEGY_HPP_

#include <cstdlib>

#include "kaa/logging/Log.hpp"
#include "kaa/log/ILogStorageStatus.hpp"
#include "kaa/log/DefaultLogUploadStrategy.hpp"

namespace kaa {

class RecordCountLogUploadStrategy : public DefaultLogUploadStrategy {
public:
    RecordCountLogUploadStrategy(std::size_t countThreshold, IKaaClientContext &context)
        : DefaultLogUploadStrategy(context)
    {
        setCountThreshold(countThreshold);
    }

    virtual LogUploadStrategyDecision isUploadNeeded(ILogStorageStatus& status) override
    {
        auto currentRecordCount = status.getRecordsCount();

        if (currentRecordCount >= uploadCountThreshold_) {
            KAA_LOG_INFO(boost::format("Need to upload logs - current count: %llu, threshold: %llu")
                                                    % currentRecordCount % uploadCountThreshold_);
            return LogUploadStrategyDecision::UPLOAD;
        }

        return LogUploadStrategyDecision::NOOP;
    }
};

}

#endif /* RECORDCOUNTLOGUPLOADSTRATEGY_HPP_ */

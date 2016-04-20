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

#include "kaa/log/DefaultLogUploadStrategy.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/log/ILogStorageStatus.hpp"
#include "kaa/log/ILogFailoverCommand.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

const std::size_t DefaultLogUploadStrategy::DEFAULT_UPLOAD_TIMEOUT;
const std::size_t DefaultLogUploadStrategy::DEFAULT_RETRY_PERIOD;
const std::size_t DefaultLogUploadStrategy::DEFAULT_TIMEOUT_CHECK_PERIOD;
const std::size_t DefaultLogUploadStrategy::DEFAULT_LOG_UPLOAD_CHECK_PERIOD;

const std::size_t DefaultLogUploadStrategy::DEFAULT_UPLOAD_VOLUME_THRESHOLD;
const std::size_t DefaultLogUploadStrategy::DEFAULT_UPLOAD_COUNT_THRESHOLD;

const std::size_t DefaultLogUploadStrategy::DEFAULT_MAX_PARALLEL_UPLOADS;

LogUploadStrategyDecision DefaultLogUploadStrategy::isUploadNeeded(ILogStorageStatus& status)
{
    LogUploadStrategyDecision decision = LogUploadStrategyDecision::NOOP;

    if (nextUploadAttemptTS_.time_since_epoch().count()) {
        if (Clock::now() >= nextUploadAttemptTS_) {
            KAA_LOG_INFO("Retry log upload after failure");
            nextUploadAttemptTS_ = std::chrono::time_point<Clock>();
            decision = LogUploadStrategyDecision::UPLOAD;
        }
        return decision;
    }

    if (status.getConsumedVolume() >= uploadVolumeThreshold_) {
        KAA_LOG_INFO(boost::format("Need to upload logs - current size: %1%, max: %2%")
                                        % status.getConsumedVolume() % uploadVolumeThreshold_);
        decision = LogUploadStrategyDecision::UPLOAD;
    } else if (status.getRecordsCount() >= uploadCountThreshold_) {
        KAA_LOG_INFO(boost::format("Need to upload logs - current record count: %1%, max: %2%")
                                        % status.getRecordsCount() % uploadCountThreshold_);
        decision = LogUploadStrategyDecision::UPLOAD;
    }

    return decision;
}

void DefaultLogUploadStrategy::onTimeout(ILogFailoverCommand& controller)
{
    KAA_LOG_WARN("Log upload timeout occurred.");

    controller.switchAccessPoint();
}

void DefaultLogUploadStrategy::onFailure(ILogFailoverCommand& controller, LogDeliveryErrorCode code)
{
    switch (code) {
        case LogDeliveryErrorCode::NO_APPENDERS_CONFIGURED:
        case LogDeliveryErrorCode::APPENDER_INTERNAL_ERROR:
        case LogDeliveryErrorCode::REMOTE_CONNECTION_ERROR:
        case LogDeliveryErrorCode::REMOTE_INTERNAL_ERROR:
            KAA_LOG_WARN(boost::format("Log upload failed with error code %1%. Retry upload after %2% seconds")
                                                                                           % code % retryReriod_);
            nextUploadAttemptTS_ = std::chrono::system_clock::now() + std::chrono::seconds(retryReriod_);
            controller.retryLogUpload(retryReriod_);
            break;
        default:
            break;
    }
}

} /* namespace kaa */

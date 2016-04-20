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

#ifndef DEFAULTLOGUPLOADSTRATEGY_HPP_
#define DEFAULTLOGUPLOADSTRATEGY_HPP_

#include <chrono>
#include <cstdint>

#include "kaa/log/ILogUploadStrategy.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

/**
 * @brief The default @c ILogUploadStrategy implementation.
 *
 * The decision algorithm depends on the log delivery status:
 *
 * 1. The normal work flow (the Operations server successfully receives logs).
 *
 * The @c LogUploadStrategyDecision::UPLOAD decision applies in two cases:
 * 1) the consumed volume of the log storage is equal or greater than the corresponding value, specified
 * in the strategy via @link setVolumeThreshold() @endlink (@link DEFAULT_UPLOAD_VOLUME_THRESHOLD @endlink is used
 * by default);
 * 2) the number of collected logs is equal or greater than the corresponding value, specified in the strategy
 * via @link setCountThreshold() @endlink (@link DEFAULT_UPLOAD_COUNT_THRESHOLD @endlink is used by default);
 *
 * 2. The delivery error.
 *
 * If one of @c LogDeliveryErrorCode errors has occurred, the @c LogUploadStrategyDecision::NOOP will apply until
 * the retry period, specified via @link setRetryPeriod @endlink (@link DEFAULT_RETRY_PERIOD @endlink is used
 * by default) will have elapsed.
 *
 * 3. The delivery timeout.
 *
 * In this case the strategy tries to switch to the next transport channel supported the logging feature.
 */
class DefaultLogUploadStrategy: public ILogUploadStrategy {
public:
    DefaultLogUploadStrategy(IKaaClientContext &context): context_(context) {}

    virtual LogUploadStrategyDecision isUploadNeeded(ILogStorageStatus& status);

    virtual void onTimeout(ILogFailoverCommand& controller);
    virtual void onFailure(ILogFailoverCommand& controller, LogDeliveryErrorCode code);

    virtual std::size_t getTimeout() { return uploadTimeout_; }
    void setUploadTimeout(std::size_t timeout) { uploadTimeout_ = timeout; }

    virtual std::size_t getTimeoutCheckPeriod() { return timeoutCheckPeriod_; }
    void setTimeoutCheckPeriod(std::size_t period) { timeoutCheckPeriod_ = period; }

    virtual std::size_t getLogUploadCheckPeriod() { return logUploadCheckReriod_;  };
    void setLogUploadCheckPeriod(std::size_t period) { logUploadCheckReriod_ = period; }

    virtual std::size_t getMaxParallelUploads() { return maxParallelUploads_; }
    void setMaxParallelUploads(std::size_t count) { maxParallelUploads_ = count; }

    std::size_t getRetryPeriod() { return retryReriod_; }
    void setRetryPeriod(std::size_t period) { retryReriod_ = period; }

    std::size_t getVolumeThreshold() const { return uploadVolumeThreshold_; }
    void setVolumeThreshold(std::size_t maxVolume) { uploadVolumeThreshold_ = maxVolume; }

    std::size_t getCountThreshold() const { return uploadCountThreshold_; }
    void setCountThreshold(std::size_t maxCount) { uploadCountThreshold_ = maxCount; }

public:
    static const std::size_t DEFAULT_UPLOAD_TIMEOUT = 2 * 60; /*!< The default value (in seconds) for time to wait
                                                                   the log delivery response. */

    static const std::size_t DEFAULT_TIMEOUT_CHECK_PERIOD = 10;

    static const std::size_t DEFAULT_LOG_UPLOAD_CHECK_PERIOD = 30;

    static const std::size_t DEFAULT_RETRY_PERIOD = 5 * 60; /*!< The default value (in seconds) for time to postpone
                                                                 log upload. */

    static const std::size_t DEFAULT_UPLOAD_VOLUME_THRESHOLD = 8 * 1024; /*!< The default value (in bytes) for log
                                                                              volume to initiate the log upload. */

    static const std::size_t DEFAULT_UPLOAD_COUNT_THRESHOLD = 64; /*!< The default value for the log count to initiate
                                                                       the log upload. */

    static const std::size_t DEFAULT_MAX_PARALLEL_UPLOADS = INT32_MAX;  /*!< The default value for Max amount of log batches
                                                                             allowed to be uploaded parallel. */

protected:
    std::size_t uploadTimeout_ = DEFAULT_UPLOAD_TIMEOUT;
    std::size_t retryReriod_ = DEFAULT_RETRY_PERIOD;

    std::size_t timeoutCheckPeriod_ = DEFAULT_TIMEOUT_CHECK_PERIOD;

    std::size_t logUploadCheckReriod_ = DEFAULT_LOG_UPLOAD_CHECK_PERIOD;

    std::size_t uploadVolumeThreshold_ = DEFAULT_UPLOAD_VOLUME_THRESHOLD;
    std::size_t uploadCountThreshold_ = DEFAULT_UPLOAD_COUNT_THRESHOLD;

    std::size_t maxParallelUploads_ = DEFAULT_MAX_PARALLEL_UPLOADS;

    IKaaClientContext &context_;

private:
    typedef std::chrono::system_clock Clock;
    std::chrono::time_point<Clock> nextUploadAttemptTS_;
};

} /* namespace kaa */

#endif /* DEFAULTLOGUPLOADSTRATEGY_HPP_ */

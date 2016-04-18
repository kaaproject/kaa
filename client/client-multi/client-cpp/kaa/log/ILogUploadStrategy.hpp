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

#ifndef ILOGUPLOADSTRATEGY_HPP_
#define ILOGUPLOADSTRATEGY_HPP_

#include <memory>
#include <cstdint>

#include "kaa/gen/EndpointGen.hpp"

namespace kaa {

/*
 * Forward declaration.
 */
class ILogStorageStatus;
class ILogFailoverCommand;

/**
 * @brief Log upload decisions.
 */
enum class LogUploadStrategyDecision {
    NOOP = 0, /*!< Nothing to be done. */
    UPLOAD    /*!< Initiate log upload. */
};

/**
 * @brief The public interface for the log upload strategy.
 *
 * The default implementation can be found in @c DefaultLogUploadStrategy.
 */
class ILogUploadStrategy {
public:
    /**
     * @brief Decides whether the log upload is needed.
     *
     * The decision is made based on the current log storage status and, depending on the strategy implementation,
     * on some additional information.
     *
     * @param[in] status    The log storage status.
     *
     * @return    The log upload decision.
     *
     * @see ILogStorageStatus
     * @see LogUploadStrategyDecision
     */
    virtual LogUploadStrategyDecision isUploadNeeded(ILogStorageStatus& status) = 0;

    /**
     * @brief Maximum time to wait the log delivery response.
     *
     * @param[in] controller
     *
     * @return    Time in seconds.
     */
    virtual std::size_t getTimeout() = 0;

    virtual std::size_t getTimeoutCheckPeriod() = 0;

    virtual std::size_t getLogUploadCheckPeriod() = 0;

    /**
     * @brief Max amount of log batches allowed to be uploaded parallel.
     *
     * @return Amount of batches.
     */
    virtual std::size_t getMaxParallelUploads() = 0;

    /**
     * @brief Callback is used when the log delivery timeout detected.
     *
     * More information about the detection of the log delivery timeout read in the documentation for @c ILogCollector.
     */
    virtual void onTimeout(ILogFailoverCommand& controller) = 0;

    /**
     * @brief Callback is used when the log delivery is failed.
     *
     * @param[in] controller
     * @param[in] code    The reason code of the log delivery failure.
     *
     * @see LogDeliveryErrorCode
     */
    virtual void onFailure(ILogFailoverCommand& controller, LogDeliveryErrorCode code) = 0;

    virtual ~ILogUploadStrategy() {}
};


/**
 * @typedef The shared pointer to @c ILogUploadStrategy.
 */
typedef std::shared_ptr<ILogUploadStrategy> ILogUploadStrategyPtr;

}  // namespace kaa

#endif /* ILOGUPLOADSTRATEGY_HPP_ */

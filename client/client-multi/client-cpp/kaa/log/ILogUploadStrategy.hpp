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

#ifndef ILOGUPLOADSTRATEGY_HPP_
#define ILOGUPLOADSTRATEGY_HPP_

#include <memory>
#include <cstdint>

#include "kaa/gen/EndpointGen.hpp"

namespace kaa {

class ILogStorageStatus;

/**
 * Enumeration of available decisions of log storage modifications.
 */
enum class LogUploadStrategyDecision {
    NOOP = 0, /*!< Nothing to be done */
    UPLOAD /*!< Start uploading */
};

/**
 * Interface for determination if upload is needed.
 */
class ILogUploadStrategy {
public:
    /**
     * Retrieves log upload decision based on current storage status and defined
     * upload configuration.
     *
     * @param status
     *            Log storage status
     *
     * @return Upload decision ({@link LogUploadStrategyDecision})
     */
    virtual LogUploadStrategyDecision isUploadNeeded(ILogStorageStatus& status) = 0;

    /**
     * Retrieves maximum size of the report pack
     * that will be delivered in single request to server
     * @return size of the batch
     */
    virtual std::size_t getBatchSize() = 0;

    /**
     * Maximum time to wait log delivery response.
     *
     * @return Time in seconds.
     */
    virtual std::size_t getTimeout() = 0;

    /**
     * Handles timeout of log delivery
     * @param controller
     */
    virtual void onTimeout() = 0;

    /**
     * Handles failure of log delivery
     * @param controller
     */
    virtual void onFailure(LogDeliveryErrorCode code) = 0;

    virtual ~ILogUploadStrategy() {}
};

typedef std::shared_ptr<ILogUploadStrategy> ILogUploadStrategyPtr;

}  // namespace kaa

#endif /* ILOGUPLOADSTRATEGY_HPP_ */

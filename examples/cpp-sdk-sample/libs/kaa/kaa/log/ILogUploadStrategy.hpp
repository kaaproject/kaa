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

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_LOGGING

#include "kaa/log/ILogUploadConfiguration.hpp"
#include "kaa/log/ILogStorageStatus.hpp"

namespace kaa {

/**
 * Enumeration of available decisions of log storage modifications.
 */
enum LogUploadStrategyDecision {
    /** Nothing to be done */
    NOOP = 0,
    /** Start uploading */
    UPLOAD,
    /** Release space */
    CLEANUP,
};

/**
 * Interface for determination if upload is needed.
 */
class ILogUploadStrategy {
public:
    /**
     * Checks if log upload should be triggered.
     * Called when each log record is produced or log upload parameters are changed
     *
     * \param configuration Current log upload configuration.
     * \param status        Log storage status.
     *
     * \return  \see LogUploadStrategyDecision.
     */
    virtual LogUploadStrategyDecision isUploadNeeded(const ILogUploadConfiguration* configuration, const ILogStorageStatus* status) = 0;

    virtual ~ILogUploadStrategy() {}
};

}  // namespace kaa

#endif

#endif /* ILOGUPLOADSTRATEGY_HPP_ */

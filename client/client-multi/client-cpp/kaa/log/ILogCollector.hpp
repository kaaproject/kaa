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

#ifndef ILOGCOLLECTOR_HPP_
#define ILOGCOLLECTOR_HPP_

#ifdef KAA_USE_LOGGING

#include "kaa/KaaDefaults.hpp"
#include "kaa/log/gen/LogGen.hpp"

namespace kaa {

class ILogStorage;
class ILogUploadConfiguration;
class ILogUploadStrategy;
class ILogUploadFailoverStrategy;
class ILogStorageStatus;
struct LogSyncResponse;

/**
 * Public interface for accessing Kaa Log Subsystem.
 */
class ILogCollector {
public:

    /**
     * Adds new log record to a storage.
     *
     * \param   record  log record to be added.
     */
    virtual void addLogRecord(const SuperRecord& record) = 0;

    /**
     * Provide specific Log storage.
     *
     * \param   storage \c ILogStorage implementation.
     */
    virtual void setStorage(ILogStorage * storage) = 0;

    /**
     * Provide specific log upload configurations used by \c ILogUploadStrategy
     *
     * \param   storage \c ILogUploadConfiguration implementation.
     */
    virtual void setConfiguration(ILogUploadConfiguration * configuration) = 0;

    /**
     * Provide specific strategy to determine if log upload is needed.
     *
     * \param   strategy    \c ILogUploadStrategy implementation.
     */
    virtual void setUploadStrategy(ILogUploadStrategy * strategy) = 0;

    /**
     * Provide specific strategy to determine what to do in case of a log upload failover.
     *
     * \param   strategy    \c ILogUploadFailoverStrategy implementation.
     */
    virtual void setFailoverStrategy(ILogUploadFailoverStrategy * strategy) = 0;

    /**
     * Provide object having information about current log storage state.
     *
     * \param   status  \c ILogStorageStatus implementation.
     */
    virtual void setStorageStatus(ILogStorageStatus * status) = 0;

    /**
     * Called when log upload response arrived.
     *
     * \param   response    Response from operations server.
     */
    virtual void onLogUploadResponse(const LogSyncResponse& response) = 0;

    virtual ~ILogCollector()
    {
    }
};

}  // namespace kaa

#endif

#endif /* ILOGCOLLECTOR_HPP_ */


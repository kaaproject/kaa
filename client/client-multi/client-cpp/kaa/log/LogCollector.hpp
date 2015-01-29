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

#ifndef LOGCOLLECTOR_HPP_
#define LOGCOLLECTOR_HPP_

#ifdef KAA_USE_LOGGING

#include "kaa/KaaDefaults.hpp"
#include "kaa/log/ILogCollector.hpp"

#include <chrono>
#include <memory>
#include <unordered_map>

#include "kaa/KaaThread.hpp"
#include "kaa/log/LoggingTransport.hpp"
#include "kaa/log/MemoryLogStorage.hpp"
#include "kaa/log/SizeUploadStrategy.hpp"
#include "kaa/log/LogUploadFailoverStrategy.hpp"
#include "kaa/log/DefaultLogUploadConfiguration.hpp"

namespace kaa {

/**
 * Default \c ILogCollector implementation.
 */
class LogCollector : public ILogCollector {
public:
    LogCollector(IKaaChannelManagerPtr manager);
    LogCollector(ILogStorage* storage
               , ILogStorageStatus* status
               , ILogUploadConfiguration* configuration
               , ILogUploadStrategy* strategy
               , ILogUploadFailoverStrategy* failoverStrategy);

    virtual void addLogRecord(const SuperRecord& record) {
        makeLogRecord(LogRecord(record));
    }

    virtual void setStorage(ILogStorage * storage);
    virtual void setStorageStatus(ILogStorageStatus * status);
    virtual void setConfiguration(ILogUploadConfiguration * configuration);
    virtual void setUploadStrategy(ILogUploadStrategy * strategy);
    virtual void setFailoverStrategy(ILogUploadFailoverStrategy * strategy);

    LogSyncRequest getLogUploadRequest();
    virtual void onLogUploadResponse(const LogSyncResponse& response);

    void setTransport(LoggingTransport *transport);

    ~LogCollector() {}

private:
    void makeLogRecord(const LogRecord& record);
    void doUpload();
    void doCleanup();

    bool isDeliveryTimeout();

private:
    ILogStorage *                  storage_;
    ILogStorageStatus *            status_;
    ILogUploadConfiguration *      configuration_;
    ILogUploadStrategy *           uploadStrategy_;
    ILogUploadFailoverStrategy *   failoverStrategy_;

    LoggingTransport*              transport_;

    std::map<std::int32_t, LogSyncRequest> requests_;

    KAA_MUTEX_DECLARE(storageGuard_);
    KAA_MUTEX_DECLARE(requestsGuard_);
    bool                        isUploading_;

    std::unique_ptr<MemoryLogStorage>                 defaultLogStorage_;
    std::unique_ptr<SizeUploadStrategy>               defaultUploadStrategy_;
    std::unique_ptr<LogUploadFailoverStrategy>        defaultFailoverStrategy_;
    std::unique_ptr<DefaultLogUploadConfiguration>    defaultConfiguration_;

    typedef std::chrono::system_clock clock_t;
    std::unordered_map<std::string, std::chrono::time_point<clock_t>> timeoutsMap_;
};

}  // namespace kaa

#endif

#endif /* LOGCOLLECTOR_HPP_ */

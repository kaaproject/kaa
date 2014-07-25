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

#include "kaa/log/ILogCollector.hpp"

#include <boost/thread/mutex.hpp>
#include <boost/scoped_ptr.hpp>
#include "kaa/log/MemoryLogStorage.hpp"
#include "kaa/log/SizeUploadStrategy.hpp"
#include "kaa/log/DefaultLogUploadConfiguration.hpp"
#include "kaa/log/LoggingTransport.hpp"

namespace kaa {

/**
 * Default \c ILogCollector implementation.
 */
class LogCollector : public ILogCollector {
public:
    LogCollector();
    LogCollector(ILogStorage* storage, ILogStorageStatus* status, ILogUploadConfiguration* configuration, ILogUploadStrategy* strategy);

    void addLogRecord(const SuperRecord& record) {
        makeLogRecord(LogRecord(record));
    }

    void setStorage(ILogStorage * storage);
    void setStorageStatus(ILogStorageStatus * status);
    void setConfiguration(ILogUploadConfiguration * configuration);
    void setUploadStrategy(ILogUploadStrategy * strategy);

    LogSyncRequest getLogUploadRequest();
    void onLogUploadResponse(const LogSyncResponse& response);

    void setTransport(LoggingTransport *transport);

    ~LogCollector() {}

private:
    void makeLogRecord(const LogRecord& record);
    void doUpload();
    void doCleanup();
private:
    typedef boost::mutex                    mutex_type;
    typedef boost::unique_lock<mutex_type>  lock_type;

    ILogStorage *               storage_;
    ILogStorageStatus *         status_;
    ILogUploadConfiguration *   configuration_;
    ILogUploadStrategy *        strategy_;

    LoggingTransport*           transport_;

    std::map<std::string, LogSyncRequest> requests_;

    mutex_type                  storageGuard_;
    mutex_type                  requestsGuard_;
    bool                        isUploading_;

    boost::scoped_ptr<MemoryLogStorage>                 defaultLogStorage_;
    boost::scoped_ptr<SizeUploadStrategy>               defaultUploadStrategy_;
    boost::scoped_ptr<DefaultLogUploadConfiguration>    defaultConfiguration_;
};

}  // namespace kaa

#endif /* LOGCOLLECTOR_HPP_ */

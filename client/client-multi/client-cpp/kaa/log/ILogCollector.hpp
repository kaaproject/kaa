/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#include "kaa/log/gen/LogGen.hpp"
#include "kaa/log/ILogStorage.hpp"
#include "kaa/log/ILogUploadStrategy.hpp"

namespace kaa {

struct LogSyncResponse;

typedef SuperRecord KaaUserLogRecord;

/**
 * Public interface for accessing Kaa Log Subsystem.
 */
class ILogCollector {
public:

    /**
     * Adds new log record to a storage.
     *
     * @param[in]   record  log record to be added.
     */
    virtual void addLogRecord(const KaaUserLogRecord& record) = 0;

    /**
     * Provide specific Log storage.
     *
     * @param[in]   storage @c ILogStorage implementation.
     */
    virtual void setStorage(ILogStoragePtr storage) = 0;

    /**
     * Provide specific strategy to determine if log upload is needed.
     *
     * @param[in]   strategy    @c ILogUploadStrategy implementation.
     */
    virtual void setUploadStrategy(ILogUploadStrategyPtr strategy) = 0;

    virtual ~ILogCollector() {}
};

}  // namespace kaa

#endif /* ILOGCOLLECTOR_HPP_ */


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

#ifndef ILOGCOLLECTOR_HPP_
#define ILOGCOLLECTOR_HPP_

#include <future>

#include "kaa/log/gen/LogDefinitions.hpp"
#include "kaa/log/ILogStorage.hpp"
#include "kaa/log/ILogUploadStrategy.hpp"
#include "kaa/log/ILogDeliveryListener.hpp"
#include "kaa/log/RecordFuture.hpp"

/**
 * @file ILogCollector.hpp
 * @brief @b NOTE: THIS FILE IS AUTO-GENERATED. DO NOT EDIT IT MANUALLY.
 */

namespace kaa {

/*
 * Forward declaration.
 */
struct LogSyncResponse;

/**
 * @brief The public interface to the Kaa log collecting subsystem.
 *
 * The log collecting subsystem is based on two main components - a log storage and an upload strategy. Each time
 * a new log record is added to the storage, the strategy decides whether the log upload is needed at the moment.
 *
 * By default, @c MemoryLogStorage and @c DefaultLogUploadStrategy are used as the log storage and as the upload
 * strategy respectively.
 *
 * The subsystem also tracks whether the log delivery timeout is occurred. The timeout means the log delivery response
 * isn't received in time, specified by @link ILogUploadStrategy::getTimeout() @endlink.
 * The check is done in the lazy manner, on each the @link addLogRecord() @endlink call. If the timeout is occurred,
 * the log upload strategy will be notified of it via the @link ILogUploadStrategy::onTimeout() @endlink callback.
 */
class ILogCollector {
public:
    /**
     * @brief Adds a new log record to the log storage.
     *
     * To store log records, @c MemoryLogStorage is used by default. Use @link setStorage() @endlink to set
     * your own implementation.
     *
     * @param[in] record    The log record to be added.
     *
     * @see KaaUserLogRecord
     * @see ILogStorage
     */
    virtual RecordFuture addLogRecord(const KaaUserLogRecord& record) = 0;

    /**
     * @brief Sets the new log storage.
     *
     * @c MemoryLogStorage is used by default.
     *
     * @param[in] storage    The @c ILogStorage implementation.
     *
     * @throw KaaException    The storage is NULL.
     */
    virtual void setStorage(ILogStoragePtr storage) = 0;

    /**
     * @brief Sets the new log upload strategy.
     *
     * @c DefaultLogUploadStrategy is used by default.
     *
     * @param[in] strategy    The @c ILogUploadStrategy implementation.
     *
     * @throw KaaException    The strategy is NULL.
     */
    virtual void setUploadStrategy(ILogUploadStrategyPtr strategy) = 0;

    /**
     * @brief Set a listener which receives a delivery status of each log bucket.
     */
    virtual void setLogDeliveryListener(ILogDeliveryListenerPtr listener) = 0;

    virtual ~ILogCollector() {}
};

}  // namespace kaa

#endif /* ILOGCOLLECTOR_HPP_ */

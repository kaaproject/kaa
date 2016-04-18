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

#ifndef ILOGSTORAGE_HPP_
#define ILOGSTORAGE_HPP_

#include <memory>
#include <cstdint>

#include "kaa/log/BucketInfo.hpp"
#include "kaa/log/LogBucket.hpp"

namespace kaa {

class LogRecord;
class ILogStorageStatus;

/**
 * @brief Interface of a log storage.
 *
 * Persists log records, forms on demand a new log bucket for sending
 * it to the Operation server, removes already sent log buckets, cleans up elder
 * records in case if there is some limitation on a size of a log storage.
 *
 * @c MemoryLogStorage is used by default.
 */
class ILogStorage {
public:
    /**
     * @brief Persists a log record.
     *
     * @param record The @c LogRecord object.
     * @return The @c BucketInfo object which contains information about a bucket the log record is added.
     * @see LogRecord
     * @see BucketInfo
     */
    virtual BucketInfo addLogRecord(LogRecord&& record) = 0;

    /**
     * @brief Returns a log storage status.
     *
     * @return The @c LogStorageStatus object.
     * @see LogStorageStatus
     */
    virtual ILogStorageStatus& getStatus() = 0;

    /**
     * @brief Returns a new log bucket.
     *
     * @return The @c  LogBucket object.
     * @see LogBucket
     */
    virtual LogBucket getNextBucket() = 0;

    /**
     * @brief Tells a log storage to remove a log bucket.
     *
     * @param bucketId The id of a log bucket.
     * @see LogBucket
     * @see BucketInfo
     */
    virtual void removeBucket(std::int32_t bucketId) = 0;

    /**
     * @brief Tells a log storage to consider a log bucket as unused, i.e. a log bucket will be accessible again
     * via @link getNextBucket() @endlink.
     *
     * @param bucketId The id of a log bucket.
     * @see LogBucket
     * @see BucketInfo
     */
    virtual void rollbackBucket(std::int32_t bucketId) = 0;

    virtual ~ILogStorage() {}
};

/**
 * @typedef The shared pointer to @c ILogStorage.
 */
typedef std::shared_ptr<ILogStorage> ILogStoragePtr;

}  // namespace kaa

#endif /* ILOGSTORAGE_HPP_ */

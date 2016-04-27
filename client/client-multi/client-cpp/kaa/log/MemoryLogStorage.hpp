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

#ifndef MEMORYLOGSTORAGE_HPP_
#define MEMORYLOGSTORAGE_HPP_

#include <list>
#include <cstdint>

#include "kaa/KaaThread.hpp"
#include "kaa/log/ILogStorage.hpp"
#include "kaa/log/ILogStorageStatus.hpp"
#include "kaa/log/LogStorageConstants.hpp"

namespace kaa {

class IKaaClientContext;

/**
 * @brief The default @c ILogStorage implementation.
 *
 * @b NOTE: Collected logs are stored in a memory. So logs will be lost if the SDK has been restarted earlier than
 * they are delivered to the Operations server.
 */
class MemoryLogStorage : public ILogStorage, public ILogStorageStatus {
public:
    /**
     * @brief Creates the size-unlimited log storage.
     *
     * @param[in] bucketSize           The bucket size in bytes.
     * @param[in] bucketRecordCount    The number of records in a bucket.
     */
    MemoryLogStorage(IKaaClientContext &context, std::size_t bucketSize = LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                     std::size_t bucketRecordCount = LogStorageConstants::DEFAULT_MAX_BUCKET_RECORD_COUNT);

    /**
     * @brief Creates the size-limited log storage.
     *
     * If the size of collected logs exceeds the specified maximum size of the log storage, elder logs will be
     * forcibly deleted. The amount of logs (in bytes) to be deleted is computed by the formula:
     *
     * SIZE = (MAX_SIZE * PERCENT_TO_DELETE) / 100, where PERCENT_TO_DELETE is in the (0.0, 100.0] range.
     *
     * @param[in] maxOccupiedSize      The maximum size (in bytes) that collected logs can occupy.
     * @param[in] percentToDelete      The percent of logs (in bytes) to be forcibly deleted.
     * @param[in] bucketSize           The bucket size in bytes.
     * @param[in] bucketRecordCount    The number of records in a bucket.
     *
     * @throw KaaException The percentage is out of the range.
     */
    MemoryLogStorage(IKaaClientContext &context,
                     std::size_t maxOccupiedSize,
                     float percentToDelete,
                     std::size_t bucketSize = LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                     std::size_t bucketRecordCount = LogStorageConstants::DEFAULT_MAX_BUCKET_RECORD_COUNT);

    virtual BucketInfo addLogRecord(LogRecord&& record);
    virtual ILogStorageStatus& getStatus() { return *this; }

    virtual LogBucket getNextBucket();
    virtual void removeBucket(std::int32_t bucketId);
    virtual void rollbackBucket(std::int32_t bucketId);

    virtual std::size_t getConsumedVolume();
    virtual std::size_t getRecordsCount();

private:
    void shrinkToSize(std::size_t allowedVolume);

    void addNewBucket() {
        buckets_.emplace_back(++currentBucketId_);
    }

    bool checkBucketOverflow(const LogRecord& record) {
        const auto& currentBucket = buckets_.back();
        return (currentBucket.occupiedSize_ + record.getSize() > maxBucketSize_) ||
               (currentBucket.logs_.size() + 1 > maxBucketRecordCount_);
    }

    void internalAddLogRecord(LogRecord&& record);

private:
    enum class BucketState {
        FREE,
        IN_USE
    };

    struct InternalBucket {
        InternalBucket(std::int32_t bucketId)
            : bucketId_(bucketId) {}

        BucketState             state_ = BucketState::FREE;
        std::int32_t            bucketId_ = 0;
        std::size_t             occupiedSize_ = 0;
        std::list<LogRecord>    logs_;
    };

private:
    const std::size_t maxBucketSize_;
    const std::size_t maxBucketRecordCount_;

    std::int32_t currentBucketId_ = 0;

    std::size_t occupiedSizeOfUnmarkedRecords_ = 0;
    std::size_t unmarkedRecordCount_ = 0;

    std::size_t totalOccupiedSize_ = 0;
    std::size_t maxOccupiedSize_ = 0;
    std::size_t shrinkedSize_ = 0;

    std::list<InternalBucket> buckets_;
    KAA_MUTEX_DECLARE(memoryLogStorageGuard_);
    IKaaClientContext &context_;
};

}  // namespace kaa

#endif /* MEMORYLOGSTORAGE_HPP_ */

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

#include "kaa/log/MemoryLogStorage.hpp"

#include <algorithm>

#include "kaa/KaaThread.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/log/LogRecord.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

MemoryLogStorage::MemoryLogStorage(IKaaClientContext &context,std::size_t bucketSize, std::size_t bucketRecordCount)
    : maxBucketSize_(bucketSize), maxBucketRecordCount_(bucketRecordCount), context_(context)
{
    KAA_LOG_INFO(boost::format("Going to use  unlimited storage. Bucket: max_size %1% bytes, max_record_count %2%")
                                                                % maxBucketSize_ % maxBucketRecordCount_);
    addNewBucket();
}

MemoryLogStorage::MemoryLogStorage(IKaaClientContext &context,
                                   std::size_t maxOccupiedSize, float percentToDelete,
                                   std::size_t bucketSize, std::size_t bucketRecordCount)
    : maxBucketSize_(bucketSize), maxBucketRecordCount_(bucketRecordCount), context_(context)
{
    if (0.0 >= percentToDelete || percentToDelete > 100.0) {
        KAA_LOG_ERROR(boost::format("Failed to create limited log storage: max_size %1% bytes, percentToDelete %2%%%")
                                                                            % maxOccupiedSize % percentToDelete);
        throw KaaException("Percent should be in 0-100 range");
    }

    KAA_LOG_INFO(boost::format("Going to use limited storage: max_size %1% bytes, percentToDelete %2%%%. "
                               "Bucket: max_size %3% bytes, max_record_count %4%")
                                    % maxOccupiedSize % percentToDelete % maxBucketSize_ % maxBucketRecordCount_);

    maxOccupiedSize_ = maxOccupiedSize;
    shrinkedSize_ = ((float) maxOccupiedSize_ * (100.0 - percentToDelete)) / 100.0;

    addNewBucket();
}

BucketInfo MemoryLogStorage::addLogRecord(LogRecord&& record)
{
    auto recordSize = record.getSize();
    if (recordSize > maxBucketSize_) {
        KAA_LOG_WARN(boost::format("Failed to add log record: record_size %1%B, max_bucket_size %2%B")
                                                                    % recordSize % maxBucketSize_);
        throw KaaException("Too big log record");
    }

    KAA_MUTEX_LOCKING("memoryLogStorageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(logsLock, memoryLogStorageGuard_);
    KAA_MUTEX_LOCKED("memoryLogStorageGuard_");

    if (maxOccupiedSize_ && ((totalOccupiedSize_ + record.getSize()) > maxOccupiedSize_)) {
        KAA_LOG_INFO(boost::format("Log storage is full (occupied %1%, max %2%). Going to delete elder logs")
                                                                        % totalOccupiedSize_ % maxOccupiedSize_);
        shrinkToSize(shrinkedSize_);
    }

    if (checkBucketOverflow(record)) {
        addNewBucket();
    }

    internalAddLogRecord(std::move(record));

    KAA_LOG_TRACE(boost::format("Added log record (%1% bytes). Non-used records: count %2%, occupied size %3% bytes")
                                                     % recordSize % unmarkedRecordCount_ % occupiedSizeOfUnmarkedRecords_);

    return BucketInfo(currentBucketId_, buckets_.back().logs_.size());
}

LogBucket MemoryLogStorage::getNextBucket()
{
    KAA_MUTEX_LOCKING("memoryLogStorageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(logsLock, memoryLogStorageGuard_);
    KAA_MUTEX_LOCKED("memoryLogStorageGuard_");

    std::size_t totalRecordCount = 0;
    for (auto& internalBucket : buckets_) {
        if (internalBucket.state_ == MemoryLogStorage::BucketState::FREE && !internalBucket.logs_.empty()) {
            internalBucket.state_ = MemoryLogStorage::BucketState::IN_USE;

            unmarkedRecordCount_ -= internalBucket.logs_.size();
            occupiedSizeOfUnmarkedRecords_ -= internalBucket.occupiedSize_;

            if (!unmarkedRecordCount_) {
                addNewBucket();
            }

            KAA_LOG_INFO(boost::format("Create log bucket: id %1%, size %2%, %3% record(s). "
                                       "Non-used records: count %4%, occupied size %5% bytes")
                            % internalBucket.bucketId_ % internalBucket.occupiedSize_ % internalBucket.logs_.size()
                            % unmarkedRecordCount_ % occupiedSizeOfUnmarkedRecords_);

            return LogBucket(internalBucket.bucketId_, internalBucket.logs_);
        } else {
            totalRecordCount += internalBucket.logs_.size();
        }
    }

    KAA_LOG_TRACE(boost::format("No free log buckets found: total_log_count %1%, total_occupied_size %2%")
                                                                    % totalRecordCount % totalOccupiedSize_);

    return LogBucket();
}

void MemoryLogStorage::removeBucket(std::int32_t bucketId)
{
    KAA_MUTEX_LOCKING("memoryLogStorageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(logsLock, memoryLogStorageGuard_);
    KAA_MUTEX_LOCKED("memoryLogStorageGuard_");

    bool found = false;
    buckets_.remove_if([&] (const MemoryLogStorage::InternalBucket& bucket)
                        {
                             if (bucket.bucketId_ == bucketId) {
                                 totalOccupiedSize_ -= bucket.occupiedSize_;
                                 KAA_LOG_TRACE(boost::format("Log bucket %1% removed (%2% records). "
                                                             "Non-used records: count %3%, occupied size %4% bytes")
                                                             % bucketId % bucket.logs_.size() % unmarkedRecordCount_
                                                             % occupiedSizeOfUnmarkedRecords_);
                                 found = true;
                                 return true;
                             }
                             return false;
                        });

    if (!found) {
        KAA_LOG_WARN(boost::format("Failed to remove log bucket %1%: not found") % bucketId);
    }
}

void MemoryLogStorage::rollbackBucket(std::int32_t bucketId)
{
    KAA_MUTEX_LOCKING("memoryLogStorageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(logsLock, memoryLogStorageGuard_);
    KAA_MUTEX_LOCKED("memoryLogStorageGuard_");

    auto it = std::find_if(buckets_.begin(), buckets_.end(), [&bucketId] (const MemoryLogStorage::InternalBucket& bucket)
            {
                 return bucket.bucketId_ == bucketId;
            });

    if (it != buckets_.end()) {
        it->state_ = MemoryLogStorage::BucketState::FREE;
        occupiedSizeOfUnmarkedRecords_ += it->occupiedSize_;
        unmarkedRecordCount_ += it->logs_.size();

        KAA_LOG_DEBUG(boost::format("Rollback log bucket %1% (%2% records). Non-used records: count %3%, occupied size %4% bytes")
                                            % bucketId % it->logs_.size() % unmarkedRecordCount_ % occupiedSizeOfUnmarkedRecords_);
    } else {
        KAA_LOG_WARN(boost::format("Failed to rollback log bucket %1%: not found") % bucketId);
    }

}

void MemoryLogStorage::shrinkToSize(std::size_t newSize)
{
    if (!newSize) {
        unmarkedRecordCount_ = 0;
        totalOccupiedSize_ = occupiedSizeOfUnmarkedRecords_ = 0;
        buckets_.clear();
        addNewBucket();

        KAA_LOG_INFO("All log records removed");

        return;
    }

    size_t recordCount = 0;
    while (totalOccupiedSize_ > newSize) {
        auto& theOldestBucket = buckets_.front();

        if (totalOccupiedSize_ - theOldestBucket.occupiedSize_ >= newSize) {
            KAA_LOG_INFO(boost::format("Removing in-use log bucket %1% (%2% records, %3% bytes)")
                                    % theOldestBucket.bucketId_ % theOldestBucket.logs_.size() % theOldestBucket.occupiedSize_);

            totalOccupiedSize_ -= theOldestBucket.occupiedSize_;
            recordCount += theOldestBucket.logs_.size();

            if (theOldestBucket.state_ == MemoryLogStorage::BucketState::FREE) {
                unmarkedRecordCount_ -= theOldestBucket.logs_.size();
                occupiedSizeOfUnmarkedRecords_ -= theOldestBucket.occupiedSize_;
            }

            buckets_.pop_front();

            if (buckets_.empty()) {
                addNewBucket();
            }
        } else {
            while (totalOccupiedSize_ > newSize) {
                const auto& theOldestRecord = theOldestBucket.logs_.front();

                if (theOldestBucket.state_ == MemoryLogStorage::BucketState::FREE) {
                    --unmarkedRecordCount_;
                    occupiedSizeOfUnmarkedRecords_ -= theOldestRecord.getSize();
                }

                totalOccupiedSize_ -= theOldestRecord.getSize();
                theOldestBucket.logs_.pop_front();

                ++recordCount;
            }
        }
    }

    KAA_LOG_INFO(boost::format("%1% log records removed") % recordCount);
}

std::size_t MemoryLogStorage::getConsumedVolume()
{
    KAA_MUTEX_LOCKING("memoryLogStorageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(logsLock, memoryLogStorageGuard_);
    KAA_MUTEX_LOCKED("memoryLogStorageGuard_");
    return occupiedSizeOfUnmarkedRecords_;
}

std::size_t MemoryLogStorage::getRecordsCount()
{
    KAA_MUTEX_LOCKING("memoryLogStorageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(logsLock, memoryLogStorageGuard_);
    KAA_MUTEX_LOCKED("memoryLogStorageGuard_");
    return unmarkedRecordCount_;
}

void MemoryLogStorage::internalAddLogRecord(LogRecord&& record)
{
    auto recordSize = record.getSize();

    totalOccupiedSize_ += recordSize;
    occupiedSizeOfUnmarkedRecords_ += recordSize;
    ++unmarkedRecordCount_;

    auto& currentBucket  = buckets_.back();
    currentBucket.occupiedSize_ += recordSize;
    currentBucket.logs_.push_back(std::move(record));
}

}  // namespace kaa

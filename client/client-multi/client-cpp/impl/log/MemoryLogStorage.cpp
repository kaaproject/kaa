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

#include "kaa/log/MemoryLogStorage.hpp"

#include <algorithm>

#include "kaa/KaaThread.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/log/LogRecord.hpp"

namespace kaa {

const MemoryLogStorage::BlockId MemoryLogStorage::NO_OWNER(-1);

MemoryLogStorage::MemoryLogStorage()
    : recordBlockId_(0) {}

MemoryLogStorage::MemoryLogStorage(size_t maxOccupiedSize, float percentToDelete)
    : recordBlockId_(0)
{
    if (0.0 >= percentToDelete || percentToDelete > 100.0) {
        KAA_LOG_ERROR(boost::format("Failed to create limited log storage: max_size %1%, percentToDelete %2%%%")
                                                                            % maxOccupiedSize % percentToDelete);
        throw KaaException("Percent should be in 0-100 range");
    }

    maxOccupiedSize_ = maxOccupiedSize;
    shrinkedSize_ = ((float) maxOccupiedSize_ * (100.0 - percentToDelete)) / 100.0;
}

void MemoryLogStorage::addLogRecord(LogRecordPtr serializedRecord)
{
    KAA_MUTEX_LOCKING("logsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(logsLock, memoryLogStorageGuard_);
    KAA_MUTEX_LOCKED("logsGuard_");

    if (maxOccupiedSize_ && ((totalOccupiedSize_ + serializedRecord->getSize()) > maxOccupiedSize_)) {
        KAA_LOG_INFO(boost::format("Log storage is full (occupied %1%, max %2%). Going to delete elder logs")
                                                                        % totalOccupiedSize_ % maxOccupiedSize_);
        shrinkToSize(shrinkedSize_);
    }

    logs_.push_back(LogRecordWrapper(serializedRecord));
    totalOccupiedSize_ += serializedRecord->getSize();
    occupiedSizeOfUnmarkedRecords_ += serializedRecord->getSize();
    ++unmarkedRecordCount_;

    KAA_LOG_TRACE(boost::format("Added log record (%1% bytes). Record count: %2%. Occupied size: %3% bytes")
                                                % serializedRecord->getSize() % logs_.size() % totalOccupiedSize_);

}

ILogStorage::RecordPack MemoryLogStorage::getRecordBlock(std::size_t blockSize, std::size_t recordsBlockCount)
{
    static std::int32_t bucketId = 0;

    KAA_MUTEX_LOCKING("logsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(logsLock, memoryLogStorageGuard_);
    KAA_MUTEX_LOCKED("logsGuard_");

    ILogStorage::RecordBlock block;

    if (bucketId++ == NO_OWNER) {
        bucketId++;
    }

    RecordBlockId recordBlockId = bucketId;

    for (auto& log : logs_) {
        if (log.blockId_ == NO_OWNER) {
            if (recordsBlockCount == 0 || log.record_->getSize() > blockSize) {
                if (block.empty()) {
                    KAA_LOG_ERROR(boost::format("Failed to get logs: block size (%1%B) is less than the size of "
                                        "the serialized log record (%2%B)") % blockSize % log.record_->getSize());
                    throw KaaException("Block size is less than the size of the serialized log record");
                }
                break;
            }

            block.push_back(log.record_);
            blockSize -= log.record_->getSize();
            recordsBlockCount--;

            log.blockId_ = recordBlockId;

            --unmarkedRecordCount_;
            occupiedSizeOfUnmarkedRecords_ -= log.record_->getSize();
        }
    }

    return ILogStorage::RecordPack((block.empty() ? -1 : recordBlockId), std::move(block));
}

void MemoryLogStorage::removeRecordBlock(RecordBlockId blockId)
{
    KAA_MUTEX_LOCKING("logsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(logsLock, memoryLogStorageGuard_);
    KAA_MUTEX_LOCKED("logsGuard_");

    std::uint32_t removedRecordCount = 0;

    logs_.remove_if([&] (const LogRecordWrapper& wrapper)
                        {
                             if (wrapper.blockId_ == blockId) {
                                 totalOccupiedSize_ -= wrapper.record_->getSize();
                                 ++removedRecordCount;
                                 return true;
                             }
                             return false;
                        });

    KAA_LOG_DEBUG(boost::format("Log block %1% removed (%2% records)") % blockId % removedRecordCount);
}

void MemoryLogStorage::notifyUploadFailed(RecordBlockId blockId)
{
    KAA_MUTEX_LOCKING("logsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(logsLock, memoryLogStorageGuard_);
    KAA_MUTEX_LOCKED("logsGuard_");

    std::uint32_t recordCount = 0;

    std::for_each(logs_.begin(), logs_.end(), [&] (LogRecordWrapper& wrapper)
                                                  {
                                                      if (wrapper.blockId_ == blockId) {
                                                          occupiedSizeOfUnmarkedRecords_ += wrapper.record_->getSize();
                                                          ++recordCount;
                                                          ++unmarkedRecordCount_;
                                                          wrapper.blockId_ = NO_OWNER;
                                                      }
                                                  });

    KAA_LOG_DEBUG(boost::format("Failed to upload %1% log block (%2% records unmarked)") % blockId % recordCount);
}

void MemoryLogStorage::shrinkToSize(std::size_t newSize)
{
    if (!newSize) {
        logs_.clear();
        unmarkedRecordCount_ = 0;
        totalOccupiedSize_ = occupiedSizeOfUnmarkedRecords_ = 0;
        KAA_LOG_INFO("All log were forcibly deleted");
        return;
    }

    size_t recordCount = 0;
    while (totalOccupiedSize_ > newSize) {
        const auto& wrapper = logs_.front();
        if (wrapper.blockId_ == NO_OWNER) {
            --unmarkedRecordCount_;
            occupiedSizeOfUnmarkedRecords_ -= wrapper.record_->getSize();
        }

        totalOccupiedSize_ -= wrapper.record_->getSize();
        logs_.pop_front();
        ++recordCount;
    }

    KAA_LOG_INFO(boost::format("%1% log records were forcibly deleted") % recordCount);
}

std::size_t MemoryLogStorage::getConsumedVolume()
{
    KAA_MUTEX_LOCKING("logsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(logsLock, memoryLogStorageGuard_);
    KAA_MUTEX_LOCKED("logsGuard_");
    return occupiedSizeOfUnmarkedRecords_;
}

std::size_t MemoryLogStorage::getRecordsCount()
{
    KAA_MUTEX_LOCKING("logsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(logsLock, memoryLogStorageGuard_);
    KAA_MUTEX_LOCKED("logsGuard_");
    return unmarkedRecordCount_;
}

}  // namespace kaa

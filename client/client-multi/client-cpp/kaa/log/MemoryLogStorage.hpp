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

#ifndef MEMORYLOGSTORAGE_HPP_
#define MEMORYLOGSTORAGE_HPP_

#include <list>
#include <cstdint>

#include "kaa/KaaThread.hpp"
#include "kaa/log/ILogStorage.hpp"
#include "kaa/log/ILogStorageStatus.hpp"

namespace kaa {

/**
 * Default @c ILogStorage implementation.
 *
 * Log records are stored in memory. After application restarts logs will be purged.
 */
class MemoryLogStorage : public ILogStorage, public ILogStorageStatus {
public:
    MemoryLogStorage();
    MemoryLogStorage(size_t maxOccupiedSize, float percentToDelete);

    virtual void addLogRecord(LogRecordPtr serializedRecord);
    virtual ILogStorageStatus& getStatus() { return *(static_cast<ILogStorageStatus*>(this)); }

    virtual RecordPack getRecordBlock(std::size_t blockSize);
    virtual void removeRecordBlock(RecordBlockId blockId);
    virtual void notifyUploadFailed(RecordBlockId blockId);

    virtual std::size_t getConsumedVolume();
    virtual std::size_t getRecordsCount();

private:
    void shrinkToSize(std::size_t allowedVolume);

private:
    struct LogRecordWrapper {
        LogRecordWrapper(LogRecordPtr record, RecordBlockId id = NO_OWNER)
            : record_(record), blockId_(id) {}

        LogRecordPtr     record_;
        RecordBlockId    blockId_;
    };

    typedef RequestId BlockId;

private:
    size_t totalOccupiedSize_ = 0;
    size_t occupiedSizeOfUnmarkedRecords_ = 0;

    size_t unmarkedRecordCount_ = 0;

    size_t maxOccupiedSize_ = 0;
    size_t shrinkedSize_ = 0;

    std::list<LogRecordWrapper> logs_;
    KAA_MUTEX_DECLARE(logsGuard_);

    BlockId recordBlockId_;
    static const BlockId NO_OWNER;
};

}  // namespace kaa

#endif /* MEMORYLOGSTORAGE_HPP_ */

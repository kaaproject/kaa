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
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

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
     */
    MemoryLogStorage(IKaaClientContext &context);

    /**
     * @brief Creates the size-limited log storage.
     *
     * If the size of collected logs exceeds the specified maximum size of the log storage, elder logs will be
     * forcibly deleted. The amount of logs (in bytes) to be deleted is computed by the formula:
     *
     * SIZE = (MAX_SIZE * PERCENT_TO_DELETE) / 100, where PERCENT_TO_DELETE is in the (0.0, 100.0] range.
     *
     * @param[in] maxOccupiedSize    The maximum size (in bytes) that collected logs can occupy.
     * @param[in] percentToDelete    The percent of logs (in bytes) to be forcibly deleted.
     *
     * @throw KaaException The percentage is out of the range.
     */
    MemoryLogStorage(size_t maxOccupiedSize, float percentToDelete, IKaaClientContext &context);

    virtual void addLogRecord(LogRecordPtr serializedRecord);
    virtual ILogStorageStatus& getStatus() { return *this; }

    virtual RecordPack getRecordBlock(std::size_t blockSize, std::size_t recordsBlockCount);
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
    KAA_MUTEX_DECLARE(memoryLogStorageGuard_);

    BlockId recordBlockId_;
    static const BlockId NO_OWNER;
    IKaaClientContext &context_;
};

}  // namespace kaa

#endif /* MEMORYLOGSTORAGE_HPP_ */

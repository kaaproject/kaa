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

#ifndef MEMORYLOGSTORAGE_HPP_
#define MEMORYLOGSTORAGE_HPP_

#include <list>
#include <cstdint>

#include "kaa/log/ILogStorage.hpp"
#include "kaa/log/ILogStorageStatus.hpp"
#include "kaa/common/UuidGenerator.hpp"

namespace kaa {

/**
 * Default \c ILogStorage implementation.
 *
 * Log records are stored in memory. After application restarts logs will be purged.
 */
class MemoryLogStorage : public ILogStorage, public ILogStorageStatus {
private:
    typedef struct __MemoryLogStorage__LogBlock__ {
        __MemoryLogStorage__LogBlock__(size_t blockSize)
                : actualSize_(0)
                , blockSize_(blockSize)
                , finalized_(false)
        {
        }

        std::string                 blockId;
        ILogStorage::container_type logs_;
        std::size_t                 actualSize_;
        std::size_t                 blockSize_;
        bool                        finalized_;
    } LogBlock;

public:
    MemoryLogStorage(std::size_t blockSize) : blockSize_(blockSize), occupiedSize_(0) {
        LogBlock initialBlock(blockSize);
        initialBlock.actualSize_ = 0;
        initialBlock.finalized_ = false;
        logBlocks_.push_back(initialBlock);
    }
    ~MemoryLogStorage() {}

    /**
     * \c ILogStorage public interface implementation
     */
    void            addLogRecord(const LogRecord & record);
    container_type  getRecordBlock(std::size_t blockSize, const std::string& blockId);
    void            removeRecordBlock(const std::string& blockId);
    void            notifyUploadFailed(const std::string& blockId);
    void            removeOldestRecords(std::size_t allowedVolume);

    /**
     * \c ILogStorageStatus public interface implementation
     */
    std::size_t          getConsumedVolume() const;
    std::size_t          getRecordsCount() const;

private:
    void            resize(std::size_t blockSize);

private:
    std::size_t          blockSize_;
    std::size_t          occupiedSize_;
    std::list<LogBlock> logBlocks_;
};

}  // namespace kaa

#endif /* MEMORYLOGSTORAGE_HPP_ */

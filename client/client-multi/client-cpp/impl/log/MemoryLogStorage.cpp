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

#include "kaa/log/MemoryLogStorage.hpp"
#include "kaa/logging/Log.hpp"

namespace kaa {

void MemoryLogStorage::addLogRecord(const LogRecord & record)
{
    LogBlock & block = logBlocks_.back();
    if (block.finalized_ || block.actualSize_ + record.getSize() > blockSize_) {
        KAA_LOG_FTRACE(boost::format("Generating new Block: is finalized: %1% size is: %2% records count: %3%") % block.finalized_ % block.actualSize_ % block.logs_.size());
        LogBlock newBlock(blockSize_);
        newBlock.logs_.push_back(record);
        newBlock.actualSize_ = record.getSize();
        logBlocks_.push_back(newBlock);
    } else {
        block.logs_.push_back(record);
        block.actualSize_ += record.getSize();
        KAA_LOG_FTRACE(boost::format("Appending data to old block: records count: %1% size: %2%") % block.logs_.size() % block.actualSize_);
    }
    occupiedSize_ += record.getSize();

    KAA_LOG_DEBUG(boost::format("Added log record (size: %1% bytes). Occupied size: %2% bytes")
                     % record.getSize() % occupiedSize_);
}

ILogStorage::container_type MemoryLogStorage::getRecordBlock(std::size_t blockSize, const std::string& blockId)
{
    if (blockSize_ != blockSize) {
        resize(blockSize);
        blockSize_ = blockSize;
    }

    for (auto block = logBlocks_.begin(); block != logBlocks_.end(); ++block) {
        if (!block->finalized_) {
            block->blockId = blockId;
            block->finalized_ = true;
            return block->logs_;
        }
    }

    static MemoryLogStorage::container_type emptyBlock;
    return emptyBlock;
}

void MemoryLogStorage::removeRecordBlock(const std::string& blockId)
{
    for (auto block = logBlocks_.begin(); block != logBlocks_.end(); ++block) {
        if (block->blockId.compare(blockId) == 0) {
            if (block->finalized_) {
                occupiedSize_ -= block->actualSize_;
                logBlocks_.erase(block);
                return;
            } else {
                KAA_LOG_ERROR(boost::format("Can not remove non-finalized log block with id: \"%1%\"") % blockId);
            }
        }
    }
    KAA_LOG_ERROR(boost::format("Can not remove find log block with id: \"%1%\"") % blockId);
}

void MemoryLogStorage::notifyUploadFailed(const std::string& blockId)
{
    for (auto block = logBlocks_.begin(); block != logBlocks_.end(); ++block) {
        if (block->blockId.compare(blockId) == 0) {
            if (block->finalized_) {
                KAA_LOG_WARN(boost::format("Failed to upload log block with id: \"%1%\"") % blockId);
                block->finalized_ = false;
                return;
            } else {
                KAA_LOG_ERROR(boost::format("Log block with id: \"%1%\" was not finalized") % blockId);
            }
        }
    }
    KAA_LOG_ERROR(boost::format("Can not remove find log block with id: \"%1%\"") % blockId);
}

void MemoryLogStorage::removeOldestRecords(std::size_t allowedVolume)
{
    KAA_LOG_INFO(boost::format("Going to perform clean-up. Occupied %1% bytes, allowed %2% bytes") % occupiedSize_ % allowedVolume);
    std::size_t releasedSpace = 0;
    for (auto block = logBlocks_.begin(); block != logBlocks_.end(); ++block) {
        if (!block->finalized_) {
            while (occupiedSize_ > allowedVolume && !block->logs_.empty()) {
                auto log_it = block->logs_.begin();
                std::size_t frontLogSize = log_it->getSize();
                block->actualSize_ -= frontLogSize;
                occupiedSize_ -= frontLogSize;
                releasedSpace += frontLogSize;
                block->logs_.pop_front();
                if (occupiedSize_ <= allowedVolume) {
                    KAA_LOG_INFO(boost::format("Released %1% bytes. Occupied %2% bytes, allowed %3% bytes")
                            % releasedSpace % occupiedSize_ % allowedVolume);
                    return;
                }
            }
        }
    }
    KAA_LOG_ERROR(boost::format("Can not release enough space. "
                                "Released %1% bytes. Occupied %2% bytes"
                                ", allowed %3% bytes")
                        % releasedSpace % occupiedSize_ % allowedVolume);
}

std::size_t MemoryLogStorage::getConsumedVolume() const
{
    std::size_t totalSize = 0;
    for (const LogBlock& block : logBlocks_) {
        totalSize += (block.finalized_ ? 0 : block.actualSize_);
    }
    return totalSize;
}

std::size_t MemoryLogStorage::getRecordsCount() const
{
    std::size_t totalRecordCount = 0;
    for (const LogBlock& block : logBlocks_) {
        totalRecordCount += block.logs_.size();
    }
    return totalRecordCount;
}

void MemoryLogStorage::resize(std::size_t blockSize)
{
    container_type logsPool;
    for (auto it = logBlocks_.begin(); it != logBlocks_.end(); ++it) {
        if (it->finalized_) {
            continue;
        }
        logsPool.insert(logsPool.end(), it->logs_.begin(), it->logs_.end());
        logBlocks_.erase(it);
    }

    LogBlock tempBlock(blockSize);
    for (auto it = logsPool.begin(); it != logsPool.end(); ++it) {
        if (tempBlock.actualSize_ + it->getSize() >= blockSize) {
            logBlocks_.push_back(tempBlock);
            tempBlock = LogBlock(blockSize);
        } else {
            tempBlock.logs_.push_back(*it);
            tempBlock.actualSize_ += it->getSize();
        }
    }
}

}  // namespace kaa



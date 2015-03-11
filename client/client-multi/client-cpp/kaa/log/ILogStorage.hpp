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

#ifndef ILOGSTORAGE_HPP_
#define ILOGSTORAGE_HPP_

#include <list>
#include <memory>
#include <cstdint>
#include <utility>

namespace kaa {

class LogRecord;
class ILogStorageStatus;

typedef std::shared_ptr<LogRecord> LogRecordPtr;

/**
 * Interface for log storage.
 *
 * Default implementation can be found in @c MemoryLogStorage
 * @see MemoryLogStorage
 */
class ILogStorage {
public:
    typedef std::int32_t RecordBlockId;
    typedef std::list<LogRecordPtr> RecordBlock;
    typedef std::pair<RecordBlockId, RecordBlock> RecordPack;

    /**
     *  Adds log record to storage.
     */
    virtual void addLogRecord(LogRecordPtr record) = 0;

    /**
     *
     * @return
     */
    virtual ILogStorageStatus& getStatus() = 0;

    /**
     * Returns record block of given size
     *
     * @param[in] blockSize     Size of a log record block
     * @return  Container of records
     */
    virtual RecordPack getRecordBlock(std::size_t blockSize) = 0;

    /**
     * Called when log block was successfully uploaded.
     *
     * @param[in] blockId   Unique identifier of the log block.
     */
    virtual void removeRecordBlock(RecordBlockId id) = 0;

    /**
     * Called when log block upload failed.
     *
     * @param[in] blockId   Unique identifier of the log block.
     */
    virtual void notifyUploadFailed(RecordBlockId id) = 0;

    virtual ~ILogStorage() {}
};

typedef std::shared_ptr<ILogStorage> ILogStoragePtr;

}  // namespace kaa

#endif /* ILOGSTORAGE_HPP_ */

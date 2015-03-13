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

/*
 * Forward declarations.
 */
class LogRecord;
class ILogStorageStatus;

/**
 * @typedef The shared pointer to the serialized @c LogRecord instance.
 */
typedef std::shared_ptr<LogRecord> LogRecordPtr;

/**
 * @brief The public interface to access to the log storage.
 *
 * The default implementation can be found in @c MemoryLogStorage.
 */
class ILogStorage {
public:
    /**
     * @brief The alias for the unique identifier of the requested log block.
     *
     * The identifier may be reuse after notifying of its status via @link removeRecordBlock(RecordBlockId id) @endlink
     * and @link notifyUploadFailed(RecordBlockId id) @endlink.
     */
    typedef std::int32_t RecordBlockId;

    /**
     * @brief The alias for the log block container.
     */
    typedef std::list<LogRecordPtr> RecordBlock;

    /**
     * @brief The alias for the log block marked by the unique identifier.
     */
    typedef std::pair<RecordBlockId, RecordBlock> RecordPack;

    /**
     * @brief Adds the log record to the storage.
     */
    virtual void addLogRecord(LogRecordPtr record) = 0;

    /**
     * @brief Returns the current log storage status.
     *
     * @return The current log storage status.
     */
    virtual ILogStorageStatus& getStatus() = 0;

    /**
     * @brief Returns the block of log records which total size is less or equal to the specified block size.
     *
     * @param[in] blockSize    The maximum size (in bytes) of the requested log record block.
     *
     * @return The log record block marked by the unique @c RecordBlockId identifier.
     */
    virtual RecordPack getRecordBlock(std::size_t blockSize) = 0;

    /**
     * @brief Removes the log block marked by the specified id.
     *
     * @param[in] id    The unique identifier of the log block.
     */
    virtual void removeRecordBlock(RecordBlockId id) = 0;

    /**
     * @brief Notifies of the delivery of the log block marked by the specified id has been failed.
     *
     * @param[in] id    The unique identifier of the log block.
     */
    virtual void notifyUploadFailed(RecordBlockId id) = 0;

    virtual ~ILogStorage() {}
};

/**
 * @typedef The shared pointer to @c ILogStorage.
 */
typedef std::shared_ptr<ILogStorage> ILogStoragePtr;

}  // namespace kaa

#endif /* ILOGSTORAGE_HPP_ */

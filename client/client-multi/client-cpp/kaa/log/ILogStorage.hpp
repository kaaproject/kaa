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

#ifndef ILOGSTORAGE_HPP_
#define ILOGSTORAGE_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_LOGGING

#include "kaa/log/LogRecord.hpp"
#include <list>
#include <cstdint>
#include <memory>

namespace kaa {

/**
 * Interface for log storage.
 *
 * Default implementation can be found in \c MemoryLogStorage
 * \see MemoryLogStorage
 */
class ILogStorage {
public:
    typedef std::list<LogRecord>    container_type;

    /**
     *  Adds log record to storage.
     */
    virtual void            addLogRecord(const LogRecord & record)  = 0;

    /**
     * Returns record block of given size
     *
     * \param blockSize     Size of a log record block
     * \param blockId       Unique identifier of the log record block.
     * \return  Container of records
     */
    virtual container_type  getRecordBlock(std::size_t blockSize, std::int32_t blockId)        = 0;

    /**
     * Called when log block was successfully uploaded.
     *
     * \param blockId   Unique identifier of the log block.
     */
    virtual void            removeRecordBlock(std::int32_t blockId)       = 0;

    /**
     * Called when log block upload failed.
     *
     * \param blockId   Unique identifier of the log block.
     */
    virtual void            notifyUploadFailed(std::int32_t blockId)      = 0;

    /**
     * Shrink storage to fit allowed volume size.
     */
    virtual void            removeOldestRecords(std::size_t allowedVolume)   = 0;

    virtual ~ILogStorage() {}
};

typedef std::shared_ptr<ILogStorage> LogStoragePtr;

}  // namespace kaa

#endif

#endif /* ILOGSTORAGE_HPP_ */

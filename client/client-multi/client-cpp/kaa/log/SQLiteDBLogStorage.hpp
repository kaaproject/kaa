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

#ifndef SQLITEDBLOGSTORAGE_HPP_
#define SQLITEDBLOGSTORAGE_HPP_

#include <memory>
#include <list>
#include <cstdint>
#include <string>
#include <unordered_map>

#include <sqlite3.h>

#include "kaa/KaaThread.hpp"
#include "kaa/log/ILogStorage.hpp"
#include "kaa/log/ILogStorageStatus.hpp"

namespace kaa {

class SQLiteDBLogStorage : public ILogStorage, public ILogStorageStatus {
public:
    SQLiteDBLogStorage(const std::string& dbName);
    ~SQLiteDBLogStorage();

    virtual void addLogRecord(LogRecordPtr record);

    virtual ILogStorageStatus& getStatus() {return *this; }

    virtual RecordPack getRecordBlock(std::size_t blockSize);
    virtual void removeRecordBlock(RecordBlockId id);
    virtual void notifyUploadFailed(RecordBlockId id);

    virtual std::size_t getRecordsCount();
    virtual std::size_t getConsumedVolume();

private:
    void openDBConnection();
    void closeDBConnection();
    void initLogTable();
    void resetBucketID();

    void updateBucketIDForRecords(std::int32_t id, std::list<int>& idList);
    void removeRecordById(sqlite3_int64 id);

private:
    const std::string dbName_;
    sqlite3 *db_;

    std::size_t unmarkedRecordCount_;
    std::size_t totalRecordCount_;

    std::size_t consumedMemory_;
    std::unordered_map<std::int32_t/*Bucket id*/, std::size_t /*Bucket size*/> consumedMemoryStorage_;

    KAA_MUTEX_DECLARE(storageGuard);
};

} /* namespace kaa */

#endif /* SQLITEDBLOGSTORAGE_HPP_ */

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
#include "kaa/log/LogStorageConstants.hpp"



#define KAA_DEFAULT_LOG_DB_STORAGE    "logs.db"

namespace kaa {

class IKaaClientContext;

enum SQLiteOptimizationOptions
{
    SQLITE_NO_OPTIMIZATIONS    = 0x0,

    SQLITE_SYNCHRONOUS_OFF     = 0x1,
    SQLITE_MEMORY_JOURNAL_MODE = 0x2,
    SQLITE_MEMORY_TEMP_STORE   = 0x4,
    SQLITE_COUNT_CHANGES_OFF   = 0x8,

    SQLITE_ALL_OPTIMIZATIONS   = SQLITE_SYNCHRONOUS_OFF |
                                 SQLITE_MEMORY_JOURNAL_MODE |
                                 SQLITE_MEMORY_TEMP_STORE |
                                 SQLITE_COUNT_CHANGES_OFF
};

class SQLiteDBLogStorage : public ILogStorage, public ILogStorageStatus {
public:
    SQLiteDBLogStorage(IKaaClientContext &context,
                       std::size_t bucketSize = LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                       std::size_t bucketRecordCount = LogStorageConstants::DEFAULT_MAX_BUCKET_RECORD_COUNT);

    SQLiteDBLogStorage(IKaaClientContext &context,
                       const std::string& dbName,
                       int optimizationMask = (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                       std::size_t bucketSize = LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                       std::size_t bucketRecordCount = LogStorageConstants::DEFAULT_MAX_BUCKET_RECORD_COUNT);

    ~SQLiteDBLogStorage();

    virtual BucketInfo addLogRecord(LogRecord&& record);
    virtual ILogStorageStatus& getStatus() { return *this; }

    virtual LogBucket getNextBucket();
    virtual void removeBucket(std::int32_t bucketId);
    virtual void rollbackBucket(std::int32_t bucketId);

    virtual std::size_t getConsumedVolume();
    virtual std::size_t getRecordsCount();

private:
    void init(int optimizationMask);

    void openDBConnection();
    void closeDBConnection();

    void initDBTables();
    void applyDBOptimization(int mask);

    bool checkBucketOverflow(const LogRecord& record) {
        return (currentBucketSize_ + record.getSize() > maxBucketSize_) ||
               (currentBucketRecordCount_ + 1 > maxBucketRecordCount_);
    }

    void addNextBucket();
    void markBucketAsInUse(std::int32_t id);
    void markBucketsAsFree();
    bool retrieveLastBucketInfo();

    void retrieveConsumedSizeAndVolume();
    bool truncateIfBucketSizeIncompatible();

    std::string storageStatisticsToStr() {
        return (boost::format("Storage: total_logs %d, unmarked_logs %d, total_size: %d B")
                                % totalRecordCount_ % unmarkedRecordCount_ % consumedMemory_).str();
    }

    std::string bucketStatisticsToStr() {
        return (boost::format("Bucket: id %d, logs %d, size %d B")
                    % currentBucketId_ % currentBucketRecordCount_ % currentBucketSize_).str();
    }

private:
    struct InnerBucketInfo {
        InnerBucketInfo(std::size_t sizeInBytes, std::size_t sizeInLogs)
            : sizeInBytes_(sizeInBytes), sizeInLogs_(sizeInLogs) {}

        std::size_t sizeInBytes_ = 0;
        std::size_t sizeInLogs_ = 0;
    };

private:

    const std::string dbName_;
    sqlite3 *db_ = nullptr;

    const std::size_t maxBucketSize_;
    const std::size_t maxBucketRecordCount_;

    std::int32_t currentBucketId_ = 0;
    std::size_t currentBucketSize_ = 0;
    std::size_t currentBucketRecordCount_ = 0;

    std::size_t unmarkedRecordCount_ = 0;
    std::size_t totalRecordCount_ = 0;

    std::size_t consumedMemory_ = 0;
    std::unordered_map<std::int32_t/*Bucket id*/, InnerBucketInfo> consumedMemoryStorage_;

    KAA_MUTEX_DECLARE(sqliteLogStorageGuard_);

    IKaaClientContext &context_;
};

} /* namespace kaa */

#endif /* SQLITEDBLOGSTORAGE_HPP_ */

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

#include <boost/test/unit_test.hpp>

#include <string>
#include <cstdio>
#include <fstream>

#include "kaa/log/SQLiteDBLogStorage.hpp"
#include "kaa/log/LogRecord.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

static std::string testLogData("very big test data");
static std::string testLogStorageName("test_logs.db");

void removeDatabase(const std::string& dbName)
{
    std::remove(dbName.c_str());
}


BOOST_AUTO_TEST_SUITE(FileLogStorageTestSuite)

BOOST_AUTO_TEST_CASE(CreateLogDataBaseTest)
{
    removeDatabase(testLogStorageName);

    SQLiteDBLogStorage logStorage(testLogStorageName);

    std::ofstream dbFile(testLogStorageName);
    if (dbFile) {
        removeDatabase(testLogStorageName);
    } else {
        BOOST_CHECK(false);
    }
}

BOOST_AUTO_TEST_CASE(AddLogRecordTest)
{
    SQLiteDBLogStorage logStorage(testLogStorageName);

    KaaUserLogRecord record;
    record.logdata = testLogData;
    LogRecord serializedRecord(record);

    std::size_t recordCount = 5;
    for (std::size_t i = 0; i < recordCount; ++i) {
        logStorage.addLogRecord(std::move(serializedRecord));
    }

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), recordCount);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), recordCount * serializedRecord.getSize());

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(RestoreAfterRestartTest)
{
    std::size_t recordCount = 5;
    KaaUserLogRecord record;
    record.logdata = testLogData;
    LogRecord serializedRecord(record);

    {
        SQLiteDBLogStorage logStorage1(testLogStorageName);
        for (std::size_t i = 0; i < recordCount; ++i) {
            logStorage1.addLogRecord(std::move(serializedRecord));
        }
    }

    SQLiteDBLogStorage logStorage2(testLogStorageName);

    BOOST_CHECK_EQUAL(logStorage2.getStatus().getRecordsCount(), recordCount);
    BOOST_CHECK_EQUAL(logStorage2.getStatus().getConsumedVolume(), recordCount * serializedRecord.getSize());

    auto bucket = logStorage2.getNextBucket();

    for (auto& encodedLog : bucket.getRecords()) {
        AvroByteArrayConverter<KaaUserLogRecord> decoder;
        KaaUserLogRecord decodedLog;
        decoder.fromByteArray(encodedLog.getData().data(), encodedLog.getSize(), decodedLog);

        BOOST_CHECK_EQUAL(decodedLog.logdata, testLogData);
    }

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(GetAllRecordsTest)
{
    SQLiteDBLogStorage logStorage(testLogStorageName);

    KaaUserLogRecord record;
    record.logdata = testLogData;
    LogRecord serializedRecord(record);

    std::size_t recordCount = 5;
    for (std::size_t i = 0; i < recordCount; ++i) {
        logStorage.addLogRecord(std::move(serializedRecord));
    }

    auto logs = logStorage.getNextBucket();

    BOOST_CHECK_EQUAL(logs.getRecords().size(), recordCount);

    std::size_t totalSize = 0;
    for (auto& record : logs.getRecords()) {
        totalSize += record.getSize();
    }

    BOOST_CHECK_EQUAL(totalSize, recordCount * serializedRecord.getSize());

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(GetPartOfRecordsByCountTest)
{
    std::size_t totalRecordCount = 4;
    std::size_t expectedRecordCount = totalRecordCount / 2;
    KaaUserLogRecord record;
    record.logdata = testLogData;

    SQLiteDBLogStorage logStorage(testLogStorageName,
                                 (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                 LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                 expectedRecordCount);

    LogRecord serializedRecord(record);
    for (std::size_t i = 0; i < totalRecordCount; ++i) {
        logStorage.addLogRecord(std::move(serializedRecord));
    }

    auto logs = logStorage.getNextBucket();

    BOOST_CHECK_EQUAL(logs.getRecords().size(), expectedRecordCount);

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(GetPartOfRecordsBySizeInBytesTest)
{
    KaaUserLogRecord record;
    record.logdata = testLogData;
    std::size_t totalRecordCount = 4;
    std::size_t expectedRecordCount = totalRecordCount / 2;
    std::size_t sizeOfOneRecord = LogRecord(record).getSize();
    std::size_t expectedSizeInBytes = expectedRecordCount * sizeOfOneRecord;

    SQLiteDBLogStorage logStorage(testLogStorageName,
                                 (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                 expectedSizeInBytes);

    for (std::size_t i = 0; i < totalRecordCount; ++i) {
        logStorage.addLogRecord(LogRecord(record));
    }

    auto logs = logStorage.getNextBucket();

    std::size_t actualSizeInBytes = 0;
    for (auto& record : logs.getRecords()) {
        actualSizeInBytes += record.getSize();
    }

    BOOST_CHECK_EQUAL(actualSizeInBytes, expectedSizeInBytes);

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(RemoveLogRecordsTest)
{
    {

        KaaUserLogRecord record;
        record.logdata = testLogData;
        std::size_t sizeOfOneRecord = LogRecord(record).getSize();

        std::size_t recordCount = 10;
        std::size_t count = recordCount / 2;

        SQLiteDBLogStorage logStorage1(testLogStorageName,
                                     (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                     LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                     count);

        for (std::size_t i = 0; i < recordCount; ++i) {
            logStorage1.addLogRecord(LogRecord(record));
        }

        auto logs1 = logStorage1.getNextBucket();

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), (recordCount - count));
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), (recordCount - count) * sizeOfOneRecord);

        logStorage1.removeBucket(logs1.getBucketId());

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), (recordCount - count));
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), (recordCount - count) * sizeOfOneRecord);

        auto logs2 = logStorage1.getNextBucket();
        logStorage1.removeBucket(logs2.getBucketId());

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), 0);
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), 0);
    }

    SQLiteDBLogStorage logStorage2(testLogStorageName);

    BOOST_CHECK_EQUAL(logStorage2.getStatus().getRecordsCount(), 0);
    BOOST_CHECK_EQUAL(logStorage2.getStatus().getConsumedVolume(), 0);

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(RollbackTest)
{
    KaaUserLogRecord record;
    record.logdata = testLogData;

    std::size_t sizeOfOneRecord = LogRecord(record).getSize();
    std::size_t recordCount = 10;
    std::size_t count = recordCount / 2;

    SQLiteDBLogStorage logStorage(testLogStorageName,
                                 (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                 LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                 count);

    for (std::size_t i = 0; i < recordCount; ++i) {
        logStorage.addLogRecord(LogRecord(record));
    }

    auto logs1 = logStorage.getNextBucket();

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), (recordCount - count));
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), (recordCount - count) * sizeOfOneRecord);

    logStorage.rollbackBucket(logs1.getBucketId());

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), recordCount);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), recordCount * sizeOfOneRecord);

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(RollbackWithRestartTest)
{
    KaaUserLogRecord record;
    record.logdata = testLogData;
    LogRecord serializedRecord(record);

    std::size_t recordCount = 10;
    std::size_t count = recordCount - 1;
    std::size_t sizeOfOneRecord = LogRecord(record).getSize();

    {
        SQLiteDBLogStorage logStorage1(testLogStorageName,
                                     (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                     LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                     count);

        for (std::size_t i = 0; i < recordCount; ++i) {
            logStorage1.addLogRecord(LogRecord(record));
        }

        auto logs1 = logStorage1.getNextBucket();

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), (recordCount - count));
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), (recordCount - count) * sizeOfOneRecord);
    }

    SQLiteDBLogStorage logStorage2(testLogStorageName);

    BOOST_CHECK_EQUAL(logStorage2.getStatus().getRecordsCount(), recordCount);
    BOOST_CHECK_EQUAL(logStorage2.getStatus().getConsumedVolume(), recordCount * sizeOfOneRecord);

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(TruncateTest)
{
    KaaUserLogRecord record;
    record.logdata = testLogData;
    LogRecord serializedRecord(record);

    std::size_t recordCount = 10;
    std::size_t count = recordCount / 2;
    std::size_t sizeOfOneRecord = LogRecord(record).getSize();

    {
        SQLiteDBLogStorage logStorage1(testLogStorageName,
                                     (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                     LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                     count);

        for (std::size_t i = 0; i < recordCount; ++i) {
            logStorage1.addLogRecord(LogRecord(record));
        }

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), recordCount);
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), recordCount * sizeOfOneRecord);
    }

    SQLiteDBLogStorage logStorage1(testLogStorageName,
                                 (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                 LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                 count - 1);

    BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), 0);
    BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), 0);

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_SUITE_END()

}

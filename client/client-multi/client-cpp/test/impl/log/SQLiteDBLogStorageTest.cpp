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

#include <boost/test/unit_test.hpp>

#include <string>
#include <cstdio>
#include <fstream>

#include "kaa/log/SQLiteDBLogStorage.hpp"
#include "kaa/log/LogRecord.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/logging/DefaultLogger.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"

#include "headers/MockKaaClientStateStorage.hpp"
#include "headers/context/MockExecutorContext.hpp"

namespace kaa {

static std::string testLogData("very big test data");
static std::string testLogStorageName("test_logs.db");

static KaaClientProperties properties;
static DefaultLogger tmp_logger(properties.getClientId());
static IKaaClientStateStoragePtr tmp_state(new MockKaaClientStateStorage);
static MockExecutorContext tmpExecContext;
static KaaClientContext clientContext(properties, tmp_logger, tmpExecContext, tmp_state);

static KaaClientContext getClientContext()
{
    IKaaClientStateStoragePtr stateMock(new MockKaaClientStateStorage);
    properties.setLogsDatabaseFileName(testLogStorageName);
    return KaaClientContext(properties, tmp_logger, tmpExecContext, stateMock);
}

void removeDatabase(const std::string& dbFullPath)
{
    std::remove(dbFullPath.c_str());
}

static LogRecord createSerializedLogRecord()
{
    KaaUserLogRecord logRecord;
    logRecord.logdata = testLogData;

    return LogRecord(logRecord);
}

BOOST_AUTO_TEST_SUITE(FileLogStorageTestSuite)

BOOST_AUTO_TEST_CASE(CreateLogDataBaseTest)
{
    auto clientContext = getClientContext();
    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());

    SQLiteDBLogStorage logStorage(clientContext);

    std::ofstream dbFile(testLogStorageName);
    if (dbFile) {
        removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
    } else {
        BOOST_CHECK(false);
    }
}

BOOST_AUTO_TEST_CASE(AddLogRecordTest)
{
    auto clientContext = getClientContext();
    SQLiteDBLogStorage logStorage(clientContext, testLogStorageName);
    std::size_t sizeOfOneRecord = createSerializedLogRecord().getSize();

    std::size_t recordCount = 5;
    for (std::size_t i = 0; i < recordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), recordCount);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), recordCount * sizeOfOneRecord);

    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
}

BOOST_AUTO_TEST_CASE(RestoreAfterRestartTest)
{
    std::size_t recordCount = 5;
    std::size_t sizeOfOneRecord = createSerializedLogRecord().getSize();

    {
        auto clientContext = getClientContext();
        SQLiteDBLogStorage logStorage1(clientContext);
        for (std::size_t i = 0; i < recordCount; ++i) {
            logStorage1.addLogRecord(createSerializedLogRecord());
        }
    }

    auto clientContext = getClientContext();
    SQLiteDBLogStorage logStorage2(clientContext);

    BOOST_CHECK_EQUAL(logStorage2.getStatus().getRecordsCount(), recordCount);
    BOOST_CHECK_EQUAL(logStorage2.getStatus().getConsumedVolume(), recordCount * sizeOfOneRecord);

    auto bucket = logStorage2.getNextBucket();

    for (auto& encodedLog : bucket.getRecords()) {
        AvroByteArrayConverter<KaaUserLogRecord> decoder;
        KaaUserLogRecord decodedLog;
        decoder.fromByteArray(encodedLog.getData().data(), encodedLog.getSize(), decodedLog);

        BOOST_CHECK_EQUAL(decodedLog.logdata, testLogData);
    }

    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
}

BOOST_AUTO_TEST_CASE(GetAllRecordsTest)
{
    auto clientContext = getClientContext();
    SQLiteDBLogStorage logStorage(clientContext, testLogStorageName);
    std::size_t sizeOfOneRecord = createSerializedLogRecord().getSize();

    std::size_t recordCount = 5;
    for (std::size_t i = 0; i < recordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    auto bucket = logStorage.getNextBucket();

    BOOST_CHECK_EQUAL(bucket.getRecords().size(), recordCount);

    std::size_t totalSize = 0;
    for (auto& record : bucket.getRecords()) {
        totalSize += record.getSize();
    }

    BOOST_CHECK_EQUAL(totalSize, recordCount * sizeOfOneRecord);

    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
}

BOOST_AUTO_TEST_CASE(FillInBucketGetItAndAddNewLogTest)
{
    std::size_t recordCount = 5;

    auto clientContext = getClientContext();
    SQLiteDBLogStorage logStorage(clientContext,
                                 testLogStorageName,
                                 (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                 LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                 recordCount);


    for (std::size_t i = 0; i < recordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    auto bucket1 = logStorage.getNextBucket();
    BOOST_CHECK_EQUAL(bucket1.getRecords().size(), recordCount);

    logStorage.addLogRecord(createSerializedLogRecord());
    auto bucket2 = logStorage.getNextBucket();
    BOOST_CHECK_EQUAL(bucket2.getRecords().size(), 1);

    BOOST_CHECK(bucket1.getBucketId() != bucket2.getBucketId());

    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
}

BOOST_AUTO_TEST_CASE(GetPartOfRecordsByCountTest)
{
    std::size_t totalRecordCount = 4;
    std::size_t expectedRecordCount = totalRecordCount / 2;

    auto clientContext = getClientContext();
    SQLiteDBLogStorage logStorage(clientContext, testLogStorageName,
                                 (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                 LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                 expectedRecordCount);

    for (std::size_t i = 0; i < totalRecordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    auto logs = logStorage.getNextBucket();

    BOOST_CHECK_EQUAL(logs.getRecords().size(), expectedRecordCount);

    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
}

BOOST_AUTO_TEST_CASE(GetPartOfRecordsBySizeInBytesTest)
{
    std::size_t totalRecordCount = 4;
    std::size_t expectedRecordCount = totalRecordCount / 2;
    std::size_t sizeOfOneRecord = createSerializedLogRecord().getSize();
    std::size_t expectedSizeInBytes = expectedRecordCount * sizeOfOneRecord;

    auto clientContext = getClientContext();
    SQLiteDBLogStorage logStorage(clientContext, testLogStorageName,
                                 (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                 expectedSizeInBytes);

    for (std::size_t i = 0; i < totalRecordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    auto logs = logStorage.getNextBucket();

    std::size_t actualSizeInBytes = 0;
    for (auto& record : logs.getRecords()) {
        actualSizeInBytes += record.getSize();
    }

    BOOST_CHECK_EQUAL(actualSizeInBytes, expectedSizeInBytes);

    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
}

BOOST_AUTO_TEST_CASE(RemoveLogRecordsTest)
{
    {
        std::size_t recordCount = 10;
        std::size_t recordInBucket = recordCount / 2;
        std::size_t sizeOfOneRecord = createSerializedLogRecord().getSize();

        auto clientContext = getClientContext();
        SQLiteDBLogStorage logStorage1(clientContext, testLogStorageName,
                                     (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                     LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                     recordInBucket);

        for (std::size_t i = 0; i < recordCount; ++i) {
            logStorage1.addLogRecord(createSerializedLogRecord());
        }

        auto logs1 = logStorage1.getNextBucket();

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), (recordCount - recordInBucket));
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), (recordCount - recordInBucket) * sizeOfOneRecord);

        logStorage1.removeBucket(logs1.getBucketId());

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), (recordCount - recordInBucket));
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), (recordCount - recordInBucket) * sizeOfOneRecord);

        auto logs2 = logStorage1.getNextBucket();
        logStorage1.removeBucket(logs2.getBucketId());

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), 0);
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), 0);
    }

    SQLiteDBLogStorage logStorage2(clientContext);

    BOOST_CHECK_EQUAL(logStorage2.getStatus().getRecordsCount(), 0);
    BOOST_CHECK_EQUAL(logStorage2.getStatus().getConsumedVolume(), 0);

    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
}

BOOST_AUTO_TEST_CASE(RollbackTest)
{
    std::size_t sizeOfOneRecord = createSerializedLogRecord().getSize();
    std::size_t recordCount = 10;
    std::size_t recordInBucket = recordCount / 2;

    auto clientContext = getClientContext();
    SQLiteDBLogStorage logStorage(clientContext, testLogStorageName,
                                 (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                 LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                 recordInBucket);

    for (std::size_t i = 0; i < recordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    auto logs1 = logStorage.getNextBucket();

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), (recordCount - recordInBucket));
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), (recordCount - recordInBucket) * sizeOfOneRecord);

    logStorage.rollbackBucket(logs1.getBucketId());

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), recordCount);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), recordCount * sizeOfOneRecord);

    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
}

BOOST_AUTO_TEST_CASE(RollbackWithRestartTest)
{
    std::size_t recordCount = 10;
    std::size_t recordInBucket = recordCount - 1;
    std::size_t sizeOfOneRecord = createSerializedLogRecord().getSize();

    {
        auto clientContext = getClientContext();
        SQLiteDBLogStorage logStorage1(clientContext, testLogStorageName,
                                     (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                     LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                     recordInBucket);

        for (std::size_t i = 0; i < recordCount; ++i) {
            logStorage1.addLogRecord(createSerializedLogRecord());
        }

        auto logs1 = logStorage1.getNextBucket();

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), (recordCount - recordInBucket));
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), (recordCount - recordInBucket) * sizeOfOneRecord);
    }

    auto clientContext = getClientContext();
    SQLiteDBLogStorage logStorage2(clientContext);

    BOOST_CHECK_EQUAL(logStorage2.getStatus().getRecordsCount(), recordCount);
    BOOST_CHECK_EQUAL(logStorage2.getStatus().getConsumedVolume(), recordCount * sizeOfOneRecord);

    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
}

BOOST_AUTO_TEST_CASE(TruncateTest)
{
    std::size_t recordCount = 10;
    std::size_t recordInBucket = recordCount / 2;
    std::size_t sizeOfOneRecord = createSerializedLogRecord().getSize();

    {
        auto clientContext = getClientContext();
        SQLiteDBLogStorage logStorage1(clientContext, testLogStorageName,
                                     (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                     LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                     recordInBucket);

        for (std::size_t i = 0; i < recordCount; ++i) {
            logStorage1.addLogRecord(createSerializedLogRecord());
        }

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), recordCount);
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), recordCount * sizeOfOneRecord);
    }

    auto clientContext = getClientContext();
    SQLiteDBLogStorage logStorage2(clientContext, testLogStorageName,
                                 (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                 LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                 recordInBucket - 1);

    BOOST_CHECK_EQUAL(logStorage2.getStatus().getRecordsCount(), 0);
    BOOST_CHECK_EQUAL(logStorage2.getStatus().getConsumedVolume(), 0);

    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
}

BOOST_AUTO_TEST_CASE(ReuseNotFullBucketAfterRestart)
{
    std::size_t recordInBucket = 5;
    std::size_t recordInBucketBeforeRestart = recordInBucket - 1;
    BucketInfo bucketBeforeRestart;

    {
        auto clientContext = getClientContext();
        SQLiteDBLogStorage logStorage1(clientContext, testLogStorageName,
                                     (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                     LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                     recordInBucket);

        /*
         * Add (recordInBucket - 1) records
         */
        bucketBeforeRestart = logStorage1.addLogRecord(createSerializedLogRecord());
        for (std::size_t i = 1; i < recordInBucketBeforeRestart; ++i) {
            auto currentBucket = logStorage1.addLogRecord(createSerializedLogRecord());
            BOOST_CHECK_EQUAL(bucketBeforeRestart.getBucketId(), currentBucket.getBucketId());
            bucketBeforeRestart = currentBucket;
        }

        BOOST_CHECK_EQUAL(bucketBeforeRestart.getLogCount(), recordInBucketBeforeRestart);
    }

    auto clientContext = getClientContext();
    SQLiteDBLogStorage logStorage2(clientContext, testLogStorageName,
                                 (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                 LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                 recordInBucket);

    BOOST_CHECK_EQUAL(logStorage2.getRecordsCount(), recordInBucketBeforeRestart);

    auto bucketAfterRestart = logStorage2.addLogRecord(createSerializedLogRecord());
    BOOST_CHECK_EQUAL(bucketBeforeRestart.getBucketId(), bucketAfterRestart.getBucketId());

    auto newBucketAfterRestart = logStorage2.addLogRecord(createSerializedLogRecord());
    BOOST_CHECK(newBucketAfterRestart.getBucketId() != bucketAfterRestart.getBucketId());

    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
}

BOOST_AUTO_TEST_CASE(AddNewBucketAfterRestart)
{
    std::size_t recordInBucket = 5;
    BucketInfo bucketBeforeRestart;

    {
        auto clientContext = getClientContext();
        SQLiteDBLogStorage logStorage1(clientContext, testLogStorageName,
                                     (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                     LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                     recordInBucket);

        bucketBeforeRestart = logStorage1.addLogRecord(createSerializedLogRecord());
        for (std::size_t i = 1; i < recordInBucket; ++i) {
            auto currentBucket = logStorage1.addLogRecord(createSerializedLogRecord());
            BOOST_CHECK_EQUAL(bucketBeforeRestart.getBucketId(), currentBucket.getBucketId());
            bucketBeforeRestart = currentBucket;
        }

        BOOST_CHECK_EQUAL(bucketBeforeRestart.getLogCount(), recordInBucket);
    }

    auto clientContext = getClientContext();
    SQLiteDBLogStorage logStorage2(clientContext, testLogStorageName,
                                 (int)SQLiteOptimizationOptions::SQLITE_NO_OPTIMIZATIONS,
                                 LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE,
                                 recordInBucket);

    BOOST_CHECK_EQUAL(logStorage2.getRecordsCount(), recordInBucket);

    auto bucketAfterRestart = logStorage2.addLogRecord(createSerializedLogRecord());
    BOOST_CHECK(bucketAfterRestart.getBucketId() != bucketBeforeRestart.getBucketId());

    removeDatabase(clientContext.getProperties().getLogsDatabaseFileName());
}

BOOST_AUTO_TEST_SUITE_END()

}

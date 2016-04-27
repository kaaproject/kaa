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
#include <cmath>

#include "kaa/log/LogRecord.hpp"
#include "kaa/log/MemoryLogStorage.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/log/LogStorageConstants.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/logging/DefaultLogger.hpp"

#include "headers/MockKaaClientStateStorage.hpp"
#include "headers/context/MockExecutorContext.hpp"

namespace kaa {

static KaaClientProperties properties;
static DefaultLogger tmp_logger(properties.getClientId());
static IKaaClientStateStoragePtr tmp_state(new MockKaaClientStateStorage);
static MockExecutorContext tmpExecContext;
static KaaClientContext clientContext(properties, tmp_logger, tmpExecContext, tmp_state);

#define LOG_TEST_DATA "test data"

static std::int32_t mockBlocksCount = 10000;

static LogRecord createSerializedLogRecord()
{
    KaaUserLogRecord logRecord;
    logRecord.logdata = LOG_TEST_DATA;

    return LogRecord(logRecord);
}

BOOST_AUTO_TEST_SUITE(MemoryLogStorageTestSuite)

BOOST_AUTO_TEST_CASE(BadInitializationParamsTest)
{
    BOOST_CHECK_THROW(
            {
                MemoryLogStorage logStorage(clientContext, 100500, (float)-1.0);
            }, KaaException);

    BOOST_CHECK_THROW(
            {
                MemoryLogStorage logStorage(clientContext, 100500, (float)100.1);
            }, KaaException);
}

BOOST_AUTO_TEST_CASE(BucketizeIsLessThanLogRecordSizeTest)
{
    auto serializedLogRecord = createSerializedLogRecord();

    MemoryLogStorage logStorage(clientContext, serializedLogRecord.getSize() / 2);

    BOOST_CHECK_THROW(logStorage.addLogRecord(std::move(serializedLogRecord)), KaaException);
}

BOOST_AUTO_TEST_CASE(AddRecordsAndCheckStatusTest)
{
    MemoryLogStorage logStorage(clientContext);
    std::size_t serializedLogSize = createSerializedLogRecord().getSize();

    std::srand(std::time(nullptr));
    /*
     * At least 1 records.
     */
    std::size_t logRecordCount = 1 + rand() % 10;

    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), logRecordCount);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), logRecordCount * serializedLogSize);
}

BOOST_AUTO_TEST_CASE(BucketSizeInRecordsConstraint)
{
    const size_t recordInBucket = 2;
    MemoryLogStorage logStorage(clientContext, LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE, recordInBucket);

    /*
     * At least 1 records.
     */
    std::size_t logRecordCount = 1 + rand() % 10;

    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    for (std::size_t i = 0; i < std::ceil((float)logRecordCount / recordInBucket); ++i) {
        LogBucket bucket = logStorage.getNextBucket();
        BOOST_CHECK(!bucket.getRecords().empty());
    }

    LogBucket bucket = logStorage.getNextBucket();
    BOOST_CHECK(bucket.getRecords().empty());
}

BOOST_AUTO_TEST_CASE(BucketSizeInBytesConstraint)
{
    std::srand(std::time(nullptr));

    std::size_t serializedLogSize = createSerializedLogRecord().getSize();
    std::size_t bucketSizeInBytes = serializedLogSize * (1 + rand() % 2);
    std::size_t recordsInBucket = bucketSizeInBytes / serializedLogSize;
    MemoryLogStorage logStorage(clientContext, bucketSizeInBytes, LogStorageConstants::DEFAULT_MAX_BUCKET_RECORD_COUNT);

    /*
     * At least 1 records.
     */
    std::size_t logRecordCount = 1 + rand() % 10;

    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    for (std::size_t i = 0; i < std::ceil((float)logRecordCount / recordsInBucket); ++i) {
        LogBucket bucket = logStorage.getNextBucket();
        BOOST_CHECK(!bucket.getRecords().empty());
    }

    LogBucket bucket = logStorage.getNextBucket();
    BOOST_CHECK(bucket.getRecords().empty());
}

BOOST_AUTO_TEST_CASE(GetStatusAfterLogBlockTest)
{
    /*
     * At least 2 records.
     */
    std::size_t logRecordCount = 2 + rand() % 10;
    std::size_t serializedLogSize = createSerializedLogRecord().getSize();
    std::size_t recordBucketSize = (logRecordCount * serializedLogSize) / 2;

    MemoryLogStorage logStorage(clientContext, recordBucketSize);

    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    auto bucket = logStorage.getNextBucket();

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), (logRecordCount - bucket.getRecords().size()));
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), ((logRecordCount - bucket.getRecords().size()) * serializedLogSize));
}

BOOST_AUTO_TEST_CASE(RemoveLogBlockAndGetStatusTest)
{
    /*
     * At least 2 records.
     */
    std::size_t logRecordCount = 2 + rand() % 10;
    std::size_t serializedLogSize = createSerializedLogRecord().getSize();
    std::size_t recordBucketSize1 = (logRecordCount * serializedLogSize) / 2;

    MemoryLogStorage logStorage(clientContext, recordBucketSize1);
    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    auto bucket1 = logStorage.getNextBucket();

    logStorage.removeBucket(bucket1.getBucketId());

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), (logRecordCount - bucket1.getRecords().size()));
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(),
                     ((logRecordCount - bucket1.getRecords().size()) * serializedLogSize));

    auto bucket2 = logStorage.getNextBucket();

    logStorage.removeBucket(bucket2.getBucketId());

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), logRecordCount - (recordBucketSize1 / serializedLogSize * 2));
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), (logRecordCount * serializedLogSize - (recordBucketSize1 / serializedLogSize * 2) * serializedLogSize));
}

BOOST_AUTO_TEST_CASE(NotifyUploadFailedAndGetStatusTest)
{
    /*
     * At least 2 records.
     */
    std::size_t logRecordCount = 2 + rand() % 10;
    std::size_t serializedLogSize = createSerializedLogRecord().getSize();
    std::size_t recordBucketSize1 = (logRecordCount * serializedLogSize) / 2;

    MemoryLogStorage logStorage(clientContext, recordBucketSize1, (std::size_t)mockBlocksCount);

    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    auto bucket1 = logStorage.getNextBucket();

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), (logRecordCount - bucket1.getRecords().size()));
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(),
                     ((logRecordCount - bucket1.getRecords().size()) * serializedLogSize));

    logStorage.rollbackBucket(bucket1.getBucketId());

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), logRecordCount);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), (logRecordCount * serializedLogSize));
}

BOOST_AUTO_TEST_CASE(ForceRemovalOfAllLogsTest)
{
    std::size_t logRecordCount = 5;
    std::size_t serializedLogSize = createSerializedLogRecord().getSize();
    std::size_t maxLogStorageSize = logRecordCount * serializedLogSize;
    float percentToDelete = 100.0;

    MemoryLogStorage logStorage(clientContext, maxLogStorageSize, percentToDelete);
    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    /*
     * Should cause force removal
     */
    logStorage.addLogRecord(createSerializedLogRecord());

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), 1);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), serializedLogSize);
}

BOOST_AUTO_TEST_CASE(ForceRemovalOfSpecifiedPercentOfLogsTest)
{
    std::size_t logRecordCount = 10;
    std::size_t serializedLogSize = createSerializedLogRecord().getSize();
    std::size_t maxLogStorageSize = logRecordCount * serializedLogSize;
    float percentToDelete = 51.1;

    MemoryLogStorage logStorage(clientContext, maxLogStorageSize, percentToDelete, LogStorageConstants::DEFAULT_MAX_BUCKET_SIZE, logRecordCount / 2);

    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(createSerializedLogRecord());
    }

    logStorage.getNextBucket();

    /*
     * Should cause force removal
     */
    logStorage.addLogRecord(createSerializedLogRecord());

    std::size_t recordLeftAfterRemoval = 1 + ((maxLogStorageSize * (100.0 - percentToDelete) / 100.0) / serializedLogSize);
    size_t sizeAfterRemoval = recordLeftAfterRemoval * serializedLogSize;

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), recordLeftAfterRemoval);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), sizeAfterRemoval);
}

BOOST_AUTO_TEST_SUITE_END()

}

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

#include "kaa/log/LogRecord.hpp"
#include "kaa/log/MemoryLogStorage.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

#define LOG_TEST_DATA "test data"

static LogRecordPtr createSerializedLogRecord()
{
    static LogRecordPtr serializedLogRecord;

    if (!serializedLogRecord) {
        KaaUserLogRecord logRecord;
        logRecord.logdata = LOG_TEST_DATA;

        serializedLogRecord.reset(new LogRecord(logRecord));
    }

    return serializedLogRecord;
}

BOOST_AUTO_TEST_SUITE(MemoryLogStorageTestSuite)

BOOST_AUTO_TEST_CASE(BadInitializationParamsTest)
{
    BOOST_CHECK_THROW(
            {
                MemoryLogStorage logStorage(100500, -1.0);
            }, KaaException);

    BOOST_CHECK_THROW(
            {
                MemoryLogStorage logStorage(100500, 100.1);
            }, KaaException);
}

BOOST_AUTO_TEST_CASE(BlockSizeIsLessThanLogRecordSizeTest)
{
    MemoryLogStorage logStorage;

    auto serializedLogRecord = createSerializedLogRecord();
    logStorage.addLogRecord(serializedLogRecord);

    BOOST_CHECK_THROW(logStorage.getRecordBlock(serializedLogRecord->getSize() - 1), KaaException);
}

BOOST_AUTO_TEST_CASE(AddRecordsAndCheckStatusTest)
{
    MemoryLogStorage logStorage;

    std::srand(std::time(nullptr));
    /*
     * At least 1 records.
     */
    std::size_t logRecordCount = 1 + rand() % 10;

    auto serializedLogRecord = createSerializedLogRecord();
    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(serializedLogRecord);
    }

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), logRecordCount);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), logRecordCount * serializedLogRecord->getSize());
}

BOOST_AUTO_TEST_CASE(AddRecordsAndGetRecordBlockTest)
{
    MemoryLogStorage logStorage;

    /*
     * At least 1 records.
     */
    std::size_t logRecordCount = 1 + rand() % 10;

    auto serializedLogRecord = createSerializedLogRecord();
    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(serializedLogRecord);
    }

    std::size_t recordBlockSize = logRecordCount * serializedLogRecord->getSize();
    ILogStorage::RecordPack pack = logStorage.getRecordBlock(recordBlockSize);

    BOOST_CHECK_EQUAL(pack.second.size(), logRecordCount);
}

BOOST_AUTO_TEST_CASE(GetStatusAfterLogBlockTest)
{
    MemoryLogStorage logStorage;

    /*
     * At least 2 records.
     */
    std::size_t logRecordCount = 2 + rand() % 10;

    auto serializedLogRecord = createSerializedLogRecord();
    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(serializedLogRecord);
    }

    std::size_t recordBlockSize = (logRecordCount * serializedLogRecord->getSize()) / 2;
    ILogStorage::RecordPack pack1 = logStorage.getRecordBlock(recordBlockSize);

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), (logRecordCount - pack1.second.size()));
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(),
                     ((logRecordCount - pack1.second.size()) * serializedLogRecord->getSize()));
}

BOOST_AUTO_TEST_CASE(RemoveLogBlockAndGetStatusTest)
{
    MemoryLogStorage logStorage;

    /*
     * At least 2 records.
     */
    std::size_t logRecordCount = 2 + rand() % 10;

    auto serializedLogRecord = createSerializedLogRecord();
    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(serializedLogRecord);
    }

    std::size_t recordBlockSize1 = (logRecordCount * serializedLogRecord->getSize()) / 2;
    ILogStorage::RecordPack pack1 = logStorage.getRecordBlock(recordBlockSize1);

    logStorage.removeRecordBlock(pack1.first);

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), (logRecordCount - pack1.second.size()));
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(),
                     ((logRecordCount - pack1.second.size()) * serializedLogRecord->getSize()));

    std::size_t recordBlockSize2 = (logRecordCount - pack1.second.size()) * serializedLogRecord->getSize();
    ILogStorage::RecordPack pack2 = logStorage.getRecordBlock(recordBlockSize2);

    logStorage.removeRecordBlock(pack2.first);

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), 0);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), 0);
}

BOOST_AUTO_TEST_CASE(NotifyUploadFailedAndGetStatusTest)
{
    MemoryLogStorage logStorage;

    /*
     * At least 2 records.
     */
    std::size_t logRecordCount = 2 + rand() % 10;

    auto serializedLogRecord = createSerializedLogRecord();
    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(serializedLogRecord);
    }

    std::size_t recordBlockSize1 = (logRecordCount * serializedLogRecord->getSize()) / 2;
    ILogStorage::RecordPack pack1 = logStorage.getRecordBlock(recordBlockSize1);

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), (logRecordCount - pack1.second.size()));
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(),
                     ((logRecordCount - pack1.second.size()) * serializedLogRecord->getSize()));

    logStorage.notifyUploadFailed(pack1.first);

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), logRecordCount);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), (logRecordCount * serializedLogRecord->getSize()));
}

BOOST_AUTO_TEST_CASE(ForceRemovalOfAllLogsTest)
{
    std::size_t logRecordCount = 5;
    auto serializedLogRecord = createSerializedLogRecord();
    std::size_t maxLogStorageSize = logRecordCount * serializedLogRecord->getSize();
    float percentToDelete = 100.0;

    MemoryLogStorage logStorage(maxLogStorageSize, percentToDelete);
    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(serializedLogRecord);
    }

    /*
     * Should cause force removal
     */
    logStorage.addLogRecord(serializedLogRecord);

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), 1);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), serializedLogRecord->getSize());
}

BOOST_AUTO_TEST_CASE(ForceRemovalOfSpecifiedPercentOfLogsTest)
{
    std::size_t logRecordCount = 10;
    auto serializedLogRecord = createSerializedLogRecord();
    std::size_t maxLogStorageSize = logRecordCount * serializedLogRecord->getSize();
    float percentToDelete = 51.1;

    MemoryLogStorage logStorage(maxLogStorageSize, percentToDelete);
    for (std::size_t i = 1; i <= logRecordCount; ++i) {
        logStorage.addLogRecord(serializedLogRecord);
    }

    /*
     * Should cause force removal
     */
    logStorage.addLogRecord(serializedLogRecord);

    std::size_t recordLeftAfterRemoval = 1 + ((maxLogStorageSize * (100.0 - percentToDelete) / 100.0) / serializedLogRecord->getSize());
    size_t sizeAfterRemoval = recordLeftAfterRemoval * serializedLogRecord->getSize();

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), recordLeftAfterRemoval);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), sizeAfterRemoval);
}

BOOST_AUTO_TEST_SUITE_END()

}

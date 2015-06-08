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
static std::string testLogStorageName("logs.db");

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
    LogRecordPtr serializedRecord(new LogRecord(record));

    std::size_t recordCount = 5;
    for (std::size_t i = 0; i < recordCount; ++i) {
        logStorage.addLogRecord(serializedRecord);
    }

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), recordCount);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), recordCount * serializedRecord->getSize());

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(RestoreAfterRestartTest)
{
    std::size_t recordCount = 5;
    KaaUserLogRecord record;
    record.logdata = testLogData;
    LogRecordPtr serializedRecord(new LogRecord(record));

    {
        SQLiteDBLogStorage logStorage1(testLogStorageName);
        for (std::size_t i = 0; i < recordCount; ++i) {
            logStorage1.addLogRecord(serializedRecord);
        }
    }

    SQLiteDBLogStorage logStorage2(testLogStorageName);

    BOOST_CHECK_EQUAL(logStorage2.getStatus().getRecordsCount(), recordCount);
    BOOST_CHECK_EQUAL(logStorage2.getStatus().getConsumedVolume(), recordCount * serializedRecord->getSize());

    auto logs = logStorage2.getRecordBlock(SIZE_MAX);

    for (auto& encodedLog : logs.second) {
        AvroByteArrayConverter<KaaUserLogRecord> decoder;
        auto decodedLog = decoder.fromByteArray(encodedLog->getData().data(), encodedLog->getSize());

        BOOST_CHECK_EQUAL(decodedLog.logdata, testLogData);
    }

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(GetAllRecordsTest)
{
    SQLiteDBLogStorage logStorage(testLogStorageName);

    KaaUserLogRecord record;
    record.logdata = testLogData;
    LogRecordPtr serializedRecord(new LogRecord(record));

    std::size_t recordCount = 5;
    for (std::size_t i = 0; i < recordCount; ++i) {
        logStorage.addLogRecord(serializedRecord);
    }

    auto logs = logStorage.getRecordBlock(recordCount * serializedRecord->getSize());

    BOOST_CHECK_EQUAL(logs.second.size(), recordCount);

    std::size_t totalSize = 0;
    for (auto& record : logs.second) {
        totalSize += record->getSize();
    }

    BOOST_CHECK_EQUAL(totalSize, recordCount * serializedRecord->getSize());

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(GetPartOfRecordsTest)
{
    SQLiteDBLogStorage logStorage(testLogStorageName);

    KaaUserLogRecord record;
    record.logdata = testLogData;
    LogRecordPtr serializedRecord(new LogRecord(record));

    std::size_t recordCount = 5;
    for (std::size_t i = 0; i < recordCount; ++i) {
        logStorage.addLogRecord(serializedRecord);
    }

    std::size_t count = recordCount / 2;
    auto logs = logStorage.getRecordBlock((count * serializedRecord->getSize()) + 1);

    BOOST_CHECK_EQUAL(logs.second.size(), count);

    std::size_t totalSize = 0;
    for (auto& record : logs.second) {
        totalSize += record->getSize();
    }

    BOOST_CHECK_EQUAL(totalSize, count * serializedRecord->getSize());

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(RemoveLogRecordsTest)
{
    {
        SQLiteDBLogStorage logStorage1(testLogStorageName);

        KaaUserLogRecord record;
        record.logdata = testLogData;
        LogRecordPtr serializedRecord(new LogRecord(record));

        std::size_t recordCount = 10;
        for (std::size_t i = 0; i < recordCount; ++i) {
            logStorage1.addLogRecord(serializedRecord);
        }

        std::size_t count = recordCount / 2;
        auto logs1 = logStorage1.getRecordBlock(count * serializedRecord->getSize());

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), (recordCount - count));
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), (recordCount - count) * serializedRecord->getSize());

        logStorage1.removeRecordBlock(logs1.first);

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), (recordCount - count));
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), (recordCount - count) * serializedRecord->getSize());

        auto logs2 = logStorage1.getRecordBlock(count * serializedRecord->getSize());
        logStorage1.removeRecordBlock(logs2.first);

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), 0);
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), 0);
    }

    SQLiteDBLogStorage logStorage2(testLogStorageName);

    BOOST_CHECK_EQUAL(logStorage2.getStatus().getRecordsCount(), 0);
    BOOST_CHECK_EQUAL(logStorage2.getStatus().getConsumedVolume(), 0);

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(DeliveyFailedTest)
{
    SQLiteDBLogStorage logStorage(testLogStorageName);

    KaaUserLogRecord record;
    record.logdata = testLogData;
    LogRecordPtr serializedRecord(new LogRecord(record));

    std::size_t recordCount = 10;
    for (std::size_t i = 0; i < recordCount; ++i) {
        logStorage.addLogRecord(serializedRecord);
    }

    std::size_t count = recordCount / 2;
    auto logs1 = logStorage.getRecordBlock(count * serializedRecord->getSize());

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), (recordCount - count));
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), (recordCount - count) * serializedRecord->getSize());

    logStorage.notifyUploadFailed(logs1.first);

    BOOST_CHECK_EQUAL(logStorage.getStatus().getRecordsCount(), recordCount);
    BOOST_CHECK_EQUAL(logStorage.getStatus().getConsumedVolume(), recordCount * serializedRecord->getSize());

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_CASE(DeliveyFailedWithRestartTest)
{
    KaaUserLogRecord record;
    record.logdata = testLogData;
    LogRecordPtr serializedRecord(new LogRecord(record));

    std::size_t recordCount = 10;

    {
        SQLiteDBLogStorage logStorage1(testLogStorageName);

        for (std::size_t i = 0; i < recordCount; ++i) {
            logStorage1.addLogRecord(serializedRecord);
        }

        std::size_t count = recordCount - 1;
        auto logs1 = logStorage1.getRecordBlock(count * serializedRecord->getSize());

        BOOST_CHECK_EQUAL(logStorage1.getStatus().getRecordsCount(), (recordCount - count));
        BOOST_CHECK_EQUAL(logStorage1.getStatus().getConsumedVolume(), (recordCount - count) * serializedRecord->getSize());
    }

    SQLiteDBLogStorage logStorage2(testLogStorageName);

    BOOST_CHECK_EQUAL(logStorage2.getStatus().getRecordsCount(), recordCount);
    BOOST_CHECK_EQUAL(logStorage2.getStatus().getConsumedVolume(), recordCount * serializedRecord->getSize());

    removeDatabase(testLogStorageName);
}

BOOST_AUTO_TEST_SUITE_END()

}

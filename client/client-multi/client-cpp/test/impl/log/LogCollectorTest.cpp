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

#include <memory>
#include <thread>
#include <chrono>
#include <cstdlib>

#include "kaa/log/LogRecord.hpp"
#include "kaa/log/LogCollector.hpp"
#include "kaa/log/LoggingTransport.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"
#include "kaa/KaaClientProperties.hpp"

#include "headers/log/MockLogStorage.hpp"
#include "headers/log/MockLogUploadStrategy.hpp"
#include "headers/channel/MockChannelManager.hpp"
#include "headers/context/MockExecutorContext.hpp"

namespace kaa {

static void testSleep(std::size_t seconds)
{
    std::this_thread::sleep_for(std::chrono::seconds(seconds));
}

class CustomLoggingTransport : public LoggingTransport {
public:
    CustomLoggingTransport(IKaaChannelManager &manager, ILogProcessor& logProcessor)
        : LoggingTransport(manager, logProcessor) {}

    virtual void sync() { ++onSync_; }

public:
    std::size_t onSync_ = 0;
};

#define LOG_TEST_DATA "test data"

static KaaUserLogRecord createLogRecord()
{
    static KaaUserLogRecord logRecord;

    if (logRecord.logdata.empty()) {
        KaaUserLogRecord logRecord;
        logRecord.logdata = LOG_TEST_DATA;
    }

    return logRecord;
}

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

BOOST_AUTO_TEST_SUITE(LogCollectorTestSuite)

BOOST_AUTO_TEST_CASE(BadLogStorageTest)
{
    KaaClientProperties properties;
    MockChannelManager channelManager;
    MockExecutorContext executor;
    LogCollector logCollector(&channelManager, executor, properties);

    ILogUploadStrategyPtr fakeStrategy;
    BOOST_CHECK_THROW(logCollector.setUploadStrategy(fakeStrategy), KaaException);
}

BOOST_AUTO_TEST_CASE(BadLogUploadStrategyTest)
{
    KaaClientProperties properties;
    MockChannelManager channelManager;
    MockExecutorContext executor;
    LogCollector logCollector(&channelManager, executor, properties);

    ILogStoragePtr fakeStorage;
    BOOST_CHECK_THROW(logCollector.setStorage(fakeStorage), KaaException);
}

BOOST_AUTO_TEST_CASE(AddLogRecordAndCheckStorageAndStrategyTest)
{
    KaaClientProperties properties;
    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    executor.init();
    LogCollector logCollector(&channelManager, executor, properties);
    CustomLoggingTransport transport(channelManager, logCollector);

    logCollector.setTransport(&transport);

    KaaUserLogRecord logRecord = createLogRecord();
    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);

    uploadStrategy->recordsBatchCount_ = 10;
    uploadStrategy->timeoutCheckPeriod_ = 10;
    uploadStrategy->logUploadCheckPeriod_ = 10;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

    uploadStrategy->decision_ = LogUploadStrategyDecision::NOOP;
    logCollector.addLogRecord(logRecord);
    testSleep(1);

    BOOST_CHECK_EQUAL(logStorage->onAddLogRecord_, 1);
    BOOST_CHECK_EQUAL(uploadStrategy->onIsUploadNeeded_, 1);
    BOOST_CHECK_EQUAL(transport.onSync_, 0);

    uploadStrategy->decision_ = LogUploadStrategyDecision::UPLOAD;
    logCollector.addLogRecord(logRecord);
    testSleep(1);

    BOOST_CHECK_EQUAL(logStorage->onAddLogRecord_, 2);
    BOOST_CHECK_EQUAL(uploadStrategy->onIsUploadNeeded_, 2);
    BOOST_CHECK_EQUAL(transport.onSync_, 1);
}

BOOST_AUTO_TEST_CASE(CreateRequestTest)
{
    KaaClientProperties properties;
    MockChannelManager channelManager;
    MockExecutorContext executor;
    LogCollector logCollector(&channelManager, executor, properties);
    CustomLoggingTransport transport(channelManager, logCollector);

    logCollector.setTransport(&transport);

    KaaUserLogRecord logRecord = createLogRecord();
    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->recordsBatchCount_ = 10;
    uploadStrategy->timeoutCheckPeriod_ = 10;
    uploadStrategy->logUploadCheckPeriod_ = 10;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

    ILogStorage::RecordPack recordPack;
    logStorage->recordPack_ = recordPack;

    BOOST_CHECK_EQUAL(logCollector.getLogUploadRequest(), std::shared_ptr<LogSyncRequest>());
}

BOOST_AUTO_TEST_CASE(CreateRequestWithLogsTest)
{
    KaaClientProperties properties;
    const size_t BATCH_SIZE = 100500;
    MockChannelManager channelManager;
    MockExecutorContext executor;
    LogCollector logCollector(&channelManager, executor, properties);
    CustomLoggingTransport transport(channelManager, logCollector);

    logCollector.setTransport(&transport);

    ILogStorage::RecordBlock block{ createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord()
                                  };

    ILogStorage::RecordPack recordPack(1, block);

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = recordPack;

    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->batchSize_ = BATCH_SIZE;
    uploadStrategy->recordsBatchCount_ = 10;
    uploadStrategy->timeoutCheckPeriod_ = 10;
    uploadStrategy->logUploadCheckPeriod_ = 10;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

    auto request = logCollector.getLogUploadRequest();

    BOOST_CHECK(request);
    BOOST_CHECK(!request->logEntries.is_null());
    BOOST_CHECK_EQUAL(request->logEntries.get_array().size(), block.size());
}

BOOST_AUTO_TEST_CASE(SuccessDeliveryTest)
{
    KaaClientProperties properties;
    const size_t BATCH_SIZE = 100500;
    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    executor.init();
    LogCollector logCollector(&channelManager, executor, properties);
    CustomLoggingTransport transport(channelManager, logCollector);

    logCollector.setTransport(&transport);

    ILogStorage::RecordBlock block{ createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord()
                                  };

    ILogStorage::RecordPack recordPack(1, block);

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = recordPack;

    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->batchSize_ = BATCH_SIZE;
    uploadStrategy->decision_ = LogUploadStrategyDecision::NOOP;
    uploadStrategy->recordsBatchCount_ = 10;
    uploadStrategy->timeoutCheckPeriod_ = 10;
    uploadStrategy->logUploadCheckPeriod_ = 10;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

    auto request = logCollector.getLogUploadRequest();

    LogSyncResponse response;
    LogDeliveryStatus deliveryStatus;
    deliveryStatus.requestId = request->requestId;
    deliveryStatus.result = SyncResponseResultType::SUCCESS;
    response.deliveryStatuses.set_array({ deliveryStatus });

    logCollector.onLogUploadResponse(response);
    testSleep(1);

    BOOST_CHECK_EQUAL(logStorage->onRemoveRecordBlock_, 1);
    BOOST_CHECK_EQUAL(logStorage->onNotifyUploadFailed_, 0);
    BOOST_CHECK_EQUAL(uploadStrategy->onIsUploadNeeded_, 1);
}

BOOST_AUTO_TEST_CASE(FailedDeliveryTest)
{
    KaaClientProperties properties;
    const size_t BATCH_SIZE = 100500;
    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    executor.init();
    LogCollector logCollector(&channelManager, executor, properties);
    CustomLoggingTransport transport(channelManager, logCollector);

    logCollector.setTransport(&transport);

    ILogStorage::RecordBlock block{ createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord()
                                  };

    ILogStorage::RecordPack recordPack(1, block);

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = recordPack;

    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->batchSize_ = BATCH_SIZE;
    uploadStrategy->decision_ = LogUploadStrategyDecision::NOOP;
    uploadStrategy->recordsBatchCount_ = 10;
    uploadStrategy->timeoutCheckPeriod_ = 10;
    uploadStrategy->logUploadCheckPeriod_ = 10;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

    auto request = logCollector.getLogUploadRequest();

    LogSyncResponse response;
    LogDeliveryStatus deliveryStatus;
    deliveryStatus.requestId = request->requestId;
    deliveryStatus.result = SyncResponseResultType::FAILURE;
    deliveryStatus.errorCode.set_LogDeliveryErrorCode(LogDeliveryErrorCode::APPENDER_INTERNAL_ERROR);
    response.deliveryStatuses.set_array({ deliveryStatus });

    logCollector.onLogUploadResponse(response);
    testSleep(1);

    BOOST_CHECK_EQUAL(logStorage->onNotifyUploadFailed_, 1);
    BOOST_CHECK_EQUAL(logStorage->onRemoveRecordBlock_, 0);
    BOOST_CHECK_EQUAL(uploadStrategy->onIsUploadNeeded_, 1);
}

BOOST_AUTO_TEST_CASE(TimeoutDetectionTest)
{
    KaaClientProperties properties;
    const size_t BATCH_SIZE = 100500;
    const size_t DELIVERY_TIMEOUT = 2;

    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    executor.init();
    LogCollector logCollector(&channelManager, executor, properties);
    CustomLoggingTransport transport(channelManager, logCollector);

    logCollector.setTransport(&transport);

    ILogStorage::RecordBlock block{ createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord()
                                  };

    ILogStorage::RecordPack recordPack(1, block);

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = recordPack;

    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->batchSize_ = BATCH_SIZE;
    uploadStrategy->timeout_ = DELIVERY_TIMEOUT;
    uploadStrategy->logUploadCheckPeriod_ = DELIVERY_TIMEOUT;
    uploadStrategy->timeoutCheckPeriod_ = 1;
    uploadStrategy->recordsBatchCount_ = 10;
    uploadStrategy->decision_ = LogUploadStrategyDecision::NOOP;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

    auto request = logCollector.getLogUploadRequest();

    BOOST_CHECK_EQUAL(uploadStrategy->onTimeout_, 0);
    BOOST_CHECK_EQUAL(uploadStrategy->onGetTimeout_, 1);

    std::this_thread::sleep_for(std::chrono::seconds(2 * DELIVERY_TIMEOUT));

    BOOST_CHECK_EQUAL(uploadStrategy->onTimeout_, 1);
    BOOST_CHECK_EQUAL(logStorage->onNotifyUploadFailed_, 1);
}

class RetryLogUploadStrategy : public MockLogUploadStrategy {
public:
    virtual void onFailure(ILogFailoverCommand& controller, LogDeliveryErrorCode code)
    {
        MockLogUploadStrategy::onFailure(controller, code);
        controller.retryLogUpload(retryTimeout_);
    }
};

BOOST_AUTO_TEST_CASE(RetryUploadTest)
{
    const size_t BATCH_SIZE = 100500;
    const size_t RETRY_TIMEOUT = 3;

    KaaClientProperties properties;
    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    executor.init();
    LogCollector logCollector(&channelManager, executor, properties);
    CustomLoggingTransport transport(channelManager, logCollector);

    logCollector.setTransport(&transport);

    ILogStorage::RecordBlock block{ createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord()
                                  };

    ILogStorage::RecordPack recordPack(1, block);

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = recordPack;

    std::shared_ptr<RetryLogUploadStrategy> uploadStrategy(new RetryLogUploadStrategy);
    uploadStrategy->batchSize_ = BATCH_SIZE;
    uploadStrategy->decision_ = LogUploadStrategyDecision::NOOP;
    uploadStrategy->retryTimeout_ = RETRY_TIMEOUT;
    uploadStrategy->recordsBatchCount_ = 10;
    uploadStrategy->timeoutCheckPeriod_ = 10;
    uploadStrategy->logUploadCheckPeriod_ = 10;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

    auto request = logCollector.getLogUploadRequest();

    LogSyncResponse response;
    LogDeliveryStatus deliveryStatus;
    deliveryStatus.requestId = request->requestId;
    deliveryStatus.result = SyncResponseResultType::FAILURE;
    deliveryStatus.errorCode.set_LogDeliveryErrorCode(LogDeliveryErrorCode::REMOTE_INTERNAL_ERROR);
    response.deliveryStatuses.set_array({ deliveryStatus });

    BOOST_CHECK_EQUAL(transport.onSync_, 0);

    logCollector.onLogUploadResponse(response);
    testSleep(1);

    BOOST_CHECK_EQUAL(logStorage->onNotifyUploadFailed_, 1);
    BOOST_CHECK_EQUAL(logStorage->onRemoveRecordBlock_, 0);
    BOOST_CHECK_EQUAL(uploadStrategy->onIsUploadNeeded_, 1);
    BOOST_CHECK_EQUAL(uploadStrategy->onFailure_, 1);
    BOOST_CHECK_EQUAL(transport.onSync_, 0);

    std::this_thread::sleep_for(std::chrono::seconds(RETRY_TIMEOUT + 1));

    BOOST_CHECK_EQUAL(transport.onSync_, 1);
}

BOOST_AUTO_TEST_SUITE_END()

}

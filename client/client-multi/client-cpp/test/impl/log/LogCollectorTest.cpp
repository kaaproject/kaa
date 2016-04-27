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

#include <memory>
#include <thread>
#include <chrono>
#include <cstdlib>
#include <list>

#include "kaa/log/LogRecord.hpp"
#include "kaa/log/LogCollector.hpp"
#include "kaa/log/LoggingTransport.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/logging/DefaultLogger.hpp"

#include "headers/MockKaaClientStateStorage.hpp"

#include "headers/log/MockLogStorage.hpp"
#include "headers/log/MockLogUploadStrategy.hpp"
#include "headers/log/MockLogDeliveryListener.hpp"
#include "headers/channel/MockChannelManager.hpp"
#include "headers/context/MockExecutorContext.hpp"

namespace kaa {

static KaaClientProperties tmp_properties;
static DefaultLogger tmp_logger(tmp_properties.getClientId());
static IKaaClientStateStoragePtr tmp_state(new MockKaaClientStateStorage);
static void testSleep(std::size_t seconds)
{
    std::this_thread::sleep_for(std::chrono::seconds(seconds));
}

class CustomLoggingTransport : public LoggingTransport {
public:
    CustomLoggingTransport(IKaaChannelManager &manager, ILogProcessor& logProcessor, IKaaClientContext &context)
        : LoggingTransport(manager, logProcessor, context) {}

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

static LogRecord createSerializedLogRecord()
{
    KaaUserLogRecord logRecord;
    logRecord.logdata = LOG_TEST_DATA;
    return LogRecord(logRecord);
}

static void joinThreadsSync(SimpleExecutorContext& executor)
{
    executor.getLifeCycleExecutor().shutdownNow();
    executor.getApiExecutor().shutdownNow();
    executor.getCallbackExecutor().shutdownNow();
}

BOOST_AUTO_TEST_SUITE(LogCollectorTestSuite)

BOOST_AUTO_TEST_CASE(BadLogStorageTest)
{
    KaaClientProperties properties;
    MockChannelManager channelManager;
    DefaultLogger tmp_logger(properties.getClientId());
    IKaaClientStateStoragePtr tmp_state(new MockKaaClientStateStorage);
    MockExecutorContext tmpExecContext;
    KaaClientContext clientContext(properties, tmp_logger, tmpExecContext, tmp_state);
    LogCollector logCollector(&channelManager, clientContext);

    ILogUploadStrategyPtr fakeStrategy;
    BOOST_CHECK_THROW(logCollector.setUploadStrategy(fakeStrategy), KaaException);
}

BOOST_AUTO_TEST_CASE(BadLogUploadStrategyTest)
{
    KaaClientProperties properties;
    MockChannelManager channelManager;
    DefaultLogger tmp_logger(properties.getClientId());
    IKaaClientStateStoragePtr tmp_state(new MockKaaClientStateStorage);
    MockExecutorContext tmpExecContext;
    KaaClientContext clientContext(properties, tmp_logger, tmpExecContext, tmp_state);
    LogCollector logCollector(&channelManager, clientContext);

    ILogStoragePtr fakeStorage;
    BOOST_CHECK_THROW(logCollector.setStorage(fakeStorage), KaaException);
}

BOOST_AUTO_TEST_CASE(AddLogRecordAndCheckStorageAndStrategyTest)
{
    KaaClientProperties properties;
    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    executor.init();
    KaaClientContext clientContext(properties, tmp_logger, executor, tmp_state);
    LogCollector logCollector(&channelManager, clientContext);
    CustomLoggingTransport transport(channelManager, logCollector, clientContext);

    logCollector.setTransport(&transport);

    KaaUserLogRecord logRecord = createLogRecord();
    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);

    uploadStrategy->timeoutCheckPeriod_ = USHRT_MAX;
    uploadStrategy->logUploadCheckPeriod_ = USHRT_MAX;
    uploadStrategy->maxParallelUploads_ = USHRT_MAX;
    uploadStrategy->decision_ = LogUploadStrategyDecision::NOOP;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

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
    KaaClientContext clientContext(properties, tmp_logger, executor, tmp_state);
    LogCollector logCollector(&channelManager, clientContext);
    CustomLoggingTransport transport(channelManager, logCollector, clientContext);

    logCollector.setTransport(&transport);

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = LogBucket();

    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->timeoutCheckPeriod_ = USHRT_MAX;
    uploadStrategy->logUploadCheckPeriod_ = USHRT_MAX;
    uploadStrategy->maxParallelUploads_ = USHRT_MAX;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

    BOOST_CHECK_EQUAL(logCollector.getLogUploadRequest(), std::shared_ptr<LogSyncRequest>());
}

BOOST_AUTO_TEST_CASE(CreateRequestWithLogsTest)
{
    KaaClientProperties properties;
    MockChannelManager channelManager;
    MockExecutorContext executor;
    KaaClientContext clientContext(properties, tmp_logger, executor, tmp_state);
    LogCollector logCollector(&channelManager, clientContext);
    CustomLoggingTransport transport(channelManager, logCollector, clientContext);

    logCollector.setTransport(&transport);

    std::list<LogRecord> logs{ createSerializedLogRecord(),
                               createSerializedLogRecord(),
                               createSerializedLogRecord(),
                               createSerializedLogRecord()
                             };

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = LogBucket(1, logs);

    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->timeoutCheckPeriod_ = USHRT_MAX;
    uploadStrategy->logUploadCheckPeriod_ = USHRT_MAX;
    uploadStrategy->maxParallelUploads_ = USHRT_MAX;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

    auto request = logCollector.getLogUploadRequest();

    BOOST_CHECK(request);
    BOOST_CHECK(!request->logEntries.is_null());
    BOOST_CHECK_EQUAL(request->logEntries.get_array().size(), logs.size());
}

BOOST_AUTO_TEST_CASE(SuccessDeliveryTest)
{
    KaaClientProperties properties;
    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    executor.init();
    KaaClientContext clientContext(properties, tmp_logger, executor, tmp_state);
    LogCollector logCollector(&channelManager, clientContext);
    CustomLoggingTransport transport(channelManager, logCollector, clientContext);

    logCollector.setTransport(&transport);

    std::list<LogRecord> logs{ createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord()
                                  };

    LogBucket bucket(1, std::move(logs));

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = bucket;

    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->timeoutCheckPeriod_ = USHRT_MAX;
    uploadStrategy->logUploadCheckPeriod_ = USHRT_MAX;
    uploadStrategy->maxParallelUploads_ = USHRT_MAX;
    uploadStrategy->decision_ = LogUploadStrategyDecision::NOOP;

    auto logDeliveryListener = std::make_shared<MockLogDeliveryListener>();

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);
    logCollector.setLogDeliveryListener(logDeliveryListener);

    auto request = logCollector.getLogUploadRequest();

    LogSyncResponse response;
    LogDeliveryStatus deliveryStatus;
    deliveryStatus.requestId = request->requestId;
    deliveryStatus.result = SyncResponseResultType::SUCCESS;
    response.deliveryStatuses.set_array({ deliveryStatus });

    logCollector.onLogUploadResponse(response);
    testSleep(1);

    BOOST_CHECK_EQUAL(uploadStrategy->onIsUploadNeeded_, 1);
    BOOST_CHECK_EQUAL(logStorage->onRemoveBucket_, 1);
    BOOST_CHECK_EQUAL(logDeliveryListener->onSuccess_, 1);
    BOOST_CHECK_EQUAL(logStorage->onRollbackBucket_, 0);
}

BOOST_AUTO_TEST_CASE(FailedDeliveryTest)
{
    KaaClientProperties properties;
    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    executor.init();
    KaaClientContext clientContext(properties, tmp_logger, executor, tmp_state);
    LogCollector logCollector(&channelManager, clientContext);
    CustomLoggingTransport transport(channelManager, logCollector, clientContext);

    logCollector.setTransport(&transport);

    std::list<LogRecord> logs{ createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord()
                                  };

    LogBucket bucket(1, std::move(logs));

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = bucket;

    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->timeoutCheckPeriod_ = USHRT_MAX;
    uploadStrategy->logUploadCheckPeriod_ = USHRT_MAX;
    uploadStrategy->maxParallelUploads_ = USHRT_MAX;
    uploadStrategy->decision_ = LogUploadStrategyDecision::NOOP;

    auto logDeliveryListener = std::make_shared<MockLogDeliveryListener>();

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);
    logCollector.setLogDeliveryListener(logDeliveryListener);

    auto request = logCollector.getLogUploadRequest();

    LogSyncResponse response;
    LogDeliveryStatus deliveryStatus;
    deliveryStatus.requestId = request->requestId;
    deliveryStatus.result = SyncResponseResultType::FAILURE;
    deliveryStatus.errorCode.set_LogDeliveryErrorCode(LogDeliveryErrorCode::APPENDER_INTERNAL_ERROR);
    response.deliveryStatuses.set_array({ deliveryStatus });

    logCollector.onLogUploadResponse(response);
    testSleep(1);

    BOOST_CHECK_EQUAL(logStorage->onRollbackBucket_, 1);
    BOOST_CHECK_EQUAL(logStorage->onRemoveBucket_, 0);
    BOOST_CHECK_EQUAL(uploadStrategy->onIsUploadNeeded_, 1);
    BOOST_CHECK_EQUAL(logDeliveryListener->onFailure_, 1);
}

BOOST_AUTO_TEST_CASE(TimeoutDetectionTest)
{
    const size_t DELIVERY_TIMEOUT = 2;

    KaaClientProperties properties;
    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    executor.init();
    KaaClientContext clientContext(properties, tmp_logger, executor, tmp_state);
    LogCollector logCollector(&channelManager, clientContext);
    CustomLoggingTransport transport(channelManager, logCollector, clientContext);

    logCollector.setTransport(&transport);

    std::list<LogRecord> logs{ createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord()
                                  };

    LogBucket bucket(1, logs);
    LogBucket bucket2(2, logs);

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = bucket;

    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->timeout_ = DELIVERY_TIMEOUT;
    uploadStrategy->logUploadCheckPeriod_ = DELIVERY_TIMEOUT;
    uploadStrategy->timeoutCheckPeriod_ = 1;
    uploadStrategy->decision_ = LogUploadStrategyDecision::NOOP;
    uploadStrategy->maxParallelUploads_ = USHRT_MAX;

    auto logDeliveryListener = std::make_shared<MockLogDeliveryListener>();

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);
    logCollector.setLogDeliveryListener(logDeliveryListener);

    auto request = logCollector.getLogUploadRequest();

    BOOST_CHECK_EQUAL(uploadStrategy->onTimeout_, 0);
    BOOST_CHECK_EQUAL(uploadStrategy->onGetTimeout_, 1);

    //increase delivery timeout so that bucket2 won't expire
    uploadStrategy->timeout_ = DELIVERY_TIMEOUT * 3;
    logStorage->recordPack_ = bucket2;
    request = logCollector.getLogUploadRequest();

    BOOST_CHECK_EQUAL(uploadStrategy->onGetTimeout_, 2);

    std::this_thread::sleep_for(std::chrono::seconds(2 * DELIVERY_TIMEOUT));

    BOOST_CHECK_EQUAL(uploadStrategy->onTimeout_, 1);
    // Timeout which orrured on first bucket should trigger AP switch
    // The second bucket should be rolled back because of AP switch
    BOOST_CHECK_EQUAL(logStorage->onRollbackBucket_, 2);
    BOOST_CHECK_EQUAL(logDeliveryListener->onTimeout_, 1);
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
    const size_t RETRY_TIMEOUT = 3;

    KaaClientProperties properties;
    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    executor.init();
    KaaClientContext clientContext(properties, tmp_logger, executor, tmp_state);
    LogCollector logCollector(&channelManager, clientContext);
    CustomLoggingTransport transport(channelManager, logCollector, clientContext);

    logCollector.setTransport(&transport);

    std::list<LogRecord> logs{ createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord()
                                  };

    LogBucket bucket(1, (logs));

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = bucket;

    std::shared_ptr<RetryLogUploadStrategy> uploadStrategy(new RetryLogUploadStrategy);
    uploadStrategy->decision_ = LogUploadStrategyDecision::NOOP;
    uploadStrategy->retryTimeout_ = RETRY_TIMEOUT;
    uploadStrategy->timeoutCheckPeriod_ = USHRT_MAX;
    uploadStrategy->logUploadCheckPeriod_ = USHRT_MAX;
    uploadStrategy->maxParallelUploads_ = USHRT_MAX;

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

    BOOST_CHECK_EQUAL(logStorage->onRollbackBucket_, 1);
    BOOST_CHECK_EQUAL(logStorage->onRemoveBucket_, 0);
    BOOST_CHECK_EQUAL(uploadStrategy->onIsUploadNeeded_, 1);
    BOOST_CHECK_EQUAL(uploadStrategy->onFailure_, 1);
    BOOST_CHECK_EQUAL(transport.onSync_, 0);

    std::this_thread::sleep_for(std::chrono::seconds(RETRY_TIMEOUT + 1));

    BOOST_CHECK_EQUAL(transport.onSync_, 1);
}

BOOST_AUTO_TEST_CASE(MaxLogUploadLimitWithSyncAll)
{
    KaaClientProperties properties;
    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    KaaClientContext clientContext(properties, tmp_logger, executor, tmp_state);
    executor.init();
    LogCollector logCollector(&channelManager, clientContext);
    CustomLoggingTransport transport(channelManager, logCollector, clientContext);

    logCollector.setTransport(&transport);

    std::list<LogRecord> logs{ createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord()
                                  };

    LogBucket bucket(1, logs);

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = bucket;

    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->timeout_ = USHRT_MAX;
    uploadStrategy->logUploadCheckPeriod_ = USHRT_MAX;
    uploadStrategy->timeoutCheckPeriod_ = USHRT_MAX;
    uploadStrategy->decision_ = LogUploadStrategyDecision::NOOP;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

    auto testMaxParallelUpload =
        [&] (int maxParallelUpload)
            {
                std::int32_t disallowedRequestId = maxParallelUpload;
                uploadStrategy->maxParallelUploads_ = maxParallelUpload;

                std::list<std::int32_t> requestIds;
                for (std::int32_t i = 0; i < maxParallelUpload; ++i) {
                    logStorage->recordPack_ = LogBucket(i, logs);
                    auto request = logCollector.getLogUploadRequest();
                    BOOST_CHECK(request);
                    requestIds.push_back(request->requestId);
                }

                logStorage->recordPack_ = LogBucket(disallowedRequestId, logs);
                BOOST_CHECK(!logCollector.getLogUploadRequest());

                if (requestIds.empty() && !maxParallelUpload) {
                    return;
                }

                LogSyncResponse response;
                std::vector<LogDeliveryStatus > statuses;
                statuses.reserve(requestIds.size());

                for (const auto& requestId : requestIds) {
                    LogDeliveryStatus status;
                    status.requestId = requestId;
                    status.result = SyncResponseResultType::SUCCESS;
                    statuses.push_back(status);
                }

                response.deliveryStatuses.set_array(statuses);
                logCollector.onLogUploadResponse(response);
            };

    testMaxParallelUpload(0);

    testMaxParallelUpload(3);

    testMaxParallelUpload(5);

    joinThreadsSync(executor);
}

BOOST_AUTO_TEST_CASE(MaxLogUploadLimitWithSyncLogging)
{
    KaaClientProperties properties;
    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    executor.init();
    KaaClientContext clientContext(properties, tmp_logger, executor, tmp_state);
    LogCollector logCollector(&channelManager, clientContext);
    CustomLoggingTransport transport(channelManager, logCollector, clientContext);

    logCollector.setTransport(&transport);

    std::list<LogRecord> logs{ createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord(),
                                    createSerializedLogRecord()
                                  };

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = LogBucket(1, logs);

    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->timeout_ = USHRT_MAX;
    uploadStrategy->logUploadCheckPeriod_ = USHRT_MAX;
    uploadStrategy->timeoutCheckPeriod_ = USHRT_MAX;
    uploadStrategy->decision_ = LogUploadStrategyDecision::UPLOAD;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

    auto testMaxParallelUpload =
        [&] (int maxParallelUpload)
            {
                transport.onSync_ = 0;
                uploadStrategy->maxParallelUploads_ = maxParallelUpload;

                std::list<std::int32_t> requestIds;
                for (std::int32_t i = 0; i < maxParallelUpload; ++i) {
                    logStorage->recordPack_ = LogBucket(i, logs);
                    logCollector.addLogRecord(createLogRecord());
                }

                testSleep(1);
                BOOST_CHECK_EQUAL(transport.onSync_, maxParallelUpload);
            };

    testMaxParallelUpload(0);

    testMaxParallelUpload(3);

    testMaxParallelUpload(5);

    joinThreadsSync(executor);
}

BOOST_AUTO_TEST_CASE(RecordFuturesResult)
{
    std::srand(std::time(nullptr));

    KaaClientProperties properties;
    MockChannelManager channelManager;
    SimpleExecutorContext executor;
    executor.init();
    KaaClientContext clientContext(properties, tmp_logger, executor, tmp_state);
    LogCollector logCollector(&channelManager, clientContext);
    CustomLoggingTransport transport(channelManager, logCollector, clientContext);

    logCollector.setTransport(&transport);

    std::int32_t bucketId = rand();
    std::size_t recordCount = 5/*1 + rand() % 10*/;

    std::shared_ptr<MockLogStorage> logStorage(new MockLogStorage);
    logStorage->recordPack_ = LogBucket(bucketId, { createSerializedLogRecord() });
    logStorage->bucketInfo_ = BucketInfo(bucketId, recordCount);

    std::shared_ptr<MockLogUploadStrategy> uploadStrategy(new MockLogUploadStrategy);
    uploadStrategy->timeout_ = USHRT_MAX;
    uploadStrategy->logUploadCheckPeriod_ = USHRT_MAX;
    uploadStrategy->timeoutCheckPeriod_ = USHRT_MAX;
    uploadStrategy->maxParallelUploads_ = USHRT_MAX;
    uploadStrategy->decision_ = LogUploadStrategyDecision::NOOP;

    logCollector.setStorage(logStorage);
    logCollector.setUploadStrategy(uploadStrategy);

    std::list<RecordFuture> recordFutures;

    for (std::size_t i = 0; i < recordCount; ++i) {
        recordFutures.push_back(logCollector.addLogRecord(createLogRecord()));
    }

    while (logStorage->onAddLogRecord_ < recordCount) {
        testSleep(1);
    }

    auto request = logCollector.getLogUploadRequest();

    LogSyncResponse response;
    LogDeliveryStatus status;
    status.requestId = bucketId;
    status.result = SyncResponseResultType::SUCCESS;
    response.deliveryStatuses.set_array({ status });
    logCollector.onLogUploadResponse(response);

    for (auto &f : recordFutures) {
        try {
            auto bucketInfo = f.get().getBucketInfo();
            BOOST_CHECK_EQUAL(bucketInfo.getBucketId(), bucketId);
            BOOST_CHECK_EQUAL(bucketInfo.getLogCount(), recordCount);
        } catch (...) {
            BOOST_CHECK(false);
        }
    }

    joinThreadsSync(executor);
}

BOOST_AUTO_TEST_SUITE_END()

}

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

#include <thread>
#include <chrono>
#include <memory>

#include "kaa/log/DefaultLogUploadStrategy.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/logging/DefaultLogger.hpp"

#include "headers/MockKaaClientStateStorage.hpp"
#include "headers/context/MockExecutorContext.hpp"

#include "headers/log/MockLogFailoverCommand.hpp"
#include "headers/log/MockLogStorageStatus.hpp"

namespace kaa {

static KaaClientProperties properties;
static DefaultLogger tmp_logger(properties.getClientId());
static IKaaClientStateStoragePtr tmp_state(new MockKaaClientStateStorage);
static MockExecutorContext tmpExecContext;
static KaaClientContext clientContext(properties, tmp_logger, tmpExecContext, tmp_state);

BOOST_AUTO_TEST_SUITE(DefaultLogUploadStrategyTestSuite)

BOOST_AUTO_TEST_CASE(GetSetUplaodTimeoutTest)
{
    MockLogFailoverCommand failoverCommand;
    DefaultLogUploadStrategy strategy(clientContext);

    BOOST_CHECK_EQUAL(strategy.getTimeout(), DefaultLogUploadStrategy::DEFAULT_UPLOAD_TIMEOUT);

    std::size_t uploadTimeout = std::rand();
    strategy.setUploadTimeout(uploadTimeout);

    BOOST_CHECK_EQUAL(strategy.getTimeout(), uploadTimeout);
}

BOOST_AUTO_TEST_CASE(GetSetMaxParallelUploads)
{
    MockLogFailoverCommand failoverCommand;
    DefaultLogUploadStrategy strategy(clientContext);

    BOOST_CHECK_EQUAL(strategy.getMaxParallelUploads(), DefaultLogUploadStrategy::DEFAULT_MAX_PARALLEL_UPLOADS);

    std::size_t count = std::rand();
    strategy.setMaxParallelUploads(count);

    BOOST_CHECK_EQUAL(strategy.getMaxParallelUploads(), count);
}

BOOST_AUTO_TEST_CASE(UploadByOccupiedSizeTest)
{
    const std::size_t THRESHOLD_SIZE = 35;
    const std::size_t THRESHOLD_COUNT = 100500;

    MockLogStorageStatus logStorageStatus;
    logStorageStatus.consumedVolume_ = THRESHOLD_SIZE - 1;
    logStorageStatus.recordsCount_ = THRESHOLD_COUNT - 1;

    MockLogFailoverCommand failoverCommand;
    DefaultLogUploadStrategy strategy(clientContext);;

    strategy.setVolumeThreshold(THRESHOLD_SIZE);
    strategy.setCountThreshold(THRESHOLD_COUNT);

    BOOST_CHECK(strategy.isUploadNeeded(logStorageStatus) == LogUploadStrategyDecision::NOOP);

    logStorageStatus.consumedVolume_ = THRESHOLD_SIZE;
    BOOST_CHECK(strategy.isUploadNeeded(logStorageStatus) == LogUploadStrategyDecision::UPLOAD);

    logStorageStatus.consumedVolume_ = THRESHOLD_SIZE + 1;
    BOOST_CHECK(strategy.isUploadNeeded(logStorageStatus) == LogUploadStrategyDecision::UPLOAD);
}

BOOST_AUTO_TEST_CASE(UploadByRecordCountTest)
{
    const std::size_t THRESHOLD_SIZE = 35;
    const std::size_t THRESHOLD_COUNT = 100500;

    MockLogStorageStatus logStorageStatus;
    logStorageStatus.consumedVolume_ = THRESHOLD_SIZE - 1;
    logStorageStatus.recordsCount_ = THRESHOLD_COUNT - 1;

    MockLogFailoverCommand failoverCommand;
    DefaultLogUploadStrategy strategy(clientContext);;

    strategy.setVolumeThreshold(THRESHOLD_SIZE);
    strategy.setCountThreshold(THRESHOLD_COUNT);

    BOOST_CHECK(strategy.isUploadNeeded(logStorageStatus) == LogUploadStrategyDecision::NOOP);

    logStorageStatus.recordsCount_ = THRESHOLD_COUNT;
    BOOST_CHECK(strategy.isUploadNeeded(logStorageStatus) == LogUploadStrategyDecision::UPLOAD);

    logStorageStatus.recordsCount_ = THRESHOLD_COUNT + 1;
    BOOST_CHECK(strategy.isUploadNeeded(logStorageStatus) == LogUploadStrategyDecision::UPLOAD);
}

BOOST_AUTO_TEST_CASE(OnFailureTest)
{
    MockLogFailoverCommand failoverCommand;
    DefaultLogUploadStrategy strategy(clientContext);;

    const size_t RETRY_PERIOD = 2;
    const std::size_t THRESHOLD_SIZE = 35;
    const std::size_t THRESHOLD_COUNT = 100500;

    MockLogStorageStatus logStorageStatus;
    logStorageStatus.consumedVolume_ = THRESHOLD_SIZE;
    logStorageStatus.recordsCount_ = THRESHOLD_COUNT;

    strategy.setRetryPeriod(RETRY_PERIOD);
    strategy.setVolumeThreshold(THRESHOLD_SIZE);
    strategy.setCountThreshold(THRESHOLD_COUNT);

    BOOST_CHECK(strategy.isUploadNeeded(logStorageStatus) == LogUploadStrategyDecision::UPLOAD);

    strategy.onFailure(failoverCommand, LogDeliveryErrorCode::REMOTE_CONNECTION_ERROR);

    BOOST_CHECK(failoverCommand.onRetryLogUploadWithDelay_ == 1);
    BOOST_CHECK(failoverCommand.onRetryLogUpload_ == 0);
    BOOST_CHECK(failoverCommand.onSwitchAccessPoint_ == 0);
    BOOST_CHECK(strategy.isUploadNeeded(logStorageStatus) == LogUploadStrategyDecision::NOOP);

    std::this_thread::sleep_for(std::chrono::seconds(RETRY_PERIOD / 2));
    BOOST_CHECK(strategy.isUploadNeeded(logStorageStatus) == LogUploadStrategyDecision::NOOP);

    std::this_thread::sleep_for(std::chrono::seconds(RETRY_PERIOD / 2));
    BOOST_CHECK(strategy.isUploadNeeded(logStorageStatus) == LogUploadStrategyDecision::UPLOAD);
}

BOOST_AUTO_TEST_CASE(OnTimeoutTest)
{
    MockLogFailoverCommand failoverCommand;
    DefaultLogUploadStrategy strategy(clientContext);;

    strategy.onTimeout(failoverCommand);

    BOOST_CHECK(failoverCommand.onSwitchAccessPoint_ == 1);
    BOOST_CHECK(failoverCommand.onRetryLogUploadWithDelay_ == 0);
    BOOST_CHECK(failoverCommand.onRetryLogUpload_ == 0);
}

BOOST_AUTO_TEST_SUITE_END()

}

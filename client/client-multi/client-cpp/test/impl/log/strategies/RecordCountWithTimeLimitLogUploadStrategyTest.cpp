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
#include <cstdlib>

#include "kaa/log/strategies/RecordCountWithTimeLimitLogUploadStrategy.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/logging/DefaultLogger.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"

#include "headers/MockKaaClientStateStorage.hpp"
#include "headers/context/MockExecutorContext.hpp"

#include "headers/log/MockLogStorage.hpp"

namespace kaa {

static void sleepFor(std::size_t seconds)
{
    std::this_thread::sleep_for(std::chrono::seconds(seconds));
}

static std::size_t getRand()
{
    return std::rand() + 1;
}

static KaaClientProperties properties;
static DefaultLogger tmp_logger(properties.getClientId());
static MockExecutorContext tmpExecContext;
static IKaaClientStateStoragePtr tmp_state(new MockKaaClientStateStorage);
static KaaClientContext clientContext(properties, tmp_logger, tmpExecContext, tmp_state);

BOOST_AUTO_TEST_SUITE(RecordCountWithTimeLimitLogUploadStrategySuite)

BOOST_AUTO_TEST_CASE(TriggerByRecordCountTest)
{
    std::size_t logUploadPeriod = 2;
    std::size_t thresholdRecordCount = getRand();

    RecordCountWithTimeLimitLogUploadStrategy strategy(thresholdRecordCount, logUploadPeriod, clientContext);

    MockLogStorageStatus storageStatus1;
    storageStatus1.recordsCount_ = thresholdRecordCount - 1;

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus1) ==  LogUploadStrategyDecision::NOOP);

    MockLogStorageStatus storageStatus2;
    storageStatus2.recordsCount_ = thresholdRecordCount;

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus2) ==  LogUploadStrategyDecision::UPLOAD);

    MockLogStorageStatus storageStatus3;
    storageStatus3.recordsCount_ = thresholdRecordCount + getRand();

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus3) ==  LogUploadStrategyDecision::UPLOAD);
}

BOOST_AUTO_TEST_CASE(TriggerByTimeTest)
{
    std::size_t logUploadPeriod = 2;
    std::size_t thresholdRecordCount = getRand();

    RecordCountWithTimeLimitLogUploadStrategy strategy(thresholdRecordCount, logUploadPeriod, clientContext);

    MockLogStorageStatus storageStatus;
    storageStatus.consumedVolume_ = 0;
    storageStatus.recordsCount_ = 0;

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus) ==  LogUploadStrategyDecision::NOOP);

    sleepFor(logUploadPeriod);

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus) == LogUploadStrategyDecision::UPLOAD);

    sleepFor(logUploadPeriod / 2);

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus) ==  LogUploadStrategyDecision::NOOP);

    sleepFor(logUploadPeriod / 2);

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus) == LogUploadStrategyDecision::UPLOAD);
}

BOOST_AUTO_TEST_SUITE_END()

}

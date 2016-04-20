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

#include "kaa/log/strategies/PeriodicLogUploadStrategy.hpp"
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

static KaaClientProperties properties;
static DefaultLogger tmp_logger(properties.getClientId());
static IKaaClientStateStoragePtr tmp_state(new MockKaaClientStateStorage);
static MockExecutorContext tmpExecContext;
static KaaClientContext clientContext(properties, tmp_logger, tmpExecContext, tmp_state);

BOOST_AUTO_TEST_SUITE(PeriodicLogUploadStrategySuite)

BOOST_AUTO_TEST_CASE(CheckAfterSomeTime)
{
    std::size_t logUploadPeriod = 2;

    MockLogStorageStatus storageStatus;
    storageStatus.consumedVolume_ = std::rand();
    storageStatus.recordsCount_ = std::rand();

    PeriodicLogUploadStrategy strategy(logUploadPeriod, clientContext);

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus) == LogUploadStrategyDecision::NOOP);

    sleepFor(logUploadPeriod / 2);

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus) == LogUploadStrategyDecision::NOOP);

    sleepFor(logUploadPeriod / 2);

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus) == LogUploadStrategyDecision::UPLOAD);

    sleepFor(logUploadPeriod / 2);

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus) == LogUploadStrategyDecision::NOOP);

    sleepFor(logUploadPeriod / 2);

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus) == LogUploadStrategyDecision::UPLOAD);
}

BOOST_AUTO_TEST_SUITE_END()

}

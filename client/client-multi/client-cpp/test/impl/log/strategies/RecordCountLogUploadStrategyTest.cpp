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

#include <cstdlib>

#include "kaa/log/strategies/RecordCountLogUploadStrategy.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/logging/DefaultLogger.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"

#include "headers/MockKaaClientStateStorage.hpp"
#include "headers/context/MockExecutorContext.hpp"

#include "headers/log/MockLogStorage.hpp"

namespace kaa {

static std::size_t getRand()
{
    return std::rand() + 1;
}

static KaaClientProperties properties;
static DefaultLogger tmp_logger(properties.getClientId());
static IKaaClientStateStoragePtr tmp_state(new MockKaaClientStateStorage);
static MockExecutorContext tmpExecContext;
static KaaClientContext clientContext(properties, tmp_logger, tmpExecContext, tmp_state);

BOOST_AUTO_TEST_SUITE(RecordCountLogUploadStrategySuite)

BOOST_AUTO_TEST_CASE(CurrentRecordCountLessThanThresholdCount)
{
    std::size_t thresholdRecordCount = getRand();

    MockLogStorageStatus storageStatus;
    storageStatus.recordsCount_ = thresholdRecordCount - 1;

    RecordCountLogUploadStrategy strategy(thresholdRecordCount, clientContext);

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus) ==  LogUploadStrategyDecision::NOOP);
}

BOOST_AUTO_TEST_CASE(CurrentRecordCountEqualToThresholdCount)
{
    std::size_t thresholdRecordCount = getRand();

    MockLogStorageStatus storageStatus;
    storageStatus.recordsCount_ = thresholdRecordCount;

    RecordCountLogUploadStrategy strategy(thresholdRecordCount, clientContext);

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus) ==  LogUploadStrategyDecision::UPLOAD);
}

BOOST_AUTO_TEST_CASE(CurrentRecordCountGreaterThanThresholdCount)
{
    std::size_t thresholdRecordCount = getRand();

    MockLogStorageStatus storageStatus;
    storageStatus.recordsCount_ = thresholdRecordCount + getRand();

    RecordCountLogUploadStrategy strategy(thresholdRecordCount, clientContext);

    BOOST_CHECK(strategy.isUploadNeeded(storageStatus) ==  LogUploadStrategyDecision::UPLOAD);
}

BOOST_AUTO_TEST_SUITE_END()

}

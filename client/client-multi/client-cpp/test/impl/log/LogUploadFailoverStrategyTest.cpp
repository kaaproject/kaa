/*
 * Copyright 2014 CyberVision, Inc.
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

#include <kaa/log/LogUploadFailoverStrategy.hpp>

#include "headers/channel/MockDataChannel.hpp"
#include "headers/channel/MockChannelManager.hpp"

namespace kaa
{

class TestChannelManager : public MockChannelManager
{
public:
    TestChannelManager() : channel_(nullptr), onServerFailed_(false) {}

    virtual IDataChannelPtr getChannelByTransportType(TransportType type) {
        return channel_;
    }

    virtual void onServerFailed(ITransportConnectionInfoPtr server) {
        onServerFailed_ = true;
    }

    IDataChannelPtr channel_;
    bool onServerFailed_;
};

BOOST_AUTO_TEST_SUITE(LogUploadFailoverStrategyTest)

BOOST_AUTO_TEST_CASE(testSwitchServerFailure)
{
    TestChannelManager channelManager;
    LogUploadFailoverStrategy strategy(&channelManager);

    strategy.onTimeout();

    BOOST_CHECK(!channelManager.onServerFailed_);
}

BOOST_AUTO_TEST_CASE(testSwitchServerSuccess)
{
    MockDataChannel channel;
    TestChannelManager channelManager;
    channelManager.channel_ = &channel;

    LogUploadFailoverStrategy strategy(&channelManager);

    strategy.onTimeout();

    BOOST_CHECK(channelManager.onServerFailed_);
}

BOOST_AUTO_TEST_CASE(testLogUploadFailure)
{
    TestChannelManager channelManager;
    LogUploadFailoverStrategy strategy(&channelManager);

    BOOST_CHECK(strategy.isUploadApproved());

    strategy.onFailure(LogDeliveryErrorCode::APPENDER_INTERNAL_ERROR);

    BOOST_CHECK(!strategy.isUploadApproved());
}

BOOST_AUTO_TEST_CASE(testRetry)
{
    uint32_t retryPeriod = 2; // in seconds
    TestChannelManager channelManager;
    LogUploadFailoverStrategy strategy(&channelManager);
    strategy.setRetryPeriod(retryPeriod);

    strategy.onFailure(LogDeliveryErrorCode::APPENDER_INTERNAL_ERROR);

    BOOST_CHECK(!strategy.isUploadApproved());

    std::this_thread::sleep_for(std::chrono::seconds(retryPeriod / 2));
    BOOST_CHECK(!strategy.isUploadApproved());
    std::this_thread::sleep_for(std::chrono::seconds(retryPeriod / 2));
    BOOST_CHECK(strategy.isUploadApproved());
}

BOOST_AUTO_TEST_SUITE_END()

}


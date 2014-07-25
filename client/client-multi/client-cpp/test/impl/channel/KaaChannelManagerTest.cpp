/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      ChannelType::HTTP://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <boost/test/unit_test.hpp>

#include "kaa/channel/KaaChannelManager.hpp"

#include "headers/channel/MockDataChannel.hpp"
#include "headers/bootstrap/MockBootstrapManager.hpp"

namespace kaa {

class UserDataChannel : public MockDataChannel {
public:
    virtual const std::string& getId() const {
        return id_;
    }

    virtual ChannelType getType() const {
        return channelType_;
    }

    virtual const std::map<TransportType, ChannelDirection>& getSupportedTransportTypes() const {
        static std::map<TransportType, ChannelDirection> types;
        if (!types.empty()) {
            types.clear();
        }

        types[transportType_] = ChannelDirection::BIDIRECTIONAL;
        return types;
    }

    virtual void setServer(IServerInfoPtr server) {
        server_ = server;
    }

public:
    std::string       id_;
    ChannelType       channelType_;
    TransportType    transportType_;

    IServerInfoPtr    server_;
};

BOOST_AUTO_TEST_SUITE(ChannelManagerTestSuite)

BOOST_AUTO_TEST_CASE(AddChannelTest)
{
    MockBootstrapManager BootstrapManager;
    KaaChannelManager channelManager(BootstrapManager);

    IDataChannelPtr fakeChannel(nullptr);
    BOOST_CHECK_THROW(channelManager.addChannel(fakeChannel), KaaException);

    const std::string ch1Id("id1");
    UserDataChannel* tmp = new UserDataChannel;
    tmp->id_ = ch1Id;
    tmp->channelType_ = ChannelType::HTTP;
    IDataChannelPtr ch1(tmp);

    const std::string ch2Id("id2");
    tmp = new UserDataChannel;
    tmp->id_ = ch2Id;
    tmp->channelType_ = ChannelType::HTTP_LP;
    IDataChannelPtr ch2(tmp);

    std::list<IDataChannelPtr> expectedList = {ch1, ch2};

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);

    const auto& actualList = channelManager.getChannels();

    BOOST_CHECK(actualList.size() == expectedList.size());

    for (const auto& channel : actualList) {
        bool found = false;
        for (auto it = expectedList.begin(); it != expectedList.end() && !found; ++it) {
            if (channel->getId() == (*it)->getId()) {
                found = true;
            }
        }

        BOOST_CHECK(found);
    }
}

BOOST_AUTO_TEST_CASE(RemoveChannelTest)
{
    MockBootstrapManager BootstrapManager;
    KaaChannelManager channelManager(BootstrapManager);

    IDataChannelPtr fakeChannel(nullptr);
    BOOST_CHECK_THROW(channelManager.removeChannel(fakeChannel), KaaException);

    const std::string ch1Id("id1");
    UserDataChannel* tmp = new UserDataChannel;
    tmp->id_ = ch1Id;
    tmp->channelType_ = ChannelType::HTTP;
    IDataChannelPtr ch1(tmp);

    const std::string ch2Id("id2");
    tmp = new UserDataChannel;
    tmp->id_ = ch2Id;
    tmp->channelType_ = ChannelType::HTTP_LP;
    IDataChannelPtr ch2(tmp);

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);

    channelManager.removeChannel(ch2);

    BOOST_CHECK(channelManager.getChannels().size() == 1);
    BOOST_CHECK(channelManager.getChannels().front()->getType() == ch1->getType());
}

BOOST_AUTO_TEST_CASE(GetChannelBySomeCriteriaTest)
{
    MockBootstrapManager BootstrapManager;
    KaaChannelManager channelManager(BootstrapManager);

    auto bootstrapCh = channelManager.getChannelsByType(ChannelType::BOOTSTRAP);
    BOOST_CHECK(bootstrapCh.empty());

    auto configurationCh = channelManager.getChannelByTransportType(TransportType::CONFIGURATION);
    BOOST_CHECK(!configurationCh);

    auto fakeIdCh = channelManager.getChannel("fake");
    BOOST_CHECK(!fakeIdCh);

    const std::string ch1Id("id1");
    UserDataChannel* tmp = new UserDataChannel;
    tmp->id_ = ch1Id;
    tmp->channelType_ = ChannelType::HTTP;
    tmp->transportType_ = TransportType::LOGGING;
    IDataChannelPtr ch1(tmp);

    const std::string ch2Id("id2");
    tmp = new UserDataChannel;
    tmp->id_ = ch2Id;
    tmp->channelType_ = ChannelType::HTTP_LP;
    tmp->transportType_ = TransportType::NOTIFICATION;
    IDataChannelPtr ch2(tmp);

    const std::string ch3Id("id3");
    tmp = new UserDataChannel;
    tmp->id_ = ch3Id;
    tmp->channelType_ = ChannelType::BOOTSTRAP;
    tmp->transportType_ = TransportType::LOGGING;
    IDataChannelPtr ch3(tmp);

    const std::string ch4Id("id4");
    tmp = new UserDataChannel;
    tmp->id_ = ch4Id;
    tmp->channelType_ = ChannelType::HTTP_LP;
    tmp->transportType_ = TransportType::EVENT;
    IDataChannelPtr ch4(tmp);

    channelManager.addChannel(ch3);
    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);
    channelManager.addChannel(ch4);

    auto idCh = channelManager.getChannel(ch3->getId());
    BOOST_CHECK(idCh->getType() == ch3->getType());

    auto transportTypeCh = channelManager.getChannelByTransportType(TransportType::NOTIFICATION);
    BOOST_CHECK(transportTypeCh->getId() == ch2->getId());

    std::list<IDataChannelPtr> expectedList = {ch2, ch4};
    auto channelTypeList = channelManager.getChannelsByType(ChannelType::HTTP_LP);
    BOOST_CHECK(expectedList.size() == channelTypeList.size());

    for (const auto& channel : channelTypeList) {
        bool found = false;
        for (auto it = expectedList.begin(); it != expectedList.end() && !found; ++it) {
            if (channel->getId() == (*it)->getId()) {
                found = true;
            }
        }

        BOOST_CHECK(found);
    }
}

BOOST_AUTO_TEST_CASE(ClearChannelsTest)
{
    MockBootstrapManager BootstrapManager;
    KaaChannelManager channelManager(BootstrapManager);

    const std::string ch1Id("id1");
    UserDataChannel* tmp = new UserDataChannel;
    tmp->id_ = ch1Id;
    tmp->channelType_ = ChannelType::HTTP;
    IDataChannelPtr ch1(tmp);

    const std::string ch2Id("id2");
    tmp = new UserDataChannel;
    tmp->id_ = ch2Id;
    tmp->channelType_ = ChannelType::HTTP_LP;
    IDataChannelPtr ch2(tmp);

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);

    channelManager.clearChannelList();

    BOOST_CHECK(channelManager.getChannels().empty());
}

BOOST_AUTO_TEST_CASE(ServerUpdateTest)
{
    MockBootstrapManager BootstrapManager;
    KaaChannelManager channelManager(BootstrapManager);

    IServerInfoPtr fakeServer;
    BOOST_CHECK_THROW(channelManager.onServerUpdated(fakeServer), KaaException);

    const std::string ch1Id("id1");
    UserDataChannel* userCh1 = new UserDataChannel;
    userCh1->id_ = ch1Id;
    userCh1->channelType_ = ChannelType::BOOTSTRAP;
    userCh1->transportType_ = TransportType::BOOTSTRAP;
    IDataChannelPtr ch1(userCh1);

    const std::string ch2Id("id2");
    UserDataChannel* userCh2 = new UserDataChannel;
    userCh2->id_ = ch2Id;
    userCh2->channelType_ = ChannelType::HTTP;
    userCh2->transportType_ = TransportType::CONFIGURATION;
    IDataChannelPtr ch2(userCh2);

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);

    IServerInfoPtr serverInfo(new BootstrapServerInfo("localhost:80", "key"));

    channelManager.onServerUpdated(serverInfo);

    BOOST_CHECK(userCh1->server_);
    BOOST_CHECK(static_cast<BootstrapServerInfo*>(userCh1->server_.get())->getPort() == 80);

    BOOST_CHECK(!userCh2->server_);
}

class UserServerInfo : public IServerInfo {
public:
    virtual ChannelType getType() const {
        return ChannelType::HTTP_LP;
    }
};

BOOST_AUTO_TEST_CASE(ServerFailedTest)
{
    MockBootstrapManager BootstrapManager;
    KaaChannelManager channelManager(BootstrapManager);

    IServerInfoPtr fakeServer;
    BOOST_CHECK_THROW(channelManager.onServerFailed(fakeServer), KaaException);

    const std::string ch1Id("id1");
    UserDataChannel* userCh1 = new UserDataChannel;
    userCh1->id_ = ch1Id;
    userCh1->channelType_ = ChannelType::BOOTSTRAP;
    userCh1->transportType_ = TransportType::BOOTSTRAP;
    IDataChannelPtr ch1(userCh1);

    const std::string ch2Id("id2");
    UserDataChannel* userCh2 = new UserDataChannel;
    userCh2->id_ = ch2Id;
    userCh2->channelType_ = ChannelType::HTTP;
    userCh2->transportType_ = TransportType::CONFIGURATION;
    IDataChannelPtr ch2(userCh2);

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);

    BOOST_CHECK_THROW(channelManager.addChannel(ch2), KaaException);

//    IServerInfoPtr userServer(new UserServerInfo);
//    channelManager.onServerFailed(userServer);
//    BOOST_CHECK(userCh1->server_);
//    BOOST_CHECK(userCh2->server_);

    IServerInfoPtr serverInfo(new BootstrapServerInfo("localhost:80", "key"));
    channelManager.onServerFailed(serverInfo);

    BOOST_CHECK(userCh1->server_);
    /* Port 54 is hardcoded in KaaDefaults.cpp*/
    BOOST_CHECK(static_cast<BootstrapServerInfo*>(userCh1->server_.get())->getPort() == 54);

    channelManager.onServerFailed(serverInfo);
    /* Port 443 is hardcoded in KaaDefaults.cpp*/
    BOOST_CHECK(static_cast<BootstrapServerInfo*>(userCh1->server_.get())->getPort() == 443);
}

BOOST_AUTO_TEST_SUITE_END()

}

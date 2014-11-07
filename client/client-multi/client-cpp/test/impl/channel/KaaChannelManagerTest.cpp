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

#include "kaa/KaaDefaults.hpp"
#include "kaa/channel/KaaChannelManager.hpp"
#include "kaa/channel/server/HttpServerInfo.hpp"
#include "kaa/channel/server/KaaTcpServerInfo.hpp"
#include "kaa/common/exception/KaaException.hpp"

#include "headers/channel/MockDataChannel.hpp"
#include "headers/bootstrap/MockBootstrapManager.hpp"

namespace kaa {

class UserDataChannel : public MockDataChannel {
public:
    virtual const std::string& getId() const {
        return id_;
    }

    virtual ChannelType getChannelType() const {
        return channelType_;
    }

    virtual ServerType getServerType() const {
        return serverType_;
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
    TransportType     transportType_;
    ServerType        serverType_;

    IServerInfoPtr    server_;
};

class UserServerInfo : public IServerInfo {
public:
    virtual ChannelType getChannelType() const {
        return ChannelType::HTTP_LP;
    }

    virtual ServerType getServerType() const {
        return ServerType::OPERATIONS;
    }
};

BOOST_AUTO_TEST_SUITE(ChannelManagerTestSuite)

BOOST_AUTO_TEST_CASE(AddChannelTest)
{
    MockBootstrapManager BootstrapManager;
    KaaChannelManager channelManager(BootstrapManager, getServerInfoList());

    IDataChannelPtr fakeChannel(nullptr);
    BOOST_CHECK_THROW(channelManager.addChannel(fakeChannel), KaaException);

    const std::string ch1Id("id1");
    UserDataChannel* tmp = new UserDataChannel;
    tmp->id_ = ch1Id;
    tmp->channelType_ = ChannelType::HTTP;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch1(tmp);

    const std::string ch2Id("id2");
    tmp = new UserDataChannel;
    tmp->id_ = ch2Id;
    tmp->channelType_ = ChannelType::HTTP_LP;
    tmp->serverType_ = ServerType::OPERATIONS;
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
    KaaChannelManager channelManager(BootstrapManager, getServerInfoList());

    IDataChannelPtr fakeChannel(nullptr);
    BOOST_CHECK_THROW(channelManager.removeChannel(fakeChannel), KaaException);

    const std::string ch1Id("id1");
    UserDataChannel* tmp = new UserDataChannel;
    tmp->id_ = ch1Id;
    tmp->channelType_ = ChannelType::HTTP;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch1(tmp);

    const std::string ch2Id("id2");
    tmp = new UserDataChannel;
    tmp->id_ = ch2Id;
    tmp->channelType_ = ChannelType::HTTP_LP;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch2(tmp);

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);

    channelManager.removeChannel(ch2);

    BOOST_CHECK(channelManager.getChannels().size() == 1);
    BOOST_CHECK(channelManager.getChannels().front()->getChannelType() == ch1->getChannelType());
}

BOOST_AUTO_TEST_CASE(GetChannelBySomeCriteriaTest)
{
    MockBootstrapManager BootstrapManager;
    KaaChannelManager channelManager(BootstrapManager, getServerInfoList());

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
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch1(tmp);

    const std::string ch2Id("id2");
    tmp = new UserDataChannel;
    tmp->id_ = ch2Id;
    tmp->channelType_ = ChannelType::HTTP_LP;
    tmp->transportType_ = TransportType::NOTIFICATION;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch2(tmp);

    const std::string ch3Id("id3");
    tmp = new UserDataChannel;
    tmp->id_ = ch3Id;
    tmp->channelType_ = ChannelType::HTTP;
    tmp->transportType_ = TransportType::BOOTSTRAP;
    tmp->serverType_ = ServerType::BOOTSTRAP;
    IDataChannelPtr ch3(tmp);

    const std::string ch4Id("id4");
    tmp = new UserDataChannel;
    tmp->id_ = ch4Id;
    tmp->channelType_ = ChannelType::HTTP_LP;
    tmp->transportType_ = TransportType::EVENT;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch4(tmp);

    channelManager.addChannel(ch3);
    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);
    channelManager.addChannel(ch4);

    auto idCh = channelManager.getChannel(ch3->getId());
    BOOST_CHECK(idCh->getChannelType() == ch3->getChannelType());

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
    KaaChannelManager channelManager(BootstrapManager, getServerInfoList());

    const std::string ch1Id("id1");
    UserDataChannel* tmp = new UserDataChannel;
    tmp->id_ = ch1Id;
    tmp->channelType_ = ChannelType::HTTP;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch1(tmp);

    const std::string ch2Id("id2");
    tmp = new UserDataChannel;
    tmp->id_ = ch2Id;
    tmp->channelType_ = ChannelType::HTTP_LP;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch2(tmp);

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);

    channelManager.clearChannelList();

    BOOST_CHECK(channelManager.getChannels().empty());
}

BOOST_AUTO_TEST_CASE(ServerUpdateTest)
{
    MockBootstrapManager BootstrapManager;
    KaaChannelManager channelManager(BootstrapManager, getServerInfoList());

    IServerInfoPtr fakeServer;
    BOOST_CHECK_THROW(channelManager.onServerUpdated(fakeServer), KaaException);

    const std::string ch1Id("id1");
    UserDataChannel* userCh1 = new UserDataChannel;
    userCh1->id_ = ch1Id;
    userCh1->channelType_ = ChannelType::HTTP;
    userCh1->transportType_ = TransportType::BOOTSTRAP;
    userCh1->serverType_ = ServerType::BOOTSTRAP;
    IDataChannelPtr ch1(userCh1);

    const std::string ch2Id("id2");
    UserDataChannel* userCh2 = new UserDataChannel;
    userCh2->id_ = ch2Id;
    userCh2->channelType_ = ChannelType::HTTP;
    userCh2->transportType_ = TransportType::CONFIGURATION;
    userCh2->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch2(userCh2);

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);

    IServerInfoPtr serverInfo(new HttpServerInfo(ServerType::BOOTSTRAP, "localhost:80", "key"));

    channelManager.onServerUpdated(serverInfo);

    BOOST_CHECK(userCh1->server_);
    BOOST_CHECK(static_cast<HttpServerInfo*>(userCh1->server_.get())->getPort() == 80);

    BOOST_CHECK(!userCh2->server_);
}

BOOST_AUTO_TEST_CASE(ServerFailedTest)
{

    BootstrapServers servers = { IServerInfoPtr(new KaaTcpServerInfo(ServerType::BOOTSTRAP, "test", 80, "key"))
                               , IServerInfoPtr(new KaaTcpServerInfo(ServerType::BOOTSTRAP, "test", 54, "key"))
                               , IServerInfoPtr(new KaaTcpServerInfo(ServerType::BOOTSTRAP, "test", 443, "key"))};
    MockBootstrapManager BootstrapManager;
    KaaChannelManager channelManager(BootstrapManager, servers);

    IServerInfoPtr fakeServer;
    BOOST_CHECK_THROW(channelManager.onServerFailed(fakeServer), KaaException);

    const std::string ch1Id("id1");
    UserDataChannel* userCh1 = new UserDataChannel;
    userCh1->id_ = ch1Id;
    userCh1->channelType_ = ChannelType::KAATCP;
    userCh1->transportType_ = TransportType::BOOTSTRAP;
    userCh1->serverType_ = ServerType::BOOTSTRAP;
    IDataChannelPtr ch1(userCh1);

    const std::string ch2Id("id2");
    UserDataChannel* userCh2 = new UserDataChannel;
    userCh2->id_ = ch2Id;
    userCh2->channelType_ = ChannelType::HTTP;
    userCh2->transportType_ = TransportType::CONFIGURATION;
    userCh2->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch2(userCh2);

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);
    channelManager.addChannel(ch2);

    BOOST_CHECK(userCh1->server_);
    channelManager.onServerFailed(servers[0]);
    BOOST_CHECK(userCh1->server_);
    BOOST_CHECK(static_cast<HttpServerInfo*>(userCh1->server_.get())->getPort() == 54);

    channelManager.onServerFailed(servers[1]);
    BOOST_CHECK(static_cast<HttpServerInfo*>(userCh1->server_.get())->getPort() == 443);

    channelManager.onServerFailed(servers[2]);
    BOOST_CHECK(static_cast<HttpServerInfo*>(userCh1->server_.get())->getPort() == 80);
}

class ConfLogDataChannel : public UserDataChannel {
public:
    virtual const std::map<TransportType, ChannelDirection>& getSupportedTransportTypes() const {
        return SUPPORTED_TYPES;
    }
private:
    static const std::map<TransportType, ChannelDirection> SUPPORTED_TYPES;
};

const std::map<TransportType, ChannelDirection> ConfLogDataChannel::SUPPORTED_TYPES =
{
        { TransportType::CONFIGURATION, ChannelDirection::BIDIRECTIONAL },
        { TransportType::LOGGING, ChannelDirection::UP },
        { TransportType::EVENT, ChannelDirection::DOWN }
};

BOOST_AUTO_TEST_CASE(SetChannelTest)
{
    MockBootstrapManager BootstrapManager;
    KaaChannelManager channelManager(BootstrapManager, getServerInfoList());

    const std::string ch1Id("id1");
    ConfLogDataChannel* userCh1 = new ConfLogDataChannel;
    userCh1->id_ = ch1Id;
    userCh1->channelType_ = ChannelType::HTTP;
    userCh1->serverType_ = ServerType::OPERATIONS;

    const std::string ch2Id("id2");
    ConfLogDataChannel* userCh2 = new ConfLogDataChannel;
    userCh2->id_ = ch2Id;
    userCh2->channelType_ = ChannelType::HTTP;
    userCh2->serverType_ = ServerType::OPERATIONS;

    channelManager.addChannel(userCh2);
    BOOST_CHECK_EQUAL(userCh2, channelManager.getChannelByTransportType(TransportType::CONFIGURATION));
    BOOST_CHECK_EQUAL(userCh2, channelManager.getChannelByTransportType(TransportType::LOGGING));

    channelManager.setChannel(TransportType::LOGGING, userCh1);
    BOOST_CHECK_EQUAL(userCh2, channelManager.getChannelByTransportType(TransportType::CONFIGURATION));
    BOOST_CHECK_EQUAL(userCh1, channelManager.getChannelByTransportType(TransportType::LOGGING));

    channelManager.removeChannel("id1");
    BOOST_CHECK_EQUAL(userCh2, channelManager.getChannelByTransportType(TransportType::LOGGING));

    delete userCh1;
    delete userCh2;
}

BOOST_AUTO_TEST_CASE(SetChannelNegativeTest)
{
    MockBootstrapManager BootstrapManager;
    KaaChannelManager channelManager(BootstrapManager, getServerInfoList());

    const std::string ch1Id("id1");
    ConfLogDataChannel* userCh1 = new ConfLogDataChannel;
    userCh1->id_ = ch1Id;
    userCh1->channelType_ = ChannelType::HTTP;
    userCh1->serverType_ = ServerType::OPERATIONS;

    BOOST_CHECK_THROW(channelManager.setChannel(TransportType::EVENT, nullptr), KaaException);
    BOOST_CHECK_THROW(channelManager.setChannel(TransportType::EVENT, userCh1), KaaException);

    delete userCh1;
}

BOOST_AUTO_TEST_SUITE_END()

}

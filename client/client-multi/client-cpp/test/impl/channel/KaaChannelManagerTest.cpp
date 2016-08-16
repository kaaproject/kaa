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
#include <boost/asio/detail/socket_ops.hpp>

#include "kaa/KaaDefaults.hpp"
#include "kaa/channel/KaaChannelManager.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/failover/DefaultFailoverStrategy.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/logging/DefaultLogger.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"

#include "headers/channel/MockDataChannel.hpp"
#include "headers/bootstrap/MockBootstrapManager.hpp"
#include "headers/MockKaaClientStateStorage.hpp"

#include "kaa/channel/IPTransportInfo.hpp"
#include "kaa/channel/TransportProtocolIdConstants.hpp"

namespace kaa {

static KaaClientProperties properties;
static DefaultLogger tmp_logger(properties.getClientId());
static SimpleExecutorContext context;
static IKaaClientStateStoragePtr state(new MockKaaClientStateStorage);

class UserDataChannel : public MockDataChannel {
public:
    UserDataChannel() : isShutdown_(false), isPaused_(false)
                      , protocolId_(TransportProtocolId(1, 1))
                      , transportType_(TransportType::BOOTSTRAP)
                      , serverType_(ServerType::BOOTSTRAP) { }

    virtual const std::string& getId() const {
        return id_;
    }

    virtual TransportProtocolId getTransportProtocolId() const {
        return protocolId_;
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

    virtual void setServer(ITransportConnectionInfoPtr server) {
        server_.reset(new IPTransportInfo(server));
    }

    virtual void shutdown()
    {
        isShutdown_ = true;
    }

    virtual void pause()
    {
        isPaused_ = true;
    }

    virtual void resume()
    {
        isPaused_ = false;
    }

    virtual void setFailoverStrategy(IFailoverStrategyPtr strategy) {}

    bool isPaused() const { return isPaused_; }
    bool isShutdown() const { return isShutdown_; }

public:
    bool isShutdown_;
    bool isPaused_;

    TransportProtocolId protocolId_;
    std::string         id_;
    TransportType       transportType_;
    ServerType          serverType_;

    std::unique_ptr<IPTransportInfo> server_;
};

BOOST_AUTO_TEST_SUITE(ChannelManagerTestSuite)

BOOST_AUTO_TEST_CASE(AddChannelTest)
{
    MockBootstrapManager BootstrapManager;
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, getBootstrapServers(), clientContext, nullptr);

    IDataChannelPtr fakeChannel(nullptr);
    BOOST_CHECK_THROW(channelManager.addChannel(fakeChannel), KaaException);

    const std::string ch1Id("id1");
    UserDataChannel* tmp = new UserDataChannel;
    tmp->id_ = ch1Id;
    tmp->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch1(tmp);

    const std::string ch2Id("id2");
    tmp = new UserDataChannel;
    tmp->id_ = ch2Id;
    tmp->protocolId_ = TransportProtocolIdConstants::TCP_TRANSPORT_ID;
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
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, getBootstrapServers(), clientContext, nullptr);

    IDataChannelPtr fakeChannel(nullptr);
    BOOST_CHECK_THROW(channelManager.removeChannel(fakeChannel), KaaException);

    const std::string ch1Id("id1");
    UserDataChannel* tmp = new UserDataChannel;
    tmp->id_ = ch1Id;
    tmp->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch1(tmp);

    const std::string ch2Id("id2");
    tmp = new UserDataChannel;
    tmp->id_ = ch2Id;
    tmp->protocolId_ = TransportProtocolIdConstants::TCP_TRANSPORT_ID;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch2(tmp);

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);

    channelManager.removeChannel(ch2);

    BOOST_CHECK(channelManager.getChannels().size() == 1);
    BOOST_CHECK(channelManager.getChannels().front()->getTransportProtocolId() == ch1->getTransportProtocolId());
}

BOOST_AUTO_TEST_CASE(GetChannelBySomeCriteriaTest)
{
    MockBootstrapManager BootstrapManager;
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, getBootstrapServers(), clientContext, nullptr);

    auto configurationCh = channelManager.getChannelByTransportType(TransportType::CONFIGURATION);
    BOOST_CHECK(!configurationCh);

    auto fakeIdCh = channelManager.getChannel("fake");
    BOOST_CHECK(!fakeIdCh);

    const std::string ch1Id("id1");
    UserDataChannel* tmp = new UserDataChannel;
    tmp->id_ = ch1Id;
    tmp->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    tmp->transportType_ = TransportType::LOGGING;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch1(tmp);

    const std::string ch2Id("id2");
    tmp = new UserDataChannel;
    tmp->id_ = ch2Id;
    tmp->protocolId_ = TransportProtocolIdConstants::TCP_TRANSPORT_ID;
    tmp->transportType_ = TransportType::NOTIFICATION;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch2(tmp);

    const std::string ch3Id("id3");
    tmp = new UserDataChannel;
    tmp->id_ = ch3Id;
    tmp->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    tmp->transportType_ = TransportType::BOOTSTRAP;
    tmp->serverType_ = ServerType::BOOTSTRAP;
    IDataChannelPtr ch3(tmp);

    const std::string ch4Id("id4");
    tmp = new UserDataChannel;
    tmp->id_ = ch4Id;
    tmp->protocolId_ = TransportProtocolIdConstants::TCP_TRANSPORT_ID;
    tmp->transportType_ = TransportType::EVENT;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch4(tmp);

    channelManager.addChannel(ch3);
    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);
    channelManager.addChannel(ch4);

    auto idCh = channelManager.getChannel(ch3->getId());
    BOOST_CHECK(idCh->getTransportProtocolId() == ch3->getTransportProtocolId());

    auto transportTypeCh = channelManager.getChannelByTransportType(TransportType::NOTIFICATION);
    BOOST_CHECK(transportTypeCh->getId() == ch2->getId());
}

BOOST_AUTO_TEST_CASE(ClearChannelsTest)
{
    MockBootstrapManager BootstrapManager;
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, getBootstrapServers(), clientContext, nullptr);

    const std::string ch1Id("id1");
    UserDataChannel* tmp = new UserDataChannel;
    tmp->id_ = ch1Id;
    tmp->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch1(tmp);

    const std::string ch2Id("id2");
    tmp = new UserDataChannel;
    tmp->id_ = ch2Id;
    tmp->protocolId_ = TransportProtocolIdConstants::TCP_TRANSPORT_ID;
    tmp->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch2(tmp);

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);

    channelManager.clearChannelList();

    BOOST_CHECK(channelManager.getChannels().empty());
}

std::vector<uint8_t> serializeConnectionInfo(const std::string& publicKey
                                           , const std::string& host
                                           , const std::int32_t& port)
{
    std::vector<uint8_t> serializedData(3 * sizeof(std::int32_t) + publicKey.length() + host.length());

    auto *data = serializedData.data();

    std::int32_t networkOrder32 = boost::asio::detail::socket_ops::host_to_network_long(publicKey.length());
    memcpy(data, &networkOrder32, sizeof(std::int32_t));
    data += sizeof(std::int32_t);

    memcpy(data, publicKey.data(), publicKey.length());
    data += publicKey.length();

    networkOrder32 = boost::asio::detail::socket_ops::host_to_network_long(host.length());
    memcpy(data, &networkOrder32, sizeof(std::int32_t));
    data += sizeof(std::int32_t);

    memcpy(data, host.data(), host.length());
    data += host.length();

    networkOrder32 = boost::asio::detail::socket_ops::host_to_network_long(port);
    memcpy(data, &networkOrder32, sizeof(std::int32_t));

    return serializedData;
}

ITransportConnectionInfoPtr createTransportConnectionInfo(ServerType type
                                                        , const std::int32_t& accessPointId
                                                        , TransportProtocolId protocolId
                                                        , const std::vector<uint8_t>& connectionData)
{
    ProtocolMetaData metaData;
    metaData.accessPointId = accessPointId;
    metaData.protocolVersionInfo.id = protocolId.getId();
    metaData.protocolVersionInfo.version = protocolId.getVersion();
    metaData.connectionInfo = connectionData;

    ITransportConnectionInfoPtr info(new GenericTransportInfo(type, metaData));
    return info;
}

BOOST_AUTO_TEST_CASE(ServerUpdateTest)
{
    MockBootstrapManager BootstrapManager;
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, getBootstrapServers(), clientContext, nullptr);

    ITransportConnectionInfoPtr fakeServer;
    BOOST_CHECK_THROW(channelManager.onTransportConnectionInfoUpdated(fakeServer), KaaException);

    const std::string ch1Id("id1");
    UserDataChannel* userCh1 = new UserDataChannel;
    userCh1->id_ = ch1Id;
    userCh1->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    userCh1->transportType_ = TransportType::BOOTSTRAP;
    userCh1->serverType_ = ServerType::BOOTSTRAP;
    IDataChannelPtr ch1(userCh1);

    const std::string ch2Id("id2");
    UserDataChannel* userCh2 = new UserDataChannel;
    userCh2->id_ = ch2Id;
    userCh2->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    userCh2->transportType_ = TransportType::CONFIGURATION;
    userCh2->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch2(userCh2);

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);


    std::string publicKey("key");
    std::string host("host");
    std::int32_t port = 9888;

    ITransportConnectionInfoPtr serverInfo =
            createTransportConnectionInfo(ServerType::BOOTSTRAP
                                        , 0x111
                                        , TransportProtocolIdConstants::HTTP_TRANSPORT_ID
                                        , serializeConnectionInfo(publicKey
                                                                , host
                                                                , port));

    channelManager.onTransportConnectionInfoUpdated(serverInfo);

    BOOST_CHECK(userCh1->server_);
    BOOST_CHECK(userCh1->server_->getHost() == host);
    BOOST_CHECK(userCh1->server_->getPort() == port);

    BOOST_CHECK(!userCh2->server_);
}

BOOST_AUTO_TEST_CASE(ServerFailedTest)
{

    BootstrapServers servers = { createTransportConnectionInfo(ServerType::BOOTSTRAP
                                                             , 0x111
                                                             , TransportProtocolIdConstants::TCP_TRANSPORT_ID
                                                             , serializeConnectionInfo("key"
                                                                                    , "host"
                                                                                    , 80))
                               , createTransportConnectionInfo(ServerType::BOOTSTRAP
                                                             , 0x111
                                                             , TransportProtocolIdConstants::TCP_TRANSPORT_ID
                                                             , serializeConnectionInfo("key"
                                                                                     , "host"
                                                                                     , 54))
                               , createTransportConnectionInfo(ServerType::BOOTSTRAP
                                                             , 0x111
                                                             , TransportProtocolIdConstants::TCP_TRANSPORT_ID
                                                             , serializeConnectionInfo("key"
                                                                                     , "host"
                                                                                     , 443))
                                };
    MockBootstrapManager BootstrapManager;
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, servers, clientContext, nullptr);

    std::size_t testRetryPeriod = 1;
    channelManager.setFailoverStrategy(
                std::make_shared<DefaultFailoverStrategy>(clientContext, testRetryPeriod));

    const std::string ch1Id("id1");
    UserDataChannel* userCh1 = new UserDataChannel;
    userCh1->id_ = ch1Id;
    userCh1->protocolId_ = TransportProtocolIdConstants::TCP_TRANSPORT_ID;
    userCh1->transportType_ = TransportType::BOOTSTRAP;
    userCh1->serverType_ = ServerType::BOOTSTRAP;
    IDataChannelPtr ch1(userCh1);

    const std::string ch2Id("id2");
    UserDataChannel* userCh2 = new UserDataChannel;
    userCh2->id_ = ch2Id;
    userCh2->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    userCh2->transportType_ = TransportType::CONFIGURATION;
    userCh2->serverType_ = ServerType::OPERATIONS;
    IDataChannelPtr ch2(userCh2);

    channelManager.addChannel(ch1);
    channelManager.addChannel(ch2);
    channelManager.addChannel(ch2);

    BOOST_CHECK(userCh1->server_);
    BOOST_CHECK(userCh1->server_->getPort() == 80);

    channelManager.onServerFailed(servers[0], KaaFailoverReason::CURRENT_BOOTSTRAP_SERVER_NA);

    std::this_thread::sleep_for(std::chrono::seconds(testRetryPeriod * 2));
    BOOST_CHECK(userCh1->server_->getPort() == 54);

    channelManager.onServerFailed(servers[1], KaaFailoverReason::CURRENT_BOOTSTRAP_SERVER_NA);

    std::this_thread::sleep_for(std::chrono::seconds(testRetryPeriod * 2));
    BOOST_CHECK(userCh1->server_->getPort() == 443);

    channelManager.onServerFailed(servers[2], KaaFailoverReason::ALL_BOOTSTRAP_SERVERS_NA);

    std::this_thread::sleep_for(std::chrono::seconds(testRetryPeriod * 2));
    BOOST_CHECK(userCh1->server_->getPort() == 80);
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
    std::unique_ptr<ConfLogDataChannel> userCh1(new ConfLogDataChannel);
    std::unique_ptr<ConfLogDataChannel> userCh2(new ConfLogDataChannel);

    MockBootstrapManager BootstrapManager;
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, getBootstrapServers(), clientContext, nullptr);

    const std::string ch1Id("id1");
    userCh1->id_ = ch1Id;
    userCh1->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    userCh1->serverType_ = ServerType::OPERATIONS;

    const std::string ch2Id("id2");
    userCh2->id_ = ch2Id;
    userCh2->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    userCh2->serverType_ = ServerType::OPERATIONS;

    channelManager.addChannel(userCh2.get());
    BOOST_CHECK_EQUAL(userCh2.get(), channelManager.getChannelByTransportType(TransportType::CONFIGURATION));
    BOOST_CHECK_EQUAL(userCh2.get(), channelManager.getChannelByTransportType(TransportType::LOGGING));

    channelManager.setChannel(TransportType::LOGGING, userCh1.get());
    BOOST_CHECK_EQUAL(userCh2.get(), channelManager.getChannelByTransportType(TransportType::CONFIGURATION));
    BOOST_CHECK_EQUAL(userCh1.get(), channelManager.getChannelByTransportType(TransportType::LOGGING));

    channelManager.removeChannel("id1");
    BOOST_CHECK_EQUAL(userCh2.get(), channelManager.getChannelByTransportType(TransportType::LOGGING));
    BOOST_CHECK(userCh1->isShutdown());
}

BOOST_AUTO_TEST_CASE(SetChannelNegativeTest)
{
    MockBootstrapManager BootstrapManager;
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, getBootstrapServers(), clientContext, nullptr);

    const std::string ch1Id("id1");
    ConfLogDataChannel* userCh1 = new ConfLogDataChannel;
    userCh1->id_ = ch1Id;
    userCh1->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    userCh1->serverType_ = ServerType::OPERATIONS;

    BOOST_CHECK_THROW(channelManager.setChannel(TransportType::EVENT, nullptr), KaaException);
    BOOST_CHECK_THROW(channelManager.setChannel(TransportType::EVENT, userCh1), KaaException);

    delete userCh1;
}

BOOST_AUTO_TEST_CASE(ShutdownTest)
{
    MockBootstrapManager BootstrapManager;
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, getBootstrapServers(), clientContext, nullptr);

    const std::string ch1Id("id1");
    ConfLogDataChannel* userCh1 = new ConfLogDataChannel;
    userCh1->id_ = ch1Id;
    userCh1->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    userCh1->serverType_ = ServerType::OPERATIONS;

    channelManager.addChannel(userCh1);
    channelManager.shutdown();
    BOOST_CHECK(userCh1->isShutdown());

    delete userCh1;
}

BOOST_AUTO_TEST_CASE(PauseBeforeAddTest)
{
    std::unique_ptr<ConfLogDataChannel> userCh1(new ConfLogDataChannel);

    MockBootstrapManager BootstrapManager;
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, getBootstrapServers(), clientContext, nullptr);

    const std::string ch1Id("id1");

    userCh1->id_ = ch1Id;
    userCh1->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    userCh1->serverType_ = ServerType::OPERATIONS;

    channelManager.pause();
    channelManager.addChannel(userCh1.get());
    BOOST_CHECK(userCh1->isPaused());
}

BOOST_AUTO_TEST_CASE(PauseAfterAddTest)
{
    std::unique_ptr<ConfLogDataChannel> userCh1(new ConfLogDataChannel);

    MockBootstrapManager BootstrapManager;
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, getBootstrapServers(), clientContext, nullptr);

    const std::string ch1Id("id1");

    userCh1->id_ = ch1Id;
    userCh1->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    userCh1->serverType_ = ServerType::OPERATIONS;

    channelManager.addChannel(userCh1.get());
    channelManager.pause();
    BOOST_CHECK(userCh1->isPaused());
}

BOOST_AUTO_TEST_CASE(PauseBeforeSetTest)
{
    std::unique_ptr<ConfLogDataChannel> userCh1(new ConfLogDataChannel);

    MockBootstrapManager BootstrapManager;
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, getBootstrapServers(), clientContext, nullptr);

    const std::string ch1Id("id1");
    userCh1->id_ = ch1Id;
    userCh1->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    userCh1->serverType_ = ServerType::OPERATIONS;

    channelManager.pause();
    channelManager.setChannel(TransportType::CONFIGURATION, userCh1.get());
    BOOST_CHECK(userCh1->isPaused());
}

BOOST_AUTO_TEST_CASE(ResumetTest)
{
    std::unique_ptr<ConfLogDataChannel> userCh1(new ConfLogDataChannel);

    MockBootstrapManager BootstrapManager;
    KaaClientContext clientContext(properties, tmp_logger, context, state);
    KaaChannelManager channelManager(BootstrapManager, getBootstrapServers(), clientContext, nullptr);

    const std::string ch1Id("id1");
    userCh1->id_ = ch1Id;
    userCh1->protocolId_ = TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    userCh1->serverType_ = ServerType::OPERATIONS;

    channelManager.addChannel(userCh1.get());
    channelManager.pause();
    BOOST_CHECK(userCh1->isPaused());
    channelManager.resume();
    BOOST_CHECK(!userCh1->isPaused());
}


BOOST_AUTO_TEST_SUITE_END()

}
;

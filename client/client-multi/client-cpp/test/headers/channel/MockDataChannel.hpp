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

#ifndef MOCKDATACHANNEL_HPP_
#define MOCKDATACHANNEL_HPP_

#include "kaa/channel/ITransportConnectionInfo.hpp"

namespace kaa {

class MockTransportConnectionInfo : public ITransportConnectionInfo {
public:

    MockTransportConnectionInfo ():mockTransportPID(someMockMagicNumber_, someMockMagicNumber_) {}

    virtual ServerType getServerType() const { return mockServerType_; };

    virtual std::int32_t getAccessPointId() const { return someMockMagicNumber_; };

    virtual TransportProtocolId getTransportId() const { return mockTransportPID; };

    virtual const std::vector<std::uint8_t>& getConnectionInfo() const { return  mockConnectionInfo_; };

    virtual bool isFailedState() const { return false; };

    virtual void setFailedState() {};

    virtual void resetFailedState() {};
private:

    const std::int32_t someMockMagicNumber_ = 12345;
    std::vector<std::uint8_t> mockConnectionInfo_;
    const ServerType  mockServerType_ = ServerType::BOOTSTRAP;
    TransportProtocolId mockTransportPID;
};

class MockDataChannel: public IDataChannel {
public:
    virtual void sync(TransportType type) {}
    virtual void syncAll() {}
    virtual void syncAck(TransportType type) {}
    virtual const std::string& getId() const {
        static std::string id("id");
        return id;
    }

    virtual TransportProtocolId getTransportProtocolId() const {
        static TransportProtocolId protocolId(1, 1);
        return protocolId;
    }

    virtual ServerType getServerType() const {
        return ServerType::OPERATIONS;
    }

    virtual void setMultiplexer(IKaaDataMultiplexer *multiplexer) {}
    virtual void setDemultiplexer(IKaaDataDemultiplexer *demultiplexer) {}

    virtual void setFailoverStrategy(IFailoverStrategyPtr strategy){}
    virtual void setServer(ITransportConnectionInfoPtr server) {}

    virtual ITransportConnectionInfoPtr getServer() {
        return ITransportConnectionInfoPtr(new MockTransportConnectionInfo);
    }

    virtual const std::map<TransportType, ChannelDirection>& getSupportedTransportTypes() const {
        static std::map<TransportType, ChannelDirection> types =
                {
                        { TransportType::BOOTSTRAP, ChannelDirection::BIDIRECTIONAL },
                        { TransportType::PROFILE, ChannelDirection::BIDIRECTIONAL },
                        { TransportType::CONFIGURATION, ChannelDirection::BIDIRECTIONAL },
                        { TransportType::NOTIFICATION, ChannelDirection::BIDIRECTIONAL },
                        { TransportType::USER, ChannelDirection::BIDIRECTIONAL },
                        { TransportType::EVENT, ChannelDirection::BIDIRECTIONAL },
                        { TransportType::LOGGING, ChannelDirection::BIDIRECTIONAL }
                };
        return types;
    }

    virtual void shutdown() { }

    virtual void pause() { }

    virtual void resume() { }

    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker) {}
};

} /* namespace kaa */

#endif /* MOCKDATACHANNEL_HPP_ */

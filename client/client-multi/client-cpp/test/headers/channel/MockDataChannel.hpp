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

#ifndef MOCKDATACHANNEL_HPP_
#define MOCKDATACHANNEL_HPP_

namespace kaa {

class MockDataChannel: public IDataChannel {
public:
    virtual void sync(TransportType type) {}
    virtual void syncAll() {}

    virtual const std::string& getId() const {
        static std::string id("id");
        return id;
    }

    virtual ChannelType getType() const {
        return HTTP;
    }

    virtual void setMultiplexer(IKaaDataMultiplexer *multiplexer) {}
    virtual void setDemultiplexer(IKaaDataDemultiplexer *demultiplexer) {}

    virtual void setServer(IServerInfoPtr server) {}

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
};

} /* namespace kaa */

#endif /* MOCKDATACHANNEL_HPP_ */

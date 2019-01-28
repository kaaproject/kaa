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

#ifndef KAA_CHANNEL_GENERICTRANSPORTINFO_HPP_
#define KAA_CHANNEL_GENERICTRANSPORTINFO_HPP_

#include <botan/base64.h>

#include "ITransportConnectionInfo.hpp"

namespace kaa {


class GenericTransportInfo: public ITransportConnectionInfo
{
public:
    GenericTransportInfo(ServerType type, const ProtocolMetaData& metaData) :
        serverType_(type), accessPointId_(metaData.accessPointId)
      , protocolId_(metaData.protocolVersionInfo), connectionData_(metaData.connectionInfo), isFailedState_(false) {}

    GenericTransportInfo(ServerType type, const std::int32_t& accessPointId
        , const TransportProtocolId& protocolId, const std::vector<std::uint8_t>& connectionData)
        : serverType_(type), accessPointId_(accessPointId)
        , protocolId_(protocolId), connectionData_(connectionData), isFailedState_(false) {}

    virtual ServerType getServerType() const {
        return serverType_;
    }

    virtual std::int32_t getAccessPointId() const {
        return accessPointId_;
    }

    virtual TransportProtocolId getTransportId() const {
        return protocolId_;
    }

    virtual const std::vector<std::uint8_t>& getConnectionInfo() const {
        return connectionData_;
    }

    virtual bool isFailedState() const {
        return isFailedState_;
    }

    virtual void setFailedState() {
        isFailedState_ = true;
    }

    virtual void resetFailedState() {
        isFailedState_ = false;
    }

protected:
    ServerType                   serverType_;
    std::int32_t                 accessPointId_;
    TransportProtocolId          protocolId_;
    std::vector<std::uint8_t>    connectionData_;
    bool                         isFailedState_;
};

} /* namespace kaa */

#endif /* KAA_CHANNEL_GENERICTRANSPORTINFO_HPP_ */

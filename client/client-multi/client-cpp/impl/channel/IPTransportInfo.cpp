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

#include <sstream>

#include <boost/asio/detail/socket_ops.hpp>

#include "kaa/channel/IPTransportInfo.hpp"

#include "kaa/gen/EndpointGen.hpp"
#include "kaa/channel/GenericTransportInfo.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

IPTransportInfo::IPTransportInfo(ITransportConnectionInfoPtr connectionInfo)
    : GenericTransportInfo(connectionInfo->getServerType(), ProtocolMetaData())
{
    if (!connectionInfo || connectionInfo->getConnectionInfo().empty()) {
        throw KaaException("Bad connection data");
    }

    serverType_ = connectionInfo->getServerType();
    connectionData_ = connectionInfo->getConnectionInfo();
    accessPointId_ = connectionInfo->getAccessPointId();
    protocolId_ = connectionInfo->getTransportId();

    std::uint8_t *data = connectionData_.data();
    std::size_t publicKeyLength = boost::asio::detail::socket_ops::network_to_host_long(*((int32_t *)data));
    data += sizeof(std::int32_t);

    publicKey_ = PublicKey();
    publicKey_.assign(data, data+publicKeyLength);
    data += publicKeyLength;

    std::int32_t hostLength = boost::asio::detail::socket_ops::network_to_host_long(*((int32_t *)data));
    data += sizeof(std::int32_t);
    host_.assign(data, data + hostLength);
    data += hostLength;

    port_ = boost::asio::detail::socket_ops::network_to_host_long(*((int32_t *)data));

    std::ostringstream ss;
    ss << "http://" << host_ << ":" << port_;
    url_.assign(ss.str());

    if ((3 * sizeof(std::int32_t) + publicKeyLength + hostLength) > connectionData_.size()) {
        throw KaaException("Bad connection data");
    }
}

} /* namespace kaa */

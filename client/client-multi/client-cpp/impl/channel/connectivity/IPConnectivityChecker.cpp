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

#ifdef KAA_DEFAULT_CONNECTIVITY_CHECKER

#include "kaa/channel/connectivity/IPConnectivityChecker.hpp"

#include <string>
#include <sstream>
#include <cstdint>

#include <boost/asio/ip/tcp.hpp>
#include <boost/asio/connect.hpp>
#include <boost/asio/io_service.hpp>

#include "kaa/logging/Log.hpp"
#include "kaa/channel/IPTransportInfo.hpp"
#include "kaa/channel/connectivity/IPingServerStorage.hpp"
#include "kaa/channel/TransportProtocolIdConstants.hpp"

namespace kaa {

bool IPConnectivityChecker::checkConnectivity()
{
    try {
        ITransportConnectionInfoPtr server = serverStorage_.getPingServer();

        if (isIPServer(server)) {
            IPTransportInfo transportInfo(server);

            boost::asio::io_service io_service;
            boost::asio::ip::tcp::resolver resolver(io_service);

            std::ostringstream ss;
            ss << transportInfo.getPort();

            boost::asio::ip::tcp::resolver::query query(transportInfo.getHost(), ss.str()
                    , boost::asio::ip::resolver_query_base::numeric_service);
            const auto& ep = resolver.resolve(query);

            boost::asio::ip::tcp::socket sock_(io_service);
            boost::asio::connect(sock_, ep);
        }
        return true;
    } catch (std::exception& e) {}

    return false;
}

bool IPConnectivityChecker::isIPServer(ITransportConnectionInfoPtr serverConnectionInfo)
{
    return (serverConnectionInfo && (serverConnectionInfo->getTransportId() == TransportProtocolIdConstants::HTTP_TRANSPORT_ID ||
                                     serverConnectionInfo->getTransportId() == TransportProtocolIdConstants::TCP_TRANSPORT_ID));
}

} /* namespace kaa */

#endif

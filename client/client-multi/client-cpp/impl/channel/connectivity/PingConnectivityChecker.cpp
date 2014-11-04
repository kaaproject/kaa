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

#include "kaa/channel/connectivity/PingConnectivityChecker.hpp"

#ifdef KAA_DEFAULT_CONNECTIVITY_CHECKER

#include <string>
#include <sstream>
#include <cstdint>

#include <boost/asio/ip/tcp.hpp>
#include <boost/asio/connect.hpp>
#include <boost/asio/io_service.hpp>

#include "kaa/logging/Log.hpp"
#include "kaa/gen/BootstrapGen.hpp"
#include "kaa/channel/server/HttpServerInfo.hpp"
#include "kaa/channel/server/HttpLPServerInfo.hpp"
#include "kaa/channel/server/KaaTcpServerInfo.hpp"
#include "kaa/channel/connectivity/IPingServerStorage.hpp"

namespace kaa {

bool PingConnectivityChecker::checkConnectivity()
{
    bool is_connection_exits = false;

    try {
        boost::asio::io_service io_service;
        boost::asio::ip::tcp::resolver resolver(io_service);

        std::string host;
        std::uint16_t port;
        IServerInfoPtr si = serverStorage_.getPingServer();

        if (si->getServerType() == ServerType::BOOTSTRAP) {
            switch (si->getChannelType()) {
            case ChannelType::HTTP:
            {
                HttpServerInfo* i = static_cast<HttpServerInfo*>(si.get());
                host = i->getHost();
                port = i->getPort();
                break;
            }
            case ChannelType::HTTP_LP:
            {
                HttpLPServerInfo* i = static_cast<HttpLPServerInfo*>(si.get());
                host = i->getHost();
                port = i->getPort();
                break;
            }
            case ChannelType::KAATCP:
            {
                KaaTcpServerInfo* i = static_cast<KaaTcpServerInfo*>(si.get());
                host = i->getHost();
                port = i->getPort();
                break;
            }
            default:
                host = "www.google.com";
                port = 80;
                break;
            }
        } else {
            host = "www.google.com";
            port = 80;
        }

        std::ostringstream ss;
        ss << port;

        boost::asio::ip::tcp::resolver::query query(host, ss.str()
                , boost::asio::ip::resolver_query_base::numeric_service);
        const auto& ep = resolver.resolve(query);

        boost::asio::ip::tcp::socket sock_(io_service);
        boost::asio::connect(sock_, ep);
        is_connection_exits = true;

        KAA_LOG_INFO("Connection to the network exists");
    } catch (std::exception& e) {
        KAA_LOG_INFO(boost::format("Connection to the network has disappeared: %1%") % e.what());
    }

    return is_connection_exits;
}

} /* namespace kaa */

#endif

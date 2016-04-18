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

#include "kaa/channel/connectivity/PingConnectivityChecker.hpp"

#include <string>
#include <sstream>
#include <cstdint>

#include <boost/asio/ip/tcp.hpp>
#include <boost/asio/connect.hpp>
#include <boost/asio/io_service.hpp>

#include "kaa/logging/Log.hpp"

namespace kaa {

const std::string PingConnectivityChecker::DEFAULT_HOST = "www.google.com";

bool PingConnectivityChecker::checkConnectivity()
{
    try {
        boost::asio::io_service io_service;
        boost::asio::ip::tcp::resolver resolver(io_service);

        std::ostringstream ss;
        ss << port_;

        boost::asio::ip::tcp::resolver::query query(host_, ss.str()
                , boost::asio::ip::resolver_query_base::numeric_service);
        const auto& ep = resolver.resolve(query);

        boost::asio::ip::tcp::socket sock_(io_service);
        boost::asio::connect(sock_, ep);
        return true;
    } catch (std::exception& e) {}

    return false;
}

} /* namespace kaa */


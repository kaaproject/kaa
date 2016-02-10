/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#ifndef HTTPUTILS_HPP_
#define HTTPUTILS_HPP_

#include "kaa/KaaDefaults.hpp"

#include <cstdint>
#include <boost/noncopyable.hpp>
#include <boost/asio.hpp>

namespace kaa {

class HttpUtils : public boost::noncopyable {
public:
    static boost::asio::ip::tcp::endpoint getEndpoint(std::string host, std::uint16_t port)
    {
        char portStr[6];
#ifdef _WIN32
        _snprintf_s(portStr, 6, 6, "%u", port);
#else
        snprintf(portStr, 6, "%u", port);
#endif
        boost::asio::io_service io_service;
        boost::asio::ip::tcp::resolver resolver(io_service);
        boost::asio::ip::tcp::resolver::query query(host, portStr, boost::asio::ip::resolver_query_base::numeric_service);
        return *resolver.resolve(query);
    }

private:
    HttpUtils();
    ~HttpUtils();
};

}


#endif /* HTTPUTILS_HPP_ */

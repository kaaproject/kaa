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

#ifndef HTTPUTILS_HPP_
#define HTTPUTILS_HPP_

#include <cstdint>
#include <string>

#ifdef QNX_650_CPP11_TO_STRING_PATCH
#include <custom/string.h>
#endif

#include <boost/asio.hpp>

namespace kaa {

class HttpUtils {
public:
    static boost::asio::ip::tcp::endpoint resolveEndpoint(std::string host,
                                                          std::uint16_t port,
                                                          boost::system::error_code& errorCode)
    {
        boost::asio::io_service io_service;
        boost::asio::ip::tcp::resolver resolver(io_service);
        boost::asio::ip::tcp::resolver::query query(host,
                                                    std::to_string(port),
                                                    boost::asio::ip::resolver_query_base::numeric_service);

        auto endpointIt = resolver.resolve(query, errorCode);

        if (errorCode) {
            return boost::asio::ip::tcp::endpoint();
        }

        return *endpointIt;
    }
};

}


#endif /* HTTPUTILS_HPP_ */

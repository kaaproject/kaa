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

#ifndef HTTPCLIENT_HPP_
#define HTTPCLIENT_HPP_

#include "kaa/KaaDefaults.hpp"

#include "kaa/http/IHttpClient.hpp"
#include <boost/asio.hpp>

#include "kaa/KaaThread.hpp"

#include "kaa/IKaaClientContext.hpp"

namespace kaa {

class HttpClient : public IHttpClient
{
public:
    HttpClient(IKaaClientContext &context)
        : io_(), sock_(io_), context_(context)
    { }

    virtual std::shared_ptr<IHttpResponse> sendRequest(const IHttpRequest& request, EndpointConnectionInfo* connection = nullptr);
    virtual void closeConnection();

private:
    void checkError(const boost::system::error_code& code);
    void doSocketClose();

private:
    boost::asio::io_service io_;
    boost::asio::ip::tcp::socket sock_;

    KAA_MUTEX_DECLARE(httpClientGuard_);

    IKaaClientContext &context_;
};

}

#endif /* HTTPCLIENT_HPP_ */

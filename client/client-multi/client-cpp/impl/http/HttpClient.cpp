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

#include "kaa/http/HttpClient.hpp"

#if defined(KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL) || \
    defined(KAA_DEFAULT_OPERATION_HTTP_CHANNEL) || \
    defined(KAA_DEFAULT_LONG_POLL_CHANNEL)

#include "kaa/logging/Log.hpp"
#include "kaa/transport/TransportException.hpp"
#include "kaa/http/HttpUtils.hpp"
#include "kaa/http/HttpResponse.hpp"

namespace kaa {

void HttpClient::checkError(const boost::system::error_code& code)
{
    if (code && code != boost::asio::error::eof) {
        if (sock_.is_open()) {
            doSocketClose();
        }
        throw TransportException(code);
    }
}

std::shared_ptr<IHttpResponse> HttpClient::sendRequest(const IHttpRequest& request)
{
    KAA_MUTEX_LOCKING("httpClientGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(httpClientGuardLock, httpClientGuard_);
    KAA_MUTEX_LOCKED("httpClientGuard_");

    if (sock_.is_open()) {
        doSocketClose();
    }
    KAA_LOG_INFO(boost::format("Sending request to the server %1%:%2%") % request.getHost() % request.getPort());
    const auto& ep = HttpUtils::getEndpoint(request.getHost(), request.getPort());
    const auto& data = request.getRequestData();
    boost::system::error_code errorCode;
    sock_.open(ep.protocol(), errorCode);
    checkError(errorCode);
    sock_.connect(ep, errorCode);
    checkError(errorCode);
    boost::asio::write(sock_, boost::asio::buffer(data.data(), data.size()), errorCode);
    checkError(errorCode);
    std::ostringstream responseStream;
    boost::asio::streambuf responseBuf;
    while (boost::asio::read(sock_, responseBuf, boost::asio::transfer_at_least(1), errorCode)) {
        responseStream << &responseBuf;
    }
    checkError(errorCode);
    const std::string& responseStr = responseStream.str();
    KAA_LOG_INFO(boost::format("Response from server %1%:%2% successfully received") % request.getHost() % request.getPort());
    doSocketClose();
    return std::shared_ptr<IHttpResponse>(new HttpResponse(responseStr));
}

void HttpClient::closeConnection()
{
    doSocketClose();
}

void HttpClient::doSocketClose()
{
    KAA_LOG_INFO("Closing socket connection...");
    boost::system::error_code errorCode;
    sock_.shutdown(boost::asio::ip::tcp::socket::shutdown_both, errorCode);
    sock_.close(errorCode);
}

}

#endif

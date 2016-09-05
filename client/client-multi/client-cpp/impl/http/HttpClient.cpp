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
    if (!code) {
        return;
    }

    if (code == boost::asio::error::eof) {
        KAA_LOG_TRACE("Socket EOF ignored");
        return;
    }

    KAA_LOG_WARN(boost::format("Transport error occurred: %s") % code.message());

    doSocketClose();

    throw TransportException(code);
}

std::shared_ptr<IHttpResponse> HttpClient::sendRequest(const IHttpRequest& request, EndpointConnectionInfo* connection)
{
    KAA_MUTEX_LOCKING("httpClientGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(httpClientGuardLock, httpClientGuard_);
    KAA_MUTEX_LOCKED("httpClientGuard_");

    KAA_LOG_TRACE(boost::format("Sending request to %s:%d")
                                                % request.getHost()
                                                % request.getPort());

    boost::system::error_code errorCode;

    auto ep = HttpUtils::resolveEndpoint(request.getHost(), request.getPort(), errorCode);
    checkError(errorCode);

    sock_.open(ep.protocol(), errorCode);
    checkError(errorCode);

    sock_.connect(ep, errorCode);
    checkError(errorCode);

    if (connection != nullptr) {
        connection->endpointIp_ = sock_.local_endpoint().address().to_string();
        connection->serverIp_ = ep.address().to_string();
    }

    const auto& data = request.getRequestData();
    boost::asio::write(sock_, boost::asio::buffer(data.data(), data.size()), errorCode);
    checkError(errorCode);

    std::ostringstream responseStream;
    boost::asio::streambuf responseBuf;

    while (boost::asio::read(sock_, responseBuf, boost::asio::transfer_at_least(1), errorCode)) {
        responseStream << &responseBuf;
    }

    checkError(errorCode);

    KAA_LOG_INFO(boost::format("Received response from server %s:%d")
                                                        % request.getHost()
                                                        % request.getPort());

    doSocketClose();

    return std::make_shared<HttpResponse>(responseStream.str());
}

void HttpClient::closeConnection()
{
    doSocketClose();
}

void HttpClient::doSocketClose()
{
    if (!sock_.is_open()) {
        return;
    }

    // Boost doc says:
    // "For portable behaviour with respect to graceful closure of
    // a connected socket, call shutdown() before closing the socket."
    // http://www.boost.org/doc/libs/1_61_0/doc/html/boost_asio/reference/basic_socket/close/overload1.html

    boost::system::error_code shutdownErrorCode;
    sock_.shutdown(boost::asio::ip::tcp::socket::shutdown_both, shutdownErrorCode);

    boost::system::error_code closingErrorCode;
    sock_.close(closingErrorCode);

    KAA_LOG_DEBUG(boost::format("Socket closed: shutdown status '%d', closing status '%s'")
                                                                    % shutdownErrorCode.message()
                                                                    % closingErrorCode.message());
}

}

#endif

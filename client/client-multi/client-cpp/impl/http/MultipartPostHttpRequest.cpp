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

#include "kaa/http/MultipartPostHttpRequest.hpp"

#if defined(KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL) || \
    defined(KAA_DEFAULT_OPERATION_HTTP_CHANNEL) || \
    defined(KAA_DEFAULT_LONG_POLL_CHANNEL)

#include <sstream>
#include <cstdint>

#include "kaa/logging/Log.hpp"

namespace kaa {

const std::string MultipartPostHttpRequest::BOUNDARY = "----Sanj56fD843koI0";

MultipartPostHttpRequest::MultipartPostHttpRequest(const HttpUrl& url, IKaaClientContext &context) : url_(url),context_(context)
{

}

MultipartPostHttpRequest::~MultipartPostHttpRequest()
{

}

std::string MultipartPostHttpRequest::getHost() const
{
    return url_.getHost();
}

uint16_t MultipartPostHttpRequest::getPort() const
{
    return url_.getPort();
}

std::string MultipartPostHttpRequest::getRequestData() const
{
    std::ostringstream stream;
    stream << "POST " << url_.getUri() << " HTTP/1.1\r\n";

    KAA_LOG_TRACE(boost::format("Executing request POST %1% HTTP/1.1") % url_.getUri());

    stream << "Accept: */*\r\n";
    stream << "Content-Type: multipart/form-data; boundary=" << BOUNDARY << "\r\n";
    stream << "Host: " << url_.getHost() << "\r\n";
    for (auto it = headerFields_.begin(); it != headerFields_.end(); ++it) {
        stream << it->first << ": " << it->second << "\r\n";
    }
    stream << "Connection: Close\r\n";
    std::ostringstream bodyStream;
    for (auto it = bodyFields_.begin(); it != bodyFields_.end(); ++it) {
        bodyStream << "--" << BOUNDARY << "\r\n";
        bodyStream << "Content-Disposition: form-data; name=\"" << it->first << "\"\r\n\r\n";
        const std::vector<std::uint8_t>& body = it->second;
        bodyStream.write(reinterpret_cast<const char *>(body.data()), body.size());
        bodyStream << "\r\n";
    }
    bodyStream << "--" << BOUNDARY << "--\r\n\r\n";
    const std::string& body = bodyStream.str();
    stream << "Content-Length: " << body.length() << "\r\n";
    stream << "\r\n";
    if (body.length() > 0) {
        stream << body << "\r\n\r\n";
    }

    return stream.str();
}

void MultipartPostHttpRequest::setHeaderField(const std::string& name, const std::string& value)
{
    headerFields_.insert(std::make_pair(name, value));
}

void MultipartPostHttpRequest::removeHeaderField(const std::string& name)
{
    headerFields_.erase(name);
}

void MultipartPostHttpRequest::setBodyField(const std::string& name, const std::vector<std::uint8_t>& value)
{
    bodyFields_.insert(std::make_pair(name, value));
}

void MultipartPostHttpRequest::removeBodyField(const std::string& name)
{
    bodyFields_.erase(name);
}

}

#endif

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

#include "kaa/http/HttpResponse.hpp"

#if defined(KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL) || \
    defined(KAA_DEFAULT_OPERATION_HTTP_CHANNEL) || \
    defined(KAA_DEFAULT_LONG_POLL_CHANNEL)

#include <cstring>
#include <cstdlib>
#include <cstdint>
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

HttpResponse::HttpResponse(const char *data, std::size_t len) : statusCode_(0)
{
    if (data == nullptr || len < HTTP_VERSION_OFFSET + 5) {
        throw KaaException("Empty response was given");
    }
    parseResponse(data, len);
}

HttpResponse::HttpResponse(const std::string& data) : statusCode_(0)
{
    if (data.length() < HTTP_VERSION_OFFSET + 5) {
        throw KaaException("Empty response was given");
    }
    parseResponse(data.c_str(), data.length());
}

std::string HttpResponse::getHeaderField(const std::string& name) const
{
    auto it = header_.find(name);
    return it != header_.end() ? it->second : std::string();
}

SharedBody HttpResponse::getBody() const
{
    return body_;
}

int HttpResponse::getStatusCode() const
{
    return statusCode_;
}

void HttpResponse::parseResponse(const char *data, std::size_t len)
{
    const char *cursor = data;
    cursor += HTTP_VERSION_OFFSET;
    std::string code(cursor, 3);
    statusCode_ = static_cast<int>(std::strtol(code.c_str(), nullptr, 10));
    cursor = strstr(data, "\r\n") + 2;

    bool headerProcessed = false;
    while (!headerProcessed) {
        auto sep = strchr(cursor, ':');
        std::string name(cursor, sep - cursor);
        cursor = sep + 2;
        auto end = strstr(cursor, "\r\n");
        std::string value(cursor, end - cursor);
        cursor = end + 2;
        header_.insert(std::make_pair(name, value));
        if (strstr(cursor, "\r\n") == cursor) {
            cursor += 2;
            headerProcessed = true;
        }
    }
    auto it = header_.find("Content-Length");
    if (it != header_.end()) {
        auto len = std::strtol(it->second.c_str(), nullptr, 10);
        if (len > 0) {
            body_.first.reset(new std::uint8_t[len]);
            body_.second = len;
            memcpy(body_.first.get(), cursor, len);
        }
    }
}

}

#endif


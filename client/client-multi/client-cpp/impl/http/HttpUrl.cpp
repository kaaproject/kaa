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

#include "kaa/http/HttpUrl.hpp"


#include <cstring>
#include <cctype>
#include <cstdlib>

namespace kaa {

HttpUrl::HttpUrl(const std::string& url) : url_(url)
{
    if (!url.empty()) {
        parseUrl();
    }
}

HttpUrl::HttpUrl(const char *url) {
    if (url) {
        url_.assign(url);
        parseUrl();
    }
}

void HttpUrl::parseUrl()
{
    std::uint16_t default_port = HTTP_DEFAULT_PORT;
    auto cursor = url_.c_str();
    if (strncmp(cursor, "http", 4) == 0) {
        if (*(cursor + 4) == ':') {
            // Passing "http://"
            cursor += 7;
        } else if (*(cursor + 5) == ':') {
            // HTTPS URL
            // Passing "https://"
            cursor += 8;
            default_port = HTTPS_DEFAULT_PORT;
        }
    }
    const char *host_begin = cursor;
    const char *host_end   = nullptr;
    const char *port_begin = nullptr;
    const char *port_end   = nullptr;
    const char *uri_begin  = nullptr;
    auto c = cursor;
    for (; *c != '\0'; ++c) {
        if (*c == ':') {
            port_begin = c + 1;
            host_end = c;
        } else if (*c == '/' && uri_begin == nullptr) {
            if (port_begin != nullptr) {
                port_end = c;
            } else {
                host_end = c;
            }
            uri_begin = c;
        }
    }
    if (!host_end) {
        host_end = c;
    } else if (!port_end) {
        port_end = c;
    }
    if (host_begin) {
        host_.assign(host_begin, host_end);
    }
    if (port_begin) {
        std::string port_str = std::string(port_begin, port_end - port_begin);
        // TODO: check correctness
        port_ = static_cast<unsigned short>(std::strtoul(port_str.c_str(), nullptr, 10));
    } else {
        port_ = default_port;
    }
    if (uri_begin) {
        uri_.assign(uri_begin);
    } else {
        uri_ = "/";
    }
}

} /* namespace kaa */


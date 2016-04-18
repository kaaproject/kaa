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

#ifndef HTTPURL_HPP_
#define HTTPURL_HPP_

#include "kaa/KaaDefaults.hpp"

#include <string>
#include <cstdint>

namespace kaa {

class HttpUrl {
public:
    HttpUrl(const std::string& url);
    HttpUrl(const char *url);

    std::string getHost() const { return host_; }
    std::uint16_t getPort() const { return port_; }
    std::string getUri()  const { return uri_; }

private:
    static const std::uint16_t HTTP_DEFAULT_PORT  = 80;
    static const std::uint16_t HTTPS_DEFAULT_PORT = 443;

    void parseUrl();

private:
    std::string        url_;
    std::string        host_;
    std::uint16_t    port_;
    std::string        uri_;
};

} /* namespace kaa */


#endif /* HTTPURL_HPP_ */

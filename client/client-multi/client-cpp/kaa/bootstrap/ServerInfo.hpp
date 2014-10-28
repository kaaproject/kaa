/*
 * Copyright 2014 CyberVision, Inc.
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

#ifndef SERVERINFO_HPP_
#define SERVERINFO_HPP_

#include <string>
#include <cstdint>

#include <botan/botan.h>
#include <botan/base64.h>

#include "kaa/http/HttpUrl.hpp"

namespace kaa {

class ServerInfo {
public:
    ServerInfo() { }
    ServerInfo(const std::string& host, const Botan::MemoryVector<std::uint8_t>& publicKey)
            : host_(host), publicKey_(publicKey) { }
    ServerInfo(const std::string& host, const std::string& publicKey)
            : host_(host), publicKey_(Botan::base64_decode(publicKey)) { }
    ~ServerInfo() { };

    bool isValid() const { return !host_.empty(); }

    const std::string& getHost() const { return host_; }
    const Botan::MemoryVector<std::uint8_t>& getPublicKey() const { return publicKey_; }

    HttpUrl getUrl() const { return HttpUrl(host_); }

private:
    std::string host_;
    Botan::MemoryVector<std::uint8_t> publicKey_;
};

}


#endif /* SERVERINFO_HPP_ */

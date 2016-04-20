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

#ifndef PINGCONNECTIVITYCHECKER_HPP_
#define PINGCONNECTIVITYCHECKER_HPP_

#include <cstring>
#include <string>
#include <cstdint>

#include "kaa/channel/connectivity/IConnectivityChecker.hpp"

namespace kaa {

class PingConnectivityChecker: public IConnectivityChecker {
public:
    static const std::string DEFAULT_HOST;
    static const std::uint16_t DEFAULT_PORT = 80;

public:
    PingConnectivityChecker()
        : host_(DEFAULT_HOST), port_(DEFAULT_PORT) {}

    PingConnectivityChecker(const std::string& host, std::uint16_t port = DEFAULT_PORT)
        : host_(host), port_(port) {}

    virtual bool checkConnectivity();

private:
    const std::string host_;
    const std::uint16_t port_;
};

} /* namespace kaa */

#endif /* PINGCONNECTIVITYCHECKER_HPP_ */

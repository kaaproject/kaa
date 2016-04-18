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

#ifndef IP_TRANSPORT_INFO_HPP_
#define IP_TRANSPORT_INFO_HPP_

#include <string>

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/security/SecurityDefinitions.hpp"
#include "kaa/channel/GenericTransportInfo.hpp"

namespace kaa {

class IPTransportInfo : public GenericTransportInfo {
public:
    IPTransportInfo(ITransportConnectionInfoPtr connectionInfo);

    const std::string& getHost() const {
        return host_;
    }

    std::uint16_t getPort() const {
        return port_;
    }

    const std::string& getURL() const {
        return url_;
    }

    const PublicKey& getPublicKey() const {
        return publicKey_;
    }

private:
    std::string      host_;
    std::uint16_t    port_;
    std::string      url_;
    PublicKey        publicKey_;

};

} /* namespace kaa */

#endif /* IP_TRANSPORT_INFO_HPP_ */

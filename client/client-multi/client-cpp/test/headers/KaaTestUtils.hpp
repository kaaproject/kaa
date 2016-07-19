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


#ifndef KAATESTUTILS_HPP_
#define KAATESTUTILS_HPP_

#include <cstdint>
#include <string>
#include <vector>
#include <cstddef>

#include <kaa/channel/ServerType.hpp>
#include <kaa/channel/TransportProtocolId.hpp>
#include <kaa/channel/ITransportConnectionInfo.hpp>
#include <kaa/security/SecurityDefinitions.hpp>

namespace kaa {

class KaaTestUtils {
public:
    static std::vector<uint8_t> serializeConnectionInfo(const std::string& host,
                                                        std::int32_t port,
                                                        const PublicKey& publicKey);

    static ITransportConnectionInfoPtr createTransportConnectionInfo(ServerType type,
                                                                     std::int32_t accessPointId,
                                                                     TransportProtocolId protocolId,
                                                                     const std::vector<uint8_t>& connectionData);

    static KeyPair generateKeyPair(std::size_t size = 2018);
};

} /* namespace kaa */

#endif /* KAATESTUTILS_HPP_ */

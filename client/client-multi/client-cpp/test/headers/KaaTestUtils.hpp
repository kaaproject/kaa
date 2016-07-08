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

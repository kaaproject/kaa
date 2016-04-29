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

/**
 * NOTE: This is a auto-generated file. Do not edit it.
 */

#include "kaa/KaaDefaults.hpp"

#include <algorithm>
#include <cstdint>
#include <sstream>

#include "kaa/channel/GenericTransportInfo.hpp"

namespace kaa {

const char * const BUILD_VERSION = "0.7.1-SNAPSHOT";

const char * const BUILD_COMMIT_HASH = "";

const char * const SDK_TOKEN = "48461193020513321075";

const std::uint32_t POLLING_PERIOD_SECONDS = 5;

const char * const CLIENT_PUB_KEY_LOCATION = "key.public";

const char * const CLIENT_PRIV_KEY_LOCATION = "key.private";

const char * const CLIENT_STATUS_FILE_LOCATION = "kaa.status";

const char * const DEFAULT_USER_VERIFIER_TOKEN = "";

ITransportConnectionInfoPtr createTransportInfo(const std::int32_t& accessPointId
                                              , const std::int32_t& protocolId
                                              , const std::int32_t& protocolVersion
                                              , const std::string& encodedConnectionData)
{
    auto buffer = Botan::base64_decode(encodedConnectionData);
    ITransportConnectionInfoPtr connectionInfo(
            new GenericTransportInfo(ServerType::BOOTSTRAP
                                   , accessPointId
                                   , TransportProtocolId(protocolId, protocolVersion)
                                   , std::vector<std::uint8_t>(buffer.begin(), buffer.end())));

    return connectionInfo;
}

const BootstrapServers& getBootstrapServers()
{
    static BootstrapServers listOfServers;
    if (listOfServers.empty()) {
        listOfServers.push_back(createTransportInfo(0x95f7e40f, 0xfb9a3cf0, 1, "AAABJjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAInAdYDtJzx02MWRJmAzyhcsOpGZv55tQ5gmNfRM/9LyJpy9vkuSsxn7bTzks9bkptBsV05i0ms0eaOeMdSElUMAuUQL5lfWgG/StUKBtBaxWZclNlkPkvHKwrMKUWSGEjuaBme4Qk23WrpRVmFMerR62DJ557pXZfIicJ8n3Fety0ox4Qs2WwgbudAY3qfs5Md7d7lXoWNq9hxL92SHNluTEtmgFkb/PQGrWYnGy73kyUSrz4iZURF47cPt0cazHySxHl/3gxcOk1SuXKu2Jv6HGaH+AjHwKYNFiW6hLWU0Q3vUkmlPaEDJsGdRfQ7b1SZM31+ivF1Q2xfgADTBiVsCAwEAAQAAAAkxMC4yLjMuOTMAACah"));
listOfServers.push_back(createTransportInfo(0x95f7e40f, 0x56c8ff92, 1, "AAABJjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAInAdYDtJzx02MWRJmAzyhcsOpGZv55tQ5gmNfRM/9LyJpy9vkuSsxn7bTzks9bkptBsV05i0ms0eaOeMdSElUMAuUQL5lfWgG/StUKBtBaxWZclNlkPkvHKwrMKUWSGEjuaBme4Qk23WrpRVmFMerR62DJ557pXZfIicJ8n3Fety0ox4Qs2WwgbudAY3qfs5Md7d7lXoWNq9hxL92SHNluTEtmgFkb/PQGrWYnGy73kyUSrz4iZURF47cPt0cazHySxHl/3gxcOk1SuXKu2Jv6HGaH+AjHwKYNFiW6hLWU0Q3vUkmlPaEDJsGdRfQ7b1SZM31+ivF1Q2xfgADTBiVsCAwEAAQAAAAkxMC4yLjMuOTMAACag"));
;
        std::random_shuffle(listOfServers.begin(), listOfServers.end());
    }
    return listOfServers;
}

const Botan::secure_vector<std::uint8_t>& getDefaultConfigData()
{
    static const Botan::secure_vector<std::uint8_t> configData = Botan::base64_decode("JENPTkZJR1VSQVRJT04gREFUQQIAAQIDBAUGBwgJCgsMDQ4P");
    return configData;
}

HashDigest getPropertiesHash()
{
    std::ostringstream ss;

    ss << SDK_TOKEN;
    ss << POLLING_PERIOD_SECONDS;
    ss << CLIENT_PUB_KEY_LOCATION;
    ss << CLIENT_PRIV_KEY_LOCATION;
    ss << CLIENT_STATUS_FILE_LOCATION;

    for (const auto& server : getBootstrapServers()) {
        const auto& connectionInfo = server->getConnectionInfo();
        ss.write(reinterpret_cast<const char*>(connectionInfo.data()), connectionInfo.size());
    }

    ss.write(reinterpret_cast<const char*>(
            getDefaultConfigData().data()), getDefaultConfigData().size());

    return EndpointObjectHash(ss.str()).getHashDigest();
}

}

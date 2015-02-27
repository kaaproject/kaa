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

/**
 * NOTE: This is a auto-generated file. Do not edit it.
 */

#include "kaa/KaaDefaults.hpp"

#include <algorithm>
#include <cstdint>
#include <sstream>

#include "kaa/channel/GenericTransportInfo.hpp"

namespace kaa {

const char * const BUILD_VERSION = "0.6.3-SNAPSHOT";

const char * const BUILD_COMMIT_HASH = "2";

const char * const APPLICATION_TOKEN = "31442896109454287616";

const std::uint32_t PROFILE_VERSION = 2;

const std::uint32_t CONFIG_VERSION = 2;

const std::uint32_t SYSTEM_NF_VERSION = 1;

const std::uint32_t USER_NF_VERSION = 2;

const std::uint32_t LOG_SCHEMA_VERSION = 2;

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

const BootstrapServers& getBootstrapServers() {
    static BootstrapServers listOfServers = { createTransportInfo(0x4c22e496, 0xfb9a3cf0, 1, "AAABJjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJvTnE/W607EBl/4dA81Lo1HJcEbJRa24zIYqFxKRFCD5rhI35siAb9ZS5i8G0u3Kffz2YdB71WFut1q7c4xhvHf1LaMlu/hDz8G1vfqcHvV6VAsaJz7vcQ5oHqQhlIgv+1iI6A9z/4qNRe5sZ3h0kN3zdJk2rA/L/FVrfM36fNfK6cNDkXeD75mhGhgyXhrW0zkt8mHF9m1k9fBA5sarkwKNT0WP+TUY8oB6Rkr1dcdOYW4tuuR0dWxngtn1j2Oghm2DCHxx4FGse3IdQHIsIeMmcR5/JXPOCE1arqe0Pk6HYJ/jtSBqTvKqb8k+54RrvauyfD+V04/nWZulHpuNZMCAwEAAQAAAAwxOTIuMTY4Ljc3LjIAACah")
                                          , createTransportInfo(0x4c22e496, 0x56c8ff92, 1, "AAABJjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJvTnE/W607EBl/4dA81Lo1HJcEbJRa24zIYqFxKRFCD5rhI35siAb9ZS5i8G0u3Kffz2YdB71WFut1q7c4xhvHf1LaMlu/hDz8G1vfqcHvV6VAsaJz7vcQ5oHqQhlIgv+1iI6A9z/4qNRe5sZ3h0kN3zdJk2rA/L/FVrfM36fNfK6cNDkXeD75mhGhgyXhrW0zkt8mHF9m1k9fBA5sarkwKNT0WP+TUY8oB6Rkr1dcdOYW4tuuR0dWxngtn1j2Oghm2DCHxx4FGse3IdQHIsIeMmcR5/JXPOCE1arqe0Pk6HYJ/jtSBqTvKqb8k+54RrvauyfD+V04/nWZulHpuNZMCAwEAAQAAAAwxOTIuMTY4Ljc3LjIAACag") };
    std::random_shuffle(listOfServers.begin(), listOfServers.end());
    return listOfServers;
}

const Botan::SecureVector<std::uint8_t>& getDefaultConfigData() {
    static const Botan::SecureVector<std::uint8_t> configData = Botan::base64_decode("AgAAAABAPTA49ltAWLGYSuIAmDFgAA==");
    return configData;
}

const std::string& getDefaultConfigSchema() {
    static const std::string configSchema = "{\"items\":{\"name\":\"deltaT\",\"type\":\"record\",\"fields\":[{\"name\":\"delta\",\"type\":[{\"name\":\"testT\",\"type\":\"record\",\"addressable\":true,\"fields\":[{\"name\":\"testField1\",\"type\":[\"string\",{\"symbols\":[\"unchanged\"],\"name\":\"unchangedT\",\"type\":\"enum\",\"namespace\":\"org.kaaproject.configuration\"}],\"by_default\":\"\"},{\"optional\":true,\"name\":\"testField2\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"testRecordT\",\"namespace\":\"org.kaa.config\",\"fields\":[{\"name\":\"testField3\",\"type\":[\"int\",\"org.kaaproject.configuration.unchangedT\"]},{\"name\":\"__uuid\",\"type\":{\"name\":\"uuidT\",\"type\":\"fixed\",\"size\":16,\"namespace\":\"org.kaaproject.configuration\"}}]},\"org.kaaproject.configuration.unchangedT\"]},{\"name\":\"__uuid\",\"type\":\"org.kaaproject.configuration.uuidT\"}],\"namespace\":\"org.kaa.config\"},\"org.kaa.config.testRecordT\"]}],\"namespace\":\"org.kaaproject.configuration\"},\"type\":\"array\"}";
    return configSchema;
}

const EventClassFamilyVersionInfos& getEventClassFamilyVersionInfo() {
    static const EventClassFamilyVersionInfos versions = { {"test_event_family",1} };/* = {{"familyName1",1}, {"familyName2",3}};*/
    return versions;
}

SharedDataBuffer getPropertiesHash() {
    std::ostringstream ss;

    ss << APPLICATION_TOKEN;
    ss << PROFILE_VERSION;
    ss << CONFIG_VERSION;
    ss << SYSTEM_NF_VERSION;
    ss << USER_NF_VERSION;
    ss << LOG_SCHEMA_VERSION;
    ss << POLLING_PERIOD_SECONDS;
    ss << CLIENT_PUB_KEY_LOCATION;
    ss << CLIENT_PRIV_KEY_LOCATION;
    ss << CLIENT_STATUS_FILE_LOCATION;

    for (const auto& server : getBootstrapServers()) {
        const auto& connectionInfo = server->getConnectionInfo();
        ss.write(reinterpret_cast<const char*>(connectionInfo.data()), connectionInfo.size());
    }

    ss.write(reinterpret_cast<const char*>(
            getDefaultConfigData().begin()), getDefaultConfigData().size());
    ss << getDefaultConfigSchema();

    for (const auto& eventFamily : getEventClassFamilyVersionInfo()) {
        ss << eventFamily.first << eventFamily.second;
    }

    return EndpointObjectHash(ss.str()).getHash();
}

}

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

#include "kaa/KaaDefaults.hpp"

#include <algorithm>
#include <cstdint>
#include <sstream>

namespace kaa {

const char * const BUILD_VERSION = "0.6.1-SNAPSHOT";

const char * const BUILD_COMMIT_HASH = "N/A";

const char * const APPLICATION_TOKEN = "999739850901241";

const std::uint32_t PROFILE_VERSION = 1;

const std::uint32_t CONFIG_VERSION = 1;

const std::uint32_t SYSTEM_NF_VERSION = 1;

const std::uint32_t USER_NF_VERSION = 1;

const std::uint32_t LOG_SCHEMA_VERSION = 1;

const std::uint32_t POLLING_PERIOD_SECONDS = 5;

const char * const CLIENT_PUB_KEY_LOCATION = "key.public";

const char * const CLIENT_PRIV_KEY_LOCATION = "key.private";

const char * const CLIENT_STATUS_FILE_LOCATION = "kaa.status";

const BootstrapServers& getBootstrapServers() {
    /* Default value for unit test */
    static BootstrapServers listOfServers;
    std::random_shuffle(listOfServers.begin(), listOfServers.end());
    return listOfServers;
}

const Botan::SecureVector<std::uint8_t>& getDefaultConfigData() {
    static const Botan::SecureVector<std::uint8_t> configData = Botan::base64_decode("");
    return configData;
}

const std::string& getDefaultConfigSchema() {
    static const std::string configSchema = "";
    return configSchema;
}

const EventClassFamilyVersionInfos& getEventClassFamilyVersionInfo() {
    static const EventClassFamilyVersionInfos versions;/* = {{"familyName1",1}, {"familyName2",3}};*/
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

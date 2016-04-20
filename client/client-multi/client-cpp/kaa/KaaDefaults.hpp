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

#ifndef KAADEFAULTS_HPP_
#define KAADEFAULTS_HPP_

#include <map>
#include <list>
#include <string>

#include <botan/base64.h>
#include <cstdint>

#include "kaa/common/EndpointObjectHash.hpp"
#include "kaa/channel/ITransportConnectionInfo.hpp"

#define KAA_LOG_LEVEL_NONE        0
#define KAA_LOG_LEVEL_FATAL       1
#define KAA_LOG_LEVEL_ERROR       2
#define KAA_LOG_LEVEL_WARNING     3
#define KAA_LOG_LEVEL_INFO        4
#define KAA_LOG_LEVEL_DEBUG       5
#define KAA_LOG_LEVEL_TRACE       6
#define KAA_LOG_LEVEL_FINE_TRACE  7

#define KAA_LOG_LEVEL  KAA_MAX_LOG_LEVEL

namespace kaa {

/**
 * Base endpoint configuration
 */
extern const char * const BUILD_VERSION;
extern const char * const BUILD_COMMIT_HASH;

extern const char * const SDK_TOKEN;

extern const std::uint32_t POLLING_PERIOD_SECONDS;

extern const char * const CLIENT_PUB_KEY_LOCATION;
extern const char * const CLIENT_PRIV_KEY_LOCATION;
extern const char * const CLIENT_STATUS_FILE_LOCATION;

extern const char * const DEFAULT_USER_VERIFIER_TOKEN;



typedef std::vector<ITransportConnectionInfoPtr> BootstrapServers;
const BootstrapServers& getBootstrapServers();

const Botan::secure_vector<std::uint8_t>& getDefaultConfigData();

HashDigest getPropertiesHash();

}

#endif /* KAADEFAULTS_HPP_ */

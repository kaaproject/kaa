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

#ifndef KAA_ENDPOINT_CONNECTION_INFO_HPP_
#define KAA_ENDPOINT_CONNECTION_INFO_HPP_

#include <kaa/channel/ServerType.hpp>

namespace kaa {
/**
 * The structure represents connection information in readable format.
 */
struct EndpointConnectionInfo {
    std::string endpointIp_; /**< The ip address of the endpoint. */
    std::string serverIp_; /**< The ip address of the server */
    ServerType serverType_; /**< The type of server, see @c ServerType */

    EndpointConnectionInfo(const std::string& endpointIp, const std::string& serverIp, const ServerType serverType)
        : endpointIp_(endpointIp),
          serverIp_(serverIp),
          serverType_(serverType) {}
};

};

#endif /* KAA_ENDPOINT_CONNECTION_INFO_HPP_ */

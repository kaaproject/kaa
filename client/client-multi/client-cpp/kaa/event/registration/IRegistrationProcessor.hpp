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

#ifndef IREGISTRATIONPROCESSOR_HPP_
#define IREGISTRATIONPROCESSOR_HPP_

#include "kaa/KaaDefaults.hpp"


#include <string>
#include <vector>
#include <unordered_map>

#include "kaa/gen/EndpointGen.hpp"

namespace kaa {

typedef std::shared_ptr<UserAttachRequest> UserAttachRequestPtr;

class IRegistrationProcessor {
public:
    virtual UserAttachRequestPtr                           getUserAttachRequest() = 0;
    virtual std::unordered_map<std::int32_t, std::string>  getEndpointsToAttach() = 0;
    virtual std::unordered_map<std::int32_t, std::string>  getEndpointsToDetach() = 0;

    virtual void onUserAttach(const UserAttachResponse& response) = 0;

    virtual void onEndpointsAttach(const std::vector<EndpointAttachResponse>& endpoints) = 0;
    virtual void onEndpointsDetach(const std::vector<EndpointDetachResponse>& endpoints) = 0;

    virtual void onCurrentEndpointAttach(const UserAttachNotification& response) = 0;
    virtual void onCurrentEndpointDetach(const UserDetachNotification& response) = 0;

    virtual ~IRegistrationProcessor() {}
};

} /* namespace kaa */

#endif /* IREGISTRATIONPROCESSOR_HPP_ */

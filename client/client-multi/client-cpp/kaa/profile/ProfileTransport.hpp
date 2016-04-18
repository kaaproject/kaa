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

#ifndef DEFAULTPROFILETRANSPORT_HPP_
#define DEFAULTPROFILETRANSPORT_HPP_

#include <vector>

#include <cstdint>

#include "kaa/channel/transport/IProfileTransport.hpp"
#include "kaa/channel/transport/AbstractKaaTransport.hpp"
#include "kaa/security/SecurityDefinitions.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

class IKaaChannelManager;

class ProfileTransport: public AbstractKaaTransport<TransportType::PROFILE>,
                        public IProfileTransport {
public:
    ProfileTransport(IKaaChannelManager& channelManager,
                     const PublicKey& publicKey, IKaaClientContext &context);

    virtual void sync() { syncAll(); }

    virtual ProfileSyncRequestPtr createProfileRequest();

    virtual void onProfileResponse(const ProfileSyncResponse& response);

    virtual void setProfileManager(IProfileManager* manager)
    {
        if (manager) {
            profileManager_ = manager;
        }
    }

private:
    bool isProfileOutDated(const HashDigest& profileHash);

private:
    IProfileManager*               profileManager_;
    std::vector<std::uint8_t>      publicKey_;
};

} /* namespace kaa */

#endif /* DEFAULTPROFILETRANSPORT_HPP_ */

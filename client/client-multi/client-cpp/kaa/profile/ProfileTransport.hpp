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

#ifndef DEFAULTPROFILETRANSPORT_HPP_
#define DEFAULTPROFILETRANSPORT_HPP_

#include <vector>

#include <boost/cstdint.hpp>

#include <botan/botan.h>

#include "kaa/channel/transport/IProfileTransport.hpp"
#include "kaa/channel/transport/AbstractKaaTransport.hpp"

namespace kaa {

class IKaaChannelManager;

class ProfileTransport: public AbstractKaaTransport<TransportType::PROFILE>,
                        public IProfileTransport
{
public:
    ProfileTransport(IKaaChannelManager& channelManager,
                     const Botan::MemoryVector<boost::uint8_t>& publicKey);

    virtual void sync() {
        syncAll();
    }

    virtual ProfileSyncRequestPtr createProfileRequest();

    virtual void onProfileResponse(const ProfileSyncResponse& response);

    virtual void setProfileManager(IProfileManager* manager) {
        if (manager != nullptr) {
            profileManager_ = manager;
        }
    }

private:
    void populateEventFamilyVersions(EndpointVersionInfo::eventFamilyVersions_t& versions);
    bool isProfileOutDated(SharedDataBuffer profileHash);

private:
    IProfileManager*               profileManager_;
    std::vector<boost::uint8_t>    publicKey_;
};

} /* namespace kaa */

#endif /* DEFAULTPROFILETRANSPORT_HPP_ */

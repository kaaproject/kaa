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

#ifndef DEFAULTPROFILEMANAGER_HPP_
#define DEFAULTPROFILEMANAGER_HPP_

#include <memory>

#include "kaa/channel/transport/IProfileTransport.hpp"
#include "kaa/profile/IProfileManager.hpp"
#include "kaa/profile/IProfileContainer.hpp"

namespace kaa {

/**
 * Default profile manager
 * Responsible for the profile container management and ProfileListener creation
 */
class ProfileManager : public IProfileManager {
public:
    ProfileManager() : ProfileContainer_(ProfileContainerPtr()) { }

    /**
     * Sets profile container implemented by the user
     * @param container user-defined container
     */
    virtual void setProfileContainer(ProfileContainerPtr container);

    /**
     * Retrieves serialized profile container
     * @return serialized profile container
     */

    SharedDataBuffer getSerializedProfile()
    {
        return ProfileContainer_->getSerializedProfile();
    }

    void updateProfile()
    {
        SharedDataBuffer serializedProfile = getSerializedProfile();
        if (serializedProfile.first.get() && serializedProfile.second > 0) {
            transport_->sync();
        }
    }

    /**
     * Sets profile transport
     * @param Profile transport
     */
    virtual void setTransport(IProfileTransportPtr transport)
    {
        if (transport) {
            transport_ = transport;
        }
    }

private:
    IProfileTransportPtr            transport_;
    ProfileContainerPtr   ProfileContainer_;
};

inline void ProfileManager::setProfileContainer(ProfileContainerPtr container)
{
    if (container) {
        ProfileContainer_ = container;
    }
}

} /* namespace kaa */

#endif /* DEFAULTPROFILEMANAGER_HPP_ */

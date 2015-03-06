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
#include "kaa/profile/ISerializedProfileContainer.hpp"
#include "kaa/profile/SerializedProfileContainer.hpp"
#include "kaa/profile/ProfileListener.hpp"

namespace kaa {

/**
 * Default profile manager
 * Responsible for the profile container management and ProfileListener creation
 */
class ProfileManager : public IProfileManager {
public:
    ProfileManager() {
        serializedProfileContainer_.reset(new SerializedProfileContainer);
    }

    /**
     * Sets profile container implemented by the user
     * @param container user-defined container
     */
    virtual void setProfileContainer(ProfileContainerPtr container);

    /**
     * Retrieves serialized profile container
     * @return serialized profile container
     */
    ISerializedProfileContainerPtr getSerializedProfileContainer() {
        return ISerializedProfileContainerPtr(serializedProfileContainer_);
    }

    /**
     * Sets profile transport
     * @param Profile transport
     */
    virtual void setTransport(IProfileTransportPtr transport) {
        if (transport) {
            transport_ = transport;
        }
    }

private:
    IProfileTransportPtr            transport_;
    SerializedProfileContainerPtr   serializedProfileContainer_;
};

inline void ProfileManager::setProfileContainer(ProfileContainerPtr container)
{
    if (container) {
        ProfileListenerPtr listener(new ProfileListener(transport_));

        container->setProfileListener(listener);
        serializedProfileContainer_->setProfileContainer(container);
    }
}

} /* namespace kaa */

#endif /* DEFAULTPROFILEMANAGER_HPP_ */

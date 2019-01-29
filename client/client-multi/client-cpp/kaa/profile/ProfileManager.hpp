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

#ifndef DEFAULTPROFILEMANAGER_HPP_
#define DEFAULTPROFILEMANAGER_HPP_

#include <memory>

#include "kaa/profile/IProfileManager.hpp"
#include "kaa/profile/IProfileContainer.hpp"
#include "kaa/profile/DefaultProfileContainer.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/channel/transport/IProfileTransport.hpp"
#include "kaa/profile/gen/ProfileDefinitions.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

/**
 * Default profile manager
 * Responsible for the profile container management and ProfileListener creation
 */
class ProfileManager : public IProfileManager {
public:
    ProfileManager(IKaaClientContext &context) : profileContainer_(std::make_shared<DefaultProfileContainer>()), context_(context) { }

    /**
     * Sets profile container implemented by the user
     * @param container user-defined container
     */
    virtual void setProfileContainer(IProfileContainerPtr container)
    {
        if (container) {
            profileContainer_ = container;
        }
    }

    /**
     * Retrieves serialized profile
     *
     * @return byte array with serialized profile
     *
     */
    SharedDataBuffer getSerializedProfile()
    {
        static AvroByteArrayConverter<KaaProfile> avroConverter;

        if (profileContainer_) {
            return avroConverter.toByteArray(profileContainer_->getProfile());
        }
#if KAA_PROFILE_SCHEMA_VERSION > 0
        else {
            throw KaaException("Profile container is not set!");
        }
#endif

        return avroConverter.toByteArray(KaaProfile());
    }

    /**
     * Notifies server that profile has been updated.
     */
    void updateProfile()
    {
        SharedDataBuffer serializedProfile = getSerializedProfile();
        if ((serializedProfile.first.get() && serializedProfile.second > 0)
                || context_.getStatus().isProfileResyncNeeded()) {
            transport_->sync();
        }
    }

    virtual bool isInitialized()
    {
#if KAA_PROFILE_SCHEMA_VERSION > 0
        return profileContainer_.operator bool();
#else
        return true;
#endif
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
    IProfileContainerPtr     profileContainer_;
    IKaaClientContext                &context_;
};

} /* namespace kaa */

#endif /* DEFAULTPROFILEMANAGER_HPP_ */

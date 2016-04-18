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

#ifndef IPROFILETRANSPORT_HPP_
#define IPROFILETRANSPORT_HPP_

#include <memory>

#include "kaa/gen/EndpointGen.hpp"

namespace kaa {

class IProfileManager;

typedef std::shared_ptr<ProfileSyncRequest> ProfileSyncRequestPtr;

/**
 * Updates the Profile manager state.
 */
class IProfileTransport
{
public:

    /**
     * Creates a new Profile update request.
     *
     * @return new Profile update request.
     * @see ProfileSyncRequest
     *
     */
    virtual ProfileSyncRequestPtr createProfileRequest() = 0;

    /**
     * Updates the state of the Profile manager from the given response.
     *
     * @param response the response from the server.
     * @see ProfileSyncResponse
     *
     */
    virtual void onProfileResponse(const ProfileSyncResponse& response) = 0;

    /**
     * Sets the given Profile manager.
     *
     * @param manager the Profile manager to be set.
     * @see ProfileManager
     *
     */

    virtual void setProfileManager(IProfileManager* manager) = 0;

    /**
     * Sends the update request to the server.
     */
    virtual void sync() = 0;

    virtual ~IProfileTransport() {}
};

typedef std::shared_ptr<IProfileTransport> IProfileTransportPtr;

} /* namespace kaa */

#endif /* IPROFILETRANSPORT_HPP_ */

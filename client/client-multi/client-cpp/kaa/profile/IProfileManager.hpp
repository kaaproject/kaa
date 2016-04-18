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

#ifndef IPROFILEMANAGER_HPP_
#define IPROFILEMANAGER_HPP_

#include <boost/shared_ptr.hpp>

#include "kaa/profile/IProfileContainer.hpp"
#include "kaa/common/EndpointObjectHash.hpp"

namespace kaa {

/**
 * Interface for the profile manager
 * Responsible for the profile container management and @link IProfileListener @endlink creation
 */
class IProfileManager {
public:

    /**
     * Sets profile container implemented by the user.
     *
     * @param container User-defined container
     * @see AbstractProfileContainer
     *
     */
    virtual void setProfileContainer(IProfileContainerPtr container) = 0;

    /**
     * Notifies server that profile has been updated.
     */
    virtual void updateProfile() = 0;

    /**
     * Check if a profile container is set what is mandatory in case of a non-default profile.
     */
    virtual bool isInitialized() = 0;

    /**
     * Returns serialized profile
     */
    virtual SharedDataBuffer getSerializedProfile() = 0;

    virtual ~IProfileManager() {}
};

} /* namespace kaa */

#endif /* IPROFILEMANAGER_HPP_ */

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

#ifndef DEFAULT_PROFILE_LISTENER_HPP_
#define DEFAULT_PROFILE_LISTENER_HPP_

#include "kaa/profile/ProfileTransport.hpp"
#include "kaa/profile/IProfileListener.hpp"

namespace kaa {

/**
 * Default profile listener
 */
class ProfileListener : public IProfileListener {
public:
    ProfileListener(IProfileTransportPtr transport);

    /**
     * Called on each profile update
     * @param serializedProfile byte array with serialized profile
     */
    void onProfileUpdated(SharedDataBuffer serializedProfile);

private:
    std::shared_ptr<ProfileTransport> transport_;
};

} /* namespace kaa */

#endif /* DEFAULT_PROFILE_LISTENER_HPP_ */

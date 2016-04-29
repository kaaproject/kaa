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

#ifndef I_PROFILE_LISTENER_HPP_
#define I_PROFILE_LISTENER_HPP_

#include <memory>

#include "kaa/common/EndpointObjectHash.hpp"

namespace kaa {

class IProfileListener;
typedef std::shared_ptr<IProfileListener> ProfileListenerPtr;

/**
 * Interface for the listener of profile updates.
 *
 * DO NOT implement your own version.
 * For the correct operation of Kaa use only @link DefaultProfileListener @endlink.
 *
 */
class IProfileListener {
public:

    /**
     * Called on each profile update
     *
     * @param profile byte array with serialized profile
     *
     */
    virtual void onProfileUpdated(SharedDataBuffer profile) = 0;

    virtual ~IProfileListener() {}
};

} /* namespace kaa */

#endif /* I_PROFILE_LISTENER_HPP_ */

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

#ifndef IPROFILECONTAINER_HPP_
#define IPROFILECONTAINER_HPP_

#include "kaa/common/EndpointObjectHash.hpp"

namespace kaa {

class IProfileContainer;
typedef std::shared_ptr<IProfileContainer> ProfileContainerPtr;

/**
 * Interface for the profile container.
 * User-defined version should be inherited from @link AbstractProfileContainer @endlink
 */
class IProfileContainer {
public:

    /**
     * Retrieves serialized profile
     *
     * @return byte array with serialized profile
     *
     */
    virtual SharedDataBuffer                        getSerializedProfile() = 0;

    /**
     * Set Kaa profile listener @link IProfileListener @endlink for the container.
     * DO NOT use this API explicitly. When user sets his implementation
     * of the profile container, Kaa uses this method to inject its
     * own listener @link DefaultProfileListener @endlink.
     *
     * @param listener Listener that tracks profile updates
     */

    virtual ~IProfileContainer() {}
};

} /* namespace kaa */

#endif /* IPROFILECONTAINER_HPP_ */

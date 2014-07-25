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

#ifndef ISERIALIZEDPROFILECONTAINER_HPP_
#define ISERIALIZEDPROFILECONTAINER_HPP_

#include <boost/shared_ptr.hpp>

#include "kaa/common/EndpointObjectHash.hpp"

namespace kaa {

class ISerializedProfileContainer;
typedef boost::shared_ptr<ISerializedProfileContainer> ISerializedProfileContainerPtr;

/**
 * Container for the serialized profile
 */
class ISerializedProfileContainer {
public:
    /**
     * Retrieves serialized profile
     * @return byte array with serialized profile
     */
    virtual SharedDataBuffer getSerializedProfile() = 0;

    virtual ~ISerializedProfileContainer() {}
};

} /* namespace kaa */

#endif /* ISERIALIZEDPROFILECONTAINER_HPP_ */

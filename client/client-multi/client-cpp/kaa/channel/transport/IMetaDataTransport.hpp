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

#ifndef IMETADATATRANSPORT_HPP_
#define IMETADATATRANSPORT_HPP_

#include "kaa/gen/EndpointGen.hpp"
#include <boost/shared_ptr.hpp>

namespace kaa {

/**
 * Transport for general client's state.
 */
class IMetaDataTransport {
public:

    /**
     * Creates new Meta data request.
     *
     * @return new Meta data  request.
     *
     */
    virtual std::shared_ptr<SyncRequestMetaData> createSyncRequestMetaData() = 0;

    virtual ~IMetaDataTransport() {}
};

}  // namespace kaa


#endif /* IMETADATATRANSPORT_HPP_ */

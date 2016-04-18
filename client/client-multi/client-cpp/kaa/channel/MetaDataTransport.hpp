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

#ifndef METADATATRANSPORT_HPP_
#define METADATATRANSPORT_HPP_

#include <vector>
#include <algorithm>

#include "kaa/KaaDefaults.hpp"
#include "kaa/channel/transport/AbstractKaaTransport.hpp"
#include "kaa/channel/transport/IMetaDataTransport.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/common/EndpointObjectHash.hpp"

namespace kaa {

class MetaDataTransport : public IMetaDataTransport
{
public:
    MetaDataTransport(IKaaClientStateStoragePtr status, EndpointObjectHash &keyHash, long timeout)
        : clientStatus_(status), publicKeyHash_(keyHash), timeout_(timeout) {}

    std::shared_ptr<SyncRequestMetaData> createSyncRequestMetaData()
    {
        std::shared_ptr<SyncRequestMetaData> request(new SyncRequestMetaData);

        request->sdkToken = SDK_TOKEN;
        request->endpointPublicKeyHash.set_bytes(publicKeyHash_);
        request->profileHash.set_bytes(clientStatus_->getProfileHash());
        request->timeout.set_long(timeout_);

        return request;
    }

private:
    IKaaClientStateStoragePtr   clientStatus_;
    EndpointObjectHash          publicKeyHash_;
    long                        timeout_;
};

}  // namespace kaa


#endif /* METADATATRANSPORT_HPP_ */

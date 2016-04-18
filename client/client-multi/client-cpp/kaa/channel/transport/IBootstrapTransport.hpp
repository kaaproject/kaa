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

#ifndef IBOOTSTRAPTRANSPORT_HPP_
#define IBOOTSTRAPTRANSPORT_HPP_

#include <memory>

namespace kaa {

/**
 * Interface to implement module responsible for creation the Bootstrap sync request
 * and processing the response from the Bootstrap server.
 */
class IBootstrapTransport
{
public:

    /**
     * Creates Bootstrap sync request.
     *
     * @return The Bootstrap sync request.
     * @see BootstrapSyncRequest
     */
    virtual std::shared_ptr<BootstrapSyncRequest> createBootstrapSyncRequest() = 0;

    /**
     * Updates the state of the Bootstrap manager according the given response.
     *
     * @param response The Bootstrap server response.
     * @see BootstrapSyncResponse
     *
     */
    virtual void onBootstrapResponse(const BootstrapSyncResponse& response) = 0;

    virtual ~IBootstrapTransport() {}
};

}  // namespace kaa


#endif /* IBOOTSTRAPTRANSPORT_HPP_ */

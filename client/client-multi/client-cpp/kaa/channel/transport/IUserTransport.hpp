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

#ifndef IUSERTRANSPORT_HPP_
#define IUSERTRANSPORT_HPP_

#include "kaa/gen/EndpointGen.hpp"
#include <memory>

namespace kaa {

/**
 * @link IKaaTransport @endlink for the Endpoint service.
 * Updates the Endpoint manager state.
 */
class IUserTransport {
public:

    /**
     * Creates new User update request.
     *
     * @return new User update request.
     * @see UserSyncRequest
     *
     */
    virtual std::shared_ptr<UserSyncRequest> createUserRequest() = 0;

    /**
     * Updates the state of the Endpoint manager according to the given response.
     *
     * @param response the response from the server.
     * @see UserSyncResponse
     *
     */
    virtual void onUserResponse(const UserSyncResponse& response) = 0;

    virtual ~IUserTransport() {}
};

}  // namespace kaa



#endif /* IUSERTRANSPORT_HPP_ */

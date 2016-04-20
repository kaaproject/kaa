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

#ifndef IEVENTTRANSPORT_HPP_
#define IEVENTTRANSPORT_HPP_

#include "kaa/gen/EndpointGen.hpp"
#include <cstdint>
#include <memory>

namespace kaa {

/**
 * Updates the Event manager state.
 */
class IEventTransport {
public:

    /**
     * Creates the Event request.
     *
     * @return new Event request.
     * @see EventSyncRequest
     *
     */
    virtual std::shared_ptr<EventSyncRequest> createEventRequest(std::int32_t requestId) = 0;

    /**
     * Updates the state of the Event manager according to the given response.
     *
     * @param response the response from the server.
     * @see EventSyncResponse
     *
     */
    virtual void onEventResponse(const EventSyncResponse& response) = 0;

    virtual void onSyncResponseId(std::int32_t requestId) = 0;

    virtual ~IEventTransport() {}
};

}  // namespace kaa


#endif /* IEVENTTRANSPORT_HPP_ */

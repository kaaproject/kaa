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

#ifndef ILOGGINGTRANSPORT_HPP_
#define ILOGGINGTRANSPORT_HPP_

#include "kaa/gen/EndpointGen.hpp"
#include <memory>

namespace kaa {

/**
 * Used for sending logs to the remote server.
 */
class ILoggingTransport {
public:

    /**
     * Creates the Log request that consists of current log records.
     *
     * @return new Log request
     * @see LogSyncRequest
     */
    virtual std::shared_ptr<LogSyncRequest> createLogSyncRequest() = 0;

    /**
     * Updates the state of the Log collector according to the given response.
     *
     * @param response the response from the server
     * @see LogSyncResponse
     * @param deliveryTime The time point the log was delivered at
     */
    virtual void onLogSyncResponse(const LogSyncResponse& response, std::size_t deliveryTime) = 0;

    virtual ~ILoggingTransport() {}
};

}  // namespace kaa


#endif /* ILOGGINGTRANSPORT_HPP_ */

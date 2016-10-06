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

#ifndef LOGGINGTRANSPORT_HPP_
#define LOGGINGTRANSPORT_HPP_

#include "kaa/KaaDefaults.hpp"

#include "kaa/channel/transport/AbstractKaaTransport.hpp"
#include "kaa/channel/transport/ILoggingTransport.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

class ILogProcessor;

class LoggingTransport : public AbstractKaaTransport<TransportType::LOGGING>, public ILoggingTransport {
public:
    LoggingTransport(IKaaChannelManager &manager, ILogProcessor& logProcessor, IKaaClientContext &context);

    virtual void sync();

    virtual std::shared_ptr<LogSyncRequest> createLogSyncRequest();
    virtual void onLogSyncResponse(const LogSyncResponse& response, std::size_t deliveryTime);

private:
    ILogProcessor&   logProcessor_;
};

}  // namespace kaa

#endif /* LOGGINGTRANSPORT_HPP_ */

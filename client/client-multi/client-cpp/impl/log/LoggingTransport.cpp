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

#include "kaa/log/LoggingTransport.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/log/LogCollector.hpp"
#include "kaa/log/ILogProcessor.hpp"

namespace kaa {

LoggingTransport::LoggingTransport(IKaaChannelManager& manager, ILogProcessor& logProcessor_, IKaaClientContext &context)
    : AbstractKaaTransport(manager, context), logProcessor_(logProcessor_)
{
}

void LoggingTransport::sync()
{
    syncByType();
}

std::shared_ptr<LogSyncRequest> LoggingTransport::createLogSyncRequest()
{
    return logProcessor_.getLogUploadRequest();
}

void LoggingTransport::onLogSyncResponse(const LogSyncResponse& response, std::size_t deliveryTime)
{
    logProcessor_.onLogUploadResponse(response, deliveryTime);
}

}  // namespace kaa

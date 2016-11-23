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

#ifndef MOCKBOOTSTRAPMANAGER_HPP_
#define MOCKBOOTSTRAPMANAGER_HPP_

#include "kaa/bootstrap/IBootstrapManager.hpp"
#include "kaa/failover/IFailoverStrategy.hpp"

namespace kaa {

class MockBootstrapManager: public IBootstrapManager {
    virtual void receiveOperationsServerList() {}
    virtual void setFailoverStrategy(IFailoverStrategyPtr strategy) {}
    virtual void onOperationsServerFailed(const TransportProtocolId& protocolId,
                                          KaaFailoverReason reason = KaaFailoverReason::NO_CONNECTIVITY) {}

    virtual void useNextOperationsServerByAccessPointId(std::int32_t id) {}

    virtual void setTransport(IBootstrapTransport* transport) {}
    virtual void setChannelManager(IKaaChannelManager* manager) {}

    virtual void onServerListUpdated(const std::vector<ProtocolMetaData>& operationsServers) {}
};

} /* namespace kaa */

#endif /* MOCKBOOTSTRAPMANAGER_HPP_ */

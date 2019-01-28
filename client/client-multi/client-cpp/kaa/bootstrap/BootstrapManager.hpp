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

#ifndef BOOTSTRAPMANAGER_HPP_
#define BOOTSTRAPMANAGER_HPP_

#include <vector>

#include "kaa/KaaThread.hpp"
#include "kaa/bootstrap/IBootstrapManager.hpp"
#include "kaa/bootstrap/BootstrapTransport.hpp"
#include "kaa/channel/GenericTransportInfo.hpp"
#include "kaa/utils/KaaTimer.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

class IKaaClient;

class BootstrapManager : public IBootstrapManager, public boost::noncopyable {
public:
    BootstrapManager(IKaaClientContext& context, IKaaClient *client)
        : bootstrapTransport_(nullptr)
        , channelManager_(nullptr)
        , context_(context)
        , retryTimer_("BootstrapManager retryTimer")
        , client_(client)
    {

    }

    virtual void setFailoverStrategy(IFailoverStrategyPtr strategy);
    virtual void receiveOperationsServerList();
    virtual void onOperationsServerFailed(const TransportProtocolId& protocolId, KaaFailoverReason reason);
    virtual void useNextOperationsServerByAccessPointId(std::int32_t id);
    virtual void setTransport(IBootstrapTransport* transport);
    virtual void setChannelManager(IKaaChannelManager* manager);
    virtual void onServerListUpdated(const std::vector<ProtocolMetaData>& operationsServers);

private:
    typedef std::vector<ITransportConnectionInfoPtr> OperationsServers;

    OperationsServers getOPSByAccessPointId(std::int32_t id);
    void notifyChannelManangerAboutServer(const OperationsServers& servers);

    void onCurrentBootstrapServerFailed(KaaFailoverReason reason);

private:
    std::map<TransportProtocolId, OperationsServers > operationServers_;
    std::map<TransportProtocolId, OperationsServers::iterator > lastOperationsServers_;

    BootstrapTransport *bootstrapTransport_;
    IKaaChannelManager *channelManager_;

    IKaaClientContext& context_;

    IFailoverStrategyPtr failoverStrategy_;

    std::unique_ptr<std::int32_t> serverToApply;

    KaaTimer<void ()>        retryTimer_;

    // Temporary solution to stop app
    IKaaClient *client_;

    KAA_R_MUTEX_MUTABLE_DECLARE(guard_);
};

}



#endif /* BOOTSTRAPMANAGER_HPP_ */

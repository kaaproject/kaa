/*
 * Copyright 2014 CyberVision, Inc.
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

#include "kaa/bootstrap/IBootstrapManager.hpp"
#include "kaa/bootstrap/BootstrapTransport.hpp"
#include "kaa/gen/BootstrapGen.hpp"
#include "kaa/KaaThread.hpp"

namespace kaa {

class BootstrapManager : public IBootstrapManager, public boost::noncopyable {
public:
    BootstrapManager() : bootstrapTransport_(nullptr), channelManager_(nullptr) { }
    ~BootstrapManager() { }

    virtual void receiveOperationsServerList();
    virtual void useNextOperationsServer(ChannelType type);
    virtual void useNextOperationsServerByDnsName(const std::string& name);
    virtual void setTransport(IBootstrapTransport* transport);
    virtual void setChannelManager(IKaaChannelManager* manager);
    virtual void onServerListUpdated(const OperationsServerList& list);
    virtual const std::vector<OperationsServer>& getOperationsServerList();

private:

    const OperationsServer* getOPSByDnsName(const std::string& name);
    IServerInfoPtr          getServerInfoByChannel(const OperationsServer& server, const SupportedChannel& channel);
    IServerInfoPtr          getServerInfoByChannelType(const OperationsServer& server, ChannelType channelType);
    void                    notifyChannelManangerAboutServer(const OperationsServer& server);

    std::vector<OperationsServer> operationServerList_;
    std::map<ChannelType, std::vector<OperationsServer> > operationServers_;
    std::map<ChannelType, /*std::vector<OperationsServer>::const_iterator*/ std::size_t  > operationServersIterators_;

    BootstrapTransport *bootstrapTransport_;
    IKaaChannelManager *channelManager_;

    std::string serverToApply;

    KAA_R_MUTEX_MUTABLE_DECLARE(guard_);
};

}



#endif /* BOOTSTRAPMANAGER_HPP_ */

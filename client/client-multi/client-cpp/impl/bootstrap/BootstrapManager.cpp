/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include "kaa/bootstrap/BootstrapManager.hpp"

#include <ctime>
#include <cstdlib>
#include <cstdint>
#include <algorithm>
#include <random>

#include "kaa/KaaDefaults.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

void BootstrapManager::setFailoverStrategy(IFailoverStrategyPtr strategy) {
    failoverStrategy_ = strategy;
}

void BootstrapManager::receiveOperationsServerList()
{
    if (bootstrapTransport_ != nullptr) {
        bootstrapTransport_->sync();
    }
}

BootstrapManager::OperationsServers BootstrapManager::getOPSByAccessPointId(std::int32_t id)
{
    OperationsServers servers;

    for (const auto& serversByProtocolId : operationServers_) {
        for (const auto& server : serversByProtocolId.second) {
            if (server->getAccessPointId() == id) {
                servers.push_back(server);
            }
        }
    }

    return servers;
}

void BootstrapManager::useNextOperationsServer(const TransportProtocolId& protocolId)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);

    auto lastServerIt = lastOperationsServers_.find(protocolId);
    auto serverIt = operationServers_.find(protocolId);

    if (lastServerIt != lastOperationsServers_.end() && serverIt != operationServers_.end()) {
        OperationsServers::iterator nextOperationIterator = (lastServerIt->second)+1;
        if (nextOperationIterator != serverIt->second.end()) {
            KAA_LOG_INFO(boost::format("New server [%1%] will be user for %2%")
                                            % (*nextOperationIterator)->getAccessPointId()
                                            % LoggingUtils::TransportProtocolIdToString(protocolId));
            lastOperationsServers_[protocolId] = nextOperationIterator;
            if (channelManager_ != nullptr) {
                channelManager_->onTransportConnectionInfoUpdated(*(nextOperationIterator));
            } else {
                KAA_LOG_ERROR("Can not process server change. Channel manager was not specified");
            }
        } else {
            KAA_LOG_WARN(boost::format("Failed to find server for channel %1%.")
                                            % LoggingUtils::TransportProtocolIdToString(protocolId));

            FailoverStrategyDecision decision = failoverStrategy_->onFailover(Failover::OPERATION_SERVERS_NA);
            switch (decision.getAction()) {
                case FailoverStrategyAction::NOOP:
                    KAA_LOG_WARN("No operation is performed according to failover strategy decision.");
                    break;
                case FailoverStrategyAction::RETRY:
                {
                    std::size_t period = decision.getRetryPeriod();
                    KAA_LOG_WARN(boost::format("Attempt to receive operations server list will be made in %1% secs "
                            "according to failover strategy decision.") % period);
                    retryTimer_.stop();
                    retryTimer_.start(period, [&] { receiveOperationsServerList(); });
                    break;
                }
                case FailoverStrategyAction::STOP_APP:
                    KAA_LOG_WARN("Stopping application according to failover strategy decision!");
                    exit(EXIT_FAILURE);
                    break;
                default:
                    break;
            }
        }
    } else {
        throw KaaException("There are no available servers at the time");
    }
}

void BootstrapManager::useNextOperationsServerByAccessPointId(std::int32_t id)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);

    KAA_LOG_DEBUG(boost::format("Going to use new operations server: access_point=0x%X") % id);

    auto servers = getOPSByAccessPointId(id);
    if (servers.size() > 0) {
        notifyChannelManangerAboutServer(servers);
    } else {
        serverToApply.reset(new std::int32_t(id));
        bootstrapTransport_->sync();
    }
}

void BootstrapManager::setTransport(IBootstrapTransport* transport)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);
    bootstrapTransport_ = dynamic_cast<BootstrapTransport*>(transport);
    if (bootstrapTransport_ != nullptr) {
        return;
    }
    throw KaaException("Bad bootstrap transport");
}

void BootstrapManager::setChannelManager(IKaaChannelManager* manager)
{
    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);
    channelManager_ = manager;
}

void BootstrapManager::onServerListUpdated(const std::vector<ProtocolMetaData>& operationsServers)
{
    if (operationsServers.empty()) {
        KAA_LOG_WARN("Received empty operations server list");
        FailoverStrategyDecision decision = failoverStrategy_->onFailover(Failover::NO_OPERATION_SERVERS_RECEIVED);
        switch (decision.getAction()) {
        case FailoverStrategyAction::NOOP:
            KAA_LOG_WARN("No operation is performed according to failover strategy decision.");
            break;
        case FailoverStrategyAction::RETRY:
        {
            std::size_t period = decision.getRetryPeriod();
            KAA_LOG_WARN(boost::format("Attempt to receive operations server list will be made in %1% secs "
                                       "according to failover strategy decision.") % period);
            retryTimer_.stop();
            retryTimer_.start(period, [&] { receiveOperationsServerList(); });
            break;
        }
        case FailoverStrategyAction::USE_NEXT_BOOTSTRAP:
            KAA_LOG_WARN("Try next bootstrap server.");
            channelManager_->onServerFailed(channelManager_->getChannelByTransportType(TransportType::BOOTSTRAP)->getServer());
            break;
        case FailoverStrategyAction::STOP_APP:
            KAA_LOG_WARN("Stopping application according to failover strategy decision!");
            exit(EXIT_FAILURE);
            break;
        default:
            break;
        }
        return;
    }

    KAA_R_MUTEX_UNIQUE_DECLARE(lock, guard_);

    KAA_LOG_INFO(boost::format("Received %1% new operations servers") % operationsServers.size());

    lastOperationsServers_.clear();
    operationServers_.clear();

    for (const auto& serverMetaData : operationsServers) {
        ITransportConnectionInfoPtr connectionInfo(
                    new GenericTransportInfo(ServerType::OPERATIONS, serverMetaData));

        auto& servers = operationServers_[serverMetaData.protocolVersionInfo];
        servers.push_back(connectionInfo);
    }

    for (auto& transportSpecificServers : operationServers_) {
        std::shuffle (transportSpecificServers.second.begin()
                      , transportSpecificServers.second.end()
                      , std::default_random_engine(std::chrono::high_resolution_clock::now().time_since_epoch().count()));

        lastOperationsServers_[transportSpecificServers.first] =
                transportSpecificServers.second.begin();
    }

    if (serverToApply) {
        auto servers = getOPSByAccessPointId(*serverToApply);
        if (!servers.empty()) {
            KAA_LOG_DEBUG(boost::format("Found %1% servers by access point id %2%")
                          % servers.size() % *serverToApply);
            serverToApply.reset();
            notifyChannelManangerAboutServer(servers);
        }
    } else {
        for (const auto& transportSpecificServers : operationServers_) {
            channelManager_->onTransportConnectionInfoUpdated(transportSpecificServers.second.front());
        }
    }
}

void BootstrapManager::notifyChannelManangerAboutServer(const OperationsServers& servers)
{
    if (channelManager_ == nullptr) {
        KAA_LOG_ERROR("Channel manager was not specified");
        return;
    }

    for (const auto& connectionInfo : servers) {
        channelManager_->onTransportConnectionInfoUpdated(connectionInfo);
    }
}

}

